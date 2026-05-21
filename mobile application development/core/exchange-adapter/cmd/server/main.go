package main

import (
	"context"
	"log"
	"net/http"

	"github.com/gin-gonic/gin/binding"
	amqp "github.com/rabbitmq/amqp091-go"

	"github.com/com.vt/exchange-adapter/internal/config"
	"github.com/com.vt/exchange-adapter/internal/driverclient"
	"github.com/com.vt/exchange-adapter/internal/exchange"
	"github.com/com.vt/exchange-adapter/internal/execution"
	httpapi "github.com/com.vt/exchange-adapter/internal/http"
	"github.com/com.vt/exchange-adapter/internal/ingest"
	"github.com/com.vt/exchange-adapter/internal/orders"
	"github.com/com.vt/exchange-adapter/internal/outbox"
	chstorage "github.com/com.vt/exchange-adapter/internal/storage/clickhouse"
	sqlitestorage "github.com/com.vt/exchange-adapter/internal/storage/sqlite"
)

func main() {
	cfg := config.Load()
	binding.EnableDecoderDisallowUnknownFields = true
	var client exchange.Client
	var simulator *driverclient.Simulator
	if cfg.DriverSimulatorEnabled {
		simulator = driverclient.NewSimulator(cfg)
		client = simulator
		log.Printf(
			"exchange-adapter: driver simulator enabled tick=%s eventsPerTick=%d tradeBurst=%d..%d stepBps=%d spreadBps=%d",
			cfg.DriverSimulatorTickInterval,
			cfg.DriverSimulatorEventsPerTick,
			cfg.DriverSimulatorTradeBurstMin,
			cfg.DriverSimulatorTradeBurstMax,
			cfg.DriverSimulatorPriceStepBps,
			cfg.DriverSimulatorSpreadBps,
		)
	} else {
		client = driverclient.New(cfg.DevicePath)
		log.Printf("exchange-adapter: external driver enabled device=%s", cfg.DevicePath)
	}
	service := exchange.NewService(client)
	sqliteStore, err := sqlitestorage.New(cfg.SQLitePath)
	if err != nil {
		log.Fatalf("open sqlite store failed: %v", err)
	}
	defer sqliteStore.Close()

	orderService := orders.NewService(sqliteStore, service)
	server := httpapi.NewServer(service, orderService)

	if cfg.PollEnabled {
		store := chstorage.New(cfg)
		if cfg.DriverSimulatorBackfillDays > 0 {
			result, err := ingest.BackfillHistoricalTradesIfEmpty(
				context.Background(),
				service,
				store,
				cfg.DriverSimulatorBackfillDays,
				cfg.DriverSimulatorSeed,
			)
			if err != nil {
				log.Printf("historical trade backfill failed: %v", err)
			} else if result.InsertedRows > 0 {
				if simulator != nil {
					simulator.SyncState(result.LastPrices, 0)
					log.Printf("historical trade backfill synced simulator prices from last historical rows")
				}
				log.Printf(
					"historical trade backfill inserted %d rows for %d days",
					result.InsertedRows,
					cfg.DriverSimulatorBackfillDays,
				)
			} else {
				log.Printf("historical trade backfill skipped: stocks_trades is not empty or disabled")
			}
		}
		if simulator != nil {
			state, err := store.LoadLatestTradeState(context.Background())
			if err != nil {
				log.Printf("simulator state sync from clickhouse failed: %v", err)
			} else if len(state.Prices) > 0 {
				simulator.SyncState(state.Prices, state.MaxEventID)
				log.Printf(
					"simulator state synced from clickhouse tickers=%d nextEventId>%d",
					len(state.Prices),
					state.MaxEventID,
				)
			}
		}
		poller := ingest.NewPoller(service, store, cfg.PollInterval)
		go poller.Run(context.Background())
		log.Printf("trade poller enabled with interval %s", cfg.PollInterval)
	}

	executionMatcher := execution.NewMatcher(sqliteStore)
	executionPoller := execution.NewPoller(service, executionMatcher, cfg.PollInterval)
	go executionPoller.Run(context.Background())
	log.Printf("execution poller enabled with interval %s", cfg.PollInterval)

	if cfg.RabbitMQURL != "" && cfg.RabbitMQExchange != "" {
		conn, ch, err := openRabbitMQ(cfg.RabbitMQURL)
		if err != nil {
			log.Fatalf("open rabbitmq failed: %v", err)
		}
		defer conn.Close()
		defer ch.Close()

		publisher := outbox.NewPublisher(sqliteStore, ch, cfg.RabbitMQExchange, cfg.OutboxPollInterval, 100)
		go publisher.Run(context.Background())
		log.Printf("outbox publisher enabled for exchange %s", cfg.RabbitMQExchange)
	} else {
		log.Printf("outbox publisher disabled: rabbitmq is not configured")
	}

	log.Printf("exchange-adapter listening on %s", cfg.ListenAddr)
	if err := http.ListenAndServe(cfg.ListenAddr, server.Router()); err != nil {
		log.Fatalf("http server failed: %v", err)
	}
}

func openRabbitMQ(url string) (*amqp.Connection, *amqp.Channel, error) {
	conn, err := amqp.Dial(url)
	if err != nil {
		return nil, nil, err
	}

	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, nil, err
	}

	return conn, ch, nil
}
