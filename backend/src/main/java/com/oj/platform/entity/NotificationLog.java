package com.oj.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Audit log entity tracking every outbound email notification event.
 * Records delivery outcome, error reasons, timestamps, and provides idempotency
 * guarantees against duplicate email dispatches.
 */
@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notif_user_type_entity", columnList = "user_id, notification_type, related_entity_id"),
        @Index(name = "idx_notif_status", columnList = "status"),
        @Index(name = "idx_notif_created_at", columnList = "created_at")
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @NotBlank
    @Column(name = "notification_type", nullable = false, length = 60)
    private String notificationType;

    @Column(name = "related_entity_id", length = 80)
    private String relatedEntityId;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String status; // SENT, FAILED, NOT_CONFIGURED, SKIPPED

    @Column(length = 250)
    private String subject;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public NotificationLog() {
    }

    public NotificationLog(Long userId, String recipientEmail, String notificationType,
                           String relatedEntityId, String status, String subject,
                           String errorMessage, LocalDateTime sentAt) {
        this.userId = userId;
        this.recipientEmail = recipientEmail;
        this.notificationType = notificationType;
        this.relatedEntityId = relatedEntityId;
        this.status = status;
        this.subject = subject;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if ("SENT".equalsIgnoreCase(this.status) && this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
