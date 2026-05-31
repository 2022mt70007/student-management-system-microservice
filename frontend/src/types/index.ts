export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT';

export type RegistrationStatus =
  | 'PENDING_REGISTRATION'
  | 'ACTIVE'
  | 'INACTIVE';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
  profileId: number;
}

export interface ValidateCodeRequest {
  email: string;
  code: string;
}

export interface SetPasswordRequest {
  email: string;
  code: string;
  password: string;
}

export interface StudentRequest {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  rollNumber?: string;
  className?: string;
  department?: string;
  subjects?: string[];
}

export interface TeacherRequest {
  name: string;
  teacherId: string;
  department?: string;
  email: string;
  phone?: string;
  address?: string;
  subjects?: string[];
}

export interface AdminUserRequest {
  name: string;
  adminId: string;
  department?: string;
  email: string;
  phone?: string;
  address?: string;
}

export interface CourseRequest {
  title: string;
  description?: string;
  department?: string;
  instructor?: string;
  credits?: number;
}

export interface NotificationRequest {
  title: string;
  message: string;
  targetRole?: string;
}

export interface StudentResponse {
  id: number;
  name: string;
  email: string;
  phone?: string;
  address?: string;
  rollNumber?: string;
  className?: string;
  department?: string;
  subjects?: string[];
  status: RegistrationStatus;
}

export interface TeacherResponse {
  id: number;
  name: string;
  teacherId: string;
  department?: string;
  email: string;
  phone?: string;
  address?: string;
  subjects?: string[];
  status: RegistrationStatus;
}

export interface AdminUserResponse {
  id: number;
  name: string;
  adminId: string;
  department?: string;
  email: string;
  phone?: string;
  address?: string;
  status: RegistrationStatus;
}

export interface CourseResponse {
  id: number;
  title: string;
  description?: string;
  department?: string;
  instructor?: string;
  credits?: number;
}

export interface NotificationResponse {
  id: number;
  title: string;
  message: string;
  targetRole?: string;
  createdAt?: string;
}

export interface StudentProgressResponse {
  courseId: number;
  courseTitle: string;
  progressPercent: number;
}

export interface StudentDashboardResponse {
  courses: CourseResponse[];
  progress: StudentProgressResponse[];
  latestNotification?: NotificationResponse;
}

export interface TeacherDashboardResponse {
  courses: CourseResponse[];
  notifications: NotificationResponse[];
  students: StudentResponse[];
}

export interface AdminDashboardResponse {
  students: StudentResponse[];
  teachers: TeacherResponse[];
  admins: AdminUserResponse[];
  courses: CourseResponse[];
  notifications: NotificationResponse[];
}

export interface AuthUser {
  token: string;
  email: string;
  role: UserRole;
  profileId: number;
}
