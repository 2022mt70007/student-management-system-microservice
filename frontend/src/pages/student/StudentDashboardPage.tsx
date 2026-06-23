import { useEffect, useState } from 'react';
import {
  getStudentCourses,
  getStudentDashboard,
  getStudentProfile,
} from '../../api/student';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type {
  AssignmentPriorityStatus,
  ExamPriorityStatus,
  StudentDashboardResponse,
  StudentEnrolledCourseResponse,
  StudentResponse,
} from '../../types';

type DashboardTab = 'dashboard' | 'courses';

function formatDate(dateStr: string) {
  return new Date(`${dateStr}T00:00:00`).toLocaleDateString(undefined, {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function assignmentBadge(status: AssignmentPriorityStatus) {
  switch (status) {
    case 'OVERDUE':
      return { label: 'Overdue', className: 'badge badge-danger' };
    case 'DUE_SOON':
      return { label: 'Due soon', className: 'badge badge-warning' };
    default:
      return { label: 'Upcoming', className: 'badge badge-muted' };
  }
}

function examBadge(status: ExamPriorityStatus) {
  switch (status) {
    case 'EXAM_SOON':
      return { label: 'This week', className: 'badge badge-warning' };
    default:
      return { label: 'Upcoming', className: 'badge badge-muted' };
  }
}

function CourseCard({ course }: { course: StudentEnrolledCourseResponse }) {
  return (
    <article className="course-card">
      <header className="course-card-header">
        <div>
          <h3>{course.title}</h3>
          <p className="course-meta">
            Instructor: {course.instructor ?? 'TBA'}
            {course.credits != null && ` · ${course.credits} credits`}
          </p>
        </div>
        <span className="course-progress-pill">{course.progressPercent}% complete</span>
      </header>

      {course.description && <p className="course-description">{course.description}</p>}

      <section className="course-section">
        <h4>Assignments</h4>
        {course.assignments.length ? (
          <ul className="task-list">
            {course.assignments.map((a) => {
              const badge = assignmentBadge(a.priorityStatus);
              return (
                <li key={a.id} className={`task-item task-${a.priorityStatus.toLowerCase()}`}>
                  <div className="task-item-main">
                    <strong>{a.title}</strong>
                    {a.description && <p>{a.description}</p>}
                  </div>
                  <div className="task-item-meta">
                    <span className={badge.className}>{badge.label}</span>
                    <time dateTime={a.dueDate}>Due {formatDate(a.dueDate)}</time>
                  </div>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="empty-state">No assignments scheduled.</p>
        )}
      </section>

      <section className="course-section">
        <h4>Exams</h4>
        {course.exams.length ? (
          <ul className="task-list">
            {course.exams.map((e) => {
              const badge = examBadge(e.priorityStatus);
              return (
                <li key={e.id} className={`task-item task-${e.priorityStatus.toLowerCase()}`}>
                  <div className="task-item-main">
                    <strong>{e.title}</strong>
                    {e.description && <p>{e.description}</p>}
                  </div>
                  <div className="task-item-meta">
                    <span className={badge.className}>{badge.label}</span>
                    <time dateTime={e.scheduledDate}>
                      {formatDate(e.scheduledDate)}
                    </time>
                  </div>
                </li>
              );
            })}
          </ul>
        ) : (
          <p className="empty-state">No exams scheduled.</p>
        )}
      </section>
    </article>
  );
}

export function StudentDashboardPage() {
  const [activeTab, setActiveTab] = useState<DashboardTab>('dashboard');
  const [dashboard, setDashboard] = useState<StudentDashboardResponse | null>(null);
  const [courses, setCourses] = useState<StudentEnrolledCourseResponse[]>([]);
  const [profile, setProfile] = useState<StudentResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [coursesLoading, setCoursesLoading] = useState(false);
  const [coursesLoaded, setCoursesLoaded] = useState(false);

  useEffect(() => {
    Promise.all([getStudentDashboard(), getStudentProfile()])
      .then(([dash, prof]) => {
        setDashboard(dash);
        setProfile(prof);
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (activeTab !== 'courses' || coursesLoaded) return;

    setCoursesLoading(true);
    getStudentCourses()
      .then((data) => {
        setCourses(data);
        setCoursesLoaded(true);
      })
      .catch((err) => setError(err instanceof Error ? err.message : 'Failed to load courses'))
      .finally(() => setCoursesLoading(false));
  }, [activeTab, coursesLoaded]);

  if (loading) return <LoadingSpinner />;
  if (error && !dashboard) return <Alert type="error" message={error} />;

  return (
    <div>
      <PageHeader
        title={`Welcome, ${profile?.name ?? 'Student'}`}
        subtitle="Track your progress, notifications, and enrolled courses"
      />

      {error && <Alert type="error" message={error} />}

      <div className="tab-bar" role="tablist" aria-label="Student dashboard sections">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'dashboard'}
          className={`tab-btn ${activeTab === 'dashboard' ? 'active' : ''}`}
          onClick={() => setActiveTab('dashboard')}
        >
          My Dashboard
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'courses'}
          className={`tab-btn ${activeTab === 'courses' ? 'active' : ''}`}
          onClick={() => setActiveTab('courses')}
        >
          Courses
        </button>
      </div>

      {activeTab === 'dashboard' && (
        <div role="tabpanel">
          {dashboard?.latestNotification && (
            <div className="card highlight-card">
              <h3>Latest notification</h3>
              <strong>{dashboard.latestNotification.title}</strong>
              <p>{dashboard.latestNotification.message}</p>
            </div>
          )}

          <div className="dashboard-grid">
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
                        <div
                          className="progress-fill"
                          style={{ width: `${p.progressPercent}%` }}
                        />
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
                  <dt>Department</dt><dd>{profile.departmentName ?? '—'}</dd>
                  <dt>Subjects</dt><dd>{profile.subjectNames?.join(', ') || '—'}</dd>
                </dl>
              )}
            </section>
          </div>
        </div>
      )}

      {activeTab === 'courses' && (
        <div role="tabpanel">
          {coursesLoading ? (
            <LoadingSpinner />
          ) : courses.length ? (
            <div className="course-list">
              {courses.map((course) => (
                <CourseCard key={course.id} course={course} />
              ))}
            </div>
          ) : (
            <div className="card">
              <p className="empty-state">You are not enrolled in any courses yet.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
