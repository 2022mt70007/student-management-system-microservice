import { useEffect, useState } from 'react';
import { getDashboard } from '../../api/admin';
import { Alert, LoadingSpinner, PageHeader, StatusBadge } from '../../components/ui';
import type { AdminDashboardResponse } from '../../types';

export function AdminDashboardPage() {
  const [data, setData] = useState<AdminDashboardResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboard()
      .then(setData)
      .catch((err: unknown) => setError(err instanceof Error ? err.message : 'Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <Alert type="error" message={error} />;
  if (!data) return null;

  const stats = [
    { label: 'Students', count: data.students.length, color: '#2563eb' },
    { label: 'Teachers', count: data.teachers.length, color: '#7c3aed' },
    { label: 'Admins', count: data.admins.length, color: '#0891b2' },
    { label: 'Subjects', count: data.subjects.length, color: '#059669' },
    { label: 'Notifications', count: data.notifications.length, color: '#d97706' },
  ];

  return (
    <div>
      <PageHeader
        title="Admin Dashboard"
        subtitle="Overview of the student management system"
      />
      <div className="stats-grid">
        {stats.map((stat) => (
          <div key={stat.label} className="stat-card" style={{ borderTopColor: stat.color }}>
            <span className="stat-label">{stat.label}</span>
            <strong className="stat-value">{stat.count}</strong>
          </div>
        ))}
      </div>

      <div className="dashboard-grid">
        <section className="card">
          <h3>Recent students</h3>
          <ul className="simple-list">
            {data.students.slice(0, 5).map((s) => (
              <li key={s.id}>
                <span>{s.name}</span>
                <StatusBadge status={s.status} />
              </li>
            ))}
          </ul>
        </section>
        <section className="card">
          <h3>Recent notifications</h3>
          <ul className="simple-list">
            {data.notifications.slice(0, 5).map((n) => (
              <li key={n.id}>
                <span>{n.title}</span>
                <small>{n.targetRole ?? 'ALL'}</small>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </div>
  );
}
