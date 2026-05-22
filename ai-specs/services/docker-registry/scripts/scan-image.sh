#!/usr/bin/env bash
# =============================================================================
# scan-image.sh — Vulnerability Scanner Wrapper (Trivy)
#
# Scans a Docker image from the private registry for known CVEs.
# Exits with code 1 if vulnerabilities at or above the severity threshold
# are found — suitable for use in CI/CD gate checks.
#
# Usage:
#   bash scripts/scan-image.sh <image:tag> [severity] [format]
#
# Examples:
#   bash scripts/scan-image.sh registry.example.com/myapp:1.0
#   bash scripts/scan-image.sh registry.example.com/myapp:1.0 CRITICAL
#   bash scripts/scan-image.sh registry.example.com/myapp:1.0 HIGH,CRITICAL json
#
# Arguments:
#   image     Full image reference including registry host (required)
#   severity  Comma-separated list: LOW,MEDIUM,HIGH,CRITICAL (default: HIGH,CRITICAL)
#   format    Output format: table | json | sarif (default: table)
#
# CI/CD usage (fail pipeline on HIGH/CRITICAL):
#   bash scripts/scan-image.sh "$IMAGE" HIGH,CRITICAL && echo "Scan passed"
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/.."

# ── Arguments ─────────────────────────────────────────────────────────────────
IMAGE="${1:-}"
SEVERITY="${2:-HIGH,CRITICAL}"
FORMAT="${3:-table}"

if [ -z "${IMAGE}" ]; then
    echo "Usage: $0 <image:tag> [severity] [format]"
    echo "       $0 registry.example.com/myapp:1.0"
    echo "       $0 registry.example.com/myapp:1.0 HIGH,CRITICAL"
    echo "       $0 registry.example.com/myapp:1.0 HIGH,CRITICAL json"
    exit 1
fi

TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
echo "[${TIMESTAMP}] Scanning image: ${IMAGE}"
echo "[${TIMESTAMP}] Severity:       ${SEVERITY}"
echo "[${TIMESTAMP}] Format:         ${FORMAT}"
echo ""

# ── Run Trivy via Docker Compose (profile: tools) ─────────────────────────────
docker compose --profile tools run --rm trivy \
    image \
    --severity "${SEVERITY}" \
    --exit-code 1 \
    --format "${FORMAT}" \
    --timeout 10m0s \
    "${IMAGE}"

EXIT_CODE=$?

echo ""
if [ "${EXIT_CODE}" -eq 0 ]; then
    echo "[${TIMESTAMP}] RESULT: PASS — No ${SEVERITY} vulnerabilities found in ${IMAGE}"
elif [ "${EXIT_CODE}" -eq 1 ]; then
    echo "[${TIMESTAMP}] RESULT: FAIL — ${SEVERITY} vulnerabilities found in ${IMAGE}"
    echo "          Do not promote this image to production."
    exit 1
else
    echo "[${TIMESTAMP}] RESULT: ERROR — Trivy returned exit code ${EXIT_CODE}"
    exit "${EXIT_CODE}"
fi
