package main

import (
	"bytes"
	"context"
	"encoding/json"
	"io"
	"net/http"
	"net/url"
	"strings"
)

func withRawQuery(r *http.Request, path string) string {
	if r.URL.RawQuery == "" {
		return path
	}
	return path + "?" + r.URL.RawQuery
}

func (a *app) proxy(w http.ResponseWriter, r *http.Request, method, path, authHeader string) {
	status, body, contentType, err := a.doRaw(r.Context(), method, path, nil, authHeader)
	if err != nil {
		writeError(w, http.StatusBadGateway, "upstream_unavailable", err.Error())
		return
	}
	if contentType != "" {
		w.Header().Set("Content-Type", contentType)
	}
	w.WriteHeader(status)
	_, _ = w.Write(body)
}

func (a *app) doJSON(ctx context.Context, method, path string, body any, authHeader string, target any) (int, []byte, error) {
	var reader io.Reader
	if body != nil {
		raw, err := json.Marshal(body)
		if err != nil {
			return 0, nil, err
		}
		reader = bytes.NewReader(raw)
	}
	status, raw, _, err := a.doRaw(ctx, method, path, reader, authHeader)
	if err != nil {
		return 0, nil, err
	}
	if status < 400 && target != nil && len(raw) > 0 {
		if err := json.Unmarshal(raw, target); err != nil {
			return status, raw, err
		}
	}
	return status, raw, nil
}

func (a *app) doRaw(ctx context.Context, method, path string, body io.Reader, authHeader string) (int, []byte, string, error) {
	target, err := url.JoinPath(strings.TrimRight(a.cfg.CoreBaseURL, "/"), strings.TrimLeft(path, "/"))
	if err != nil {
		return 0, nil, "", err
	}
	if strings.Contains(path, "?") {
		target = strings.TrimRight(a.cfg.CoreBaseURL, "/") + "/" + strings.TrimLeft(path, "/")
	}
	req, err := http.NewRequestWithContext(ctx, method, target, body)
	if err != nil {
		return 0, nil, "", err
	}
	req.Header.Set("Accept", "application/json")
	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}
	if authHeader != "" {
		req.Header.Set("Authorization", authHeader)
	}
	res, err := a.http.Do(req)
	if err != nil {
		return 0, nil, "", err
	}
	defer res.Body.Close()
	raw, err := io.ReadAll(res.Body)
	if err != nil {
		return 0, nil, "", err
	}
	return res.StatusCode, raw, res.Header.Get("Content-Type"), nil
}
