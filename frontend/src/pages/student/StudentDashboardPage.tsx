import { useEffect, useState } from 'react';
import { getStudentDashboard, getStudentProfile } from '../../api/student';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { StudentDashboardResponse, StudentResponse } from '../../types';

export function StudentDashboardPage() {
  const [dashboard, setDashboard] = useState<StudentDashboardResponse | null>(null);
  const [profile, setProfile] = useState<StudentResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getStudentDashboard(), getStudentProfile()])
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
        title={`Welcome, ${profile?.name ?? 'Student'}`}
        subtitle="Your courses, progress, and latest updates"
      />

      {dashboard?.latestNotification && (
        <div className="card highlight-card">
          <h3>Latest notification</h3>
          <strong>{dashboard.latestNotification.title}</strong>
          <p>{dashboard.latestNotification.message}</p>
        </div>
      )}

      <div className="dashboard-grid">
        <section className="card">
          <h3>My courses</h3>
          {dashboard?.courses.length ? (
            <ul className="simple-list">
              {dashboard.courses.map((c) => (
                <li key={c.id}>
                  <span>{c.title}</span>
                  <small>{c.instructor ?? 'TBA'} · {c.credits ?? '—'} credits</small>
                </li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">No courses assigned yet.</p>
          )}
        </section>

        <section className="card">
          <h3>Progress</h3>
          {dashboard?.progress.length ? (
            <ul className="progress-list">
              {dashboard.progress.map((p) => (
                <li key={p.courseId}>
                  <div className="progress-row">
                    <span>{p.courseTitle}</span>
                    <strong>{p.progressPercent}%</strong>
                  </div>
                  <div className="progress-bar">
                    <div className="progress-fill" style={{ width: `${p.progressPercent}%` }} />
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">No progress data yet.</p>
          )}
        </section>

        <section className="card">
          <h3>Profile</h3>
          {profile && (
            <dl className="detail-list">
              <dt>Email</dt><dd>{profile.email}</dd>
              <dt>Roll number</dt><dd>{profile.rollNumber ?? '—'}</dd>
              <dt>Class</dt><dd>{profile.className ?? '—'}</dd>
              <dt>Department</dt><dd>{profile.department ?? '—'}</dd>
            </dl>
          )}
        </section>
      </div>
    </div>
  );
}
