package main

import (
	"context"
	"encoding/json"
	"errors"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"go.temporal.io/sdk/client"
)

func TestValidateRegisterRequest(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name    string
		req     registerRequest
		wantErr string
	}{
		{name: "missing name", req: registerRequest{Username: "ab", Email: "user@example.com", Password: "secret1"}, wantErr: "name is required"},
		{name: "short username", req: registerRequest{Name: "User", Username: "a", Email: "user@example.com", Password: "secret1"}, wantErr: "username must contain at least 2 characters"},
		{name: "invalid email", req: registerRequest{Name: "User", Username: "ab", Email: "userexample.com", Password: "secret1"}, wantErr: "email must be valid"},
		{name: "short password", req: registerRequest{Name: "User", Username: "ab", Email: "user@example.com", Password: "12345"}, wantErr: "password must contain at least 6 characters"},
		{name: "valid", req: registerRequest{Name: "User", Username: "ab", Email: "user@example.com", Password: "secret1"}},
	}

	for _, tc := range tests {
		tc := tc
		t.Run(tc.name, func(t *testing.T) {
			t.Parallel()
			err := validateRegisterRequest(tc.req)
			if tc.wantErr == "" && err != nil {
				t.Fatalf("expected no error, got %v", err)
			}
			if tc.wantErr != "" && (err == nil || err.Error() != tc.wantErr) {
				t.Fatalf("expected %q, got %v", tc.wantErr, err)
			}
		})
	}
}

func TestRegisterStartsWorkflowAndReturnsCreated(t *testing.T) {
	t.Parallel()

	fakeRun := fakeWorkflowRun{id: "wf-123", runID: "run-456"}
	fakeTemporal := &fakeWorkflowClient{run: fakeRun}
	app := &app{
		cfg: config{
			TaskQueue:         "core-worker-queue",
			WorkflowTimeout:   2 * time.Second,
			BaseCurrencyID:    643,
			LanguageCode:      "ru",
			RegionID:          7,
			BasePortfolioName: "Main",
		},
		temporalClient: fakeTemporal,
	}

	req := httptest.NewRequest(http.MethodPost, "/api/v1/core-gateway/register", strings.NewReader(`{
		"name":"  Test User  ",
		"username":"  tester  ",
		"email":"  USER@Example.COM ",
		"password":"secret1"
	}`))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()

	app.routes().ServeHTTP(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected status 201, got %d with body %s", rec.Code, rec.Body.String())
	}
	if fakeTemporal.workflowName != "RegisterUserWorkflow" {
		t.Fatalf("expected workflow name RegisterUserWorkflow, got %q", fakeTemporal.workflowName)
	}
	if fakeTemporal.options.TaskQueue != "core-worker-queue" {
		t.Fatalf("expected task queue to be propagated, got %+v", fakeTemporal.options)
	}

	input, ok := fakeTemporal.args[0].(registrationWorkflowInput)
	if !ok {
		t.Fatalf("expected registrationWorkflowInput arg, got %T", fakeTemporal.args[0])
	}
	if input.Username != "tester" || input.Email != "user@example.com" || input.Name != "Test User" {
		t.Fatalf("expected normalized input, got %+v", input)
	}

	var got registerResponse
	if err := json.Unmarshal(rec.Body.Bytes(), &got); err != nil {
		t.Fatalf("decode response: %v", err)
	}
	if got.WorkflowID != "wf-123" || got.RunID != "run-456" || got.Status != "registered" {
		t.Fatalf("unexpected response %+v", got)
	}
}

func TestRegisterReturnsBadGatewayWhenWorkflowStartFails(t *testing.T) {
	t.Parallel()

	app := &app{
		cfg:            config{WorkflowTimeout: time.Second},
		temporalClient: &fakeWorkflowClient{err: errors.New("temporal unavailable")},
	}

	req := httptest.NewRequest(http.MethodPost, "/api/v1/core-gateway/register", strings.NewReader(`{
		"name":"User",
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
	if got.Error != "workflow_start_failed" {
		t.Fatalf("unexpected error response %+v", got)
	}
}

type fakeWorkflowClient struct {
	options      client.StartWorkflowOptions
	workflowName interface{}
	args         []interface{}
	run          client.WorkflowRun
	err          error
}

func (f *fakeWorkflowClient) ExecuteWorkflow(ctx context.Context, options client.StartWorkflowOptions, workflow interface{}, args ...interface{}) (client.WorkflowRun, error) {
	f.options = options
	f.workflowName = workflow
	f.args = args
	return f.run, f.err
}

type fakeWorkflowRun struct {
	id    string
	runID string
	err   error
}

func (f fakeWorkflowRun) Get(ctx context.Context, valuePtr interface{}) error { return f.err }
func (f fakeWorkflowRun) GetWithOptions(ctx context.Context, valuePtr interface{}, options client.WorkflowRunGetOptions) error {
	return f.err
}
func (f fakeWorkflowRun) GetID() string    { return f.id }
func (f fakeWorkflowRun) GetRunID() string { return f.runID }
