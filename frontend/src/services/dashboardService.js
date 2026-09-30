import api from './api';

export const dashboardService = {
  getStats: async () => {
    const response = await api.get('/dashboard/stats');
    return response.data;
  },

  getActivity: async () => {
    const response = await api.get('/dashboard/activity');
    return response.data;
  },

  getLanguageStats: async () => {
    const response = await api.get('/dashboard/language-stats');
    return response.data;
  },

  getMilestones: async () => {
    const response = await api.get('/dashboard/milestones');
    return response.data;
  },

  getCertificates: async () => {
    const response = await api.get('/dashboard/certificates');
    return response.data;
  },
};

export default dashboardService;
