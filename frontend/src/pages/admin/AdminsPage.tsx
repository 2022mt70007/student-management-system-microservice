import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createAdmin,
  deleteAdmin,
  listAdmins,
  updateAdmin,
} from '../../api/admin';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader, StatusBadge } from '../../components/ui';
import type { AdminUserRequest, AdminUserResponse } from '../../types';

const emptyForm: AdminUserRequest = {
  name: '',
  adminId: '',
  email: '',
  department: '',
  phone: '',
  address: '',
};

export function AdminsPage() {
  const [rows, setRows] = useState<AdminUserResponse[]>([]);
  const [form, setForm] = useState<AdminUserRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRows(await listAdmins());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load admins');
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

  function openEdit(row: AdminUserResponse) {
    setEditingId(row.id);
    setForm({
      name: row.name,
      adminId: row.adminId,
      email: row.email,
      department: row.department ?? '',
      phone: row.phone ?? '',
      address: row.address ?? '',
    });
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateAdmin(editingId, form);
        setMessage('Admin updated');
      } else {
        const res = await createAdmin(form);
        setMessage(res.message || 'Admin created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: AdminUserResponse) {
    if (!confirm(`Delete admin ${row.name}?`)) return;
    try {
      await deleteAdmin(row.id);
      setMessage('Admin deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Admins"
        subtitle="Manage administrator accounts"
        action={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Add admin
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
            { key: 'adminId', header: 'Admin ID', render: (r) => r.adminId },
            { key: 'email', header: 'Email', render: (r) => r.email },
            { key: 'status', header: 'Status', render: (r) => <StatusBadge status={r.status} /> },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit admin' : 'Create admin'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>Name *<input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></label>
            <label>Admin ID *<input value={form.adminId} onChange={(e) => setForm({ ...form, adminId: e.target.value })} required /></label>
            <label>Email *<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></label>
            <label>Department<input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} /></label>
            <label>Phone<input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></label>
            <label className="full-width">Address<input value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} /></label>
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
