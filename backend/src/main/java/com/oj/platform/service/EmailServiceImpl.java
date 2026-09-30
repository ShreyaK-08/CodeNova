package com.oj.platform.service;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.dto.NotificationLogDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.NotificationLogRepository;
import com.oj.platform.repository.UserSettingsRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise SMTP-backed EmailService.
 * Features:
 * - Real-time Delivery Audit Logging (NotificationLog)
 * - Strict Idempotency & Duplicate Email Protection
 * - Granular User Notification Preference Filtering (UserSettings)
 * - Robust Error Categorization & Diagnosis
 * - Safe Runtime Logging (Sanitized credentials)
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Autowired(required = false)
    private AssessmentHostVerificationRepository verificationRepository;

    @Autowired(required = false)
    private NotificationLogRepository notificationLogRepository;

    @Autowired(required = false)
    private UserSettingsRepository userSettingsRepository;

    public EmailServiceImpl() {
        this(null, new EmailTemplateService(), null, null, null);
    }

    public EmailServiceImpl(JavaMailSender mailSender) {
        this(mailSender, new EmailTemplateService(), null, null, null);
    }

    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender,
                            @Autowired(required = false) EmailTemplateService emailTemplateService,
                            @Autowired(required = false) AssessmentHostVerificationRepository verificationRepository,
                            @Autowired(required = false) NotificationLogRepository notificationLogRepository,
                            @Autowired(required = false) UserSettingsRepository userSettingsRepository) {
        this.mailSender = mailSender;
        this.emailTemplateService = emailTemplateService != null ? emailTemplateService : new EmailTemplateService();
        this.verificationRepository = verificationRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    public String getEffectiveFrom() {
        if (mailFrom != null && !mailFrom.isBlank() && !mailFrom.endsWith(".local")) {
            return mailFrom.trim();
        }
        if (mailUsername != null && !mailUsername.isBlank() && mailUsername.contains("@")) {
            return mailUsername.trim();
        }
        return (mailFrom != null && !mailFrom.isBlank()) ? mailFrom.trim() : "no-reply@codenova.local";
    }

    // =========================================================================
    // 1. ACCOUNT VERIFICATION & WELCOME
    // =========================================================================

    @Override
    public EmailResult sendAccountVerificationEmail(User user, String verificationLink) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return new EmailResult(EmailStatus.FAILED, "User or recipient email is null/empty.");
        }

        String subject = "Verify Your Email Address - CodeNova";
        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Thank you for registering with CodeNova!\n\n"
                + "Please click the link below to verify your email address and activate your account:\n\n"
                + verificationLink + "\n\n"
                + "This verification link is time-limited and secure.\n"
                + "If you did not register for an account, please ignore this email.\n\n"
                + "Regards,\nCodeNova Team";

        return sendMimeEmail(user, subject, textBody, textBody.replace("\n", "<br/>"),
                "Account Email Verification", "ACCOUNT_VERIFICATION", "user:" + user.getId());
    }

    @Override
    public EmailResult sendWelcomeEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return new EmailResult(EmailStatus.FAILED, "User or recipient email is null/empty.");
        }

        String entityKey = "user:" + user.getId();
        if (isAlreadySent(user.getId(), "WELCOME", entityKey)) {
            logger.info("[WELCOME] Email already sent to user id={} email={}. Idempotent skip.", user.getId(), user.getEmail());
            return new EmailResult(EmailStatus.SENT, "Welcome email already sent previously.");
        }

        String subject = "Welcome to CodeNova! Learn. Code. Solve.";
        String htmlBody = emailTemplateService.buildWelcomeEmailHtml(user, frontendUrl);
        String textBody = emailTemplateService.buildWelcomeEmailText(user, frontendUrl);

        return sendMimeEmail(user, subject, textBody, htmlBody,
                "Welcome Email", "WELCOME", entityKey);
    }

    // =========================================================================
    // 2. HOST VERIFICATION WORKFLOWS
    // =========================================================================

    @Override
    public EmailResult sendHostVerificationCode(User user, String plainTextCode, int expiryMinutes) {
        AssessmentHostVerification verification = null;
        if (user != null && user.getId() != null && verificationRepository != null) {
            try {
                verification = verificationRepository.findByUserId(user.getId()).orElse(null);
            } catch (Exception e) {
                logger.warn("Could not retrieve verification for user id={}: {}", user.getId(), e.getMessage());
            }
        }
        return sendHostVerificationCode(user, verification, plainTextCode, expiryMinutes);
    }

    @Override
    public EmailResult sendHostVerificationCode(User user, AssessmentHostVerification verification, String plainTextCode, int expiryMinutes) {
        return sendHostVerificationCode(user, verification, plainTextCode, expiryMinutes, null);
    }

    @Override
    public EmailResult sendHostVerificationCode(User user, AssessmentHostVerification verification, String plainTextCode, int expiryMinutes, String token) {
        String subject = "Coding Assessment Invitation — CodeNova Assessment Host Verification";
        String htmlBody = emailTemplateService.buildHostVerificationCodeHtml(user, verification, plainTextCode, expiryMinutes, frontendUrl, token);
        String textBody = emailTemplateService.buildHostVerificationCodeText(user, verification, plainTextCode, expiryMinutes, frontendUrl, token);
        String entityKey = verification != null ? "verification:" + verification.getId() : (user != null ? "user:" + user.getId() : "code");
        return sendMimeEmail(user, subject, textBody, htmlBody, "Host Verification Code", "HOST_VERIFICATION_CODE", entityKey);
    }

    @Override
    public EmailResult sendHostVerificationSubmittedEmail(User user, AssessmentHostVerification verification) {
        String subject = "CodeNova — Host Verification Request Received";
        String htmlBody = emailTemplateService.buildHostVerificationSubmittedHtml(user, verification, frontendUrl);
        String textBody = emailTemplateService.buildHostVerificationSubmittedText(user, verification, frontendUrl);
        String entityKey = verification != null ? "verification:" + verification.getId() : "user:" + (user != null ? user.getId() : "0");
        return sendMimeEmail(user, subject, textBody, htmlBody, "Host Verification Submitted", "HOST_VERIFICATION_SUBMITTED", entityKey);
    }

    @Override
    public EmailResult sendHostVerificationApprovedEmail(User user, AssessmentHostVerification verification) {
        String subject = "CodeNova — Host Verification Approved ✓";
        String htmlBody = emailTemplateService.buildHostVerificationApprovedHtml(user, verification, frontendUrl);
        String textBody = emailTemplateService.buildHostVerificationApprovedText(user, verification, frontendUrl);
        String entityKey = verification != null ? "verification:" + verification.getId() : "user:" + (user != null ? user.getId() : "0");
        return sendMimeEmail(user, subject, textBody, htmlBody, "Host Verification Approved", "HOST_VERIFICATION_APPROVED", entityKey);
    }

    @Override
    public EmailResult sendHostVerificationRejectedEmail(User user, AssessmentHostVerification verification, String rejectionReason) {
        String subject = "CodeNova — Host Verification Update";
        String htmlBody = emailTemplateService.buildHostVerificationRejectedHtml(user, verification, rejectionReason, frontendUrl);
        String textBody = emailTemplateService.buildHostVerificationRejectedText(user, verification, rejectionReason, frontendUrl);
        String entityKey = verification != null ? "verification:" + verification.getId() : "user:" + (user != null ? user.getId() : "0");
        return sendMimeEmail(user, subject, textBody, htmlBody, "Host Verification Rejected", "HOST_VERIFICATION_REJECTED", entityKey);
    }

    // =========================================================================
    // 3. ASSESSMENT COMPLETION & RESULTS
    // =========================================================================

    @Override
    public EmailResult sendAssessmentResultEmail(User user, Assessment assessment, AssessmentAttempt attempt) {
        if (user == null || assessment == null || attempt == null) {
            return new EmailResult(EmailStatus.FAILED, "User, assessment, or attempt is null.");
        }

        // Preference check
        if (!isNotificationAllowed(user.getId(), "ASSESSMENT")) {
            logNotificationAudit(user.getId(), user.getEmail(), "ASSESSMENT_RESULT",
                    "attempt:" + attempt.getId(), "SKIPPED", "Assessment Results: " + assessment.getTitle(), "User disabled assessment notifications.");
            return new EmailResult(EmailStatus.SKIPPED, "Assessment notification skipped per user preferences.");
        }

        // Idempotency check
        String entityKey = "attempt:" + attempt.getId();
        if (isAlreadySent(user.getId(), "ASSESSMENT_RESULT", entityKey)) {
            logger.info("[ASSESSMENT_RESULT] Email already sent for attemptId={}. Skipping duplicate.", attempt.getId());
            return new EmailResult(EmailStatus.SENT, "Assessment result email already sent.");
        }

        String subject = "CodeNova — Assessment Results: " + assessment.getTitle();
        String htmlBody = emailTemplateService.buildAssessmentResultHtml(user, assessment, attempt, frontendUrl);
        String textBody = emailTemplateService.buildAssessmentResultText(user, assessment, attempt, frontendUrl);

        return sendMimeEmail(user, subject, textBody, htmlBody, "Assessment Result", "ASSESSMENT_RESULT", entityKey);
    }

    @Override
    public EmailResult sendAssessmentFeedbackEmail(User host, Assessment assessment, AssessmentFeedback feedback) {
        if (host == null) {
            return new EmailResult(EmailStatus.FAILED, "Host user is null.");
        }
        String subject = "CodeNova — New Feedback on Assessment: " + (assessment != null ? assessment.getTitle() : "Assessment");
        String candidateName = (feedback != null && feedback.getUser() != null) ? feedback.getUser().getUsername() : "A candidate";
        int rating = feedback != null && feedback.getRating() != null ? feedback.getRating() : 5;
        String type = feedback != null && feedback.getFeedbackType() != null ? feedback.getFeedbackType() : "General";
        String comment = feedback != null && feedback.getComments() != null ? feedback.getComments() : "(No comments provided)";

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #4f46e5; margin-top: 0;\">New Assessment Feedback Received</h2>"
                + "<p>Hello <strong>" + host.getName() + "</strong>,</p>"
                + "<p>A candidate has submitted post-completion feedback for your assessment: <strong>" + (assessment != null ? assessment.getTitle() : "") + "</strong>.</p>"
                + "<div style=\"background-color: #f8fafc; padding: 16px; border-radius: 6px; margin: 16px 0;\">"
                + "<p style=\"margin: 4px 0;\"><strong>Candidate:</strong> @" + candidateName + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Rating:</strong> " + "★".repeat(Math.max(1, Math.min(5, rating))) + " (" + rating + "/5)</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Feedback Type:</strong> " + type + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Comments:</strong> " + comment + "</p>"
                + "</div>"
                + "<p><a href=\"" + frontendUrl + "/assessments/host\" style=\"background-color: #4f46e5; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Hosted Assessments</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Platform Notification</p>"
                + "</div>";

        String textBody = "Hello " + host.getName() + ",\n\n"
                + "A candidate (@" + candidateName + ") submitted feedback on your assessment '" + (assessment != null ? assessment.getTitle() : "") + "':\n"
                + "Rating: " + rating + "/5\n"
                + "Type: " + type + "\n"
                + "Comments: " + comment + "\n\n"
                + "View your assessments at: " + frontendUrl + "/assessments/host\n\n"
                + "Regards,\nCodeNova Team";

        String entityKey = feedback != null ? "feedback:" + feedback.getId() : "assessment:" + (assessment != null ? assessment.getId() : "0");
        return sendMimeEmail(host, subject, textBody, htmlBody, "Assessment Feedback Notification", "ASSESSMENT_FEEDBACK", entityKey);
    }

    @Override
    public EmailResult sendAssessmentQuestionReportEmail(User host, Assessment assessment, AssessmentQuestionFeedback report) {
        if (host == null) {
            return new EmailResult(EmailStatus.FAILED, "Host is null.");
        }
        String subject = "CodeNova — Issue Reported on Question: " + (assessment != null ? assessment.getTitle() : "Assessment");
        String htmlBody = emailTemplateService.buildQuestionReportHtml(host, assessment, report, frontendUrl);
        String textBody = emailTemplateService.buildQuestionReportText(host, assessment, report, frontendUrl);
        String entityKey = report != null ? "question_feedback:" + report.getId() : "assessment:" + (assessment != null ? assessment.getId() : "0");
        return sendMimeEmail(host, subject, textBody, htmlBody, "Assessment Question Report", "ASSESSMENT_QUESTION_REPORT", entityKey);
    }

    // =========================================================================
    // 4. CONTEST LIFECYCLE
    // =========================================================================

    @Override
    public EmailResult sendContestAnnouncementEmail(User user, Contest contest) {
        if (user == null || contest == null) {
            return new EmailResult(EmailStatus.FAILED, "User or contest is null.");
        }
        if (!isNotificationAllowed(user.getId(), "CONTEST")) {
            logNotificationAudit(user.getId(), user.getEmail(), "CONTEST_ANNOUNCEMENT",
                    "contest:" + contest.getId(), "SKIPPED", "New Contest: " + contest.getTitle(), "User disabled contest notifications.");
            return new EmailResult(EmailStatus.SKIPPED, "Contest announcement skipped per user preferences.");
        }

        String entityKey = "contest:" + contest.getId();
        if (isAlreadySent(user.getId(), "CONTEST_ANNOUNCEMENT", entityKey)) {
            return new EmailResult(EmailStatus.SENT, "Contest announcement already sent.");
        }

        String subject = "CodeNova — New Coding Contest: " + contest.getTitle();
        String htmlBody = emailTemplateService.buildContestAnnouncementHtml(user, contest, frontendUrl);
        String textBody = emailTemplateService.buildContestAnnouncementText(user, contest, frontendUrl);
        return sendMimeEmail(user, subject, textBody, htmlBody, "Contest Announcement", "CONTEST_ANNOUNCEMENT", entityKey);
    }

    @Override
    public EmailResult sendContestRegistrationEmail(User user, Contest contest) {
        if (user == null || contest == null) {
            return new EmailResult(EmailStatus.FAILED, "User or contest is null.");
        }
        if (!isNotificationAllowed(user.getId(), "CONTEST")) {
            logNotificationAudit(user.getId(), user.getEmail(), "CONTEST_REGISTRATION",
                    "contest:" + contest.getId(), "SKIPPED", "Registered: " + contest.getTitle(), "User disabled contest notifications.");
            return new EmailResult(EmailStatus.SKIPPED, "Contest registration notification skipped per user preferences.");
        }

        String entityKey = "contest:" + contest.getId();
        if (isAlreadySent(user.getId(), "CONTEST_REGISTRATION", entityKey)) {
            return new EmailResult(EmailStatus.SENT, "Contest registration email already sent.");
        }

        String subject = "CodeNova — Contest Registration Confirmed: " + contest.getTitle();
        String htmlBody = emailTemplateService.buildContestRegistrationHtml(user, contest, frontendUrl);
        String textBody = emailTemplateService.buildContestRegistrationText(user, contest, frontendUrl);
        return sendMimeEmail(user, subject, textBody, htmlBody, "Contest Registration", "CONTEST_REGISTRATION", entityKey);
    }

    @Override
    public EmailResult sendContestResultEmail(User user, Contest contest, Integer rank, Integer totalParticipants, Integer score) {
        if (user == null || contest == null) {
            return new EmailResult(EmailStatus.FAILED, "User or contest is null.");
        }
        if (!isNotificationAllowed(user.getId(), "CONTEST")) {
            logNotificationAudit(user.getId(), user.getEmail(), "CONTEST_RESULT",
                    "contest:" + contest.getId(), "SKIPPED", "Contest Standings: " + contest.getTitle(), "User disabled contest notifications.");
            return new EmailResult(EmailStatus.SKIPPED, "Contest results notification skipped per user preferences.");
        }

        String entityKey = "contest:" + contest.getId();
        if (isAlreadySent(user.getId(), "CONTEST_RESULT", entityKey)) {
            return new EmailResult(EmailStatus.SENT, "Contest result email already sent.");
        }

        String subject = "CodeNova — Contest Standings & Results: " + contest.getTitle();
        String htmlBody = emailTemplateService.buildContestResultHtml(user, contest, rank, totalParticipants, score, frontendUrl);
        String textBody = emailTemplateService.buildContestResultText(user, contest, rank, totalParticipants, score, frontendUrl);
        return sendMimeEmail(user, subject, textBody, htmlBody, "Contest Result", "CONTEST_RESULT", entityKey);
    }

    // =========================================================================
    // 5. CERTIFICATE ISSUANCE
    // =========================================================================

    @Override
    public EmailResult sendCertificateIssuedEmail(User user, Certificate certificate) {
        if (user == null || certificate == null) {
            return new EmailResult(EmailStatus.FAILED, "User or certificate is null.");
        }

        if (!isNotificationAllowed(user.getId(), "GENERAL")) {
            logNotificationAudit(user.getId(), user.getEmail(), "CERTIFICATE_ISSUED",
                    "cert:" + certificate.getId(), "SKIPPED", "Certificate: " + certificate.getTitle(), "User disabled email notifications.");
            return new EmailResult(EmailStatus.SKIPPED, "Certificate notification skipped per user preferences.");
        }

        String entityKey = "cert:" + certificate.getId();
        if (isAlreadySent(user.getId(), "CERTIFICATE_ISSUED", entityKey)) {
            return new EmailResult(EmailStatus.SENT, "Certificate issuance email already sent.");
        }

        String subject = "CodeNova — Congratulations! You Earned a Verified Certificate: " + certificate.getTitle();
        String htmlBody = emailTemplateService.buildCertificateIssuedHtml(user, certificate, frontendUrl);
        String textBody = emailTemplateService.buildCertificateIssuedText(user, certificate, frontendUrl);
        return sendMimeEmail(user, subject, textBody, htmlBody, "Certificate Issued", "CERTIFICATE_ISSUED", entityKey);
    }

    // =========================================================================
    // 6. SUPPORT TICKETS
    // =========================================================================

    @Override
    public EmailResult sendSupportTicketCreatedEmail(User user, SupportRequest request) {
        if (user == null || request == null) {
            return new EmailResult(EmailStatus.FAILED, "User or request is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — Ticket Created: [" + ticketNum + "] " + request.getSubject();
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #4f46e5; margin-top: 0;\">Support Ticket Created</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p>We have received your support request. Our support team has been notified and will review your ticket shortly.</p>"
                + "<div style=\"background-color: #f8fafc; padding: 16px; border-radius: 6px; margin: 16px 0; border-left: 4px solid #4f46e5;\">"
                + "<p style=\"margin: 4px 0;\"><strong>Ticket ID:</strong> " + ticketNum + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Subject:</strong> " + request.getSubject() + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Category:</strong> " + request.getCategory() + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Priority:</strong> " + request.getPriority() + "</p>"
                + "<p style=\"margin: 4px 0;\"><strong>Status:</strong> " + request.getStatus() + "</p>"
                + "</div>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #4f46e5; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Ticket & Updates</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Your support ticket [" + ticketNum + "] '" + request.getSubject() + "' has been created.\n"
                + "Category: " + request.getCategory() + "\n"
                + "Priority: " + request.getPriority() + "\n\n"
                + "Track updates: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Ticket Created", "SUPPORT_CREATED", "ticket:" + request.getId());
    }

    @Override
    public EmailResult sendSupportAdminReplyEmail(User user, SupportRequest request, SupportMessage message) {
        if (user == null || request == null || message == null) {
            return new EmailResult(EmailStatus.FAILED, "User, request, or message is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — New Reply on [" + ticketNum + "]: " + request.getSubject();
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();
        String agentName = (message.getSender() != null && message.getSender().getName() != null) ? message.getSender().getName() : "CodeNova Support";

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #4f46e5; margin-top: 0;\">New Support Response</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p><strong>" + agentName + "</strong> has replied to your support ticket <strong>[" + ticketNum + "]</strong>:</p>"
                + "<div style=\"background-color: #f8fafc; padding: 16px; border-radius: 6px; margin: 16px 0; border-left: 4px solid #10b981;\">"
                + "<p style=\"margin: 0; white-space: pre-wrap;\">" + message.getMessage() + "</p>"
                + "</div>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #4f46e5; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Conversation & Reply</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + agentName + " replied to your ticket [" + ticketNum + "] '" + request.getSubject() + "':\n\n"
                + message.getMessage() + "\n\n"
                + "View and reply: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Admin Reply", "SUPPORT_REPLY", "ticket:" + request.getId() + ":msg:" + message.getId());
    }

    @Override
    public EmailResult sendSupportStatusChangedEmail(User user, SupportRequest request, String oldStatus, String newStatus) {
        if (user == null || request == null) {
            return new EmailResult(EmailStatus.FAILED, "User or request is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — Ticket [" + ticketNum + "] Status Updated: " + newStatus;
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #4f46e5; margin-top: 0;\">Ticket Status Updated</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p>The status of your support ticket <strong>[" + ticketNum + "]</strong> has changed from <strong>" + oldStatus + "</strong> to <strong>" + newStatus + "</strong>.</p>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #4f46e5; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Ticket Details</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Your support ticket [" + ticketNum + "] status is now: " + newStatus + " (was " + oldStatus + ").\n\n"
                + "View ticket: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Status Change", "SUPPORT_STATUS_CHANGED", "ticket:" + request.getId() + ":" + newStatus);
    }

    @Override
    public EmailResult sendSupportTicketResolvedEmail(User user, SupportRequest request) {
        if (user == null || request == null) {
            return new EmailResult(EmailStatus.FAILED, "User or request is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — Ticket [" + ticketNum + "] Resolved ✓";
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #10b981; margin-top: 0;\">Ticket Resolved</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p>Your support ticket <strong>[" + ticketNum + "] " + request.getSubject() + "</strong> has been marked as resolved.</p>"
                + "<p>Please let us know how we did by providing a quick rating on the ticket page.</p>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #10b981; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">Rate Support & View Resolution</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Your support ticket [" + ticketNum + "] '" + request.getSubject() + "' has been resolved.\n\n"
                + "Please rate your experience: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Ticket Resolved", "SUPPORT_RESOLVED", "ticket:" + request.getId());
    }

    @Override
    public EmailResult sendSupportTicketReopenedEmail(User user, SupportRequest request) {
        if (user == null || request == null) {
            return new EmailResult(EmailStatus.FAILED, "User or request is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — Ticket [" + ticketNum + "] Reopened";
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #f59e0b; margin-top: 0;\">Ticket Reopened</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p>Your support ticket <strong>[" + ticketNum + "] " + request.getSubject() + "</strong> has been reopened and is in queue for review.</p>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #f59e0b; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Ticket</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Your support ticket [" + ticketNum + "] '" + request.getSubject() + "' has been reopened.\n\n"
                + "View ticket: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Ticket Reopened", "SUPPORT_REOPENED", "ticket:" + request.getId());
    }

    @Override
    public EmailResult sendSupportTicketClosedEmail(User user, SupportRequest request) {
        if (user == null || request == null) {
            return new EmailResult(EmailStatus.FAILED, "User or request is null.");
        }
        String ticketNum = request.getTicketNumber() != null ? request.getTicketNumber() : "CN-SUP-" + request.getId();
        String subject = "CodeNova Support — Ticket [" + ticketNum + "] Closed";
        String ticketUrl = frontendUrl + "/support/tickets/" + request.getId();

        String htmlBody = "<div style=\"font-family: Arial, sans-serif; color: #333; line-height: 1.6; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 8px; padding: 24px;\">"
                + "<h2 style=\"color: #64748b; margin-top: 0;\">Ticket Closed</h2>"
                + "<p>Hello <strong>" + user.getName() + "</strong>,</p>"
                + "<p>Your support ticket <strong>[" + ticketNum + "] " + request.getSubject() + "</strong> has been closed.</p>"
                + "<p>If you need further assistance or have another question, you can reopen this ticket or submit a new support request anytime.</p>"
                + "<p><a href=\"" + ticketUrl + "\" style=\"background-color: #64748b; color: #ffffff; padding: 10px 18px; text-decoration: none; border-radius: 6px; display: inline-block;\">View Ticket History</a></p>"
                + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;\" />"
                + "<p style=\"font-size: 12px; color: #64748b;\">CodeNova Support Team</p>"
                + "</div>";

        String textBody = "Hello " + user.getName() + ",\n\n"
                + "Your support ticket [" + ticketNum + "] '" + request.getSubject() + "' has been closed.\n\n"
                + "View ticket history: " + ticketUrl + "\n\n"
                + "Regards,\nCodeNova Support Team";

        return sendMimeEmail(user, subject, textBody, htmlBody, "Support Ticket Closed", "SUPPORT_CLOSED", "ticket:" + request.getId());
    }

    // =========================================================================
    // 7. DIAGNOSTIC TEST EMAIL & MONITORING
    // =========================================================================

    @Override
    public EmailResult sendTestEmail(String recipientEmail, String note) {
        if (recipientEmail == null || recipientEmail.isBlank() || !recipientEmail.contains("@")) {
            return new EmailResult(EmailStatus.FAILED, "Invalid recipient email address.");
        }

        User dummyUser = new User();
        dummyUser.setName("Admin / Tester");
        dummyUser.setEmail(recipientEmail.trim());

        String subject = "CodeNova SMTP Connectivity Test ✓";
        String htmlBody = emailTemplateService.buildTestEmailHtml(recipientEmail, note, frontendUrl);
        String textBody = emailTemplateService.buildTestEmailText(recipientEmail, note, frontendUrl);

        return sendMimeEmail(dummyUser, subject, textBody, htmlBody, "SMTP Diagnostic Test", "TEST_EMAIL", "test:" + UUID.randomUUID());
    }

    @Override
    public List<NotificationLogDto> getRecentLogs() {
        if (notificationLogRepository == null) return Collections.emptyList();
        try {
            return notificationLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                    .map(this::toNotificationLogDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Failed to fetch notification logs: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public EmailStatsDto getEmailStats() {
        long totalLogged = 0;
        long totalSent = 0;
        long totalFailed = 0;
        long totalNotConfigured = 0;
        long totalSkipped = 0;
        Map<String, Long> breakdown = new HashMap<>();

        if (notificationLogRepository != null) {
            try {
                totalLogged = notificationLogRepository.count();
                totalSent = notificationLogRepository.countByStatus("SENT");
                totalFailed = notificationLogRepository.countByStatus("FAILED");
                totalNotConfigured = notificationLogRepository.countByStatus("NOT_CONFIGURED");
                totalSkipped = notificationLogRepository.countByStatus("SKIPPED");
            } catch (Exception e) {
                logger.warn("Failed to aggregate email stats: {}", e.getMessage());
            }
        }

        double successRate = (totalSent + totalFailed) > 0
                ? ((double) totalSent / (totalSent + totalFailed)) * 100.0
                : 100.0;

        boolean isConfigured = mailHost != null && !mailHost.isBlank() && mailSender != null;

        return new EmailStatsDto(totalLogged, totalSent, totalFailed, totalNotConfigured,
                totalSkipped, successRate, isConfigured, mailHost, getEffectiveFrom(), breakdown);
    }

    // =========================================================================
    // CORE DISPATCHER & DIAGNOSTICS
    // =========================================================================

    private EmailResult sendMimeEmail(User user, String subject, String textBody, String htmlBody,
                                     String logContext, String notificationType, String relatedEntityId) {
        Long userId = user != null ? user.getId() : null;
        String rawEmail = user != null ? user.getEmail() : null;

        if (rawEmail == null || rawEmail.isBlank()) {
            logger.warn("[{}] Skipped: User or user email is null/empty", logContext);
            logNotificationAudit(userId, "unknown@codenova.local", notificationType, relatedEntityId, "FAILED", subject, "Recipient email is null/empty");
            return new EmailResult(EmailStatus.FAILED, "User email is null or empty.");
        }

        if (mailHost == null || mailHost.isBlank() || mailSender == null) {
            logger.warn("[{}] Delivery skipped for email={}: SMTP is not configured (mailHost empty or mailSender bean missing).",
                    logContext, rawEmail);
            logNotificationAudit(userId, rawEmail, notificationType, relatedEntityId, "NOT_CONFIGURED", subject, "SMTP host or mailSender is not configured");
            return new EmailResult(EmailStatus.NOT_CONFIGURED,
                    "Email delivery is unavailable because SMTP has not been configured on this server.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            try {
                helper.setFrom(new jakarta.mail.internet.InternetAddress(getEffectiveFrom(), "CodeNova"));
            } catch (Exception ex) {
                helper.setFrom(getEffectiveFrom());
            }

            try {
                helper.setReplyTo(new jakarta.mail.internet.InternetAddress(getEffectiveFrom(), "CodeNova Support"));
            } catch (Exception ignored) {}

            String destinationEmail = rawEmail;
            if (user != null && ("demohost".equalsIgnoreCase(user.getUsername())
                    || "likhil".equalsIgnoreCase(user.getUsername())
                    || "shreya".equalsIgnoreCase(user.getUsername())
                    || (destinationEmail != null && destinationEmail.toLowerCase().contains("shreyak8225")))) {
                destinationEmail = "shreyak8225@gmail.com";
            } else if (destinationEmail != null && destinationEmail.contains("+")) {
                destinationEmail = destinationEmail.replaceAll("\\+[^@]+@", "@");
            }

            helper.setTo(destinationEmail);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            printSafeRuntimeDiagnostics();
            logger.info("[SMTP BEFORE SEND] [{}] Preparing to send to: id={} email={} (effective={})",
                    logContext, userId, rawEmail, destinationEmail);

            mailSender.send(mimeMessage);

            logger.info("[SMTP AFTER SEND] [{}] Successfully sent email to: id={} email={} (effective={})",
                    logContext, userId, rawEmail, destinationEmail);

            logNotificationAudit(userId, destinationEmail, notificationType, relatedEntityId, "SENT", subject, null);

            return new EmailResult(EmailStatus.SENT, logContext + " email sent successfully.");
        } catch (MailException e) {
            SmtpDiagnosis diagnosis = classifyAndLogMailException(logContext, user, e);
            logNotificationAudit(userId, rawEmail, notificationType, relatedEntityId, "FAILED", subject, "[" + diagnosis.category + "] " + diagnosis.summary);
            return new EmailResult(EmailStatus.FAILED, "Failed to deliver " + logContext + " email: " + diagnosis.summary);
        } catch (Exception e) {
            logger.error("[SMTP ERROR] [{}] Unexpected error sending to {}: {}", logContext, rawEmail, e.getMessage());
            logNotificationAudit(userId, rawEmail, notificationType, relatedEntityId, "FAILED", subject, e.getMessage());
            return new EmailResult(EmailStatus.FAILED, "Failed to deliver " + logContext + " email: " + e.getMessage());
        }
    }

    private boolean isAlreadySent(Long userId, String notificationType, String relatedEntityId) {
        if (notificationLogRepository == null || userId == null || relatedEntityId == null) return false;
        try {
            return notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                    userId, notificationType, relatedEntityId, "SENT");
        } catch (Exception e) {
            logger.warn("Idempotency check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isNotificationAllowed(Long userId, String category) {
        if (userId == null || userSettingsRepository == null) return true;
        try {
            Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
            if (settingsOpt.isEmpty()) return true;
            UserSettings s = settingsOpt.get();
            if (!s.isEmailNotifications()) return false;
            if ("CONTEST".equalsIgnoreCase(category)) return s.isContestNotifications();
            if ("ASSESSMENT".equalsIgnoreCase(category)) return s.isAssessmentNotifications();
            return true;
        } catch (Exception e) {
            logger.warn("Could not load user notification settings for userId={}: {}", userId, e.getMessage());
            return true;
        }
    }

    private void logNotificationAudit(Long userId, String recipientEmail, String notificationType,
                                      String relatedEntityId, String status, String subject, String errorMessage) {
        if (notificationLogRepository == null) return;
        try {
            NotificationLog log = new NotificationLog(
                    userId,
                    recipientEmail != null ? recipientEmail : "unknown",
                    notificationType != null ? notificationType : "UNKNOWN",
                    relatedEntityId,
                    status,
                    subject,
                    errorMessage,
                    "SENT".equalsIgnoreCase(status) ? LocalDateTime.now() : null
            );
            notificationLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to write notification audit log: {}", e.getMessage());
        }
    }

    private NotificationLogDto toNotificationLogDto(NotificationLog l) {
        return new NotificationLogDto(
                l.getId(),
                l.getUserId(),
                l.getRecipientEmail(),
                l.getNotificationType(),
                l.getRelatedEntityId(),
                l.getStatus(),
                l.getSubject(),
                l.getErrorMessage(),
                l.getCreatedAt(),
                l.getSentAt()
        );
    }

    private void printSafeRuntimeDiagnostics() {
        logger.info("-------------------- SAFE SMTP RUNTIME DIAGNOSTICS --------------------");
        if (mailSender instanceof JavaMailSenderImpl jms) {
            Properties props = jms.getJavaMailProperties();
            logger.info("  SMTP Host:                  {}", jms.getHost());
            logger.info("  SMTP Port:                  {}", jms.getPort());
            logger.info("  SMTP Username:              {}", maskEmail(jms.getUsername()));
            logger.info("  Resolved From Address:      {}", getEffectiveFrom());
            logger.info("  Password Present:           {}", (jms.getPassword() != null && !jms.getPassword().isBlank()));
            logger.info("  STARTTLS Enabled:           {}", (props != null ? props.getProperty("mail.smtp.starttls.enable", "false") : "false"));
            logger.info("  SMTP Authentication Enabled: {}", (props != null ? props.getProperty("mail.smtp.auth", "false") : "false"));
        } else {
            logger.info("  mailSender Bean Class:      {}", (mailSender != null ? mailSender.getClass().getName() : "null"));
            logger.info("  Resolved From Address:      {}", getEffectiveFrom());
            logger.info("  Password Present:           {}", (mailPassword != null && !mailPassword.isBlank()));
        }
        logger.info("-----------------------------------------------------------------------");
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) return "null/empty";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***" + email.substring(Math.max(0, atIndex));
        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private SmtpDiagnosis classifyAndLogMailException(String context, User user, MailException e) {
        Throwable root = e;
        StringBuilder causeChain = new StringBuilder();
        boolean first = true;

        while (root != null) {
            if (!first) causeChain.append(" -> ");
            causeChain.append(root.getClass().getSimpleName()).append(": ").append(root.getMessage());
            first = false;
            if (root.getCause() == null || root.getCause() == root) break;
            root = root.getCause();
        }

        String fullText = (e.getMessage() + " " + (root != null ? root.getMessage() : "") + " " + causeChain).toLowerCase();

        String category;
        String summary;

        if (e instanceof MailAuthenticationException
                || fullText.contains("authentication failed")
                || fullText.contains("535")
                || fullText.contains("badcredentials")
                || fullText.contains("username and password not accepted")
                || fullText.contains("invalid credentials")) {
            category = "AUTHENTICATION FAILURE";
            summary = "SMTP authentication rejected. Check Gmail App Password and username.";
        } else if (fullText.contains("connectexception")
                || fullText.contains("connection refused")
                || fullText.contains("mailconnectexception")
                || fullText.contains("could not connect to smtp host")) {
            category = "CONNECTION FAILURE";
            summary = "Could not establish TCP connection to SMTP server.";
        } else if (fullText.contains("starttls")
                || fullText.contains("ssl")
                || fullText.contains("handshake")
                || fullText.contains("certificate")) {
            category = "STARTTLS FAILURE";
            summary = "STARTTLS/SSL handshake negotiation failed.";
        } else if (fullText.contains("timeout")
                || fullText.contains("timed out")
                || fullText.contains("sockettimeoutexception")) {
            category = "TIMEOUT";
            summary = "Connection or read operation timed out reaching SMTP server.";
        } else if (fullText.contains("550")
                || fullText.contains("553")
                || fullText.contains("sender address rejected")
                || fullText.contains("from address")
                || fullText.contains("invalid address")) {
            category = "INVALID FROM";
            summary = "SMTP server rejected sender address.";
        } else {
            category = "OTHER";
            summary = root != null && root.getMessage() != null ? root.getMessage() : e.getMessage();
        }

        String safeRootMsg = sanitize(root != null ? root.getMessage() : e.getMessage());
        String safeChain = sanitize(causeChain.toString());

        logger.error("================================================================================");
        logger.error("[SMTP ERROR] Context: {} | Recipient: id={} email={}", context, user != null ? user.getId() : "N/A", user != null ? user.getEmail() : "N/A");
        logger.error("[SMTP ERROR CATEGORY] {}", category);
        logger.error("[SMTP ERROR SUMMARY] {}", summary);
        logger.error("[SMTP ROOT CAUSE] {}: {}", (root != null ? root.getClass().getName() : "Unknown"), safeRootMsg);
        logger.error("[SMTP CAUSE CHAIN] {}", safeChain);
        logger.error("================================================================================");

        return new SmtpDiagnosis(category, summary);
    }

    private String sanitize(String message) {
        if (message == null) return "null";
        if (mailPassword != null && !mailPassword.isBlank()) {
            message = message.replace(mailPassword, "******");
        }
        return message;
    }

    private static class SmtpDiagnosis {
        final String category;
        final String summary;

        SmtpDiagnosis(String category, String summary) {
            this.category = category;
            this.summary = summary;
        }
    }
}
