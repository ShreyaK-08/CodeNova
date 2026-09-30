package com.oj.platform.service;

import com.oj.platform.dto.SavedCodeDto;
import com.oj.platform.entity.Problem;
import com.oj.platform.entity.SavedCode;
import com.oj.platform.entity.User;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SavedCodeRepository;
import com.oj.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persists a user's last-edited code per (problem, language), independent of and never
 * affecting Submission history. Supports JAVA/PYTHON/CPP/JAVASCRIPT identically - language
 * is stored/matched as plain text (case-insensitive), the same convention Submission
 * already uses, so no enum/whitelist changes were needed to support all four.
 */
@Service
public class SavedCodeService {

    private final SavedCodeRepository savedCodeRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    public SavedCodeService(SavedCodeRepository savedCodeRepository,
                             UserRepository userRepository,
                             ProblemRepository problemRepository) {
        this.savedCodeRepository = savedCodeRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public Optional<SavedCodeDto> getSavedCode(Long userId, Long problemId, String language) {
        return savedCodeRepository.findByUserIdAndProblemIdAndLanguageIgnoreCase(userId, problemId, language)
                .map(this::toDto);
    }

    /**
     * Creates the saved-code row if none exists yet for this (user, problem, language),
     * otherwise overwrites its code and updatedAt. Never touches Submission data.
     */
    @Transactional
    public SavedCodeDto saveOrUpdate(Long userId, Long problemId, String language, String code) {
        Optional<SavedCode> existing = savedCodeRepository
                .findByUserIdAndProblemIdAndLanguageIgnoreCase(userId, problemId, language);

        SavedCode savedCode;
        if (existing.isPresent()) {
            savedCode = existing.get();
            savedCode.setCode(code);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            Problem problem = problemRepository.findById(problemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + problemId));
            savedCode = new SavedCode(user, problem, language, code);
        }

        SavedCode saved = savedCodeRepository.save(savedCode);
        return toDto(saved);
    }

    private SavedCodeDto toDto(SavedCode entity) {
        return new SavedCodeDto(entity.getProblem().getId(), entity.getLanguage(), entity.getCode(), entity.getUpdatedAt());
    }
}
