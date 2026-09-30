package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Records one code submission made by a candidate during an assessment attempt.
 * Kept separate from the global Submission entity so:
 *   - assessment submissions don't pollute the public submissions list
 *   - hidden test-case results are never exposed through public APIs
 *   - scoring metadata (passedTests, totalTests, programmingScore) is stored per-submission
 */
@Entity
@Table(name = "assessment_programming_submissions")
public class AssessmentProgrammingSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnoreProperties("programmingSubmissions")
    private AssessmentAttempt attempt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private AssessmentQuestion question;

    @NotNull
    @Column(nullable = false, length = 50)
    private String language;

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(name = "passed_tests", nullable = false)
    private Integer passedTests = 0;

    @Column(name = "total_tests", nullable = false)
    private Integer totalTests = 0;

    /** Proportional score = (passedTests / totalTests) * question.marks */
    @Column(name = "programming_score", nullable = false)
    private Double programmingScore = 0.0;

    /** RUN_ONLY = candidate clicked "Run" (sample tests only); FULL = clicked "Submit" */
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false, length = 20)
    private SubmissionType submissionType = SubmissionType.FULL;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_output", columnDefinition = "TEXT")
    private String errorOutput;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        if (this.submittedAt == null) this.submittedAt = LocalDateTime.now();
    }

    public enum SubmissionType { RUN_ONLY, FULL }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AssessmentAttempt getAttempt() { return attempt; }
    public void setAttempt(AssessmentAttempt attempt) { this.attempt = attempt; }

    public AssessmentQuestion getQuestion() { return question; }
    public void setQuestion(AssessmentQuestion question) { this.question = question; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getPassedTests() { return passedTests; }
    public void setPassedTests(Integer passedTests) { this.passedTests = passedTests; }

    public Integer getTotalTests() { return totalTests; }
    public void setTotalTests(Integer totalTests) { this.totalTests = totalTests; }

    public Double getProgrammingScore() { return programmingScore; }
    public void setProgrammingScore(Double programmingScore) { this.programmingScore = programmingScore; }

    public SubmissionType getSubmissionType() { return submissionType; }
    public void setSubmissionType(SubmissionType submissionType) { this.submissionType = submissionType; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getErrorOutput() { return errorOutput; }
    public void setErrorOutput(String errorOutput) { this.errorOutput = errorOutput; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
