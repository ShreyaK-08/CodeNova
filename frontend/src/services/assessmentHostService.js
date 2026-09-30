import api from './api';

// Verified directly against AssessmentHostVerificationController.java /
// AdminAssessmentHostVerificationController.java / HostAssessmentController.java -
// every path and payload shape below matches the real backend exactly. No endpoints
// are invented here.
//
// Handled error shapes (per the backend's GlobalExceptionHandler): pending (200 with
// status "PENDING"), approved (200 with status "APPROVED", codePending true),
// rejected (200 with status "REJECTED", rejectionReason set), verified (200 with
// status "VERIFIED"), expired/invalid code (400 from verify-code, message explains
// which), and generic network/server errors (no response / 5xx) - callers should
// catch and branch on err.response?.status and err.response?.data?.message.
const assessmentHostService = {
  // ---- User-facing verification (/api/assessment-host/verification) ----

  /** Submits (or resubmits, after a rejection) the verification document and organization details. */
  submitVerification: async (payload) => {
    let formData;
    if (payload instanceof FormData) {
      formData = payload;
    } else if (payload instanceof File) {
      formData = new FormData();
      formData.append('document', payload);
    } else if (payload && typeof payload === 'object') {
      formData = new FormData();
      Object.entries(payload).forEach(([k, v]) => {
        if (v !== null && v !== undefined) {
          formData.append(k, v);
        }
      });
    } else {
      formData = new FormData();
    }
    const response = await api.post('/assessment-host/verification', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  /** Returns the caller's own verification status - one of NOT_SUBMITTED, PENDING,
   *  APPROVED (codePending true means a code is awaiting entry), REJECTED, VERIFIED. */
  getVerificationStatus: async () => {
    const response = await api.get('/assessment-host/verification');
    return response.data;
  },

  /** Submits the one-time code emailed to the user after admin approval. */
  verifyCode: async (code) => {
    const response = await api.post('/assessment-host/verification/verify-code', { code });
    return response.data;
  },

  // ---- Admin review (/api/admin/assessment-host/verifications) ----

  getAdminVerifications: async () => {
    const response = await api.get('/admin/assessment-host/verifications');
    return response.data;
  },

  getAdminVerification: async (id) => {
    const response = await api.get(`/admin/assessment-host/verifications/${id}`);
    return response.data;
  },

  /** Fetches the uploaded document as a blob and returns an object URL the admin page
   *  can preview (image) or open/download (PDF). docType: 'primary', 'gst', 'auth-letter', 'supporting'. */
  getDocumentBlobUrl: async (id, docType = 'primary') => {
    const endpoint = docType && docType !== 'primary'
      ? `/admin/assessment-host/verifications/${id}/document/${docType}`
      : `/admin/assessment-host/verifications/${id}/document`;
    const response = await api.get(endpoint, {
      responseType: 'blob',
    });
    const contentType = response.headers['content-type'] || 'application/octet-stream';
    const blob = new Blob([response.data], { type: contentType });
    return { url: URL.createObjectURL(blob), contentType };
  },

  /** Response shape: { verification, emailStatus, message }. emailStatus is one of
   *  SENT / NOT_CONFIGURED / FAILED - show `message` to the admin either way. */
  approveVerification: async (id) => {
    const response = await api.post(`/admin/assessment-host/verifications/${id}/approve`);
    return response.data;
  },

  /** Admin direct verification bypassing email code entry. */
  directVerifyVerification: async (id) => {
    const response = await api.post(`/admin/assessment-host/verifications/${id}/direct-verify`);
    return response.data;
  },

  rejectVerification: async (id, reason) => {
    const response = await api.post(`/admin/assessment-host/verifications/${id}/reject`, { reason });
    return response.data;
  },

  // ---- Hosted assessment authoring, for verified users (/api/assessments/host) ----
  // Same request/response shapes as the admin assessment authoring API
  // (assessmentService.js) - only the base path and the server-side ownership/
  // verification checks differ.

  createHostedAssessment: async (data) => {
    const response = await api.post('/assessments/host', data);
    return response.data;
  },

  getMyHostedAssessments: async () => {
    const response = await api.get('/assessments/host');
    return response.data;
  },

  getHostedAssessment: async (id) => {
    const response = await api.get(`/assessments/host/${id}`);
    return response.data;
  },

  updateHostedAssessment: async (id, data) => {
    const response = await api.put(`/assessments/host/${id}`, data);
    return response.data;
  },

  publishHostedAssessment: async (id) => {
    const response = await api.post(`/assessments/host/${id}/publish`);
    return response.data;
  },

  archiveHostedAssessment: async (id) => {
    const response = await api.post(`/assessments/host/${id}/archive`);
    return response.data;
  },

  addHostedQuestion: async (assessmentId, data) => {
    const response = await api.post(`/assessments/host/${assessmentId}/questions`, data);
    return response.data;
  },

  updateHostedQuestion: async (assessmentId, questionId, data) => {
    const response = await api.put(`/assessments/host/${assessmentId}/questions/${questionId}`, data);
    return response.data;
  },

  deleteHostedQuestion: async (assessmentId, questionId) => {
    const response = await api.delete(`/assessments/host/${assessmentId}/questions/${questionId}`);
    return response.data;
  },

  addHostedOption: async (assessmentId, questionId, data) => {
    const response = await api.post(`/assessments/host/${assessmentId}/questions/${questionId}/options`, data);
    return response.data;
  },

  updateHostedOption: async (assessmentId, questionId, optionId, data) => {
    const response = await api.put(`/assessments/host/${assessmentId}/questions/${questionId}/options/${optionId}`, data);
    return response.data;
  },

  deleteHostedOption: async (assessmentId, questionId, optionId) => {
    const response = await api.delete(`/assessments/host/${assessmentId}/questions/${questionId}/options/${optionId}`);
    return response.data;
  },

  getHostAssessmentAttempts: async (id) => {
    const response = await api.get(`/assessments/host/${id}/attempts`);
    return response.data;
  },

  getHostAssessmentFeedback: async (id) => {
    const response = await api.get(`/assessments/host/${id}/feedback`);
    return response.data;
  },

  getMyAssessmentsFeedback: async () => {
    const response = await api.get('/assessments/host/feedback');
    return response.data;
  },

  // ── Test Cases (PROGRAMMING questions) ─────────────────────────────────
  // Path: /api/assessments/host/{id}/questions/{questionId}/test-cases

  getTestCases: async (assessmentId, questionId) => {
    const response = await api.get(`/assessments/host/${assessmentId}/questions/${questionId}/test-cases`);
    return response.data;
  },

  addTestCase: async (assessmentId, questionId, data) => {
    const response = await api.post(`/assessments/host/${assessmentId}/questions/${questionId}/test-cases`, data);
    return response.data;
  },

  updateTestCase: async (assessmentId, questionId, testCaseId, data) => {
    const response = await api.put(`/assessments/host/${assessmentId}/questions/${questionId}/test-cases/${testCaseId}`, data);
    return response.data;
  },

  deleteTestCase: async (assessmentId, questionId, testCaseId) => {
    await api.delete(`/assessments/host/${assessmentId}/questions/${questionId}/test-cases/${testCaseId}`);
  },

  // ── CSV export ──────────────────────────────────────────────────────────
  exportAttemptsCsv: (assessmentId) => {
    // Triggers a direct file download (uses window.open or an <a> tag)
    window.open(`${api.defaults.baseURL}/assessments/host/${assessmentId}/attempts/export`, '_blank');
  },
};

export default assessmentHostService;

// ── Contest Host Results service ────────────────────────────────────────────
// Verified against HostContestController.java

export const contestHostService = {
  getMyContests: async () => {
    const response = await api.get('/contests/host');
    return response.data;
  },

  getContestResults: async (contestId) => {
    const response = await api.get(`/contests/host/${contestId}/results`);
    return response.data;
  },

  getParticipantResult: async (contestId, participantId) => {
    const response = await api.get(`/contests/host/${contestId}/results/${participantId}`);
    return response.data;
  },

  exportResultsCsv: (contestId) => {
    window.open(`${api.defaults.baseURL}/contests/host/${contestId}/results/export`, '_blank');
  },

  /** Returns the raw export URL for use with an anchor tag download. */
  exportResultsCsvUrl: (contestId) => {
    return `${api.defaults.baseURL}/contests/host/${contestId}/results/export`;
  },
};
