package com.oj.platform.dto;

public class PlatformConfigDto {

    private String platformName;
    private String platformType;
    private String environment;
    private int maxAssessmentDurationMinutes;
    private int maxAssessmentAttemptsDefault;
    private boolean proctoringEnabledDefault;
    private boolean programmingQuestionsEnabled;
    private boolean contestCreationEnabled;
    private boolean contestNotificationsEnabled;
    private boolean contestProctoringAvailable;
    private boolean emailNotificationsEnabled;
    private boolean contestAnnouncementNotificationsEnabled;
    private boolean hostVerificationNotificationsEnabled;
    private boolean maintenanceMode;

    public PlatformConfigDto() {
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

    public int getMaxAssessmentDurationMinutes() {
        return maxAssessmentDurationMinutes;
    }

    public void setMaxAssessmentDurationMinutes(int maxAssessmentDurationMinutes) {
        this.maxAssessmentDurationMinutes = maxAssessmentDurationMinutes;
    }

    public int getMaxAssessmentAttemptsDefault() {
        return maxAssessmentAttemptsDefault;
    }

    public void setMaxAssessmentAttemptsDefault(int maxAssessmentAttemptsDefault) {
        this.maxAssessmentAttemptsDefault = maxAssessmentAttemptsDefault;
    }

    public boolean isProctoringEnabledDefault() {
        return proctoringEnabledDefault;
    }

    public void setProctoringEnabledDefault(boolean proctoringEnabledDefault) {
        this.proctoringEnabledDefault = proctoringEnabledDefault;
    }

    public boolean isProgrammingQuestionsEnabled() {
        return programmingQuestionsEnabled;
    }

    public void setProgrammingQuestionsEnabled(boolean programmingQuestionsEnabled) {
        this.programmingQuestionsEnabled = programmingQuestionsEnabled;
    }

    public boolean isContestCreationEnabled() {
        return contestCreationEnabled;
    }

    public void setContestCreationEnabled(boolean contestCreationEnabled) {
        this.contestCreationEnabled = contestCreationEnabled;
    }

    public boolean isContestNotificationsEnabled() {
        return contestNotificationsEnabled;
    }

    public void setContestNotificationsEnabled(boolean contestNotificationsEnabled) {
        this.contestNotificationsEnabled = contestNotificationsEnabled;
    }

    public boolean isContestProctoringAvailable() {
        return contestProctoringAvailable;
    }

    public void setContestProctoringAvailable(boolean contestProctoringAvailable) {
        this.contestProctoringAvailable = contestProctoringAvailable;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public boolean isContestAnnouncementNotificationsEnabled() {
        return contestAnnouncementNotificationsEnabled;
    }

    public void setContestAnnouncementNotificationsEnabled(boolean contestAnnouncementNotificationsEnabled) {
        this.contestAnnouncementNotificationsEnabled = contestAnnouncementNotificationsEnabled;
    }

    public boolean isHostVerificationNotificationsEnabled() {
        return hostVerificationNotificationsEnabled;
    }

    public void setHostVerificationNotificationsEnabled(boolean hostVerificationNotificationsEnabled) {
        this.hostVerificationNotificationsEnabled = hostVerificationNotificationsEnabled;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}
