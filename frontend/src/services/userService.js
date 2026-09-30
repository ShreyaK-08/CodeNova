import api from './api';

export const userService = {
  getProfile: async () => {
    const response = await api.get('/users/profile');
    return response.data;
  },

  updateProfile: async (profileData) => {
    const response = await api.put('/users/profile', profileData);
    return response.data;
  },

  getDashboardStats: async () => {
    const response = await api.get('/users/dashboard-stats');
    return response.data;
  },

  getUserSubmissions: async () => {
    const response = await api.get('/users/submissions');
    return response.data;
  },

  changePassword: async (passwordData) => {
    const response = await api.put('/users/change-password', passwordData);
    return response.data;
  },
};

export default userService;
