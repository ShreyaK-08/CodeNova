package com.oj.platform.dto;

import com.oj.platform.entity.SubmissionStatus;
import java.time.LocalDateTime;

public class SubmissionResponse {

    private Long id;
    private Long problemId;
    private String problemTitle;
    private Long userId;
    private String username;
    private String language;
    private String code;
    private SubmissionStatus status;
    private Long executionTime;
    private Long memoryUsed;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private Integer failedTestCases;
    private LocalDateTime submittedAt;
    private String errorMessage;
    private java.util.List<TestCaseResultDto> results;
    /** Nullable (Task 8) - non-null only when this submission was made under a contest. */
    private Long contestId;

    public SubmissionResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(Integer passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(Integer totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public Integer getFailedTestCases() {
        if (failedTestCases != null) {
            return failedTestCases;
        }
        if (totalTestCases != null && passedTestCases != null) {
            return Math.max(0, totalTestCases - passedTestCases);
        }
        return 0;
    }

    public void setFailedTestCases(Integer failedTestCases) {
        this.failedTestCases = failedTestCases;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public java.util.List<TestCaseResultDto> getResults() {
        return results;
    }

    public void setResults(java.util.List<TestCaseResultDto> results) {
        this.results = results;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }
}
