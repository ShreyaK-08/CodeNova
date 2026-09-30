package com.oj.platform.dto;

import java.time.LocalDateTime;

public class ContestAttemptDto {
    private Long contestId;
    private Integer score;
    private Integer problemsSolved;
    private Integer submissionCount;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    /** Task 9 - current fullscreen/tab-switch violation count and the configured
     *  threshold, so the frontend can show "Security warnings: X / Y" immediately on
     *  entry/refresh without waiting for a violation to occur. */
    private Integer securityViolationCount;
    private Integer maxViolations;

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getSecurityViolationCount() {
        return securityViolationCount;
    }

    public void setSecurityViolationCount(Integer securityViolationCount) {
        this.securityViolationCount = securityViolationCount;
    }

    public Integer getMaxViolations() {
        return maxViolations;
    }

    public void setMaxViolations(Integer maxViolations) {
        this.maxViolations = maxViolations;
    }
}
