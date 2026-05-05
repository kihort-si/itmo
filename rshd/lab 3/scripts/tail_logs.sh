#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/actions.sh"

service="${1:-pg-primary}"
pattern="${2:-}"

action_show_logs "$service" "$pattern"
