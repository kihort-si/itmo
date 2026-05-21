package ingest

import (
	"context"
	"fmt"
	"log"
	"math"
	"math/rand"
	"time"

	"github.com/com.vt/exchange-adapter/internal/model"
)

const historicalTradesPerDay = 8
const denseLastDayInterval = 5 * time.Minute

type BackfillStore interface {
	SyncStocks(ctx context.Context, instruments []model.InstrumentSnapshot) (map[string]int32, error)
	CountTrades(ctx context.Context) (uint64, error)
	InsertTrades(ctx context.Context, trades []TradeRow) error
}

type BackfillResult struct {
	InsertedRows int
	LastPrices   map[string]float64
}

func BackfillHistoricalTradesIfEmpty(
	ctx context.Context,
	exchange ExchangeReader,
	store BackfillStore,
	days int,
	seed int64,
) (BackfillResult, error) {
	if days <= 0 {
		return BackfillResult{}, nil
	}

	log.Printf("BACKFILL START days=%d", days)
	count, err := store.CountTrades(ctx)
	if err != nil {
		return BackfillResult{}, err
	}
	if count > 0 {
		log.Printf("BACKFILL SKIP stocks_trades is not empty rows=%d", count)
		return BackfillResult{}, nil
	}

	instruments, err := exchange.GetAllInstruments()
	if err != nil {
		return BackfillResult{}, fmt.Errorf("fetch instruments for historical backfill: %w", err)
	}
	stockIDs, err := store.SyncStocks(ctx, instruments)
	if err != nil {
		return BackfillResult{}, fmt.Errorf("sync stocks for historical backfill: %w", err)
	}

	rng := rand.New(rand.NewSource(seed + 1009))
	now := time.Now().UTC()
	rows := make([]TradeRow, 0, len(instruments)*estimatedRowsPerTicker(days))
	series := make(map[string][][]TradeRow, len(instruments))

	for _, instrument := range instruments {
		stockID, ok := stockIDs[instrument.Ticker]
		if !ok {
			continue
		}
		series[instrument.Ticker] = buildHistoricalRows(rng, instrument, stockID, now, days)
	}

	lastPrices := make(map[string]float64, len(series))
	for day := 0; day < days; day++ {
		dayRows := 0
		for ticker, tickerRows := range series {
			if day >= len(tickerRows) {
				continue
			}
			rows = append(rows, tickerRows[day]...)
			dayRows += len(tickerRows[day])
			if len(tickerRows[day]) > 0 {
				lastPrices[ticker] = tickerRows[day][len(tickerRows[day])-1].Price
			}
		}
		log.Printf("BACKFILL DAYS %d/%d rows=%d", day+1, days, dayRows)
	}

	if err := store.InsertTrades(ctx, rows); err != nil {
		return BackfillResult{}, fmt.Errorf("insert historical trades: %w", err)
	}

	log.Printf("BACKFILL DONE days=%d rows=%d", days, len(rows))
	return BackfillResult{InsertedRows: len(rows), LastPrices: lastPrices}, nil
}

func buildHistoricalRows(
	rng *rand.Rand,
	instrument model.InstrumentSnapshot,
	stockID int32,
	now time.Time,
	days int,
) [][]TradeRow {
	if days <= 0 {
		return nil
	}

	currentPrice := math.Max(instrument.LastPrice, 0.01)
	price := currentPrice * (0.985 + rng.Float64()*0.03)
	phaseA := rng.Float64() * math.Pi * 2
	phaseB := rng.Float64() * math.Pi * 2
	rowsByDay := make([][]TradeRow, days)
	totalRows := estimatedRowsPerTicker(days)
	rowIndex := 0
	sparseStart := now.AddDate(0, 0, -days).Truncate(24 * time.Hour).Add(10 * time.Hour)

	for day := 0; day < days; day++ {
		dense := day == days-1
		timestamps := historicalTimestampsForDay(rng, sparseStart, now, day, dense)
		rowsByDay[day] = make([]TradeRow, 0, len(timestamps))
		for i, ingestedAt := range timestamps {
			progress := 0.0
			if totalRows > 1 {
				progress = float64(rowIndex) / float64(totalRows-1)
			}
			target := historicalTargetPrice(currentPrice, progress, phaseA, phaseB)
			noiseBps := (rng.Float64()*2 - 1) * 18
			reversion := 0.14
			if dense {
				noiseBps = (rng.Float64()*2 - 1) * 9
				reversion = 0.22
			}
			price += (target - price) * reversion
			price += price * noiseBps / 10000

			if dense && len(timestamps) > 0 {
				tailStart := maxInt(0, len(timestamps)-24)
				if i >= tailStart {
					alpha := float64(i-tailStart+1) / float64(len(timestamps)-tailStart+1)
					price = price*(1-alpha) + currentPrice*alpha
				}
			}
			if price < 0.01 {
				price = 0.01
			}

			rowsByDay[day] = append(rowsByDay[day], TradeRow{
				IngestedAt: ingestedAt,
				StockID:    stockID,
				Ticker:     instrument.Ticker,
				Price:      roundMoney(price),
				Volume:     historicalVolume(rng, dense),
				EventID:    0,
			})
			rowIndex++
		}
	}

	return rowsByDay
}

func historicalTimestampsForDay(rng *rand.Rand, sparseStart time.Time, now time.Time, day int, dense bool) []time.Time {
	if dense {
		start := now.Add(-24 * time.Hour)
		end := now.Add(-1 * time.Minute)
		timestamps := make([]time.Time, 0, int(24*time.Hour/denseLastDayInterval))
		for ts := start; ts.Before(end); ts = ts.Add(denseLastDayInterval) {
			timestamps = append(timestamps, ts.Add(time.Duration(rng.Intn(40))*time.Second))
		}
		return timestamps
	}

	dayStart := sparseStart.AddDate(0, 0, day)
	timestamps := make([]time.Time, 0, historicalTradesPerDay)
	for slot := 0; slot < historicalTradesPerDay; slot++ {
		timestamps = append(
			timestamps,
			dayStart.
				Add(time.Duration(slot)*time.Hour).
				Add(time.Duration(rng.Intn(45))*time.Minute).
				Add(time.Duration(rng.Intn(60))*time.Second),
		)
	}
	return timestamps
}

func historicalTargetPrice(base float64, progress float64, phaseA float64, phaseB float64) float64 {
	wave := 0.007*math.Sin(progress*math.Pi*5+phaseA) + 0.004*math.Sin(progress*math.Pi*17+phaseB)
	return base * (1 + wave)
}

func historicalVolume(rng *rand.Rand, dense bool) uint64 {
	if dense {
		return uint64(45 + rng.Intn(150))
	}
	return uint64(700 + rng.Intn(3600))
}

func estimatedRowsPerTicker(days int) int {
	if days <= 0 {
		return 0
	}
	denseRows := int(24 * time.Hour / denseLastDayInterval)
	return maxInt(0, days-1)*historicalTradesPerDay + denseRows
}

func maxInt(a int, b int) int {
	if a > b {
		return a
	}
	return b
}

func roundMoney(value float64) float64 {
	return math.Round(value*100) / 100
}
