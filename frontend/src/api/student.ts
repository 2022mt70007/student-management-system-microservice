import { apiClient } from './client';
import type {
  ApiResponse,
  AssignmentResponse,
  ExamResponse,
  StudentDashboardResponse,
  StudentEnrolledCourseResponse,
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

export async function getStudentCourses() {
  const { data } = await apiClient.get<ApiResponse<StudentEnrolledCourseResponse[]>>(
    '/api/students/courses',
  );
  return data.data;
}

export async function getCourseAssignments(courseId: number) {
  const { data } = await apiClient.get<ApiResponse<AssignmentResponse[]>>(
    `/api/students/courses/${courseId}/assignments`,
  );
  return data.data;
}

export async function getCourseExams(courseId: number) {
  const { data } = await apiClient.get<ApiResponse<ExamResponse[]>>(
    `/api/students/courses/${courseId}/exams`,
  );
  return data.data;
}
