import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Editor from '@monaco-editor/react';
import assessmentService from '../../services/assessmentService';
import supportFeedbackService from '../../services/supportFeedbackService';
import {
  ArrowLeft,
  Clock,
  Send,
  AlertCircle,
  CheckCircle2,
  XCircle,
  RefreshCw,
  Camera,
  CameraOff,
  Mic,
  MicOff,
  Shield,
  ShieldCheck,
  AlertTriangle,
  Minimize2,
  Maximize2,
  Play,
  Code2,
  Terminal,
  MessageSquare,
  Star,
  X,
} from 'lucide-react';

const SUPPORTED_LANGUAGES = ['java', 'python', 'cpp', 'javascript', 'c'];
const MONACO_LANG_MAP = { java: 'java', python: 'python', cpp: 'cpp', javascript: 'javascript', c: 'c' };
const STARTER_CODE = {
  java: `public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n}`,
  python: `# Write your solution here\n`,
  cpp: `#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    // Write your solution here\n    return 0;\n}`,
  javascript: `// Write your solution here\n`,
  c: `#include <stdio.h>\n\nint main() {\n    // Write your solution here\n    return 0;\n}`,
};

/** Formats a millisecond duration as MM:SS, or HH:MM:SS once past an hour. */
const formatCountdown = (millis) => {
  const totalSeconds = Math.max(0, Math.floor(millis / 1000));
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const pad = (n) => String(n).padStart(2, '0');
  return hours > 0 ? `${pad(hours)}:${pad(minutes)}:${pad(seconds)}` : `${pad(minutes)}:${pad(seconds)}`;
};

/**
 * Student assessment attempt screen with camera & microphone proctoring,
 * MCQ + Programming question support, violation recording.
 */
const AssessmentAttempt = () => {
  const { id } = useParams(); // assessmentId
  const { t } = useTranslation();

  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [attempt, setAttempt] = useState(null); // AssessmentAttemptDto

  // Proctoring States
  const [proctoringAgreed, setProctoringAgreed] = useState(false);
  const [cameraActive, setCameraActive] = useState(false);
  const [micActive, setMicActive] = useState(false);
  const [mediaError, setMediaError] = useState('');
  const [proctoringWarning, setProctoringWarning] = useState('');
  const [pipMinimized, setPipMinimized] = useState(false);
  const [audioLevel, setAudioLevel] = useState(0);

  const mediaStreamRef = useRef(null);
  const previewVideoRef = useRef(null);
  const liveVideoRef = useRef(null);
  const audioContextRef = useRef(null);
  const animFrameRef = useRef(null);

  const [now, setNow] = useState(new Date());
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectingOptionId, setSelectingOptionId] = useState(null);
  const [actionError, setActionError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Programming question states (per-question map by questionId)
  const [codeByQuestion, setCodeByQuestion] = useState({});
  const [langByQuestion, setLangByQuestion] = useState({});
  const [runResult, setRunResult] = useState(null); // { stdout, stderr, exitCode, timedOut, compilationError, testResults }
  const [running, setRunning] = useState(false);
  const [submittingCode, setSubmittingCode] = useState(false);

  const autoRefreshedOnExpiryRef = useRef(false);

  // ---- Fullscreen exit & tab switch violation detection ----
  useEffect(() => {
    if (!proctoringAgreed || !attempt) return;
    const attemptId = attempt.id;

    const handleVisibilityChange = () => {
      if (document.hidden) {
        assessmentService.recordViolation(id, attemptId, { violationType: 'TAB_SWITCH' });
        setProctoringWarning('Tab switch detected! This is recorded as a violation.');
      }
    };

    const handleFullscreenChange = () => {
      if (!document.fullscreenElement) {
        assessmentService.recordViolation(id, attemptId, { violationType: 'FULLSCREEN_EXIT' });
        setProctoringWarning('Fullscreen exit detected! This is recorded as a violation.');
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
    };
  }, [proctoringAgreed, attempt, id]);

  const stopMediaStream = useCallback(() => {
    if (animFrameRef.current) {
      cancelAnimationFrame(animFrameRef.current);
      animFrameRef.current = null;
    }
    if (audioContextRef.current) {
      try { audioContextRef.current.close(); } catch {}
      audioContextRef.current = null;
    }
    if (mediaStreamRef.current) {
      mediaStreamRef.current.getTracks().forEach((track) => { try { track.stop(); } catch {} });
      mediaStreamRef.current = null;
    }
    setCameraActive(false);
    setMicActive(false);
  }, []);

  const requestMediaPermissions = async () => {
    setMediaError('');
    setProctoringWarning('');
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 } },
        audio: true,
      });
      mediaStreamRef.current = stream;

      const videoTracks = stream.getVideoTracks();
      const audioTracks = stream.getAudioTracks();
      const hasVideo = videoTracks.length > 0 && videoTracks[0].readyState === 'live';
      const hasAudio = audioTracks.length > 0 && audioTracks[0].readyState === 'live';
      setCameraActive(hasVideo);
      setMicActive(hasAudio);

      videoTracks.forEach((t) => { t.onended = () => { setCameraActive(false); setProctoringWarning('Camera was disconnected or turned off!'); }; });
      audioTracks.forEach((t) => { t.onended = () => { setMicActive(false); setProctoringWarning('Microphone was disconnected or turned off!'); }; });

      if (previewVideoRef.current) previewVideoRef.current.srcObject = stream;
      if (liveVideoRef.current) liveVideoRef.current.srcObject = stream;

      try {
        const AudioContextClass = window.AudioContext || window.webkitAudioContext;
        if (AudioContextClass) {
          const audioCtx = new AudioContextClass();
          audioContextRef.current = audioCtx;
          const source = audioCtx.createMediaStreamSource(stream);
          const analyser = audioCtx.createAnalyser();
          analyser.fftSize = 256;
          source.connect(analyser);
          const dataArray = new Uint8Array(analyser.frequencyBinCount);
          const updateMeter = () => {
            if (!mediaStreamRef.current) return;
            analyser.getByteFrequencyData(dataArray);
            let sum = 0;
            for (let i = 0; i < dataArray.length; i++) sum += dataArray[i];
            setAudioLevel(Math.min(100, Math.round((sum / dataArray.length / 128) * 100)));
            animFrameRef.current = requestAnimationFrame(updateMeter);
          };
          updateMeter();
        }
      } catch (audioErr) { console.warn('Audio metering unavailable:', audioErr); }
    } catch (err) {
      console.error('Media permission error:', err);
      let msg = 'Unable to access camera and microphone. Please allow permissions in your browser.';
      if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        msg = 'Camera/Microphone permission was denied. Please click the lock/camera icon in your browser URL bar to allow access.';
      } else if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
        msg = 'No camera or microphone device found. Please connect a working webcam and microphone.';
      }
      setMediaError(msg);
      setCameraActive(false);
      setMicActive(false);
    }
  };

  useEffect(() => { return () => { stopMediaStream(); }; }, [stopMediaStream]);

  useEffect(() => {
    if (proctoringAgreed && liveVideoRef.current && mediaStreamRef.current) {
      liveVideoRef.current.srcObject = mediaStreamRef.current;
    }
  }, [proctoringAgreed, pipMinimized]);

  // ---- Start (or resume) the attempt on mount ----
  const startOrResume = useCallback(async () => {
    setLoading(true);
    setLoadError('');
    try {
      const data = await assessmentService.startAttempt(id);
      setAttempt(data);
      setCurrentIndex(0);
    } catch (err) {
      setLoadError(err.response?.data?.message || 'Unable to start this assessment.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { startOrResume(); }, [startOrResume]);

  // ---- Timer ----
  useEffect(() => {
    const interval = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(interval);
  }, []);

  const deadline = attempt?.deadlineAt ? new Date(attempt.deadlineAt) : null;
  const millisRemaining = deadline ? Math.max(0, deadline.getTime() - now.getTime()) : null;
  const timeIsUp = attempt?.status === 'IN_PROGRESS' && deadline != null && millisRemaining === 0;
  const isActive = attempt?.status === 'IN_PROGRESS' && !timeIsUp;

  const refreshAttempt = useCallback(async () => {
    if (!attempt) return;
    try {
      const data = await assessmentService.getAttempt(id, attempt.id);
      setAttempt(data);
    } catch (err) { console.error('Failed to refresh attempt:', err); }
  }, [id, attempt?.id]);

  useEffect(() => {
    if (timeIsUp && !autoRefreshedOnExpiryRef.current) {
      autoRefreshedOnExpiryRef.current = true;
      refreshAttempt();
    }
  }, [timeIsUp, refreshAttempt]);

  const questions = useMemo(
    () => (attempt?.questions || []).slice().sort((a, b) => a.orderIndex - b.orderIndex),
    [attempt?.questions]
  );
  const answersByQuestionId = useMemo(() => {
    const map = {};
    (attempt?.answers || []).forEach((a) => { map[a.questionId] = a; });
    return map;
  }, [attempt?.answers]);

  const currentQuestion = questions[currentIndex];
  const currentAnswer = currentQuestion ? answersByQuestionId[currentQuestion.id] : null;
  const answeredCount = questions.filter((q) => answersByQuestionId[q.id] || q.questionType === 'PROGRAMMING').length;

  // Helper: get code for current programming question
  const getCurrentCode = () => {
    if (!currentQuestion) return '';
    const qId = currentQuestion.id;
    const lang = langByQuestion[qId] || (currentQuestion.programmingQuestion?.supportedLanguages?.[0] ?? 'java');
    return codeByQuestion[`${qId}__${lang}`] || STARTER_CODE[lang] || '';
  };
  const getCurrentLang = () => {
    if (!currentQuestion) return 'java';
    const qId = currentQuestion.id;
    return langByQuestion[qId] || (currentQuestion.programmingQuestion?.supportedLanguages?.[0] ?? 'java');
  };

  const setCurrentCode = (code) => {
    if (!currentQuestion) return;
    const qId = currentQuestion.id;
    const lang = getCurrentLang();
    setCodeByQuestion((prev) => ({ ...prev, [`${qId}__${lang}`]: code }));
  };
  const setCurrentLang = (lang) => {
    if (!currentQuestion) return;
    const qId = currentQuestion.id;
    setLangByQuestion((prev) => ({ ...prev, [qId]: lang }));
    setRunResult(null);
  };

  const handleSelectOption = async (optionId) => {
    if (!isActive || !currentQuestion || selectingOptionId) return;
    setSelectingOptionId(optionId);
    setActionError('');
    try {
      await assessmentService.recordAnswer(id, attempt.id, {
        questionId: currentQuestion.id,
        selectedOptionId: optionId,
      });
      await refreshAttempt();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Unable to save your answer.');
      if (err.response?.data?.message?.toLowerCase().includes('expired')) await refreshAttempt();
    } finally {
      setSelectingOptionId(null);
    }
  };

  const handleRunCode = async () => {
    if (!isActive || !currentQuestion) return;
    setRunning(true);
    setRunResult(null);
    setActionError('');
    try {
      const result = await assessmentService.runCode(id, attempt.id, {
        questionId: currentQuestion.id,
        language: getCurrentLang(),
        code: getCurrentCode(),
        submissionType: 'RUN_ONLY',
      });
      setRunResult(result);
    } catch (err) {
      setRunResult({ stderr: err.response?.data?.message || 'Run failed. Please try again.', exitCode: -1 });
    } finally {
      setRunning(false);
    }
  };

  const handleSubmitCode = async () => {
    if (!isActive || !currentQuestion) return;
    if (!window.confirm('Submit your code for this question? This will be graded against all test cases.')) return;
    setSubmittingCode(true);
    setRunResult(null);
    setActionError('');
    try {
      const result = await assessmentService.submitCode(id, attempt.id, {
        questionId: currentQuestion.id,
        language: getCurrentLang(),
        code: getCurrentCode(),
        submissionType: 'FULL',
      });
      setRunResult(result);
      await refreshAttempt();
    } catch (err) {
      setRunResult({ stderr: err.response?.data?.message || 'Submission failed.', exitCode: -1 });
    } finally {
      setSubmittingCode(false);
    }
  };

  const handleSubmit = async () => {
    if (!window.confirm('Submit this assessment? You will not be able to change your answers after submitting.')) return;
    setSubmitting(true);
    setActionError('');
    try {
      const result = await assessmentService.submitAttempt(id, attempt.id);
      setAttempt(result);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Unable to submit this assessment.');
      if (err.response?.data?.message?.toLowerCase().includes('expired')) await refreshAttempt();
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Question Feedback Modal States ----
  const [showFeedbackModal, setShowFeedbackModal] = useState(false);
  const [feedbackReason, setFeedbackReason] = useState('Typo / Formatting');
  const [feedbackMessage, setFeedbackMessage] = useState('');
  const [feedbackSubmitting, setFeedbackSubmitting] = useState(false);
  const [feedbackSuccess, setFeedbackSuccess] = useState('');
  const [feedbackError, setFeedbackError] = useState('');

  const handleOpenFeedback = () => {
    setFeedbackReason('Typo / Formatting');
    setFeedbackMessage('');
    setFeedbackSuccess('');
    setFeedbackError('');
    setShowFeedbackModal(true);
  };

  const handleSendQuestionFeedback = async (e) => {
    e.preventDefault();
    if (!currentQuestion) return;
    try {
      setFeedbackSubmitting(true);
      setFeedbackError('');
      await supportFeedbackService.createAssessmentQuestionFeedback({
        assessmentId: Number(id),
        questionId: currentQuestion.id,
        reason: feedbackReason,
        message: feedbackMessage,
      });
      setFeedbackSuccess('Feedback submitted successfully!');
      setTimeout(() => {
        setShowFeedbackModal(false);
        setFeedbackSuccess('');
      }, 1200);
    } catch (err) {
      setFeedbackError(err.response?.data?.message || 'Failed to submit feedback. Please try again.');
    } finally {
      setFeedbackSubmitting(false);
    }
  };

  // ---- Post-Completion Assessment Feedback States ----
  const [postRating, setPostRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [postFeedbackType, setPostFeedbackType] = useState('Excellent');
  const [postComments, setPostComments] = useState('');
  const [postFeedbackSubmitting, setPostFeedbackSubmitting] = useState(false);
  const [postFeedbackSuccess, setPostFeedbackSuccess] = useState(false);
  const [postFeedbackSkipped, setPostFeedbackSkipped] = useState(false);
  const [postFeedbackError, setPostFeedbackError] = useState('');
  const [existingPostFeedback, setExistingPostFeedback] = useState(null);

  useEffect(() => {
    if (attempt && attempt.status === 'COMPLETED') {
      assessmentService.getAttemptFeedback(id, attempt.id)
        .then((fb) => {
          if (fb && fb.id) {
            setExistingPostFeedback(fb);
            setPostRating(fb.rating || 5);
            setPostFeedbackType(fb.feedbackType || 'Excellent');
            setPostComments(fb.comments || '');
            setPostFeedbackSuccess(true);
          }
        })
        .catch(() => {});
    }
  }, [id, attempt?.id, attempt?.status]);

  const handlePostFeedbackSubmit = async (e) => {
    e.preventDefault();
    if (!attempt) return;
    try {
      setPostFeedbackSubmitting(true);
      setPostFeedbackError('');
      const res = await assessmentService.submitAssessmentFeedback(id, {
        assessmentId: Number(id),
        attemptId: attempt.id,
        rating: postRating,
        feedbackType: postFeedbackType,
        comments: postComments.trim(),
      });
      setExistingPostFeedback(res);
      setPostFeedbackSuccess(true);
    } catch (err) {
      setPostFeedbackError(err.response?.data?.message || 'Failed to submit assessment feedback. Please try again.');
    } finally {
      setPostFeedbackSubmitting(false);
    }
  };

  // ---- Render states ----
  if (loading) {
    return <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>{t('assessments.loading', 'Loading assessment...')}</div>;
  }

  if (loadError || !attempt) {
    return (
      <div className="card">
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{loadError || t('assessments.loadError', 'Unable to load this assessment.')}</span>
        </div>
        <Link to="/assessments" className="btn btn-outline btn-sm">
          <ArrowLeft size={14} />
          <span>{t('assessments.backToAssessments', 'Back to Assessments')}</span>
        </Link>
      </div>
    );
  }

  // ---- Completed ----
  if (attempt.status === 'COMPLETED') {
    return (
      <div>
        <Link to="/assessments" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem' }}>
          <ArrowLeft size={14} /> {t('assessments.backToAssessments', 'Back to Assessments')}
        </Link>

        <div className="card" style={{ marginBottom: '1.25rem', borderLeft: `4px solid ${attempt.passed ? 'var(--success)' : 'var(--danger)'}` }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            {attempt.passed ? <CheckCircle2 size={28} color="var(--success)" /> : <XCircle size={28} color="var(--danger)" />}
            <div>
              <h2 style={{ fontSize: '1.3rem', fontWeight: 800 }}>{attempt.assessmentTitle}</h2>
              <span style={{ fontWeight: 700, color: attempt.passed ? 'var(--success)' : 'var(--danger)' }}>
                {attempt.passed ? t('assessments.passed', 'Passed') : t('assessments.failed', 'Failed')}
              </span>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '1rem', fontSize: '0.9rem' }}>
            <div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Total Score</div>
              <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{attempt.score} / {attempt.totalMarks}</div>
            </div>
            <div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Percentage</div>
              <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>
                {attempt.totalMarks > 0 ? Math.round((attempt.score / attempt.totalMarks) * 100) : 0}%
              </div>
            </div>
            {attempt.mcqScore != null && (
              <div>
                <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>MCQ Score</div>
                <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{attempt.mcqScore}</div>
              </div>
            )}
            {attempt.programmingScore != null && (
              <div>
                <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Programming Score</div>
                <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{attempt.programmingScore}</div>
              </div>
            )}
            <div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Passing Marks</div>
              <div style={{ fontWeight: 700, fontSize: '1.1rem' }}>{attempt.passingMarks}</div>
            </div>
            {attempt.violationCount != null && attempt.violationCount > 0 && (
              <div>
                <div style={{ color: 'var(--text-subtle)', fontSize: '0.75rem' }}>Violations</div>
                <div style={{ fontWeight: 700, fontSize: '1.1rem', color: 'var(--danger)' }}>{attempt.violationCount}</div>
              </div>
            )}
          </div>
          {attempt.completedAt && (
            <p style={{ marginTop: '0.9rem', fontSize: '0.78rem', color: 'var(--text-subtle)' }}>
              {t('assessments.submitted', 'Submitted')} {new Date(attempt.completedAt).toLocaleString()}
            </p>
          )}
        </div>

        {/* Post-Completion Assessment Feedback Card */}
        {postFeedbackSuccess ? (
          <div className="card" style={{ marginBottom: '1.25rem', backgroundColor: 'var(--bg-input)', border: '1px solid var(--border-color)', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <CheckCircle2 size={22} color="var(--success)" />
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>Thank you for your assessment feedback!</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Your rating of {postRating}/5 stars ({postFeedbackType}) has been shared with the host.
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
              {[1, 2, 3, 4, 5].map((star) => (
                <Star
                  key={star}
                  size={16}
                  color={postRating >= star ? '#f59e0b' : 'var(--text-subtle)'}
                  fill={postRating >= star ? '#f59e0b' : 'transparent'}
                />
              ))}
            </div>
          </div>
        ) : postFeedbackSkipped ? (
          <div className="card" style={{ marginBottom: '1.25rem', padding: '0.75rem 1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            <span>Assessment feedback was skipped.</span>
            <button
              type="button"
              onClick={() => setPostFeedbackSkipped(false)}
              className="btn btn-outline btn-xs"
              style={{ fontSize: '0.78rem' }}
            >
              Give Feedback
            </button>
          </div>
        ) : (
          <div className="card" style={{ marginBottom: '1.25rem', border: '1px solid var(--primary-soft, #e0e7ff)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.35rem' }}>
              <MessageSquare size={20} color="var(--primary)" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800, margin: 0 }}>Assessment Feedback</h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              How was your overall experience taking this assessment? Your feedback directly helps the creator and host improve future evaluations.
            </p>

            {postFeedbackError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem', fontSize: '0.85rem' }}>
                <AlertCircle size={16} />
                <span>{postFeedbackError}</span>
              </div>
            )}

            <form onSubmit={handlePostFeedbackSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1.25rem', marginBottom: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 700, marginBottom: '0.4rem' }}>
                    Overall Rating <span style={{ color: 'var(--danger)' }}>*</span>
                  </label>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        onClick={() => setPostRating(star)}
                        onMouseEnter={() => setHoverRating(star)}
                        onMouseLeave={() => setHoverRating(0)}
                        style={{ background: 'transparent', border: 'none', cursor: 'pointer', padding: '0.2rem' }}
                        title={`${star} star${star > 1 ? 's' : ''}`}
                      >
                        <Star
                          size={24}
                          color={(hoverRating || postRating) >= star ? '#f59e0b' : 'var(--border-color)'}
                          fill={(hoverRating || postRating) >= star ? '#f59e0b' : 'transparent'}
                          style={{ transition: 'all 0.15s ease' }}
                        />
                      </button>
                    ))}
                    <span style={{ fontSize: '0.85rem', fontWeight: 700, marginLeft: '0.4rem', color: 'var(--text-main)' }}>
                      {(hoverRating || postRating)} / 5
                    </span>
                  </div>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 700, marginBottom: '0.4rem' }}>
                    Feedback Type <span style={{ color: 'var(--danger)' }}>*</span>
                  </label>
                  <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                    {['Excellent', 'Good', 'Average', 'Poor', 'Needs Improvement'].map((type) => (
                      <button
                        key={type}
                        type="button"
                        onClick={() => setPostFeedbackType(type)}
                        className={`btn btn-xs ${postFeedbackType === type ? 'btn-primary' : 'btn-outline'}`}
                        style={{ fontSize: '0.78rem', padding: '0.25rem 0.6rem' }}
                      >
                        {type}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              <div className="form-group" style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 700, marginBottom: '0.4rem' }}>
                  Comments / Observations (Optional)
                </label>
                <textarea
                  rows={2}
                  className="form-control"
                  placeholder="Share feedback on question clarity, difficulty balance, test cases, or suggestions..."
                  value={postComments}
                  onChange={(e) => setPostComments(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.6rem' }}>
                <button
                  type="button"
                  onClick={() => setPostFeedbackSkipped(true)}
                  className="btn btn-outline btn-sm"
                  disabled={postFeedbackSubmitting}
                >
                  Skip for Now
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
                  disabled={postFeedbackSubmitting}
                >
                  {postFeedbackSubmitting ? 'Submitting...' : 'Submit Feedback'}
                </button>
              </div>
            </form>
          </div>
        )}

        <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.9rem' }}>{t('assessments.review', 'Review')}</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
          {questions.map((question, idx) => {
            const answer = answersByQuestionId[question.id];
            const isProgramming = question.questionType === 'PROGRAMMING';
            return (
              <div key={question.id} className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: '0.75rem', marginBottom: '0.6rem' }}>
                  <div>
                    <span style={{ fontWeight: 700 }}>{idx + 1}. {question.questionText}</span>
                    {isProgramming && (
                      <span style={{ marginLeft: '0.5rem', fontSize: '0.72rem', backgroundColor: 'rgba(99,102,241,0.12)', color: '#6366f1', padding: '0.15rem 0.4rem', borderRadius: '4px', fontWeight: 700 }}>
                        PROGRAMMING
                      </span>
                    )}
                  </div>
                  <span style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-subtle)', whiteSpace: 'nowrap' }}>
                    {question.marks} marks
                  </span>
                </div>

                {isProgramming ? (
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    {answer ? (
                      <p>Code submitted. Score: <strong>{answer.marksAwarded ?? 0}</strong> / {question.marks}</p>
                    ) : (
                      <p style={{ color: 'var(--text-subtle)' }}>No code submitted.</p>
                    )}
                  </div>
                ) : (
                  <>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                      {(question.options || []).slice().sort((a, b) => a.orderIndex - b.orderIndex).map((option) => {
                        const wasSelected = answer?.selectedOptionId === option.id;
                        const cls = option.isCorrect ? 'correct' : wasSelected ? 'incorrect' : '';
                        return (
                          <div key={option.id} className={`assessment-option-card readonly ${cls}`}>
                            <span>{option.optionText}</span>
                            {wasSelected && <span style={{ marginLeft: 'auto', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>Your answer</span>}
                            {option.isCorrect && <CheckCircle2 size={16} color="var(--success)" style={{ marginLeft: wasSelected ? '0.5rem' : 'auto' }} />}
                          </div>
                        );
                      })}
                    </div>
                    {!answer && <p style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--text-subtle)' }}>Not answered.</p>}
                  </>
                )}
                {question.explanation && (
                  <p style={{ marginTop: '0.6rem', fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>{question.explanation}</p>
                )}
              </div>
            );
          })}
        </div>
      </div>
    );
  }

  // ---- Expired ----
  if (attempt.status === 'EXPIRED') {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2.5rem' }}>
        <Clock size={36} style={{ margin: '0 auto 1rem', color: 'var(--danger)' }} />
        <h2 style={{ fontSize: '1.3rem', fontWeight: 800, marginBottom: '0.5rem' }}>{t('assessments.timesUp', "Time's Up")}</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
          {t('assessments.expiredNotice', 'This attempt at {{title}} expired before it was submitted.', { title: attempt.assessmentTitle })}
        </p>
        <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
          {t('assessments.expiredExplanation', "An expired attempt isn't scored - only a submitted attempt is graded.")}
        </p>
        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
          <Link to="/assessments" className="btn btn-outline btn-sm">
            <ArrowLeft size={14} />
            <span>{t('assessments.backToAssessments', 'Back to Assessments')}</span>
          </Link>
          <button onClick={startOrResume} className="btn btn-primary btn-sm">
            <RefreshCw size={14} />
            <span>{t('assessments.startNewAttempt', 'Start New Attempt')}</span>
          </button>
        </div>
      </div>
    );
  }

  // ---- Proctoring verification setup screen ----
  if (!proctoringAgreed) {
    return (
      <div style={{ maxWidth: '680px', margin: '2rem auto' }}>
        <Link to="/assessments" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem' }}>
          <ArrowLeft size={14} /> {t('assessments.backToAssessments', 'Back to Assessments')}
        </Link>

        <div className="card" style={{ padding: '2rem', textAlign: 'center' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '64px', height: '64px', borderRadius: '50%', backgroundColor: 'rgba(99, 102, 241, 0.12)', color: 'var(--primary)', marginBottom: '1rem' }}>
            <ShieldCheck size={36} />
          </div>

          <h1 style={{ fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.5rem' }}>Assessment Proctoring Setup</h1>
          <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '1rem' }}>{attempt.assessmentTitle}</h2>

          <p style={{ color: 'var(--text-subtle)', fontSize: '0.88rem', maxWidth: '520px', margin: '0 auto 1.5rem', lineHeight: 1.5 }}>
            To uphold academic and competition integrity, this assessment requires continuous live <strong>camera and microphone monitoring</strong>. Please verify your devices below before beginning.
          </p>

          <div style={{ position: 'relative', width: '100%', maxWidth: '440px', height: '270px', margin: '0 auto 1.5rem', backgroundColor: '#0f172a', borderRadius: '12px', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid var(--border-color)', boxShadow: '0 4px 16px rgba(0,0,0,0.15)' }}>
            <video ref={previewVideoRef} autoPlay playsInline muted style={{ width: '100%', height: '100%', objectFit: 'cover', display: cameraActive ? 'block' : 'none' }} />
            {!cameraActive && (
              <div style={{ color: '#94a3b8', textAlign: 'center', padding: '1.5rem' }}>
                <Camera size={44} style={{ margin: '0 auto 0.75rem', opacity: 0.6 }} />
                <p style={{ fontSize: '0.9rem', margin: 0, fontWeight: 500 }}>Camera stream will preview here once permitted.</p>
              </div>
            )}
            {cameraActive && (
              <div style={{ position: 'absolute', top: '12px', left: '12px', display: 'flex', alignItems: 'center', gap: '0.35rem', backgroundColor: 'rgba(0,0,0,0.7)', color: '#10b981', padding: '0.25rem 0.6rem', borderRadius: '9999px', fontSize: '0.72rem', fontWeight: 700, backdropFilter: 'blur(4px)' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#10b981' }} />
                CAM STREAMING
              </div>
            )}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '440px', margin: '0 auto 1.5rem', textAlign: 'left' }}>
            <div style={{ padding: '0.75rem 1rem', borderRadius: '8px', border: `1px solid ${cameraActive ? 'rgba(16, 185, 129, 0.4)' : 'var(--border-color)'}`, backgroundColor: cameraActive ? 'rgba(16, 185, 129, 0.08)' : 'var(--bg-input)', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              {cameraActive ? <CheckCircle2 size={20} color="var(--success)" /> : <CameraOff size={20} color="var(--text-subtle)" />}
              <div>
                <div style={{ fontSize: '0.82rem', fontWeight: 700 }}>Camera Access</div>
                <div style={{ fontSize: '0.72rem', color: cameraActive ? 'var(--success)' : 'var(--text-subtle)', fontWeight: 600 }}>{cameraActive ? 'Active & Verified' : 'Permission Required'}</div>
              </div>
            </div>
            <div style={{ padding: '0.75rem 1rem', borderRadius: '8px', border: `1px solid ${micActive ? 'rgba(16, 185, 129, 0.4)' : 'var(--border-color)'}`, backgroundColor: micActive ? 'rgba(16, 185, 129, 0.08)' : 'var(--bg-input)', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              {micActive ? <CheckCircle2 size={20} color="var(--success)" /> : <MicOff size={20} color="var(--text-subtle)" />}
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: '0.82rem', fontWeight: 700 }}>Microphone</div>
                <div style={{ fontSize: '0.72rem', color: micActive ? 'var(--success)' : 'var(--text-subtle)', fontWeight: 600 }}>{micActive ? 'Active & Detecting' : 'Permission Required'}</div>
                {micActive && (
                  <div style={{ marginTop: '5px', height: '4px', width: '100%', backgroundColor: 'var(--bg-main)', borderRadius: '2px', overflow: 'hidden' }}>
                    <div style={{ width: `${Math.max(15, audioLevel)}%`, height: '100%', backgroundColor: '#10b981', transition: 'width 0.1s ease' }} />
                  </div>
                )}
              </div>
            </div>
          </div>

          {mediaError && (
            <div className="alert alert-error" style={{ maxWidth: '440px', margin: '0 auto 1.5rem', textAlign: 'left' }}>
              <AlertCircle size={18} />
              <span>{mediaError}</span>
            </div>
          )}

          <div style={{ display: 'flex', justifyContent: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
            <Link to="/assessments" className="btn btn-outline btn-sm">
              <ArrowLeft size={14} /> Back to Assessments
            </Link>
            {(!cameraActive || !micActive) ? (
              <button onClick={requestMediaPermissions} className="btn btn-primary btn-sm">
                <Camera size={15} /> Allow Camera & Microphone
              </button>
            ) : (
              <button onClick={() => setProctoringAgreed(true)} className="btn btn-primary btn-sm" style={{ backgroundColor: '#10b981', borderColor: '#10b981' }}>
                <ShieldCheck size={15} /> I Agree & Start Assessment
              </button>
            )}
          </div>
        </div>
      </div>
    );
  }

  // ---- In progress: the question-taking UI ----
  const isProgrammingQuestion = currentQuestion?.questionType === 'PROGRAMMING';
  const pq = currentQuestion?.programmingQuestion;
  const currentLang = getCurrentLang();
  const currentCode = getCurrentCode();
  const supportedLangs = pq?.supportedLanguages?.length ? pq.supportedLanguages : SUPPORTED_LANGUAGES;

  return (
    <div>
      <div className="card assessment-timer-bar">
        <div>
          <Link to="/assessments" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.4rem' }}>
            <ArrowLeft size={14} /> {t('assessments.backToAssessments', 'Back to Assessments')}
          </Link>
          <h1 style={{ fontSize: '1.25rem', fontWeight: 800 }}>{attempt.assessmentTitle}</h1>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            {questions.filter((q) => q.questionType !== 'PROGRAMMING' ? !!answersByQuestionId[q.id] : false).length} / {questions.filter((q) => q.questionType !== 'PROGRAMMING').length} MCQ answered
          </span>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{t('common.timeRemaining', 'Time Remaining')}</div>
          <div className="assessment-timer-clock" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', justifyContent: 'flex-end', color: millisRemaining != null && millisRemaining < 60000 ? 'var(--danger)' : 'var(--primary)' }}>
            <Clock size={18} />
            {millisRemaining != null ? formatCountdown(millisRemaining) : '--:--'}
          </div>
          {millisRemaining != null && millisRemaining < 60000 && (
            <div style={{ fontSize: '0.72rem', color: 'var(--danger)', fontWeight: 600 }}>{t('assessments.lessThanMinute', 'Less than a minute left!')}</div>
          )}
        </div>
      </div>

      {actionError && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem' }}>
          <AlertCircle size={16} />
          <span>{actionError}</span>
        </div>
      )}

      {proctoringWarning && (
        <div className="alert alert-error" style={{ marginBottom: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <AlertTriangle size={18} color="#ef4444" />
            <span><strong>Proctoring Notice:</strong> {proctoringWarning}</span>
          </div>
          <button onClick={requestMediaPermissions} className="btn btn-outline btn-sm" style={{ borderColor: '#ef4444', color: '#ef4444' }}>
            Reconnect Camera / Mic
          </button>
        </div>
      )}

      {questions.length === 0 ? (
        <div className="card empty-state">
          <p>{t('assessments.noQuestions', 'This assessment has no questions yet.')}</p>
        </div>
      ) : (
        <>
          {/* Question Navigation */}
          <div className="assessment-question-nav">
            {questions.map((q, idx) => {
              const isProg = q.questionType === 'PROGRAMMING';
              const isAnswered = isProg ? false : !!answersByQuestionId[q.id];
              return (
                <button
                  key={q.id}
                  onClick={() => { setCurrentIndex(idx); setRunResult(null); }}
                  className={`assessment-question-dot ${isAnswered ? 'answered' : ''} ${idx === currentIndex ? 'current' : ''}`}
                  title={isProg ? 'Programming' : (isAnswered ? 'Answered' : 'Not answered')}
                  style={isProg ? { borderStyle: 'dashed', borderColor: '#8b5cf6', color: '#8b5cf6' } : {}}
                >
                  {isProg ? <Code2 size={10} /> : idx + 1}
                </button>
              );
            })}
          </div>

          {currentQuestion && (
            <>
              {/* Question Header */}
              <div className="card" style={{ marginBottom: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flex: 1 }}>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>
                      {currentIndex + 1}. {currentQuestion.questionText}
                    </h3>
                    {isProgrammingQuestion && (
                      <span style={{ fontSize: '0.72rem', backgroundColor: 'rgba(139,92,246,0.12)', color: '#8b5cf6', padding: '0.15rem 0.5rem', borderRadius: '4px', fontWeight: 700, whiteSpace: 'nowrap' }}>
                        PROGRAMMING
                      </span>
                    )}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <button
                      type="button"
                      onClick={handleOpenFeedback}
                      className="btn btn-outline btn-xs"
                      style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.72rem', color: 'var(--text-muted)', padding: '0.2rem 0.55rem' }}
                      title="Report Question / Give Feedback"
                    >
                      <MessageSquare size={12} />
                      <span>Report Question</span>
                    </button>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', whiteSpace: 'nowrap' }}>
                      {currentQuestion.marks} {currentQuestion.marks === 1 ? 'mark' : 'marks'}
                    </span>
                  </div>
                </div>

                {/* Programming question details */}
                {isProgrammingQuestion && pq && (
                  <div style={{ fontSize: '0.875rem', lineHeight: 1.6, color: 'var(--text-main)' }}>
                    {pq.problemStatement && <p style={{ whiteSpace: 'pre-wrap', marginBottom: '0.75rem' }}>{pq.problemStatement}</p>}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '0.75rem', marginBottom: '0.75rem' }}>
                      {pq.inputFormat && (
                        <div>
                          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-subtle)', marginBottom: '0.25rem' }}>INPUT FORMAT</div>
                          <pre style={{ fontSize: '0.82rem', backgroundColor: 'var(--bg-input)', padding: '0.5rem', borderRadius: '6px', whiteSpace: 'pre-wrap', margin: 0 }}>{pq.inputFormat}</pre>
                        </div>
                      )}
                      {pq.outputFormat && (
                        <div>
                          <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-subtle)', marginBottom: '0.25rem' }}>OUTPUT FORMAT</div>
                          <pre style={{ fontSize: '0.82rem', backgroundColor: 'var(--bg-input)', padding: '0.5rem', borderRadius: '6px', whiteSpace: 'pre-wrap', margin: 0 }}>{pq.outputFormat}</pre>
                        </div>
                      )}
                    </div>
                    {pq.constraints && (
                      <div style={{ marginBottom: '0.75rem' }}>
                        <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-subtle)', marginBottom: '0.25rem' }}>CONSTRAINTS</div>
                        <p style={{ fontSize: '0.85rem', margin: 0 }}>{pq.constraints}</p>
                      </div>
                    )}
                    {(pq.sampleInput || pq.sampleOutput) && (
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                        {pq.sampleInput && (
                          <div>
                            <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-subtle)', marginBottom: '0.25rem' }}>SAMPLE INPUT</div>
                            <pre style={{ fontFamily: 'monospace', fontSize: '0.82rem', backgroundColor: 'var(--bg-input)', padding: '0.5rem', borderRadius: '6px', whiteSpace: 'pre-wrap', margin: 0 }}>{pq.sampleInput}</pre>
                          </div>
                        )}
                        {pq.sampleOutput && (
                          <div>
                            <div style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--text-subtle)', marginBottom: '0.25rem' }}>SAMPLE OUTPUT</div>
                            <pre style={{ fontFamily: 'monospace', fontSize: '0.82rem', backgroundColor: 'var(--bg-input)', padding: '0.5rem', borderRadius: '6px', whiteSpace: 'pre-wrap', margin: 0 }}>{pq.sampleOutput}</pre>
                          </div>
                        )}
                      </div>
                    )}
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', marginTop: '0.5rem' }}>
                      Time Limit: {pq.timeLimitMs ?? 2000}ms | Memory: {pq.memoryLimitMb ?? 256}MB
                    </p>
                  </div>
                )}
              </div>

              {/* Programming Editor */}
              {isProgrammingQuestion ? (
                <div>
                  {/* Language selector */}
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Language:</span>
                    {supportedLangs.map((lang) => (
                      <button
                        key={lang}
                        onClick={() => setCurrentLang(lang)}
                        className={`btn btn-sm ${currentLang === lang ? 'btn-primary' : 'btn-outline'}`}
                        style={{ fontSize: '0.78rem', padding: '0.2rem 0.6rem' }}
                      >
                        {lang}
                      </button>
                    ))}
                  </div>

                  {/* Monaco Editor */}
                  <div style={{ border: '1px solid var(--border-color)', borderRadius: '8px', overflow: 'hidden', marginBottom: '0.75rem' }}>
                    <Editor
                      height="380px"
                      language={MONACO_LANG_MAP[currentLang] || currentLang}
                      value={currentCode}
                      onChange={(val) => setCurrentCode(val || '')}
                      theme="vs-dark"
                      options={{
                        minimap: { enabled: false },
                        fontSize: 14,
                        scrollBeyondLastLine: false,
                        wordWrap: 'on',
                        readOnly: !isActive,
                      }}
                    />
                  </div>

                  {/* Run / Submit Code buttons */}
                  <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
                    <button onClick={handleRunCode} className="btn btn-outline btn-sm" disabled={running || submittingCode || !isActive}>
                      <Play size={14} />
                      <span>{running ? 'Running...' : 'Run Code'}</span>
                    </button>
                    <button onClick={handleSubmitCode} className="btn btn-primary btn-sm" disabled={running || submittingCode || !isActive}>
                      <Send size={14} />
                      <span>{submittingCode ? 'Submitting...' : 'Submit Code'}</span>
                    </button>
                  </div>

                  {/* Run Result */}
                  {runResult && (
                    <div style={{ backgroundColor: 'var(--bg-input)', borderRadius: '8px', padding: '0.85rem', border: '1px solid var(--border-color)', marginBottom: '0.75rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                        <Terminal size={14} color="var(--text-muted)" />
                        <span style={{ fontSize: '0.8rem', fontWeight: 700 }}>Output</span>
                        {runResult.testResults != null && (
                          <span style={{ fontSize: '0.75rem', color: runResult.testsPassed === runResult.totalTests ? 'var(--success)' : 'var(--danger)', fontWeight: 700, marginLeft: 'auto' }}>
                            {runResult.testsPassed ?? 0}/{runResult.totalTests ?? 0} tests passed
                          </span>
                        )}
                      </div>
                      {runResult.compilationError && (
                        <pre style={{ fontSize: '0.8rem', color: '#ef4444', whiteSpace: 'pre-wrap', margin: 0 }}>{runResult.compilationError}</pre>
                      )}
                      {runResult.stdout && (
                        <pre style={{ fontSize: '0.8rem', color: 'var(--success)', whiteSpace: 'pre-wrap', margin: 0 }}>{runResult.stdout}</pre>
                      )}
                      {runResult.stderr && (
                        <pre style={{ fontSize: '0.8rem', color: '#ef4444', whiteSpace: 'pre-wrap', margin: 0 }}>{runResult.stderr}</pre>
                      )}
                      {runResult.timedOut && (
                        <p style={{ fontSize: '0.8rem', color: '#f59e0b', margin: 0 }}>⏱ Time limit exceeded</p>
                      )}
                    </div>
                  )}
                </div>
              ) : (
                /* MCQ Options */
                <div className="card" style={{ marginBottom: '1.25rem' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                    {(currentQuestion.options || []).slice().sort((a, b) => a.orderIndex - b.orderIndex).map((option) => {
                      const isSelected = currentAnswer?.selectedOptionId === option.id;
                      return (
                        <button
                          key={option.id}
                          type="button"
                          onClick={() => handleSelectOption(option.id)}
                          disabled={!isActive || selectingOptionId != null}
                          className={`assessment-option-card ${isSelected ? 'selected' : ''}`}
                        >
                          <span style={{ width: '18px', height: '18px', borderRadius: '50%', flexShrink: 0, border: `2px solid ${isSelected ? 'var(--primary)' : 'var(--border-color)'}`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            {isSelected && <span style={{ width: '9px', height: '9px', borderRadius: '50%', backgroundColor: 'var(--primary)' }} />}
                          </span>
                          <span>{option.optionText}</span>
                          {selectingOptionId === option.id && <span style={{ marginLeft: 'auto', fontSize: '0.75rem', color: 'var(--text-subtle)' }}>Saving...</span>}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}
            </>
          )}

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem', marginTop: '0.5rem' }}>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button onClick={() => { setCurrentIndex((i) => Math.max(0, i - 1)); setRunResult(null); }} disabled={currentIndex === 0} className="btn btn-outline btn-sm">
                {t('common.previous', 'Previous')}
              </button>
              <button onClick={() => { setCurrentIndex((i) => Math.min(questions.length - 1, i + 1)); setRunResult(null); }} disabled={currentIndex === questions.length - 1} className="btn btn-outline btn-sm">
                {t('common.next', 'Next')}
              </button>
            </div>
            <button onClick={handleSubmit} className="btn btn-primary btn-sm" disabled={submitting || !isActive}>
              <Send size={14} />
              <span>{submitting ? t('assessments.submitting', 'Submitting...') : t('common.submitAssessment', 'Submit Assessment')}</span>
            </button>
          </div>
        </>
      )}

      {/* Floating Picture-In-Picture Proctoring Video Window */}
      {proctoringAgreed && isActive && (
        <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 1000, backgroundColor: '#0f172a', color: '#ffffff', borderRadius: '10px', padding: pipMinimized ? '8px 12px' : '10px', boxShadow: '0 8px 24px rgba(0,0,0,0.35)', border: proctoringWarning ? '2px solid #ef4444' : '2px solid rgba(99, 102, 241, 0.5)', maxWidth: '220px', transition: 'all 0.2s ease' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px', marginBottom: pipMinimized ? 0 : '8px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.72rem', fontWeight: 700 }}>
              <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: proctoringWarning ? '#ef4444' : '#10b981', boxShadow: proctoringWarning ? '0 0 6px #ef4444' : '0 0 6px #10b981' }} />
              <span>{proctoringWarning ? 'ALERT' : 'PROCTOR LIVE'}</span>
            </div>
            <button type="button" onClick={() => setPipMinimized(!pipMinimized)} style={{ background: 'transparent', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: 0 }} title={pipMinimized ? 'Expand camera' : 'Minimize camera'}>
              {pipMinimized ? <Maximize2 size={13} /> : <Minimize2 size={13} />}
            </button>
          </div>
          {!pipMinimized && (
            <div style={{ width: '180px', height: '120px', borderRadius: '6px', overflow: 'hidden', backgroundColor: '#000' }}>
              <video ref={liveVideoRef} autoPlay playsInline muted style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            </div>
          )}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: pipMinimized ? 0 : '6px', fontSize: '0.68rem', color: '#94a3b8' }}>
            <span style={{ color: cameraActive ? '#10b981' : '#ef4444', fontWeight: 600 }}>{cameraActive ? '● Cam Active' : '✕ Cam Lost'}</span>
            <span style={{ color: micActive ? '#10b981' : '#ef4444', fontWeight: 600 }}>{micActive ? '● Mic Active' : '✕ Mic Lost'}</span>
          </div>
        </div>
      )}

      {/* Assessment Question Feedback Modal */}
      {showFeedbackModal && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 1100, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' }}>
          <div style={{ backgroundColor: 'var(--bg-card, #fff)', color: 'var(--text-main, #1e293b)', borderRadius: '12px', padding: '1.5rem', width: '100%', maxWidth: '480px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)', border: '1px solid var(--border-color, #e2e8f0)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                <MessageSquare size={18} color="var(--primary, #3b82f6)" /> Report Question
              </h3>
              <button
                type="button"
                onClick={() => setShowFeedbackModal(false)}
                style={{ background: 'transparent', border: 'none', cursor: 'pointer', color: 'var(--text-muted)' }}
              >
                <X size={18} />
              </button>
            </div>

            {feedbackSuccess && (
              <div className="alert alert-success" style={{ marginBottom: '1rem', fontSize: '0.85rem' }}>
                <CheckCircle2 size={16} />
                <span>{feedbackSuccess}</span>
              </div>
            )}

            {feedbackError && (
              <div className="alert alert-error" style={{ marginBottom: '1rem', fontSize: '0.85rem' }}>
                <AlertCircle size={16} />
                <span>{feedbackError}</span>
              </div>
            )}

            <form onSubmit={handleSendQuestionFeedback} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  Issue Type <span style={{ color: '#ef4444' }}>*</span>
                </label>
                <select
                  value={feedbackReason}
                  onChange={(e) => setFeedbackReason(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border-color, #cbd5e1)', backgroundColor: 'var(--bg-input, #fff)', color: 'inherit' }}
                >
                  <option value="Typo / Formatting">Typo / Formatting</option>
                  <option value="Incorrect Test Cases">Incorrect Test Cases</option>
                  <option value="Ambiguous Statement">Ambiguous Statement</option>
                  <option value="Wrong Answer / Options">Wrong Answer / Options</option>
                  <option value="Other">Other</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, marginBottom: '0.35rem' }}>
                  Details (Optional)
                </label>
                <textarea
                  rows={3}
                  value={feedbackMessage}
                  onChange={(e) => setFeedbackMessage(e.target.value)}
                  placeholder="Explain what is wrong with this question..."
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border-color, #cbd5e1)', backgroundColor: 'var(--bg-input, #fff)', color: 'inherit', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setShowFeedbackModal(false)}
                  className="btn btn-outline btn-sm"
                  disabled={feedbackSubmitting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
                  disabled={feedbackSubmitting}
                >
                  {feedbackSubmitting ? 'Submitting...' : 'Submit Report'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AssessmentAttempt;
