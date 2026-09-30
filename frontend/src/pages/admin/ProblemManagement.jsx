import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router-dom';
import problemService from '../../services/problemService';
import {
  PlusCircle,
  Edit,
  Trash2,
  Code2,
  AlertCircle,
  CheckCircle,
  X,
  Save,
  BookOpen,
  FileCode2,
  Info,
} from 'lucide-react';

const ProblemManagement = () => {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [problems, setProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProblemId, setEditingProblemId] = useState(null);
  const [modalTab, setModalTab] = useState('basic'); // 'basic' | 'code' | 'editorial'

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    difficulty: 'EASY',
    topic: '',
    constraints: '',
    methodName: '',
    starterCode: '',
    starterCodePython: '',
    starterCodeCpp: '',
    starterCodeJs: '',
    editorialTitle: '',
    editorialUnlockAttempts: 3,
    editorialApproach: '',
    editorialAlgorithm: '',
    editorialComplexity: '',
    editorialSolution: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchProblems = async () => {
    try {
      setLoading(true);
      const data = await problemService.getAllProblems();
      setProblems(data);
    } catch (err) {
      setError('Failed to fetch problems.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProblems();
  }, []);

  // Deep-link support: /admin/problems?edit={id}&tab=editorial opens the edit
  // modal directly on a given tab (used by Admin > Hints & Editorials so it can
  // reuse this existing editor instead of duplicating an editorial UI).
  useEffect(() => {
    const editId = searchParams.get('edit');
    if (editId && problems.length > 0) {
      const target = problems.find((p) => String(p.id) === String(editId));
      if (target) {
        openEditModal(target);
        const tab = searchParams.get('tab');
        if (tab === 'basic' || tab === 'code' || tab === 'editorial') {
          setModalTab(tab);
        }
      }
      setSearchParams({}, { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [problems]);

  const openCreateModal = () => {
    setEditingProblemId(null);
    setModalTab('basic');
    setFormData({
      title: '',
      description: '',
      difficulty: 'EASY',
      topic: '',
      constraints: '',
      methodName: '',
      starterCode: 'public class Solution {\n    public int[] solve(int[] nums, int target) {\n        // TODO: write your solution here\n        return new int[]{-1, -1};\n    }\n}',
      starterCodePython: 'import sys\n\ndef main():\n    data = sys.stdin.read().split(\'\\n\')\n    # TODO: solve\n    print()\n\nif __name__ == "__main__":\n    main()',
      starterCodeCpp: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // TODO: read input and solve\n    return 0;\n}',
      starterCodeJs: 'const readline = require(\'readline\');\nconst rl = readline.createInterface({ input: process.stdin });\nlet lines = [];\nrl.on(\'line\', (line) => lines.push(line));\nrl.on(\'close\', () => {\n    // TODO: solve\n    console.log();\n});',
      editorialTitle: '',
      editorialUnlockAttempts: 3,
      editorialApproach: '',
      editorialAlgorithm: '',
      editorialComplexity: '',
      editorialSolution: '',
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (problem) => {
    setEditingProblemId(problem.id);
    setModalTab('basic');
    setFormData({
      title: problem.title || '',
      description: problem.description || '',
      difficulty: problem.difficulty || 'EASY',
      topic: problem.topic || '',
      constraints: problem.constraints || '',
      methodName: problem.methodName || '',
      starterCode: problem.starterCode || '',
      starterCodePython: problem.starterCodePython || '',
      starterCodeCpp: problem.starterCodeCpp || '',
      starterCodeJs: problem.starterCodeJs || '',
      editorialTitle: problem.editorialTitle || '',
      editorialUnlockAttempts: problem.editorialUnlockAttempts != null ? problem.editorialUnlockAttempts : 3,
      editorialApproach: problem.editorialApproach || '',
      editorialAlgorithm: problem.editorialAlgorithm || '',
      editorialComplexity: problem.editorialComplexity || '',
      editorialSolution: problem.editorialSolution || '',
    });
    setError('');
    setIsModalOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this problem and its test cases?')) return;
    try {
      await problemService.deleteProblem(id);
      setSuccess('Problem deleted successfully.');
      fetchProblems();
    } catch (err) {
      setError('Failed to delete problem.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      if (editingProblemId) {
        await problemService.updateProblem(editingProblemId, formData);
        setSuccess('Problem updated successfully.');
      } else {
        await problemService.createProblem(formData);
        setSuccess('Problem created successfully.');
      }
      setIsModalOpen(false);
      fetchProblems();
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving problem.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800 }}>{t('admin.problemManagement', 'Problem Management')}</h1>
          <p style={{ color: 'var(--text-muted)' }}>{t('admin.problemManagementSubtitle', 'Create, update, and manage platform algorithmic challenges, starter codes, and editorials.')}</p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary btn-sm">
          <PlusCircle size={16} />
          <span>{t('admin.createProblem', 'Add New Problem')}</span>
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

      <div className="card">
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('common.loading', 'Loading problems...')}</div>
        ) : problems.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '60px' }}>{t('admin.id', 'ID')}</th>
                  <th>{t('common.title', 'Title')}</th>
                  <th>{t('common.topic', 'Topic')}</th>
                  <th>{t('common.difficulty', 'Difficulty')}</th>
                  <th>{t('editor.editorial', 'Editorial')}</th>
                  <th style={{ textAlign: 'right' }}>{t('common.actions', 'Actions')}</th>
                </tr>
              </thead>
              <tbody>
                {problems.map((prob) => (
                  <tr key={prob.id}>
                    <td>{prob.id}</td>
                    <td style={{ fontWeight: 600 }}>{prob.title}</td>
                    <td>{prob.topic || 'General'}</td>
                    <td>
                      <span className={`badge badge-${prob.difficulty.toLowerCase()}`}>
                        {prob.difficulty}
                      </span>
                    </td>
                    <td>
                      {prob.hasEditorial ? (
                        <span style={{ color: '#4ade80', fontSize: '0.85rem', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                          <BookOpen size={14} />
                          <span>{t('assessments.published', 'Published')}</span>
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-subtle)', fontSize: '0.85rem' }}>—</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <button onClick={() => openEditModal(prob)} className="btn btn-outline btn-sm">
                          <Edit size={14} />
                          <span>{t('common.edit', 'Edit')}</span>
                        </button>
                        <button onClick={() => handleDelete(prob.id)} className="btn btn-danger btn-sm">
                          <Trash2 size={14} />
                          <span>{t('common.delete', 'Delete')}</span>
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
            <Code2 size={40} style={{ margin: '0 auto 1rem auto', opacity: 0.5 }} />
            <p>{t('problems.noProblemsFound', 'No problems in database yet.')}</p>
            <button onClick={openCreateModal} className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
              {t('admin.createProblem', 'Create First Problem')}
            </button>
          </div>
        )}
      </div>

      {/* Create / Edit Problem Modal */}
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
          <div className="card" style={{ maxWidth: '780px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingProblemId ? 'Edit Problem' : 'Create New Problem'}
              </h2>
              <button onClick={() => setIsModalOpen(false)} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {/* Modal Tabs */}
            <div style={{
              display: 'flex',
              gap: '0.5rem',
              borderBottom: '1px solid var(--border-color)',
              marginBottom: '1.5rem',
              paddingBottom: '0.5rem',
            }}>
              <button
                type="button"
                onClick={() => setModalTab('basic')}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  padding: '0.45rem 0.85rem',
                  borderRadius: 'var(--radius-md)',
                  background: modalTab === 'basic' ? 'var(--primary-soft)' : 'transparent',
                  color: modalTab === 'basic' ? 'var(--primary)' : 'var(--text-muted)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                <Info size={15} />
                <span>Basic Details</span>
              </button>

              <button
                type="button"
                onClick={() => setModalTab('code')}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  padding: '0.45rem 0.85rem',
                  borderRadius: 'var(--radius-md)',
                  background: modalTab === 'code' ? 'var(--primary-soft)' : 'transparent',
                  color: modalTab === 'code' ? 'var(--primary)' : 'var(--text-muted)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                <FileCode2 size={15} />
                <span>Starter Codes</span>
              </button>

              <button
                type="button"
                onClick={() => setModalTab('editorial')}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.4rem',
                  padding: '0.45rem 0.85rem',
                  borderRadius: 'var(--radius-md)',
                  background: modalTab === 'editorial' ? 'var(--primary-soft)' : 'transparent',
                  color: modalTab === 'editorial' ? 'var(--primary)' : 'var(--text-muted)',
                  fontWeight: 600,
                  fontSize: '0.85rem',
                  border: 'none',
                  cursor: 'pointer',
                }}
              >
                <BookOpen size={15} />
                <span>Editorial Solution</span>
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              {/* TAB 1: BASIC DETAILS */}
              {modalTab === 'basic' && (
                <>
                  <div className="form-group">
                    <label>Problem Title *</label>
                    <input
                      type="text"
                      required
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                      className="form-control"
                      placeholder="e.g. Valid Palindrome"
                    />
                  </div>

                  <div className="grid-cols-2">
                    <div className="form-group">
                      <label>Difficulty *</label>
                      <select
                        value={formData.difficulty}
                        onChange={(e) => setFormData({ ...formData, difficulty: e.target.value })}
                        className="form-control"
                      >
                        <option value="EASY">Easy</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HARD">Hard</option>
                      </select>
                    </div>

                    <div className="form-group">
                      <label>Topic / Tags</label>
                      <input
                        type="text"
                        value={formData.topic}
                        onChange={(e) => setFormData({ ...formData, topic: e.target.value })}
                        className="form-control"
                        placeholder="e.g. Strings, Two Pointers"
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Java Method Name *</label>
                    <input
                      type="text"
                      value={formData.methodName}
                      onChange={(e) => setFormData({ ...formData, methodName: e.target.value })}
                      className="form-control"
                      placeholder="e.g. twoSum"
                      style={{ fontFamily: 'JetBrains Mono, monospace' }}
                    />
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                      Must match the method name in the Java Starter Code. The backend generates a Main.java driver that invokes Solution.{formData.methodName || '<methodName>'}(...).
                    </span>
                  </div>

                  <div className="form-group">
                    <label>Problem Description *</label>
                    <textarea
                      required
                      rows={6}
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      className="form-control"
                      placeholder="Provide detailed description, constraints, and examples..."
                    />
                  </div>

                  <div className="form-group">
                    <label>Constraints</label>
                    <textarea
                      rows={4}
                      value={formData.constraints}
                      onChange={(e) => setFormData({ ...formData, constraints: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\nEach input has exactly one valid solution.'}
                    />
                  </div>
                </>
              )}

              {/* TAB 2: STARTER CODES */}
              {modalTab === 'code' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  <div className="form-group">
                    <label>Java Starter Code (Solution class)</label>
                    <textarea
                      rows={6}
                      value={formData.starterCode}
                      onChange={(e) => setFormData({ ...formData, starterCode: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'public class Solution {\n    public int[] solve(int[] nums, int target) {\n        return new int[]{-1, -1};\n    }\n}'}
                    />
                  </div>

                  <div className="form-group">
                    <label>Python Starter Code</label>
                    <textarea
                      rows={5}
                      value={formData.starterCodePython}
                      onChange={(e) => setFormData({ ...formData, starterCodePython: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'import sys\n\ndef main():\n    # code here\n    pass\n\nif __name__ == "__main__":\n    main()'}
                    />
                  </div>

                  <div className="form-group">
                    <label>C++ Starter Code</label>
                    <textarea
                      rows={5}
                      value={formData.starterCodeCpp}
                      onChange={(e) => setFormData({ ...formData, starterCodeCpp: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // code here\n    return 0;\n}'}
                    />
                  </div>

                  <div className="form-group">
                    <label>JavaScript Starter Code</label>
                    <textarea
                      rows={5}
                      value={formData.starterCodeJs}
                      onChange={(e) => setFormData({ ...formData, starterCodeJs: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'const readline = require("readline");\n// code here'}
                    />
                  </div>
                </div>
              )}

              {/* TAB 3: EDITORIAL SOLUTION */}
              {modalTab === 'editorial' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  <div className="grid-cols-2">
                    <div className="form-group">
                      <label>Editorial Title</label>
                      <input
                        type="text"
                        value={formData.editorialTitle}
                        onChange={(e) => setFormData({ ...formData, editorialTitle: e.target.value })}
                        className="form-control"
                        placeholder="e.g. Two-Pass Hash Table vs One-Pass"
                      />
                    </div>

                    <div className="form-group">
                      <label>Unlock After Failed Attempts</label>
                      <input
                        type="number"
                        min="1"
                        max="20"
                        value={formData.editorialUnlockAttempts}
                        onChange={(e) => setFormData({ ...formData, editorialUnlockAttempts: parseInt(e.target.value) || 3 })}
                        className="form-control"
                        placeholder="Default is 3"
                      />
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                        Problem solvers unlock the editorial immediately upon Accepted, or after this many failed attempts.
                      </span>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Approach &amp; Intuition (Markdown supported)</label>
                    <textarea
                      rows={4}
                      value={formData.editorialApproach}
                      onChange={(e) => setFormData({ ...formData, editorialApproach: e.target.value })}
                      className="form-control"
                      placeholder="Explain high-level intuition, key observation, and strategy..."
                    />
                  </div>

                  <div className="form-group">
                    <label>Algorithm Steps (Markdown supported)</label>
                    <textarea
                      rows={4}
                      value={formData.editorialAlgorithm}
                      onChange={(e) => setFormData({ ...formData, editorialAlgorithm: e.target.value })}
                      className="form-control"
                      placeholder="Step 1: Initialize hash map...\nStep 2: Iterate through nums..."
                    />
                  </div>

                  <div className="form-group">
                    <label>Complexity Analysis (Markdown supported)</label>
                    <textarea
                      rows={3}
                      value={formData.editorialComplexity}
                      onChange={(e) => setFormData({ ...formData, editorialComplexity: e.target.value })}
                      className="form-control"
                      placeholder="Time: O(N) because...\nSpace: O(N) for map storage..."
                    />
                  </div>

                  <div className="form-group">
                    <label>Reference Solution Code</label>
                    <textarea
                      rows={6}
                      value={formData.editorialSolution}
                      onChange={(e) => setFormData({ ...formData, editorialSolution: e.target.value })}
                      className="form-control"
                      style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.85rem' }}
                      placeholder={'public class Solution {\n    // complete reference code\n}'}
                    />
                  </div>
                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid var(--border-color)' }}>
                <button type="button" onClick={() => setIsModalOpen(false)} className="btn btn-outline">
                  {t('common.cancel', 'Cancel')}
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  <Save size={16} />
                  <span>{saving ? t('common.saving', 'Saving...') : t('common.save', 'Save Problem')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProblemManagement;
