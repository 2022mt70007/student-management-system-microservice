import { apiClient } from './client';
import type {
  ApiResponse,
  StudentDashboardResponse,
  StudentResponse,
} from '../types';

export async function getStudentDashboard() {
  const { data } = await apiClient.get<ApiResponse<StudentDashboardResponse>>(
    '/api/students/dashboard',
  );
  return data.data;
}

export async function getStudentProfile() {
  const { data } = await apiClient.get<ApiResponse<StudentResponse>>(
    '/api/students/me',
  );
  return data.data;
}
