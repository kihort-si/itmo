package sqlite

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/google/uuid"
	_ "modernc.org/sqlite"

	"github.com/com.vt/exchange-adapter/internal/model"
)

const (
	outboxStatusNew  = "NEW"
	outboxStatusSent = "SENT"
)

type Store struct {
	db *sql.DB
}

type ClientOrderRecord struct {
	OrderID          string
	UserID           string
	Ticker           string
	Side             model.Side
	Type             model.OrderType
	Quantity         int
	LimitPrice       float64
	FilledQuantity   int
	FilledAmount     float64
	Status           model.OrderStatus
	DriverMessage    string
	CreatedAt        time.Time
	UpdatedAt        time.Time
	LastTradeEventID int
}

type OrderFilter struct {
	UserID string
	Status model.OrderStatus
	Ticker string
}

type FillRecord struct {
	OrderID      string
	Ticker       string
	TradeEventID int
	Price        float64
	Quantity     int
	CreatedAt    time.Time
}

type OutboxMessage struct {
	ID         int64
	EventType  string
	RoutingKey string
	Payload    []byte
	CreatedAt  time.Time
}

func New(path string) (*Store, error) {
	if strings.TrimSpace(path) == "" {
		return nil, fmt.Errorf("sqlite path is required")
	}

	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return nil, fmt.Errorf("create sqlite dir: %w", err)
	}

	db, err := sql.Open("sqlite", path)
	if err != nil {
		return nil, fmt.Errorf("open sqlite: %w", err)
	}

	db.SetMaxOpenConns(1)
	db.SetMaxIdleConns(1)

	store := &Store{db: db}
	if err := store.init(context.Background()); err != nil {
		db.Close()
		return nil, err
	}

	return store, nil
}

func (s *Store) Close() error {
	if s == nil || s.db == nil {
		return nil
	}
	return s.db.Close()
}

func (s *Store) CreateOrder(ctx context.Context, order ClientOrderRecord) error {
	now := time.Now().UTC()
	if order.CreatedAt.IsZero() {
		order.CreatedAt = now
	}
	order.UpdatedAt = now

	_, err := s.db.ExecContext(ctx, `
		INSERT INTO client_orders (
			order_id, user_id, ticker, side, type, quantity, limit_price,
			filled_quantity, filled_amount, status, driver_message,
			created_at, updated_at, last_trade_event_id
		) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	`,
		order.OrderID,
		order.UserID,
		strings.ToUpper(order.Ticker),
		string(order.Side),
		string(order.Type),
		order.Quantity,
		order.LimitPrice,
		order.FilledQuantity,
		order.FilledAmount,
		string(order.Status),
		order.DriverMessage,
		order.CreatedAt.Format(time.RFC3339Nano),
		order.UpdatedAt.Format(time.RFC3339Nano),
		order.LastTradeEventID,
	)
	if err != nil {
		if strings.Contains(strings.ToLower(err.Error()), "unique") {
			return fmt.Errorf("order already exists")
		}
		return fmt.Errorf("insert order: %w", err)
	}

	return nil
}

func (s *Store) MarkOrderSubmitted(ctx context.Context, orderID, driverMessage string) error {
	_, err := s.db.ExecContext(ctx, `
		UPDATE client_orders
		SET status = ?, driver_message = ?, updated_at = ?
		WHERE order_id = ?
	`,
		string(model.OrderStatusPending),
		driverMessage,
		time.Now().UTC().Format(time.RFC3339Nano),
		orderID,
	)
	if err != nil {
		return fmt.Errorf("mark order submitted: %w", err)
	}
	return nil
}

func (s *Store) MarkOrderRejected(ctx context.Context, orderID, driverMessage string) error {
	_, err := s.db.ExecContext(ctx, `
		UPDATE client_orders
		SET status = ?, driver_message = ?, updated_at = ?
		WHERE order_id = ?
	`,
		string(model.OrderStatusRejected),
		driverMessage,
		time.Now().UTC().Format(time.RFC3339Nano),
		orderID,
	)
	if err != nil {
		return fmt.Errorf("mark order rejected: %w", err)
	}
	return nil
}

func (s *Store) GetOrder(ctx context.Context, orderID string) (ClientOrderRecord, error) {
	row := s.db.QueryRowContext(ctx, `
		SELECT order_id, user_id, ticker, side, type, quantity, limit_price,
		       filled_quantity, filled_amount, status, driver_message,
		       created_at, updated_at, last_trade_event_id
		FROM client_orders
		WHERE order_id = ?
	`, orderID)

	record, err := scanOrder(row.Scan)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return ClientOrderRecord{}, fmt.Errorf("order not found")
		}
		return ClientOrderRecord{}, err
	}
	return record, nil
}

func (s *Store) ListOrders(ctx context.Context, filter OrderFilter) ([]ClientOrderRecord, error) {
	query := `
		SELECT order_id, user_id, ticker, side, type, quantity, limit_price,
		       filled_quantity, filled_amount, status, driver_message,
		       created_at, updated_at, last_trade_event_id
		FROM client_orders
		WHERE 1 = 1
	`
	args := make([]any, 0, 3)
	if filter.UserID != "" {
		query += ` AND user_id = ?`
		args = append(args, filter.UserID)
	}
	if filter.Status != "" {
		query += ` AND status = ?`
		args = append(args, string(filter.Status))
	}
	if filter.Ticker != "" {
		query += ` AND ticker = ?`
		args = append(args, strings.ToUpper(filter.Ticker))
	}
	query += ` ORDER BY created_at ASC, order_id ASC`

	rows, err := s.db.QueryContext(ctx, query, args...)
	if err != nil {
		return nil, fmt.Errorf("list orders: %w", err)
	}
	defer rows.Close()

	result := make([]ClientOrderRecord, 0)
	for rows.Next() {
		record, err := scanOrder(rows.Scan)
		if err != nil {
			return nil, err
		}
		result = append(result, record)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate orders: %w", err)
	}

	return result, nil
}

func (s *Store) LoadTradeWatermarks(ctx context.Context) (map[string]int, error) {
	rows, err := s.db.QueryContext(ctx, `
		SELECT ticker, last_event_id
		FROM trade_watermarks
	`)
	if err != nil {
		return nil, fmt.Errorf("load sqlite trade watermarks: %w", err)
	}
	defer rows.Close()

	result := make(map[string]int)
	for rows.Next() {
		var ticker string
		var eventID int
		if err := rows.Scan(&ticker, &eventID); err != nil {
			return nil, fmt.Errorf("scan sqlite trade watermark: %w", err)
		}
		result[ticker] = eventID
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate sqlite trade watermarks: %w", err)
	}

	return result, nil
}

func (s *Store) ApplyTrade(ctx context.Context, ticker string, trade model.Trade) ([]ClientOrderRecord, error) {
	tx, err := s.db.BeginTx(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("begin apply trade tx: %w", err)
	}
	defer tx.Rollback()

	alreadyProcessed, err := tradeProcessed(ctx, tx, ticker, trade.EventID)
	if err != nil {
		return nil, err
	}
	if alreadyProcessed {
		if err := tx.Commit(); err != nil {
			return nil, fmt.Errorf("commit processed trade tx: %w", err)
		}
		return nil, nil
	}

	buyUpdated, err := s.applyTradeToSide(ctx, tx, ticker, model.SideBuy, trade)
	if err != nil {
		return nil, err
	}
	sellUpdated, err := s.applyTradeToSide(ctx, tx, ticker, model.SideSell, trade)
	if err != nil {
		return nil, err
	}

	if _, err := tx.ExecContext(ctx, `
		INSERT INTO processed_trades (ticker, trade_event_id, created_at)
		VALUES (?, ?, ?)
	`, strings.ToUpper(ticker), trade.EventID, time.Now().UTC().Format(time.RFC3339Nano)); err != nil {
		return nil, fmt.Errorf("insert processed trade: %w", err)
	}

	if _, err := tx.ExecContext(ctx, `
		INSERT INTO trade_watermarks (ticker, last_event_id, updated_at)
		VALUES (?, ?, ?)
		ON CONFLICT(ticker) DO UPDATE SET
			last_event_id = excluded.last_event_id,
			updated_at = excluded.updated_at
	`, strings.ToUpper(ticker), trade.EventID, time.Now().UTC().Format(time.RFC3339Nano)); err != nil {
		return nil, fmt.Errorf("update trade watermark: %w", err)
	}

	updated := append(buyUpdated, sellUpdated...)
	if err := tx.Commit(); err != nil {
		return nil, fmt.Errorf("commit apply trade tx: %w", err)
	}

	return updated, nil
}

func (s *Store) EnqueueEvent(ctx context.Context, eventType, routingKey string, payload any) error {
	body, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("marshal outbox payload: %w", err)
	}

	_, err = s.db.ExecContext(ctx, `
		INSERT INTO outbox (event_type, routing_key, payload, status, created_at, updated_at)
		VALUES (?, ?, ?, ?, ?, ?)
	`, eventType, routingKey, body, outboxStatusNew, time.Now().UTC().Format(time.RFC3339Nano), time.Now().UTC().Format(time.RFC3339Nano))
	if err != nil {
		return fmt.Errorf("insert outbox message: %w", err)
	}

	return nil
}

func (s *Store) FetchPendingOutbox(ctx context.Context, limit int) ([]OutboxMessage, error) {
	rows, err := s.db.QueryContext(ctx, `
		SELECT id, event_type, routing_key, payload, created_at
		FROM outbox
		WHERE status = ?
		ORDER BY id ASC
		LIMIT ?
	`, outboxStatusNew, limit)
	if err != nil {
		return nil, fmt.Errorf("fetch outbox: %w", err)
	}
	defer rows.Close()

	result := make([]OutboxMessage, 0)
	for rows.Next() {
		var message OutboxMessage
		var createdAt string
		if err := rows.Scan(&message.ID, &message.EventType, &message.RoutingKey, &message.Payload, &createdAt); err != nil {
			return nil, fmt.Errorf("scan outbox row: %w", err)
		}
		message.CreatedAt, err = time.Parse(time.RFC3339Nano, createdAt)
		if err != nil {
			return nil, fmt.Errorf("parse outbox createdAt: %w", err)
		}
		result = append(result, message)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate outbox rows: %w", err)
	}

	return result, nil
}

func (s *Store) MarkOutboxSent(ctx context.Context, id int64) error {
	_, err := s.db.ExecContext(ctx, `
		UPDATE outbox
		SET status = ?, updated_at = ?, sent_at = ?, last_error = ''
		WHERE id = ?
	`, outboxStatusSent, time.Now().UTC().Format(time.RFC3339Nano), time.Now().UTC().Format(time.RFC3339Nano), id)
	if err != nil {
		return fmt.Errorf("mark outbox sent: %w", err)
	}
	return nil
}

func (s *Store) MarkOutboxFailed(ctx context.Context, id int64, publishErr error) error {
	_, err := s.db.ExecContext(ctx, `
		UPDATE outbox
		SET attempts = attempts + 1, updated_at = ?, last_error = ?
		WHERE id = ?
	`, time.Now().UTC().Format(time.RFC3339Nano), publishErr.Error(), id)
	if err != nil {
		return fmt.Errorf("mark outbox failed: %w", err)
	}
	return nil
}

func (s *Store) applyTradeToSide(ctx context.Context, tx *sql.Tx, ticker string, side model.Side, trade model.Trade) ([]ClientOrderRecord, error) {
	rows, err := tx.QueryContext(ctx, `
		SELECT order_id, user_id, ticker, side, type, quantity, limit_price,
		       filled_quantity, filled_amount, status, driver_message,
		       created_at, updated_at, last_trade_event_id
		FROM client_orders
		WHERE ticker = ? AND side = ? AND status IN (?, ?)
		ORDER BY created_at ASC, order_id ASC
	`, strings.ToUpper(ticker), string(side), string(model.OrderStatusPending), string(model.OrderStatusPartiallyFilled))
	if err != nil {
		return nil, fmt.Errorf("query active orders: %w", err)
	}
	defer rows.Close()

	orders := make([]ClientOrderRecord, 0)
	for rows.Next() {
		record, err := scanOrder(rows.Scan)
		if err != nil {
			return nil, err
		}
		orders = append(orders, record)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate active orders: %w", err)
	}

	remainingTradeVolume := trade.Volume
	updated := make([]ClientOrderRecord, 0)
	for _, order := range orders {
		if remainingTradeVolume <= 0 {
			break
		}
		if !tradeMatchesOrder(order, trade) {
			continue
		}

		remainingOrderVolume := order.Quantity - order.FilledQuantity
		if remainingOrderVolume <= 0 {
			continue
		}

		fillQuantity := remainingOrderVolume
		if fillQuantity > remainingTradeVolume {
			fillQuantity = remainingTradeVolume
		}

		order.FilledQuantity += fillQuantity
		order.FilledAmount += float64(fillQuantity) * trade.Price
		order.LastTradeEventID = trade.EventID
		if order.FilledQuantity >= order.Quantity {
			order.Status = model.OrderStatusFilled
		} else {
			order.Status = model.OrderStatusPartiallyFilled
		}
		order.UpdatedAt = time.Now().UTC()

		if _, err := tx.ExecContext(ctx, `
			UPDATE client_orders
			SET filled_quantity = ?, filled_amount = ?, status = ?, updated_at = ?, last_trade_event_id = ?
			WHERE order_id = ?
		`,
			order.FilledQuantity,
			order.FilledAmount,
			string(order.Status),
			order.UpdatedAt.Format(time.RFC3339Nano),
			order.LastTradeEventID,
			order.OrderID,
		); err != nil {
			return nil, fmt.Errorf("update filled order: %w", err)
		}

		if _, err := tx.ExecContext(ctx, `
			INSERT INTO order_fills (order_id, ticker, trade_event_id, price, quantity, created_at)
			VALUES (?, ?, ?, ?, ?, ?)
		`,
			order.OrderID,
			order.Ticker,
			trade.EventID,
			trade.Price,
			fillQuantity,
			time.Now().UTC().Format(time.RFC3339Nano),
		); err != nil {
			return nil, fmt.Errorf("insert order fill: %w", err)
		}

		eventType := "order.partially_filled"
		if order.Status == model.OrderStatusFilled {
			eventType = "order.filled"
		}

		payload, err := json.Marshal(orderEventPayload(order, eventType, trade.Price, fillQuantity, trade.EventID))
		if err != nil {
			return nil, fmt.Errorf("marshal fill event payload: %w", err)
		}

		if _, err := tx.ExecContext(ctx, `
			INSERT INTO outbox (event_type, routing_key, payload, status, created_at, updated_at)
			VALUES (?, ?, ?, ?, ?, ?)
		`,
			eventType,
			eventType,
			payload,
			outboxStatusNew,
			time.Now().UTC().Format(time.RFC3339Nano),
			time.Now().UTC().Format(time.RFC3339Nano),
		); err != nil {
			return nil, fmt.Errorf("insert fill outbox message: %w", err)
		}

		updated = append(updated, order)
		remainingTradeVolume -= fillQuantity
	}

	return updated, nil
}

func (s *Store) init(ctx context.Context) error {
	statements := []string{
		`PRAGMA journal_mode = WAL;`,
		`PRAGMA synchronous = NORMAL;`,
		`CREATE TABLE IF NOT EXISTS client_orders (
			order_id TEXT PRIMARY KEY,
			user_id TEXT NOT NULL,
			ticker TEXT NOT NULL,
			side TEXT NOT NULL,
			type TEXT NOT NULL,
			quantity INTEGER NOT NULL,
			limit_price REAL NOT NULL,
			filled_quantity INTEGER NOT NULL DEFAULT 0,
			filled_amount REAL NOT NULL DEFAULT 0,
			status TEXT NOT NULL,
			driver_message TEXT NOT NULL DEFAULT '',
			created_at TEXT NOT NULL,
			updated_at TEXT NOT NULL,
			last_trade_event_id INTEGER NOT NULL DEFAULT 0
		);`,
		`CREATE INDEX IF NOT EXISTS idx_client_orders_lookup
			ON client_orders (ticker, side, status, created_at, order_id);`,
		`CREATE INDEX IF NOT EXISTS idx_client_orders_user
			ON client_orders (user_id, created_at, order_id);`,
		`CREATE TABLE IF NOT EXISTS order_fills (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			order_id TEXT NOT NULL,
			ticker TEXT NOT NULL,
			trade_event_id INTEGER NOT NULL,
			price REAL NOT NULL,
			quantity INTEGER NOT NULL,
			created_at TEXT NOT NULL
		);`,
		`CREATE UNIQUE INDEX IF NOT EXISTS idx_order_fills_unique
			ON order_fills (order_id, trade_event_id);`,
		`CREATE TABLE IF NOT EXISTS processed_trades (
			ticker TEXT NOT NULL,
			trade_event_id INTEGER NOT NULL,
			created_at TEXT NOT NULL,
			PRIMARY KEY (ticker, trade_event_id)
		);`,
		`CREATE TABLE IF NOT EXISTS trade_watermarks (
			ticker TEXT PRIMARY KEY,
			last_event_id INTEGER NOT NULL,
			updated_at TEXT NOT NULL
		);`,
		`CREATE TABLE IF NOT EXISTS outbox (
			id INTEGER PRIMARY KEY AUTOINCREMENT,
			event_type TEXT NOT NULL,
			routing_key TEXT NOT NULL,
			payload BLOB NOT NULL,
			status TEXT NOT NULL,
			attempts INTEGER NOT NULL DEFAULT 0,
			last_error TEXT NOT NULL DEFAULT '',
			created_at TEXT NOT NULL,
			updated_at TEXT NOT NULL,
			sent_at TEXT NOT NULL DEFAULT ''
		);`,
		`CREATE INDEX IF NOT EXISTS idx_outbox_status_id ON outbox (status, id);`,
	}

	for _, statement := range statements {
		if _, err := s.db.ExecContext(ctx, statement); err != nil {
			return fmt.Errorf("init sqlite schema: %w", err)
		}
	}

	return nil
}

func scanOrder(scan func(dest ...any) error) (ClientOrderRecord, error) {
	var record ClientOrderRecord
	var side string
	var orderType string
	var status string
	var createdAt string
	var updatedAt string
	if err := scan(
		&record.OrderID,
		&record.UserID,
		&record.Ticker,
		&side,
		&orderType,
		&record.Quantity,
		&record.LimitPrice,
		&record.FilledQuantity,
		&record.FilledAmount,
		&status,
		&record.DriverMessage,
		&createdAt,
		&updatedAt,
		&record.LastTradeEventID,
	); err != nil {
		return ClientOrderRecord{}, err
	}

	record.Side = model.Side(side)
	record.Type = model.OrderType(orderType)
	record.Status = model.OrderStatus(status)

	var err error
	record.CreatedAt, err = time.Parse(time.RFC3339Nano, createdAt)
	if err != nil {
		return ClientOrderRecord{}, fmt.Errorf("parse order createdAt: %w", err)
	}
	record.UpdatedAt, err = time.Parse(time.RFC3339Nano, updatedAt)
	if err != nil {
		return ClientOrderRecord{}, fmt.Errorf("parse order updatedAt: %w", err)
	}

	return record, nil
}

func tradeProcessed(ctx context.Context, tx *sql.Tx, ticker string, eventID int) (bool, error) {
	var exists int
	if err := tx.QueryRowContext(ctx, `
		SELECT 1
		FROM processed_trades
		WHERE ticker = ? AND trade_event_id = ?
	`, strings.ToUpper(ticker), eventID).Scan(&exists); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return false, nil
		}
		return false, fmt.Errorf("query processed trade: %w", err)
	}

	return exists == 1, nil
}

func tradeMatchesOrder(order ClientOrderRecord, trade model.Trade) bool {
	switch order.Side {
	case model.SideBuy:
		return trade.Price <= order.LimitPrice
	case model.SideSell:
		return trade.Price >= order.LimitPrice
	default:
		return false
	}
}

func orderEventPayload(order ClientOrderRecord, eventType string, lastFillPrice float64, lastFillQuantity int, tradeEventID int) map[string]any {
	averagePrice := 0.0
	if order.FilledQuantity > 0 {
		averagePrice = order.FilledAmount / float64(order.FilledQuantity)
	}

	return map[string]any{
		"eventId":           uuid.NewString(),
		"eventType":         eventType,
		"eventVersion":      1,
		"occurredAt":        time.Now().UTC().Format(time.RFC3339Nano),
		"orderId":           order.OrderID,
		"userId":            order.UserID,
		"ticker":            order.Ticker,
		"side":              order.Side,
		"status":            order.Status,
		"quantity":          order.Quantity,
		"filledQuantity":    order.FilledQuantity,
		"remainingQuantity": order.Quantity - order.FilledQuantity,
		"limitPrice":        order.LimitPrice,
		"averageFillPrice":  averagePrice,
		"lastFillPrice":     lastFillPrice,
		"lastFillQuantity":  lastFillQuantity,
		"tradeEventId":      tradeEventID,
	}
}
