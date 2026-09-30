package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.UnauthorizedException;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focused tests for the Assessment backend (Task 10) - creation/publish validation,
 * student attempt lifecycle, server-side scoring, and the answer-key-hiding rules.
 * Student assessment UI, admin assessment UI, and the AI chatbot are out of scope for
 * this task and are not tested here.
 */
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock private AssessmentRepository assessmentRepository;
    @Mock private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock private AssessmentOptionRepository assessmentOptionRepository;
    @Mock private AssessmentAttemptRepository assessmentAttemptRepository;
    @Mock private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock private UserRepository userRepository;
    @Mock private AssessmentHostVerificationService hostVerificationService;
    @Mock private ProgrammingQuestionRepository programmingQuestionRepository;
    @Mock private ProgrammingTestCaseRepository programmingTestCaseRepository;
    @Mock private CandidateDetailsRepository candidateDetailsRepository;
    @Mock private AssessmentViolationRepository assessmentViolationRepository;
    @Mock private AssessmentProgrammingSubmissionRepository programmingSubmissionRepository;
    @Mock private CodeExecutionService codeExecutionService;

    private AssessmentService assessmentService;

    private User admin;
    private User student;
    private Assessment publishedAssessment;
    private AssessmentQuestion question1;
    private AssessmentOption correctOption;
    private AssessmentOption wrongOption;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentService(
                assessmentRepository, assessmentQuestionRepository, assessmentOptionRepository,
                assessmentAttemptRepository, assessmentAnswerRepository, userRepository,
                hostVerificationService, programmingQuestionRepository, programmingTestCaseRepository,
                candidateDetailsRepository, assessmentViolationRepository, programmingSubmissionRepository,
                codeExecutionService);

        admin = new User("Admin", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        admin.setId(1L);

        student = new User("Student", "student", "student@example.com", "hashed", Role.ROLE_USER);
        student.setId(2L);

        publishedAssessment = new Assessment("Java & OOP Fundamentals", "desc", "instructions", 10, 1, admin);
        publishedAssessment.setId(100L);
        publishedAssessment.setStatus(AssessmentStatus.PUBLISHED);
        publishedAssessment.setTotalMarks(1);

        question1 = new AssessmentQuestion(publishedAssessment, "Which keyword is used to inherit a class?",
                AssessmentQuestionType.MCQ, 1, 1, "extends is used for inheritance.");
        question1.setId(200L);

        correctOption = new AssessmentOption(question1, "extends", true, 1);
        correctOption.setId(300L);
        wrongOption = new AssessmentOption(question1, "implements", false, 2);
        wrongOption.setId(301L);
    }

    // ---- creation ----

    @Test
    void testCreateAssessment() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
            Assessment a = inv.getArgument(0);
            a.setId(999L);
            return a;
        });
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(999L)).thenReturn(List.of());

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("New Quiz");
        request.setDescription("desc");
        request.setInstructions("instructions");
        request.setDurationMinutes(15);
        request.setPassingMarks(2);

        AssessmentDto dto = assessmentService.createAssessment(request, 1L);

        assertEquals("New Quiz", dto.getTitle());
        assertEquals(15, dto.getDurationMinutes());
        assertEquals("DRAFT", dto.getStatus());
        assertEquals(0, dto.getQuestionCount());
    }

    // ---- host assessment verification gate (Task: "Host Assessment Verification +
    // Admin Approval + Email System", Section 8 / test cases 13 & 14) ----

    @Test
    void testUnverifiedUserCannotCreateHostedAssessment() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(hostVerificationService.isVerifiedHost(2L)).thenReturn(false);

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Community Quiz");
        request.setDurationMinutes(10);
        request.setPassingMarks(1);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.createHostedAssessment(request, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("verified"));
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    void testVerifiedUserCanCreateHostedAssessment() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(hostVerificationService.isVerifiedHost(2L)).thenReturn(true);
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
            Assessment a = inv.getArgument(0);
            a.setId(555L);
            return a;
        });
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(555L)).thenReturn(List.of());

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Community Quiz");
        request.setDurationMinutes(10);
        request.setPassingMarks(1);

        AssessmentDto dto = assessmentService.createHostedAssessment(request, 2L);

        assertEquals("Community Quiz", dto.getTitle());
        assertEquals("DRAFT", dto.getStatus());
        verify(hostVerificationService).isVerifiedHost(2L);
    }

    @Test
    void testAdminCanCreateHostedAssessmentWithoutVerification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
            Assessment a = inv.getArgument(0);
            a.setId(556L);
            return a;
        });
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(556L)).thenReturn(List.of());

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Admin Quiz");
        request.setDurationMinutes(10);
        request.setPassingMarks(1);

        AssessmentDto dto = assessmentService.createHostedAssessment(request, 1L);

        assertEquals("Admin Quiz", dto.getTitle());
        verify(hostVerificationService, never()).isVerifiedHost(anyLong());
    }

    @Test
    void testHostCannotManageAnotherHostsAssessment() {
        User otherHost = new User("Other Host", "otherhost", "other@example.com", "hashed", Role.ROLE_USER);
        otherHost.setId(3L);
        Assessment ownedByOther = new Assessment("Other's Quiz", "d", "i", 10, 1, otherHost);
        ownedByOther.setId(777L);

        when(assessmentRepository.findById(777L)).thenReturn(Optional.of(ownedByOther));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Hijacked title");
        request.setDurationMinutes(10);
        request.setPassingMarks(1);

        assertThrows(ForbiddenException.class,
                () -> assessmentService.updateOwnAssessment(777L, request, 2L));
    }

    // ---- publish validation ----

    @Test
    void testPublishFailsWithNoQuestions() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> assessmentService.publishAssessment(100L));
        assertTrue(ex.getMessage().toLowerCase().contains("no questions"));
    }

    @Test
    void testPublishSucceedsWithValidQuestionAndOptions() {
        publishedAssessment.setStatus(AssessmentStatus.DRAFT);
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> inv.getArgument(0));

        AssessmentDto dto = assessmentService.publishAssessment(100L);

        assertEquals("PUBLISHED", dto.getStatus());
    }

    // ---- published assessment retrieval ----

    @Test
    void testListPublishedAssessmentsOnlyReturnsPublished() {
        when(assessmentRepository.findByStatus(AssessmentStatus.PUBLISHED)).thenReturn(List.of(publishedAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));

        List<AssessmentDto> result = assessmentService.listPublishedAssessments();

        assertEquals(1, result.size());
        assertEquals("Java & OOP Fundamentals", result.get(0).getTitle());
        assertNull(result.get(0).getQuestions()); // list view never embeds questions
    }

    // ---- starting an attempt ----

    @Test
    void testStartAttemptCreatesNewAttemptWhenNoneActive() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentAttemptRepository.findInProgressByAssessmentIdAndUserId(100L, 2L))
                .thenReturn(Optional.empty());
        when(assessmentAttemptRepository.countByAssessmentIdAndUserId(100L, 2L)).thenReturn(0L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(assessmentQuestionRepository.countByAssessmentId(100L)).thenReturn(1L);
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
            AssessmentAttempt a = inv.getArgument(0);
            a.setId(500L);
            return a;
        });
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));
        when(assessmentAnswerRepository.findByAttemptId(500L)).thenReturn(List.of());

        AssessmentAttemptDto dto = assessmentService.startAttempt(100L, 2L);

        assertEquals("IN_PROGRESS", dto.getStatus());
        assertEquals(1, dto.getTotalQuestions());
        assertEquals(1, dto.getAttemptNumber());
        // Answer key must be hidden while IN_PROGRESS.
        assertNull(dto.getQuestions().get(0).getOptions().get(0).getIsCorrect());
        verify(assessmentAttemptRepository, times(1)).save(any(AssessmentAttempt.class));
    }

    @Test
    void testStartAttemptResumesExistingInProgressAttemptInsteadOfDuplicating() {
        AssessmentAttempt existing = new AssessmentAttempt(publishedAssessment, student);
        existing.setId(501L);
        existing.setStartedAt(LocalDateTime.now().minusMinutes(2));
        existing.setStatus(AssessmentAttemptStatus.IN_PROGRESS);
        existing.setTotalQuestions(1);
        existing.setAttemptNumber(1);

        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentAttemptRepository.findInProgressByAssessmentIdAndUserId(100L, 2L))
                .thenReturn(Optional.of(existing));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));
        when(assessmentAnswerRepository.findByAttemptId(501L)).thenReturn(List.of());

        AssessmentAttemptDto dto = assessmentService.startAttempt(100L, 2L);

        assertEquals(501L, dto.getId());
        assertEquals("IN_PROGRESS", dto.getStatus());
        verify(userRepository, never()).findById(anyLong()); // resumes without creating new
        verify(assessmentAttemptRepository, never()).save(argThat(a -> a != existing));
    }

    @Test
    void testStartAttemptCreatesSecondAttemptWhenMaxAttemptsAllows() {
        publishedAssessment.setMaxAttempts(2);

        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentAttemptRepository.findInProgressByAssessmentIdAndUserId(100L, 2L))
                .thenReturn(Optional.empty());
        when(assessmentAttemptRepository.countByAssessmentIdAndUserId(100L, 2L)).thenReturn(1L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(assessmentQuestionRepository.countByAssessmentId(100L)).thenReturn(1L);
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
            AssessmentAttempt a = inv.getArgument(0);
            a.setId(505L);
            return a;
        });
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));
        when(assessmentAnswerRepository.findByAttemptId(505L)).thenReturn(List.of());

        AssessmentAttemptDto dto = assessmentService.startAttempt(100L, 2L);

        assertEquals(505L, dto.getId());
        assertEquals(2, dto.getAttemptNumber());
        assertEquals("IN_PROGRESS", dto.getStatus());
    }

    @Test
    void testStartAttemptThrowsWhenMaxAttemptsExceeded() {
        publishedAssessment.setMaxAttempts(1);

        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(publishedAssessment));
        when(assessmentAttemptRepository.findInProgressByAssessmentIdAndUserId(100L, 2L))
                .thenReturn(Optional.empty());
        when(assessmentAttemptRepository.countByAssessmentIdAndUserId(100L, 2L)).thenReturn(1L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> assessmentService.startAttempt(100L, 2L));
        assertTrue(ex.getMessage().contains("used all 1 attempt"));
    }

    // ---- unauthorized attempt access ----

    @Test
    void testGetAttemptRejectsAccessByADifferentUser() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> assessmentService.getAttempt(100L, 501L, 999L)); // 999L is not this attempt's owner
        assertTrue(ex.getMessage().toLowerCase().contains("another user"));
    }

    // ---- answer recording / duplicate protection ----

    @Test
    void testRecordAnswerUpsertsRatherThanDuplicating() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentQuestionRepository.findById(200L)).thenReturn(Optional.of(question1));
        when(assessmentOptionRepository.findById(300L)).thenReturn(Optional.of(correctOption));
        when(assessmentOptionRepository.findById(301L)).thenReturn(Optional.of(wrongOption));

        // A mutable holder standing in for "the database row" - findByAttemptIdAndQuestionId
        // returns whatever the most recent save() actually persisted, so the second call
        // sees the row the first call created (rather than a second, separately-built one).
        final AssessmentAnswer[] persisted = new AssessmentAnswer[1];
        when(assessmentAnswerRepository.save(any(AssessmentAnswer.class))).thenAnswer(inv -> {
            AssessmentAnswer a = inv.getArgument(0);
            persisted[0] = a;
            return a;
        });
        when(assessmentAnswerRepository.findByAttemptIdAndQuestionId(501L, 200L))
                .thenAnswer(inv -> Optional.ofNullable(persisted[0]));

        // First answer: pick the wrong option.
        SubmitAssessmentAnswerRequest req1 = new SubmitAssessmentAnswerRequest();
        req1.setQuestionId(200L);
        req1.setSelectedOptionId(301L);
        assessmentService.recordAnswer(100L, 501L, 2L, req1);

        // Second call for the SAME question: change the answer to the correct option.
        SubmitAssessmentAnswerRequest req2 = new SubmitAssessmentAnswerRequest();
        req2.setQuestionId(200L);
        req2.setSelectedOptionId(300L);
        AssessmentAnswerDto result = assessmentService.recordAnswer(100L, 501L, 2L, req2);

        assertEquals(300L, result.getSelectedOptionId());
        // Never graded at record time - only submitAttempt grades.
        assertNull(result.getIsCorrect());

        org.mockito.ArgumentCaptor<AssessmentAnswer> captor = org.mockito.ArgumentCaptor.forClass(AssessmentAnswer.class);
        verify(assessmentAnswerRepository, times(2)).save(captor.capture());
        // Both saves touched the exact same row - the second call updated it, never inserted another.
        assertSame(captor.getAllValues().get(0), captor.getAllValues().get(1));
    }

    @Test
    void testRecordAnswerRejectedOnceAttemptHasExpired() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(30)); // way past the 10-minute duration
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitAssessmentAnswerRequest req = new SubmitAssessmentAnswerRequest();
        req.setQuestionId(200L);
        req.setSelectedOptionId(300L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> assessmentService.recordAnswer(100L, 501L, 2L, req));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        assertEquals(AssessmentAttemptStatus.EXPIRED, attempt.getStatus());
    }

    // ---- server-side scoring / pass-fail ----

    @Test
    void testSubmitAttemptComputesScoreServerSideFromStoredAnswers() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);
        attempt.setTotalQuestions(1);

        AssessmentAnswer answer = new AssessmentAnswer(attempt, question1, correctOption);
        answer.setId(700L);
        // isCorrect/marksAwarded intentionally left null here - as they always are before submit.

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentAnswerRepository.findByAttemptId(501L)).thenReturn(List.of(answer));
        when(assessmentAnswerRepository.save(any(AssessmentAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));

        AssessmentAttemptDto dto = assessmentService.submitAttempt(100L, 501L, 2L);

        assertEquals("COMPLETED", dto.getStatus());
        assertEquals(1, dto.getScore());       // question1 is worth 1 mark, answered correctly
        assertEquals(1, dto.getCorrectAnswers());
        assertTrue(dto.getPassed());           // passingMarks is 1 on publishedAssessment
        assertTrue(answer.getIsCorrect());
        assertEquals(1, answer.getMarksAwarded());
    }

    @Test
    void testSubmitAttemptFailsWhenScoreBelowPassingMarks() {
        publishedAssessment.setPassingMarks(1);
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);
        attempt.setTotalQuestions(1);

        AssessmentAnswer wrongAnswer = new AssessmentAnswer(attempt, question1, wrongOption);
        wrongAnswer.setId(701L);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentAnswerRepository.findByAttemptId(501L)).thenReturn(List.of(wrongAnswer));
        when(assessmentAnswerRepository.save(any(AssessmentAnswer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));

        AssessmentAttemptDto dto = assessmentService.submitAttempt(100L, 501L, 2L);

        assertEquals(0, dto.getScore());
        assertFalse(dto.getPassed());
        assertFalse(wrongAnswer.getIsCorrect());
    }

    // ---- completed / expired attempt rejection ----

    @Test
    void testSubmitAttemptRejectedIfAlreadyCompleted() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> assessmentService.submitAttempt(100L, 501L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("already"));
    }

    @Test
    void testSubmitAttemptRejectedIfExpired() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(30)); // past the 10-minute duration
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentAttemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> assessmentService.submitAttempt(100L, 501L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("expired"));
        assertEquals(AssessmentAttemptStatus.EXPIRED, attempt.getStatus());
    }

    // ---- correct answers hidden before submission ----

    @Test
    void testAnswerKeyHiddenWhileAttemptInProgress() {
        AssessmentAttempt attempt = new AssessmentAttempt(publishedAssessment, student);
        attempt.setId(501L);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setStatus(AssessmentAttemptStatus.IN_PROGRESS);
        attempt.setTotalQuestions(1);

        when(assessmentAttemptRepository.findById(501L)).thenReturn(Optional.of(attempt));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L)).thenReturn(List.of(question1));
        when(assessmentOptionRepository.findByQuestionIdOrderByOrderIndexAsc(200L)).thenReturn(List.of(correctOption, wrongOption));
        when(assessmentAnswerRepository.findByAttemptId(501L)).thenReturn(List.of());

        AssessmentAttemptDto dto = assessmentService.getAttempt(100L, 501L, 2L);

        assertNull(dto.getQuestions().get(0).getExplanation());
        for (AssessmentOptionDto option : dto.getQuestions().get(0).getOptions()) {
            assertNull(option.getIsCorrect());
        }
        assertNull(dto.getPassed());
    }

    @Test
    void testAdminCannotEditAssessmentHostedByOtherUser() {
        User hostUser = new User("Host", "hostuser", "host@example.com", "pass", Role.ROLE_USER);
        hostUser.setId(5L);
        Assessment hostedAssessment = new Assessment("Hosted Assessment", "desc", "instructions", 20, 2, hostUser);
        hostedAssessment.setId(888L);

        when(assessmentRepository.findById(888L)).thenReturn(Optional.of(hostedAssessment));

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Admin Trying To Overwrite");
        request.setDurationMinutes(25);
        request.setPassingMarks(3);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.updateAssessment(888L, request));
        assertTrue(ex.getMessage().contains("hosted by other users"));
    }

    @Test
    void testAdminCannotAddQuestionToAssessmentHostedByOtherUser() {
        User hostUser = new User("Host", "hostuser", "host@example.com", "pass", Role.ROLE_USER);
        hostUser.setId(5L);
        Assessment hostedAssessment = new Assessment("Hosted Assessment", "desc", "instructions", 20, 2, hostUser);
        hostedAssessment.setId(888L);

        when(assessmentRepository.findById(888L)).thenReturn(Optional.of(hostedAssessment));

        CreateAssessmentQuestionRequest request = new CreateAssessmentQuestionRequest();
        request.setQuestionText("Admin Added Question?");
        request.setQuestionType("MCQ");
        request.setMarks(2);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.addQuestion(888L, request));
        assertTrue(ex.getMessage().contains("hosted by other users"));
    }

    @Test
    void testAdminCannotPublishAssessmentHostedByOtherUser() {
        User hostUser = new User("Host", "hostuser", "host@example.com", "pass", Role.ROLE_USER);
        hostUser.setId(5L);
        Assessment hostedAssessment = new Assessment("Hosted Assessment", "desc", "instructions", 20, 2, hostUser);
        hostedAssessment.setId(888L);

        when(assessmentRepository.findById(888L)).thenReturn(Optional.of(hostedAssessment));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.publishAssessment(888L));
        assertTrue(ex.getMessage().contains("hosted by other users"));
    }

    @Test
    void testVerifiedUserCreatingSecondAssessmentDoesNotNeedReverification() {
        User verifiedUser = new User("Verified Host", "host1", "host1@example.com", "pass", Role.ROLE_USER);
        verifiedUser.setId(11L);
        when(userRepository.findById(11L)).thenReturn(Optional.of(verifiedUser));
        when(hostVerificationService.isVerifiedHost(11L)).thenReturn(true);
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
            Assessment a = inv.getArgument(0);
            a.setId(1001L);
            return a;
        });

        CreateAssessmentRequest request = new CreateAssessmentRequest();
        request.setTitle("Second Assessment");
        request.setDurationMinutes(60);
        request.setPassingMarks(20);

        AssessmentDto dto = assessmentService.createHostedAssessment(request, 11L);
        assertNotNull(dto);
        assertEquals("Second Assessment", dto.getTitle());
        // Still verified, successfully creates 2nd assessment
        verify(hostVerificationService, times(1)).isVerifiedHost(11L);
    }

    @Test
    void testUserACannotEditUserBAssessment() {
        User userA = new User("User A", "usera", "a@example.com", "pass", Role.ROLE_USER);
        userA.setId(21L);
        User userB = new User("User B", "userb", "b@example.com", "pass", Role.ROLE_USER);
        userB.setId(22L);

        Assessment assessmentB = new Assessment("Assessment B", "desc", "inst", 30, 10, userB);
        assessmentB.setId(500L);

        when(assessmentRepository.findById(500L)).thenReturn(Optional.of(assessmentB));
        when(userRepository.findById(21L)).thenReturn(Optional.of(userA));

        CreateAssessmentRequest updateReq = new CreateAssessmentRequest();
        updateReq.setTitle("Hacked Title");
        updateReq.setDurationMinutes(30);
        updateReq.setPassingMarks(10);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.updateOwnAssessment(500L, updateReq, 21L));
        assertTrue(ex.getMessage().contains("Cannot edit an assessment hosted by another user")
                || ex.getMessage().contains("permission"));
    }

    @Test
    void testUserACannotViewUserBCandidateAttempts() {
        User userA = new User("User A", "usera", "a@example.com", "pass", Role.ROLE_USER);
        userA.setId(21L);
        User userB = new User("User B", "userb", "b@example.com", "pass", Role.ROLE_USER);
        userB.setId(22L);

        Assessment assessmentB = new Assessment("Assessment B", "desc", "inst", 30, 10, userB);
        assessmentB.setId(500L);

        when(assessmentRepository.findById(500L)).thenReturn(Optional.of(assessmentB));
        when(userRepository.findById(21L)).thenReturn(Optional.of(userA));

        assertThrows(ForbiddenException.class,
                () -> assessmentService.getHostAssessmentAttempts(500L, 21L));
    }

    @Test
    void testUserACannotAddQuestionToUserBAssessment() {
        User userA = new User("User A", "usera", "a@example.com", "pass", Role.ROLE_USER);
        userA.setId(21L);
        User userB = new User("User B", "userb", "b@example.com", "pass", Role.ROLE_USER);
        userB.setId(22L);

        Assessment assessmentB = new Assessment("Assessment B", "desc", "inst", 30, 10, userB);
        assessmentB.setId(500L);

        when(assessmentRepository.findById(500L)).thenReturn(Optional.of(assessmentB));
        when(userRepository.findById(21L)).thenReturn(Optional.of(userA));

        CreateAssessmentQuestionRequest qReq = new CreateAssessmentQuestionRequest();
        qReq.setQuestionText("Malicious question");
        qReq.setQuestionType("MCQ");
        qReq.setMarks(5);

        assertThrows(ForbiddenException.class,
                () -> assessmentService.addOwnQuestion(500L, qReq, 21L));
    }

    @Test
    void testAdminCannotDeleteUserAAssessmentQuestion() {
        User userA = new User("User A", "usera", "a@example.com", "pass", Role.ROLE_USER);
        userA.setId(21L);

        Assessment assessmentA = new Assessment("Assessment A", "desc", "inst", 30, 10, userA);
        assessmentA.setId(700L);

        AssessmentQuestion question = new AssessmentQuestion(assessmentA, "Q1", AssessmentQuestionType.MCQ, 2, 1, "exp");
        question.setId(701L);

        when(assessmentQuestionRepository.findById(701L)).thenReturn(Optional.of(question));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> assessmentService.deleteQuestion(700L, 701L));
        assertTrue(ex.getMessage().contains("hosted by other users"));
    }

    @Test
    void testAdminCanViewAssessmentMonitoringDetails() {
        User userA = new User("User A", "usera", "a@example.com", "pass", Role.ROLE_USER);
        userA.setId(21L);

        Assessment assessmentA = new Assessment("User Assessment", "desc", "inst", 30, 10, userA);
        assessmentA.setId(800L);

        when(assessmentRepository.findById(800L)).thenReturn(Optional.of(assessmentA));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(800L)).thenReturn(List.of());
        when(assessmentAttemptRepository.countByAssessmentId(800L)).thenReturn(5L);
        when(assessmentAttemptRepository.countDistinctUsersByAssessmentId(800L)).thenReturn(3L);
        when(hostVerificationService.getOrganizationName(21L)).thenReturn(Optional.of("Tech University"));

        AssessmentDto dto = assessmentService.getAssessmentAdmin(800L);

        assertNotNull(dto);
        assertEquals(800L, dto.getId());
        assertEquals("User Assessment", dto.getTitle());
        assertTrue(dto.isHostedByOtherUser());
        assertEquals("usera", dto.getCreatedByUsername());
        assertEquals("a@example.com", dto.getHostEmail());
        assertEquals("Tech University", dto.getOrganizationName());
        assertEquals(5, dto.getTotalAttempts());
        assertEquals(3, dto.getCandidateCount());
    }

    @Test
    void testHostCanViewTheirCandidateAttempts() {
        User host = new User("Host", "host1", "host@example.com", "pass", Role.ROLE_USER);
        host.setId(31L);

        Assessment assessment = new Assessment("Host Assessment", "desc", "inst", 30, 10, host);
        assessment.setId(900L);

        User candidate = new User("Candidate One", "cand1", "cand1@example.com", "pass", Role.ROLE_USER);
        candidate.setId(32L);

        AssessmentAttempt attempt = new AssessmentAttempt(assessment, candidate);
        attempt.setId(901L);
        attempt.setStatus(AssessmentAttemptStatus.COMPLETED);
        attempt.setScore(10);
        attempt.setCompletedAt(LocalDateTime.now());

        when(assessmentRepository.findById(900L)).thenReturn(Optional.of(assessment));
        when(userRepository.findById(31L)).thenReturn(Optional.of(host));
        when(assessmentAttemptRepository.findByAssessmentIdOrderByStartedAtDesc(900L)).thenReturn(List.of(attempt));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(900L)).thenReturn(List.of());
        when(assessmentAnswerRepository.findByAttemptId(901L)).thenReturn(List.of());

        List<AssessmentAttemptDto> attempts = assessmentService.getHostAssessmentAttempts(900L, 31L);

        assertNotNull(attempts);
        assertEquals(1, attempts.size());
        AssessmentAttemptDto attDto = attempts.get(0);
        assertEquals("cand1", attDto.getCandidateUsername());
        assertEquals("Candidate One", attDto.getCandidateFullName());
        assertEquals("cand1@example.com", attDto.getCandidateEmail());
        assertEquals(10, attDto.getScore());
        assertTrue(attDto.getPassed());
    }
}
