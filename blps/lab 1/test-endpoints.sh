#!/usr/bin/env bash
set -uo pipefail

BASE="http://localhost:23204/api"
CT="Content-Type: application/json"
TOKEN=""

echo "========================================"
echo " 1. POST /api/auth/register/customer"
echo "========================================"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register/customer" \
  -H "$CT" \
  -d '{"fullName":"Иван Иванов","email":"ivan@mts.ru","username":"ivan","password":"secret123"}')
CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "HTTP $CODE"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
CUSTOMER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['token'])" 2>/dev/null) || true
CUSTOMER_ID=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['participantId'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 2. POST /api/auth/register/shop-assistant"
echo "========================================"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register/shop-assistant" \
  -H "$CT" \
  -d '{"name":"Анна Ассистент","shopId":1,"username":"anna","password":"secret123"}')
CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "HTTP $CODE"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
ASSISTANT_TOKEN=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['token'])" 2>/dev/null) || true
ASSISTANT_ID=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['participantId'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 3. POST /api/auth/register/courier"
echo "========================================"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/register/courier" \
  -H "$CT" \
  -d '{"name":"Денис Курьер","username":"denis","password":"secret123","passportId":"4510 123456"}')
CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "HTTP $CODE"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
COURIER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['token'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 4. POST /api/auth/login"
echo "========================================"
RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/auth/login" \
  -H "$CT" \
  -d '{"username":"ivan","password":"secret123"}')
CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
echo "HTTP $CODE"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
CUSTOMER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['token'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 5. GET /api/catalog/products"
echo "========================================"
curl -s -X GET "$BASE/catalog/products" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 6. POST /api/catalog/products"
echo "========================================"
curl -s -X POST "$BASE/catalog/products" \
  -H "$CT" \
  -H "Authorization: Bearer $ASSISTANT_TOKEN" \
  -d '{"name":"Тестовый товар","description":"Описание тестового товара","price":9990,"stock":10}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 7. GET /api/catalog/shops"
echo "========================================"
curl -s -X GET "$BASE/catalog/shops" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 8. POST /api/catalog/shops"
echo "========================================"
curl -s -X POST "$BASE/catalog/shops" \
  -H "$CT" \
  -H "Authorization: Bearer aca67e6a-04e4-4caf-b5e7-d471cd96e9f0363cd82f2142409db17280c9f5334546 " \
  -d '{"name":"МТС Тестовый","address":"ул. Тестовая, д.1"}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 9. POST /api/catalog/promo-codes"
echo "========================================"
curl -s -X POST "$BASE/catalog/promo-codes" \
  -H "$CT" \
  -H "Authorization: Bearer $ASSISTANT_TOKEN" \
  -d '{"code":"TEST99","discountPercent":10,"active":true}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 10. GET /api/customers/me"
echo "========================================"
curl -s -X GET "$BASE/customers/me" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 11. GET /api/customers/{customerId}/cart"
echo "========================================"
curl -s -X GET "$BASE/customers/$CUSTOMER_ID/cart" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 12. POST /api/customers/{customerId}/cart/items"
echo "========================================"
RESP=$(curl -s -X POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"productId":1,"quantity":2}')
echo "$RESP" | python3 -m json.tool 2>/dev/null
ITEM_ID=$(echo "$RESP" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['items'][0]['id'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 13. PUT /api/customers/{customerId}/cart/items/{itemId}"
echo "========================================"
curl -s -X PUT "$BASE/customers/$CUSTOMER_ID/cart/items/$ITEM_ID" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"quantity":3}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 14. DELETE /api/customers/{customerId}/cart/items/{itemId}"
echo "========================================"
curl -s -X DELETE "$BASE/customers/$CUSTOMER_ID/cart/items/$ITEM_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 15. POST /api/customers/{customerId}/cart/clear"
echo "========================================"
curl -s -X POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"productId":1,"quantity":1}' > /dev/null
curl -s -X POST "$BASE/customers/$CUSTOMER_ID/cart/clear" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 16. POST /api/customers/{customerId}/cart/items (подготовка к checkout)"
echo "========================================"
curl -s -X POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"productId":1,"quantity":1}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 17. POST /api/customers/{customerId}/orders/checkout"
echo "========================================"
RESP=$(curl -s -X POST "$BASE/customers/$CUSTOMER_ID/orders/checkout" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")
echo "$RESP" | python3 -m json.tool 2>/dev/null
ORDER_ID=$(echo "$RESP" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['id'])" 2>/dev/null) || true
echo ""

echo "========================================"
echo " 18. GET /api/customers/{customerId}/orders"
echo "========================================"
curl -s -X GET "$BASE/customers/$CUSTOMER_ID/orders" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 19. GET /api/orders/{orderId}"
echo "========================================"
curl -s -X GET "$BASE/orders/$ORDER_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 20. POST /api/orders/{orderId}/promo"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/promo" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"promoCode":"MTS2025"}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 21. POST /api/orders/{orderId}/fulfillment"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/fulfillment" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"method":"PICKUP"}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 22. GET /api/orders/{orderId}/delivery-estimate"
echo "========================================"
curl -s -X GET "$BASE/orders/$ORDER_ID/delivery-estimate" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 23. POST /api/orders/{orderId}/payment"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/payment" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"paymentMethod":"ONLINE"}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 24. POST /api/orders/{orderId}/payment/online"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/payment/online" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 25. POST /api/orders/{orderId}/pickup/shop"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/pickup/shop" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"shopId":1}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 26. POST /api/orders/{orderId}/pickup/notify-shop"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/pickup/notify-shop" \
  -H "$CT" \
  -H "Authorization: Bearer $ASSISTANT_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 27. POST /api/orders/{orderId}/pickup/prepare"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/pickup/prepare" \
  -H "$CT" \
  -H "Authorization: Bearer $ASSISTANT_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 28. POST /api/orders/{orderId}/pickup/notify-ready"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/pickup/notify-ready" \
  -H "$CT" \
  -H "Authorization: Bearer $ASSISTANT_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 29. POST /api/orders/{orderId}/receive (самовывоз, оплачено онлайн)"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER_ID/receive" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"payOnReceiptNow":false}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " Подготовка заказа для доставки курьером"
echo "========================================"
curl -s -X POST "$BASE/customers/$CUSTOMER_ID/cart/items" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"productId":5,"quantity":1}' > /dev/null
RESP=$(curl -s -X POST "$BASE/customers/$CUSTOMER_ID/orders/checkout" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")
ORDER2_ID=$(echo "$RESP" | python3 -c "import sys,json;print(json.loads(sys.stdin.read())['id'])" 2>/dev/null) || true
curl -s -X POST "$BASE/orders/$ORDER2_ID/fulfillment" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"method":"COURIER_DELIVERY"}' > /dev/null
curl -s -X POST "$BASE/orders/$ORDER2_ID/payment" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"paymentMethod":"CASH_ON_RECEIPT"}' > /dev/null
echo "ORDER2_ID=$ORDER2_ID"
echo ""

echo "========================================"
echo " 30. POST /api/orders/{orderId}/delivery/address"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER2_ID/delivery/address" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"address":"ул. Ленина, д.10, кв.42, Санкт-Петербург"}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 31. POST /api/orders/{orderId}/delivery/take"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER2_ID/delivery/take" \
  -H "$CT" \
  -H "Authorization: Bearer $COURIER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 32. POST /api/orders/{orderId}/delivery/pickup"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER2_ID/delivery/pickup" \
  -H "$CT" \
  -H "Authorization: Bearer $COURIER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 33. POST /api/orders/{orderId}/delivery/complete"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER2_ID/delivery/complete" \
  -H "$CT" \
  -H "Authorization: Bearer $COURIER_TOKEN" | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 34. POST /api/orders/{orderId}/receive (доставка, оплата при получении)"
echo "========================================"
curl -s -X POST "$BASE/orders/$ORDER2_ID/receive" \
  -H "$CT" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{"payOnReceiptNow":true}' | python3 -m json.tool 2>/dev/null
echo ""

echo "========================================"
echo " 35. POST /api/auth/logout"
echo "========================================"
curl -s -w "\nHTTP %{http_code}\n" -X POST "$BASE/auth/logout" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
echo ""

