import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import settingsService from '../../services/settingsService';
import { LANGUAGES } from '../../i18n/languages';
import { changeAppLanguage } from '../../i18n/i18n';
import {
  User,
  Mail,
  Shield,
  Globe,
  Bell,
  Sun,
  Eye,
  Save,
  CheckCircle2,
  AlertCircle,
  Loader2
} from 'lucide-react';

const Settings = () => {
  const { t, i18n } = useTranslation();
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [settings, setSettings] = useState({
    username: '',
    email: '',
    role: '',
    accountStatus: 'Active',
    language: i18n.language || 'en',
    emailNotifications: true,
    contestNotifications: true,
    assessmentNotifications: true,
    platformUpdates: true,
    theme: 'LIGHT',
    showProfile: true,
    showLeaderboard: true
  });

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await settingsService.getUserSettings();
      setSettings(data);
      if (data.language && data.language !== i18n.language) {
        changeAppLanguage(data.language);
      }
    } catch (err) {
      console.error('Failed to load user settings:', err);
      setError(t('settings.loadError', 'Failed to load settings. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = (key) => {
    setSettings((prev) => ({
      ...prev,
      [key]: !prev[key]
    }));
    setSuccess('');
  };

  const handleLanguageChange = (e) => {
    const newLang = e.target.value;
    setSettings((prev) => ({ ...prev, language: newLang }));
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      setSuccess('');

      const updated = await settingsService.updateUserSettings({
        language: settings.language,
        emailNotifications: settings.emailNotifications,
        contestNotifications: settings.contestNotifications,
        assessmentNotifications: settings.assessmentNotifications,
        platformUpdates: settings.platformUpdates,
        theme: 'LIGHT',
        showProfile: settings.showProfile,
        showLeaderboard: settings.showLeaderboard
      });

      setSettings(updated);

      // Apply language change globally
      if (updated.language) {
        await changeAppLanguage(updated.language);
      }

      setSuccess(t('settings.savedSuccessfully', 'Settings saved successfully.'));
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      console.error('Failed to save settings:', err);
      setError(err.response?.data?.message || t('settings.saveError', 'Failed to save settings. Please try again.'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: '1rem' }}>
        <Loader2 className="animate-spin" size={36} color="var(--primary)" />
        <span style={{ color: 'var(--text-muted)' }}>{t('common.loading', 'Loading settings...')}</span>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
          {t('settings.title', 'Account Settings')}
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          {t('settings.subtitle', 'Manage your account preferences, notification settings, language, and privacy.')}
        </p>
      </div>

      {/* Notifications / Alerts */}
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <CheckCircle2 size={20} />
          <span>{success}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {/* A. Account Preferences */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
            <User size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
              {t('settings.accountPreferences', 'Account Preferences')}
            </h2>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                {t('settings.username', 'Username')}
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.65rem 0.85rem', background: 'var(--bg-subtle, #f8fafc)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)', fontWeight: 600 }}>
                <User size={16} color="var(--text-muted)" />
                <span>{settings.username || user?.username || '-'}</span>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                {t('settings.email', 'Email Address')}
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.65rem 0.85rem', background: 'var(--bg-subtle, #f8fafc)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)' }}>
                <Mail size={16} color="var(--text-muted)" />
                <span>{settings.email || user?.email || '-'}</span>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                {t('settings.role', 'Account Role')}
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.65rem 0.85rem', background: 'var(--bg-subtle, #f8fafc)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)' }}>
                <Shield size={16} color="var(--text-muted)" />
                <span className="badge badge-primary">{settings.role || user?.role || 'ROLE_USER'}</span>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                {t('settings.accountStatus', 'Account Status')}
              </label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.65rem 0.85rem', background: 'var(--bg-subtle, #f8fafc)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--success, #10b981)', fontWeight: 600 }}>
                <CheckCircle2 size={16} />
                <span>{settings.accountStatus || 'Active'}</span>
              </div>
            </div>
          </div>
        </div>

        {/* B. Language */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
            <Globe size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
              {t('settings.language', 'Language')}
            </h2>
          </div>

          <div style={{ maxWidth: '400px' }}>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
              {t('settings.selectLanguage', 'Preferred Interface Language')}
            </label>
            <select
              value={settings.language || 'en'}
              onChange={handleLanguageChange}
              className="form-control"
              style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '0.95rem' }}
            >
              {LANGUAGES.map((lang) => (
                <option key={lang.code} value={lang.code}>
                  {lang.name} ({lang.nativeName})
                </option>
              ))}
            </select>
            <span style={{ display: 'block', marginTop: '0.4rem', fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
              {t('settings.languageHelp', 'Your preferred language persists across page refreshes and active sessions.')}
            </span>
          </div>
        </div>

        {/* C. Notification Preferences */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
            <Bell size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
              {t('settings.notificationPreferences', 'Notification Preferences')}
            </h2>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.emailNotifications', 'Email Notifications')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.emailNotificationsDesc', 'Receive important account, verification, and performance alerts via email.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('emailNotifications')}
                className={`btn btn-sm ${settings.emailNotifications ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.emailNotifications ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.contestNotifications', 'Contest Notifications')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.contestNotificationsDesc', 'Receive reminders when registered contests are starting or results are published.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('contestNotifications')}
                className={`btn btn-sm ${settings.contestNotifications ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.contestNotifications ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.assessmentNotifications', 'Assessment Notifications')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.assessmentNotificationsDesc', 'Get notified about invitation links, attempt deadlines, and score releases.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('assessmentNotifications')}
                className={`btn btn-sm ${settings.assessmentNotifications ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.assessmentNotifications ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.platformUpdates', 'Platform Updates')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.platformUpdatesDesc', 'Receive occasional updates about new coding problems and feature releases.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('platformUpdates')}
                className={`btn btn-sm ${settings.platformUpdates ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.platformUpdates ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>
          </div>
        </div>

        {/* D. Appearance */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
            <Sun size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
              {t('settings.appearance', 'Appearance')}
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                padding: '0.75rem 1.25rem',
                borderRadius: '8px',
                border: '2px solid var(--primary)',
                background: 'var(--primary-subtle, #eff6ff)',
                color: 'var(--primary)',
                fontWeight: 700
              }}
            >
              <Sun size={18} />
              <span>● {t('settings.lightTheme', 'Light Mode')}</span>
              <CheckCircle2 size={16} />
            </div>
          </div>
          <span style={{ display: 'block', marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
            {t('settings.appearanceHelp', 'CodeNova uses an optimized light-mode theme for clean coding and assessments.')}
          </span>
        </div>

        {/* E. Privacy */}
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
            <Eye size={20} color="var(--primary)" />
            <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
              {t('settings.privacy', 'Privacy')}
            </h2>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderBottom: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.showProfile', 'Show profile information to other users')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.showProfileDesc', 'Allow peer candidates to view your public profile, badges, and solved problems.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('showProfile')}
                className={`btn btn-sm ${settings.showProfile ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.showProfile ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('settings.showLeaderboard', 'Show my leaderboard profile')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('settings.showLeaderboardDesc', 'Display your username and score ranking on global and contest leaderboards.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('showLeaderboard')}
                className={`btn btn-sm ${settings.showLeaderboard ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {settings.showLeaderboard ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>
          </div>
        </div>

        {/* F. Save Action */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '0.5rem' }}>
          <button
            type="submit"
            disabled={saving}
            className="btn btn-primary"
            style={{ padding: '0.75rem 2rem', display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '1rem' }}
          >
            {saving ? (
              <>
                <Loader2 className="animate-spin" size={18} />
                <span>{t('common.saving', 'Saving...')}</span>
              </>
            ) : (
              <>
                <Save size={18} />
                <span>{t('settings.saveSettings', 'Save Settings')}</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default Settings;
