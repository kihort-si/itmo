import { API_HOST, USE_MOCK_API } from '../config/api';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly body?: unknown
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

type RequestOpts = {
  method?: string;
  body?: unknown;
  jwt?: string | null;
  auth?: boolean;
};

let tokenGetter: () => string | null = () => null;

export function setTokenGetter(getter: () => string | null) {
  tokenGetter = getter;
}

export async function apiRequest<T>(path: string, opts: RequestOpts = {}): Promise<T> {
  if (USE_MOCK_API) {
    throw new ApiError('Mock API mode', 0);
  }

  const headers: Record<string, string> = {
    Accept: 'application/json',
  };

  if (opts.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  const jwt = opts.jwt ?? (opts.auth !== false ? tokenGetter() : null);
  if (jwt) {
    headers.Authorization = `Bearer ${jwt}`;
  }

  const res = await fetch(`${API_HOST}${path}`, {
    method: opts.method ?? (opts.body !== undefined ? 'POST' : 'GET'),
    headers,
    body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
  });

  if (!res.ok) {
    let body: unknown;
    try {
      body = await res.json();
    } catch {
      body = await res.text();
    }
    throw new ApiError(`HTTP ${res.status}`, res.status, body);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return (await res.json()) as T;
}

export function isNetworkOrMockError(e: unknown): boolean {
  if (e instanceof ApiError && e.status === 0) return true;
  if (e instanceof TypeError) return true;
  return false;
}
