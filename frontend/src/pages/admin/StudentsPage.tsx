import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createStudent,
  deleteStudent,
  listStudents,
  updateStudent,
} from '../../api/admin';
import { DataTable, Modal, formatSubjects, parseSubjects } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader, StatusBadge } from '../../components/ui';
import type { StudentRequest, StudentResponse } from '../../types';

const emptyForm: StudentRequest = {
  name: '',
  email: '',
  phone: '',
  address: '',
  rollNumber: '',
  className: '',
  department: '',
  subjects: [],
};

export function StudentsPage() {
  const [rows, setRows] = useState<StudentResponse[]>([]);
  const [form, setForm] = useState<StudentRequest>(emptyForm);
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
      setRows(await listStudents());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load students');
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

  function openEdit(row: StudentResponse) {
    setEditingId(row.id);
    setForm({
      name: row.name,
      email: row.email,
      phone: row.phone ?? '',
      address: row.address ?? '',
      rollNumber: row.rollNumber ?? '',
      className: row.className ?? '',
      department: row.department ?? '',
      subjects: row.subjects ?? [],
    });
    setSubjectsText(formatSubjects(row.subjects));
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    const payload = { ...form, subjects: parseSubjects(subjectsText) };
    try {
      if (editingId) {
        await updateStudent(editingId, payload);
        setMessage('Student updated');
      } else {
        const res = await createStudent(payload);
        setMessage(res.message || 'Student created. Check MailHog for registration email.');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: StudentResponse) {
    if (!confirm(`Delete student ${row.name}?`)) return;
    try {
      await deleteStudent(row.id);
      setMessage('Student deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Students"
        subtitle="Manage student profiles and send registration invites"
        action={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Add student
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
            { key: 'email', header: 'Email', render: (r) => r.email },
            { key: 'roll', header: 'Roll #', render: (r) => r.rollNumber ?? '—' },
            { key: 'class', header: 'Class', render: (r) => r.className ?? '—' },
            {
              key: 'status',
              header: 'Status',
              render: (r) => <StatusBadge status={r.status} />,
            },
          ]}
        />
      )}

      <Modal
        title={editingId ? 'Edit student' : 'Create student'}
        open={modalOpen}
        onClose={() => setModalOpen(false)}
      >
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>
              Name *
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </label>
            <label>
              Email *
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            </label>
            <label>
              Roll number
              <input value={form.rollNumber} onChange={(e) => setForm({ ...form, rollNumber: e.target.value })} />
            </label>
            <label>
              Class
              <input value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} />
            </label>
            <label>
              Department
              <input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} />
            </label>
            <label>
              Phone
              <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </label>
            <label className="full-width">
              Address
              <input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
            </label>
            <label className="full-width">
              Subjects (comma-separated)
              <input value={subjectsText} onChange={(e) => setSubjectsText(e.target.value)} placeholder="Math, Physics" />
            </label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : editingId ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
