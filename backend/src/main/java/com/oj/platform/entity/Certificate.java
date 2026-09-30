package com.oj.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * An earned achievement certificate for a user reaching a solved-problem milestone
 * (every 50 distinct ACCEPTED problems). Created exclusively by CertificateService's
 * milestone logic - never inserted directly - so its existence always reflects a real,
 * calculated achievement.
 */
@Entity
@Table(name = "certificates", uniqueConstraints = {
        @UniqueConstraint(name = "uq_certificate_user_title", columnNames = {"user_id", "title"}),
        @UniqueConstraint(name = "uq_certificate_number", columnNames = {"certificate_number"}),
        @UniqueConstraint(name = "uq_certificate_verification_code", columnNames = {"verification_code"})
})
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The milestone reached: 1, 10, 25, 50, 100, etc. */
    @NotNull
    @Column(nullable = false)
    private Integer milestone;

    /** Human-facing certificate number, e.g. "CN-2026-000001". */
    @NotBlank
    @Column(name = "certificate_number", nullable = false, length = 40)
    private String certificateNumber;

    /** e.g. "50 Problems Solved" or "Java Problem Solver". */
    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    /** Bronze / Silver / Gold / Platinum / Diamond, based on milestone tier. */
    @Column(name = "achievement_level", length = 30)
    private String achievementLevel;

    /** Snapshot of the recipient's display name at issuance time. */
    @NotBlank
    @Column(name = "recipient_name", nullable = false, length = 200)
    private String recipientName;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Public, opaque code used by /verify-certificate/{code}. */
    @NotBlank
    @Column(name = "verification_code", nullable = false, length = 40)
    private String verificationCode;

    /** Null for overall milestones; language name (e.g. "Java", "Python", "C++") for language certificates. */
    @Column(length = 50)
    private String language;

    /** "OVERALL" or "LANGUAGE" */
    @Column(length = 50)
    private String category = "OVERALL";

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    public Certificate() {
    }

    public Certificate(User user, Integer milestone, String certificateNumber, String title,
                        String achievementLevel, String recipientName, String description,
                        String verificationCode) {
        this(user, milestone, certificateNumber, title, achievementLevel, recipientName, description, verificationCode, null, "OVERALL");
    }

    public Certificate(User user, Integer milestone, String certificateNumber, String title,
                        String achievementLevel, String recipientName, String description,
                        String verificationCode, String language, String category) {
        this.user = user;
        this.milestone = milestone;
        this.certificateNumber = certificateNumber;
        this.title = title;
        this.achievementLevel = achievementLevel;
        this.recipientName = recipientName;
        this.description = description;
        this.verificationCode = verificationCode;
        this.language = language;
        this.category = category != null ? category : "OVERALL";
    }

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getMilestone() {
        return milestone;
    }

    public void setMilestone(Integer milestone) {
        this.milestone = milestone;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAchievementLevel() {
        return achievementLevel;
    }

    public void setAchievementLevel(String achievementLevel) {
        this.achievementLevel = achievementLevel;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
