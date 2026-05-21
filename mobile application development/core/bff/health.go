package main

import "net/http"

func (a *app) health(w http.ResponseWriter, r *http.Request) {
	redisStatus := "disabled"
	if a.redis != nil {
		redisStatus = "connected"
		if err := a.redis.Ping(r.Context()).Err(); err != nil {
			redisStatus = "disconnected"
		}
	}
	writeJSON(w, http.StatusOK, map[string]string{
		"status": "ok",
		"core":   a.cfg.CoreBaseURL,
		"redis":  redisStatus,
	})
}
