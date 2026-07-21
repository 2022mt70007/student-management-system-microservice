import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../api/client';
import { Alert } from '../components/ui';
import { roleHomePath, useAuth } from '../context/AuthContext';
import type { LoginErrorDetails } from '../types';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [warning, setWarning] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setWarning('');
    setLoading(true);
    try {
      const role = await login({ email, password });
      navigate(roleHomePath(role));
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Login failed';
      setError(message);
      if (err instanceof ApiError && err.data && typeof err.data === 'object') {
        const details = err.data as LoginErrorDetails;
        if (details.locked) {
          setWarning('Use Forgot password to reset your password and unlock the account.');
        } else if (typeof details.failedAttempts === 'number' && details.failedAttempts > 0) {
          const max = details.maxAttempts ?? 6;
          const remaining = details.remainingAttempts ?? Math.max(0, max - details.failedAttempts);
          if (details.warnLockout) {
            setWarning(
              `Warning: your account will be locked after ${max} failed attempts. ` +
                `You have used ${details.failedAttempts} of ${max}; ${remaining} remaining.`,
            );
          } else {
            setWarning(`Failed attempt ${details.failedAttempts} of ${max} (${remaining} remaining).`);
          }
        }
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Student Management System</h1>
          <p>Sign in to your account</p>
        </div>
        <Alert type="error" message={error} />
        <Alert type="info" message={warning} />
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
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </label>
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/forgot-password">Forgot password?</Link>
          {' · '}
          New admin? <Link to="/register">Complete registration</Link> with the code from your email first, then sign in here.
        </p>
      </div>
    </div>
  );
}
