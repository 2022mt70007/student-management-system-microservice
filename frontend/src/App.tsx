import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ProtectedRoute, PublicOnlyRoute } from './components/ProtectedRoute';
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminsPage } from './pages/admin/AdminsPage';
import { CoursesPage } from './pages/admin/CoursesPage';
import { NotificationsPage } from './pages/admin/NotificationsPage';
import { StudentsPage } from './pages/admin/StudentsPage';
import { TeachersPage } from './pages/admin/TeachersPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { StudentDashboardPage } from './pages/student/StudentDashboardPage';
import { TeacherDashboardPage } from './pages/teacher/TeacherDashboardPage';

const adminNav = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/students', label: 'Students' },
  { to: '/admin/teachers', label: 'Teachers' },
  { to: '/admin/admins', label: 'Admins' },
  { to: '/admin/courses', label: 'Courses' },
  { to: '/admin/notifications', label: 'Notifications' },
];

export default function App() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
        <Route element={<Layout title="Admin Portal" navItems={adminNav} />}>
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/students" element={<StudentsPage />} />
          <Route path="/admin/teachers" element={<TeachersPage />} />
          <Route path="/admin/admins" element={<AdminsPage />} />
          <Route path="/admin/courses" element={<CoursesPage />} />
          <Route path="/admin/notifications" element={<NotificationsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
        <Route element={<Layout title="Student Portal" navItems={[{ to: '/student', label: 'Dashboard' }]} />}>
          <Route path="/student" element={<StudentDashboardPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['TEACHER']} />}>
        <Route element={<Layout title="Teacher Portal" navItems={[{ to: '/teacher', label: 'Dashboard' }]} />}>
          <Route path="/teacher" element={<TeacherDashboardPage />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
