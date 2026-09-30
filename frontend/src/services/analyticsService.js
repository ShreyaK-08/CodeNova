import api from './api';

const analyticsService = {
  getOverview: async (range = 'ALL_TIME') => {
    const response = await api.get(`/admin/analytics/overview?range=${encodeURIComponent(range)}`);
    return response.data;
  },

  exportCsv: async (range = 'ALL_TIME') => {
    const response = await api.get(`/admin/analytics/export?range=${encodeURIComponent(range)}`, {
      responseType: 'blob'
    });
    return response.data;
  }
};

export default analyticsService;
