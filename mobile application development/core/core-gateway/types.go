package main

import (
	"context"

	amqp "github.com/rabbitmq/amqp091-go"
	"go.temporal.io/sdk/client"
)

type workflowExecutor interface {
	ExecuteWorkflow(ctx context.Context, options client.StartWorkflowOptions, workflow interface{}, args ...interface{}) (client.WorkflowRun, error)
}

type app struct {
	cfg            config
	temporalClient workflowExecutor
	rabbitConn     *amqp.Connection
}

type registerRequest struct {
	Name     string `json:"name"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

type registrationWorkflowInput struct {
	Name              string `json:"name"`
	Username          string `json:"username"`
	Email             string `json:"email"`
	Password          string `json:"password"`
	BaseCurrencyID    int    `json:"base_currency_id"`
	LanguageCode      string `json:"language_code"`
	RegionID          int    `json:"region_id"`
	BasePortfolioName string `json:"base_portfolio_name"`
}

type registerResponse struct {
	Status     string `json:"status"`
	WorkflowID string `json:"workflowId"`
	RunID      string `json:"runId"`
}

type errorResponse struct {
	Error   string `json:"error"`
	Message string `json:"message"`
}
