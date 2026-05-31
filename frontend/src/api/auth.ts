import { apiClient } from './client';
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  SetPasswordRequest,
  ValidateCodeRequest,
} from '../types';

export async function login(request: LoginRequest) {
  const { data } = await apiClient.post<ApiResponse<LoginResponse>>(
    '/api/auth/login',
    request,
  );
  return data;
}

export async function validateCode(request: ValidateCodeRequest) {
  const { data } = await apiClient.post<ApiResponse<boolean>>(
    '/api/auth/register/validate-code',
    request,
  );
  return data;
}

export async function setPassword(request: SetPasswordRequest) {
  const { data } = await apiClient.post<ApiResponse<LoginResponse>>(
    '/api/auth/register/set-password',
    request,
  );
  return data;
}
