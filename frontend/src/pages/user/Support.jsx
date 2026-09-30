import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  LifeBuoy,
  Send,
  CheckCircle2,
  Clock,
  AlertCircle,
  RefreshCw,
  HelpCircle,
  Search,
  Plus,
  Paperclip,
  X,
  FileText,
  ShieldAlert,
  ArrowRight,
  Filter,
  Check,
  ChevronRight,
  Sparkles,
  Inbox,
  AlertTriangle,
  Lock
} from 'lucide-react';
import supportFeedbackService from '../../services/supportFeedbackService';

export const SUPPORT_CATEGORIES = [
  'Account & Login',
  'Coding Problems',
  'Code Submission / Judge',
  'Assessments',
  'Assessment Question Issue',
  'Contests',
  'Contest Technical Issue',
  'Certificates',
  'Payments / Other Platform Issue',
  'Technical Problem',
  'Feature Request',
  'Other'
];

const PRIORITIES = [
  { value: 'LOW', label: 'Low (General question / low urgency)', color: 'var(--text-muted)' },
  { value: 'MEDIUM', label: 'Medium (Standard issue)', color: 'var(--primary)' },
  { value: 'HIGH', label: 'High (Blocks solving or assessment)', color: 'var(--warning)' },
  { value: 'CRITICAL', label: 'Critical (Platform failure / system bug)', color: 'var(--danger)' }
];

const Support = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Pre-fill context if query params exist (e.g. from problem/assessment/contest)
  const queryCategory = searchParams.get('category');
  const queryAssessmentId = searchParams.get('assessmentId');
  const queryAssessmentTitle = searchParams.get('assessmentTitle');
  const queryProblemId = searchParams.get('problemId');
  const queryProblemTitle = searchParams.get('problemTitle');
  const queryContestId = searchParams.get('contestId');
  const queryContestTitle = searchParams.get('contestTitle');

  const [requests, setRequests] = useState([]);
  const [summary, setSummary] = useState({
    totalTickets: 0,
    openTickets: 0,
    inProgressTickets: 0,
    waitingForUserTickets: 0,
    resolvedTickets: 0,
    closedTickets: 0
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('ALL');

  // Create Ticket Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [successTicket, setSuccessTicket] = useState(null);

  const [formData, setFormData] = useState({
    category: queryCategory && SUPPORT_CATEGORIES.includes(queryCategory) ? queryCategory : SUPPORT_CATEGORIES[0],
    subject: '',
    description: '',
    priority: 'MEDIUM',
    assessmentId: queryAssessmentId ? Number(queryAssessmentId) : null,
    assessmentTitle: queryAssessmentTitle || '',
    problemId: queryProblemId ? Number(queryProblemId) : null,
    problemTitle: queryProblemTitle || '',
    contestId: queryContestId ? Number(queryContestId) : null,
    contestTitle: queryContestTitle || '',
    submissionId: null,
    attachmentUrl: '',
    attachmentName: '',
    attachmentType: '',
    attachmentSize: null
  });

  const [uploadingFile, setUploadingFile] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [reqList, sumData] = await Promise.all([
        supportFeedbackService.getMySupportRequests({
          status: selectedStatus !== 'ALL' ? selectedStatus : undefined,
          category: selectedCategory !== 'ALL' ? selectedCategory : undefined,
          search: search.trim() || undefined
        }),
        supportFeedbackService.getUserSupportSummary()
      ]);
      setRequests(reqList || []);
      if (sumData) setSummary(sumData);
    } catch (err) {
      console.error('Failed to load support requests', err);
      setError('Could not load support requests. Please check your connection.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [selectedStatus, selectedCategory]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadData();
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setSubmitError('File size exceeds 10MB limit.');
      return;
    }

    try {
      setUploadingFile(true);
      setSubmitError('');
      const uploaded = await supportFeedbackService.uploadAttachment(file);
      setFormData((prev) => ({
        ...prev,
        attachmentUrl: uploaded.url,
        attachmentName: uploaded.fileName,
        attachmentType: uploaded.fileType,
        attachmentSize: uploaded.fileSize
      }));
    } catch (err) {
      setSubmitError(err.response?.data?.message || 'Failed to upload attachment.');
    } finally {
      setUploadingFile(false);
    }
  };

  const removeAttachment = () => {
    setFormData((prev) => ({
      ...prev,
      attachmentUrl: '',
      attachmentName: '',
      attachmentType: '',
      attachmentSize: null
    }));
  };

  const handleCreateTicket = async (e) => {
    e.preventDefault();
    if (!formData.subject.trim() || !formData.description.trim()) {
      setSubmitError('Please provide both a subject and problem description.');
      return;
    }

    try {
      setSubmitting(true);
      setSubmitError('');
      const res = await supportFeedbackService.createSupportRequest(formData);
      setSuccessTicket(res.ticketNumber || `CN-SUP-${res.id}`);
      setIsModalOpen(false);
      setFormData({
        category: SUPPORT_CATEGORIES[0],
        subject: '',
        description: '',
        priority: 'MEDIUM',
        assessmentId: null,
        assessmentTitle: '',
        problemId: null,
        problemTitle: '',
        contestId: null,
        contestTitle: '',
        submissionId: null,
        attachmentUrl: '',
        attachmentName: '',
        attachmentType: '',
        attachmentSize: null
      });
      loadData();
    } catch (err) {
      setSubmitError(err.response?.data?.message || 'Failed to submit support request.');
    } finally {
      setSubmitting(false);
    }
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
            <AlertTriangle size={12} /> Action Needed
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
        return <span style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--danger)', backgroundColor: 'var(--danger-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>CRITICAL</span>;
      case 'HIGH':
        return <span style={{ fontSize: '0.72rem', fontWeight: 700, color: '#ea580c', backgroundColor: 'rgba(234, 88, 12, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>HIGH</span>;
      case 'MEDIUM':
        return <span style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--primary)', backgroundColor: 'var(--primary-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>MEDIUM</span>;
      case 'LOW':
        return <span style={{ fontSize: '0.72rem', fontWeight: 700, color: 'var(--text-muted)', backgroundColor: 'rgba(100, 116, 139, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>LOW</span>;
      default:
        return <span>{priority}</span>;
    }
  };

  return (
    <div style={{ maxWidth: '1100px', margin: '0 auto', paddingBottom: '3rem' }}>
      {/* Top Banner */}
      <div
        style={{
          background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.08) 0%, rgba(99, 102, 241, 0.03) 100%)',
          border: '1px solid rgba(79, 70, 229, 0.2)',
          borderRadius: '16px',
          padding: '2rem 2.5rem',
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1.5rem'
        }}
      >
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)', fontWeight: 700, fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.4rem' }}>
            <Sparkles size={16} /> CodeNova Support Center
          </div>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-main)', margin: '0 0 0.5rem 0' }}>
            Help & Support
          </h1>
          <p style={{ color: 'var(--text-muted)', margin: 0, maxWidth: '600px', fontSize: '0.95rem', lineHeight: 1.5 }}>
            Encountered an issue with judge execution, assessment attempts, contests, or certificate verification? Our team is here to help.
          </p>
        </div>

        <button
          onClick={() => {
            setSubmitError('');
            setIsModalOpen(true);
          }}
          className="btn btn-primary"
          style={{ padding: '0.75rem 1.4rem', fontSize: '0.95rem', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.5rem', boxShadow: '0 4px 14px rgba(79, 70, 229, 0.3)' }}
        >
          <Plus size={18} />
          <span>New Support Ticket</span>
        </button>
      </div>

      {/* Success Notification */}
      {successTicket && (
        <div className="alert alert-success" style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <CheckCircle2 size={22} color="var(--success)" />
            <div>
              <strong>Support Ticket Created Successfully!</strong> Your tracking ID is{' '}
              <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, textDecoration: 'underline' }}>
                {successTicket}
              </span>. You can view progress and replies below.
            </div>
          </div>
          <button
            onClick={() => setSuccessTicket(null)}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
          >
            <X size={18} />
          </button>
        </div>
      )}

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Summary Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
          gap: '1rem',
          marginBottom: '2rem'
        }}
      >
        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'ALL' ? '2px solid var(--primary)' : '1px solid var(--border-color)',
            background: selectedStatus === 'ALL' ? 'var(--primary-soft)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('ALL')}
        >
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            All Tickets
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '0.25rem' }}>
            {summary.totalTickets}
          </div>
        </div>

        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'OPEN' ? '2px solid var(--primary)' : '1px solid var(--border-color)',
            background: selectedStatus === 'OPEN' ? 'rgba(79, 70, 229, 0.08)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('OPEN')}
        >
          <div style={{ color: 'var(--primary)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Open
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.25rem' }}>
            {summary.openTickets}
          </div>
        </div>

        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'IN_PROGRESS' ? '2px solid #2563eb' : '1px solid var(--border-color)',
            background: selectedStatus === 'IN_PROGRESS' ? 'rgba(37, 99, 235, 0.08)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('IN_PROGRESS')}
        >
          <div style={{ color: '#2563eb', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            In Progress
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#2563eb', marginTop: '0.25rem' }}>
            {summary.inProgressTickets}
          </div>
        </div>

        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'WAITING_FOR_USER' ? '2px solid var(--warning)' : '1px solid var(--border-color)',
            background: selectedStatus === 'WAITING_FOR_USER' ? 'rgba(245, 158, 11, 0.08)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('WAITING_FOR_USER')}
        >
          <div style={{ color: 'var(--warning)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Waiting For You
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--warning)', marginTop: '0.25rem' }}>
            {summary.waitingForUserTickets}
          </div>
        </div>

        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'RESOLVED' ? '2px solid var(--success)' : '1px solid var(--border-color)',
            background: selectedStatus === 'RESOLVED' ? 'rgba(16, 185, 129, 0.08)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('RESOLVED')}
        >
          <div style={{ color: 'var(--success)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Resolved
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--success)', marginTop: '0.25rem' }}>
            {summary.resolvedTickets}
          </div>
        </div>

        <div
          className="card"
          style={{
            padding: '1.25rem',
            cursor: 'pointer',
            border: selectedStatus === 'CLOSED' ? '2px solid var(--text-muted)' : '1px solid var(--border-color)',
            background: selectedStatus === 'CLOSED' ? 'rgba(100, 116, 139, 0.08)' : 'var(--bg-card)'
          }}
          onClick={() => setSelectedStatus('CLOSED')}
        >
          <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Closed
          </div>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            {summary.closedTickets}
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div
        className="card"
        style={{
          marginBottom: '1.5rem',
          padding: '1rem 1.25rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem'
        }}
      >
        <form onSubmit={handleSearchSubmit} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: '1 1 300px' }}>
          <div style={{ position: 'relative', width: '100%' }}>
            <Search size={16} color="var(--text-subtle)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              className="form-control"
              style={{ paddingLeft: '2.4rem' }}
              placeholder="Search by Ticket ID (CN-SUP-000001), Subject, or Details..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button type="submit" className="btn btn-outline btn-sm" style={{ whiteSpace: 'nowrap' }}>
            Search
          </button>
        </form>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            <Filter size={14} /> Category:
          </div>
          <select
            className="form-control"
            style={{ width: 'auto', fontSize: '0.85rem', padding: '0.4rem 0.75rem' }}
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            <option value="ALL">All Categories</option>
            {SUPPORT_CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>

          <button
            onClick={loadData}
            disabled={loading}
            className="btn btn-outline btn-sm"
            title="Refresh"
          >
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Requests Table */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0 }}>My Support Requests</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: '0.2rem 0 0 0' }}>
              Select any ticket to view updates, chat with support, or submit feedback
            </p>
          </div>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: '3.5rem 0', color: 'var(--text-muted)' }}>
            <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 0.75rem auto' }} />
            <p>Loading your support tickets...</p>
          </div>
        ) : requests.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-muted)' }}>
            <Inbox size={48} style={{ margin: '0 auto 1rem auto', opacity: 0.35 }} />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
              No support tickets found
            </h3>
            <p style={{ maxWidth: '400px', margin: '0 auto 1.5rem auto', fontSize: '0.9rem' }}>
              {search || selectedCategory !== 'ALL' || selectedStatus !== 'ALL'
                ? 'Try clearing your search query or filters.'
                : 'Need assistance? Create a new support ticket and our team will get right on it.'}
            </p>
            <button
              onClick={() => setIsModalOpen(true)}
              className="btn btn-primary btn-sm"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
            >
              <Plus size={15} /> Create First Ticket
            </button>
          </div>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Ticket ID</th>
                  <th>Subject & Details</th>
                  <th>Category</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Last Updated</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((ticket) => (
                  <tr
                    key={ticket.id}
                    style={{ cursor: 'pointer', transition: 'background-color 0.15s ease' }}
                    onClick={() => navigate(`/support/tickets/${ticket.id}`)}
                  >
                    <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--primary)', whiteSpace: 'nowrap' }}>
                      {ticket.ticketNumber || `CN-SUP-${ticket.id}`}
                    </td>
                    <td style={{ maxWidth: '340px' }}>
                      <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.2rem' }}>
                        {ticket.subject}
                      </div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {ticket.lastMessagePreview || ticket.description}
                      </div>
                      {ticket.rating && (
                        <div style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: '#f59e0b', fontWeight: 600 }}>
                          ★ {ticket.rating}/5 Rated
                        </div>
                      )}
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem', whiteSpace: 'nowrap' }}>
                      {ticket.category}
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {getPriorityBadge(ticket.priority)}
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {getStatusBadge(ticket.status)}
                    </td>
                    <td style={{ color: 'var(--text-subtle)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                      {ticket.updatedAt ? new Date(ticket.updatedAt).toLocaleDateString() : (ticket.createdAt ? new Date(ticket.createdAt).toLocaleDateString() : '—')}
                    </td>
                    <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/support/tickets/${ticket.id}`);
                        }}
                        className="btn btn-outline btn-sm"
                        style={{ padding: '0.35rem 0.75rem', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}
                      >
                        <span>View</span>
                        <ChevronRight size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* CREATE TICKET MODAL */}
      {isModalOpen && (
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
          onClick={() => !submitting && setIsModalOpen(false)}
        >
          <div
            className="card"
            style={{
              maxWidth: '650px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '2rem',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.2)'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <HelpCircle size={22} color="var(--primary)" />
                <h2 style={{ fontSize: '1.3rem', fontWeight: 800, margin: 0 }}>Create Support Request</h2>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                disabled={submitting}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
              >
                <X size={20} />
              </button>
            </div>

            {submitError && (
              <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
                <AlertCircle size={18} />
                <span>{submitError}</span>
              </div>
            )}

            <form onSubmit={handleCreateTicket}>
              {/* Category */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.875rem' }}>Category *</label>
                <select
                  className="form-control"
                  value={formData.category}
                  onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                  required
                >
                  {SUPPORT_CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>
              </div>

              {/* Priority */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.875rem' }}>Priority</label>
                <select
                  className="form-control"
                  value={formData.priority}
                  onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                >
                  {PRIORITIES.map((p) => (
                    <option key={p.value} value={p.value}>
                      {p.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* Subject */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.875rem' }}>Subject *</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="e.g. Assessment timer frozen on Question 3"
                  value={formData.subject}
                  onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                  required
                />
              </div>

              {/* Problem Description */}
              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.875rem' }}>Problem Description *</label>
                <textarea
                  className="form-control"
                  rows={5}
                  placeholder="Describe what happened, any error messages you encountered, and steps to reproduce the issue..."
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  required
                />
              </div>

              {/* Optional Context Information */}
              {(formData.assessmentTitle || formData.problemTitle || formData.contestTitle) && (
                <div style={{ backgroundColor: 'var(--primary-soft)', padding: '0.75rem 1rem', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.85rem' }}>
                  <div style={{ fontWeight: 700, color: 'var(--primary)', marginBottom: '0.25rem' }}>Linked Context:</div>
                  {formData.assessmentTitle && <div>Assessment: {formData.assessmentTitle}</div>}
                  {formData.problemTitle && <div>Problem: {formData.problemTitle}</div>}
                  {formData.contestTitle && <div>Contest: {formData.contestTitle}</div>}
                </div>
              )}

              {/* Attachment Dropzone */}
              <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                <label style={{ fontWeight: 600, fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Paperclip size={15} /> Attachment (Optional: PNG, JPG, PDF, TXT up to 10MB)
                </label>

                {formData.attachmentName ? (
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.6rem 0.85rem', backgroundColor: 'var(--bg-subtle)', borderRadius: '6px', border: '1px solid var(--border-color)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                      <FileText size={16} color="var(--primary)" />
                      <span style={{ fontWeight: 600 }}>{formData.attachmentName}</span>
                      {formData.attachmentSize && (
                        <span style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>
                          ({Math.round(formData.attachmentSize / 1024)} KB)
                        </span>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={removeAttachment}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--danger)' }}
                      title="Remove attachment"
                    >
                      <X size={16} />
                    </button>
                  </div>
                ) : (
                  <div>
                    <input
                      type="file"
                      id="file-upload"
                      style={{ display: 'none' }}
                      accept=".png,.jpg,.jpeg,.webp,.pdf,.txt"
                      onChange={handleFileUpload}
                      disabled={uploadingFile}
                    />
                    <label
                      htmlFor="file-upload"
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '0.5rem',
                        padding: '0.75rem',
                        border: '2px dashed var(--border-color)',
                        borderRadius: '8px',
                        cursor: uploadingFile ? 'not-allowed' : 'pointer',
                        color: 'var(--text-muted)',
                        fontSize: '0.85rem',
                        backgroundColor: 'var(--bg-subtle)'
                      }}
                    >
                      <Paperclip size={16} />
                      <span>{uploadingFile ? 'Uploading file...' : 'Click to select screenshot or log file'}</span>
                    </label>
                  </div>
                )}
              </div>

              {/* Submit Buttons */}
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  disabled={submitting}
                  className="btn btn-outline"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting || uploadingFile}
                  className="btn btn-primary"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}
                >
                  <Send size={16} />
                  <span>{submitting ? 'Submitting...' : 'Create Ticket'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Support;
