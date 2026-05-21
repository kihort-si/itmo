package clickhouse

import (
	"context"
	"database/sql"
	"fmt"
	"net/url"
	"strconv"
	"strings"

	clickhouse "github.com/ClickHouse/clickhouse-go/v2"
	"github.com/com.vt/exchange-adapter/internal/config"
	"github.com/com.vt/exchange-adapter/internal/ingest"
	"github.com/com.vt/exchange-adapter/internal/model"
	"github.com/shopspring/decimal"
)

type Client struct {
	database string
	db       *sql.DB
	conn     clickhouse.Conn
}

type LatestTradeState struct {
	Prices     map[string]float64
	MaxEventID int
}

func New(cfg config.Config) *Client {
	addr := normalizeAddr(cfg.ClickHouseAddr)
	options := &clickhouse.Options{
		Addr: []string{addr},
		Auth: clickhouse.Auth{
			Database: cfg.ClickHouseDatabase,
			Username: cfg.ClickHouseUser,
			Password: cfg.ClickHousePassword,
		},
		DialTimeout: cfg.ClickHouseTimeout,
		ReadTimeout: cfg.ClickHouseTimeout,
	}

	db := clickhouse.OpenDB(&clickhouse.Options{
		Addr:        options.Addr,
		Auth:        options.Auth,
		DialTimeout: options.DialTimeout,
		ReadTimeout: options.ReadTimeout,
	})
	conn, err := clickhouse.Open(options)
	if err != nil {
		panic(fmt.Errorf("open clickhouse connection: %w", err))
	}

	return &Client{
		database: cfg.ClickHouseDatabase,
		db:       db,
		conn:     conn,
	}
}

func (c *Client) SyncStocks(ctx context.Context, instruments []model.InstrumentSnapshot) (map[string]int32, error) {
	stocks, err := c.loadStocks(ctx)
	if err != nil {
		return nil, err
	}

	maxStockID := int32(0)
	for _, stockID := range stocks {
		if stockID > maxStockID {
			maxStockID = stockID
		}
	}

	toInsert := make([]stockRecord, 0)
	for _, instrument := range instruments {
		if _, exists := stocks[instrument.Ticker]; exists {
			continue
		}
		maxStockID++
		stocks[instrument.Ticker] = maxStockID
		toInsert = append(toInsert, stockRecord{StockID: maxStockID, Ticker: instrument.Ticker})
	}

	if len(toInsert) > 0 {
		if err := c.insertStocks(ctx, toInsert); err != nil {
			return nil, err
		}
	}

	return stocks, nil
}

func (c *Client) LoadTradeWatermarks(ctx context.Context) (map[string]int, error) {
	query := fmt.Sprintf(
		"SELECT ticker, max(eventId) AS eventId FROM %s.stocks_trades GROUP BY ticker",
		c.database,
	)
	rows, err := c.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := make(map[string]int)
	for rows.Next() {
		var ticker string
		var eventID uint32
		if err := rows.Scan(&ticker, &eventID); err != nil {
			return nil, fmt.Errorf("scan trade watermark: %w", err)
		}
		result[ticker] = int(eventID)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate trade watermarks: %w", err)
	}

	return result, nil
}

func (c *Client) CountTrades(ctx context.Context) (uint64, error) {
	query := fmt.Sprintf("SELECT count() FROM %s.stocks_trades", c.database)
	var count uint64
	if err := c.db.QueryRowContext(ctx, query).Scan(&count); err != nil {
		return 0, fmt.Errorf("count trades: %w", err)
	}
	return count, nil
}

func (c *Client) LoadLatestTradeState(ctx context.Context) (LatestTradeState, error) {
	query := fmt.Sprintf(
		"SELECT ticker, toFloat64(argMax(price, ingestedAt)) AS price, max(eventId) AS eventId FROM %s.stocks_trades GROUP BY ticker",
		c.database,
	)
	rows, err := c.db.QueryContext(ctx, query)
	if err != nil {
		return LatestTradeState{}, err
	}
	defer rows.Close()

	state := LatestTradeState{
		Prices: make(map[string]float64),
	}
	for rows.Next() {
		var ticker string
		var price float64
		var eventID uint32
		if err := rows.Scan(&ticker, &price, &eventID); err != nil {
			return LatestTradeState{}, fmt.Errorf("scan latest trade state: %w", err)
		}
		state.Prices[ticker] = price
		if int(eventID) > state.MaxEventID {
			state.MaxEventID = int(eventID)
		}
	}
	if err := rows.Err(); err != nil {
		return LatestTradeState{}, fmt.Errorf("iterate latest trade state: %w", err)
	}
	return state, nil
}

func (c *Client) InsertTrades(ctx context.Context, trades []ingest.TradeRow) error {
	if len(trades) == 0 {
		return nil
	}

	batch, err := c.conn.PrepareBatch(
		ctx,
		fmt.Sprintf(
			"INSERT INTO %s.stocks_trades (ingestedAt, stockId, ticker, price, volume, eventId)",
			c.database,
		),
	)
	if err != nil {
		return fmt.Errorf("prepare trades batch: %w", err)
	}

	for _, trade := range trades {
		price := decimal.NewFromFloat(trade.Price)
		if err := batch.Append(
			trade.IngestedAt.UTC(),
			trade.StockID,
			trade.Ticker,
			price,
			trade.Volume,
			trade.EventID,
		); err != nil {
			return fmt.Errorf("append trade for %s event %d: %w", trade.Ticker, trade.EventID, err)
		}
	}

	if err := batch.Send(); err != nil {
		return fmt.Errorf("send trades batch: %w", err)
	}

	return nil
}

func (c *Client) loadStocks(ctx context.Context) (map[string]int32, error) {
	query := fmt.Sprintf(
		"SELECT stockId, ticker FROM %s.broker_stocks FINAL",
		c.database,
	)
	rows, err := c.db.QueryContext(ctx, query)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	result := make(map[string]int32)
	for rows.Next() {
		var row stockRecord
		if err := rows.Scan(&row.StockID, &row.Ticker); err != nil {
			return nil, fmt.Errorf("scan stock row: %w", err)
		}
		result[row.Ticker] = row.StockID
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate stock rows: %w", err)
	}

	return result, nil
}

type stockRecord struct {
	StockID int32
	Ticker  string
}

func (c *Client) insertStocks(ctx context.Context, stocks []stockRecord) error {
	if len(stocks) == 0 {
		return nil
	}

	batch, err := c.conn.PrepareBatch(
		ctx,
		fmt.Sprintf("INSERT INTO %s.broker_stocks (stockId, ticker)", c.database),
	)
	if err != nil {
		return fmt.Errorf("prepare stocks batch: %w", err)
	}

	for _, stock := range stocks {
		if err := batch.Append(stock.StockID, stock.Ticker); err != nil {
			return fmt.Errorf("append stock %s: %w", stock.Ticker, err)
		}
	}

	if err := batch.Send(); err != nil {
		return fmt.Errorf("send stocks batch: %w", err)
	}

	return nil
}

func normalizeAddr(raw string) string {
	if raw == "" {
		return "clickhouse:9000"
	}

	if !strings.Contains(raw, "://") {
		return raw
	}

	parsed, err := url.Parse(raw)
	if err != nil {
		return raw
	}

	host := parsed.Hostname()
	port := parsed.Port()
	if port == "" {
		switch parsed.Scheme {
		case "http", "https":
			port = "9000"
		case "tcp":
			port = "9000"
		default:
			port = "9000"
		}
	}

	if _, err := strconv.Atoi(port); err != nil {
		port = "9000"
	}

	return host + ":" + port
}
