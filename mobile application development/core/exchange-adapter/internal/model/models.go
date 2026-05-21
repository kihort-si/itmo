package model

type Side string

const (
	SideBuy  Side = "BUY"
	SideSell Side = "SELL"
)

type OrderType string

const (
	OrderTypeLimit OrderType = "LIMIT"
)

type OrderStatus string

const (
	OrderStatusNew             OrderStatus = "NEW"
	OrderStatusPending         OrderStatus = "PENDING"
	OrderStatusPartiallyFilled OrderStatus = "PARTIALLY_FILLED"
	OrderStatusFilled          OrderStatus = "FILLED"
	OrderStatusRejected        OrderStatus = "REJECTED"
)

type SubmitOrderRequest struct {
	OrderID    string    `json:"orderId" binding:"required,uuid"`
	UserID     string    `json:"userId,omitempty"`
	Ticker     string    `json:"ticker" binding:"required"`
	Side       Side      `json:"side" binding:"required,oneof=BUY SELL"`
	Type       OrderType `json:"type" binding:"required,oneof=LIMIT"`
	Quantity   int       `json:"quantity" binding:"required,gt=0"`
	LimitPrice float64   `json:"limitPrice" binding:"required,gt=0"`
}

type SubmitOrderResponse struct {
	OrderID       string    `json:"orderId"`
	Accepted      bool      `json:"accepted"`
	Ticker        string    `json:"ticker"`
	Side          Side      `json:"side"`
	Type          OrderType `json:"type"`
	Quantity      int       `json:"quantity"`
	LimitPrice    float64   `json:"limitPrice"`
	DriverMessage string    `json:"driverMessage"`
}

type PriceResponse struct {
	Ticker string  `json:"ticker"`
	Price  float64 `json:"price"`
}

type InstrumentSnapshot struct {
	Ticker    string  `json:"ticker"`
	LastPrice float64 `json:"lastPrice"`
}

type InstrumentListResponse struct {
	Instruments []InstrumentSnapshot `json:"instruments"`
}

type OrderBookLevel struct {
	Price     float64 `json:"price"`
	Volume    int     `json:"volume"`
	Generated bool    `json:"generated"`
}

type OrderBookResponse struct {
	Ticker string           `json:"ticker"`
	Bids   []OrderBookLevel `json:"bids"`
	Asks   []OrderBookLevel `json:"asks"`
}

type Trade struct {
	Price   float64 `json:"price"`
	Volume  int     `json:"volume"`
	EventID int     `json:"eventId"`
}

type TradesResponse struct {
	Ticker string  `json:"ticker"`
	Trades []Trade `json:"trades"`
}

type HealthResponse struct {
	Status string `json:"status"`
}

type ErrorResponse struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

type CreateClientOrderRequest struct {
	OrderID    string    `json:"orderId" binding:"required,uuid"`
	UserID     string    `json:"userId" binding:"required,uuid"`
	Ticker     string    `json:"ticker" binding:"required"`
	Side       Side      `json:"side" binding:"required,oneof=BUY SELL"`
	Type       OrderType `json:"type" binding:"required,oneof=LIMIT"`
	Quantity   int       `json:"quantity" binding:"required,gt=0"`
	LimitPrice float64   `json:"limitPrice" binding:"required,gt=0"`
}

type ClientOrder struct {
	OrderID        string      `json:"orderId"`
	UserID         string      `json:"userId"`
	Ticker         string      `json:"ticker"`
	Side           Side        `json:"side"`
	Type           OrderType   `json:"type"`
	Quantity       int         `json:"quantity"`
	LimitPrice     float64     `json:"limitPrice"`
	FilledQuantity int         `json:"filledQuantity"`
	RemainingQty   int         `json:"remainingQuantity"`
	AveragePrice   float64     `json:"averageFillPrice"`
	Status         OrderStatus `json:"status"`
	DriverMessage  string      `json:"driverMessage,omitempty"`
	CreatedAt      string      `json:"createdAt"`
	UpdatedAt      string      `json:"updatedAt"`
}

type ClientOrderListResponse struct {
	Orders []ClientOrder `json:"orders"`
}
