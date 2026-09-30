package com.oj.platform.entity;

/** Status for an AssessmentAttempt (Task 10): IN_PROGRESS while within the assessment's
 *  duration window, COMPLETED once the student has submitted (score/correctAnswers are
 *  final at that point), EXPIRED if the duration elapsed before a submission was made
 *  (server-detected, never trusts the browser timer - see AssessmentService). */
public enum AssessmentAttemptStatus {
    IN_PROGRESS,
    COMPLETED,
    EXPIRED
}
