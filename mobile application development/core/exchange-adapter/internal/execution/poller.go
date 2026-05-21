package execution

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

type TradeMatcher interface {
	LoadTradeWatermarks(ctx context.Context) (map[string]int, error)
	ApplyTrade(ctx context.Context, ticker string, trade model.Trade) error
}

type Poller struct {
	exchange ExchangeReader
	matcher  TradeMatcher
	interval time.Duration
}

func NewPoller(exchange ExchangeReader, matcher TradeMatcher, interval time.Duration) *Poller {
	return &Poller{
		exchange: exchange,
		matcher:  matcher,
		interval: interval,
	}
}

func (p *Poller) Run(ctx context.Context) {
	watermarks, err := p.matcher.LoadTradeWatermarks(ctx)
	if err != nil {
		log.Printf("execution poller: load watermarks failed: %v", err)
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
	instruments, err := p.exchange.GetAllInstruments()
	if err != nil {
		log.Printf("execution poller: fetch instruments failed: %v", err)
		return
	}

	for _, instrument := range instruments {
		response, err := p.exchange.GetTrades(instrument.Ticker)
		if err != nil {
			log.Printf("execution poller: fetch trades for %s failed: %v", instrument.Ticker, err)
			continue
		}

		lastSeen := watermarks[instrument.Ticker]
		newTrades := make([]model.Trade, 0, len(response.Trades))
		for _, trade := range response.Trades {
			if trade.EventID <= lastSeen {
				continue
			}
			newTrades = append(newTrades, trade)
		}

		sort.Slice(newTrades, func(i, j int) bool {
			return newTrades[i].EventID < newTrades[j].EventID
		})

		for _, trade := range newTrades {
			if err := p.matcher.ApplyTrade(ctx, instrument.Ticker, trade); err != nil {
				log.Printf("execution poller: apply trade %s/%d failed: %v", instrument.Ticker, trade.EventID, err)
				break
			}
			if trade.EventID > watermarks[instrument.Ticker] {
				watermarks[instrument.Ticker] = trade.EventID
			}
		}
	}
}
