import api from './api';

export const leaderboardService = {
  getLeaderboard: async (scope = 'GLOBAL', language = null) => {
    const params = { scope };
    if (language && language !== 'ALL') {
      params.language = language;
    }
    const response = await api.get('/leaderboard', { params });
    return response.data;
  },
};

export default leaderboardService;
