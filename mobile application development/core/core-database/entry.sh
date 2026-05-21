#!/bin/bash
set -e

run_liquibase() {
    local service_name=$1
    local db_name=$2
    local project_path="/opt/liquibase-projects/${service_name}"

    echo "Start migrations $db_name"

    cd "$project_path" && liquibase \
    --password="$POSTGRES_PASSWORD" --username "$POSTGRES_USER" \
    --url="jdbc:postgresql://localhost:5432/${db_name}" \
    --driver="org.postgresql.Driver" \
    --changeLogFile "changelog.xml" \
    update
}

docker-entrypoint.sh postgres \
    -c "port=${POSTGRES_PORT:-5432}" \
    -c "max_connections=${POSTGRES_MAX_CONNECTIONS:-300}" \
    -c "shared_buffers=${POSTGRES_SHARED_BUFFERS:-512MB}" \
    -c "effective_cache_size=${POSTGRES_EFFECTIVE_CACHE_SIZE:-4GB}" \
    -c "work_mem=${POSTGRES_WORK_MEM:-10MB}" \
    -c "maintenance_work_mem=${POSTGRES_MAINTENANCE_WORK_MEM:-64MB}" \
    -c "random_page_cost=${POSTGRES_RANDOM_PAGE_COST:-4.0}" \
    -c "temp_file_limit=${POSTGRES_TEMP_FILE_LIMIT:--1}" \
    -c "log_min_duration_statement=${POSTGRES_LOG_MIN_DURATION_STATEMENT:--1}" \
    -c "idle_in_transaction_session_timeout=${POSTGRES_IDLE_IN_TRANSACTION_SESSION_TIMEOUT:-0}" \
    -c "lock_timeout=${POSTGRES_LOCK_TIMEOUT:-0}" \
    -c "statement_timeout=${POSTGRES_STATEMENT_TIMEOUT:-0}" \
    -c "shared_preload_libraries=${POSTGRES_SHARED_PRELOAD_LIBRARIES:-}" \
    -c "pg_stat_statements.max=${POSTGRES_STAT_STATEMENTS_MAX:-5000}" \
    -c "pg_stat_statements.track=${POSTGRES_STAT_STATEMENTS_TRACK:-top}" &

until pg_isready -U "$POSTGRES_USER" -p "$POSTGRES_PORT"; do
    sleep 1
done

sleep 7

until pg_isready -U "$POSTGRES_USER" -p "$POSTGRES_PORT"; do
    sleep 1
done



if [[ -f "/opt/liquibase-projects/migrations" ]]; then
    dos2unix /opt/liquibase-projects/migrations
    grep -v '^[[:space:]]*$' /opt/liquibase-projects/migrations | grep -v '^#' | while read -r service; do
        service=$(echo "$service" | xargs) 
        run_liquibase "${service}" "$APP_DATABASE"
    done
else
    echo "Warning: File /opt/liquibase-projects/migrations not found"
fi

wait