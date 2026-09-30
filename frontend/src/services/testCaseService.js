import api from './api';

export const testCaseService = {
  getTestCases: async (problemId) => {
    const response = await api.get(`/problems/${problemId}/testcases`);
    return response.data;
  },

  createTestCase: async (problemId, testCaseData) => {
    const response = await api.post(`/problems/${problemId}/testcases`, testCaseData);
    return response.data;
  },

  updateTestCase: async (id, testCaseData) => {
    const response = await api.put(`/testcases/${id}`, testCaseData);
    return response.data;
  },

  deleteTestCase: async (id) => {
    const response = await api.delete(`/testcases/${id}`);
    return response.data;
  },
};

export default testCaseService;
