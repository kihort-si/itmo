package execution

import (
	"context"

	"github.com/com.vt/exchange-adapter/internal/model"
	"github.com/com.vt/exchange-adapter/internal/storage/sqlite"
)

type Store interface {
	LoadTradeWatermarks(ctx context.Context) (map[string]int, error)
	ApplyTrade(ctx context.Context, ticker string, trade model.Trade) ([]sqlite.ClientOrderRecord, error)
}

type Matcher struct {
	store Store
}

func NewMatcher(store Store) *Matcher {
	return &Matcher{store: store}
}

func (m *Matcher) LoadTradeWatermarks(ctx context.Context) (map[string]int, error) {
	return m.store.LoadTradeWatermarks(ctx)
}

func (m *Matcher) ApplyTrade(ctx context.Context, ticker string, trade model.Trade) error {
	_, err := m.store.ApplyTrade(ctx, ticker, trade)
	return err
}
