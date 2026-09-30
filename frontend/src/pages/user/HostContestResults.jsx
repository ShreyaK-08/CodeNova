import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Trophy, Download, AlertCircle, Users } from 'lucide-react';
import { contestHostService } from '../../services/assessmentHostService';

const HostContestResults = () => {
  const { id } = useParams();
  const [contest, setContest] = useState(null);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await contestHostService.getContestResults(id);
        // data may be { contest, leaderboard } or just the leaderboard array
        if (Array.isArray(data)) {
          setResults(data);
        } else {
          setContest(data.contest || null);
          setResults(data.leaderboard || data.results || []);
        }
      } catch (err) {
        if (err.response?.status === 403) {
          setError('Access denied. You can only view results for contests you own.');
        } else {
          setError(err.response?.data?.message || 'Failed to load contest results.');
        }
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleExport = () => {
    const url = contestHostService.exportResultsCsvUrl(id);
    const a = document.createElement('a');
    a.href = url;
    a.download = `contest_${id}_results.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  const exportCsvFallback = () => {
    if (!results.length) return;
    const headers = ['Rank', 'Username', 'Score', 'Problems Solved', 'Submissions', 'Status', 'Violations', 'Completed At'];
    const rows = results.map((r, idx) => [
      idx + 1,
      `"${(r.participantUsername || r.username || '').replace(/"/g, '""')}"`,
      r.score ?? '',
      r.problemsSolved ?? '',
      r.submissionCount ?? '',
      r.status ?? '',
      r.securityViolationCount ?? 0,
      r.completedAt ? new Date(r.completedAt).toLocaleString() : '',
    ]);
    const csv = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const a = document.createElement('a');
    a.href = encodeURI(csv);
    a.download = `contest_${id}_results.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  return (
    <div>
      <Link
        to="/assessments/host"
        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem' }}
      >
        <ArrowLeft size={14} /> Back to My Assessments
      </Link>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Trophy size={22} color="#f59e0b" /> Contest Results
          </h2>
          {contest && (
            <p style={{ color: 'var(--text-muted)', margin: '0.25rem 0 0', fontSize: '0.9rem' }}>
              {contest.title}
            </p>
          )}
        </div>
        {results.length > 0 && (
          <button onClick={exportCsvFallback} className="btn btn-outline btn-sm">
            <Download size={14} />
            <span>Export CSV</span>
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading results...</div>
        ) : results.length === 0 && !error ? (
          <div className="empty-state" style={{ padding: '2.5rem 0' }}>
            <Users size={36} />
            <p>No participants have completed this contest yet.</p>
          </div>
        ) : results.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ textAlign: 'center', width: '60px' }}>Rank</th>
                  <th>Participant</th>
                  <th style={{ textAlign: 'center' }}>Score</th>
                  <th style={{ textAlign: 'center' }}>Problems Solved</th>
                  <th style={{ textAlign: 'center' }}>Submissions</th>
                  <th style={{ textAlign: 'center' }}>Violations</th>
                  <th style={{ textAlign: 'center' }}>Status</th>
                  <th style={{ textAlign: 'right' }}>Completed At</th>
                </tr>
              </thead>
              <tbody>
                {results.map((entry, idx) => (
                  <tr key={entry.participantId || entry.userId || idx}>
                    <td style={{ textAlign: 'center' }}>
                      <span style={{
                        display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                        width: '28px', height: '28px', borderRadius: '50%', fontWeight: 800, fontSize: '0.85rem',
                        backgroundColor: idx === 0 ? 'rgba(245,158,11,0.2)' : idx === 1 ? 'rgba(156,163,175,0.2)' : idx === 2 ? 'rgba(205,127,50,0.15)' : 'transparent',
                        color: idx === 0 ? '#f59e0b' : idx === 1 ? '#9ca3af' : idx === 2 ? '#cd7f32' : 'var(--text-muted)',
                      }}>
                        {idx + 1}
                      </span>
                    </td>
                    <td>
                      <div style={{ fontWeight: 600 }}>{entry.participantName || entry.name || 'Participant'}</div>
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>@{entry.participantUsername || entry.username || 'user'}</div>
                    </td>
                    <td style={{ textAlign: 'center', fontWeight: 700, fontSize: '1rem' }}>{entry.score ?? '-'}</td>
                    <td style={{ textAlign: 'center' }}>{entry.problemsSolved ?? '-'}</td>
                    <td style={{ textAlign: 'center' }}>{entry.submissionCount ?? '-'}</td>
                    <td style={{ textAlign: 'center' }}>
                      {(entry.securityViolationCount ?? 0) > 0 ? (
                        <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--danger)', backgroundColor: 'rgba(239,68,68,0.1)', padding: '0.15rem 0.5rem', borderRadius: '4px' }}>
                          {entry.securityViolationCount}
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>—</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span style={{
                        fontSize: '0.72rem', fontWeight: 700, padding: '0.15rem 0.5rem', borderRadius: '4px',
                        backgroundColor: entry.status === 'COMPLETED' ? 'rgba(34,197,94,0.12)' : 'rgba(245,158,11,0.12)',
                        color: entry.status === 'COMPLETED' ? '#15803d' : '#b45309',
                      }}>
                        {entry.status ?? 'IN_PROGRESS'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                      {entry.completedAt ? new Date(entry.completedAt).toLocaleString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default HostContestResults;
