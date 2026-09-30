import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import leaderboardService from '../../services/leaderboardService';
import { Award, Trophy, Medal, Search, Flame, Clock, CheckCircle, RefreshCw, AlertCircle } from 'lucide-react';

const TABS = [
  { id: 'GLOBAL', labelKey: 'leaderboard.global', defaultLabel: 'Global', scope: 'GLOBAL', language: null },
  { id: 'WEEKLY', labelKey: 'leaderboard.weekly', defaultLabel: 'Weekly', scope: 'WEEKLY', language: null },
  { id: 'JAVA', labelKey: null, defaultLabel: 'Java', scope: 'GLOBAL', language: 'JAVA' },
  { id: 'PYTHON', labelKey: null, defaultLabel: 'Python', scope: 'GLOBAL', language: 'PYTHON' },
  { id: 'CPP', labelKey: null, defaultLabel: 'C++', scope: 'GLOBAL', language: 'CPP' },
  { id: 'JAVASCRIPT', labelKey: null, defaultLabel: 'JavaScript', scope: 'GLOBAL', language: 'JAVASCRIPT' },
];

const Leaderboard = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState(TABS[0]);
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchLeaderboard = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await leaderboardService.getLeaderboard(activeTab.scope, activeTab.language);
      setEntries(data);
    } catch (err) {
      setError(err.response?.data?.message || t('common.networkError', 'Failed to load leaderboard data.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLeaderboard();
  }, [activeTab]);

  const filteredEntries = entries.filter((e) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (e.name && e.name.toLowerCase().includes(q)) || (e.username && e.username.toLowerCase().includes(q));
  });

  const topThree = entries.slice(0, 3);

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.25rem' }}>
            <div style={{ padding: '0.5rem', background: 'var(--primary-soft)', borderRadius: '8px', color: 'var(--primary)' }}>
              <Trophy size={24} />
            </div>
            <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>{t('leaderboard.title', 'Platform Leaderboard')}</h1>
          </div>
          <p style={{ color: 'var(--text-muted)' }}>
            {t('leaderboard.subtitle', 'Track rankings, points, problem solve counts, and fastest execution times across the community.')}
          </p>
        </div>

        <button onClick={fetchLeaderboard} className="btn btn-outline btn-sm" disabled={loading}>
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          <span>{t('leaderboard.refresh', 'Refresh')}</span>
        </button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', overflowX: 'auto', paddingBottom: '0.5rem' }}>
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab)}
            className={`btn btn-sm ${activeTab.id === tab.id ? 'btn-primary' : 'btn-outline'}`}
            style={{ fontWeight: 600, minWidth: '90px' }}
          >
            {tab.labelKey ? t(tab.labelKey, tab.defaultLabel) : tab.defaultLabel}
          </button>
        ))}
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Top 3 Podium (rendered when there are entries and not filtering) */}
      {!loading && topThree.length > 0 && !searchQuery.trim() && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1.25rem',
          marginBottom: '2rem',
        }}>
          {topThree.map((entry, idx) => {
            const isFirst = idx === 0;
            const isSecond = idx === 1;
            const isThird = idx === 2;

            const badgeColor = isFirst ? '#fbbf24' : isSecond ? '#94a3b8' : '#d97706';
            const badgeLabel = isFirst ? '1st Place' : isSecond ? '2nd Place' : '3rd Place';
            const Icon = isFirst ? Trophy : Medal;

            return (
              <div
                key={entry.userId}
                className="card"
                style={{
                  textAlign: 'center',
                  padding: '1.75rem 1.25rem',
                  borderTop: `4px solid ${badgeColor}`,
                  position: 'relative',
                  background: isFirst ? 'linear-gradient(180deg, rgba(251, 191, 36, 0.08) 0%, var(--bg-card) 100%)' : 'var(--bg-card)',
                }}
              >
                <div style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  width: '56px',
                  height: '56px',
                  borderRadius: '50%',
                  background: 'var(--bg-main)',
                  border: `2px solid ${badgeColor}`,
                  marginBottom: '1rem',
                  color: badgeColor,
                }}>
                  <Icon size={28} />
                </div>

                <span className="badge" style={{ background: `${badgeColor}22`, color: badgeColor, marginBottom: '0.75rem' }}>
                  {badgeLabel}
                </span>

                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.25rem' }}>
                  {entry.name}
                </h3>
                <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', marginBottom: '1rem' }}>
                  @{entry.username}
                </p>

                <div style={{ display: 'flex', justifyContent: 'center', gap: '1.25rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                  <div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--primary)' }}>{entry.score}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase' }}>{t('common.points', 'Points')}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--success)' }}>{entry.problemsSolved}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase' }}>{t('leaderboard.solved', 'Solved')}</div>
                  </div>
                  <div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#38bdf8' }}>{entry.acceptanceRate}%</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase' }}>{t('dashboard.acceptanceRate', 'Rate')}</div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Main Leaderboard Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>
            Showing {filteredEntries.length} {filteredEntries.length === 1 ? 'candidate' : 'candidates'}
          </div>

          <div style={{ position: 'relative', width: '260px' }}>
            <Search size={16} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-subtle)' }} />
            <input
              type="text"
              placeholder={t('leaderboard.searchUser', 'Search user...')}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="form-control"
              style={{ paddingLeft: '2rem', fontSize: '0.85rem' }}
            />
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3.5rem 0', color: 'var(--text-muted)' }}>
            {t('leaderboard.loading', 'Loading rankings...')}
          </div>
        ) : filteredEntries.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '70px', textAlign: 'center' }}>{t('leaderboard.rank', 'Rank')}</th>
                  <th>{t('leaderboard.user', 'Candidate')}</th>
                  <th style={{ textAlign: 'center' }}>{t('leaderboard.solved', 'Problems Solved')}</th>
                  <th style={{ textAlign: 'center' }}>{t('leaderboard.score', 'Score')}</th>
                  <th style={{ textAlign: 'center' }}>{t('dashboard.acceptanceRate', 'Acceptance Rate')}</th>
                  <th style={{ textAlign: 'center' }}>{t('leaderboard.certificates', 'Certificates')}</th>
                  <th style={{ textAlign: 'center' }}>{t('submissions.runtime', 'Fastest Time')}</th>
                </tr>
              </thead>
              <tbody>
                {filteredEntries.map((entry) => {
                  const isCurrentUser = user && (entry.userId === user.id || entry.username === user.username);
                  const isTopOne = entry.rank === 1;
                  const isTopTwo = entry.rank === 2;
                  const isTopThree = entry.rank === 3;

                  return (
                    <tr
                      key={entry.userId}
                      style={{
                        background: isCurrentUser ? 'rgba(139, 92, 246, 0.12)' : undefined,
                        borderLeft: isCurrentUser ? '3px solid var(--primary)' : undefined,
                      }}
                    >
                      <td style={{ textAlign: 'center', fontWeight: 700 }}>
                        {isTopOne ? (
                          <span style={{ color: '#fbbf24', display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                            <Trophy size={16} /> #1
                          </span>
                        ) : isTopTwo ? (
                          <span style={{ color: '#94a3b8', display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                            <Medal size={16} /> #2
                          </span>
                        ) : isTopThree ? (
                          <span style={{ color: '#d97706', display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                            <Medal size={16} /> #3
                          </span>
                        ) : (
                          `#${entry.rank}`
                        )}
                      </td>

                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                          <div
                            className="avatar-circle"
                            style={{
                              width: '34px',
                              height: '34px',
                              fontSize: '0.85rem',
                              background: isCurrentUser ? 'var(--primary)' : 'var(--bg-elevated)',
                              border: isCurrentUser ? '1px solid var(--primary-light)' : '1px solid var(--border-color)',
                            }}
                          >
                            {(entry.name || entry.username || 'U').charAt(0).toUpperCase()}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                              <span>{entry.name}</span>
                              {isCurrentUser && (
                                <span className="badge badge-tag" style={{ fontSize: '0.7rem', padding: '1px 6px' }}>
                                  You
                                </span>
                              )}
                            </div>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
                              @{entry.username}
                            </div>
                          </div>
                        </div>
                      </td>

                      <td style={{ textAlign: 'center' }}>
                        <div style={{ fontWeight: 700 }}>{entry.problemsSolved}</div>
                        {(entry.easySolved > 0 || entry.mediumSolved > 0 || entry.hardSolved > 0) && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', display: 'flex', justifyContent: 'center', gap: '0.35rem' }}>
                            <span style={{ color: 'var(--success)' }}>E:{entry.easySolved}</span>
                            <span style={{ color: 'var(--warning)' }}>M:{entry.mediumSolved}</span>
                            <span style={{ color: 'var(--danger)' }}>H:{entry.hardSolved}</span>
                          </div>
                        )}
                      </td>

                      <td style={{ textAlign: 'center' }}>
                        <span style={{
                          fontWeight: 800,
                          fontSize: '1rem',
                          color: 'var(--primary)',
                          background: 'var(--primary-soft)',
                          padding: '4px 10px',
                          borderRadius: '6px',
                        }}>
                          {entry.score} pts
                        </span>
                      </td>

                      <td style={{ textAlign: 'center' }}>
                        <span style={{ fontWeight: 600 }}>{entry.acceptanceRate}%</span>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                          {entry.totalAccepted} / {entry.totalSubmissions}
                        </div>
                      </td>

                      <td style={{ textAlign: 'center' }}>
                        {entry.certificatesCount > 0 ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontWeight: 700, color: '#fbbf24' }}>
                            <Trophy size={14} /> {entry.certificatesCount}
                          </span>
                        ) : (
                          <span style={{ color: 'var(--text-subtle)' }}>—</span>
                        )}
                      </td>

                      <td style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        {entry.fastestExecutionTimeMs != null ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                            <Clock size={13} /> {entry.fastestExecutionTimeMs}ms
                          </span>
                        ) : (
                          '—'
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3.5rem 0', color: 'var(--text-muted)' }}>
            <Award size={40} style={{ margin: '0 auto 1rem auto', opacity: 0.5 }} />
            <p>{t('leaderboard.noEntries', 'No leaderboard entries found.')}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Leaderboard;
