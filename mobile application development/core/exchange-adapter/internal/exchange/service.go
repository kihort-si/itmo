package exchange

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/google/uuid"

	"github.com/com.vt/exchange-adapter/internal/driverclient"
	"github.com/com.vt/exchange-adapter/internal/model"
)

type Client interface {
	Execute(command string) (string, error)
}

type Service struct {
	client Client
}

func NewService(client Client) *Service {
	return &Service{client: client}
}

func (s *Service) SubmitOrder(request model.SubmitOrderRequest) (model.SubmitOrderResponse, error) {
	if err := validateSubmitOrder(request); err != nil {
		return model.SubmitOrderResponse{}, err
	}

	command := fmt.Sprintf(
		"%s %d %s %d",
		strings.ToUpper(request.Ticker),
		sideToFlag(request.Side),
		formatPrice(request.LimitPrice),
		request.Quantity,
	)

	raw, err := s.client.Execute(command)
	if err != nil {
		return model.SubmitOrderResponse{}, err
	}

	return model.SubmitOrderResponse{
		OrderID:       request.OrderID,
		Accepted:      true,
		Ticker:        strings.ToUpper(request.Ticker),
		Side:          request.Side,
		Type:          request.Type,
		Quantity:      request.Quantity,
		LimitPrice:    request.LimitPrice,
		DriverMessage: raw,
	}, nil
}

func (s *Service) GetPrice(ticker string) (model.PriceResponse, error) {
	raw, err := s.client.Execute(fmt.Sprintf("PRICE %s", strings.ToUpper(ticker)))
	if err != nil {
		return model.PriceResponse{}, err
	}
	return driverclient.ParsePriceResponse(raw)
}

func (s *Service) GetOrderBook(ticker string) (model.OrderBookResponse, error) {
	raw, err := s.client.Execute(fmt.Sprintf("BOOK %s", strings.ToUpper(ticker)))
	if err != nil {
		return model.OrderBookResponse{}, err
	}
	return driverclient.ParseOrderBookResponse(raw)
}

func (s *Service) GetTrades(ticker string) (model.TradesResponse, error) {
	raw, err := s.client.Execute(fmt.Sprintf("DEALS %s", strings.ToUpper(ticker)))
	if err != nil {
		return model.TradesResponse{}, err
	}
	return driverclient.ParseTradesResponse(raw)
}

func (s *Service) GetAllInstruments() ([]model.InstrumentSnapshot, error) {
	raw, err := s.client.Execute("ALL")
	if err != nil {
		return nil, err
	}
	return driverclient.ParseAllInstrumentsResponse(raw)
}

func validateSubmitOrder(request model.SubmitOrderRequest) error {
	if strings.TrimSpace(request.OrderID) == "" {
		return fmt.Errorf("orderId is required")
	}
	if _, err := uuid.Parse(request.OrderID); err != nil {
		return fmt.Errorf("orderId must be a valid uuid")
	}
	if strings.TrimSpace(request.Ticker) == "" {
		return fmt.Errorf("ticker is required")
	}
	if request.Side != model.SideBuy && request.Side != model.SideSell {
		return fmt.Errorf("side must be BUY or SELL")
	}
	if request.Type != model.OrderTypeLimit {
		return fmt.Errorf("only LIMIT orders are supported")
	}
	if request.Quantity <= 0 {
		return fmt.Errorf("quantity must be positive")
	}
	if request.LimitPrice <= 0 {
		return fmt.Errorf("limitPrice must be positive")
	}
	return nil
}

func sideToFlag(side model.Side) int {
	if side == model.SideBuy {
		return 1
	}
	return 0
}

func formatPrice(value float64) string {
	return strconv.FormatFloat(value, 'f', 2, 64)
}
