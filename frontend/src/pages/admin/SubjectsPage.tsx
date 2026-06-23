import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createSubject,
  deleteSubject,
  listClasses,
  listSubjects,
  updateSubject,
} from '../../api/academic';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { AcademicClassResponse, SubjectRequest, SubjectResponse } from '../../types';

const emptyForm: SubjectRequest = {
  subjectCode: '',
  subjectName: '',
  classId: 0,
  credits: 3,
  description: '',
};

export function SubjectsPage() {
  const [rows, setRows] = useState<SubjectResponse[]>([]);
  const [classes, setClasses] = useState<AcademicClassResponse[]>([]);
  const [form, setForm] = useState<SubjectRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [subjectRows, classRows] = await Promise.all([listSubjects(), listClasses()]);
      setRows(subjectRows);
      setClasses(classRows);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load subjects');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  function openCreate() {
    setEditingId(null);
    setForm({ ...emptyForm, classId: classes[0]?.id ?? 0 });
    setModalOpen(true);
  }

  function openEdit(row: SubjectResponse) {
    setEditingId(row.id);
    setForm({
      subjectCode: row.subjectCode,
      subjectName: row.subjectName,
      classId: row.classId,
      credits: row.credits ?? 3,
      description: row.description ?? '',
    });
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateSubject(editingId, form);
        setMessage('Subject updated');
      } else {
        await createSubject(form);
        setMessage('Subject created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: SubjectResponse) {
    if (!confirm(`Delete subject ${row.subjectName}?`)) return;
    try {
      await deleteSubject(row.id);
      setMessage('Subject deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Subjects"
        subtitle="Manage subjects within classes"
        action={<button type="button" className="btn btn-primary" onClick={openCreate}>+ Add subject</button>}
      />
      <Alert type="error" message={error} />
      <Alert type="success" message={message} />
      {loading ? <LoadingSpinner /> : (
        <DataTable
          rows={rows}
          onEdit={openEdit}
          onDelete={handleDelete}
          columns={[
            { key: 'code', header: 'Code', render: (r) => r.subjectCode },
            { key: 'name', header: 'Subject', render: (r) => r.subjectName },
            { key: 'class', header: 'Class', render: (r) => r.className ?? '—' },
            { key: 'credits', header: 'Credits', render: (r) => r.credits ?? '—' },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit subject' : 'Create subject'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>Code *<input value={form.subjectCode} onChange={(e) => setForm({ ...form, subjectCode: e.target.value })} required /></label>
            <label>Name *<input value={form.subjectName} onChange={(e) => setForm({ ...form, subjectName: e.target.value })} required /></label>
            <label>Credits<input type="number" min={1} value={form.credits ?? ''} onChange={(e) => setForm({ ...form, credits: Number(e.target.value) })} /></label>
            <label className="full-width">Class *
              <select value={form.classId || ''} onChange={(e) => setForm({ ...form, classId: Number(e.target.value) })} required>
                <option value="">Select class</option>
                {classes.map((c) => <option key={c.id} value={c.id}>{c.departmentName} — {c.className}</option>)}
              </select>
            </label>
            <label className="full-width">Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} rows={3} /></label>
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
