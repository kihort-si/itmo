# Agent Context

## Workspace

- Repo root: `C:\Users\Artyom\Downloads\broker_final\MyBrokerApp-core`
- Main top-level dirs:
  - `core/` - backend services, infra, env files
  - `front/` - mobile frontend (`kotlin/` and `react-native/`)
  - `driver/` - original external exchange driver
  - `design/` - design assets

## Current architecture

- Kotlin mobile app talks to `bff` on Go.
- `bff` talks to backend services through `core-nginx`.
- Registration flow:
  - mobile -> `bff`
  - `bff` -> `core-gateway`
  - `core-gateway` -> Temporal workflow in `core-worker`
  - workflow calls `core-clients`, `core-auth`, `core-balm`, `core-depository`
- `core-auth` is the JWT issuer.
- `core-worker` publishes mail events through RabbitMQ.

## Recent implemented changes

### New Go services

- Added `core/core-gateway`:
  - endpoint `POST /api/v1/core-gateway/register`
  - starts `RegisterUserWorkflow` synchronously through Temporal
  - registration system fields are taken from env
- Added `core/bff`:
  - public prefix `/api/broker-app/v1`
  - login/register/logout/me endpoints
  - username/email availability checks through `core-clients`
  - protected `GET /api/broker-app/v1/client/{clntId}`
  - Redis cache for `/refs/*`

### Kotlin frontend

- Registration form switched from mock-style fields to backend-ready fields:
  - `name`
  - `username`
  - `email`
  - `password`
- Removed account currency from registration form.
- Added visible debug markers on auth screen:
  - `KOTLIN_AUTH_FORM_V2_BFF_2026_05_20`
  - `SIGNUP_FORM_V2_VISIBLE: name + username + email + password; currency removed`
- Added detailed Logcat logging tags:
  - `MyBrokerApp`
  - `MyBrokerAuth`
  - `MyBrokerSession`
  - `MyBrokerApi`
- `front/kotlin/local.properties` points to local Android SDK.
- Kotlin compile check passed via `:app:compileDebugKotlin`.

### exchange-adapter simulator

- Studied `driver/` and `core/exchange-adapter/`.
- Added built-in Go simulator for `exchange-adapter`.
- Source selection is now env-driven:
  - `DRIVER_SIMULATOR_ENABLED=false` -> external driver via `EXCHANGE_DEVICE_PATH`
  - `DRIVER_SIMULATOR_ENABLED=true` -> built-in simulator
- Simulator was implemented as a `driverclient` compatible source, so the rest of the adapter flow stays unchanged.
- Added tunable simulator envs:
  - `DRIVER_SIMULATOR_SEED`
  - `DRIVER_SIMULATOR_TICK_INTERVAL`
  - `DRIVER_SIMULATOR_EVENTS_PER_TICK`
  - `DRIVER_SIMULATOR_TRADE_BURST_MIN`
  - `DRIVER_SIMULATOR_TRADE_BURST_MAX`
  - `DRIVER_SIMULATOR_PRICE_STEP_BPS`
  - `DRIVER_SIMULATOR_SPREAD_BPS`
  - `DRIVER_SIMULATOR_MAX_TRADES_PER_TICKER`
  - `DRIVER_SIMULATOR_BACKFILL_DAYS`
- Extended parser to accept both:
  - legacy external driver text format
  - new simulator ASCII format
- Added historical ClickHouse backfill:
  - runs only when `broker.stocks_trades` is empty at startup
  - controlled by `DRIVER_SIMULATOR_BACKFILL_DAYS`
  - logs `BACKFILL START`, `BACKFILL DAYS x/n rows=...`, `BACKFILL DONE`, or `BACKFILL SKIP`
  - first `n-1` days are sparse, last 24 hours are dense at about one point per 5 minutes so short charts (`10M`, `1H`, `6H`, `1D`) have data immediately
  - backfill historical rows use `eventId=0` so live watermarks are not poisoned
- Added live simulator smoothing:
  - simulator has `BasePrice` and mean-reverting price moves around it
  - after backfill, simulator syncs `BasePrice`/`LastPrice` from the last historical price
  - live price is hard-bounded around base (`~±3%`) to avoid runaway random-walk charts
- Added redeploy safety:
  - when `stocks_trades` already has data and backfill skips, adapter now loads latest ClickHouse prices and max `eventId`
  - simulator `SyncState(prices, nextEventID)` sets prices and event counter above the ClickHouse watermark
  - this fixes the issue where a restarted simulator began at `eventId=1` and poller discarded trades as `<= max(eventId)`
- `go test ./...` passed for `core/exchange-adapter`.

### React Native frontend market data

- Removed automatic chart/price fallback to local mocks when backend data is bad/missing.
- Local market mocks now apply only when `Use local mock charts` toggle is enabled.
- `quoteStore` no longer seeds local mock prices or starts mock stream unless the toggle is enabled.
- Empty/bad backend chart data now renders as empty/neutral UI instead of mock-generated data.
- Added 5 second polling for:
  - stock chart
  - exchange instrument list
  - exchange sparklines
  - portfolio summary and portfolio chart
  - trade screen REST price
- Fixed exchange list layout:
  - price and currency now render in one line
  - price/day-change columns widened/rebalanced to avoid header overlap
- Changed stock chart filters from old set (`1D`, `1W`, `1M`, `6M`, `1Y`, `All`) to:
  - `10М`
  - `1Ч`
  - `6Ч`
  - `1Д`
  - `10Д`
  - `1М`
- `npm run tsc` passed in `front/react-native`.

### core-mds chart periods

- Updated `core/core-mds` chart enums to support new frontend filters:
  - timeframe `M1`
  - periods `10M`, `1H`, `6H`, `10D`
- Updated routing error message to list `M1`.
- `mvn test` passed in `core/core-mds`.

## Important files

- Core infra:
  - `core/docker-compose.yml`
  - `core/core-nginx/default.conf`
  - `core/core-nginx/upstreams.conf`
- Registration:
  - `core/core-docs/workflows/Регистрация пользователей.md`
  - `core/core-worker/workflows/register_user.go`
  - `core/core-gateway/`
  - `core/bff/`
- Exchange adapter:
  - `core/exchange-adapter/cmd/server/main.go`
  - `core/exchange-adapter/internal/config/config.go`
  - `core/exchange-adapter/internal/driverclient/client.go`
  - `core/exchange-adapter/internal/driverclient/simulator.go`
  - `core/exchange-adapter/internal/ingest/backfill.go`
  - `core/exchange-adapter/internal/driverclient/parser.go`
  - `core/exchange-adapter/internal/storage/clickhouse/client.go`
  - `core/env/exchange-adapter.env`
- Market data service:
  - `core/core-mds/src/main/kotlin/model/Models.kt`
  - `core/core-mds/src/main/kotlin/plugins/Routing.kt`
- React Native market frontend:
  - `front/react-native/src/services/marketService.ts`
  - `front/react-native/src/stores/quoteStore.ts`
  - `front/react-native/src/services/quotesService.ts`
  - `front/react-native/src/screens/StockScreen.tsx`
  - `front/react-native/src/screens/ExchangeScreen.tsx`
  - `front/react-native/src/screens/PortfolioScreen.tsx`
  - `front/react-native/src/screens/TradeScreen.tsx`
- Kotlin auth frontend:
  - `front/kotlin/app/build.gradle.kts`
  - `front/kotlin/app/src/main/java/com/itmo/mybroker/ui/screens/AuthScreen.kt`
  - `front/kotlin/app/src/main/java/com/itmo/mybroker/api/ApiClient.kt`
  - `front/kotlin/app/src/main/java/com/itmo/mybroker/core/SessionStore.kt`
  - `front/kotlin/app/src/main/java/com/itmo/mybroker/MainActivity.kt`

## Current env state of exchange-adapter

Current `core/env/exchange-adapter.env` has simulator enabled:

```env
DRIVER_SIMULATOR_ENABLED=true
DRIVER_SIMULATOR_SEED=42
DRIVER_SIMULATOR_TICK_INTERVAL=300ms
DRIVER_SIMULATOR_EVENTS_PER_TICK=4
DRIVER_SIMULATOR_TRADE_BURST_MIN=1
DRIVER_SIMULATOR_TRADE_BURST_MAX=3
DRIVER_SIMULATOR_PRICE_STEP_BPS=3
DRIVER_SIMULATOR_SPREAD_BPS=20
DRIVER_SIMULATOR_MAX_TRADES_PER_TICKER=256
DRIVER_SIMULATOR_BACKFILL_DAYS=30
POLL_ENABLED=true
POLL_INTERVAL=250ms
SQLITE_PATH=/tmp/exchange-adapter.db
RABBITMQ_URL=amqp://admin:admin@rabbit:5672/
RABBITMQ_EXCHANGE=exchange.orders
OUTBOX_POLL_INTERVAL=250ms
CLICKHOUSE_ADDR=clickhouse:9000
CLICKHOUSE_DATABASE=broker
CLICKHOUSE_USER=broker
CLICKHOUSE_PASSWORD=broker
CLICKHOUSE_TIMEOUT=5s
```

## Known state / caveats

- `exchange-adapter` service is still commented out in `core/docker-compose.yml`.
- There are existing user changes in the worktree. Do not reset or revert blindly.
- `core/core-database/liquibase/migrations` is modified by the user and was not touched here.
- `core/.gitignore` is currently open in IDE, but the repo root `.gitignore` was modified earlier to ignore `node_modules/`.
- Backfill will not re-run while `broker.stocks_trades` is non-empty. To see regenerated historical data, clear/truncate that table first.
- If backfill skips after redeploy, simulator still syncs from ClickHouse latest prices and max `eventId`; expected log:
  - `simulator state synced from clickhouse tickers=... nextEventId>...`
- Before the `SyncState` fix, redeploying with existing rows caused no live writes because simulator event IDs restarted from 1 and poller discarded trades below ClickHouse watermarks.
- If short charts look stale/empty, confirm `core-mds` deployed with new enums (`M1`, `10M`, `1H`, `6H`, `10D`) and frontend is using the latest build.

## Current modified files in worktree

Tracked modified:

- `core/core-mds/.gitignore` (added)
- `core/core-mds/src/main/kotlin/model/Models.kt`
- `core/core-mds/src/main/kotlin/plugins/Routing.kt`
- `core/env/exchange-adapter.env`
- `core/exchange-adapter/README.md`
- `core/exchange-adapter/cmd/server/main.go`
- `core/exchange-adapter/internal/config/config.go`
- `core/exchange-adapter/internal/driverclient/simulator.go`
- `core/exchange-adapter/internal/ingest/backfill.go` (added)
- `core/exchange-adapter/internal/storage/clickhouse/client.go`
- `front/react-native/src/api/types.ts`
- `front/react-native/src/hooks/usePortfolio.ts`
- `front/react-native/src/screens/ExchangeScreen.tsx`
- `front/react-native/src/screens/PortfolioScreen.tsx`
- `front/react-native/src/screens/StockScreen.tsx`
- `front/react-native/src/screens/TradeScreen.tsx`
- `front/react-native/src/services/marketService.ts`

## Verified checks

- `go test ./...` in `core/core-gateway` passed earlier.
- `go test ./...` in `core/bff` passed earlier.
- `docker compose config` in `core/` passed earlier.
- `:app:compileDebugKotlin` in `front/kotlin` passed earlier.
- `go test ./...` in `core/exchange-adapter` passed after simulator changes.
- `npm run tsc` in `front/react-native` passed after market chart/mock/polling changes.
- `mvn test` in `core/core-mds` passed after chart period enum changes.

## Suggested next steps

- Redeploy `exchange-adapter` after simulator changes and look for:
  - `BACKFILL SKIP` or `BACKFILL DONE`
  - `simulator state synced from clickhouse ...`
  - `trade poller enabled ...`
- If testing new backfill shape, truncate/clear `broker.stocks_trades` first because backfill only runs on an empty table.
- Redeploy `core-mds` when using the new chart filters, otherwise requests with `10M`, `1H`, `6H`, `10D`, or `M1` will fail.
