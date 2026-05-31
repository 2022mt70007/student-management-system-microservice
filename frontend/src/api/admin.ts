import { apiClient } from './client';
import type {
  AdminDashboardResponse,
  AdminUserRequest,
  AdminUserResponse,
  ApiResponse,
  CourseRequest,
  CourseResponse,
  NotificationRequest,
  NotificationResponse,
  StudentRequest,
  StudentResponse,
  TeacherRequest,
  TeacherResponse,
} from '../types';

export async function getDashboard() {
  const { data } = await apiClient.get<ApiResponse<AdminDashboardResponse>>(
    '/api/admin/dashboard',
  );
  return data.data;
}

export async function listStudents() {
  const { data } = await apiClient.get<ApiResponse<StudentResponse[]>>(
    '/api/admin/students',
  );
  return data.data;
}

export async function createStudent(body: StudentRequest) {
  const { data } = await apiClient.post<ApiResponse<StudentResponse>>(
    '/api/admin/students',
    body,
  );
  return data;
}

export async function updateStudent(id: number, body: StudentRequest) {
  const { data } = await apiClient.put<ApiResponse<StudentResponse>>(
    `/api/admin/students/${id}`,
    body,
  );
  return data;
}

export async function deleteStudent(id: number) {
  await apiClient.delete(`/api/admin/students/${id}`);
}

export async function listTeachers() {
  const { data } = await apiClient.get<ApiResponse<TeacherResponse[]>>(
    '/api/admin/teachers',
  );
  return data.data;
}

export async function createTeacher(body: TeacherRequest) {
  const { data } = await apiClient.post<ApiResponse<TeacherResponse>>(
    '/api/admin/teachers',
    body,
  );
  return data;
}

export async function updateTeacher(id: number, body: TeacherRequest) {
  const { data } = await apiClient.put<ApiResponse<TeacherResponse>>(
    `/api/admin/teachers/${id}`,
    body,
  );
  return data;
}

export async function deleteTeacher(id: number) {
  await apiClient.delete(`/api/admin/teachers/${id}`);
}

export async function listAdmins() {
  const { data } = await apiClient.get<ApiResponse<AdminUserResponse[]>>(
    '/api/admin/admins',
  );
  return data.data;
}

export async function createAdmin(body: AdminUserRequest) {
  const { data } = await apiClient.post<ApiResponse<AdminUserResponse>>(
    '/api/admin/admins',
    body,
  );
  return data;
}

export async function updateAdmin(id: number, body: AdminUserRequest) {
  const { data } = await apiClient.put<ApiResponse<AdminUserResponse>>(
    `/api/admin/admins/${id}`,
    body,
  );
  return data;
}

export async function deleteAdmin(id: number) {
  await apiClient.delete(`/api/admin/admins/${id}`);
}

export async function listCourses() {
  const { data } = await apiClient.get<ApiResponse<CourseResponse[]>>(
    '/api/admin/courses',
  );
  return data.data;
}

export async function createCourse(body: CourseRequest) {
  const { data } = await apiClient.post<ApiResponse<CourseResponse>>(
    '/api/admin/courses',
    body,
  );
  return data;
}

export async function updateCourse(id: number, body: CourseRequest) {
  const { data } = await apiClient.put<ApiResponse<CourseResponse>>(
    `/api/admin/courses/${id}`,
    body,
  );
  return data;
}

export async function deleteCourse(id: number) {
  await apiClient.delete(`/api/admin/courses/${id}`);
}

export async function listNotifications() {
  const { data } = await apiClient.get<ApiResponse<NotificationResponse[]>>(
    '/api/admin/notifications',
  );
  return data.data;
}

export async function createNotification(body: NotificationRequest) {
  const { data } = await apiClient.post<ApiResponse<NotificationResponse>>(
    '/api/admin/notifications',
    body,
  );
  return data;
}

export async function updateNotification(id: number, body: NotificationRequest) {
  const { data } = await apiClient.put<ApiResponse<NotificationResponse>>(
    `/api/admin/notifications/${id}`,
    body,
  );
  return data;
}

export async function deleteNotification(id: number) {
  await apiClient.delete(`/api/admin/notifications/${id}`);
}
