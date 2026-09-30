package com.oj.platform.entity;

/** Question type for an AssessmentQuestion (Task 10). Both current types are graded the
 *  same way (single correct AssessmentOption) - TRUE_FALSE is really just an MCQ
 *  restricted to two options, kept as its own value so the frontend can render it
 *  differently. Deliberately small; add new values here as future types are needed. */
public enum AssessmentQuestionType {
    MCQ,
    TRUE_FALSE,
    PROGRAMMING
}
