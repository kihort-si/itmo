package orders

import (
	"context"
	"fmt"
	"path/filepath"
	"testing"

	"github.com/com.vt/exchange-adapter/internal/exchange"
	"github.com/com.vt/exchange-adapter/internal/model"
	sqlitestorage "github.com/com.vt/exchange-adapter/internal/storage/sqlite"
)

func TestCreateOrderPersistsAndReturnsPendingState(t *testing.T) {
	store, err := sqlitestorage.New(filepath.Join(t.TempDir(), "orders.db"))
	if err != nil {
		t.Fatalf("new sqlite store: %v", err)
	}
	defer store.Close()

	exchangeService := newStubExchangeService(t, "Order executed: SBER BUY 100.00 3")
	service := NewService(store, exchangeService)

	order, err := service.CreateOrder(context.Background(), model.CreateClientOrderRequest{
		OrderID:    "44444444-4444-4444-4444-444444444444",
		UserID:     "dddddddd-dddd-dddd-dddd-dddddddddddd",
		Ticker:     "sber",
		Side:       model.SideBuy,
		Type:       model.OrderTypeLimit,
		Quantity:   3,
		LimitPrice: 100,
	})
	if err != nil {
		t.Fatalf("create order: %v", err)
	}

	if order.Status != model.OrderStatusPending {
		t.Fatalf("expected pending status, got %s", order.Status)
	}
	if order.DriverMessage == "" {
		t.Fatalf("expected driver message in response")
	}
	if order.Ticker != "SBER" {
		t.Fatalf("expected uppercased ticker, got %s", order.Ticker)
	}
}

type stubClient struct {
	response string
	err      error
}

func (s stubClient) Execute(command string) (string, error) {
	if s.err != nil {
		return "", s.err
	}
	return s.response, nil
}

func newStubExchangeService(t *testing.T, response string) *exchange.Service {
	t.Helper()
	return exchange.NewService(stubClient{response: response})
}

func TestCreateOrderMarksRejectedOnDriverFailure(t *testing.T) {
	store, err := sqlitestorage.New(filepath.Join(t.TempDir(), "orders.db"))
	if err != nil {
		t.Fatalf("new sqlite store: %v", err)
	}
	defer store.Close()

	service := NewService(store, exchange.NewService(stubClient{err: fmt.Errorf("driver unavailable")}))

	_, err = service.CreateOrder(context.Background(), model.CreateClientOrderRequest{
		OrderID:    "55555555-5555-5555-5555-555555555555",
		UserID:     "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee",
		Ticker:     "GAZP",
		Side:       model.SideSell,
		Type:       model.OrderTypeLimit,
		Quantity:   2,
		LimitPrice: 120,
	})
	if err == nil {
		t.Fatalf("expected create order error")
	}

	record, err := store.GetOrder(context.Background(), "55555555-5555-5555-5555-555555555555")
	if err != nil {
		t.Fatalf("get rejected order: %v", err)
	}
	if record.Status != model.OrderStatusRejected {
		t.Fatalf("expected rejected status, got %s", record.Status)
	}
}
