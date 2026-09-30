package com.oj.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_config")
public class PlatformConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String platformName = "CodeNova";

    @Column(nullable = false, length = 200)
    private String platformType = "Online Coding & Skill Assessment Platform";

    @Column(nullable = false, length = 50)
    private String environment = "Production";

    @Column(nullable = false)
    private int maxAssessmentDurationMinutes = 180;

    @Column(nullable = false)
    private int maxAssessmentAttemptsDefault = 3;

    @Column(nullable = false)
    private boolean proctoringEnabledDefault = true;

    @Column(nullable = false)
    private boolean programmingQuestionsEnabled = true;

    @Column(nullable = false)
    private boolean contestCreationEnabled = true;

    @Column(nullable = false)
    private boolean contestNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean contestProctoringAvailable = true;

    @Column(nullable = false)
    private boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean contestAnnouncementNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean hostVerificationNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean maintenanceMode = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PlatformConfig() {
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
