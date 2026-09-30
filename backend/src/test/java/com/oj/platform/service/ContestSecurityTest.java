package com.oj.platform.service;

import com.oj.platform.dto.ContestRegistrationDto;
import com.oj.platform.dto.ContestSecurityViolationDto;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focused tests for contest exam-security violation tracking (Task 9) - basic
 * browser-based monitoring (fullscreen-exit / tab-switch) against the existing
 * ContestAttempt, NOT a guaranteed anti-cheat mechanism. Fullscreen/visibility
 * detection itself lives in the frontend (ContestCoding.jsx) and isn't unit-testable
 * here; this covers the server-side recording/termination/enforcement it calls into.
 */
@ExtendWith(MockitoExtension.class)
class ContestSecurityTest {

    @Mock private ContestRepository contestRepository;
    @Mock private ContestProblemRepository contestProblemRepository;
    @Mock private ContestRegistrationRepository contestRegistrationRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ContestAttemptRepository contestAttemptRepository;
    @Mock private SubmissionRepository submissionRepository;

    private ContestService contestService;

    private User adminUser;
    private User student;
    private Contest ongoingContest;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(contestRepository, contestProblemRepository,
                contestRegistrationRepository, problemRepository, userRepository,
                contestAttemptRepository, submissionRepository);
        // application.properties defaults this to 3 - set explicitly here so the test
        // doesn't silently break if that default ever changes.
        ReflectionTestUtils.setField(contestService, "maxViolations", 3);

        adminUser = new User("Admin", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        adminUser.setId(1L);

        student = new User("Student", "student", "student@example.com", "hashed", Role.ROLE_USER);
        student.setId(2L);

        ongoingContest = new Contest("Weekly", "Org", "desc",
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().plusHours(1), adminUser);
        ongoingContest.setId(500L);
        ongoingContest.setStatus(ContestStatus.ONGOING);
    }

    private ContestAttempt attemptWithViolations(int count) {
        ContestAttempt attempt = new ContestAttempt(student, ongoingContest);
        attempt.setId(900L);
        attempt.setStatus(ContestAttemptStatus.IN_PROGRESS);
        attempt.setScore(300);
        attempt.setProblemsSolved(2);
        attempt.setSubmissionCount(4);
        attempt.setSecurityViolationCount(count);
        return attempt;
    }

    // 1. Security violation increments correctly.
    @Test
    void testViolationIncrementsCorrectly() {
        ContestAttempt attempt = attemptWithViolations(0);
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        ContestSecurityViolationDto result = contestService.recordSecurityViolation(500L, 2L);

        assertEquals(1, result.getViolationCount());
        assertFalse(result.isTerminated());
        assertEquals(1, attempt.getSecurityViolationCount());
    }

    // 2. Violation belongs to the authenticated participant - always looked up by the
    // userId the controller took from the JWT principal, never a client-supplied one.
    @Test
    void testViolationIsRecordedAgainstTheAuthenticatedParticipantsAttempt() {
        ContestAttempt attempt = attemptWithViolations(0);
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordSecurityViolation(500L, 2L);

        verify(contestAttemptRepository).findByParticipantIdAndContestId(2L, 500L);
        verify(contestAttemptRepository, never()).findByParticipantIdAndContestId(eq(1L), anyLong());
    }

    // 3. Security violation cannot be recorded for an unregistered user.
    @Test
    void testViolationRejectedForUnregisteredUser() {
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.recordSecurityViolation(500L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("register"));
        verify(contestAttemptRepository, never()).save(any());
    }

    // 4. Contest attempt is terminated at the configured threshold (3).
    @Test
    void testAttemptTerminatedAtConfiguredThreshold() {
        ContestAttempt attempt = attemptWithViolations(2); // one more violation hits the threshold
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        ContestSecurityViolationDto result = contestService.recordSecurityViolation(500L, 2L);

        assertEquals(3, result.getViolationCount());
        assertTrue(result.isTerminated());
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(ContestAttemptStatus.COMPLETED, attempt.getStatus());
        assertNotNull(attempt.getCompletedAt());
    }

    // 5. Submission is rejected after attempt termination.
    @Test
    void testSubmissionRejectedAfterTermination() {
        ContestAttempt terminated = attemptWithViolations(3);
        terminated.setStatus(ContestAttemptStatus.COMPLETED);
        terminated.setCompletedAt(LocalDateTime.now());

        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestProblemRepository.existsByContestIdAndProblemId(500L, 10L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(terminated));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.validateContestSubmissionAllowed(500L, 10L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("ended"));
    }

    // 6. Existing contest score remains intact after termination - only status,
    // completedAt, and securityViolationCount change; score/solved/submissionCount
    // are never touched by termination.
    @Test
    void testScoreAndSolvedCountRemainIntactAfterTermination() {
        ContestAttempt attempt = attemptWithViolations(2);
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordSecurityViolation(500L, 2L); // pushes to 3 -> terminates

        assertEquals(300, attempt.getScore());
        assertEquals(2, attempt.getProblemsSolved());
        assertEquals(4, attempt.getSubmissionCount());
    }

    // A second violation report after termination is handled idempotently - it returns
    // the already-terminated state rather than incrementing further or throwing.
    @Test
    void testViolationAfterTerminationDoesNotIncrementFurther() {
        ContestAttempt terminated = attemptWithViolations(3);
        terminated.setStatus(ContestAttemptStatus.COMPLETED);
        terminated.setCompletedAt(LocalDateTime.now());

        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(terminated));

        ContestSecurityViolationDto result = contestService.recordSecurityViolation(500L, 2L);

        assertEquals(3, result.getViolationCount());
        assertTrue(result.isTerminated());
        verify(contestAttemptRepository, never()).save(any());
    }

    @Test
    void testViolationWithCameraOrMicrophoneType() {
        ContestAttempt attempt = attemptWithViolations(1);
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        ContestSecurityViolationDto result = contestService.recordSecurityViolation(500L, 2L, "CAMERA_DISABLED");

        assertEquals(2, result.getViolationCount());
        assertFalse(result.isTerminated());
        assertEquals(2, attempt.getSecurityViolationCount());
    }

    @Test
    void testGetRegistrationStatusReflectsTerminatedAttempt() {
        ContestAttempt terminated = attemptWithViolations(3);
        terminated.setStatus(ContestAttemptStatus.COMPLETED);
        ContestRegistration registration = new ContestRegistration(student, ongoingContest);

        when(contestRegistrationRepository.findByUserIdAndContestId(2L, 500L)).thenReturn(Optional.of(registration));
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(terminated));

        ContestRegistrationDto dto = contestService.getRegistrationStatus(500L, 2L);

        assertTrue(dto.isRegistered());
        assertTrue(dto.getTerminated());
        assertEquals("COMPLETED", dto.getAttemptStatus());
        assertEquals(3, dto.getSecurityViolationCount());
    }
}
