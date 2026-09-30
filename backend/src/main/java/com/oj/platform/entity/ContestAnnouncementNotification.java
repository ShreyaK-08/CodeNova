package com.oj.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks individual contest announcement delivery per recipient.
 * Ensures duplicate emails are never sent if a contest announcement is triggered again.
 */
@Entity
@Table(name = "contest_announcement_notifications",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_contest_recipient_user", columnNames = {"contest_id", "recipient_user_id"}),
           @UniqueConstraint(name = "uk_contest_recipient_email", columnNames = {"contest_id", "recipient_email"})
       })
 public class ContestAnnouncementNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // "SENT" or "FAILED"

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public ContestAnnouncementNotification() {
    }

    public ContestAnnouncementNotification(Long contestId, Long recipientUserId, String recipientEmail,
                                           LocalDateTime sentAt, String status, String errorMessage) {
        this.contestId = contestId;
        this.recipientUserId = recipientUserId;
        this.recipientEmail = recipientEmail;
        this.sentAt = sentAt;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
