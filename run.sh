#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ENV_FILE="$SCRIPT_DIR/.env"

read_env_value() {
  local key="$1"
  local default_value="$2"

  if [ -f "$ENV_FILE" ] && grep -Eq "^${key}=" "$ENV_FILE"; then
    grep -E "^${key}=" "$ENV_FILE" |
      head -n 1 |
      cut -d= -f2- |
      sed 's/^"//; s/"$//' |
      tr -d '\r'
  else
    echo "$default_value"
  fi
}

is_port_in_use() {
  local port="$1"

  # WSL (Windows)
  if grep -qi microsoft /proc/version 2>/dev/null; then
    if powershell.exe -NoProfile -NonInteractive -Command "
      \$connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
      if (\$connection) {
        exit 0
      } else {
        exit 1
      }
    " >/dev/null 2>&1; then
      return 0
    else
      return 1
    fi
  fi

  # Linux / macOS
  (: >"/dev/tcp/127.0.0.1/$port") >/dev/null 2>&1
}

find_free_port() {
  local port="$1"

  while is_port_in_use "$port"; do
    port=$((port + 1))
  done

  echo "$port"
}

write_env_file() {
  local server="$1"
  local db="$2"

  python3 - "$ENV_FILE" "$server" "$db" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
server = sys.argv[2]
db = sys.argv[3]

entries = {}

if path.exists():
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()

        if not stripped or stripped.startswith("#"):
            continue

        if "=" in line:
            key, value = line.split("=", 1)
            entries[key.strip()] = value.strip()

entries["SERVER_PORT"] = server
entries["DB_PORT"] = db

out = "\n".join(f"{k}={v}" for k, v in entries.items()) + "\n"

path.write_text(out, encoding="utf-8", newline="\n")
PY
}

docker info >/dev/null 2>&1 || {
  echo "Docker no esta disponible. Inicia Docker Desktop y vuelve a intentarlo." >&2
  exit 1
}

echo "Docker disponible. Continuando..."

if docker compose ps --services --status running | grep -q .; then
  echo "Docker Compose ya esta activo."
else
  echo "No hay contenedores activos. Buscando puertos libres..."

  SERVER_PORT_ENV="$(read_env_value SERVER_PORT 8080)"
  DB_PORT_ENV="$(read_env_value DB_PORT 5432)"

  SERVER_PORT="$(find_free_port "$SERVER_PORT_ENV")"
  DB_PORT="$(find_free_port "$DB_PORT_ENV")"

  write_env_file "$SERVER_PORT" "$DB_PORT"

  echo "Levantando Docker Compose..."
  docker compose up -d

  echo "Docker Compose iniciado."
fi

echo "Configuracion actual: SERVER_PORT=$(read_env_value SERVER_PORT 8080) DB_PORT=$(read_env_value DB_PORT 5432)"
