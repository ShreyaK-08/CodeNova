import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import assessmentHostService from '../../services/assessmentHostService';
import {
  ShieldCheck,
  AlertCircle,
  CheckCircle,
  XCircle,
  FileText,
  RefreshCw,
  ExternalLink,
  X,
  Building2,
  FileCheck,
  UserCheck,
  Globe,
  Mail,
  Phone,
  ClipboardCheck,
  User,
  Calendar,
} from 'lucide-react';

const STATUS_STYLES = {
  PENDING: { color: '#b45309', bg: 'rgba(245, 158, 11, 0.15)', label: 'Pending Review' },
  APPROVED: { color: 'var(--primary)', bg: 'var(--primary-soft)', label: 'Approved (Awaiting Code)' },
  REJECTED: { color: 'var(--danger)', bg: 'rgba(239, 68, 68, 0.12)', label: 'Rejected' },
  VERIFIED: { color: '#15803d', bg: 'rgba(34, 197, 94, 0.15)', label: 'Verified Host' },
};

/**
 * Admin review UI for the "Host Assessment" Business/Organization verification workflow.
 */
const AssessmentHostVerification = () => {
  const { t } = useTranslation();
  const [verifications, setVerifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [busyId, setBusyId] = useState(null);

  const [previewUrl, setPreviewUrl] = useState(null);
  const [previewType, setPreviewType] = useState('');
  const [previewTitle, setPreviewTitle] = useState('');

  const [rejectTargetId, setRejectTargetId] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectError, setRejectError] = useState('');

  const [reviewTarget, setReviewTarget] = useState(null);

  const fetchVerifications = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await assessmentHostService.getAdminVerifications();
      setVerifications(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load verification requests.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVerifications();
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handlePreview = async (id, docType = 'primary', label = 'Document') => {
    try {
      setError('');
      const { url, contentType } = await assessmentHostService.getDocumentBlobUrl(id, docType);
      setPreviewUrl(url);
      setPreviewType(contentType);
      setPreviewTitle(label);
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load the requested document.');
    }
  };

  const closePreview = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl(null);
    setPreviewType('');
    setPreviewTitle('');
  };

  const handleApprove = async (id) => {
    setBusyId(id);
    setError('');
    setSuccessMessage('');
    try {
      const result = await assessmentHostService.approveVerification(id);
      const emailNote = result?.emailDeliveryStatus === 'SENT'
        ? ' Approval notification email sent to user.'
        : result?.emailDeliveryMessage ? ` (${result.emailDeliveryMessage})` : '';
      setSuccessMessage(`Verification approved! 6-digit verification code has been dispatched to ${result?.userEmail || 'applicant email'}.${emailNote} (Please check your Inbox as well as Spam/Promotions folder).`);
      await fetchVerifications();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to approve this request.');
    } finally {
      setBusyId(null);
    }
  };

  const handleDirectVerify = async (id, orgName) => {
    if (!window.confirm(`Directly verify "${orgName || 'this host'}"? The user will immediately be granted permanent Host status without requiring code entry.`)) return;
    setBusyId(id);
    setError('');
    setSuccessMessage('');
    try {
      const result = await assessmentHostService.directVerifyVerification(id);
      const emailNote = result?.emailDeliveryStatus === 'SENT'
        ? ' Approval notification email sent to user.'
        : result?.emailDeliveryMessage ? ` (${result.emailDeliveryMessage})` : '';
      setSuccessMessage(`Verified host successfully! Permanent host status activated.${emailNote}`);
      await fetchVerifications();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to direct verify this host.');
    } finally {
      setBusyId(null);
    }
  };

  const openRejectForm = (id) => {
    setRejectTargetId(id);
    setRejectReason('');
    setRejectError('');
  };

  const cancelReject = () => {
    setRejectTargetId(null);
    setRejectReason('');
    setRejectError('');
  };

  const submitReject = async () => {
    const trimmed = rejectReason.trim();
    if (!trimmed || trimmed.length < 5) {
      setRejectError('Please provide a meaningful rejection reason explaining what needs correction (minimum 5 characters).');
      return;
    }
    setBusyId(rejectTargetId);
    try {
      const result = await assessmentHostService.rejectVerification(rejectTargetId, trimmed);
      const emailNote = result?.emailDeliveryStatus === 'SENT'
        ? ' Notification email sent to applicant.'
        : result?.emailDeliveryMessage ? ` (${result.emailDeliveryMessage})` : '';
      setSuccessMessage(`Verification request rejected.${emailNote}`);
      cancelReject();
      await fetchVerifications();
    } catch (err) {
      setRejectError(err.response?.data?.message || 'Failed to reject this request.');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.6rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Building2 size={24} color="#8b5cf6" /> {t('admin.hostVerification', 'Assessment Host Verification')}
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          {t('admin.hostVerificationSubtitle', 'Review business & organization credentials submitted by users requesting assessment hosting privileges.')}
        </p>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
      {successMessage && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          <CheckCircle size={18} />
          <span>{successMessage}</span>
        </div>
      )}

      <div className="card">
        {loading ? (
          <div className="empty-state">{t('common.loading', 'Loading verification requests...')}</div>
        ) : verifications.length === 0 ? (
          <div className="empty-state">
            <ShieldCheck size={40} />
            <p>{t('common.noData', 'No verification requests yet.')}</p>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>{t('admin.user', 'Organization & User')}</th>
                  <th>Type & Registration</th>
                  <th>Representative & Contact</th>
                  <th>Purpose & Country</th>
                  <th>{t('admin.viewDocument', 'Verification Documents')}</th>
                  <th>{t('common.status', 'Status')}</th>
                  <th>{t('common.action', 'Action')}</th>
                </tr>
              </thead>
              <tbody>
                {verifications.map((v) => {
                  const statusStyle = STATUS_STYLES[v.status] || { color: 'var(--text-muted)', bg: 'var(--bg-input)', label: v.status };
                  const isBusy = busyId === v.id;
                  return (
                    <tr key={v.id}>
                      <td>
                        <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>
                          {v.organizationName || 'Personal / Unnamed Org'}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                          User: <strong>{v.userName}</strong> (@{v.username})
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {v.userEmail}
                        </div>
                        {v.website && (
                          <div style={{ marginTop: '0.2rem' }}>
                            <a
                              href={v.website.startsWith('http') ? v.website : `https://${v.website}`}
                              target="_blank"
                              rel="noopener noreferrer"
                              style={{ fontSize: '0.75rem', color: 'var(--primary)', display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}
                            >
                              <Globe size={11} /> Website
                            </a>
                          </div>
                        )}
                      </td>

                      <td>
                        <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{v.organizationType || 'Not specified'}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          Reg #: <code>{v.registrationNumber || '-'}</code>
                        </div>
                      </td>

                      <td>
                        <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{v.representativeName || '-'}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          <Phone size={11} style={{ display: 'inline', marginRight: '3px' }} /> {v.contactNumber || '-'}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          <Mail size={11} style={{ display: 'inline', marginRight: '3px' }} /> {v.officialEmail || '-'}
                        </div>
                      </td>

                      <td>
                        <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{v.purpose || '-'}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                          {v.country || '-'}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '0.2rem' }}>
                          {v.submittedAt ? new Date(v.submittedAt).toLocaleDateString() : '-'}
                        </div>
                      </td>

                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                          {v.documentAvailable ? (
                            <button
                              className="btn btn-outline btn-sm"
                              style={{ justifyContent: 'flex-start', fontSize: '0.78rem', padding: '0.2rem 0.5rem' }}
                              onClick={() => handlePreview(v.id, 'primary', `Primary Certificate (${v.documentOriginalName || 'Document'})`)}
                            >
                              <FileCheck size={12} color="var(--primary)" />
                              <span>{v.documentOriginalName || 'Primary Doc'}</span>
                            </button>
                          ) : (
                            <span style={{ color: 'var(--text-muted)', fontSize: '0.78rem' }}>No primary doc</span>
                          )}

                          {v.gstDocAvailable && (
                            <button
                              className="btn btn-outline btn-sm"
                              style={{ justifyContent: 'flex-start', fontSize: '0.78rem', padding: '0.2rem 0.5rem' }}
                              onClick={() => handlePreview(v.id, 'gst', `GST Certificate (${v.gstOriginalName})`)}
                            >
                              <FileText size={12} />
                              <span>GST: {v.gstOriginalName || 'Document'}</span>
                            </button>
                          )}

                          {v.authLetterDocAvailable && (
                            <button
                              className="btn btn-outline btn-sm"
                              style={{ justifyContent: 'flex-start', fontSize: '0.78rem', padding: '0.2rem 0.5rem' }}
                              onClick={() => handlePreview(v.id, 'auth-letter', `Auth Letter (${v.authLetterOriginalName})`)}
                            >
                              <FileText size={12} />
                              <span>Auth Letter</span>
                            </button>
                          )}

                          {v.supportingDocAvailable && (
                            <button
                              className="btn btn-outline btn-sm"
                              style={{ justifyContent: 'flex-start', fontSize: '0.78rem', padding: '0.2rem 0.5rem' }}
                              onClick={() => handlePreview(v.id, 'supporting', `Supporting Document (${v.supportingDocOriginalName})`)}
                            >
                              <FileText size={12} />
                              <span>Supporting Doc</span>
                            </button>
                          )}
                        </div>
                      </td>

                      <td>
                        <span className="badge" style={{ color: statusStyle.color, background: statusStyle.bg }}>
                          {statusStyle.label}
                        </span>
                        {v.status === 'REJECTED' && v.rejectionReason && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem', maxWidth: 180 }}>
                            Reason: {v.rejectionReason}
                          </div>
                        )}
                      </td>

                      <td>
                        {rejectTargetId === v.id ? (
                          <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '0.5rem',
                            minWidth: 260,
                            padding: '0.75rem',
                            backgroundColor: 'var(--bg-main)',
                            border: '1px solid var(--border-color)',
                            borderRadius: 'var(--radius-md)'
                          }}>
                            <label style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--danger)' }}>
                              Rejection Reason *
                            </label>
                            <textarea
                              className="form-input"
                              rows={3}
                              placeholder="Explain why the verification could not be approved, for example: document is unclear, organization details do not match, registration information is incomplete..."
                              value={rejectReason}
                              onChange={(e) => setRejectReason(e.target.value)}
                              style={{ fontSize: '0.82rem', padding: '0.4rem', lineHeight: '1.4' }}
                            />
                            {rejectError && (
                              <span style={{ color: 'var(--danger)', fontSize: '0.78rem', fontWeight: 600 }}>
                                {rejectError}
                              </span>
                            )}
                            <div style={{ fontSize: '0.76rem', color: 'var(--text-muted)', fontWeight: 500 }}>
                              Reject verification and notify applicant?
                            </div>
                            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.2rem' }}>
                              <button className="btn btn-outline btn-sm" disabled={isBusy} onClick={cancelReject}>
                                Cancel
                              </button>
                              <button
                                className="btn btn-danger btn-sm"
                                disabled={isBusy}
                                onClick={submitReject}
                                style={{ fontWeight: 700 }}
                              >
                                Reject &amp; Notify
                              </button>
                            </div>
                          </div>
                        ) : (
                          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                            <button
                              className="btn btn-outline btn-sm"
                              disabled={isBusy}
                              onClick={() => setReviewTarget(v)}
                              title="Review all organization details and credentials"
                              style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                            >
                              <ClipboardCheck size={14} color="var(--primary)" />
                              <span>Review Details</span>
                            </button>
                            {v.status !== 'VERIFIED' && (
                              <button
                                className="btn btn-success btn-sm"
                                disabled={isBusy}
                                onClick={() => handleDirectVerify(v.id, v.organizationName)}
                                title="Directly verify and activate permanent Host status"
                                style={{ backgroundColor: '#15803d', borderColor: '#15803d', color: '#fff' }}
                              >
                                <UserCheck size={14} />
                                <span>Direct Verify</span>
                              </button>
                            )}
                            {(v.status === 'PENDING' || v.status === 'APPROVED') && (
                              <button
                                className="btn btn-primary btn-sm"
                                disabled={isBusy}
                                onClick={() => handleApprove(v.id)}
                                title={v.status === 'APPROVED' ? 'Resend a fresh code' : 'Approve & Send Code'}
                              >
                                <CheckCircle size={14} />
                                <span>{v.status === 'APPROVED' ? 'Resend Code' : t('admin.approve', 'Approve')}</span>
                              </button>
                            )}
                            {v.status !== 'REJECTED' && v.status !== 'VERIFIED' && (
                              <button className="btn btn-outline btn-sm" disabled={isBusy} onClick={() => openRejectForm(v.id)}>
                                <XCircle size={14} />
                                <span>{t('admin.reject', 'Reject')}</span>
                              </button>
                            )}
                            {v.status === 'VERIFIED' && (
                              <span style={{ color: '#15803d', fontSize: '0.8rem', fontWeight: 600 }}>{t('admin.verifiedHost', 'Verified Host')}</span>
                            )}
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
        <div style={{ marginTop: '1rem' }}>
          <button className="btn btn-outline btn-sm" onClick={fetchVerifications}>
            <RefreshCw size={14} />
            <span>{t('common.refresh', 'Refresh')}</span>
          </button>
        </div>
      </div>

      {/* Comprehensive Host Review & Approval Dossier Modal */}
      {reviewTarget && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0, 0, 0, 0.65)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 999,
            padding: '1rem',
          }}
          onClick={() => setReviewTarget(null)}
        >
          <div
            className="card"
            style={{
              maxWidth: '860px',
              width: '100%',
              maxHeight: '92vh',
              overflowY: 'auto',
              borderRadius: '12px',
              border: '1px solid var(--border-color)',
              boxShadow: '0 12px 36px rgba(0, 0, 0, 0.35)',
              padding: '1.5rem',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-start',
                borderBottom: '1px solid var(--border-color)',
                paddingBottom: '1rem',
                marginBottom: '1.25rem',
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', flexWrap: 'wrap' }}>
                  <h2 style={{ margin: 0, fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Building2 size={22} color="var(--primary)" />
                    {reviewTarget.organizationName || 'Unnamed Organization'}
                  </h2>
                  <span
                    className="badge"
                    style={{
                      color: STATUS_STYLES[reviewTarget.status]?.color || 'var(--text-muted)',
                      background: STATUS_STYLES[reviewTarget.status]?.bg || 'var(--bg-input)',
                    }}
                  >
                    {STATUS_STYLES[reviewTarget.status]?.label || reviewTarget.status}
                  </span>
                </div>
                <p style={{ color: 'var(--text-muted)', margin: '0.35rem 0 0 0', fontSize: '0.88rem' }}>
                  Verification Request #{reviewTarget.id} &bull; Submitted by <strong>{reviewTarget.userName}</strong> (@{reviewTarget.username})
                </p>
              </div>
              <button className="btn btn-outline btn-sm" onClick={() => setReviewTarget(null)} style={{ padding: '0.3rem 0.5rem' }}>
                <X size={18} />
              </button>
            </div>

            {/* Verification Checklist Alert */}
            <div
              style={{
                background: 'var(--primary-soft)',
                border: '1px solid rgba(124, 58, 237, 0.25)',
                borderRadius: '8px',
                padding: '0.85rem 1rem',
                marginBottom: '1.25rem',
                fontSize: '0.88rem',
              }}
            >
              <strong>📋 Verification Checklist:</strong> Carefully review all applicant and organization credentials, representative details, and attached documents below before proceeding to approve.
            </div>

            {/* Grid of Details */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem', marginBottom: '1.25rem' }}>
              {/* Organization Overview Card */}
              <div style={{ border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1rem', background: 'var(--bg-card)' }}>
                <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)' }}>
                  <Building2 size={16} /> Organization Profile
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.85rem' }}>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Legal Name:</span> <strong>{reviewTarget.organizationName || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Organization Type:</span> <strong>{reviewTarget.organizationType || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Registration / Tax ID:</span> <code>{reviewTarget.registrationNumber || '-'}</code>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Country:</span> <strong>{reviewTarget.country || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Official Website:</span>{' '}
                    {reviewTarget.website ? (
                      <a
                        href={reviewTarget.website.startsWith('http') ? reviewTarget.website : `https://${reviewTarget.website}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ color: 'var(--primary)', fontWeight: 600 }}
                      >
                        {reviewTarget.website} <ExternalLink size={12} style={{ display: 'inline' }} />
                      </a>
                    ) : (
                      '-'
                    )}
                  </div>
                </div>
              </div>

              {/* Representative & Account Card */}
              <div style={{ border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1rem', background: 'var(--bg-card)' }}>
                <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)' }}>
                  <User size={16} /> Representative &amp; Recipient
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.85rem' }}>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Representative:</span> <strong>{reviewTarget.representativeName || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Official Contact:</span> <strong>{reviewTarget.contactNumber || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Org Official Email:</span> <strong>{reviewTarget.officialEmail || '-'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Platform Username:</span> @{reviewTarget.username}
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Code Destination Email:</span>{' '}
                    <strong style={{ color: '#2563eb' }}>{reviewTarget.userEmail}</strong>
                  </div>
                </div>
              </div>

              {/* Purpose & Timeline Card */}
              <div style={{ border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1rem', background: 'var(--bg-card)', gridColumn: '1 / -1' }}>
                <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)' }}>
                  <Calendar size={16} /> Assessment Hosting Purpose &amp; Timeline
                </h4>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.6rem', fontSize: '0.85rem' }}>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Intended Purpose:</span> <strong>{reviewTarget.purpose || 'General Assessment Hosting'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Submitted On:</span> {reviewTarget.submittedAt ? new Date(reviewTarget.submittedAt).toLocaleString() : '-'}
                  </div>
                  <div>
                    <span style={{ color: 'var(--text-muted)' }}>Last Reviewed:</span> {reviewTarget.reviewedAt ? new Date(reviewTarget.reviewedAt).toLocaleString() : 'Not yet reviewed'}
                  </div>
                  {reviewTarget.rejectionReason && (
                    <div style={{ gridColumn: '1 / -1', color: 'var(--danger)', background: 'rgba(239, 68, 68, 0.08)', padding: '0.6rem', borderRadius: '6px' }}>
                      <strong>Prior Review Feedback:</strong> {reviewTarget.rejectionReason}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Verification Documents Section */}
            <div style={{ border: '1px solid var(--border-color)', borderRadius: '8px', padding: '1rem', marginBottom: '1.5rem', background: 'var(--bg-main)' }}>
              <h4 style={{ margin: '0 0 0.75rem 0', fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <FileCheck size={16} color="#15803d" /> Attached Verification Documents (Click to Inspect)
              </h4>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.6rem' }}>
                {reviewTarget.documentAvailable ? (
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={() => handlePreview(reviewTarget.id, 'primary', `Primary Certificate (${reviewTarget.documentOriginalName || 'Document'})`)}
                  >
                    <FileCheck size={14} color="var(--primary)" />
                    <span>Primary Incorporation Doc ({reviewTarget.documentOriginalName || 'PDF'})</span>
                  </button>
                ) : (
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>No primary document attached</span>
                )}

                {reviewTarget.gstDocAvailable && (
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={() => handlePreview(reviewTarget.id, 'gst', `GST Certificate (${reviewTarget.gstOriginalName})`)}
                  >
                    <FileText size={14} />
                    <span>GST Certificate ({reviewTarget.gstOriginalName || 'Doc'})</span>
                  </button>
                )}

                {reviewTarget.authLetterDocAvailable && (
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={() => handlePreview(reviewTarget.id, 'auth-letter', `Auth Letter (${reviewTarget.authLetterOriginalName})`)}
                  >
                    <FileText size={14} />
                    <span>Authorization Letter ({reviewTarget.authLetterOriginalName || 'Doc'})</span>
                  </button>
                )}

                {reviewTarget.supportingDocAvailable && (
                  <button
                    className="btn btn-outline btn-sm"
                    onClick={() => handlePreview(reviewTarget.id, 'supporting', `Supporting Document (${reviewTarget.supportingDocOriginalName})`)}
                  >
                    <FileText size={14} />
                    <span>Supporting Document ({reviewTarget.supportingDocOriginalName || 'Doc'})</span>
                  </button>
                )}
              </div>
            </div>

            {/* Decision Action Buttons */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
              <button className="btn btn-outline" onClick={() => setReviewTarget(null)}>
                Close
              </button>

              <div style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap' }}>
                {reviewTarget.status !== 'REJECTED' && reviewTarget.status !== 'VERIFIED' && (
                  <button
                    className="btn btn-outline btn-danger"
                    disabled={busyId === reviewTarget.id}
                    onClick={() => {
                      const id = reviewTarget.id;
                      setReviewTarget(null);
                      openRejectForm(id);
                    }}
                  >
                    <XCircle size={16} />
                    <span>Reject Request</span>
                  </button>
                )}

                {reviewTarget.status !== 'VERIFIED' && (
                  <button
                    className="btn btn-success"
                    disabled={busyId === reviewTarget.id}
                    onClick={async () => {
                      const id = reviewTarget.id;
                      const org = reviewTarget.organizationName;
                      setReviewTarget(null);
                      await handleDirectVerify(id, org);
                    }}
                    style={{ backgroundColor: '#15803d', borderColor: '#15803d', color: '#fff' }}
                  >
                    <UserCheck size={16} />
                    <span>Direct Verify</span>
                  </button>
                )}

                {(reviewTarget.status === 'PENDING' || reviewTarget.status === 'APPROVED') && (
                  <button
                    className="btn btn-primary"
                    disabled={busyId === reviewTarget.id}
                    onClick={async () => {
                      const id = reviewTarget.id;
                      setReviewTarget(null);
                      await handleApprove(id);
                    }}
                    style={{ fontWeight: 700 }}
                  >
                    <CheckCircle size={16} />
                    <span>{reviewTarget.status === 'APPROVED' ? 'Resend 6-Digit Code' : 'Approve & Send 6-Digit Code'}</span>
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {previewUrl && (
        <div
          style={{
            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000,
          }}
          onClick={closePreview}
        >
          <div className="card" style={{ maxWidth: '90vw', maxHeight: '90vh', overflow: 'auto' }} onClick={(e) => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
              <h3 style={{ margin: 0 }}>{previewTitle || 'Uploaded Document'}</h3>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <a className="btn btn-outline btn-sm" href={previewUrl} target="_blank" rel="noopener noreferrer">
                  <ExternalLink size={14} />
                  <span>Open in new tab</span>
                </a>
                <button className="btn btn-outline btn-sm" onClick={closePreview}>
                  <X size={14} />
                </button>
              </div>
            </div>
            {previewType.startsWith('image/') ? (
              <img src={previewUrl} alt="Verification document" style={{ maxWidth: '80vw', maxHeight: '75vh' }} />
            ) : (
              <iframe title="Verification document" src={previewUrl} style={{ width: '80vw', height: '75vh', border: 'none' }} />
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default AssessmentHostVerification;
