import api from './api';

const supportFeedbackService = {
  // ── User Support Center APIs ──────────────────────────────────────────

  // Create a new support ticket (with optional context and attachment)
  createSupportRequest: async (data) => {
    const response = await api.post('/support', data);
    return response.data;
  },

  // Get current user's tickets with optional status, category, search filters
  getMySupportRequests: async (params = {}) => {
    const response = await api.get('/support', { params });
    return response.data;
  },

  // Get user's tickets summary counts
  getUserSupportSummary: async () => {
    const response = await api.get('/support/summary');
    return response.data;
  },

  // Get ticket detail with full conversation
  getSupportTicketById: async (id) => {
    const response = await api.get(`/support/${id}`);
    return response.data;
  },

  // Send a reply message on a ticket
  addSupportMessage: async (id, data) => {
    const response = await api.post(`/support/${id}/messages`, data);
    return response.data;
  },

  // Mark ticket as resolved by user
  resolveSupportTicket: async (id) => {
    const response = await api.post(`/support/${id}/resolve`);
    return response.data;
  },

  // Reopen ticket
  reopenSupportTicket: async (id) => {
    const response = await api.post(`/support/${id}/reopen`);
    return response.data;
  },

  // Close ticket
  closeSupportTicket: async (id) => {
    const response = await api.post(`/support/${id}/close`);
    return response.data;
  },

  // Submit 1-5 star satisfaction rating and comment
  submitSupportRating: async (id, data) => {
    const response = await api.post(`/support/${id}/rating`, data);
    return response.data;
  },

  // Upload attachment file (multipart/form-data)
  uploadAttachment: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/support/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  // ── Admin Support Center APIs ─────────────────────────────────────────

  // Get all support tickets with filters (status, priority, category, assignedToId, search)
  getAdminSupportRequests: async (params = {}) => {
    const response = await api.get('/admin/support', { params });
    return response.data;
  },

  // Get admin support ticket detail (including internal notes)
  getAdminSupportTicketById: async (id) => {
    const response = await api.get(`/admin/support/${id}`);
    return response.data;
  },

  // Triage ticket (status, priority, category, assign staff)
  adminTriageTicket: async (id, data) => {
    const response = await api.patch(`/admin/support/${id}/triage`, data);
    return response.data;
  },

  // Legacy quick status update
  updateSupportStatus: async (id, status) => {
    const response = await api.put(`/admin/support/${id}/status`, { status });
    return response.data;
  },

  // Admin reply or internal note
  addAdminSupportMessage: async (id, data) => {
    const response = await api.post(`/admin/support/${id}/messages`, data);
    return response.data;
  },

  // AI Support Assistant draft response generator
  generateAiSupportDraft: async (id, data = {}) => {
    const response = await api.post(`/admin/support/${id}/ai-draft`, data);
    return response.data;
  },

  // List staff members for assignment
  getStaffMembers: async () => {
    const response = await api.get('/admin/support/staff');
    return response.data;
  },

  // Aggregate support analytics
  getSupportAnalytics: async () => {
    const response = await api.get('/admin/support/analytics');
    return response.data;
  },

  // ── Assessment & General Feedback APIs ───────────────────────────────

  createAssessmentQuestionFeedback: async (data) => {
    const response = await api.post('/feedback/assessment-question', data);
    return response.data;
  },

  getAdminAssessmentQuestionFeedback: async () => {
    const response = await api.get('/admin/assessment-feedback');
    return response.data;
  },

  submitAssessmentFeedback: async (data) => {
    const response = await api.post('/assessments/feedback', data);
    return response.data;
  },

  getAdminAssessmentPostFeedback: async () => {
    const response = await api.get('/admin/assessment-post-feedback');
    return response.data;
  },

  getHostAssessmentFeedback: async (assessmentId) => {
    const response = await api.get(`/assessments/host/${assessmentId}/feedback`);
    return response.data;
  },

  createGeneralFeedback: async (data) => {
    const response = await api.post('/feedback/general', data);
    return response.data;
  },

  getAdminGeneralFeedback: async () => {
    const response = await api.get('/admin/feedback');
    return response.data;
  }
};

export default supportFeedbackService;
