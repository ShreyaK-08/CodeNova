package com.oj.platform.entity;

/** Status for a ContestAttempt (Task 8): NOT_STARTED is the default before an attempt
 *  row is created (not persisted in that state - creation always sets IN_PROGRESS or,
 *  if the contest already ended, COMPLETED directly), IN_PROGRESS while the contest
 *  window is open, COMPLETED once endTime has passed (set lazily). */
public enum ContestAttemptStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
