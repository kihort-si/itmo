package main

import (
	"log"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"
	"github.com/com.vt/core-worker/config"
	"github.com/com.vt/core-worker/workflows"
	"github.com/com.vt/core-worker/activities"
)

func main() {
	cfg := config.Load()

	c, err := client.Dial(client.Options{
		HostPort: cfg.TemporalHostPort,
	})
	if err != nil {
		log.Fatalln("Unable to create Temporal client", err)
	}
	defer c.Close()

	w := worker.New(c, cfg.TaskQueue, worker.Options{})

	w.RegisterWorkflow(workflows.RegisterUserWorkflow)
	w.RegisterActivity(activities.CreateClientActivity)
	w.RegisterActivity(activities.CreateAuthAccountActivity)
	w.RegisterActivity(activities.CreateBalanceAccountActivity)
	w.RegisterActivity(activities.CreatePortfolioActivity)
	w.RegisterActivity(activities.DeleteClientActivity)
	w.RegisterActivity(activities.DeleteAuthAccountActivity)
	w.RegisterActivity(activities.CloseBalanceAccountActivity)
	w.RegisterActivity(activities.ClosePortfolioActivity)
	w.RegisterActivity(activities.SendEmailNotificationActivity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}