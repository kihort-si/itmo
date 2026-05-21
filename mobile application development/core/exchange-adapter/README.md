# exchange-adapter

`exchange-adapter` can work with two market data sources:

- external driver via `EXCHANGE_DEVICE_PATH`
- built-in Go simulator via `DRIVER_SIMULATOR_ENABLED=true`

## Modes

If `DRIVER_SIMULATOR_ENABLED=false`, the adapter behaves as before and writes commands to the external character device.

If `DRIVER_SIMULATOR_ENABLED=true`, the adapter starts an in-process market simulator. It accepts the same commands:

- `ALL`
- `PRICE <ticker>`
- `BOOK <ticker>`
- `DEALS <ticker>`
- `<TICKER> <SIDE_FLAG> <PRICE> <QTY>` for order submission

This is useful for local development when the kernel driver is unavailable or generates events too rarely.

## Simulator settings

- `DRIVER_SIMULATOR_ENABLED` - enable the built-in simulator.
- `DRIVER_SIMULATOR_SEED` - random seed.
- `DRIVER_SIMULATOR_TICK_INTERVAL` - generation tick interval.
- `DRIVER_SIMULATOR_EVENTS_PER_TICK` - number of market events per tick.
- `DRIVER_SIMULATOR_TRADE_BURST_MIN` - minimum trades inside one event.
- `DRIVER_SIMULATOR_TRADE_BURST_MAX` - maximum trades inside one event.
- `DRIVER_SIMULATOR_PRICE_STEP_BPS` - maximum price move step in basis points.
- `DRIVER_SIMULATOR_SPREAD_BPS` - synthetic book spread in basis points.
- `DRIVER_SIMULATOR_MAX_TRADES_PER_TICKER` - retained trade history per ticker.
- `DRIVER_SIMULATOR_BACKFILL_DAYS` - generate this many days of historical ClickHouse trades on startup, only when `broker.stocks_trades` is empty.

## Fast local profile

```env
DRIVER_SIMULATOR_ENABLED=true
DRIVER_SIMULATOR_TICK_INTERVAL=200ms
DRIVER_SIMULATOR_EVENTS_PER_TICK=8
DRIVER_SIMULATOR_TRADE_BURST_MIN=2
DRIVER_SIMULATOR_TRADE_BURST_MAX=6
DRIVER_SIMULATOR_PRICE_STEP_BPS=60
DRIVER_SIMULATOR_BACKFILL_DAYS=30
```
