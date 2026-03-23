#!/usr/bin/env bash
# Сценарий A: Самовывоз (PICKUP) + Онлайн-оплата + Промокод (всё в checkout)
# Сценарий B: Курьерская доставка (COURIER_DELIVERY) + Оплата при получении (всё в checkout)
set -uo pipefail

BASE="http://localhost:23204/api"
CT="Content-Type: application/json"

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

ERRORS=0

banner() {
  echo ""
  echo -e "${BOLD}╔══════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}║  $1${NC}"
  echo -e "${BOLD}╚══════════════════════════════════════════════════════════════╝${NC}"
}
step() { echo -e "\n${CYAN}===== $1 =====${NC}"; }
info() { echo -e "${YELLOW}→ $1${NC}"; }
ok()   { echo -e "${GREEN}✔ $1${NC}"; }
fail() {
  echo -e "${RED}✘ $1${NC}"
  ((ERRORS++)) || true
}

LAST_BODY=""
LAST_CODE=""

call() {
  local method="$1"; shift
  local url="$1";    shift
  local desc="$1";   shift

  info "$desc"
  echo "  ${method} ${url}"

  local response
  response=$(curl -s -w "\n%{http_code}" "$@" -X "$method" "$url" 2>/dev/null) || true

  LAST_CODE=$(echo "$response" | tail -n1)
  LAST_BODY=$(echo "$response" | sed '$d')

  echo "  HTTP ${LAST_CODE}"
  echo "$LAST_BODY" | python3 -m json.tool 2>/dev/null || echo "$LAST_BODY"
  echo ""

  if [[ "$LAST_CODE" =~ ^2 ]]; then
    ok "Успешно (HTTP ${LAST_CODE})"
    return 0
  else
    fail "Ошибка (HTTP ${LAST_CODE})"
    return 1
  fi
}

json_val() {
  local result
  result=$(echo "$1" | python3 -c "import sys,json; print(json.loads(sys.stdin.read())$2)" 2>/dev/null) || true
  if [ -z "$result" ]; then
    echo "__EXTRACT_FAILED__"
    return 1
  fi
  echo "$result"
}

extract() {
  local varname="$1"
  local body="$2"
  local jpath="$3"

  local val
  val=$(json_val "$body" "$jpath")
  if [ "$val" = "__EXTRACT_FAILED__" ]; then
    fail "Не удалось извлечь $jpath из ответа"
    eval "$varname=''"
    return 1
  fi
  eval "$varname='$val'"
  echo "  → ${varname}=${val}"
  return 0
}

require_var() {
  local name="$1"
  local val="${!name:-}"
  if [ -z "$val" ]; then
    fail "Переменная $name не установлена — пропуск шага"
    return 1
  fi
  return 0
}

banner "Тестирование бизнес-процесса МТС-магазина (BPMN)"

step "0. Регистрация пользователей"

CUSTOMER_TOKEN="" CUSTOMER_ID=""
if call POST "$BASE/auth/register/customer" \
     "Регистрация покупателя (Иван Иванов)" \
     -H "$CT" \
     -d '{"fullName":"Иван Иванов","email":"ivan@mts.ru","username":"ivan","password":"secret123"}'; then
  extract CUSTOMER_TOKEN "$LAST_BODY" "['token']"
  extract CUSTOMER_ID    "$LAST_BODY" "['participantId']"
fi

CUSTOMER2_TOKEN="" CUSTOMER2_ID=""
if call POST "$BASE/auth/register/customer" \
     "Регистрация второго покупателя (Пётр Петров)" \
     -H "$CT" \
     -d '{"fullName":"Пётр Петров","email":"petr@mts.ru","username":"petr","password":"secret123"}'; then
  extract CUSTOMER2_TOKEN "$LAST_BODY" "['token']"
  extract CUSTOMER2_ID    "$LAST_BODY" "['participantId']"
fi

ASSISTANT_TOKEN="" ASSISTANT_ID=""
if call POST "$BASE/auth/register/shop-assistant" \
     "Регистрация сотрудника салона связи (shopId=1)" \
     -H "$CT" \
     -d '{"name":"Анна Ассистент","shopId":1,"username":"anna","password":"secret123"}'; then
  extract ASSISTANT_TOKEN "$LAST_BODY" "['token']"
  extract ASSISTANT_ID    "$LAST_BODY" "['participantId']"
fi

COURIER_TOKEN="" COURIER_ID=""
if call POST "$BASE/auth/register/courier" \
     "Регистрация курьера (Денис Курьер)" \
     -H "$CT" \
     -d '{"name":"Денис Курьер","username":"denis","password":"secret123","passportId":"4510 123456"}'; then
  extract COURIER_TOKEN "$LAST_BODY" "['token']"
  extract COURIER_ID    "$LAST_BODY" "['participantId']"
fi

step "1. Проверка каталога (данные из data.sql)"

PRODUCT1_ID=1
PRODUCT2_ID=5
PRODUCT3_ID=23
SHOP_ID=1

info "PRODUCT1_ID=$PRODUCT1_ID (iPhone 15 Pro), PRODUCT2_ID=$PRODUCT2_ID (Samsung Galaxy S24 Ultra), PRODUCT3_ID=$PRODUCT3_ID (AirPods Pro 2)"
info "SHOP_ID=$SHOP_ID (Салон связи)"
info "Промокод: MTS2025 (15%)"

banner "СЦЕНАРИЙ A: Самовывоз + Онлайн-оплата + Промокод"

step "A1. Вход → Просмотреть каталог"

if require_var CUSTOMER_TOKEN; then
  if call POST "$BASE/auth/login" \
       "Вход покупателя Иван (логин)" \
       -H "$CT" \
       -d '{"username":"ivan","password":"secret123"}'; then
    extract CUSTOMER_TOKEN "$LAST_BODY" "['token']"
  fi

  call GET "$BASE/catalog/products" \
    "Просмотр каталога товаров" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"

  call GET "$BASE/catalog/shops" \
    "Просмотр списка салонов связи" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"
fi

step "A2. Выбрать товары и добавить в корзину"

if require_var CUSTOMER_TOKEN && require_var CUSTOMER_ID; then
  call POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
    "Добавить iPhone 15 Pro (id=$PRODUCT1_ID) в корзину (x1)" \
    -H "$CT" -H "Authorization: Bearer $CUSTOMER_TOKEN" \
    -d "{\"productId\":$PRODUCT1_ID,\"quantity\":1}"

  call POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
    "Добавить AirPods Pro 2 (id=$PRODUCT3_ID) в корзину (x2)" \
    -H "$CT" -H "Authorization: Bearer $CUSTOMER_TOKEN" \
    -d "{\"productId\":$PRODUCT3_ID,\"quantity\":2}"

  call GET "$BASE/customers/$CUSTOMER_ID/cart" \
    "Просмотреть корзину" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"
fi

step "A3. Оформить заказ (checkout) с самовывозом, онлайн-оплатой и промокодом"

ORDER_A_ID=""
if require_var CUSTOMER_TOKEN && require_var CUSTOMER_ID; then
  if call POST "$BASE/customers/$CUSTOMER_ID/orders/checkout" \
       "Оформить заказ с параметрами: fulfillmentMethod=PICKUP, paymentMethod=ONLINE, promoCode=MTS2025, shopId=$SHOP_ID" \
       -H "$CT" -H "Authorization: Bearer $CUSTOMER_TOKEN" \
       -d "{\"fulfillmentMethod\":\"PICKUP\",\"paymentMethod\":\"ONLINE\",\"promoCode\":\"MTS2025\",\"shopId\":$SHOP_ID}"; then
    extract ORDER_A_ID "$LAST_BODY" "['id']"
  fi
fi

step "A4. Сотрудник: отметить заказ как готовый к самовывозу"

if require_var ASSISTANT_TOKEN && require_var ORDER_A_ID; then
  call POST "$BASE/orders/$ORDER_A_ID/ready-for-pickup" \
    "Сотрудник салона связи: заказ готов к самовывозу" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "A5. Сотрудник: подготовить товар к выдаче"

if require_var ASSISTANT_TOKEN && require_var ORDER_A_ID; then
  call POST "$BASE/orders/$ORDER_A_ID/assistant/prepare" \
    "Сотрудник салона связи: товар подготовлен к выдаче" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "A6. Сотрудник: уведомить клиента о готовности"

if require_var ASSISTANT_TOKEN && require_var ORDER_A_ID; then
  call POST "$BASE/orders/$ORDER_A_ID/assistant/notify" \
    "Сотрудник салона связи: уведомил клиента о готовности заказа" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "A7. Клиент: получить товар (заказ уже оплачен онлайн)"

if require_var CUSTOMER_TOKEN && require_var ORDER_A_ID; then
  call POST "$BASE/orders/$ORDER_A_ID/pickup/process" \
    "Клиент получил товар в салоне (онлайн-оплата уже произведена)" \
    -H "$CT" -H "Authorization: Bearer $CUSTOMER_TOKEN"
fi

banner "СЦЕНАРИЙ B: Курьерская доставка + Оплата при получении"

step "B1. Вход → Просмотреть каталог"

if require_var CUSTOMER2_TOKEN; then
  if call POST "$BASE/auth/login" \
       "Вход второго покупателя Пётр (логин)" \
       -H "$CT" \
       -d '{"username":"petr","password":"secret123"}'; then
    extract CUSTOMER2_TOKEN "$LAST_BODY" "['token']"
  fi

  call GET "$BASE/catalog/products" \
    "Просмотр каталога товаров" \
    -H "Authorization: Bearer $CUSTOMER2_TOKEN"
fi

step "B2. Выбрать товар и добавить в корзину"

if require_var CUSTOMER2_TOKEN && require_var CUSTOMER2_ID; then
  call POST "$BASE/customers/$CUSTOMER2_ID/cart/items" \
    "Добавить Samsung Galaxy S24 Ultra (id=$PRODUCT2_ID) в корзину (x1)" \
    -H "$CT" -H "Authorization: Bearer $CUSTOMER2_TOKEN" \
    -d "{\"productId\":$PRODUCT2_ID,\"quantity\":1}"

  call GET "$BASE/customers/$CUSTOMER2_ID/cart" \
    "Просмотреть корзину" \
    -H "Authorization: Bearer $CUSTOMER2_TOKEN"
fi

step "B3. Оформить заказ (checkout) с курьерской доставкой и оплатой при получении"

ORDER_B_ID=""
if require_var CUSTOMER2_TOKEN && require_var CUSTOMER2_ID; then
  if call POST "$BASE/customers/$CUSTOMER2_ID/orders/checkout" \
       "Оформить заказ с параметрами: fulfillmentMethod=COURIER_DELIVERY, paymentMethod=CASH_ON_RECEIPT, deliveryAddress=ул. Ленина, д.10, кв.42, Санкт-Петербург" \
       -H "$CT" -H "Authorization: Bearer $CUSTOMER2_TOKEN" \
       -d '{"fulfillmentMethod":"COURIER_DELIVERY","paymentMethod":"CASH_ON_RECEIPT","deliveryAddress":"ул. Ленина, д.10, кв.42, Санкт-Петербург"}'; then
    extract ORDER_B_ID "$LAST_BODY" "['id']"
  fi
fi

step "B4. Сотрудник: отметить заказ как готовый к доставке курьером"

if require_var ASSISTANT_TOKEN && require_var ORDER_B_ID; then
  call POST "$BASE/orders/$ORDER_B_ID/ready-for-delivery" \
    "Сотрудник салона связи: заказ готов к доставке курьером" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "B5. Сотрудник: назначить курьера для доставки"

if require_var ASSISTANT_TOKEN && require_var ORDER_B_ID; then
  call POST "$BASE/orders/$ORDER_B_ID/assistant/assign-courier" \
    "Сотрудник салона связи: назначил курьера для доставки" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "B6. Сотрудник: передать товар курьеру"

if require_var ASSISTANT_TOKEN && require_var ORDER_B_ID; then
  call POST "$BASE/orders/$ORDER_B_ID/assistant/courier-pickup" \
    "Сотрудник салона связи: передал товар курьеру" \
    -H "$CT" -H "Authorization: Bearer $ASSISTANT_TOKEN"
fi

step "B6.1. Курьер: доставить товар покупателю"

if require_var COURIER_TOKEN && require_var ORDER_B_ID; then
  call POST "$BASE/orders/$ORDER_B_ID/courier/delivered" \
    "Курьер доставил товар покупателю" \
    -H "$CT" -H "Authorization: Bearer $COURIER_TOKEN"
fi

step "B7. Клиент: получить товар (оплата при получении)"

if require_var CUSTOMER2_TOKEN && require_var ORDER_B_ID; then
  call POST "$BASE/orders/$ORDER_B_ID/pickup/process" \
    "Клиент получил товар от курьера (оплата при получении)" \
    -H "$CT" -H "Authorization: Bearer $CUSTOMER2_TOKEN"
fi

banner "Дополнительные проверки"

step "Список заказов покупателей"

if require_var CUSTOMER_TOKEN && require_var CUSTOMER_ID; then
  call GET "$BASE/customers/$CUSTOMER_ID/orders" \
    "Заказы покупателя А (Иван)" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"
fi

if require_var CUSTOMER2_TOKEN && require_var CUSTOMER2_ID; then
  call GET "$BASE/customers/$CUSTOMER2_ID/orders" \
    "Заказы покупателя Б (Пётр)" \
    -H "Authorization: Bearer $CUSTOMER2_TOKEN"
fi

step "Профиль покупателя"

if require_var CUSTOMER_TOKEN; then
  call GET "$BASE/customers/me" \
    "Профиль текущего покупателя (Иван)" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"
fi

step "Logout из системы"

[ -n "${CUSTOMER_TOKEN:-}" ] && \
  call POST "$BASE/auth/logout" \
    "Выход покупателя Иван" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN"

[ -n "${CUSTOMER2_TOKEN:-}" ] && \
  call POST "$BASE/auth/logout" \
    "Выход покупателя Пётр" \
    -H "Authorization: Bearer $CUSTOMER2_TOKEN"

[ -n "${ASSISTANT_TOKEN:-}" ] && \
  call POST "$BASE/auth/logout" \
    "Выход сотрудника салона" \
    -H "Authorization: Bearer $ASSISTANT_TOKEN"

[ -n "${COURIER_TOKEN:-}" ] && \
  call POST "$BASE/auth/logout" \
    "Выход курьера" \
    -H "Authorization: Bearer $COURIER_TOKEN"

echo ""
echo "════════════════════════════════════════════════════════════"
if [ "$ERRORS" -eq 0 ]; then
  echo -e "${GREEN}${BOLD}  РЕЗУЛЬТАТ: Все шаги выполнены успешно! (ошибок: 0)${NC}"
else
  echo -e "${RED}${BOLD}  РЕЗУЛЬТАТ: Завершено с ошибками: $ERRORS${NC}"
fi
echo "════════════════════════════════════════════════════════════"
echo ""
