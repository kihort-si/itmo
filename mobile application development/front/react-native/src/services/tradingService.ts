import { isNetworkOrMockError } from '../api/client';
import { mockExecuteTrade, mockGetPortfolio } from '../api/mockApi';
import { buyTradeApi, sellTradeApi } from '../api/tradesApi';
import { getPortfolioApi } from '../api/userApi';
import type { PortfolioPosition, TradeResponse } from '../api/types';
import { brokerData } from '../data/repository';
import type { Ccy, Ticker } from '../data/market';
import { useAuthStore } from '../stores/authStore';
import { usePortfolioStore } from '../stores/portfolioStore';
import { getQuote } from './quotesService';

export type HoldingRow = {
  instrumentId: string;
  symbol: string;
  quantity: number;
  avgCost: number;
  price: number;
  change: number;
  changePct: number;
  spark: number[];
  ticker: Ticker;
  valueRub: number;
  investedRub: number;
  pnlRub: number;
  pnlPct: number;
};

export type PortfolioSummary = {
  totalValueRub: number;
  cashRub: number;
  totalPnlRub: number;
  totalPnlPct: number;
  dayChangeRub: number;
  dayChangePct: number;
  holdings: HoldingRow[];
};

function toRub(amount: number, ccy: Ccy): number {
  return ccy === 'USD' ? amount * brokerData.fx.USD_RUB : amount;
}

export async function getPortfolioPositions(): Promise<{
  positions: PortfolioPosition[];
  cashRub: number;
}> {
  try {
    const p = await getPortfolioApi();
    return {
      positions: p.positions,
      cashRub: p.cashRub ?? useAuthStore.getState().balance,
    };
  } catch (e) {
    if (!isNetworkOrMockError(e)) throw e;
    const mock = mockGetPortfolio();
    return { positions: mock.positions, cashRub: mock.cashRub ?? brokerData.cashRub };
  }
}

export async function executeTrade(
  instrumentId: string,
  quantity: number,
  side: 'buy' | 'sell'
): Promise<TradeResponse> {
  const body = { instrumentId, quantity };
  let trade: TradeResponse;
  try {
    trade = side === 'buy' ? await buyTradeApi(body) : await sellTradeApi(body);
  } catch (e) {
    if (!isNetworkOrMockError(e)) throw e;
    trade = mockExecuteTrade(body, side);
  }

  usePortfolioStore.getState().recordTrade(trade);

  const tk = brokerData.findTicker(trade.symbol);
  const totalRub = toRub(trade.price * trade.quantity, tk.ccy);
  const auth = useAuthStore.getState();
  if (side === 'buy') {
    auth.setBalance(Math.max(0, auth.balance - totalRub * 1.005));
  } else {
    auth.setBalance(auth.balance + totalRub * 0.995);
  }

  return trade;
}

export async function listHoldings(
  userId?: string,
  positions?: PortfolioPosition[]
): Promise<HoldingRow[]> {
  void userId;
  usePortfolioStore.getState().seedFromMockIfEmpty();

  const { positions: pos } = positions
    ? { positions }
    : await getPortfolioPositions();

  const { getCostBasis, getQuantity } = usePortfolioStore.getState();

  return pos
    .map((p) => {
      const sym = p.symbol || p.instrumentId;
      const tk = brokerData.findTicker(sym);
      const q = getQuote(sym);
      const qty = p.quantity > 0 ? p.quantity : getQuantity(sym);
      if (qty <= 0) return null;

      const avgCost =
        getCostBasis(sym) ??
        brokerData.portfolioHoldings.find((h) => h.sym === sym)?.avg ??
        q?.price ??
        0;
      const price = q?.price ?? 0;
      const change = q?.change ?? 0;
      const changePct = q?.changePct ?? 0;
      const spark = q?.spark ?? [];
      const valueRub = toRub(price * qty, tk.ccy);
      const investedRub = toRub(avgCost * qty, tk.ccy);
      const pnlRub = valueRub - investedRub;

      return {
        instrumentId: p.instrumentId,
        symbol: sym,
        quantity: qty,
        avgCost,
        price,
        change,
        changePct,
        spark,
        ticker: tk,
        valueRub,
        investedRub,
        pnlRub,
        pnlPct: investedRub > 0 ? (pnlRub / investedRub) * 100 : 0,
      };
    })
    .filter((h): h is HoldingRow => h !== null);
}

export async function getPortfolioSummary(): Promise<PortfolioSummary> {
  const holdings = await listHoldings();
  const cashRub = useAuthStore.getState().balance;
  const holdingsValue = holdings.reduce((s, h) => s + h.valueRub, 0);
  const totalValueRub = holdingsValue + cashRub;
  const totalPnlRub = holdings.reduce((s, h) => s + h.pnlRub, 0);
  const totalInvested = holdings.reduce((s, h) => s + h.investedRub, 0) + cashRub;
  const totalPnlPct = totalInvested > 0 ? (totalPnlRub / totalInvested) * 100 : 0;
  const dayChangeRub = holdings.reduce(
    (s, h) => s + toRub(h.change * h.quantity, h.ticker.ccy),
    0
  );
  const dayChangePct = holdingsValue > 0 ? (dayChangeRub / holdingsValue) * 100 : 0;

  return {
    totalValueRub,
    cashRub,
    totalPnlRub,
    totalPnlPct,
    dayChangeRub,
    dayChangePct,
    holdings,
  };
}
