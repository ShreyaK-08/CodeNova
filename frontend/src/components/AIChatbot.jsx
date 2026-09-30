import React, { useState, useRef, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import aiService from '../services/aiService';
import MarkdownRenderer from './MarkdownRenderer';
import { getLanguageDetails } from '../i18n/languages';
import {
  MessageSquare,
  Bot,
  X,
  Send,
  Trash2,
  Minimize2,
  Maximize2,
  Sparkles,
  AlertCircle,
  HelpCircle,
  Code,
  FileCode,
  CheckCircle2
} from 'lucide-react';

const AIChatbot = () => {
  const { t, i18n } = useTranslation();
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  const [isOpen, setIsOpen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [attachedContext, setAttachedContext] = useState(null);

  const currentLangDetails = getLanguageDetails(i18n.language || 'en');

  const [messages, setMessages] = useState([
    {
      id: 'welcome-1',
      sender: 'assistant',
      text: `Hello ${user?.name || user?.username || 'there'}! 👋 I am your **CodeNova AI Assistant**.\n\nI can help you:
- **Track Progress**: Ask *"my progress"*, *"how many easy/medium/hard problems?"*, or *"what is my acceptance rate?"*
- **Review Performance**: Ask *"show my assessment results"* or *"my contest performance"*.
- **Solve Problems**: Ask for conceptual hints, time/space complexity analysis, or approach tips.
- **Debug Code**: Paste your code or runtime error to find the root cause.

How can I assist your coding journey today?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);

  const messagesEndRef = useRef(null);
  const textareaRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen && !isMinimized) {
      scrollToBottom();
    }
  }, [messages, isOpen, isMinimized]);

  // Listen for global AI trigger events from Monaco coding pages or buttons
  useEffect(() => {
    const handleTrigger = (e) => {
      const { message, autoSend, context } = e.detail || {};
      setIsOpen(true);
      setIsMinimized(false);

      if (context) {
        setAttachedContext(context);
      }

      if (message) {
        if (autoSend) {
          executeSendMessage(message, context);
        } else {
          setInputMessage(message);
          setTimeout(() => textareaRef.current?.focus(), 100);
        }
      }
    };

    window.addEventListener('codenova-ai-trigger', handleTrigger);
    return () => window.removeEventListener('codenova-ai-trigger', handleTrigger);
  }, [isAuthenticated, i18n.language]);

  const executeSendMessage = async (msgText, overrideContext = null) => {
    const textToSend = (msgText || inputMessage).trim();
    if (!textToSend || loading) return;

    setError('');
    const userMsg = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: textToSend,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    if (!msgText) setInputMessage('');
    setLoading(true);

    const ctx = overrideContext || attachedContext || {};

    const payload = {
      message: textToSend,
      language: i18n.language || 'en',
      page: location.pathname,
      problem: ctx.problem || null,
      programmingLanguage: ctx.programmingLanguage || null,
      code: ctx.code || null,
      error: ctx.error || null,
      testResults: ctx.testResults || null,
      assessment: ctx.assessment || null,
      contest: ctx.contest || null,
    };

    try {
      if (!isAuthenticated) {
        const authWarningMsg = {
          id: `ai-${Date.now()}`,
          sender: 'assistant',
          text: t('ai.loginRequired', 'Please log in to chat with the CodeNova AI assistant. Your session token is required to ensure secure pair programming.'),
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          status: 'warning',
        };
        setMessages((prev) => [...prev, authWarningMsg]);
        setLoading(false);
        return;
      }

      const res = await aiService.sendMessage(payload);
      const aiReply = {
        id: `ai-${Date.now()}`,
        sender: 'assistant',
        text: res.reply,
        status: res.status,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, aiReply]);
    } catch (err) {
      console.error('Chat error:', err);
      const errMsg = err.response?.data?.message || 'Unable to contact CodeNova AI assistant. Please check your connection or try again.';
      setError(errMsg);
      const aiErrorMsg = {
        id: `ai-err-${Date.now()}`,
        sender: 'assistant',
        text: `⚠️ **Error**: ${errMsg}`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        status: 'error',
      };
      setMessages((prev) => [...prev, aiErrorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      executeSendMessage();
    }
  };

  const handleClearChat = () => {
    setMessages([
      {
        id: 'cleared-1',
        sender: 'assistant',
        text: 'Chat history cleared. How else can I assist your coding today?',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
    setError('');
  };

  const suggestedQuestions = [
    t('ai.myProgress', 'My progress'),
    t('ai.howManyEasy', 'How many easy problems?'),
    t('ai.myAssessmentResults', 'Show my assessment results'),
    t('ai.myContestProgress', 'My contest progress'),
    t('ai.whatCanIDo', 'What can I do on CodeNova?'),
    t('ai.howContestsWork', 'How do contests and security work?'),
  ];

  return (
    <div className="ai-chatbot-container" style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 1200 }}>
      {/* Floating Toggle Button */}
      {!isOpen && (
        <button
          type="button"
          className="ai-chatbot-floating-btn"
          onClick={() => {
            setIsOpen(true);
            setIsMinimized(false);
          }}
          title={t('ai.title', 'CodeNova AI Assistant')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.6rem',
            padding: '0.75rem 1.25rem',
            background: 'linear-gradient(135deg, var(--primary) 0%, #6366f1 100%)',
            color: '#ffffff',
            border: 'none',
            borderRadius: '9999px',
            boxShadow: '0 8px 24px rgba(124, 58, 237, 0.35)',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '0.925rem',
            transition: 'all 0.25s ease',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.transform = 'translateY(-2px)')}
          onMouseLeave={(e) => (e.currentTarget.style.transform = 'translateY(0)')}
        >
          <Bot size={22} />
          <span>CodeNova AI</span>
          <span
            style={{
              fontSize: '0.7rem',
              background: 'rgba(255, 255, 255, 0.25)',
              padding: '1px 7px',
              borderRadius: '999px',
              fontWeight: 700,
            }}
          >
            {currentLangDetails?.code?.toUpperCase() || 'EN'}
          </span>
        </button>
      )}

      {/* Chat Window Panel */}
      {isOpen && (
        <div
          className="ai-chat-panel"
          style={{
            width: '390px',
            maxWidth: 'calc(100vw - 32px)',
            height: isMinimized ? '56px' : '560px',
            maxHeight: 'calc(100vh - 100px)',
            background: 'var(--bg-card)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-xl)',
            boxShadow: 'var(--shadow-lg)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            transition: 'height 0.25s ease',
          }}
        >
          {/* Header */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '0.75rem 1rem',
              background: 'linear-gradient(135deg, var(--primary) 0%, #6366f1 100%)',
              color: '#ffffff',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <div
                style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '50%',
                  background: 'rgba(255, 255, 255, 0.2)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Bot size={18} color="#ffffff" />
              </div>
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.925rem', lineHeight: 1.2 }}>CodeNova AI</div>
                <div style={{ fontSize: '0.72rem', opacity: 0.85 }}>
                  {currentLangDetails?.nativeName || 'English'} ({currentLangDetails?.name})
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              {!isMinimized && (
                <button
                  type="button"
                  onClick={handleClearChat}
                  title={t('ai.clearChat', 'Clear Chat')}
                  style={{ background: 'transparent', border: 'none', color: '#ffffff', cursor: 'pointer', padding: '4px', opacity: 0.85 }}
                >
                  <Trash2 size={16} />
                </button>
              )}
              <button
                type="button"
                onClick={() => setIsMinimized((m) => !m)}
                title={isMinimized ? t('ai.maximize', 'Expand') : t('ai.minimize', 'Minimize')}
                style={{ background: 'transparent', border: 'none', color: '#ffffff', cursor: 'pointer', padding: '4px', opacity: 0.85 }}
              >
                {isMinimized ? <Maximize2 size={16} /> : <Minimize2 size={16} />}
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                title={t('ai.close', 'Close')}
                style={{ background: 'transparent', border: 'none', color: '#ffffff', cursor: 'pointer', padding: '4px', opacity: 0.85 }}
              >
                <X size={18} />
              </button>
            </div>
          </div>

          {!isMinimized && (
            <>
              {/* Context Bar */}
              {attachedContext && (
                <div
                  style={{
                    padding: '0.4rem 0.85rem',
                    background: 'var(--primary-soft)',
                    borderBottom: '1px solid var(--border-color)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    fontSize: '0.75rem',
                    color: 'var(--primary)',
                    fontWeight: 600,
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    <Code size={13} />
                    <span>
                      Context: {attachedContext.problem || attachedContext.contest || attachedContext.assessment || 'Editor active'}
                      {attachedContext.programmingLanguage ? ` (${attachedContext.programmingLanguage})` : ''}
                    </span>
                  </div>
                  <button
                    type="button"
                    onClick={() => setAttachedContext(null)}
                    style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '0.7rem' }}
                    title="Remove attached context"
                  >
                    Clear
                  </button>
                </div>
              )}

              {/* Message List */}
              <div
                style={{
                  flex: 1,
                  overflowY: 'auto',
                  padding: '1rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.85rem',
                  background: 'var(--bg-main)',
                }}
              >
                {messages.map((m) => {
                  const isUser = m.sender === 'user';
                  return (
                    <div
                      key={m.id}
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: isUser ? 'flex-end' : 'flex-start',
                        maxWidth: '100%',
                      }}
                    >
                      <div
                        style={{
                          maxWidth: '85%',
                          padding: isUser ? '0.65rem 0.95rem' : '0.85rem 1rem',
                          borderRadius: isUser ? '16px 16px 4px 16px' : '16px 16px 16px 4px',
                          background: isUser ? 'var(--primary)' : 'var(--bg-card)',
                          color: isUser ? '#ffffff' : 'var(--text-main)',
                          border: isUser ? 'none' : '1px solid var(--border-color)',
                          boxShadow: 'var(--shadow-sm)',
                          fontSize: '0.875rem',
                          lineHeight: 1.55,
                        }}
                      >
                        {isUser ? (
                          <div style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>{m.text}</div>
                        ) : (
                          <div className="ai-markdown-reply">
                            <MarkdownRenderer content={m.text} />
                          </div>
                        )}
                      </div>
                      <span
                        style={{
                          fontSize: '0.65rem',
                          color: 'var(--text-subtle)',
                          marginTop: '3px',
                          padding: '0 4px',
                        }}
                      >
                        {m.timestamp}
                      </span>
                    </div>
                  );
                })}

                {loading && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.5rem 0.75rem' }}>
                    <div
                      style={{
                        width: '28px',
                        height: '28px',
                        borderRadius: '50%',
                        background: 'var(--primary-soft)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      <Sparkles size={14} color="var(--primary)" />
                    </div>
                    <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                      {t('ai.thinking', 'AI Assistant is thinking...')}
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Suggested Quick Question Chips (when only initial message exists) */}
              {messages.length <= 2 && !loading && (
                <div
                  style={{
                    padding: '0.5rem 0.85rem',
                    background: 'var(--bg-elevated)',
                    borderTop: '1px solid var(--border-color)',
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: '0.35rem',
                  }}
                >
                  {suggestedQuestions.map((q, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => executeSendMessage(q)}
                      style={{
                        fontSize: '0.75rem',
                        padding: '3px 9px',
                        borderRadius: '999px',
                        border: '1px solid var(--border-color)',
                        background: 'var(--bg-card)',
                        color: 'var(--primary)',
                        cursor: 'pointer',
                        textAlign: 'left',
                        transition: 'all 0.15s',
                      }}
                      onMouseEnter={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                      onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'var(--border-color)')}
                    >
                      {q}
                    </button>
                  ))}
                </div>
              )}

              {/* Input Area */}
              <div
                style={{
                  padding: '0.75rem',
                  background: 'var(--bg-card)',
                  borderTop: '1px solid var(--border-color)',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'flex-end',
                    gap: '0.5rem',
                    background: 'var(--bg-input)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    padding: '0.4rem 0.65rem',
                  }}
                >
                  <textarea
                    ref={textareaRef}
                    rows={2}
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder={t('ai.askPlaceholder', 'Ask a question about this problem, code, or CodeNova...')}
                    disabled={loading}
                    style={{
                      flex: 1,
                      border: 'none',
                      outline: 'none',
                      resize: 'none',
                      fontSize: '0.85rem',
                      fontFamily: 'var(--font-sans)',
                      background: 'transparent',
                      color: 'var(--text-main)',
                      lineHeight: 1.4,
                    }}
                  />
                  <button
                    type="button"
                    onClick={() => executeSendMessage()}
                    disabled={!inputMessage.trim() || loading}
                    aria-label={t('ai.send', 'Send')}
                    style={{
                      padding: '0.45rem',
                      borderRadius: 'var(--radius-sm)',
                      background: inputMessage.trim() && !loading ? 'var(--primary)' : 'var(--border-color)',
                      color: '#ffffff',
                      border: 'none',
                      cursor: inputMessage.trim() && !loading ? 'pointer' : 'not-allowed',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      transition: 'background 0.2s',
                    }}
                  >
                    <Send size={15} />
                  </button>
                </div>
                <div style={{ fontSize: '0.65rem', color: 'var(--text-subtle)', marginTop: '4px', textAlign: 'right' }}>
                  Press Enter to send, Shift+Enter for new line
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default AIChatbot;
