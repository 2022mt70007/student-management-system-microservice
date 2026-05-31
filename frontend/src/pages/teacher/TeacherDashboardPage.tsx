import { useEffect, useState } from 'react';
import { getTeacherDashboard, getTeacherProfile } from '../../api/teacher';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { TeacherDashboardResponse, TeacherResponse } from '../../types';

export function TeacherDashboardPage() {
  const [dashboard, setDashboard] = useState<TeacherDashboardResponse | null>(null);
  const [profile, setProfile] = useState<TeacherResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getTeacherDashboard(), getTeacherProfile()])
      .then(([dash, prof]) => {
        setDashboard(dash);
        setProfile(prof);
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <Alert type="error" message={error} />;

  return (
    <div>
      <PageHeader
        title={`Welcome, ${profile?.name ?? 'Teacher'}`}
        subtitle="Your courses, students, and notifications"
      />

      <div className="dashboard-grid">
        <section className="card">
          <h3>My courses</h3>
          {dashboard?.courses.length ? (
            <ul className="simple-list">
              {dashboard.courses.map((c) => (
                <li key={c.id}>
                  <span>{c.title}</span>
                  <small>{c.department ?? '—'}</small>
                </li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">No courses yet.</p>
          )}
        </section>

        <section className="card">
          <h3>Students</h3>
          {dashboard?.students.length ? (
            <ul className="simple-list">
              {dashboard.students.map((s) => (
                <li key={s.id}>
                  <span>{s.name}</span>
                  <small>{s.email}</small>
                </li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">No students listed.</p>
          )}
        </section>

        <section className="card">
          <h3>Notifications</h3>
          {dashboard?.notifications.length ? (
            <ul className="simple-list">
              {dashboard.notifications.map((n) => (
                <li key={n.id}>
                  <span>{n.title}</span>
                  <small>{n.message}</small>
                </li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">No notifications.</p>
          )}
        </section>

        <section className="card">
          <h3>Profile</h3>
          {profile && (
            <dl className="detail-list">
              <dt>Teacher ID</dt><dd>{profile.teacherId}</dd>
              <dt>Email</dt><dd>{profile.email}</dd>
              <dt>Department</dt><dd>{profile.department ?? '—'}</dd>
              <dt>Subjects</dt><dd>{profile.subjects?.join(', ') || '—'}</dd>
            </dl>
          )}
        </section>
      </div>
    </div>
  );
}
