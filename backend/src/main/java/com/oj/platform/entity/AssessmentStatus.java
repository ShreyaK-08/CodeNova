package com.oj.platform.entity;

/** Lifecycle status for an Assessment (Task 10): DRAFT while an admin is still
 *  authoring it (never visible to students), PUBLISHED once students can start
 *  attempts, ARCHIVED once retired (existing attempts/results remain intact). */
public enum AssessmentStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}
