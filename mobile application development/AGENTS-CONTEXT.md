# MyBrokerApp Project Context

This file is a practical orientation guide for agents working in this repository. It summarizes the real project structure from code and config, not just the report documents.

## 1. Repository shape

Top-level directories:

- `core/` - main backend and infrastructure
- `front/` - two separate mobile/front clients
- `driver/` - Linux kernel module implementing a virtual exchange device
- `design/` - design assets
- `report.md` - course/project report
- `report-implementation.md` - implementation report

The repo is polyglot:

- Go
- Kotlin / Ktor
- Kotlin / Android Compose
- React Native / Expo / TypeScript
- C (Linux kernel module)

## 2. What is actually important for local work

The simplest local flow is:

1. Run backend from `core/` via Docker Compose.
2. Use one of the frontends separately.

For most tasks, you do **not** need:

- `driver/`
- `core/exchange-adapter`

Reason: `exchange-adapter` exists, but is commented out in `core/docker-compose.yml`, so it is not part of the default local runtime.

## 3. Main architecture

### 3.1 Public entrypoint

Frontend is expected to talk to the Go `bff` service, not directly to internal domain services.

Primary public base URL:

`http://127.0.0.1:8060/api/broker-app/v1`

Relevant files:

- `core/bff/main.go`
- `core/bff/routes.go`
- `core/env/bff.env`
- `core/README.md`

### 3.2 Backend layers

Backend consists of:

- `bff` - public facade for frontend
- `core-nginx` - internal reverse proxy for backend services
- `core-gateway` - orchestration API for registration
- `core-worker` - Temporal worker
- domain services on Kotlin/Ktor:
  - `core-auth`
  - `core-clients`
  - `core-balm`
  - `core-depository`
  - `core-mds`
  - `core-notify`
  - `core-orders`
  - `core-rep`
  - `core-refs`
- infra:
  - PostgreSQL
  - Redis
  - RabbitMQ
  - Temporal + Temporal UI
  - ClickHouse

### 3.3 Registration orchestration

Registration is not a single-service action.

Flow:

1. Frontend calls `bff`.
2. `bff` checks username/email availability.
3. `bff` calls `core-gateway`.
4. `core-gateway` starts Temporal workflow in `core-worker`.
5. `core-worker` creates:
   - client
   - auth account
   - balance account
   - portfolio
6. Worker publishes notification via RabbitMQ.
7. `bff` logs user in and returns auth/profile data.

Relevant files:

- `core/core-gateway/main.go`
- `core/core-gateway/routes.go`
- `core/core-worker/main.go`
- `core/core-worker/workflows/register_user.go`

## 4. Domain services

### `core-auth`

Responsibilities:

- register
- login
- refresh
- logout
- me
- lookup by `userId` / `clntId`

Files:

- `core/core-auth/src/main/kotlin/Application.kt`
- `core/core-auth/src/main/kotlin/plugins/Routing.kt`

### `core-clients`

Responsibilities:

- client profile
- client attributes
- client-account links
- username/email uniqueness checks

Main routes live in:

- `core/core-clients/src/main/kotlin/plugins/Routing.kt`

### `core-balm`

Responsibilities:

- balance accounts
- charges
- recalculation
- reports
- calculation scripts
- reference synchronization

Main routes:

- `core/core-balm/src/main/kotlin/plugins/Routing.kt`

### `core-depository`

Responsibilities:

- portfolios
- portfolio operations

Main routes:

- `core/core-depository/src/main/kotlin/plugins/Routing.kt`

### `core-mds`

Responsibilities:

- market data service over ClickHouse
- stock listing and filtering
- chart data for instruments
- FX rate lookup

Key details:

- Kotlin/Ktor service
- uses ClickHouse, not PostgreSQL
- default port in service config: `8037`

Main routes:

- `GET /health`
- `GET /v1/stocks`
- `GET /v1/stocks/{ticker}/chart`
- `GET /v1/fx/rates`

Relevant files:

- `core/core-mds/src/main/kotlin/Application.kt`
- `core/core-mds/src/main/kotlin/plugins/Routing.kt`
- `core/core-mds/src/main/resources/application.conf`

### `core-notify`

Responsibilities:

- notification delivery facade
- direct email and push endpoints
- RabbitMQ consumer for report-related notification events
- integration with `core-rep` for template rendering before delivery

Key details:

- Kotlin/Ktor service
- default port in service config: `8039`
- consumes from RabbitMQ queue `reports.in` in vhost `/reports`
- uses `REP_SERVICE_URL` to call `core-rep`
- supports SMTP delivery; empty SMTP host effectively enables log-only/no-op email mode

Main routes:

- `GET /health`
- `POST /v1/notifications/email`
- `POST /v1/notifications/push`

Important behavior:

- background consumer `MailMessageConsumer` processes report events
- maps `reportType` values such as `SUCCESSFULL_REGISTRATION_EMAIL`, `FAILED_REGISTRATION_EMAIL`, `TRADE_EXECUTED_EMAIL`, `TRADE_EXECUTED_PUSH`, `EOD_REPORT`
- resolves template IDs from `core-rep`, renders templates, then sends EMAIL or PUSH notifications

Relevant files:

- `core/core-notify/src/main/kotlin/Application.kt`
- `core/core-notify/src/main/kotlin/plugins/Routing.kt`
- `core/core-notify/src/main/kotlin/consumer/MailMessageConsumer.kt`
- `core/core-notify/src/main/kotlin/client/ReportingClient.kt`
- `core/core-notify/src/main/resources/application.conf`

### `core-orders`

Responsibilities:

- order creation
- order lookup
- order status updates
- client order history

Main routes:

- `core/core-orders/src/main/kotlin/plugins/Routing.kt`

### `core-rep`

Responsibilities:

- reporting and template service
- report building
- report template storage
- template rendering with runtime variables

Key details:

- Kotlin/Ktor service
- uses PostgreSQL
- default port in service config: `8038`

Main routes:

- `GET /health`
- `POST /v1/reports/end-of-day/build`
- `POST /v1/reports/trade/build`
- `GET /v1/templates`
- `GET /v1/templates/{templateId}`
- `POST /v1/templates/{templateId}/render`
- `POST /v1/templates`

Relevant files:

- `core/core-rep/src/main/kotlin/Application.kt`
- `core/core-rep/src/main/kotlin/plugins/Routing.kt`
- `core/core-rep/src/main/resources/application.conf`

### `core-refs`

Responsibilities:

- languages
- schemas
- multilingual reference data
- single reference values

Main routes:

- `core/core-refs/src/main/kotlin/plugins/Routing.kt`

## 5. Frontends

There are two different clients.

### 5.1 `front/kotlin`

This is the more obviously integrated Android client.

Important facts:

- Uses Jetpack Compose.
- Has mock mode and backend mode.
- Is currently configured for backend mode:
  - `API_BASE_URL = "http://127.0.0.1:8060"`
  - `USE_MOCK_API = false`

Key file:

- `front/kotlin/app/build.gradle.kts`

Notes:

- For Android Emulator, `127.0.0.1` usually must become `10.0.2.2`.
- For a physical Android device over USB, `adb reverse tcp:8060 tcp:8060` is the intended flow.

Supporting file:

- `front/kotlin/README.md`

### 5.2 `front/react-native`

This is a separate Expo / React Native client.

Key file:

- `front/react-native/package.json`

Use this when the task explicitly targets React Native / Expo, or when a CLI-only frontend path is needed.

## 6. Driver and exchange adapter

### `driver/`

Linux kernel module implementing a virtual exchange device:

- creates `/dev/exchange`
- maintains stock/order-book state
- performs matching
- generates events

Key files:

- `driver/main.c`
- `driver/Makefile`

### `core/exchange-adapter`

Go integration service around the device:

- talks to `/dev/exchange`
- exposes HTTP API
- uses SQLite
- can publish events to RabbitMQ
- can export data to ClickHouse

Key file:

- `core/exchange-adapter/cmd/server/main.go`

Important caveat:

- this service is **not** part of the default compose runtime right now

## 7. Default local startup

Backend:

```bash
cd core
docker compose up --build
```

Useful endpoints:

- `http://localhost:8060/health` - BFF health
- `http://localhost:8050` - internal nginx
- `http://localhost:8088` - Temporal UI
- `http://localhost:15672` - RabbitMQ UI

Additional backend services now included in compose:

- `core-rep` - report/template service
- `core-mds` - market data service over ClickHouse
- `core-notify` - notification delivery + report-event consumer

Current compose dependencies:

- `core-rep` depends on `core-postgres`
- `core-mds` depends on `clickhouse`
- `core-notify` depends on `rabbit` and `core-rep`

For the Kotlin Android app:

- backend URL is already set to `http://127.0.0.1:8060`
- may need `10.0.2.2` in emulator

## 8. Files worth reading first

If an agent needs orientation, read these first:

1. `core/README.md`
2. `core/docker-compose.yml`
3. `core/bff/routes.go`
4. `core/core-gateway/routes.go`
5. `core/core-worker/workflows/register_user.go`
6. `front/kotlin/README.md`
7. `front/kotlin/app/build.gradle.kts`

Then drill into the specific domain service relevant to the task.

## 9. Working assumptions and warnings

- Prefer code over report documents when they conflict.
- `report.md` and `report-implementation.md` are useful context, but they describe the system more broadly than the active runtime in some places.
- `bff` is the main contract for frontend-facing work.
- `core-mds`, `core-rep`, and `core-notify` are now part of the active backend runtime in `core/docker-compose.yml`.
- Do not assume `exchange-adapter` or `driver` are required unless the task is explicitly about market device integration.
- There are uncommitted user files in the repo root, including:
  - `Makefile`
  - `report.md`
  - `report-implementation.md`
  Agents should avoid overwriting or reverting user changes there.

## 10. Suggested routing by task

If the task is about:

- auth or session flow -> `core-auth`, `bff`, `core-gateway`, `core-worker`
- registration bugs -> `bff` + `core-gateway` + `core-worker` + `core-clients` + `core-auth` + `core-balm` + `core-depository`
- balances/accounts -> `core-balm`
- portfolios -> `core-depository`
- market data / stock lists / charts / FX -> `core-mds`
- report building / template rendering -> `core-rep`
- email / push / report-event delivery -> `core-notify`
- orders -> `core-orders`
- reference dictionaries -> `core-refs`
- Android UI -> `front/kotlin`
- Expo / React Native UI -> `front/react-native`
- exchange device / kernel behavior -> `driver`
- exchange integration / outbox / SQLite / RabbitMQ / ClickHouse ingest -> `core/exchange-adapter`
