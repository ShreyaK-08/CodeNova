import React, { useState, useEffect, useMemo } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { problemService } from '../../services/problemService';
import { Code, Search, Filter, ArrowRight, AlertCircle, CheckCircle2, Circle, Tag } from 'lucide-react';

const DIFFICULTY_STYLES = {
  EASY:   { color: '#15803d', bg: 'rgba(34,197,94,0.12)' },
  MEDIUM: { color: '#b45309', bg: 'rgba(245,158,11,0.12)' },
  HARD:   { color: '#b91c1c', bg: 'rgba(239,68,68,0.12)'  },
};

const DifficultyBadge = ({ difficulty }) => {
  const s = DIFFICULTY_STYLES[difficulty] || { color: 'var(--text-muted)', bg: 'var(--bg-input)' };
  return (
    <span style={{
      fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.55rem',
      borderRadius: '9999px', color: s.color, backgroundColor: s.bg,
    }}>
      {difficulty}
    </span>
  );
};

const ProblemsList = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const [problems, setProblems]   = useState([]);
  const [searchTerm, setSearchTerm]         = useState(searchParams.get('search') || '');
  const [difficultyFilter, setDifficultyFilter] = useState('ALL');
  const [topicFilter, setTopicFilter]       = useState('ALL');
  const [solvedFilter, setSolvedFilter]     = useState('ALL'); // ALL | SOLVED | UNSOLVED
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');

  // Keep search in sync with URL query param
  useEffect(() => {
    setSearchTerm(searchParams.get('search') || '');
  }, [searchParams]);

  // Fetch problems once
  useEffect(() => {
    const fetchProblems = async () => {
      try {
        setLoading(true);
        const data = await problemService.getAllProblems();
        setProblems(data);
      } catch (err) {
        setError('Failed to fetch problems from database.');
      } finally {
        setLoading(false);
      }
    };
    fetchProblems();
  }, []);

  // Derive unique sorted topics from all problems
  const allTopics = useMemo(() => {
    const topics = new Set(problems.map(p => p.topic).filter(Boolean));
    return ['ALL', ...Array.from(topics).sort()];
  }, [problems]);

  // Derived filtered list
  const filteredProblems = useMemo(() => {
    let result = problems;
    if (searchTerm.trim()) {
      const q = searchTerm.trim().toLowerCase();
      result = result.filter(p =>
        p.title.toLowerCase().includes(q) ||
        (p.topic && p.topic.toLowerCase().includes(q))
      );
    }
    if (difficultyFilter !== 'ALL') {
      result = result.filter(p => p.difficulty === difficultyFilter);
    }
    if (topicFilter !== 'ALL') {
      result = result.filter(p => p.topic === topicFilter);
    }
    if (solvedFilter === 'SOLVED') {
      result = result.filter(p => p.isSolved === true);
    } else if (solvedFilter === 'UNSOLVED') {
      result = result.filter(p => p.isSolved !== true);
    }
    return result;
  }, [problems, searchTerm, difficultyFilter, topicFilter, solvedFilter]);

  // Stats bar
  const solvedCount   = problems.filter(p => p.isSolved === true).length;
  const hasSolvedData = problems.some(p => p.isSolved !== undefined && p.isSolved !== null);

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>
          {t('problems.title', 'Practice Problems')}
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          {t('problems.subtitle', 'Explore coding problems, sharpen your algorithms, and test your skills.')}
          {hasSolvedData && (
            <span style={{ marginLeft: '1rem', fontWeight: 600, color: 'var(--primary)' }}>
              ✓ {solvedCount} / {problems.length} solved
            </span>
          )}
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* ── Filter Bar ── */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.5rem' }}>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'center' }}>

          {/* Search */}
          <div style={{ flex: 1, minWidth: '220px', display: 'flex', alignItems: 'center', position: 'relative' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px' }} />
            <input
              type="text"
              placeholder={t('problems.searchPlaceholder', 'Search by title or topic...')}
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
              className="form-control"
              style={{ width: '100%', paddingLeft: '2.4rem' }}
            />
          </div>

          {/* Difficulty filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Filter size={15} color="var(--text-muted)" />
            <select
              value={difficultyFilter}
              onChange={e => setDifficultyFilter(e.target.value)}
              className="form-control"
              style={{ padding: '0.75rem 1rem' }}
            >
              <option value="ALL">All Difficulties</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          {/* Topic filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Tag size={15} color="var(--text-muted)" />
            <select
              value={topicFilter}
              onChange={e => setTopicFilter(e.target.value)}
              className="form-control"
              style={{ padding: '0.75rem 1rem' }}
            >
              {allTopics.map(topic => (
                <option key={topic} value={topic}>
                  {topic === 'ALL' ? 'All Topics' : topic}
                </option>
              ))}
            </select>
          </div>

          {/* Solved / Unsolved filter — only shown when isSolved data is available */}
          {hasSolvedData && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <CheckCircle2 size={15} color="var(--text-muted)" />
              <select
                value={solvedFilter}
                onChange={e => setSolvedFilter(e.target.value)}
                className="form-control"
                style={{ padding: '0.75rem 1rem' }}
              >
                <option value="ALL">All Status</option>
                <option value="SOLVED">Solved</option>
                <option value="UNSOLVED">Unsolved</option>
              </select>
            </div>
          )}
        </div>
      </div>

      {/* ── Problems Table ── */}
      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
            Loading problems...
          </div>
        ) : filteredProblems.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '48px', textAlign: 'center' }}>Status</th>
                  <th style={{ width: '52px' }}>#</th>
                  <th>Title</th>
                  <th>Topic</th>
                  <th style={{ textAlign: 'center' }}>Difficulty</th>
                  <th style={{ textAlign: 'center' }}>Acceptance</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredProblems.map((prob, idx) => (
                  <tr key={prob.id}>
                    {/* Solved / unsolved indicator */}
                    <td style={{ textAlign: 'center' }}>
                      {prob.isSolved === true ? (
                        <CheckCircle2 size={17} color="#15803d" title="Solved" />
                      ) : prob.isSolved === false ? (
                        <Circle size={17} color="var(--border-color)" title="Not solved yet" />
                      ) : (
                        <span style={{ color: 'var(--border-color)', fontSize: '0.75rem' }}>–</span>
                      )}
                    </td>

                    <td style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{idx + 1}</td>

                    <td>
                      <Link
                        to={`/problems/${prob.id}`}
                        style={{ fontWeight: 600, color: prob.isSolved ? '#15803d' : 'var(--text-main)' }}
                      >
                        {prob.title}
                      </Link>
                    </td>

                    <td>
                      {prob.topic ? (
                        <span
                          onClick={() => setTopicFilter(prob.topic)}
                          style={{
                            fontSize: '0.8rem', color: 'var(--primary)', cursor: 'pointer',
                            padding: '0.15rem 0.5rem', borderRadius: '9999px',
                            backgroundColor: 'rgba(99,102,241,0.08)',
                          }}
                          title={`Filter by ${prob.topic}`}
                        >
                          {prob.topic}
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>General</span>
                      )}
                    </td>

                    <td style={{ textAlign: 'center' }}>
                      <DifficultyBadge difficulty={prob.difficulty} />
                    </td>

                    <td style={{ textAlign: 'center' }}>
                      {prob.acceptanceRate != null ? (
                        <span style={{
                          fontSize: '0.82rem', fontWeight: 600,
                          color: prob.acceptanceRate >= 60
                            ? '#15803d'
                            : prob.acceptanceRate >= 35
                              ? '#b45309'
                              : '#b91c1c',
                        }}>
                          {prob.acceptanceRate.toFixed(1)}%
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>–</span>
                      )}
                    </td>

                    <td style={{ textAlign: 'right' }}>
                      <Link to={`/problems/${prob.id}`} className="btn btn-primary btn-sm">
                        <span>{t('problems.solve', 'Solve')}</span>
                        <ArrowRight size={13} />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
            <Code size={32} style={{ marginBottom: '0.5rem' }} />
            <p>{t('problems.noProblemsFound', 'No problems found matching your criteria.')}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProblemsList;
