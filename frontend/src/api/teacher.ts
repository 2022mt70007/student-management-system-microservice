import { apiClient } from './client';
import type {
  ApiResponse,
  TeacherDashboardResponse,
  TeacherResponse,
} from '../types';

export async function getTeacherDashboard() {
  const { data } = await apiClient.get<ApiResponse<TeacherDashboardResponse>>(
    '/api/teachers/dashboard',
  );
  return data.data;
}

export async function getTeacherProfile() {
  const { data } = await apiClient.get<ApiResponse<TeacherResponse>>(
    '/api/teachers/me',
  );
  return data.data;
}
