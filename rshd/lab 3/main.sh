#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/scripts/actions.sh"

TARGET_STAGE="full"
EXPLAIN_ONLY=0
DEMO_MODE=0
TEACHER_MODE=0
STEP_BY_STEP=0
PAUSE_ENABLED=1
PACE_DELAY=0
COLOR_ENABLED=0

RESET=""
BOLD=""
DIM=""
CYAN=""
BLUE=""
GREEN=""
YELLOW=""
MAGENTA=""
RED=""

STYLE_SEPARATOR=""
STYLE_BANNER=""
STYLE_STEP=""
STYLE_TASK=""
STYLE_REALIZATION=""
STYLE_COMMAND=""
STYLE_SQL=""
STYLE_EXPLANATION=""
STYLE_CHECK=""
STYLE_SUCCESS=""
STYLE_WARNING=""
STYLE_ERROR=""
STYLE_SHOW=""
STYLE_SPOKEN=""
STYLE_INTERPRETATION=""
STYLE_FIELD=""
STYLE_NOTE=""
STYLE_PAUSE=""

DEMO_SESSION_PIDS=()
DEMO_SESSION_LOGS=()

choose_text() {
  local verbose=$1
  local concise=${2:-$1}
  if (( TEACHER_MODE )); then
    printf '%s' "$concise"
  else
    printf '%s' "$verbose"
  fi
}

init_colors() {
  if [[ -t 1 && -z "${NO_COLOR:-}" && "${TERM:-}" != "dumb" ]]; then
    COLOR_ENABLED=1
  fi

  if (( COLOR_ENABLED )); then
    RESET=$'\033[0m'
    BOLD=$'\033[1m'
    DIM=$'\033[2m'
    CYAN=$'\033[36m'
    BLUE=$'\033[34m'
    GREEN=$'\033[32m'
    YELLOW=$'\033[33m'
    MAGENTA=$'\033[35m'
    RED=$'\033[31m'

    STYLE_SEPARATOR="${DIM}${CYAN}"
    STYLE_BANNER="${BOLD}${CYAN}"
    STYLE_STEP="${BOLD}${CYAN}"
    STYLE_TASK="${BOLD}${YELLOW}"
    STYLE_REALIZATION="${BOLD}${MAGENTA}"
    STYLE_COMMAND="${BOLD}${GREEN}"
    STYLE_SQL="${BOLD}${GREEN}"
    STYLE_EXPLANATION="${BOLD}${BLUE}"
    STYLE_CHECK="${BOLD}${CYAN}"
    STYLE_SUCCESS="${BOLD}${GREEN}"
    STYLE_WARNING="${BOLD}${YELLOW}"
    STYLE_ERROR="${BOLD}${RED}"
    STYLE_SHOW="${BOLD}${MAGENTA}"
    STYLE_SPOKEN="${BOLD}${MAGENTA}"
    STYLE_INTERPRETATION="${DIM}${BLUE}"
    STYLE_FIELD="${DIM}"
    STYLE_NOTE="${DIM}"
    STYLE_PAUSE="${BOLD}${YELLOW}"
  fi
}

label_style() {
  case "$1" in
    "STEP")
      printf '%s' "$STYLE_STEP"
      ;;
    "TASK SENTENCE")
      printf '%s' "$STYLE_TASK"
      ;;
    "OUR REALIZATION")
      printf '%s' "$STYLE_REALIZATION"
      ;;
    "COMMAND")
      printf '%s' "$STYLE_COMMAND"
      ;;
    "SQL")
      printf '%s' "$STYLE_SQL"
      ;;
    "HOW IT WORKS")
      printf '%s' "$STYLE_EXPLANATION"
      ;;
    "CHECK")
      printf '%s' "$STYLE_CHECK"
      ;;
    "WHAT TO SHOW")
      printf '%s' "$STYLE_SHOW"
      ;;
    "SUCCESS CRITERIA")
      printf '%s' "$STYLE_SUCCESS"
      ;;
    "INTERPRETATION")
      printf '%s' "$STYLE_INTERPRETATION"
      ;;
    "WHAT TO SAY OUT LOUD")
      printf '%s' "$STYLE_SPOKEN"
      ;;
    "PAUSE")
      printf '%s' "$STYLE_PAUSE"
      ;;
    "WARNING")
      printf '%s' "$STYLE_WARNING"
      ;;
    "ERROR")
      printf '%s' "$STYLE_ERROR"
      ;;
    *)
      printf '%s' "$STYLE_NOTE"
      ;;
  esac
}

print_separator() {
  printf '\n%b============================================================%b\n' "$STYLE_SEPARATOR" "$RESET"
}

print_banner() {
  local title=$1
  print_separator
  printf '%b%s%b\n' "$STYLE_BANNER" "$title" "$RESET"
  printf '%b============================================================%b\n' "$STYLE_SEPARATOR" "$RESET"
}

print_field() {
  local field=$1
  local text=${2:-}
  local value_style=${3:-}
  local first=1
  while IFS= read -r line || [[ -n "$line" ]]; do
    if (( first )); then
      printf '%b%s%b -> %b%s%b\n' "$STYLE_FIELD" "$field" "$RESET" "$value_style" "$line" "$RESET"
      first=0
    else
      printf '  %b%s%b\n' "$value_style" "$line" "$RESET"
    fi
  done <<< "$text"

  if (( first )); then
    printf '%b%s%b ->\n' "$STYLE_FIELD" "$field" "$RESET"
  fi
}

print_block() {
  local label=$1
  local field=$2
  local text=$3
  local block_style
  block_style="$(label_style "$label")"
  printf '%b[%s]%b\n' "$block_style" "$label" "$RESET"
  print_field "$field" "$text" "$block_style"
  printf '\n'
}

print_step() {
  print_block "STEP" "step" "$1"
}

print_task_sentence() {
  print_block "TASK SENTENCE" "sentence from task" "$1"
}

print_realization() {
  print_block "OUR REALIZATION" "our realization" "$1"
}

print_commands_block() {
  local label=$1
  local text=$2
  local lower_field="commands / sql"
  print_block "$label" "$lower_field" "$text"
}

print_explanation() {
  print_block "HOW IT WORKS" "how it works" "$1"
}

print_check() {
  print_block "CHECK" "check" "$1"
}

print_teacher_hint() {
  print_block "WHAT TO SHOW" "what to show to teacher" "$1"
}

print_success_criteria() {
  print_block "SUCCESS CRITERIA" "what result proves success" "$1"
}

print_spoken_hint() {
  print_block "WHAT TO SAY OUT LOUD" "what to say out loud" "$1"
}

print_interpretation() {
  print_block "INTERPRETATION" "interpretation" "$1"
}

print_warning() {
  print_block "WARNING" "warning" "$1"
}

print_error() {
  print_block "ERROR" "error" "$1"
}

die() {
  print_error "$*" >&2
  exit 1
}

pause_step() {
  local message=${1:-Review the current output before proceeding.}
  if (( ! PAUSE_ENABLED )); then
    if (( DEMO_MODE )) && (( PACE_DELAY > 0 )); then
      sleep "$PACE_DELAY"
    fi
    return 0
  fi

  print_block "PAUSE" "pause" "$message"
  printf '%bPress Enter to continue to the next step...%b' "$STYLE_PAUSE" "$RESET"
  read -r
  printf '\n'
}

ensure_pause_compatibility() {
  if (( PAUSE_ENABLED )) && [[ ! -t 0 ]]; then
    die "Interactive pause mode requires a TTY on stdin. Re-run with --no-pause for non-interactive use."
  fi
}

run_with_feedback() {
  local description=$1
  shift

  if ! "$@"; then
    print_error "Execution failed while handling: $description" >&2
    return 1
  fi
}

run_note() {
  local text=$1
  print_check "$text"
}

run_explain_only_note() {
  local text=$1
  print_warning "$text"
}

handle_post_block_pause() {
  local message=$1
  if (( STEP_BY_STEP )); then
    pause_step "$message"
  elif (( DEMO_MODE )) && (( PACE_DELAY > 0 )) && (( ! PAUSE_ENABLED )); then
    sleep "$PACE_DELAY"
  fi
}

run_cmd() {
  local display_text=$1
  shift

  if (( EXPLAIN_ONLY )); then
    run_explain_only_note "Explain-only mode is active, so this command block is not executed now: $display_text"
    print_interpretation "In study mode focus on the command list above, the proof criteria, and the spoken explanation. No cluster state is changed."
    return 0
  fi

  run_note "The script now executes the command block shown above: $display_text"
  run_with_feedback "$display_text" "$@"
}

run_sql() {
  local display_text=$1
  shift

  if (( EXPLAIN_ONLY )); then
    run_explain_only_note "Explain-only mode is active, so this SQL is not executed now: $display_text"
    print_interpretation "In study mode the important part is what this SQL proves and what result you expect to see."
    return 0
  fi

  run_note "The script now executes the SQL shown above: $display_text"
  run_with_feedback "$display_text" "$@"
}

present_step_block() {
  local step=$1
  local sentence=$2
  local realization=$3
  local commands=$4
  local explanation=$5
  local show_teacher=$6
  local success=$7
  local spoken=$8

  print_separator
  print_step "$step"
  print_task_sentence "$sentence"
  print_realization "$realization"
  print_commands_block "COMMAND" "$commands"
  print_explanation "$explanation"
  print_teacher_hint "$show_teacher"
  print_success_criteria "$success"
  print_spoken_hint "$spoken"
  handle_post_block_pause "Review this sub-step explanation, then press Enter to execute it."
}

usage() {
  cat <<'EOF'
Usage:
  ./main.sh
  ./main.sh full
  ./main.sh step-by-step
  ./main.sh stage1
  ./main.sh stage2
  ./main.sh stage3

Optional flags:
  --no-pause       Disable the default interactive Enter pauses.
  --explain-only   Print the same defense guidance, but do not execute commands.
  --demo-mode      Use presentation-friendly pacing and transitions.
  --teacher-mode   Keep the output concise and defense-oriented.
  --help           Show this help.

Examples:
  ./main.sh
  ./main.sh --explain-only
  ./main.sh --demo-mode
  ./main.sh --teacher-mode
  ./main.sh full --demo-mode
  ./main.sh full --demo-mode --no-pause
  ./main.sh step-by-step
  ./main.sh stage2 --teacher-mode
  ./main.sh --explain-only --no-pause full
EOF
}

parse_args() {
  local stage_seen=0
  local arg

  if (( $# == 0 )); then
    return 0
  fi

  for arg in "$@"; do
    case "$arg" in
      full|stage1|stage2|stage3)
        if (( stage_seen )); then
          die "Only one stage selector is allowed."
        fi
        TARGET_STAGE="$arg"
        stage_seen=1
        ;;
      step-by-step)
        if (( stage_seen )); then
          die "step-by-step cannot be combined with another stage selector."
        fi
        TARGET_STAGE="full"
        STEP_BY_STEP=1
        stage_seen=1
        ;;
      --explain-only)
        EXPLAIN_ONLY=1
        ;;
      --no-pause)
        PAUSE_ENABLED=0
        ;;
      --demo-mode)
        DEMO_MODE=1
        ;;
      --teacher-mode)
        TEACHER_MODE=1
        ;;
      --help|-h)
        usage
        exit 0
        ;;
      *)
        die "Unknown argument: $arg"
        ;;
    esac
  done

  if (( DEMO_MODE )); then
    PACE_DELAY=1
  fi
}

cleanup_demo_sessions() {
  local pid log_file
  for pid in "${DEMO_SESSION_PIDS[@]:-}"; do
    kill "$pid" >/dev/null 2>&1 || true
    wait "$pid" >/dev/null 2>&1 || true
  done
  for log_file in "${DEMO_SESSION_LOGS[@]:-}"; do
    rm -f "$log_file" >/dev/null 2>&1 || true
  done
  DEMO_SESSION_PIDS=()
  DEMO_SESSION_LOGS=()
}

spawn_demo_session() {
  local app_name=$1
  local sql_block=$2
  local log_file

  log_file="$(mktemp -t rshd-session.XXXXXX.log)"
  DEMO_SESSION_LOGS+=("$log_file")

  compose exec -T "$CLIENT_SERVICE" bash -lc "
    export PGPASSWORD='$LAB_PASS'
    export PGAPPNAME='$app_name'
    psql -v ON_ERROR_STOP=1 -h pg-primary -U '$LAB_USER' -d '$LAB_DB' <<'SQL'
$sql_block
SQL
  " >"$log_file" 2>&1 &

  DEMO_SESSION_PIDS+=("$!")
}

spawn_demo_sessions() {
  cleanup_demo_sessions

  spawn_demo_session "client-1" "$(cat <<'EOF'
BEGIN;
SELECT 'client-1 keeps a transaction open for the activity demo';
SELECT pg_sleep(20);
COMMIT;
EOF
)"

  spawn_demo_session "client-2" "$(cat <<'EOF'
SELECT 'client-2 keeps a long read session open';
SELECT pg_sleep(20);
EOF
)"

  spawn_demo_session "client-3" "$(cat <<'EOF'
SELECT 'client-3 keeps a client connection open';
SELECT pg_sleep(20);
EOF
)"

  sleep 2
}

print_mode_summary() {
  local explain_state="execution mode"
  local pacing_state="normal pacing"
  local speech_state="detailed defense language"
  local pause_state="interactive pauses after major steps"
  local color_state="plain-text fallback"

  if (( EXPLAIN_ONLY )); then
    explain_state="explain-only mode"
  fi
  if (( DEMO_MODE )); then
    pacing_state="demo pacing"
  fi
  if (( TEACHER_MODE )); then
    speech_state="teacher-oriented concise language"
  fi
  if (( ! PAUSE_ENABLED )); then
    pause_state="non-interactive mode via --no-pause"
  elif (( STEP_BY_STEP )); then
    pause_state="interactive pauses after major steps and before each sub-step"
  fi
  if (( COLOR_ENABLED )); then
    color_state="colorized output"
  fi

  print_banner "Docker PostgreSQL HA Lab Defense Driver"
  printf '%bTarget stage%b: %s\n' "$STYLE_FIELD" "$RESET" "$TARGET_STAGE"
  printf '%bMode summary%b: %s, %s, %s\n' "$STYLE_FIELD" "$RESET" "$explain_state" "$pacing_state" "$speech_state"
  printf '%bPause behavior%b: %s\n' "$STYLE_FIELD" "$RESET" "$pause_state"
  printf '%bOutput style%b: %s\n' "$STYLE_FIELD" "$RESET" "$color_state"
}

stage_environment_validation() {
  print_banner "Stage 0. Environment Validation"

  present_step_block \
    "Stage 0. Environment Validation" \
    "Validate that Docker, Docker Compose, and the key project artifacts are available before starting the lab stand." \
    "$(choose_text \
      "The driver checks the docker CLI, the docker compose plugin, the Docker daemon, and the required files of this repository before touching cluster state." \
      "The driver first proves that the environment itself is runnable.")" \
    $'docker --version\ndocker compose version\ndocker info --format ...\ncheck required files in the repository' \
    "$(choose_text \
      "This prevents false explanations during the defense. If Docker is unavailable or the repository is incomplete, any PostgreSQL failure that follows would be misleading." \
      "I validate Docker and the repository first, so later PostgreSQL output can be trusted.")" \
    "Show that Docker answers, the daemon is reachable, and the repository really contains docker-compose.yml, shell scripts, and SQL files." \
    "The docker CLI and daemon respond, and the driver reports that all required project files are present." \
    "I first prove that the stand can really run from this repository, so the rest of the defense is about PostgreSQL high availability, not about a broken environment."

  run_cmd $'docker --version\ndocker compose version\ndocker info --format ...' action_validate_environment
  run_cmd "check required project files in the repository" action_validate_required_files
  print_interpretation "If Docker answers and the files are present, the stand is ready for a reproducible launch."
  pause_step "Environment validation is complete. Press Enter to continue to container startup."
}

stage_infrastructure_startup() {
  print_banner "Stage 1. Infrastructure Startup"

  present_step_block \
    "Stage 1. Infrastructure Startup" \
    "Start the Docker stand that will replace the two VM hosts and the separate client context." \
    "$(choose_text \
      "The stand is a three-container topology: pg-primary, pg-standby, and client. In a full run the driver resets old state, starts the containers, and prints their status." \
      "The stand consists of pg-primary, pg-standby, and client. The driver starts them and shows their state.")" \
    $'docker compose down -v --remove-orphans\ndocker compose up -d\ndocker compose ps' \
    "$(choose_text \
      "The PostgreSQL containers are intentionally started with sleep infinity. PostgreSQL itself is then initialized and started by our scripts, which makes recovery and reseeding easier to control." \
      "Containers start first; PostgreSQL is initialized inside them by our own scripts.")" \
    "Show that all three containers exist and are running before the database-specific stages begin." \
    "docker compose ps shows pg-primary, pg-standby, and client in running state." \
    "I first build the infrastructure layer, then I initialize PostgreSQL roles on top of it."

  run_cmd $'docker compose down -v --remove-orphans\ndocker compose up -d\ndocker compose ps' action_reset_lab
  run_cmd "docker compose up -d" action_start_containers
  run_cmd "docker compose ps" action_check_container_status
  print_interpretation "At this point the isolated Docker hosts exist and are ready for database initialization."

  present_step_block \
    "Stage 1. Infrastructure Startup" \
    "Use two identical hosts." \
    "In Docker this requirement is satisfied by running pg-primary and pg-standby from the same official postgres:16 image. Their software stack is identical; only their runtime role differs." \
    $'docker inspect <pg-primary-container-id> --format {{.Config.Image}}\ndocker inspect <pg-standby-container-id> --format {{.Config.Image}}' \
    "If both nodes use the same image and binaries, then the Docker stand is equivalent to two equal PostgreSQL hosts from the point of view of the lab." \
    "Show the image names and running status of pg-primary and pg-standby." \
    "Both database containers report image=postgres:16 and running status." \
    "Instead of two equal VMs, I use two equal containers from the same postgres:16 image. That satisfies the requirement for identical hosts."

  run_cmd "inspect the image and state of pg-primary, pg-standby, and client" action_show_node_images
  print_interpretation "The two database nodes are identical at the software level, which is the key meaning of this lab requirement."

  present_step_block \
    "Stage 1. Infrastructure Startup" \
    "Ensure network connectivity." \
    "All containers are attached to the same bridge network pg-lab, and the client container resolves the database nodes by DNS names pg-primary and pg-standby." \
    $'docker compose exec -T client bash -lc \'getent hosts pg-primary; getent hosts pg-standby\'' \
    "Streaming replication and client access both depend on network reachability. If the client resolves both names, the stand has the connectivity needed for the rest of the lab." \
    "Show that the client container resolves both database hosts on the Docker network." \
    "The client container resolves pg-primary and pg-standby to reachable container IP addresses." \
    "Here I prove that the nodes can really see each other on the Docker network, so replication and client connections can work."

  run_cmd "resolve pg-primary and pg-standby from the client container" action_check_network_connectivity
  print_interpretation "DNS resolution from the client container confirms network-level reachability inside the Docker stand."

  present_step_block \
    "Stage 1. Infrastructure Startup" \
    "For client connections use a separate machine or context." \
    "The separate client context is the dedicated client container. All user-level psql sessions, SQL scripts, and activity checks originate from that container instead of from inside the database nodes." \
    $'make client1\nmake client2\nmake client3\nmake standby-client' \
    "This keeps the demo honest: PostgreSQL nodes act as servers, and the client container acts as an external machine from which users connect." \
    "Point at the client container and show that SQL scripts and interactive sessions are launched from it." \
    "The driver and the helper scripts use the client container for psql connections, not the database containers themselves." \
    "I keep the client role separate from the server role, so the demo matches the assignment and a real deployment more closely."

  print_interpretation "The client container is the dedicated user context that satisfies the separate-machine requirement."
  pause_step "Infrastructure startup and base host requirements are complete. Press Enter to continue to primary deployment."
}

stage_primary_deployment() {
  print_banner "Stage 1. Primary Deployment"

  present_step_block \
    "Stage 1. Primary Deployment" \
    "Развернуть postgres на двух узлах в режиме трансляции логов." \
    "$(choose_text \
      "On the primary node the driver runs initdb, writes replication-related settings, configures pg_hba.conf, starts PostgreSQL, creates the replication role, the application role, and the lab database. Credentials are sourced from environment variables and are not echoed to the screen." \
      "The driver initializes pg-primary, enables replication settings, and creates the roles and database needed for the lab.")" \
    $'./scripts/init_primary.sh\nSHOW wal_level;\nSHOW max_wal_senders;\nSHOW max_replication_slots;\nSHOW wal_keep_size;\nSHOW hot_standby;\nSHOW archive_mode;\nSHOW archive_command;\nSHOW wal_log_hints;\nSHOW full_page_writes;' \
    "$(choose_text \
      "The primary is the only writable node before failover. It generates WAL, accepts replication connections, and archives WAL files. Without these settings there would be no standby creation and no streaming replication." \
      "The primary must generate WAL and allow standby connections; that is what these settings enable.")" \
    "Show the primary initialization, then show the actual replication parameters from pg_settings." \
    "Primary starts successfully, pg_is_in_recovery() is false, and the expected replication parameters are visible in pg_settings." \
    "Here I am preparing the authoritative write node. It is the source of WAL that the standby will later stream and replay."

  run_cmd "./scripts/init_primary.sh" action_init_primary
  run_sql $'SHOW wal_level;\nSHOW max_wal_senders;\nSHOW max_replication_slots;\nSHOW wal_keep_size;\nSHOW hot_standby;\nSHOW archive_mode;\nSHOW archive_command;\nSHOW wal_log_hints;\nSHOW full_page_writes;' action_show_primary_replication_config
  run_sql "SELECT pg_is_in_recovery();" action_show_recovery_state "pg-primary"
  print_interpretation "pg-primary is now a writable primary with replication enabled and the required roles already created."

  present_step_block \
    "Stage 1. Primary Deployment" \
    "Не использовать дополнительные пакеты." \
    "The implementation uses only the official postgres:16 image and PostgreSQL built-in utilities such as initdb, pg_basebackup, pg_ctl, pg_isready, and psql. No Patroni, repmgr, pgpool, or other external HA layer is installed." \
    $'command -v patroni || true\ncommand -v repmgr || true\ncommand -v pgpool || true' \
    "The point of this lab is to show native PostgreSQL mechanisms: WAL, physical replication, promotion, and manual recovery. External HA packages would hide those mechanics." \
    "Show that the listed external tools are absent and that the scripts use only PostgreSQL native commands." \
    "The primary container reports that patroni, repmgr, and pgpool are not installed." \
    "This cluster is built with native PostgreSQL tools only. The failover logic is explained directly, not delegated to an HA orchestrator."

  run_cmd "verify that Patroni, repmgr, and pgpool are not installed in pg-primary" action_show_absent_external_tools
  print_interpretation "The stand meets the assignment constraint of using base PostgreSQL without external HA frameworks."
  pause_step "Primary deployment is complete. Press Enter to continue to standby deployment."
}

stage_standby_deployment() {
  print_banner "Stage 1. Standby Deployment"

  present_step_block \
    "Stage 1. Standby Deployment" \
    "Развернуть postgres на двух узлах в режиме трансляции логов." \
    "$(choose_text \
      "The standby is not initialized independently. Instead, the driver clones it from the primary with pg_basebackup -R -X stream -C -S standby_slot, which creates a recovery-ready data directory and the connection settings needed for streaming replication." \
      "The standby is built from the primary with pg_basebackup -R so it can immediately start in recovery and stream WAL.")" \
    $'./scripts/init_standby.sh\npg_basebackup -R -X stream -C -S standby_slot\nls $PGDATA/standby.signal $PGDATA/postgresql.auto.conf\nSELECT pg_is_in_recovery();\nSELECT * FROM pg_stat_wal_receiver;' \
    "$(choose_text \
      "pg_basebackup copies a physical base snapshot of the cluster. The -R flag writes recovery configuration so the new node knows how to connect back to the primary. After startup the standby stays read-only and replays WAL received from pg-primary." \
      "The standby is a physical clone of the primary. After startup it stays in recovery and replays incoming WAL.")" \
    "Show the base backup artifacts, pg_is_in_recovery() on the standby, and pg_stat_wal_receiver." \
    "standby.signal and postgresql.auto.conf exist, pg_is_in_recovery() returns true, and pg_stat_wal_receiver shows an active receiver." \
    "The standby starts as a physical copy of the primary. It is read-only because its job is to replay WAL, not to accept independent writes."

  run_cmd "./scripts/init_standby.sh" action_init_standby
  run_cmd "show standby.signal and postgresql.auto.conf on pg-standby" action_show_standby_bootstrap_artifacts
  run_sql $'SELECT pg_is_in_recovery();\nSELECT status, receive_start_lsn, written_lsn, flushed_lsn, latest_end_lsn, slot_name FROM pg_stat_wal_receiver;' action_show_standby_recovery_state
  run_cmd "check client-side connectivity to both PostgreSQL nodes with pg_isready" action_check_database_connectivity
  print_interpretation "pg-standby is alive in recovery mode and is already receiving WAL from pg-primary."
  pause_step "Standby deployment is complete. Press Enter to continue to application data and replication checks."
}

stage_application_data_demo() {
  print_banner "Stage 1. Application Data And Read/Write Demo"

  present_step_block \
    "Stage 1. Application Data Demonstration" \
    "Показать наполнение БД и доступ на запись минимум на примере двух таблиц, столбцов, строк, транзакций и клиентских сессий." \
    "The stand uses two demonstration tables: accounts and transfers. The driver creates the schema, loads seed rows, and prints both table definitions and table contents so the student can point to tables, columns, and rows explicitly." \
    $'./scripts/load_demo.sh\n\\d accounts\n\\d transfers\nTABLE accounts;\nTABLE transfers;' \
    "accounts represents account balances, while transfers stores the history of money movements. This gives a simple but realistic pair of related tables for transaction and replication demos." \
    "Show the table definitions first, then the seed rows in accounts and transfers." \
    "Both tables exist, their columns are visible, and the seed rows are present in the result sets." \
    "I use two connected business tables so I can show not only storage, but also a meaningful transaction that changes one table and inserts into another."

  run_cmd "./scripts/load_demo.sh" action_load_demo
  run_sql $'\\d accounts\n\\d transfers' action_show_table_definitions "pg-primary"
  run_sql $'TABLE accounts;\nTABLE transfers;' action_show_node_data "pg-primary"
  print_interpretation "The lab now has visible tables, visible columns, and visible seed rows, so the later write and replication checks are easy to explain."

  present_step_block \
    "Stage 1. Primary Read/Write Demonstration" \
    "Продемонстрировать доступ в режиме чтение/запись на основном сервере." \
    "The driver runs a real transfer transaction on pg-primary. It debits one account, credits another, inserts a row into transfers, commits the transaction, and prints the updated state." \
    $'./scripts/run_transfer.sh pg-primary 1 2 150.00 stage1-primary-transfer\nSELECT pg_is_in_recovery();\nTABLE accounts;\nTABLE transfers;' \
    "Before failover only the primary accepts writes. The transaction updates balances in accounts and appends an audit row to transfers, which is exactly the kind of cross-table change the lab requires." \
    "Show pg_is_in_recovery() = false on pg-primary, the COMMIT, the updated balances, and the new transfer row." \
    "The transaction succeeds on pg-primary, pg_is_in_recovery() is false there, and the printed data shows the new state." \
    "This proves that the main node is writable and that I am demonstrating a real transaction, not just isolated INSERTs."

  run_cmd "./scripts/run_transfer.sh pg-primary 1 2 150.00 stage1-primary-transfer" action_run_transfer "pg-primary" "1" "2" "150.00" "stage1-primary-transfer"
  run_sql $'SELECT pg_is_in_recovery();\nTABLE accounts;\nTABLE transfers;' action_show_node_data "pg-primary"
  print_interpretation "At this point the assignment requirement for read/write on the main node is satisfied with a real multi-statement transaction."
  pause_step "Application data and primary write access are demonstrated. Press Enter to continue to replication proof."
}

stage_replication_demo() {
  print_banner "Stage 1. Replication Demonstration"

  present_step_block \
    "Stage 1. Replication Demonstration" \
    "Продемонстрировать, что новые данные синхронизируются на резервный сервер." \
    "$(choose_text \
      "After the write on pg-primary, the driver prints the state of both nodes. On the primary you can see pg_stat_replication, and on the standby you can see pg_stat_wal_receiver plus the same table contents." \
      "The driver now shows the same business data on both nodes and the replication views that explain why they match.")" \
    $'./scripts/show_status.sh\nTABLE accounts on pg-primary\nTABLE accounts on pg-standby\nTABLE transfers on pg-primary\nTABLE transfers on pg-standby' \
    "The standby does not re-execute application SQL. It receives WAL from the primary and replays those physical changes locally. That is why the same rows appear on the standby after the primary transaction commits." \
    "Show that the balances and transfer history are the same on both nodes, while the node roles still differ." \
    "The data matches on both nodes, pg_stat_replication is visible on the primary, and pg_stat_wal_receiver is visible on the standby." \
    "This is the key proof of streaming replication: the standby stays read-only, but it receives the same committed state through WAL replay."

  run_cmd "./scripts/show_status.sh" action_show_status
  print_interpretation "Matching table state plus active replication views prove that physical streaming replication is working."
  pause_step "Stage 1 is complete. Press Enter to continue to Stage 2."
}

stage_multiple_client_sessions() {
  print_banner "Stage 2.1. Multiple Client Sessions"

  present_step_block \
    "Stage 2.1. Multiple Client Sessions" \
    "Установить несколько клиентских подключений к СУБД." \
    "$(choose_text \
      "For a live defense the student can open make client1, make client2, and make client3 in separate terminals. During an automated run the driver emulates the same idea by starting three concurrent psql sessions from the client container with distinct PGAPPNAME values." \
      "The driver proves multiple sessions either manually with make client1/2/3 or automatically with three background psql sessions.")" \
    $'make client1\nmake client2\nmake client3\nmake activity\n# automated full-run proof:\nspawn three client sessions with PGAPPNAME=client-1, client-2, client-3' \
    "What matters here is not the exact SQL of the helper sessions, but the fact that PostgreSQL sees multiple independent client backends at the same time." \
    "Show pg_stat_activity and point at application_name values client-1, client-2, and client-3." \
    "pg_stat_activity shows several rows for the current database with distinct application_name values." \
    "The lab requires several client sessions. I satisfy that requirement with separate psql sessions coming from the dedicated client container."

  run_cmd "spawn three concurrent psql sessions from the client container" spawn_demo_sessions
  run_sql "SELECT * FROM pg_stat_activity WHERE datname = current_database();" action_show_activity
  print_interpretation "Multiple visible backends in pg_stat_activity are the direct proof that several client sessions are connected at the same time."

  present_step_block \
    "Stage 2.1. Client Read/Write Demonstration" \
    "Продемонстрировать состояние данных и работу клиентов в режиме чтение/запись." \
    "While several client sessions are connected, the driver executes another transfer transaction on pg-primary and then prints the current business data." \
    $'./scripts/run_transfer.sh pg-primary 1 2 25.00 stage2-multi-session-transfer\nTABLE accounts;\nTABLE transfers;' \
    "This shows that the cluster is still serving read/write work normally before the failure is introduced." \
    "Show the successful COMMIT and the updated data while the extra client sessions are still visible in pg_stat_activity." \
    "The write succeeds on pg-primary and the data reflects the new transfer while multiple sessions remain connected." \
    "Here I prove that the system is healthy before the fault: several clients are connected and write traffic still works."

  run_cmd "./scripts/run_transfer.sh pg-primary 1 2 25.00 stage2-multi-session-transfer" action_run_transfer "pg-primary" "1" "2" "25.00" "stage2-multi-session-transfer"
  run_sql $'TABLE accounts;\nTABLE transfers;' action_show_node_data "pg-primary"
  print_interpretation "Stage 2.1 is satisfied because the demo now shows several client sessions and a real write workload before the failure."
  cleanup_demo_sessions
  pause_step "Client-session demonstration is complete. Press Enter to continue to the disk-full failure."
}

stage_failure_simulation() {
  print_banner "Stage 2.2. Failure Simulation"

  present_step_block \
    "Stage 2.2. Failure Simulation" \
    "Симулировать переполнение дискового пространства на основном узле — заполнить всё свободное пространство раздела с PGDATA мусорными файлами." \
    "$(choose_text \
      "In this Docker stand the primary PGDATA lives on a size-limited tmpfs volume. The driver shows the filesystem usage, writes a ballast file directly into \$PGDATA/fill/ballast.bin until ENOSPC occurs, and then forces a write attempt through PostgreSQL." \
      "The primary really uses a limited filesystem for PGDATA. The driver fills that exact filesystem with a ballast file until no free space remains.")" \
    $'df -h "$PGDATA"\n./scripts/fill_primary_disk.sh\nls -lh "$PGDATA/fill/ballast.bin"\nINSERT INTO transfers ... generate_series(...)' \
    "This is not a verbal simulation. PostgreSQL data files and WAL live in the same constrained filesystem, so when that filesystem is full the server can no longer extend relation files or write safely." \
    "Show the filesystem usage before and after, show the ballast file path, and show that the test write starts failing." \
    "dd ends with No space left on device, the ballast file exists in PGDATA, and the follow-up PostgreSQL write fails." \
    "At this step I reproduce a real storage failure on the main node. PostgreSQL stops being able to extend files safely, so continuing to use this node for writes is no longer acceptable."

  run_cmd 'df -h "$PGDATA" on pg-primary' action_show_primary_fs_usage
  run_cmd 'write ballast.bin into $PGDATA/fill until ENOSPC' action_fill_primary_data_fs
  run_cmd 'df -h "$PGDATA" on pg-primary after filling the filesystem' action_show_primary_fs_usage
  run_cmd 'show the ballast file created inside PGDATA' action_show_primary_ballast_file
  run_cmd "force a PostgreSQL write on the full primary" action_trigger_full_primary_write_failure
  print_interpretation "If the ballast file is inside PGDATA and the write fails after ENOSPC, the failure has been reproduced correctly."
  pause_step "The disk-full failure is reproduced. Press Enter to continue to log analysis."
}

stage_log_analysis() {
  print_banner "Stage 2.3. Log Analysis"

  present_step_block \
    "Stage 2.3. Log Analysis" \
    "Найти и продемонстрировать в логах релевантные сообщения об ошибках." \
    "PostgreSQL logs are stored outside the limited PGDATA filesystem, so the driver can still print them after ENOSPC. The log filter looks for No space left on device, could not extend file, could not write, checkpoint failures, and severe messages such as ERROR, FATAL, or PANIC." \
    $'./scripts/tail_logs.sh pg-primary \'No space left on device|could not write|could not extend file|PANIC|FATAL|ERROR|checkpoint\'' \
    "These messages are the server-side proof of the fault. SQL failure alone is not enough during a defense; the log shows the storage-level cause recognized by PostgreSQL itself." \
    "Open the filtered primary log and point at the lines that explicitly mention lack of space or file extension failure." \
    "The log contains lines with No space left on device or closely related write/extend/checkpoint failures." \
    "The important part here is that PostgreSQL itself confirms the storage problem in the log, so failover is justified by direct server evidence."

  run_cmd "print the filtered primary log excerpt" action_show_primary_error_logs
  print_interpretation "Messages such as 'No space left on device' and 'could not extend file' directly connect the application failure to the exhausted PGDATA filesystem."
  pause_step "Log analysis is complete. Press Enter to continue to failover."
}

stage_failover() {
  print_banner "Stage 2.3. Failover"

  present_step_block \
    "Stage 2.3. Failover" \
    "Выполнить переключение (failover) на резервный сервер." \
    "The driver stops the failed primary if it is still running and promotes pg-standby with pg_ctl promote. After promotion, pg-standby must leave recovery and become the new writable primary." \
    $'./scripts/failover.sh\ngosu postgres pg_ctl -D "$PGDATA" promote\nSELECT pg_is_in_recovery();' \
    "Because the standby has been continuously streaming and replaying WAL, it already contains the latest replicated state. Promotion tells it to stop waiting for the old primary and to take ownership of writes." \
    "Show the promote step and then show pg_is_in_recovery() = false on pg-standby." \
    "pg-standby leaves recovery, reports pg_is_in_recovery() = false, and is ready to accept writes." \
    "I promote the standby because the original primary can no longer write safely. The standby already has the replicated state, so it becomes the new source of truth."

  run_cmd "./scripts/failover.sh" action_failover_to_standby
  print_interpretation "Once pg-standby reports pg_is_in_recovery() = false, the failover has actually happened."

  present_step_block \
    "Stage 2.3. Read/Write After Failover" \
    "Продемонстрировать состояние данных и работу клиентов в режиме чтение/запись уже после переключения." \
    "After failover the driver writes to the promoted pg-standby, then prints the current topology and the updated data. The stopped pg-primary is no longer the writable node." \
    $'./scripts/run_transfer.sh pg-standby 2 3 75.00 stage2-post-failover-transfer\n./scripts/show_status.sh' \
    "This is the practical proof that the standby is no longer a passive replica. It has become the active primary for new transactions." \
    "Show the successful write on pg-standby, its pg_is_in_recovery() = false state, and the updated table contents." \
    "A new transaction succeeds on pg-standby and the printed data includes the post-failover transfer." \
    "After promotion the former standby is the new primary. Successful write traffic on that node is the clearest proof that failover worked."

  run_cmd "./scripts/run_transfer.sh pg-standby 2 3 75.00 stage2-post-failover-transfer" action_run_transfer "pg-standby" "2" "3" "75.00" "stage2-post-failover-transfer"
  run_cmd "./scripts/show_status.sh" action_show_status
  print_interpretation "The cluster remains operational because writes continue on the promoted node even though the old primary is out of service."
  pause_step "Failover demonstration is complete. Press Enter to continue to recovery of the old primary."
}

stage_old_primary_recovery() {
  print_banner "Stage 3. Recovery Of The Former Primary"

  present_step_block \
    "Stage 3. Recovery Of The Former Primary" \
    "Восстановить работу основного узла." \
    "$(choose_text \
      "The driver rebuilds the old pg-primary from the current primary, which is now pg-standby. This means wiping the stale data directory, taking a fresh pg_basebackup, and starting the former primary back up as a standby." \
      "The old primary is not restarted as primary. It is rebuilt from the new primary and returned as a standby.")" \
    $'./scripts/recover_primary.sh\npg_basebackup -R -X stream -C -S primary_slot\nSELECT pg_is_in_recovery();\nSELECT * FROM pg_stat_wal_receiver;' \
    "After failover the current source of truth is the promoted standby. The old primary must be made consistent with that new history before it can rejoin the cluster." \
    "Show the recovery command and then show that pg-primary comes back with pg_is_in_recovery() = true and an active WAL receiver." \
    "pg-primary starts successfully, reports pg_is_in_recovery() = true, and shows pg_stat_wal_receiver activity." \
    "I am not turning the old primary back on blindly. I rebuild it from the current primary so it rejoins the cluster with a consistent WAL history."

  run_cmd "./scripts/recover_primary.sh" action_recover_primary_as_standby
  print_interpretation "The former primary is now healthy again, but only in the correct role: it has rejoined as a standby."

  present_step_block \
    "Stage 3. Failure Cause Removal" \
    "Откатить действие, вызвавшее отказ." \
    "The original failure cause was the ballast file inside the full PGDATA filesystem. During recovery the old PGDATA is replaced entirely, so the ballast file and the broken state disappear together." \
    $'ls -lh "$PGDATA/fill/ballast.bin" || echo "ballast.bin is absent because PGDATA was rebuilt"' \
    "Simply deleting one file would not be enough, because the old primary also has the wrong WAL history after failover. Rebuilding PGDATA removes both the storage symptom and the logical inconsistency." \
    "Show that ballast.bin is no longer present on the rebuilt pg-primary." \
    "The ballast file is absent, and the node starts normally because it now has a clean data directory." \
    "The root cause was disk exhaustion inside PGDATA. Recovery removes that cause by rebuilding the whole data directory from a healthy source."

  run_cmd 'verify that ballast.bin is absent after recovery' action_check_ballast_absent
  print_interpretation "Absence of ballast.bin plus successful startup proves that the storage-failure trigger has been removed."

  present_step_block \
    "Stage 3. Catch-Up After Failover" \
    "Актуализировать состояние базы на основном узле — накатить изменения, выполненные после failover." \
    "After the former primary is back as a standby, the driver writes one more transaction to the current primary and then shows that the same change appears on the rebuilt pg-primary." \
    $'./scripts/run_transfer.sh pg-standby 1 3 60.00 stage3-catch-up-transfer\n./scripts/show_status.sh' \
    "This proves that the old primary not only restarted, but also resumed WAL reception from the promoted node and caught up with post-failover changes." \
    "Show the new transaction on the current primary and then show the same updated state on the rebuilt pg-primary." \
    "The post-failover transaction appears on both nodes, and pg-primary still shows WAL receiver activity." \
    "Now I prove that the old primary is no longer stale. It is actively catching up with the changes that happened after failover."

  run_cmd "./scripts/run_transfer.sh pg-standby 1 3 60.00 stage3-catch-up-transfer" action_run_transfer "pg-standby" "1" "3" "60.00" "stage3-catch-up-transfer"
  run_cmd "./scripts/show_status.sh" action_show_status
  print_interpretation "The former primary is now synchronized with the new primary and is ready for a controlled switchback."
  pause_step "Recovery of the former primary is complete. Press Enter to continue to restoring the original topology."
}

stage_switchback() {
  print_banner "Stage 3. Return To Original Topology"

  present_step_block \
    "Stage 3. Return To Original Topology" \
    "Восстановить исправную работу узлов в исходной конфигурации." \
    "$(choose_text \
      "The driver waits until pg-primary has zero replay lag relative to the current primary, stops the current primary cleanly, promotes pg-primary, and then reseeds pg-standby back from pg-primary." \
      "The driver waits for zero lag, promotes pg-primary back, and rebuilds pg-standby from it.")" \
    $'./scripts/switchback.sh\nSELECT pg_current_wal_lsn();\nSELECT pg_last_wal_replay_lsn();\nSELECT pg_wal_lsn_diff(...);' \
    "The switchback must be controlled. If pg-primary were promoted before it had replayed all outstanding WAL, the cluster could lose post-failover transactions. That is why the script checks LSN lag first." \
    "Show that lag is checked before promotion and then show the final topology where pg-primary is primary again." \
    "pg-primary becomes the writable node again, pg-standby returns to recovery mode, and the data matches on both nodes." \
    "Before returning to the original roles, I wait for the old primary to fully catch up. That prevents data loss during switchback."

  run_cmd "./scripts/switchback.sh" action_switchback_to_original_topology
  print_interpretation "The original primary/standby role split is restored without discarding the transactions written after failover."
  pause_step "The original topology is restored. Press Enter to continue to the final health checks."
}

stage_final_health_checks() {
  print_banner "Stage 3. Final Health Checks"

  present_step_block \
    "Stage 3. Final Health Checks" \
    "Продемонстрировать состояние данных и работу клиентов в режиме чтение/запись." \
    "As the final proof, the driver writes one more transaction on pg-primary after switchback and then prints the state of both nodes again." \
    $'./scripts/run_transfer.sh pg-primary 3 2 40.00 final-switchback-transfer\n./scripts/show_status.sh' \
    "This closes the full lab cycle: initial primary/standby configuration, failure, failover, recovery of the former primary, and return to the original topology with live replication still working." \
    "Show a successful write on pg-primary, then show pg-primary as primary and pg-standby as standby with the same final data." \
    "The write succeeds on pg-primary, pg_is_in_recovery() is false there and true on pg-standby, and both nodes show the same final business state." \
    "This final step proves that the cluster is healthy again in the original topology, not just temporarily alive after failover."

  run_cmd "./scripts/run_transfer.sh pg-primary 3 2 40.00 final-switchback-transfer" action_run_transfer "pg-primary" "3" "2" "40.00" "final-switchback-transfer"
  run_cmd "./scripts/show_status.sh" action_show_status
  print_interpretation "The lab cycle is complete: the original topology is restored and replication still works after the entire failure-and-recovery chain."
  pause_step "Final health checks are complete. Press Enter to finish the defense driver."
}

run_stage1() {
  stage_infrastructure_startup
  stage_primary_deployment
  stage_standby_deployment
  stage_application_data_demo
  stage_replication_demo
}

run_stage2() {
  present_step_block \
    "Stage 2 Prerequisite Check" \
    "Prepare stage 2 only after stage 1 has already produced a healthy primary/standby topology." \
    "The driver verifies that pg-primary is writable, pg-standby is in recovery, and the demo tables already exist before it attempts the failure scenario." \
    $'./scripts/show_status.sh\nSELECT pg_is_in_recovery();\nSELECT count(*) FROM information_schema.tables WHERE table_name IN (\'accounts\', \'transfers\');' \
    "Failing over from the wrong starting point makes the demonstration misleading. The guard keeps the scenario honest." \
    "Show the guard or its success before entering the failure section." \
    "The guard passes only if the expected stage 1 state is already in place." \
    "Before simulating a failure, I first confirm that the cluster is healthy and in the expected initial topology."
  run_cmd "assert that stage1 topology and demo data are ready" action_assert_stage1_ready
  print_interpretation "Stage 2 starts only from a clean, explainable stage 1 state."
  pause_step "The stage 2 prerequisite check is complete. Press Enter to continue to multiple client sessions."

  stage_multiple_client_sessions
  stage_failure_simulation
  stage_log_analysis
  stage_failover
}

run_stage3() {
  present_step_block \
    "Stage 3 Prerequisite Check" \
    "Prepare stage 3 only after failover has already been performed." \
    "The driver verifies that pg-standby is already the promoted writable node before it starts recovering the old primary." \
    $'./scripts/show_status.sh\nSELECT pg_is_in_recovery(); -- on pg-standby' \
    "Recovery and switchback only make sense if failover has already happened and pg-standby currently owns writes." \
    "Show that the script refuses to continue if failover was not done yet." \
    "The guard passes only if pg-standby is already out of recovery and acting as the current primary." \
    "Stage 3 assumes that failover already happened, so I verify that first instead of pretending the topology is something else."
  run_cmd "assert that failover has already happened and pg-standby is current primary" action_assert_failover_happened
  print_interpretation "Stage 3 continues only when the promoted-standby topology already exists."
  pause_step "The stage 3 prerequisite check is complete. Press Enter to continue to recovery of the former primary."

  stage_old_primary_recovery
  stage_switchback
  stage_final_health_checks
}

main() {
  init_colors
  parse_args "$@"
  ensure_pause_compatibility
  export LAB_DRIVER_MODE=1
  if (( TEACHER_MODE )); then
    export LAB_QUIET=1
  fi

  trap cleanup_demo_sessions EXIT

  print_mode_summary
  stage_environment_validation

  case "$TARGET_STAGE" in
    full)
      run_stage1
      run_stage2
      run_stage3
      ;;
    stage1)
      run_stage1
      ;;
    stage2)
      run_stage2
      ;;
    stage3)
      run_stage3
      ;;
    *)
      die "Unsupported stage selector: $TARGET_STAGE"
      ;;
  esac

  print_banner "Defense Driver Completed"
  printf 'Completed target: %s\n' "$TARGET_STAGE"
  if (( EXPLAIN_ONLY )); then
    printf 'No state-changing commands were executed because explain-only mode was enabled.\n'
  else
    printf 'The requested scenario has finished. Use the printed explanations and evidence blocks as the spoken defense script.\n'
  fi
}

main "$@"
