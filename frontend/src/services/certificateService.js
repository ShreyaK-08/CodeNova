import api from './api';

export const certificateService = {
  getMyCertificates: async (language) => {
    const response = await api.get('/certificates', {
      params: language ? { language } : {},
    });
    return response.data;
  },

  getProgress: async () => {
    const response = await api.get('/certificates/progress');
    return response.data;
  },

  downloadPdf: async (certificateId) => {
    const response = await api.get(`/certificates/${certificateId}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Public - no auth required
  verify: async (code) => {
    const response = await api.get(`/certificates/verify/${encodeURIComponent(code)}`);
    return response.data;
  },

  // Admin
  getAllCertificates: async () => {
    const response = await api.get('/admin-certificates');
    return response.data;
  },
};

export default certificateService;
