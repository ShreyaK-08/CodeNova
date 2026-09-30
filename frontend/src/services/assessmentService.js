import api from './api';

// Verified directly against AdminAssessmentController.java / AssessmentController.java
// Every path and payload shape below matches the real backend exactly.
const assessmentService = {
  // ---- Admin authoring (/api/admin/assessments) ----
  getAssessments: async () => {
    const response = await api.get('/admin/assessments');
    return response.data;
  },

  getAssessment: async (id) => {
    const response = await api.get(`/admin/assessments/${id}`);
    return response.data;
  },

  createAssessment: async (data) => {
    const response = await api.post('/admin/assessments', data);
    return response.data;
  },

  updateAssessment: async (id, data) => {
    const response = await api.put(`/admin/assessments/${id}`, data);
    return response.data;
  },

  publishAssessment: async (id) => {
    const response = await api.post(`/admin/assessments/${id}/publish`);
    return response.data;
  },

  archiveAssessment: async (id) => {
    const response = await api.post(`/admin/assessments/${id}/archive`);
    return response.data;
  },

  addQuestion: async (assessmentId, data) => {
    const response = await api.post(`/admin/assessments/${assessmentId}/questions`, data);
    return response.data;
  },

  updateQuestion: async (assessmentId, questionId, data) => {
    const response = await api.put(`/admin/assessments/${assessmentId}/questions/${questionId}`, data);
    return response.data;
  },

  deleteQuestion: async (assessmentId, questionId) => {
    await api.delete(`/admin/assessments/${assessmentId}/questions/${questionId}`);
  },

  addOption: async (assessmentId, questionId, data) => {
    const response = await api.post(`/admin/assessments/${assessmentId}/questions/${questionId}/options`, data);
    return response.data;
  },

  updateOption: async (assessmentId, questionId, optionId, data) => {
    const response = await api.put(`/admin/assessments/${assessmentId}/questions/${questionId}/options/${optionId}`, data);
    return response.data;
  },

  deleteOption: async (assessmentId, questionId, optionId) => {
    await api.delete(`/admin/assessments/${assessmentId}/questions/${questionId}/options/${optionId}`);
  },

  // ---- Student-facing (/api/assessments) ----

  getPublishedAssessments: async () => {
    const response = await api.get('/assessments');
    return response.data;
  },

  getPublishedAssessment: async (id) => {
    const response = await api.get(`/assessments/${id}`);
    return response.data;
  },

  startAttempt: async (assessmentId) => {
    const response = await api.post(`/assessments/${assessmentId}/attempt`);
    return response.data;
  },

  getAttempt: async (assessmentId, attemptId) => {
    const response = await api.get(`/assessments/${assessmentId}/attempt/${attemptId}`);
    return response.data;
  },

  recordAnswer: async (assessmentId, attemptId, data) => {
    const response = await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/answers`, data);
    return response.data;
  },

  /**
   * Run code against custom input or all test cases.
   * @param {Object} data - { questionId, language, code, customInput, submitForGrading }
   * When submitForGrading=false, only customInput is run.
   * When submitForGrading=true, all test cases are run and a submission is saved.
   * Hidden test case inputs/expectedOutputs are always null in the response.
   */
  runCode: async (assessmentId, attemptId, data) => {
    const response = await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/code`, {
      ...data,
      submitForGrading: false,
    });
    return response.data;
  },

  submitCode: async (assessmentId, attemptId, data) => {
    const response = await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/code`, {
      ...data,
      submitForGrading: true,
    });
    return response.data;
  },

  /**
   * Record a proctoring violation (fire-and-forget from UI).
   * @param {Object} data - { violationType, description }
   * violationType: CAMERA_DISABLED | MIC_DISABLED | FULLSCREEN_EXITED | TAB_SWITCH | COPY_PASTE | OTHER
   */
  recordViolation: async (assessmentId, attemptId, data) => {
    try {
      await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/violations`, data);
    } catch (e) {
      // Intentionally fire-and-forget; don't block the UI
      console.warn('Violation record failed:', e);
    }
  },

  /**
   * Save candidate details before the attempt questions are shown.
   * @param {Object} data - { fullName, email, phone, organization, registrationId }
   */
  saveCandidateDetails: async (assessmentId, attemptId, data) => {
    await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/candidate-details`, data);
  },

  submitAttempt: async (assessmentId, attemptId) => {
    const response = await api.post(`/assessments/${assessmentId}/attempt/${attemptId}/submit`);
    return response.data;
  },

  submitAssessmentFeedback: async (assessmentId, data) => {
    const response = await api.post(`/assessments/${assessmentId}/feedback`, data);
    return response.data;
  },

  getAttemptFeedback: async (assessmentId, attemptId) => {
    const response = await api.get(`/assessments/${assessmentId}/attempt/${attemptId}/feedback`);
    return response.data;
  },
};

export default assessmentService;
