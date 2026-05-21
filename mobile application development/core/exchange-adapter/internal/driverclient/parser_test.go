package driverclient

import "testing"

func TestParseAllInstrumentsResponseIgnoresGarbageBytesAroundPrice(t *testing.T) {
	response := "--- SBER РїРѕСЃР»РµРґРЅСЏСЏ С†РµРЅР°: 1\xa4E00.50 ---\nР—РђРЇР’РљР РќРђ РџРћРљРЈРџРљРЈ:\n--- GAZP РїРѕСЃР»РµРґРЅСЏСЏ С†РµРЅР°: 250.10 ---"

	result, err := ParseAllInstrumentsResponse(response)
	if err != nil {
		t.Fatalf("ParseAllInstrumentsResponse returned error: %v", err)
	}

	if len(result) != 2 {
		t.Fatalf("expected 2 instruments, got %d", len(result))
	}

	if result[0].Ticker != "SBER" || result[0].LastPrice != 100.50 {
		t.Fatalf("unexpected first instrument: %#v", result[0])
	}
	if result[1].Ticker != "GAZP" || result[1].LastPrice != 250.10 {
		t.Fatalf("unexpected second instrument: %#v", result[1])
	}
}

func TestParseEmbeddedPriceExtractsNumericPrefix(t *testing.T) {
	price, err := parseEmbeddedPrice("100.25 (Р°РІС‚Рѕ)")
	if err != nil {
		t.Fatalf("parseEmbeddedPrice returned error: %v", err)
	}
	if price != 100.25 {
		t.Fatalf("expected 100.25, got %v", price)
	}
}

func TestParseSimulatorAsciiFormats(t *testing.T) {
	price, err := ParsePriceResponse("SBER: last price 321.15")
	if err != nil {
		t.Fatalf("ParsePriceResponse returned error: %v", err)
	}
	if price.Ticker != "SBER" || price.Price != 321.15 {
		t.Fatalf("unexpected price response: %#v", price)
	}

	book, err := ParseOrderBookResponse("--- SBER order book ---\nBIDS:\n320.95 120 (auto)\nASKS:\n321.35 95 (auto)")
	if err != nil {
		t.Fatalf("ParseOrderBookResponse returned error: %v", err)
	}
	if book.Ticker != "SBER" || len(book.Bids) != 1 || len(book.Asks) != 1 {
		t.Fatalf("unexpected order book response: %#v", book)
	}
	if !book.Bids[0].Generated || book.Bids[0].Price != 320.95 || book.Bids[0].Volume != 120 {
		t.Fatalf("unexpected bid level: %#v", book.Bids[0])
	}

	trades, err := ParseTradesResponse("--- trades for SBER ---\n321.10 40 ev7")
	if err != nil {
		t.Fatalf("ParseTradesResponse returned error: %v", err)
	}
	if trades.Ticker != "SBER" || len(trades.Trades) != 1 || trades.Trades[0].EventID != 7 {
		t.Fatalf("unexpected trades response: %#v", trades)
	}

	all, err := ParseAllInstrumentsResponse("--- SBER last price: 321.10 ---\nBUY ORDERS:\n--- GAZP last price: 135.20 ---")
	if err != nil {
		t.Fatalf("ParseAllInstrumentsResponse returned error: %v", err)
	}
	if len(all) != 2 || all[0].Ticker != "SBER" || all[1].Ticker != "GAZP" {
		t.Fatalf("unexpected all response: %#v", all)
	}
}
