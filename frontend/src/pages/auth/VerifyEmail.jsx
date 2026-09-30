import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import authService from '../../services/authService';
import { CheckCircle, AlertCircle, Mail, ArrowRight, RefreshCw } from 'lucide-react';
import Logo from '../../components/Logo';

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState('idle'); // 'verifying', 'success', 'error', 'idle'
  const [message, setMessage] = useState('');
  const [resendEmail, setResendEmail] = useState('');
  const [resending, setResending] = useState(false);
  const [resendMessage, setResendMessage] = useState('');
  const [resendError, setResendError] = useState('');

  useEffect(() => {
    if (token) {
      handleVerification(token);
    }
  }, [token]);

  const handleVerification = async (verifyToken) => {
    setLoading(true);
    setStatus('verifying');
    try {
      const data = await authService.verifyEmail(verifyToken);
      setStatus('success');
      setMessage(data.message || 'Email verified successfully! You can now log in.');
    } catch (err) {
      setStatus('error');
      setMessage(err.response?.data?.message || 'Verification link is invalid or has expired.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async (e) => {
    e.preventDefault();
    if (!resendEmail) return;

    setResending(true);
    setResendMessage('');
    setResendError('');
    try {
      const res = await authService.resendVerification(resendEmail);
      setResendMessage(res.message || 'A fresh verification link has been sent to your email.');
    } catch (err) {
      setResendError(err.response?.data?.message || 'Failed to resend verification link.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div style={{ maxWidth: '480px', margin: '4rem auto' }}>
      <div className="card" style={{ padding: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
          <Logo size={36} withWordmark />
        </div>

        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', textAlign: 'center' }}>
          Email Verification
        </h2>

        {status === 'verifying' && (
          <div style={{ textAlign: 'center', padding: '2rem 0' }}>
            <div className="spinner" style={{ margin: '0 auto 1rem' }} />
            <p style={{ color: 'var(--text-muted)' }}>Verifying your email address...</p>
          </div>
        )}

        {status === 'success' && (
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            <div style={{
              display: 'inline-flex',
              padding: '1rem',
              borderRadius: '50%',
              backgroundColor: 'rgba(34, 197, 94, 0.1)',
              color: '#22c55e',
              marginBottom: '1rem',
            }}>
              <CheckCircle size={48} />
            </div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '0.5rem', color: '#22c55e' }}>
              Verification Successful!
            </h3>
            <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.95rem' }}>
              {message}
            </p>
            <Link
              to="/login"
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', width: '100%', justifyContent: 'center' }}
            >
              <span>Continue to Sign In</span>
              <ArrowRight size={18} />
            </Link>
          </div>
        )}

        {(status === 'error' || (!token && status === 'idle')) && (
          <div>
            {status === 'error' && (
              <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
                <AlertCircle size={18} />
                <span>{message}</span>
              </div>
            )}

            <div style={{
              backgroundColor: 'var(--bg-input, #1e293b)',
              padding: '1.25rem',
              borderRadius: '0.5rem',
              marginBottom: '1.5rem',
              border: '1px solid var(--border-color, rgba(255,255,255,0.1))'
            }}>
              <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Mail size={18} color="var(--primary)" />
                Resend Verification Link
              </h4>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1rem' }}>
                Enter your registered email address or username to receive a new verification link.
              </p>

              {resendMessage && (
                <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
                  <CheckCircle size={18} />
                  <span>{resendMessage}</span>
                </div>
              )}

              {resendError && (
                <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                  <AlertCircle size={18} />
                  <span>{resendError}</span>
                </div>
              )}

              <form onSubmit={handleResend}>
                <div className="form-group" style={{ marginBottom: '1rem' }}>
                  <input
                    type="text"
                    value={resendEmail}
                    onChange={(e) => setResendEmail(e.target.value)}
                    required
                    placeholder="e.g. alex@example.com"
                    className="form-control"
                  />
                </div>
                <button
                  type="submit"
                  disabled={resending || !resendEmail}
                  className="btn btn-primary"
                  style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                >
                  <RefreshCw size={16} className={resending ? 'animate-spin' : ''} />
                  <span>{resending ? 'Sending...' : 'Send Verification Email'}</span>
                </button>
              </form>
            </div>

            <div style={{ textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
              Already verified?{' '}
              <Link to="/login" style={{ fontWeight: 600, color: 'var(--primary)' }}>
                Sign In
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VerifyEmail;
