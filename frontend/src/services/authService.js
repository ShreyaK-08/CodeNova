import api from './api';

export const authService = {
  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  verifyEmail: async (token) => {
    const response = await api.post('/auth/verify-email', { token });
    return response.data;
  },

  resendVerification: async (email) => {
    const response = await api.post('/auth/resend-verification', { email });
    return response.data;
  },

  getGoogleConfig: async () => {
    const response = await api.get('/auth/google/config');
    return response.data;
  },

  getGoogleAuthUrl: async () => {
    const response = await api.get('/auth/google/url');
    return response.data;
  },

  googleCallback: async (code) => {
    const response = await api.post('/auth/google/callback', { code });
    return response.data;
  },

  googleTokenLogin: async (credential) => {
    const response = await api.post('/auth/google/token', { credential });
    return response.data;
  },
};

export default authService;
