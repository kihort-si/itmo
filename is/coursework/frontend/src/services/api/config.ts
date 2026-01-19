import axios, {AxiosError, type AxiosInstance} from 'axios';

export const GATEWAY_BASE_URL = '/gateway';

const API_BASE_URL = `${GATEWAY_BASE_URL}/resource`;

export const LOGIN_URL = `${GATEWAY_BASE_URL}/oauth2/authorization/gateway`;

export const gatewayClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

let redirectingToLogin = false;

let skipRedirect = false;

export function setSkipRedirect(value: boolean) {
  skipRedirect = value;
}

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 && !skipRedirect && !redirectingToLogin) {
      const path = window.location.pathname;
      const publicPaths = ['/', '/catalog', '/about', '/discounts', '/legal', '/auth'];
      const isPublicPath = publicPaths.some(publicPath => path === publicPath || path.startsWith(publicPath + '/'));

      if (!isPublicPath) {
        redirectingToLogin = true;
        const returnTo = window.location.href;
        window.location.href = `${LOGIN_URL}?returnTo=${encodeURIComponent(returnTo)}`;
      }
    }
    return Promise.reject(error);
  }
);

export interface ApiError {
  code?: string;
  message?: string;
  details?: unknown;
  title?: string;
  status?: number;
  detail?: string;
}

export function extractApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data;
    if (responseData && typeof responseData === 'object') {
      return {
        ...(responseData as ApiError),
        status: error.response?.status,
        message: (responseData as ApiError).message || (responseData as ApiError).detail || error.message || 'Произошла ошибка при выполнении запроса',
      };
    }
    return {
      message: error.message || 'Произошла ошибка при выполнении запроса',
      status: error.response?.status,
    };
  }
  if (error instanceof Error) {
    return {
      message: error.message || 'Произошла ошибка',
    };
  }
  return {
    message: 'Неизвестная ошибка',
  };
}

