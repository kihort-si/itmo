

export type Market = 'MOEX' | 'NASDAQ';
export type Ccy = 'RUB' | 'USD' | 'EUR';

export type Ticker = {
  sym: string;
  market: Market;
  nameRu: string;
  nameEn: string;
  sector: string;
  sectorEn: string;
  base: number;
  vol: number;
  drift: number;
  ccy: Ccy;
};

export type Candle = { t: number; o: number; h: number; l: number; c: number; v: number };

export type Tf = '5m' | '30m' | '1h' | '1d' | '1w';

function imul(a: number, b: number): number {

  return Math.imul(a | 0, b | 0);
}

export function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return function () {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = imul(t ^ (t >>> 15), t | 1);
    t ^= t + imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export function strSeed(s: string): number {
  let h = 2166136261;
  for (let i = 0; i < s.length; i++) h = imul(h ^ s.charCodeAt(i), 16777619);
  return h >>> 0;
}

export const TICKERS: Ticker[] = [
  { sym: 'SBER',  market: 'MOEX',   nameRu: 'Сбербанк',     nameEn: 'Sberbank',           sector: 'Банки',           sectorEn: 'Banks',        base: 312.45, vol: 0.018, drift:  0.0006, ccy: 'RUB' },
  { sym: 'GAZP',  market: 'MOEX',   nameRu: 'Газпром',      nameEn: 'Gazprom',            sector: 'Энергетика',      sectorEn: 'Energy',       base: 138.62, vol: 0.022, drift: -0.0002, ccy: 'RUB' },
  { sym: 'LKOH',  market: 'MOEX',   nameRu: 'Лукойл',       nameEn: 'Lukoil',             sector: 'Энергетика',      sectorEn: 'Energy',       base: 7642.5, vol: 0.014, drift:  0.0003, ccy: 'RUB' },
  { sym: 'YDEX',  market: 'MOEX',   nameRu: 'Яндекс',       nameEn: 'Yandex',             sector: 'Технологии',      sectorEn: 'Tech',         base: 4378.0, vol: 0.026, drift:  0.0010, ccy: 'RUB' },
  { sym: 'T',     market: 'MOEX',   nameRu: 'Т-Технологии', nameEn: 'T-Technologies',     sector: 'Финансы',         sectorEn: 'Finance',      base: 3215.6, vol: 0.024, drift:  0.0007, ccy: 'RUB' },
  { sym: 'GMKN',  market: 'MOEX',   nameRu: 'Норникель',    nameEn: 'Nornickel',          sector: 'Металлургия',     sectorEn: 'Metals',       base: 132.84, vol: 0.020, drift: -0.0001, ccy: 'RUB' },
  { sym: 'ROSN',  market: 'MOEX',   nameRu: 'Роснефть',     nameEn: 'Rosneft',            sector: 'Энергетика',      sectorEn: 'Energy',       base: 588.30, vol: 0.017, drift:  0.0001, ccy: 'RUB' },
  { sym: 'NVTK',  market: 'MOEX',   nameRu: 'Новатэк',      nameEn: 'Novatek',            sector: 'Энергетика',      sectorEn: 'Energy',       base: 1234.5, vol: 0.019, drift: -0.0003, ccy: 'RUB' },
  { sym: 'AAPL',  market: 'NASDAQ', nameRu: 'Apple',        nameEn: 'Apple Inc.',         sector: 'Технологии',      sectorEn: 'Tech',         base: 218.74, vol: 0.014, drift:  0.0004, ccy: 'USD' },
  { sym: 'MSFT',  market: 'NASDAQ', nameRu: 'Microsoft',    nameEn: 'Microsoft Corp.',    sector: 'Технологии',      sectorEn: 'Tech',         base: 462.18, vol: 0.013, drift:  0.0005, ccy: 'USD' },
  { sym: 'NVDA',  market: 'NASDAQ', nameRu: 'NVIDIA',       nameEn: 'NVIDIA Corp.',       sector: 'Полупроводники',  sectorEn: 'Semis',        base: 138.92, vol: 0.030, drift:  0.0014, ccy: 'USD' },
  { sym: 'TSLA',  market: 'NASDAQ', nameRu: 'Tesla',        nameEn: 'Tesla Inc.',         sector: 'Авто',            sectorEn: 'Auto',         base: 248.50, vol: 0.034, drift: -0.0003, ccy: 'USD' },
  { sym: 'GOOGL', market: 'NASDAQ', nameRu: 'Alphabet',     nameEn: 'Alphabet Inc.',      sector: 'Технологии',      sectorEn: 'Tech',         base: 178.20, vol: 0.016, drift:  0.0004, ccy: 'USD' },
  { sym: 'AMZN',  market: 'NASDAQ', nameRu: 'Amazon',       nameEn: 'Amazon.com',         sector: 'E-commerce',      sectorEn: 'E-commerce',   base: 196.85, vol: 0.018, drift:  0.0005, ccy: 'USD' },
  { sym: 'META',  market: 'NASDAQ', nameRu: 'Meta',         nameEn: 'Meta Platforms',     sector: 'Технологии',      sectorEn: 'Tech',         base: 528.60, vol: 0.020, drift:  0.0006, ccy: 'USD' },
  { sym: 'AMD',   market: 'NASDAQ', nameRu: 'AMD',          nameEn: 'Advanced Micro Devices', sector: 'Полупроводники', sectorEn: 'Semis',     base: 156.30, vol: 0.028, drift:  0.0008, ccy: 'USD' },
];

export const FX = {
  RUB_USD: 0.0103,
  RUB_EUR: 0.0096,
  USD_RUB: 97.05,
  EUR_RUB: 104.8,
  USD_EUR: 0.93,
  EUR_USD: 1.075,
};

export function genCandles(t: Ticker, tf: Tf = '1d', n = 240): Candle[] {
  const msMap: Record<Tf, number> = {
    '5m': 5 * 60e3,
    '30m': 30 * 60e3,
    '1h': 60 * 60e3,
    '1d': 24 * 60 * 60e3,
    '1w': 7 * 24 * 60 * 60e3,
  };
  const ms = msMap[tf] || 24 * 60 * 60e3;
  const seedKey = t.sym + ':' + tf;
  const rand = mulberry32(strSeed(seedKey));
  const candles: Candle[] = [];
  let price = t.base * (0.85 + rand() * 0.3);
  const now = Date.UTC(2026, 4, 8, 9, 30) - 60e3;
  const start = now - n * ms;
  const volBase = t.base * 0.001 * (t.sym === 'GAZP' ? 5e6 : 2e5);
  for (let i = 0; i < n; i++) {
    const time = start + i * ms;
    let g = 0;
    for (let k = 0; k < 6; k++) g += rand();
    g = (g - 3) / 1.732;
    const ret = t.drift + t.vol * g;
    const open = price;
    const close = open * (1 + ret);
    const hRet = Math.abs(t.vol * (rand() * 1.5 + 0.2));
    const lRet = Math.abs(t.vol * (rand() * 1.5 + 0.2));
    const high = Math.max(open, close) * (1 + hRet);
    const low = Math.min(open, close) * (1 - lRet);
    const v = volBase * (0.4 + rand() * 1.8) * (1 + Math.abs(g) * 0.6);
    candles.push({ t: time, o: open, h: high, l: low, c: close, v });
    price = close;
  }
  const last = candles[candles.length - 1];
  const adjust = t.base / last.c;
  if (Math.abs(1 - adjust) < 0.4) {
    const k = Math.min(6, candles.length);
    for (let i = candles.length - k; i < candles.length; i++) {
      const w = (i - (candles.length - k) + 1) / k;
      const m = 1 + (adjust - 1) * w;
      candles[i].o *= m;
      candles[i].c *= m;
      candles[i].h *= m;
      candles[i].l *= m;
    }
  }
  return candles;
}

export type DayStats = {
  price: number;
  change: number;
  changePct: number;
  spark: number[];
  candles: Candle[];
};

export function dayStats(t: Ticker): DayStats {
  const c = genCandles(t, '1d', 60);
  const last = c[c.length - 1];
  const prev = c[c.length - 2];
  const change = last.c - prev.c;
  const changePct = (change / prev.c) * 100;
  const spark = c.slice(-30).map((b) => b.c);
  return { price: last.c, change, changePct, spark, candles: c };
}

export type ObLevel = { price: number; size: number };
export type Orderbook = { bids: ObLevel[]; asks: ObLevel[]; mid: number; tick: number };

export function genOrderbook(t: Ticker, levels = 12): Orderbook {
  const stats = dayStats(t);
  const mid = stats.price;
  const tick = mid > 1000 ? 0.5 : mid > 100 ? 0.05 : 0.01;
  const rand = mulberry32(strSeed(t.sym + ':ob'));
  const bids: ObLevel[] = [];
  const asks: ObLevel[] = [];
  for (let i = 0; i < levels; i++) {
    const bp = mid - tick * (i + 1) - rand() * tick * 0.4;
    const ap = mid + tick * (i + 1) + rand() * tick * 0.4;
    const baseSize = Math.round(50 + rand() * 950);
    const depthFactor = 1 + i * 0.15 + rand() * 0.4;
    bids.push({ price: bp, size: Math.round(baseSize * depthFactor) });
    asks.push({ price: ap, size: Math.round(baseSize * depthFactor * (0.7 + rand() * 0.6)) });
  }
  return { bids, asks, mid, tick };
}

export type Holding = { sym: string; qty: number; avg: number };
export const PORTFOLIO_HOLDINGS: Holding[] = [
  { sym: 'SBER', qty: 240, avg: 285.4 },
  { sym: 'YDEX', qty: 8, avg: 4012.0 },
  { sym: 'LKOH', qty: 3, avg: 7320.0 },
  { sym: 'NVDA', qty: 14, avg: 121.2 },
  { sym: 'AAPL', qty: 22, avg: 198.4 },
  { sym: 'T', qty: 18, avg: 3105.0 },
];

export const CASH_RUB = 184_320;

export type HistoryStatus = 'filled' | 'partial' | 'cancelled' | 'rejected' | 'pending';
export type HistoryType = 'buy' | 'sell' | 'cancel' | 'reject';
export type HistoryItem = {
  id: string;
  t: number;
  type: HistoryType;
  sym: string;
  qty: number;
  price: number;
  status: HistoryStatus;
  currency: Ccy;
  filledQty?: number;
  reason?: string;
};

export const HISTORY: HistoryItem[] = [
  { id: 'op-2024', t: Date.UTC(2026, 4, 8, 8, 12),  type: 'buy',    sym: 'NVDA', qty: 4,   price: 138.92,  status: 'filled',    currency: 'USD' },
  { id: 'op-2023', t: Date.UTC(2026, 4, 7, 16, 45), type: 'sell',   sym: 'GAZP', qty: 100, price: 138.62,  status: 'filled',    currency: 'RUB' },
  { id: 'op-2022', t: Date.UTC(2026, 4, 7, 12, 30), type: 'buy',    sym: 'YDEX', qty: 2,   price: 4368.50, status: 'partial',   currency: 'RUB', filledQty: 1 },
  { id: 'op-2021', t: Date.UTC(2026, 4, 6, 14, 15), type: 'buy',    sym: 'SBER', qty: 60,  price: 309.80,  status: 'filled',    currency: 'RUB' },
  { id: 'op-2020', t: Date.UTC(2026, 4, 6, 10, 2),  type: 'cancel', sym: 'TSLA', qty: 5,   price: 250.00,  status: 'cancelled', currency: 'USD' },
  { id: 'op-2019', t: Date.UTC(2026, 4, 5, 18, 22), type: 'sell',   sym: 'LKOH', qty: 1,   price: 7615.00, status: 'filled',    currency: 'RUB' },
  { id: 'op-2018', t: Date.UTC(2026, 4, 5, 11, 50), type: 'buy',    sym: 'AAPL', qty: 6,   price: 215.30,  status: 'filled',    currency: 'USD' },
  { id: 'op-2017', t: Date.UTC(2026, 4, 4, 15, 10), type: 'buy',    sym: 'T',    qty: 5,   price: 3201.00, status: 'filled',    currency: 'RUB' },
  { id: 'op-2016', t: Date.UTC(2026, 4, 3, 17, 35), type: 'reject', sym: 'NVDA', qty: 50,  price: 138.92,  status: 'rejected',  currency: 'USD', reason: 'Недостаточно средств' },
];

export type ActiveOrder = {
  id: string;
  sym: string;
  side: 'buy' | 'sell';
  qty: number;
  limit: number;
  filled: number;
  placedAt: number;
};

export const ACTIVE_ORDERS: ActiveOrder[] = [
  { id: 'lo-501', sym: 'SBER', side: 'buy',  qty: 100, limit: 308.00, filled: 0, placedAt: Date.UTC(2026, 4, 8, 9, 0) },
  { id: 'lo-502', sym: 'GMKN', side: 'sell', qty: 50,  limit: 135.00, filled: 0, placedAt: Date.UTC(2026, 4, 7, 14, 30) },
];

export function findTicker(sym: string): Ticker {
  const t = TICKERS.find((x) => x.sym === sym);
  if (!t) {
    return {
      sym,
      market: 'MOEX',
      nameRu: sym,
      nameEn: sym,
      sector: 'Неизвестно',
      sectorEn: 'Unknown',
      base: 100,
      vol: 0.02,
      drift: 0,
      ccy: 'RUB',
    };
  }
  return t;
}

export type UserProfile = {
  initials: string;
  nameRu: string;
  nameEn: string;
  email: string;
  memberSinceRu: string;
  memberSinceEn: string;
  ordersCount: string;
  streak: string;
};

export type MarketIndex = { label: string; value: string };
