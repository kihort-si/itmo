package main

import (
	"encoding/json"
	"net/http"
	"strings"
)

func (a *app) cachedRefs(w http.ResponseWriter, r *http.Request) {
	upstreamPath := strings.TrimPrefix(r.URL.Path, "/api/broker-app/v1/refs")
	if upstreamPath == "" {
		upstreamPath = "/"
	}
	targetPath := "/api/v1/refs" + upstreamPath
	if r.URL.RawQuery != "" {
		targetPath += "?" + r.URL.RawQuery
	}
	key := "bff:static:" + targetPath

	if a.redis != nil {
		if raw, err := a.redis.Get(r.Context(), key).Bytes(); err == nil {
			var cached cachedPayload
			if json.Unmarshal(raw, &cached) == nil {
				writeCached(w, cached)
				return
			}
		}
	}

	status, body, contentType, err := a.doRaw(r.Context(), http.MethodGet, targetPath, nil, "")
	if err != nil {
		writeError(w, http.StatusBadGateway, "refs_unavailable", err.Error())
		return
	}
	payload := cachedPayload{Status: status, ContentType: contentType, Body: body}
	if status == http.StatusOK && a.redis != nil && a.cfg.StaticCacheTTL > 0 {
		if raw, err := json.Marshal(payload); err == nil {
			_ = a.redis.Set(r.Context(), key, raw, a.cfg.StaticCacheTTL).Err()
		}
	}
	writeCached(w, payload)
}

func writeCached(w http.ResponseWriter, payload cachedPayload) {
	if payload.ContentType != "" {
		w.Header().Set("Content-Type", payload.ContentType)
	}
	w.WriteHeader(payload.Status)
	_, _ = w.Write(payload.Body)
}
