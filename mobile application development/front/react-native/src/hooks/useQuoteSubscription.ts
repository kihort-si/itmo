import { useEffect } from 'react';
import { subscribeQuote, subscribeQuotes } from '../services/quotesService';
import { useQuoteStore } from '../stores/quoteStore';

export function useQuoteSubscription(symbol: string | null | undefined) {
  const quote = useQuoteStore((s) => (symbol ? s.getQuote(symbol) : undefined));

  useEffect(() => {
    if (!symbol) return;
    return subscribeQuote(symbol);
  }, [symbol]);

  return quote;
}

export function useQuotesSubscription(symbols: string[]) {
  const key = symbols.join(',');
  const quotes = useQuoteStore((s) => s.quotes);

  useEffect(() => {
    if (symbols.length === 0) return;
    return subscribeQuotes(symbols);
  }, [key]);

  return quotes;
}
