package main

import (
	"context"
	"fmt"
	"net/http"
	"strconv"
	"strings"
)

func (a *app) me(w http.ResponseWriter, r *http.Request) {
	principal, ok := a.requirePrincipal(w, r)
	if !ok {
		return
	}
	client, _ := a.fetchClient(r.Context(), principal.ClntID)
	writeJSON(w, http.StatusOK, a.mobileUser(principal, client))
}

func (a *app) emptyPortfolio(w http.ResponseWriter, r *http.Request) {
	if _, ok := a.requirePrincipal(w, r); !ok {
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"positions": []any{}, "cashRub": a.cfg.DefaultBalance})
}

func (a *app) updateBalance(w http.ResponseWriter, r *http.Request) {
	principal, ok := a.requirePrincipal(w, r)
	if !ok {
		return
	}
	var body struct {
		Balance float64 `json:"balance"`
	}
	if !decodeJSON(w, r, &body) {
		return
	}
	client, _ := a.fetchClient(r.Context(), principal.ClntID)
	user := a.mobileUser(principal, client)
	user.Balance = body.Balance
	writeJSON(w, http.StatusOK, user)
}

func (a *app) clientByID(w http.ResponseWriter, r *http.Request) {
	principal, ok := a.requirePrincipal(w, r)
	if !ok {
		return
	}
	clntID, err := strconv.Atoi(r.PathValue("clntId"))
	if err != nil {
		writeError(w, http.StatusBadRequest, "validation_error", "invalid clntId")
		return
	}
	if !hasRole(principal.Roles, "ADMIN") && principal.ClntID != clntID {
		writeError(w, http.StatusForbidden, "forbidden", "USER can only access own client")
		return
	}
	a.proxy(w, r, http.MethodGet, fmt.Sprintf("/api/v1/clients/client/%d", clntID), "")
}

func (a *app) requirePrincipal(w http.ResponseWriter, r *http.Request) (authUser, bool) {
	authHeader := r.Header.Get("Authorization")
	if authHeader == "" {
		writeError(w, http.StatusUnauthorized, "unauthorized", "missing bearer token")
		return authUser{}, false
	}
	var user authUser
	status, raw, err := a.doJSON(r.Context(), http.MethodGet, "/api/v1/auth/me", nil, authHeader, &user)
	if err != nil {
		writeError(w, http.StatusBadGateway, "auth_unavailable", err.Error())
		return authUser{}, false
	}
	if status >= 400 {
		writeUpstreamError(w, status, raw)
		return authUser{}, false
	}
	return user, true
}

func (a *app) writeMobileAuth(w http.ResponseWriter, r *http.Request, status int, auth tokenPairResponse) {
	client, _ := a.fetchClient(r.Context(), auth.User.ClntID)
	user := a.mobileUser(auth.User, client)
	writeJSON(w, status, mobileAuthResponse{
		Token:        auth.AccessToken,
		AccessToken:  auth.AccessToken,
		RefreshToken: auth.RefreshToken,
		Email:        user.Email,
		Username:     user.Username,
		Name:         user.Name,
		ClntID:       user.ClntID,
		Roles:        user.Roles,
		Balance:      user.Balance,
		User:         user,
	})
}

func (a *app) mobileUser(user authUser, client *clientResponse) mobileUser {
	name := user.Username
	email := user.Email
	username := user.Username
	if client != nil {
		if strings.TrimSpace(client.FullName) != "" {
			name = client.FullName
		}
		if strings.TrimSpace(client.Email) != "" {
			email = client.Email
		}
		if strings.TrimSpace(client.Username) != "" {
			username = client.Username
		}
	}
	return mobileUser{
		ID:       strconv.Itoa(user.UserID),
		UserID:   user.UserID,
		ClntID:   user.ClntID,
		Email:    email,
		Username: username,
		Name:     name,
		Roles:    user.Roles,
		Status:   user.Status,
		Balance:  a.cfg.DefaultBalance,
	}
}

func (a *app) fetchClient(ctx context.Context, clntID int) (*clientResponse, error) {
	var client clientResponse
	status, raw, err := a.doJSON(ctx, http.MethodGet, fmt.Sprintf("/api/v1/clients/client/%d", clntID), nil, "", &client)
	if err != nil {
		return nil, err
	}
	if status >= 400 {
		return nil, fmt.Errorf("client upstream returned %d: %s", status, string(raw))
	}
	return &client, nil
}
