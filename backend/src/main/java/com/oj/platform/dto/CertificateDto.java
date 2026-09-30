package com.oj.platform.dto;

import java.time.LocalDateTime;

public class CertificateDto {
    private Long id;
    private Integer milestone;
    private String certificateNumber;
    private String title;
    private String achievementLevel;
    private String recipientName;
    private String description;
    private String verificationCode;
    private LocalDateTime issuedAt;
    // Admin-only view convenience fields
    private String username;
    private String userEmail;

    private String language;
    private String category;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMilestone() { return milestone; }
    public void setMilestone(Integer milestone) { this.milestone = milestone; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAchievementLevel() { return achievementLevel; }
    public void setAchievementLevel(String achievementLevel) { this.achievementLevel = achievementLevel; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
