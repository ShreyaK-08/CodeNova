import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import authService from '../../services/authService';
import { isAdminRole } from '../../utils/roleUtils';
import AuthVisualPanel from '../../components/auth/AuthVisualPanel';
import LanguageSelector from '../../components/LanguageSelector';
import Logo from '../../components/Logo';
import {
  AlertCircle,
  CheckCircle,
  RefreshCw,
  Eye,
  EyeOff,
  ArrowLeft
} from 'lucide-react';

const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" style={{ marginRight: '0.6rem', flexShrink: 0 }}>
    <path
      fill="#4285F4"
      d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.8-2.4 3.66v3.04h3.88c2.27-2.09 3.665-5.17 3.665-9.14z"
    />
    <path
      fill="#34A853"
      d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.04c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.1-6.72-4.93H1.24v3.13C3.26 21.36 7.33 24 12 24z"
    />
    <path
      fill="#FBBC05"
      d="M5.28 14.28c-.25-.72-.38-1.49-.38-2.28s.13-1.56.38-2.28V6.59H1.24C.45 8.16 0 9.94 0 12s.45 3.84 1.24 5.41l4.04-3.13z"
    />
    <path
      fill="#EA4335"
      d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.33 0 3.26 2.64 1.24 6.59l4.04 3.13c.95-2.83 3.6-4.97 6.72-4.97z"
    />
  </svg>
);

const Login = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [isUnverified, setIsUnverified] = useState(false);
  const [resendStatus, setResendStatus] = useState('');
  const [resending, setResending] = useState(false);
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [verifiedNotice, setVerifiedNotice] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (searchParams.get('verified') === 'true') {
      setVerifiedNotice(true);
    }
  }, [searchParams]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setIsUnverified(false);
  };

  const handleGoogleLogin = async () => {
    setError('');
    setGoogleLoading(true);
    try {
      const config = await authService.getGoogleConfig();
      if (!config.configured) {
        setError('Google OAuth is not configured on this server. Please configure GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in backend environment variables.');
        setGoogleLoading(false);
        return;
      }
      const urlResponse = await authService.getGoogleAuthUrl();
      if (urlResponse?.url) {
        window.location.href = urlResponse.url;
      } else {
        setError('Could not retrieve Google authorization URL.');
        setGoogleLoading(false);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Google OAuth is currently unavailable. Please verify server configuration.');
      setGoogleLoading(false);
    }
  };

  const handleResend = async () => {
    if (!formData.usernameOrEmail) {
      setError('Please enter your username or email above first.');
      return;
    }
    setResending(true);
    setResendStatus('');
    try {
      const res = await authService.resendVerification(formData.usernameOrEmail);
      setResendStatus(res.message || 'A new verification link has been sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend verification link.');
    } finally {
      setResending(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsUnverified(false);
    setResendStatus('');
    setLoading(true);

    try {
      const data = await authService.login(formData);
      login(data, data.token);

      if (isAdminRole(data.role)) {
        navigate('/admin/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || t('auth.invalidCredentials', 'Invalid username/email or password.');
      setError(errorMsg);
      if (errorMsg.includes('EMAIL_NOT_VERIFIED')) {
        setIsUnverified(true);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-fullscreen-container">
      <style>{`
        .auth-fullscreen-container {
          min-height: 100vh;
          width: 100%;
          display: grid;
          grid-template-columns: 48% 52%;
          background: #ffffff;
        }

        .auth-form-side {
          display: flex;
          flex-direction: column;
          justify-content: space-between;
          padding: 2.5rem 3.5rem;
          min-height: 100vh;
          background: #ffffff;
          position: relative;
        }

        .auth-top-actions {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
          margin-bottom: 1.5rem;
        }

        .auth-back-link {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          color: #64748b;
          font-size: 0.85rem;
          font-weight: 500;
          text-decoration: none;
          transition: color 0.2s;
        }
        .auth-back-link:hover {
          color: var(--primary);
        }

        .auth-form-wrapper {
          width: 100%;
          max-width: 440px;
          margin: auto;
          padding: 1rem 0;
        }

        .auth-header-block {
          margin-bottom: 1.75rem;
        }

        .auth-title {
          font-size: 2rem;
          font-weight: 800;
          color: #0f172a;
          line-height: 1.2;
          margin: 0 0 0.4rem;
          letter-spacing: -0.02em;
        }

        .auth-subtitle {
          font-size: 0.95rem;
          color: #64748b;
          margin: 0;
          line-height: 1.5;
        }

        .auth-google-btn {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #ffffff;
          color: #1e293b;
          border: 1px solid #cbd5e1;
          border-radius: 8px;
          padding: 0.65rem 1rem;
          font-size: 0.925rem;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s ease;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
        }
        .auth-google-btn:hover {
          background: #f8fafc;
          border-color: #94a3b8;
        }

        .auth-divider {
          display: flex;
          align-items: center;
          text-align: center;
          color: #94a3b8;
          font-size: 0.825rem;
          font-weight: 500;
          margin: 1.35rem 0;
        }
        .auth-divider::before,
        .auth-divider::after {
          content: '';
          flex: 1;
          height: 1px;
          background: #e2e8f0;
        }
        .auth-divider span {
          padding: 0 0.85rem;
        }

        .auth-field-group {
          margin-bottom: 1.15rem;
        }

        .auth-field-label {
          display: block;
          font-size: 0.875rem;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 0.35rem;
        }

        .auth-input-wrapper {
          position: relative;
          display: flex;
          align-items: center;
        }

        .auth-input-field {
          width: 100%;
          height: 42px;
          padding: 0.5rem 0.85rem;
          font-size: 0.925rem;
          border: 1px solid #cbd5e1;
          border-radius: 8px;
          background: #ffffff;
          color: #0f172a;
          transition: border-color 0.2s, box-shadow 0.2s;
        }
        .auth-input-field:focus {
          outline: none;
          border-color: #7c3aed;
          box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.12);
        }

        .auth-eye-toggle {
          position: absolute;
          right: 0.75rem;
          background: none;
          border: none;
          color: #94a3b8;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 0.25rem;
        }
        .auth-eye-toggle:hover {
          color: #475569;
        }

        .auth-meta-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin: 0.75rem 0 1.25rem;
          font-size: 0.85rem;
        }

        .auth-checkbox-label {
          display: inline-flex;
          align-items: center;
          gap: 0.4rem;
          color: #64748b;
          cursor: pointer;
          user-select: none;
          margin: 0;
        }

        .auth-submit-btn {
          width: 100%;
          height: 44px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
          color: #ffffff;
          border: none;
          border-radius: 8px;
          font-size: 0.95rem;
          font-weight: 700;
          cursor: pointer;
          transition: transform 0.15s, box-shadow 0.15s;
          box-shadow: 0 4px 12px rgba(124, 58, 237, 0.25);
        }
        .auth-submit-btn:hover:not(:disabled) {
          transform: translateY(-1px);
          box-shadow: 0 6px 16px rgba(124, 58, 237, 0.35);
        }
        .auth-submit-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        .auth-footer-nav {
          margin-top: 1.5rem;
          text-align: center;
          font-size: 0.9rem;
          color: #64748b;
        }

        @media (max-width: 1024px) {
          .auth-fullscreen-container {
            grid-template-columns: 42% 58%;
          }
          .auth-form-side {
            padding: 2rem;
          }
        }

        @media (max-width: 840px) {
          .auth-fullscreen-container {
            grid-template-columns: 1fr;
          }
          .auth-form-side {
            padding: 2rem 1.5rem;
            min-height: auto;
          }
        }
      `}</style>

      {/* LEFT SIDE: Visual Panel with Developer Illustration & Benefits */}
      <AuthVisualPanel />

      {/* RIGHT SIDE: Authentication Area (Clean Canvas, NO Floating Card) */}
      <div className="auth-form-side">
        {/* Top bar actions */}
        <div className="auth-top-actions">
          <Link to="/" className="auth-back-link">
            <ArrowLeft size={16} />
            <span>Back to CodeNova</span>
          </Link>
          <LanguageSelector />
        </div>

        {/* Main Authentication Form Content */}
        <div className="auth-form-wrapper">
          <div className="auth-header-block">
            <h2 className="auth-title">Welcome Back</h2>
            <p className="auth-subtitle">Sign in to continue your coding journey.</p>
          </div>

          {verifiedNotice && (
            <div className="alert alert-success" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem', borderRadius: '8px' }}>
              <CheckCircle size={18} />
              <span>Email verified successfully! You can now log in with your credentials.</span>
            </div>
          )}

          {error && (
            <div className="alert alert-error" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem', borderRadius: '8px' }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {resendStatus && (
            <div className="alert alert-success" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem', borderRadius: '8px' }}>
              <CheckCircle size={18} />
              <span>{resendStatus}</span>
            </div>
          )}

          {isUnverified && (
            <div style={{
              backgroundColor: 'rgba(234, 179, 8, 0.1)',
              border: '1px solid rgba(234, 179, 8, 0.3)',
              borderRadius: '8px',
              padding: '0.75rem 1rem',
              marginBottom: '1.25rem',
              fontSize: '0.85rem'
            }}>
              <p style={{ color: '#d97706', margin: '0 0 0.5rem', fontWeight: 600 }}>
                Your account email is unverified.
              </p>
              <button
                type="button"
                onClick={handleResend}
                disabled={resending}
                className="btn btn-secondary"
                style={{ fontSize: '0.775rem', padding: '0.3rem 0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
              >
                <RefreshCw size={14} className={resending ? 'animate-spin' : ''} />
                <span>{resending ? 'Resending...' : 'Resend Verification Email'}</span>
              </button>
            </div>
          )}

          {/* Google OAuth Login Button */}
          <button
            type="button"
            onClick={handleGoogleLogin}
            disabled={googleLoading}
            className="auth-google-btn"
          >
            <GoogleIcon />
            <span>{googleLoading ? 'Connecting to Google...' : 'Continue with Google'}</span>
          </button>

          <div className="auth-divider">
            <span>or continue with email</span>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="auth-field-group">
              <label className="auth-field-label">{t('auth.usernameOrEmail', 'Username or Email')}</label>
              <input
                type="text"
                name="usernameOrEmail"
                value={formData.usernameOrEmail}
                onChange={handleChange}
                required
                className="auth-input-field"
                placeholder="e.g. likhil or alex@example.com"
              />
            </div>

            <div className="auth-field-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <label className="auth-field-label" style={{ margin: 0 }}>{t('auth.password', 'Password')}</label>
              </div>
              <div className="auth-input-wrapper">
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="auth-input-field"
                  placeholder="••••••••"
                  style={{ paddingRight: '2.5rem' }}
                />
                <button
                  type="button"
                  className="auth-eye-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  tabIndex={-1}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="auth-meta-row">
              <label className="auth-checkbox-label">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  style={{ borderRadius: '4px', accentColor: '#7c3aed' }}
                />
                <span>Remember me</span>
              </label>
              <Link to="/verify-email" style={{ color: '#7c3aed', fontWeight: 600, textDecoration: 'none' }}>
                Forgot password?
              </Link>
            </div>

            <button
              type="submit"
              className="auth-submit-btn"
              disabled={loading}
            >
              {loading ? t('auth.signingIn', 'Signing in...') : t('auth.signIn', 'Sign In')}
            </button>
          </form>

          <div className="auth-footer-nav">
            {t('auth.noAccount', "Don't have an account?")}{' '}
            <Link to="/register" style={{ fontWeight: 700, color: '#7c3aed', textDecoration: 'none' }}>
              {t('auth.signUp', 'Sign up')}
            </Link>
          </div>
        </div>

        {/* Subtle footer credit / help */}
        <div style={{ textAlign: 'center', fontSize: '0.775rem', color: '#94a3b8', marginTop: '1.5rem' }}>
          Protected by CodeNova Enterprise Security • <Link to="/" style={{ color: '#94a3b8' }}>Terms</Link> & <Link to="/" style={{ color: '#94a3b8' }}>Privacy</Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
