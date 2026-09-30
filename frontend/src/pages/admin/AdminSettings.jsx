import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import settingsService from '../../services/settingsService';
import emailMonitoringService from '../../services/emailMonitoringService';
import {
  Settings,
  Server,
  FileCheck,
  Flag,
  Bell,
  Activity,
  Cpu,
  Save,
  CheckCircle2,
  AlertCircle,
  Loader2,
  RefreshCw,
  HardDrive,
  Clock,
  Mail,
  Send,
  ShieldCheck,
  Search,
  Filter,
  Inbox,
  Check,
  XCircle,
  AlertTriangle
} from 'lucide-react';

const AdminSettings = () => {
  const { t } = useTranslation();

  const [activeTab, setActiveTab] = useState('platform'); // 'platform' | 'email_monitoring'
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [config, setConfig] = useState({
    platformName: 'CodeNova',
    platformType: 'Online Coding & Skill Assessment Platform',
    environment: 'Production',
    maxAssessmentDurationMinutes: 180,
    maxAssessmentAttemptsDefault: 3,
    proctoringEnabledDefault: true,
    programmingQuestionsEnabled: true,
    contestCreationEnabled: true,
    contestNotificationsEnabled: true,
    contestProctoringAvailable: true,
    emailNotificationsEnabled: true,
    contestAnnouncementNotificationsEnabled: true,
    hostVerificationNotificationsEnabled: true,
    maintenanceMode: false
  });

  const [systemStatus, setSystemStatus] = useState(null);
  const [loadingStatus, setLoadingStatus] = useState(false);

  // Email Monitoring State
  const [emailLogs, setEmailLogs] = useState([]);
  const [emailStats, setEmailStats] = useState(null);
  const [loadingEmailData, setLoadingEmailData] = useState(false);
  const [logFilterStatus, setLogFilterStatus] = useState('ALL');
  const [logSearchQuery, setLogSearchQuery] = useState('');

  // Safe SMTP Test Tool State
  const [testRecipient, setTestRecipient] = useState('');
  const [testNote, setTestNote] = useState('');
  const [testingEmail, setTestingEmail] = useState(false);
  const [testResult, setTestResult] = useState(null);

  useEffect(() => {
    fetchAdminSettings();
    fetchStatus();
    fetchEmailMonitoringData();
  }, []);

  const fetchAdminSettings = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await settingsService.getAdminSettings();
      setConfig(data);
    } catch (err) {
      console.error('Failed to load admin settings:', err);
      setError(err.response?.data?.message || t('adminSettings.loadError', 'Failed to load platform settings.'));
    } finally {
      setLoading(false);
    }
  };

  const fetchStatus = async () => {
    try {
      setLoadingStatus(true);
      const statusData = await settingsService.getSystemStatus();
      setSystemStatus(statusData);
    } catch (err) {
      console.warn('Failed to load JVM status:', err);
    } finally {
      setLoadingStatus(false);
    }
  };

  const fetchEmailMonitoringData = async () => {
    try {
      setLoadingEmailData(true);
      const [logs, stats] = await Promise.all([
        emailMonitoringService.getEmailLogs().catch(() => []),
        emailMonitoringService.getEmailStats().catch(() => null)
      ]);
      setEmailLogs(logs || []);
      setEmailStats(stats || null);
    } catch (err) {
      console.error('Failed to load email monitoring data:', err);
    } finally {
      setLoadingEmailData(false);
    }
  };

  const handleToggle = (key) => {
    setConfig((prev) => ({
      ...prev,
      [key]: !prev[key]
    }));
    setSuccess('');
  };

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setConfig((prev) => ({
      ...prev,
      [name]: type === 'number' ? parseInt(value, 10) || 0 : value
    }));
    setSuccess('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError('');
      setSuccess('');

      const updated = await settingsService.updateAdminSettings(config);
      setConfig(updated);
      setSuccess(t('adminSettings.savedSuccessfully', 'Platform settings saved successfully.'));
      setTimeout(() => setSuccess(''), 4000);
    } catch (err) {
      console.error('Failed to update platform settings:', err);
      setError(err.response?.data?.message || t('adminSettings.saveError', 'Failed to update platform settings.'));
    } finally {
      setSaving(false);
    }
  };

  const handleSendTestEmail = async (e) => {
    e.preventDefault();
    if (!testRecipient || !testRecipient.includes('@')) {
      alert('Please enter a valid recipient email address.');
      return;
    }

    try {
      setTestingEmail(true);
      setTestResult(null);
      const result = await emailMonitoringService.sendTestEmail({
        recipientEmail: testRecipient.trim(),
        note: testNote.trim()
      });
      setTestResult(result);
      fetchEmailMonitoringData();
    } catch (err) {
      setTestResult({
        status: 'FAILED',
        message: err.response?.data?.message || 'Failed to dispatch test email.'
      });
    } finally {
      setTestingEmail(false);
    }
  };

  const formatUptime = (seconds) => {
    if (!seconds) return '0m';
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    if (hrs > 0) return `${hrs}h ${mins}m ${secs}s`;
    return `${mins}m ${secs}s`;
  };

  const formatDateTime = (dt) => {
    if (!dt) return '—';
    try {
      const d = new Date(dt);
      return d.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    } catch {
      return String(dt);
    }
  };

  const getStatusBadge = (status) => {
    const st = (status || '').toUpperCase();
    if (st === 'SENT') {
      return (
        <span className="badge" style={{ backgroundColor: '#dcfce7', color: '#15803d', fontWeight: 700, padding: '4px 8px', borderRadius: '4px', fontSize: '11px', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
          <Check size={12} /> SENT
        </span>
      );
    }
    if (st === 'FAILED') {
      return (
        <span className="badge" style={{ backgroundColor: '#fee2e2', color: '#dc2626', fontWeight: 700, padding: '4px 8px', borderRadius: '4px', fontSize: '11px', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
          <XCircle size={12} /> FAILED
        </span>
      );
    }
    if (st === 'SKIPPED') {
      return (
        <span className="badge" style={{ backgroundColor: '#f1f5f9', color: '#475569', fontWeight: 700, padding: '4px 8px', borderRadius: '4px', fontSize: '11px', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
          SKIPPED
        </span>
      );
    }
    return (
      <span className="badge" style={{ backgroundColor: '#fef3c7', color: '#b45309', fontWeight: 700, padding: '4px 8px', borderRadius: '4px', fontSize: '11px', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
        <AlertTriangle size={12} /> NOT CONFIGURED
      </span>
    );
  };

  const filteredEmailLogs = emailLogs.filter((log) => {
    if (logFilterStatus !== 'ALL' && log.status !== logFilterStatus) return false;
    if (logSearchQuery.trim()) {
      const q = logSearchQuery.toLowerCase();
      const matchEmail = log.recipientEmail?.toLowerCase().includes(q);
      const matchType = log.notificationType?.toLowerCase().includes(q);
      const matchSub = log.subject?.toLowerCase().includes(q);
      return matchEmail || matchType || matchSub;
    }
    return true;
  });

  if (loading) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: '1rem' }}>
        <Loader2 className="animate-spin" size={36} color="var(--primary)" />
        <span style={{ color: 'var(--text-muted)' }}>{t('common.loading', 'Loading platform configuration...')}</span>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
            {t('adminSettings.title', 'Platform Settings & Email Hub')}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            {t('adminSettings.subtitle', 'Configure platform defaults, feature toggles, notifications, and monitor live email delivery.')}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            type="button"
            onClick={() => {
              fetchStatus();
              fetchEmailMonitoringData();
            }}
            disabled={loadingStatus || loadingEmailData}
            className="btn btn-outline btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <RefreshCw size={14} className={loadingStatus || loadingEmailData ? 'animate-spin' : ''} />
            <span>{t('adminSettings.refreshStatus', 'Refresh Hub')}</span>
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '1px solid var(--border-color)', marginBottom: '1.75rem' }}>
        <button
          type="button"
          onClick={() => setActiveTab('platform')}
          style={{
            padding: '0.75rem 1.25rem',
            fontWeight: 700,
            fontSize: '0.95rem',
            border: 'none',
            borderBottom: activeTab === 'platform' ? '3px solid var(--primary)' : '3px solid transparent',
            background: 'none',
            color: activeTab === 'platform' ? 'var(--primary)' : 'var(--text-muted)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Settings size={18} />
          Platform Settings & System Health
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('email_monitoring')}
          style={{
            padding: '0.75rem 1.25rem',
            fontWeight: 700,
            fontSize: '0.95rem',
            border: 'none',
            borderBottom: activeTab === 'email_monitoring' ? '3px solid var(--primary)' : '3px solid transparent',
            background: 'none',
            color: activeTab === 'email_monitoring' ? 'var(--primary)' : 'var(--text-muted)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Mail size={18} />
          Email Delivery Audit & Diagnostic Hub
          {emailStats?.totalSent > 0 && (
            <span style={{ backgroundColor: 'var(--primary-subtle, #ede9fe)', color: 'var(--primary)', fontSize: '11px', padding: '2px 8px', borderRadius: '999px', fontWeight: 800 }}>
              {emailStats.totalSent} Sent
            </span>
          )}
        </button>
      </div>

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

      {/* TAB 1: PLATFORM SETTINGS */}
      {activeTab === 'platform' && (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* A. Platform Information */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              <Server size={20} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('adminSettings.platformInfo', 'Platform Information')}
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                  {t('adminSettings.platformName', 'Platform Name')}
                </label>
                <input
                  type="text"
                  name="platformName"
                  value={config.platformName || ''}
                  onChange={handleChange}
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                  {t('adminSettings.platformType', 'Platform Type')}
                </label>
                <input
                  type="text"
                  name="platformType"
                  value={config.platformType || ''}
                  onChange={handleChange}
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                  {t('adminSettings.environment', 'Environment')}
                </label>
                <input
                  type="text"
                  name="environment"
                  value={config.environment || ''}
                  onChange={handleChange}
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
                />
              </div>
            </div>
          </div>

          {/* B. Assessment Defaults */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              <FileCheck size={20} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('adminSettings.assessmentDefaults', 'Assessment Defaults & Policies')}
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem', marginBottom: '1.25rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                  {t('adminSettings.maxDuration', 'Max Assessment Duration (Minutes)')}
                </label>
                <input
                  type="number"
                  name="maxAssessmentDurationMinutes"
                  value={config.maxAssessmentDurationMinutes || 180}
                  onChange={handleChange}
                  min="10"
                  max="600"
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.35rem' }}>
                  {t('adminSettings.maxAttempts', 'Default Max Attempts Allowed')}
                </label>
                <input
                  type="number"
                  name="maxAssessmentAttemptsDefault"
                  value={config.maxAssessmentAttemptsDefault || 3}
                  onChange={handleChange}
                  min="1"
                  max="10"
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid var(--border-color)' }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderTop: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                  {t('adminSettings.proctoringDefault', 'Proctoring Enabled by Default')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('adminSettings.proctoringDefaultDesc', 'Enforce webcam, fullscreen, and tab-switch monitoring when creating new assessments.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('proctoringEnabledDefault')}
                className={`btn btn-sm ${config.proctoringEnabledDefault ? 'btn-primary' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {config.proctoringEnabledDefault ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>
          </div>

          {/* C. Maintenance & System Health */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              <Activity size={20} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('adminSettings.systemHealth', 'Maintenance & System Health')}
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  <Activity size={14} />
                  <span>System Status</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--success, #10b981)' }}>
                  {systemStatus?.status || 'OPERATIONAL'}
                </div>
              </div>

              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  <Clock size={14} />
                  <span>Server Uptime</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                  {formatUptime(systemStatus?.uptimeSeconds)}
                </div>
              </div>

              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  <HardDrive size={14} />
                  <span>Memory (Used / Total)</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                  {systemStatus?.usedMemoryMb || 0} MB / {systemStatus?.totalMemoryMb || 0} MB
                </div>
              </div>

              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  <Cpu size={14} />
                  <span>Java & CPU Cores</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                  Java {systemStatus?.javaVersion?.split('.')[0] || '17'} ({systemStatus?.availableProcessors || 4} cores)
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0', borderTop: '1px solid var(--border-color)' }}>
              <div>
                <div style={{ fontWeight: 600, color: config.maintenanceMode ? 'var(--danger)' : 'var(--text-primary)' }}>
                  {t('adminSettings.maintenanceMode', 'Maintenance Mode')}
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  {t('adminSettings.maintenanceModeDesc', 'Show maintenance banner and prevent new code submissions or assessments.')}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleToggle('maintenanceMode')}
                className={`btn btn-sm ${config.maintenanceMode ? 'btn-danger' : 'btn-outline'}`}
                style={{ minWidth: '80px' }}
              >
                {config.maintenanceMode ? t('common.on', 'ON') : t('common.off', 'OFF')}
              </button>
            </div>
          </div>

          {/* Save Action */}
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
                  <span>{t('adminSettings.saveSettings', 'Save Platform Settings')}</span>
                </>
              )}
            </button>
          </div>
        </form>
      )}

      {/* TAB 2: EMAIL MONITORING & TEST HUB */}
      {activeTab === 'email_monitoring' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.75rem' }}>
          {/* KPI Stats Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #10b981' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>Total Delivered (SENT)</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 900, color: '#10b981', marginTop: '0.35rem' }}>
                {emailStats?.totalSent ?? 0}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Audit verified dispatches</div>
            </div>

            <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #6366f1' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>Success Rate</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 900, color: '#6366f1', marginTop: '0.35rem' }}>
                {emailStats?.successRate ? emailStats.successRate.toFixed(1) : '100.0'}%
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Delivery reliability ratio</div>
            </div>

            <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #ef4444' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>Failed Dispatches</div>
              <div style={{ fontSize: '1.75rem', fontWeight: 900, color: emailStats?.totalFailed > 0 ? '#ef4444' : 'var(--text-primary)', marginTop: '0.35rem' }}>
                {emailStats?.totalFailed ?? 0}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>Requires SMTP inspection</div>
            </div>

            <div className="card" style={{ padding: '1.25rem', borderLeft: '4px solid #f59e0b' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>SMTP Pipeline</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 800, color: emailStats?.smtpConfigured ? '#10b981' : '#f59e0b', marginTop: '0.5rem' }}>
                {emailStats?.smtpConfigured ? '✓ LIVE (Active)' : 'NOT CONFIGURED'}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                Host: {emailStats?.mailHost || 'smtp.gmail.com'}
              </div>
            </div>
          </div>

          {/* Interactive Safe SMTP Test Tool */}
          <div className="card" style={{ padding: '1.5rem', backgroundColor: '#faf5ff', border: '1px solid #e9d5ff' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1rem' }}>
              <Send size={20} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 800, margin: 0, color: '#581c87' }}>
                Safe SMTP Connectivity & Diagnostics Test Tool
              </h2>
            </div>
            <p style={{ fontSize: '0.9rem', color: '#6b21a8', marginTop: 0, marginBottom: '1.25rem' }}>
              Dispatch a real-time diagnostic test message to verify Gmail SMTP transport, STARTTLS handshake, and authentication.
            </p>

            <form onSubmit={handleSendTestEmail} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem', alignItems: 'flex-end' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#581c87', marginBottom: '0.35rem' }}>
                  Destination Email Address *
                </label>
                <input
                  type="email"
                  required
                  placeholder="e.g. your-email@gmail.com"
                  value={testRecipient}
                  onChange={(e) => setTestRecipient(e.target.value)}
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid #d8b4fe', backgroundColor: '#ffffff' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 700, color: '#581c87', marginBottom: '0.35rem' }}>
                  Diagnostic Note (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. Verification of CodeNova Production Mailer"
                  value={testNote}
                  onChange={(e) => setTestNote(e.target.value)}
                  className="form-control"
                  style={{ width: '100%', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid #d8b4fe', backgroundColor: '#ffffff' }}
                />
              </div>

              <div>
                <button
                  type="submit"
                  disabled={testingEmail}
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.7rem 1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', fontWeight: 700 }}
                >
                  {testingEmail ? (
                    <>
                      <Loader2 className="animate-spin" size={16} />
                      <span>Sending Test Email...</span>
                    </>
                  ) : (
                    <>
                      <Send size={16} />
                      <span>Send Diagnostic Email</span>
                    </>
                  )}
                </button>
              </div>
            </form>

            {testResult && (
              <div
                style={{
                  marginTop: '1.25rem',
                  padding: '1rem',
                  borderRadius: '8px',
                  backgroundColor: testResult.sent ? '#f0fdf4' : '#fef2f2',
                  border: `1px solid ${testResult.sent ? '#bbf7d0' : '#fecaca'}`,
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.75rem'
                }}
              >
                {testResult.sent ? <CheckCircle2 size={20} color="#16a34a" /> : <AlertCircle size={20} color="#dc2626" />}
                <div>
                  <div style={{ fontWeight: 800, color: testResult.sent ? '#15803d' : '#991b1b' }}>
                    {testResult.sent ? '✓ Email Dispatched Successfully!' : 'Delivery Attempt Failed'}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: testResult.sent ? '#166534' : '#b91c1c', marginTop: '0.2rem' }}>
                    {testResult.message}
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Delivery Audit Log Viewer */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <ShieldCheck size={20} color="var(--primary)" />
                <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                  Real-time Outbound Notification Audit Log
                </h2>
              </div>

              {/* Filters */}
              <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
                <div style={{ position: 'relative' }}>
                  <Search size={14} style={{ position: 'absolute', left: '10px', top: '10px', color: 'var(--text-muted)' }} />
                  <input
                    type="text"
                    placeholder="Search recipient / type..."
                    value={logSearchQuery}
                    onChange={(e) => setLogSearchQuery(e.target.value)}
                    style={{ padding: '0.45rem 0.75rem 0.45rem 2rem', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '0.85rem' }}
                  />
                </div>

                <select
                  value={logFilterStatus}
                  onChange={(e) => setLogFilterStatus(e.target.value)}
                  style={{ padding: '0.45rem 0.75rem', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '0.85rem', fontWeight: 600 }}
                >
                  <option value="ALL">All Statuses</option>
                  <option value="SENT">SENT</option>
                  <option value="FAILED">FAILED</option>
                  <option value="SKIPPED">SKIPPED</option>
                  <option value="NOT_CONFIGURED">NOT CONFIGURED</option>
                </select>
              </div>
            </div>

            {loadingEmailData ? (
              <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '3rem 0', gap: '0.75rem' }}>
                <Loader2 className="animate-spin" size={24} color="var(--primary)" />
                <span style={{ color: 'var(--text-muted)' }}>Loading notification audit records...</span>
              </div>
            ) : filteredEmailLogs.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
                <Inbox size={36} style={{ margin: '0 auto 0.75rem auto', opacity: 0.5 }} />
                <p style={{ margin: 0, fontWeight: 600 }}>No notification audit logs found.</p>
                <p style={{ fontSize: '0.85rem', margin: '0.25rem 0 0 0' }}>Trigger an assessment submission, contest registration, or run a diagnostic test above.</p>
              </div>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table className="table" style={{ width: '100%', fontSize: '0.85rem', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border-color)', textAlign: 'left', color: 'var(--text-muted)' }}>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Timestamp</th>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Status</th>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Notification Type</th>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Recipient Email</th>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Subject Line</th>
                      <th style={{ padding: '0.75rem 0.5rem' }}>Error / Outcome</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredEmailLogs.map((log) => (
                      <tr key={log.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ padding: '0.75rem 0.5rem', whiteSpace: 'nowrap', color: 'var(--text-muted)' }}>
                          {formatDateTime(log.createdAt)}
                        </td>
                        <td style={{ padding: '0.75rem 0.5rem' }}>
                          {getStatusBadge(log.status)}
                        </td>
                        <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                          {log.notificationType || 'UNKNOWN'}
                        </td>
                        <td style={{ padding: '0.75rem 0.5rem', fontFamily: 'monospace', color: 'var(--primary)' }}>
                          {log.recipientEmail}
                        </td>
                        <td style={{ padding: '0.75rem 0.5rem', maxWidth: '220px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {log.subject || '—'}
                        </td>
                        <td style={{ padding: '0.75rem 0.5rem', color: log.errorMessage ? '#dc2626' : 'var(--text-muted)', fontSize: '0.8rem', maxWidth: '240px' }}>
                          {log.errorMessage || (log.status === 'SENT' ? 'Delivered successfully' : '—')}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminSettings;
