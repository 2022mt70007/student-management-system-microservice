import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createCourse,
  deleteCourse,
  listCourses,
  updateCourse,
} from '../../api/admin';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { CourseRequest, CourseResponse } from '../../types';

const emptyForm: CourseRequest = {
  title: '',
  description: '',
  department: '',
  instructor: '',
  credits: 3,
};

export function CoursesPage() {
  const [rows, setRows] = useState<CourseResponse[]>([]);
  const [form, setForm] = useState<CourseRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRows(await listCourses());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load courses');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function openCreate() {
    setEditingId(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(row: CourseResponse) {
    setEditingId(row.id);
    setForm({
      title: row.title,
      description: row.description ?? '',
      department: row.department ?? '',
      instructor: row.instructor ?? '',
      credits: row.credits ?? 3,
    });
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateCourse(editingId, form);
        setMessage('Course updated');
      } else {
        await createCourse(form);
        setMessage('Course created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: CourseResponse) {
    if (!confirm(`Delete course ${row.title}?`)) return;
    try {
      await deleteCourse(row.id);
      setMessage('Course deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Courses"
        subtitle="Manage course catalog"
        action={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Add course
          </button>
        }
      />
      <Alert type="error" message={error} />
      <Alert type="success" message={message} />
      {loading ? (
        <LoadingSpinner />
      ) : (
        <DataTable
          rows={rows}
          onEdit={openEdit}
          onDelete={handleDelete}
          columns={[
            { key: 'title', header: 'Title', render: (r) => r.title },
            { key: 'dept', header: 'Department', render: (r) => r.department ?? '—' },
            { key: 'instructor', header: 'Instructor', render: (r) => r.instructor ?? '—' },
            { key: 'credits', header: 'Credits', render: (r) => r.credits ?? '—' },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit course' : 'Create course'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label className="full-width">Title *<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></label>
            <label className="full-width">Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={3} /></label>
            <label>Department<input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} /></label>
            <label>Instructor<input value={form.instructor} onChange={(e) => setForm({ ...form, instructor: e.target.value })} /></label>
            <label>Credits<input type="number" min={1} value={form.credits ?? 3} onChange={(e) => setForm({ ...form, credits: Number(e.target.value) })} /></label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>Save</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
