import api from './api';

const emailMonitoringService = {
  getEmailLogs: async () => {
    const response = await api.get('/admin/emails/logs');
    return response.data;
  },

  getEmailStats: async () => {
    const response = await api.get('/admin/emails/stats');
    return response.data;
  },

  sendTestEmail: async (data) => {
    const response = await api.post('/admin/emails/test', data);
    return response.data;
  }
};

export default emailMonitoringService;
