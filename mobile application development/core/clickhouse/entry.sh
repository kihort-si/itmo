#!/bin/bash
set -e

run_liquibase() {
    local service_name=$1
    local db_name=$2
    local project_path="/opt/liquibase-projects/${service_name}"

    echo "Start migrations $db_name"

    cd "$project_path" && liquibase \
    --password="$CLICKHOUSE_PASSWORD" --username "$CLICKHOUSE_USER" \
    --url="jdbc:clickhouse://localhost:8123/${db_name}" \
    --driver="com.clickhouse.jdbc.ClickHouseDriver" \
    --changeLogFile "changelog.xml" \
    update
}

/entrypoint.sh &

until clickhouse-client --query "SELECT 1" &>/dev/null; do
    sleep 1
done

sleep 7

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