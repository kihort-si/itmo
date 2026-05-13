#!/usr/bin/env bash
# Управление Docker-окружением blps-lab
# Использование:
#   ./docker-manage.sh up [taiga]     — запустить инфру (+ Taiga если указано)
#   ./docker-manage.sh down [taiga]   — остановить инфру (+ Taiga если указано)
#   ./docker-manage.sh restart [taiga]— down + up
#   ./docker-manage.sh ports          — показать, какие процессы занимают порты
#   ./docker-manage.sh free-ports     — убить процессы, занимающие порты
#   ./docker-manage.sh status         — статус контейнеров
#   ./docker-manage.sh logs [svc]     — логи всех контейнеров или одного сервиса
#   ./docker-manage.sh taiga-token    — получить Bearer-токен из Taiga
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; }
head_() { echo -e "${CYAN}$*${NC}"; }

# ---------- профили по умолчанию / с Taiga -----------------------------------
profiles_for() {
    local with_taiga="${1:-}"
    if [[ "$with_taiga" == "taiga" ]]; then
        echo "--profile infra --profile taiga"
    else
        echo "--profile infra"
    fi
}

# ---------- порты из docker-compose (хостовые, с учётом env-переменных) ------
get_ports() {
    echo "${ACTIVEMQ_OPENWIRE_PORT:-61616}  activemq(openwire)"
    echo "${ACTIVEMQ_STOMP_PORT:-61613}     activemq(stomp)"
    echo "${ACTIVEMQ_CONSOLE_PORT:-8161}    activemq(console)"
    echo "${POSTGRES_PORT:-5432}            postgres"
    echo "${TAIGA_PORT:-9000}               taiga-gateway"
}

# ---------- показать процессы на портах --------------------------------------
cmd_ports() {
    head_ "\n=== Процессы на портах docker-compose ==="
    printf "%-8s %-26s %-10s %s\n" "PORT" "SERVICE" "PID" "PROCESS"
    printf '%0.s-' {1..68}; echo

    while IFS= read -r line; do
        port=$(echo "$line" | awk '{print $1}')
        svc=$(echo "$line"  | awk '{print $2}')

        pid=$(ss -tlnp "sport = :${port}" 2>/dev/null \
            | awk 'NR>1 && /LISTEN/ {match($0,/pid=([0-9]+)/,a); if(a[1]) print a[1]}' \
            | head -1)

        if [[ -n "$pid" ]]; then
            pname=$(ps -p "$pid" -o comm= 2>/dev/null || echo "?")
            printf "%-8s %-26s %-10s %s\n" "$port" "$svc" "$pid" "$pname"
        else
            printf "%-8s %-26s %-10s %s\n" "$port" "$svc" "(свободен)" ""
        fi
    done < <(get_ports)
    echo
}

# ---------- убить процессы на портах ----------------------------------------
cmd_free_ports() {
    head_ "\n=== Освобождение портов ==="
    killed=0

    while IFS= read -r line; do
        port=$(echo "$line" | awk '{print $1}')
        svc=$(echo "$line"  | awk '{print $2}')

        pids=$(ss -tlnp "sport = :${port}" 2>/dev/null \
            | awk 'NR>1 && /LISTEN/ {
                while (match($0,/pid=([0-9]+)/,a)) {
                    print a[1]; $0=substr($0,RSTART+RLENGTH)
                }
              }' | sort -u)

        if [[ -z "$pids" ]]; then
            info "Порт $port ($svc) — свободен"
            continue
        fi

        for pid in $pids; do
            pname=$(ps -p "$pid" -o comm= 2>/dev/null || echo "?")
            if kill -0 "$pid" 2>/dev/null; then
                warn "Порт $port ($svc): убиваю PID $pid ($pname)..."
                kill -TERM "$pid" 2>/dev/null || true
                sleep 1
                kill -0 "$pid" 2>/dev/null && kill -KILL "$pid" 2>/dev/null || true
                info "PID $pid завершён"
                killed=$((killed + 1))
            fi
        done
    done < <(get_ports)

    [[ $killed -eq 0 ]] && info "Все порты были свободны" || info "Завершено процессов: $killed"
    echo
}

# ---------- поднять контейнеры -----------------------------------------------
cmd_up() {
    local profiles
    profiles=$(profiles_for "${1:-}")
    info "Запуск контейнеров (${profiles})..."
    # shellcheck disable=SC2086
    docker compose -f "$COMPOSE_FILE" $profiles up -d
    echo
    # shellcheck disable=SC2086
    docker compose -f "$COMPOSE_FILE" $profiles ps
    echo
}

# ---------- остановить и удалить контейнеры -----------------------------------
cmd_down() {
    local profiles
    profiles=$(profiles_for "${1:-}")
    info "Остановка и удаление контейнеров (${profiles})..."
    # shellcheck disable=SC2086
    docker compose -f "$COMPOSE_FILE" $profiles down
    echo
}

# ---------- статус -----------------------------------------------------------
cmd_status() {
    head_ "\n=== Статус контейнеров ==="
    docker compose -f "$COMPOSE_FILE" --profile infra --profile taiga ps
    echo
    cmd_ports
}

# ---------- логи -------------------------------------------------------------
cmd_logs() {
    local svc="${1:-}"
    if [[ -n "$svc" ]]; then
        docker compose -f "$COMPOSE_FILE" --profile infra --profile taiga logs -f "$svc"
    else
        docker compose -f "$COMPOSE_FILE" --profile infra --profile taiga logs -f
    fi
}

# ---------- получить Bearer-токен из Taiga -----------------------------------
cmd_taiga_token() {
    local taiga_url="http://localhost:${TAIGA_PORT:-9000}"
    head_ "\n=== Получение Taiga Bearer-токена ==="
    read -rp "  Логин: " taiga_user
    read -rsp "  Пароль: " taiga_pass
    echo

    token=$(curl -sf -X POST "${taiga_url}/api/v1/auth" \
        -H "Content-Type: application/json" \
        -d "{\"type\":\"normal\",\"username\":\"${taiga_user}\",\"password\":\"${taiga_pass}\"}" \
        | python3 -c "import sys,json; print(json.load(sys.stdin)['auth_token'])" 2>/dev/null || true)

    if [[ -z "$token" ]]; then
        error "Не удалось получить токен. Проверьте, что Taiga запущена на ${taiga_url} и данные верны."
        exit 1
    fi

    info "Bearer-токен:"
    echo
    echo "  $token"
    echo
    info "Установите в ra.xml или переменную среды TAIGA_BEARER_TOKEN."
}

# ---------- диспетчер --------------------------------------------------------
CMD="${1:-help}"
shift || true

case "$CMD" in
    up)           cmd_up "${1:-}" ;;
    down)         cmd_down "${1:-}" ;;
    restart)      cmd_down "${1:-}"; cmd_up "${1:-}" ;;
    ports)        cmd_ports ;;
    free-ports)   cmd_free_ports ;;
    status)       cmd_status ;;
    logs)         cmd_logs "${1:-}" ;;
    taiga-token)  cmd_taiga_token ;;
    help|-h|--help)
        sed -n '3,11p' "$0"
        ;;
    *)
        error "Неизвестная команда: $CMD"
        sed -n '3,11p' "$0"
        exit 1
        ;;
esac
