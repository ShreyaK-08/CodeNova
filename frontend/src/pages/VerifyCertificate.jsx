import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import certificateService from '../services/certificateService';
import { CheckCircle, XCircle, Award } from 'lucide-react';

const VerifyCertificate = () => {
  const { code } = useParams();
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await certificateService.verify(code);
        setResult(data);
      } catch (err) {
        setResult({ verified: false, message: 'Unable to verify this certificate right now.' });
      } finally {
        setLoading(false);
      }
    })();
  }, [code]);

  return (
    <div style={{ maxWidth: '520px', margin: '4rem auto' }}>
      <div className="card" style={{ textAlign: 'center' }}>
        <div style={{ fontWeight: 800, fontSize: '1.3rem', marginBottom: '1.5rem' }}>
          {'</> '}Code<span style={{ color: 'var(--primary)' }}>Nova</span>
        </div>

        {loading ? (
          <p style={{ color: 'var(--text-muted)' }}>Verifying...</p>
        ) : result?.verified ? (
          <>
            <CheckCircle size={48} color="var(--success)" style={{ marginBottom: '0.75rem' }} />
            <h2 style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--success)', marginBottom: '1.5rem' }}>
              Certificate Verified
            </h2>
            <div style={{ textAlign: 'left', display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              <Row label="Recipient" value={result.recipientName} />
              <Row label="Achievement" value={result.title} icon={<Award size={14} color="var(--primary)" />} />
              <Row label="Achievement Level" value={result.achievementLevel} />
              <Row label="Issued By" value="CodeNova" />
              <Row label="Certificate ID" value={result.certificateNumber} />
              <Row label="Issued Date" value={result.issuedAt ? new Date(result.issuedAt).toLocaleDateString() : ''} />
              <Row label="Verification Code" value={result.verificationCode} mono />
            </div>
          </>
        ) : (
          <>
            <XCircle size={48} color="var(--danger)" style={{ marginBottom: '0.75rem' }} />
            <h2 style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--danger)', marginBottom: '0.5rem' }}>
              Certificate Not Found
            </h2>
            <p style={{ color: 'var(--text-muted)' }}>
              {result?.message || 'This verification code does not match any issued certificate.'}
            </p>
          </>
        )}
      </div>
    </div>
  );
};

const Row = ({ label, value, icon, mono }) => (
  <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
    <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>{label}</span>
    <span style={{ fontWeight: 600, fontFamily: mono ? 'var(--font-mono)' : 'inherit', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
      {icon}{value}
    </span>
  </div>
);

export default VerifyCertificate;
