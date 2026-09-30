package com.oj.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AssessmentDto {
    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer passingMarks;
    private String status;
    private Integer questionCount;
    private String createdByUsername;
    private String hostEmail;
    private String hostName;
    private String organizationName;
    private Integer totalAttempts;
    private Integer candidateCount;
    private boolean hostedByOtherUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── New fields ────────────────────────────────────────────────────────
    private Integer maxAttempts;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean negativeMarkingEnabled;
    private Double negativeMarkingValue;
    private Boolean requireCandidateDetails;
    private String candidateFieldsConfig;

    /** Populated only for the host/admin authoring view - left null elsewhere. */
    private List<AssessmentQuestionDto> questions;

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public String getHostEmail() { return hostEmail; }
    public void setHostEmail(String hostEmail) { this.hostEmail = hostEmail; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public Integer getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; }

    public Integer getCandidateCount() { return candidateCount; }
    public void setCandidateCount(Integer candidateCount) { this.candidateCount = candidateCount; }

    public boolean isHostedByOtherUser() { return hostedByOtherUser; }
    public void setHostedByOtherUser(boolean hostedByOtherUser) { this.hostedByOtherUser = hostedByOtherUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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

    public List<AssessmentQuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<AssessmentQuestionDto> questions) { this.questions = questions; }
}
