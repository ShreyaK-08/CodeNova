package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks one candidate's attempt at an Assessment.
 *
 * Multi-attempt design:
 *  - The old unique constraint (user_id, assessment_id) is REMOVED.
 *  - Instead, attemptNumber is tracked, and the service enforces maxAttempts.
 *  - Existing single-attempt assessments (maxAttempts=1) continue to work unchanged.
 *
 * New scoring fields:
 *  - mcqScore          : sum of marks awarded for MCQ/TRUE_FALSE questions
 *  - programmingScore  : sum of proportional marks for PROGRAMMING questions
 *  - score             : mcqScore + programmingScore  (server-computed, never client-supplied)
 *  - incorrectAnswers  : number of MCQ answers that were wrong
 *  - unansweredCount   : questions left blank
 *  - programmingAttempted : how many programming questions the candidate submitted code for
 *  - programmingSolved    : how many programming questions scored > 0
 */
@Entity
@Table(name = "assessment_attempts")
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "email"})
    private User user;

    /** 1-based. First attempt = 1, second = 2, … */
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Total score = mcqScore + programmingScore. Always server-computed. */
    @Column(nullable = false)
    private Integer score = 0;

    @Column(name = "mcq_score", nullable = false)
    private Integer mcqScore = 0;

    @Column(name = "programming_score", nullable = false)
    private Double programmingScore = 0.0;

    /** Snapshotted at start time. */
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 0;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers = 0;

    @Column(name = "incorrect_answers", nullable = false)
    private Integer incorrectAnswers = 0;

    @Column(name = "unanswered_count", nullable = false)
    private Integer unansweredCount = 0;

    @Column(name = "programming_attempted", nullable = false)
    private Integer programmingAttempted = 0;

    @Column(name = "programming_solved", nullable = false)
    private Integer programmingSolved = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentAttemptStatus status = AssessmentAttemptStatus.IN_PROGRESS;

    /** Total violation events recorded during this attempt. */
    @Column(name = "violation_count", nullable = false)
    private Integer violationCount = 0;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("attempt")
    private List<AssessmentViolation> violations = new ArrayList<>();

    public AssessmentAttempt() {}

    public AssessmentAttempt(Assessment assessment, User user) {
        this.assessment = assessment;
        this.user = user;
    }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMcqScore() { return mcqScore; }
    public void setMcqScore(Integer mcqScore) { this.mcqScore = mcqScore; }

    public Double getProgrammingScore() { return programmingScore; }
    public void setProgrammingScore(Double programmingScore) { this.programmingScore = programmingScore; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getIncorrectAnswers() { return incorrectAnswers; }
    public void setIncorrectAnswers(Integer incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }

    public Integer getUnansweredCount() { return unansweredCount; }
    public void setUnansweredCount(Integer unansweredCount) { this.unansweredCount = unansweredCount; }

    public Integer getProgrammingAttempted() { return programmingAttempted; }
    public void setProgrammingAttempted(Integer programmingAttempted) { this.programmingAttempted = programmingAttempted; }

    public Integer getProgrammingSolved() { return programmingSolved; }
    public void setProgrammingSolved(Integer programmingSolved) { this.programmingSolved = programmingSolved; }

    public AssessmentAttemptStatus getStatus() { return status; }
    public void setStatus(AssessmentAttemptStatus status) { this.status = status; }

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }

    public List<AssessmentViolation> getViolations() { return violations; }
    public void setViolations(List<AssessmentViolation> violations) { this.violations = violations; }
}
