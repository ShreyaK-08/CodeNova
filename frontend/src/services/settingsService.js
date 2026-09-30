import api from './api';

const settingsService = {
  // User Settings
  getUserSettings: async () => {
    const response = await api.get('/users/settings');
    return response.data;
  },

  updateUserSettings: async (settingsData) => {
    const response = await api.put('/users/settings', settingsData);
    return response.data;
  },

  // Admin Platform Settings
  getAdminSettings: async () => {
    const response = await api.get('/admin/settings');
    return response.data;
  },

  updateAdminSettings: async (configData) => {
    const response = await api.put('/admin/settings', configData);
    return response.data;
  },

  getSystemStatus: async () => {
    const response = await api.get('/admin/settings/system-status');
    return response.data;
  }
};

export default settingsService;
