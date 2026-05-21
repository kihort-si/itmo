# bff

Backend for Frontend для мобильного приложения. Внешний префикс API: `/api/broker-app/v1`.

## Роль

BFF принимает запросы фронтенда, валидирует JWT через `core-auth`, применяет frontend-specific правила доступа и вызывает доменные сервисы через `core-nginx`.

## Основные эндпоинты

- `POST /api/broker-app/v1/auth/login` - проксирует логин в `core-auth`, возвращает удобный для мобильного клиента ответ.
- `POST /api/broker-app/v1/auth/register` - проверяет уникальность username/email в `core-clients`, вызывает `core-gateway`, затем логинит пользователя.
- `POST /api/broker-app/v1/auth/logout` - проксирует logout в `core-auth`.
- `GET /api/broker-app/v1/auth/me` и `GET /api/broker-app/v1/users/me` - данные текущего пользователя из JWT/auth и client profile.
- `POST /api/broker-app/v1/auth/checkUsernameInUse` - проверка username через `core-clients`.
- `POST /api/broker-app/v1/auth/checkEmailInUse` - проверка email через `core-clients`.
- `GET /api/broker-app/v1/client/{clntId}` - авторизованный доступ к `GET /api/v1/clients/client/{clntId}`. Роль `USER` видит только свой `clntId`, `ADMIN` видит любого клиента.
- `GET /api/broker-app/v1/refs/*` - прокси к справочникам `/api/v1/refs/*` с Redis-кэшированием успешных ответов.

## Env

- `PORT` - HTTP порт, по умолчанию `8060`.
- `CORE_BASE_URL` - адрес nginx внутри docker network, по умолчанию `http://core-nginx:8050`.
- `REDIS_ADDR`, `REDIS_PASSWORD`, `REDIS_DB` - подключение к Redis.
- `STATIC_CACHE_TTL_SECONDS` - TTL кэша справочников.
- `DEFAULT_BALANCE` - баланс-заглушка для текущих мобильных экранов.
- `HTTP_TIMEOUT_SECONDS` - timeout вызовов к backend через nginx.
