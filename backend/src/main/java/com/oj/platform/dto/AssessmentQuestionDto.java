package com.oj.platform.dto;

import java.util.List;

public class AssessmentQuestionDto {
    private Long id;
    private String questionText;
    private String questionType;
    private Integer marks;
    private Integer orderIndex;
    /** Null while hidden (IN_PROGRESS); populated for host/admin views and after COMPLETED/EXPIRED. */
    private String explanation;
    private List<AssessmentOptionDto> options;

    /**
     * Populated for PROGRAMMING type questions.
     * For candidates: hides hidden test-case inputs/outputs.
     * For hosts: includes all details plus test-case counts.
     */
    private ProgrammingQuestionDto programmingQuestion;

    /** For candidates: best submission result for this programming question in the current attempt. */
    private ProgrammingSubmissionResultDto latestSubmission;

    // ── Nested DTO for latest submission result ───────────────────────────

    public static class ProgrammingSubmissionResultDto {
        private Long id;
        private Integer passedTests;
        private Integer totalTests;
        private Double programmingScore;
        private Long executionTimeMs;
        private String language;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getPassedTests() { return passedTests; }
        public void setPassedTests(Integer passedTests) { this.passedTests = passedTests; }
        public Integer getTotalTests() { return totalTests; }
        public void setTotalTests(Integer totalTests) { this.totalTests = totalTests; }
        public Double getProgrammingScore() { return programmingScore; }
        public void setProgrammingScore(Double programmingScore) { this.programmingScore = programmingScore; }
        public Long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<AssessmentOptionDto> getOptions() { return options; }
    public void setOptions(List<AssessmentOptionDto> options) { this.options = options; }

    public ProgrammingQuestionDto getProgrammingQuestion() { return programmingQuestion; }
    public void setProgrammingQuestion(ProgrammingQuestionDto programmingQuestion) { this.programmingQuestion = programmingQuestion; }

    public ProgrammingSubmissionResultDto getLatestSubmission() { return latestSubmission; }
    public void setLatestSubmission(ProgrammingSubmissionResultDto latestSubmission) { this.latestSubmission = latestSubmission; }
}
