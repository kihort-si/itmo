package main

import "net/http"

func (a *app) routes() http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", a.health)
	mux.HandleFunc("POST /api/v1/core-gateway/register", a.register)
	return mux
}
