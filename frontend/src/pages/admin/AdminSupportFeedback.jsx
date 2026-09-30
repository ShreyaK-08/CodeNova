import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LifeBuoy,
  MessageSquare,
  HelpCircle,
  CheckCircle2,
  Clock,
  AlertCircle,
  RefreshCw,
  Star,
  Search,
  Check,
  RotateCcw,
  ClipboardList,
  Filter,
  BarChart3,
  TrendingUp,
  UserCheck,
  ChevronRight,
  ShieldAlert,
  Flame,
  User,
  Shield,
  ExternalLink,
  Layers,
  Sparkles,
  SlidersHorizontal,
  Lock
} from 'lucide-react';
import supportFeedbackService from '../../services/supportFeedbackService';
import { SUPPORT_CATEGORIES } from '../user/Support';

const PRIORITIES = ['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
const STATUSES = ['ALL', 'OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER', 'RESOLVED', 'CLOSED'];

const AdminSupportFeedback = () => {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('support'); // 'support' | 'analytics' | 'post-assessment' | 'question-reports' | 'general'
  const [supportRequests, setSupportRequests] = useState([]);
  const [analytics, setAnalytics] = useState(null);
  const [staffMembers, setStaffMembers] = useState([]);
  const [postAssessmentFeedback, setPostAssessmentFeedback] = useState([]);
  const [questionReports, setQuestionReports] = useState([]);
  const [generalFeedback, setGeneralFeedback] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [filterCategory, setFilterCategory] = useState('ALL');
  const [filterStaff, setFilterStaff] = useState('ALL');

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [supportRes, analyticsRes, staffRes, postAssessRes, questionRes, generalRes] = await Promise.all([
        supportFeedbackService.getAdminSupportRequests({
          status: filterStatus !== 'ALL' ? filterStatus : undefined,
          priority: filterPriority !== 'ALL' ? filterPriority : undefined,
          category: filterCategory !== 'ALL' ? filterCategory : undefined,
          assignedToId: filterStaff !== 'ALL' ? Number(filterStaff) : undefined,
          search: searchTerm.trim() || undefined
        }),
        supportFeedbackService.getSupportAnalytics(),
        supportFeedbackService.getStaffMembers(),
        supportFeedbackService.getAdminAssessmentPostFeedback(),
        supportFeedbackService.getAdminAssessmentQuestionFeedback(),
        supportFeedbackService.getAdminGeneralFeedback()
      ]);
      setSupportRequests(supportRes || []);
      setAnalytics(analyticsRes);
      setStaffMembers(staffRes || []);
      setPostAssessmentFeedback(postAssessRes || []);
      setQuestionReports(questionRes || []);
      setGeneralFeedback(generalRes || []);
    } catch (err) {
      console.error('Failed to load admin support data', err);
      setError('Failed to fetch data from the server.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [filterStatus, filterPriority, filterCategory, filterStaff]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadData();
  };

  const handleQuickStatusChange = async (id, newStatus, e) => {
    e.stopPropagation();
    try {
      setUpdatingId(id);
      await supportFeedbackService.adminTriageTicket(id, { status: newStatus });
      setSupportRequests((prev) =>
        prev.map((req) => (req.id === id ? { ...req, status: newStatus } : req))
      );
      // Refresh analytics
      const updatedAnalytics = await supportFeedbackService.getSupportAnalytics();
      setAnalytics(updatedAnalytics);
    } catch (err) {
      alert('Failed to update status: ' + (err.response?.data?.message || err.message));
    } finally {
      setUpdatingId(null);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OPEN':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--primary-soft)', color: 'var(--primary)' }}>
            <Clock size={11} /> Open
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(59, 130, 246, 0.1)', color: '#2563eb' }}>
            <RefreshCw size={11} className="animate-spin" /> In Progress
          </span>
        );
      case 'WAITING_FOR_USER':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--warning-soft)', color: 'var(--warning)' }}>
            <Clock size={11} /> Waiting for User
          </span>
        );
      case 'RESOLVED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'var(--success-soft)', color: 'var(--success)' }}>
            <CheckCircle2 size={11} /> Resolved
          </span>
        );
      case 'CLOSED':
        return (
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 700, backgroundColor: 'rgba(100, 116, 139, 0.15)', color: 'var(--text-muted)' }}>
            <Lock size={11} /> Closed
          </span>
        );
      default:
        return <span>{status}</span>;
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'CRITICAL':
        return <span style={{ fontSize: '0.7rem', fontWeight: 800, color: 'var(--danger)', backgroundColor: 'var(--danger-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>CRITICAL</span>;
      case 'HIGH':
        return <span style={{ fontSize: '0.7rem', fontWeight: 800, color: '#ea580c', backgroundColor: 'rgba(234, 88, 12, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>HIGH</span>;
      case 'MEDIUM':
        return <span style={{ fontSize: '0.7rem', fontWeight: 800, color: 'var(--primary)', backgroundColor: 'var(--primary-soft)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>MEDIUM</span>;
      case 'LOW':
        return <span style={{ fontSize: '0.7rem', fontWeight: 800, color: 'var(--text-muted)', backgroundColor: 'rgba(100, 116, 139, 0.1)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>LOW</span>;
      default:
        return <span>{priority}</span>;
    }
  };

  return (
    <div>
      {/* Page Header */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--primary)', fontWeight: 700, fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.25rem' }}>
            <Sparkles size={16} /> Admin Operations
          </div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem', margin: 0 }}>
            <HelpCircle size={28} color="var(--primary)" /> Support & Feedback Management
          </h1>
          <p style={{ color: 'var(--text-muted)', margin: '0.35rem 0 0 0', fontSize: '0.9rem' }}>
            Triage customer support requests, manage internal notes, AI response drafts, and track customer satisfaction analytics.
          </p>
        </div>

        <button
          onClick={loadData}
          disabled={loading}
          className="btn btn-outline btn-sm"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          <span>Refresh Data</span>
        </button>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: '1.5rem' }}>
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Top Metric Cards */}
      {analytics && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
            gap: '1rem',
            marginBottom: '1.75rem'
          }}
        >
          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Total Tickets</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '0.2rem' }}>{analytics.totalTickets}</div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: 'var(--primary)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Open</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.2rem' }}>{analytics.openTickets}</div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: '#2563eb', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>In Progress</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#2563eb', marginTop: '0.2rem' }}>{analytics.inProgressTickets}</div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: 'var(--warning)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Waiting on User</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--warning)', marginTop: '0.2rem' }}>{analytics.waitingForUserTickets}</div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: 'var(--danger)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Critical / High</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--danger)', marginTop: '0.2rem' }}>
              {(analytics.criticalPriorityCount || 0) + (analytics.highPriorityCount || 0)}
            </div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: 'var(--success)', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Resolved</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--success)', marginTop: '0.2rem' }}>{analytics.resolvedTickets}</div>
          </div>

          <div className="card" style={{ padding: '1.1rem' }}>
            <div style={{ color: '#f59e0b', fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase' }}>Avg CSAT</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f59e0b', marginTop: '0.2rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
              <Star size={20} fill="#f59e0b" color="#f59e0b" />
              <span>{analytics.averageSatisfactionRating || '5.0'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Tabs Navigation */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem' }}>
        <button
          onClick={() => setActiveTab('support')}
          className={`btn btn-sm ${activeTab === 'support' ? 'btn-primary' : 'btn-outline'}`}
          style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <LifeBuoy size={15} /> Support Tickets ({supportRequests.length})
        </button>
        <button
          onClick={() => setActiveTab('analytics')}
          className={`btn btn-sm ${activeTab === 'analytics' ? 'btn-primary' : 'btn-outline'}`}
          style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <BarChart3 size={15} /> Support Analytics & CSAT
        </button>
        <button
          onClick={() => setActiveTab('post-assessment')}
          className={`btn btn-sm ${activeTab === 'post-assessment' ? 'btn-primary' : 'btn-outline'}`}
          style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <ClipboardList size={15} /> Assessment Feedback ({postAssessmentFeedback.length})
        </button>
        <button
          onClick={() => setActiveTab('question-reports')}
          className={`btn btn-sm ${activeTab === 'question-reports' ? 'btn-primary' : 'btn-outline'}`}
          style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <AlertCircle size={15} /> Question Reports ({questionReports.length})
        </button>
        <button
          onClick={() => setActiveTab('general')}
          className={`btn btn-sm ${activeTab === 'general' ? 'btn-primary' : 'btn-outline'}`}
          style={{ fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <MessageSquare size={15} /> General Feedback ({generalFeedback.length})
        </button>
      </div>

      {/* TAB 1: Support Tickets List */}
      {activeTab === 'support' && (
        <>
          {/* Advanced Filter Toolbar */}
          <div
            className="card"
            style={{
              marginBottom: '1.5rem',
              padding: '1.25rem',
              display: 'flex',
              flexDirection: 'column',
              gap: '1rem'
            }}
          >
            <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.5rem', width: '100%' }}>
              <div style={{ position: 'relative', flex: 1 }}>
                <Search size={16} color="var(--text-subtle)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                <input
                  type="text"
                  className="form-control"
                  style={{ paddingLeft: '2.4rem' }}
                  placeholder="Search tickets by ID (CN-SUP-000001), Requester username, Email, or Subject..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <button type="submit" className="btn btn-outline btn-sm">
                Search
              </button>
            </form>

            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
              {/* Status Filter */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Status:</span>
                <select
                  className="form-control"
                  style={{ width: 'auto', padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                >
                  {STATUSES.map((st) => (
                    <option key={st} value={st}>
                      {st === 'ALL' ? 'All Statuses' : st}
                    </option>
                  ))}
                </select>
              </div>

              {/* Priority Filter */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Priority:</span>
                <select
                  className="form-control"
                  style={{ width: 'auto', padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                  value={filterPriority}
                  onChange={(e) => setFilterPriority(e.target.value)}
                >
                  {PRIORITIES.map((pr) => (
                    <option key={pr} value={pr}>
                      {pr === 'ALL' ? 'All Priorities' : pr}
                    </option>
                  ))}
                </select>
              </div>

              {/* Category Filter */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Category:</span>
                <select
                  className="form-control"
                  style={{ width: 'auto', padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                  value={filterCategory}
                  onChange={(e) => setFilterCategory(e.target.value)}
                >
                  <option value="ALL">All Categories</option>
                  {SUPPORT_CATEGORIES.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))}
                </select>
              </div>

              {/* Staff Filter */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.85rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>Staff:</span>
                <select
                  className="form-control"
                  style={{ width: 'auto', padding: '0.35rem 0.65rem', fontSize: '0.85rem' }}
                  value={filterStaff}
                  onChange={(e) => setFilterStaff(e.target.value)}
                >
                  <option value="ALL">All Staff</option>
                  {staffMembers.map((staff) => (
                    <option key={staff.id} value={staff.id}>
                      {staff.name} (@{staff.username})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          {/* Tickets Table */}
          <div className="card">
            {loading ? (
              <div style={{ textAlign: 'center', padding: '3.5rem 0', color: 'var(--text-muted)' }}>
                <RefreshCw size={24} className="animate-spin" style={{ margin: '0 auto 0.75rem auto' }} />
                <p>Loading support tickets...</p>
              </div>
            ) : supportRequests.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-muted)' }}>
                <LifeBuoy size={40} style={{ margin: '0 auto 0.75rem auto', opacity: 0.4 }} />
                <p style={{ fontWeight: 600 }}>No support tickets matching the selected filters.</p>
              </div>
            ) : (
              <div className="table-container">
                <table>
                  <thead>
                    <tr>
                      <th>Ticket ID</th>
                      <th>Requester</th>
                      <th>Subject & Preview</th>
                      <th>Category</th>
                      <th>Priority</th>
                      <th>Status</th>
                      <th>Assigned To</th>
                      <th>Date</th>
                      <th style={{ textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {supportRequests.map((req) => {
                      const isResolved = req.status === 'RESOLVED';
                      return (
                        <tr
                          key={req.id}
                          style={{ cursor: 'pointer', transition: 'background-color 0.15s ease' }}
                          onClick={() => navigate(`/admin/support/tickets/${req.id}`)}
                        >
                          <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: 'var(--primary)', whiteSpace: 'nowrap' }}>
                            {req.ticketNumber || `CN-SUP-${req.id}`}
                          </td>
                          <td style={{ whiteSpace: 'nowrap' }}>
                            <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>@{req.username}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{req.userEmail}</div>
                          </td>
                          <td style={{ maxWidth: '300px' }}>
                            <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.2rem' }}>
                              {req.subject}
                            </div>
                            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                              {req.lastMessagePreview || req.description}
                            </div>
                            {req.rating && (
                              <div style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: '#f59e0b', fontWeight: 700 }}>
                                ★ {req.rating}/5 CSAT
                              </div>
                            )}
                          </td>
                          <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem', whiteSpace: 'nowrap' }}>
                            {req.category}
                          </td>
                          <td style={{ whiteSpace: 'nowrap' }}>
                            {getPriorityBadge(req.priority)}
                          </td>
                          <td style={{ whiteSpace: 'nowrap' }}>
                            {getStatusBadge(req.status)}
                          </td>
                          <td style={{ fontSize: '0.85rem', whiteSpace: 'nowrap' }}>
                            {req.assignedToName ? (
                              <span style={{ color: 'var(--primary)', fontWeight: 600 }}>{req.assignedToName}</span>
                            ) : (
                              <span style={{ color: 'var(--text-subtle)', fontStyle: 'italic' }}>Unassigned</span>
                            )}
                          </td>
                          <td style={{ color: 'var(--text-subtle)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                            {req.createdAt ? new Date(req.createdAt).toLocaleDateString() : '—'}
                          </td>
                          <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                            <div style={{ display: 'inline-flex', gap: '0.4rem' }}>
                              {req.status !== 'RESOLVED' && req.status !== 'CLOSED' ? (
                                <button
                                  onClick={(e) => handleQuickStatusChange(req.id, 'RESOLVED', e)}
                                  disabled={updatingId === req.id}
                                  className="btn btn-sm btn-success"
                                  style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                                  title="Quick Resolve"
                                >
                                  <Check size={12} />
                                </button>
                              ) : (
                                <button
                                  onClick={(e) => handleQuickStatusChange(req.id, 'OPEN', e)}
                                  disabled={updatingId === req.id}
                                  className="btn btn-sm btn-outline"
                                  style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                                  title="Reopen"
                                >
                                  <RotateCcw size={12} />
                                </button>
                              )}

                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  navigate(`/admin/support/tickets/${req.id}`);
                                }}
                                className="btn btn-primary btn-sm"
                                style={{ padding: '0.25rem 0.65rem', fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}
                              >
                                <span>Manage</span>
                                <ChevronRight size={13} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}

      {/* TAB 2: Support Analytics */}
      {activeTab === 'analytics' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {analytics ? (
            <>
              {/* Analytics Highlights */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                  gap: '1rem'
                }}
              >
                <div className="card" style={{ padding: '1.5rem', borderLeft: '4px solid #f59e0b' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 600 }}>Customer Satisfaction (CSAT)</div>
                  <div style={{ fontSize: '2.2rem', fontWeight: 800, color: '#f59e0b', marginTop: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Star size={28} fill="#f59e0b" color="#f59e0b" />
                    <span>{analytics.averageSatisfactionRating || '5.0'}</span>
                    <span style={{ fontSize: '1rem', color: 'var(--text-subtle)', fontWeight: 500 }}>/ 5.0</span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', marginTop: '0.4rem' }}>
                    Based on {analytics.totalRatedTickets || 0} user ratings
                  </div>
                </div>

                <div className="card" style={{ padding: '1.5rem', borderLeft: '4px solid var(--primary)' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 600 }}>Avg Resolution Time</div>
                  <div style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.4rem' }}>
                    {analytics.averageResolutionTimeHours || 0.5} <span style={{ fontSize: '1rem', color: 'var(--text-subtle)', fontWeight: 500 }}>hrs</span>
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', marginTop: '0.4rem' }}>
                    From ticket creation to resolution
                  </div>
                </div>

                <div className="card" style={{ padding: '1.5rem', borderLeft: '4px solid var(--success)' }}>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', fontWeight: 600 }}>Resolution Rate</div>
                  <div style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--success)', marginTop: '0.4rem' }}>
                    {analytics.totalTickets > 0 ? Math.round(((analytics.resolvedTickets + analytics.closedTickets) / analytics.totalTickets) * 100) : 100}%
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', marginTop: '0.4rem' }}>
                    {analytics.resolvedTickets + analytics.closedTickets} of {analytics.totalTickets} tickets resolved
                  </div>
                </div>
              </div>

              {/* Charts Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '1.5rem' }}>
                {/* Tickets by Category */}
                <div className="card" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Layers size={18} color="var(--primary)" /> Tickets by Category
                  </h3>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                    {Object.entries(analytics.ticketsByCategory || {}).map(([cat, count]) => {
                      const total = analytics.totalTickets || 1;
                      const pct = Math.round((count / total) * 100);
                      return (
                        <div key={cat}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                            <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{cat}</span>
                            <span style={{ color: 'var(--text-subtle)' }}>{count} ({pct}%)</span>
                          </div>
                          <div style={{ height: '8px', backgroundColor: 'var(--bg-subtle)', borderRadius: '9999px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${pct}%`, backgroundColor: 'var(--primary)', borderRadius: '9999px' }} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Rating Distribution */}
                <div className="card" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <Star size={18} color="#f59e0b" /> CSAT Rating Distribution
                  </h3>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                    {[5, 4, 3, 2, 1].map((stars) => {
                      const count = (analytics.ratingDistribution && analytics.ratingDistribution[stars]) || 0;
                      const totalRated = analytics.totalRatedTickets || 1;
                      const pct = analytics.totalRatedTickets > 0 ? Math.round((count / totalRated) * 100) : 0;
                      return (
                        <div key={stars}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                            <span style={{ fontWeight: 600, color: 'var(--text-main)', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                              {'★'.repeat(stars)} ({stars} Star)
                            </span>
                            <span style={{ color: 'var(--text-subtle)' }}>{count} ({pct}%)</span>
                          </div>
                          <div style={{ height: '8px', backgroundColor: 'var(--bg-subtle)', borderRadius: '9999px', overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${pct}%`, backgroundColor: '#f59e0b', borderRadius: '9999px' }} />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '3.5rem 0', color: 'var(--text-muted)' }}>
              Loading analytics...
            </div>
          )}
        </div>
      )}

      {/* TAB 3: Post-Completion Assessment Feedback */}
      {activeTab === 'post-assessment' && (
        <div className="card">
          {postAssessmentFeedback.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-muted)' }}>
              <ClipboardList size={40} style={{ margin: '0 auto 0.75rem auto', opacity: 0.4 }} />
              <p style={{ fontWeight: 500 }}>No post-completion assessment feedback received yet.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Candidate</th>
                    <th>Assessment</th>
                    <th>Host</th>
                    <th style={{ textAlign: 'center' }}>Rating</th>
                    <th>Type</th>
                    <th>Comments</th>
                    <th style={{ textAlign: 'right' }}>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {postAssessmentFeedback.map((item) => (
                    <tr key={item.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>@{item.username}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{item.userEmail}</div>
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--text-main)' }}>
                        {item.assessmentTitle || `Assessment #${item.assessmentId}`}
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem', whiteSpace: 'nowrap' }}>
                        {item.hostUsername ? `@${item.hostUsername}` : 'Admin'}
                      </td>
                      <td style={{ textAlign: 'center', whiteSpace: 'nowrap' }}>
                        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}>
                          {[1, 2, 3, 4, 5].map((star) => (
                            <Star
                              key={star}
                              size={13}
                              color={(item.rating || 0) >= star ? '#f59e0b' : 'var(--text-subtle)'}
                              fill={(item.rating || 0) >= star ? '#f59e0b' : 'transparent'}
                            />
                          ))}
                          <span style={{ fontSize: '0.8rem', fontWeight: 600, marginLeft: '0.25rem' }}>({item.rating}/5)</span>
                        </div>
                      </td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <span
                          style={{
                            padding: '0.25rem 0.6rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            backgroundColor: 'var(--primary-soft)',
                            color: 'var(--primary)',
                          }}
                        >
                          {item.feedbackType}
                        </span>
                      </td>
                      <td style={{ maxWidth: '300px', fontSize: '0.85rem', color: 'var(--text-main)', whiteSpace: 'pre-wrap' }}>
                        {item.comments || <span style={{ color: 'var(--text-subtle)', fontStyle: 'italic' }}>—</span>}
                      </td>
                      <td style={{ textAlign: 'right', color: 'var(--text-subtle)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                        {item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: Question Reports */}
      {activeTab === 'question-reports' && (
        <div className="card">
          {questionReports.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-muted)' }}>
              <AlertCircle size={40} style={{ margin: '0 auto 0.75rem auto', opacity: 0.4 }} />
              <p style={{ fontWeight: 500 }}>No assessment question issues reported yet.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Assessment</th>
                    <th>Question</th>
                    <th>Reason</th>
                    <th>Message</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {questionReports.map((item) => (
                    <tr key={item.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>@{item.username}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{item.userEmail}</div>
                      </td>
                      <td style={{ fontWeight: 600, color: 'var(--text-main)', whiteSpace: 'nowrap' }}>
                        {item.assessmentTitle || `Assessment #${item.assessmentId}`}
                      </td>
                      <td style={{ maxWidth: '280px', color: 'var(--text-main)' }} title={item.questionText}>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {item.questionText || `Question #${item.questionId}`}
                        </div>
                      </td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <span
                          style={{
                            padding: '0.25rem 0.6rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            backgroundColor: 'var(--danger-soft)',
                            color: 'var(--danger)',
                          }}
                        >
                          {item.reason}
                        </span>
                      </td>
                      <td style={{ maxWidth: '320px', fontSize: '0.85rem', color: 'var(--text-muted)', whiteSpace: 'pre-wrap' }}>
                        {item.message || '—'}
                      </td>
                      <td style={{ color: 'var(--text-subtle)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                        {item.createdAt ? new Date(item.createdAt).toLocaleString() : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* TAB 5: General Feedback */}
      {activeTab === 'general' && (
        <div className="card">
          {generalFeedback.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3.5rem 1rem', color: 'var(--text-muted)' }}>
              <MessageSquare size={40} style={{ margin: '0 auto 0.75rem auto', opacity: 0.4 }} />
              <p style={{ fontWeight: 500 }}>No general feedback received yet.</p>
            </div>
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Type</th>
                    <th>Rating</th>
                    <th>Message</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {generalFeedback.map((item) => (
                    <tr key={item.id}>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-main)' }}>@{item.username}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{item.userEmail}</div>
                      </td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <span
                          style={{
                            padding: '0.25rem 0.6rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            backgroundColor: 'var(--primary-soft)',
                            color: 'var(--primary)',
                          }}
                        >
                          {item.feedbackType}
                        </span>
                      </td>
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                          {[1, 2, 3, 4, 5].map((star) => (
                            <Star
                              key={star}
                              size={15}
                              color={(item.rating || 0) >= star ? '#f59e0b' : 'var(--text-subtle)'}
                              fill={(item.rating || 0) >= star ? '#f59e0b' : 'transparent'}
                            />
                          ))}
                          <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginLeft: '0.35rem' }}>
                            ({item.rating}/5)
                          </span>
                        </div>
                      </td>
                      <td style={{ maxWidth: '380px', fontSize: '0.85rem', color: 'var(--text-main)', whiteSpace: 'pre-wrap' }}>
                        {item.message}
                      </td>
                      <td style={{ color: 'var(--text-subtle)', fontSize: '0.8rem', whiteSpace: 'nowrap' }}>
                        {item.createdAt ? new Date(item.createdAt).toLocaleString() : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminSupportFeedback;
