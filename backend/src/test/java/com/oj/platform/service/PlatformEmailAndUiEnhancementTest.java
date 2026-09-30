package com.oj.platform.service;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.NotificationLogRepository;
import com.oj.platform.repository.UserSettingsRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlatformEmailAndUiEnhancementTest {

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

    private User testUser;
    private User hostUser;
    private MimeMessage mimeMessage;

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
        ReflectionTestUtils.setField(emailService, "mailFrom", "notifications@codenova.io");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");

        testUser = new User("Alex Developer", "alexdev", "alex@example.com", "hash", Role.ROLE_USER);
        testUser.setId(101L);

        hostUser = new User("Host Admin", "hostadmin", "host@example.com", "hash", Role.ROLE_ADMIN);
        hostUser.setId(201L);

        mimeMessage = new MimeMessage((Session) null);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Support Ticket Resolved email dispatches successfully and records log")
    void testSupportTicketResolvedEmail() {
        SupportRequest request = new SupportRequest();
        request.setId(55L);
        request.setTicketNumber("CN-SUP-00055");
        request.setSubject("Issue with assessment submission");
        request.setStatus("Resolved");

        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("SUPPORT_RESOLVED"), eq("ticket:55"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendSupportTicketResolvedEmail(testUser, request);

        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Support Ticket Closed email dispatches successfully and records log")
    void testSupportTicketClosedEmail() {
        SupportRequest request = new SupportRequest();
        request.setId(56L);
        request.setTicketNumber("CN-SUP-00056");
        request.setSubject("Question regarding contest timing");
        request.setStatus("Closed");

        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("SUPPORT_CLOSED"), eq("ticket:56"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendSupportTicketClosedEmail(testUser, request);

        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Assessment Result email contains detailed scoring and is idempotent")
    void testAssessmentResultEmail_Idempotency() {
        Assessment assessment = new Assessment("Fullstack Java Spring Boot Exam", "Exam desc", "Rules", 120, 70, hostUser);
        assessment.setId(10L);
        assessment.setTotalMarks(100);

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setId(201L);
        attempt.setUser(testUser);
        attempt.setAssessment(assessment);
        attempt.setScore(85);
        attempt.setMcqScore(40);
        attempt.setProgrammingScore(45.0);
        attempt.setViolationCount(0);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);

        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());

        // First dispatch: Not sent yet
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("ASSESSMENT_RESULT"), eq("attempt:201"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result1 = emailService.sendAssessmentResultEmail(testUser, assessment, attempt);
        assertEquals(EmailService.EmailStatus.SENT, result1.getStatus());
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // Second dispatch: Already sent
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("ASSESSMENT_RESULT"), eq("attempt:201"), eq("SENT"))).thenReturn(true);

        EmailService.EmailResult result2 = emailService.sendAssessmentResultEmail(testUser, assessment, attempt);
        assertEquals(EmailService.EmailStatus.SENT, result2.getStatus());
        assertTrue(result2.getMessage().contains("already sent"));
        // MailSender should not be invoked a second time
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Contest Result email dispatches with rank and standings")
    void testContestResultEmail() {
        Contest contest = new Contest("Weekly Algorithm Challenge #42", "Org", "Challenge",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3), hostUser);
        contest.setId(5L);

        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.empty());
        when(notificationLogRepository.existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
                eq(101L), eq("CONTEST_RESULT"), eq("contest:5"), eq("SENT"))).thenReturn(false);

        EmailService.EmailResult result = emailService.sendContestResultEmail(testUser, contest, 1, 150, 300);

        assertEquals(EmailService.EmailStatus.SENT, result.getStatus());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("User preferences disabling email skips delivery cleanly")
    void testUserPreferencesDisabled() {
        UserSettings settings = new UserSettings(testUser);
        settings.setAssessmentNotifications(false);
        when(userSettingsRepository.findByUserId(101L)).thenReturn(Optional.of(settings));

        Assessment assessment = new Assessment("Java Assessment", "Desc", "Rules", 60, 50, hostUser);
        assessment.setId(10L);

        AssessmentAttempt attempt = new AssessmentAttempt();
        attempt.setId(202L);

        EmailService.EmailResult result = emailService.sendAssessmentResultEmail(testUser, assessment, attempt);

        assertEquals(EmailService.EmailStatus.SKIPPED, result.getStatus());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
