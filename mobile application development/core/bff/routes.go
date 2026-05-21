package main

import "net/http"

func (a *app) routes() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", a.health)
	mux.HandleFunc("POST /api/broker-app/v1/auth/login", a.login)
	mux.HandleFunc("POST /api/broker-app/v1/auth/register", a.register)
	mux.HandleFunc("POST /api/broker-app/v1/auth/logout", a.logout)
	mux.HandleFunc("GET /api/broker-app/v1/auth/me", a.me)
	mux.HandleFunc("GET /api/broker-app/v1/users/me", a.me)
	mux.HandleFunc("GET /api/broker-app/v1/users/me/portfolio", a.emptyPortfolio)
	mux.HandleFunc("PUT /api/broker-app/v1/users/me/balance", a.updateBalance)
	mux.HandleFunc("POST /api/broker-app/v1/auth/checkUsernameInUse", a.checkUsername)
	mux.HandleFunc("POST /api/broker-app/v1/auth/checkEmailInUse", a.checkEmail)
	mux.HandleFunc("GET /api/broker-app/v1/client/{clntId}", a.clientByID)
	mux.HandleFunc("GET /api/broker-app/v1/refs/", a.cachedRefs)
	mux.HandleFunc("GET /api/broker-app/v1/market/stocks", a.marketStocks)
	mux.HandleFunc("GET /api/broker-app/v1/market/stocks/{ticker}/chart", a.marketStockChart)
	return mux
}
