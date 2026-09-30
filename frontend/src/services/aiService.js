import api from './api';

const aiService = {
  /**
   * Send a chat message with contextual metadata to the CodeNova AI assistant.
   * @param {Object} payload
   * @param {string} payload.message - User query
   * @param {string} [payload.language] - Selected human language code (e.g. 'en', 'kn', 'hi')
   * @param {string} [payload.page] - Current page path
   * @param {string} [payload.problem] - Problem title / statement
   * @param {string} [payload.programmingLanguage] - Selected programming language ('JAVA', etc.)
   * @param {string} [payload.code] - Current editor code
   * @param {string} [payload.error] - Runtime or compilation error
   * @param {string} [payload.testResults] - Test results summary
   * @param {string} [payload.assessment] - Assessment context
   * @param {string} [payload.contest] - Contest context
   */
  async sendMessage(payload) {
    const response = await api.post('/ai/chat', payload);
    return response.data;
  },

  /**
   * Fetch authenticated user progress and learning metrics for AI assistant.
   */
  async getUserContext() {
    const response = await api.get('/ai/context');
    return response.data;
  },
};

export default aiService;
