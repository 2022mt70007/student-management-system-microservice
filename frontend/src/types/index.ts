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

export type AcademicStatus = 'ACTIVE' | 'INACTIVE';

export interface DepartmentRequest {
  departmentCode: string;
  departmentName: string;
  description?: string;
  status: AcademicStatus;
}

export interface DepartmentResponse {
  id: number;
  departmentCode: string;
  departmentName: string;
  description?: string;
  status: AcademicStatus;
}

export interface AcademicClassRequest {
  classCode: string;
  className: string;
  departmentId: number;
  description?: string;
}

export interface AcademicClassResponse {
  id: number;
  classCode: string;
  className: string;
  departmentId: number;
  departmentName?: string;
  description?: string;
}

export interface SubjectRequest {
  subjectCode: string;
  subjectName: string;
  classId: number;
  credits?: number;
  description?: string;
}

export interface SubjectResponse {
  id: number;
  subjectCode: string;
  subjectName: string;
  classId: number;
  className?: string;
  departmentId?: number;
  departmentName?: string;
  credits?: number;
  description?: string;
}

export interface StudentRequest {
  name: string;
  email: string;
  phone?: string;
  address?: string;
  rollNumber?: string;
  departmentId: number;
  classId: number;
  subjectIds: number[];
}

export interface TeacherRequest {
  name: string;
  teacherId: string;
  email: string;
  phone?: string;
  address?: string;
  departmentId: number;
  classId: number;
  subjectIds: number[];
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
  departmentId?: number;
  departmentName?: string;
  classId?: number;
  className?: string;
  subjectIds?: number[];
  subjectNames?: string[];
  status: RegistrationStatus;
}

export interface TeacherResponse {
  id: number;
  name: string;
  teacherId: string;
  email: string;
  phone?: string;
  address?: string;
  departmentId?: number;
  departmentName?: string;
  classId?: number;
  className?: string;
  subjectIds?: number[];
  subjectNames?: string[];
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

export type AssignmentPriorityStatus = 'OVERDUE' | 'DUE_SOON' | 'UPCOMING';
export type ExamPriorityStatus = 'EXAM_SOON' | 'UPCOMING';

export interface AssignmentResponse {
  id: number;
  courseId: number;
  title: string;
  description?: string;
  dueDate: string;
  priorityStatus: AssignmentPriorityStatus;
}

export interface ExamResponse {
  id: number;
  courseId: number;
  title: string;
  description?: string;
  scheduledDate: string;
  priorityStatus: ExamPriorityStatus;
}

export interface StudentEnrolledCourseResponse {
  id: number;
  title: string;
  description?: string;
  department?: string;
  instructor?: string;
  credits?: number;
  progressPercent: number;
  assignments: AssignmentResponse[];
  exams: ExamResponse[];
}

export interface StudentDashboardResponse {
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
