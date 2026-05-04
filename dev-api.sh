#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "$0")" && pwd)"
API_DIR="$ROOT_DIR/apps/api"
START_PORT="${API_PORT_START:-8080}"
MAX_OFFSET="${API_PORT_MAX_OFFSET:-20}"

is_port_in_use() {
  local port="$1"
  lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
}

port="$START_PORT"
end_port=$((START_PORT + MAX_OFFSET))

while [ "$port" -le "$end_port" ]; do
  if ! is_port_in_use "$port"; then
    echo "$port" > "$ROOT_DIR/.api-port"
    echo "[dev:api] 使用端口: $port (已写入 .api-port)"
    exec "$API_DIR/mvnw" -f "$API_DIR/pom.xml" spring-boot:run \
      -Dspring-boot.run.profiles=local \
      -Dspring-boot.run.arguments="--server.port=$port"
  fi

  echo "[dev:api] 端口 $port 已被占用，尝试下一个端口..."
  port=$((port + 1))
done

echo "[dev:api] 从 $START_PORT 到 $end_port 的端口都被占用了，启动失败。" >&2
exit 1
