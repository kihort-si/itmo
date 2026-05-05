#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

PRIMARY_SERVICE="${PRIMARY_SERVICE:-pg-primary}"
STANDBY_SERVICE="${STANDBY_SERVICE:-pg-standby}"
CLIENT_SERVICE="${CLIENT_SERVICE:-client}"

LAB_DB="${LAB_DB:-labdb}"
LAB_USER="${LAB_USER:-labuser}"
LAB_PASS="${LAB_PASS:-labpass}"

REPL_USER="${REPL_USER:-repl}"
REPL_PASS="${REPL_PASS:-replpass}"

LOG_FILE="${LOG_FILE:-/var/lib/postgresql/pglogs/postgresql.log}"

log() {
  if [[ "${LAB_QUIET:-0}" == "1" ]]; then
    return 0
  fi
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

compose() {
  (
    cd "$PROJECT_ROOT"
    docker compose "$@"
  )
}

require_docker() {
  if ! docker info >/dev/null 2>&1; then
    printf 'Docker daemon is unavailable. Start Docker/OrbStack/Docker Desktop and retry.\n' >&2
    exit 1
  fi
}

ensure_artifact_dirs() {
  mkdir -p \
    "$PROJECT_ROOT/artifacts/primary/logs" \
    "$PROJECT_ROOT/artifacts/primary/archive" \
    "$PROJECT_ROOT/artifacts/standby/logs" \
    "$PROJECT_ROOT/artifacts/standby/archive"
}

service_shell() {
  local service=$1
  local script=$2
  compose exec -T "$service" bash -lc "export PATH=/usr/lib/postgresql/16/bin:\$PATH; $script"
}

service_tty_shell() {
  local service=$1
  local script=$2
  compose exec "$service" bash -lc "export PATH=/usr/lib/postgresql/16/bin:\$PATH; $script"
}

admin_sql() {
  local service=$1
  local database=$2
  local sql=$3
  compose exec -T "$service" bash -lc \
    "export PATH=/usr/lib/postgresql/16/bin:\$PATH; gosu postgres psql -v ON_ERROR_STOP=1 -d $(printf '%q' "$database") -c $(printf '%q' "$sql")"
}

admin_query() {
  local service=$1
  local database=$2
  local sql=$3
  compose exec -T "$service" bash -lc \
    "export PATH=/usr/lib/postgresql/16/bin:\$PATH; gosu postgres psql -v ON_ERROR_STOP=1 -At -d $(printf '%q' "$database") -c $(printf '%q' "$sql")"
}

client_sql() {
  local host=$1
  local sql=$2
  local database=${3:-$LAB_DB}
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" \
    psql -v ON_ERROR_STOP=1 -h "$host" -U "$LAB_USER" -d "$database" -c "$sql"
}

client_query() {
  local host=$1
  local sql=$2
  local database=${3:-$LAB_DB}
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" \
    psql -v ON_ERROR_STOP=1 -At -h "$host" -U "$LAB_USER" -d "$database" -c "$sql"
}

client_file() {
  local host=$1
  local file=$2
  local database=${3:-$LAB_DB}
  compose exec -T "$CLIENT_SERVICE" \
    env PGPASSWORD="$LAB_PASS" \
    psql -v ON_ERROR_STOP=1 -h "$host" -U "$LAB_USER" -d "$database" -f "$file"
}

wait_for_pg() {
  local service=$1
  local tries=${2:-${WAIT_FOR_PG_TRIES:-120}}
  local attempt=0

  until service_shell "$service" "gosu postgres pg_isready -h /var/run/postgresql -d postgres >/dev/null 2>&1"; do
    attempt=$((attempt + 1))
    if (( attempt >= tries )); then
      printf 'PostgreSQL in %s did not become ready in time.\n' "$service" >&2
      return 1
    fi
    sleep 1
  done
}

wait_for_recovery_state() {
  local service=$1
  local expected=$2
  local tries=${3:-${WAIT_FOR_RECOVERY_TRIES:-120}}
  local attempt=0

  until [[ "$(admin_query "$service" postgres "SELECT pg_is_in_recovery();")" == "$expected" ]]; do
    attempt=$((attempt + 1))
    if (( attempt >= tries )); then
      printf '%s did not reach pg_is_in_recovery()=%s in time.\n' "$service" "$expected" >&2
      return 1
    fi
    sleep 1
  done
}

stop_postgres() {
  local service=$1
  local mode=${2:-fast}
  service_shell "$service" "
    if gosu postgres pg_ctl -D \"\$PGDATA\" status >/dev/null 2>&1; then
      gosu postgres pg_ctl -D \"\$PGDATA\" -m $mode stop
    fi
  "
}

start_postgres() {
  local service=$1
  service_shell "$service" "
    mkdir -p /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    chmod 700 \"\$PGDATA\"
    chown -R postgres:postgres \"\$PGDATA\" /var/lib/postgresql/pglogs /var/lib/postgresql/archive
    gosu postgres pg_ctl -D \"\$PGDATA\" -l \"$LOG_FILE\" start
  "
  wait_for_pg "$service"
}

reset_data_dir() {
  local service=$1
  service_shell "$service" "
    mkdir -p \"\$PGDATA\"
    find \"\$PGDATA\" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
    chmod 700 \"\$PGDATA\"
    chown -R postgres:postgres \"\$PGDATA\"
  "
}

drop_slot_if_exists() {
  local service=$1
  local slot_name=$2
  admin_sql "$service" postgres "
DO \$\$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name = '$slot_name') THEN
    PERFORM pg_drop_replication_slot('$slot_name');
  END IF;
END
\$\$;
"
}

drop_inactive_replication_slots() {
  local service=$1
  if ! service_shell "$service" 'gosu postgres pg_ctl -D "$PGDATA" status >/dev/null 2>&1'; then
    return 0
  fi
  admin_sql "$service" postgres "
DO \$\$
DECLARE
  s record;
BEGIN
  FOR s IN SELECT slot_name FROM pg_replication_slots WHERE NOT active LOOP
    PERFORM pg_drop_replication_slot(s.slot_name);
    RAISE NOTICE 'dropped inactive replication slot: %', s.slot_name;
  END LOOP;
END
\$\$;
"
}

visible_lsn_sql() {
  cat <<'EOF'
SELECT CASE
         WHEN pg_is_in_recovery() THEN pg_last_wal_replay_lsn()
         ELSE pg_current_wal_lsn()
       END;
EOF
}
