import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createTeacher,
  deleteTeacher,
  listTeachers,
  updateTeacher,
} from '../../api/admin';
import { DataTable, Modal, formatSubjects, parseSubjects } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader, StatusBadge } from '../../components/ui';
import type { TeacherRequest, TeacherResponse } from '../../types';

const emptyForm: TeacherRequest = {
  name: '',
  teacherId: '',
  email: '',
  department: '',
  phone: '',
  address: '',
  subjects: [],
};

export function TeachersPage() {
  const [rows, setRows] = useState<TeacherResponse[]>([]);
  const [form, setForm] = useState<TeacherRequest>(emptyForm);
  const [subjectsText, setSubjectsText] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRows(await listTeachers());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load teachers');
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
    setSubjectsText('');
    setModalOpen(true);
  }

  function openEdit(row: TeacherResponse) {
    setEditingId(row.id);
    setForm({
      name: row.name,
      teacherId: row.teacherId,
      email: row.email,
      department: row.department ?? '',
      phone: row.phone ?? '',
      address: row.address ?? '',
      subjects: row.subjects ?? [],
    });
    setSubjectsText(formatSubjects(row.subjects));
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = { ...form, subjects: parseSubjects(subjectsText) };
    try {
      if (editingId) {
        await updateTeacher(editingId, payload);
        setMessage('Teacher updated');
      } else {
        const res = await createTeacher(payload);
        setMessage(res.message || 'Teacher created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: TeacherResponse) {
    if (!confirm(`Delete teacher ${row.name}?`)) return;
    try {
      await deleteTeacher(row.id);
      setMessage('Teacher deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Teachers"
        subtitle="Manage teacher profiles"
        action={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Add teacher
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
            { key: 'name', header: 'Name', render: (r) => r.name },
            { key: 'id', header: 'Teacher ID', render: (r) => r.teacherId },
            { key: 'email', header: 'Email', render: (r) => r.email },
            { key: 'dept', header: 'Department', render: (r) => r.department ?? '—' },
            { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit teacher' : 'Create teacher'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>Name *<input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></label>
            <label>Teacher ID *<input value={form.teacherId} onChange={(e) => setForm({ ...form, teacherId: e.target.value })} required /></label>
            <label>Email *<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></label>
            <label>Department<input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} /></label>
            <label>Phone<input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></label>
            <label className="full-width">Address<input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} /></label>
            <label className="full-width">Subjects<input value={subjectsText} onChange={(e) => setSubjectsText(e.target.value)} /></label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving...' : 'Save'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
