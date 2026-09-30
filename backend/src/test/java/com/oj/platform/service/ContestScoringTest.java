package com.oj.platform.service;

import com.oj.platform.dto.ContestAttemptDto;
import com.oj.platform.dto.ContestLeaderboardEntryDto;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focused tests for contest scoring, participation tracking, and ranking (Task 8).
 * Contest solving itself (Monaco/run/submit UI, Task 7) and fullscreen/exam-security
 * (still unimplemented) are out of scope here.
 */
@ExtendWith(MockitoExtension.class)
class ContestScoringTest {

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
    private Problem problemA;
    private Problem problemB;
    private ContestProblem contestProblemA;
    private ContestProblem contestProblemB;

    @BeforeEach
    void setUp() {
        contestService = new ContestService(contestRepository, contestProblemRepository,
                contestRegistrationRepository, problemRepository, userRepository,
                contestAttemptRepository, submissionRepository);

        adminUser = new User("Admin", "admin", "admin@example.com", "hashed", Role.ROLE_ADMIN);
        adminUser.setId(1L);

        student = new User("Student", "student", "student@example.com", "hashed", Role.ROLE_USER);
        student.setId(2L);

        ongoingContest = new Contest("Weekly", "Org", "desc",
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().plusHours(1), adminUser);
        ongoingContest.setId(500L);
        ongoingContest.setStatus(ContestStatus.ONGOING);

        problemA = new Problem("Two Sum", "desc", Difficulty.EASY, "Arrays", "starter");
        problemA.setId(10L);
        problemB = new Problem("Binary Search", "desc", Difficulty.EASY, "Search", "starter");
        problemB.setId(11L);

        contestProblemA = new ContestProblem(ongoingContest, problemA, 1, 100);
        contestProblemB = new ContestProblem(ongoingContest, problemB, 2, 200);
    }

    private ContestAttempt existingAttempt() {
        ContestAttempt attempt = new ContestAttempt(student, ongoingContest);
        attempt.setId(900L);
        attempt.setStatus(ContestAttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(10));
        attempt.setScore(0);
        attempt.setProblemsSolved(0);
        attempt.setSubmissionCount(0);
        return attempt;
    }

    // 1. First accepted contest problem awards points.
    @Test
    void testFirstAcceptedContestProblemAwardsPoints() {
        ContestAttempt attempt = existingAttempt();
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(500L)).thenReturn(List.of(contestProblemA, contestProblemB));
        when(submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(500L, 2L)).thenReturn(List.of(10L));
        when(submissionRepository.countByContestIdAndUserId(500L, 2L)).thenReturn(1L);
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordContestSubmissionOutcome(500L, 2L, 10L, SubmissionStatus.ACCEPTED);

        assertEquals(100, attempt.getScore());
        assertEquals(1, attempt.getProblemsSolved());
    }

    // 2. Same accepted problem submitted again does not award duplicate points.
    @Test
    void testReSubmittingSameAcceptedProblemDoesNotDoubleScore() {
        ContestAttempt attempt = existingAttempt();
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(500L)).thenReturn(List.of(contestProblemA, contestProblemB));
        // Distinct-accepted-problem-ids stays [10] on the DB whether the participant
        // accepted problem 10 once or five times - that's exactly what "distinct" means.
        when(submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(500L, 2L)).thenReturn(List.of(10L));
        when(submissionRepository.countByContestIdAndUserId(500L, 2L)).thenReturn(1L, 2L);
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordContestSubmissionOutcome(500L, 2L, 10L, SubmissionStatus.ACCEPTED);
        assertEquals(100, attempt.getScore());

        contestService.recordContestSubmissionOutcome(500L, 2L, 10L, SubmissionStatus.ACCEPTED);

        // Never 100 + 100 = 200 for the same solved problem.
        assertEquals(100, attempt.getScore());
        assertEquals(1, attempt.getProblemsSolved());
        assertEquals(2, attempt.getSubmissionCount());
    }

    // 3. Wrong answer awards zero points.
    @Test
    void testWrongAnswerAwardsZeroPoints() {
        ContestAttempt attempt = existingAttempt();
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(500L)).thenReturn(List.of(contestProblemA, contestProblemB));
        when(submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(500L, 2L)).thenReturn(List.of());
        when(submissionRepository.countByContestIdAndUserId(500L, 2L)).thenReturn(1L);
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordContestSubmissionOutcome(500L, 2L, 10L, SubmissionStatus.WRONG_ANSWER);

        assertEquals(0, attempt.getScore());
        assertEquals(0, attempt.getProblemsSolved());
        assertEquals(1, attempt.getSubmissionCount());
    }

    // 4. Contest score equals the sum of distinct solved problems' configured points.
    @Test
    void testScoreEqualsSumOfDistinctSolvedProblemPoints() {
        ContestAttempt attempt = existingAttempt();
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(500L)).thenReturn(List.of(contestProblemA, contestProblemB));
        when(submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(500L, 2L)).thenReturn(List.of(10L, 11L));
        when(submissionRepository.countByContestIdAndUserId(500L, 2L)).thenReturn(2L);
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordContestSubmissionOutcome(500L, 2L, 11L, SubmissionStatus.ACCEPTED);

        assertEquals(300, attempt.getScore()); // 100 (A) + 200 (B)
    }

    // 5. Solved count uses distinct solved problems (covered above too, isolated here).
    @Test
    void testSolvedCountUsesDistinctSolvedProblems() {
        ContestAttempt attempt = existingAttempt();
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L)).thenReturn(Optional.of(attempt));
        when(contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(500L)).thenReturn(List.of(contestProblemA, contestProblemB));
        when(submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(500L, 2L)).thenReturn(List.of(10L, 11L));
        when(submissionRepository.countByContestIdAndUserId(500L, 2L)).thenReturn(3L);
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        contestService.recordContestSubmissionOutcome(500L, 2L, 11L, SubmissionStatus.ACCEPTED);

        assertEquals(2, attempt.getProblemsSolved());
    }

    // 6. Duplicate contest registration/attempt does not create duplicate attempts.
    @Test
    void testGetOrCreateAttemptDoesNotCreateDuplicates() {
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 500L)).thenReturn(true);
        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        ContestAttempt created = existingAttempt();
        // First call: no attempt exists yet -> created.
        when(contestAttemptRepository.findByParticipantIdAndContestId(2L, 500L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(created));
        when(contestAttemptRepository.save(any(ContestAttempt.class))).thenReturn(created);

        ContestAttemptDto first = contestService.getOrCreateAttempt(500L, 2L);
        ContestAttemptDto second = contestService.getOrCreateAttempt(500L, 2L);

        assertNotNull(first);
        assertNotNull(second);
        // Exactly one attempt row is ever created/saved across both calls.
        verify(contestAttemptRepository, times(1)).save(any(ContestAttempt.class));
        verify(userRepository, times(1)).findById(2L);
    }

    // 7. Submission before contest start is rejected.
    @Test
    void testSubmissionBeforeContestStartIsRejected() {
        Contest future = new Contest("Future", "Org", "desc",
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), adminUser);
        future.setId(600L);
        when(contestRepository.findById(600L)).thenReturn(Optional.of(future));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 600L)).thenReturn(true);
        when(contestProblemRepository.existsByContestIdAndProblemId(600L, 10L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.validateContestSubmissionAllowed(600L, 10L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("not started"));
    }

    // 8. Submission after contest end is rejected.
    @Test
    void testSubmissionAfterContestEndIsRejected() {
        Contest ended = new Contest("Ended", "Org", "desc",
                LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1), adminUser);
        ended.setId(700L);
        when(contestRepository.findById(700L)).thenReturn(Optional.of(ended));
        when(contestRegistrationRepository.existsByUserIdAndContestId(2L, 700L)).thenReturn(true);
        when(contestProblemRepository.existsByContestIdAndProblemId(700L, 10L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> contestService.validateContestSubmissionAllowed(700L, 10L, 2L));
        assertTrue(ex.getMessage().toLowerCase().contains("ended"));
    }

    // 9. Contest leaderboard orders participants correctly (score desc, then solved
    //    desc, then earlier last-accepted-submission time first).
    @Test
    void testLeaderboardOrdersParticipantsCorrectly() {
        User alice = new User("Alice", "alice", "alice@example.com", "hashed", Role.ROLE_USER);
        alice.setId(21L);
        User bob = new User("Bob", "bob", "bob@example.com", "hashed", Role.ROLE_USER);
        bob.setId(22L);
        User carol = new User("Carol", "carol", "carol@example.com", "hashed", Role.ROLE_USER);
        carol.setId(23L);

        ContestAttempt attemptAlice = new ContestAttempt(alice, ongoingContest);
        attemptAlice.setStatus(ContestAttemptStatus.IN_PROGRESS);
        attemptAlice.setScore(300);
        attemptAlice.setProblemsSolved(2);
        attemptAlice.setSubmissionCount(3);

        ContestAttempt attemptBob = new ContestAttempt(bob, ongoingContest);
        attemptBob.setStatus(ContestAttemptStatus.IN_PROGRESS);
        attemptBob.setScore(300);
        attemptBob.setProblemsSolved(2);
        attemptBob.setSubmissionCount(2);

        ContestAttempt attemptCarol = new ContestAttempt(carol, ongoingContest);
        attemptCarol.setStatus(ContestAttemptStatus.IN_PROGRESS);
        attemptCarol.setScore(100);
        attemptCarol.setProblemsSolved(1);
        attemptCarol.setSubmissionCount(1);

        when(contestRepository.findById(500L)).thenReturn(Optional.of(ongoingContest));
        when(contestAttemptRepository.findByContestId(500L)).thenReturn(List.of(attemptAlice, attemptBob, attemptCarol));

        LocalDateTime earlier = LocalDateTime.now().minusMinutes(20);
        LocalDateTime later = LocalDateTime.now().minusMinutes(5);
        // Alice and Bob are tied on score AND solved count - Bob finished earlier, so
        // Bob must rank above Alice despite an identical score/solved count.
        when(submissionRepository.findLastAcceptedSubmissionTime(500L, 21L)).thenReturn(later);
        when(submissionRepository.findLastAcceptedSubmissionTime(500L, 22L)).thenReturn(earlier);
        when(submissionRepository.findLastAcceptedSubmissionTime(500L, 23L)).thenReturn(later);

        List<ContestLeaderboardEntryDto> leaderboard = contestService.getLeaderboard(500L);

        assertEquals(3, leaderboard.size());
        assertEquals("bob", leaderboard.get(0).getUsername());
        assertEquals(1, leaderboard.get(0).getRank());
        assertEquals("alice", leaderboard.get(1).getUsername());
        assertEquals(2, leaderboard.get(1).getRank());
        assertEquals("carol", leaderboard.get(2).getUsername());
        assertEquals(3, leaderboard.get(2).getRank());
    }
}
