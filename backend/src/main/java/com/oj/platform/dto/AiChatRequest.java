package com.oj.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 4000, message = "Message exceeds maximum allowed length of 4000 characters")
    private String message;

    @Size(max = 50, message = "Language code is too long")
    private String language;

    @Size(max = 255, message = "Page path is too long")
    private String page;

    @Size(max = 2000, message = "Problem context is too long")
    private String problem;

    @Size(max = 50, message = "Programming language is too long")
    private String programmingLanguage;

    @Size(max = 30000, message = "Code snippet exceeds maximum allowed length of 30000 characters")
    private String code;

    @Size(max = 4000, message = "Error context is too long")
    private String error;

    @Size(max = 4000, message = "Test results context is too long")
    private String testResults;

    @Size(max = 2000, message = "Assessment context is too long")
    private String assessment;

    @Size(max = 2000, message = "Contest context is too long")
    private String contest;

    public AiChatRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getProgrammingLanguage() {
        return programmingLanguage;
    }

    public void setProgrammingLanguage(String programmingLanguage) {
        this.programmingLanguage = programmingLanguage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTestResults() {
        return testResults;
    }

    public void setTestResults(String testResults) {
        this.testResults = testResults;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }

    public String getContest() {
        return contest;
    }

    public void setContest(String contest) {
        this.contest = contest;
    }
}
