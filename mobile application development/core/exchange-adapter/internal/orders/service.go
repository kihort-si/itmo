package orders

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/com.vt/exchange-adapter/internal/exchange"
	"github.com/com.vt/exchange-adapter/internal/model"
	"github.com/com.vt/exchange-adapter/internal/storage/sqlite"
)

type Store interface {
	CreateOrder(ctx context.Context, order sqlite.ClientOrderRecord) error
	MarkOrderSubmitted(ctx context.Context, orderID, driverMessage string) error
	MarkOrderRejected(ctx context.Context, orderID, driverMessage string) error
	GetOrder(ctx context.Context, orderID string) (sqlite.ClientOrderRecord, error)
	ListOrders(ctx context.Context, filter sqlite.OrderFilter) ([]sqlite.ClientOrderRecord, error)
	EnqueueEvent(ctx context.Context, eventType, routingKey string, payload any) error
}

type Service struct {
	store    Store
	exchange *exchange.Service
}

func NewService(store Store, exchangeService *exchange.Service) *Service {
	return &Service{
		store:    store,
		exchange: exchangeService,
	}
}

func (s *Service) CreateOrder(ctx context.Context, request model.CreateClientOrderRequest) (model.ClientOrder, error) {
	record := sqlite.ClientOrderRecord{
		OrderID:    strings.TrimSpace(request.OrderID),
		UserID:     strings.TrimSpace(request.UserID),
		Ticker:     strings.ToUpper(strings.TrimSpace(request.Ticker)),
		Side:       request.Side,
		Type:       request.Type,
		Quantity:   request.Quantity,
		LimitPrice: request.LimitPrice,
		Status:     model.OrderStatusNew,
	}

	if err := s.store.CreateOrder(ctx, record); err != nil {
		return model.ClientOrder{}, err
	}

	submitResponse, err := s.exchange.SubmitOrder(model.SubmitOrderRequest{
		OrderID:    request.OrderID,
		UserID:     request.UserID,
		Ticker:     request.Ticker,
		Side:       request.Side,
		Type:       request.Type,
		Quantity:   request.Quantity,
		LimitPrice: request.LimitPrice,
	})
	if err != nil {
		rejectMessage := err.Error()
		if updateErr := s.store.MarkOrderRejected(ctx, request.OrderID, rejectMessage); updateErr != nil {
			return model.ClientOrder{}, fmt.Errorf("submit order: %v; mark rejected: %w", err, updateErr)
		}
		if eventErr := s.store.EnqueueEvent(ctx, "order.rejected", "order.rejected", map[string]any{
			"orderId":    request.OrderID,
			"userId":     request.UserID,
			"ticker":     strings.ToUpper(strings.TrimSpace(request.Ticker)),
			"side":       request.Side,
			"status":     model.OrderStatusRejected,
			"message":    rejectMessage,
			"occurredAt": nowRFC3339(),
		}); eventErr != nil {
			return model.ClientOrder{}, fmt.Errorf("submit order: %v; enqueue rejected event: %w", err, eventErr)
		}
		return model.ClientOrder{}, err
	}

	if err := s.store.MarkOrderSubmitted(ctx, request.OrderID, submitResponse.DriverMessage); err != nil {
		return model.ClientOrder{}, err
	}
	if err := s.store.EnqueueEvent(ctx, "order.created", "order.created", map[string]any{
		"orderId":           request.OrderID,
		"userId":            request.UserID,
		"ticker":            strings.ToUpper(strings.TrimSpace(request.Ticker)),
		"side":              request.Side,
		"type":              request.Type,
		"status":            model.OrderStatusPending,
		"quantity":          request.Quantity,
		"filledQuantity":    0,
		"remainingQuantity": request.Quantity,
		"limitPrice":        request.LimitPrice,
		"driverMessage":     submitResponse.DriverMessage,
		"occurredAt":        nowRFC3339(),
	}); err != nil {
		return model.ClientOrder{}, err
	}

	return s.GetOrder(ctx, request.OrderID)
}

func (s *Service) GetOrder(ctx context.Context, orderID string) (model.ClientOrder, error) {
	record, err := s.store.GetOrder(ctx, strings.TrimSpace(orderID))
	if err != nil {
		return model.ClientOrder{}, err
	}
	return toClientOrder(record), nil
}

func (s *Service) ListOrders(ctx context.Context, userID, status, ticker string) ([]model.ClientOrder, error) {
	records, err := s.store.ListOrders(ctx, sqlite.OrderFilter{
		UserID: strings.TrimSpace(userID),
		Status: model.OrderStatus(strings.TrimSpace(status)),
		Ticker: strings.TrimSpace(ticker),
	})
	if err != nil {
		return nil, err
	}

	result := make([]model.ClientOrder, 0, len(records))
	for _, record := range records {
		result = append(result, toClientOrder(record))
	}
	return result, nil
}

func toClientOrder(record sqlite.ClientOrderRecord) model.ClientOrder {
	remainingQty := record.Quantity - record.FilledQuantity
	if remainingQty < 0 {
		remainingQty = 0
	}

	averagePrice := 0.0
	if record.FilledQuantity > 0 {
		averagePrice = record.FilledAmount / float64(record.FilledQuantity)
	}

	return model.ClientOrder{
		OrderID:        record.OrderID,
		UserID:         record.UserID,
		Ticker:         record.Ticker,
		Side:           record.Side,
		Type:           record.Type,
		Quantity:       record.Quantity,
		LimitPrice:     record.LimitPrice,
		FilledQuantity: record.FilledQuantity,
		RemainingQty:   remainingQty,
		AveragePrice:   averagePrice,
		Status:         record.Status,
		DriverMessage:  record.DriverMessage,
		CreatedAt:      record.CreatedAt.Format(timeLayout),
		UpdatedAt:      record.UpdatedAt.Format(timeLayout),
	}
}

const timeLayout = "2006-01-02T15:04:05.999999999Z07:00"

func nowRFC3339() string {
	return time.Now().UTC().Format(time.RFC3339Nano)
}
