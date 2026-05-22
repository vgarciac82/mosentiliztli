#!/usr/bin/env bash
# =============================================================================
# backup-registry.sh — Backup Docker Registry Data
#
# Backs up two things:
#   1. Registry image data  — via MinIO mc mirror to a backup bucket
#   2. Config & auth files  — local tar.gz archive (no secrets in .env)
#
# Usage:
#   bash scripts/backup-registry.sh [--config-only]
#
# Prerequisites:
#   - Stack must be running (docker compose up -d)
#   - MinIO must be healthy
#
# Recommended schedule (add to /etc/cron.d/registry-backup):
#   0 3 * * *  root  /opt/registry/scripts/backup-registry.sh >> /var/log/registry-backup.log 2>&1
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/.."

# Load environment
set -a
# shellcheck disable=SC1091
source .env
set +a

TIMESTAMP=$(date '+%Y-%m-%d_%H%M%S')
BACKUP_DIR="backups"
CONFIG_BACKUP="${BACKUP_DIR}/config-backup-${TIMESTAMP}.tar.gz"
CONFIG_ONLY=false

if [[ "${1:-}" == "--config-only" ]]; then
    CONFIG_ONLY=true
fi

mkdir -p "${BACKUP_DIR}"

echo "[${TIMESTAMP}] ── Registry Backup ─────────────────────────────────────"

# ── 1. Config & Auth Backup (no secrets) ──────────────────────────────────────
echo "[${TIMESTAMP}] [INFO] Backing up config and auth files..."

tar -czf "${CONFIG_BACKUP}" \
    --exclude='.env' \
    --exclude='certs/*.key' \
    --exclude='certs/*.pem' \
    config/ \
    auth/htpasswd \
    docker-compose.yml \
    .env.example

echo "[${TIMESTAMP}] [INFO] Config backup saved: ${CONFIG_BACKUP}"

if [ "${CONFIG_ONLY}" = true ]; then
    echo "[${TIMESTAMP}] [INFO] --config-only mode: skipping MinIO data backup."
    exit 0
fi

# ── 2. MinIO Data Mirror ───────────────────────────────────────────────────────
MINIO_BUCKET="${MINIO_BUCKET:-docker-registry}"
BACKUP_BUCKET="${MINIO_BUCKET}-backup"

echo "[${TIMESTAMP}] [INFO] Mirroring registry data: ${MINIO_BUCKET} → ${BACKUP_BUCKET}"

# Check that minio-init service is not running (avoid conflicts)
docker compose run --rm --no-deps \
    -e MINIO_ROOT_USER="${MINIO_ROOT_USER}" \
    -e MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD}" \
    -e SRC_BUCKET="${MINIO_BUCKET}" \
    -e DST_BUCKET="${BACKUP_BUCKET}" \
    --entrypoint /bin/sh \
    minio-init -c "
        mc alias set local http://minio:9000 \$MINIO_ROOT_USER \$MINIO_ROOT_PASSWORD &&
        mc mb --ignore-existing local/\$DST_BUCKET &&
        mc anonymous set private local/\$DST_BUCKET &&
        mc mirror --overwrite local/\$SRC_BUCKET local/\$DST_BUCKET &&
        echo '[INFO] Mirror complete.'
    "

echo "[${TIMESTAMP}] [INFO] Data backup complete."

# ── 3. Prune old config backups (keep 30 days) ────────────────────────────────
echo "[${TIMESTAMP}] [INFO] Pruning config backups older than 30 days..."
find "${BACKUP_DIR}" -name "config-backup-*.tar.gz" -mtime +30 -delete
echo "[${TIMESTAMP}] [INFO] Pruning complete."

echo "[${TIMESTAMP}] ────────────────────────────────────────────────────────"
echo "[${TIMESTAMP}] [INFO] To restore MinIO data run:"
echo "              mc mirror local/${BACKUP_BUCKET} local/${MINIO_BUCKET}"
