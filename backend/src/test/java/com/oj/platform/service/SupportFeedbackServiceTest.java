package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportFeedbackServiceTest {

    @Mock
    private SupportRequestRepository supportRequestRepository;

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository;

    @Mock
    private AssessmentFeedbackRepository assessmentFeedbackRepository;

    @Mock
    private GeneralFeedbackRepository generalFeedbackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentAttemptRepository assessmentAttemptRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AiService aiService;

    private SupportFeedbackService supportFeedbackService;

    private User testUser;
    private User testHost;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        supportFeedbackService = new SupportFeedbackService(
                supportRequestRepository,
                supportMessageRepository,
                assessmentQuestionFeedbackRepository,
                assessmentFeedbackRepository,
                generalFeedbackRepository,
                userRepository,
                assessmentRepository,
                assessmentQuestionRepository,
                assessmentAttemptRepository,
                emailService,
                aiService
        );

        testUser = new User("Test User", "testuser", "test@example.com", "pass", Role.ROLE_USER);
        testUser.setId(1L);

        testHost = new User("Host User", "hostuser", "host@example.com", "pass", Role.ROLE_USER);
        testHost.setId(2L);

        testAdmin = new User("Admin Staff", "adminstaff", "admin@example.com", "pass", Role.ROLE_ADMIN);
        testAdmin.setId(3L);
    }

    @Test
    void testCreateSupportRequestGeneratesTicketNumber() {
        CreateSupportRequest req = new CreateSupportRequest();
        req.setCategory("Coding Problems");
        req.setSubject("Judge timed out");
        req.setDescription("My solution timed out without explanation");
        req.setPriority("HIGH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        SupportRequest initialSaved = new SupportRequest();
        initialSaved.setId(42L);
        initialSaved.setUser(testUser);
        initialSaved.setCategory(req.getCategory());
        initialSaved.setSubject(req.getSubject());
        initialSaved.setDescription(req.getDescription());
        initialSaved.setPriority("HIGH");
        initialSaved.setStatus("OPEN");

        when(supportRequestRepository.saveAndFlush(any(SupportRequest.class))).thenReturn(initialSaved);
        when(supportRequestRepository.save(any(SupportRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportRequestDto result = supportFeedbackService.createSupportRequest(req, 1L);

        assertNotNull(result);
        assertEquals("CN-SUP-000042", result.getTicketNumber());
        assertEquals("OPEN", result.getStatus());
        assertEquals("HIGH", result.getPriority());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        verify(supportMessageRepository, times(1)).save(any(SupportMessage.class));
    }

    @Test
    void testGetMySupportRequests() {
        SupportRequest req1 = new SupportRequest();
        req1.setId(1L);
        req1.setUser(testUser);
        req1.setTicketNumber("CN-SUP-000001");
        req1.setStatus("OPEN");

        when(supportRequestRepository.searchUserRequests(eq(1L), isNull(), isNull(), isNull())).thenReturn(List.of(req1));

        List<SupportRequestDto> list = supportFeedbackService.getMySupportRequests(1L, null, null, null);
        assertEquals(1, list.size());
        assertEquals("CN-SUP-000001", list.get(0).getTicketNumber());
    }

    @Test
    void testAddSupportMessage() {
        SupportRequest req = new SupportRequest();
        req.setId(5L);
        req.setUser(testUser);
        req.setStatus("OPEN");

        when(supportRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateSupportMessageRequest msgReq = new CreateSupportMessageRequest();
        msgReq.setMessage("Here is more detail about the bug");

        SupportMessageDto dto = supportFeedbackService.addSupportMessage(5L, msgReq, 1L, false);
        assertNotNull(dto);
        assertEquals("Here is more detail about the bug", dto.getMessage());
        assertFalse(dto.isInternalNote());
    }

    @Test
    void testAdminTriageTicket() {
        SupportRequest req = new SupportRequest();
        req.setId(5L);
        req.setUser(testUser);
        req.setStatus("OPEN");
        req.setPriority("MEDIUM");

        when(supportRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testAdmin));
        when(supportRequestRepository.save(any(SupportRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSupportTicketRequest triage = new UpdateSupportTicketRequest();
        triage.setStatus("IN_PROGRESS");
        triage.setPriority("CRITICAL");
        triage.setAssignedToUserId(3L);

        SupportRequestDto result = supportFeedbackService.adminTriageTicket(5L, triage, 3L);
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("CRITICAL", result.getPriority());
        assertEquals(3L, result.getAssignedToUserId());
    }

    @Test
    void testSubmitSupportRating() {
        SupportRequest req = new SupportRequest();
        req.setId(8L);
        req.setUser(testUser);
        req.setStatus("RESOLVED");

        when(supportRequestRepository.findById(8L)).thenReturn(Optional.of(req));
        when(supportRequestRepository.save(any(SupportRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportRatingRequest ratingReq = new SupportRatingRequest();
        ratingReq.setRating(5);
        ratingReq.setComments("Super fast response, thank you!");

        SupportRequestDto dto = supportFeedbackService.submitSupportRating(8L, ratingReq, 1L);
        assertNotNull(dto);
        assertEquals(5, dto.getRating());
        assertEquals("Super fast response, thank you!", dto.getRatingComments());
    }

    @Test
    void testCreateGeneralFeedback() {
        CreateGeneralFeedbackRequest req = new CreateGeneralFeedbackRequest();
        req.setFeedbackType("Feature Request");
        req.setRating(5);
        req.setMessage("Add dark mode toggle in sidebar");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        GeneralFeedback gf = new GeneralFeedback();
        gf.setId(10L);
        gf.setUser(testUser);
        gf.setFeedbackType("Feature Request");
        gf.setRating(5);
        gf.setMessage("Add dark mode toggle in sidebar");

        when(generalFeedbackRepository.save(any(GeneralFeedback.class))).thenReturn(gf);

        GeneralFeedbackDto result = supportFeedbackService.createGeneralFeedback(req, 1L);
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Feature Request", result.getFeedbackType());
    }

    @Test
    void testCreateAssessmentQuestionFeedback() {
        CreateAssessmentQuestionFeedbackRequest req = new CreateAssessmentQuestionFeedbackRequest();
        req.setAssessmentId(20L);
        req.setQuestionId(30L);
        req.setReason("Wrong test case");
        req.setMessage("Sample output does not match description");

        Assessment assessment = new Assessment();
        assessment.setId(20L);
        assessment.setTitle("Java Basics Test");

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(30L);
        question.setQuestionText("What is JVM?");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findById(20L)).thenReturn(Optional.of(assessment));
        when(assessmentQuestionRepository.findById(30L)).thenReturn(Optional.of(question));

        AssessmentQuestionFeedback feedback = new AssessmentQuestionFeedback();
        feedback.setId(100L);
        feedback.setUser(testUser);
        feedback.setAssessment(assessment);
        feedback.setQuestion(question);
        feedback.setReason(req.getReason());
        feedback.setMessage(req.getMessage());

        when(assessmentQuestionFeedbackRepository.save(any(AssessmentQuestionFeedback.class))).thenReturn(feedback);

        AssessmentQuestionFeedbackDto result = supportFeedbackService.createAssessmentQuestionFeedback(req, 1L);
        assertNotNull(result);
        assertEquals("Wrong test case", result.getReason());
        assertEquals("Java Basics Test", result.getAssessmentTitle());
        assertEquals("What is JVM?", result.getQuestionText());
    }

    @Test
    void testSubmitPostCompletionAssessmentFeedback() {
        CreateAssessmentFeedbackRequest req = new CreateAssessmentFeedbackRequest();
        req.setAssessmentId(20L);
        req.setAttemptId(50L);
        req.setRating(5);
        req.setFeedbackType("Excellent");
        req.setComments("Great assessment questions!");

        Assessment assessment = new Assessment();
        assessment.setId(20L);
        assessment.setTitle("Full Stack Evaluation");
        assessment.setCreatedBy(testHost);

        AssessmentAttempt attempt = new AssessmentAttempt(assessment, testUser);
        attempt.setId(50L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findById(20L)).thenReturn(Optional.of(assessment));
        when(assessmentAttemptRepository.findById(50L)).thenReturn(Optional.of(attempt));
        when(assessmentFeedbackRepository.findByAttemptId(50L)).thenReturn(Optional.empty());

        AssessmentFeedback savedFeedback = new AssessmentFeedback(testUser, assessment, testHost, attempt, 5, "Excellent", "Great assessment questions!");
        savedFeedback.setId(200L);
        when(assessmentFeedbackRepository.save(any(AssessmentFeedback.class))).thenReturn(savedFeedback);

        AssessmentFeedbackDto dto = supportFeedbackService.submitAssessmentFeedback(req, 1L);

        assertNotNull(dto);
        assertEquals(200L, dto.getId());
        assertEquals(5, dto.getRating());
        assertEquals("Excellent", dto.getFeedbackType());
        assertEquals(2L, dto.getHostId());
        assertEquals("hostuser", dto.getHostUsername());
        verify(emailService, times(1)).sendAssessmentFeedbackEmail(eq(testHost), eq(assessment), any(AssessmentFeedback.class));
    }

    @Test
    void testGetHostAssessmentFeedback() {
        Assessment assessment = new Assessment();
        assessment.setId(20L);
        assessment.setTitle("Full Stack Evaluation");
        assessment.setCreatedBy(testHost);

        AssessmentFeedback fb1 = new AssessmentFeedback(testUser, assessment, testHost, null, 5, "Excellent", "Great");
        fb1.setId(1L);
        AssessmentFeedback fb2 = new AssessmentFeedback(testUser, assessment, testHost, null, 4, "Good", "Nice");
        fb2.setId(2L);

        when(assessmentRepository.findById(20L)).thenReturn(Optional.of(assessment));
        when(assessmentFeedbackRepository.findByAssessmentIdOrderByCreatedAtDesc(20L)).thenReturn(List.of(fb1, fb2));

        AssessmentHostFeedbackSummaryDto summary = supportFeedbackService.getHostAssessmentFeedback(20L, 2L);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalFeedback());
        assertEquals(4.5, summary.getAverageRating());
        assertEquals(2, summary.getFeedbacks().size());
    }
}
