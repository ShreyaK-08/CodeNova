import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  LifeBuoy,
  ArrowLeft,
  Send,
  CheckCircle2,
  Clock,
  AlertCircle,
  RefreshCw,
  HelpCircle,
  Paperclip,
  FileText,
  Download,
  Star,
  User,
  ShieldCheck,
  Check,
  RotateCcw,
  Lock,
  MessageSquare,
  AlertTriangle,
  ExternalLink,
  Calendar,
  Tag,
  Flame,
  Info
} from 'lucide-react';
import supportFeedbackService from '../../services/supportFeedbackService';

const SupportTicketDetail = () => {
  const { ticketId } = useParams();
  const navigate = useNavigate();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');

  // Reply state
  const [replyMessage, setReplyMessage] = useState('');
  const [replyAttachment, setReplyAttachment] = useState(null);
  const [uploadingAttachment, setUploadingAttachment] = useState(false);
  const [sendingReply, setSendingReply] = useState(false);

  // Rating Modal state
  const [isRatingModalOpen, setIsRatingModalOpen] = useState(false);
  const [ratingValue, setRatingValue] = useState(5);
  const [ratingHover, setRatingHover] = useState(0);
  const [ratingComments, setRatingComments] = useState('');
  const [submittingRating, setSubmittingRating] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchTicket = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await supportFeedbackService.getSupportTicketById(ticketId);
      setTicket(data);
    } catch (err) {
      console.error('Failed to load support ticket', err);
      setError(err.response?.data?.message || 'Failed to load support ticket.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (ticketId) {
      fetchTicket();
    }
  }, [ticketId]);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setActionError('File size exceeds 10MB limit.');
      return;
    }

    try {
      setUploadingAttachment(true);
      setActionError('');
      const uploaded = await supportFeedbackService.uploadAttachment(file);
      setReplyAttachment(uploaded);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to upload attachment.');
    } finally {
      setUploadingAttachment(false);
    }
  };

  const handleSendReply = async (e) => {
    e.preventDefault();
    if (!replyMessage.trim() && !replyAttachment) {
      setActionError('Please enter a message or attach a file.');
      return;
    }

    try {
      setSendingReply(true);
      setActionError('');
      setActionSuccess('');

      await supportFeedbackService.addSupportMessage(ticketId, {
        message: replyMessage.trim() || '(Attachment uploaded)',
        attachmentUrl: replyAttachment?.url,
        attachmentName: replyAttachment?.fileName,
        attachmentType: replyAttachment?.fileType,
        attachmentSize: replyAttachment?.fileSize,
        isInternalNote: false
      });

      setReplyMessage('');
      setReplyAttachment(null);
      setActionSuccess('Reply sent successfully!');
      fetchTicket();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to send reply.');
    } finally {
      setSendingReply(false);
    }
  };

  const handleResolveTicket = async () => {
    if (actionLoading) return;
    try {
      setActionLoading(true);
      setActionError('');
      await supportFeedbackService.resolveSupportTicket(ticketId);
      setActionSuccess('Ticket marked as resolved!');
      fetchTicket();
      setIsRatingModalOpen(true);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to resolve ticket.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleReopenTicket = async () => {
    if (actionLoading) return;
    try {
      setActionLoading(true);
      setActionError('');
      await supportFeedbackService.reopenSupportTicket(ticketId);
      setActionSuccess('Ticket reopened!');
      fetchTicket();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to reopen ticket.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCloseTicket = async () => {
    if (actionLoading) return;
    try {
      setActionLoading(true);
      setActionError('');
      await supportFeedbackService.closeSupportTicket(ticketId);
      setActionSuccess('Ticket closed.');
      fetchTicket();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to close ticket.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmitRating = async (e) => {
    e.preventDefault();
    try {
      setSubmittingRating(true);
      setActionError('');
      await supportFeedbackService.submitSupportRating(ticketId, {
        rating: ratingValue,
        comments: ratingComments.trim()
      });
      setIsRatingModalOpen(false);
      setActionSuccess('Thank you for rating your support experience!');
      fetchTicket();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to submit rating.');
    } finally {
      setSubmittingRating(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OPEN':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.3rem 0.8rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700, backgroundColor: 'var(--primary-soft)', color: 'var(--primary)' }}>
            <Clock size={13} /> Open
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.3rem 0.8rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700, backgroundColor: 'rgba(59, 130, 246, 0.1)', color: '#2563eb' }}>
            <RefreshCw size={13} className="animate-spin" /> In Progress
          </span>
        );
      case 'WAITING_FOR_USER':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.3rem 0.8rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700, backgroundColor: 'var(--warning-soft)', color: 'var(--warning)' }}>
            <AlertTriangle size={13} /> Waiting for Your Response
          </span>
        );
      case 'RESOLVED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.3rem 0.8rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700, backgroundColor: 'var(--success-soft)', color: 'var(--success)' }}>
            <CheckCircle2 size={13} /> Resolved
          </span>
        );
      case 'CLOSED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.3rem 0.8rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 700, backgroundColor: 'rgba(100, 116, 139, 0.15)', color: 'var(--text-muted)' }}>
            <Lock size={13} /> Closed
          </span>
        );
      default:
        return <span>{status}</span>;
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'CRITICAL':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--danger)', backgroundColor: 'var(--danger-soft)', padding: '0.25rem 0.6rem', borderRadius: '4px' }}>CRITICAL PRIORITY</span>;
      case 'HIGH':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#ea580c', backgroundColor: 'rgba(234, 88, 12, 0.1)', padding: '0.25rem 0.6rem', borderRadius: '4px' }}>HIGH PRIORITY</span>;
      case 'MEDIUM':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--primary)', backgroundColor: 'var(--primary-soft)', padding: '0.25rem 0.6rem', borderRadius: '4px' }}>MEDIUM PRIORITY</span>;
      case 'LOW':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', backgroundColor: 'rgba(100, 116, 139, 0.1)', padding: '0.25rem 0.6rem', borderRadius: '4px' }}>LOW PRIORITY</span>;
      default:
        return <span>{priority}</span>;
    }
  };

  if (loading) {
    return (
      <div style={{ maxWidth: '960px', margin: '3rem auto', textAlign: 'center', color: 'var(--text-muted)' }}>
        <RefreshCw size={32} className="animate-spin" style={{ margin: '0 auto 1rem auto' }} />
        <p style={{ fontWeight: 600 }}>Loading ticket details...</p>
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div style={{ maxWidth: '800px', margin: '2rem auto' }}>
        <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={20} />
          <span>{error || 'Support ticket not found.'}</span>
        </div>
        <button onClick={() => navigate('/support')} className="btn btn-outline">
          <ArrowLeft size={16} /> Back to Support Center
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Back Button & Breadcrumbs */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <button
          onClick={() => navigate('/support')}
          className="btn btn-outline btn-sm"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <ArrowLeft size={14} /> Back to Support Center
        </button>
        <span style={{ color: 'var(--text-subtle)' }}>/</span>
        <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          {ticket.ticketNumber}
        </span>
      </div>

      {/* Action Alerts */}
      {actionSuccess && (
        <div className="alert alert-success" style={{ marginBottom: '1.25rem' }}>
          <CheckCircle2 size={18} />
          <span>{actionSuccess}</span>
        </div>
      )}

      {actionError && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
          <AlertCircle size={18} />
          <span>{actionError}</span>
        </div>
      )}

      {/* Ticket Header Card */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 800, fontSize: '1.1rem', color: 'var(--primary)' }}>
                {ticket.ticketNumber}
              </span>
              {getStatusBadge(ticket.status)}
              {getPriorityBadge(ticket.priority)}
              <span style={{ padding: '0.25rem 0.6rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, backgroundColor: 'var(--bg-subtle)', color: 'var(--text-muted)', border: '1px solid var(--border-color)' }}>
                {ticket.category}
              </span>
            </div>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-main)', margin: '0.25rem 0' }}>
              {ticket.subject}
            </h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', color: 'var(--text-subtle)', fontSize: '0.85rem', flexWrap: 'wrap', marginTop: '0.5rem' }}>
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                <Calendar size={14} /> Created: {new Date(ticket.createdAt).toLocaleString()}
              </span>
              {ticket.assignedToName && (
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', color: 'var(--primary)', fontWeight: 600 }}>
                  <ShieldCheck size={14} /> Assigned to: {ticket.assignedToName}
                </span>
              )}
            </div>
          </div>

          {/* Quick Status Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {ticket.status !== 'RESOLVED' && ticket.status !== 'CLOSED' && (
              <button
                onClick={handleResolveTicket}
                disabled={actionLoading}
                className="btn btn-sm btn-success"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}
              >
                <Check size={14} /> {actionLoading ? 'Updating...' : 'Mark as Resolved'}
              </button>
            )}
            {(ticket.status === 'RESOLVED' || ticket.status === 'CLOSED') && (
              <button
                onClick={handleReopenTicket}
                disabled={actionLoading}
                className="btn btn-sm btn-outline"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}
              >
                <RotateCcw size={14} /> {actionLoading ? 'Updating...' : 'Reopen Ticket'}
              </button>
            )}
            {ticket.status !== 'CLOSED' && (
              <button
                onClick={handleCloseTicket}
                disabled={actionLoading}
                className="btn btn-sm btn-outline"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}
              >
                <Lock size={14} /> {actionLoading ? 'Updating...' : 'Close Ticket'}
              </button>
            )}
          </div>
        </div>

        {/* Linked Context Reference */}
        {(ticket.assessmentTitle || ticket.problemTitle || ticket.contestTitle) && (
          <div
            style={{
              backgroundColor: 'var(--bg-subtle)',
              border: '1px solid var(--border-color)',
              borderRadius: '8px',
              padding: '0.85rem 1.25rem',
              marginTop: '1rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              flexWrap: 'wrap',
              gap: '0.75rem'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <Info size={16} color="var(--primary)" />
              <div style={{ fontSize: '0.85rem' }}>
                <strong style={{ color: 'var(--text-main)' }}>Linked Resource: </strong>
                {ticket.assessmentTitle && <span>Assessment: <em>{ticket.assessmentTitle}</em> (ID: #{ticket.assessmentId})</span>}
                {ticket.problemTitle && <span>Problem: <em>{ticket.problemTitle}</em> (ID: #{ticket.problemId})</span>}
                {ticket.contestTitle && <span>Contest: <em>{ticket.contestTitle}</em> (ID: #{ticket.contestId})</span>}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Post-Resolution Rating Banner */}
      {(ticket.status === 'RESOLVED' || ticket.status === 'CLOSED') && (
        <div
          className="card"
          style={{
            marginBottom: '1.5rem',
            padding: '1.25rem 1.75rem',
            background: ticket.rating ? 'linear-gradient(135deg, rgba(245, 158, 11, 0.08) 0%, rgba(245, 158, 11, 0.02) 100%)' : 'linear-gradient(135deg, rgba(79, 70, 229, 0.08) 0%, rgba(79, 70, 229, 0.02) 100%)',
            border: ticket.rating ? '1px solid rgba(245, 158, 11, 0.3)' : '1px solid rgba(79, 70, 229, 0.25)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1rem'
          }}
        >
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: '0 0 0.25rem 0', color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Star size={18} color="#f59e0b" fill="#f59e0b" />
              {ticket.rating ? 'Your Support Rating & Feedback' : 'How was your support experience?'}
            </h3>
            {ticket.rating ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', marginTop: '0.25rem' }}>
                  {[1, 2, 3, 4, 5].map((s) => (
                    <Star
                      key={s}
                      size={16}
                      color={s <= ticket.rating ? '#f59e0b' : 'var(--text-subtle)'}
                      fill={s <= ticket.rating ? '#f59e0b' : 'transparent'}
                    />
                  ))}
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', marginLeft: '0.35rem', color: '#f59e0b' }}>
                    {ticket.rating} / 5 Stars
                  </span>
                </div>
                {ticket.ratingComments && (
                  <p style={{ margin: '0.4rem 0 0 0', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    "{ticket.ratingComments}"
                  </p>
                )}
              </div>
            ) : (
              <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                This ticket is resolved. Please take a moment to rate the assistance you received.
              </p>
            )}
          </div>

          <button
            onClick={() => setIsRatingModalOpen(true)}
            className={`btn btn-sm ${ticket.rating ? 'btn-outline' : 'btn-primary'}`}
            style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <Star size={14} />
            <span>{ticket.rating ? 'Edit Rating' : 'Rate Support'}</span>
          </button>
        </div>
      )}

      {/* Conversation Thread */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.75rem' }}>
        <h2 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <MessageSquare size={20} color="var(--primary)" /> Conversation History ({ticket.messages?.length || 0})
        </h2>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {ticket.messages && ticket.messages.map((msg, index) => {
            const isUser = msg.senderRole === 'USER';
            return (
              <div
                key={msg.id || index}
                style={{
                  display: 'flex',
                  gap: '1rem',
                  alignItems: 'flex-start',
                  padding: '1.25rem',
                  borderRadius: '12px',
                  backgroundColor: isUser ? 'var(--bg-card)' : 'rgba(79, 70, 229, 0.04)',
                  border: isUser ? '1px solid var(--border-color)' : '1px solid rgba(79, 70, 229, 0.2)'
                }}
              >
                {/* Avatar */}
                <div
                  style={{
                    width: '38px',
                    height: '38px',
                    borderRadius: '50%',
                    backgroundColor: isUser ? 'var(--bg-subtle)' : 'var(--primary)',
                    color: isUser ? 'var(--text-main)' : '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 700,
                    fontSize: '0.9rem',
                    flexShrink: 0
                  }}
                >
                  {isUser ? <User size={18} /> : <ShieldCheck size={20} />}
                </div>

                {/* Message Body */}
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: '0.95rem' }}>
                        {msg.senderName || msg.senderUsername || (isUser ? 'You' : 'Support Team')}
                      </span>
                      <span
                        style={{
                          fontSize: '0.7rem',
                          fontWeight: 700,
                          padding: '0.15rem 0.45rem',
                          borderRadius: '4px',
                          backgroundColor: isUser ? 'var(--bg-subtle)' : 'var(--primary-soft)',
                          color: isUser ? 'var(--text-muted)' : 'var(--primary)'
                        }}
                      >
                        {isUser ? 'CANDIDATE' : 'SUPPORT STAFF'}
                      </span>
                    </div>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                      {msg.createdAt ? new Date(msg.createdAt).toLocaleString() : '—'}
                    </span>
                  </div>

                  <div style={{ color: 'var(--text-main)', fontSize: '0.9rem', lineHeight: 1.6, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                    {msg.message}
                  </div>

                  {/* Attachment if present */}
                  {msg.attachmentUrl && (
                    <div style={{ marginTop: '0.75rem' }}>
                      <a
                        href={msg.attachmentUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="btn btn-outline btn-sm"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', padding: '0.35rem 0.75rem' }}
                      >
                        <FileText size={14} color="var(--primary)" />
                        <span>{msg.attachmentName || 'Download Attachment'}</span>
                        {msg.attachmentSize && (
                          <span style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>
                            ({Math.round(msg.attachmentSize / 1024)} KB)
                          </span>
                        )}
                        <Download size={13} />
                      </a>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Reply Composer */}
      {ticket.status !== 'CLOSED' ? (
        <div className="card" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Send size={18} color="var(--primary)" /> Reply to Support
          </h3>

          <form onSubmit={handleSendReply}>
            <div className="form-group" style={{ marginBottom: '1rem' }}>
              <textarea
                className="form-control"
                rows={4}
                placeholder="Type your response, additional details, or reproduction steps..."
                value={replyMessage}
                onChange={(e) => setReplyMessage(e.target.value)}
                disabled={sendingReply}
              />
            </div>

            {/* Attachment preview in reply */}
            {replyAttachment && (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.6rem 0.85rem', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', border: '1px solid var(--border-color)', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                  <FileText size={16} color="var(--primary)" />
                  <span style={{ fontWeight: 600 }}>{replyAttachment.fileName}</span>
                  <span style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>
                    ({Math.round(replyAttachment.fileSize / 1024)} KB)
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => setReplyAttachment(null)}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--danger)' }}
                >
                  <AlertCircle size={16} />
                </button>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
              <div>
                <input
                  type="file"
                  id="reply-attachment"
                  style={{ display: 'none' }}
                  accept=".png,.jpg,.jpeg,.webp,.pdf,.txt"
                  onChange={handleFileUpload}
                  disabled={uploadingAttachment || sendingReply}
                />
                <label
                  htmlFor="reply-attachment"
                  className="btn btn-outline btn-sm"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', cursor: uploadingAttachment ? 'not-allowed' : 'pointer' }}
                >
                  <Paperclip size={14} />
                  <span>{uploadingAttachment ? 'Uploading...' : 'Attach File'}</span>
                </label>
              </div>

              <button
                type="submit"
                disabled={sendingReply || uploadingAttachment || (!replyMessage.trim() && !replyAttachment)}
                className="btn btn-primary"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600 }}
              >
                <Send size={16} />
                <span>{sendingReply ? 'Sending...' : 'Send Reply'}</span>
              </button>
            </div>
          </form>
        </div>
      ) : (
        <div className="card" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          <Lock size={28} style={{ margin: '0 auto 0.5rem auto', opacity: 0.5 }} />
          <p style={{ fontWeight: 600, margin: '0 0 0.5rem 0' }}>This ticket has been closed.</p>
          <button onClick={handleReopenTicket} className="btn btn-outline btn-sm">
            <RotateCcw size={14} /> Reopen Ticket to Continue
          </button>
        </div>
      )}

      {/* RATING MODAL */}
      {isRatingModalOpen && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(15, 23, 42, 0.6)',
            backdropFilter: 'blur(4px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1rem'
          }}
          onClick={() => !submittingRating && setIsRatingModalOpen(false)}
        >
          <div
            className="card"
            style={{ maxWidth: '500px', width: '100%', padding: '2rem', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.2)' }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ fontSize: '1.3rem', fontWeight: 800, marginBottom: '0.5rem', textAlign: 'center', color: 'var(--text-main)' }}>
              Rate Your Support Experience
            </h2>
            <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
              How satisfied are you with the resolution of ticket <strong>{ticket.ticketNumber}</strong>?
            </p>

            <form onSubmit={handleSubmitRating}>
              {/* Star Picker */}
              <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setRatingValue(star)}
                    onMouseEnter={() => setRatingHover(star)}
                    onMouseLeave={() => setRatingHover(0)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '0.25rem', transition: 'transform 0.15s ease' }}
                  >
                    <Star
                      size={36}
                      color={(ratingHover || ratingValue) >= star ? '#f59e0b' : 'var(--text-subtle)'}
                      fill={(ratingHover || ratingValue) >= star ? '#f59e0b' : 'transparent'}
                    />
                  </button>
                ))}
              </div>

              <div style={{ textAlign: 'center', fontWeight: 700, color: '#f59e0b', fontSize: '1rem', marginBottom: '1.25rem' }}>
                {ratingValue === 5 && '★★★★★ Excellent (5/5)'}
                {ratingValue === 4 && '★★★★☆ Good (4/5)'}
                {ratingValue === 3 && '★★★☆☆ Average (3/5)'}
                {ratingValue === 2 && '★★☆☆☆ Poor (2/5)'}
                {ratingValue === 1 && '★☆☆☆☆ Unsatisfactory (1/5)'}
              </div>

              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.85rem' }}>Feedback Comments (Optional)</label>
                <textarea
                  className="form-control"
                  rows={3}
                  placeholder="Tell us what went well or how we can improve..."
                  value={ratingComments}
                  onChange={(e) => setRatingComments(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => setIsRatingModalOpen(false)}
                  disabled={submittingRating}
                  className="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submittingRating}
                  className="btn btn-primary"
                  style={{ fontWeight: 600 }}
                >
                  {submittingRating ? 'Submitting...' : 'Submit Rating'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default SupportTicketDetail;
