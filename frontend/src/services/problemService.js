import api from './api';

export const problemService = {
  getAllProblems: async () => {
    const response = await api.get('/problems');
    return response.data;
  },

  getProblemById: async (id) => {
    const response = await api.get(`/problems/${id}`);
    return response.data;
  },

  createProblem: async (problemData) => {
    const response = await api.post('/problems', problemData);
    return response.data;
  },

  updateProblem: async (id, problemData) => {
    const response = await api.put(`/problems/${id}`, problemData);
    return response.data;
  },

  deleteProblem: async (id) => {
    const response = await api.delete(`/problems/${id}`);
    return response.data;
  },

  submitCode: async (submissionData) => {
    const response = await api.post('/submissions', submissionData);
    return response.data;
  },

  runCode: async (submissionData) => {
    const response = await api.post('/submissions/run', submissionData);
    return response.data;
  },

  getHints: async (problemId) => {
    const response = await api.get(`/problems/${problemId}/hints`);
    return response.data;
  },

  getEditorial: async (problemId) => {
    const response = await api.get(`/problems/${problemId}/editorial`);
    return response.data;
  },

  createHint: async (problemId, hintData) => {
    const response = await api.post(`/problems/${problemId}/hints`, hintData);
    return response.data;
  },

  updateHint: async (hintId, hintData) => {
    const response = await api.put(`/problems/hints/${hintId}`, hintData);
    return response.data;
  },

  deleteHint: async (hintId) => {
    const response = await api.delete(`/problems/hints/${hintId}`);
    return response.data;
  },

  // Persistent "last saved code" per problem+language (Task 1 backend, Task 2 frontend).
  // Returns null when nothing has been saved yet (backend responds 204 No Content).
  getSavedCode: async (problemId, language) => {
    const response = await api.get(`/problems/${problemId}/saved-code`, { params: { language } });
    return response.status === 204 ? null : response.data;
  },

  saveCode: async (problemId, language, code) => {
    const response = await api.put(`/problems/${problemId}/saved-code`, { language, code });
    return response.data;
  },
};

export default problemService;
