import axios from 'axios';
import type { AuthUser } from '../types';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

export const apiClient = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

export function setAuthToken(token: string | null) {
  if (token) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common.Authorization;
  }
}

export function loadStoredAuth(): AuthUser | null {
  const raw = localStorage.getItem('sms_auth');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function saveAuth(user: AuthUser) {
  localStorage.setItem('sms_auth', JSON.stringify(user));
  setAuthToken(user.token);
}

export function clearAuth() {
  localStorage.removeItem('sms_auth');
  setAuthToken(null);
}

const stored = loadStoredAuth();
if (stored?.token) {
  setAuthToken(stored.token);
}

apiClient.interceptors.request.use((config) => {
  const auth = loadStoredAuth();
  if (auth?.profileId != null) {
    config.headers['X-Profile-Id'] = String(auth.profileId);
    config.headers['X-User-Email'] = auth.email;
    config.headers['X-User-Role'] = auth.role;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      'Request failed';
    return Promise.reject(new Error(message));
  },
);
