# Core integration

Этот каталог содержит backend-контур MyBrokerApp: Kotlin/Ktor доменные сервисы, Go worker, Temporal, RabbitMQ, nginx, PostgreSQL, Redis, новый `core-gateway` и новый `bff`.

## Поток регистрации

1. Мобильное приложение вызывает `bff`:
   `POST /api/broker-app/v1/auth/register`.
2. `bff` проверяет уникальность `username` и `email` через nginx:
   - `POST /api/v1/clients/checkUsernameInUse`
   - `POST /api/v1/clients/checkEmailInUse`
3. `bff` вызывает через nginx:
   `POST /api/v1/core-gateway/register`.
4. `core-gateway` запускает Temporal workflow `RegisterUserWorkflow` в `core-worker` и синхронно ждет завершения.
5. `core-worker` создает client, auth account, balance account, portfolio и отправляет mail-событие через RabbitMQ.
6. После успешного workflow `bff` логинит пользователя через `core-auth` и возвращает мобильному приложению JWT и профиль.

## Публичный вход для фронтенда

Фронтенд должен ходить только в BFF:

```text
http://127.0.0.1:8060/api/broker-app/v1
```

`127.0.0.1` рассчитан на Android-телефон с `adb reverse`. Для эмулятора можно заменить base URL на `http://10.0.2.2:8060`.

## Запуск backend

```powershell
cd core
docker compose up --build
```

Полезные URL:

- BFF: `http://localhost:8060/health`
- nginx для внутренних backend API: `http://localhost:8050`
- Temporal UI: `http://localhost:8088`
- RabbitMQ UI: `http://localhost:15672` (`admin` / `admin`)

## Авторизация в BFF

Закрытые endpoints проверяют Bearer JWT через `core-auth /api/v1/auth/me`.

Для тестового endpoint:

```text
GET /api/broker-app/v1/client/{clntId}
```

- `USER` может получить только свой `clntId` из JWT.
- `ADMIN` может получить любого клиента.
- Без JWT endpoint возвращает `401`.

## Redis cache

`bff` использует Redis для кэширования статических справочников:

```text
GET /api/broker-app/v1/refs/*
```

TTL задается в `env/bff.env` через `STATIC_CACHE_TTL_SECONDS`.
