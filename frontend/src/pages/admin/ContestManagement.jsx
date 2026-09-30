import React, { useState, useEffect, useMemo } from 'react';
import contestService from '../../services/contestService';
import problemService from '../../services/problemService';
import { formatContestDateTime } from '../../utils/contestTime';
import {
  PlusCircle,
  Edit,
  Trash2,
  Trophy,
  AlertCircle,
  CheckCircle,
  X,
  Save,
  Eye,
  Search,
  Users,
  List,
  ArrowUp,
  ArrowDown,
  Send,
} from 'lucide-react';

const STATUS_STYLES = {
  DRAFT: { color: 'var(--text-muted)', bg: 'var(--bg-input)', label: 'Draft' },
  PUBLISHED: { color: 'var(--primary)', bg: 'var(--primary-soft)', label: 'Published' },
  UPCOMING: { color: 'var(--primary)', bg: 'var(--primary-soft)', label: 'Upcoming' },
  ONGOING: { color: 'var(--success)', bg: 'var(--success-soft)', label: 'Ongoing' },
  ENDED: { color: 'var(--text-subtle)', bg: 'var(--bg-input)', label: 'Ended' },
};

const emptyFormData = {
  title: '',
  organizationName: '',
  description: '',
  startTime: '',
  endTime: '',
  problems: [], // [{ problemId, displayOrder, points, title, difficulty }]
};

// Converts a backend ISO datetime string ("2026-01-15T10:00:00") to the value a
// <input type="datetime-local"> needs ("2026-01-15T10:00").
const toDatetimeLocalValue = (isoString) => {
  if (!isoString) return '';
  return isoString.length >= 16 ? isoString.slice(0, 16) : isoString;
};

const ContestManagement = () => {
  const [contests, setContests] = useState([]);
  const [allProblems, setAllProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContestId, setEditingContestId] = useState(null);
  const [editingContestStatus, setEditingContestStatus] = useState('DRAFT');
  const [formData, setFormData] = useState(emptyFormData);
  const [formErrors, setFormErrors] = useState([]);
  const [saving, setSaving] = useState(false);

  const [viewContest, setViewContest] = useState(null);
  const [busyContestId, setBusyContestId] = useState(null); // disables actions on one row during publish/delete

  const fetchContests = async () => {
    try {
      setLoading(true);
      const data = await contestService.getAllContests();
      setContests(data);
    } catch (err) {
      setError('Failed to load contests.');
    } finally {
      setLoading(false);
    }
  };

  const fetchProblems = async () => {
    try {
      const data = await problemService.getAllProblems();
      setAllProblems(data);
    } catch (err) {
      // Non-fatal for the list page itself - only the create/edit form needs this.
      console.error('Failed to load problems for selection:', err);
    }
  };

  useEffect(() => {
    fetchContests();
    fetchProblems();
  }, []);

  const filteredContests = useMemo(() => {
    if (!searchTerm.trim()) return contests;
    const term = searchTerm.trim().toLowerCase();
    return contests.filter(
      (c) => c.title?.toLowerCase().includes(term) || c.organizationName?.toLowerCase().includes(term)
    );
  }, [contests, searchTerm]);

  // ---------------- Create / Edit form ----------------

  const openCreateModal = () => {
    setEditingContestId(null);
    setEditingContestStatus('DRAFT');
    setFormData(emptyFormData);
    setFormErrors([]);
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (contest) => {
    setEditingContestId(contest.id);
    setEditingContestStatus(contest.status);
    const seen = new Set();
    const uniqueProblems = [];
    for (const p of (contest.problems || [])) {
      if (p && p.problemId != null && !seen.has(p.problemId)) {
        seen.add(p.problemId);
        uniqueProblems.push({
          problemId: p.problemId,
          displayOrder: p.displayOrder,
          points: p.points,
          title: p.problemTitle,
          difficulty: p.difficulty,
        });
      }
    }
    setFormData({
      title: contest.title || '',
      organizationName: contest.organizationName || '',
      description: contest.description || '',
      startTime: toDatetimeLocalValue(contest.startTime),
      endTime: toDatetimeLocalValue(contest.endTime),
      problems: uniqueProblems,
    });
    setFormErrors([]);
    setError('');
    setIsModalOpen(true);
  };

  const closeModal = () => {
    if (saving) return;
    setIsModalOpen(false);
  };

  const toggleProblemSelected = (problem) => {
    setFormData((prev) => {
      const exists = prev.problems.find((p) => p.problemId === problem.id);
      if (exists) {
        const remaining = prev.problems.filter((p) => p.problemId !== problem.id);
        // Re-number display order so it stays contiguous after removal.
        const renumbered = remaining.map((p, idx) => ({ ...p, displayOrder: idx + 1 }));
        return { ...prev, problems: renumbered };
      }
      const next = {
        problemId: problem.id,
        displayOrder: prev.problems.length + 1,
        points: 100,
        title: problem.title,
        difficulty: problem.difficulty,
      };
      return { ...prev, problems: [...prev.problems, next] };
    });
  };

  const updateProblemPoints = (problemId, points) => {
    setFormData((prev) => ({
      ...prev,
      problems: prev.problems.map((p) => (p.problemId === problemId ? { ...p, points } : p)),
    }));
  };

  const moveProblem = (index, direction) => {
    setFormData((prev) => {
      const list = [...prev.problems];
      const targetIndex = index + direction;
      if (targetIndex < 0 || targetIndex >= list.length) return prev;
      [list[index], list[targetIndex]] = [list[targetIndex], list[index]];
      const renumbered = list.map((p, idx) => ({ ...p, displayOrder: idx + 1 }));
      return { ...prev, problems: renumbered };
    });
  };

  const validateForm = () => {
    const errors = [];
    if (!formData.title.trim()) errors.push('Contest name is required.');
    if (!formData.organizationName.trim()) errors.push('Organization name is required.');
    if (!formData.startTime) errors.push('Start date & time is required.');
    if (!formData.endTime) errors.push('End date & time is required.');
    if (formData.startTime && formData.endTime && new Date(formData.endTime) <= new Date(formData.startTime)) {
      errors.push('End time must be after start time.');
    }
    return errors;
  };

  const buildPayload = (statusOverride) => {
    const seen = new Set();
    const uniqueProblems = [];
    for (const p of (formData.problems || [])) {
      if (p && p.problemId != null && !seen.has(p.problemId)) {
        seen.add(p.problemId);
        uniqueProblems.push(p);
      }
    }
    return {
      title: formData.title.trim(),
      organizationName: formData.organizationName.trim(),
      description: formData.description.trim(),
      startTime: formData.startTime,
      endTime: formData.endTime,
      status: statusOverride || editingContestStatus,
      problems: uniqueProblems.map((p, idx) => ({
        problemId: p.problemId,
        displayOrder: p.displayOrder || (idx + 1),
        points: p.points,
      })),
    };
  };

  const handleSubmitWithStatus = async (chosenStatus) => {
    const errors = validateForm();
    setFormErrors(errors);
    if (errors.length > 0) return;

    setSaving(true);
    setError('');
    try {
      if (editingContestId) {
        await contestService.updateContest(editingContestId, buildPayload(chosenStatus));
        setSuccess('Contest updated successfully.' + (chosenStatus === 'PUBLISHED' ? ' Announcement emails queued for all registered users.' : ''));
      } else {
        await contestService.createContest(buildPayload(chosenStatus || 'PUBLISHED'));
        setSuccess('Contest created successfully!' + (chosenStatus === 'PUBLISHED' ? ' Announcement emails are being sent to all registered users.' : ''));
      }
      setIsModalOpen(false);
      fetchContests();
    } catch (err) {
      const backendErrors = err.response?.data?.errors;
      const message = backendErrors
        ? Object.values(backendErrors).join(' ')
        : err.response?.data?.message || 'Failed to save contest.';
      setFormErrors([message]);
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    handleSubmitWithStatus(editingContestId ? editingContestStatus : 'PUBLISHED');
  };

  const handleBroadcastAnnouncement = async (contest) => {
    if (!window.confirm(`Send contest announcement email for "${contest.title}" to all registered users now?`)) return;

    setBusyContestId(contest.id);
    setError('');
    try {
      await contestService.broadcastAnnouncement(contest.id);
      setSuccess(`Announcement broadcast queued for "${contest.title}". Emails are being sent to all registered users.`);
      fetchContests();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to trigger contest announcement broadcast.');
    } finally {
      setBusyContestId(null);
    }
  };

  // ---------------- Row actions ----------------

  const handlePublishToggle = async (contest) => {
    const nextStatus = contest.status === 'DRAFT' ? 'PUBLISHED' : 'DRAFT';
    const verb = nextStatus === 'PUBLISHED' ? 'publish' : 'unpublish (move back to Draft)';
    if (!window.confirm(`Are you sure you want to ${verb} "${contest.title}"?`)) return;

    setBusyContestId(contest.id);
    setError('');
    try {
      const payload = {
        title: contest.title,
        organizationName: contest.organizationName,
        description: contest.description || '',
        startTime: toDatetimeLocalValue(contest.startTime),
        endTime: toDatetimeLocalValue(contest.endTime),
        status: nextStatus,
        problems: (contest.problems || []).map((p) => ({
          problemId: p.problemId,
          displayOrder: p.displayOrder,
          points: p.points,
        })),
      };
      const res = await contestService.updateContest(contest.id, payload);
      const msg = nextStatus === 'PUBLISHED'
        ? `Contest published successfully. Contest announcement emails queued for registered users.`
        : `Contest moved back to draft.`;
      setSuccess(msg);
      fetchContests();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update contest status.');
    } finally {
      setBusyContestId(null);
    }
  };

  const handleDelete = async (contest) => {
    if (!window.confirm(`Delete "${contest.title}"? This cannot be undone.`)) return;
    setBusyContestId(contest.id);
    setError('');
    try {
      await contestService.deleteContest(contest.id);
      setSuccess('Contest deleted.');
      fetchContests();
    } catch (err) {
      // Surfaces the backend's real "safe delete" message (e.g. blocked because the
      // contest already has registered participants) rather than a generic failure.
      setError(err.response?.data?.message || 'Failed to delete contest.');
    } finally {
      setBusyContestId(null);
    }
  };

  const formatDateTime = (value) => {
    return formatContestDateTime(value);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Trophy size={26} color="#fbbf24" /> Contest Management
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>Create and manage coding contests for participants.</p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary btn-sm">
          <PlusCircle size={16} />
          <span>Create Contest</span>
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div className="alert alert-success">
          <CheckCircle size={18} />
          <span>{success}</span>
        </div>
      )}

      <div className="card" style={{ marginBottom: '1rem', padding: '0.75rem 1rem' }}>
        <div style={{ position: 'relative', maxWidth: '360px' }}>
          <Search size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-subtle)' }} />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search by contest or organization name..."
            className="form-control"
            style={{ paddingLeft: '2.25rem' }}
          />
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading contests...</div>
        ) : filteredContests.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Contest</th>
                  <th>Organization</th>
                  <th>Start</th>
                  <th>End</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'center' }}>Problems</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredContests.map((contest) => {
                  const statusStyle = STATUS_STYLES[contest.status] || STATUS_STYLES.DRAFT;
                  const rowBusy = busyContestId === contest.id;
                  return (
                    <tr key={contest.id}>
                      <td style={{ fontWeight: 600 }}>{contest.title}</td>
                      <td style={{ color: 'var(--text-muted)' }}>{contest.organizationName}</td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{formatDateTime(contest.startTime)}</td>
                      <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{formatDateTime(contest.endTime)}</td>
                      <td>
                        <span style={{
                          fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.6rem',
                          borderRadius: '9999px', color: statusStyle.color, backgroundColor: statusStyle.bg,
                        }}>
                          {statusStyle.label}
                        </span>
                      </td>
                      <td style={{ textAlign: 'center' }}>{contest.problems?.length || 0}</td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.4rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          <button onClick={() => setViewContest(contest)} className="btn btn-outline btn-sm" title="View details">
                            <Eye size={14} />
                          </button>
                          <button onClick={() => openEditModal(contest)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Edit">
                            <Edit size={14} />
                          </button>
                          {contest.status !== 'ENDED' && contest.status !== 'ONGOING' && (
                            <button
                              onClick={() => handlePublishToggle(contest)}
                              className="btn btn-outline btn-sm"
                              disabled={rowBusy}
                              title={contest.status === 'DRAFT' ? 'Publish' : 'Move back to Draft'}
                            >
                              <CheckCircle size={14} />
                              <span>{contest.status === 'DRAFT' ? 'Publish' : 'Unpublish'}</span>
                            </button>
                          )}
                          <button
                            onClick={() => handleBroadcastAnnouncement(contest)}
                            className="btn btn-outline btn-sm"
                            disabled={rowBusy}
                            title="Send contest announcement email to all registered users"
                            style={{ color: '#7c3aed', borderColor: '#c4b5fd' }}
                          >
                            <Send size={14} />
                            <span>Notify Users</span>
                          </button>
                          <button onClick={() => handleDelete(contest)} className="btn btn-danger btn-sm" disabled={rowBusy} title="Delete">
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <Trophy size={40} />
            <p>{searchTerm ? 'No contests match your search.' : 'No contests created yet.'}</p>
            {!searchTerm && (
              <button onClick={openCreateModal} className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
                Create Your First Contest
              </button>
            )}
          </div>
        )}
      </div>

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '760px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingContestId ? 'Edit Contest' : 'Create New Contest'}
              </h2>
              <button onClick={closeModal} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {formErrors.length > 0 && (
              <div className="alert alert-error" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                {formErrors.map((msg, i) => (
                  <span key={i}>{msg}</span>
                ))}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Contest Name *</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder="e.g. Weekly Contest #1"
                />
              </div>

              <div className="form-group">
                <label>Organization Name *</label>
                <input
                  type="text"
                  className="form-control"
                  value={formData.organizationName}
                  onChange={(e) => setFormData({ ...formData, organizationName: e.target.value })}
                  placeholder="e.g. CodeNova University"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows={3}
                  className="form-control"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Briefly describe this contest..."
                />
              </div>

              <div className="grid-cols-2">
                <div className="form-group">
                  <label>Start Date & Time *</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={formData.startTime}
                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>End Date & Time *</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={formData.endTime}
                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <List size={16} /> Contest Problems
                </label>

                {formData.problems.length > 0 && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '0.75rem' }}>
                    {formData.problems
                      .slice()
                      .sort((a, b) => a.displayOrder - b.displayOrder)
                      .map((p, idx) => (
                        <div key={p.problemId} style={{
                          display: 'flex', alignItems: 'center', gap: '0.6rem',
                          padding: '0.5rem 0.75rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)',
                        }}>
                          <span style={{ fontWeight: 700, color: 'var(--text-muted)', width: '1.5rem' }}>{idx + 1}.</span>
                          <span style={{ flex: 1, fontWeight: 600 }}>{p.title}</span>
                          <span className={`badge badge-${(p.difficulty || '').toLowerCase()}`}>{p.difficulty}</span>
                          <input
                            type="number"
                            min="0"
                            value={p.points}
                            onChange={(e) => updateProblemPoints(p.problemId, parseInt(e.target.value) || 0)}
                            className="form-control"
                            style={{ width: '90px' }}
                            title="Points"
                          />
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>pts</span>
                          <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <button type="button" onClick={() => moveProblem(idx, -1)} disabled={idx === 0}
                              style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: idx === 0 ? 'default' : 'pointer', padding: 0 }}>
                              <ArrowUp size={14} />
                            </button>
                            <button type="button" onClick={() => moveProblem(idx, 1)} disabled={idx === formData.problems.length - 1}
                              style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: idx === formData.problems.length - 1 ? 'default' : 'pointer', padding: 0 }}>
                              <ArrowDown size={14} />
                            </button>
                          </div>
                          <button type="button" onClick={() => toggleProblemSelected({ id: p.problemId })} className="btn btn-danger btn-sm">
                            <Trash2 size={13} />
                          </button>
                        </div>
                      ))}
                  </div>
                )}

                <div style={{
                  maxHeight: '220px', overflowY: 'auto', border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-md)', padding: '0.5rem',
                }}>
                  {allProblems.length === 0 ? (
                    <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', padding: '0.5rem' }}>Loading problems...</p>
                  ) : (
                    allProblems.map((problem) => {
                      const selected = formData.problems.some((p) => p.problemId === problem.id);
                      return (
                        <label
                          key={problem.id}
                          style={{
                            display: 'flex', alignItems: 'center', gap: '0.6rem', padding: '0.4rem 0.5rem',
                            borderRadius: 'var(--radius-sm)', cursor: 'pointer',
                            backgroundColor: selected ? 'var(--primary-soft)' : 'transparent',
                          }}
                        >
                          <input type="checkbox" checked={selected} onChange={() => toggleProblemSelected(problem)} />
                          <span style={{ flex: 1 }}>
                            {problem.title}
                            {problem.topic && (
                              <span style={{ color: 'var(--text-subtle)', fontSize: '0.78rem', marginLeft: '0.5rem' }}>
                                {problem.topic}
                              </span>
                            )}
                          </span>
                          <span className={`badge badge-${problem.difficulty?.toLowerCase()}`}>{problem.difficulty}</span>
                        </label>
                      );
                    })
                  )}
                </div>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '0.4rem' }}>
                  Problems come from the existing problem bank - select any number and set their point value.
                </p>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem', flexWrap: 'wrap' }}>
                <button type="button" onClick={closeModal} className="btn btn-outline btn-sm" disabled={saving}>
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={() => handleSubmitWithStatus('DRAFT')}
                  className="btn btn-outline btn-sm"
                  disabled={saving}
                  title="Save contest as draft without notifying users"
                >
                  <Save size={14} />
                  <span>Save as Draft</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleSubmitWithStatus('PUBLISHED')}
                  className="btn btn-primary btn-sm"
                  disabled={saving}
                  title="Publish contest immediately and send announcement emails to all users"
                >
                  <Send size={14} />
                  <span>{saving ? 'Publishing & Notifying...' : 'Host / Publish Contest & Notify All Users'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View Details Modal */}
      {viewContest && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '600px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.3rem', fontWeight: 700 }}>{viewContest.title}</h2>
              <button onClick={() => setViewContest(null)} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.9rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                <span>{viewContest.organizationName}</span>
              </div>
              {viewContest.description && <p>{viewContest.description}</p>}
              <div className="grid-cols-2" style={{ gap: '0.75rem' }}>
                <div>
                  <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Start</div>
                  <div>{formatDateTime(viewContest.startTime)}</div>
                </div>
                <div>
                  <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>End</div>
                  <div>{formatDateTime(viewContest.endTime)}</div>
                </div>
              </div>
              <div>
                <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem', marginBottom: '0.3rem' }}>Status</div>
                <span style={{
                  fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.6rem', borderRadius: '9999px',
                  color: (STATUS_STYLES[viewContest.status] || STATUS_STYLES.DRAFT).color,
                  backgroundColor: (STATUS_STYLES[viewContest.status] || STATUS_STYLES.DRAFT).bg,
                }}>
                  {(STATUS_STYLES[viewContest.status] || STATUS_STYLES.DRAFT).label}
                </span>
              </div>

              <div>
                <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                  <List size={13} /> Problems ({viewContest.problems?.length || 0})
                </div>
                {viewContest.problems && viewContest.problems.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                    {viewContest.problems
                      .slice()
                      .sort((a, b) => a.displayOrder - b.displayOrder)
                      .map((p) => (
                        <div key={p.problemId} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <span style={{ color: 'var(--text-muted)' }}>{p.displayOrder}.</span>
                          <span style={{ flex: 1, fontWeight: 600 }}>{p.problemTitle}</span>
                          <span className={`badge badge-${(p.difficulty || '').toLowerCase()}`}>{p.difficulty}</span>
                          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{p.points} pts</span>
                        </div>
                      ))}
                  </div>
                ) : (
                  <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem' }}>No problems added yet.</p>
                )}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-subtle)', fontSize: '0.8rem', borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
                <Users size={14} />
                <span>Registered participant counts will be shown here once available from the backend.</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ContestManagement;
