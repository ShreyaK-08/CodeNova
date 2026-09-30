import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import contestService from '../../services/contestService';
import {
  getContestTimeState,
  getContestDisplayStatus,
  formatContestDateTime,
  formatContestSchedule,
  syncServerTime,
  getServerNow,
} from '../../utils/contestTime';
import ContestCountdown from '../../components/common/ContestCountdown';
import {
  Flag,
  Calendar,
  List,
  AlertCircle,
  RefreshCw,
  CheckCircle,
  ArrowLeft,
  PlayCircle,
  Trophy,
  Lock,
  ShieldAlert,
} from 'lucide-react';

const STATUS_STYLES = {
  DRAFT: { color: 'var(--text-muted)', bg: 'var(--bg-input)', translationKey: 'contests.draft', defaultLabel: 'Draft' },
  PUBLISHED: { color: 'var(--primary)', bg: 'var(--primary-soft)', translationKey: 'contests.upcoming', defaultLabel: 'Upcoming' },
  UPCOMING: { color: 'var(--primary)', bg: 'var(--primary-soft)', translationKey: 'contests.upcoming', defaultLabel: 'Upcoming' },
  ONGOING: { color: 'var(--success)', bg: 'var(--success-soft)', translationKey: 'contests.ongoing', defaultLabel: 'Ongoing' },
  ENDED: { color: 'var(--text-subtle)', bg: 'var(--bg-input)', translationKey: 'contests.ended', defaultLabel: 'Ended' },
};

const ContestDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [contest, setContest] = useState(null);
  const [registered, setRegistered] = useState(false);
  const [registration, setRegistration] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [registering, setRegistering] = useState(false);
  const [registerError, setRegisterError] = useState('');
  const [registerSuccess, setRegisterSuccess] = useState('');
  const [now, setNow] = useState(() => new Date());

  // Lightweight periodic update so contest statuses transition cleanly in real-time
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 10000);
    return () => clearInterval(timer);
  }, []);

  // ---- Leaderboard (Task 8, Step 13) - loaded on demand, not on page load ----
  const [showLeaderboard, setShowLeaderboard] = useState(false);
  const [leaderboard, setLeaderboard] = useState(null);
  const [leaderboardLoading, setLeaderboardLoading] = useState(false);
  const [leaderboardError, setLeaderboardError] = useState('');

  const loadContest = async () => {
    setLoading(true);
    setError('');
    try {
      const [contestData, registrationData] = await Promise.all([
        contestService.getContestDetails(id),
        contestService.getRegistrationStatus(id),
      ]);
      setContest(contestData);
      setRegistered(registrationData.registered);
      setRegistration(registrationData);
      if (contestData?.serverTime || contestData?.serverTimeIso) {
        syncServerTime(contestData.serverTime || contestData.serverTimeIso);
      }
    } catch (err) {
      setError('Unable to load contest. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const handleRegister = async () => {
    if (registering || registered) return;
    setRegistering(true);
    setRegisterError('');
    setRegisterSuccess('');
    try {
      await contestService.registerForContest(id);
      setRegistered(true);
      setRegisterSuccess('You are registered for this contest.');
    } catch (err) {
      // Surfaces the backend's real message (e.g. duplicate-registration is rejected
      // with "You are already registered for this contest.") rather than a generic one.
      const message = err.response?.data?.message || 'Failed to register for this contest. Please try again.';
      setRegisterError(message);
      // If the backend says we're already registered, reflect that in the UI instead
      // of leaving a stale "Register" button showing.
      if (message.toLowerCase().includes('already registered')) {
        setRegistered(true);
      }
    } finally {
      setRegistering(false);
    }
  };

  const handleToggleLeaderboard = async () => {
    const next = !showLeaderboard;
    setShowLeaderboard(next);
    if (next) {
      setLeaderboardLoading(true);
      setLeaderboardError('');
      try {
        const data = await contestService.getLeaderboard(id);
        setLeaderboard(data);
      } catch (err) {
        setLeaderboardError('Unable to load leaderboard.');
      } finally {
        setLeaderboardLoading(false);
      }
    }
  };

  const isAttemptTerminated = Boolean(
    registration?.terminated ||
    (registration?.securityViolationCount != null &&
      registration?.maxViolations != null &&
      registration.securityViolationCount >= registration.maxViolations) ||
    registration?.attemptStatus === 'COMPLETED'
  );

  const handleEnterContest = () => {
    if (isAttemptTerminated) return;
    // Best-effort (Task 9): request fullscreen synchronously inside this click handler
    // so it's still within the user gesture the Fullscreen API requires. If it's denied
    // or unsupported, ContestCoding.jsx's own "Enable Fullscreen" retry button (also a
    // genuine user gesture) covers that - this never blocks navigation either way.
    try {
      document.documentElement.requestFullscreen?.().catch(() => {});
    } catch {
      // Fullscreen API not supported in this browser - ContestCoding.jsx handles that.
    }
    navigate(`/contests/${id}/code`);
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('common.loading', 'Loading contest...')}</div>;
  }

  if (error || !contest) {
    return (
      <div className="card">
        <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
          <AlertCircle size={18} />
          <span>{error || 'Contest not found.'}</span>
        </div>
        <button onClick={loadContest} className="btn btn-outline btn-sm">
          <RefreshCw size={14} />
          <span>{t('common.retry', 'Retry')}</span>
        </button>
      </div>
    );
  }

  const displayStatus = contest ? getContestDisplayStatus(contest, now) : 'ENDED';
  const statusStyle = STATUS_STYLES[displayStatus] || STATUS_STYLES.ENDED;
  const timeState = contest ? getContestTimeState(contest, now) : 'not-started';
  const canRegister = (displayStatus === 'UPCOMING' || displayStatus === 'ONGOING' || contest?.status === 'PUBLISHED') && timeState !== 'ended';

  return (
    <div>
      <Link to="/contests" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1rem' }}>
        <ArrowLeft size={14} /> {t('contests.backToContests', 'Back to Contests')}
      </Link>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', marginBottom: '0.75rem', flexWrap: 'wrap' }}>
          <div>
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Flag size={22} color="var(--primary)" /> {contest.title}
            </h1>
            <p style={{ color: 'var(--text-muted)', marginTop: '0.25rem' }}>{contest.organizationName}</p>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', flexWrap: 'wrap' }}>
            <ContestCountdown contest={contest} onStatusChange={() => loadContest()} />
            <span style={{
              fontSize: '0.8rem', fontWeight: 700, padding: '0.3rem 0.75rem', borderRadius: '9999px',
              color: statusStyle.color, backgroundColor: statusStyle.bg, whiteSpace: 'nowrap',
            }}>
              {t(statusStyle.translationKey, statusStyle.defaultLabel)}
            </span>
          </div>
        </div>

        {contest.description && <p style={{ marginBottom: '1rem' }}>{contest.description}</p>}

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-subtle)', fontSize: '0.95rem', marginBottom: '1rem', fontWeight: 600 }}>
          <Calendar size={16} color="var(--primary)" />
          <span>{formatContestSchedule(contest.startTime, contest.endTime)}</span>
        </div>

        <div className="grid-cols-2" style={{ gap: '1rem', marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            <Calendar size={15} />
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{t('contests.start', 'Start')}</div>
              <div>{formatContestDateTime(contest.startTime)}</div>
            </div>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            <Calendar size={15} />
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)' }}>{t('contests.end', 'End')}</div>
              <div>{formatContestDateTime(contest.endTime)}</div>
            </div>
          </div>
        </div>

        {registerError && (
          <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
            <AlertCircle size={16} />
            <span>{registerError}</span>
          </div>
        )}
        {registerSuccess && (
          <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
            <CheckCircle size={16} />
            <span>{registerSuccess}</span>
          </div>
        )}

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
          {registered ? (
            <button className="btn btn-outline btn-sm" disabled style={{ cursor: 'default' }}>
              <CheckCircle size={14} color="var(--success)" />
              <span>{t('contests.registered', 'Registered')}</span>
            </button>
          ) : canRegister ? (
            <button onClick={handleRegister} className="btn btn-primary btn-sm" disabled={registering}>
              <span>{registering ? t('contests.registering', 'Registering...') : t('contests.registerForContest', 'Register for Contest')}</span>
            </button>
          ) : (
            <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', margin: 0 }}>
              {t('contests.registrationClosed', 'Registration is not open for this contest right now.')}
            </p>
          )}

          {/* Entering the contest coding screen is only offered once registered, and its
              enabled/disabled state is driven purely by the contest's real startTime/endTime
              (see utils/contestTime.js) - never a hardcoded duration or the status field alone. */}
          {registered && isAttemptTerminated ? (
            <button className="btn btn-outline btn-sm" disabled style={{ cursor: 'not-allowed', color: 'var(--danger)', borderColor: 'var(--danger)', opacity: 0.9, display: 'inline-flex', alignItems: 'center', gap: '0.4rem' }}>
              <Lock size={14} />
              <span>{t('contests.attemptLocked', 'Attempt Terminated (Locked)')}</span>
            </button>
          ) : registered && timeState === 'ongoing' ? (
            <button onClick={handleEnterContest} className="btn btn-primary btn-sm">
              <PlayCircle size={14} />
              <span>{t('contests.enterContest', 'Enter Contest')}</span>
            </button>
          ) : registered && timeState === 'not-started' ? (
            <p style={{ color: 'var(--text-subtle)', fontSize: '0.85rem', margin: 0 }}>
              {t('contests.contestStartsAt', 'Contest starts at')} <strong>{formatContestDateTime(contest.startTime)}</strong>
            </p>
          ) : registered && timeState === 'ended' ? (
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-subtle)' }}>
              {t('contests.contestEnded', 'Contest Ended')}
            </span>
          ) : null}
        </div>

        {registered && isAttemptTerminated && (
          <div style={{
            marginTop: '1.25rem',
            padding: '1rem 1.25rem',
            borderRadius: 'var(--radius-md)',
            backgroundColor: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.35)',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.75rem',
          }}>
            <ShieldAlert size={22} color="var(--danger)" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <div style={{ fontWeight: 700, color: 'var(--danger)', fontSize: '0.95rem' }}>
                {t('contests.attemptTerminatedTitle', 'Contest Attempt Terminated')}
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.3rem', lineHeight: 1.5 }}>
                {registration?.securityViolationCount >= (registration?.maxViolations || 3)
                  ? t('contests.attemptTerminatedViolationsDesc', 'You exceeded the maximum allowed security violations (fullscreen exits or tab switches). Your contest attempt has been permanently terminated and cannot be re-opened.')
                  : t('contests.attemptEndedDesc', 'Your contest attempt has ended.')}
              </div>
              {registration?.securityViolationCount > 0 && (
                <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', fontWeight: 600, color: 'var(--danger)' }}>
                  {t('contests.securityViolations', 'Security Violations')}: {registration.securityViolationCount}/{registration.maxViolations || 3}
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      <div className="card">
        <div className="panel-header">
          <span className="panel-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <List size={16} /> {t('contests.problems', 'Problems')} ({contest.problems?.length || 0})
          </span>
        </div>

        {contest.problems && contest.problems.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {contest.problems
              .slice()
              .sort((a, b) => a.displayOrder - b.displayOrder)
              .map((p) => (
                <div key={p.problemId} style={{
                  display: 'flex', alignItems: 'center', gap: '0.6rem', padding: '0.6rem 0.75rem',
                  border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)',
                }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>{p.displayOrder}.</span>
                  <span style={{ flex: 1, fontWeight: 600 }}>{p.problemTitle}</span>
                  {p.difficulty && (
                    <span className={`badge badge-${p.difficulty.toLowerCase()}`}>{p.difficulty}</span>
                  )}
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{p.points} pts</span>
                </div>
              ))}
          </div>
        ) : (
          <div className="empty-state">
            <p>{t('contests.noProblems', 'No problems have been added to this contest yet.')}</p>
          </div>
        )}
      </div>

      <div className="card" style={{ marginTop: '1.5rem' }}>
        <div className="panel-header">
          <span className="panel-title" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <Trophy size={16} /> {t('contests.leaderboard', 'Leaderboard')}
          </span>
          <button onClick={handleToggleLeaderboard} className="btn btn-outline btn-sm">
            <span>{showLeaderboard ? t('contests.hideLeaderboard', 'Hide Leaderboard') : t('contests.viewLeaderboard', 'View Leaderboard')}</span>
          </button>
        </div>

        {showLeaderboard && (
          leaderboardLoading ? (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '1rem 0' }}>{t('contests.loadingLeaderboard', 'Loading leaderboard...')}</p>
          ) : leaderboardError ? (
            <div className="alert alert-error"><AlertCircle size={16} /><span>{leaderboardError}</span></div>
          ) : !leaderboard || leaderboard.length === 0 ? (
            <div className="empty-state">
              <p>{t('contests.noParticipants', 'No participants yet.')}</p>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', textAlign: 'left', color: 'var(--text-subtle)' }}>
                    <th style={{ padding: '0.5rem' }}>{t('contests.rank', 'Rank')}</th>
                    <th style={{ padding: '0.5rem' }}>{t('contests.participant', 'Participant')}</th>
                    <th style={{ padding: '0.5rem' }}>{t('contests.score', 'Score')}</th>
                    <th style={{ padding: '0.5rem' }}>{t('contests.solved', 'Solved')}</th>
                    <th style={{ padding: '0.5rem' }}>{t('contests.submissions', 'Submissions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {leaderboard.map((entry) => (
                    <tr key={entry.rank} style={{ borderBottom: '1px solid var(--border-color)' }}>
                      <td style={{ padding: '0.5rem', fontWeight: 700 }}>{entry.rank}</td>
                      <td style={{ padding: '0.5rem' }}>{entry.name || entry.username}</td>
                      <td style={{ padding: '0.5rem', fontWeight: 700, color: 'var(--primary)' }}>{entry.score}</td>
                      <td style={{ padding: '0.5rem' }}>{entry.solvedCount}</td>
                      <td style={{ padding: '0.5rem' }}>{entry.submissionCount}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default ContestDetail;
