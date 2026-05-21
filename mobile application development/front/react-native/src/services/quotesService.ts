import { brokerData } from '../data/repository';
import { useQuoteStore, type Quote } from '../stores/quoteStore';
import { useDevStore } from '../stores/devStore';

function getMockQuote(symbol: string): Quote {
  const tk = brokerData.findTicker(symbol);
  const s = brokerData.dayStats(tk);
  return {
    symbol: tk.sym,
    price: s.price,
    change: s.change,
    changePct: s.changePct,
    spark: s.spark,
    updatedAt: Date.now(),
  };
}

export function getQuote(symbol: string): Quote | undefined {
  const q = useQuoteStore.getState().getQuote(symbol);
  if (q) return q;
  if (useDevStore.getState().mockMarketData) {
    return getMockQuote(symbol);
  }
  return undefined;
}

export function listWatchlist(): Quote[] {
  const { quotes } = useQuoteStore.getState();
  const keys = Object.keys(quotes);
  if (keys.length === 0) {
    if (!useDevStore.getState().mockMarketData) return [];
    return brokerData.tickers
      .map((tk) => getMockQuote(tk.sym))
      .sort((a, b) => a.symbol.localeCompare(b.symbol));
  }
  return keys
    .map((sym) => quotes[sym])
    .filter((q): q is Quote => !!q)
    .sort((a, b) => a.symbol.localeCompare(b.symbol));
}

export function subscribeQuote(symbol: string): () => void {
  return useQuoteStore.getState().subscribe(symbol);
}

export function subscribeQuotes(symbols: string[]): () => void {
  const unsubs = symbols.map((s) => subscribeQuote(s));
  return () => unsubs.forEach((u) => u());
}
