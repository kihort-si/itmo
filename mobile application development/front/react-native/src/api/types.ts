export type AuthResponse = {
  token: string;
  accessToken?: string | null;
  refreshToken?: string | null;
  email: string;
  username?: string | null;
  name?: string | null;
  clntId?: number | null;
  roles: string[];
  balance: number;
  user?: UserMe | null;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
  username: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type UserMe = {
  id: string;
  userId?: number | null;
  clntId?: number | null;
  email: string;
  username?: string | null;
  name?: string | null;
  roles: string[];
  status?: string | null;
  balance: number;
};

export type AvailabilityResponse = {
  inUse: boolean;
};

export type PortfolioPosition = {
  instrumentId: string;
  symbol: string;
  quantity: number;
};

export type PortfolioResponse = {
  positions: PortfolioPosition[];
  cashRub?: number;
};

export type TradeRequest = {
  instrumentId: string;
  quantity: number;
};

export type TradeResponse = {
  id: string;
  instrumentId: string;
  symbol: string;
  side: 'buy' | 'sell';
  quantity: number;
  price: number;
  currency: 'RUB' | 'USD' | 'EUR';
  executedAt: number;
};

export type QuoteStreamMessage =
  | { type: 'quote'; symbol: string; price: number; change: number; changePct: number; spark?: number[] }
  | { type: 'subscribe'; symbols: string[] }
  | { type: 'error'; message: string };

export type MarketStocksRequest = {
  search?: string;
  sortBy?: 'PRICE' | 'NAME' | 'DAY_CHANGE_PCT';
  sortOrder?: 'ASC' | 'DESC';
  minPrice?: number;
  maxPrice?: number;
  minDayChangePct?: number;
  maxDayChangePct?: number;
};

export type MarketStockRow = {
  ticker: string;
  name: string;
  currency: string;
  lastPrice: number | null;
  dayChangePct: number | null;
};

export type MarketStocksResponse = {
  stocks: MarketStockRow[];
};

export type MarketChartType = 'LINE' | 'CANDLE';
export type MarketChartPeriod = '10M' | '1H' | '6H' | '1D' | '10D' | '1M' | '1W' | '6M' | '1Y' | 'ALL';
export type MarketChartTimeframe = 'M1' | 'M5' | 'M30' | 'H1' | 'D1' | 'W1';

export type MarketChartRequest = {
  chartType: MarketChartType;
  timeframe: MarketChartTimeframe;
  period: MarketChartPeriod;
};

export type MarketLinePoint = {
  ts: number;
  close: number;
};

export type MarketCandlePoint = {
  ts: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
};

export type MarketLineChartResponse = {
  ticker: string;
  chartType: 'LINE';
  timeframe: string;
  period: string;
  points: MarketLinePoint[];
};

export type MarketCandleChartResponse = {
  ticker: string;
  chartType: 'CANDLE';
  timeframe: string;
  period: string;
  candles: MarketCandlePoint[];
};

export type MarketChartResponse = MarketLineChartResponse | MarketCandleChartResponse;
