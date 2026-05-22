#!/usr/bin/env bash
# =============================================================================
# gc-registry.sh — Docker Registry Garbage Collection
#
# Removes unreferenced (dangling) blobs from storage to reclaim space.
# Safe to run while the registry is live (read operations continue).
# Push operations during GC may fail — schedule during low-traffic windows.
#
# Usage:
#   bash scripts/gc-registry.sh [--dry-run]
#
# Recommended schedule (add to /etc/cron.d/registry-gc):
#   0 2 * * 0  root  /opt/registry/scripts/gc-registry.sh >> /var/log/registry-gc.log 2>&1
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/.."

DRY_RUN=false
if [[ "${1:-}" == "--dry-run" ]]; then
    DRY_RUN=true
fi

TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
CONTAINER="registry-core"
CONFIG="/etc/docker/registry/config.yml"

echo "[${TIMESTAMP}] ── Garbage Collection ──────────────────────────────────"

# Verify registry container is running
if ! docker inspect --format '{{.State.Running}}' "${CONTAINER}" 2>/dev/null | grep -q true; then
    echo "[ERROR] Container '${CONTAINER}' is not running. Start the stack first."
    exit 1
fi

# Show storage use before GC
echo "[${TIMESTAMP}] [INFO] Requesting storage metrics before GC..."
BEFORE=$(docker exec "${CONTAINER}" \
    wget -qO- http://localhost:5001/metrics 2>/dev/null \
    | grep "registry_storage_bytes_total" \
    | awk '{print $2}' | head -1 || echo "unavailable")
echo "[${TIMESTAMP}] [INFO] Storage bytes before: ${BEFORE:-unavailable}"

if [ "${DRY_RUN}" = true ]; then
    echo "[${TIMESTAMP}] [DRY-RUN] Previewing blobs that would be removed..."
    docker exec "${CONTAINER}" \
        registry garbage-collect \
        --dry-run \
        --delete-untagged \
        "${CONFIG}"
    echo "[${TIMESTAMP}] [DRY-RUN] Done. No data was removed."
else
    echo "[${TIMESTAMP}] [INFO] Running garbage collection (delete-untagged enabled)..."
    docker exec "${CONTAINER}" \
        registry garbage-collect \
        --delete-untagged \
        "${CONFIG}"

    # Show storage use after GC
    AFTER=$(docker exec "${CONTAINER}" \
        wget -qO- http://localhost:5001/metrics 2>/dev/null \
        | grep "registry_storage_bytes_total" \
        | awk '{print $2}' | head -1 || echo "unavailable")
    echo "[${TIMESTAMP}] [INFO] Storage bytes after:  ${AFTER:-unavailable}"
    echo "[${TIMESTAMP}] [INFO] Garbage collection complete."
fi

echo "[${TIMESTAMP}] ────────────────────────────────────────────────────────"
