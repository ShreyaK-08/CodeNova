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
  Sparkles,
  LockKeyhole,
  Bot,
  UserCheck,
  Save,
  CheckSquare
} from 'lucide-react';
import supportFeedbackService from '../../services/supportFeedbackService';
import { SUPPORT_CATEGORIES } from '../user/Support';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const STATUSES = ['OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER', 'RESOLVED', 'CLOSED'];

const AdminSupportTicketDetail = () => {
  const { ticketId } = useParams();
  const navigate = useNavigate();

  const [ticket, setTicket] = useState(null);
  const [staffMembers, setStaffMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');
  const [actionError, setActionError] = useState('');

  // Triage state
  const [triageForm, setTriageForm] = useState({
    status: 'OPEN',
    priority: 'MEDIUM',
    category: SUPPORT_CATEGORIES[0],
    assignedToUserId: 0
  });
  const [savingTriage, setSavingTriage] = useState(false);

  // Composer state
  const [composerMode, setComposerMode] = useState('reply'); // 'reply' | 'internal'
  const [composerMessage, setComposerMessage] = useState('');
  const [composerAttachment, setComposerAttachment] = useState(null);
  const [uploadingAttachment, setUploadingAttachment] = useState(false);
  const [submittingComposer, setSubmittingComposer] = useState(false);

  // AI Draft state
  const [generatingAi, setGeneratingAi] = useState(false);
  const [aiDraft, setAiDraft] = useState(null);
  const [aiInstruction, setAiInstruction] = useState('');

  const loadTicketAndStaff = async () => {
    try {
      setLoading(true);
      setError('');
      const [ticketData, staffData] = await Promise.all([
        supportFeedbackService.getAdminSupportTicketById(ticketId),
        supportFeedbackService.getStaffMembers()
      ]);
      setTicket(ticketData);
      setStaffMembers(staffData || []);
      setTriageForm({
        status: ticketData.status || 'OPEN',
        priority: ticketData.priority || 'MEDIUM',
        category: ticketData.category || SUPPORT_CATEGORIES[0],
        assignedToUserId: ticketData.assignedToUserId || 0
      });
    } catch (err) {
      console.error('Failed to load ticket for admin', err);
      setError(err.response?.data?.message || 'Failed to load support ticket.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (ticketId) {
      loadTicketAndStaff();
    }
  }, [ticketId]);

  const handleTriageSave = async (e) => {
    e.preventDefault();
    try {
      setSavingTriage(true);
      setActionError('');
      setActionSuccess('');
      const updated = await supportFeedbackService.adminTriageTicket(ticketId, {
        status: triageForm.status,
        priority: triageForm.priority,
        category: triageForm.category,
        assignedToUserId: Number(triageForm.assignedToUserId)
      });
      setTicket(updated);
      setActionSuccess('Ticket triage settings saved successfully!');
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to update ticket triage settings.');
    } finally {
      setSavingTriage(false);
    }
  };

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
      setComposerAttachment(uploaded);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to upload attachment.');
    } finally {
      setUploadingAttachment(false);
    }
  };

  const handleComposerSubmit = async (e) => {
    e.preventDefault();
    if (!composerMessage.trim() && !composerAttachment) {
      setActionError('Please provide a message or attachment.');
      return;
    }

    try {
      setSubmittingComposer(true);
      setActionError('');
      setActionSuccess('');

      await supportFeedbackService.addAdminSupportMessage(ticketId, {
        message: composerMessage.trim() || '(Attachment uploaded)',
        isInternalNote: composerMode === 'internal',
        attachmentUrl: composerAttachment?.url,
        attachmentName: composerAttachment?.fileName,
        attachmentType: composerAttachment?.fileType,
        attachmentSize: composerAttachment?.fileSize
      });

      setComposerMessage('');
      setComposerAttachment(null);
      setActionSuccess(composerMode === 'internal' ? 'Internal note added!' : 'Reply sent to user!');
      loadTicketAndStaff();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to submit response.');
    } finally {
      setSubmittingComposer(false);
    }
  };

  const handleGenerateAiDraft = async () => {
    try {
      setGeneratingAi(true);
      setActionError('');
      const res = await supportFeedbackService.generateAiSupportDraft(ticketId, {
        customInstruction: aiInstruction.trim() || undefined
      });
      setAiDraft(res);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to generate AI response draft.');
    } finally {
      setGeneratingAi(false);
    }
  };

  const applyAiDraftToComposer = () => {
    if (!aiDraft?.draftReply) return;
    setComposerMode('reply');
    setComposerMessage(aiDraft.draftReply);
    setAiDraft(null);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OPEN':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.65rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--primary-soft)', color: 'var(--primary)' }}>
            <Clock size={12} /> Open
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.65rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(59, 130, 246, 0.1)', color: '#2563eb' }}>
            <RefreshCw size={12} className="animate-spin" /> In Progress
          </span>
        );
      case 'WAITING_FOR_USER':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.65rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--warning-soft)', color: 'var(--warning)' }}>
            <AlertTriangle size={12} /> Waiting for User
          </span>
        );
      case 'RESOLVED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.65rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--success-soft)', color: 'var(--success)' }}>
            <CheckCircle2 size={12} /> Resolved
          </span>
        );
      case 'CLOSED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.25rem 0.65rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(100, 116, 139, 0.15)', color: 'var(--text-muted)' }}>
            <Lock size={12} /> Closed
          </span>
        );
      default:
        return <span>{status}</span>;
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'CRITICAL':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--danger)', backgroundColor: 'var(--danger-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>CRITICAL</span>;
      case 'HIGH':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#ea580c', backgroundColor: 'rgba(234, 88, 12, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>HIGH</span>;
      case 'MEDIUM':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--primary)', backgroundColor: 'var(--primary-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>MEDIUM</span>;
      case 'LOW':
        return <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-muted)', backgroundColor: 'rgba(100, 116, 139, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>LOW</span>;
      default:
        return <span>{priority}</span>;
    }
  };

  if (loading) {
    return (
      <div style={{ maxWidth: '960px', margin: '3rem auto', textAlign: 'center', color: 'var(--text-muted)' }}>
        <RefreshCw size={32} className="animate-spin" style={{ margin: '0 auto 1rem auto' }} />
        <p style={{ fontWeight: 600 }}>Loading admin ticket workspace...</p>
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
        <button onClick={() => navigate('/admin/support')} className="btn btn-outline">
          <ArrowLeft size={16} /> Back to Support Management
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Back Button & Breadcrumbs */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <button
          onClick={() => navigate('/admin/support')}
          className="btn btn-outline btn-sm"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <ArrowLeft size={14} /> Back to Support Management
        </button>
        <span style={{ color: 'var(--text-subtle)' }}>/</span>
        <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          {ticket.ticketNumber}
        </span>
      </div>

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

      {/* Main Two-Column Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) minmax(320px, 1fr)', gap: '1.5rem', alignItems: 'start' }}>
        {/* LEFT COLUMN: Ticket details, History, AI Draft, Composer */}
        <div>
          {/* Header Card */}
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1.75rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 800, fontSize: '1.1rem', color: 'var(--primary)' }}>
                {ticket.ticketNumber}
              </span>
              {getStatusBadge(ticket.status)}
              {getPriorityBadge(ticket.priority)}
              <span style={{ padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, backgroundColor: 'var(--bg-subtle)', color: 'var(--text-muted)', border: '1px solid var(--border-color)' }}>
                {ticket.category}
              </span>
            </div>

            <h1 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-main)', margin: '0.25rem 0 0.5rem 0' }}>
              {ticket.subject}
            </h1>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', color: 'var(--text-subtle)', fontSize: '0.8rem', flexWrap: 'wrap' }}>
              <span>Created: {new Date(ticket.createdAt).toLocaleString()}</span>
              {ticket.updatedAt && <span>Updated: {new Date(ticket.updatedAt).toLocaleString()}</span>}
            </div>

            {/* Linked Context */}
            {(ticket.assessmentTitle || ticket.problemTitle || ticket.contestTitle) && (
              <div style={{ backgroundColor: 'var(--bg-subtle)', border: '1px solid var(--border-color)', borderRadius: '8px', padding: '0.75rem 1rem', marginTop: '1rem', fontSize: '0.85rem' }}>
                <strong style={{ color: 'var(--primary)' }}>Linked Context: </strong>
                {ticket.assessmentTitle && <span>Assessment: <strong>{ticket.assessmentTitle}</strong> (ID: #{ticket.assessmentId}) </span>}
                {ticket.problemTitle && <span>Problem: <strong>{ticket.problemTitle}</strong> (ID: #{ticket.problemId}) </span>}
                {ticket.contestTitle && <span>Contest: <strong>{ticket.contestTitle}</strong> (ID: #{ticket.contestId}) </span>}
              </div>
            )}
          </div>

          {/* AI Support Assistant Box */}
          <div
            className="card"
            style={{
              marginBottom: '1.5rem',
              padding: '1.5rem',
              background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(99, 102, 241, 0.02) 100%)',
              border: '1px solid rgba(79, 70, 229, 0.25)'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem', flexWrap: 'wrap', gap: '0.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Sparkles size={18} color="var(--primary)" />
                <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: 'var(--text-main)' }}>
                  CodeNova AI Support Assistant
                </h3>
              </div>
              <button
                onClick={handleGenerateAiDraft}
                disabled={generatingAi}
                className="btn btn-primary btn-sm"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem' }}
              >
                {generatingAi ? <RefreshCw size={13} className="animate-spin" /> : <Sparkles size={13} />}
                <span>{generatingAi ? 'Generating Draft...' : 'Generate AI Draft'}</span>
              </button>
            </div>

            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: '0 0 0.75rem 0' }}>
              Analyzes the ticket category, user problem description, context references, and conversation history to draft a polite, professional reply.
            </p>

            {/* AI Draft Result */}
            {aiDraft && (
              <div style={{ backgroundColor: 'var(--bg-card)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border-color)', marginTop: '0.75rem' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', marginBottom: '0.5rem', fontWeight: 600 }}>
                  Generated Draft (Review & Insert):
                </div>
                <div style={{ whiteSpace: 'pre-wrap', fontSize: '0.85rem', lineHeight: 1.5, color: 'var(--text-main)', maxHeight: '200px', overflowY: 'auto', backgroundColor: 'var(--bg-subtle)', padding: '0.75rem', borderRadius: '6px' }}>
                  {aiDraft.draftReply}
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.75rem' }}>
                  <button
                    onClick={() => setAiDraft(null)}
                    className="btn btn-outline btn-sm"
                    style={{ fontSize: '0.75rem' }}
                  >
                    Dismiss
                  </button>
                  <button
                    onClick={applyAiDraftToComposer}
                    className="btn btn-primary btn-sm"
                    style={{ fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}
                  >
                    <CheckSquare size={13} />
                    <span>Insert into Reply Box</span>
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Conversation History */}
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1.75rem' }}>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <MessageSquare size={20} color="var(--primary)" /> Conversation Thread ({ticket.messages?.length || 0})
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {ticket.messages && ticket.messages.map((msg, idx) => {
                const isUser = msg.senderRole === 'USER';
                const isInternal = msg.isInternalNote;

                return (
                  <div
                    key={msg.id || idx}
                    style={{
                      display: 'flex',
                      gap: '1rem',
                      alignItems: 'flex-start',
                      padding: '1.25rem',
                      borderRadius: '12px',
                      backgroundColor: isInternal
                        ? 'rgba(245, 158, 11, 0.08)'
                        : (isUser ? 'var(--bg-card)' : 'rgba(79, 70, 229, 0.04)'),
                      border: isInternal
                        ? '1px solid rgba(245, 158, 11, 0.35)'
                        : (isUser ? '1px solid var(--border-color)' : '1px solid rgba(79, 70, 229, 0.2)')
                    }}
                  >
                    {/* Avatar */}
                    <div
                      style={{
                        width: '38px',
                        height: '38px',
                        borderRadius: '50%',
                        backgroundColor: isInternal ? '#f59e0b' : (isUser ? 'var(--bg-subtle)' : 'var(--primary)'),
                        color: isInternal ? '#ffffff' : (isUser ? 'var(--text-main)' : '#ffffff'),
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontWeight: 700,
                        fontSize: '0.9rem',
                        flexShrink: 0
                      }}
                    >
                      {isInternal ? <LockKeyhole size={18} /> : (isUser ? <User size={18} /> : <ShieldCheck size={20} />)}
                    </div>

                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <span style={{ fontWeight: 700, color: 'var(--text-main)', fontSize: '0.95rem' }}>
                            {msg.senderName || msg.senderUsername}
                          </span>
                          <span
                            style={{
                              fontSize: '0.7rem',
                              fontWeight: 700,
                              padding: '0.15rem 0.45rem',
                              borderRadius: '4px',
                              backgroundColor: isInternal ? 'rgba(245, 158, 11, 0.2)' : (isUser ? 'var(--bg-subtle)' : 'var(--primary-soft)'),
                              color: isInternal ? '#b45309' : (isUser ? 'var(--text-muted)' : 'var(--primary)')
                            }}
                          >
                            {isInternal ? 'INTERNAL NOTE' : (isUser ? 'CANDIDATE' : 'STAFF')}
                          </span>

                          {isInternal && (
                            <span style={{ fontSize: '0.7rem', fontWeight: 600, color: '#b45309', display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
                              <Lock size={10} /> Hidden from user
                            </span>
                          )}
                        </div>

                        <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                          {msg.createdAt ? new Date(msg.createdAt).toLocaleString() : '—'}
                        </span>
                      </div>

                      <div style={{ color: 'var(--text-main)', fontSize: '0.9rem', lineHeight: 1.6, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                        {msg.message}
                      </div>

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

          {/* Admin Composer */}
          <div className="card" style={{ padding: '1.75rem' }}>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              <button
                type="button"
                onClick={() => setComposerMode('reply')}
                className={`btn btn-sm ${composerMode === 'reply' ? 'btn-primary' : 'btn-outline'}`}
                style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
              >
                <Send size={14} /> Public Reply (Emailed to User)
              </button>
              <button
                type="button"
                onClick={() => setComposerMode('internal')}
                className={`btn btn-sm ${composerMode === 'internal' ? 'btn-primary' : 'btn-outline'}`}
                style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
              >
                <LockKeyhole size={14} /> Internal Staff Note (Staff Only)
              </button>
            </div>

            <form onSubmit={handleComposerSubmit}>
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <textarea
                  className="form-control"
                  rows={5}
                  placeholder={composerMode === 'internal' ? 'Write an internal note for staff members (never visible to candidate)...' : 'Type public reply to the candidate...'}
                  value={composerMessage}
                  onChange={(e) => setComposerMessage(e.target.value)}
                  disabled={submittingComposer}
                />
              </div>

              {/* Attachment Preview */}
              {composerAttachment && (
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.6rem 0.85rem', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', border: '1px solid var(--border-color)', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                    <FileText size={16} color="var(--primary)" />
                    <span style={{ fontWeight: 600 }}>{composerAttachment.fileName}</span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setComposerAttachment(null)}
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
                    id="admin-attachment"
                    style={{ display: 'none' }}
                    accept=".png,.jpg,.jpeg,.webp,.pdf,.txt"
                    onChange={handleFileUpload}
                    disabled={uploadingAttachment || submittingComposer}
                  />
                  <label
                    htmlFor="admin-attachment"
                    className="btn btn-outline btn-sm"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', cursor: uploadingAttachment ? 'not-allowed' : 'pointer' }}
                  >
                    <Paperclip size={14} />
                    <span>{uploadingAttachment ? 'Uploading...' : 'Attach File'}</span>
                  </label>
                </div>

                <button
                  type="submit"
                  disabled={submittingComposer || uploadingAttachment || (!composerMessage.trim() && !composerAttachment)}
                  className={`btn ${composerMode === 'internal' ? 'btn-outline' : 'btn-primary'}`}
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600 }}
                >
                  {composerMode === 'internal' ? <LockKeyhole size={15} /> : <Send size={15} />}
                  <span>{submittingComposer ? 'Saving...' : (composerMode === 'internal' ? 'Save Internal Note' : 'Send Public Reply')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* RIGHT COLUMN: Requester Profile, Triage Controls, Rating */}
        <div>
          {/* Triage Panel Card */}
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-main)' }}>
              <Tag size={18} color="var(--primary)" /> Ticket Triage & Assignment
            </h3>

            <form onSubmit={handleTriageSave}>
              {/* Status */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Status</label>
                <select
                  className="form-control"
                  value={triageForm.status}
                  onChange={(e) => setTriageForm({ ...triageForm, status: e.target.value })}
                >
                  {STATUSES.map((st) => (
                    <option key={st} value={st}>
                      {st}
                    </option>
                  ))}
                </select>
              </div>

              {/* Priority */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Priority</label>
                <select
                  className="form-control"
                  value={triageForm.priority}
                  onChange={(e) => setTriageForm({ ...triageForm, priority: e.target.value })}
                >
                  {PRIORITIES.map((pr) => (
                    <option key={pr} value={pr}>
                      {pr}
                    </option>
                  ))}
                </select>
              </div>

              {/* Category */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Category</label>
                <select
                  className="form-control"
                  value={triageForm.category}
                  onChange={(e) => setTriageForm({ ...triageForm, category: e.target.value })}
                >
                  {SUPPORT_CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>
              </div>

              {/* Assign Staff */}
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Assign Staff Member</label>
                <select
                  className="form-control"
                  value={triageForm.assignedToUserId}
                  onChange={(e) => setTriageForm({ ...triageForm, assignedToUserId: Number(e.target.value) })}
                >
                  <option value={0}>— Unassigned —</option>
                  {staffMembers.map((staff) => (
                    <option key={staff.id} value={staff.id}>
                      {staff.name} (@{staff.username})
                    </option>
                  ))}
                </select>
              </div>

              <button
                type="submit"
                disabled={savingTriage}
                className="btn btn-primary"
                style={{ width: '100%', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem', fontWeight: 600 }}
              >
                <Save size={16} />
                <span>{savingTriage ? 'Saving...' : 'Save Triage Changes'}</span>
              </button>
            </form>
          </div>

          {/* Requester Profile Card */}
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 800, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-main)' }}>
              <UserCheck size={18} color="var(--primary)" /> Requester Profile
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.85rem' }}>
              <div>
                <span style={{ color: 'var(--text-subtle)', display: 'block', fontSize: '0.75rem' }}>Username</span>
                <strong style={{ color: 'var(--text-main)', fontSize: '0.95rem' }}>@{ticket.username}</strong>
              </div>

              <div>
                <span style={{ color: 'var(--text-subtle)', display: 'block', fontSize: '0.75rem' }}>Full Name</span>
                <span style={{ color: 'var(--text-main)' }}>{ticket.userName || ticket.username}</span>
              </div>

              <div>
                <span style={{ color: 'var(--text-subtle)', display: 'block', fontSize: '0.75rem' }}>Email Address</span>
                <span style={{ color: 'var(--text-main)', fontFamily: 'var(--font-mono)' }}>{ticket.userEmail}</span>
              </div>

              <div>
                <span style={{ color: 'var(--text-subtle)', display: 'block', fontSize: '0.75rem' }}>User ID</span>
                <span style={{ color: 'var(--text-muted)' }}>#{ticket.userId}</span>
              </div>
            </div>
          </div>

          {/* User Rating Card if rated */}
          {ticket.rating && (
            <div
              className="card"
              style={{
                padding: '1.5rem',
                border: '1px solid rgba(245, 158, 11, 0.35)',
                background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.08) 0%, rgba(245, 158, 11, 0.02) 100%)'
              }}
            >
              <h3 style={{ fontSize: '1.05rem', fontWeight: 800, margin: '0 0 0.5rem 0', display: 'flex', alignItems: 'center', gap: '0.4rem', color: '#b45309' }}>
                <Star size={18} color="#f59e0b" fill="#f59e0b" /> Customer Satisfaction
              </h3>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', margin: '0.5rem 0' }}>
                {[1, 2, 3, 4, 5].map((s) => (
                  <Star
                    key={s}
                    size={18}
                    color={s <= ticket.rating ? '#f59e0b' : 'var(--text-subtle)'}
                    fill={s <= ticket.rating ? '#f59e0b' : 'transparent'}
                  />
                ))}
                <span style={{ fontWeight: 800, fontSize: '0.95rem', color: '#f59e0b', marginLeft: '0.4rem' }}>
                  {ticket.rating}/5
                </span>
              </div>

              {ticket.ratingComments ? (
                <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.85rem', color: 'var(--text-main)', fontStyle: 'italic' }}>
                  "{ticket.ratingComments}"
                </p>
              ) : (
                <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  (No comment text provided)
                </p>
              )}

              {ticket.ratedAt && (
                <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '0.5rem' }}>
                  Rated on {new Date(ticket.ratedAt).toLocaleString()}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminSupportTicketDetail;
