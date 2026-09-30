import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import userService from '../../services/userService';
import certificateService from '../../services/certificateService';
import {
  CheckCircle,
  TrendingUp,
  Award,
  Code2,
  Clock,
  AlertCircle,
  Flame,
  Trophy,
} from 'lucide-react';

// Same convention as UserDashboard.jsx's coding-activity heatmap, reused here so the
// two pages stay visually/behaviorally consistent.
const HEATMAP_WEEKS = 16;

const buildHeatmapDays = (submissions) => {
  const countByDate = {};
  submissions.forEach((s) => {
    const d = new Date(s.submittedAt);
    const key = d.toISOString().slice(0, 10);
    countByDate[key] = (countByDate[key] || 0) + 1;
  });

  const totalDays = HEATMAP_WEEKS * 7;
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const endDayOfWeek = today.getDay();
  const daysAfterToday = 6 - endDayOfWeek;

  const days = [];
  for (let i = totalDays - 1; i >= -daysAfterToday; i--) {
    const date = new Date(today);
    date.setDate(today.getDate() - i);
    const key = date.toISOString().slice(0, 10);
    days.push({ date: key, count: countByDate[key] || 0 });
  }
  return days;
};

const heatmapColor = (count) => {
  if (count <= 0) return 'var(--bg-main)';
  if (count === 1) return 'rgba(59, 130, 246, 0.35)';
  if (count <= 3) return 'rgba(59, 130, 246, 0.6)';
  return 'rgba(59, 130, 246, 0.9)';
};

const RECENT_SUBMISSIONS_LIMIT = 10;

const ProgressDashboard = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [certProgress, setCertProgress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError('');
        // Reuses the exact same endpoints as UserDashboard.jsx - no new backend
        // endpoints were needed since /users/dashboard-stats and /users/submissions
        // already carry everything this page needs (difficulty breakdown, real
        // acceptance rate, real rank, and per-submission submittedAt/language/
        // executionTime for the activity heatmap, language usage, and recent list).
        const [statsData, submissionsData] = await Promise.all([
          userService.getDashboardStats(),
          userService.getUserSubmissions(),
        ]);
        setStats(statsData);
        setSubmissions(submissionsData || []);
      } catch (err) {
        console.error('Error fetching progress dashboard data:', err);
        setError(t('common.networkError', 'Could not load your progress data. Please try again.'));
      } finally {
        setLoading(false);
      }
    };

    loadData();

    // Read-only reuse of the existing certificate progress API - this page does not
    // create, modify, or manage certificates in any way.
    certificateService.getProgress().then(setCertProgress).catch(() => {});
  }, [t]);

  const heatmapDays = useMemo(() => buildHeatmapDays(submissions), [submissions]);

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

  const recentSubmissions = useMemo(
    () => submissions.slice(0, RECENT_SUBMISSIONS_LIMIT),
    [submissions]
  );

  const easyPercent = stats?.totalEasy ? Math.round((stats.easySolved / stats.totalEasy) * 100) : 0;
  const medPercent = stats?.totalMedium ? Math.round((stats.mediumSolved / stats.totalMedium) * 100) : 0;
  const hardPercent = stats?.totalHard ? Math.round((stats.hardSolved / stats.totalHard) * 100) : 0;

  const hasAnySubmissions = (stats?.totalSubmissions || 0) > 0;

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <TrendingUp size={22} color="var(--primary)" /> {t('progress.title', 'Progress Dashboard')}
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          {t('progress.subtitle', 'A detailed look at your solving activity, languages, and standing - all from your real submission history.')}
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* 1. Total Solved / 3. Success Rate / 6. Leaderboard Rank / Streak - metrics row */}
      <div className="grid-cols-4" style={{ marginBottom: '1.5rem' }}>
        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('dashboard.problemsSolved', 'Total Problems Solved')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(16,185,129,0.12)' }}>
              <CheckCircle size={18} color="#10b981" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.totalProblemsSolved || 0}</div>
          <span className="stat-caption">{t('dashboard.outOf', 'out of')} {loading ? '-' : stats?.totalProblems || 0} {t('dashboard.available', 'available')}</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('dashboard.acceptanceRate', 'Submission Success Rate')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(59,130,246,0.12)' }}>
              <TrendingUp size={18} color="#3b82f6" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : `${stats?.acceptanceRate || 0}%`}</div>
          <span className="stat-caption">
            {loading ? '-' : `${stats?.acceptedSubmissions || 0} accepted / ${stats?.totalSubmissions || 0} total`}
          </span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('dashboard.currentRank', 'Leaderboard Rank')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(139,92,246,0.12)' }}>
              <Award size={18} color="var(--primary)" />
            </div>
          </div>
          <div className="stat-value">{loading ? '-' : stats?.currentRank || '-'}</div>
          <span className="stat-caption">{t('dashboard.byProblemsSolved', 'By distinct problems solved')}</span>
        </div>

        <div className="stat-card">
          <div className="stat-card-top">
            <span className="stat-label">{t('dashboard.codingStreak', 'Solving Streak')}</span>
            <div className="stat-icon-box" style={{ backgroundColor: 'rgba(245,158,11,0.12)' }}>
              <Flame size={18} color="#f59e0b" />
            </div>
          </div>
          <div className="stat-value">
            {loading ? '-' : stats?.currentStreakDays || 0}{' '}
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-muted)' }}>{t('dashboard.days', 'days')}</span>
          </div>
          <span className="stat-caption">{stats?.currentStreakDays ? t('dashboard.keepItGoing', 'Keep it going') : t('dashboard.startStreak', 'Solve today to start a streak')}</span>
        </div>
      </div>

      {/* 8. Certificate milestone progress (read-only display, reusing the existing API) */}
      {certProgress && (
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <div className="panel-header">
            <span className="panel-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Trophy size={16} color="#fbbf24" /> {t('dashboard.achievementProgress', 'Certificate Milestone Progress')}
            </span>
            <Link to="/certificates" style={{ fontSize: '0.85rem' }}>{t('dashboard.viewCertificates', 'View Certificates')}</Link>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
            <span style={{ fontWeight: 700 }}>{certProgress.solvedCount} {t('certificates.problemsSolved', 'solved')}</span>
            <span style={{ color: 'var(--text-muted)' }}>Next milestone: {certProgress.nextMilestone}</span>
          </div>
          <div style={{ height: '10px', borderRadius: '9999px', background: 'var(--bg-input)', overflow: 'hidden', border: '1px solid var(--border-color)', marginBottom: '0.5rem' }}>
            <div
              style={{
                height: '100%',
                width: `${certProgress.progressPercent}%`,
                background: 'linear-gradient(90deg, var(--primary), var(--primary-hover))',
              }}
            />
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            {certProgress.remainingToNextMilestone > 0
              ? `${certProgress.remainingToNextMilestone} more problem${certProgress.remainingToNextMilestone === 1 ? '' : 's'} to reach ${certProgress.nextMilestone}.`
              : 'Milestone reached!'}
          </p>
        </div>
      )}

      <div className="grid-cols-2" style={{ marginBottom: '1.5rem' }}>
        {/* 2. Solved by Difficulty */}
        <div className="card">
          <div className="panel-header">
            <span className="panel-title">{t('dashboard.solvedByDifficulty', 'Solved by Difficulty')}</span>
          </div>
          {hasAnySubmissions ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.1rem' }}>
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.35rem' }}>
                  <span style={{ color: 'var(--success)', fontWeight: 600 }}>{t('common.easy', 'Easy')}</span>
                  <span style={{ fontWeight: 600 }}>{stats?.easySolved || 0} / {stats?.totalEasy || 0}</span>
                </div>
                <div style={{ width: '100%', height: '8px', background: 'var(--bg-main)', borderRadius: '4px', overflow: 'hidden' }}>
                  <div style={{ width: `${easyPercent}%`, height: '100%', background: '#34d399', borderRadius: '4px' }}></div>
                </div>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.35rem' }}>
                  <span style={{ color: 'var(--warning)', fontWeight: 600 }}>{t('common.medium', 'Medium')}</span>
                  <span style={{ fontWeight: 600 }}>{stats?.mediumSolved || 0} / {stats?.totalMedium || 0}</span>
                </div>
                <div style={{ width: '100%', height: '8px', background: 'var(--bg-main)', borderRadius: '4px', overflow: 'hidden' }}>
                  <div style={{ width: `${medPercent}%`, height: '100%', background: '#fbbf24', borderRadius: '4px' }}></div>
                </div>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.35rem' }}>
                  <span style={{ color: 'var(--danger)', fontWeight: 600 }}>{t('common.hard', 'Hard')}</span>
                  <span style={{ fontWeight: 600 }}>{stats?.hardSolved || 0} / {stats?.totalHard || 0}</span>
                </div>
                <div style={{ width: '100%', height: '8px', background: 'var(--bg-main)', borderRadius: '4px', overflow: 'hidden' }}>
                  <div style={{ width: `${hardPercent}%`, height: '100%', background: '#f87171', borderRadius: '4px' }}></div>
                </div>
              </div>
            </div>
          ) : (
            <div className="empty-state">
              <CheckCircle size={32} />
              <p>No solved problems yet. Solve your first problem to see a breakdown here.</p>
            </div>
          )}
        </div>

        {/* 4. Language Usage */}
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
              <Code2 size={32} />
              <p>{t('dashboard.noSubmissionsLanguage', 'No submissions yet to show language usage.')}</p>
            </div>
          )}
        </div>
      </div>

      {/* 7. Solving Streak / Activity heatmap */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div className="panel-header">
          <span className="panel-title">{t('dashboard.codingActivity', 'Solving Activity')}</span>
          <span className="panel-subtitle" style={{ marginTop: 0 }}>{t('dashboard.lastWeeks', 'Last {{weeks}} weeks', { weeks: HEATMAP_WEEKS })}</span>
        </div>
        {hasAnySubmissions ? (
          <>
            <div className="heatmap-wrapper">
              <div className="heatmap-grid">
                {heatmapDays.map((day) => (
                  <div
                    key={day.date}
                    className="heatmap-cell"
                    style={{ backgroundColor: heatmapColor(day.count) }}
                    title={`${day.date}: ${day.count} submission${day.count === 1 ? '' : 's'}`}
                  />
                ))}
              </div>
            </div>
            <div className="heatmap-legend">
              <span>{t('dashboard.less', 'Less')}</span>
              <div className="heatmap-cell" style={{ backgroundColor: heatmapColor(0) }} />
              <div className="heatmap-cell" style={{ backgroundColor: heatmapColor(1) }} />
              <div className="heatmap-cell" style={{ backgroundColor: heatmapColor(2) }} />
              <div className="heatmap-cell" style={{ backgroundColor: heatmapColor(4) }} />
              <span>{t('dashboard.more', 'More')}</span>
            </div>
          </>
        ) : (
          <div className="empty-state">
            <Flame size={32} />
            <p>No activity yet - your submission dates will show up here as a heatmap.</p>
          </div>
        )}
      </div>

      {/* 5. Recent Submissions */}
      <div className="card">
        <div className="panel-header">
          <span className="panel-title">{t('dashboard.recentSubmissions', 'Recent Submissions')}</span>
          <Link to="/submissions" style={{ fontSize: '0.85rem' }}>{t('dashboard.viewAll', 'View All')}</Link>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '2rem 0', color: 'var(--text-muted)' }}>{t('submissions.loading', 'Loading...')}</div>
        ) : recentSubmissions.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>{t('submissions.problem', 'Problem')}</th>
                  <th>{t('submissions.language', 'Language')}</th>
                  <th>{t('submissions.status', 'Status')}</th>
                  <th>{t('submissions.runtime', 'Execution')}</th>
                  <th>{t('submissions.date', 'Date / Time')}</th>
                </tr>
              </thead>
              <tbody>
                {recentSubmissions.map((sub) => (
                  <tr key={sub.id}>
                    <td style={{ fontWeight: 600 }}>{sub.problemTitle}</td>
                    <td>{sub.language}</td>
                    <td>
                      <span className={`badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}`}>
                        {sub.status}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {sub.executionTime != null ? (
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                          <Clock size={13} /> {sub.executionTime}ms
                        </span>
                      ) : (
                        '—'
                      )}
                      {sub.totalTestCases ? (
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                          {sub.passedTestCases ?? 0}/{sub.totalTestCases} tests passed
                        </div>
                      ) : null}
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <Clock size={36} />
            <p>{t('dashboard.noSubmissionsRecorded', 'No submissions recorded yet.')}</p>
            <Link to="/problems" className="btn btn-outline btn-sm" style={{ marginTop: '0.75rem' }}>
              {t('dashboard.startSolving', 'Start Solving')}
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProgressDashboard;
