import { FormEvent, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/auth';
import { saveAuth } from '../api/client';
import { Alert } from '../components/ui';
import { roleHomePath } from '../context/AuthContext';
import type { AuthUser } from '../types';

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const initialEmail = useMemo(() => searchParams.get('email') || '', [searchParams]);
  const initialCode = useMemo(() => searchParams.get('code') || '', [searchParams]);

  const [email, setEmail] = useState(initialEmail);
  const [code, setCode] = useState(initialCode);
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await resetPassword({ email, code, newPassword });
      if (!res.success || !res.data) {
        throw new Error(res.message || 'Reset failed');
      }
      const authUser: AuthUser = {
        token: res.data.token,
        email: res.data.email,
        role: res.data.role,
        profileId: res.data.profileId,
      };
      saveAuth(authUser);
      navigate(roleHomePath(authUser.role));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Reset failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Reset password</h1>
          <p>Enter the code from your email and choose a new password</p>
        </div>
        <Alert type="error" message={error} />
        <form onSubmit={handleSubmit} className="form">
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </label>
          <label>
            Reset code
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              required
              inputMode="numeric"
            />
          </label>
          <label>
            New password
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
              autoComplete="new-password"
            />
          </label>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Resetting...' : 'Reset password'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/forgot-password">Request a new code</Link>
          {' · '}
          <Link to="/login">Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}
