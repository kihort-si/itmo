package driverclient

import (
	"strings"
	"testing"
	"time"

	"github.com/com.vt/exchange-adapter/internal/config"
)

func TestSimulatorExecuteSupportsCoreCommands(t *testing.T) {
	sim := NewSimulator(config.Config{
		DriverSimulatorSeed:               7,
		DriverSimulatorTickInterval:       time.Hour,
		DriverSimulatorEventsPerTick:      1,
		DriverSimulatorTradeBurstMin:      1,
		DriverSimulatorTradeBurstMax:      1,
		DriverSimulatorPriceStepBps:       10,
		DriverSimulatorSpreadBps:          10,
		DriverSimulatorMaxTradesPerTicker: 16,
	})

	all, err := sim.Execute("ALL")
	if err != nil {
		t.Fatalf("ALL failed: %v", err)
	}
	if !strings.Contains(all, "--- SBER last price:") {
		t.Fatalf("unexpected ALL response: %q", all)
	}

	price, err := sim.Execute("PRICE SBER")
	if err != nil {
		t.Fatalf("PRICE failed: %v", err)
	}
	if !strings.Contains(price, "SBER: last price") {
		t.Fatalf("unexpected PRICE response: %q", price)
	}

	book, err := sim.Execute("BOOK SBER")
	if err != nil {
		t.Fatalf("BOOK failed: %v", err)
	}
	if !strings.Contains(book, "--- SBER order book ---") {
		t.Fatalf("unexpected BOOK response: %q", book)
	}

	order, err := sim.Execute("SBER 1 321.10 15")
	if err != nil {
		t.Fatalf("submit order failed: %v", err)
	}
	if !strings.Contains(order, "SIM ORDER ACCEPTED SBER BUY 321.10 15") {
		t.Fatalf("unexpected order response: %q", order)
	}

	deals, err := sim.Execute("DEALS SBER")
	if err != nil {
		t.Fatalf("DEALS failed: %v", err)
	}
	if !strings.Contains(deals, "ev") {
		t.Fatalf("unexpected DEALS response: %q", deals)
	}
}
