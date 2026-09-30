import React, { useState, useEffect } from 'react';
import assessmentService from '../../services/assessmentService';
import {
  PlusCircle,
  Edit,
  Trash2,
  ClipboardList,
  AlertCircle,
  CheckCircle,
  X,
  Save,
  ListChecks,
  ArrowLeft,
  Send,
} from 'lucide-react';

const STATUS_STYLES = {
  DRAFT: { color: 'var(--text-muted)', bg: 'var(--bg-input)', label: 'Draft' },
  PUBLISHED: { color: 'var(--primary)', bg: 'var(--primary-soft)', label: 'Published' },
  ARCHIVED: { color: 'var(--text-subtle)', bg: 'var(--bg-input)', label: 'Archived' },
};

const emptyAssessmentForm = { title: '', description: '', instructions: '', durationMinutes: 30, passingMarks: 0 };

const makeEmptyOption = () => ({ id: null, optionText: '', isCorrect: false });
const emptyQuestionForm = () => ({
  id: null,
  questionText: '',
  questionType: 'MCQ',
  marks: 1,
  orderIndex: 1,
  explanation: '',
  options: [makeEmptyOption(), makeEmptyOption()],
});

/**
 * Admin Assessment Management (Task 11). Every request payload below matches
 * CreateAssessmentRequest/CreateAssessmentQuestionRequest/CreateAssessmentOptionRequest
 * field-for-field (verified against the actual backend DTOs) - notably there is NO
 * "totalMarks" input anywhere here: the backend always recomputes totalMarks itself as
 * the sum of question marks and does not accept it from a request, so this UI shows it
 * as a read-only, server-reported value instead of inventing a field the API doesn't have.
 */
const AssessmentManagement = () => {
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busyAssessmentId, setBusyAssessmentId] = useState(null);

  // Create / Edit assessment modal
  const [isAssessmentModalOpen, setIsAssessmentModalOpen] = useState(false);
  const [editingAssessmentId, setEditingAssessmentId] = useState(null);
  const [editingAssessmentTotalMarks, setEditingAssessmentTotalMarks] = useState(null);
  const [assessmentForm, setAssessmentForm] = useState(emptyAssessmentForm);
  const [assessmentFormErrors, setAssessmentFormErrors] = useState([]);
  const [savingAssessment, setSavingAssessment] = useState(false);

  // Manage Questions modal
  const [manageAssessment, setManageAssessment] = useState(null); // full admin AssessmentDto (with questions)
  const [questionsLoading, setQuestionsLoading] = useState(false);
  const [questionsError, setQuestionsError] = useState('');
  const [busyQuestionId, setBusyQuestionId] = useState(null);

  // Add/Edit Question form, nested inside the Manage Questions modal
  const [questionFormOpen, setQuestionFormOpen] = useState(false);
  const [questionForm, setQuestionForm] = useState(emptyQuestionForm());
  const [originalOptionIds, setOriginalOptionIds] = useState([]);
  const [questionFormErrors, setQuestionFormErrors] = useState([]);
  const [savingQuestion, setSavingQuestion] = useState(false);

  useEffect(() => {
    fetchAssessments();
  }, []);

  const fetchAssessments = async () => {
    try {
      setLoading(true);
      const data = await assessmentService.getAssessments();
      setAssessments(data);
    } catch (err) {
      setError('Failed to load assessments.');
    } finally {
      setLoading(false);
    }
  };

  // ================= Create / Edit assessment =================

  const openCreateAssessmentModal = () => {
    setEditingAssessmentId(null);
    setEditingAssessmentTotalMarks(null);
    setAssessmentForm(emptyAssessmentForm);
    setAssessmentFormErrors([]);
    setError('');
    setIsAssessmentModalOpen(true);
  };

  const openEditAssessmentModal = (assessment) => {
    if (assessment.hostedByOtherUser) return;
    setEditingAssessmentId(assessment.id);
    setEditingAssessmentTotalMarks(assessment.totalMarks ?? 0);
    setAssessmentForm({
      title: assessment.title || '',
      description: assessment.description || '',
      instructions: assessment.instructions || '',
      durationMinutes: assessment.durationMinutes ?? 30,
      passingMarks: assessment.passingMarks ?? 0,
    });
    setAssessmentFormErrors([]);
    setError('');
    setIsAssessmentModalOpen(true);
  };

  const closeAssessmentModal = () => {
    if (savingAssessment) return;
    setIsAssessmentModalOpen(false);
  };

  // totalMarksForEdit is null on create (no questions can exist yet, so the
  // passingMarks<=totalMarks rule is not meaningful until at least one question exists).
  const validateAssessmentForm = (form, totalMarksForEdit) => {
    const errors = [];
    if (!form.title.trim()) errors.push('Title is required.');
    if (!form.durationMinutes || Number(form.durationMinutes) <= 0) {
      errors.push('Duration must be greater than 0 minutes.');
    }
    if (form.passingMarks === '' || Number(form.passingMarks) < 0) {
      errors.push('Passing marks cannot be negative.');
    }
    if (totalMarksForEdit != null && totalMarksForEdit > 0 && Number(form.passingMarks) > totalMarksForEdit) {
      errors.push(`Passing marks cannot exceed the current total marks (${totalMarksForEdit}).`);
    }
    return errors;
  };

  const buildAssessmentPayload = (form) => ({
    title: form.title.trim(),
    description: form.description.trim(),
    instructions: form.instructions.trim(),
    durationMinutes: Number(form.durationMinutes),
    passingMarks: Number(form.passingMarks),
  });

  const handleAssessmentSubmit = async (e) => {
    e.preventDefault();
    const errors = validateAssessmentForm(assessmentForm, editingAssessmentTotalMarks);
    setAssessmentFormErrors(errors);
    if (errors.length > 0) return;

    setSavingAssessment(true);
    setError('');
    try {
      const payload = buildAssessmentPayload(assessmentForm);
      if (editingAssessmentId) {
        await assessmentService.updateAssessment(editingAssessmentId, payload);
        setSuccess('Assessment updated successfully.');
      } else {
        await assessmentService.createAssessment(payload);
        setSuccess('Assessment created successfully.');
      }
      setIsAssessmentModalOpen(false);
      fetchAssessments();
    } catch (err) {
      const backendErrors = err.response?.data?.errors;
      const message = backendErrors
        ? Object.values(backendErrors).join(' ')
        : err.response?.data?.message || 'Unable to save assessment.';
      setAssessmentFormErrors([message]);
    } finally {
      setSavingAssessment(false);
    }
  };

  // ================= Publish / Archive =================

  const handlePublish = async (assessment) => {
    if (assessment.hostedByOtherUser) return;
    if (!window.confirm(`Publish "${assessment.title}"? Students will be able to start it immediately.`)) return;
    setBusyAssessmentId(assessment.id);
    setError('');
    try {
      await assessmentService.publishAssessment(assessment.id);
      setSuccess('Assessment published successfully.');
      fetchAssessments();
    } catch (err) {
      // Surfaces the backend's real validation message, e.g. "Please add at least one
      // question before publishing." or "Question ... must have exactly one correct option."
      setError(err.response?.data?.message || 'Unable to publish assessment.');
    } finally {
      setBusyAssessmentId(null);
    }
  };

  const handleArchive = async (assessment) => {
    if (assessment.hostedByOtherUser) return;
    if (!window.confirm(`Archive "${assessment.title}"? Students will no longer be able to start new attempts.`)) return;
    setBusyAssessmentId(assessment.id);
    setError('');
    try {
      await assessmentService.archiveAssessment(assessment.id);
      setSuccess('Assessment archived.');
      fetchAssessments();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to archive assessment.');
    } finally {
      setBusyAssessmentId(null);
    }
  };

  // ================= Manage Questions =================

  const openManageQuestions = async (assessment) => {
    setError('');
    setQuestionsError('');
    setQuestionFormOpen(false);
    setManageAssessment({ ...assessment, questions: assessment.questions || [] });
    setQuestionsLoading(true);
    try {
      const full = await assessmentService.getAssessment(assessment.id);
      setManageAssessment(full);
    } catch (err) {
      setQuestionsError('Unable to load questions.');
    } finally {
      setQuestionsLoading(false);
    }
  };

  const closeManageQuestions = () => {
    if (savingQuestion) return;
    setManageAssessment(null);
    setQuestionFormOpen(false);
  };

  const refreshManageAssessment = async (assessmentId) => {
    try {
      const full = await assessmentService.getAssessment(assessmentId);
      setManageAssessment(full);
    } catch (err) {
      setQuestionsError('Unable to refresh questions.');
    }
  };

  const handleDeleteQuestion = async (question) => {
    if (manageAssessment?.hostedByOtherUser) return;
    if (!window.confirm('Delete this question?')) return;
    setBusyQuestionId(question.id);
    setQuestionsError('');
    try {
      await assessmentService.deleteQuestion(manageAssessment.id, question.id);
      setSuccess('Question deleted.');
      await refreshManageAssessment(manageAssessment.id);
      fetchAssessments(); // question count / total marks changed
    } catch (err) {
      setQuestionsError(err.response?.data?.message || 'Unable to delete question.');
    } finally {
      setBusyQuestionId(null);
    }
  };

  // ================= Add / Edit Question form =================

  const openAddQuestionForm = () => {
    if (manageAssessment?.hostedByOtherUser) return;
    const nextOrder = (manageAssessment.questions?.length || 0) + 1;
    setQuestionForm({ ...emptyQuestionForm(), orderIndex: nextOrder });
    setOriginalOptionIds([]);
    setQuestionFormErrors([]);
    setQuestionFormOpen(true);
  };

  const openEditQuestionForm = (question) => {
    if (manageAssessment?.hostedByOtherUser) return;
    const options = (question.options || []).map((o) => ({
      id: o.id,
      optionText: o.optionText || '',
      isCorrect: !!o.isCorrect,
    }));
    setQuestionForm({
      id: question.id,
      questionText: question.questionText || '',
      questionType: question.questionType || 'MCQ',
      marks: question.marks ?? 1,
      orderIndex: question.orderIndex ?? 1,
      explanation: question.explanation || '',
      options: options.length > 0 ? options : [makeEmptyOption(), makeEmptyOption()],
    });
    setOriginalOptionIds((question.options || []).map((o) => o.id));
    setQuestionFormErrors([]);
    setQuestionFormOpen(true);
  };

  const closeQuestionForm = () => {
    if (savingQuestion) return;
    setQuestionFormOpen(false);
  };

  const handleQuestionTypeChange = (type) => {
    setQuestionForm((prev) => {
      if (type === 'TRUE_FALSE') {
        return {
          ...prev,
          questionType: type,
          options: [
            { id: null, optionText: 'True', isCorrect: false },
            { id: null, optionText: 'False', isCorrect: false },
          ],
        };
      }
      return {
        ...prev,
        questionType: type,
        options: prev.options.length >= 2 ? prev.options : [makeEmptyOption(), makeEmptyOption()],
      };
    });
  };

  // Marking one option correct always unmarks every other one - exactly one correct
  // option is enforced in the UI itself, not just at save time.
  const setCorrectOption = (index) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.map((o, i) => ({ ...o, isCorrect: i === index })),
    }));
  };

  const updateOptionText = (index, text) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.map((o, i) => (i === index ? { ...o, optionText: text } : o)),
    }));
  };

  const addOptionRow = () => {
    setQuestionForm((prev) => ({ ...prev, options: [...prev.options, makeEmptyOption()] }));
  };

  const removeOptionRow = (index) => {
    setQuestionForm((prev) => ({ ...prev, options: prev.options.filter((_, i) => i !== index) }));
  };

  const validateQuestionForm = (form) => {
    const errors = [];
    if (!form.questionText.trim()) errors.push('Question text is required.');
    if (form.marks === '' || Number(form.marks) < 0) errors.push('Marks cannot be negative.');

    const filledOptions = (form.options || []).filter((o) => o.optionText.trim() !== '');
    if (filledOptions.length < 2) errors.push('At least 2 options are required.');

    const correctCount = filledOptions.filter((o) => o.isCorrect).length;
    if (correctCount === 0) errors.push('Exactly one option must be marked correct.');
    if (correctCount > 1) errors.push('Only one option can be marked correct.');

    if (form.questionType === 'TRUE_FALSE' && filledOptions.length !== 2) {
      errors.push('True/False questions must have exactly 2 options.');
    }
    return errors;
  };

  const handleQuestionSubmit = async (e) => {
    e.preventDefault();
    const errors = validateQuestionForm(questionForm);
    setQuestionFormErrors(errors);
    if (errors.length > 0) return;

    setSavingQuestion(true);
    setQuestionFormErrors([]);
    const assessmentId = manageAssessment.id;
    try {
      if (questionForm.id) {
        // EDIT: question-level fields go through updateQuestion (which never touches
        // options); options are then reconciled individually against their real IDs -
        // updates/additions/deletions - since updateQuestion's DTO has no options field.
        await assessmentService.updateQuestion(assessmentId, questionForm.id, {
          questionText: questionForm.questionText.trim(),
          questionType: questionForm.questionType,
          marks: Number(questionForm.marks),
          orderIndex: Number(questionForm.orderIndex),
          explanation: questionForm.explanation.trim() || null,
        });

        const currentIds = questionForm.options.filter((o) => o.id).map((o) => o.id);
        const removedIds = originalOptionIds.filter((id) => !currentIds.includes(id));
        for (const removedId of removedIds) {
          await assessmentService.deleteOption(assessmentId, questionForm.id, removedId);
        }
        for (const option of questionForm.options) {
          if (!option.optionText.trim()) continue;
          const payload = { optionText: option.optionText.trim(), isCorrect: !!option.isCorrect };
          if (option.id) {
            await assessmentService.updateOption(assessmentId, questionForm.id, option.id, payload);
          } else {
            await assessmentService.addOption(assessmentId, questionForm.id, payload);
          }
        }
        setSuccess('Question updated successfully.');
      } else {
        // CREATE: one call - the backend accepts options nested in the same request.
        await assessmentService.addQuestion(assessmentId, {
          questionText: questionForm.questionText.trim(),
          questionType: questionForm.questionType,
          marks: Number(questionForm.marks),
          orderIndex: Number(questionForm.orderIndex),
          explanation: questionForm.explanation.trim() || null,
          options: questionForm.options
            .filter((o) => o.optionText.trim())
            .map((o) => ({ optionText: o.optionText.trim(), isCorrect: !!o.isCorrect })),
        });
        setSuccess('Question added successfully.');
      }
      setQuestionFormOpen(false);
      await refreshManageAssessment(assessmentId);
      fetchAssessments();
    } catch (err) {
      setQuestionFormErrors([err.response?.data?.message || 'Unable to save question.']);
    } finally {
      setSavingQuestion(false);
    }
  };

  const sortedQuestions = (manageAssessment?.questions || []).slice().sort((a, b) => a.orderIndex - b.orderIndex);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ClipboardList size={26} color="#8b5cf6" /> Assessment Monitoring
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>
            Monitor assessments, question banks, and candidate attempt statistics across platform and verified hosts.
          </p>
        </div>
        <button onClick={openCreateAssessmentModal} className="btn btn-primary btn-sm">
          <PlusCircle size={16} />
          <span>Create Official Assessment</span>
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
          <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>Loading assessments...</div>
        ) : assessments.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th style={{ width: '60px' }}>ID</th>
                  <th>Title</th>
                  <th>Host & Organization</th>
                  <th style={{ textAlign: 'center' }}>Duration</th>
                  <th style={{ textAlign: 'center' }}>Marks (Pass/Total)</th>
                  <th style={{ textAlign: 'center' }}>Questions</th>
                  <th style={{ textAlign: 'center' }}>Candidates & Attempts</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {assessments.map((assessment) => {
                  const statusStyle = STATUS_STYLES[assessment.status] || STATUS_STYLES.DRAFT;
                  const rowBusy = busyAssessmentId === assessment.id;
                  return (
                    <tr key={assessment.id}>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>#{assessment.id}</td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{assessment.title}</div>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', maxWidth: '240px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {assessment.description || '-'}
                        </div>
                      </td>
                      <td>
                        {assessment.hostedByOtherUser ? (
                          <div>
                            <span style={{
                              fontSize: '0.72rem',
                              padding: '0.15rem 0.5rem',
                              borderRadius: '9999px',
                              backgroundColor: 'rgba(139, 92, 246, 0.15)',
                              color: '#7c3aed',
                              fontWeight: 600,
                              display: 'inline-block',
                              marginBottom: '0.2rem',
                            }}>
                              Hosted by @{assessment.createdByUsername || 'User'}
                            </span>
                            <div style={{ fontSize: '0.8rem', fontWeight: 600 }}>
                              {assessment.organizationName || assessment.hostName || 'Verified Organization'}
                            </div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                              {assessment.hostEmail || '-'}
                            </div>
                          </div>
                        ) : (
                          <div>
                            <span style={{
                              fontSize: '0.72rem',
                              padding: '0.15rem 0.5rem',
                              borderRadius: '9999px',
                              backgroundColor: 'rgba(59, 130, 246, 0.12)',
                              color: '#2563eb',
                              fontWeight: 600,
                            }}>
                              Platform Official
                            </span>
                            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                              CodeNova Core
                            </div>
                          </div>
                        )}
                      </td>
                      <td style={{ textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>{assessment.durationMinutes} min</td>
                      <td style={{ textAlign: 'center', fontSize: '0.85rem' }}>
                        <strong>{assessment.passingMarks}</strong> / {assessment.totalMarks}
                      </td>
                      <td style={{ textAlign: 'center' }}>{assessment.questionCount ?? 0}</td>
                      <td style={{ textAlign: 'center' }}>
                        <span style={{
                          fontSize: '0.78rem',
                          fontWeight: 600,
                          padding: '0.2rem 0.5rem',
                          borderRadius: '6px',
                          backgroundColor: 'var(--bg-input)',
                          color: 'var(--text-muted)',
                        }}>
                          {assessment.totalAttempts ?? 0} attempts ({assessment.candidateCount ?? 0} users)
                        </span>
                      </td>
                      <td>
                        <span style={{
                          fontSize: '0.75rem', fontWeight: 700, padding: '0.2rem 0.6rem',
                          borderRadius: '9999px', color: statusStyle.color, backgroundColor: statusStyle.bg,
                        }}>
                          {statusStyle.label}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.4rem', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          {assessment.hostedByOtherUser ? (
                            <button
                              onClick={() => openManageQuestions(assessment)}
                              className="btn btn-outline btn-sm"
                              disabled={rowBusy}
                              title="Monitor Assessment Details (Read-Only)"
                            >
                              <ListChecks size={14} />
                              <span>View Details</span>
                            </button>
                          ) : (
                            <>
                              <button onClick={() => openEditAssessmentModal(assessment)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Edit">
                                <Edit size={14} />
                              </button>
                              <button
                                onClick={() => openManageQuestions(assessment)}
                                className="btn btn-outline btn-sm"
                                disabled={rowBusy}
                                title="Manage Questions"
                              >
                                <ListChecks size={14} />
                                <span>Manage Questions</span>
                              </button>
                              {assessment.status === 'DRAFT' && (
                                <button onClick={() => handlePublish(assessment)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Publish">
                                  <Send size={14} />
                                  <span>Publish</span>
                                </button>
                              )}
                              {assessment.status !== 'ARCHIVED' && (
                                <button onClick={() => handleArchive(assessment)} className="btn btn-danger btn-sm" disabled={rowBusy} title="Archive">
                                  <Trash2 size={14} />
                                  <span>Archive</span>
                                </button>
                              )}
                            </>
                          )}
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
            <ClipboardList size={40} />
            <p>No assessments created yet.</p>
            <button onClick={openCreateAssessmentModal} className="btn btn-primary btn-sm" style={{ marginTop: '1rem' }}>
              Create Your First Assessment
            </button>
          </div>
        )}
      </div>

      {/* Create / Edit Assessment Modal */}
      {isAssessmentModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '620px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingAssessmentId ? 'Edit Assessment' : 'Create New Assessment'}
              </h2>
              <button onClick={closeAssessmentModal} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {assessmentFormErrors.length > 0 && (
              <div className="alert alert-error" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                {assessmentFormErrors.map((msg, i) => (
                  <span key={i}>{msg}</span>
                ))}
              </div>
            )}

            <form onSubmit={handleAssessmentSubmit}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  className="form-control"
                  value={assessmentForm.title}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, title: e.target.value })}
                  placeholder="e.g. Java & OOP Fundamentals"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  rows={2}
                  className="form-control"
                  value={assessmentForm.description}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, description: e.target.value })}
                  placeholder="Briefly describe this assessment..."
                />
              </div>

              <div className="form-group">
                <label>Instructions</label>
                <textarea
                  rows={2}
                  className="form-control"
                  value={assessmentForm.instructions}
                  onChange={(e) => setAssessmentForm({ ...assessmentForm, instructions: e.target.value })}
                  placeholder="Instructions shown to the student before/during the attempt..."
                />
              </div>

              <div className="grid-cols-2">
                <div className="form-group">
                  <label>Duration (minutes) *</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    value={assessmentForm.durationMinutes}
                    onChange={(e) => setAssessmentForm({ ...assessmentForm, durationMinutes: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Passing Marks *</label>
                  <input
                    type="number"
                    min="0"
                    className="form-control"
                    value={assessmentForm.passingMarks}
                    onChange={(e) => setAssessmentForm({ ...assessmentForm, passingMarks: e.target.value })}
                  />
                </div>
              </div>

              <p style={{ fontSize: '0.78rem', color: 'var(--text-subtle)', marginTop: '-0.5rem', marginBottom: '1rem' }}>
                Total Marks isn't set here - it's calculated automatically from each
                question's marks once you add questions under "Manage Questions".
                {editingAssessmentTotalMarks != null && (
                  <> Current total marks: <strong>{editingAssessmentTotalMarks}</strong>.</>
                )}
              </p>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
                <button type="button" onClick={closeAssessmentModal} className="btn btn-outline btn-sm" disabled={savingAssessment}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary btn-sm" disabled={savingAssessment}>
                  <Save size={14} />
                  <span>{savingAssessment ? 'Saving...' : editingAssessmentId ? 'Update Assessment' : 'Create Assessment'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Manage Questions Modal */}
      {manageAssessment && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', zIndex: 1000, padding: '1rem',
        }}>
          <div className="card" style={{ maxWidth: '720px', width: '100%', maxHeight: '92vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div>
                <h2 style={{ fontSize: '1.3rem', fontWeight: 700 }}>{manageAssessment.title}</h2>
                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
                  <span>Duration: <strong>{manageAssessment.durationMinutes} min</strong></span>
                  <span>Total Marks: <strong>{manageAssessment.totalMarks}</strong></span>
                  <span>Passing Marks: <strong>{manageAssessment.passingMarks}</strong></span>
                </div>
              </div>
              <button onClick={closeManageQuestions} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            {questionsError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
                <AlertCircle size={16} />
                <span>{questionsError}</span>
              </div>
            )}

            {manageAssessment.hostedByOtherUser && (
              <div className="alert alert-info" style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <AlertCircle size={16} />
                <span>
                  This assessment is hosted by <strong>@{manageAssessment.createdByUsername || 'another user'}</strong>. Questions and options are read-only for administrators.
                </span>
              </div>
            )}

            {!questionFormOpen ? (
              <>
                {!manageAssessment.hostedByOtherUser && (
                  <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
                    <button onClick={openAddQuestionForm} className="btn btn-primary btn-sm">
                      <PlusCircle size={14} />
                      <span>Add Question</span>
                    </button>
                  </div>
                )}

                {questionsLoading ? (
                  <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '1.5rem 0' }}>Loading questions...</p>
                ) : sortedQuestions.length === 0 ? (
                  <div className="empty-state">
                    <ListChecks size={32} />
                    <p>No questions added yet.</p>
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    {sortedQuestions.map((question, idx) => {
                      const rowBusy = busyQuestionId === question.id;
                      return (
                        <div key={question.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '0.85rem 1rem' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem' }}>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: 700, marginBottom: '0.25rem' }}>
                                {idx + 1}. {question.questionText}
                              </div>
                              <div style={{ display: 'flex', gap: '0.6rem', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                                <span className="badge badge-easy">{question.questionType}</span>
                                <span>{question.marks} mark{question.marks === 1 ? '' : 's'}</span>
                              </div>
                            </div>
                            {!manageAssessment.hostedByOtherUser && (
                              <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
                                <button onClick={() => openEditQuestionForm(question)} className="btn btn-outline btn-sm" disabled={rowBusy} title="Edit">
                                  <Edit size={13} />
                                </button>
                                <button onClick={() => handleDeleteQuestion(question)} className="btn btn-danger btn-sm" disabled={rowBusy} title="Delete">
                                  <Trash2 size={13} />
                                </button>
                              </div>
                            )}
                          </div>

                          {question.options && question.options.length > 0 && (
                            <div style={{ marginTop: '0.6rem', display: 'flex', flexDirection: 'column', gap: '0.3rem' }}>
                              {question.options.slice().sort((a, b) => a.orderIndex - b.orderIndex).map((option) => (
                                <div key={option.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem' }}>
                                  <span style={{
                                    width: '9px', height: '9px', borderRadius: '50%', flexShrink: 0,
                                    backgroundColor: option.isCorrect ? 'var(--success)' : 'var(--border-color)',
                                  }} />
                                  <span style={{ color: option.isCorrect ? 'var(--success)' : 'var(--text-muted)', fontWeight: option.isCorrect ? 700 : 400 }}>
                                    {option.optionText}
                                  </span>
                                </div>
                              ))}
                            </div>
                          )}

                          {question.explanation && (
                            <p style={{ marginTop: '0.5rem', fontSize: '0.78rem', color: 'var(--text-subtle)', fontStyle: 'italic' }}>
                              {question.explanation}
                            </p>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </>
            ) : (
              // ---------- Add / Edit Question form ----------
              <form onSubmit={handleQuestionSubmit}>
                <button
                  type="button"
                  onClick={closeQuestionForm}
                  style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '0.85rem', marginBottom: '0.75rem', padding: 0 }}
                >
                  <ArrowLeft size={14} /> Back to questions
                </button>

                <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.9rem' }}>
                  {questionForm.id ? 'Edit Question' : 'Add Question'}
                </h3>

                {questionFormErrors.length > 0 && (
                  <div className="alert alert-error" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                    {questionFormErrors.map((msg, i) => (
                      <span key={i}>{msg}</span>
                    ))}
                  </div>
                )}

                <div className="form-group">
                  <label>Question Text *</label>
                  <textarea
                    rows={2}
                    className="form-control"
                    value={questionForm.questionText}
                    onChange={(e) => setQuestionForm({ ...questionForm, questionText: e.target.value })}
                    placeholder="e.g. Which keyword is used to inherit a class in Java?"
                  />
                </div>

                <div className="grid-cols-2">
                  <div className="form-group">
                    <label>Question Type</label>
                    <select
                      className="form-control"
                      value={questionForm.questionType}
                      onChange={(e) => handleQuestionTypeChange(e.target.value)}
                    >
                      <option value="MCQ">MCQ</option>
                      <option value="TRUE_FALSE">TRUE_FALSE</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Marks</label>
                    <input
                      type="number"
                      min="0"
                      className="form-control"
                      value={questionForm.marks}
                      onChange={(e) => setQuestionForm({ ...questionForm, marks: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Order</label>
                  <input
                    type="number"
                    min="1"
                    className="form-control"
                    style={{ maxWidth: '140px' }}
                    value={questionForm.orderIndex}
                    onChange={(e) => setQuestionForm({ ...questionForm, orderIndex: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Explanation (optional, shown after submission)</label>
                  <textarea
                    rows={2}
                    className="form-control"
                    value={questionForm.explanation}
                    onChange={(e) => setQuestionForm({ ...questionForm, explanation: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Options - mark exactly one as correct *</label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {questionForm.options.map((option, idx) => (
                      <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <input
                          type="radio"
                          name="correct-option"
                          checked={!!option.isCorrect}
                          onChange={() => setCorrectOption(idx)}
                          title="Mark as correct"
                        />
                        <input
                          type="text"
                          className="form-control"
                          value={option.optionText}
                          onChange={(e) => updateOptionText(idx, e.target.value)}
                          placeholder={`Option ${idx + 1}`}
                          disabled={questionForm.questionType === 'TRUE_FALSE'}
                        />
                        {questionForm.questionType !== 'TRUE_FALSE' && (
                          <button
                            type="button"
                            onClick={() => removeOptionRow(idx)}
                            className="btn btn-danger btn-sm"
                            disabled={questionForm.options.length <= 2}
                            title="Remove option"
                          >
                            <Trash2 size={13} />
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                  {questionForm.questionType !== 'TRUE_FALSE' && (
                    <button type="button" onClick={addOptionRow} className="btn btn-outline btn-sm" style={{ marginTop: '0.6rem' }}>
                      <PlusCircle size={13} />
                      <span>Add Option</span>
                    </button>
                  )}
                </div>

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.25rem' }}>
                  <button type="button" onClick={closeQuestionForm} className="btn btn-outline btn-sm" disabled={savingQuestion}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary btn-sm" disabled={savingQuestion}>
                    <Save size={14} />
                    <span>{savingQuestion ? 'Saving...' : questionForm.id ? 'Update Question' : 'Add Question'}</span>
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default AssessmentManagement;
