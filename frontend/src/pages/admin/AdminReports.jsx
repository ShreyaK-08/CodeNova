import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import analyticsService from '../../services/analyticsService';
import {
  BarChart2,
  Users,
  Code,
  ClipboardList,
  Flag,
  FileCheck,
  LifeBuoy,
  MessageSquare,
  TrendingUp,
  Download,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Loader2,
  Star,
  Activity,
  Layers,
  Clock,
  HelpCircle,
  Calendar
} from 'lucide-react';

const AdminReports = () => {
  const { t } = useTranslation();

  const [range, setRange] = useState('ALL_TIME');
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchAnalytics(range);
  }, [range]);

  const fetchAnalytics = async (selectedRange) => {
    try {
      setLoading(true);
      setError('');
      const data = await analyticsService.getOverview(selectedRange);
      setAnalytics(data);
    } catch (err) {
      console.error('Failed to load analytics:', err);
      setError(err.response?.data?.message || t('analytics.loadError', 'Failed to load platform analytics.'));
    } finally {
      setLoading(false);
    }
  };

  const handleExportCsv = async () => {
    try {
      setExporting(true);
      const blobData = await analyticsService.exportCsv(range);
      const url = window.URL.createObjectURL(new Blob([blobData], { type: 'text/csv;charset=utf-8;' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `codenova_analytics_${range.toLowerCase()}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Failed to export CSV report:', err);
      alert('Failed to export report CSV. Please try again.');
    } finally {
      setExporting(false);
    }
  };

  const formatTimestamp = (ts) => {
    if (!ts) return '-';
    const d = new Date(ts);
    return d.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading && !analytics) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: '1rem' }}>
        <Loader2 className="animate-spin" size={36} color="var(--primary)" />
        <span style={{ color: 'var(--text-muted)' }}>{t('common.loading', 'Loading reports & analytics...')}</span>
      </div>
    );
  }

  const { overview, users, problems, assessments, contests, submissions, languageUsage, feedback, support, recentActivity } = analytics || {};

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
            {t('analytics.title', 'Reports & Analytics')}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
            {t('analytics.subtitle', 'Real-time aggregated platform metrics, candidate performance, submissions, and feedback.')}
          </p>
        </div>

        {/* Action Controls: Date Range Filter + Refresh + Export */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', background: 'var(--card-bg, #fff)', border: '1px solid var(--border-color)', borderRadius: '6px', padding: '0.35rem 0.65rem' }}>
            <Calendar size={15} color="var(--text-muted)" />
            <select
              value={range}
              onChange={(e) => setRange(e.target.value)}
              style={{ border: 'none', background: 'transparent', outline: 'none', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)', cursor: 'pointer' }}
            >
              <option value="ALL_TIME">All Time</option>
              <option value="LAST_30_DAYS">Last 30 Days</option>
              <option value="LAST_7_DAYS">Last 7 Days</option>
              <option value="TODAY">Today</option>
              <option value="THIS_YEAR">This Year</option>
            </select>
          </div>

          <button
            onClick={() => fetchAnalytics(range)}
            disabled={loading}
            className="btn btn-outline btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>{t('common.refresh', 'Refresh')}</span>
          </button>

          <button
            onClick={handleExportCsv}
            disabled={exporting}
            className="btn btn-primary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            {exporting ? <Loader2 size={14} className="animate-spin" /> : <Download size={14} />}
            <span>{t('analytics.exportReport', 'Export Report (CSV)')}</span>
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Tabs Navigation */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '1px solid var(--border-color)', marginBottom: '1.5rem', overflowX: 'auto', paddingBottom: '0.25rem' }}>
        {[
          { id: 'overview', label: t('analytics.tabOverview', 'Overview'), icon: Layers },
          { id: 'users', label: t('analytics.tabUsers', 'User Analytics'), icon: Users },
          { id: 'problems', label: t('analytics.tabProblems', 'Problems & Submissions'), icon: Code },
          { id: 'assessments', label: t('analytics.tabAssessments', 'Assessments & Contests'), icon: ClipboardList },
          { id: 'support', label: t('analytics.tabSupport', 'Support & Feedback'), icon: HelpCircle },
          { id: 'activity', label: t('analytics.tabActivity', 'Recent Activity'), icon: Activity },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                padding: '0.65rem 1rem',
                borderRadius: '6px',
                border: 'none',
                background: isActive ? 'var(--primary)' : 'transparent',
                color: isActive ? '#fff' : 'var(--text-muted)',
                fontWeight: isActive ? 700 : 500,
                fontSize: '0.9rem',
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              <Icon size={16} />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab 1: Overview */}
      {activeTab === 'overview' && (
        <div>
          {/* Overview Metric Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Total Users</span>
                <Users size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{overview?.totalUsers || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--success, #10b981)', marginTop: '0.25rem' }}>
                {overview?.activeUsers || 0} Active • {overview?.inactiveUsers || 0} Inactive
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Coding Problems</span>
                <Code size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{overview?.totalProblems || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                {problems?.easy || 0} Easy • {problems?.medium || 0} Med • {problems?.hard || 0} Hard
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Total Submissions</span>
                <FileCheck size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{overview?.totalSubmissions || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--success, #10b981)', marginTop: '0.25rem' }}>
                {problems?.acceptanceRate || 0}% Acceptance Rate
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Assessments</span>
                <ClipboardList size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{overview?.totalAssessments || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                {assessments?.totalAttempts || 0} Attempts • {assessments?.averageScore || 0} Avg Score
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Contests</span>
                <Flag size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{overview?.totalContests || 0}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                {contests?.totalParticipants || 0} Registered Participants
              </div>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Support Tickets</span>
                <LifeBuoy size={18} color="var(--primary)" />
              </div>
              <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>{support?.totalRequests || 0}</div>
              <div style={{ fontSize: '0.8rem', color: support?.openRequests > 0 ? 'var(--warning, #f59e0b)' : 'var(--success, #10b981)', marginTop: '0.25rem' }}>
                {support?.openRequests || 0} Open • {support?.resolvedRequests || 0} Resolved
              </div>
            </div>
          </div>

          {/* Highlights Grid: Submission Verdicts + Top Languages */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Submission Verdict Distribution */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <FileCheck size={18} color="var(--primary)" />
                <span>Submission Verdict Distribution</span>
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    <span style={{ color: 'var(--success, #10b981)' }}>Accepted</span>
                    <span>{submissions?.accepted || 0} ({submissions?.acceptedPct || 0}%)</span>
                  </div>
                  <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${submissions?.acceptedPct || 0}%`, height: '100%', background: 'var(--success, #10b981)' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    <span style={{ color: 'var(--danger, #ef4444)' }}>Wrong Answer</span>
                    <span>{submissions?.wrongAnswer || 0} ({submissions?.wrongAnswerPct || 0}%)</span>
                  </div>
                  <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${submissions?.wrongAnswerPct || 0}%`, height: '100%', background: 'var(--danger, #ef4444)' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    <span style={{ color: '#f59e0b' }}>Compilation Error</span>
                    <span>{submissions?.compilationError || 0} ({submissions?.compilationErrorPct || 0}%)</span>
                  </div>
                  <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${submissions?.compilationErrorPct || 0}%`, height: '100%', background: '#f59e0b' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    <span style={{ color: '#8b5cf6' }}>Runtime Error</span>
                    <span>{submissions?.runtimeError || 0} ({submissions?.runtimeErrorPct || 0}%)</span>
                  </div>
                  <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${submissions?.runtimeErrorPct || 0}%`, height: '100%', background: '#8b5cf6' }} />
                  </div>
                </div>

                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                    <span style={{ color: '#64748b' }}>Time Limit Exceeded</span>
                    <span>{submissions?.timeLimitExceeded || 0} ({submissions?.timeLimitExceededPct || 0}%)</span>
                  </div>
                  <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${submissions?.timeLimitExceededPct || 0}%`, height: '100%', background: '#64748b' }} />
                  </div>
                </div>
              </div>
            </div>

            {/* Language Usage Breakdown */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Code size={18} color="var(--primary)" />
                <span>Programming Language Usage</span>
              </h3>
              {(!languageUsage || languageUsage.length === 0) ? (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '2rem 0' }}>
                  No submission language data recorded yet.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                  {languageUsage.slice(0, 5).map((lang) => (
                    <div key={lang.language}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.25rem' }}>
                        <span>{lang.language}</span>
                        <span style={{ color: 'var(--text-muted)' }}>{lang.count} ({lang.percentage}%)</span>
                      </div>
                      <div style={{ width: '100%', height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${lang.percentage}%`, height: '100%', background: 'var(--primary)' }} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: User Analytics */}
      {activeTab === 'users' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Users size={18} color="var(--primary)" />
              <span>User Base Breakdown</span>
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Registered</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{users?.totalUsers || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Active Users</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--success, #10b981)' }}>{users?.activeUsers || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Inactive Users</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-muted)' }}>{users?.inactiveUsers || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Platform Admins</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)' }}>{users?.adminUsers || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Regular Users</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{users?.regularUsers || 0}</div>
              </div>
            </div>

            {/* Registration Trend */}
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
              User Registration Timeline (Monthly)
            </h4>
            {(!users?.registrationTrend || users.registrationTrend.length === 0) ? (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No registration timeline data in this range.</div>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '0.75rem' }}>
                {users.registrationTrend.map((item) => (
                  <div key={item.month} style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '6px', textAlign: 'center' }}>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>{item.month}</div>
                    <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--primary)' }}>+{item.count}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab 3: Problems & Submissions */}
      {activeTab === 'problems' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Code size={18} color="var(--primary)" />
              <span>Coding Problems Analytics</span>
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Problems</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{problems?.totalProblems || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--success, #10b981)', fontWeight: 600 }}>Easy Problems</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--success, #10b981)' }}>{problems?.easy || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: '#f59e0b', fontWeight: 600 }}>Medium Problems</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f59e0b' }}>{problems?.medium || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--danger, #ef4444)', fontWeight: 600 }}>Hard Problems</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--danger, #ef4444)' }}>{problems?.hard || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Overall Acceptance</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)' }}>{problems?.acceptanceRate || 0}%</div>
              </div>
            </div>

            {/* Language Usage Table */}
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
              All Programming Languages Breakdown
            </h4>
            <div className="table-container" style={{ overflowX: 'auto' }}>
              <table className="table" style={{ width: '100%' }}>
                <thead>
                  <tr>
                    <th>Language</th>
                    <th>Submissions</th>
                    <th>Distribution</th>
                  </tr>
                </thead>
                <tbody>
                  {(!languageUsage || languageUsage.length === 0) ? (
                    <tr>
                      <td colSpan="3" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No submissions found.</td>
                    </tr>
                  ) : (
                    languageUsage.map((lang) => (
                      <tr key={lang.language}>
                        <td style={{ fontWeight: 600 }}>{lang.language}</td>
                        <td>{lang.count}</td>
                        <td style={{ width: '50%' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <div style={{ flex: 1, height: '8px', background: 'var(--bg-subtle, #f1f5f9)', borderRadius: '4px', overflow: 'hidden' }}>
                              <div style={{ width: `${lang.percentage}%`, height: '100%', background: 'var(--primary)' }} />
                            </div>
                            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', minWidth: '45px' }}>{lang.percentage}%</span>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Tab 4: Assessments & Contests */}
      {activeTab === 'assessments' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ClipboardList size={18} color="var(--primary)" />
              <span>Assessment Performance & Attempt Metrics</span>
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Assessments</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{assessments?.totalAssessments || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Published / Draft</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--success, #10b981)' }}>{assessments?.published || 0} / {assessments?.draft || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Attempts</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{assessments?.totalAttempts || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Average Score</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)' }}>{assessments?.averageScore || 0} pts</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--danger, #ef4444)', fontWeight: 600 }}>Proctoring Violations</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--danger, #ef4444)' }}>{assessments?.totalViolations || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Hosted vs Platform</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{assessments?.userHosted || 0} / {assessments?.platformAdmin || 0}</div>
              </div>
            </div>
          </div>

          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Flag size={18} color="var(--primary)" />
              <span>Contest Participation Analytics</span>
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem' }}>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Contests</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{contests?.totalContests || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Upcoming / Ongoing / Ended</div>
                <div style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                  {contests?.upcoming || 0} / {contests?.running || 0} / {contests?.completed || 0}
                </div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Contest Registrations</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)' }}>{contests?.totalParticipants || 0}</div>
              </div>
              <div style={{ padding: '1rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Contest Attempts</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-primary)' }}>{contests?.totalSubmissions || 0}</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 5: Support & Feedback */}
      {activeTab === 'support' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '1.5rem' }}>
            {/* Support Requests */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <LifeBuoy size={18} color="var(--primary)" />
                <span>Support Tickets Analytics</span>
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem', marginBottom: '1.25rem' }}>
                <div style={{ padding: '0.85rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Total Tickets</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>{support?.totalRequests || 0}</div>
                </div>
                <div style={{ padding: '0.85rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '0.8rem', color: '#f59e0b', fontWeight: 600 }}>Open Tickets</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f59e0b' }}>{support?.openRequests || 0}</div>
                </div>
                <div style={{ padding: '0.85rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--success, #10b981)', fontWeight: 600 }}>Resolved Tickets</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--success, #10b981)' }}>{support?.resolvedRequests || 0}</div>
                </div>
                <div style={{ padding: '0.85rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Avg Resolution Time</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--primary)' }}>{support?.averageResolutionTimeHours || 0} hrs</div>
                </div>
              </div>
            </div>

            {/* Assessment Feedback & Reports */}
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <MessageSquare size={18} color="var(--primary)" />
                <span>Candidate Feedback & Question Reports</span>
              </h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem', marginBottom: '1.25rem' }}>
                <div style={{ padding: '0.75rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>Post-Feedbacks</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{feedback?.totalPostFeedbacks || 0}</div>
                </div>
                <div style={{ padding: '0.75rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.75rem', color: '#f59e0b', fontWeight: 600 }}>Avg Rating</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f59e0b', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.2rem' }}>
                    <Star size={16} fill="#f59e0b" />
                    <span>{feedback?.averageAssessmentRating || 5.0}</span>
                  </div>
                </div>
                <div style={{ padding: '0.75rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '8px', border: '1px solid var(--border-color)', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--danger, #ef4444)', fontWeight: 600 }}>Question Reports</div>
                  <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--danger, #ef4444)' }}>{feedback?.totalQuestionReports || 0}</div>
                </div>
              </div>

              <h4 style={{ fontSize: '0.85rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--text-muted)' }}>
                Most Reported Question Issue Categories
              </h4>
              {(!feedback?.mostReportedQuestionCategories || feedback.mostReportedQuestionCategories.length === 0) ? (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No question issues reported yet.</div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                  {feedback.mostReportedQuestionCategories.map((c) => (
                    <div key={c.category} style={{ display: 'flex', justifyContent: 'space-between', padding: '0.4rem 0.6rem', background: 'var(--bg-subtle, #f8fafc)', borderRadius: '4px', fontSize: '0.85rem' }}>
                      <span style={{ fontWeight: 600 }}>{c.category}</span>
                      <span className="badge badge-danger">{c.count}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 6: Recent Activity */}
      {activeTab === 'activity' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Activity size={18} color="var(--primary)" />
            <span>Recent Platform Activity Feed</span>
          </h3>

          {(!recentActivity || recentActivity.length === 0) ? (
            <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
              No recent platform activity found.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              {recentActivity.map((act, idx) => (
                <div
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.85rem 1rem',
                    borderRadius: '8px',
                    border: '1px solid var(--border-color)',
                    background: 'var(--bg-subtle, #f8fafc)'
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                    <div
                      style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: '50%',
                        background: act.type === 'SUBMISSION' ? '#eff6ff' : act.type === 'USER_REGISTER' ? '#f0fdf4' : '#fef3c7',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: act.type === 'SUBMISSION' ? 'var(--primary)' : act.type === 'USER_REGISTER' ? 'var(--success)' : '#f59e0b'
                      }}
                    >
                      {act.type === 'SUBMISSION' ? <FileCheck size={18} /> : act.type === 'USER_REGISTER' ? <Users size={18} /> : <LifeBuoy size={18} />}
                    </div>
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-primary)' }}>{act.title}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{act.detail}</div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.2rem' }}>
                    <span className={`badge ${act.status === 'ACCEPTED' || act.status === 'RESOLVED' || act.status === 'VERIFIED' ? 'badge-success' : 'badge-outline'}`}>
                      {act.status}
                    </span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                      {formatTimestamp(act.timestamp)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminReports;
