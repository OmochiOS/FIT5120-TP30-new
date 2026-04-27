#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_NAME="amityspace-current"
SERVICE_NAME="amityspace_app"
HOST_PORT="3001"
INTERNAL_PORT="8080"
HEALTH_URL="http://127.0.0.1:${HOST_PORT}/actuator/health"
RELEASE_JAR="deploy/release/AmitySpace-0.0.1-SNAPSHOT.jar"
APP_IMAGE="amityspace/current:latest"
BASE_IMAGE="eclipse-temurin:21-jre-jammy"
DEFAULT_JAVA_OPTS="-Xms128m -Xmx512m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ActiveProcessorCount=1 -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true"

cd "$ROOT_DIR"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

phase() {
  printf '\n[%s] %s\n' "$1" "$2"
}

ensure_env_value() {
  local key="$1"
  local value="$2"

  if ! grep -q "^${key}=" .env; then
    printf '%s=%s\n' "$key" "$value" >> .env
  else
    sed -i "s/^${key}=.*/${key}=${value}/" .env
  fi
}

ensure_env_value_if_blank() {
  local key="$1"
  local value="$2"

  if ! grep -q "^${key}=" .env; then
    printf '%s=%s\n' "$key" "$value" >> .env
  elif grep -q "^${key}=$" .env; then
    sed -i "s|^${key}=.*|${key}=${value}|" .env
  fi
}

require_command docker
require_command curl

phase "1/5" "Preparing Docker deployment environment"

if [[ ! -f .env ]]; then
  cp .env.example .env
fi

if [[ ! -f "$RELEASE_JAR" ]]; then
  echo "Missing release jar: $RELEASE_JAR" >&2
  echo "Build it locally first, then upload the updated deploy/release folder." >&2
  exit 1
fi

ensure_env_value "AMITYSPACE_PORT" "$HOST_PORT"
ensure_env_value "AMITYSPACE_INTERNAL_PORT" "$INTERNAL_PORT"
ensure_env_value_if_blank "AMITYSPACE_JAVA_OPTS" "$DEFAULT_JAVA_OPTS"

if ! grep -q '^OPENWEATHER_API_KEY=' .env; then
  printf 'OPENWEATHER_API_KEY=\n' >> .env
fi

phase "2/5" "Preparing base runtime image"

if ! docker image inspect "$BASE_IMAGE" >/dev/null 2>&1; then
  echo "Base image is missing. Pulling ${BASE_IMAGE} first..."
  docker pull "$BASE_IMAGE"
  sleep 3
else
  echo "Base image already exists: ${BASE_IMAGE}"
fi

phase "3/5" "Building runtime image before cutover"
echo "Using classic Docker builder to keep memory usage more predictable on small hosts..."
DOCKER_BUILDKIT=0 COMPOSE_DOCKER_CLI_BUILD=0 docker build --pull=false --tag "$APP_IMAGE" -f Dockerfile .

phase "4/5" "Replacing running container"
docker compose -p "$PROJECT_NAME" down --remove-orphans || true
sleep 4
docker compose -p "$PROJECT_NAME" up -d --no-build --remove-orphans

phase "5/5" "Waiting for ${SERVICE_NAME} health check"
for _ in $(seq 1 60); do
  if curl -fsS "$HEALTH_URL" >/dev/null 2>&1; then
    echo
    echo "Deployment is up."
    docker compose -p "$PROJECT_NAME" ps
    exit 0
  fi

  printf '.'
  sleep 2
done

echo
echo "Deployment did not become healthy in time. Showing status and recent logs..." >&2
docker compose -p "$PROJECT_NAME" ps >&2 || true
docker images | grep 'amityspace/current' >&2 || true
docker compose -p "$PROJECT_NAME" logs --tail=150 >&2 || true
exit 1
