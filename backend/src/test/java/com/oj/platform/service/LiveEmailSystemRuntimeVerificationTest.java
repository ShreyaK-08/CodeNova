package com.oj.platform.service;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.dto.NotificationLogDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live Runtime Email System Verification Test.
 * Executes against the real Spring Boot container, real MySQL database,
 * and live JavaMailSender/SMTP configuration.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LiveEmailSystemRuntimeVerificationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentAttemptRepository assessmentAttemptRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private SupportRequestRepository supportRequestRepository;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private AssessmentHostVerificationRepository verificationRepository;

    private static final String TARGET_TEST_GMAIL = "shreyak8225@gmail.com";

    // -------------------------------------------------------------------------
    // 1. SMTP CONFIGURATION & ADMIN TEST TOOL
    // -------------------------------------------------------------------------
    @Test
    @Order(1)
    void test1_AdminSmtpTestTool() {
        System.out.println("=== 1. TESTING ADMIN SMTP TEST TOOL ===");
        EmailService.EmailResult result = emailService.sendTestEmail(
                TARGET_TEST_GMAIL,
                "Live Runtime Diagnostic Test from Spring Boot Test Suite"
        );

        System.out.println("Result Status: " + result.getStatus());
        System.out.println("Result Message: " + result.getMessage());

        assertNotNull(result);
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus(),
                "SMTP sending must succeed with real credentials");
        assertTrue(result.isSent());
    }

    // -------------------------------------------------------------------------
    // 2. WELCOME EMAIL & DUPLICATE PROTECTION
    // -------------------------------------------------------------------------
    @Test
    @Order(2)
    void test2_WelcomeEmailAndDuplicateProtection() {
        System.out.println("=== 2. TESTING WELCOME EMAIL & IDEMPOTENCY ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> {
                    String unique = "candidate_" + System.currentTimeMillis();
                    return userRepository.save(new User("Test Candidate", unique, unique + "@example.com", "hash", Role.ROLE_USER));
                });

        // Ensure we test welcome idempotency cleanly
        String entityKey = "user:" + user.getId();
        notificationLogRepository.deleteAll(
                notificationLogRepository.findAll().stream()
                        .filter(l -> "WELCOME".equals(l.getNotificationType()) && entityKey.equals(l.getRelatedEntityId()))
                        .toList()
        );

        // First dispatch -> should SEND
        EmailService.EmailResult firstResult = emailService.sendWelcomeEmail(user);
        System.out.println("First Welcome Dispatch: " + firstResult.getStatus() + " - " + firstResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, firstResult.getStatus());

        // Second dispatch (immediate retry) -> should be IDEMPOTENT SKIP
        EmailService.EmailResult secondResult = emailService.sendWelcomeEmail(user);
        System.out.println("Second Welcome Dispatch (Idempotent): " + secondResult.getStatus() + " - " + secondResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, secondResult.getStatus());
        assertTrue(secondResult.getMessage().toLowerCase().contains("already sent"));
    }

    // -------------------------------------------------------------------------
    // 3. ASSESSMENT RESULT EMAIL
    // -------------------------------------------------------------------------
    @Test
    @Order(3)
    void test3_AssessmentResultEmail() {
        System.out.println("=== 3. TESTING ASSESSMENT RESULT EMAIL ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        Assessment assessment = new Assessment("Dynamic Programming Masterclass",
                "Advanced DP Assessment", "Strict time limit", 90, 60, user);
        assessment.setTotalMarks(100);
        Assessment savedAssessment = assessmentRepository.save(assessment);

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setUser(user);
        attempt.setAssessment(savedAssessment);
        attempt.setScore(92);
        attempt.setMcqScore(40);
        attempt.setProgrammingScore(52.0);
        attempt.setCorrectAnswers(8);
        attempt.setIncorrectAnswers(2);
        attempt.setUnansweredCount(0);
        attempt.setViolationCount(0);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(45));
        attempt.setCompletedAt(LocalDateTime.now());
        AssessmentAttempt savedAttempt = assessmentAttemptRepository.save(attempt);

        EmailService.EmailResult result = emailService.sendAssessmentResultEmail(user, savedAssessment, savedAttempt);
        System.out.println("Assessment Result Dispatch: " + result.getStatus() + " - " + result.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
    }

    // -------------------------------------------------------------------------
    // 4. CONTEST RESULT EMAIL
    // -------------------------------------------------------------------------
    @Test
    @Order(4)
    void test4_ContestResultEmail() {
        System.out.println("=== 4. TESTING CONTEST RESULT EMAIL ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        Contest contest = new Contest("CodeNova Grand Prix 2026", "CodeNova Arena", "Live Competition",
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1), user);
        Contest savedContest = contestRepository.save(contest);

        EmailService.EmailResult result = emailService.sendContestResultEmail(user, savedContest, 1, 120, 500);
        System.out.println("Contest Result Dispatch: " + result.getStatus() + " - " + result.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
    }

    // -------------------------------------------------------------------------
    // 5. CERTIFICATE ISSUANCE EMAIL
    // -------------------------------------------------------------------------
    @Test
    @Order(5)
    void test5_CertificateIssuanceEmail() {
        System.out.println("=== 5. TESTING CERTIFICATE ISSUANCE EMAIL ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        String uniqueTitle = "Master Problem Solver " + (System.currentTimeMillis() % 100000);
        String certNum = "CN-2026-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String verifyCode = "VERIFY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Certificate cert = new Certificate(user, 100, certNum, uniqueTitle,
                "Platinum", user.getName(), "Awarded for exceptional algorithmic mastery", verifyCode);
        Certificate savedCert = certificateRepository.save(cert);

        EmailService.EmailResult result = emailService.sendCertificateIssuedEmail(user, savedCert);
        System.out.println("Certificate Issuance Dispatch: " + result.getStatus() + " - " + result.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
    }

    // -------------------------------------------------------------------------
    // 6. SUPPORT TICKET LIFECYCLE EMAILS
    // -------------------------------------------------------------------------
    @Test
    @Order(6)
    void test6_SupportTicketEmails() throws Exception {
        System.out.println("=== 6. TESTING SUPPORT TICKET EMAILS ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        SupportRequest req = new SupportRequest();
        req.setUser(user);
        req.setTicketNumber("CN-SUP-" + System.currentTimeMillis() % 100000);
        req.setSubject("Compiler Runtime Performance Query");
        req.setDescription("Checking standard output memory limit allocation for Java submissions.");
        req.setCategory("Compiler / Judge");
        req.setPriority("HIGH");
        req.setStatus("OPEN");
        SupportRequest savedReq = supportRequestRepository.save(req);

        // Created email
        EmailService.EmailResult createdRes = emailService.sendSupportTicketCreatedEmail(user, savedReq);
        System.out.println("Support Created Dispatch: " + createdRes.getStatus() + " - " + createdRes.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, createdRes.getStatus());

        Thread.sleep(800);

        // Admin Reply email
        SupportMessage replyMsg = new SupportMessage(savedReq, user, "ADMIN",
                "Our sandbox environment has 512MB heap limit per process. This is plenty for competitive solutions.", false);
        SupportMessage savedMsg = supportMessageRepository.save(replyMsg);
        EmailService.EmailResult replyRes = emailService.sendSupportAdminReplyEmail(user, savedReq, savedMsg);
        System.out.println("Support Admin Reply Dispatch: " + replyRes.getStatus() + " - " + replyRes.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, replyRes.getStatus());

        Thread.sleep(800);

        // Resolved email
        savedReq.setStatus("RESOLVED");
        savedReq.setResolvedAt(LocalDateTime.now());
        supportRequestRepository.save(savedReq);
        EmailService.EmailResult resolvedRes = emailService.sendSupportTicketResolvedEmail(user, savedReq);
        System.out.println("Support Resolved Dispatch: " + resolvedRes.getStatus() + " - " + resolvedRes.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, resolvedRes.getStatus());
    }

    // -------------------------------------------------------------------------
    // 7. HOST VERIFICATION LIFECYCLE (APPROVAL / REJECTION WITH EXACT REASON)
    // -------------------------------------------------------------------------
    @Test
    @Order(7)
    void test7_HostVerificationEmails() throws Exception {
        System.out.println("=== 7. TESTING HOST VERIFICATION EMAILS ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        AssessmentHostVerification verification = new AssessmentHostVerification();
        verification.setUser(user);
        verification.setOrganizationName("National Coding Institute " + (System.currentTimeMillis() % 10000));
        verification.setOrganizationType("University / College");
        verification.setCountry("India");
        verification.setWebsite("https://nci.edu.in");
        verification.setStatus(HostVerificationStatus.PENDING);
        AssessmentHostVerification savedVerification = verificationRepository.save(verification);

        // Submitted email
        EmailService.EmailResult subResult = emailService.sendHostVerificationSubmittedEmail(user, savedVerification);
        System.out.println("Host Verification Submitted Dispatch: " + subResult.getStatus() + " - " + subResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, subResult.getStatus());

        Thread.sleep(1000);

        // Code email
        EmailService.EmailResult codeResult = emailService.sendHostVerificationCode(user, savedVerification, "839201", 30);
        System.out.println("Host Verification Code Dispatch: " + codeResult.getStatus() + " - " + codeResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, codeResult.getStatus());

        Thread.sleep(1000);

        // Approved email
        EmailService.EmailResult appResult = emailService.sendHostVerificationApprovedEmail(user, savedVerification);
        System.out.println("Host Verification Approved Dispatch: " + appResult.getStatus() + " - " + appResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, appResult.getStatus());

        Thread.sleep(1000);

        // Rejected email with exact reason
        String exactReason = "Official institutional accreditation letter is missing official seal and authorized signature.";
        EmailService.EmailResult rejResult = emailService.sendHostVerificationRejectedEmail(user, savedVerification, exactReason);
        System.out.println("Host Verification Rejected Dispatch: " + rejResult.getStatus() + " - " + rejResult.getMessage());
        assertEquals(EmailService.EmailStatus.SENT, rejResult.getStatus());
    }

    // -------------------------------------------------------------------------
    // 8. AUDIT LOGGING & STATS VERIFICATION
    // -------------------------------------------------------------------------
    @Test
    @Order(8)
    void test8_NotificationAuditLogAndStats() {
        System.out.println("=== 8. TESTING AUDIT LOGS AND STATS ===");
        List<NotificationLogDto> logs = emailService.getRecentLogs();
        assertNotNull(logs);
        assertFalse(logs.isEmpty(), "Notification logs must contain entries from tests");

        System.out.println("Total Recent Logs Retrieved: " + logs.size());
        NotificationLogDto latest = logs.get(0);
        System.out.println("Latest Log: type=" + latest.getNotificationType()
                + " status=" + latest.getStatus()
                + " recipient=" + latest.getRecipientEmail()
                + " sentAt=" + latest.getSentAt());

        EmailStatsDto stats = emailService.getEmailStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalSent() > 0, "Total sent count must be > 0");
        assertTrue(stats.isSmtpConfigured(), "SMTP must be marked configured");
        System.out.println("Aggregate Stats: Sent=" + stats.getTotalSent()
                + " Failed=" + stats.getTotalFailed()
                + " SuccessRate=" + String.format("%.1f%%", stats.getSuccessRate()));
    }

    // -------------------------------------------------------------------------
    // 9. USER PREFERENCES CHECK
    // -------------------------------------------------------------------------
    @Test
    @Order(9)
    void test9_UserPreferencesRespected() {
        System.out.println("=== 9. TESTING USER NOTIFICATION PREFERENCES ===");
        User user = userRepository.findByEmail(TARGET_TEST_GMAIL)
                .orElseGet(() -> userRepository.findAll().get(0));

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserSettings(user));
        settings.setAssessmentNotifications(false); // opt out of assessments
        userSettingsRepository.save(settings);

        Assessment assessment = assessmentRepository.findAll().get(0);
        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setId(9999L);
        attempt.setUser(user);
        attempt.setAssessment(assessment);
        attempt.setScore(75);

        EmailService.EmailResult result = emailService.sendAssessmentResultEmail(user, assessment, attempt);
        System.out.println("Opted-out Assessment Dispatch: " + result.getStatus() + " - " + result.getMessage());
        assertEquals(EmailService.EmailStatus.SKIPPED, result.getStatus(),
                "Should return SKIPPED when user opted out of assessment notifications");

        // Restore settings
        settings.setAssessmentNotifications(true);
        userSettingsRepository.save(settings);
    }
}
