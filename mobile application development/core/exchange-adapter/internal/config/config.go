package config

import (
	"os"
	"strconv"
	"time"
)

type Config struct {
	ListenAddr                        string
	DevicePath                        string
	DriverSimulatorEnabled            bool
	DriverSimulatorSeed               int64
	DriverSimulatorTickInterval       time.Duration
	DriverSimulatorEventsPerTick      int
	DriverSimulatorTradeBurstMin      int
	DriverSimulatorTradeBurstMax      int
	DriverSimulatorPriceStepBps       int
	DriverSimulatorSpreadBps          int
	DriverSimulatorMaxTradesPerTicker int
	DriverSimulatorBackfillDays       int
	PollEnabled                       bool
	PollInterval                      time.Duration
	SQLitePath                        string
	RabbitMQURL                       string
	RabbitMQExchange                  string
	OutboxPollInterval                time.Duration
	ClickHouseAddr                    string
	ClickHouseDatabase                string
	ClickHouseUser                    string
	ClickHousePassword                string
	ClickHouseTimeout                 time.Duration
}

func Load() Config {
	return Config{
		ListenAddr:                        getEnv("LISTEN_ADDR", ":8040"),
		DevicePath:                        getEnv("EXCHANGE_DEVICE_PATH", "/dev/exchange"),
		DriverSimulatorEnabled:            getEnvBool("DRIVER_SIMULATOR_ENABLED", false),
		DriverSimulatorSeed:               getEnvInt64("DRIVER_SIMULATOR_SEED", 42),
		DriverSimulatorTickInterval:       getEnvDuration("DRIVER_SIMULATOR_TICK_INTERVAL", 500*time.Millisecond),
		DriverSimulatorEventsPerTick:      getEnvInt("DRIVER_SIMULATOR_EVENTS_PER_TICK", 3),
		DriverSimulatorTradeBurstMin:      getEnvInt("DRIVER_SIMULATOR_TRADE_BURST_MIN", 1),
		DriverSimulatorTradeBurstMax:      getEnvInt("DRIVER_SIMULATOR_TRADE_BURST_MAX", 4),
		DriverSimulatorPriceStepBps:       getEnvInt("DRIVER_SIMULATOR_PRICE_STEP_BPS", 35),
		DriverSimulatorSpreadBps:          getEnvInt("DRIVER_SIMULATOR_SPREAD_BPS", 20),
		DriverSimulatorMaxTradesPerTicker: getEnvInt("DRIVER_SIMULATOR_MAX_TRADES_PER_TICKER", 64),
		DriverSimulatorBackfillDays:       getEnvInt("DRIVER_SIMULATOR_BACKFILL_DAYS", 0),
		PollEnabled:                       getEnvBool("POLL_ENABLED", false),
		PollInterval:                      getEnvDuration("POLL_INTERVAL", 2*time.Second),
		SQLitePath:                        getEnv("SQLITE_PATH", "var/exchange-adapter.db"),
		RabbitMQURL:                       getEnv("RABBITMQ_URL", ""),
		RabbitMQExchange:                  getEnv("RABBITMQ_EXCHANGE", ""),
		OutboxPollInterval:                getEnvDuration("OUTBOX_POLL_INTERVAL", time.Second),
		ClickHouseAddr:                    getEnv("CLICKHOUSE_ADDR", "clickhouse:9000"),
		ClickHouseDatabase:                getEnv("CLICKHOUSE_DATABASE", "broker"),
		ClickHouseUser:                    getEnv("CLICKHOUSE_USER", "broker"),
		ClickHousePassword:                getEnv("CLICKHOUSE_PASSWORD", "broker"),
		ClickHouseTimeout:                 getEnvDuration("CLICKHOUSE_TIMEOUT", 5*time.Second),
	}
}

func getEnv(key, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func getEnvBool(key string, fallback bool) bool {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return fallback
	}
	return parsed
}

func getEnvInt(key string, fallback int) int {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}

func getEnvInt64(key string, fallback int64) int64 {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := strconv.ParseInt(value, 10, 64)
	if err != nil {
		return fallback
	}
	return parsed
}

func getEnvDuration(key string, fallback time.Duration) time.Duration {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}

	parsed, err := time.ParseDuration(value)
	if err != nil {
		return fallback
	}
	return parsed
}
