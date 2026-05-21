import { apiRequest } from './client';
import { API_PREFIX } from '../config/api';
import type {
  MarketChartRequest,
  MarketChartResponse,
  MarketStocksRequest,
  MarketStocksResponse,
} from './types';

function withQuery(path: string, params: Record<string, string | number | undefined>) {
  const qs = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    qs.set(key, String(value));
  });
  const raw = qs.toString();
  return raw ? `${path}?${raw}` : path;
}

export function getMarketStocksApi(params: MarketStocksRequest = {}) {
  return apiRequest<MarketStocksResponse>(
    withQuery(`${API_PREFIX}/market/stocks`, params)
  );
}

export function getMarketChartApi(ticker: string, params: MarketChartRequest) {
  return apiRequest<MarketChartResponse>(
    withQuery(`${API_PREFIX}/market/stocks/${encodeURIComponent(ticker)}/chart`, params)
  );
}
