package main

import (
	"errors"
	"net/http"
	"strings"
)

func (a *app) login(w http.ResponseWriter, r *http.Request) {
	var req loginRequest
	if !decodeJSON(w, r, &req) {
		return
	}
	login := strings.TrimSpace(req.Login)
	if login == "" {
		login = strings.TrimSpace(req.Email)
	}
	if login == "" || req.Password == "" {
		writeError(w, http.StatusBadRequest, "validation_error", "login/email and password are required")
		return
	}

	var auth tokenPairResponse
	status, raw, err := a.doJSON(r.Context(), http.MethodPost, "/api/v1/auth/login", map[string]string{
		"login":    login,
		"password": req.Password,
	}, "", &auth)
	if err != nil {
		writeError(w, http.StatusBadGateway, "auth_unavailable", err.Error())
		return
	}
	if status >= 400 {
		writeUpstreamError(w, status, raw)
		return
	}
	a.writeMobileAuth(w, r, http.StatusOK, auth)
}

func (a *app) register(w http.ResponseWriter, r *http.Request) {
	var req registerRequest
	if !decodeJSON(w, r, &req) {
		return
	}
	req.Name = strings.TrimSpace(req.Name)
	req.Username = strings.TrimSpace(req.Username)
	req.Email = strings.TrimSpace(strings.ToLower(req.Email))
	if err := validateRegistration(req); err != nil {
		writeError(w, http.StatusBadRequest, "validation_error", err.Error())
		return
	}

	if inUse, err := a.checkInUse(r.Context(), "/api/v1/clients/checkUsernameInUse", map[string]string{"username": req.Username}); err != nil {
		writeError(w, http.StatusBadGateway, "clients_unavailable", err.Error())
		return
	} else if inUse {
		writeError(w, http.StatusConflict, "username_in_use", "username is already in use")
		return
	}
	if inUse, err := a.checkInUse(r.Context(), "/api/v1/clients/checkEmailInUse", map[string]string{"email": req.Email}); err != nil {
		writeError(w, http.StatusBadGateway, "clients_unavailable", err.Error())
		return
	} else if inUse {
		writeError(w, http.StatusConflict, "email_in_use", "email is already in use")
		return
	}

	status, raw, err := a.doJSON(r.Context(), http.MethodPost, "/api/v1/core-gateway/register", req, "", nil)
	if err != nil {
		writeError(w, http.StatusBadGateway, "gateway_unavailable", err.Error())
		return
	}
	if status >= 400 {
		writeUpstreamError(w, status, raw)
		return
	}

	var auth tokenPairResponse
	status, raw, err = a.doJSON(r.Context(), http.MethodPost, "/api/v1/auth/login", map[string]string{
		"login":    req.Email,
		"password": req.Password,
	}, "", &auth)
	if err != nil {
		writeError(w, http.StatusBadGateway, "auth_unavailable", err.Error())
		return
	}
	if status >= 400 {
		writeUpstreamError(w, status, raw)
		return
	}
	a.writeMobileAuth(w, r, http.StatusCreated, auth)
}

func (a *app) logout(w http.ResponseWriter, r *http.Request) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized", "missing bearer token")
		return
	}
	status, raw, err := a.doJSON(r.Context(), http.MethodPost, "/api/v1/auth/logout", nil, authHeader, nil)
	if err != nil {
		writeError(w, http.StatusBadGateway, "auth_unavailable", err.Error())
		return
	}
	if status >= 400 {
		writeUpstreamError(w, status, raw)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func validateRegistration(req registerRequest) error {
	switch {
	case req.Name == "":
		return errors.New("name is required")
	case len(req.Username) < 2:
		return errors.New("username must contain at least 2 characters")
	case !strings.Contains(req.Email, "@"):
		return errors.New("email must be valid")
	case len(req.Password) < 6:
		return errors.New("password must contain at least 6 characters")
	default:
		return nil
	}
}
