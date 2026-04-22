#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# deploy-local.sh — сборка и деплой blps-lab на локальный WildFly
# ==============================================================================
# Использование:
#   ./deploy-local.sh [--wildfly-home PATH] [--skip-build] [--start-wildfly]
#
# Переменные окружения:
#   WILDFLY_HOME         — путь к локальному WildFly (обязательно или через флаг)
#   WILDFLY_MGMT_PORT    — порт management HTTP API     (default: 25204)
#   WILDFLY_MGMT_USER    — логин management             (default: admin)
#   WILDFLY_MGMT_PASS    — пароль management            (default: пусто)
#   APP_PORT             — порт приложения              (default: 25203)
#   DB_URL               — JDBC URL                     (default: localhost PostgreSQL)
#   USER_STORE_PATH      — путь к XML-хранилищу         (default: /tmp/blps/security/users.xml)
# ==============================================================================

# ---------- значения по умолчанию --------------------------------------------
WILDFLY_HOME="${WILDFLY_HOME:-}"
WILDFLY_MGMT_PORT="${WILDFLY_MGMT_PORT:-25204}"
WILDFLY_MGMT_USER="${WILDFLY_MGMT_USER:-admin}"
WILDFLY_MGMT_PASS="${WILDFLY_MGMT_PASS:-admin}"
APP_PORT="${APP_PORT:-25203}"
DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/studs}"
USER_STORE_PATH="${USER_STORE_PATH:-/tmp/blps/security/users.xml}"
SKIP_BUILD=false
START_WILDFLY=false
ARTIFACT_NAME="app-0.0.1-SNAPSHOT.war"
DEPLOY_NAME="blps-lab.war"

# ---------- цвета ------------------------------------------------------------
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*" >&2; }
die()   { error "$*"; exit 1; }

# ---------- аргументы командной строки ---------------------------------------
while [[ $# -gt 0 ]]; do
    case $1 in
        --wildfly-home)  WILDFLY_HOME="$2";       shift 2 ;;
        --mgmt-port)     WILDFLY_MGMT_PORT="$2";  shift 2 ;;
        --mgmt-user)     WILDFLY_MGMT_USER="$2";  shift 2 ;;
        --mgmt-pass)     WILDFLY_MGMT_PASS="$2";  shift 2 ;;
        --app-port)      APP_PORT="$2";           shift 2 ;;
        --db-url)        DB_URL="$2";             shift 2 ;;
        --user-store)    USER_STORE_PATH="$2";    shift 2 ;;
        --skip-build)    SKIP_BUILD=true;         shift   ;;
        --start-wildfly) START_WILDFLY=true;      shift   ;;
        -h|--help)
            sed -n '3,19p' "$0"; exit 0 ;;
        *) die "Неизвестный аргумент: $1. Используйте -h для справки." ;;
    esac
done

# ---------- проверка WILDFLY_HOME --------------------------------------------
[[ -n "$WILDFLY_HOME" ]] \
    || die "Укажите путь к WildFly: --wildfly-home PATH  или  WILDFLY_HOME=PATH"
[[ -d "$WILDFLY_HOME" ]] \
    || die "Директория WildFly не найдена: $WILDFLY_HOME"

WILDFLY_CLI="${WILDFLY_HOME}/bin/jboss-cli.sh"
WILDFLY_STANDALONE="${WILDFLY_HOME}/bin/standalone.sh"
[[ -x "$WILDFLY_CLI" ]]        || die "jboss-cli.sh не найден: $WILDFLY_CLI"
[[ -x "$WILDFLY_STANDALONE" ]] || die "standalone.sh не найден: $WILDFLY_STANDALONE"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WAR_PATH="${SCRIPT_DIR}/build/libs/${ARTIFACT_NAME}"

# ---------- вспомогательная функция: вызов jboss-cli с учётными данными ------
cli() {
    local creds=()
    [[ -n "$WILDFLY_MGMT_USER" ]] && creds+=(--user="$WILDFLY_MGMT_USER")
    [[ -n "$WILDFLY_MGMT_PASS" ]] && creds+=(--password="$WILDFLY_MGMT_PASS")
    "$WILDFLY_CLI" --connect \
        --controller="localhost:${WILDFLY_MGMT_PORT}" \
        "${creds[@]}" \
        "$@"
}

# ---------- вспомогательная функция: проверка доступности WildFly ------------
wildfly_running() {
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 \
        "http://localhost:${WILDFLY_MGMT_PORT}/management" 2>/dev/null || true)
    # 200 — без аутентификации, 401 — требует пароль (WildFly запущен)
    [[ "$code" =~ ^(200|401)$ ]]
}

# ---------- шаг 1: сборка WAR ------------------------------------------------
if [[ "$SKIP_BUILD" == false ]]; then
    info "Сборка WAR..."
    cd "$SCRIPT_DIR"
    ./gradlew bootWar --no-daemon -q \
        || die "Сборка завершилась с ошибкой"
    info "Сборка завершена: ${WAR_PATH}"
else
    warn "Сборка пропущена (--skip-build)"
fi

[[ -f "$WAR_PATH" ]] || die "WAR не найден: ${WAR_PATH}. Запустите без --skip-build."

# ---------- шаг 2: проверка / запуск WildFly ---------------------------------
MGMT_URL="http://localhost:${WILDFLY_MGMT_PORT}/management"

if wildfly_running; then
    info "WildFly уже запущен (management: ${MGMT_URL})"
elif [[ "$START_WILDFLY" == true ]]; then
    info "Запуск WildFly..."
    mkdir -p "$(dirname "$USER_STORE_PATH")"

    JAVA_OPTS_EXTRA="\
        -DDB_URL=${DB_URL} \
        -DUSER_STORE_PATH=${USER_STORE_PATH} \
        -DJTA_ENABLED=false \
        -DPROCESS_JMS_ENABLED=false \
        -DDDL_AUTO=update \
        -DSQL_INIT_MODE=always \
        -DSERVER_PORT=${APP_PORT}"

    JAVA_OPTS="$JAVA_OPTS_EXTRA" \
        "$WILDFLY_STANDALONE" \
            -Djboss.bind.address=0.0.0.0 \
            -Djboss.bind.address.management=0.0.0.0 \
            -Djboss.management.http.port="${WILDFLY_MGMT_PORT}" \
            -Djboss.http.port="${APP_PORT}" \
            > /tmp/wildfly-local.log 2>&1 &

    WILDFLY_PID=$!
    info "WildFly PID: $WILDFLY_PID (лог: /tmp/wildfly-local.log)"
    info "Ожидание готовности WildFly (до 90 сек)..."

    for i in $(seq 1 45); do
        sleep 2
        if wildfly_running; then
            info "WildFly готов"
            break
        fi
        if [[ $i -eq 45 ]]; then
            die "WildFly не запустился за 90 сек. Лог: /tmp/wildfly-local.log"
        fi
    done
else
    die "WildFly не запущен на ${MGMT_URL}.
  Запустите WildFly вручную или добавьте флаг --start-wildfly:
    ${WILDFLY_HOME}/bin/standalone.sh \\
        -Djboss.http.port=${APP_PORT} \\
        -Djboss.management.http.port=${WILDFLY_MGMT_PORT} \\
        -DDB_URL=${DB_URL} \\
        -DUSER_STORE_PATH=${USER_STORE_PATH} \\
        -DJTA_ENABLED=false \\
        -DPROCESS_JMS_ENABLED=false"
fi

# ---------- шаг 3: деплой WAR ------------------------------------------------
info "Деплой ${DEPLOY_NAME}..."

# Переразвернуть если уже задеплоено
if cli --command="deployment-info --name=${DEPLOY_NAME}" >/dev/null 2>&1; then
    info "Обнаружен существующий деплой — выполняю undeploy..."
    cli --command="undeploy ${DEPLOY_NAME}" \
        || die "Не удалось удалить старый деплой"
fi

# Создать директорию для XML-хранилища
mkdir -p "$(dirname "$USER_STORE_PATH")"

cli --command="deploy ${WAR_PATH} --name=${DEPLOY_NAME} --runtime-name=${DEPLOY_NAME}" \
    || die "Деплой завершился с ошибкой"

# ---------- шаг 4: ожидание деплоя ------------------------------------------
info "Ожидание завершения деплоя..."
for i in $(seq 1 20); do
    STATUS=$(cli --command="deployment-info --name=${DEPLOY_NAME}" 2>/dev/null \
        | awk '/STATUS/{print $2}' || true)
    if [[ "$STATUS" == "OK" ]]; then
        info "Деплой успешен (статус: OK)"
        break
    fi
    if [[ "$STATUS" == "FAILED" ]]; then
        die "Деплой завершился с ошибкой (статус: FAILED). Проверьте логи WildFly:
  ${WILDFLY_HOME}/standalone/log/server.log"
    fi
    if [[ $i -eq 20 ]]; then
        warn "Ожидание превышено. Последний статус: '${STATUS}'. Проверьте лог WildFly."
    fi
    sleep 2
done

# ---------- шаг 5: проверка доступности приложения ---------------------------
info "Проверка доступности приложения..."
HEALTH_URL="http://localhost:${APP_PORT}/blps-lab/api/catalog/products"

for i in $(seq 1 15); do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
        --max-time 5 "${HEALTH_URL}" 2>/dev/null || true)
    if [[ "$HTTP_CODE" =~ ^(200|401|403)$ ]]; then
        info "Приложение отвечает (HTTP ${HTTP_CODE})"
        break
    fi
    if [[ $i -eq 15 ]]; then
        warn "Приложение не отвечает. Проверьте лог:
  ${WILDFLY_HOME}/standalone/log/server.log"
    fi
    sleep 3
done

# ---------- итог -------------------------------------------------------------
echo
echo -e "${GREEN}=============================="
echo -e "  Деплой завершён"
echo -e "==============================${NC}"
echo -e "  Приложение:    http://localhost:${APP_PORT}/blps-lab"
echo -e "  Swagger UI:    http://localhost:${APP_PORT}/blps-lab/swagger-ui/index.html"
echo -e "  WildFly admin: http://localhost:${WILDFLY_MGMT_PORT}"
echo -e "  Лог WildFly:   ${WILDFLY_HOME}/standalone/log/server.log"
echo
