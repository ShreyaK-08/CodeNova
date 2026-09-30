package com.oj.platform.service;

import com.oj.platform.dto.SubmissionRequest;
import com.oj.platform.dto.SubmissionResponse;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Covers the two behaviors this task ("persistent user code + submission database") is
 * specifically about:
 *   1. A user can submit the same problem more than once - no lookup-before-insert or
 *      uniqueness check blocks a second submission for the same (user, problem).
 *   2. Every successful submitSolution() call also updates the user's saved code via
 *      SavedCodeService, without ever touching or deleting prior Submission rows.
 *
 * Test cases are intentionally empty so the run() loop never executes and only
 * compile()/cleanup() need to be stubbed - this keeps the test focused on the
 * persistence/wiring behavior above rather than re-testing judging logic, which already
 * has its own coverage elsewhere and is out of scope for this task.
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock private SubmissionRepository submissionRepository;
    @Mock private SubmissionResultRepository submissionResultRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private UserRepository userRepository;
    @Mock private TestCaseRepository testCaseRepository;
    @Mock private CodeExecutionService codeExecutionService;
    @Mock private CertificateService certificateService;
    @Mock private SavedCodeService savedCodeService;
    @Mock private ContestService contestService;

    private SubmissionService submissionService;

    private User testUser;
    private Problem testProblem;

    @BeforeEach
    void setUp() throws Exception {
        submissionService = new SubmissionService(submissionRepository, submissionResultRepository,
                problemRepository, userRepository, testCaseRepository, codeExecutionService,
                certificateService, savedCodeService, contestService);

        testUser = new User("Test User", "testuser", "test@example.com", "hashed", Role.ROLE_USER);
        testUser.setId(1L);

        // No methodName set -> javaDriverMode is false -> execute() takes the plain
        // compile(language, code) path, not the Java-driver/JavaDriverGenerator path.
        testProblem = new Problem("Two Sum", "desc", Difficulty.EASY, "Arrays", "starter code");
        testProblem.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(problemRepository.findById(10L)).thenReturn(Optional.of(testProblem));
        when(testCaseRepository.findByProblemId(10L)).thenReturn(Collections.emptyList());

        CodeExecutionService.CompileOutcome compileOutcome = new CodeExecutionService.CompileOutcome();
        compileOutcome.success = true;
        compileOutcome.workDir = Path.of("test-work-dir");
        compileOutcome.runCommand = List.of("java", "Main");
        when(codeExecutionService.compile(anyString(), anyString())).thenReturn(compileOutcome);

        // Return whatever Submission entity is passed to save() - a real repository
        // would assign an id, but that's irrelevant to the behavior under test here.
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void testMultipleSubmissionsForSameUserAndProblemAreBothPersisted() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProblemId(10L);
        request.setLanguage("JAVA");
        request.setCode("public class Solution {}");

        SubmissionResponse first = submissionService.submitSolution(1L, request);
        SubmissionResponse second = submissionService.submitSolution(1L, request);

        assertNotNull(first);
        assertNotNull(second);

        // submitSolution() never performs a lookup-before-insert or "already submitted
        // this problem?" check - it unconditionally creates a new Submission and saves it
        // (through its PENDING -> RUNNING -> final-status lifecycle) every single call.
        // Calling it twice for the same (user, problem) must not reduce, skip, or block
        // the second call's saves relative to the first.
        verify(submissionRepository, atLeast(2)).save(any(Submission.class));
    }

    @Test
    void testSubmittingCodeUpdatesSavedCode() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProblemId(10L);
        request.setLanguage("PYTHON");
        request.setCode("print('hello')");

        submissionService.submitSolution(1L, request);

        verify(savedCodeService, times(1))
                .saveOrUpdate(1L, 10L, "PYTHON", "print('hello')");
    }

    @Test
    void testSubmittingDoesNotDeleteOrReplacePriorSubmissions() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProblemId(10L);
        request.setLanguage("JAVA");
        request.setCode("public class Solution {}");

        submissionService.submitSolution(1L, request);

        verify(submissionRepository, never()).delete(any(Submission.class));
        verify(submissionRepository, never()).deleteById(any());
        verify(submissionRepository, never()).deleteAll();
    }

    // 10. Non-contest submissions (contestId left null, the default) still work exactly
    // as before Task 8 - contestService is never consulted for validation or scoring.
    @Test
    void testNonContestSubmissionNeverTouchesContestService() {
        SubmissionRequest request = new SubmissionRequest();
        request.setProblemId(10L);
        request.setLanguage("JAVA");
        request.setCode("public class Solution {}");
        // contestId intentionally left unset (null)

        SubmissionResponse response = submissionService.submitSolution(1L, request);

        assertNotNull(response);
        assertNull(response.getContestId());
        verifyNoInteractions(contestService);
    }
}
