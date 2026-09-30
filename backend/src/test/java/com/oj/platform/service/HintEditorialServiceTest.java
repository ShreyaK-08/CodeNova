package com.oj.platform.service;

import com.oj.platform.dto.EditorialDto;
import com.oj.platform.dto.HintDto;
import com.oj.platform.entity.Hint;
import com.oj.platform.entity.Problem;
import com.oj.platform.repository.HintRepository;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HintEditorialServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private HintRepository hintRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    private HintEditorialService hintEditorialService;
    private Problem testProblem;

    @BeforeEach
    void setUp() {
        hintEditorialService = new HintEditorialService(problemRepository, hintRepository, submissionRepository);

        testProblem = new Problem();
        testProblem.setId(10L);
        testProblem.setTitle("Two Sum");
        testProblem.setEditorialTitle("Two Sum Editorial");
        testProblem.setEditorialApproach("Hash map approach");
        testProblem.setEditorialSolution("Code here");
        testProblem.setEditorialUnlockAttempts(3);
    }

    @Test
    void testHintsLockedBeforeFailedSubmission() {
        Hint h1 = new Hint(testProblem, 1, "Think about using a hash map.", 1);
        Hint h2 = new Hint(testProblem, 2, "Store elements in map.", 2);

        when(problemRepository.existsById(10L)).thenReturn(true);
        when(hintRepository.findByProblemIdOrderByHintOrderAsc(10L)).thenReturn(List.of(h1, h2));
        when(submissionRepository.countFailedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(0L);

        List<HintDto> hints = hintEditorialService.getHintsForProblem(10L, 1L, false);

        assertEquals(2, hints.size());
        assertFalse(hints.get(0).isUnlocked());
        assertNull(hints.get(0).getContent(), "Locked hint 1 content must be null");
        assertFalse(hints.get(1).isUnlocked());
        assertNull(hints.get(1).getContent(), "Locked hint 2 content must be null");
    }

    @Test
    void testHint1UnlocksAfterFirstFailedSubmission() {
        Hint h1 = new Hint(testProblem, 1, "Think about using a hash map.", 1);
        Hint h2 = new Hint(testProblem, 2, "Store elements in map.", 2);

        when(problemRepository.existsById(10L)).thenReturn(true);
        when(hintRepository.findByProblemIdOrderByHintOrderAsc(10L)).thenReturn(List.of(h1, h2));
        when(submissionRepository.countFailedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(1L);

        List<HintDto> hints = hintEditorialService.getHintsForProblem(10L, 1L, false);

        assertTrue(hints.get(0).isUnlocked());
        assertEquals("Think about using a hash map.", hints.get(0).getContent());
        assertFalse(hints.get(1).isUnlocked());
        assertNull(hints.get(1).getContent(), "Hint 2 must remain locked after only 1 failed attempt");
    }

    @Test
    void testEditorialLockedBeforeUnlockAttempts() {
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));
        when(submissionRepository.countFailedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(2L);
        when(submissionRepository.countAcceptedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(0L);

        EditorialDto editorial = hintEditorialService.getEditorialForProblem(10L, 1L, false);

        assertFalse(editorial.isUnlocked(), "Editorial must be locked after only 2 failed attempts when 3 required");
        assertNull(editorial.getApproach(), "Locked editorial approach must not be exposed");
        assertNull(editorial.getSolution(), "Locked editorial solution must not be exposed");
    }

    @Test
    void testEditorialUnlockedAfterUnlockAttempts() {
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));
        when(submissionRepository.countFailedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(3L);
        when(submissionRepository.countAcceptedSubmissionsByUserAndProblem(1L, 10L)).thenReturn(0L);

        EditorialDto editorial = hintEditorialService.getEditorialForProblem(10L, 1L, false);

        assertTrue(editorial.isUnlocked(), "Editorial must be unlocked after 3 failed attempts");
        assertEquals("Hash map approach", editorial.getApproach());
        assertEquals("Code here", editorial.getSolution());
    }

    @Test
    void testEditorialUnlockedForAdminEvenWithoutAttempts() {
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));

        EditorialDto editorial = hintEditorialService.getEditorialForProblem(10L, 1L, true);

        assertTrue(editorial.isUnlocked(), "Admin should always have access to editorial");
        assertEquals("Hash map approach", editorial.getApproach());
    }
}
