package com.oj.platform.service;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.dto.NotificationLogDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.NotificationLogRepository;
import com.oj.platform.repository.UserSettingsRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDeliveryServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AssessmentHostVerificationRepository verificationRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    private EmailTemplateService emailTemplateService;
    private EmailServiceImpl emailService;

    private User candidate;
    private User host;
    private Assessment assessment;
    private AssessmentAttempt attempt;
    private Contest contest;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService();
        emailService = new EmailServiceImpl(
                mailSender,
                emailTemplateService,
                verificationRepository,
                notificationLogRepository,
                userSettingsRepository
        );

        ReflectionTestUtils.setField(emailService, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailService, "mailFrom", "notifications@codenova.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");

        lenient().when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        candidate = new User("John Doe", "johndoe", "john.doe@example.com", "hash", Role.ROLE_USER);
        candidate.setId(101L);

        host = new User("Host Admin", "hostadmin", "host@example.com", "hash", Role.ROLE_ADMIN);
        host.setId(201L);

        assessment = new Assessment("Java Pro Certification", "Deep Java Assessment", "Rules", 120, 70, host);
        assessment.setId(301L);
        assessment.setTotalMarks(100);

        attempt = new AssessmentAttempt();
        attempt.setId(401L);
        attempt.setUser(candidate);
        attempt.setAssessment(assessment);
        attempt.setScore(85);
        attempt.setMcqScore(40);
        attempt.setProgrammingScore(45.0);
        attempt.setViolationCount(0);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);

        contest = new Contest("CodeNova Autumn Cup", "TechCorp", "Coding Battle",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3), host);
        contest.setId(501L);

        certificate = new Certificate(candidate, 50, "CN-2026-00050", "50 Problems Solved",
                "Gold", "John Doe", "Earned for solving 50 problems", "CN-VERIFY-9988");
        ReflectionTestUtils.setField(certificate, "id", 601L);
    }

    @Test
    void testSendWelcomeEmail_Success() {
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("WELCOME"), eq("user:101"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendWelcomeEmail(candidate);

        assertTrue(result.isSent());
        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendWelcomeEmail_IdempotentSkip() {
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("WELCOME"), eq("user:101"), eq("SENT"))).thenReturn(true);

        EmailService.EmailResult result = emailService.sendWelcomeEmail(candidate);

        assertTrue(result.isSent());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendAssessmentResultEmail_Success() {
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("ASSESSMENT_RESULT"), eq("attempt:401"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendAssessmentResultEmail(candidate, assessment, attempt);

        assertTrue(result.isSent());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendAssessmentResultEmail_SkippedWhenUserOptedOut() {
        UserSettings settings = new UserSettings(candidate);
        settings.setAssessmentNotifications(false);
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.of(settings));

        EmailService.EmailResult result = emailService.sendAssessmentResultEmail(candidate, assessment, attempt);

        assertEquals(EmailService.EmailStatus.SKIPPED, result.getStatus());
        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testSendContestRegistrationEmail_Success() {
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("CONTEST_REGISTRATION"), eq("contest:501"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendContestRegistrationEmail(candidate, contest);

        assertTrue(result.isSent());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendContestResultEmail_Success() {
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("CONTEST_RESULT"), eq("contest:501"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendContestResultEmail(candidate, contest, 3, 150, 450);

        assertTrue(result.isSent());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendCertificateIssuedEmail_Success() {
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("CERTIFICATE_ISSUED"), eq("cert:601"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendCertificateIssuedEmail(candidate, certificate);

        assertTrue(result.isSent());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendTestEmail_DiagnosticSuccess() {
        EmailService.EmailResult result = emailService.sendTestEmail("admin@example.com", "Manual test from test suite");

        assertTrue(result.isSent());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_WhenSmtpNotConfigured_ReturnsNotConfigured() {
        ReflectionTestUtils.setField(emailService, "mailHost", "");

        EmailService.EmailResult result = emailService.sendWelcomeEmail(candidate);

        assertEquals(EmailService.EmailStatus.NOT_CONFIGURED, result.getStatus());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_WhenAuthenticationFails_LogsDiagnosisAndFailsGracefully() {
        doThrow(new MailAuthenticationException("535 5.7.8 Username and Password not accepted"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailService.EmailResult result = emailService.sendWelcomeEmail(candidate);

        assertEquals(EmailService.EmailStatus.FAILED, result.getStatus());
        assertTrue(result.getMessage().contains("SMTP authentication rejected"));
    }

    @Test
    void testGetEmailStats() {
        when(notificationLogRepository.count()).thenReturn(10L);
        when(notificationLogRepository.countByStatus("SENT")).thenReturn(8L);
        when(notificationLogRepository.countByStatus("FAILED")).thenReturn(2L);
        when(notificationLogRepository.countByStatus("NOT_CONFIGURED")).thenReturn(0L);
        when(notificationLogRepository.countByStatus("SKIPPED")).thenReturn(0L);

        EmailStatsDto stats = emailService.getEmailStats();

        assertEquals(10L, stats.getTotalLogged());
        assertEquals(8L, stats.getTotalSent());
        assertEquals(2L, stats.getTotalFailed());
        assertEquals(80.0, stats.getSuccessRate(), 0.01);
        assertTrue(stats.isSmtpConfigured());
    }

    @Test
    void testEmailTemplateService_BuildsValidHtmlAndText() {
        String welcomeHtml = emailTemplateService.buildWelcomeEmailHtml(candidate, "http://localhost:5173");
        assertTrue(welcomeHtml.contains("CODENOVA"));
        assertTrue(welcomeHtml.contains("Welcome aboard"));

        String assessmentHtml = emailTemplateService.buildAssessmentResultHtml(candidate, assessment, attempt, "http://localhost:5173");
        assertTrue(assessmentHtml.contains("85 / 100"));
        assertTrue(assessmentHtml.contains("PASSED"));

        String contestHtml = emailTemplateService.buildContestRegistrationHtml(candidate, contest, "http://localhost:5173");
        assertTrue(contestHtml.contains("CodeNova Autumn Cup"));

        String certHtml = emailTemplateService.buildCertificateIssuedHtml(candidate, certificate, "http://localhost:5173");
        assertTrue(certHtml.contains("CN-VERIFY-9988"));
    }
}
