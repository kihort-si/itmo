package sqlite

import (
	"context"
	"path/filepath"
	"testing"

	"github.com/com.vt/exchange-adapter/internal/model"
)

func TestApplyTradeFillsBuyOrdersInFIFO(t *testing.T) {
	store := newTestStore(t)
	ctx := context.Background()

	err := store.CreateOrder(ctx, ClientOrderRecord{
		OrderID:    "11111111-1111-1111-1111-111111111111",
		UserID:     "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
		Ticker:     "SBER",
		Side:       model.SideBuy,
		Type:       model.OrderTypeLimit,
		Quantity:   10,
		LimitPrice: 105,
		Status:     model.OrderStatusPending,
	})
	if err != nil {
		t.Fatalf("create first order: %v", err)
	}
	err = store.CreateOrder(ctx, ClientOrderRecord{
		OrderID:    "22222222-2222-2222-2222-222222222222",
		UserID:     "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
		Ticker:     "SBER",
		Side:       model.SideBuy,
		Type:       model.OrderTypeLimit,
		Quantity:   10,
		LimitPrice: 110,
		Status:     model.OrderStatusPending,
	})
	if err != nil {
		t.Fatalf("create second order: %v", err)
	}

	updated, err := store.ApplyTrade(ctx, "SBER", model.Trade{
		Price:   100,
		Volume:  12,
		EventID: 1,
	})
	if err != nil {
		t.Fatalf("apply trade: %v", err)
	}
	if len(updated) != 2 {
		t.Fatalf("expected 2 updated orders, got %d", len(updated))
	}

	first, err := store.GetOrder(ctx, "11111111-1111-1111-1111-111111111111")
	if err != nil {
		t.Fatalf("get first order: %v", err)
	}
	if first.Status != model.OrderStatusFilled || first.FilledQuantity != 10 {
		t.Fatalf("unexpected first order state: %#v", first)
	}

	second, err := store.GetOrder(ctx, "22222222-2222-2222-2222-222222222222")
	if err != nil {
		t.Fatalf("get second order: %v", err)
	}
	if second.Status != model.OrderStatusPartiallyFilled || second.FilledQuantity != 2 {
		t.Fatalf("unexpected second order state: %#v", second)
	}
}

func TestApplyTradeIsIdempotentByTickerAndEventID(t *testing.T) {
	store := newTestStore(t)
	ctx := context.Background()

	err := store.CreateOrder(ctx, ClientOrderRecord{
		OrderID:    "33333333-3333-3333-3333-333333333333",
		UserID:     "cccccccc-cccc-cccc-cccc-cccccccccccc",
		Ticker:     "GAZP",
		Side:       model.SideSell,
		Type:       model.OrderTypeLimit,
		Quantity:   5,
		LimitPrice: 95,
		Status:     model.OrderStatusPending,
	})
	if err != nil {
		t.Fatalf("create order: %v", err)
	}

	trade := model.Trade{Price: 100, Volume: 5, EventID: 7}
	if _, err := store.ApplyTrade(ctx, "GAZP", trade); err != nil {
		t.Fatalf("first apply trade: %v", err)
	}
	if _, err := store.ApplyTrade(ctx, "GAZP", trade); err != nil {
		t.Fatalf("second apply trade: %v", err)
	}

	order, err := store.GetOrder(ctx, "33333333-3333-3333-3333-333333333333")
	if err != nil {
		t.Fatalf("get order: %v", err)
	}
	if order.FilledQuantity != 5 || order.Status != model.OrderStatusFilled {
		t.Fatalf("unexpected order after duplicate trade: %#v", order)
	}
}

func newTestStore(t *testing.T) *Store {
	t.Helper()

	path := filepath.Join(t.TempDir(), "orders.db")
	store, err := New(path)
	if err != nil {
		t.Fatalf("new store: %v", err)
	}
	t.Cleanup(func() {
		store.Close()
	})
	return store
}
