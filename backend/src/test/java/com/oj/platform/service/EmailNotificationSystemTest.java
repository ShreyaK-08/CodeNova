package com.oj.platform.service;

import com.oj.platform.dto.ContestDto;
import com.oj.platform.dto.ContestRequest;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSystemTest {

    @Mock private ContestRepository contestRepository;
    @Mock private ContestProblemRepository contestProblemRepository;
    @Mock private ContestRegistrationRepository contestRegistrationRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ContestAttemptRepository contestAttemptRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private EmailService emailService;
    @Mock private ContestAnnouncementNotificationRepository notificationRepository;

    @Mock private AssessmentHostVerificationRepository verificationRepository;
    @Mock private FileStorageService fileStorageService;

    private ContestService contestService;
    private AssessmentHostVerificationService verificationService;
    private EmailTemplateService emailTemplateService;

    private User admin;
    private User student1;
    private User student2;
    private User studentWithDuplicateEmail;
    private User studentWithoutEmail;
    private User studentWithInvalidEmail;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(
                contestRepository,
                contestProblemRepository,
                contestRegistrationRepository,
                problemRepository,
                userRepository,
                contestAttemptRepository,
                submissionRepository,
                emailService,
                notificationRepository
        );

        verificationService = new AssessmentHostVerificationService(
                verificationRepository,
                userRepository,
                fileStorageService,
                emailService,
                new BCryptPasswordEncoder()
        );

        emailTemplateService = new EmailTemplateService();

        admin = new User("Admin User", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        admin.setId(1L);

        student1 = new User("Alice Walker", "alice", "alice@example.com", "hashed", Role.ROLE_USER);
        student1.setId(2L);

        student2 = new User("Bob Smith", "bob", "bob@example.com", "hashed", Role.ROLE_USER);
        student2.setId(3L);

        studentWithDuplicateEmail = new User("Alice Clone", "alice2", "alice@example.com", "hashed", Role.ROLE_USER);
        studentWithDuplicateEmail.setId(4L);

        studentWithoutEmail = new User("No Email User", "noemail", null, "hashed", Role.ROLE_USER);
        studentWithoutEmail.setId(5L);

        studentWithInvalidEmail = new User("Bad Email User", "bademail", "not-an-email", "hashed", Role.ROLE_USER);
        studentWithInvalidEmail.setId(6L);
    }

    // 1. Contest creation triggers announcement when published
    @Test
    void testAdminCreatesOrPublishesContestTriggersAnnouncement() throws Exception {
        ContestRequest req = new ContestRequest();
        req.setTitle("Spring Code Challenge");
        req.setOrganizationName("CodeNova High");
        req.setDescription("Competitive coding battle");
        req.setStartTime(LocalDateTime.now().plusHours(2));
        req.setEndTime(LocalDateTime.now().plusHours(4));
        req.setStatus("PUBLISHED");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });
        when(userRepository.findAll()).thenReturn(List.of(admin, student1, student2));
        when(notificationRepository.existsByContestIdAndRecipientEmailIgnoreCase(anyLong(), anyString())).thenReturn(false);

        ContestDto created = contestService.createContest(req, 1L);

        assertNotNull(created);
        assertEquals("Spring Code Challenge", created.getTitle());
        assertEquals("PUBLISHED", created.getStatus());

        // Allow async thread pool in ContestService to execute
        Thread.sleep(1000);

        verify(emailService, atLeastOnce()).sendContestAnnouncementEmail(any(User.class), any(Contest.class));
    }

    // 2. Multiple registered users receive announcement emails
    @Test
    void testMultipleRegisteredUsersQueuedForAnnouncement() throws Exception {
        Contest contest = new Contest("Fall Contest", "CodeNova", "Desc",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), admin);
        contest.setId(200L);
        contest.setStatus(ContestStatus.PUBLISHED);

        when(userRepository.findAll()).thenReturn(List.of(student1, student2));
        when(notificationRepository.existsByContestIdAndRecipientEmailIgnoreCase(anyLong(), anyString())).thenReturn(false);

        contestService.broadcastContestAnnouncement(contest);

        Thread.sleep(1000);

        verify(emailService, times(1)).sendContestAnnouncementEmail(eq(student1), eq(contest));
        verify(emailService, times(1)).sendContestAnnouncementEmail(eq(student2), eq(contest));
    }

    // 3. Duplicate emails skipped safely
    @Test
    void testDuplicateEmailSkipped() throws Exception {
        Contest contest = new Contest("Duplicate Test Contest", "CodeNova", "Desc",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), admin);
        contest.setId(300L);
        contest.setStatus(ContestStatus.PUBLISHED);

        // List includes student1 and studentWithDuplicateEmail who share alice@example.com
        when(userRepository.findAll()).thenReturn(List.of(student1, studentWithDuplicateEmail));
        when(notificationRepository.existsByContestIdAndRecipientEmailIgnoreCase(anyLong(), anyString())).thenReturn(false);

        contestService.broadcastContestAnnouncement(contest);

        Thread.sleep(1000);

        // Should only be called once for alice@example.com (the first user encountered)
        verify(emailService, times(1)).sendContestAnnouncementEmail(any(User.class), eq(contest));
    }

    // 4. Users without email or with invalid email format skipped safely
    @Test
    void testUserWithoutEmailSkippedSafely() throws Exception {
        Contest contest = new Contest("Null Email Test", "CodeNova", "Desc",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), admin);
        contest.setId(400L);
        contest.setStatus(ContestStatus.PUBLISHED);

        when(userRepository.findAll()).thenReturn(List.of(studentWithoutEmail, studentWithInvalidEmail));

        contestService.broadcastContestAnnouncement(contest);

        Thread.sleep(1000);

        verify(emailService, never()).sendContestAnnouncementEmail(any(User.class), any(Contest.class));
    }

    // 5. Verification submitted email triggered
    @Test
    void testVerificationSubmittedEmailTriggered() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student1));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(fileStorageService.storeVerificationDocument(any())).thenReturn("stored.pdf");
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> {
            AssessmentHostVerification v = inv.getArgument(0);
            v.setId(50L);
            return v;
        });

        MockMultipartFile file = new MockMultipartFile("doc", "id.pdf", "application/pdf", "data".getBytes());

        verificationService.submitVerification(2L, file);

        verify(emailService, times(1)).sendHostVerificationSubmittedEmail(
                eq(student1),
                any(AssessmentHostVerification.class)
        );
    }

    // 6. Admin approves verification triggers approval email
    @Test
    void testAdminApprovesVerificationTriggersApprovalEmail() {
        AssessmentHostVerification verification = new AssessmentHostVerification();
        verification.setId(60L);
        verification.setUser(student1);
        verification.setStatus(HostVerificationStatus.PENDING);
        verification.setOrganizationName("Acme Corp");

        when(verificationRepository.findById(60L)).thenReturn(Optional.of(verification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenReturn(verification);

        verificationService.directVerify(60L, 1L);

        assertEquals(HostVerificationStatus.VERIFIED, verification.getStatus());
        verify(emailService, times(1)).sendHostVerificationApprovedEmail(
                eq(student1),
                eq(verification)
        );
    }

    // 7. Admin rejects verification triggers rejection email with exact reason
    @Test
    void testAdminRejectsVerificationTriggersRejectionEmailWithExactReason() {
        AssessmentHostVerification verification = new AssessmentHostVerification();
        verification.setId(70L);
        verification.setUser(student1);
        verification.setStatus(HostVerificationStatus.PENDING);

        when(verificationRepository.findById(70L)).thenReturn(Optional.of(verification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenReturn(verification);

        String specificReason = "ID card is blurred and organization domain does not match submission details.";
        verificationService.reject(70L, specificReason, 1L);

        assertEquals(HostVerificationStatus.REJECTED, verification.getStatus());
        assertEquals(specificReason, verification.getRejectionReason());

        verify(emailService, times(1)).sendHostVerificationRejectedEmail(
                eq(student1),
                eq(verification),
                eq(specificReason)
        );
    }

    // 8. Malicious HTML in rejection reason is properly escaped
    @Test
    void testMaliciousHtmlInRejectionReasonIsEscaped() {
        AssessmentHostVerification verification = new AssessmentHostVerification();
        verification.setId(75L);
        verification.setUser(student1);
        verification.setOrganizationName("Test Security Corp");

        String xssPayload = "<script>alert('pwned')</script><img src=x onerror=\"hack()\"> & \"quotes\"";
        String html = emailTemplateService.buildHostVerificationRejectedHtml(
                student1, verification, xssPayload, "http://localhost:5173"
        );

        // Raw malicious tags must NOT be present unescaped
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img"));

        // Escaped entities must be present
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("&lt;/script&gt;"));
        assertTrue(html.contains("&lt;img src=x"));
        assertTrue(html.contains("&amp;"));
        assertTrue(html.contains("&quot;quotes&quot;"));
    }

    // 9. Email delivery failure does not rollback contest creation
    @Test
    void testEmailDeliveryFailureDoesNotRollbackContest() throws Exception {
        ContestRequest req = new ContestRequest();
        req.setTitle("Fault Tolerant Contest");
        req.setOrganizationName("Resilience Labs");
        req.setDescription("Desc");
        req.setStartTime(LocalDateTime.now().plusHours(1));
        req.setEndTime(LocalDateTime.now().plusHours(3));
        req.setStatus("PUBLISHED");

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(contestRepository.save(any(Contest.class))).thenAnswer(inv -> {
            Contest c = inv.getArgument(0);
            c.setId(800L);
            return c;
        });
        lenient().when(userRepository.findAll()).thenThrow(new RuntimeException("Simulated DB connection glitch during email batch"));

        // Creating contest must not fail even if broadcasting encounters an error
        ContestDto result = assertDoesNotThrow(() -> contestService.createContest(req, 1L));
        assertNotNull(result);
        assertEquals("Fault Tolerant Contest", result.getTitle());

        Thread.sleep(200);
    }

    // 10. Email delivery failure does not rollback verification rejection or approval
    @Test
    void testEmailDeliveryFailureDoesNotRollbackVerification() {
        AssessmentHostVerification verification = new AssessmentHostVerification();
        verification.setId(90L);
        verification.setUser(student1);
        verification.setStatus(HostVerificationStatus.PENDING);

        when(verificationRepository.findById(90L)).thenReturn(Optional.of(verification));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenReturn(verification);

        doThrow(new RuntimeException("SMTP connection timed out")).when(emailService)
                .sendHostVerificationRejectedEmail(any(User.class), any(AssessmentHostVerification.class), anyString());

        // Reject must succeed and status must be REJECTED despite SMTP failure
        assertDoesNotThrow(() -> verificationService.reject(90L, "Official document could not be authenticated.", 1L));
        assertEquals(HostVerificationStatus.REJECTED, verification.getStatus());
    }
}
