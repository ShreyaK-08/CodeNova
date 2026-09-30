import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import contestService from '../../services/contestService';
import { getContestDisplayStatus, formatContestSchedule, syncServerTime, getServerNow } from '../../utils/contestTime';
import ContestCountdown from '../../components/common/ContestCountdown';
import {
  Flag,
  Calendar,
  List,
  AlertCircle,
  RefreshCw,
  CheckCircle,
} from 'lucide-react';

const STATUS_STYLES = {
  DRAFT: { color: 'var(--text-muted)', bg: 'var(--bg-input)', translationKey: 'contests.draft', defaultLabel: 'Draft' },
  PUBLISHED: { color: 'var(--primary)', bg: 'var(--primary-soft)', translationKey: 'contests.upcoming', defaultLabel: 'Upcoming' },
  UPCOMING: { color: 'var(--primary)', bg: 'var(--primary-soft)', translationKey: 'contests.upcoming', defaultLabel: 'Upcoming' },
  ONGOING: { color: 'var(--success)', bg: 'var(--success-soft)', translationKey: 'contests.ongoing', defaultLabel: 'Ongoing' },
  ENDED: { color: 'var(--text-subtle)', bg: 'var(--bg-input)', translationKey: 'contests.ended', defaultLabel: 'Ended' },
};

const Contests = () => {
  const { t } = useTranslation();
  const [contests, setContests] = useState([]);
  const [registrations, setRegistrations] = useState({}); // { [contestId]: boolean }
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [now, setNow] = useState(() => new Date());

  // Lightweight periodic update so contest statuses transition cleanly in real-time
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 10000);
    return () => clearInterval(timer);
  }, []);

  const loadContests = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await contestService.getAvailableContests();
      setContests(data);
      if (Array.isArray(data) && data.length > 0) {
        const firstWithTime = data.find((c) => c.serverTime || c.serverTimeIso);
        if (firstWithTime) {
          syncServerTime(firstWithTime.serverTime || firstWithTime.serverTimeIso);
        }
      }

      // Fetch each contest's registration status in parallel so cards can show at a
      // glance whether the current user is already registered. The backend determines
      // the user from the JWT - no user id is sent from here.
      const statusEntries = await Promise.all(
        data.map(async (c) => {
          try {
            const status = await contestService.getRegistrationStatus(c.id);
            return [c.id, status.registered];
          } catch {
            return [c.id, false];
          }
        })
      );
      setRegistrations(Object.fromEntries(statusEntries));
    } catch (err) {
      setError('Unable to load contests. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContests();
  }, []);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.875rem', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Flag size={26} color="var(--primary)" /> {t('contests.title', 'Contests')}
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>{t('contests.subtitle', 'Participate in coding contests and test your skills.')}</p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem 0', color: 'var(--text-muted)' }}>{t('contests.loading', 'Loading contests...')}</div>
      ) : error ? (
        <div className="card">
          <div className="alert alert-error" style={{ marginBottom: '1rem' }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
          <button onClick={loadContests} className="btn btn-outline btn-sm">
            <RefreshCw size={14} />
            <span>{t('common.retry', 'Retry')}</span>
          </button>
        </div>
      ) : contests.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <Flag size={40} />
            <p>{t('contests.noContests', 'No contests available right now.')}</p>
          </div>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.25rem' }}>
          {contests.map((contest) => {
            const displayStatus = getContestDisplayStatus(contest, now);
            const statusStyle = STATUS_STYLES[displayStatus] || STATUS_STYLES.ENDED;
            const isRegistered = registrations[contest.id];
            return (
              <Link
                key={contest.id}
                to={`/contests/${contest.id}`}
                className="card"
                style={{ display: 'block', textDecoration: 'none', color: 'inherit' }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem', gap: '0.5rem' }}>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>{contest.title}</h3>
                  <span style={{
                    fontSize: '0.7rem', fontWeight: 700, padding: '0.2rem 0.55rem', borderRadius: '9999px',
                    color: statusStyle.color, backgroundColor: statusStyle.bg, whiteSpace: 'nowrap',
                  }}>
                    {t(statusStyle.translationKey, statusStyle.defaultLabel)}
                  </span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
                  <span>{contest.organizationName}</span>
                </div>

                {contest.description && (
                  <p style={{
                    color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '0.75rem',
                    display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden',
                  }}>
                    {contest.description}
                  </p>
                )}

                <div style={{ marginTop: '0.5rem', marginBottom: '0.5rem' }}>
                  <ContestCountdown contest={contest} onStatusChange={() => loadContests()} compact={true} />
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-subtle)', fontSize: '0.8rem', marginBottom: '0.4rem' }}>
                  <Calendar size={13} />
                  <span>{formatContestSchedule(contest.startTime, contest.endTime)}</span>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.75rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-color)' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                    <List size={13} />
                    {contest.problems?.length || 0} {(contest.problems?.length || 0) === 1 ? t('contests.problem', 'problem') : t('contests.problemsCount', 'problems')}
                  </span>
                  {isRegistered && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: 'var(--success)', fontSize: '0.8rem', fontWeight: 600 }}>
                      <CheckCircle size={13} /> {t('contests.registered', 'Registered')}
                    </span>
                  )}
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Contests;
