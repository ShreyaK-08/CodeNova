package com.oj.platform.dto;

import java.util.List;

/**
 * Response for running/submitting code against test cases in an assessment.
 * For hidden test cases, input/expectedOutput are NOT returned — only counts.
 */
public class AssessmentCodeRunResponse {

    private boolean submitForGrading;
    private int passedTests;
    private int totalTests;
    private Double programmingScore;
    private boolean compilationError;
    private String compilationErrorMessage;
    /** Sample test results (only for visible/sample tests). */
    private List<TestResult> testResults;
    /** Custom-input output (only when run against custom input). */
    private String customOutput;
    private String customError;

    public static class TestResult {
        private int testNumber;
        private boolean hidden;
        private boolean passed;
        private String input;       // null if hidden
        private String expected;    // null if hidden
        private String actual;
        private Long executionTimeMs;
        private String error;

        public int getTestNumber() { return testNumber; }
        public void setTestNumber(int testNumber) { this.testNumber = testNumber; }
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getExpected() { return expected; }
        public void setExpected(String expected) { this.expected = expected; }
        public String getActual() { return actual; }
        public void setActual(String actual) { this.actual = actual; }
        public Long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public boolean isSubmitForGrading() { return submitForGrading; }
    public void setSubmitForGrading(boolean submitForGrading) { this.submitForGrading = submitForGrading; }
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    public Double getProgrammingScore() { return programmingScore; }
    public void setProgrammingScore(Double programmingScore) { this.programmingScore = programmingScore; }
    public boolean isCompilationError() { return compilationError; }
    public void setCompilationError(boolean compilationError) { this.compilationError = compilationError; }
    public String getCompilationErrorMessage() { return compilationErrorMessage; }
    public void setCompilationErrorMessage(String compilationErrorMessage) { this.compilationErrorMessage = compilationErrorMessage; }
    public List<TestResult> getTestResults() { return testResults; }
    public void setTestResults(List<TestResult> testResults) { this.testResults = testResults; }
    public String getCustomOutput() { return customOutput; }
    public void setCustomOutput(String customOutput) { this.customOutput = customOutput; }
    public String getCustomError() { return customError; }
    public void setCustomError(String customError) { this.customError = customError; }
}
