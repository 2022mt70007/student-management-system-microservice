import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createClass,
  deleteClass,
  listClasses,
  listDepartments,
  updateClass,
} from '../../api/academic';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { AcademicClassRequest, AcademicClassResponse, DepartmentResponse } from '../../types';

const emptyForm: AcademicClassRequest = {
  classCode: '',
  className: '',
  departmentId: 0,
  description: '',
};

export function ClassesPage() {
  const [rows, setRows] = useState<AcademicClassResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [form, setForm] = useState<AcademicClassRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [classRows, deptRows] = await Promise.all([listClasses(), listDepartments()]);
      setRows(classRows);
      setDepartments(deptRows);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load classes');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  function openCreate() {
    setEditingId(null);
    setForm({ ...emptyForm, departmentId: departments[0]?.id ?? 0 });
    setModalOpen(true);
  }

  function openEdit(row: AcademicClassResponse) {
    setEditingId(row.id);
    setForm({
      classCode: row.classCode,
      className: row.className,
      departmentId: row.departmentId,
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
        await updateClass(editingId, form);
        setMessage('Class updated');
      } else {
        await createClass(form);
        setMessage('Class created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: AcademicClassResponse) {
    if (!confirm(`Delete class ${row.className}?`)) return;
    try {
      await deleteClass(row.id);
      setMessage('Class deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Classes"
        subtitle="Manage classes within departments"
        action={<button type="button" className="btn btn-primary" onClick={openCreate}>+ Add class</button>}
      />
      <Alert type="error" message={error} />
      <Alert type="success" message={message} />
      {loading ? <LoadingSpinner /> : (
        <DataTable
          rows={rows}
          onEdit={openEdit}
          onDelete={handleDelete}
          columns={[
            { key: 'code', header: 'Code', render: (r) => r.classCode },
            { key: 'name', header: 'Class', render: (r) => r.className },
            { key: 'dept', header: 'Department', render: (r) => r.departmentName ?? '—' },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit class' : 'Create class'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>Code *<input value={form.classCode} onChange={(e) => setForm({ ...form, classCode: e.target.value })} required /></label>
            <label>Name *<input value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} required /></label>
            <label className="full-width">Department *
              <select value={form.departmentId || ''} onChange={(e) => setForm({ ...form, departmentId: Number(e.target.value) })} required>
                <option value="">Select department</option>
                {departments.map((d) => <option key={d.id} value={d.id}>{d.departmentName}</option>)}
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
