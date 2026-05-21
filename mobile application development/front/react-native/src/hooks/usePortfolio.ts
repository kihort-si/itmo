import { useCallback, useEffect, useMemo, useState } from 'react';
import { getPortfolioSummary, type PortfolioSummary } from '../services/tradingService';
import { subscribeQuotes } from '../services/quotesService';
import { useAuthStore } from '../stores/authStore';
import { usePortfolioStore } from '../stores/portfolioStore';
import { useQuoteStore } from '../stores/quoteStore';

const PORTFOLIO_REFRESH_MS = 5000;

export function usePortfolio() {
  const isAuthed = useAuthStore((s) => !!s.jwt);
  const balance = useAuthStore((s) => s.balance);
  const trades = usePortfolioStore((s) => s.trades);
  const quotes = useQuoteStore((s) => s.quotes);
  const [summary, setSummary] = useState<PortfolioSummary | null>(null);
  const [loading, setLoading] = useState(false);
  const holdingSymbols = useMemo(
    () => (summary?.holdings ?? []).map((holding) => holding.symbol),
    [summary]
  );

  const refresh = useCallback(async () => {
    if (!isAuthed) {
      setSummary(null);
      return;
    }
    setLoading(true);
    try {
      usePortfolioStore.getState().seedFromMockIfEmpty();
      const s = await getPortfolioSummary();
      setSummary(s);
    } finally {
      setLoading(false);
    }
  }, [isAuthed]);

  useEffect(() => {
    void refresh();
    const timer = setInterval(() => {
      void refresh();
    }, PORTFOLIO_REFRESH_MS);
    return () => clearInterval(timer);
  }, [refresh, balance, trades.length, quotes]);

  useEffect(() => {
    if (!isAuthed || holdingSymbols.length === 0) return;
    return subscribeQuotes(holdingSymbols);
  }, [holdingSymbols, isAuthed]);

  return { summary, loading, refresh };
}
