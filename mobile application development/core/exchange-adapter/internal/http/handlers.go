package http

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"

	"github.com/com.vt/exchange-adapter/internal/model"
)

func (s *Server) handleHealth(c *gin.Context) {
	writeJSON(c, http.StatusOK, model.HealthResponse{Status: "ok"})
}

func (s *Server) handleSubmitOrder(c *gin.Context) {
	var request model.SubmitOrderRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		writeError(c, http.StatusBadRequest, "bad_request", validationMessage(err))
		return
	}

	request.Ticker = strings.TrimSpace(request.Ticker)
	request.OrderID = strings.TrimSpace(request.OrderID)
	request.UserID = strings.TrimSpace(request.UserID)

	response, err := s.service.SubmitOrder(request)
	if err != nil {
		writeError(c, http.StatusBadRequest, "submit_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusCreated, response)
}

func (s *Server) handleCreateClientOrder(c *gin.Context) {
	var request model.CreateClientOrderRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		writeError(c, http.StatusBadRequest, "bad_request", validationMessage(err))
		return
	}

	request.Ticker = strings.TrimSpace(request.Ticker)
	request.OrderID = strings.TrimSpace(request.OrderID)
	request.UserID = strings.TrimSpace(request.UserID)

	response, err := s.orders.CreateOrder(c.Request.Context(), request)
	if err != nil {
		status := http.StatusInternalServerError
		code := "create_order_failed"

		errText := err.Error()
		switch {
		case strings.Contains(errText, "valid uuid"),
			strings.Contains(errText, "required"),
			strings.Contains(errText, "positive"),
			strings.Contains(errText, "LIMIT"),
			strings.Contains(errText, "BUY"),
			strings.Contains(errText, "SELL"):
			status = http.StatusBadRequest
			code = "bad_request"
		case strings.Contains(errText, "already exists"):
			status = http.StatusConflict
			code = "order_exists"
		case strings.Contains(errText, "submit_failed"),
			strings.Contains(errText, "read response"),
			strings.Contains(errText, "write command"),
			strings.Contains(errText, "open device"):
			status = http.StatusBadGateway
			code = "submit_failed"
		}

		writeError(c, status, code, errText)
		return
	}

	writeJSON(c, http.StatusCreated, response)
}

func (s *Server) handleGetClientOrder(c *gin.Context) {
	orderID := strings.TrimSpace(c.Param("orderId"))
	if orderID == "" {
		writeError(c, http.StatusBadRequest, "bad_request", "orderId is required")
		return
	}

	response, err := s.orders.GetOrder(c.Request.Context(), orderID)
	if err != nil {
		status := http.StatusInternalServerError
		code := "get_order_failed"
		if strings.Contains(err.Error(), "not found") {
			status = http.StatusNotFound
			code = "not_found"
		}
		writeError(c, status, code, err.Error())
		return
	}

	writeJSON(c, http.StatusOK, response)
}

func (s *Server) handleListClientOrders(c *gin.Context) {
	response, err := s.orders.ListOrders(
		c.Request.Context(),
		strings.TrimSpace(c.Query("userId")),
		strings.TrimSpace(c.Query("status")),
		strings.TrimSpace(c.Query("ticker")),
	)
	if err != nil {
		writeError(c, http.StatusInternalServerError, "list_orders_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusOK, model.ClientOrderListResponse{Orders: response})
}

func (s *Server) handleInstruments(c *gin.Context) {
	response, err := s.service.GetAllInstruments()
	if err != nil {
		writeError(c, http.StatusBadGateway, "instruments_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusOK, model.InstrumentListResponse{Instruments: response})
}

func (s *Server) handlePrice(c *gin.Context) {
	ticker := strings.TrimSpace(c.Param("ticker"))
	if ticker == "" {
		writeError(c, http.StatusBadRequest, "bad_request", "ticker is required")
		return
	}

	response, err := s.service.GetPrice(ticker)
	if err != nil {
		writeError(c, http.StatusBadRequest, "price_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusOK, response)
}

func (s *Server) handleOrderBook(c *gin.Context) {
	ticker := strings.TrimSpace(c.Param("ticker"))
	if ticker == "" {
		writeError(c, http.StatusBadRequest, "bad_request", "ticker is required")
		return
	}

	response, err := s.service.GetOrderBook(ticker)
	if err != nil {
		writeError(c, http.StatusBadRequest, "orderbook_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusOK, response)
}

func (s *Server) handleTrades(c *gin.Context) {
	ticker := strings.TrimSpace(c.Param("ticker"))
	if ticker == "" {
		writeError(c, http.StatusBadRequest, "bad_request", "ticker is required")
		return
	}

	response, err := s.service.GetTrades(ticker)
	if err != nil {
		writeError(c, http.StatusBadRequest, "trades_failed", err.Error())
		return
	}

	writeJSON(c, http.StatusOK, response)
}

func (s *Server) handleNotFound(c *gin.Context) {
	writeError(c, http.StatusNotFound, "not_found", "route not found")
}

func (s *Server) handleMethodNotAllowed(c *gin.Context) {
	writeError(c, http.StatusMethodNotAllowed, "method_not_allowed", "method not allowed")
}

func writeJSON(c *gin.Context, status int, payload any) {
	c.JSON(status, payload)
}

func writeError(c *gin.Context, status int, code, message string) {
	writeJSON(c, status, model.ErrorResponse{
		Code:    code,
		Message: message,
	})
}
