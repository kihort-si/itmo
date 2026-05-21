package main

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type config struct {
	Port           string
	CoreBaseURL    string
	RedisAddr      string
	RedisPassword  string
	RedisDB        int
	StaticCacheTTL time.Duration
	DefaultBalance float64
	HTTPTimeout    time.Duration
}

func loadConfig() config {
	return config{
		Port:           getEnv("PORT", "8060"),
		CoreBaseURL:    getEnv("CORE_BASE_URL", "http://core-nginx:8050"),
		RedisAddr:      getEnv("REDIS_ADDR", "redis:6379"),
		RedisPassword:  getEnv("REDIS_PASSWORD", ""),
		RedisDB:        getIntEnv("REDIS_DB", 0),
		StaticCacheTTL: getDurationEnv("STATIC_CACHE_TTL_SECONDS", 300) * time.Second,
		DefaultBalance: getFloatEnv("DEFAULT_BALANCE", 0),
		HTTPTimeout:    getDurationEnv("HTTP_TIMEOUT_SECONDS", 130) * time.Second,
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

func getFloatEnv(key string, fallback float64) float64 {
	raw := getEnv(key, "")
	if raw == "" {
		return fallback
	}
	v, err := strconv.ParseFloat(raw, 64)
	if err != nil {
		return fallback
	}
	return v
}

func getDurationEnv(key string, fallback int) time.Duration {
	return time.Duration(getIntEnv(key, fallback))
}
