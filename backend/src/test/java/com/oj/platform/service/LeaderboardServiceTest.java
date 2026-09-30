package com.oj.platform.service;

import com.oj.platform.dto.LeaderboardEntryDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.CertificateRepository;
import com.oj.platform.repository.SubmissionRepository;
import com.oj.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CertificateRepository certificateRepository;

    private LeaderboardService leaderboardService;

    private User alice;
    private User bob;
    private Problem easyProb;
    private Problem medProb;
    private Problem hardProb;

    @BeforeEach
    void setUp() {
        leaderboardService = new LeaderboardService(submissionRepository, userRepository, certificateRepository);

        alice = new User("Alice", "alice", "alice@test.com", "pass", Role.ROLE_USER);
        alice.setId(1L);

        bob = new User("Bob", "bob", "bob@test.com", "pass", Role.ROLE_USER);
        bob.setId(2L);

        easyProb = new Problem("Easy", "desc", Difficulty.EASY, "topic", "code");
        easyProb.setId(101L);

        medProb = new Problem("Med", "desc", Difficulty.MEDIUM, "topic", "code");
        medProb.setId(102L);

        hardProb = new Problem("Hard", "desc", Difficulty.HARD, "topic", "code");
        hardProb.setId(103L);
    }

    @Test
    void testRankingAndPoints() {
        // Alice solves Easy (100) and Med (200) = 300 pts. 2 submissions, 2 accepted (100%)
        Submission s1 = new Submission(alice, easyProb, "JAVA", "code");
        s1.setStatus(SubmissionStatus.ACCEPTED);
        s1.setExecutionTime(40L);

        Submission s2 = new Submission(alice, medProb, "JAVA", "code");
        s2.setStatus(SubmissionStatus.ACCEPTED);
        s2.setExecutionTime(60L);

        // Duplicate solve for Alice on Easy should NOT add extra score
        Submission s3 = new Submission(alice, easyProb, "JAVA", "code");
        s3.setStatus(SubmissionStatus.ACCEPTED);
        s3.setExecutionTime(35L);

        // Bob solves Hard (300) = 300 pts. 2 submissions, 1 failed, 1 accepted (50% acceptance)
        Submission b1 = new Submission(bob, hardProb, "PYTHON", "code");
        b1.setStatus(SubmissionStatus.WRONG_ANSWER);
        b1.setExecutionTime(50L);

        Submission b2 = new Submission(bob, hardProb, "PYTHON", "code");
        b2.setStatus(SubmissionStatus.ACCEPTED);
        b2.setExecutionTime(50L);

        when(userRepository.findAll()).thenReturn(List.of(alice, bob));
        when(submissionRepository.findAll()).thenReturn(List.of(s1, s2, s3, b1, b2));

        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard("GLOBAL", null);

        assertEquals(2, leaderboard.size());

        // Both have 300 points, but Alice solved 2 distinct problems (vs Bob's 1) and has 100% acceptance
        LeaderboardEntryDto rank1 = leaderboard.get(0);
        assertEquals("alice", rank1.getUsername());
        assertEquals(1, rank1.getRank());
        assertEquals(300, rank1.getScore());
        assertEquals(2, rank1.getProblemsSolved());
        assertEquals(35L, rank1.getFastestExecutionTimeMs());

        LeaderboardEntryDto rank2 = leaderboard.get(1);
        assertEquals("bob", rank2.getUsername());
        assertEquals(2, rank2.getRank());
        assertEquals(300, rank2.getScore());
        assertEquals(1, rank2.getProblemsSolved());
        assertEquals(50.0, rank2.getAcceptanceRate());
    }

    @Test
    void testLanguageFiltering() {
        Submission s1 = new Submission(alice, easyProb, "JAVA", "code");
        s1.setStatus(SubmissionStatus.ACCEPTED);

        Submission b1 = new Submission(bob, hardProb, "PYTHON", "code");
        b1.setStatus(SubmissionStatus.ACCEPTED);

        when(userRepository.findAll()).thenReturn(List.of(alice, bob));
        when(submissionRepository.findAll()).thenReturn(List.of(s1, b1));

        // Filter by Python: Only Bob should be present
        List<LeaderboardEntryDto> pyLeaderboard = leaderboardService.getLeaderboard("GLOBAL", "PYTHON");
        assertEquals(1, pyLeaderboard.size());
        assertEquals("bob", pyLeaderboard.get(0).getUsername());

        // Filter by Java: Only Alice should be present
        List<LeaderboardEntryDto> javaLeaderboard = leaderboardService.getLeaderboard("GLOBAL", "JAVA");
        assertEquals(1, javaLeaderboard.size());
        assertEquals("alice", javaLeaderboard.get(0).getUsername());
    }
}
