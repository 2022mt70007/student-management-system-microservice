import { useEffect, useState } from 'react';
import { getStudentDashboard, getStudentProfile } from '../../api/student';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { StudentDashboardResponse, StudentResponse, SubjectResponse } from '../../types';

type DashboardTab = 'overview' | 'subjects';

function SubjectCard({ subject }: { subject: SubjectResponse }) {
  return (
    <article className="subject-card">
      <div className="subject-card-top">
        <span className="subject-code">{subject.subjectCode}</span>
        {subject.credits != null && (
          <span className="subject-credits">{subject.credits} credits</span>
        )}
      </div>
      <h3 className="subject-name">{subject.subjectName}</h3>
      {subject.description && (
        <p className="subject-description">{subject.description}</p>
      )}
      <dl className="subject-meta">
        {subject.className && (
          <>
            <dt>Class</dt>
            <dd>{subject.className}</dd>
          </>
        )}
        {subject.departmentName && (
          <>
            <dt>Department</dt>
            <dd>{subject.departmentName}</dd>
          </>
        )}
      </dl>
    </article>
  );
}

export function StudentDashboardPage() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('overview');
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
  if (error && !dashboard) return <Alert type="error" message={error} />;

  const subjects = dashboard?.subjects ?? [];

  return (
    <div>
      <PageHeader
        title={`Welcome, ${profile?.name ?? 'Student'}`}
        subtitle="Your class, department, and assigned subjects"
      />

      {error && <Alert type="error" message={error} />}

      <div className="stats-grid student-stats">
        <div className="stat-card">
          <span className="stat-label">Subjects</span>
          <strong className="stat-value">{dashboard?.subjectCount ?? 0}</strong>
        </div>
        <div className="stat-card">
          <span className="stat-label">Class</span>
          <strong className="stat-value stat-value-text">
            {dashboard?.className ?? profile?.className ?? '—'}
          </strong>
        </div>
        <div className="stat-card">
          <span className="stat-label">Department</span>
          <strong className="stat-value stat-value-text">
            {dashboard?.departmentName ?? profile?.departmentName ?? '—'}
          </strong>
        </div>
        <div className="stat-card">
          <span className="stat-label">Roll no.</span>
          <strong className="stat-value stat-value-text">
            {dashboard?.rollNumber ?? profile?.rollNumber ?? '—'}
          </strong>
        </div>
      </div>

      <div className="tab-bar" role="tablist" aria-label="Student dashboard sections">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'overview'}
          className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
          onClick={() => setActiveTab('overview')}
        >
          Overview
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'subjects'}
          className={`tab-btn ${activeTab === 'subjects' ? 'active' : ''}`}
          onClick={() => setActiveTab('subjects')}
        >
          My Subjects
          {subjects.length > 0 && (
            <span className="tab-count">{subjects.length}</span>
          )}
        </button>
      </div>

      {activeTab === 'overview' && (
        <div role="tabpanel" className="student-overview">
          {dashboard?.latestNotification && (
            <div className="card highlight-card">
              <h3>Latest notification</h3>
              <strong>{dashboard.latestNotification.title}</strong>
              <p>{dashboard.latestNotification.message}</p>
            </div>
          )}

          <div className="dashboard-grid">
            <section className="card">
              <div className="card-header-row">
                <h3>Your subjects</h3>
                {subjects.length > 0 && (
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => setActiveTab('subjects')}
                  >
                    View all
                  </button>
                )}
              </div>
              {subjects.length ? (
                <ul className="subject-chip-list">
                  {subjects.map((s) => (
                    <li key={s.id} className="subject-chip">
                      <span className="subject-chip-code">{s.subjectCode}</span>
                      <span>{s.subjectName}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="empty-state">
                  No subjects assigned yet. Contact your administrator.
                </p>
              )}
            </section>

            <section className="card">
              <h3>Profile</h3>
              {profile && (
                <dl className="detail-list">
                  <dt>Email</dt>
                  <dd>{profile.email}</dd>
                  <dt>Phone</dt>
                  <dd>{profile.phone ?? '—'}</dd>
                  <dt>Class</dt>
                  <dd>{profile.className ?? '—'}</dd>
                  <dt>Department</dt>
                  <dd>{profile.departmentName ?? '—'}</dd>
                  <dt>Roll number</dt>
                  <dd>{profile.rollNumber ?? '—'}</dd>
                </dl>
              )}
            </section>
          </div>
        </div>
      )}

      {activeTab === 'subjects' && (
        <div role="tabpanel">
          {subjects.length ? (
            <div className="subject-grid">
              {subjects.map((subject) => (
                <SubjectCard key={subject.id} subject={subject} />
              ))}
            </div>
          ) : (
            <div className="card empty-card">
              <h3>No subjects assigned</h3>
              <p className="empty-state">
                Your administrator has not assigned any subjects to you yet.
                Once subjects are added to your profile, they will appear here.
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
