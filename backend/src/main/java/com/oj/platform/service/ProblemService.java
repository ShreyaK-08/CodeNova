package com.oj.platform.service;

import com.oj.platform.dto.ProblemDto;
import com.oj.platform.dto.TestCaseDto;
import com.oj.platform.entity.Problem;
import com.oj.platform.entity.TestCase;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SubmissionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;

    public ProblemService(ProblemRepository problemRepository,
                          SubmissionRepository submissionRepository) {
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Returns all problems enriched with:
     * - totalSubmissions, acceptedSubmissions, acceptanceRate  (global, computed once in batch)
     * - isSolved (per-user, only when authenticated — uses a single query to fetch accepted problem IDs)
     */
    @Transactional(readOnly = true)
    public List<ProblemDto> getAllProblems() {
        List<Problem> problems = problemRepository.findAll();

        // ── Batch-load submission stats to avoid N+1 ────────────────────────
        // Query 1: total submissions grouped by problem_id
        // Query 2: accepted submissions grouped by problem_id
        // We use native Spring Data derived queries per problem but aggregate on the Java side
        // (A proper JPQL GROUP BY would need a custom @Query; for simplicity we use two list fetches)
        Map<Long, Long> totalMap = new HashMap<>();
        Map<Long, Long> acceptedMap = new HashMap<>();

        // Fetch all submissions in two aggregate queries using JPQL
        // These are declared as @Query in SubmissionRepository
        List<Object[]> totalCounts = submissionRepository.countSubmissionsGroupedByProblem();
        for (Object[] row : totalCounts) {
            totalMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        List<Object[]> acceptedCounts = submissionRepository.countAcceptedSubmissionsGroupedByProblem();
        for (Object[] row : acceptedCounts) {
            acceptedMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        // ── isSolved: fetch accepted problem IDs for the current user ────────
        Set<Long> solvedProblemIds = new HashSet<>();
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            solvedProblemIds.addAll(submissionRepository.findAcceptedProblemIdsByUser(currentUserId));
        }

        // ── Map to DTOs ───────────────────────────────────────────────────────
        return problems.stream().map(p -> {
            ProblemDto dto = mapToDto(p);
            long total = totalMap.getOrDefault(p.getId(), 0L);
            long accepted = acceptedMap.getOrDefault(p.getId(), 0L);
            dto.setTotalSubmissions(total);
            dto.setAcceptedSubmissions(accepted);
            dto.setAcceptanceRate(total > 0 ? Math.round(((double) accepted / total) * 1000.0) / 10.0 : null);
            dto.setIsSolved(currentUserId != null ? solvedProblemIds.contains(p.getId()) : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProblemDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));
        return mapToDto(problem);
    }

    @Transactional
    public ProblemDto createProblem(ProblemDto dto) {
        Problem problem = new Problem();
        problem.setTitle(dto.getTitle());
        problem.setDescription(dto.getDescription());
        problem.setDifficulty(dto.getDifficulty());
        problem.setTopic(dto.getTopic());
        problem.setConstraints(dto.getConstraints());
        problem.setStarterCode(dto.getStarterCode());
        problem.setStarterCodePython(dto.getStarterCodePython());
        problem.setStarterCodeCpp(dto.getStarterCodeCpp());
        problem.setStarterCodeJs(dto.getStarterCodeJs());
        problem.setMethodName(dto.getMethodName());
        if (dto.getTimeLimitMs() != null) problem.setTimeLimitMs(dto.getTimeLimitMs());
        if (dto.getMemoryLimitMb() != null) problem.setMemoryLimitMb(dto.getMemoryLimitMb());

        problem.setEditorialTitle(dto.getEditorialTitle());
        problem.setEditorialApproach(dto.getEditorialApproach());
        problem.setEditorialAlgorithm(dto.getEditorialAlgorithm());
        problem.setEditorialComplexity(dto.getEditorialComplexity());
        problem.setEditorialSolution(dto.getEditorialSolution());
        if (dto.getEditorialUnlockAttempts() != null) {
            problem.setEditorialUnlockAttempts(dto.getEditorialUnlockAttempts());
        }

        Problem savedProblem = problemRepository.save(problem);
        return mapToDto(savedProblem);
    }

    @Transactional
    public ProblemDto updateProblem(Long id, ProblemDto dto) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + id));

        problem.setTitle(dto.getTitle());
        problem.setDescription(dto.getDescription());
        problem.setDifficulty(dto.getDifficulty());
        problem.setTopic(dto.getTopic());
        problem.setConstraints(dto.getConstraints());
        problem.setStarterCode(dto.getStarterCode());
        problem.setStarterCodePython(dto.getStarterCodePython());
        problem.setStarterCodeCpp(dto.getStarterCodeCpp());
        problem.setStarterCodeJs(dto.getStarterCodeJs());
        problem.setMethodName(dto.getMethodName());
        if (dto.getTimeLimitMs() != null) problem.setTimeLimitMs(dto.getTimeLimitMs());
        if (dto.getMemoryLimitMb() != null) problem.setMemoryLimitMb(dto.getMemoryLimitMb());

        problem.setEditorialTitle(dto.getEditorialTitle());
        problem.setEditorialApproach(dto.getEditorialApproach());
        problem.setEditorialAlgorithm(dto.getEditorialAlgorithm());
        problem.setEditorialComplexity(dto.getEditorialComplexity());
        problem.setEditorialSolution(dto.getEditorialSolution());
        if (dto.getEditorialUnlockAttempts() != null) {
            problem.setEditorialUnlockAttempts(dto.getEditorialUnlockAttempts());
        }

        Problem updated = problemRepository.save(problem);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    private ProblemDto mapToDto(Problem problem) {
        ProblemDto dto = new ProblemDto();
        dto.setId(problem.getId());
        dto.setTitle(problem.getTitle());
        dto.setDescription(problem.getDescription());
        dto.setDifficulty(problem.getDifficulty());
        dto.setTopic(problem.getTopic());
        dto.setConstraints(problem.getConstraints());
        dto.setStarterCode(problem.getStarterCode());
        dto.setStarterCodePython(problem.getStarterCodePython());
        dto.setStarterCodeCpp(problem.getStarterCodeCpp());
        dto.setStarterCodeJs(problem.getStarterCodeJs());
        dto.setMethodName(problem.getMethodName());
        dto.setTimeLimitMs(problem.getTimeLimitMs());
        dto.setMemoryLimitMb(problem.getMemoryLimitMb());
        dto.setEditorialUnlockAttempts(problem.getEditorialUnlockAttempts() != null ? problem.getEditorialUnlockAttempts() : 3);
        dto.setHasEditorial(problem.getEditorialSolution() != null && !problem.getEditorialSolution().isBlank()
                || (problem.getEditorialApproach() != null && !problem.getEditorialApproach().isBlank()));
        dto.setCreatedAt(problem.getCreatedAt());
        dto.setUpdatedAt(problem.getUpdatedAt());

        boolean isAdmin = isCurrentUserAdmin();
        if (isAdmin) {
            dto.setEditorialTitle(problem.getEditorialTitle());
            dto.setEditorialApproach(problem.getEditorialApproach());
            dto.setEditorialAlgorithm(problem.getEditorialAlgorithm());
            dto.setEditorialComplexity(problem.getEditorialComplexity());
            dto.setEditorialSolution(problem.getEditorialSolution());

            // Admin management views (e.g. Admin > Hints & Editorials) need the
            // actual hint content and counts, not the locked/redacted student view.
            if (problem.getHints() != null) {
                List<com.oj.platform.dto.HintDto> hintDtos = new ArrayList<>();
                for (com.oj.platform.entity.Hint h : problem.getHints()) {
                    hintDtos.add(new com.oj.platform.dto.HintDto(
                            h.getId(), problem.getId(), h.getHintOrder(), h.getContent(),
                            h.getUnlockAfterAttempts(), true));
                }
                dto.setHints(hintDtos);
            }
        }

        if (problem.getTestCases() != null) {
            List<TestCaseDto> tcDtos = new ArrayList<>();
            for (int i = 0; i < problem.getTestCases().size(); i++) {
                TestCase tc = problem.getTestCases().get(i);
                boolean hidden = tc.getIsHidden() != null && tc.getIsHidden();
                TestCaseDto tcDto = new TestCaseDto();
                tcDto.setId(tc.getId());
                tcDto.setProblemId(problem.getId());
                tcDto.setIsHidden(hidden);
                tcDto.setTimeLimitMs(tc.getTimeLimitMs());
                tcDto.setMemoryLimitMb(tc.getMemoryLimitMb());
                tcDto.setTestCaseNumber(i + 1);
                tcDto.setCreatedAt(tc.getCreatedAt());

                // Security: never leak hidden testcase inputs or expected outputs to normal users!
                if (!hidden || isAdmin) {
                    tcDto.setInput(tc.getInput());
                    tcDto.setExpectedOutput(tc.getExpectedOutput());
                } else {
                    tcDto.setInput(null);
                    tcDto.setExpectedOutput(null);
                }
                tcDtos.add(tcDto);
            }
            dto.setTestCases(tcDtos);
        }

        return dto;
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
    }

    /**
     * Returns the current authenticated user's ID from the JWT principal,
     * or null if the request is anonymous.
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof com.oj.platform.security.UserPrincipal up) {
            return up.getId();
        }
        return null;
    }
}
