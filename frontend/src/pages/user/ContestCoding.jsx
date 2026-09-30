import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Editor from '@monaco-editor/react';
import contestService from '../../services/contestService';
import problemService from '../../services/problemService';
import ContestProctoringCheck from '../../components/common/ContestProctoringCheck';
import { useTheme } from '../../context/ThemeContext';
import { getContestTimeState, getMillisRemaining, formatCountdown, formatContestDateTime } from '../../utils/contestTime';
import {
  LANGUAGE_TEMPLATES,
  getStarterCode,
  MONACO_LANGUAGE_MAP,
  STATUS_STYLES,
  UNKNOWN_STATUS_STYLE,
} from './ProblemDetail';
import {
  ArrowLeft,
  AlertCircle,
  Play,
  Send,
  Clock,
  Flag,
  Maximize2,
  ShieldAlert,
  Camera,
  Mic,
} from 'lucide-react';

/**
 * Student contest participation screen (Task 7), extended in Task 8 with real
 * contest scoring: starts/refreshes the participant's ContestAttempt and shows a
 * compact Score/Solved readout backed entirely by the server - never computed here.
 *
 * Deliberately reuses, rather than reimplements:
 *  - contestService (contest details + registration status + attempt/leaderboard)
 *  - problemService (problem details, saved-code, run, submit) - the exact same
 *    APIs/backend evaluation mechanism ProblemDetail.jsx uses for normal /problems
 *    solving. There is no second code-execution or submission system here.
 *  - The Monaco editor configuration/starter-code helpers exported from ProblemDetail.jsx.
 *
 * The countdown timer is UI-side protection only (Task 7 Step 6) - the actual gate is
 * server-side too (Task 8 Step 8): the backend independently checks the contest's real
 * start/end time before accepting a contest submission, so a tampered client-side clock
 * can no longer bypass it.
 *
 * Task 9 adds basic browser-based exam-security monitoring: requesting fullscreen on
 * entry, and recording a violation (against the server-side ContestAttempt, never just
 * local state) if the participant exits fullscreen or switches away from the tab while
 * the contest is active. This is honestly limited - browser APIs cannot prove someone is
 * cheating, and none of this is a guaranteed anti-cheat mechanism (see README).
 */
const ContestCoding = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { theme } = useTheme();
  const { t } = useTranslation();

  const [contest, setContest] = useState(null);
  const [registered, setRegistered] = useState(null); // null = not checked yet
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  // Contest camera + microphone pre-entry proctoring verification
  const [proctoringPassed, setProctoringPassed] = useState(false);
  const [proctoringStream, setProctoringStream] = useState(null);
  const miniVideoRef = useRef(null);

  const [now, setNow] = useState(new Date());

  // Server-derived score/solved/submissionCount for the current user's ContestAttempt
  // (Task 8) - never calculated locally, always exactly what the backend returns.
  const [attempt, setAttempt] = useState(null);

  const [selectedProblemId, setSelectedProblemId] = useState(null);
  const [problemDetails, setProblemDetails] = useState({}); // problemId -> full Problem
  const [problemLoading, setProblemLoading] = useState(false);
  const [problemError, setProblemError] = useState('');

  // problemId -> { language, code } - Step 13's minimum requirement, so switching
  // problems never mixes up which code belongs to which problem.
  const [codeByProblem, setCodeByProblem] = useState({});
  const [running, setRunning] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  // problemId -> { run, submit } result
  const [resultsByProblem, setResultsByProblem] = useState({});
  const [actionError, setActionError] = useState('');

  // ---- Basic exam-security monitoring (Task 9) ----
  const [fullscreenActive, setFullscreenActive] = useState(() => !!document.fullscreenElement);
  const [securityWarning, setSecurityWarning] = useState('');
  const wasFullscreenRef = useRef(!!document.fullscreenElement);
  const violationInFlightRef = useRef(false);

  const loadRequestIdRef = useRef(0);

  // ---- Load contest + registration status + attempt ----
  const loadContest = useCallback(async () => {
    setLoading(true);
    setLoadError('');
    try {
      const [contestData, registrationData] = await Promise.all([
        contestService.getContestDetails(id),
        contestService.getRegistrationStatus(id),
      ]);
      setContest(contestData);
      setRegistered(!!registrationData.registered);
      if (registrationData.registered) {
        try {
          const attemptData = await contestService.startOrGetAttempt(id);
          setAttempt(attemptData);
        } catch (attemptErr) {
          console.error('Failed to start/refresh contest attempt:', attemptErr);
        }
      }
    } catch (err) {
      setLoadError('Unable to load contest.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadContest();
  }, [loadContest]);

  // ---- Refresh attempt state (score / violations / status) ----
  const refreshAttempt = useCallback(async () => {
    try {
      const data = await contestService.startOrGetAttempt(id);
      setAttempt(data);
    } catch (err) {
      console.error('Failed to start/refresh contest attempt:', err);
    }
  }, [id]);

  // ---- Timer: one interval, ticking once per second, cleaned up on unmount ----
  useEffect(() => {
    const intervalId = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(intervalId);
  }, []);

  const timeState = contest ? getContestTimeState(contest, now) : 'not-started';
  const millisRemaining = contest ? getMillisRemaining(contest, now) : 0;

  // Once security violations reach the threshold or status is completed with violations,
  // the attempt is permanently locked and cannot be resumed.
  const isTerminatedBySecurity = Boolean(
    (attempt?.securityViolationCount != null &&
      attempt?.maxViolations != null &&
      attempt.securityViolationCount >= attempt.maxViolations) ||
    (attempt?.status === 'COMPLETED' &&
      (attempt?.securityViolationCount || 0) >= (attempt?.maxViolations || 3))
  );
  const contestUsable = timeState === 'ongoing' && !isTerminatedBySecurity;

  // Cleanup proctoring media and exit fullscreen if attempt terminated
  useEffect(() => {
    if (isTerminatedBySecurity) {
      if (document.fullscreenElement) {
        document.exitFullscreen?.().catch(() => {});
      }
      if (proctoringStream) {
        proctoringStream.getTracks().forEach((t) => t.stop());
        setProctoringStream(null);
      }
    }
  }, [isTerminatedBySecurity, proctoringStream]);

  // ---- Report one security violation to the backend (Step 5) ----
  const recordViolation = useCallback(async (message, violationType = null) => {
    if (violationInFlightRef.current) return;
    if (attempt?.status === 'COMPLETED' || isTerminatedBySecurity) return; // already ended
    violationInFlightRef.current = true;
    try {
      const result = await contestService.recordSecurityViolation(id, violationType);
      const isTerminatedNow = Boolean(
        result.terminated ||
        (result.violationCount != null && result.maxViolations != null && result.violationCount >= result.maxViolations)
      );

      setAttempt((prev) => (prev
        ? {
            ...prev,
            securityViolationCount: result.violationCount,
            maxViolations: result.maxViolations,
            status: isTerminatedNow ? 'COMPLETED' : result.status,
          }
        : prev));

      if (isTerminatedNow) {
        if (document.fullscreenElement) {
          document.exitFullscreen?.().catch(() => {});
        }
        if (proctoringStream) {
          proctoringStream.getTracks().forEach((t) => t.stop());
          setProctoringStream(null);
        }
        setSecurityWarning(
          'Maximum fullscreen/security violations reached. Your contest attempt has been permanently terminated.'
        );
      } else {
        setSecurityWarning(message);
      }
    } catch (err) {
      console.error('Failed to record security violation:', err);
    } finally {
      violationInFlightRef.current = false;
    }
  }, [id, attempt, isTerminatedBySecurity, proctoringStream]);

  const enterFullscreen = useCallback(() => {
    // Called from a button's onClick (Step 1) - always a genuine user gesture, so this
    // works even if the earlier best-effort attempt from ContestDetail.jsx was denied.
    document.documentElement.requestFullscreen?.().catch(() => {
      setSecurityWarning('Fullscreen is required for the contest. Please enable fullscreen to continue.');
    });
  }, []);

  // ---- Fullscreen detection (Step 2/3) - one listener, cleaned up on unmount. ----
  useEffect(() => {
    const handleFullscreenChange = () => {
      const isFullscreenNow = !!document.fullscreenElement;
      setFullscreenActive(isFullscreenNow);
      // Only a true fullscreen -> not-fullscreen transition counts as a violation, and
      // only while the contest is actually usable - guards against firing on the
      // initial mount or after the contest/attempt has already ended.
      if (wasFullscreenRef.current && !isFullscreenNow && contestUsable) {
        recordViolation('Warning: You exited fullscreen. This action has been recorded.');
      }
      wasFullscreenRef.current = isFullscreenNow;
    };
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => document.removeEventListener('fullscreenchange', handleFullscreenChange);
  }, [contestUsable, recordViolation]);

  // ---- Tab/visibility detection (Step 11) - basic monitoring only, not proof of
  // cheating. One listener, cleaned up on unmount. ----
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'hidden' && contestUsable) {
        recordViolation('Warning: You switched away from the contest tab. This action has been recorded.');
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [contestUsable, recordViolation]);

  // Connect media stream to floating mini preview once proctoring passes
  useEffect(() => {
    if (miniVideoRef.current && proctoringStream) {
      miniVideoRef.current.srcObject = proctoringStream;
    }
  }, [proctoringStream, proctoringPassed]);

  // Monitor camera and microphone disconnection during active contest
  useEffect(() => {
    if (!proctoringStream || !contestUsable) return;
    const tracks = proctoringStream.getTracks();
    const handleTrackEnded = (e) => {
      const kind = e.target?.kind || 'device';
      const vType = kind === 'video' ? 'CAMERA_DISABLED' : 'MICROPHONE_DISABLED';
      recordViolation(`Security Warning: Your ${kind} was disconnected or disabled. This action has been recorded.`, vType);
    };
    tracks.forEach((t) => t.addEventListener('ended', handleTrackEnded));
    return () => {
      tracks.forEach((t) => t.removeEventListener('ended', handleTrackEnded));
    };
  }, [proctoringStream, contestUsable, recordViolation]);

  // Clean up media tracks on unmount
  useEffect(() => {
    return () => {
      if (proctoringStream) {
        proctoringStream.getTracks().forEach((t) => t.stop());
      }
    };
  }, [proctoringStream]);

  // ---- Default problem selection once contest loads ----
  useEffect(() => {
    if (contest?.problems?.length && selectedProblemId == null) {
      const sorted = [...contest.problems].sort((a, b) => a.displayOrder - b.displayOrder);
      setSelectedProblemId(sorted[0].problemId);
    }
  }, [contest, selectedProblemId]);

  // ---- Load the selected problem's full details + saved/starter code ----
  useEffect(() => {
    if (selectedProblemId == null || !registered) return;
    if (problemDetails[selectedProblemId]) return; // already loaded this session

    const myRequestId = ++loadRequestIdRef.current;
    const loadProblem = async () => {
      setProblemLoading(true);
      setProblemError('');
      try {
        const data = await problemService.getProblemById(selectedProblemId);
        if (myRequestId !== loadRequestIdRef.current) return;
        setProblemDetails((prev) => ({ ...prev, [selectedProblemId]: data }));

        let initialCode = getStarterCode(data, 'JAVA');
        try {
          const saved = await problemService.getSavedCode(selectedProblemId, 'JAVA');
          if (myRequestId !== loadRequestIdRef.current) return;
          if (saved?.code) initialCode = saved.code;
        } catch (savedErr) {
          console.error('Failed to load saved code for contest problem:', savedErr);
        }

        setCodeByProblem((prev) => ({
          ...prev,
          [selectedProblemId]: prev[selectedProblemId] || { language: 'JAVA', code: initialCode },
        }));
      } catch (err) {
        if (myRequestId === loadRequestIdRef.current) setProblemError('Unable to load problem.');
      } finally {
        if (myRequestId === loadRequestIdRef.current) setProblemLoading(false);
      }
    };

    loadProblem();
  }, [selectedProblemId, registered, problemDetails]);

  const handleSelectProblem = (problemId) => {
    if (problemId === selectedProblemId) return;
    setActionError('');
    setSelectedProblemId(problemId);
  };

  const handleLanguageChange = async (newLanguage) => {
    const problem = problemDetails[selectedProblemId];
    setCodeByProblem((prev) => ({
      ...prev,
      [selectedProblemId]: { ...prev[selectedProblemId], language: newLanguage },
    }));

    let nextCode = getStarterCode(problem, newLanguage);
    try {
      const saved = await problemService.getSavedCode(selectedProblemId, newLanguage);
      if (saved?.code) nextCode = saved.code;
    } catch (err) {
      console.error('Failed to load saved code for language change:', err);
    }
    setCodeByProblem((prev) => ({
      ...prev,
      [selectedProblemId]: { ...prev[selectedProblemId], language: newLanguage, code: nextCode },
    }));
  };

  const handleCodeChange = (value) => {
    setCodeByProblem((prev) => ({
      ...prev,
      [selectedProblemId]: { ...prev[selectedProblemId], code: value ?? '' },
    }));
  };

  const busy = running || submitting;

  const handleRun = async () => {
    const current = codeByProblem[selectedProblemId];
    if (!current || !contestUsable) return;
    setActionError('');
    setRunning(true);
    try {
      const res = await problemService.runCode({
        problemId: selectedProblemId,
        language: current.language,
        code: current.code,
      });
      setResultsByProblem((prev) => ({
        ...prev,
        [selectedProblemId]: { ...prev[selectedProblemId], run: res, submit: null },
      }));
    } catch (err) {
      setActionError(err.response?.data?.message || 'Run failed. Please try again.');
    } finally {
      setRunning(false);
    }
  };

  const handleSubmit = async () => {
    const current = codeByProblem[selectedProblemId];
    if (!current || !contestUsable) return;
    setActionError('');
    setSubmitting(true);
    try {
      const res = await problemService.submitCode({
        problemId: selectedProblemId,
        language: current.language,
        code: current.code,
        contestId: id, // Task 8: scores this submission against the contest server-side.
      });
      setResultsByProblem((prev) => ({
        ...prev,
        [selectedProblemId]: { ...prev[selectedProblemId], run: null, submit: res },
      }));
      // Score/solved/submissionCount are server-computed - refresh rather than
      // calculating anything locally.
      refreshAttempt();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Submission failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Render states ----
  if (loading) {
    return <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading contest...</div>;
  }

  if (loadError || !contest) {
    return (
      <div className="card">
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{loadError || 'Unable to load contest.'}</span>
        </div>
        <Link to={`/contests/${id}`} className="btn btn-outline btn-sm">
          <ArrowLeft size={14} />
          <span>{t('contests.backToContest', 'Back to Contest')}</span>
        </Link>
      </div>
    );
  }

  if (!registered) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2.5rem' }}>
        <AlertCircle size={32} style={{ margin: '0 auto 1rem', color: 'var(--warning)' }} />
        <p style={{ marginBottom: '1.25rem' }}>{t('contests.registrationClosed', 'You must register for this contest before entering.')}</p>
        <Link to={`/contests/${id}`} className="btn btn-primary btn-sm">
          <ArrowLeft size={14} />
          <span>{t('contests.backToContest', 'Back to Contest Details')}</span>
        </Link>
      </div>
    );
  }

  if (isTerminatedBySecurity) {
    return (
      <div className="card" style={{ maxWidth: '680px', margin: '3rem auto', textAlign: 'center', padding: '3rem 2rem', border: '1px solid rgba(239, 68, 68, 0.35)', borderRadius: '16px', boxShadow: '0 10px 30px rgba(0,0,0,0.15)' }}>
        <div style={{ width: '72px', height: '72px', borderRadius: '50%', backgroundColor: 'rgba(239, 68, 68, 0.12)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.5rem', color: 'var(--danger)' }}>
          <ShieldAlert size={42} />
        </div>
        <span style={{ backgroundColor: 'rgba(239, 68, 68, 0.15)', color: 'var(--danger)', fontWeight: 800, padding: '0.4rem 1rem', borderRadius: '9999px', fontSize: '0.78rem', letterSpacing: '0.06em', textTransform: 'uppercase' }}>
          Security Violations Exceeded
        </span>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, marginTop: '1rem', marginBottom: '0.75rem', color: 'var(--text-color)' }}>
          Contest Attempt Terminated
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', lineHeight: 1.6, maxWidth: '520px', margin: '0 auto 1.75rem' }}>
          You have exceeded the maximum allowed security violations ({attempt?.securityViolationCount || 0}/{attempt?.maxViolations || 3}) by exiting fullscreen mode or switching tabs during the contest.
          Your attempt has been <strong>automatically and permanently terminated</strong>. You cannot open or resume this test again.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', margin: '0 auto 2rem', maxWidth: '480px', backgroundColor: 'var(--bg-secondary, rgba(255,255,255,0.03))', padding: '1.25rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
          <div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase', fontWeight: 600 }}>Violations</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--danger)', marginTop: '0.25rem' }}>
              {attempt?.securityViolationCount || 0} / {attempt?.maxViolations || 3}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase', fontWeight: 600 }}>Final Score</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.25rem' }}>
              {attempt?.score ?? 0}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', textTransform: 'uppercase', fontWeight: 600 }}>Problems Solved</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-color)', marginTop: '0.25rem' }}>
              {attempt?.problemsSolved ?? 0} / {contest?.problems?.length || 0}
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <Link to={`/contests/${id}`} className="btn btn-primary btn-sm" style={{ padding: '0.6rem 1.25rem' }}>
            <ArrowLeft size={16} />
            <span>View Contest & Leaderboard</span>
          </Link>
          <Link to="/contests" className="btn btn-outline btn-sm" style={{ padding: '0.6rem 1.25rem' }}>
            <span>Back to Contests</span>
          </Link>
        </div>
      </div>
    );
  }

  if (!proctoringPassed) {
    return (
      <ContestProctoringCheck
        contestTitle={contest.title}
        onPassed={(stream) => {
          setProctoringStream(stream);
          setProctoringPassed(true);
        }}
        onCancel={() => navigate(`/contests/${id}`)}
      />
    );
  }

  const sortedProblems = [...(contest.problems || [])].sort((a, b) => a.displayOrder - b.displayOrder);
  const currentProblem = selectedProblemId != null ? problemDetails[selectedProblemId] : null;
  const currentCode = selectedProblemId != null ? codeByProblem[selectedProblemId] : null;
  const currentResults = selectedProblemId != null ? resultsByProblem[selectedProblemId] : null;
  const activeResult = currentResults?.submit || currentResults?.run;
  const isSubmitResult = !!currentResults?.submit;
  const statusInfo = activeResult ? (STATUS_STYLES[activeResult.status] || UNKNOWN_STATUS_STYLE) : null;
  const StatusIcon = statusInfo?.icon;

  return (
    <div>
      {/* Header: contest name + live countdown */}
      <div className="card contest-timer-bar">
        <div>
          <Link to={`/contests/${id}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
            <ArrowLeft size={14} /> {t('contests.backToContest', 'Back to Contest')}
          </Link>
          <h1 style={{ fontSize: '1.35rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Flag size={20} color="var(--primary)" /> {contest.title}
          </h1>
          {attempt && (
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.3rem', fontWeight: 600 }}>
              {t('contests.score', 'Score')}: <strong style={{ color: 'var(--primary)' }}>{attempt.score}</strong>
              {'  '}·{'  '}{t('contests.solved', 'Solved')}: <strong>{attempt.problemsSolved}/{contest.problems?.length || 0}</strong>
              {typeof attempt.maxViolations === 'number' && (
                <>
                  {'  '}·{'  '}{t('contests.securityWarnings', 'Security warnings')}:{' '}
                  <strong style={{ color: (attempt.securityViolationCount || 0) > 0 ? 'var(--danger)' : 'inherit' }}>
                    {attempt.securityViolationCount || 0}/{attempt.maxViolations}
                  </strong>
                </>
              )}
            </div>
          )}
        </div>

        <div style={{ textAlign: 'right' }}>
          {timeState === 'not-started' && (
            <span style={{ color: 'var(--text-subtle)', fontWeight: 600 }}>{t('contests.notStarted', 'Contest has not started')}</span>
          )}
          {timeState === 'ongoing' && (
            <>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{t('contests.timeRemaining', 'Time Remaining')}</div>
              <div className="contest-timer-clock" style={{ color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '0.4rem', justifyContent: 'flex-end' }}>
                <Clock size={18} />
                {formatCountdown(millisRemaining)}
              </div>
            </>
          )}
          {timeState === 'ended' && (
            <span style={{ color: 'var(--text-subtle)', fontWeight: 700 }}>{t('contests.ended', 'Contest Ended')}</span>
          )}
        </div>
      </div>

      {!contestUsable && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
          <AlertCircle size={16} />
          <span>
            {terminatedBySecurity
              ? 'Contest attempt ended — maximum fullscreen/security violations reached.'
              : timeState === 'not-started'
              ? `Coding and submission will be enabled once the contest starts at ${contest.startTime ? formatContestDateTime(contest.startTime) : 'the scheduled time'}.`
              : 'This contest has ended. Coding and submission are disabled.'}
          </span>
        </div>
      )}

      {contestUsable && !fullscreenActive && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertCircle size={16} />
            {t('contests.fullscreenRequired', 'Fullscreen is required for the contest. Please enable fullscreen to continue.')}
          </span>
          <button onClick={enterFullscreen} className="btn btn-outline btn-sm">
            <Maximize2 size={14} />
            <span>{t('contests.enableFullscreen', 'Enable Fullscreen')}</span>
          </button>
        </div>
      )}

      {securityWarning && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
          <ShieldAlert size={16} />
          <span>{securityWarning}</span>
        </div>
      )}

      {actionError && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
          <AlertCircle size={16} />
          <span>{actionError}</span>
        </div>
      )}

      <div className="contest-coding-layout">
        {/* LEFT: Problem list */}
        <div className="contest-problem-list">
          <div className="card">
            <div className="panel-header">
              <span className="panel-title">{t('contests.problems', 'Problems')} ({sortedProblems.length})</span>
            </div>
            {sortedProblems.length === 0 ? (
              <div className="empty-state">
                <p>{t('contests.noProblems', 'No problems have been added to this contest yet.')}</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {sortedProblems.map((p) => (
                  <button
                    key={p.problemId}
                    onClick={() => handleSelectProblem(p.problemId)}
                    className={`contest-problem-item${p.problemId === selectedProblemId ? ' active' : ''}`}
                  >
                    <span style={{ fontWeight: 700, color: 'var(--text-muted)' }}>{p.displayOrder}.</span>
                    <span style={{ flex: 1, fontWeight: 600 }}>{p.problemTitle}</span>
                    {p.difficulty && (
                      <span className={`badge badge-${p.difficulty.toLowerCase()}`}>{p.difficulty}</span>
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* MIDDLE: Problem description */}
        <div className="contest-coding-description">
          {problemError ? (
            <div className="alert alert-error"><AlertCircle size={16} /><span>{problemError}</span></div>
          ) : problemLoading || !currentProblem ? (
            <div className="card" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
              {t('problems.loading', 'Loading problem...')}
            </div>
          ) : (
            <>
              <div className="card">
                <div className="panel-header">
                  <span className="panel-title">{currentProblem.title}</span>
                  {currentProblem.difficulty && (
                    <span className={`badge badge-${currentProblem.difficulty.toLowerCase()}`}>
                      {currentProblem.difficulty}
                    </span>
                  )}
                </div>
                <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.65, color: 'var(--text-muted)', fontSize: '0.95rem' }}>
                  {currentProblem.description}
                </div>
              </div>

              {currentProblem.constraints && currentProblem.constraints.trim() !== '' && (
                <div className="card">
                  <div className="panel-header">
                    <span className="panel-title">{t('problemDetail.constraints', 'Constraints')}</span>
                  </div>
                  <pre style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6, color: 'var(--text-muted)', fontSize: '0.9rem', fontFamily: 'JetBrains Mono, monospace', margin: 0 }}>
                    {currentProblem.constraints}
                  </pre>
                </div>
              )}

              <div className="card">
                <div className="panel-header">
                  <span className="panel-title">{t('problemDetail.examplesAndSamples', 'Examples')}</span>
                </div>
                {currentProblem.testCases && currentProblem.testCases.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {currentProblem.testCases.map((tc, idx) => (
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
        </div>

        {/* RIGHT: Editor + Run/Submit + Results */}
        <div className="contest-coding-right">
          <div className="card editor-card">
            <div className="editor-toolbar">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)' }}>{t('submissions.language', 'Language')}</label>
                <select
                  value={currentCode?.language || 'JAVA'}
                  onChange={(e) => handleLanguageChange(e.target.value)}
                  className="form-control"
                  style={{ padding: '0.4rem 0.75rem', fontSize: '0.85rem', width: 'auto' }}
                  disabled={!currentProblem}
                >
                  <option value="JAVA">Java</option>
                  <option value="PYTHON">Python</option>
                  <option value="CPP">C++</option>
                  <option value="JAVASCRIPT">JavaScript</option>
                </select>
              </div>
            </div>

            <div className="monaco-editor-wrapper">
              <Editor
                height="100%"
                language={MONACO_LANGUAGE_MAP[currentCode?.language] || 'plaintext'}
                value={currentCode?.code || ''}
                onChange={handleCodeChange}
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
                  readOnly: !contestUsable,
                }}
                loading={<div style={{ padding: '1rem', color: 'var(--text-muted)' }}>Loading editor...</div>}
              />
            </div>

            <div className="editor-actions">
              <button onClick={handleRun} className="btn btn-outline" disabled={busy || !contestUsable || !currentProblem}>
                <Play size={16} />
                <span>{running ? t('editor.running', 'Running...') : t('common.run', 'Run')}</span>
              </button>
              <button onClick={handleSubmit} className="btn btn-primary" disabled={busy || !contestUsable || !currentProblem}>
                <Send size={16} />
                <span>{submitting ? t('editor.evaluating', 'Evaluating...') : t('common.submit', 'Submit')}</span>
              </button>
            </div>
          </div>

          {activeResult ? (
            <div className="card" style={{ borderLeft: `4px solid ${statusInfo.color}` }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <StatusIcon size={22} color={statusInfo.color} />
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: statusInfo.color }}>{statusInfo.label}</h3>
                </div>
                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', color: 'var(--text-subtle)' }}>
                  {isSubmitResult ? t('problemDetail.gradedSubmission', 'Graded Submission') : t('problemDetail.sampleCases', 'Run (Sample Cases)')}
                </span>
              </div>

              <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', fontSize: '0.875rem', color: 'var(--text-muted)', marginBottom: activeResult.results?.length ? '1rem' : 0 }}>
                <span>{t('problemDetail.passedTestCases', 'Passed {{passed}} / {{total}} test cases', { passed: activeResult.passedTestCases, total: activeResult.totalTestCases })}</span>
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
                            {r.errorMessage && <pre className="error-output" style={{ marginTop: '0.35rem' }}>{r.errorMessage}</pre>}
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
                <p>{t('submissions.noSubmissions', 'No submission yet.')}</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Floating Proctoring Monitor Widget */}
      <aside
        aria-label="Proctoring Live Monitor"
        style={{
          position: 'fixed',
          bottom: '1.25rem',
          right: '1.25rem',
          zIndex: 9999,
          background: 'rgba(15, 23, 42, 0.94)',
          backdropFilter: 'blur(8px)',
          border: '1px solid rgba(51, 65, 85, 0.8)',
          borderRadius: '0.75rem',
          padding: '0.45rem',
          boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.6)',
          display: 'flex',
          flexDirection: 'column',
          gap: '0.35rem',
          width: '135px',
        }}
      >
        <div
          style={{
            position: 'relative',
            width: '100%',
            height: '80px',
            background: '#020617',
            borderRadius: '0.5rem',
            overflow: 'hidden',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <video
            ref={miniVideoRef}
            autoPlay
            playsInline
            muted
            style={{ width: '100%', height: '100%', objectFit: 'cover', transform: 'scaleX(-1)' }}
          />
          <div
            style={{
              position: 'absolute',
              top: '4px',
              left: '4px',
              background: 'rgba(0,0,0,0.7)',
              borderRadius: '4px',
              padding: '1px 5px',
              fontSize: '9px',
              fontWeight: 700,
              color: '#10b981',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
            }}
          >
            <span style={{ width: '5px', height: '5px', borderRadius: '50%', background: '#10b981', display: 'inline-block' }} />
            LIVE
          </div>
        </div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: '10px',
            color: '#94a3b8',
            padding: '0 2px',
          }}
        >
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#10b981', fontWeight: 600 }}>
            <Camera size={12} /> <Mic size={12} />
          </span>
          <span style={{ fontSize: '9px', color: '#94a3b8', fontWeight: 500 }}>Active</span>
        </div>
      </aside>
    </div>
  );
};

export default ContestCoding;
