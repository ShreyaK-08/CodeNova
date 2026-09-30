import React, { useState, useEffect } from 'react';
import userService from '../../services/userService';
import { FileCheck, Clock, AlertCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const MySubmissions = () => {
  const { t } = useTranslation();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        setLoading(true);
        const data = await userService.getUserSubmissions();
        setSubmissions(data);
      } catch (err) {
        setError(t('submissions.failedLoad', 'Failed to load submission history.'));
      } finally {
        setLoading(false);
      }
    };

    fetchSubmissions();
  }, [t]);

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>{t('submissions.title', 'My Submissions')}</h1>
        <p style={{ color: 'var(--text-muted)' }}>{t('submissions.subtitle', 'Complete history of your problem attempts and judging results.')}</p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('submissions.loading', 'Loading submissions...')}</div>
        ) : submissions.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>{t('submissions.number', '#')}</th>
                  <th>{t('submissions.problem', 'Problem')}</th>
                  <th>{t('submissions.status', 'Status')}</th>
                  <th>{t('submissions.language', 'Language')}</th>
                  <th>{t('submissions.runtime', 'Runtime')}</th>
                  <th>{t('submissions.memory', 'Memory')}</th>
                  <th>{t('submissions.date', 'Date')}</th>
                </tr>
              </thead>
              <tbody>
                {submissions.map((sub, idx) => (
                  <tr key={sub.id}>
                    <td style={{ color: 'var(--text-muted)' }}>{sub.id}</td>
                    <td style={{ fontWeight: 600 }}>
                      <Link to={`/problems/${sub.problemId}`} style={{ color: 'var(--text-main)' }}>
                        {sub.problemTitle}
                      </Link>
                    </td>
                    <td>
                      <span className={`badge ${sub.status === 'ACCEPTED' ? 'badge-easy' : 'badge-hard'}`}>
                        {sub.status}
                      </span>
                    </td>
                    <td>{sub.language}</td>
                    <td>{sub.executionTime ? `${sub.executionTime} ms` : '-'}</td>
                    <td>{sub.memoryUsed ? `${sub.memoryUsed} KB` : '-'}</td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {new Date(sub.submittedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
            <FileCheck size={40} style={{ margin: '0 auto 1rem auto', opacity: 0.5 }} />
            <p>{t('submissions.noSubmissions', 'You have not submitted any solutions yet.')}</p>
            <Link to="/problems" className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
              {t('submissions.solveAProblem', 'Solve a Problem')}
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default MySubmissions;
