import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import problemService from '../../services/problemService';
import {
  BookOpen,
  Lightbulb,
  Edit,
  Trash2,
  PlusCircle,
  X,
  Save,
  AlertCircle,
  CheckCircle,
  Lock,
  Unlock,
} from 'lucide-react';

/**
 * Admin > Hints & Editorials
 *
 * Lists every problem together with its hint count and editorial status, and
 * lets the admin manage hints inline (add / edit / delete / reorder-by-order)
 * and jump straight into the existing Problem Management "Editorial Solution"
 * tab to edit the editorial itself, instead of duplicating that editor here.
 */
const AdminEditorials = () => {
  const navigate = useNavigate();
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [hintModalProblem, setHintModalProblem] = useState(null);
  const [hints, setHints] = useState([]);
  const [hintsLoading, setHintsLoading] = useState(false);
  const [editingHintId, setEditingHintId] = useState(null);
  const [hintForm, setHintForm] = useState({ hintOrder: 1, content: '', unlockAfterAttempts: 1 });
  const [savingHint, setSavingHint] = useState(false);

  const fetchProblems = async () => {
    try {
      setLoading(true);
      const data = await problemService.getAllProblems();
      setProblems(data);
    } catch (err) {
      setError('Failed to load problems.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProblems();
  }, []);

  const openHintManager = async (problem) => {
    setHintModalProblem(problem);
    setEditingHintId(null);
    setHintForm({ hintOrder: (problem.hints?.length || 0) + 1, content: '', unlockAfterAttempts: (problem.hints?.length || 0) + 1 });
    setError('');
    setHintsLoading(true);
    try {
      const data = await problemService.getHints(problem.id);
      setHints(data);
    } catch (err) {
      setHints(problem.hints || []);
    } finally {
      setHintsLoading(false);
    }
  };

  const closeHintManager = () => {
    setHintModalProblem(null);
    setHints([]);
    setEditingHintId(null);
  };

  const startEditHint = (hint) => {
    setEditingHintId(hint.id);
    setHintForm({
      hintOrder: hint.hintOrder,
      content: hint.content,
      unlockAfterAttempts: hint.unlockAfterAttempts,
    });
  };

  const startNewHint = () => {
    setEditingHintId(null);
    setHintForm({
      hintOrder: hints.length + 1,
      content: '',
      unlockAfterAttempts: hints.length + 1,
    });
  };

  const saveHint = async (e) => {
    e.preventDefault();
    if (!hintForm.content.trim()) {
      setError('Hint content cannot be empty.');
      return;
    }
    setSavingHint(true);
    setError('');
    try {
      if (editingHintId) {
        await problemService.updateHint(editingHintId, hintForm);
        setSuccess('Hint updated.');
      } else {
        await problemService.createHint(hintModalProblem.id, hintForm);
        setSuccess('Hint added.');
      }
      const data = await problemService.getHints(hintModalProblem.id);
      setHints(data);
      startNewHint();
      fetchProblems();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save hint.');
    } finally {
      setSavingHint(false);
    }
  };

  const deleteHint = async (hintId) => {
    if (!window.confirm('Delete this hint?')) return;
    try {
      await problemService.deleteHint(hintId);
      setHints(hints.filter((h) => h.id !== hintId));
      fetchProblems();
      setSuccess('Hint deleted.');
    } catch (err) {
      setError('Failed to delete hint.');
    }
  };

  const goEditEditorial = (problemId) => {
    navigate(`/admin/problems?edit=${problemId}&tab=editorial`);
  };

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>Hints & Editorials</h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Manage per-problem hints and editorial solutions. Hints unlock progressively after failed
          attempts; editorials unlock after the configured failed-attempt threshold or upon Accepted.
        </p>
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

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading problems...</div>
        ) : problems.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Problem</th>
                  <th>Difficulty</th>
                  <th>Hints</th>
                  <th>Editorial</th>
                  <th>Unlock After</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {problems.map((prob) => (
                  <tr key={prob.id}>
                    <td style={{ fontWeight: 600 }}>{prob.title}</td>
                    <td>
                      <span className={`badge badge-${prob.difficulty?.toLowerCase()}`}>{prob.difficulty}</span>
                    </td>
                    <td>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', color: (prob.hints?.length || 0) > 0 ? '#fbbf24' : 'var(--text-subtle)' }}>
                        <Lightbulb size={14} />
                        {prob.hints?.length || 0} hint{(prob.hints?.length || 0) === 1 ? '' : 's'}
                      </span>
                    </td>
                    <td>
                      {prob.hasEditorial ? (
                        <span style={{ color: '#4ade80', fontSize: '0.85rem', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                          <BookOpen size={14} />
                          <span>Published</span>
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                          <Lock size={13} />
                          Not written
                        </span>
                      )}
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      {prob.editorialUnlockAttempts ?? 3} failed attempt{(prob.editorialUnlockAttempts ?? 3) === 1 ? '' : 's'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button onClick={() => openHintManager(prob)} className="btn btn-outline btn-sm">
                          <Lightbulb size={14} />
                          <span>Manage Hints</span>
                        </button>
                        <button onClick={() => goEditEditorial(prob.id)} className="btn btn-primary btn-sm">
                          <BookOpen size={14} />
                          <span>{prob.hasEditorial ? 'Edit Editorial' : 'Write Editorial'}</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <BookOpen size={40} />
            <p>No problems in database yet. Create a problem in Problem Management first.</p>
          </div>
        )}
      </div>

      {/* Hint Manager Modal */}
      {hintModalProblem && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex',
          alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '640px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.3rem', fontWeight: 700 }}>
                Hints — {hintModalProblem.title}
              </h2>
              <button onClick={closeHintManager} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {hintsLoading ? (
              <div style={{ textAlign: 'center', padding: '1.5rem 0', color: 'var(--text-muted)' }}>Loading hints...</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.5rem' }}>
                {hints.length === 0 && (
                  <p style={{ color: 'var(--text-subtle)', fontSize: '0.9rem' }}>No hints yet for this problem.</p>
                )}
                {hints
                  .slice()
                  .sort((a, b) => a.hintOrder - b.hintOrder)
                  .map((h) => (
                    <div key={h.id} style={{
                      display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start',
                      gap: '0.75rem', padding: '0.75rem', borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--border-color)',
                    }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.25rem' }}>
                          <Unlock size={12} />
                          Hint {h.hintOrder} · unlocks after {h.unlockAfterAttempts} failed attempt{h.unlockAfterAttempts === 1 ? '' : 's'}
                        </div>
                        <div style={{ fontSize: '0.9rem' }}>{h.content}</div>
                      </div>
                      <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
                        <button onClick={() => startEditHint(h)} className="btn btn-outline btn-sm">
                          <Edit size={13} />
                        </button>
                        <button onClick={() => deleteHint(h.id)} className="btn btn-danger btn-sm">
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </div>
                  ))}
              </div>
            )}

            <form onSubmit={saveHint} style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
              <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                {editingHintId ? <Edit size={15} /> : <PlusCircle size={15} />}
                {editingHintId ? 'Edit Hint' : 'Add New Hint'}
              </h3>

              <div className="grid-cols-2">
                <div className="form-group">
                  <label>Hint Order</label>
                  <input
                    type="number"
                    min="1"
                    value={hintForm.hintOrder}
                    onChange={(e) => setHintForm({ ...hintForm, hintOrder: parseInt(e.target.value) || 1 })}
                    className="form-control"
                  />
                </div>
                <div className="form-group">
                  <label>Unlock After Failed Attempts</label>
                  <input
                    type="number"
                    min="1"
                    value={hintForm.unlockAfterAttempts}
                    onChange={(e) => setHintForm({ ...hintForm, unlockAfterAttempts: parseInt(e.target.value) || 1 })}
                    className="form-control"
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Hint Content</label>
                <textarea
                  rows={3}
                  value={hintForm.content}
                  onChange={(e) => setHintForm({ ...hintForm, content: e.target.value })}
                  className="form-control"
                  placeholder="e.g. Think about whether you can remember previously seen values while scanning the array."
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                {editingHintId && (
                  <button type="button" onClick={startNewHint} className="btn btn-outline btn-sm">
                    Cancel Edit
                  </button>
                )}
                <button type="submit" className="btn btn-primary btn-sm" disabled={savingHint}>
                  <Save size={14} />
                  <span>{savingHint ? 'Saving...' : editingHintId ? 'Update Hint' : 'Add Hint'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminEditorials;
