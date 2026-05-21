package main

import (
	"net/http"

	"github.com/redis/go-redis/v9"
)

type app struct {
	cfg   config
	http  *http.Client
	redis *redis.Client
}

type loginRequest struct {
	Email    string `json:"email"`
	Login    string `json:"login"`
	Password string `json:"password"`
}

type registerRequest struct {
	Name     string `json:"name"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

type authUser struct {
	UserID    int      `json:"userId"`
	Email     string   `json:"email"`
	Username  string   `json:"username"`
	ClntID    int      `json:"clntId"`
	Status    string   `json:"status"`
	Roles     []string `json:"roles"`
	SessionID string   `json:"sessionId"`
}

type tokenPairResponse struct {
	AccessToken  string   `json:"accessToken"`
	RefreshToken string   `json:"refreshToken"`
	SessionID    string   `json:"sessionId"`
	User         authUser `json:"user"`
}

type clientResponse struct {
	ClntID               int     `json:"clntId"`
	Username             string  `json:"username"`
	RegionRefsIdentifier int     `json:"regionRefsIdentifier"`
	CreatedAt            string  `json:"createdAt"`
	LanguageCode         string  `json:"languageCode"`
	Status               string  `json:"status"`
	FullName             string  `json:"fullName"`
	Email                string  `json:"email"`
	PhoneNumber          *string `json:"phoneNumber"`
	AdditionalInfo       *string `json:"additionalInfo"`
}

type mobileAuthResponse struct {
	Token        string     `json:"token"`
	AccessToken  string     `json:"accessToken"`
	RefreshToken string     `json:"refreshToken"`
	Email        string     `json:"email"`
	Username     string     `json:"username"`
	Name         string     `json:"name"`
	ClntID       int        `json:"clntId"`
	Roles        []string   `json:"roles"`
	Balance      float64    `json:"balance"`
	User         mobileUser `json:"user"`
}

type mobileUser struct {
	ID       string   `json:"id"`
	UserID   int      `json:"userId"`
	ClntID   int      `json:"clntId"`
	Email    string   `json:"email"`
	Username string   `json:"username"`
	Name     string   `json:"name"`
	Roles    []string `json:"roles"`
	Status   string   `json:"status"`
	Balance  float64  `json:"balance"`
}

type availabilityResponse struct {
	InUse bool `json:"inUse"`
}

type cachedPayload struct {
	Status      int         `json:"status"`
	ContentType string      `json:"contentType"`
	Body        []byte      `json:"body"`
	Header      http.Header `json:"header,omitempty"`
}

type errorResponse struct {
	Error   string `json:"error"`
	Message string `json:"message"`
}
