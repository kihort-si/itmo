package outbox

import (
	"context"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"

	"github.com/com.vt/exchange-adapter/internal/storage/sqlite"
)

type Store interface {
	FetchPendingOutbox(ctx context.Context, limit int) ([]sqlite.OutboxMessage, error)
	MarkOutboxSent(ctx context.Context, id int64) error
	MarkOutboxFailed(ctx context.Context, id int64, publishErr error) error
}

type Publisher struct {
	store        Store
	channel      *amqp.Channel
	exchange     string
	pollInterval time.Duration
	batchSize    int
}

func NewPublisher(store Store, channel *amqp.Channel, exchange string, pollInterval time.Duration, batchSize int) *Publisher {
	if batchSize <= 0 {
		batchSize = 50
	}
	if pollInterval <= 0 {
		pollInterval = time.Second
	}

	return &Publisher{
		store:        store,
		channel:      channel,
		exchange:     exchange,
		pollInterval: pollInterval,
		batchSize:    batchSize,
	}
}

func (p *Publisher) Run(ctx context.Context) {
	if p.channel == nil || p.exchange == "" {
		log.Printf("outbox publisher disabled: rabbitmq channel or exchange is not configured")
		return
	}

	ticker := time.NewTicker(p.pollInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			p.publishBatch(ctx)
		}
	}
}

func (p *Publisher) publishBatch(ctx context.Context) {
	messages, err := p.store.FetchPendingOutbox(ctx, p.batchSize)
	if err != nil {
		log.Printf("outbox publisher: fetch pending messages failed: %v", err)
		return
	}

	for _, message := range messages {
		err := p.channel.PublishWithContext(ctx, p.exchange, message.RoutingKey, false, false, amqp.Publishing{
			ContentType: "application/json",
			Body:        message.Payload,
		})
		if err != nil {
			log.Printf("outbox publisher: publish message %d failed: %v", message.ID, err)
			if markErr := p.store.MarkOutboxFailed(ctx, message.ID, err); markErr != nil {
				log.Printf("outbox publisher: mark failed message %d failed: %v", message.ID, markErr)
			}
			return
		}

		if err := p.store.MarkOutboxSent(ctx, message.ID); err != nil {
			log.Printf("outbox publisher: mark sent message %d failed: %v", message.ID, err)
			return
		}
	}
}
