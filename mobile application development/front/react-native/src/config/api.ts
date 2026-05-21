export const API_PREFIX = '/api/broker-app/v1';

export const API_HOST = process.env.EXPO_PUBLIC_API_HOST ?? 'http://127.0.0.1:8060';

export const WS_QUOTES_URL =
  process.env.EXPO_PUBLIC_WS_QUOTES_URL ??
  `${API_HOST.replace(/^http/, 'ws')}${API_PREFIX}/quotes/stream`;

export const USE_MOCK_API =
  process.env.EXPO_PUBLIC_USE_MOCK_API === '1' ||
  process.env.EXPO_PUBLIC_USE_MOCK_API === 'true';

export const WS_RECONNECT_MS = 1500;
