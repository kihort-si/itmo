#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/common.sh"

host="${1:-pg-primary}"
app_name="${2:-manual-client}"

require_docker

service_tty_shell "$CLIENT_SERVICE" "
  export PGPASSWORD='$LAB_PASS'
  export PGAPPNAME='$app_name'
  exec psql -h '$host' -U '$LAB_USER' -d '$LAB_DB'
"
