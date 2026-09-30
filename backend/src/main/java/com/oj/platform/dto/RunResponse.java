package com.oj.platform.dto;

import com.oj.platform.entity.SubmissionStatus;

import java.util.List;

/**
 * Response for POST /api/submissions/run — executes code against only the
 * problem's VISIBLE (non-hidden) test cases and returns a per-case breakdown.
 * Nothing is persisted to the database; this is purely for the student to
 * sanity-check their code before using Submit.
 */
public class RunResponse {

    private SubmissionStatus status;
    private int passedTestCases;
    private int totalTestCases;
    private int failedTestCases;
    private Long executionTime;
    private String errorMessage;
    private List<TestCaseResultDto> results;

    public RunResponse() {
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public int getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(int passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(int totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public int getFailedTestCases() {
        return Math.max(0, totalTestCases - passedTestCases);
    }

    public void setFailedTestCases(int failedTestCases) {
        this.failedTestCases = failedTestCases;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<TestCaseResultDto> getResults() {
        return results;
    }

    public void setResults(List<TestCaseResultDto> results) {
        this.results = results;
    }
}
