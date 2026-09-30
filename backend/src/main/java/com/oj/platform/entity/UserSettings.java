package com.oj.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 20)
    private String language = "en";

    @Column(nullable = false)
    private boolean emailNotifications = true;

    @Column(nullable = false)
    private boolean contestNotifications = true;

    @Column(nullable = false)
    private boolean assessmentNotifications = true;

    @Column(nullable = false)
    private boolean platformUpdates = true;

    @Column(nullable = false, length = 20)
    private String theme = "LIGHT";

    @Column(nullable = false)
    private boolean showProfile = true;

    @Column(nullable = false)
    private boolean showLeaderboard = true;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public UserSettings() {
    }

    public UserSettings(User user) {
        this.user = user;
        this.language = "en";
        this.emailNotifications = true;
        this.contestNotifications = true;
        this.assessmentNotifications = true;
        this.platformUpdates = true;
        this.theme = "LIGHT";
        this.showProfile = true;
        this.showLeaderboard = true;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
