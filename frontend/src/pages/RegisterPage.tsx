import { FormEvent, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { validateCode } from '../api/auth';
import { Alert } from '../components/ui';
import { roleHomePath, useAuth } from '../context/AuthContext';

export function RegisterPage() {
  const { completeRegistration } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [step, setStep] = useState<'code' | 'password'>('code');
  const [email, setEmail] = useState(searchParams.get('email') ?? '');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleValidateCode(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const response = await validateCode({ email, code });
      if (!response.success) {
        throw new Error(response.message || 'Invalid code');
      }
      setSuccess('Code verified. Set your password to finish.');
      setStep('password');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Validation failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleSetPassword(e: FormEvent) {
    e.preventDefault();
    setError('');
    if (password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setLoading(true);
    try {
      const role = await completeRegistration({ email, code, password });
      navigate(roleHomePath(role));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Complete Registration</h1>
          <p>
            {step === 'code'
              ? 'Enter the code from your email'
              : 'Create your password'}
          </p>
        </div>
        <Alert type="error" message={error} />
        <Alert type="success" message={success} />

        {step === 'code' ? (
          <form onSubmit={handleValidateCode} className="form">
            <label>
              Email
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </label>
            <label>
              Registration code
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 7))}
                placeholder="6–7 digits"
                required
                pattern="\d{6,7}"
              />
            </label>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Validating...' : 'Validate code'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleSetPassword} className="form">
            <label>
              New password
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength={8}
                required
              />
            </label>
            <label>
              Confirm password
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                minLength={8}
                required
              />
            </label>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Creating account...' : 'Set password & sign in'}
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => setStep('code')}
            >
              Back
            </button>
          </form>
        )}

        <p className="auth-footer">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
