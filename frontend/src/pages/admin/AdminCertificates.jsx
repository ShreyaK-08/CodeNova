import React, { useState, useEffect } from 'react';
import certificateService from '../../services/certificateService';
import { Trophy, AlertCircle } from 'lucide-react';

const AdminCertificates = () => {
  const [certificates, setCertificates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const data = await certificateService.getAllCertificates();
        setCertificates(data);
      } catch (err) {
        setError('Failed to load certificates.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <Trophy color="#fbbf24" /> Certificates
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>All achievement certificates issued by the platform.</p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading...</div>
        ) : certificates.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Certificate ID</th>
                  <th>User</th>
                  <th>Achievement</th>
                  <th>Milestone</th>
                  <th>Level</th>
                  <th>Issued Date</th>
                  <th>Verification Code</th>
                </tr>
              </thead>
              <tbody>
                {certificates.map((c) => (
                  <tr key={c.id}>
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }}>{c.certificateNumber}</td>
                    <td>
                      <div style={{ fontWeight: 600 }}>{c.recipientName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>@{c.username}</div>
                    </td>
                    <td>{c.title}</td>
                    <td>{c.milestone}</td>
                    <td>
                      <span className="badge badge-tag">{c.achievementLevel}</span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {c.issuedAt ? new Date(c.issuedAt).toLocaleDateString() : ''}
                    </td>
                    <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {c.verificationCode}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <Trophy size={40} />
            <p>No certificates issued yet. They're created automatically once a student solves 50 distinct problems.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminCertificates;
