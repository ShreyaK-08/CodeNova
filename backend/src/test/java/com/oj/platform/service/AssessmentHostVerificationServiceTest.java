package com.oj.platform.service;

import com.oj.platform.dto.AdminAssessmentHostVerificationDto;
import com.oj.platform.dto.AssessmentHostVerificationDto;
import com.oj.platform.entity.AssessmentHostVerification;
import com.oj.platform.entity.HostVerificationStatus;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.AssessmentHostVerificationRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Assessment Host verification workflow (submit -> approve/reject
 * -> code verification), covering test cases 1, 2 (partial - see note below), 3, 5, 6,
 * 7, 8, 9, 10, 11, 12 from the task's Testing section. A real BCryptPasswordEncoder is
 * used (not mocked) so the "code is hashed, not stored/compared in plain text" and
 * "correct/wrong code" behaviors are exercised for real, matching how
 * AssessmentHostVerificationService is actually wired via SecurityConfig's
 * passwordEncoder() bean.
 *
 * Test case 4 ("Non-admin cannot access admin verification APIs") is enforced
 * declaratively via @PreAuthorize("hasRole('ADMIN')") on
 * AdminAssessmentHostVerificationController plus SecurityConfig's
 * "/api/admin/**" -> hasRole("ADMIN") rule - the same mechanism every other admin
 * controller in this codebase relies on. Verifying that end-to-end would need a
 * @SpringBootTest/MockMvc security slice, which no test in this codebase currently
 * sets up (every existing test here is a plain Mockito unit test with no Spring
 * context) - see the final report for this limitation.
 */
@ExtendWith(MockitoExtension.class)
class AssessmentHostVerificationServiceTest {

    @Mock private AssessmentHostVerificationRepository verificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AssessmentHostVerificationService service;

    private User student;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new AssessmentHostVerificationService(verificationRepository, userRepository,
                fileStorageService, emailService, passwordEncoder);

        student = new User("Student", "student", "student@example.com", "hashed", Role.ROLE_USER);
        student.setId(2L);

        admin = new User("Admin", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        admin.setId(1L);
    }

    // ---- test case 1: user can submit verification request ----

    @Test
    void testUserCanSubmitVerificationRequest() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(fileStorageService.storeVerificationDocument(any())).thenReturn("uuid-generated-name.pdf");
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> {
            AssessmentHostVerification v = inv.getArgument(0);
            v.setId(10L);
            return v;
        });

        MockMultipartFile file = new MockMultipartFile("document", "id-card.pdf", "application/pdf", "dummy".getBytes());

        AssessmentHostVerificationDto dto = service.submitVerification(2L, file);

        assertEquals("PENDING", dto.getStatus());
        verify(fileStorageService).storeVerificationDocument(file);
        verify(verificationRepository).save(any(AssessmentHostVerification.class));
    }

    @Test
    void testCannotResubmitWhilePending() {
        AssessmentHostVerification existing = pendingVerification();
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(existing));

        MockMultipartFile file = new MockMultipartFile("document", "id-card.pdf", "application/pdf", "dummy".getBytes());

        assertThrows(BadRequestException.class, () -> service.submitVerification(2L, file));
    }

    // ---- test case 2: user cannot access another user's verification ----
    // (Ownership here is enforced structurally: every lookup is scoped to the userId
    // passed in from the JWT-authenticated principal - there is no method that accepts
    // a verificationId directly from a "user" caller, only from the admin API.)

    @Test
    void testGetMyVerificationStatusOnlyQueriesCallersOwnRecord() {
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());

        AssessmentHostVerificationDto dto = service.getMyVerificationStatus(2L);

        assertEquals("NOT_SUBMITTED", dto.getStatus());
        verify(verificationRepository).findByUserId(2L);
        verify(verificationRepository, never()).findById(anyLong());
    }

    // ---- test case 3: admin can list verification requests ----

    @Test
    void testAdminCanListVerificationRequests() {
        AssessmentHostVerification v1 = pendingVerification();
        v1.setId(1L);
        when(verificationRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(v1));

        List<AdminAssessmentHostVerificationDto> result = service.listAllForAdmin();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals(student.getEmail(), result.get(0).getUserEmail());
    }

    // ---- test case 5 & 7: admin can approve; verification code is generated ----

    @Test
    void testAdminCanApproveAndCodeIsGenerated() {
        AssessmentHostVerification v = pendingVerification();
        v.setId(5L);
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(v));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailService.sendHostVerificationCode(eq(student), any(), anyString(), anyInt()))
                .thenReturn(new EmailService.EmailResult(EmailService.EmailStatus.SENT, "sent"));

        AssessmentHostVerificationService.ApprovalResult result = service.approve(5L, 1L);

        assertEquals("APPROVED", result.getVerification().getStatus());
        assertEquals(EmailService.EmailStatus.SENT, result.getEmailResult().getStatus());
        assertNotNull(v.getVerificationCodeHash());
        assertTrue(v.getVerificationCodeHash().startsWith("$2")); // BCrypt hash prefix
        assertFalse(v.isCodeUsed());
        assertNotNull(v.getCodeExpiresAt());
    }

    @Test
    void testApprovalStillSucceedsWhenEmailDeliveryUnavailable() {
        AssessmentHostVerification v = pendingVerification();
        v.setId(5L);
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(v));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(emailService.sendHostVerificationCode(any(), any(), anyString(), anyInt()))
                .thenReturn(new EmailService.EmailResult(EmailService.EmailStatus.NOT_CONFIGURED, "SMTP not configured"));

        AssessmentHostVerificationService.ApprovalResult result = service.approve(5L, 1L);

        assertEquals("APPROVED", result.getVerification().getStatus());
        assertEquals(EmailService.EmailStatus.NOT_CONFIGURED, result.getEmailResult().getStatus());
    }

    // ---- test case 8: verification code is not returned in normal API responses ----

    @Test
    void testDtosNeverExposeVerificationCode() throws Exception {
        for (Method m : AssessmentHostVerificationDto.class.getMethods()) {
            assertFalse(m.getName().toLowerCase().contains("code") && m.getName().startsWith("get"),
                    "AssessmentHostVerificationDto must never expose the verification code: " + m.getName());
        }
        for (Method m : AdminAssessmentHostVerificationDto.class.getMethods()) {
            assertFalse(m.getName().toLowerCase().contains("code") && m.getName().startsWith("get"),
                    "AdminAssessmentHostVerificationDto must never expose the verification code: " + m.getName());
        }
    }

    // ---- test case 6: admin can reject ----

    @Test
    void testAdminCanReject() {
        AssessmentHostVerification v = pendingVerification();
        v.setId(6L);
        when(verificationRepository.findById(6L)).thenReturn(Optional.of(v));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminAssessmentHostVerificationDto dto = service.reject(6L, "Document is unreadable", 1L);

        assertEquals("REJECTED", dto.getStatus());
        assertEquals("Document is unreadable", dto.getRejectionReason());
    }

    // ---- test case 9: correct code verifies user ----

    @Test
    void testCorrectCodeVerifiesUser() {
        AssessmentHostVerification v = approvedVerificationWithCode("CN-123456", LocalDateTime.now().plusMinutes(10));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(v));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> inv.getArgument(0));

        AssessmentHostVerificationDto dto = service.verifyCode(2L, "CN-123456");

        assertEquals("VERIFIED", dto.getStatus());
        assertTrue(v.isCodeUsed());
    }

    // ---- test case 10: wrong code is rejected ----

    @Test
    void testWrongCodeIsRejected() {
        AssessmentHostVerification v = approvedVerificationWithCode("CN-123456", LocalDateTime.now().plusMinutes(10));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(v));

        assertThrows(BadRequestException.class, () -> service.verifyCode(2L, "CN-000000"));
        assertFalse(v.isCodeUsed());
        verify(verificationRepository, never()).save(any());
    }

    // ---- test case 11: expired code is rejected ----

    @Test
    void testExpiredCodeIsRejected() {
        AssessmentHostVerification v = approvedVerificationWithCode("CN-123456", LocalDateTime.now().minusMinutes(1));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(v));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.verifyCode(2L, "CN-123456"));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
    }

    // ---- test case 12: used code cannot be reused ----

    @Test
    void testUsedCodeCannotBeReused() {
        AssessmentHostVerification v = approvedVerificationWithCode("CN-123456", LocalDateTime.now().plusMinutes(10));
        v.setCodeUsed(true);
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(v));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.verifyCode(2L, "CN-123456"));
        assertTrue(ex.getMessage().toLowerCase().contains("already been used"));
    }

    @Test
    void testVerifyCodeThrowsWhenNoRequestExists() {
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.verifyCode(2L, "CN-123456"));
    }

    // ---- isVerifiedHost / requireVerifiedHost (used by AssessmentService's gate) ----

    @Test
    void testRequireVerifiedHostThrowsForbiddenWhenNotVerified() {
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.requireVerifiedHost(2L));
    }

    @Test
    void testRequireVerifiedHostPassesWhenVerified() {
        AssessmentHostVerification v = new AssessmentHostVerification(student);
        v.setStatus(HostVerificationStatus.VERIFIED);
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.of(v));

        assertDoesNotThrow(() -> service.requireVerifiedHost(2L));
    }

    @Test
    void testDirectVerifyMarksUserVerifiedPermanently() {
        AssessmentHostVerification v = pendingVerification();
        v.setId(99L);
        when(verificationRepository.findById(99L)).thenReturn(Optional.of(v));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminAssessmentHostVerificationDto dto = service.directVerify(99L, 1L);

        assertEquals("VERIFIED", dto.getStatus());
        assertTrue(v.isCodeUsed());
        assertNull(v.getVerificationCodeHash());
        assertNotNull(v.getVerifiedAt());
        assertNotNull(v.getReviewedAt());
        assertEquals(admin, v.getReviewedBy());
    }

    @Test
    void testSubmitBusinessVerificationSuccess() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(verificationRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(fileStorageService.storeVerificationDocument(any())).thenReturn("stored-business-cert.pdf");
        when(verificationRepository.save(any(AssessmentHostVerification.class))).thenAnswer(inv -> {
            AssessmentHostVerification saved = inv.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        com.oj.platform.dto.SubmitHostBusinessVerificationRequest request =
                new com.oj.platform.dto.SubmitHostBusinessVerificationRequest(
                        "Acme Corp", "Private Company", "REG-9999", "hr@acme.com",
                        "India", "https://acme.com", "John Doe", "+919999999999", "Hiring"
                );

        MockMultipartFile primary = new MockMultipartFile("primaryDocument", "cert.pdf", "application/pdf", "dummy".getBytes());

        AssessmentHostVerificationDto dto = service.submitBusinessVerification(2L, request, primary, null, null, null);

        assertEquals("PENDING", dto.getStatus());
        assertEquals("Acme Corp", dto.getOrganizationName());
        assertEquals("REG-9999", dto.getRegistrationNumber());
        assertEquals("hr@acme.com", dto.getOfficialEmail());
        assertEquals("Private Company", dto.getOrganizationType());
    }

    // ---- helpers ----

    private AssessmentHostVerification pendingVerification() {
        AssessmentHostVerification v = new AssessmentHostVerification(student);
        v.setDocumentStoredName("stored.pdf");
        v.setDocumentOriginalName("id-card.pdf");
        v.setStatus(HostVerificationStatus.PENDING);
        v.setSubmittedAt(LocalDateTime.now());
        return v;
    }

    private AssessmentHostVerification approvedVerificationWithCode(String plainCode, LocalDateTime expiresAt) {
        AssessmentHostVerification v = new AssessmentHostVerification(student);
        v.setStatus(HostVerificationStatus.APPROVED);
        v.setVerificationCodeHash(passwordEncoder.encode(plainCode));
        v.setCodeExpiresAt(expiresAt);
        v.setCodeUsed(false);
        return v;
    }
}
