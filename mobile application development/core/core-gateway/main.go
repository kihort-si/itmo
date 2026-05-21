package main

import (
	"errors"
	"log"
	"net/http"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.temporal.io/sdk/client"
)

func main() {
	cfg := loadConfig()

	temporalClient, err := client.Dial(client.Options{HostPort: cfg.TemporalHostPort})
	if err != nil {
		log.Fatalf("connect temporal: %v", err)
	}
	defer temporalClient.Close()

	var rabbitConn *amqp.Connection
	if cfg.RabbitMQURL != "" {
		rabbitConn, err = amqp.Dial(cfg.RabbitMQURL)
		if err != nil {
			log.Printf("rabbitmq is not connected yet: %v", err)
		} else {
			defer rabbitConn.Close()
		}
	}

	a := &app{cfg: cfg, temporalClient: temporalClient, rabbitConn: rabbitConn}
	srv := &http.Server{
		Addr:              ":" + cfg.Port,
		Handler:           withCORS(a.routes()),
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Printf("core-gateway listening on :%s", cfg.Port)
	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Fatal(err)
	}
}
