import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../api/auth';
import { Alert } from '../components/ui';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const res = await forgotPassword({ email });
      setMessage(res.message || 'If an account exists for that email, a reset code has been sent.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Forgot password</h1>
          <p>Enter your email to receive a reset code</p>
        </div>
        <Alert type="error" message={error} />
        <Alert type="success" message={message} />
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
          <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Sending...' : 'Send reset code'}
          </button>
        </form>
        <p className="auth-footer">
          <Link to="/reset-password">Already have a code? Reset password</Link>
          {' · '}
          <Link to="/login">Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}
