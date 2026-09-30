package com.oj.platform.dto;

public class AssessmentCodeRunRequest {
    private Long questionId;
    private String language;
    private String code;
    /** Optional custom input for the "Run" action (not against all test cases). */
    private String customInput;
    /** true = run against all test cases and save score; false = run against custom input only */
    private boolean submitForGrading = false;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCustomInput() { return customInput; }
    public void setCustomInput(String customInput) { this.customInput = customInput; }
    public boolean isSubmitForGrading() { return submitForGrading; }
    public void setSubmitForGrading(boolean submitForGrading) { this.submitForGrading = submitForGrading; }
}
