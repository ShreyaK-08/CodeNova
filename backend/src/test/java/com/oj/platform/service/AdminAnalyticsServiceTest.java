package com.oj.platform.service;

import com.oj.platform.dto.AdminAnalyticsDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminAnalyticsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private AssessmentAttemptRepository assessmentAttemptRepository;
    @Mock private AssessmentViolationRepository assessmentViolationRepository;
    @Mock private ContestRepository contestRepository;
    @Mock private ContestRegistrationRepository contestRegistrationRepository;
    @Mock private ContestAttemptRepository contestAttemptRepository;
    @Mock private SupportRequestRepository supportRequestRepository;
    @Mock private AssessmentFeedbackRepository assessmentFeedbackRepository;
    @Mock private AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository;
    @Mock private GeneralFeedbackRepository generalFeedbackRepository;

    @InjectMocks
    private AdminAnalyticsService adminAnalyticsService;

    private User testUser;
    private Problem testProblem;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        testUser = new User("Jane Doe", "janedoe", "jane@example.com", "pass", Role.ROLE_USER);
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(5));
        testUser.setEmailVerified(true);

        testProblem = new Problem();
        testProblem.setId(1L);
        testProblem.setTitle("Two Sum");
        testProblem.setDifficulty(Difficulty.EASY);

        testSubmission = new Submission();
        testSubmission.setId(10L);
        testSubmission.setUser(testUser);
        testSubmission.setProblem(testProblem);
        testSubmission.setStatus(SubmissionStatus.ACCEPTED);
        testSubmission.setLanguage("JAVA");
        testSubmission.setSubmittedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testGetAnalytics_AllTime() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(problemRepository.findAll()).thenReturn(List.of(testProblem));
        when(submissionRepository.findAll()).thenReturn(List.of(testSubmission));
        when(assessmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentAttemptRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentViolationRepository.count()).thenReturn(0L);
        when(contestRepository.findAll()).thenReturn(Collections.emptyList());
        when(contestRegistrationRepository.count()).thenReturn(0L);
        when(contestAttemptRepository.count()).thenReturn(0L);
        when(assessmentFeedbackRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentQuestionFeedbackRepository.findAll()).thenReturn(Collections.emptyList());
        when(supportRequestRepository.findAll()).thenReturn(Collections.emptyList());

        AdminAnalyticsDto dto = adminAnalyticsService.getAnalytics("ALL_TIME");
        assertNotNull(dto);
        assertEquals(1, dto.getOverview().getTotalUsers());
        assertEquals(1, dto.getOverview().getTotalProblems());
        assertEquals(1, dto.getOverview().getTotalSubmissions());
        assertEquals(1, dto.getProblems().getEasy());
        assertEquals(1, dto.getSubmissions().getAccepted());
        assertEquals(100.0, dto.getProblems().getAcceptanceRate());
    }

    @Test
    void testGenerateCsvReport() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        when(problemRepository.findAll()).thenReturn(List.of(testProblem));
        when(submissionRepository.findAll()).thenReturn(List.of(testSubmission));
        when(assessmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentAttemptRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentViolationRepository.count()).thenReturn(0L);
        when(contestRepository.findAll()).thenReturn(Collections.emptyList());
        when(contestRegistrationRepository.count()).thenReturn(0L);
        when(contestAttemptRepository.count()).thenReturn(0L);
        when(assessmentFeedbackRepository.findAll()).thenReturn(Collections.emptyList());
        when(assessmentQuestionFeedbackRepository.findAll()).thenReturn(Collections.emptyList());
        when(supportRequestRepository.findAll()).thenReturn(Collections.emptyList());

        String csv = adminAnalyticsService.generateCsvReport("ALL_TIME");
        assertNotNull(csv);
        assertTrue(csv.contains("CodeNova Platform Analytics & Performance Report"));
        assertTrue(csv.contains("Total Users,1"));
    }
}
