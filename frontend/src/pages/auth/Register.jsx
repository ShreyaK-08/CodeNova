import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import authService from '../../services/authService';
import AuthVisualPanel from '../../components/auth/AuthVisualPanel';
import LanguageSelector from '../../components/LanguageSelector';
import Logo from '../../components/Logo';
import {
  AlertCircle,
  CheckCircle,
  Mail,
  ArrowRight,
  ArrowLeft,
  RefreshCw,
  Eye,
  EyeOff
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

const getPasswordStrength = (pwd) => {
  if (!pwd) return { score: 0, label: '', color: 'transparent' };
  let score = 0;
  if (pwd.length >= 6) score += 1;
  if (pwd.length >= 10) score += 1;
  if (/[A-Z]/.test(pwd)) score += 1;
  if (/[0-9]/.test(pwd)) score += 1;
  if (/[^A-Za-z0-9]/.test(pwd)) score += 1;

  if (score <= 2) return { score: 1, label: 'Weak', color: '#ef4444' };
  if (score === 3) return { score: 2, label: 'Fair', color: '#f59e0b' };
  if (score === 4) return { score: 3, label: 'Good', color: '#3b82f6' };
  return { score: 4, label: 'Strong', color: '#10b981' };
};

const Register = () => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  // Email verification required state
  const [verificationPending, setVerificationPending] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState('');
  const [registeredMessage, setRegisteredMessage] = useState('');
  const [smtpConfigured, setSmtpConfigured] = useState(true);

  const [resending, setResending] = useState(false);
  const [resendStatus, setResendStatus] = useState('');

  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const strength = getPasswordStrength(formData.password);

  const handleGoogleSignup = async () => {
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
    if (!registeredEmail) return;
    setResending(true);
    setResendStatus('');
    try {
      const res = await authService.resendVerification(registeredEmail);
      setResendStatus(res.message || 'A fresh verification link has been sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend verification email.');
    } finally {
      setResending(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError(t('auth.passwordMismatch', 'Passwords do not match'));
      return;
    }

    setLoading(true);
    try {
      const data = await authService.register(formData);
      setRegisteredEmail(formData.email);
      setRegisteredMessage(data.message || 'Please check your email to verify your account.');
      setSmtpConfigured(data.smtpConfigured !== false);
      setVerificationPending(true);
    } catch (err) {
      setError(err.response?.data?.message || t('auth.registerFailed', 'Registration failed. Please check your details.'));
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
          padding: 2rem 3.5rem;
          min-height: 100vh;
          background: #ffffff;
          position: relative;
        }

        .auth-top-actions {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
          margin-bottom: 1rem;
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
          padding: 0.5rem 0;
        }

        .auth-header-block {
          margin-bottom: 1.25rem;
        }

        .auth-title {
          font-size: 1.85rem;
          font-weight: 800;
          color: #0f172a;
          line-height: 1.2;
          margin: 0 0 0.35rem;
          letter-spacing: -0.02em;
        }

        .auth-subtitle {
          font-size: 0.9rem;
          color: #64748b;
          margin: 0;
          line-height: 1.45;
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
          padding: 0.55rem 1rem;
          font-size: 0.9rem;
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
          font-size: 0.8rem;
          font-weight: 500;
          margin: 1rem 0;
        }
        .auth-divider::before,
        .auth-divider::after {
          content: '';
          flex: 1;
          height: 1px;
          background: #e2e8f0;
        }
        .auth-divider span {
          padding: 0 0.75rem;
        }

        .auth-field-group {
          margin-bottom: 0.85rem;
        }

        .auth-field-label {
          display: block;
          font-size: 0.825rem;
          font-weight: 600;
          color: #1e293b;
          margin-bottom: 0.25rem;
        }

        .auth-input-wrapper {
          position: relative;
          display: flex;
          align-items: center;
        }

        .auth-input-field {
          width: 100%;
          height: 38px;
          padding: 0.45rem 0.75rem;
          font-size: 0.875rem;
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

        .strength-bar-track {
          display: flex;
          gap: 4px;
          margin-top: 4px;
          height: 3px;
        }
        .strength-bar-seg {
          flex: 1;
          height: 100%;
          border-radius: 2px;
          background-color: #e2e8f0;
          transition: background-color 0.2s ease;
        }

        .auth-submit-btn {
          width: 100%;
          height: 42px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
          color: #ffffff;
          border: none;
          border-radius: 8px;
          font-size: 0.925rem;
          font-weight: 700;
          cursor: pointer;
          margin-top: 0.35rem;
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
          margin-top: 1.15rem;
          text-align: center;
          font-size: 0.875rem;
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

        {verificationPending ? (
          <div className="auth-form-wrapper" style={{ textAlign: 'center' }}>
            <div style={{
              display: 'inline-flex',
              padding: '1rem',
              borderRadius: '50%',
              backgroundColor: 'rgba(124, 58, 237, 0.1)',
              color: '#7c3aed',
              marginBottom: '1rem',
            }}>
              <Mail size={44} />
            </div>

            <h2 className="auth-title" style={{ fontSize: '1.65rem' }}>Verify Your Email</h2>
            <p className="auth-subtitle" style={{ marginBottom: '1.5rem' }}>{registeredMessage}</p>

            {!smtpConfigured && (
              <div style={{
                backgroundColor: 'rgba(234, 179, 8, 0.1)',
                border: '1px solid rgba(234, 179, 8, 0.3)',
                borderRadius: '8px',
                padding: '0.75rem 1rem',
                marginBottom: '1.25rem',
                fontSize: '0.85rem',
                textAlign: 'left',
                color: '#64748b'
              }}>
                <strong style={{ color: '#d97706' }}>SMTP Notice:</strong> Email delivery is not configured on this server. Configure <code>MAIL_HOST</code> and <code>MAIL_PASSWORD</code> to enable instant verification links.
              </div>
            )}

            {resendStatus && (
              <div className="alert alert-success" style={{ marginBottom: '1rem', textAlign: 'left', borderRadius: '8px' }}>
                <CheckCircle size={18} />
                <span>{resendStatus}</span>
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '1.25rem' }}>
              <Link to="/login" className="auth-submit-btn" style={{ textDecoration: 'none' }}>
                <span>Go to Sign In</span>
                <ArrowRight size={18} style={{ marginLeft: '0.5rem' }} />
              </Link>

              <button
                type="button"
                onClick={handleResend}
                disabled={resending}
                className="btn btn-secondary"
                style={{ height: '42px', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'center', borderRadius: '8px' }}
              >
                <RefreshCw size={16} className={resending ? 'animate-spin' : ''} />
                <span>{resending ? 'Sending...' : 'Resend Verification Link'}</span>
              </button>
            </div>
          </div>
        ) : (
          <div className="auth-form-wrapper">
            <div className="auth-header-block">
              <h2 className="auth-title">Create Your Account</h2>
              <p className="auth-subtitle">Join CodeNova and start your coding journey.</p>
            </div>

            {error && (
              <div className="alert alert-error" style={{ marginBottom: '1rem', padding: '0.65rem 0.85rem', borderRadius: '8px', fontSize: '0.85rem' }}>
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            {/* Google OAuth Register Button */}
            <button
              type="button"
              onClick={handleGoogleSignup}
              disabled={googleLoading}
              className="auth-google-btn"
            >
              <GoogleIcon />
              <span>{googleLoading ? 'Connecting to Google...' : 'Sign up with Google'}</span>
            </button>

            <div className="auth-divider">
              <span>or register with email</span>
            </div>

            <form onSubmit={handleSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '0.85rem' }}>
                <div>
                  <label className="auth-field-label">{t('profile.fullName', 'Full Name')}</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    className="auth-input-field"
                    placeholder="Alex Johnson"
                  />
                </div>

                <div>
                  <label className="auth-field-label">{t('profile.username', 'Username')}</label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                    className="auth-input-field"
                    placeholder="alexj"
                  />
                </div>
              </div>

              <div className="auth-field-group">
                <label className="auth-field-label">{t('profile.email', 'Email Address')}</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="auth-input-field"
                  placeholder="alex@example.com"
                />
              </div>

              <div className="auth-field-group">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                  <label className="auth-field-label" style={{ margin: 0 }}>{t('auth.password', 'Password')}</label>
                  {formData.password && (
                    <span style={{ fontSize: '0.725rem', fontWeight: 700, color: strength.color }}>
                      {strength.label}
                    </span>
                  )}
                </div>
                <div className="auth-input-wrapper">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    minLength={6}
                    className="auth-input-field"
                    placeholder="Min 6 characters"
                    style={{ paddingRight: '2.5rem' }}
                  />
                  <button
                    type="button"
                    className="auth-eye-toggle"
                    onClick={() => setShowPassword(!showPassword)}
                    tabIndex={-1}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
                {formData.password && (
                  <div className="strength-bar-track">
                    <div className="strength-bar-seg" style={{ backgroundColor: strength.score >= 1 ? strength.color : undefined }} />
                    <div className="strength-bar-seg" style={{ backgroundColor: strength.score >= 2 ? strength.color : undefined }} />
                    <div className="strength-bar-seg" style={{ backgroundColor: strength.score >= 3 ? strength.color : undefined }} />
                    <div className="strength-bar-seg" style={{ backgroundColor: strength.score >= 4 ? strength.color : undefined }} />
                  </div>
                )}
              </div>

              <div className="auth-field-group">
                <label className="auth-field-label">{t('auth.confirmPassword', 'Confirm Password')}</label>
                <div className="auth-input-wrapper">
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                    className="auth-input-field"
                    placeholder="Re-enter password"
                    style={{ paddingRight: '2.5rem' }}
                  />
                  <button
                    type="button"
                    className="auth-eye-toggle"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    tabIndex={-1}
                    aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                  >
                    {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>

              <button
                type="submit"
                className="auth-submit-btn"
                disabled={loading}
              >
                {loading ? t('auth.registering', 'Creating Account...') : t('auth.createAccount', 'Create Account')}
              </button>
            </form>

            <div className="auth-footer-nav">
              {t('auth.alreadyHaveAccount', 'Already have an account?')}{' '}
              <Link to="/login" style={{ fontWeight: 700, color: '#7c3aed', textDecoration: 'none' }}>
                {t('auth.logIn', 'Sign in')}
              </Link>
            </div>
          </div>
        )}

        {/* Subtle footer credit / help */}
        <div style={{ textAlign: 'center', fontSize: '0.775rem', color: '#94a3b8', marginTop: '1rem' }}>
          By creating an account, you agree to CodeNova's <Link to="/" style={{ color: '#94a3b8' }}>Terms of Service</Link> & <Link to="/" style={{ color: '#94a3b8' }}>Privacy Policy</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
