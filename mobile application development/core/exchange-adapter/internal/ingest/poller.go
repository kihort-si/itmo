package ingest

import (
	"context"
	"log"
	"sort"
	"time"

	"github.com/com.vt/exchange-adapter/internal/model"
)

type ExchangeReader interface {
	GetAllInstruments() ([]model.InstrumentSnapshot, error)
	GetTrades(ticker string) (model.TradesResponse, error)
}

type TradeStore interface {
	SyncStocks(ctx context.Context, instruments []model.InstrumentSnapshot) (map[string]int32, error)
	LoadTradeWatermarks(ctx context.Context) (map[string]int, error)
	InsertTrades(ctx context.Context, trades []TradeRow) error
}

type TradeRow struct {
	IngestedAt time.Time
	StockID    int32
	Ticker     string
	Price      float64
	Volume     uint64
	EventID    uint32
}

type Poller struct {
	exchange ExchangeReader
	store    TradeStore
	interval time.Duration
}

func NewPoller(exchange ExchangeReader, store TradeStore, interval time.Duration) *Poller {
	return &Poller{
		exchange: exchange,
		store:    store,
		interval: interval,
	}
}

func (p *Poller) Run(ctx context.Context) {
	watermarks, err := p.store.LoadTradeWatermarks(ctx)
	if err != nil {
		log.Printf("trade poller: load watermarks failed: %v", err)
		watermarks = make(map[string]int)
	}

	p.pollOnce(ctx, watermarks)

	ticker := time.NewTicker(p.interval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			p.pollOnce(ctx, watermarks)
		}
	}
}

func (p *Poller) pollOnce(ctx context.Context, watermarks map[string]int) {
	// TODO(driver): replace ALL with LIST when the exchange exposes a machine-friendly ticker list.
	instruments, err := p.exchange.GetAllInstruments()
	if err != nil {
		log.Printf("trade poller: fetch instruments failed: %v", err)
		return
	}

	stockIDs, err := p.store.SyncStocks(ctx, instruments)
	if err != nil {
		log.Printf("trade poller: sync stocks failed: %v", err)
		return
	}

	rows := make([]TradeRow, 0)
	nextWatermarks := make(map[string]int, len(watermarks))
	for ticker, eventID := range watermarks {
		nextWatermarks[ticker] = eventID
	}

	for _, instrument := range instruments {
		trades, err := p.exchange.GetTrades(instrument.Ticker)
		if err != nil {
			log.Printf("trade poller: fetch trades for %s failed: %v", instrument.Ticker, err)
			continue
		}

		stockID, ok := stockIDs[instrument.Ticker]
		if !ok {
			log.Printf("trade poller: stock id for %s not found", instrument.Ticker)
			continue
		}

		lastSeen := watermarks[instrument.Ticker]
		newTrades := make([]model.Trade, 0, len(trades.Trades))
		for _, trade := range trades.Trades {
			if trade.EventID <= lastSeen {
				continue
			}
			newTrades = append(newTrades, trade)
		}

		sort.Slice(newTrades, func(i, j int) bool {
			return newTrades[i].EventID < newTrades[j].EventID
		})

		for _, trade := range newTrades {
			ingestedAt := time.Now().UTC()

			rows = append(rows, TradeRow{
				IngestedAt: ingestedAt,
				StockID:    stockID,
				Ticker:     instrument.Ticker,
				Price:      trade.Price,
				Volume:     uint64(trade.Volume),
				EventID:    uint32(trade.EventID),
			})

			// TODO(driver): use trade timestamp from the exchange instead of ingestion time.
			if trade.EventID > nextWatermarks[instrument.Ticker] {
				nextWatermarks[instrument.Ticker] = trade.EventID
			}
		}
	}

	if len(rows) == 0 {
		return
	}

	if err := p.store.InsertTrades(ctx, rows); err != nil {
		log.Printf("trade poller: insert trades failed: %v", err)
		return
	}

	for ticker, eventID := range nextWatermarks {
		watermarks[ticker] = eventID
	}
}
