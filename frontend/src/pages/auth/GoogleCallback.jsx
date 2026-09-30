import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import authService from '../../services/authService';
import { isAdminRole } from '../../utils/roleUtils';
import { AlertCircle, CheckCircle, ArrowRight } from 'lucide-react';
import Logo from '../../components/Logo';

const GoogleCallback = () => {
  const [searchParams] = useSearchParams();
  const code = searchParams.get('code');
  const errorParam = searchParams.get('error');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (errorParam) {
      setError(`Google authorization was denied or cancelled: ${errorParam}`);
      setLoading(false);
      return;
    }

    if (!code) {
      setError('No authorization code provided in the callback URL.');
      setLoading(false);
      return;
    }

    exchangeCode(code);
  }, [code, errorParam]);

  const exchangeCode = async (authCode) => {
    try {
      const data = await authService.googleCallback(authCode);
      login(data, data.token);

      if (isAdminRole(data.role)) {
        navigate('/admin/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Google authentication failed. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '440px', margin: '4rem auto' }}>
      <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
          <Logo size={36} withWordmark />
        </div>

        {loading ? (
          <div style={{ padding: '2rem 0' }}>
            <div className="spinner" style={{ margin: '0 auto 1.25rem' }} />
            <h3 style={{ fontSize: '1.25rem', fontWeight: 600, marginBottom: '0.5rem' }}>
              Signing in with Google...
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
              Completing secure authentication with CodeNova
            </p>
          </div>
        ) : error ? (
          <div style={{ padding: '1rem 0' }}>
            <div className="alert alert-error" style={{ marginBottom: '1.5rem', textAlign: 'left' }}>
              <AlertCircle size={20} />
              <span>{error}</span>
            </div>
            <Link
              to="/login"
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'center', width: '100%' }}
            >
              <span>Back to Sign In</span>
              <ArrowRight size={18} />
            </Link>
          </div>
        ) : (
          <div style={{ padding: '1rem 0' }}>
            <CheckCircle size={48} color="#22c55e" style={{ margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.25rem', fontWeight: 600, color: '#22c55e', marginBottom: '0.5rem' }}>
              Authentication Successful!
            </h3>
            <p style={{ color: 'var(--text-muted)' }}>Redirecting to your dashboard...</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default GoogleCallback;
