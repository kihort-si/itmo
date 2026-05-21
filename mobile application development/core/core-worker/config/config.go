// config/config.go
package config

import (
	"os"
)

type Config struct {
	TemporalHostPort string
	TaskQueue        string
	APIGatewayURL    string

	// RabbitMQ
	RabbitMQURL         string
	MailExchange        string // "worker.mails"
	MailBaseType        string // report
	MailSuccessType     string // SUCCESSFULL_REGISTRATION_EMAIL
	MailFailureType     string // FAILED_REGISTRATION_EMAIL
}

func Load() *Config {
	return &Config{
		TemporalHostPort: getEnv("TEMPORAL_HOST_PORT", "temporal:7233"),
		TaskQueue:        getEnv("TASK_QUEUE", "core-worker-queue"),
		APIGatewayURL:    getEnv("API_GATEWAY_URL", "http://core-nginx:8050"),

		RabbitMQURL:      getEnv("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/"),
		MailExchange:     getEnv("MAIL_EXCHANGE", "worker.mails"),
		MailBaseType:     getEnv("MAIL_AMQP_BASE_TYPE", "report"),
		MailSuccessType:  getEnv("MAIL_AMQP_SUCCESS_REPORT_TYPE", "SUCCESSFULL_REGISTRATION_EMAIL"),
		MailFailureType:  getEnv("MAIL_AMQP_FAILURE_REPORT_TYPE", "FAILED_REGISTRATION_EMAIL"),
	}
}

func getEnv(key, defaultValue string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return defaultValue
}