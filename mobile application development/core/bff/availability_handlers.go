package main

import (
	"context"
	"fmt"
	"net/http"
	"strings"
)

func (a *app) checkUsername(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Username string `json:"username"`
	}
	if !decodeJSON(w, r, &req) {
		return
	}
	inUse, err := a.checkInUse(r.Context(), "/api/v1/clients/checkUsernameInUse", map[string]string{"username": strings.TrimSpace(req.Username)})
	if err != nil {
		writeError(w, http.StatusBadGateway, "clients_unavailable", err.Error())
		return
	}
	writeJSON(w, http.StatusOK, availabilityResponse{InUse: inUse})
}

func (a *app) checkEmail(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Email string `json:"email"`
	}
	if !decodeJSON(w, r, &req) {
		return
	}
	inUse, err := a.checkInUse(r.Context(), "/api/v1/clients/checkEmailInUse", map[string]string{"email": strings.TrimSpace(strings.ToLower(req.Email))})
	if err != nil {
		writeError(w, http.StatusBadGateway, "clients_unavailable", err.Error())
		return
	}
	writeJSON(w, http.StatusOK, availabilityResponse{InUse: inUse})
}

func (a *app) checkInUse(ctx context.Context, path string, payload map[string]string) (bool, error) {
	var resp availabilityResponse
	status, raw, err := a.doJSON(ctx, http.MethodPost, path, payload, "", &resp)
	if err != nil {
		return false, err
	}
	if status >= 400 {
		return false, fmt.Errorf("upstream returned %d: %s", status, string(raw))
	}
	return resp.InUse, nil
}
