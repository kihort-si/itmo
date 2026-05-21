package driverclient

import (
	"fmt"
	"strconv"
	"strings"

	"github.com/com.vt/exchange-adapter/internal/model"
)

func ParsePriceResponse(raw string) (model.PriceResponse, error) {
	line := strings.TrimSpace(raw)
	ticker, payload, err := splitByKnownDelimiter(
		line,
		": РїРѕСЃР»РµРґРЅСЏСЏ С†РµРЅР° ",
		": last price ",
	)
	if err != nil {
		return model.PriceResponse{}, fmt.Errorf("unexpected price response: %q", raw)
	}

	price, err := parseEmbeddedPrice(payload)
	if err != nil {
		return model.PriceResponse{}, fmt.Errorf("parse price: %w", err)
	}

	return model.PriceResponse{
		Ticker: ticker,
		Price:  price,
	}, nil
}

func ParseOrderBookResponse(raw string) (model.OrderBookResponse, error) {
	lines := sanitizeLines(raw)
	if len(lines) < 3 {
		return model.OrderBookResponse{}, fmt.Errorf("unexpected orderbook response: %q", raw)
	}

	ticker, err := parseWrappedHeader(lines[0], " СЃС‚Р°РєР°РЅ ---", " order book ---")
	if err != nil {
		return model.OrderBookResponse{}, fmt.Errorf("unexpected orderbook header: %q", lines[0])
	}

	book := model.OrderBookResponse{Ticker: strings.TrimSpace(ticker)}
	section := ""
	for _, line := range lines[1:] {
		switch line {
		case "BIDS:":
			section = "bids"
			continue
		case "ASKS:":
			section = "asks"
			continue
		}

		level, err := parseOrderBookLevel(line)
		if err != nil {
			return model.OrderBookResponse{}, err
		}
		switch section {
		case "bids":
			book.Bids = append(book.Bids, level)
		case "asks":
			book.Asks = append(book.Asks, level)
		}
	}

	return book, nil
}

func ParseTradesResponse(raw string) (model.TradesResponse, error) {
	lines := sanitizeLines(raw)
	if len(lines) == 0 {
		return model.TradesResponse{}, fmt.Errorf("unexpected trades response: %q", raw)
	}

	ticker, err := parseTradesHeader(lines[0])
	if err != nil {
		return model.TradesResponse{}, fmt.Errorf("unexpected trades header: %q", lines[0])
	}

	response := model.TradesResponse{Ticker: strings.TrimSpace(ticker)}
	for _, line := range lines[1:] {
		if line == "РќРµС‚ СЃРґРµР»РѕРє" || strings.EqualFold(line, "no trades") {
			return response, nil
		}

		trade, err := parseTrade(line)
		if err != nil {
			return model.TradesResponse{}, err
		}
		response.Trades = append(response.Trades, trade)
	}

	return response, nil
}

func ParseAllInstrumentsResponse(raw string) ([]model.InstrumentSnapshot, error) {
	raw = strings.ReplaceAll(raw, "\x00", "")
	lines := sanitizeLines(raw)
	result := make([]model.InstrumentSnapshot, 0)

	for _, line := range lines {
		if !strings.HasPrefix(line, "--- ") {
			continue
		}

		ticker, priceRaw, ok := parseAllInstrumentLine(line)
		if !ok {
			continue
		}
		price, err := parseEmbeddedPrice(priceRaw)
		if err != nil {
			return nil, fmt.Errorf("parse ALL price: %w", err)
		}
		result = append(result, model.InstrumentSnapshot{
			Ticker:    strings.TrimSpace(ticker),
			LastPrice: price,
		})
	}

	if len(result) == 0 {
		return nil, fmt.Errorf("unexpected ALL response: %q", raw)
	}
	return result, nil
}

func parseAllInstrumentLine(line string) (string, string, bool) {
	payload := strings.TrimPrefix(line, "--- ")
	if parts := strings.Split(payload, " РїРѕСЃР»РµРґРЅСЏСЏ С†РµРЅР°: "); len(parts) == 2 {
		return parts[0], parts[1], true
	}
	if parts := strings.Split(payload, " last price: "); len(parts) == 2 {
		return parts[0], parts[1], true
	}
	return "", "", false
}

func parseWrappedHeader(header string, legacySuffix string, asciiSuffix string) (string, error) {
	if !strings.HasPrefix(header, "--- ") {
		return "", fmt.Errorf("missing prefix")
	}
	switch {
	case strings.HasSuffix(header, legacySuffix):
		return strings.TrimSuffix(strings.TrimPrefix(header, "--- "), legacySuffix), nil
	case strings.HasSuffix(header, asciiSuffix):
		return strings.TrimSuffix(strings.TrimPrefix(header, "--- "), asciiSuffix), nil
	default:
		return "", fmt.Errorf("unexpected suffix")
	}
}

func parseTradesHeader(header string) (string, error) {
	switch {
	case strings.HasPrefix(header, "--- РЎРґРµР»РєРё РїРѕ ") && strings.HasSuffix(header, " ---"):
		return strings.TrimSuffix(strings.TrimPrefix(header, "--- РЎРґРµР»РєРё РїРѕ "), " ---"), nil
	case strings.HasPrefix(header, "--- trades for ") && strings.HasSuffix(header, " ---"):
		return strings.TrimSuffix(strings.TrimPrefix(header, "--- trades for "), " ---"), nil
	default:
		return "", fmt.Errorf("unexpected header")
	}
}

func parseOrderBookLevel(line string) (model.OrderBookLevel, error) {
	generated := strings.Contains(line, "(Р°РІС‚Рѕ)") || strings.Contains(strings.ToLower(line), "(auto)")
	cleaned := strings.TrimSpace(strings.ReplaceAll(strings.ReplaceAll(line, " (Р°РІС‚Рѕ)", ""), " (auto)", ""))
	parts := strings.Fields(cleaned)
	if len(parts) != 2 {
		return model.OrderBookLevel{}, fmt.Errorf("unexpected orderbook level: %q", line)
	}

	price, err := parseEmbeddedPrice(parts[0])
	if err != nil {
		return model.OrderBookLevel{}, fmt.Errorf("parse level price: %w", err)
	}
	volume, err := strconv.Atoi(parts[1])
	if err != nil {
		return model.OrderBookLevel{}, fmt.Errorf("parse level volume: %w", err)
	}

	return model.OrderBookLevel{
		Price:     price,
		Volume:    volume,
		Generated: generated,
	}, nil
}

func parseTrade(line string) (model.Trade, error) {
	parts := strings.Fields(strings.TrimSpace(line))
	if len(parts) != 3 {
		return model.Trade{}, fmt.Errorf("unexpected trade line: %q", line)
	}

	price, err := parseEmbeddedPrice(parts[0])
	if err != nil {
		return model.Trade{}, fmt.Errorf("parse trade price: %w", err)
	}
	volume, err := strconv.Atoi(parts[1])
	if err != nil {
		return model.Trade{}, fmt.Errorf("parse trade volume: %w", err)
	}
	eventID, err := parseEventID(parts[2])
	if err != nil {
		return model.Trade{}, err
	}

	return model.Trade{
		Price:   price,
		Volume:  volume,
		EventID: eventID,
	}, nil
}

func parseEventID(value string) (int, error) {
	switch {
	case strings.HasPrefix(value, "СЃРѕР±"):
		id, err := strconv.Atoi(strings.TrimPrefix(value, "СЃРѕР±"))
		if err != nil {
			return 0, fmt.Errorf("parse event id: %w", err)
		}
		return id, nil
	case strings.HasPrefix(strings.ToLower(value), "ev"):
		id, err := strconv.Atoi(strings.TrimPrefix(strings.ToLower(value), "ev"))
		if err != nil {
			return 0, fmt.Errorf("parse event id: %w", err)
		}
		return id, nil
	default:
		return 0, fmt.Errorf("unexpected event id token: %q", value)
	}
}

func sanitizeLines(raw string) []string {
	raw = strings.ReplaceAll(raw, "\r\n", "\n")
	raw = strings.ReplaceAll(raw, "\r", "\n")
	lines := strings.Split(raw, "\n")
	result := make([]string, 0, len(lines))
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line != "" {
			result = append(result, line)
		}
	}
	return result
}

func parseEmbeddedPrice(raw string) (float64, error) {
	token := extractNumericToken(raw)
	if token == "" {
		return 0, fmt.Errorf("missing numeric token in %q", raw)
	}

	price, err := strconv.ParseFloat(token, 64)
	if err != nil {
		return 0, fmt.Errorf("parse float token %q: %w", token, err)
	}

	return price, nil
}

func extractNumericToken(raw string) string {
	var builder strings.Builder
	started := false
	dotSeen := false

	for _, r := range raw {
		switch {
		case r >= '0' && r <= '9':
			builder.WriteRune(r)
			started = true
		case r == '-' && !started:
			builder.WriteRune(r)
			started = true
		case (r == '.' || r == ',') && started && !dotSeen:
			builder.WriteRune('.')
			dotSeen = true
		case started && isNumericTokenBoundary(r):
			return strings.TrimSuffix(builder.String(), ".")
		default:
			continue
		}
	}

	return strings.TrimSuffix(builder.String(), ".")
}

func splitByKnownDelimiter(value string, delimiters ...string) (string, string, error) {
	for _, delimiter := range delimiters {
		parts := strings.Split(value, delimiter)
		if len(parts) == 2 {
			return strings.TrimSpace(parts[0]), parts[1], nil
		}
	}
	return "", "", fmt.Errorf("delimiter not found")
}

func isNumericTokenBoundary(r rune) bool {
	switch r {
	case ' ', '\n', '\r', '\t':
		return true
	default:
		return false
	}
}
