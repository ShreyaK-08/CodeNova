package com.oj.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AssessmentAttemptDto {
    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    /** Server-enforced deadline (startedAt + assessment.durationMinutes). */
    private LocalDateTime deadlineAt;

    private Integer score;
    private Integer mcqScore;
    private Double programmingScore;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Integer unansweredCount;
    private Integer programmingAttempted;
    private Integer programmingSolved;
    private Integer violationCount;
    private Integer attemptNumber;
    private Integer maxAttempts;

    private String status;
    private Integer totalMarks;
    private Integer passingMarks;
    /** Null until the attempt is COMPLETED or EXPIRED. */
    private Boolean passed;

    private String candidateUsername;
    private String candidateFullName;
    private String candidateEmail;

    /** Answer key (isCorrect/explanation) hidden while status is IN_PROGRESS. */
    private List<AssessmentQuestionDto> questions;
    /** The candidate's own recorded MCQ answers so far. */
    private List<AssessmentAnswerDto> answers;
    /** Previous attempts summary (for multi-attempt assessments). */
    private List<AttemptSummaryDto> attemptHistory;

    // ── Nested DTO for attempt history items ─────────────────────────────

    public static class AttemptSummaryDto {
        private Long id;
        private Integer attemptNumber;
        private String status;
        private Integer score;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Boolean passed;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getAttemptNumber() { return attemptNumber; }
        public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public Boolean getPassed() { return passed; }
        public void setPassed(Boolean passed) { this.passed = passed; }
    }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getAssessmentTitle() { return assessmentTitle; }
    public void setAssessmentTitle(String assessmentTitle) { this.assessmentTitle = assessmentTitle; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }

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

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public String getCandidateUsername() { return candidateUsername; }
    public void setCandidateUsername(String candidateUsername) { this.candidateUsername = candidateUsername; }

    public String getCandidateFullName() { return candidateFullName; }
    public void setCandidateFullName(String candidateFullName) { this.candidateFullName = candidateFullName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public List<AssessmentQuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<AssessmentQuestionDto> questions) { this.questions = questions; }

    public List<AssessmentAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<AssessmentAnswerDto> answers) { this.answers = answers; }

    public List<AttemptSummaryDto> getAttemptHistory() { return attemptHistory; }
    public void setAttemptHistory(List<AttemptSummaryDto> attemptHistory) { this.attemptHistory = attemptHistory; }
}
