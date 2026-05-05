#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/actions.sh"

log "Primary error log excerpt before failover."
action_show_primary_error_logs
action_failover_to_standby
