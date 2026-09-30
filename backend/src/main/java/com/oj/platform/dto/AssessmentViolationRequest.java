package com.oj.platform.dto;

public class AssessmentViolationRequest {
    private String violationType;
    private String description;

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
