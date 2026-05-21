package workflows

import (
	"testing"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"go.temporal.io/sdk/temporal"
	"go.temporal.io/sdk/testsuite"

	"github.com/com.vt/core-worker/activities"
)

func TestRegisterUserWorkflowHappyPath(t *testing.T) {
	t.Parallel()

	var suite testsuite.WorkflowTestSuite
	env := suite.NewTestWorkflowEnvironment()

	input := RegistrationWorkflowInput{
		Name:              "Test User",
		Username:          "tester",
		Email:             "user@example.com",
		Password:          "secret1",
		BaseCurrencyID:    643,
		LanguageCode:      "ru",
		RegionID:          7,
		BasePortfolioName: "Main",
	}

	env.OnActivity(activities.CreateClientActivity, mock.Anything, mock.Anything).Return(101, nil).Once()
	env.OnActivity(activities.CreateAuthAccountActivity, mock.Anything, 101, input.Email, input.Username, input.Password).Return(nil).Once()
	env.OnActivity(activities.CreateBalanceAccountActivity, mock.Anything, 101, input.BaseCurrencyID).Return(202, nil).Once()
	env.OnActivity(activities.CreatePortfolioActivity, mock.Anything, 101, input.BasePortfolioName).Return(303, nil).Once()
	env.OnActivity(activities.SendEmailNotificationActivity, mock.Anything, "SUCCESSFULL_REGISTRATION_EMAIL", mock.Anything).Return(nil).Once()

	env.ExecuteWorkflow(RegisterUserWorkflow, input)

	require.True(t, env.IsWorkflowCompleted())
	require.NoError(t, env.GetWorkflowError())
	env.AssertExpectations(t)
}

func TestRegisterUserWorkflowCompensatesWhenAuthCreationFails(t *testing.T) {
	t.Parallel()

	var suite testsuite.WorkflowTestSuite
	env := suite.NewTestWorkflowEnvironment()

	input := RegistrationWorkflowInput{
		Name:              "Test User",
		Username:          "tester",
		Email:             "user@example.com",
		Password:          "secret1",
		BaseCurrencyID:    643,
		LanguageCode:      "ru",
		RegionID:          7,
		BasePortfolioName: "Main",
	}

	authErr := temporal.NewNonRetryableApplicationError("auth failed", "AUTH_FAILED", nil)

	env.OnActivity(activities.CreateClientActivity, mock.Anything, mock.Anything).Return(101, nil).Once()
	env.OnActivity(activities.CreateAuthAccountActivity, mock.Anything, 101, input.Email, input.Username, input.Password).Return(authErr).Once()
	env.OnActivity(activities.DeleteClientActivity, mock.Anything, 101).Return(nil).Once()
	env.OnActivity(activities.SendEmailNotificationActivity, mock.Anything, "FAILED_REGISTRATION_EMAIL", mock.Anything).Return(nil).Once()

	env.ExecuteWorkflow(RegisterUserWorkflow, input)

	require.True(t, env.IsWorkflowCompleted())
	require.ErrorContains(t, env.GetWorkflowError(), "auth failed")
	env.AssertExpectations(t)
}
