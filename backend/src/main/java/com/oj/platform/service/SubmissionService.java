package com.oj.platform.service;

import com.oj.platform.dto.RunResponse;
import com.oj.platform.dto.SubmissionRequest;
import com.oj.platform.dto.SubmissionResponse;
import com.oj.platform.dto.TestCaseResultDto;
import com.oj.platform.entity.*;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.*;
import com.oj.platform.util.OutputComparisonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionResultRepository submissionResultRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeExecutionService codeExecutionService;
    private final CertificateService certificateService;
    private final SavedCodeService savedCodeService;
    private final ContestService contestService;

    public SubmissionService(SubmissionRepository submissionRepository,
                             SubmissionResultRepository submissionResultRepository,
                             ProblemRepository problemRepository,
                             UserRepository userRepository,
                             TestCaseRepository testCaseRepository,
                             CodeExecutionService codeExecutionService,
                             CertificateService certificateService,
                             SavedCodeService savedCodeService,
                             ContestService contestService) {
        this.submissionRepository = submissionRepository;
        this.submissionResultRepository = submissionResultRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.testCaseRepository = testCaseRepository;
        this.codeExecutionService = codeExecutionService;
        this.certificateService = certificateService;
        this.savedCodeService = savedCodeService;
        this.contestService = contestService;
    }

    /** Internal result of compiling + running code against a set of test cases. */
    private static class ExecutionOutcome {
        SubmissionStatus overallStatus;
        String overallError;
        long maxExecutionTime;
        List<TestCaseResultDto> results = new ArrayList<>();
        int passed;
        int failed;
    }

    @Transactional
    public SubmissionResponse submitSolution(Long userId, SubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        // Task 8: when this submission is scoped to a contest, verify registration and
        // the server's own clock against the contest's real start/end time BEFORE doing
        // any execution work - never trusts the frontend timer. Left null (the default),
        // this is skipped entirely and behavior is identical to before Task 8.
        Contest contest = null;
        if (request.getContestId() != null) {
            contest = contestService.validateContestSubmissionAllowed(request.getContestId(), request.getProblemId(), userId);
        }

        // Submit runs against ALL test cases (visible + hidden) - this is the official grading run.
        List<TestCase> testCases = testCaseRepository.findByProblemId(problem.getId());

        // 1. Persist initial submission state as PENDING
        Submission submission = new Submission(user, problem, request.getLanguage(), request.getCode());
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setTotalTestCases(testCases.size());
        submission.setPassedTestCases(0);
        submission.setFailedTestCases(0);
        submission.setContest(contest);
        Submission saved = submissionRepository.save(submission);

        // Persist/update the user's "last saved code" for this problem+language. This is
        // separate from submission history - it never replaces or deletes any Submission
        // row, it only tracks what to restore into the editor next time.
        savedCodeService.saveOrUpdate(userId, problem.getId(), request.getLanguage(), request.getCode());

        // 2. Transition lifecycle state to RUNNING
        saved.setStatus(SubmissionStatus.RUNNING);
        saved = submissionRepository.save(saved);

        // 3. Execute against all configured test cases
        ExecutionOutcome outcome = execute(problem, request.getLanguage(), request.getCode(), testCases);

        // 4. Update submission with final status and results
        saved.setStatus(outcome.overallStatus);
        saved.setPassedTestCases(outcome.passed);
        saved.setFailedTestCases(outcome.failed);
        saved.setTotalTestCases(testCases.size());
        saved.setExecutionTime(outcome.maxExecutionTime);
        saved.setMemoryUsed(null);
        saved = submissionRepository.save(saved);

        // Persist one SubmissionResult row per test case
        for (TestCaseResultDto r : outcome.results) {
            TestCase tc = testCases.stream().filter(t -> t.getId().equals(r.getTestCaseId())).findFirst().orElse(null);
            submissionResultRepository.save(new SubmissionResult(
                    saved, tc, r.getStatus(), r.getActualOutput(), r.getExecutionTimeMs(), r.getMemoryUsed(), r.getErrorMessage()));
        }

        // Part 18: an ACCEPTED submission may have just crossed a new certificate
        // milestone (every 50 distinct solved problems) - recalculate immediately so
        // the certificate is available right away, not only on next app startup.
        if (outcome.overallStatus == SubmissionStatus.ACCEPTED) {
            certificateService.checkAndAwardMilestones(userId);
        }

        // Task 8: keep the participant's ContestAttempt (score/solvedCount/
        // submissionCount) in sync with the real Submission rows for this contest+user.
        // Recomputed from scratch every time - never incremented - so re-submitting an
        // already-solved problem can never double-award its points.
        if (request.getContestId() != null) {
            contestService.recordContestSubmissionOutcome(request.getContestId(), userId, problem.getId(), outcome.overallStatus);
        }

        SubmissionResponse response = mapToDto(saved);
        response.setErrorMessage(outcome.overallError);
        response.setResults(outcome.results);
        return response;
    }

    /**
     * "Run" mode: executes code against only the problem's VISIBLE (non-hidden) test cases.
     * Does NOT persist to database; does not affect acceptance, failed attempts, or leaderboard.
     */
    @Transactional(readOnly = true)
    public RunResponse runCode(SubmissionRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + request.getProblemId()));

        List<TestCase> allTestCases = testCaseRepository.findByProblemId(problem.getId());
        List<TestCase> visibleTestCases = allTestCases.stream()
                .filter(tc -> tc.getIsHidden() == null || !tc.getIsHidden())
                .collect(Collectors.toList());

        ExecutionOutcome outcome = execute(problem, request.getLanguage(), request.getCode(), visibleTestCases);

        RunResponse response = new RunResponse();
        response.setStatus(outcome.overallStatus);
        response.setErrorMessage(outcome.overallError);
        response.setExecutionTime(outcome.maxExecutionTime);
        response.setPassedTestCases(outcome.passed);
        response.setTotalTestCases(visibleTestCases.size());
        response.setFailedTestCases(outcome.failed);
        response.setResults(outcome.results);
        return response;
    }

    /**
     * Shared compile+run+compare logic used by both submitSolution and runCode.
     */
    private ExecutionOutcome execute(Problem problem, String language, String code, List<TestCase> testCases) {
        ExecutionOutcome outcome = new ExecutionOutcome();
        outcome.results = new ArrayList<>();
        int defaultTimeLimit = problem.getTimeLimitMs() != null ? problem.getTimeLimitMs() : 2000;

        boolean javaDriverMode = "JAVA".equalsIgnoreCase(language)
                && problem.getMethodName() != null && !problem.getMethodName().isBlank();

        String compileError = null;
        CodeExecutionService.CompileOutcome compileOutcome = null;

        try {
            if (javaDriverMode) {
                String mainJavaSource = JavaDriverGenerator.generateMainJava(problem.getMethodName(), testCases);
                compileOutcome = codeExecutionService.compileJavaWithDriver(code, mainJavaSource);
            } else {
                compileOutcome = codeExecutionService.compile(language, code);
            }
        } catch (IOException e) {
            compileError = "Could not prepare submission for execution: " + e.getMessage();
        }

        if (compileError == null && !compileOutcome.success) {
            compileError = compileOutcome.errorOutput;
        }

        if (compileError != null) {
            outcome.overallStatus = SubmissionStatus.COMPILATION_ERROR;
            outcome.overallError = truncate(compileError);
            outcome.passed = 0;
            outcome.failed = testCases.size();
            outcome.maxExecutionTime = 0;
            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                outcome.results.add(buildResult(tc, i + 1, SubmissionStatus.COMPILATION_ERROR, null, 0L,
                        truncate(compileError), "COMPILATION_ERROR", null));
            }
            if (compileOutcome != null) {
                codeExecutionService.cleanup(compileOutcome.workDir);
            }
            return outcome;
        }

        int passed = 0;
        long maxExecutionTime = 0;
        SubmissionStatus overallStatus = SubmissionStatus.ACCEPTED;
        String overallError = null;

        try {
            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                int tcTimeLimit = tc.getTimeLimitMs() != null ? tc.getTimeLimitMs() : defaultTimeLimit;

                List<String> command = compileOutcome.runCommand;
                String stdin = tc.getInput();
                if (javaDriverMode) {
                    command = new ArrayList<>(compileOutcome.runCommand);
                    command.add(String.valueOf(i)); // test case index
                    stdin = "";
                }

                CodeExecutionService.RunOutcome run = codeExecutionService.run(
                        compileOutcome.workDir, command, stdin, tcTimeLimit);

                SubmissionStatus tcStatus;
                String actualOutput = run.stdout != null ? run.stdout.trim() : "";
                String errorMessage = null;
                String errorType = null;
                String compDetails = null;

                if (run.toolMissing) {
                    tcStatus = SubmissionStatus.RUNTIME_ERROR;
                    errorMessage = run.stderr;
                    errorType = "TOOL_NOT_FOUND";
                } else if (run.timedOut) {
                    tcStatus = SubmissionStatus.TIME_LIMIT_EXCEEDED;
                    errorMessage = "Execution exceeded the " + tcTimeLimit + "ms time limit";
                    errorType = "TIME_LIMIT_EXCEEDED";
                } else if (run.exitCode != 0) {
                    tcStatus = SubmissionStatus.RUNTIME_ERROR;
                    errorMessage = truncate(firstNonBlank(run.stderr, run.stdout));
                    errorType = "RUNTIME_ERROR";
                } else {
                    OutputComparisonUtil.ComparisonResult comp = OutputComparisonUtil.compare(actualOutput, tc.getExpectedOutput());
                    if (comp.isMatches()) {
                        tcStatus = SubmissionStatus.PASSED;
                        passed++;
                    } else {
                        tcStatus = SubmissionStatus.WRONG_ANSWER;
                        errorType = "WRONG_ANSWER";
                        compDetails = comp.getDiffDetails();
                    }
                }

                maxExecutionTime = Math.max(maxExecutionTime, run.executionTimeMs);

                if (tcStatus != SubmissionStatus.PASSED && overallStatus == SubmissionStatus.ACCEPTED) {
                    overallStatus = (tcStatus == SubmissionStatus.PASSED) ? SubmissionStatus.ACCEPTED : tcStatus;
                    overallError = errorMessage != null ? errorMessage : compDetails;
                }

                outcome.results.add(buildResult(tc, i + 1, tcStatus, truncate(actualOutput),
                        run.executionTimeMs, errorMessage != null ? errorMessage : compDetails, errorType, compDetails));
            }

            // A submission MUST be ACCEPTED only if ALL test cases pass.
            if (testCases.isEmpty() || passed != testCases.size()) {
                if (overallStatus == SubmissionStatus.ACCEPTED) {
                    overallStatus = SubmissionStatus.WRONG_ANSWER;
                }
                if (overallError == null || overallError.isBlank()) {
                    overallError = String.format("%d of %d test cases failed.", (testCases.size() - passed), testCases.size());
                }
            }
        } finally {
            codeExecutionService.cleanup(compileOutcome.workDir);
        }

        outcome.overallStatus = overallStatus;
        outcome.overallError = overallError;
        outcome.passed = passed;
        outcome.failed = testCases.size() - passed;
        outcome.maxExecutionTime = maxExecutionTime;
        return outcome;
    }

    private TestCaseResultDto buildResult(TestCase tc, int testCaseNumber, SubmissionStatus status,
                                          String actualOutput, Long executionTimeMs,
                                          String errorMessage, String errorType, String comparisonResult) {
        TestCaseResultDto dto = new TestCaseResultDto();
        dto.setTestCaseId(tc.getId());
        dto.setTestCaseNumber(testCaseNumber);
        dto.setHidden(tc.getIsHidden() != null && tc.getIsHidden());
        dto.setStatus(status);
        dto.setExecutionTimeMs(executionTimeMs);
        dto.setMemoryUsed(null);
        dto.setErrorType(dto.isHidden() ? null : errorType);
        dto.setComparisonResult(dto.isHidden() ? null : comparisonResult);

        // Security: Never leak hidden test-case input/expected-output/actual-output content to normal frontend.
        dto.setInput(dto.isHidden() ? null : tc.getInput());
        dto.setExpectedOutput(dto.isHidden() ? null : tc.getExpectedOutput());
        dto.setActualOutput(dto.isHidden() ? null : actualOutput);
        dto.setErrorMessage(dto.isHidden() ? null : errorMessage);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAllSubmissions() {
        return submissionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SubmissionResponse mapToDto(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setProblemId(submission.getProblem().getId());
        response.setProblemTitle(submission.getProblem().getTitle());
        response.setUserId(submission.getUser().getId());
        response.setUsername(submission.getUser().getUsername());
        response.setLanguage(submission.getLanguage());
        response.setCode(submission.getCode());
        response.setStatus(submission.getStatus());
        response.setExecutionTime(submission.getExecutionTime());
        response.setMemoryUsed(submission.getMemoryUsed());
        response.setPassedTestCases(submission.getPassedTestCases());
        response.setTotalTestCases(submission.getTotalTestCases());
        response.setFailedTestCases(submission.getFailedTestCases());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setContestId(submission.getContest() != null ? submission.getContest().getId() : null);
        return response;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replace("\r\n", "\n").trim();
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 2000 ? s.substring(0, 2000) + "..." : s;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return "";
    }
}
