package com.oj.platform.entity;

/**
 * Lifecycle status of a user's Assessment Host verification request.
 *
 * PENDING   - document submitted, awaiting admin review.
 * APPROVED  - admin approved the document; a one-time verification code has been
 *             generated and (attempted to be) emailed, but the user has not yet
 *             entered it.
 * REJECTED  - admin rejected the document (see rejectionReason); user may resubmit,
 *             which moves the same request back to PENDING with a new document.
 * VERIFIED  - user entered the correct code before it expired; they may now host
 *             (create/publish) assessments.
 */
public enum HostVerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    VERIFIED
}
