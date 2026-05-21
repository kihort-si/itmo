# core-gateway

HTTP-шлюз для сценариев, которые должны выполняться синхронно через Temporal.

## Эндпоинты

- `GET /health` - состояние сервиса, Temporal target и RabbitMQ connection.
- `POST /api/v1/core-gateway/register` - запускает `RegisterUserWorkflow` в `core-worker` и ждет завершения workflow.

Тело регистрации:

```json
{
  "name": "Иванов Иван Иванович",
  "username": "ivanov",
  "email": "ivanov@example.com",
  "password": "secret123"
}
```

Поля `base_currency_id`, `language_code`, `region_id`, `base_portfolio_name` берутся из env, чтобы фронтенд не управлял системными параметрами регистрации.

## Env

- `PORT` - HTTP порт, по умолчанию `8062`.
- `TEMPORAL_HOST_PORT` - адрес Temporal, по умолчанию `temporal:7233`.
- `TASK_QUEUE` - очередь worker, по умолчанию `core-worker-queue`.
- `WORKFLOW_TIMEOUT_SECONDS` - сколько ждать workflow, по умолчанию `120`.
- `REGISTRATION_BASE_CURRENCY_ID` - базовая валюта счета.
- `REGISTRATION_LANGUAGE_CODE` - язык клиента.
- `REGISTRATION_REGION_ID` - регион клиента.
- `REGISTRATION_BASE_PORTFOLIO_NAME` - имя стартового портфеля.
- `RABBITMQ_URL` - AMQP URL. Подключение уже предусмотрено, хотя сам endpoint регистрации сейчас публикует почтовые события через worker.
