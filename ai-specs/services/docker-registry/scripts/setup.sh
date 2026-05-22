#!/usr/bin/env bash
# =============================================================================
# setup.sh — Bootstrap the Docker Private Registry
#
# Idempotent: safe to re-run; existing files are NOT overwritten.
#
# Steps performed:
#   1. Verify prerequisites (docker, docker compose v2, openssl)
#   2. Create required directories
#   3. Generate .env from .env.example with random secrets (if not present)
#   4. Generate self-signed TLS certificate (if not present)
#   5. Create auth/htpasswd with bcrypt-hashed admin credentials
#   6. Update nginx.conf server_name with REGISTRY_DOMAIN
#   7. Validate docker compose configuration
#   8. Print quick-start instructions
# =============================================================================
set -euo pipefail

# ── Colors ────────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }
section() { echo -e "\n${BLUE}══ $* ══${NC}"; }

# ── Working directory: always relative to this script ─────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/.."

# ─────────────────────────────────────────────────────────────────────────────
# 1. Prerequisites
# ─────────────────────────────────────────────────────────────────────────────
section "Checking prerequisites"

command -v docker   >/dev/null 2>&1 || error "docker not found. Install Docker Engine: https://docs.docker.com/engine/install/"
docker compose version >/dev/null 2>&1 || error "docker compose v2 not found. Upgrade Docker or install the plugin."
command -v openssl  >/dev/null 2>&1 || error "openssl not found. Install with: apt install openssl / yum install openssl"

info "Docker:         $(docker --version)"
info "Docker Compose: $(docker compose version)"
info "OpenSSL:        $(openssl version)"

# ─────────────────────────────────────────────────────────────────────────────
# 2. Directory structure
# ─────────────────────────────────────────────────────────────────────────────
section "Creating directory structure"

mkdir -p auth certs backups config/registry config/nginx config/prometheus scripts .ci
info "Directories ready."

# ─────────────────────────────────────────────────────────────────────────────
# 3. Generate .env
# ─────────────────────────────────────────────────────────────────────────────
section "Environment file"

if [ ! -f .env ]; then
    info "Generating .env with random secrets..."
    cp .env.example .env

    REGISTRY_SECRET=$(openssl rand -base64 32 | tr -d '\n/+=' | cut -c1-43)
    REGISTRY_PASS=$(openssl rand -base64 20 | tr -d '\n/+=' | cut -c1-20)
    MINIO_PASS=$(openssl rand -base64 32 | tr -d '\n/+=' | cut -c1-40)

    # Use a delimiter that won't appear in base64 output ('#')
    sed -i "s#REGISTRY_HTTP_SECRET=.*#REGISTRY_HTTP_SECRET=${REGISTRY_SECRET}#" .env
    sed -i "s#REGISTRY_ADMIN_PASSWORD=.*#REGISTRY_ADMIN_PASSWORD=${REGISTRY_PASS}#" .env
    sed -i "s#MINIO_ROOT_PASSWORD=.*#MINIO_ROOT_PASSWORD=${MINIO_PASS}#" .env

    warn "Generated .env — back up these credentials securely BEFORE deploying:"
    echo ""
    grep -E "REGISTRY_ADMIN_PASSWORD|MINIO_ROOT_PASSWORD" .env | sed 's/^/    /'
    echo ""
else
    info ".env already exists — skipping generation."
fi

# Load environment
set -a
# shellcheck disable=SC1091
source .env
set +a

: "${REGISTRY_DOMAIN:=registry.example.com}"
: "${REGISTRY_ADMIN_USER:=admin}"

# ─────────────────────────────────────────────────────────────────────────────
# 4. TLS Certificate
# ─────────────────────────────────────────────────────────────────────────────
section "TLS Certificate"

if [ ! -f certs/registry.crt ]; then
    info "Generating self-signed certificate for: ${REGISTRY_DOMAIN}"

    CERT_CNF=$(mktemp)
    cat > "${CERT_CNF}" << EOF
[req]
default_bits       = 4096
default_md         = sha256
prompt             = no
encrypt_key        = no
distinguished_name = dn
x509_extensions    = v3_req

[dn]
C  = ES
ST = State
L  = City
O  = Private Registry
CN = ${REGISTRY_DOMAIN}

[v3_req]
subjectAltName   = @alt_names
keyUsage         = critical,digitalSignature,keyEncipherment
extendedKeyUsage = serverAuth

[alt_names]
DNS.1 = ${REGISTRY_DOMAIN}
DNS.2 = localhost
IP.1  = 127.0.0.1
EOF

    openssl req -newkey rsa:4096 -nodes \
        -keyout certs/registry.key \
        -x509 -days 365 \
        -out certs/registry.crt \
        -config "${CERT_CNF}" \
        2>/dev/null

    rm -f "${CERT_CNF}"
    chmod 600 certs/registry.key
    chmod 644 certs/registry.crt

    info "Certificate created: certs/registry.crt (expires in 365 days)"
    warn "Self-signed cert — for production use Let's Encrypt:"
    warn "  certbot certonly --standalone -d ${REGISTRY_DOMAIN}"
    warn "  Then update certs/registry.{crt,key} and uncomment ssl_stapling in nginx.conf"
else
    info "TLS certificate already exists — skipping."
    # Warn if cert expires within 30 days
    if ! openssl x509 -checkend 2592000 -noout -in certs/registry.crt 2>/dev/null; then
        warn "Certificate expires within 30 days — consider renewing!"
    fi
fi

# ─────────────────────────────────────────────────────────────────────────────
# 5. htpasswd (bcrypt)
# ─────────────────────────────────────────────────────────────────────────────
section "Registry credentials (htpasswd)"

if [ ! -f auth/htpasswd ]; then
    info "Creating htpasswd for user: ${REGISTRY_ADMIN_USER}"

    docker run --rm httpd:alpine \
        htpasswd -Bbn "${REGISTRY_ADMIN_USER}" "${REGISTRY_ADMIN_PASSWORD}" \
        > auth/htpasswd

    chmod 600 auth/htpasswd
    info "auth/htpasswd created."
else
    info "auth/htpasswd already exists — skipping."
    info "To add a user: bash scripts/setup.sh --add-user <username>"
fi

# ─────────────────────────────────────────────────────────────────────────────
# 6. Update nginx.conf server_name
# ─────────────────────────────────────────────────────────────────────────────
section "nginx configuration"

if grep -q "registry.example.com" config/nginx/nginx.conf 2>/dev/null; then
    sed -i "s/registry\.example\.com/${REGISTRY_DOMAIN}/g" config/nginx/nginx.conf
    info "nginx.conf updated — server_name: ${REGISTRY_DOMAIN}"
else
    info "nginx.conf already configured."
fi

# ─────────────────────────────────────────────────────────────────────────────
# 7. Validate Docker Compose
# ─────────────────────────────────────────────────────────────────────────────
section "Validating Docker Compose configuration"

docker compose config >/dev/null && info "Docker Compose config: valid"

# ─────────────────────────────────────────────────────────────────────────────
# 8. Instructions
# ─────────────────────────────────────────────────────────────────────────────
section "Setup complete"

echo ""
echo -e "  ${GREEN}Next steps:${NC}"
echo ""
echo "  1. Start the stack:"
echo "       docker compose up -d"
echo ""
echo "  2. Health check:"
echo "       curl -k https://${REGISTRY_DOMAIN}/v2/"
echo "       # Expect: HTTP 401 (auth required) = registry is alive"
echo ""
echo "  3. Login:"
echo "       docker login ${REGISTRY_DOMAIN}"
echo "       Username: ${REGISTRY_ADMIN_USER}"
echo "       Password: (see REGISTRY_ADMIN_PASSWORD in .env)"
echo ""
echo "  4. Test push/pull:"
echo "       docker pull alpine"
echo "       docker tag  alpine ${REGISTRY_DOMAIN}/alpine:test"
echo "       docker push ${REGISTRY_DOMAIN}/alpine:test"
echo "       docker pull ${REGISTRY_DOMAIN}/alpine:test"
echo ""
echo "  5. For Docker clients on OTHER machines:"
echo "       scp certs/registry.crt user@other-host:/tmp/"
echo "       # On other host:"
echo "       sudo mkdir -p /etc/docker/certs.d/${REGISTRY_DOMAIN}"
echo "       sudo cp /tmp/registry.crt /etc/docker/certs.d/${REGISTRY_DOMAIN}/ca.crt"
echo "       sudo systemctl reload docker"
echo ""
echo "  6. Scan an image for vulnerabilities:"
echo "       bash scripts/scan-image.sh ${REGISTRY_DOMAIN}/alpine:test"
echo ""
echo -e "  ${YELLOW}Metrics (internal only):${NC}"
echo "       docker compose exec prometheus wget -qO- http://registry:5001/metrics | head -20"
echo ""
