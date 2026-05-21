import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';
import type { Ccy } from '../data/market';
import type { TradeResponse } from '../api/types';
import { brokerData } from '../data/repository';

export type ExecutedTrade = {
  id: string;
  instrumentId: string;
  symbol: string;
  side: 'buy' | 'sell';
  quantity: number;
  price: number;
  currency: Ccy;
  executedAt: number;
};

type PortfolioState = {
  trades: ExecutedTrade[];
  recordTrade: (trade: TradeResponse) => void;
  seedFromMockIfEmpty: () => void;
  getCostBasis: (symbol: string) => number | null;
  getQuantity: (symbol: string) => number;
};

function toExecuted(t: TradeResponse): ExecutedTrade {
  return {
    id: t.id,
    instrumentId: t.instrumentId,
    symbol: t.symbol,
    side: t.side,
    quantity: t.quantity,
    price: t.price,
    currency: t.currency,
    executedAt: t.executedAt,
  };
}

function computeCostBasis(trades: ExecutedTrade[], symbol: string): number | null {
  const sym = symbol.toUpperCase();
  let qty = 0;
  let cost = 0;
  const sorted = [...trades]
    .filter((t) => t.symbol.toUpperCase() === sym)
    .sort((a, b) => a.executedAt - b.executedAt);

  for (const t of sorted) {
    if (t.side === 'buy') {
      cost += t.price * t.quantity;
      qty += t.quantity;
    } else {
      if (qty <= 0) continue;
      const avg = cost / qty;
      const sellQty = Math.min(t.quantity, qty);
      cost -= avg * sellQty;
      qty -= sellQty;
    }
  }

  if (qty <= 0) return null;
  return cost / qty;
}

function computeQuantity(trades: ExecutedTrade[], symbol: string): number {
  const sym = symbol.toUpperCase();
  return trades
    .filter((t) => t.symbol.toUpperCase() === sym)
    .reduce((q, t) => q + (t.side === 'buy' ? t.quantity : -t.quantity), 0);
}

export const usePortfolioStore = create<PortfolioState>()(
  persist(
    (set, get) => ({
      trades: [],

      recordTrade: (trade) => {
        set((s) => ({ trades: [...s.trades, toExecuted(trade)] }));
      },

      seedFromMockIfEmpty: () => {
        if (get().trades.length > 0) return;
        const seeded: ExecutedTrade[] = brokerData.portfolioHoldings.map((h, i) => {
          const tk = brokerData.findTicker(h.sym);
          return {
            id: `seed-${h.sym}`,
            instrumentId: h.sym,
            symbol: h.sym,
            side: 'buy' as const,
            quantity: h.qty,
            price: h.avg,
            currency: tk.ccy,
            executedAt: Date.UTC(2026, 4, 1) + i * 86400000,
          };
        });
        set({ trades: seeded });
      },

      getCostBasis: (symbol) => computeCostBasis(get().trades, symbol),

      getQuantity: (symbol) => computeQuantity(get().trades, symbol),
    }),
    {
      name: 'portfolio-trades',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (s) => ({ trades: s.trades }),
    }
  )
);
