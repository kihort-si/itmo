#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/actions.sh"

action_reset_lab
action_start_containers
log "Initializing primary."
action_init_primary
log "Cloning standby from primary."
action_init_standby
action_show_standby_recovery_state
log "Loading demo schema and seed data."
action_load_demo
log "Current topology."
action_show_status
