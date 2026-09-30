import React, { useState, useEffect, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import dashboardService from '../../services/dashboardService';
import certificateService from '../../services/certificateService';
import {
  Award,
  CheckCircle,
  Code2,
  Flame,
  TrendingUp,
  Clock,
  AlertCircle,
  Trophy,
  Calendar,
  Download,
  ChevronRight,
  BarChart3,
} from 'lucide-react';

const LANGUAGE_CONFIG = {
  Python: { color: '#3b82f6', bg: 'rgba(59, 130, 246, 0.12)', border: 'rgba(59, 130, 246, 0.3)' },
  Java: { color: '#ea580c', bg: 'rgba(234, 88, 12, 0.12)', border: 'rgba(234, 88, 12, 0.3)' },
  'C++': { color: '#6366f1', bg: 'rgba(99, 102, 241, 0.12)', border: 'rgba(99, 102, 241, 0.3)' },
  JavaScript: { color: '#eab308', bg: 'rgba(234, 179, 8, 0.12)', border: 'rgba(234, 179, 8, 0.3)' },
  SQL: { color: '#06b6d4', bg: 'rgba(6, 182, 212, 0.12)', border: 'rgba(6, 182, 212, 0.3)' },
  C: { color: '#64748b', bg: 'rgba(100, 116, 139, 0.12)', border: 'rgba(100, 116, 139, 0.3)' },
};

const getHeatmapColor = (count) => {
  if (count <= 0) return 'var(--bg-main, #f1f5f9)';
  if (count <= 2) return '#86efac';
  if (count <= 5) return '#22c55e';
  return '#15803d';
};

const UserDashboard = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  const [stats, setStats] = useState(null);
  const [activity, setActivity] = useState(null);
  const [languageStats, setLanguageStats] = useState([]);
  const [milestones, setMilestones] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [certTab, setCertTab] = useState('milestones');
  const [hoveredCell, setHoveredCell] = useState(null);

  useEffect(() => {
    let isMounted = true;
    const loadDashboardData = async () => {
      try {
        setLoading(true);
        setError('');

        const [statsData, activityData, langData, milestoneData] = await Promise.all([
          dashboardService.getStats(),
          dashboardService.getActivity(),
          dashboardService.getLanguageStats(),
          dashboardService.getMilestones(),
        ]);

        if (isMounted) {
          setStats(statsData);
          setActivity(activityData);
          setLanguageStats(Array.isArray(langData) ? langData : []);
          setMilestones(milestoneData);
        }
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        if (isMounted) {
          setError('Could not load live dashboard data. Please refresh or try again later.');
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadDashboardData();
    return () => {
      isMounted = false;
    };
  }, []);

  // 52-week calendar grid builder (365 days aligned Sunday - Saturday)
  const { weeks, monthLabels } = useMemo(() => {
    const activityMap = {};
    if (activity && Array.isArray(activity.days)) {
      activity.days.forEach((item) => {
        if (item && item.date) {
          activityMap[item.date] = item;
        }
      });
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const dayOfWeek = today.getDay(); // 0 = Sun, 6 = Sat
    const totalWeeks = 53;
    const totalDays = (totalWeeks - 1) * 7 + (dayOfWeek + 1);

    const startDate = new Date(today);
    startDate.setDate(today.getDate() - (totalDays - 1));

    const weeksArray = [];
    let currentWeek = [];
    const months = [];
    let lastMonth = -1;

    for (let i = 0; i < totalDays; i++) {
      const d = new Date(startDate);
      d.setDate(startDate.getDate() + i);

      const yyyy = d.getFullYear();
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      const dateStr = `${yyyy}-${mm}-${dd}`;

      const item = activityMap[dateStr];
      const count = item ? item.submissions : 0;
      const solved = item ? item.problemsSolved : 0;

      currentWeek.push({
        date: dateStr,
        displayDate: d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }),
        count,
        solved,
        dayOfWeek: d.getDay(),
      });

      if (currentWeek.length === 7 || i === totalDays - 1) {
        const firstDayOfWeek = new Date(currentWeek[0].date);
        const m = firstDayOfWeek.getMonth();
        if (m !== lastMonth) {
          months.push({
            weekIndex: weeksArray.length,
            label: firstDayOfWeek.toLocaleDateString(undefined, { month: 'short' }),
          });
          lastMonth = m;
        }

        weeksArray.push(currentWeek);
        currentWeek = [];
      }
    }

    return { weeks: weeksArray, monthLabels: months };
  }, [activity]);

  const maxLanguageSolved = useMemo(() => {
    if (!languageStats.length) return 1;
    return Math.max(...languageStats.map((l) => l.problemsSolved || 0), 1);
  }, [languageStats]);

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Top Welcome Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.75rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)' }}>
            {t('dashboard.welcomeBack', 'Welcome back, {{name}}', { name: user?.name || user?.username || 'Student' })} 👋
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
            {t('dashboard.subtitle', "Track your live coding activity, language benchmarks, and milestone certifications.")}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/certificates" className="btn btn-outline btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
            <Trophy size={16} color="#fbbf24" />
            <span>{t('dashboard.myCertificates', 'My Certificates')}</span>
          </Link>
          <Link to="/problems" className="btn btn-primary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
            <Code2 size={16} />
            <span>{t('dashboard.browseProblems', 'Solve Problems')}</span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* ROW 1: KEY STATS CARDS */}
      <div className="grid-cols-4" style={{ marginBottom: '1.75rem', gap: '1rem' }}>
        {/* Card 1: Total Problems Solved */}
        <div className="stat-card" style={{ padding: '1.25rem', borderRadius: 'var(--radius-lg, 12px)' }}>
          <div className="stat-card-top" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
            <span className="stat-label" style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{t('dashboard.problemsSolved', 'Problems Solved')}</span>
            <div className="stat-icon-box" style={{ width: '38px', height: '38px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(16,185,129,0.12)' }}>
              <CheckCircle size={20} color="#10b981" />
            </div>
          </div>
          <div className="stat-value" style={{ fontSize: '2rem', fontWeight: 800 }}>
            {loading ? '-' : stats?.totalProblemsSolved || 0}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.35rem', fontSize: '0.8rem', color: 'var(--text-muted)', flexWrap: 'wrap' }}>
            <span style={{ color: '#10b981', fontWeight: 600 }}>{stats?.easySolved || 0} Easy</span>
            <span>•</span>
            <span style={{ color: '#f59e0b', fontWeight: 600 }}>{stats?.mediumSolved || 0} Med</span>
            <span>•</span>
            <span style={{ color: '#ef4444', fontWeight: 600 }}>{stats?.hardSolved || 0} Hard</span>
          </div>
        </div>

        {/* Card 2: Acceptance Rate */}
        <div className="stat-card" style={{ padding: '1.25rem', borderRadius: 'var(--radius-lg, 12px)' }}>
          <div className="stat-card-top" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
            <span className="stat-label" style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{t('dashboard.acceptanceRate', 'Acceptance Rate')}</span>
            <div className="stat-icon-box" style={{ width: '38px', height: '38px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(59,130,246,0.12)' }}>
              <TrendingUp size={20} color="#3b82f6" />
            </div>
          </div>
          <div className="stat-value" style={{ fontSize: '2rem', fontWeight: 800 }}>
            {loading ? '-' : `${stats?.acceptanceRate ? stats.acceptanceRate.toFixed(1) : '0.0'}%`}
          </div>
          <span className="stat-caption" style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem', display: 'block' }}>
            {stats?.acceptedSubmissions || 0} accepted of {stats?.totalSubmissions || 0} submissions
          </span>
        </div>

        {/* Card 3: Coding Streak */}
        <div className="stat-card" style={{ padding: '1.25rem', borderRadius: 'var(--radius-lg, 12px)' }}>
          <div className="stat-card-top" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
            <span className="stat-label" style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{t('dashboard.codingStreak', 'Coding Streak')}</span>
            <div className="stat-icon-box" style={{ width: '38px', height: '38px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(245,158,11,0.12)' }}>
              <Flame size={20} color="#f59e0b" />
            </div>
          </div>
          <div className="stat-value" style={{ fontSize: '2rem', fontWeight: 800 }}>
            {loading ? '-' : `${activity?.currentStreak ?? stats?.currentStreakDays ?? 0}`}{' '}
            <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-muted)' }}>
              {t('dashboard.days', 'days')}
            </span>
          </div>
          <span className="stat-caption" style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem', display: 'block' }}>
            {t('dashboard.longestStreak', { days: activity?.longestStreak ?? 0, defaultValue: `Longest streak: ${activity?.longestStreak ?? 0} days` })}
          </span>
        </div>

        {/* Card 4: Certifications Earned */}
        <div className="stat-card" style={{ padding: '1.25rem', borderRadius: 'var(--radius-lg, 12px)' }}>
          <div className="stat-card-top" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
            <span className="stat-label" style={{ fontWeight: 600, color: 'var(--text-muted)' }}>{t('dashboard.certificationsEarned', 'Certifications')}</span>
            <div className="stat-icon-box" style={{ width: '38px', height: '38px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'rgba(139,92,246,0.12)' }}>
              <Award size={20} color="var(--primary, #8b5cf6)" />
            </div>
          </div>
          <div className="stat-value" style={{ fontSize: '2rem', fontWeight: 800 }}>
            {loading ? '-' : stats?.certificatesEarnedCount ?? milestones?.earnedCertificates?.length ?? 0}
          </div>
          <span className="stat-caption" style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem', display: 'block' }}>
            {t('dashboard.certificationsEarnedSummary', {
              langCount: milestones?.languageMilestones?.filter((m) => m.earned)?.length || 0,
              milestoneCount: milestones?.overallMilestones?.filter((m) => m.earned)?.length || 0,
              defaultValue: `${milestones?.languageMilestones?.filter((m) => m.earned)?.length || 0} language & ${milestones?.overallMilestones?.filter((m) => m.earned)?.length || 0} milestone`,
            })}
          </span>
        </div>
      </div>

      {/* ROW 2: GITHUB-STYLE 52-WEEK HEATMAP */}
      <div className="card" style={{ marginBottom: '1.75rem', padding: '1.5rem', borderRadius: 'var(--radius-lg, 12px)', position: 'relative' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Calendar size={18} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('dashboard.codingActivity', 'Coding Activity')}
              </h2>
            </div>
            <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '0.2rem', display: 'block' }}>
              {t('dashboard.pastWeeksHistory', 'Past 52 weeks (365 days) verified submission history')}
            </span>
          </div>

          {/* Activity Quick Stats Pills */}
          <div style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap' }}>
            <div style={{ padding: '0.35rem 0.75rem', borderRadius: '9999px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', fontSize: '0.785rem', fontWeight: 600 }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('dashboard.submissionsStat', 'Submissions')}: </span>
              <strong style={{ color: 'var(--primary)' }}>{activity?.totalSubmissions || 0}</strong>
            </div>
            <div style={{ padding: '0.35rem 0.75rem', borderRadius: '9999px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', fontSize: '0.785rem', fontWeight: 600 }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('dashboard.activeDaysStat', 'Active Days')}: </span>
              <strong style={{ color: '#10b981' }}>{activity?.activeDays || 0}</strong>
            </div>
            <div style={{ padding: '0.35rem 0.75rem', borderRadius: '9999px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', fontSize: '0.785rem', fontWeight: 600 }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('dashboard.currentStreakStat', 'Current Streak')}: </span>
              <strong style={{ color: '#f59e0b' }}>{activity?.currentStreak || 0}d</strong>
            </div>
            <div style={{ padding: '0.35rem 0.75rem', borderRadius: '9999px', background: 'var(--bg-main)', border: '1px solid var(--border-color)', fontSize: '0.785rem', fontWeight: 600 }}>
              <span style={{ color: 'var(--text-muted)' }}>{t('dashboard.maxStreakStat', 'Max Streak')}: </span>
              <strong style={{ color: '#8b5cf6' }}>{activity?.longestStreak || 0}d</strong>
            </div>
          </div>
        </div>

        {/* Heatmap Container */}
        <div style={{ overflowX: 'auto', paddingBottom: '0.5rem' }}>
          <div style={{ minWidth: '760px', display: 'inline-block' }}>
            {/* Months Header */}
            <div style={{ display: 'flex', marginLeft: '32px', marginBottom: '6px', position: 'relative', height: '16px' }}>
              {monthLabels.map((m, idx) => (
                <div
                  key={`${m.label}-${idx}`}
                  style={{
                    position: 'absolute',
                    left: `${m.weekIndex * 15}px`,
                    fontSize: '0.72rem',
                    color: 'var(--text-subtle, #64748b)',
                    fontWeight: 600,
                  }}
                >
                  {m.label}
                </div>
              ))}
            </div>

            {/* Heatmap Grid + Day labels */}
            <div style={{ display: 'flex', gap: '6px' }}>
              {/* Day Labels Column */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '3px', width: '26px', fontSize: '0.68rem', color: 'var(--text-subtle, #64748b)', textAlign: 'right', paddingTop: '2px' }}>
                <span style={{ height: '12px', lineHeight: '12px' }}></span>
                <span style={{ height: '12px', lineHeight: '12px' }}>{t('dashboard.mon', 'Mon')}</span>
                <span style={{ height: '12px', lineHeight: '12px' }}></span>
                <span style={{ height: '12px', lineHeight: '12px' }}>{t('dashboard.wed', 'Wed')}</span>
                <span style={{ height: '12px', lineHeight: '12px' }}></span>
                <span style={{ height: '12px', lineHeight: '12px' }}>{t('dashboard.fri', 'Fri')}</span>
                <span style={{ height: '12px', lineHeight: '12px' }}></span>
              </div>

              {/* Weeks Grid */}
              <div style={{ display: 'flex', gap: '3px' }}>
                {weeks.map((week, wIdx) => (
                  <div key={wIdx} style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
                    {week.map((cell) => (
                      <div
                        key={cell.date}
                        style={{
                          width: '12px',
                          height: '12px',
                          borderRadius: '2px',
                          backgroundColor: getHeatmapColor(cell.count),
                          border: cell.count > 0 ? '1px solid rgba(0,0,0,0.06)' : '1px solid var(--border-color, #e2e8f0)',
                          cursor: 'pointer',
                          transition: 'transform 0.15s ease',
                        }}
                        onMouseEnter={(e) => {
                          const rect = e.currentTarget.getBoundingClientRect();
                          setHoveredCell({
                            date: cell.displayDate,
                            count: cell.count,
                            solved: cell.solved,
                            x: rect.left + rect.width / 2,
                            top: rect.top - 42,
                          });
                        }}
                        onMouseLeave={() => setHoveredCell(null)}
                      />
                    ))}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Floating Tooltip */}
        {hoveredCell && (
          <div
            style={{
              position: 'fixed',
              top: `${hoveredCell.top}px`,
              left: `${hoveredCell.x}px`,
              transform: 'translateX(-50%)',
              backgroundColor: '#1e293b',
              color: '#ffffff',
              padding: '4px 10px',
              borderRadius: '6px',
              fontSize: '0.75rem',
              fontWeight: 600,
              pointerEvents: 'none',
              zIndex: 9999,
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
              whiteSpace: 'nowrap',
            }}
          >
            {hoveredCell.count === 0
              ? `No activity on ${hoveredCell.date}`
              : `${hoveredCell.count} submission${hoveredCell.count === 1 ? '' : 's'}${
                  hoveredCell.solved > 0 ? ` (${hoveredCell.solved} solved)` : ''
                } on ${hoveredCell.date}`}
          </div>
        )}

        {/* Legend */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-color)', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
          <span>{t('dashboard.learnPracticeCode', 'Learn, practice, and code consistently every day')}</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span>{t('dashboard.less', 'Less')}</span>
            <div style={{ width: '12px', height: '12px', borderRadius: '2px', backgroundColor: getHeatmapColor(0), border: '1px solid var(--border-color)' }} />
            <div style={{ width: '12px', height: '12px', borderRadius: '2px', backgroundColor: getHeatmapColor(1) }} />
            <div style={{ width: '12px', height: '12px', borderRadius: '2px', backgroundColor: getHeatmapColor(3) }} />
            <div style={{ width: '12px', height: '12px', borderRadius: '2px', backgroundColor: getHeatmapColor(6) }} />
            <span>{t('dashboard.more', 'More')}</span>
          </div>
        </div>
      </div>

      {/* ROW 3: TWO COLUMNS (LANGUAGE BREAKDOWN vs CERTIFICATIONS) */}
      <div className="grid-cols-2" style={{ marginBottom: '1.75rem', gap: '1.25rem' }}>
        {/* LEFT: Programming Language Breakdown Graph */}
        <div className="card" style={{ padding: '1.5rem', borderRadius: 'var(--radius-lg, 12px)', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <BarChart3 size={18} color="var(--primary)" />
                <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                  {t('dashboard.languageComparison', 'Programming Languages')}
                </h2>
              </div>
              <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '0.2rem', display: 'block' }}>
                {t('dashboard.languageProblemsSummary', 'Problems solved, accepted submissions, and acceptance rate')}
              </span>
            </div>
            <Link to="/problems" style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
              {t('problems.solve', 'Solve')} <ChevronRight size={14} />
            </Link>
          </div>

          {languageStats && languageStats.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.1rem', flex: 1 }}>
              {languageStats.map((item) => {
                const cfg = LANGUAGE_CONFIG[item.language] || { color: '#8b5cf6', bg: 'rgba(139,92,246,0.1)', border: 'rgba(139,92,246,0.3)' };
                const barPercent = Math.max(Math.round((item.problemsSolved / maxLanguageSolved) * 100), item.problemsSolved > 0 ? 8 : 0);

                return (
                  <div key={item.language} style={{ padding: '0.75rem', borderRadius: '8px', background: 'var(--bg-main, #f8fafc)', border: `1px solid var(--border-color)` }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                        <span
                          style={{
                            padding: '0.2rem 0.6rem',
                            borderRadius: '6px',
                            fontSize: '0.82rem',
                            fontWeight: 700,
                            backgroundColor: cfg.bg,
                            color: cfg.color,
                            border: `1px solid ${cfg.border}`,
                          }}
                        >
                          {item.language}
                        </span>
                        <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-main)' }}>
                          {item.problemsSolved} {item.problemsSolved === 1 ? 'problem' : 'problems'} solved
                        </span>
                      </div>

                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontSize: '0.85rem', fontWeight: 700, color: item.acceptanceRate >= 70 ? '#10b981' : item.acceptanceRate >= 40 ? '#f59e0b' : 'var(--text-muted)' }}>
                          {item.acceptanceRate ? item.acceptanceRate.toFixed(1) : '0.0'}% Acc.
                        </span>
                        <span style={{ display: 'block', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                          {item.acceptedSubmissions} / {item.totalSubmissions} subs
                        </span>
                      </div>
                    </div>

                    {/* Progress Bar */}
                    <div style={{ height: '8px', borderRadius: '9999px', background: 'var(--bg-card, #ffffff)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                      <div
                        style={{
                          height: '100%',
                          width: `${barPercent}%`,
                          backgroundColor: cfg.color,
                          borderRadius: '9999px',
                          transition: 'width 0.4s ease',
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="empty-state" style={{ padding: '2.5rem 1rem', textAlign: 'center' }}>
              <Code2 size={36} color="var(--text-muted)" />
              <p style={{ marginTop: '0.5rem', color: 'var(--text-muted)' }}>
                {t('dashboard.noSubmissionsLanguage', 'No submissions recorded yet to show language benchmarks.')}
              </p>
              <Link to="/problems" className="btn btn-outline btn-sm" style={{ marginTop: '0.75rem' }}>
                {t('dashboard.startSolving', 'Start Solving')}
              </Link>
            </div>
          )}
        </div>

        {/* RIGHT: Milestone & Language Certifications */}
        <div className="card" style={{ padding: '1.5rem', borderRadius: 'var(--radius-lg, 12px)', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Trophy size={18} color="#fbbf24" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('dashboard.achievementProgress', 'Certifications & Milestones')}
              </h2>
            </div>
            <Link to="/certificates" style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
              {t('common.viewAll', 'View All')} <ChevronRight size={14} />
            </Link>
          </div>

          {/* Tab Selector */}
          <div style={{ display: 'flex', background: 'var(--bg-main)', borderRadius: '8px', padding: '3px', marginBottom: '1rem' }}>
            <button
              onClick={() => setCertTab('milestones')}
              style={{
                flex: 1,
                padding: '0.45rem 0.75rem',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.825rem',
                fontWeight: 600,
                cursor: 'pointer',
                backgroundColor: certTab === 'milestones' ? 'var(--bg-card, #ffffff)' : 'transparent',
                color: certTab === 'milestones' ? 'var(--primary)' : 'var(--text-muted)',
                boxShadow: certTab === 'milestones' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                transition: 'all 0.2s',
              }}
            >
              {t('dashboard.overallMilestones', 'Overall Milestones')} ({milestones?.overallMilestones?.filter((m) => m.earned)?.length || 0}/5)
            </button>
            <button
              onClick={() => setCertTab('languages')}
              style={{
                flex: 1,
                padding: '0.45rem 0.75rem',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.825rem',
                fontWeight: 600,
                cursor: 'pointer',
                backgroundColor: certTab === 'languages' ? 'var(--bg-card, #ffffff)' : 'transparent',
                color: certTab === 'languages' ? 'var(--primary)' : 'var(--text-muted)',
                boxShadow: certTab === 'languages' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
                transition: 'all 0.2s',
              }}
            >
              {t('dashboard.languageCertifications', 'Language Certifications')} ({milestones?.languageMilestones?.filter((m) => m.earned)?.length || 0}/6)
            </button>
          </div>

          {/* Tab Content */}
          <div style={{ flex: 1, overflowY: 'auto', maxHeight: '380px', paddingRight: '4px' }}>
            {certTab === 'milestones' ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {(milestones?.overallMilestones || []).map((m) => (
                  <div
                    key={m.title}
                    style={{
                      padding: '0.85rem',
                      borderRadius: '8px',
                      background: m.earned ? 'rgba(16,185,129,0.06)' : 'var(--bg-main, #f8fafc)',
                      border: m.earned ? '1px solid rgba(16,185,129,0.25)' : '1px solid var(--border-color)',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                        {m.earned ? (
                          <CheckCircle size={16} color="#10b981" />
                        ) : (
                          <Award size={16} color="var(--text-muted)" />
                        )}
                        <span style={{ fontWeight: 700, fontSize: '0.88rem', color: m.earned ? 'var(--text-main)' : 'var(--text-muted)' }}>
                          {m.title}
                        </span>
                      </div>

                      {m.earned ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                          <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 700 }}>{t('dashboard.earnedBadge', 'EARNED ✓')}</span>
                          <Link to="/certificates" style={{ fontSize: '0.75rem', color: 'var(--primary)', textDecoration: 'underline' }}>
                            {t('common.view', 'View')}
                          </Link>
                        </div>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                          {m.currentCount} / {m.requiredCount} {t('contests.solved', 'solved')}
                        </span>
                      )}
                    </div>

                    {/* Progress Bar */}
                    <div style={{ height: '6px', borderRadius: '9999px', background: 'var(--bg-card, #ffffff)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                      <div
                        style={{
                          height: '100%',
                          width: `${Math.min(m.progressPercent || 0, 100)}%`,
                          background: m.earned
                            ? '#10b981'
                            : 'linear-gradient(90deg, var(--primary), var(--primary-hover))',
                          borderRadius: '9999px',
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {(milestones?.languageMilestones || []).map((m) => {
                  const cfg = LANGUAGE_CONFIG[m.language] || { color: '#8b5cf6' };
                  return (
                    <div
                      key={m.title}
                      style={{
                        padding: '0.85rem',
                        borderRadius: '8px',
                        background: m.earned ? 'rgba(16,185,129,0.06)' : 'var(--bg-main, #f8fafc)',
                        border: m.earned ? '1px solid rgba(16,185,129,0.25)' : '1px solid var(--border-color)',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                          <span
                            style={{
                              width: '8px',
                              height: '8px',
                              borderRadius: '50%',
                              backgroundColor: cfg.color,
                              display: 'inline-block',
                            }}
                          />
                          <span style={{ fontWeight: 700, fontSize: '0.88rem', color: m.earned ? 'var(--text-main)' : 'var(--text-muted)' }}>
                            {m.title}
                          </span>
                        </div>

                        {m.earned ? (
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                            <span style={{ fontSize: '0.75rem', color: '#10b981', fontWeight: 700 }}>{t('dashboard.earnedBadge', 'EARNED ✓')}</span>
                            <Link to="/certificates" style={{ fontSize: '0.75rem', color: 'var(--primary)', textDecoration: 'underline' }}>
                              {t('common.view', 'View')}
                            </Link>
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                            {t('dashboard.solvedIn', { current: m.currentCount, required: m.requiredCount, lang: m.language, defaultValue: `${m.currentCount} / ${m.requiredCount} solved in ${m.language}` })}
                          </span>
                        )}
                      </div>

                      {/* Progress Bar */}
                      <div style={{ height: '6px', borderRadius: '9999px', background: 'var(--bg-card, #ffffff)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                        <div
                          style={{
                            height: '100%',
                            width: `${Math.min(m.progressPercent || 0, 100)}%`,
                            backgroundColor: m.earned ? '#10b981' : cfg.color,
                            borderRadius: '9999px',
                          }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ROW 4: RECENT SUBMISSIONS TABLE */}
      <div className="card" style={{ padding: '1.5rem', borderRadius: 'var(--radius-lg, 12px)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Clock size={18} color="var(--primary)" />
              <h2 style={{ fontSize: '1.15rem', fontWeight: 700, margin: 0 }}>
                {t('dashboard.recentSubmissions', 'Recent Coding Submissions')}
              </h2>
            </div>
            <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '0.2rem', display: 'block' }}>
              {t('dashboard.recentSubmissionsSubtitle', 'Your latest problem attempts and verified verdict status')}
            </span>
          </div>
          <Link to="/submissions" style={{ fontSize: '0.85rem', color: 'var(--primary)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
            {t('dashboard.viewAll', 'View All Submissions')} <ChevronRight size={14} />
          </Link>
        </div>

        {stats?.recentSubmissions && stats.recentSubmissions.length > 0 ? (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', textAlign: 'left' }}>
                  <th style={{ padding: '0.75rem 0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>{t('submissions.problem', 'Problem')}</th>
                  <th style={{ padding: '0.75rem 0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>{t('submissions.status', 'Status')}</th>
                  <th style={{ padding: '0.75rem 0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>{t('submissions.language', 'Language')}</th>
                  <th style={{ padding: '0.75rem 0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>{t('submissions.date', 'Date')}</th>
                  <th style={{ padding: '0.75rem 0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)', textAlign: 'right' }}>{t('common.action', 'Action')}</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentSubmissions.map((sub, idx) => {
                  const isAccepted = sub.status === 'ACCEPTED';
                  const dateStr = sub.submittedAt ? new Date(sub.submittedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' }) : '-';
                  return (
                    <tr key={idx} style={{ borderBottom: '1px solid var(--border-color)' }}>
                      <td style={{ padding: '0.85rem 0.5rem', fontWeight: 600 }}>
                        <Link to={sub.problemId ? `/problems/${sub.problemId}` : '/problems'} style={{ color: 'var(--text-main)', textDecoration: 'none' }}>
                          {sub.problemTitle || 'Coding Problem'}
                        </Link>
                      </td>
                      <td style={{ padding: '0.85rem 0.5rem' }}>
                        <span
                          className={`badge ${isAccepted ? 'badge-easy' : 'badge-hard'}`}
                          style={{
                            padding: '0.2rem 0.55rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            backgroundColor: isAccepted ? 'rgba(16,185,129,0.12)' : 'rgba(239,68,68,0.12)',
                            color: isAccepted ? '#10b981' : '#ef4444',
                          }}
                        >
                          {sub.status}
                        </span>
                      </td>
                      <td style={{ padding: '0.85rem 0.5rem', fontSize: '0.85rem' }}>
                        <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{sub.language}</span>
                      </td>
                      <td style={{ padding: '0.85rem 0.5rem', color: 'var(--text-muted)', fontSize: '0.825rem' }}>
                        {dateStr}
                      </td>
                      <td style={{ padding: '0.85rem 0.5rem', textAlign: 'right' }}>
                        <Link to={sub.problemId ? `/problems/${sub.problemId}` : '/problems'} className="btn btn-outline btn-sm" style={{ padding: '0.25rem 0.6rem', fontSize: '0.78rem' }}>
                          {t('common.practice', 'Practice')}
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state" style={{ textAlign: 'center', padding: '2.5rem 1rem' }}>
            <Clock size={36} color="var(--text-muted)" />
            <p style={{ marginTop: '0.5rem', color: 'var(--text-muted)' }}>
              {t('dashboard.noSubmissionsRecorded', 'No submissions recorded yet.')}
            </p>
            <Link to="/problems" className="btn btn-primary btn-sm" style={{ marginTop: '0.75rem' }}>
              {t('dashboard.startSolving', 'Start Solving Problems')}
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default UserDashboard;
