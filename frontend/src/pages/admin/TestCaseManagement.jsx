import React, { useState, useEffect } from 'react';
import problemService from '../../services/problemService';
import testCaseService from '../../services/testCaseService';
import {
  PlusCircle,
  Edit,
  Trash2,
  Database,
  AlertCircle,
  CheckCircle,
  X,
  Save,
  Eye,
  EyeOff,
  Clock,
  HardDrive,
} from 'lucide-react';

const TestCaseManagement = () => {
  const [problems, setProblems] = useState([]);
  const [selectedProblemId, setSelectedProblemId] = useState('');
  const [testCases, setTestCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTestCaseId, setEditingTestCaseId] = useState(null);
  const [formData, setFormData] = useState({
    input: '',
    expectedOutput: '',
    isHidden: false,
    timeLimitMs: '',
    memoryLimitMb: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchProblems = async () => {
      try {
        const data = await problemService.getAllProblems();
        setProblems(data);
        if (data.length > 0) {
          setSelectedProblemId(data[0].id);
        }
      } catch (err) {
        setError('Failed to load problem list.');
      }
    };

    fetchProblems();
  }, []);

  const fetchTestCases = async (problemId) => {
    if (!problemId) return;
    try {
      setLoading(true);
      const data = await testCaseService.getTestCases(problemId);
      setTestCases(data);
    } catch (err) {
      setError('Failed to fetch test cases.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedProblemId) {
      fetchTestCases(selectedProblemId);
    }
  }, [selectedProblemId]);

  const openCreateModal = () => {
    setEditingTestCaseId(null);
    setFormData({
      input: '',
      expectedOutput: '',
      isHidden: false,
      timeLimitMs: '',
      memoryLimitMb: '',
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (tc) => {
    setEditingTestCaseId(tc.id);
    setFormData({
      input: tc.input || '',
      expectedOutput: tc.expectedOutput || '',
      isHidden: !!tc.isHidden,
      timeLimitMs: tc.timeLimitMs != null ? tc.timeLimitMs : '',
      memoryLimitMb: tc.memoryLimitMb != null ? tc.memoryLimitMb : '',
    });
    setError('');
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this testcase?')) return;
    try {
      await testCaseService.deleteTestCase(id);
      setSuccess('Test case deleted successfully.');
      fetchTestCases(selectedProblemId);
    } catch (err) {
      setError('Failed to delete test case.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    const payload = {
      ...formData,
      timeLimitMs: formData.timeLimitMs !== '' ? parseInt(formData.timeLimitMs, 10) : null,
      memoryLimitMb: formData.memoryLimitMb !== '' ? parseInt(formData.memoryLimitMb, 10) : null,
    };

    try {
      if (editingTestCaseId) {
        await testCaseService.updateTestCase(editingTestCaseId, payload);
        setSuccess('Test case updated successfully.');
      } else {
        await testCaseService.createTestCase(selectedProblemId, payload);
        setSuccess('Test case created and linked to the problem.');
      }
      setIsModalOpen(false);
      fetchTestCases(selectedProblemId);
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving test case.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>Test Case Management</h1>
          <p style={{ color: 'var(--text-muted)' }}>Manage public samples, private evaluation test cases, and per-testcase execution limits.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="btn btn-primary btn-sm"
          disabled={!selectedProblemId}
        >
          <PlusCircle size={16} />
          <span>Add Test Case</span>
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

      {/* Select Problem Selector */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <label style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-muted)' }}>Select Problem:</label>
          <select
            value={selectedProblemId}
            onChange={(e) => setSelectedProblemId(e.target.value)}
            className="form-control"
            style={{ maxWidth: '400px' }}
          >
            {problems.map((p) => (
              <option key={p.id} value={p.id}>
                #{p.id} - {p.title} ({p.difficulty})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Test Cases Table */}
      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading test cases...</div>
        ) : testCases.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '50px' }}>#</th>
                  <th style={{ width: '70px' }}>ID</th>
                  <th>Input</th>
                  <th>Expected Output</th>
                  <th>Limits Override</th>
                  <th>Visibility</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {testCases.map((tc, idx) => (
                  <tr key={tc.id}>
                    <td style={{ color: 'var(--text-subtle)', fontWeight: 600 }}>
                      #{tc.testCaseNumber || (idx + 1)}
                    </td>
                    <td>{tc.id}</td>
                    <td>
                      <code style={{ background: 'var(--bg-main)', padding: '2px 6px', borderRadius: '4px', maxWidth: '280px', display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {tc.input}
                      </code>
                    </td>
                    <td>
                      <code style={{ background: 'var(--bg-main)', padding: '2px 6px', borderRadius: '4px', maxWidth: '200px', display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {tc.expectedOutput}
                      </code>
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', fontSize: '0.8rem' }}>
                        <span style={{ color: tc.timeLimitMs ? '#fbbf24' : 'var(--text-subtle)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Clock size={12} />
                          {tc.timeLimitMs ? `${tc.timeLimitMs}ms` : 'Default (2000ms)'}
                        </span>
                        <span style={{ color: tc.memoryLimitMb ? '#fbbf24' : 'var(--text-subtle)', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <HardDrive size={12} />
                          {tc.memoryLimitMb ? `${tc.memoryLimitMb}MB` : 'Default (256MB)'}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.85rem' }}>
                        {tc.isHidden ? (
                          <>
                            <EyeOff size={14} color="#f59e0b" />
                            <span style={{ color: '#fbbf24' }}>Hidden</span>
                          </>
                        ) : (
                          <>
                            <Eye size={14} color="#10b981" />
                            <span style={{ color: '#34d399' }}>Public Sample</span>
                          </>
                        )}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button onClick={() => openEditModal(tc)} className="btn btn-outline btn-sm">
                          <Edit size={14} />
                          <span>Edit</span>
                        </button>
                        <button onClick={() => handleDelete(tc.id)} className="btn btn-danger btn-sm">
                          <Trash2 size={14} />
                          <span>Delete</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>
            <Database size={40} style={{ margin: '0 auto 1rem auto', opacity: 0.5 }} />
            <p>No test cases attached to this problem yet.</p>
            <button onClick={openCreateModal} className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
              Add First Test Case
            </button>
          </div>
        )}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '600px', width: '100%', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingTestCaseId ? 'Edit Test Case' : 'Add Test Case'}
              </h2>
              <button onClick={() => setIsModalOpen(false)} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Input (method arguments, comma-separated) *</label>
                <textarea
                  required
                  rows={3}
                  value={formData.input}
                  onChange={(e) => setFormData({ ...formData, input: e.target.value })}
                  className="form-control"
                  placeholder={'e.g. [2,7,11,15], 9'}
                  style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                />
                <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                  For Java: one comma-separated argument per method parameter. Arrays use [1,2,3], strings
                  can be plain text or "quoted", booleans are true/false, numbers are plain. For Python/C++/JavaScript,
                  this raw text is fed to stdin.
                </span>
              </div>

              <div className="form-group">
                <label>Expected Output *</label>
                <textarea
                  required
                  rows={3}
                  value={formData.expectedOutput}
                  onChange={(e) => setFormData({ ...formData, expectedOutput: e.target.value })}
                  className="form-control"
                  placeholder="e.g. [0,1]"
                  style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                />
                <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                  The expected output value. Whitespace-normalized comparison is applied automatically during evaluation.
                </span>
              </div>

              <div className="grid-cols-2">
                <div className="form-group">
                  <label>Time Limit Override (ms)</label>
                  <input
                    type="number"
                    min="100"
                    max="10000"
                    step="100"
                    value={formData.timeLimitMs}
                    onChange={(e) => setFormData({ ...formData, timeLimitMs: e.target.value })}
                    className="form-control"
                    placeholder="Default: 2000ms"
                  />
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                    Leave blank to use the problem or platform default.
                  </span>
                </div>

                <div className="form-group">
                  <label>Memory Limit Override (MB)</label>
                  <input
                    type="number"
                    min="32"
                    max="1024"
                    step="32"
                    value={formData.memoryLimitMb}
                    onChange={(e) => setFormData({ ...formData, memoryLimitMb: e.target.value })}
                    className="form-control"
                    placeholder="Default: 256MB"
                  />
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                    Leave blank to use the problem or platform default.
                  </span>
                </div>
              </div>

              <div className="form-group" style={{ flexDirection: 'row', alignItems: 'center', gap: '0.75rem', marginTop: '0.5rem' }}>
                <input
                  type="checkbox"
                  id="isHidden"
                  checked={formData.isHidden}
                  onChange={(e) => setFormData({ ...formData, isHidden: e.target.checked })}
                  style={{ width: '18px', height: '18px' }}
                />
                <label htmlFor="isHidden" style={{ cursor: 'pointer', fontSize: '0.9rem' }}>
                  Hidden test case (Used for private grading, inputs/outputs withheld from candidate view)
                </label>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid var(--border-color)' }}>
                <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-outline">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  <Save size={16} />
                  <span>{saving ? 'Saving...' : 'Save Test Case'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TestCaseManagement;
