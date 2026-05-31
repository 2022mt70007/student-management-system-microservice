import { FormEvent, useCallback, useEffect, useState } from 'react';
import {
  createNotification,
  deleteNotification,
  listNotifications,
  updateNotification,
} from '../../api/admin';
import { DataTable, Modal } from '../../components/DataTable';
import { Alert, LoadingSpinner, PageHeader } from '../../components/ui';
import type { NotificationRequest, NotificationResponse } from '../../types';

const emptyForm: NotificationRequest = {
  title: '',
  message: '',
  targetRole: 'STUDENT',
};

export function NotificationsPage() {
  const [rows, setRows] = useState<NotificationResponse[]>([]);
  const [form, setForm] = useState<NotificationRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setRows(await listNotifications());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load notifications');
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

  function openEdit(row: NotificationResponse) {
    setEditingId(row.id);
    setForm({
      title: row.title,
      message: row.message,
      targetRole: row.targetRole ?? 'ALL',
    });
    setModalOpen(true);
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateNotification(editingId, form);
        setMessage('Notification updated');
      } else {
        await createNotification(form);
        setMessage('Notification created');
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(row: NotificationResponse) {
    if (!confirm(`Delete notification "${row.title}"?`)) return;
    try {
      await deleteNotification(row.id);
      setMessage('Notification deleted');
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  return (
    <div>
      <PageHeader
        title="Notifications"
        subtitle="Broadcast messages to students and teachers"
        action={
          <button type="button" className="btn btn-primary" onClick={openCreate}>
            + Add notification
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
            { key: 'message', header: 'Message', render: (r) => r.message },
            { key: 'target', header: 'Target', render: (r) => r.targetRole ?? 'ALL' },
            {
              key: 'created',
              header: 'Created',
              render: (r) => (r.createdAt ? new Date(r.createdAt).toLocaleString() : '—'),
            },
          ]}
        />
      )}
      <Modal title={editingId ? 'Edit notification' : 'Create notification'} open={modalOpen} onClose={() => setModalOpen(false)}>
        <form className="form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <label className="full-width">Title *<input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required /></label>
            <label className="full-width">Message *<textarea value={form.message} onChange={(e) => setForm({ ...form, message: e.target.value })} rows={4} required /></label>
            <label>
              Target role
              <select value={form.targetRole} onChange={(e) => setForm({ ...form, targetRole: e.target.value })}>
                <option value="STUDENT">STUDENT</option>
                <option value="TEACHER">TEACHER</option>
                <option value="ADMIN">ADMIN</option>
                <option value="ALL">ALL</option>
              </select>
            </label>
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
