package main

import (
	"errors"
	"log"
	"net/http"
	"time"

	"github.com/redis/go-redis/v9"
)

func main() {
	cfg := loadConfig()
	a := &app{
		cfg:  cfg,
		http: &http.Client{Timeout: cfg.HTTPTimeout},
	}
	if cfg.RedisAddr != "" {
		a.redis = redis.NewClient(&redis.Options{
			Addr:     cfg.RedisAddr,
			Password: cfg.RedisPassword,
			DB:       cfg.RedisDB,
		})
		if err := a.redis.Ping(ctxBackground()).Err(); err != nil {
			log.Printf("redis is not available, static cache will be bypassed: %v", err)
		}
	}

	srv := &http.Server{
		Addr:              ":" + cfg.Port,
		Handler:           withCORS(a.routes()),
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Printf("bff listening on :%s, core gateway %s", cfg.Port, cfg.CoreBaseURL)
	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		log.Fatal(err)
	}
}
