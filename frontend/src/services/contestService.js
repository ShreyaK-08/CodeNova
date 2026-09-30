import api from './api';

/**
 * Contest API client - covers both the admin management endpoints (Task 5,
 * AdminContestController) and the student-facing browse/registration endpoints
 * (Task 6, ContestController). Kept in one file since both wrap the same Contest
 * backend module; method names are distinct to avoid any ambiguity between the
 * admin and student calls.
 */
export const contestService = {
  // ---- Admin (Task 5) ----
  getAllContests: async () => {
    const response = await api.get('/admin/contests');
    return response.data;
  },

  getContestById: async (id) => {
    const response = await api.get(`/admin/contests/${id}`);
    return response.data;
  },

  createContest: async (contestData) => {
    const response = await api.post('/admin/contests', contestData);
    return response.data;
  },

  updateContest: async (id, contestData) => {
    const response = await api.put(`/admin/contests/${id}`, contestData);
    return response.data;
  },

  deleteContest: async (id) => {
    const response = await api.delete(`/admin/contests/${id}`);
    return response.data;
  },

  broadcastAnnouncement: async (id) => {
    const response = await api.post(`/admin/contests/${id}/broadcast`);
    return response.data;
  },

  // ---- Student-facing (Task 6) ----
  // The backend determines the current user from the authenticated JWT (via the
  // existing axios interceptor in api.js) - no user id is ever sent from the client.
  getAvailableContests: async () => {
    const response = await api.get('/contests');
    return response.data;
  },

  getContestDetails: async (id) => {
    const response = await api.get(`/contests/${id}`);
    return response.data;
  },

  registerForContest: async (id) => {
    const response = await api.post(`/contests/${id}/register`);
    return response.data;
  },

  getRegistrationStatus: async (id) => {
    const response = await api.get(`/contests/${id}/registration`);
    return response.data;
  },

  // ---- Student participation / scoring (Task 8) ----
  // Idempotent: ContestCoding.jsx calls this both to start the attempt on entry and to
  // refresh score/solved/submissionCount after a Run/Submit - it never creates a
  // second attempt.
  startOrGetAttempt: async (id) => {
    const response = await api.post(`/contests/${id}/attempt`);
    return response.data;
  },

  // Public per-contest leaderboard - scoped to this one contest only.
  getLeaderboard: async (id) => {
    const response = await api.get(`/contests/${id}/leaderboard`);
    return response.data;
  },

  // ---- Basic exam-security monitoring (Task 9) ----
  // Fullscreen-exit / tab-switch detection lives in ContestCoding.jsx; this just
  // reports one violation to the backend, which is the actual source of truth for the
  // count and for whether the attempt has been terminated.
  recordSecurityViolation: async (id, violationType = null) => {
    const payload = violationType ? { violationType } : {};
    const response = await api.post(`/contests/${id}/security-violation`, payload);
    return response.data;
  },

  // ---- Admin results (Task 8) ----
  getContestParticipants: async (id) => {
    const response = await api.get(`/admin/contests/${id}/participants`);
    return response.data;
  },
};

export default contestService;
