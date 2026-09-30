package com.oj.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserSettingsRequest {

    @NotBlank(message = "Language is required")
    @Size(max = 20)
    private String language = "en";

    private Boolean emailNotifications = true;
    private Boolean contestNotifications = true;
    private Boolean assessmentNotifications = true;
    private Boolean platformUpdates = true;
    private String theme = "LIGHT";
    private Boolean showProfile = true;
    private Boolean showLeaderboard = true;

    public UpdateUserSettingsRequest() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getContestNotifications() {
        return contestNotifications;
    }

    public void setContestNotifications(Boolean contestNotifications) {
        this.contestNotifications = contestNotifications;
    }

    public Boolean getAssessmentNotifications() {
        return assessmentNotifications;
    }

    public void setAssessmentNotifications(Boolean assessmentNotifications) {
        this.assessmentNotifications = assessmentNotifications;
    }

    public Boolean getPlatformUpdates() {
        return platformUpdates;
    }

    public void setPlatformUpdates(Boolean platformUpdates) {
        this.platformUpdates = platformUpdates;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Boolean getShowProfile() {
        return showProfile;
    }

    public void setShowProfile(Boolean showProfile) {
        this.showProfile = showProfile;
    }

    public Boolean getShowLeaderboard() {
        return showLeaderboard;
    }

    public void setShowLeaderboard(Boolean showLeaderboard) {
        this.showLeaderboard = showLeaderboard;
    }
}
