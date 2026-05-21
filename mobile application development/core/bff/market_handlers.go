package main

import (
	"fmt"
	"net/http"
	"strings"
)

func (a *app) marketStocks(w http.ResponseWriter, r *http.Request) {
	a.proxy(w, r, http.MethodGet, withRawQuery(r, "/v1/stocks"), "")
}

func (a *app) marketStockChart(w http.ResponseWriter, r *http.Request) {
	ticker := strings.TrimSpace(strings.ToUpper(r.PathValue("ticker")))
	if ticker == "" {
		writeError(w, http.StatusBadRequest, "validation_error", "ticker path parameter is required")
		return
	}
	a.proxy(w, r, http.MethodGet, withRawQuery(r, fmt.Sprintf("/v1/stocks/%s/chart", ticker)), "")
}
