import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import assessmentService from '../../services/assessmentService';
import {
  ClipboardList,
  Clock,
  Award,
  AlertCircle,
  RefreshCw,
  HelpCircle,
  ShieldCheck,
} from 'lucide-react';

/**
 * Student assessment list (Task 12, Phase 2). Only ever shows PUBLISHED assessments -
 * GET /api/assessments (listPublishedAssessments) already filters to that status
 * server-side, so there is nothing to filter here; a DRAFT/ARCHIVED assessment is
 * never returned by this endpoint at all.
 */
const Assessments = () => {
  const { t } = useTranslation();
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const loadAssessments = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await assessmentService.getPublishedAssessments();
      setAssessments(data);
    } catch (err) {
      setError(t('assessments.loadError', 'Unable to load assessments. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAssessments();
  }, []);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ClipboardList size={26} color="#8b5cf6" /> {t('assessments.title', 'Skill Assessments')}
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>{t('assessments.subtitle', 'Test your knowledge with timed skill assessments.')}</p>
        </div>
        <Link to="/assessments/host" className="btn btn-outline btn-sm">
          <ShieldCheck size={16} />
          <span>{t('assessments.hostAssessment', 'Host Assessment')}</span>
        </Link>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('assessments.loading', 'Loading assessments...')}</div>
      ) : error ? (
        <div className="card">
          <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
          <button onClick={loadAssessments} className="btn btn-outline btn-sm">
            <RefreshCw size={14} />
            <span>{t('common.retry', 'Retry')}</span>
          </button>
        </div>
      ) : assessments.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <ClipboardList size={40} />
            <p>{t('assessments.noAssessments', 'No assessments available right now.')}</p>
          </div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.25rem' }}>
          {assessments.map((assessment) => {
            const passingPct = assessment.totalMarks > 0
              ? Math.round((assessment.passingMarks / assessment.totalMarks) * 100)
              : null;
            return (
              <div key={assessment.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem', gap: '0.5rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>{assessment.title}</h3>
                  <span style={{
                    fontSize: '0.7rem', fontWeight: 700, padding: '0.2rem 0.55rem', borderRadius: '9999px',
                    color: 'var(--primary)', backgroundColor: 'var(--primary-soft)', whiteSpace: 'nowrap',
                  }}>
                    {t('assessments.published', 'Published')}
                  </span>
                </div>

                {assessment.description && (
                  <p style={{
                    color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.75rem',
                    display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden',
                  }}>
                    {assessment.description}
                  </p>
                )}

                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.9rem', fontSize: '0.8rem', color: 'var(--text-subtle)', marginBottom: '0.9rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Clock size={13} /> {assessment.durationMinutes} {t('assessments.mins', 'min')}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <HelpCircle size={13} /> {assessment.questionCount ?? 0} {(assessment.questionCount ?? 0) === 1 ? t('assessments.question', 'question') : t('assessments.questions', 'questions')}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                    <Award size={13} /> {t('assessments.pass', 'Pass')}: {assessment.passingMarks}/{assessment.totalMarks}
                    {passingPct != null && ` (${passingPct}%)`}
                  </span>
                </div>

                <button
                  onClick={() => navigate(`/assessments/${assessment.id}/attempt`)}
                  className="btn btn-primary btn-sm"
                  style={{ marginTop: 'auto' }}
                  disabled={!assessment.questionCount}
                  title={!assessment.questionCount ? t('assessments.noQuestions', 'This assessment has no questions yet.') : undefined}
                >
                  {t('assessments.startAssessment', 'Start Assessment')}
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Assessments;
