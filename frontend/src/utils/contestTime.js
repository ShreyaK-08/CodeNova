// Central place for contest timing and date formatting logic (Task 7 & Task 17).

/**
 * Safely parses an ISO string, date string, or Date object into a valid Date instance.
 * Returns null if invalid or missing.
 */
export const parseContestDate = (value) => {
  if (!value) return null;
  if (value instanceof Date) return isNaN(value.getTime()) ? null : value;
  if (typeof value === 'string') {
    // If the date string has a space separator instead of 'T' (e.g. "2026-09-15 11:23:09"),
    // normalize to ISO standard 'T' for consistent cross-browser parsing.
    const normalized = value.includes(' ') && !value.includes('T') ? value.replace(' ', 'T') : value;
    const d = new Date(normalized);
    if (!isNaN(d.getTime())) return d;
  }
  const fallback = new Date(value);
  return isNaN(fallback.getTime()) ? null : fallback;
};

let serverOffsetMs = 0;

/**
 * Synchronizes client clock with server clock using serverTime returned by backend.
 */
export const syncServerTime = (serverTimeValue) => {
  if (!serverTimeValue) return;
  const serverDate = parseContestDate(serverTimeValue);
  if (serverDate && !isNaN(serverDate.getTime())) {
    serverOffsetMs = serverDate.getTime() - Date.now();
  }
};

/**
 * Returns the current time adjusted by the server time offset.
 */
export const getServerNow = () => {
  return new Date(Date.now() + serverOffsetMs);
};

/**
 * Returns student-facing display status: 'UPCOMING' | 'ONGOING' | 'ENDED' | 'DRAFT'
 * according to the exact model:
 *   now < start              → UPCOMING
 *   now >= start && now < end → ONGOING
 *   now >= end               → ENDED
 */
export const getContestDisplayStatus = (contest, now = getServerNow()) => {
  if (!contest) return 'ENDED';
  if (contest.status === 'DRAFT') return 'DRAFT';
  if (contest.status === 'ENDED') return 'ENDED';

  const start = parseContestDate(contest.startTime);
  const end = parseContestDate(contest.endTime);
  const nowMs = now instanceof Date ? now.getTime() : new Date(now).getTime();

  if (end && nowMs >= end.getTime()) {
    return 'ENDED';
  }
  if (start && nowMs < start.getTime()) {
    return 'UPCOMING';
  }
  if (start && nowMs >= start.getTime() && (!end || nowMs < end.getTime())) {
    return 'ONGOING';
  }

  return contest.status || 'UPCOMING';
};

/**
 * Returns one of 'not-started' | 'ongoing' | 'ended' for contest participation,
 * based on the real current time compared against startTime/endTime.
 */
export const getContestTimeState = (contest, now = getServerNow()) => {
  if (!contest) return 'not-started';
  if (contest.status === 'ENDED') return 'ended';

  const start = parseContestDate(contest.startTime);
  const end = parseContestDate(contest.endTime);
  const nowMs = now instanceof Date ? now.getTime() : new Date(now).getTime();

  if (end && nowMs >= end.getTime()) return 'ended';
  if (start && nowMs < start.getTime()) return 'not-started';
  return 'ongoing';
};

/** Milliseconds remaining until startTime; 0 if already started/invalid. */
export const getMillisUntilStart = (contest, now = getServerNow()) => {
  const start = parseContestDate(contest?.startTime);
  if (!start) return 0;
  const nowMs = now instanceof Date ? now.getTime() : new Date(now).getTime();
  return Math.max(0, start.getTime() - nowMs);
};

/** Milliseconds remaining until endTime; 0 if already past/invalid. */
export const getMillisRemaining = (contest, now = getServerNow()) => {
  const end = parseContestDate(contest?.endTime);
  if (!end) return 0;
  const nowMs = now instanceof Date ? now.getTime() : new Date(now).getTime();
  return Math.max(0, end.getTime() - nowMs);
};

/** Formats a millisecond duration as HH:MM:SS (hours can exceed 24, e.g. "36:04:12"). */
export const formatCountdown = (millis) => {
  const totalSeconds = Math.max(0, Math.floor(millis / 1000));
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
};

/**
 * Formats a single date/time in clean student-friendly format (without seconds),
 * e.g. "Sep 15, 2026, 4:53 PM".
 */
export const formatContestDateTime = (value) => {
  const d = parseContestDate(value);
  if (!d) return '-';
  const dateStr = d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  const timeStr = d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
  return `${dateStr}, ${timeStr}`;
};

/**
 * Formats contest schedule cleanly:
 * Same day: "Sep 15, 2026 • 4:53 PM – 6:53 PM"
 * Spanning multiple days: "Sep 15, 2026, 11:00 AM → Sep 16, 2026, 1:00 PM"
 */
export const formatContestSchedule = (startTime, endTime) => {
  const start = parseContestDate(startTime);
  const end = parseContestDate(endTime);

  if (!start && !end) return '-';
  if (start && !end) return formatContestDateTime(start);
  if (!start && end) return formatContestDateTime(end);

  const isSameDay =
    start.getFullYear() === end.getFullYear() &&
    start.getMonth() === end.getMonth() &&
    start.getDate() === end.getDate();

  const formatDate = (d) =>
    d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  const formatTime = (d) =>
    d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });

  if (isSameDay) {
    return `${formatDate(start)} • ${formatTime(start)} – ${formatTime(end)}`;
  } else {
    return `${formatDate(start)}, ${formatTime(start)} → ${formatDate(end)}, ${formatTime(end)}`;
  }
};
