.PHONY: help exchange-adapter-build exchange-adapter-upload exchange-adapter-deploy exchange-adapter-tunnel exchange-adapter-tunnel-with-clickhouse exchange-adapter-tunnel-with-rabbitmq exchange-adapter-tunnel-with-clickhouse-and-rabbitmq

EXCHANGE_ADAPTER_DIR := core/exchange-adapter
EXCHANGE_ADAPTER_DIST := $(EXCHANGE_ADAPTER_DIR)/dist
EXCHANGE_ADAPTER_BIN := exchange-adapter
EXCHANGE_ADAPTER_OUT := $(EXCHANGE_ADAPTER_DIST)/$(EXCHANGE_ADAPTER_BIN)

SSH_TARGET := timur1516@84.252.135.247
REMOTE_DIR := ~

LOCAL_EXCHANGE_PORT ?= 8040
REMOTE_EXCHANGE_HOST ?= 127.0.0.1
REMOTE_EXCHANGE_PORT ?= 8040

REMOTE_CLICKHOUSE_BIND_HOST ?= 127.0.0.1
REMOTE_CLICKHOUSE_PORT ?= 9000
LOCAL_CLICKHOUSE_HOST ?= 127.0.0.1
LOCAL_CLICKHOUSE_PORT ?= 9000

REMOTE_RABBITMQ_BIND_HOST ?= 127.0.0.1
REMOTE_RABBITMQ_PORT ?= 5672
LOCAL_RABBITMQ_HOST ?= 127.0.0.1
LOCAL_RABBITMQ_PORT ?= 5672

help:
	@printf '%s\n' \
		'make exchange-adapter-build' \
		'make exchange-adapter-upload SSH_TARGET=user@host [REMOTE_DIR=/opt/exchange-adapter]' \
		'make exchange-adapter-deploy SSH_TARGET=user@host [REMOTE_DIR=/opt/exchange-adapter]' \
		'make exchange-adapter-tunnel SSH_TARGET=user@host' \
		'make exchange-adapter-tunnel-with-clickhouse SSH_TARGET=user@host' \
		'make exchange-adapter-tunnel-with-rabbitmq SSH_TARGET=user@host' \
		'make exchange-adapter-tunnel-with-clickhouse-and-rabbitmq SSH_TARGET=user@host'

exchange-adapter-build:
	mkdir -p "$(EXCHANGE_ADAPTER_DIST)"
	cd "$(EXCHANGE_ADAPTER_DIR)" && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o "dist/$(EXCHANGE_ADAPTER_BIN)" ./cmd/server

exchange-adapter-upload:
	test -n "$(SSH_TARGET)" || (echo "SSH_TARGET is required, example: make $@ SSH_TARGET=user@host" && exit 1)
	test -f "$(EXCHANGE_ADAPTER_OUT)" || (echo "Binary not found: $(EXCHANGE_ADAPTER_OUT). Run 'make exchange-adapter-build' first." && exit 1)
	ssh "$(SSH_TARGET)" "mkdir -p '$(REMOTE_DIR)'"
	scp "$(EXCHANGE_ADAPTER_OUT)" "$(SSH_TARGET):$(REMOTE_DIR)/$(EXCHANGE_ADAPTER_BIN)"

exchange-adapter-deploy: exchange-adapter-build exchange-adapter-upload

exchange-adapter-tunnel:
	test -n "$(SSH_TARGET)" || (echo "SSH_TARGET is required, example: make $@ SSH_TARGET=user@host" && exit 1)
	ssh -N -L "$(LOCAL_EXCHANGE_PORT):$(REMOTE_EXCHANGE_HOST):$(REMOTE_EXCHANGE_PORT)" "$(SSH_TARGET)"

exchange-adapter-tunnel-with-clickhouse:
	test -n "$(SSH_TARGET)" || (echo "SSH_TARGET is required, example: make $@ SSH_TARGET=user@host" && exit 1)
	ssh -N \
		-L "$(LOCAL_EXCHANGE_PORT):$(REMOTE_EXCHANGE_HOST):$(REMOTE_EXCHANGE_PORT)" \
		-R "$(REMOTE_CLICKHOUSE_BIND_HOST):$(REMOTE_CLICKHOUSE_PORT):$(LOCAL_CLICKHOUSE_HOST):$(LOCAL_CLICKHOUSE_PORT)" \
		"$(SSH_TARGET)"

exchange-adapter-tunnel-with-rabbitmq:
	test -n "$(SSH_TARGET)" || (echo "SSH_TARGET is required, example: make $@ SSH_TARGET=user@host" && exit 1)
	ssh -N \
		-L "$(LOCAL_EXCHANGE_PORT):$(REMOTE_EXCHANGE_HOST):$(REMOTE_EXCHANGE_PORT)" \
		-R "$(REMOTE_RABBITMQ_BIND_HOST):$(REMOTE_RABBITMQ_PORT):$(LOCAL_RABBITMQ_HOST):$(LOCAL_RABBITMQ_PORT)" \
		"$(SSH_TARGET)"

exchange-adapter-tunnel-with-clickhouse-and-rabbitmq:
	test -n "$(SSH_TARGET)" || (echo "SSH_TARGET is required, example: make $@ SSH_TARGET=user@host" && exit 1)
	ssh -N \
		-L "$(LOCAL_EXCHANGE_PORT):$(REMOTE_EXCHANGE_HOST):$(REMOTE_EXCHANGE_PORT)" \
		-R "$(REMOTE_CLICKHOUSE_BIND_HOST):$(REMOTE_CLICKHOUSE_PORT):$(LOCAL_CLICKHOUSE_HOST):$(LOCAL_CLICKHOUSE_PORT)" \
		-R "$(REMOTE_RABBITMQ_BIND_HOST):$(REMOTE_RABBITMQ_PORT):$(LOCAL_RABBITMQ_HOST):$(LOCAL_RABBITMQ_PORT)" \
		"$(SSH_TARGET)"
