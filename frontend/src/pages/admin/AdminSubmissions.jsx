import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import adminService from '../../services/adminService';
import { FileCheck, AlertCircle } from 'lucide-react';

const AdminSubmissions = () => {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        setLoading(true);
        const data = await adminService.getAllSubmissions();
        setSubmissions(data);
      } catch (err) {
        setError('Failed to load platform submissions.');
      } finally {
        setLoading(false);
      }
    };

    fetchSubmissions();
  }, []);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>User Submissions</h1>
        <p style={{ color: 'var(--text-muted)' }}>View and monitor coding submissions from platform users.</p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div className="empty-state">Loading submissions...</div>
        ) : submissions.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Username</th>
                  <th>Problem</th>
                  <th>Language</th>
                  <th>Verdict</th>
                  <th style={{ textAlign: 'center' }}>Score</th>
                  <th style={{ textAlign: 'center' }}>Execution Time</th>
                  <th style={{ textAlign: 'right' }}>Submission Time</th>
                </tr>
              </thead>
              <tbody>
                {submissions
                  .slice()
                  .sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt))
                  .map((sub) => {
                    const isAccepted = sub.status === 'ACCEPTED';
                    return (
                      <tr key={sub.id}>
                        <td style={{ color: 'var(--text-muted)' }}>{sub.id}</td>
                        <td style={{ fontWeight: 600 }}>@{sub.username}</td>
                        <td>
                          <Link to={`/problems/${sub.problemId}`} style={{ color: 'var(--text-main)', fontWeight: 500 }}>
                            {sub.problemTitle || `Problem #${sub.problemId}`}
                          </Link>
                        </td>
                        <td style={{ textTransform: 'capitalize' }}>{sub.language}</td>
                        <td>
                          <span className={`badge ${isAccepted ? 'badge-easy' : 'badge-hard'}`}>
                            {sub.status}
                          </span>
                        </td>
                        <td style={{ textAlign: 'center', fontWeight: 600 }}>
                          {sub.passedTestCases != null && sub.totalTestCases != null
                            ? `${sub.passedTestCases}/${sub.totalTestCases}`
                            : (isAccepted ? 'Passed' : 'Failed')}
                        </td>
                        <td style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          {sub.executionTime != null ? `${sub.executionTime} ms` : '—'}
                        </td>
                        <td style={{ textAlign: 'right', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          {sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '—'}
                        </td>
                      </tr>
                    );
                  })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <FileCheck size={40} />
            <p>No submissions recorded yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminSubmissions;
