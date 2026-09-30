import React, { useState, useEffect, useRef } from 'react';
import {
  parseContestDate,
  syncServerTime,
  getServerNow,
  getContestDisplayStatus,
  getMillisUntilStart,
  getMillisRemaining,
  formatCountdown,
} from '../../utils/contestTime';
import { Clock, PlayCircle, CheckCircle, Flag } from 'lucide-react';

const formatLongCountdown = (millis) => {
  const totalSeconds = Math.max(0, Math.floor(millis / 1000));
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const pad = (n) => String(n).padStart(2, '0');

  if (days > 0) {
    return `${days}d ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  }
  return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

const ContestCountdown = ({ contest, onStatusChange, compact = false }) => {
  const [, setTick] = useState(0);
  const lastStatusRef = useRef(null);

  useEffect(() => {
    if (contest?.serverTime || contest?.serverTimeIso) {
      syncServerTime(contest.serverTime || contest.serverTimeIso);
    }
  }, [contest?.serverTime, contest?.serverTimeIso]);

  useEffect(() => {
    const timer = setInterval(() => {
      const now = getServerNow();
      const currentStatus = getContestDisplayStatus(contest, now);
      if (lastStatusRef.current && lastStatusRef.current !== currentStatus) {
        lastStatusRef.current = currentStatus;
        onStatusChange?.(currentStatus);
      } else {
        lastStatusRef.current = currentStatus;
      }
      setTick((t) => (t + 1) % 10000);
    }, 1000);

    return () => clearInterval(timer);
  }, [contest, onStatusChange]);

  const now = getServerNow();
  const status = getContestDisplayStatus(contest, now);

  if (status === 'UPCOMING') {
    const untilStart = getMillisUntilStart(contest, now);
    return (
      <div
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.4rem',
          padding: compact ? '0.2rem 0.55rem' : '0.35rem 0.75rem',
          borderRadius: '9999px',
          backgroundColor: 'rgba(99, 102, 241, 0.12)',
          color: 'var(--primary)',
          fontSize: compact ? '0.75rem' : '0.85rem',
          fontWeight: 700,
          fontVariantNumeric: 'tabular-nums',
        }}
      >
        <Clock size={compact ? 13 : 15} />
        <span>Starts in: {formatLongCountdown(untilStart)}</span>
      </div>
    );
  }

  if (status === 'ONGOING') {
    const remaining = getMillisRemaining(contest, now);
    return (
      <div
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.4rem',
          padding: compact ? '0.2rem 0.55rem' : '0.35rem 0.75rem',
          borderRadius: '9999px',
          backgroundColor: 'rgba(16, 185, 129, 0.12)',
          color: 'var(--success)',
          fontSize: compact ? '0.75rem' : '0.85rem',
          fontWeight: 700,
          fontVariantNumeric: 'tabular-nums',
        }}
      >
        <PlayCircle size={compact ? 13 : 15} />
        <span>Ends in: {formatCountdown(remaining)}</span>
      </div>
    );
  }

  if (status === 'ENDED') {
    return (
      <div
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.35rem',
          padding: compact ? '0.2rem 0.55rem' : '0.35rem 0.75rem',
          borderRadius: '9999px',
          backgroundColor: 'var(--bg-input)',
          color: 'var(--text-subtle)',
          fontSize: compact ? '0.75rem' : '0.85rem',
          fontWeight: 600,
        }}
      >
        <CheckCircle size={compact ? 13 : 15} />
        <span>Contest Ended</span>
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.35rem',
        padding: compact ? '0.2rem 0.55rem' : '0.35rem 0.75rem',
        borderRadius: '9999px',
        backgroundColor: 'var(--bg-input)',
        color: 'var(--text-muted)',
        fontSize: compact ? '0.75rem' : '0.85rem',
        fontWeight: 600,
      }}
    >
      <Flag size={compact ? 13 : 15} />
      <span>{status || 'Draft'}</span>
    </div>
  );
};

export default ContestCountdown;
