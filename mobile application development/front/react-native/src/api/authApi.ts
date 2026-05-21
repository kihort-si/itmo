import { apiRequest } from './client';
import { API_PREFIX } from '../config/api';
import type {
  AuthResponse,
  AvailabilityResponse,
  LoginRequest,
  RegisterRequest,
} from './types';

export function registerApi(body: RegisterRequest) {
  return apiRequest<AuthResponse>(`${API_PREFIX}/auth/register`, {
    method: 'POST',
    body,
    auth: false,
  });
}

export function loginApi(body: LoginRequest) {
  return apiRequest<AuthResponse>(`${API_PREFIX}/auth/login`, {
    method: 'POST',
    body,
    auth: false,
  });
}

export function logoutApi(jwt: string) {
  return apiRequest<void>(`${API_PREFIX}/auth/logout`, { method: 'POST', jwt });
}

export function checkUsernameInUseApi(username: string) {
  return apiRequest<AvailabilityResponse>(`${API_PREFIX}/auth/checkUsernameInUse`, {
    method: 'POST',
    body: { username },
    auth: false,
  });
}

export function checkEmailInUseApi(email: string) {
  return apiRequest<AvailabilityResponse>(`${API_PREFIX}/auth/checkEmailInUse`, {
    method: 'POST',
    body: { email },
    auth: false,
  });
}
