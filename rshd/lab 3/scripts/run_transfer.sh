#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/actions.sh"

host="${1:-pg-primary}"
from_id="${2:-1}"
to_id="${3:-2}"
amount="${4:-150.00}"
note="${5:-demo-transfer}"

action_run_transfer "$host" "$from_id" "$to_id" "$amount" "$note"
