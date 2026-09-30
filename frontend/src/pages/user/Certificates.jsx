import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import certificateService from '../../services/certificateService';
import {
  Trophy,
  Award,
  Download,
  Eye,
  Lock,
  X,
  Copy,
  Check,
  CheckCircle,
  Filter,
  Sparkles,
  Code2,
} from 'lucide-react';

const LEVEL_COLORS = {
  Bronze: '#cd7f32',
  Silver: '#94a3b8',
  Gold: '#fbbf24',
  Platinum: '#a78bfa',
  Diamond: '#38bdf8',
  Specialist: '#8b5cf6',
};

const Certificates = () => {
  const { t } = useTranslation();
  const [progress, setProgress] = useState(null);
  const [allCertificates, setAllCertificates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [previewCert, setPreviewCert] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);
  const [copied, setCopied] = useState(false);
  const [filterCategory, setFilterCategory] = useState('ALL'); // 'ALL' | 'OVERALL' | 'LANGUAGE'

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const [progData, certsData] = await Promise.all([
          certificateService.getProgress().catch(() => null),
          certificateService.getMyCertificates().catch(() => []),
        ]);
        setProgress(progData);
        setAllCertificates(Array.isArray(certsData) ? certsData : []);
      } catch (err) {
        setError(t('common.networkError', 'Failed to load certificates.'));
      } finally {
        setLoading(false);
      }
    })();
  }, [t]);

  const handleDownload = async (cert) => {
    setDownloadingId(cert.id);
    try {
      const blob = await certificateService.downloadPdf(cert.id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `codenova-certificate-${cert.certificateNumber || cert.id}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Failed to download certificate PDF.');
    } finally {
      setDownloadingId(null);
    }
  };

  const copyVerificationLink = (code) => {
    const link = `${window.location.origin}/verify-certificate/${code}`;
    navigator.clipboard?.writeText(link);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  const filteredCerts = allCertificates.filter((c) => {
    if (filterCategory === 'ALL') return true;
    if (filterCategory === 'LANGUAGE') return c.category === 'LANGUAGE' || !!c.language;
    if (filterCategory === 'OVERALL') return c.category === 'OVERALL' || !c.language;
    return true;
  });

  const earnedByMilestone = {};
  allCertificates.forEach((c) => {
    if (c.milestone && (!c.category || c.category === 'OVERALL')) {
      earnedByMilestone[c.milestone] = c;
    }
  });

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-muted)' }}>
        {t('common.loading', 'Loading certificates...')}
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', paddingBottom: '3rem' }}>
      <div style={{ marginBottom: '1.75rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--text-main)' }}>
            <Trophy color="#fbbf24" /> {t('certificates.title', 'Certificates & Achievements')}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginTop: '0.25rem' }}>
            {t('certificates.subtitle', 'Earn authenticated industry-standard certificates by reaching problem-solving milestones and mastering programming languages.')}
          </p>
        </div>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
          <span>{error}</span>
        </div>
      )}

      {/* Achievement Progress banner */}
      {progress && (
        <div className="card" style={{ marginBottom: '2rem', background: 'linear-gradient(135deg, rgba(139,92,246,0.08), transparent)', border: '1px solid var(--border-color)', padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 600 }}>{t('certificates.achievementProgress', 'Achievement Progress')}</div>
              <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-main)' }}>
                {progress.solvedCount} / {progress.nextMilestone} {t('certificates.problemsSolved', 'Problems Solved')}
              </div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '0.9rem', color: 'var(--primary)', fontWeight: 600 }}>
                {progress.remainingToNextMilestone > 0
                  ? t('certificates.moreToUnlock', { count: progress.remainingToNextMilestone, defaultValue: `${progress.remainingToNextMilestone} more to unlock next milestone certificate` })
                  : t('certificates.milestoneAchieved', 'Milestone achieved!')}
              </div>
            </div>
          </div>
          <div style={{ height: '10px', borderRadius: '9999px', background: 'var(--bg-input, #e2e8f0)', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
            <div
              style={{
                height: '100%',
                width: `${progress.progressPercent || 0}%`,
                background: 'linear-gradient(90deg, var(--primary), var(--primary-hover))',
                transition: 'width 0.4s ease',
              }}
            />
          </div>
        </div>
      )}

      {/* EARNED CERTIFICATES SECTION */}
      <div style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Award size={20} color="var(--primary)" />
              {t('certificates.earnedCertificates', 'Earned Certificates')} ({allCertificates.length})
            </h2>
            <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '0.2rem', display: 'block' }}>
              {t('certificates.verifiedCredentialsDesc', 'Verified credentials issued in your name with unique IDs and verification codes')}
            </span>
          </div>

          {/* Filter Pills */}
          <div style={{ display: 'flex', background: 'var(--bg-main)', borderRadius: '8px', padding: '3px' }}>
            <button
              onClick={() => setFilterCategory('ALL')}
              style={{
                padding: '0.35rem 0.75rem',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.8rem',
                fontWeight: 600,
                cursor: 'pointer',
                backgroundColor: filterCategory === 'ALL' ? 'var(--bg-card, #fff)' : 'transparent',
                color: filterCategory === 'ALL' ? 'var(--primary)' : 'var(--text-muted)',
                boxShadow: filterCategory === 'ALL' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              }}
            >
              {t('certificates.all', 'All')} ({allCertificates.length})
            </button>
            <button
              onClick={() => setFilterCategory('OVERALL')}
              style={{
                padding: '0.35rem 0.75rem',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.8rem',
                fontWeight: 600,
                cursor: 'pointer',
                backgroundColor: filterCategory === 'OVERALL' ? 'var(--bg-card, #fff)' : 'transparent',
                color: filterCategory === 'OVERALL' ? 'var(--primary)' : 'var(--text-muted)',
                boxShadow: filterCategory === 'OVERALL' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              }}
            >
              {t('certificates.milestones', 'Milestones')} ({allCertificates.filter((c) => !c.language || c.category === 'OVERALL').length})
            </button>
            <button
              onClick={() => setFilterCategory('LANGUAGE')}
              style={{
                padding: '0.35rem 0.75rem',
                border: 'none',
                borderRadius: '6px',
                fontSize: '0.8rem',
                fontWeight: 600,
                cursor: 'pointer',
                backgroundColor: filterCategory === 'LANGUAGE' ? 'var(--bg-card, #fff)' : 'transparent',
                color: filterCategory === 'LANGUAGE' ? 'var(--primary)' : 'var(--text-muted)',
                boxShadow: filterCategory === 'LANGUAGE' ? '0 1px 3px rgba(0,0,0,0.1)' : 'none',
              }}
            >
              {t('certificates.languageSpecialists', 'Language Specialists')} ({allCertificates.filter((c) => c.category === 'LANGUAGE' || !!c.language).length})
            </button>
          </div>
        </div>

        {filteredCerts.length > 0 ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.25rem' }}>
            {filteredCerts.map((cert) => {
              const isLang = cert.category === 'LANGUAGE' || !!cert.language;
              const color = LEVEL_COLORS[cert.achievementLevel] || (isLang ? '#8b5cf6' : 'var(--primary)');
              return (
                <div
                  key={cert.id}
                  className="card"
                  style={{
                    padding: '1.25rem',
                    borderRadius: '12px',
                    border: `1px solid ${color}44`,
                    position: 'relative',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
                      <span
                        style={{
                          padding: '0.2rem 0.55rem',
                          borderRadius: '9999px',
                          fontSize: '0.72rem',
                          fontWeight: 700,
                          backgroundColor: `${color}18`,
                          color: color,
                          border: `1px solid ${color}44`,
                        }}
                      >
                        {isLang ? `${cert.language || 'Language'} Specialist` : cert.achievementLevel || 'Milestone'}
                      </span>
                      <CheckCircle size={18} color="#10b981" />
                    </div>

                    <h3 style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
                      {cert.title || (isLang ? `${cert.language} Problem Solver` : `${cert.milestone} Problems Solved`)}
                    </h3>

                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.75rem', lineHeight: '1.4' }}>
                      {cert.description || `Successfully solved ${cert.milestone} coding problems on CodeNova.`}
                    </p>

                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginBottom: '0.35rem' }}>
                      Certificate ID: <strong style={{ color: 'var(--text-muted)' }}>{cert.certificateNumber}</strong>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginBottom: '1rem' }}>
                      Issued: {cert.issuedAt ? new Date(cert.issuedAt).toLocaleDateString() : 'Active'}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button
                      className="btn btn-outline btn-sm"
                      style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.35rem' }}
                      onClick={() => setPreviewCert(cert)}
                    >
                      <Eye size={14} /> <span>View</span>
                    </button>
                    <button
                      className="btn btn-primary btn-sm"
                      style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.35rem' }}
                      onClick={() => handleDownload(cert)}
                      disabled={downloadingId === cert.id}
                    >
                      <Download size={14} /> <span>{downloadingId === cert.id ? '...' : 'PDF'}</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="empty-state" style={{ padding: '3rem 1rem', textAlign: 'center', background: 'var(--bg-main)', borderRadius: '12px' }}>
            <Award size={40} color="var(--text-muted)" />
            <p style={{ marginTop: '0.75rem', color: 'var(--text-muted)', fontSize: '0.95rem' }}>
              No earned certificates found in this category yet.
            </p>
            <p style={{ fontSize: '0.825rem', color: 'var(--text-subtle)' }}>
              Solve 5 problems in any programming language (C, C++, Java, Python, JavaScript, SQL) or reach 10 problems solved to earn your first certificate.
            </p>
          </div>
        )}
      </div>

      {/* MILESTONE ROADMAP CARDS */}
      <div>
        <div style={{ marginBottom: '1.25rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--text-main)', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Sparkles size={20} color="#fbbf24" />
            Milestone Certification Roadmap
          </h2>
          <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)', marginTop: '0.2rem', display: 'block' }}>
            Standard lifetime milestones for overall problem solving
          </span>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
            gap: '1rem',
          }}
        >
          {(progress?.milestoneCards || []).map((card) => {
            const earned = card.status === 'EARNED';
            const inProgress = card.status === 'IN_PROGRESS';
            const color = LEVEL_COLORS[card.achievementLevel] || 'var(--primary)';
            const certDetail = earnedByMilestone[card.milestone];

            return (
              <div
                key={card.milestone}
                className="card"
                style={{
                  textAlign: 'center',
                  opacity: card.status === 'LOCKED' ? 0.55 : 1,
                  border: earned ? `1px solid ${color}66` : undefined,
                  position: 'relative',
                  padding: '1.25rem',
                }}
              >
                {earned && (
                  <div style={{ position: 'absolute', top: '0.75rem', right: '0.75rem', color: 'var(--success)' }}>
                    <CheckCircle size={18} />
                  </div>
                )}
                <div style={{ fontSize: '2rem', fontWeight: 800, color }}>{card.milestone}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.6rem' }}>
                  Problems Solved
                </div>
                <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 700, color }}>
                  {card.status === 'LOCKED' ? <Lock size={16} /> : <Award size={16} />}
                  <span>{card.achievementLevel}</span>
                </div>

                <div style={{ marginTop: '1rem' }}>
                  {earned && certDetail ? (
                    <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => setPreviewCert(certDetail)}>
                        <Eye size={14} /> <span>{t('common.view', 'View')}</span>
                      </button>
                      <button
                        className="btn btn-primary btn-sm"
                        onClick={() => handleDownload(certDetail)}
                        disabled={downloadingId === certDetail.id}
                      >
                        <Download size={14} /> <span>{downloadingId === certDetail.id ? '...' : 'PDF'}</span>
                      </button>
                    </div>
                  ) : inProgress ? (
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {progress.remainingToNextMilestone} more to go
                    </span>
                  ) : (
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>Locked</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Certificate preview modal */}
      {previewCert && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.75)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem',
          }}
        >
          <div className="card" style={{ maxWidth: '640px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setPreviewCert(null)}
                style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={20} />
              </button>
            </div>
            <div
              style={{
                border: '3px solid var(--primary)',
                borderRadius: 'var(--radius-lg, 12px)',
                padding: '2rem',
                textAlign: 'center',
                backgroundColor: 'var(--bg-card, #ffffff)',
              }}
            >
              <div style={{ fontWeight: 800, fontSize: '1.4rem', marginBottom: '0.2rem' }}>
                {'</> '}Code<span style={{ color: 'var(--primary)' }}>Nova</span>
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', fontStyle: 'italic', marginBottom: '1.5rem' }}>
                Learn. Code. Solve. Achieve.
              </div>
              <div style={{ fontSize: '1.1rem', fontWeight: 700, letterSpacing: '0.05em', marginBottom: '1.5rem' }}>
                CERTIFICATE OF ACHIEVEMENT
              </div>
              <div style={{ color: 'var(--text-muted)', marginBottom: '0.5rem' }}>This certificate is proudly presented to</div>
              <div style={{ fontSize: '1.6rem', fontWeight: 800, marginBottom: '1rem', color: 'var(--text-main)' }}>
                {previewCert.recipientName}
              </div>
              <div style={{ color: 'var(--text-muted)', marginBottom: '0.25rem' }}>for demonstrating verified excellence in</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '0.75rem' }}>
                {previewCert.title || `${previewCert.milestone} PROBLEMS SOLVED`}
              </div>

              {previewCert.description && (
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', maxWidth: '460px', margin: '0 auto 1.25rem', lineHeight: '1.4' }}>
                  {previewCert.description}
                </p>
              )}

              <div style={{ fontWeight: 700, marginBottom: '1.25rem', color: 'var(--text-main)' }}>
                Level: <span style={{ color: LEVEL_COLORS[previewCert.achievementLevel] || 'var(--primary)' }}>{previewCert.achievementLevel || 'Certified'}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--text-muted)', flexWrap: 'wrap', gap: '0.5rem', borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
                <span>Date: {previewCert.issuedAt ? new Date(previewCert.issuedAt).toLocaleDateString() : ''}</span>
                <span>Certificate ID: {previewCert.certificateNumber}</span>
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.4rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem' }}>
                <span>Verification Code: {previewCert.verificationCode}</span>
                <button
                  onClick={() => copyVerificationLink(previewCert.verificationCode)}
                  style={{ background: 'transparent', border: 'none', color: 'var(--primary)', cursor: 'pointer', display: 'inline-flex' }}
                  title="Copy verification link"
                >
                  {copied ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
                </button>
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1.25rem' }}>
              <button
                className="btn btn-primary"
                onClick={() => handleDownload(previewCert)}
                disabled={downloadingId === previewCert.id}
              >
                <Download size={16} />
                <span>{downloadingId === previewCert.id ? 'Preparing...' : t('certificates.downloadPdf', 'Download Certificate')}</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Certificates;
