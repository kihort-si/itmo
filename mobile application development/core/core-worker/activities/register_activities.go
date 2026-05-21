package activities

import (
	"context"
	"fmt"
	"strings"

	"go.temporal.io/sdk/activity"
	"github.com/com.vt/core-worker/config"
)

// —————— Основные активности ——————

func CreateClientActivity(ctx context.Context, input RegistrationInput) (int, error) {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	
	req := CreateClientRequest{
		Username:          input.Username,
		RegionRefsIdentifier: input.RegionID,
		LanguageCode:      input.LanguageCode,
		FullName:          input.Name,
		Email:             input.Email,
	}
	url := fmt.Sprintf("%s/api/v1/clients/client", strings.TrimSuffix(cfg.APIGatewayURL, "/"))
	var resp ClientResponse
	err := doHTTPRequest(ctx, "POST", url, req, &resp)
	if err != nil {
		logger.Error("CreateClientActivity failed", "error", err)
		return 0, err
	}
	logger.Info("Client created", "clntId", resp.ClntId, "full response", resp)
	if resp.ClntId == 0 {
		return 0, fmt.Errorf("received clntId = 0 from API, response: %+v", resp)
	}
	return resp.ClntId, nil
}

func CreateAuthAccountActivity(ctx context.Context, clntId int, email, username, password string) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	req := AuthRegisterRequest{
		ClntId:   clntId,
		Email:    email,
		Username: username,
		Password: password,
	}
	url := fmt.Sprintf("%s/api/v1/auth/register", strings.TrimSuffix(cfg.APIGatewayURL, "/"))
	var resp AuthRegisterResponse
	err := doHTTPRequest(ctx, "POST", url, req, &resp)
	if err != nil {
		logger.Error("CreateAuthAccountActivity failed", "error", err)
		return err
	}
	logger.Info("Auth account created", "userId", resp.UserId)
	return nil
}

func CreateBalanceAccountActivity(ctx context.Context, clntId, currencyId int) (int, error) {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	req := CreateBalanceAccountRequest{
		ClntId: clntId,
		CurrId: currencyId,
	}
	url := fmt.Sprintf("%s/api/v1/balm/accounts", strings.TrimSuffix(cfg.APIGatewayURL, "/"))
	var resp BalanceAccountResponse
	err := doHTTPRequest(ctx, "POST", url, req, &resp)
	if err != nil {
		logger.Error("CreateBalanceAccountActivity failed", "error", err)
		return 0, err
	}
	logger.Info("Balance account created", "accId", resp.AccId)
	return resp.AccId, nil
}

func CreatePortfolioActivity(ctx context.Context, clntId int, portfolioName string) (int, error) {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	req := CreatePortfolioRequest{
		ClntId: clntId,
		Name:   portfolioName,
	}
	url := fmt.Sprintf("%s/api/v1/depository/portfolios", strings.TrimSuffix(cfg.APIGatewayURL, "/"))
	var resp PortfolioResponse
	err := doHTTPRequest(ctx, "POST", url, req, &resp)
	if err != nil {
		logger.Error("CreatePortfolioActivity failed", "error", err)
		return 0, err
	}
	logger.Info("Portfolio created", "portId", resp.PortId)
	return resp.PortId, nil
}

// —————— Компенсирующие активности ——————

func DeleteClientActivity(ctx context.Context, clntId int) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	url := fmt.Sprintf("%s/api/v1/clients/client/%d", strings.TrimSuffix(cfg.APIGatewayURL, "/"), clntId)
	err := doHTTPRequest(ctx, "DELETE", url, nil, nil)
	if err != nil {
		logger.Error("DeleteClientActivity failed", "error", err)
		return err
	}
	logger.Info("Client deleted", "clntId", clntId)
	return nil
}

func DeleteAuthAccountActivity(ctx context.Context, clntId int) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	url := fmt.Sprintf("%s/api/v1/auth/byClntId/%d", strings.TrimSuffix(cfg.APIGatewayURL, "/"), clntId)
	err := doHTTPRequest(ctx, "DELETE", url, nil, nil)
	if err != nil {
		logger.Error("DeleteAuthAccountActivity failed", "error", err)
		return err
	}
	logger.Info("Auth account deleted", "clntId", clntId)
	return nil
}

func CloseBalanceAccountActivity(ctx context.Context, accId int) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	url := fmt.Sprintf("%s/api/v1/balm/accounts/%d/close", strings.TrimSuffix(cfg.APIGatewayURL, "/"), accId)
	err := doHTTPRequest(ctx, "POST", url, nil, nil)
	if err != nil {
		logger.Error("CloseBalanceAccountActivity failed", "error", err)
		return err
	}
	logger.Info("Balance account closed", "accId", accId)
	return nil
}

func ClosePortfolioActivity(ctx context.Context, portId int) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	url := fmt.Sprintf("%s/api/v1/depository/portfolios/%d/close", strings.TrimSuffix(cfg.APIGatewayURL, "/"), portId)
	err := doHTTPRequest(ctx, "POST", url, nil, nil)
	if err != nil {
		logger.Error("ClosePortfolioActivity failed", "error", err)
		return err
	}
	logger.Info("Portfolio closed", "portId", portId)
	return nil
}

// Активность отправки уведомления (использует свой конфиг)
func SendEmailNotificationActivity(ctx context.Context, reportType string, input RegistrationInput) error {
	logger := activity.GetLogger(ctx)
	cfg := config.Load()
	
	publisher, err := NewRabbitMQPublisher(cfg.RabbitMQURL, cfg.MailExchange)
	if err != nil {
		logger.Error("Failed to connect to RabbitMQ", "error", err)
		return err
	}
	defer publisher.Close()

	routingKey := fmt.Sprintf("%s.%s", cfg.MailBaseType, reportType)
	msg := MailMessage{
		Type:       cfg.MailBaseType,
		ReportType: reportType,
		Data: map[string]interface{}{
			"name":     input.Name,
			"username": input.Username,
			"email":    input.Email,
		},
	}
	err = publisher.Publish(ctx, routingKey, msg)
	if err != nil {
		logger.Error("Failed to publish email notification", "error", err)
		return err
	}
	logger.Info("Email notification sent", "routingKey", routingKey)
	return nil
}