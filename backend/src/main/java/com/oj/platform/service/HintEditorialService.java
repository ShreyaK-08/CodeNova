package com.oj.platform.service;

import com.oj.platform.dto.EditorialDto;
import com.oj.platform.dto.HintDto;
import com.oj.platform.entity.Hint;
import com.oj.platform.entity.Problem;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.HintRepository;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class HintEditorialService {

    private final ProblemRepository problemRepository;
    private final HintRepository hintRepository;
    private final SubmissionRepository submissionRepository;

    public HintEditorialService(ProblemRepository problemRepository,
                                HintRepository hintRepository,
                                SubmissionRepository submissionRepository) {
        this.problemRepository = problemRepository;
        this.hintRepository = hintRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public long getFailedAttempts(Long userId, Long problemId) {
        if (userId == null) return 0;
        return submissionRepository.countFailedSubmissionsByUserAndProblem(userId, problemId);
    }

    @Transactional(readOnly = true)
    public boolean hasSolved(Long userId, Long problemId) {
        if (userId == null) return false;
        return submissionRepository.countAcceptedSubmissionsByUserAndProblem(userId, problemId) > 0;
    }

    @Transactional(readOnly = true)
    public List<HintDto> getHintsForProblem(Long problemId, Long userId, boolean isAdmin) {
        if (!problemRepository.existsById(problemId)) {
            throw new ResourceNotFoundException("Problem not found with id: " + problemId);
        }

        List<Hint> hints = hintRepository.findByProblemIdOrderByHintOrderAsc(problemId);
        long failedAttempts = (userId != null) ? getFailedAttempts(userId, problemId) : 0;

        List<HintDto> dtos = new ArrayList<>();
        for (Hint h : hints) {
            int requiredAttempts = h.getUnlockAfterAttempts() != null ? h.getUnlockAfterAttempts() : 1;
            boolean unlocked = isAdmin || (failedAttempts >= requiredAttempts);

            dtos.add(new HintDto(
                    h.getId(),
                    problemId,
                    h.getHintOrder(),
                    unlocked ? h.getContent() : null, // Locked content is null - never sent over the wire!
                    requiredAttempts,
                    unlocked
            ));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public EditorialDto getEditorialForProblem(Long problemId, Long userId, boolean isAdmin) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        int req = problem.getEditorialUnlockAttempts() != null ? problem.getEditorialUnlockAttempts() : 3;
        long failedAttempts = (userId != null) ? getFailedAttempts(userId, problemId) : 0;
        boolean solved = (userId != null) && hasSolved(userId, problemId);

        boolean hasEditorial = problem.getEditorialSolution() != null && !problem.getEditorialSolution().isBlank()
                || (problem.getEditorialApproach() != null && !problem.getEditorialApproach().isBlank());

        boolean unlocked = hasEditorial && (isAdmin || solved || (failedAttempts >= req));

        if (unlocked) {
            return EditorialDto.unlocked(
                    problem.getEditorialTitle(),
                    problem.getEditorialApproach(),
                    problem.getEditorialAlgorithm(),
                    problem.getEditorialComplexity(),
                    problem.getEditorialSolution(),
                    req,
                    failedAttempts
            );
        } else {
            // Security: Omit all content fields when locked! hasEditorial is safe to
            // reveal (it's already shown in Admin/Problem listings) - it just tells the
            // student whether there's anything to unlock, without leaking the content.
            return EditorialDto.locked(hasEditorial, req, failedAttempts);
        }
    }

    @Transactional
    public HintDto createHint(Long problemId, HintDto dto) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));

        int order = dto.getHintOrder() != null ? dto.getHintOrder()
                : (hintRepository.findByProblemIdOrderByHintOrderAsc(problemId).size() + 1);

        int unlockReq = dto.getUnlockAfterAttempts() != null ? dto.getUnlockAfterAttempts() : order;

        Hint hint = new Hint(problem, order, dto.getContent(), unlockReq);
        Hint saved = hintRepository.save(hint);

        return new HintDto(saved.getId(), problemId, saved.getHintOrder(), saved.getContent(), saved.getUnlockAfterAttempts(), true);
    }

    @Transactional
    public HintDto updateHint(Long hintId, HintDto dto) {
        Hint hint = hintRepository.findById(hintId)
                .orElseThrow(() -> new ResourceNotFoundException("Hint not found with id: " + hintId));

        if (dto.getHintOrder() != null) hint.setHintOrder(dto.getHintOrder());
        if (dto.getContent() != null) hint.setContent(dto.getContent());
        if (dto.getUnlockAfterAttempts() != null) hint.setUnlockAfterAttempts(dto.getUnlockAfterAttempts());

        Hint saved = hintRepository.save(hint);
        return new HintDto(saved.getId(), saved.getProblem().getId(), saved.getHintOrder(), saved.getContent(), saved.getUnlockAfterAttempts(), true);
    }

    @Transactional
    public void deleteHint(Long hintId) {
        if (!hintRepository.existsById(hintId)) {
            throw new ResourceNotFoundException("Hint not found with id: " + hintId);
        }
        hintRepository.deleteById(hintId);
    }
}
