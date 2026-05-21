package main

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type config struct {
	Port              string
	TemporalHostPort  string
	TaskQueue         string
	WorkflowTimeout   time.Duration
	BaseCurrencyID    int
	LanguageCode      string
	RegionID          int
	BasePortfolioName string
	RabbitMQURL       string
}

func loadConfig() config {
	return config{
		Port:              getEnv("PORT", "8062"),
		TemporalHostPort:  getEnv("TEMPORAL_HOST_PORT", "temporal:7233"),
		TaskQueue:         getEnv("TASK_QUEUE", "core-worker-queue"),
		WorkflowTimeout:   getDurationEnv("WORKFLOW_TIMEOUT_SECONDS", 120) * time.Second,
		BaseCurrencyID:    getIntEnv("REGISTRATION_BASE_CURRENCY_ID", 1),
		LanguageCode:      getEnv("REGISTRATION_LANGUAGE_CODE", "ru"),
		RegionID:          getIntEnv("REGISTRATION_REGION_ID", 1),
		BasePortfolioName: getEnv("REGISTRATION_BASE_PORTFOLIO_NAME", "Main portfolio"),
		RabbitMQURL:       getEnv("RABBITMQ_URL", "amqp://admin:admin@rabbit:5672/%2Fworker"),
	}
}

func getEnv(key, fallback string) string {
	if v, ok := os.LookupEnv(key); ok && strings.TrimSpace(v) != "" {
		return v
	}
	return fallback
}

func getIntEnv(key string, fallback int) int {
	raw := getEnv(key, "")
	if raw == "" {
		return fallback
	}
	v, err := strconv.Atoi(raw)
	if err != nil {
		return fallback
	}
	return v
}

func getDurationEnv(key string, fallback int) time.Duration {
	return time.Duration(getIntEnv(key, fallback))
}
