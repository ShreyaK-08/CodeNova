package com.oj.platform.dto;

public class UserSettingsDto {

    private String username;
    private String email;
    private String role;
    private String accountStatus;
    private String language;
    private boolean emailNotifications;
    private boolean contestNotifications;
    private boolean assessmentNotifications;
    private boolean platformUpdates;
    private String theme;
    private boolean showProfile;
    private boolean showLeaderboard;

    public UserSettingsDto() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isContestNotifications() {
        return contestNotifications;
    }

    public void setContestNotifications(boolean contestNotifications) {
        this.contestNotifications = contestNotifications;
    }

    public boolean isAssessmentNotifications() {
        return assessmentNotifications;
    }

    public void setAssessmentNotifications(boolean assessmentNotifications) {
        this.assessmentNotifications = assessmentNotifications;
    }

    public boolean isPlatformUpdates() {
        return platformUpdates;
    }

    public void setPlatformUpdates(boolean platformUpdates) {
        this.platformUpdates = platformUpdates;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isShowProfile() {
        return showProfile;
    }

    public void setShowProfile(boolean showProfile) {
        this.showProfile = showProfile;
    }

    public boolean isShowLeaderboard() {
        return showLeaderboard;
    }

    public void setShowLeaderboard(boolean showLeaderboard) {
        this.showLeaderboard = showLeaderboard;
    }
}
