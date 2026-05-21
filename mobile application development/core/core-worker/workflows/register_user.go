package workflows

import (
	"time"
	"fmt"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/workflow"
	"github.com/com.vt/core-worker/activities"
)

type RegistrationWorkflowInput struct {
	Name              string `json:"name"`
	Username          string `json:"username"`
	Email             string `json:"email"`
	Password          string `json:"password"`
	BaseCurrencyID    int    `json:"base_currency_id"`
	LanguageCode      string `json:"language_code"`
	RegionID          int    `json:"region_id"`
	BasePortfolioName string `json:"base_portfolio_name"`
}

func RegisterUserWorkflow(ctx workflow.Context, input RegistrationWorkflowInput) error {
	logger := workflow.GetLogger(ctx)

	retryPolicy := &temporal.RetryPolicy{
		InitialInterval:    3 * time.Second,
		BackoffCoefficient: 1.0,
		MaximumAttempts:    5,
	}
	activityOpts := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy:         retryPolicy,
	}
	ctx = workflow.WithActivityOptions(ctx, activityOpts)

	var clntId int
	var accId int
	var portId int

	// 1. Создание клиента
	err := workflow.ExecuteActivity(ctx, activities.CreateClientActivity, input).Get(ctx, &clntId)
	if err != nil {
		logger.Error("Failed to create client", "error", err)
		return err
	}

	// 2. Создание аккаунта авторизации
	err = workflow.ExecuteActivity(ctx, activities.CreateAuthAccountActivity, clntId, input.Email, input.Username, input.Password).Get(ctx, nil)
	if err != nil {
		logger.Error("Failed to create auth account", "error", err)
		if errComp := compensateClientDeletion(ctx, clntId); errComp != nil {
			return errComp
		}
		sendFailureEmail(ctx, input)
		return err
	}

	// 3. Создание ЛС
	err = workflow.ExecuteActivity(ctx, activities.CreateBalanceAccountActivity, clntId, input.BaseCurrencyID).Get(ctx, &accId)
	if err != nil {
		logger.Error("Failed to create balance account", "error", err)
		if errComp := compensateAuthAndClient(ctx, clntId); errComp != nil {
			return errComp
		}
		sendFailureEmail(ctx, input)
		return err
	}

	// 4. Создание портфеля
	err = workflow.ExecuteActivity(ctx, activities.CreatePortfolioActivity, clntId, input.BasePortfolioName).Get(ctx, &portId)
	if err != nil {
		logger.Error("Failed to create portfolio", "error", err)
		if errComp := compensatePortfolioBalanceAuthClient(ctx, clntId, accId); errComp != nil {
			return errComp
		}
		sendFailureEmail(ctx, input)
		return err
	}

	// 5. Отправка уведомления об успешной регистрации
	err = workflow.ExecuteActivity(ctx, activities.SendEmailNotificationActivity, "SUCCESSFULL_REGISTRATION_EMAIL", input).Get(ctx, nil)
	if err != nil {
		logger.Error("Failed to send success email, initiating rollback", "error", err)
		// Полный откат: закрыть портфель, закрыть ЛС, удалить auth, удалить клиента
		if errComp := compensateFullRollback(ctx, clntId, accId, portId); errComp != nil {
			return errComp
		}
		// Отправляем failure email (если откат успешен)
		sendFailureEmail(ctx, input)
		return fmt.Errorf("success email failed: %w", err)
	}

	logger.Info("Registration completed successfully", "clntId", clntId, "accId", accId, "portId", portId)
	return nil
}

// компенсации (используют те же опции, но с меньшими ретраями)
func compensateClientDeletion(ctx workflow.Context, clntId int) error {
	compensateOpts := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy: &temporal.RetryPolicy{
			InitialInterval:    2 * time.Second,
			BackoffCoefficient: 1.0,
			MaximumAttempts:    3,
		},
	}
	ctx = workflow.WithActivityOptions(ctx, compensateOpts)
	err := workflow.ExecuteActivity(ctx, activities.DeleteClientActivity, clntId).Get(ctx, nil)
	if err != nil {
		workflow.GetLogger(ctx).Error("Compensation DeleteClient failed", "error", err)
	}
	return err
}

func compensateAuthAndClient(ctx workflow.Context, clntId int) error {
	compensateOpts := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy: &temporal.RetryPolicy{
			InitialInterval:    2 * time.Second,
			BackoffCoefficient: 1.0,
			MaximumAttempts:    3,
		},
	}
	ctx = workflow.WithActivityOptions(ctx, compensateOpts)

	errAuth := workflow.ExecuteActivity(ctx, activities.DeleteAuthAccountActivity, clntId).Get(ctx, nil)
	if errAuth != nil {
		workflow.GetLogger(ctx).Error("Compensation DeleteAuthAccount failed", "error", errAuth)
		return errAuth
	}
	errClient := workflow.ExecuteActivity(ctx, activities.DeleteClientActivity, clntId).Get(ctx, nil)
	if errClient != nil {
		workflow.GetLogger(ctx).Error("Compensation DeleteClient failed", "error", errClient)
		return errClient
	}
	return nil
}

func compensatePortfolioBalanceAuthClient(ctx workflow.Context, clntId, accId int) error {
	compensateOpts := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy: &temporal.RetryPolicy{
			InitialInterval:    2 * time.Second,
			BackoffCoefficient: 1.0,
			MaximumAttempts:    3,
		},
	}
	ctx = workflow.WithActivityOptions(ctx, compensateOpts)

	errBalance := workflow.ExecuteActivity(ctx, activities.CloseBalanceAccountActivity, accId).Get(ctx, nil)
	if errBalance != nil {
		workflow.GetLogger(ctx).Error("Compensation CloseBalanceAccount failed", "error", errBalance)
		return errBalance
	}
	errAuth := workflow.ExecuteActivity(ctx, activities.DeleteAuthAccountActivity, clntId).Get(ctx, nil)
	if errAuth != nil {
		workflow.GetLogger(ctx).Error("Compensation DeleteAuthAccount failed", "error", errAuth)
		return errAuth
	}
	errClient := workflow.ExecuteActivity(ctx, activities.DeleteClientActivity, clntId).Get(ctx, nil)
	if errClient != nil {
		workflow.GetLogger(ctx).Error("Compensation DeleteClient failed", "error", errClient)
		return errClient
	}
	return nil
}

func sendFailureEmail(ctx workflow.Context, input RegistrationWorkflowInput) {
	compensateOpts := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
		RetryPolicy: &temporal.RetryPolicy{
			InitialInterval:    2 * time.Second,
			BackoffCoefficient: 1.0,
			MaximumAttempts:    3,
		},
	}
	ctx = workflow.WithActivityOptions(ctx, compensateOpts)
	err := workflow.ExecuteActivity(ctx, activities.SendEmailNotificationActivity, "FAILED_REGISTRATION_EMAIL", input).Get(ctx, nil)
	if err != nil {
		workflow.GetLogger(ctx).Error("Failed to send failure email (non-critical)", "error", err)
	}
}

func compensateFullRollback(ctx workflow.Context, clntId, accId, portId int) error {
    compensateOpts := workflow.ActivityOptions{
        StartToCloseTimeout: 10 * time.Second,
        RetryPolicy: &temporal.RetryPolicy{
            InitialInterval:    2 * time.Second,
            BackoffCoefficient: 1.0,
            MaximumAttempts:    3,
        },
    }
    ctx = workflow.WithActivityOptions(ctx, compensateOpts)

    // Закрыть портфель, если создан
    if portId != 0 {
        if err := workflow.ExecuteActivity(ctx, activities.ClosePortfolioActivity, portId).Get(ctx, nil); err != nil {
            workflow.GetLogger(ctx).Error("Compensation ClosePortfolio failed", "error", err)
            return err
        }
    }
    // Закрыть ЛС, если создан
    if accId != 0 {
        if err := workflow.ExecuteActivity(ctx, activities.CloseBalanceAccountActivity, accId).Get(ctx, nil); err != nil {
            workflow.GetLogger(ctx).Error("Compensation CloseBalanceAccount failed", "error", err)
            return err
        }
    }
    // Удалить auth аккаунт
    if err := workflow.ExecuteActivity(ctx, activities.DeleteAuthAccountActivity, clntId).Get(ctx, nil); err != nil {
        workflow.GetLogger(ctx).Error("Compensation DeleteAuthAccount failed", "error", err)
        return err
    }
    // Удалить клиента
    if err := workflow.ExecuteActivity(ctx, activities.DeleteClientActivity, clntId).Get(ctx, nil); err != nil {
        workflow.GetLogger(ctx).Error("Compensation DeleteClient failed", "error", err)
        return err
    }
    return nil
}