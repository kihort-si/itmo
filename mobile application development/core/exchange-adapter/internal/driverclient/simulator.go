package driverclient

import (
	"fmt"
	"math"
	"math/rand"
	"sort"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/com.vt/exchange-adapter/internal/config"
)

type Simulator struct {
	mu              sync.Mutex
	rng             *rand.Rand
	tickers         []string
	instruments     map[string]*simInstrument
	nextEventID     int
	tickInterval    time.Duration
	eventsPerTick   int
	tradeBurstMin   int
	tradeBurstMax   int
	priceStepBps    int
	spreadBps       int
	maxTradesStored int
	stopCh          chan struct{}
}

type simInstrument struct {
	Ticker    string
	BasePrice float64
	LastPrice float64
	Trades    []simTrade
}

type simTrade struct {
	Price   float64
	Volume  int
	EventID int
}

func NewSimulator(cfg config.Config) *Simulator {
	s := &Simulator{
		rng:             rand.New(rand.NewSource(cfg.DriverSimulatorSeed)),
		tickers:         []string{"SBER", "GAZP", "LKOH", "TATN", "MOEX", "NVTK", "ROSN", "YDEX", "VTBR", "GMKN"},
		instruments:     make(map[string]*simInstrument),
		tickInterval:    cfg.DriverSimulatorTickInterval,
		eventsPerTick:   maxInt(1, cfg.DriverSimulatorEventsPerTick),
		tradeBurstMin:   maxInt(1, cfg.DriverSimulatorTradeBurstMin),
		tradeBurstMax:   maxInt(1, cfg.DriverSimulatorTradeBurstMax),
		priceStepBps:    maxInt(1, cfg.DriverSimulatorPriceStepBps),
		spreadBps:       maxInt(1, cfg.DriverSimulatorSpreadBps),
		maxTradesStored: maxInt(8, cfg.DriverSimulatorMaxTradesPerTicker),
		stopCh:          make(chan struct{}),
	}
	if s.tradeBurstMax < s.tradeBurstMin {
		s.tradeBurstMax = s.tradeBurstMin
	}

	basePrices := map[string]float64{
		"SBER": 320.60,
		"GAZP": 135.20,
		"LKOH": 5624.00,
		"TATN": 590.00,
		"MOEX": 168.00,
		"NVTK": 960.00,
		"ROSN": 414.00,
		"YDEX": 4054.00,
		"VTBR": 91.05,
		"GMKN": 136.00,
	}
	for _, ticker := range s.tickers {
		s.instruments[ticker] = &simInstrument{
			Ticker:    ticker,
			BasePrice: basePrices[ticker],
			LastPrice: basePrices[ticker],
			Trades:    make([]simTrade, 0, s.maxTradesStored),
		}
	}

	go s.run()
	return s
}

func (s *Simulator) Execute(command string) (string, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	command = strings.TrimSpace(command)
	if command == "" {
		return "", fmt.Errorf("empty command")
	}
	parts := strings.Fields(command)
	if len(parts) == 0 {
		return "", fmt.Errorf("empty command")
	}

	switch strings.ToUpper(parts[0]) {
	case "ALL":
		return s.renderAll(), nil
	case "PRICE":
		if len(parts) != 2 {
			return "", fmt.Errorf("PRICE requires ticker")
		}
		return s.renderPrice(strings.ToUpper(parts[1]))
	case "BOOK":
		if len(parts) != 2 {
			return "", fmt.Errorf("BOOK requires ticker")
		}
		return s.renderBook(strings.ToUpper(parts[1]))
	case "DEALS":
		if len(parts) != 2 {
			return "", fmt.Errorf("DEALS requires ticker")
		}
		return s.renderDeals(strings.ToUpper(parts[1]))
	default:
		return s.submitOrder(parts)
	}
}

func (s *Simulator) SetLastPrices(prices map[string]float64) {
	s.SyncState(prices, 0)
}

func (s *Simulator) SyncState(prices map[string]float64, nextEventID int) {
	s.mu.Lock()
	defer s.mu.Unlock()

	for ticker, price := range prices {
		instrument := s.instruments[strings.ToUpper(ticker)]
		if instrument == nil || price <= 0 {
			continue
		}
		instrument.LastPrice = roundPrice(price)
		instrument.BasePrice = instrument.LastPrice
		instrument.Trades = instrument.Trades[:0]
	}
	if nextEventID > s.nextEventID {
		s.nextEventID = nextEventID
	}
}

func (s *Simulator) run() {
	ticker := time.NewTicker(s.tickInterval)
	defer ticker.Stop()
	for {
		select {
		case <-ticker.C:
			s.mu.Lock()
			for i := 0; i < s.eventsPerTick; i++ {
				s.generateMarketEvent()
			}
			s.mu.Unlock()
		case <-s.stopCh:
			return
		}
	}
}

func (s *Simulator) generateMarketEvent() {
	instrument := s.randomInstrument()
	if instrument == nil {
		return
	}

	burst := s.tradeBurstMin
	if s.tradeBurstMax > s.tradeBurstMin {
		burst += s.rng.Intn(s.tradeBurstMax - s.tradeBurstMin + 1)
	}
	for i := 0; i < burst; i++ {
		nextPrice := s.nextMarketPrice(instrument)
		instrument.LastPrice = nextPrice
		s.nextEventID++
		s.appendTrade(instrument, simTrade{
			Price:   instrument.LastPrice,
			Volume:  10 + s.rng.Intn(190),
			EventID: s.nextEventID,
		})
	}
}

func (s *Simulator) nextMarketPrice(instrument *simInstrument) float64 {
	base := instrument.BasePrice
	if base <= 0 {
		base = instrument.LastPrice
	}
	if base <= 0 {
		base = 0.01
	}

	deviationBps := ((instrument.LastPrice / base) - 1) * 10000
	reversionBps := -deviationBps * 0.18
	maxReversionBps := float64(maxInt(2, s.priceStepBps*2))
	if reversionBps > maxReversionBps {
		reversionBps = maxReversionBps
	}
	if reversionBps < -maxReversionBps {
		reversionBps = -maxReversionBps
	}

	noiseBps := (s.rng.Float64()*2 - 1) * float64(s.priceStepBps)
	moveBps := reversionBps + noiseBps
	nextPrice := instrument.LastPrice * (1 + moveBps/10000)
	lowerBound := base * 0.97
	upperBound := base * 1.03
	if nextPrice < lowerBound {
		nextPrice = lowerBound + (base-lowerBound)*s.rng.Float64()*0.25
	}
	if nextPrice > upperBound {
		nextPrice = upperBound - (upperBound-base)*s.rng.Float64()*0.25
	}
	if nextPrice < 0.01 {
		nextPrice = 0.01
	}
	return roundPrice(nextPrice)
}

func (s *Simulator) slowlyMoveBasePrice(instrument *simInstrument, price float64) {
	if price <= 0 {
		return
	}
	if instrument.BasePrice <= 0 {
		instrument.BasePrice = price
		return
	}
	instrument.BasePrice = roundPrice(instrument.BasePrice*0.995 + price*0.005)
	if instrument.BasePrice < 0.01 {
		instrument.BasePrice = 0.01
	}
}

func (s *Simulator) submitOrder(parts []string) (string, error) {
	if len(parts) != 4 {
		return "", fmt.Errorf("unexpected order command: %q", strings.Join(parts, " "))
	}
	ticker := strings.ToUpper(parts[0])
	sideFlag, err := strconv.Atoi(parts[1])
	if err != nil {
		return "", fmt.Errorf("parse side flag: %w", err)
	}
	price, err := strconv.ParseFloat(parts[2], 64)
	if err != nil {
		return "", fmt.Errorf("parse order price: %w", err)
	}
	volume, err := strconv.Atoi(parts[3])
	if err != nil {
		return "", fmt.Errorf("parse order volume: %w", err)
	}
	instrument := s.instruments[ticker]
	if instrument == nil {
		return "", fmt.Errorf("ticker %s not found", ticker)
	}

	instrument.LastPrice = roundPrice(price)
	s.slowlyMoveBasePrice(instrument, instrument.LastPrice)
	s.nextEventID++
	s.appendTrade(instrument, simTrade{
		Price:   roundPrice(price),
		Volume:  volume,
		EventID: s.nextEventID,
	})

	side := "SELL"
	if sideFlag == 1 {
		side = "BUY"
	}
	return fmt.Sprintf("SIM ORDER ACCEPTED %s %s %.2f %d ev%d", ticker, side, roundPrice(price), volume, s.nextEventID), nil
}

func (s *Simulator) renderPrice(ticker string) (string, error) {
	instrument := s.instruments[ticker]
	if instrument == nil {
		return "", fmt.Errorf("ticker %s not found", ticker)
	}
	return fmt.Sprintf("%s: last price %.2f", ticker, instrument.LastPrice), nil
}

func (s *Simulator) renderBook(ticker string) (string, error) {
	instrument := s.instruments[ticker]
	if instrument == nil {
		return "", fmt.Errorf("ticker %s not found", ticker)
	}
	var lines []string
	lines = append(lines, fmt.Sprintf("--- %s order book ---", ticker))
	lines = append(lines, "BIDS:")
	for _, level := range s.bookLevels(instrument, true) {
		lines = append(lines, fmt.Sprintf("%.2f %d (auto)", level.Price, level.Volume))
	}
	lines = append(lines, "ASKS:")
	for _, level := range s.bookLevels(instrument, false) {
		lines = append(lines, fmt.Sprintf("%.2f %d (auto)", level.Price, level.Volume))
	}
	return strings.Join(lines, "\n"), nil
}

func (s *Simulator) renderDeals(ticker string) (string, error) {
	instrument := s.instruments[ticker]
	if instrument == nil {
		return "", fmt.Errorf("ticker %s not found", ticker)
	}
	lines := []string{fmt.Sprintf("--- trades for %s ---", ticker)}
	if len(instrument.Trades) == 0 {
		lines = append(lines, "no trades")
		return strings.Join(lines, "\n"), nil
	}
	for _, trade := range instrument.Trades {
		lines = append(lines, fmt.Sprintf("%.2f %d ev%d", trade.Price, trade.Volume, trade.EventID))
	}
	return strings.Join(lines, "\n"), nil
}

func (s *Simulator) renderAll() string {
	var lines []string
	tickers := append([]string(nil), s.tickers...)
	sort.Strings(tickers)
	for _, ticker := range tickers {
		instrument := s.instruments[ticker]
		lines = append(lines, fmt.Sprintf("--- %s last price: %.2f ---", ticker, instrument.LastPrice))
		lines = append(lines, "BUY ORDERS:")
		for _, level := range s.bookLevels(instrument, true) {
			lines = append(lines, fmt.Sprintf("%.2f %d (auto)", level.Price, level.Volume))
		}
		lines = append(lines, "SELL ORDERS:")
		for _, level := range s.bookLevels(instrument, false) {
			lines = append(lines, fmt.Sprintf("%.2f %d (auto)", level.Price, level.Volume))
		}
	}
	return strings.Join(lines, "\n")
}

func (s *Simulator) bookLevels(instrument *simInstrument, bid bool) []simTrade {
	levels := make([]simTrade, 0, 10)
	spreadFactor := float64(s.spreadBps) / 10000.0
	stepFactor := math.Max(spreadFactor/2, 0.0005)
	for i := 0; i < 10; i++ {
		delta := float64(i+1) * stepFactor
		price := instrument.LastPrice
		if bid {
			price *= 1 - delta
		} else {
			price *= 1 + delta
		}
		levels = append(levels, simTrade{
			Price:  roundPrice(price),
			Volume: 50 + s.rng.Intn(250),
		})
	}
	if bid {
		sort.Slice(levels, func(i, j int) bool { return levels[i].Price > levels[j].Price })
	} else {
		sort.Slice(levels, func(i, j int) bool { return levels[i].Price < levels[j].Price })
	}
	return levels
}

func (s *Simulator) appendTrade(instrument *simInstrument, trade simTrade) {
	instrument.Trades = append(instrument.Trades, trade)
	if len(instrument.Trades) > s.maxTradesStored {
		instrument.Trades = append([]simTrade(nil), instrument.Trades[len(instrument.Trades)-s.maxTradesStored:]...)
	}
}

func (s *Simulator) randomInstrument() *simInstrument {
	if len(s.tickers) == 0 {
		return nil
	}
	return s.instruments[s.tickers[s.rng.Intn(len(s.tickers))]]
}

func roundPrice(value float64) float64 {
	return math.Round(value*100) / 100
}

func maxInt(a, b int) int {
	if a > b {
		return a
	}
	return b
}
