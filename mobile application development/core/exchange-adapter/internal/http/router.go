package http

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"github.com/com.vt/exchange-adapter/internal/exchange"
	"github.com/com.vt/exchange-adapter/internal/orders"
)

type Server struct {
	service *exchange.Service
	orders  *orders.Service
}

func NewServer(service *exchange.Service, orderService *orders.Service) *Server {
	return &Server{
		service: service,
		orders:  orderService,
	}
}

func (s *Server) Router() http.Handler {
	router := gin.New()
	router.Use(gin.Logger(), gin.Recovery())
	router.NoRoute(s.handleNotFound)
	router.NoMethod(s.handleMethodNotAllowed)

	router.GET("/health", s.handleHealth)

	v1 := router.Group("/v1")
	v1.POST("/orders", s.handleSubmitOrder)
	v1.POST("/client-orders", s.handleCreateClientOrder)
	v1.GET("/client-orders", s.handleListClientOrders)
	v1.GET("/client-orders/:orderId", s.handleGetClientOrder)
	v1.GET("/instruments", s.handleInstruments)
	v1.GET("/price/:ticker", s.handlePrice)
	v1.GET("/orderbook/:ticker", s.handleOrderBook)
	v1.GET("/trades/:ticker", s.handleTrades)

	return router
}
