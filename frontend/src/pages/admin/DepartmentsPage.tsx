import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createDepartment,
  deleteDepartment,
  listDepartments,
  updateDepartment,
} from '../../api/academic';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { DepartmentRequest, DepartmentResponse } from '../../types';

const emptyForm: DepartmentRequest = {
  departmentCode: '',
  departmentName: '',
  description: '',
  status: 'ACTIVE',
};

export function DepartmentsPage() {
  const [rows, setRows] = useState<DepartmentResponse[]>([]);
  const [form, setForm] = useState<DepartmentRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRows(await listDepartments());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  function openCreate() {
    setEditingId(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEdit(row: DepartmentResponse) {
    setEditingId(row.id);
    setForm({
      departmentCode: row.departmentCode,
      departmentName: row.departmentName,
      description: row.description ?? '',
      status: row.status,
    });
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateDepartment(editingId, form);
        setMessage('Department updated');
      } else {
        await createDepartment(form);
        setMessage('Department created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: DepartmentResponse) {
    if (!confirm(`Delete department ${row.departmentName}?`)) return;
    try {
      await deleteDepartment(row.id);
      setMessage('Department deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Departments"
        subtitle="Manage academic departments"
        action={<button type="button" className="btn btn-primary" onClick={openCreate}>+ Add department</button>}
      />
      <Alert type="error" message={error} />
      <Alert type="success" message={message} />
      {loading ? <LoadingSpinner /> : (
        <DataTable
          rows={rows}
          onEdit={openEdit}
          onDelete={handleDelete}
          columns={[
            { key: 'code', header: 'Code', render: (r) => r.departmentCode },
            { key: 'name', header: 'Name', render: (r) => r.departmentName },
            { key: 'status', header: 'Status', render: (r) => r.status },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit department' : 'Create department'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>Code *<input value={form.departmentCode} onChange={(e) => setForm({ ...form, departmentCode: e.target.value })} required /></label>
            <label>Name *<input value={form.departmentName} onChange={(e) => setForm({ ...form, departmentName: e.target.value })} required /></label>
            <label>Status *
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value as DepartmentRequest['status'] })}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
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
