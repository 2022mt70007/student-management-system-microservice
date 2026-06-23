import { apiClient } from './client';
import type {
  AcademicClassRequest,
  AcademicClassResponse,
  ApiResponse,
  DepartmentRequest,
  DepartmentResponse,
  SubjectRequest,
  SubjectResponse,
} from '../types';

export async function listDepartments(activeOnly = false) {
  const { data } = await apiClient.get<ApiResponse<DepartmentResponse[]>>(
    '/api/admin/academic/departments',
    { params: activeOnly ? { activeOnly: true } : {} },
  );
  return data.data;
}

export async function createDepartment(body: DepartmentRequest) {
  const { data } = await apiClient.post<ApiResponse<DepartmentResponse>>(
    '/api/admin/academic/departments',
    body,
  );
  return data.data;
}

export async function updateDepartment(id: number, body: DepartmentRequest) {
  const { data } = await apiClient.put<ApiResponse<DepartmentResponse>>(
    `/api/admin/academic/departments/${id}`,
    body,
  );
  return data.data;
}

export async function deleteDepartment(id: number) {
  await apiClient.delete(`/api/admin/academic/departments/${id}`);
}

export async function listClasses(departmentId?: number) {
  const { data } = await apiClient.get<ApiResponse<AcademicClassResponse[]>>(
    '/api/admin/academic/classes',
    { params: departmentId ? { departmentId } : {} },
  );
  return data.data;
}

export async function createClass(body: AcademicClassRequest) {
  const { data } = await apiClient.post<ApiResponse<AcademicClassResponse>>(
    '/api/admin/academic/classes',
    body,
  );
  return data.data;
}

export async function updateClass(id: number, body: AcademicClassRequest) {
  const { data } = await apiClient.put<ApiResponse<AcademicClassResponse>>(
    `/api/admin/academic/classes/${id}`,
    body,
  );
  return data.data;
}

export async function deleteClass(id: number) {
  await apiClient.delete(`/api/admin/academic/classes/${id}`);
}

export async function listSubjects(classId?: number) {
  const { data } = await apiClient.get<ApiResponse<SubjectResponse[]>>(
    '/api/admin/academic/subjects',
    { params: classId ? { classId } : {} },
  );
  return data.data;
}

export async function createSubject(body: SubjectRequest) {
  const { data } = await apiClient.post<ApiResponse<SubjectResponse>>(
    '/api/admin/academic/subjects',
    body,
  );
  return data.data;
}

export async function updateSubject(id: number, body: SubjectRequest) {
  const { data } = await apiClient.put<ApiResponse<SubjectResponse>>(
    `/api/admin/academic/subjects/${id}`,
    body,
  );
  return data.data;
}

export async function deleteSubject(id: number) {
  await apiClient.delete(`/api/admin/academic/subjects/${id}`);
}
