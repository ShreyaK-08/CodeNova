import React, { useState, useEffect, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import adminService from '../../services/adminService';
import { isAdminRole, getRoleLabel } from '../../utils/roleUtils';
import { Users, Code, FileCheck, CheckCircle2, PlusCircle, AlertCircle, Activity, Flag } from 'lucide-react';
import { Link } from 'react-router-dom';

const AdminDashboard = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        setLoading(true);
        const [statsData, submissionsData] = await Promise.all([
          adminService.getDashboardStats(),
          adminService.getAllSubmissions(),
        ]);
        setStats(statsData);
        setSubmissions(submissionsData || []);
      } catch (err) {
        console.error('Error loading admin stats:', err);
        setError('Failed to load administrator statistics.');
      } finally {
        setLoading(false);
      }
    };

    fetchAdminData();
  }, []);

  const languageUsage = useMemo(() => {
    const counts = {};
    submissions.forEach((s) => {
      const lang = s.language || 'Unknown';
      counts[lang] = (counts[lang] || 0) + 1;
    });
    const total = submissions.length || 1;
    return Object.entries(counts)
      .map(([language, count]) => ({ language, count, pct: Math.round((count / total) * 100) }))
      .sort((a, b) => b.count - a.count);
  }, [submissions]);

  const statusBreakdown = useMemo(() => {
    const counts = {};
    submissions.forEach((s) => {
      counts[s.status] = (counts[s.status] || 0) + 1;
    });
    return counts;
  }, [submissions]);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{t('admin.dashboardOverview', 'Dashboard Overview')}</h1>
          <p style={{ color: 'var(--text-muted)' }}>{t('admin.dashboardSubtitle', 'Platform management, user accounts, and submission activity.')}</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/admin/problems" className="btn btn-primary btn-sm">
            <PlusCircle size={16} />
            <span>{t('admin.manageProblems', 'Manage Problems')}</span>
          </Link>
          <Link to="/admin/users" className="btn btn-outline btn-sm">
            <Users size={16} />
            <span>{t('admin.manageUsers', 'Manage Users')}</span>
          </Link>
          <Link to="/admin/contests" className="btn btn-outline btn-sm">
            <Flag size={16} />
            <span>{t('admin.manageContests', 'Manage Contests')}</span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Stats Grid */}
      <div className="grid-cols-4" style={{ marginBottom: '1.5rem' }}>
        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('admin.totalUsers', 'Total Users')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(59,130,246,0.12)' }}>
              <Users size={18} color="#3b82f6" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.totalUsers || 0}</div>
          <span className="stat-caption">{loading ? '-' : stats?.activeUsers || 0} {t('admin.activeUsers', 'active users')}</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('admin.totalProblems', 'Total Problems')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(16,185,129,0.12)' }}>
              <Code size={18} color="#10b981" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.totalProblems || 0}</div>
          <span className="stat-caption">In active bank</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('admin.totalSubmissions', 'Total Submissions')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(245,158,11,0.12)' }}>
              <FileCheck size={18} color="#f59e0b" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.totalSubmissions || 0}</div>
          <span className="stat-caption">{stats?.todaySubmissions || 0} submitted today</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('assessments.pass', 'Accepted Solutions')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(139,92,246,0.12)' }}>
              <CheckCircle2 size={18} color="var(--primary)" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.acceptedSolutions || 0}</div>
          <span className="stat-caption">{stats?.overallAcceptanceRate || 0}% overall acceptance</span>
        </div>
      </div>

      <div className="grid-cols-2" style={{ marginBottom: '1.5rem' }}>
        {/* Submission Statistics */}
        <div className="card">
          <div className="panel-header">
            <span className="panel-title">{t('dashboard.codingActivity', 'Submission Statistics')}</span>
            <Activity size={16} color="var(--text-muted)" />
          </div>
          {submissions.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
              {Object.entries(statusBreakdown)
                .sort((a, b) => b[1] - a[1])
                .map(([status, count]) => {
                  const pct = Math.round((count / submissions.length) * 100);
                  return (
                    <div key={status}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.3rem' }}>
                        <span style={{ fontWeight: 600 }}>{status}</span>
                        <span style={{ color: 'var(--text-muted)' }}>{count} ({pct}%)</span>
                      </div>
                      <div style={{ width: '100%', height: '8px', background: 'var(--bg-main)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div
                          style={{
                            width: `${pct}%`,
                            height: '100%',
                            borderRadius: '4px',
                            background: status === 'ACCEPTED' ? '#34d399' : '#f87171',
                          }}
                        />
                      </div>
                    </div>
                  );
                })}
            </div>
          ) : (
            <div className="empty-state">
              <FileCheck size={32} />
              <p>{t('dashboard.noSubmissionsRecorded', 'No submissions recorded yet.')}</p>
            </div>
          )}
        </div>

        {/* Language Usage */}
        <div className="card">
          <div className="panel-header">
            <span className="panel-title">{t('dashboard.languageUsage', 'Language Usage')}</span>
          </div>
          {languageUsage.length > 0 ? (
            <div>
              {languageUsage.map((l) => (
                <div className="lang-usage-row" key={l.language}>
                  <span className="lang-usage-name">{l.language}</span>
                  <div className="lang-usage-track">
                    <div className="lang-usage-fill" style={{ width: `${l.pct}%` }} />
                  </div>
                  <span className="lang-usage-count">{l.count}</span>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <Code size={32} />
              <p>{t('dashboard.noSubmissionsLanguage', 'No submissions yet to show language usage.')}</p>
            </div>
          )}
        </div>
      </div>

      <div className="grid-cols-2">
        {/* Recent Registered Users */}
        <div className="card">
          <div className="panel-header">
            <span className="panel-title">{t('admin.userManagement', 'User Registrations')}</span>
            <Link to="/admin/users" style={{ fontSize: '0.85rem' }}>{t('common.viewAll', 'View All')}</Link>
          </div>

          {stats?.recentRegistrations && stats.recentRegistrations.length > 0 ? (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>{t('profile.fullName', 'Name')}</th>
                    <th>{t('profile.username', 'Username')}</th>
                    <th>{t('admin.role', 'Role')}</th>
                    <th>{t('admin.joined', 'Joined')}</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.recentRegistrations.map((u, idx) => (
                    <tr key={idx}>
                      <td style={{ fontWeight: 600 }}>{u.name}</td>
                      <td>@{u.username}</td>
                      <td>
                        <span className={`badge ${isAdminRole(u.role) ? 'badge-admin' : 'badge-user'}`}>
                          {getRoleLabel(u.role)}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">{t('common.noData', 'No users registered yet.')}</div>
          )}
        </div>

        {/* Recent Platform Submissions */}
        <div className="card">
          <div className="panel-header">
            <span className="panel-title">{t('dashboard.recentSubmissions', 'Recent Submissions')}</span>
            <Link to="/admin/submissions" style={{ fontSize: '0.85rem' }}>{t('common.viewAll', 'View All')}</Link>
          </div>

          {stats?.recentSubmissions && stats.recentSubmissions.length > 0 ? (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>{t('admin.user', 'User')}</th>
                    <th>{t('submissions.problem', 'Problem')}</th>
                    <th>{t('common.status', 'Status')}</th>
                    <th>{t('common.language', 'Language')}</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.recentSubmissions.map((sub, idx) => (
                    <tr key={idx}>
                      <td style={{ fontWeight: 600 }}>@{sub.username}</td>
                      <td>{sub.problemTitle}</td>
                      <td>
                        <span className={`badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}`}>
                          {sub.status}
                        </span>
                      </td>
                      <td>{sub.language}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">{t('dashboard.noSubmissionsRecorded', 'No submissions recorded yet.')}</div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
