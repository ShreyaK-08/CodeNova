import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Editor from '@monaco-editor/react';
import problemService from '../../services/problemService';
import translationService from '../../services/translationService';
import MarkdownRenderer from '../../components/MarkdownRenderer';
import { useTheme } from '../../context/ThemeContext';
import {
  ArrowLeft,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Send,
  Play,
  RotateCcw,
  Clock,
  Lock,
  Unlock,
  HelpCircle,
  BookOpen,
  FileText,
  Copy,
  Check,
  ChevronDown,
  ChevronUp,
  Bot,
  Sparkles,
} from 'lucide-react';

// NOTE: the constants below are exported (in addition to the default export) purely so
// ContestCoding.jsx (Task 7) can reuse this exact editor configuration/starter-code
// logic instead of re-implementing a second copy of it. Nothing about their values or
// behavior changed for the normal /problems/:id flow.
export const LANGUAGE_TEMPLATES = {
  JAVA: 'public class Solution {\n    public int[] solve(int[] nums, int target) {\n        // TODO: write your solution here\n        return new int[]{-1, -1};\n    }\n}',
  PYTHON: 'import sys\n\ndef main():\n    data = sys.stdin.read().split(\'\\n\')\n\n    # TODO: write your solution here\n\n    print()\n\nif __name__ == "__main__":\n    main()',
  CPP: '#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // TODO: read input with cin and write your solution here\n\n    return 0;\n}',
  JAVASCRIPT: 'const readline = require(\'readline\');\nconst rl = readline.createInterface({ input: process.stdin });\nlet lines = [];\nrl.on(\'line\', (line) => lines.push(line));\nrl.on(\'close\', () => {\n    // TODO: write your solution here\n\n    console.log();\n});',
};

export const getStarterCode = (prob, lang) => {
  if (!prob) return LANGUAGE_TEMPLATES[lang] || '';
  switch (lang) {
    case 'JAVA':
      return prob.starterCode || LANGUAGE_TEMPLATES.JAVA;
    case 'PYTHON':
      return prob.starterCodePython || LANGUAGE_TEMPLATES.PYTHON;
    case 'CPP':
      return prob.starterCodeCpp || LANGUAGE_TEMPLATES.CPP;
    case 'JAVASCRIPT':
      return prob.starterCodeJs || LANGUAGE_TEMPLATES.JAVASCRIPT;
    default:
      return LANGUAGE_TEMPLATES[lang] || '';
  }
};

// Maps the application's language codes to Monaco's built-in language ids so
// syntax highlighting switches automatically when the user changes language.
export const MONACO_LANGUAGE_MAP = {
  JAVA: 'java',
  PYTHON: 'python',
  CPP: 'cpp',
  JAVASCRIPT: 'javascript',
};

export const STATUS_STYLES = {
  PASSED: { color: 'var(--success)', icon: CheckCircle2, label: 'Passed' },
  ACCEPTED: { color: 'var(--success)', icon: CheckCircle2, label: 'Accepted' },
  WRONG_ANSWER: { color: 'var(--danger)', icon: XCircle, label: 'Wrong Answer' },
  RUNTIME_ERROR: { color: 'var(--danger)', icon: XCircle, label: 'Runtime Error' },
  COMPILATION_ERROR: { color: 'var(--danger)', icon: AlertCircle, label: 'Compilation Error' },
  COMPILE_ERROR: { color: 'var(--danger)', icon: AlertCircle, label: 'Compilation Error' },
  TIME_LIMIT_EXCEEDED: { color: 'var(--warning)', icon: Clock, label: 'Time Limit Exceeded' },
  MEMORY_LIMIT_EXCEEDED: { color: 'var(--warning)', icon: XCircle, label: 'Memory Limit Exceeded' },
  PENDING: { color: 'var(--text-muted)', icon: Clock, label: 'Pending' },
  RUNNING: { color: 'var(--text-muted)', icon: Clock, label: 'Running' },
};

// Used only if the backend ever returns a status value not covered above - shows an
// honest "Unknown" verdict rather than silently mislabeling it as Wrong Answer.
export const UNKNOWN_STATUS_STYLE = { color: 'var(--text-muted)', icon: AlertCircle, label: 'Unknown Status' };

const ProblemDetail = () => {
  const { id } = useParams();
  const { theme } = useTheme();
  const { t, i18n } = useTranslation();
  const [problem, setProblem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [language, setLanguage] = useState('JAVA');
  const [code, setCode] = useState('');
  const [saveStatus, setSaveStatus] = useState('idle'); // 'idle' | 'saving' | 'saved'

  const triggerAiAssistant = (actionType) => {
    let msg = '';
    const currentErr = runResult?.error || submissionResult?.error || (runResult?.status && runResult?.status !== 'ACCEPTED' ? `Status: ${runResult.status}` : null);

    switch (actionType) {
      case 'explain-code':
        msg = `Please explain my ${language} code structure and step-by-step logic.`;
        break;
      case 'explain-error':
        msg = currentErr
          ? `Please explain this error and suggest how to resolve it:\n${currentErr}`
          : `Can you review my ${language} code for potential edge cases, syntax, or runtime errors?`;
        break;
      case 'give-hint':
        msg = `Give me a conceptual hint for solving "${problem?.title || 'this problem'}" without giving away the complete solution code.`;
        break;
      case 'improve-code':
        msg = `How can I optimize the time and space complexity or improve the readability of my ${language} solution?`;
        break;
      case 'chat-problem':
      default:
        msg = `Can you explain the problem statement, constraints, and algorithmic intuition for "${problem?.title || 'this problem'}" in simple terms?`;
        break;
    }

    window.dispatchEvent(
      new CustomEvent('codenova-ai-trigger', {
        detail: {
          message: msg,
          autoSend: true,
          context: {
            problem: problem?.title ? `${problem.title} (Difficulty: ${problem.difficulty})` : null,
            programmingLanguage: language,
            code: code,
            error: currentErr,
            testResults: runResult ? `Run: ${runResult.status}` : submissionResult ? `Submission: ${submissionResult.status}` : null,
          },
        },
      })
    );
  };
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [runResult, setRunResult] = useState(null);
  const [submissionResult, setSubmissionResult] = useState(null);

  // Left panel tab: 'description' | 'hints' | 'editorial'
  const [activeTab, setActiveTab] = useState('description');

  // Hints and Editorial state
  const [hints, setHints] = useState([]);
  const [hintsLoading, setHintsLoading] = useState(false);
  const [revealedHints, setRevealedHints] = useState({});

  const [editorial, setEditorial] = useState(null);
  const [editorialLoading, setEditorialLoading] = useState(false);
  const [copiedCode, setCopiedCode] = useState(false);
  const [translatedProblem, setTranslatedProblem] = useState(null);

  const appLanguage = (i18n.language || 'en').trim().toLowerCase();

  useEffect(() => {
    if (!problem) return;
    if (appLanguage === 'en') {
      setTranslatedProblem(null);
      return;
    }

    let active = true;
    const translateContent = async () => {
      try {
        const textsToTranslate = [
          problem.title || '',
          problem.description || ''
        ];
        const res = await translationService.translateTexts(appLanguage, textsToTranslate, 'EN');
        if (!active) return;
        setTranslatedProblem({
          title: res[0] || problem.title,
          description: res[1] || problem.description
        });
      } catch (e) {
        console.warn('Failed to translate problem content:', e);
      }
    };

    translateContent();
    return () => { active = false; };
  }, [problem, appLanguage]);

  // --- Saved-code integration (Task 2) ---
  // Guards against race conditions: every time we start loading saved-code (initial
  // mount or a language switch), we bump this token. A response is only applied if it's
  // still the most recent request, so a slow response for a language the user already
  // switched away from can never clobber what's on screen.
  const loadRequestIdRef = useRef(0);
  // While true, the code-change effect below must NOT trigger an auto-save - it's the
  // loader itself (starter code or fetched saved code) setting `code`, not the user typing.
  const isApplyingLoadedCodeRef = useRef(false);
  const autoSaveTimerRef = useRef(null);

  const busy = running || submitting;

  const fetchHints = useCallback(async () => {
    try {
      setHintsLoading(true);
      const data = await problemService.getHints(id);
      setHints(data || []);
    } catch (err) {
      console.error('Failed to load hints:', err);
    } finally {
      setHintsLoading(false);
    }
  }, [id]);

  const fetchEditorial = useCallback(async () => {
    try {
      setEditorialLoading(true);
      const data = await problemService.getEditorial(id);
      setEditorial(data);
    } catch (err) {
      console.error('Failed to load editorial:', err);
    } finally {
      setEditorialLoading(false);
    }
  }, [id]);

  useEffect(() => {
    const fetchProblem = async () => {
      const myRequestId = ++loadRequestIdRef.current;
      try {
        setLoading(true);
        const data = await problemService.getProblemById(id);
        if (myRequestId !== loadRequestIdRef.current) return; // superseded, e.g. id changed
        setProblem(data);

        // Prefer the user's previously saved code for the default language (JAVA);
        // fall back to the problem's starter code if nothing was saved yet.
        let initialCode = getStarterCode(data, 'JAVA');
        try {
          const saved = await problemService.getSavedCode(id, 'JAVA');
          if (myRequestId !== loadRequestIdRef.current) return;
          if (saved?.code) initialCode = saved.code;
        } catch (savedCodeErr) {
          // Non-fatal: just use starter code if saved-code lookup fails for any reason.
          console.error('Failed to load saved code:', savedCodeErr);
        }

        isApplyingLoadedCodeRef.current = true;
        setCode(initialCode);
      } catch (err) {
        if (myRequestId === loadRequestIdRef.current) {
          setError('Failed to load problem details.');
        }
      } finally {
        if (myRequestId === loadRequestIdRef.current) setLoading(false);
      }
    };

    fetchProblem();
    fetchHints();
    fetchEditorial();
  }, [id, fetchHints, fetchEditorial]);

  const handleLanguageChange = async (newLanguage) => {
    setLanguage(newLanguage);
    setRunResult(null);
    setSubmissionResult(null);

    const myRequestId = ++loadRequestIdRef.current;
    let nextCode = getStarterCode(problem, newLanguage);
    try {
      const saved = await problemService.getSavedCode(id, newLanguage);
      if (myRequestId !== loadRequestIdRef.current) return; // user switched language again before this resolved
      if (saved?.code) nextCode = saved.code;
    } catch (err) {
      console.error('Failed to load saved code for language change:', err);
      if (myRequestId !== loadRequestIdRef.current) return;
    }

    isApplyingLoadedCodeRef.current = true;
    setCode(nextCode);
  };

  // Debounced auto-save: persists the user's code ~1.2s after they stop typing, via the
  // same saved-code endpoint Task 1 added. Skipped entirely when the `code` change came
  // from loading starter/saved code (isApplyingLoadedCodeRef), not from user input -
  // otherwise every load would immediately re-save itself right back, wastefully.
  useEffect(() => {
    if (loading || !problem) return;
    if (isApplyingLoadedCodeRef.current) {
      isApplyingLoadedCodeRef.current = false;
      return;
    }
    if (!code || !code.trim()) return;

    setSaveStatus('idle');
    if (autoSaveTimerRef.current) clearTimeout(autoSaveTimerRef.current);

    autoSaveTimerRef.current = setTimeout(async () => {
      setSaveStatus('saving');
      try {
        await problemService.saveCode(id, language, code);
        setSaveStatus('saved');
      } catch (err) {
        console.error('Auto-save of code failed:', err);
        setSaveStatus('idle');
      }
    }, 1200);

    return () => {
      if (autoSaveTimerRef.current) clearTimeout(autoSaveTimerRef.current);
    };
    // Only `code` should re-trigger this - language/id/problem are read fresh from this
    // render's closure, and including them would cause the debounce to reset on every
    // language switch even though a totally different effect already handles that case.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [code]);

  const handleResetCode = () => {
    if (!window.confirm('Reset your code back to the starter template? This will discard your current changes.')) return;
    setCode(getStarterCode(problem, language));
  };

  const toggleHintReveal = (hintId) => {
    setRevealedHints((prev) => ({
      ...prev,
      [hintId]: !prev[hintId],
    }));
  };

  const handleCopyEditorialCode = () => {
    if (!editorial?.solution) return;
    navigator.clipboard.writeText(editorial.solution);
    setCopiedCode(true);
    setTimeout(() => setCopiedCode(false), 2000);
  };

  const handleRun = async () => {
    setError('');
    setSubmissionResult(null);
    setRunResult(null);
    setRunning(true);

    try {
      const res = await problemService.runCode({
        problemId: problem.id,
        language,
        code,
      });
      setRunResult(res);
    } catch (err) {
      setError(err.response?.data?.message || 'Run failed. Please try again.');
    } finally {
      setRunning(false);
    }
  };

  const handleSubmitCode = async () => {
    setError('');
    setRunResult(null);
    setSubmissionResult(null);
    setSubmitting(true);

    try {
      const res = await problemService.submitCode({
        problemId: problem.id,
        language,
        code,
      });
      setSubmissionResult(res);
      // Task 1's backend already persists this exact code as the saved-code record as
      // part of a successful submission - reflect that in the indicator without firing
      // a redundant second save request.
      setSaveStatus('saved');

      // Submission status change may unlock hints or editorial - refresh them live!
      fetchHints();
      fetchEditorial();
    } catch (err) {
      setError(err.response?.data?.message || 'Submission failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading problem workspace...</div>;
  }

  if (!problem) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <p>Problem not found.</p>
        <Link to="/problems" className="btn btn-outline btn-sm" style={{ marginTop: '1rem' }}>
          Back to Problems
        </Link>
      </div>
    );
  }

  const activeResult = submissionResult || runResult;
  const isSubmitResult = !!submissionResult;
  const statusInfo = activeResult ? (STATUS_STYLES[activeResult.status] || UNKNOWN_STATUS_STYLE) : null;
  const StatusIcon = statusInfo?.icon;

  const displayTitle = translatedProblem?.title || problem.title;
  const displayDescription = translatedProblem?.description || problem.description;

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: '1.25rem' }}>
        <Link to="/problems" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
          <ArrowLeft size={16} />
          <span>Back to Problem List</span>
        </Link>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{displayTitle}</h1>
          <span className={`badge badge-${problem.difficulty.toLowerCase()}`}>
            {problem.difficulty}
          </span>
          {problem.topic && (
            <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
              {problem.topic.split(',').map((t, i) => (
                <span key={i} className="badge badge-tag">{t.trim()}</span>
              ))}
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="solve-layout">
        {/* LEFT PANEL: Problem Tabs (Description, Hints, Editorial) */}
        <div className="solve-left">
          {/* Navigation Tabs */}
          <div style={{
            display: 'flex',
            gap: '0.5rem',
            borderBottom: '1px solid var(--border-color)',
            paddingBottom: '0.5rem',
            alignItems: 'center',
          }}>
            <button
              onClick={() => setActiveTab('description')}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.5rem 1rem',
                borderRadius: 'var(--radius-md)',
                background: activeTab === 'description' ? 'var(--primary-soft)' : 'transparent',
                color: activeTab === 'description' ? 'var(--primary)' : 'var(--text-muted)',
                fontWeight: 600,
                fontSize: '0.9rem',
                border: 'none',
                cursor: 'pointer',
                transition: 'all 0.2s',
              }}
            >
              <FileText size={16} />
              <span>{t('editor.description', 'Description')}</span>
            </button>

            <button
              onClick={() => setActiveTab('hints')}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.5rem 1rem',
                borderRadius: 'var(--radius-md)',
                background: activeTab === 'hints' ? 'var(--primary-soft)' : 'transparent',
                color: activeTab === 'hints' ? 'var(--primary)' : 'var(--text-muted)',
                fontWeight: 600,
                fontSize: '0.9rem',
                border: 'none',
                cursor: 'pointer',
                transition: 'all 0.2s',
              }}
            >
              <HelpCircle size={16} />
              <span>{t('editor.hints', 'Hints')}</span>
              {hints.length > 0 && (
                <span style={{
                  background: activeTab === 'hints' ? 'var(--primary)' : 'rgba(255, 255, 255, 0.1)',
                  color: activeTab === 'hints' ? '#fff' : 'var(--text-muted)',
                  borderRadius: '10px',
                  padding: '1px 7px',
                  fontSize: '0.75rem',
                }}>
                  {hints.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('editorial')}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.5rem 1rem',
                borderRadius: 'var(--radius-md)',
                background: activeTab === 'editorial' ? 'var(--primary-soft)' : 'transparent',
                color: activeTab === 'editorial' ? 'var(--primary)' : 'var(--text-muted)',
                fontWeight: 600,
                fontSize: '0.9rem',
                border: 'none',
                cursor: 'pointer',
                transition: 'all 0.2s',
              }}
            >
              <BookOpen size={16} />
              <span>{t('editor.editorial', 'Editorial')}</span>
              {editorial?.unlocked && (
                <span style={{
                  background: 'var(--success-soft)',
                  color: 'var(--success)',
                  borderRadius: '10px',
                  padding: '1px 7px',
                  fontSize: '0.75rem',
                }}>
                  Unlocked
                </span>
              )}
            </button>
          </div>

          {/* TAB 1: DESCRIPTION */}
          {activeTab === 'description' && (
            <>
              <div className="card">
                <div className="panel-header">
                  <span className="panel-title">{t('problemDetail.problemStatement', 'Problem Statement')}</span>
                </div>
                <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.65, color: 'var(--text-muted)', fontSize: '0.95rem' }}>
                  {displayDescription}
                </div>
              </div>

              {problem.constraints && problem.constraints.trim() !== '' && (
                <div className="card">
                  <div className="panel-header">
                    <span className="panel-title">{t('problemDetail.constraints', 'Constraints')}</span>
                  </div>
                  <pre style={{
                    whiteSpace: 'pre-wrap',
                    lineHeight: 1.6,
                    color: 'var(--text-muted)',
                    fontSize: '0.9rem',
                    fontFamily: 'JetBrains Mono, monospace',
                    margin: 0,
                  }}>
                    {problem.constraints}
                  </pre>
                </div>
              )}

              <div className="card">
                <div className="panel-header">
                  <span className="panel-title">{t('problemDetail.examplesAndSamples', 'Examples & Sample Test Cases')}</span>
                </div>
                {problem.testCases && problem.testCases.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {problem.testCases.map((tc, idx) => (
                      <div key={idx} className="testcase-box">
                        <div className="testcase-box-header">
                          {t('problemDetail.example', 'Example')} {idx + 1} {tc.isHidden ? <span style={{ color: 'var(--text-subtle)' }}>({t('problemDetail.hidden', 'hidden')})</span> : ''}
                        </div>
                        {tc.isHidden ? (
                          <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', margin: 0 }}>
                            {t('problemDetail.hiddenExplanation', 'This test case is hidden and only used for grading on Submit.')}
                          </p>
                        ) : (
                          <>
                            <div style={{ fontSize: '0.875rem' }}><strong>{t('problemDetail.input', 'Input')}:</strong> <code>{tc.input}</code></div>
                            <div style={{ fontSize: '0.875rem', marginTop: '0.3rem' }}><strong>{t('problemDetail.output', 'Output')}:</strong> <code>{tc.expectedOutput}</code></div>
                          </>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{t('problemDetail.noPublicCases', 'No public test cases attached.')}</p>
                )}
              </div>
            </>
          )}

          {/* TAB 2: HINTS */}
          {activeTab === 'hints' && (
            <div className="card">
              <div className="panel-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <HelpCircle size={18} color="var(--primary)" />
                  <span className="panel-title">{t('problemDetail.hintsProgression', 'Hints Progression')}</span>
                </div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
                  {t('problemDetail.hintsUnlockRule', 'Hints unlock after failed submission attempts')}
                </span>
              </div>

              {hintsLoading ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>{t('problemDetail.loadingHints', 'Loading hints...')}</div>
              ) : hints.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--text-muted)' }}>
                  <HelpCircle size={36} style={{ margin: '0 auto 0.75rem', opacity: 0.4 }} />
                  <p>{t('problemDetail.noHints', 'No hints have been created for this problem yet.')}</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {hints.map((hint) => {
                    const isUnlocked = hint.unlocked;
                    const isRevealed = revealedHints[hint.id];

                    return (
                      <div
                        key={hint.id}
                        style={{
                          background: isUnlocked ? 'var(--bg-elevated)' : 'rgba(255, 255, 255, 0.02)',
                          border: `1px solid ${isUnlocked ? 'var(--primary-soft)' : 'var(--border-color)'}`,
                          borderRadius: 'var(--radius-md)',
                          padding: '1rem',
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            {isUnlocked ? (
                              <Unlock size={16} color="var(--primary)" />
                            ) : (
                              <Lock size={16} color="var(--warning)" />
                            )}
                            <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>
                              {t('problemDetail.hint', 'Hint')} {hint.hintOrder}
                            </span>
                          </div>

                          {isUnlocked ? (
                            <button
                              onClick={() => toggleHintReveal(hint.id)}
                              className="btn btn-outline btn-sm"
                              style={{ padding: '0.25rem 0.65rem', fontSize: '0.8rem' }}
                            >
                              {isRevealed ? (
                                <>
                                  <ChevronUp size={14} />
                                  <span>Hide</span>
                                </>
                              ) : (
                                <>
                                  <ChevronDown size={14} />
                                  <span>Reveal Hint</span>
                                </>
                              )}
                            </button>
                          ) : (
                            <span style={{
                              fontSize: '0.75rem',
                              color: 'var(--warning)',
                              background: 'var(--warning-soft)',
                              padding: '2px 8px',
                              borderRadius: '9999px',
                              fontWeight: 600,
                            }}>
                              Locked
                            </span>
                          )}
                        </div>

                        {isUnlocked ? (
                          isRevealed ? (
                            <div style={{
                              marginTop: '0.85rem',
                              paddingTop: '0.85rem',
                              borderTop: '1px solid var(--border-color)',
                              color: 'var(--text-muted)',
                              fontSize: '0.9rem',
                              lineHeight: 1.6,
                            }}>
                              <MarkdownRenderer content={hint.content} />
                            </div>
                          ) : (
                            <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.825rem', color: 'var(--text-subtle)' }}>
                              Hint unlocked! Click "Reveal Hint" when you're ready to read it.
                            </p>
                          )
                        ) : (
                          <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.825rem', color: 'var(--text-subtle)' }}>
                            Locked. Unlocks automatically after <strong>{hint.unlockAfterAttempts}</strong> failed submission attempt{hint.unlockAfterAttempts > 1 ? 's' : ''}.
                          </p>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 3: EDITORIAL */}
          {activeTab === 'editorial' && (
            <div className="card">
              <div className="panel-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <BookOpen size={18} color="var(--primary)" />
                  <span className="panel-title">Official Editorial</span>
                </div>
                {editorial?.unlocked && (
                  <span style={{
                    fontSize: '0.75rem',
                    color: 'var(--success)',
                    background: 'var(--success-soft)',
                    padding: '2px 8px',
                    borderRadius: '9999px',
                    fontWeight: 600,
                  }}>
                    Unlocked
                  </span>
                )}
              </div>

              {editorialLoading ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Loading editorial...</div>
              ) : !editorial || !editorial.hasEditorial ? (
                <div style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--text-muted)' }}>
                  <BookOpen size={36} style={{ margin: '0 auto 0.75rem', opacity: 0.4 }} />
                  <p>No editorial solution is available for this problem yet.</p>
                </div>
              ) : !editorial.unlocked ? (
                <div style={{
                  textAlign: 'center',
                  padding: '3rem 1.5rem',
                  background: 'rgba(255, 255, 255, 0.02)',
                  borderRadius: 'var(--radius-lg)',
                  border: '1px solid var(--border-color)',
                }}>
                  <div style={{
                    width: '56px',
                    height: '56px',
                    borderRadius: '50%',
                    background: 'var(--warning-soft)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 1rem',
                  }}>
                    <Lock size={26} color="var(--warning)" />
                  </div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                    {editorial.title || 'Official Editorial Solution'}
                  </h3>
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', maxWidth: '440px', margin: '0 auto 1.5rem' }}>
                    This editorial is locked to preserve learning integrity. It will unlock automatically once you
                    solve the problem (Accepted) or submit <strong>{editorial.unlockAfterAttempts || 3}</strong> failed attempts.
                  </p>
                  <div style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    fontSize: '0.85rem',
                    color: 'var(--warning)',
                    background: 'var(--warning-soft)',
                    padding: '0.5rem 1rem',
                    borderRadius: 'var(--radius-md)',
                  }}>
                    <span>Keep submitting your solutions to gain access!</span>
                  </div>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                  <div>
                    <h2 style={{ fontSize: '1.3rem', fontWeight: 800, marginBottom: '0.5rem' }}>
                      {editorial.title || 'Solution Explanation'}
                    </h2>
                    <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem' }}>
                      Complete breakdown, algorithmic strategy, complexity analysis, and implementation.
                    </p>
                  </div>

                  {editorial.approach && (
                    <div style={{ background: 'var(--bg-elevated)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                      <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--primary)' }}>
                        Approach &amp; Intuition
                      </h3>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.925rem', lineHeight: 1.65 }}>
                        <MarkdownRenderer content={editorial.approach} />
                      </div>
                    </div>
                  )}

                  {editorial.algorithm && (
                    <div style={{ background: 'var(--bg-elevated)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                      <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--primary)' }}>
                        {t('problemDetail.algorithm', 'Algorithm & Step-by-Step')}
                      </h3>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.925rem', lineHeight: 1.65 }}>
                        <MarkdownRenderer content={editorial.algorithm} />
                      </div>
                    </div>
                  )}

                  {editorial.complexity && (
                    <div style={{ background: 'var(--bg-elevated)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                      <h3 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', color: 'var(--primary)' }}>
                        {t('problemDetail.complexityAnalysis', 'Complexity Analysis')}
                      </h3>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.925rem', lineHeight: 1.65 }}>
                        <MarkdownRenderer content={editorial.complexity} />
                      </div>
                    </div>
                  )}

                  {editorial.solution && (
                    <div style={{ background: 'var(--bg-elevated)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                        <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--primary)', margin: 0 }}>
                          {t('problemDetail.referenceImplementation', 'Reference Implementation')}
                        </h3>
                        <button
                          onClick={handleCopyEditorialCode}
                          className="btn btn-outline btn-sm"
                          style={{ padding: '0.25rem 0.65rem', fontSize: '0.75rem', gap: '0.35rem' }}
                        >
                          {copiedCode ? <Check size={14} color="var(--success)" /> : <Copy size={14} />}
                          <span>{copiedCode ? t('problemDetail.copied', 'Copied') : t('problemDetail.copyCode', 'Copy Code')}</span>
                        </button>
                      </div>
                      <pre style={{
                        background: '#0a0f1d',
                        padding: '1rem',
                        borderRadius: 'var(--radius-sm)',
                        overflowX: 'auto',
                        fontFamily: 'JetBrains Mono, monospace',
                        fontSize: '0.85rem',
                        color: '#f8fafc',
                        lineHeight: 1.5,
                        margin: 0,
                      }}>
                        <code>{editorial.solution}</code>
                      </pre>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>

        {/* RIGHT PANEL: Code Editor + Run/Submit + Test Result details */}
        <div className="solve-right">
          <div className="card editor-card">
            <div className="editor-toolbar">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)' }}>{t('common.language', 'Language')}</label>
                <select
                  value={language}
                  onChange={(e) => handleLanguageChange(e.target.value)}
                  className="form-control"
                  style={{ padding: '0.4rem 0.75rem', fontSize: '0.85rem', width: 'auto' }}
                >
                  <option value="JAVA">Java</option>
                  <option value="PYTHON">Python</option>
                  <option value="CPP">C++</option>
                  <option value="JAVASCRIPT">JavaScript</option>
                </select>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {saveStatus !== 'idle' && (
                  <span
                    style={{
                      fontSize: '0.75rem',
                      color: saveStatus === 'saving' ? 'var(--text-muted)' : 'var(--success)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.25rem',
                    }}
                  >
                    {saveStatus === 'saving' ? (
                      t('common.saving', 'Saving...')
                    ) : (
                      <>
                        <Check size={13} /> {t('common.saved', 'Saved')}
                      </>
                    )}
                  </span>
                )}
                <button onClick={handleResetCode} className="btn btn-outline btn-sm" title="Reset to starter code" disabled={busy}>
                  <RotateCcw size={14} />
                  <span>{t('editor.reset', 'Reset')}</span>
                </button>
              </div>
            </div>

            {problem.methodName && language !== 'JAVA' && (
              <p style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', margin: '0 0 0.75rem 0' }}>
                This problem is set up for Java's Solution class format. In {language === 'CPP' ? 'C++' : language === 'JAVASCRIPT' ? 'JavaScript' : 'Python'},
                the raw test case text (e.g. <code>{problem.testCases?.[0]?.input || '[2,7,11,15], 9'}</code>) is fed to your program via
                standard input — you'll need to parse it yourself.
              </p>
            )}

            <div className="monaco-editor-wrapper">
              <Editor
                height="100%"
                language={MONACO_LANGUAGE_MAP[language] || 'plaintext'}
                value={code}
                onChange={(value) => setCode(value ?? '')}
                theme={theme === 'dark' ? 'vs-dark' : 'light'}
                options={{
                  fontSize: 14,
                  fontFamily: "'JetBrains Mono', 'Fira Code', Consolas, monospace",
                  minimap: { enabled: true },
                  lineNumbers: 'on',
                  tabSize: 4,
                  insertSpaces: true,
                  automaticLayout: true,
                  folding: true,
                  matchBrackets: 'always',
                  scrollBeyondLastLine: false,
                  wordWrap: 'off',
                  renderLineHighlight: 'line',
                }}
                loading={<div style={{ padding: '1rem', color: 'var(--text-muted)' }}>Loading editor...</div>}
              />
            </div>

            {/* AI Assistant Quick Actions Bar */}
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '0.4rem',
                padding: '0.45rem 0.75rem',
                background: 'var(--bg-elevated)',
                borderTop: '1px solid var(--border-color)',
                flexWrap: 'wrap',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem', fontWeight: 700, color: 'var(--primary)' }}>
                <Sparkles size={14} />
                <span>{t('problemDetail.aiAssistant', 'AI Assistant:')}</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', flexWrap: 'wrap' }}>
                <button
                  type="button"
                  onClick={() => triggerAiAssistant('explain-code')}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', gap: '0.3rem' }}
                  title="Explain code logic and structure"
                >
                  <Bot size={13} />
                  <span>{t('problemDetail.explainCode', 'Explain Code')}</span>
                </button>
                <button
                  type="button"
                  onClick={() => triggerAiAssistant('explain-error')}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', gap: '0.3rem' }}
                  title="Diagnose runtime or compiler error"
                >
                  <AlertCircle size={13} />
                  <span>{t('problemDetail.explainError', 'Explain Error')}</span>
                </button>
                <button
                  type="button"
                  onClick={() => triggerAiAssistant('give-hint')}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', gap: '0.3rem' }}
                  title="Get conceptual guidance"
                >
                  <HelpCircle size={13} />
                  <span>{t('problemDetail.giveHint', 'Give Hint')}</span>
                </button>
                <button
                  type="button"
                  onClick={() => triggerAiAssistant('improve-code')}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', gap: '0.3rem' }}
                  title="Improve efficiency and readability"
                >
                  <Sparkles size={13} />
                  <span>{t('problemDetail.improveCode', 'Improve Code')}</span>
                </button>
                <button
                  type="button"
                  onClick={() => triggerAiAssistant('chat-problem')}
                  className="btn btn-outline btn-sm"
                  style={{ fontSize: '0.75rem', padding: '0.2rem 0.55rem', gap: '0.3rem' }}
                  title="Discuss problem nuances and approach"
                >
                  <Bot size={13} />
                  <span>{t('problemDetail.chatProblem', 'Chat about Problem')}</span>
                </button>
              </div>
            </div>

            <div className="editor-actions">
              <button onClick={handleRun} className="btn btn-outline" disabled={busy}>
                <Play size={16} />
                <span>{running ? t('editor.running', 'Running...') : t('common.run', 'Run')}</span>
              </button>
              <button onClick={handleSubmitCode} className="btn btn-primary" disabled={busy}>
                <Send size={16} />
                <span>{submitting ? t('editor.evaluating', 'Evaluating...') : t('common.submit', 'Submit')}</span>
              </button>
            </div>
          </div>

          {/* Detailed Test Results Panel */}
          {activeResult ? (
            <div className="card" style={{ borderLeft: `4px solid ${statusInfo.color}` }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <StatusIcon size={22} color={statusInfo.color} />
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: statusInfo.color }}>
                    {statusInfo.label}
                  </h3>
                </div>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', color: 'var(--text-subtle)' }}>
                  {isSubmitResult ? t('problemDetail.gradedSubmission', 'Graded Submission') : t('problemDetail.sampleCases', 'Run (Sample Cases)')}
                </span>
              </div>

              <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: activeResult.results?.length ? '1rem' : 0 }}>
                <span>
                  {t('problemDetail.passedTestCases', 'Passed {{passed}} / {{total}} test cases', { passed: activeResult.passedTestCases, total: activeResult.totalTestCases })}
                </span>
                {typeof activeResult.executionTime === 'number' && (
                  <span>• {t('problemDetail.executionTime', 'Execution Time')}: <strong>{activeResult.executionTime}ms</strong></span>
                )}
              </div>

              {activeResult.errorMessage && (
                <pre className="error-output" style={{ marginBottom: '1rem' }}>{activeResult.errorMessage}</pre>
              )}

              {activeResult.results && activeResult.results.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {activeResult.results.map((r, idx) => {
                    const rStyle = STATUS_STYLES[r.status] || UNKNOWN_STATUS_STYLE;
                    const RIcon = rStyle.icon;
                    const caseNum = r.testCaseNumber || (idx + 1);

                    return (
                      <div key={idx} className="testcase-result" style={{ borderColor: rStyle.color + '44' }}>
                        <div className="testcase-result-header">
                          <RIcon size={16} color={rStyle.color} />
                          <span style={{ fontWeight: 700, color: rStyle.color, fontSize: '0.875rem' }}>
                            {t('problemDetail.testCase', 'Test Case')} #{caseNum} — {rStyle.label}
                          </span>
                          <div style={{ marginLeft: 'auto', display: 'flex', gap: '0.75rem', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>
                            {typeof r.executionTimeMs === 'number' && (
                              <span>{r.executionTimeMs}ms</span>
                            )}
                            {r.memoryUsed && (
                              <span>{r.memoryUsed} KB</span>
                            )}
                          </div>
                        </div>

                        {r.hidden ? (
                          <p style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', margin: 0 }}>
                            {t('problemDetail.privateTestCase', 'Private test case — inputs and outputs withheld for grading integrity.')}
                          </p>
                        ) : (
                          <div style={{ fontSize: '0.85rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                            <div><strong>{t('problemDetail.input', 'Input')}:</strong> <code>{r.input}</code></div>
                            <div><strong>{t('problemDetail.expected', 'Expected')}:</strong> <code style={{ color: 'var(--success)' }}>{r.expectedOutput}</code></div>
                            <div>
                              <strong>{t('problemDetail.output', 'Output')}:</strong>{' '}
                              <code style={{ color: r.status === 'PASSED' || r.status === 'ACCEPTED' ? 'var(--success)' : 'var(--danger)' }}>
                                {r.actualOutput ?? '(none)'}
                              </code>
                            </div>

                            {r.comparisonResult && r.status !== 'PASSED' && r.status !== 'ACCEPTED' && (
                              <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', fontStyle: 'italic', marginTop: '0.2rem' }}>
                                Diff: {r.comparisonResult}
                              </div>
                            )}

                            {r.errorMessage && (
                              <pre className="error-output" style={{ marginTop: '0.35rem' }}>{r.errorMessage}</pre>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          ) : (
            <div className="card">
              <div className="empty-state">
                <Play size={32} />
                <p>{t('problemDetail.runCodePrompt', 'Run your code to see test results.')}</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProblemDetail;
