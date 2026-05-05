#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

is_postgres_running() {
  local service=$1
  service_shell "$service" 'gosu postgres pg_ctl -D "$PGDATA" status >/dev/null 2>&1'
}

action_validate_environment() {
  if ! command -v docker >/dev/null 2>&1; then
    printf 'docker CLI is not installed or not found in PATH.\n' >&2
    return 1
  fi

  docker --version

  if ! docker compose version >/dev/null 2>&1; then
    printf 'docker compose is unavailable.\n' >&2
    return 1
  fi

  docker compose version
  require_docker
  docker info --format 'Docker server version: {{.ServerVersion}}'
}

action_validate_required_files() {
  local required_paths=(
    "docker-compose.yml"
    "Makefile"
    "README.md"
    "docs/lab-defense-guide.md"
    "scripts/common.sh"
    "scripts/actions.sh"
    "scripts/bootstrap.sh"
    "scripts/init_primary.sh"
    "scripts/init_standby.sh"
    "scripts/load_demo.sh"
    "scripts/fill_primary_disk.sh"
    "scripts/failover.sh"
    "scripts/recover_primary.sh"
    "scripts/switchback.sh"
    "scripts/show_status.sh"
    "scripts/show_activity.sh"
    "scripts/open_psql.sh"
    "scripts/run_transfer.sh"
    "sql/schema.sql"
    "sql/seed.sql"
    "sql/transfer.sql"
    "sql/checks.sql"
    "sql/show_activity.sql"
  )
  local missing=()
  local path

  for path in "${required_paths[@]}"; do
    if [[ ! -e "$PROJECT_ROOT/$path" ]]; then
      missing+=("$path")
    fi
  done

  if (( ${#missing[@]} > 0 )); then
    printf 'Missing required project files:\n' >&2
    printf '  - %s\n' "${missing[@]}" >&2
    return 1
  fi

  printf 'Required files are present.\n'
}

action_reset_lab() {
  require_docker
  ensure_artifact_dirs

  log "Resetting containers and volumes."
  compose down -v --remove-orphans >/dev/null 2>&1 || true
  rm -f "$PROJECT_ROOT"/artifacts/primary/logs/* \
        "$PROJECT_ROOT"/artifacts/primary/archive/* \
        "$PROJECT_ROOT"/artifacts/standby/logs/* \
        "$PROJECT_ROOT"/artifacts/standby/archive/* || true
}

action_start_containers() {
  require_docker
  ensure_artifact_dirs
  log "Starting containers."
  compose up -d
}

action_check_container_status() {
  require_docker
  compose ps
}

action_show_node_images() {
  require_docker

  local service container_id image status
  for service in "$PRIMARY_SERVICE" "$STANDBY_SERVICE" "$CLIENT_SERVICE"; do
    container_id="$(compose ps -q "$service")"
    if [[ -z "$container_id" ]]; then
      printf '%s container is not created.\n' "$service" >&2
      return 1
    fi
    image="$(docker inspect --format '{{.Config.Image}}' "$container_id")"
    status="$(docker inspect --format '{{.State.Status}}' "$container_id")"
    printf '%-12s image=%s status=%s\n' "$service" "$image" "$status"
  done
}

action_check_network_connectivity() {
  require_docker
  service_shell "$CLIENT_SERVICE" '
    getent hosts pg-primary
    getent hosts pg-standby
  '
}

action_check_database_connectivity() {
  require_docker
  service_shell "$CLIENT_SERVICE" "
    pg_isready -h pg-primary -p 5432 -d '$LAB_DB'
    pg_isready -h pg-standby -p 5432 -d '$LAB_DB'
  "
}

action_show_absent_external_tools() {
  require_docker
  service_shell "$PRIMARY_SERVICE" '
    for bin in patroni repmgr pgpool; do
      if command -v "$bin" >/dev/null 2>&1; then
        printf "%s: found at %s\n" "$bin" "$(command -v "$bin")"
      else
        printf "%s: not installed\n" "$bin"
      fi
    done
  '
}

action_init_primary() {
  require_docker
  ensure_artifact_dirs

  log "Preparing primary data directory."
  service_shell "$PRIMARY_SERVICE" '
    mkdir -p "$PGDATA" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    find "$PGDATA" -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
    rm -f /var/lib/postgresql/pglogs/postgresql.log
    chown -R postgres:postgres "$PGDATA" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    gosu postgres initdb -D "$PGDATA"
  '

  log "Writing primary PostgreSQL configuration."
  service_shell "$PRIMARY_SERVICE" '
    cat >> "$PGDATA/postgresql.conf" <<'"'"'EOF'"'"'

listen_addresses = '"'"'*'"'"'
password_encryption = '"'"'scram-sha-256'"'"'
wal_level = replica
max_wal_senders = 10
max_replication_slots = 10
wal_keep_size = '"'"'128MB'"'"'
hot_standby = on
archive_mode = on
archive_command = '"'"'test ! -f /var/lib/postgresql/archive/%f && cp %p /var/lib/postgresql/archive/%f'"'"'
full_page_writes = on
wal_log_hints = on
logging_collector = on
log_destination = '"'"'stderr'"'"'
log_directory = '"'"'/var/lib/postgresql/pglogs'"'"'
log_filename = '"'"'postgresql.log'"'"'
log_truncate_on_rotation = off
log_rotation_age = 0
log_rotation_size = 0
log_line_prefix = '"'"'%m [%p] %q%u@%d '"'"'
EOF
  '

  service_shell "$PRIMARY_SERVICE" '
    cat > "$PGDATA/pg_hba.conf" <<'"'"'EOF'"'"'
local   all             all                                     trust
host    all             all             0.0.0.0/0               scram-sha-256
host    all             all             ::/0                    scram-sha-256
host    replication     repl            0.0.0.0/0               scram-sha-256
host    replication     repl            ::/0                    scram-sha-256
EOF
    chown postgres:postgres "$PGDATA/postgresql.conf" "$PGDATA/pg_hba.conf"
  '

  log "Starting primary."
  start_postgres "$PRIMARY_SERVICE"

  log "Creating roles and database."
  admin_sql "$PRIMARY_SERVICE" postgres "
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$REPL_USER') THEN
    CREATE ROLE $REPL_USER WITH REPLICATION LOGIN PASSWORD '$REPL_PASS';
  END IF;
END
\$\$;
"

  admin_sql "$PRIMARY_SERVICE" postgres "
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$LAB_USER') THEN
    CREATE ROLE $LAB_USER WITH LOGIN PASSWORD '$LAB_PASS';
  END IF;
END
\$\$;
"

  if [[ "$(admin_query "$PRIMARY_SERVICE" postgres "SELECT 1 FROM pg_database WHERE datname = '$LAB_DB';")" != "1" ]]; then
    admin_sql "$PRIMARY_SERVICE" postgres "CREATE DATABASE $LAB_DB OWNER $LAB_USER;"
  fi

  admin_sql "$PRIMARY_SERVICE" "$LAB_DB" "GRANT ALL PRIVILEGES ON DATABASE $LAB_DB TO $LAB_USER;"

  log "Primary is ready."
}

action_show_primary_replication_config() {
  require_docker
  admin_sql "$PRIMARY_SERVICE" postgres "
SELECT name, setting
FROM pg_settings
WHERE name IN (
  'wal_level',
  'max_wal_senders',
  'max_replication_slots',
  'wal_keep_size',
  'hot_standby',
  'archive_mode',
  'archive_command',
  'wal_log_hints',
  'full_page_writes'
)
ORDER BY name;
"
}

action_show_recovery_state() {
  local service=$1
  require_docker
  admin_sql "$service" postgres "SELECT pg_is_in_recovery();"
}

action_init_standby() {
  require_docker

  if [[ "$(admin_query "$PRIMARY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "f" ]]; then
    printf 'Primary is not writable. Initialize or promote it first.\n' >&2
    return 1
  fi

  log "Rebuilding standby from primary via pg_basebackup."
  stop_postgres "$STANDBY_SERVICE" fast || true
  drop_inactive_replication_slots "$PRIMARY_SERVICE"
  drop_slot_if_exists "$PRIMARY_SERVICE" "standby_slot"

  service_shell "$STANDBY_SERVICE" "
    mkdir -p \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    find \"\$PGDATA\" -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
    rm -f /var/lib/postgresql/pglogs/postgresql.log
    chown -R postgres:postgres \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    export PGPASSWORD='$REPL_PASS'
    gosu postgres pg_basebackup \
      -h pg-primary \
      -p 5432 \
      -U '$REPL_USER' \
      -D \"\$PGDATA\" \
      -R \
      -c fast \
      -X stream \
      -C \
      -S standby_slot
    cat >> \"\$PGDATA/postgresql.auto.conf\" <<'EOF'
primary_conninfo = 'host=pg-primary port=5432 user=$REPL_USER password=$REPL_PASS application_name=pg-standby'
primary_slot_name = 'standby_slot'
EOF
    chown postgres:postgres \"\$PGDATA/postgresql.auto.conf\"
  "

  start_postgres "$STANDBY_SERVICE"
  wait_for_recovery_state "$STANDBY_SERVICE" "t"
}

action_show_standby_bootstrap_artifacts() {
  require_docker
  service_shell "$STANDBY_SERVICE" '
    ls -1 "$PGDATA/standby.signal" "$PGDATA/postgresql.auto.conf"
  '
}

action_show_standby_recovery_state() {
  require_docker
  admin_sql "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();"
  admin_sql "$STANDBY_SERVICE" postgres "
SELECT status, receive_start_lsn, written_lsn, flushed_lsn, latest_end_lsn, slot_name
FROM pg_stat_wal_receiver;
"
}

action_load_demo() {
  require_docker

  log "Creating demo schema on primary."
  client_file "pg-primary" "/workspace/sql/schema.sql"

  log "Loading demo rows on primary."
  client_file "pg-primary" "/workspace/sql/seed.sql"

  log "Waiting until standby sees the seeded rows."
  local primary_count standby_count
  for _ in $(seq 1 60); do
    primary_count="$(client_query "pg-primary" "SELECT count(*) FROM transfers;")"
    standby_count="$(client_query "pg-standby" "SELECT count(*) FROM transfers;")"
    if [[ "$primary_count" == "$standby_count" ]]; then
      break
    fi
    sleep 1
  done

  primary_count="$(client_query "pg-primary" "SELECT count(*) FROM transfers;")"
  standby_count="$(client_query "pg-standby" "SELECT count(*) FROM transfers;")"
  if [[ "$primary_count" != "$standby_count" ]]; then
    printf 'Standby did not catch up after seed load. primary=%s standby=%s\n' "$primary_count" "$standby_count" >&2
    return 1
  fi

  log "Verifying replication on standby."
  client_file "pg-standby" "/workspace/sql/checks.sql"
}

action_show_node_data() {
  local host=${1:-pg-primary}
  require_docker
  client_file "$host" "/workspace/sql/checks.sql"
}

action_show_table_definitions() {
  local host=${1:-pg-primary}
  require_docker
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" \
    psql -v ON_ERROR_STOP=1 -h "$host" -U "$LAB_USER" -d "$LAB_DB" \
      -c '\d accounts' \
      -c '\d transfers'
}

action_run_transfer() {
  local host="${1:-pg-primary}"
  local from_id="${2:-1}"
  local to_id="${3:-2}"
  local amount="${4:-150.00}"
  local note="${5:-demo-transfer}"

  require_docker

  log "Running transfer on $host."
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" PGAPPNAME="transfer-script" \
    psql \
      -v ON_ERROR_STOP=1 \
      -v from_id="$from_id" \
      -v to_id="$to_id" \
      -v amount="$amount" \
      -v note="$note" \
      -h "$host" \
      -U "$LAB_USER" \
      -d "$LAB_DB" \
      -f /workspace/sql/transfer.sql
}

show_node_status() {
  local label=$1
  local service=$2

  printf '\n=== %s: %s ===\n' "$label" "$service"
  if ! is_postgres_running "$service"; then
    printf 'PostgreSQL is stopped in %s.\n' "$service"
    return
  fi

  admin_sql "$service" postgres "
SELECT inet_server_addr() AS server_addr,
       inet_server_port() AS server_port,
       pg_is_in_recovery() AS in_recovery,
       CASE
         WHEN pg_is_in_recovery() THEN pg_last_wal_replay_lsn()
         ELSE pg_current_wal_lsn()
       END AS visible_lsn;
"
  if [[ "$(admin_query "$service" postgres "SELECT pg_is_in_recovery();")" == "t" ]]; then
    admin_sql "$service" postgres "
SELECT status, receive_start_lsn, written_lsn, flushed_lsn, latest_end_lsn, slot_name
FROM pg_stat_wal_receiver;
"
  else
    admin_sql "$service" postgres "
SELECT application_name, state, sync_state, sent_lsn, write_lsn, flush_lsn, replay_lsn
FROM pg_stat_replication
ORDER BY application_name;
"
  fi
  client_file "$service" "/workspace/sql/checks.sql"
}

action_show_status() {
  require_docker
  show_node_status "PRIMARY SERVICE" "$PRIMARY_SERVICE"
  show_node_status "STANDBY SERVICE" "$STANDBY_SERVICE"
}

action_show_activity() {
  require_docker
  client_file "pg-primary" "/workspace/sql/show_activity.sql"
}

action_show_logs() {
  local service="${1:-pg-primary}"
  local pattern="${2:-}"
  require_docker

  if [[ -n "$pattern" ]]; then
    service_shell "$service" "test -f '$LOG_FILE' && grep -E '$pattern' '$LOG_FILE' | tail -n 80 || true"
  else
    service_shell "$service" "test -f '$LOG_FILE' && tail -n 120 '$LOG_FILE' || true"
  fi
}

action_show_primary_error_logs() {
  action_show_logs "$PRIMARY_SERVICE" 'No space left on device|could not write|could not extend file|PANIC|FATAL|ERROR'
}

action_show_primary_fs_usage() {
  require_docker
  service_shell "$PRIMARY_SERVICE" 'df -h "$PGDATA"'
}

action_show_primary_ballast_file() {
  require_docker
  service_shell "$PRIMARY_SERVICE" 'ls -lh "$PGDATA/fill/ballast.bin"'
}

action_check_ballast_absent() {
  require_docker
  service_shell "$PRIMARY_SERVICE" '
    if [ -e "$PGDATA/fill/ballast.bin" ]; then
      ls -lh "$PGDATA/fill/ballast.bin"
    else
      echo "ballast.bin is absent because the old PGDATA was replaced during recovery."
    fi
  '
}

action_fill_primary_data_fs() {
  require_docker

  local dd_status="status=progress"
  if [[ "${LAB_DRIVER_MODE:-0}" == "1" ]]; then
    dd_status="status=none"
  fi

  service_shell "$PRIMARY_SERVICE" "
    set +e
    mkdir -p \"\$PGDATA/fill\"
    err_log=\$(mktemp)
    dd if=/dev/zero of=\"\$PGDATA/fill/ballast.bin\" bs=1M ${dd_status} conv=fsync 2> >(tee \"\$err_log\" >&2)
    rc=\$?
    set -e
    printf 'dd exit code: %s\n' \"\$rc\"
    if [ \"\$rc\" -eq 0 ]; then
      printf 'dd unexpectedly finished without ENOSPC. tmpfs limit may not be applied.\n' >&2
      rm -f \"\$err_log\"
      exit 1
    fi
    if ! grep -q 'No space left on device' \"\$err_log\"; then
      printf 'dd failed but stderr did not mention ENOSPC. Aborting; check the primary container.\n' >&2
      cat \"\$err_log\" >&2
      rm -f \"\$err_log\"
      exit 1
    fi
    rm -f \"\$err_log\"
  "
}

action_trigger_full_primary_write_failure() {
  require_docker

  log "Forcing a CHECKPOINT to flush WAL slack before the failing write."
  set +e
  compose exec -T "$PRIMARY_SERVICE" bash -lc \
    "export PATH=/usr/lib/postgresql/16/bin:\$PATH; gosu postgres psql -d postgres -c 'CHECKPOINT;'" >/dev/null 2>&1
  set -e

  set +e
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" PGAPPNAME="enospace-writer" \
    psql -v ON_ERROR_STOP=1 -h pg-primary -U "$LAB_USER" -d "$LAB_DB" \
    -c "INSERT INTO transfers (from_account_id, to_account_id, amount, note) SELECT 1, 2, 1.00, 'enospc-check' FROM generate_series(1, 1000000);"
  local rc=$?
  set -e

  if [[ $rc -eq 0 ]]; then
    printf 'Write unexpectedly succeeded after disk fill. Inspect the primary and rerun.\n' >&2
    return 1
  fi
}

action_assert_enospc_in_log() {
  require_docker
  local match
  match=$(service_shell "$PRIMARY_SERVICE" "grep -c -E 'No space left on device|could not extend file|could not write' '$LOG_FILE' 2>/dev/null || true" | tr -d '[:space:]')
  if [[ -z "$match" || "$match" == "0" ]]; then
    printf 'Expected ENOSPC log line not found in %s. Disk-fill may not have triggered a real write failure.\n' "$LOG_FILE" >&2
    return 1
  fi
  log "Confirmed ENOSPC in primary log (matches: $match)."
}

action_fill_primary_disk() {
  require_docker

  log "Primary filesystem usage before fill:"
  action_show_primary_fs_usage
  log "Writing ballast file into PGDATA until ENOSPC."
  action_fill_primary_data_fs
  log "Primary filesystem usage after fill:"
  action_show_primary_fs_usage
  log "Triggering a write on the full primary to force PostgreSQL errors."
  action_trigger_full_primary_write_failure
  log "Asserting ENOSPC pattern is present in the primary log."
  action_assert_enospc_in_log
  log "Write failed as expected. Relevant primary log lines:"
  action_show_primary_error_logs
}

action_failover_to_standby() {
  require_docker

  log "Stopping failed primary if it is still running."
  stop_postgres "$PRIMARY_SERVICE" immediate || true

  log "Fencing pg-primary at the container level to prevent split-brain."
  if is_postgres_running "$PRIMARY_SERVICE"; then
    log "pg_ctl reports primary still running; forcing docker stop."
    compose stop -t 5 "$PRIMARY_SERVICE" >/dev/null 2>&1 || true
  fi

  if compose ps --status running --services 2>/dev/null | grep -qx "$PRIMARY_SERVICE"; then
    if is_postgres_running "$PRIMARY_SERVICE"; then
      printf 'Refusing to promote standby: %s is still serving postgres. Stop it manually before retrying.\n' "$PRIMARY_SERVICE" >&2
      return 1
    fi
  fi

  log "Promoting standby."
  service_shell "$STANDBY_SERVICE" 'gosu postgres pg_ctl -D "$PGDATA" promote'
  wait_for_recovery_state "$STANDBY_SERVICE" "f"

  log "Standby is now primary."
  admin_sql "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();"
  client_sql "pg-standby" "SELECT now() AS failover_completed_at;"
}

action_recover_primary_as_standby() {
  require_docker

  if [[ "$(admin_query "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "f" ]]; then
    printf '%s must be promoted before recovery.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi

  log "Cleaning failed primary."
  stop_postgres "$PRIMARY_SERVICE" immediate || true
  drop_inactive_replication_slots "$STANDBY_SERVICE"
  drop_slot_if_exists "$STANDBY_SERVICE" "primary_slot"

  service_shell "$PRIMARY_SERVICE" "
    mkdir -p \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    find \"\$PGDATA\" -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
    rm -f /var/lib/postgresql/pglogs/postgresql.log
    chown -R postgres:postgres \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    export PGPASSWORD='$REPL_PASS'
    gosu postgres pg_basebackup \
      -h pg-standby \
      -p 5432 \
      -U '$REPL_USER' \
      -D \"\$PGDATA\" \
      -R \
      -c fast \
      -X stream \
      -C \
      -S primary_slot
    cat >> \"\$PGDATA/postgresql.auto.conf\" <<'EOF'
primary_conninfo = 'host=pg-standby port=5432 user=$REPL_USER password=$REPL_PASS application_name=pg-primary'
primary_slot_name = 'primary_slot'
EOF
    chown postgres:postgres \"\$PGDATA/postgresql.auto.conf\"
  "

  start_postgres "$PRIMARY_SERVICE"
  wait_for_recovery_state "$PRIMARY_SERVICE" "t"

  log "Former primary is back as standby."
  admin_sql "$PRIMARY_SERVICE" postgres "SELECT pg_is_in_recovery();"
  admin_sql "$PRIMARY_SERVICE" postgres "
SELECT status, receive_start_lsn, written_lsn, flushed_lsn, latest_end_lsn, slot_name
FROM pg_stat_wal_receiver;
"
}

action_switchback_to_original_topology() {
  require_docker

  if [[ "$(admin_query "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "f" ]]; then
    printf '%s must currently be primary.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi

  if [[ "$(admin_query "$PRIMARY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "t" ]]; then
    printf '%s must currently be standby before switchback.\n' "$PRIMARY_SERVICE" >&2
    return 1
  fi

  log "Blocking new writes on the current primary to close the TOCTOU window."
  admin_sql "$STANDBY_SERVICE" postgres "ALTER SYSTEM SET default_transaction_read_only = on;" >/dev/null
  admin_sql "$STANDBY_SERVICE" postgres "SELECT pg_reload_conf();" >/dev/null

  log "Terminating remaining client backends on the current primary."
  admin_sql "$STANDBY_SERVICE" postgres "
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE pid <> pg_backend_pid()
  AND backend_type = 'client backend'
  AND datname IS NOT NULL;
" >/dev/null 2>&1 || true

  log "Stopping current primary cleanly so the shutdown checkpoint flushes to pg-primary."
  stop_postgres "$STANDBY_SERVICE" fast

  log "Waiting for pg-primary to replay all WAL it received from the (now stopped) primary."
  local received_lsn replay_lsn lag_bytes
  lag_bytes=""
  for _ in $(seq 1 120); do
    received_lsn="$(admin_query "$PRIMARY_SERVICE" postgres "SELECT COALESCE(pg_last_wal_receive_lsn(), '0/0');")"
    replay_lsn="$(admin_query "$PRIMARY_SERVICE" postgres "SELECT COALESCE(pg_last_wal_replay_lsn(), '0/0');")"
    lag_bytes="$(admin_query "$PRIMARY_SERVICE" postgres "SELECT pg_wal_lsn_diff('$received_lsn', '$replay_lsn');")"
    if [[ "${lag_bytes%%.*}" == "0" || "${lag_bytes%%.*}" == "-0" ]]; then
      break
    fi
    sleep 1
  done

  if [[ "${lag_bytes%%.*}" != "0" && "${lag_bytes%%.*}" != "-0" ]]; then
    printf 'pg-primary did not replay everything received. lag=%s received=%s replay=%s\n' \
      "$lag_bytes" "$received_lsn" "$replay_lsn" >&2
    return 1
  fi

  log "Promoting pg-primary back to primary."
  service_shell "$PRIMARY_SERVICE" 'gosu postgres pg_ctl -D "$PGDATA" promote'
  wait_for_recovery_state "$PRIMARY_SERVICE" "f"

  log "Resetting default_transaction_read_only on the now-promoted primary."
  admin_sql "$PRIMARY_SERVICE" postgres "ALTER SYSTEM RESET default_transaction_read_only;" >/dev/null 2>&1 || true
  admin_sql "$PRIMARY_SERVICE" postgres "SELECT pg_reload_conf();" >/dev/null 2>&1 || true

  log "Cleaning leftover replication slots on pg-primary before re-seeding pg-standby."
  drop_inactive_replication_slots "$PRIMARY_SERVICE"
  drop_slot_if_exists "$PRIMARY_SERVICE" "standby_slot"
  service_shell "$STANDBY_SERVICE" "
    mkdir -p \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    find \"\$PGDATA\" -mindepth 1 -maxdepth 1 -exec rm -rf {} + || true
    rm -f /var/lib/postgresql/pglogs/postgresql.log
    chown -R postgres:postgres \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    export PGPASSWORD='$REPL_PASS'
    gosu postgres pg_basebackup \
      -h pg-primary \
      -p 5432 \
      -U '$REPL_USER' \
      -D \"\$PGDATA\" \
      -R \
      -c fast \
      -X stream \
      -C \
      -S standby_slot
    cat >> \"\$PGDATA/postgresql.auto.conf\" <<'EOF'
primary_conninfo = 'host=pg-primary port=5432 user=$REPL_USER password=$REPL_PASS application_name=pg-standby'
primary_slot_name = 'standby_slot'
EOF
    chown postgres:postgres \"\$PGDATA/postgresql.auto.conf\"
  "

  start_postgres "$STANDBY_SERVICE"
  wait_for_recovery_state "$STANDBY_SERVICE" "t"

  log "Original topology restored."
  action_show_status
}

action_assert_stage1_ready() {
  require_docker

  if ! is_postgres_running "$PRIMARY_SERVICE"; then
    printf '%s is not running. Run stage1 or bootstrap first.\n' "$PRIMARY_SERVICE" >&2
    return 1
  fi

  if ! is_postgres_running "$STANDBY_SERVICE"; then
    printf '%s is not running. Run stage1 or bootstrap first.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi

  if [[ "$(admin_query "$PRIMARY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "f" ]]; then
    printf '%s is not primary in the expected stage1 topology.\n' "$PRIMARY_SERVICE" >&2
    return 1
  fi

  if [[ "$(admin_query "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "t" ]]; then
    printf '%s is not standby in the expected stage1 topology.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi

  if [[ "$(client_query "pg-primary" "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('accounts', 'transfers');")" != "2" ]]; then
    printf 'Demo tables are missing on pg-primary. Run stage1 or bootstrap first.\n' >&2
    return 1
  fi
}

action_assert_failover_happened() {
  require_docker

  if ! is_postgres_running "$STANDBY_SERVICE"; then
    printf '%s is not running. A promoted standby is required before stage3.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi

  if [[ "$(admin_query "$STANDBY_SERVICE" postgres "SELECT pg_is_in_recovery();")" != "f" ]]; then
    printf '%s is not promoted. Run failover before stage3.\n' "$STANDBY_SERVICE" >&2
    return 1
  fi
}
