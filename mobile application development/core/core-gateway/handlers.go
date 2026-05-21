package main

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strings"
	"time"

	"go.temporal.io/sdk/client"
)

func (a *app) health(w http.ResponseWriter, r *http.Request) {
	rabbitStatus := "disabled"
	if a.cfg.RabbitMQURL != "" {
		rabbitStatus = "disconnected"
		if a.rabbitConn != nil && !a.rabbitConn.IsClosed() {
			rabbitStatus = "connected"
		}
	}
	writeJSON(w, http.StatusOK, map[string]string{
		"status":   "ok",
		"temporal": a.cfg.TemporalHostPort,
		"rabbitmq": rabbitStatus,
	})
}

func (a *app) register(w http.ResponseWriter, r *http.Request) {
	var req registerRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid_json", "Request body must be valid JSON")
		return
	}
	req.Name = strings.TrimSpace(req.Name)
	req.Username = strings.TrimSpace(req.Username)
	req.Email = strings.TrimSpace(strings.ToLower(req.Email))
	if err := validateRegisterRequest(req); err != nil {
		writeError(w, http.StatusBadRequest, "validation_error", err.Error())
		return
	}

	input := registrationWorkflowInput{
		Name:              req.Name,
		Username:          req.Username,
		Email:             req.Email,
		Password:          req.Password,
		BaseCurrencyID:    a.cfg.BaseCurrencyID,
		LanguageCode:      a.cfg.LanguageCode,
		RegionID:          a.cfg.RegionID,
		BasePortfolioName: a.cfg.BasePortfolioName,
	}

	ctx, cancel := context.WithTimeout(r.Context(), a.cfg.WorkflowTimeout)
	defer cancel()

	workflowID := fmt.Sprintf("register-user-%s-%d", sanitizeWorkflowID(req.Username), time.Now().UnixNano())
	run, err := a.temporalClient.ExecuteWorkflow(ctx, client.StartWorkflowOptions{
		ID:        workflowID,
		TaskQueue: a.cfg.TaskQueue,
	}, "RegisterUserWorkflow", input)
	if err != nil {
		writeError(w, http.StatusBadGateway, "workflow_start_failed", err.Error())
		return
	}
	if err := run.Get(ctx, nil); err != nil {
		writeError(w, http.StatusBadGateway, "workflow_failed", err.Error())
		return
	}

	writeJSON(w, http.StatusCreated, registerResponse{
		Status:     "registered",
		WorkflowID: run.GetID(),
		RunID:      run.GetRunID(),
	})
}

func validateRegisterRequest(req registerRequest) error {
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
