package com.oj.platform.service;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.dto.NotificationLogDto;
import com.oj.platform.entity.*;

import java.util.List;

/**
 * Abstraction over outbound email so the rest of the app (and its tests) never touch
 * JavaMailSender directly. See EmailServiceImpl for the SMTP-backed implementation,
 * its delivery tracking, idempotency enforcement, user preference checking, and
 * graceful failure handling.
 */
public interface EmailService {

    /**
     * Sends the Account Email Verification email containing a secure, time-limited
     * verification link to the user's registered email address.
     */
    EmailResult sendAccountVerificationEmail(User user, String verificationLink);

    /**
     * Sends the Welcome email to newly registered users upon account creation or verification.
     */
    EmailResult sendWelcomeEmail(User user);

    /**
     * Sends the Assessment Host verification-code email to the user's registered email.
     */
    EmailResult sendHostVerificationCode(User user, String plainTextCode, int expiryMinutes);

    EmailResult sendHostVerificationCode(User user, AssessmentHostVerification verification, String plainTextCode, int expiryMinutes);

    default EmailResult sendHostVerificationCode(User user, AssessmentHostVerification verification, String plainTextCode, int expiryMinutes, String token) {
        return sendHostVerificationCode(user, verification, plainTextCode, expiryMinutes);
    }

    /**
     * Sends confirmation when a host submits/resubmits organization verification credentials.
     */
    EmailResult sendHostVerificationSubmittedEmail(User user, AssessmentHostVerification verification);

    /**
     * Sends notification when an admin approves a candidate host.
     */
    EmailResult sendHostVerificationApprovedEmail(User user, AssessmentHostVerification verification);

    /**
     * Sends notification when a host verification request is rejected with reason.
     */
    EmailResult sendHostVerificationRejectedEmail(User user, AssessmentHostVerification verification, String rejectionReason);

    /**
     * Sends assessment final scorecard and performance breakdown upon attempt completion.
     */
    EmailResult sendAssessmentResultEmail(User user, Assessment assessment, AssessmentAttempt attempt);

    /**
     * Sends notification to the host when candidate feedback is received.
     */
    EmailResult sendAssessmentFeedbackEmail(User host, Assessment assessment, AssessmentFeedback feedback);

    /**
     * Sends notification to the host when a candidate reports an issue with an assessment question.
     */
    EmailResult sendAssessmentQuestionReportEmail(User host, Assessment assessment, AssessmentQuestionFeedback report);

    /**
     * Sends announcement email when a new contest is published.
     */
    EmailResult sendContestAnnouncementEmail(User user, Contest contest);

    /**
     * Sends confirmation email when a user registers for a contest.
     */
    EmailResult sendContestRegistrationEmail(User user, Contest contest);

    /**
     * Sends final ranking and score summary when contest rankings are finalized.
     */
    EmailResult sendContestResultEmail(User user, Contest contest, Integer rank, Integer totalParticipants, Integer score);

    /**
     * Sends certificate achievement notification with verification link.
     */
    EmailResult sendCertificateIssuedEmail(User user, Certificate certificate);

    /**
     * Sends support ticket created notification.
     */
    EmailResult sendSupportTicketCreatedEmail(User user, SupportRequest request);

    /**
     * Sends support reply notification when admin responds.
     */
    EmailResult sendSupportAdminReplyEmail(User user, SupportRequest request, SupportMessage message);

    /**
     * Sends notification when support ticket status changes.
     */
    EmailResult sendSupportStatusChangedEmail(User user, SupportRequest request, String oldStatus, String newStatus);

    /**
     * Sends notification when support ticket is resolved.
     */
    EmailResult sendSupportTicketResolvedEmail(User user, SupportRequest request);

    /**
     * Sends notification when support ticket is reopened.
     */
    EmailResult sendSupportTicketReopenedEmail(User user, SupportRequest request);

    /**
     * Sends notification when support ticket is closed.
     */
    EmailResult sendSupportTicketClosedEmail(User user, SupportRequest request);

    /**
     * Sends a real-time diagnostic test email to verify SMTP connectivity.
     */
    EmailResult sendTestEmail(String recipientEmail, String note);

    /**
     * Retrieves recent email delivery audit logs for monitoring.
     */
    List<NotificationLogDto> getRecentLogs();

    /**
     * Computes aggregate delivery statistics.
     */
    EmailStatsDto getEmailStats();

    enum EmailStatus {
        SENT,
        NOT_CONFIGURED,
        FAILED,
        SKIPPED
    }

    class EmailResult {
        private final EmailStatus status;
        private final String message;

        public EmailResult(EmailStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public EmailStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSent() {
            return status == EmailStatus.SENT;
        }
    }
}
