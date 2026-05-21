package main

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"
)

func TestMarketStocksProxyPreservesQueryAndContentType(t *testing.T) {
	t.Parallel()

	var gotPath string

	app := &app{
		cfg: config{CoreBaseURL: "http://core.local", HTTPTimeout: time.Second},
		http: &http.Client{
			Transport: roundTripFunc(func(r *http.Request) (*http.Response, error) {
				gotPath = r.URL.RequestURI()
				return jsonResponse(http.StatusOK, "application/json; charset=utf-8", `{"stocks":[{"ticker":"GAZP"}]}`), nil
			}),
		},
	}

	req := httptest.NewRequest(http.MethodGet, "/api/broker-app/v1/market/stocks?search=gaz&sortBy=PRICE", nil)
	rec := httptest.NewRecorder()

	app.routes().ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", rec.Code)
	}
	if gotPath != "/v1/stocks?search=gaz&sortBy=PRICE" {
		t.Fatalf("expected upstream path to preserve query, got %q", gotPath)
	}
	if got := rec.Header().Get("Content-Type"); got != "application/json; charset=utf-8" {
		t.Fatalf("expected content type passthrough, got %q", got)
	}
	if body := strings.TrimSpace(rec.Body.String()); body != `{"stocks":[{"ticker":"GAZP"}]}` {
		t.Fatalf("unexpected body %q", body)
	}
}

func TestLoginPassesThroughUpstreamErrorBody(t *testing.T) {
	t.Parallel()

	app := &app{
		cfg: config{CoreBaseURL: "http://core.local", HTTPTimeout: time.Second},
		http: &http.Client{
			Transport: roundTripFunc(func(r *http.Request) (*http.Response, error) {
				if r.URL.Path != "/api/v1/auth/login" {
					t.Fatalf("unexpected path %s", r.URL.Path)
				}
				return jsonResponse(http.StatusUnauthorized, "application/json", `{"error":"invalid_credentials","message":"wrong password"}`), nil
			}),
		},
	}

	req := httptest.NewRequest(http.MethodPost, "/api/broker-app/v1/auth/login", strings.NewReader(`{"login":"user","password":"bad"}`))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	app.routes().ServeHTTP(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected status 401, got %d", rec.Code)
	}

	var got errorResponse
	if err := json.Unmarshal(rec.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if got.Error != "invalid_credentials" || got.Message != "wrong password" {
		t.Fatalf("unexpected upstream error payload %+v", got)
	}
}

func TestRegisterReturnsBadGatewayWhenAvailabilityCheckFails(t *testing.T) {
	t.Parallel()

	app := &app{
		cfg: config{CoreBaseURL: "http://upstream.invalid", HTTPTimeout: time.Second},
		http: &http.Client{
			Transport: roundTripFunc(func(*http.Request) (*http.Response, error) {
				return nil, errors.New("dial failed")
			}),
		},
	}

	req := httptest.NewRequest(http.MethodPost, "/api/broker-app/v1/auth/register", strings.NewReader(`{
		"name":"Test User",
		"username":"tester",
		"email":"user@example.com",
		"password":"secret1"
	}`))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	app.routes().ServeHTTP(rec, req)

	if rec.Code != http.StatusBadGateway {
		t.Fatalf("expected status 502, got %d", rec.Code)
	}

	var got errorResponse
	if err := json.Unmarshal(rec.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if got.Error != "clients_unavailable" {
		t.Fatalf("expected clients_unavailable, got %+v", got)
	}
}

type roundTripFunc func(*http.Request) (*http.Response, error)

func (fn roundTripFunc) RoundTrip(r *http.Request) (*http.Response, error) {
	return fn(r)
}

func jsonResponse(status int, contentType, body string) *http.Response {
	return &http.Response{
		StatusCode: status,
		Header:     http.Header{"Content-Type": []string{contentType}},
		Body:       io.NopCloser(strings.NewReader(body)),
	}
}
