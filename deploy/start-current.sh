#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

echo "Docker-only deployment mode is enabled."
echo "Delegating to deploy/start-current-docker.sh ..."
exec bash "${ROOT_DIR}/deploy/start-current-docker.sh"
