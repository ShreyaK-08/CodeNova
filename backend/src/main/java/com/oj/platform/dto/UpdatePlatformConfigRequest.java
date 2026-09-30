package com.oj.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdatePlatformConfigRequest {

    @Size(max = 100)
    private String platformName;

    @Size(max = 200)
    private String platformType;

    @Size(max = 50)
    private String environment;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    private Integer maxAssessmentDurationMinutes;

    @Min(value = 1, message = "Attempts must be at least 1")
    private Integer maxAssessmentAttemptsDefault;

    private Boolean proctoringEnabledDefault;
    private Boolean programmingQuestionsEnabled;
    private Boolean contestCreationEnabled;
    private Boolean contestNotificationsEnabled;
    private Boolean contestProctoringAvailable;
    private Boolean emailNotificationsEnabled;
    private Boolean contestAnnouncementNotificationsEnabled;
    private Boolean hostVerificationNotificationsEnabled;
    private Boolean maintenanceMode;

    public UpdatePlatformConfigRequest() {
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getMaxAssessmentDurationMinutes() {
        return maxAssessmentDurationMinutes;
    }

    public void setMaxAssessmentDurationMinutes(Integer maxAssessmentDurationMinutes) {
        this.maxAssessmentDurationMinutes = maxAssessmentDurationMinutes;
    }

    public Integer getMaxAssessmentAttemptsDefault() {
        return maxAssessmentAttemptsDefault;
    }

    public void setMaxAssessmentAttemptsDefault(Integer maxAssessmentAttemptsDefault) {
        this.maxAssessmentAttemptsDefault = maxAssessmentAttemptsDefault;
    }

    public Boolean getProctoringEnabledDefault() {
        return proctoringEnabledDefault;
    }

    public void setProctoringEnabledDefault(Boolean proctoringEnabledDefault) {
        this.proctoringEnabledDefault = proctoringEnabledDefault;
    }

    public Boolean getProgrammingQuestionsEnabled() {
        return programmingQuestionsEnabled;
    }

    public void setProgrammingQuestionsEnabled(Boolean programmingQuestionsEnabled) {
        this.programmingQuestionsEnabled = programmingQuestionsEnabled;
    }

    public Boolean getContestCreationEnabled() {
        return contestCreationEnabled;
    }

    public void setContestCreationEnabled(Boolean contestCreationEnabled) {
        this.contestCreationEnabled = contestCreationEnabled;
    }

    public Boolean getContestNotificationsEnabled() {
        return contestNotificationsEnabled;
    }

    public void setContestNotificationsEnabled(Boolean contestNotificationsEnabled) {
        this.contestNotificationsEnabled = contestNotificationsEnabled;
    }

    public Boolean getContestProctoringAvailable() {
        return contestProctoringAvailable;
    }

    public void setContestProctoringAvailable(Boolean contestProctoringAvailable) {
        this.contestProctoringAvailable = contestProctoringAvailable;
    }

    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public Boolean getContestAnnouncementNotificationsEnabled() {
        return contestAnnouncementNotificationsEnabled;
    }

    public void setContestAnnouncementNotificationsEnabled(Boolean contestAnnouncementNotificationsEnabled) {
        this.contestAnnouncementNotificationsEnabled = contestAnnouncementNotificationsEnabled;
    }

    public Boolean getHostVerificationNotificationsEnabled() {
        return hostVerificationNotificationsEnabled;
    }

    public void setHostVerificationNotificationsEnabled(Boolean hostVerificationNotificationsEnabled) {
        this.hostVerificationNotificationsEnabled = hostVerificationNotificationsEnabled;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}
