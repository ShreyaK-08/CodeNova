package com.oj.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Tracks a registered participant's attempt at a contest: start/completion time, score,
 * problems solved, and a security-violation counter reserved for future exam-security
 * work (still not read/written anywhere - out of scope for Task 8). Task 8 wires this
 * entity into real contest submission scoring; the shape was already prepared by an
 * earlier task.
 */
@Entity
@Table(name = "contest_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_contest_attempt_user_contest", columnNames = {"user_id", "contest_id"})
})
public class ContestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User participant;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "problems_solved", nullable = false)
    private Integer problemsSolved = 0;

    /** COUNT(contest submissions) for this participant in this contest - every contest
     *  submission counts here regardless of status, unlike problemsSolved/score which
     *  only reflect distinct ACCEPTED problems. */
    @Column(name = "submission_count", nullable = false)
    private Integer submissionCount = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContestAttemptStatus status = ContestAttemptStatus.NOT_STARTED;

    @Column(name = "security_violation_count", nullable = false)
    private Integer securityViolationCount = 0;

    public ContestAttempt() {
    }

    public ContestAttempt(User participant, Contest contest) {
        this.participant = participant;
        this.contest = contest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getParticipant() {
        return participant;
    }

    public void setParticipant(User participant) {
        this.participant = participant;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
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

    public ContestAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(ContestAttemptStatus status) {
        this.status = status;
    }

    public Integer getSecurityViolationCount() {
        return securityViolationCount;
    }

    public void setSecurityViolationCount(Integer securityViolationCount) {
        this.securityViolationCount = securityViolationCount;
    }
}
