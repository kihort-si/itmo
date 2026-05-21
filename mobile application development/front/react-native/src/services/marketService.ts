import { isNetworkOrMockError } from '../api/client';
import { getMarketChartApi, getMarketStocksApi } from '../api/marketApi';
import type {
  MarketChartRequest,
  MarketChartResponse,
  MarketStockRow,
  MarketStocksRequest,
} from '../api/types';
import { brokerData, type Tf, type Ticker } from '../data/repository';
import type { Candle } from '../data/market';
import { useDevStore } from '../stores/devStore';

export type UiPeriod = '10min' | '1h' | '6h' | '1d' | '10d' | '1m';
export type UiChartKind = 'line' | 'candles';

const periodMap: Record<UiPeriod, MarketChartRequest['period']> = {
  '10min': '10M',
  '1h': '1H',
  '6h': '6H',
  '1d': '1D',
  '10d': '10D',
  '1m': '1M',
};

const timeframeMap: Record<UiPeriod, Tf> = {
  '10min': '5m',
  '1h': '5m',
  '6h': '30m',
  '1d': '5m',
  '10d': '1d',
  '1m': '1d',
};

const marketTimeframeMap: Record<UiPeriod, MarketChartRequest['timeframe']> = {
  '10min': 'M1',
  '1h': 'M5',
  '6h': 'M30',
  '1d': 'M5',
  '10d': 'D1',
  '1m': 'D1',
};

const mockCountMap: Record<UiPeriod, number> = {
  '10min': 10,
  '1h': 12,
  '6h': 12,
  '1d': 78,
  '10d': 10,
  '1m': 90,
};

function toLineCandle(ts: number, close: number): Candle {
  return { t: ts, o: close, h: close, l: close, c: close, v: 0 };
}

export function getFallbackCandles(ticker: Ticker, period: UiPeriod): Candle[] {
  return brokerData.genCandles(ticker, timeframeMap[period], mockCountMap[period]);
}

export function getFallbackSparkline(ticker: Ticker): number[] {
  return brokerData.dayStats(ticker).spark;
}

export function getFallbackPortfolioSeries(tickers: Array<{ ticker: Ticker; quantity: number }>, cashRub: number) {
  const length = 60;
  const totals = new Array(length).fill(0);
  tickers.forEach(({ ticker, quantity }) => {
    const candles = brokerData.genCandles(ticker, '1d', length);
    candles.forEach((bar, index) => {
      totals[index] += bar.c * quantity * (ticker.ccy === 'USD' ? brokerData.fx.USD_RUB : 1);
    });
  });
  return totals.map((value) => value + cashRub);
}

export function toMarketChartRequest(period: UiPeriod, kind: UiChartKind): MarketChartRequest {
  return {
    chartType: kind === 'line' ? 'LINE' : 'CANDLE',
    timeframe: marketTimeframeMap[period],
    period: periodMap[period],
  };
}

export function adaptMarketChartToCandles(chart: MarketChartResponse): Candle[] {
  if (chart.chartType === 'CANDLE') {
    const candles = Array.isArray(chart.candles) ? chart.candles : [];
    return candles.map((candle) => ({
      t: candle.ts,
      o: candle.open,
      h: candle.high,
      l: candle.low,
      c: candle.close,
      v: candle.volume,
    }));
  }
  const points = Array.isArray(chart.points) ? chart.points : [];
  return points.map((point) => toLineCandle(point.ts, point.close));
}

export function adaptMarketChartToSpark(chart: MarketChartResponse): number[] {
  if (chart.chartType === 'CANDLE') {
    const candles = Array.isArray(chart.candles) ? chart.candles : [];
    return candles.map((candle) => candle.close);
  }
  const points = Array.isArray(chart.points) ? chart.points : [];
  return points.map((point) => point.close);
}

export async function getMarketStocks(params: MarketStocksRequest = {}): Promise<MarketStockRow[] | null> {
  if (useDevStore.getState().mockMarketData) {
    return null;
  }
  try {
    const response = await getMarketStocksApi(params);
    return response.stocks;
  } catch (e) {
    if (!isNetworkOrMockError(e)) throw e;
    return null;
  }
}

export async function getChartCandles(
  ticker: Ticker,
  period: UiPeriod,
  kind: UiChartKind
): Promise<{ candles: Candle[]; source: 'backend' | 'fallback' }> {
  if (useDevStore.getState().mockMarketData) {
    return { candles: getFallbackCandles(ticker, period), source: 'fallback' };
  }
  try {
    const response = await getMarketChartApi(ticker.sym, toMarketChartRequest(period, kind));
    const candles = adaptMarketChartToCandles(response);
    return { candles, source: 'backend' };
  } catch {
    return { candles: [], source: 'backend' };
  }
}

export async function getSparkline(
  ticker: Ticker,
  period: UiPeriod = '1m'
): Promise<{ spark: number[]; source: 'backend' | 'fallback' }> {
  if (useDevStore.getState().mockMarketData) {
    return { spark: getFallbackSparkline(ticker), source: 'fallback' };
  }
  try {
    const response = await getMarketChartApi(ticker.sym, toMarketChartRequest(period, 'line'));
    const spark = adaptMarketChartToSpark(response);
    return { spark, source: 'backend' };
  } catch {
    return { spark: [], source: 'backend' };
  }
}
