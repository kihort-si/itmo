import { apiRequest } from './client';
import { API_PREFIX } from '../config/api';
import type { TradeRequest, TradeResponse } from './types';

export function buyTradeApi(body: TradeRequest) {
  return apiRequest<TradeResponse>(`${API_PREFIX}/trades/buy`, { method: 'POST', body });
}

export function sellTradeApi(body: TradeRequest) {
  return apiRequest<TradeResponse>(`${API_PREFIX}/trades/sell`, { method: 'POST', body });
}
