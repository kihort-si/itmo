import { apiRequest } from './client';
import { API_PREFIX } from '../config/api';
import type { PortfolioResponse, UserMe } from './types';

export function getMeApi() {
  return apiRequest<UserMe>(`${API_PREFIX}/users/me`);
}

export function updateBalanceApi(balance: number) {
  return apiRequest<UserMe>(`${API_PREFIX}/users/me/balance`, {
    method: 'PUT',
    body: { balance },
  });
}

export function getPortfolioApi() {
  return apiRequest<PortfolioResponse>(`${API_PREFIX}/users/me/portfolio`);
}
