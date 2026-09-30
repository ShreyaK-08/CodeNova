package com.oj.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAssessmentRequest {

    @NotBlank(message = "Assessment title is required")
    private String title;

    private String description;

    private String instructions;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Passing marks is required")
    @Min(value = 0, message = "Passing marks cannot be negative")
    private Integer passingMarks;

    /** Optional - defaults to DRAFT on create. */
    private String status;

    /** Maximum attempts allowed (default 1). */
    @Min(value = 1, message = "maxAttempts must be at least 1")
    private Integer maxAttempts = 1;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean negativeMarkingEnabled = false;
    private Double negativeMarkingValue = 0.0;

    private Boolean requireCandidateDetails = false;

    /** JSON array string e.g. ["fullName","email","organization"] */
    private String candidateFieldsConfig;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Boolean getNegativeMarkingEnabled() { return negativeMarkingEnabled; }
    public void setNegativeMarkingEnabled(Boolean negativeMarkingEnabled) { this.negativeMarkingEnabled = negativeMarkingEnabled; }

    public Double getNegativeMarkingValue() { return negativeMarkingValue; }
    public void setNegativeMarkingValue(Double negativeMarkingValue) { this.negativeMarkingValue = negativeMarkingValue; }

    public Boolean getRequireCandidateDetails() { return requireCandidateDetails; }
    public void setRequireCandidateDetails(Boolean requireCandidateDetails) { this.requireCandidateDetails = requireCandidateDetails; }

    public String getCandidateFieldsConfig() { return candidateFieldsConfig; }
    public void setCandidateFieldsConfig(String candidateFieldsConfig) { this.candidateFieldsConfig = candidateFieldsConfig; }
}
