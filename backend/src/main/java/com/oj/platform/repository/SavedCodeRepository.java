package com.oj.platform.repository;

import com.oj.platform.entity.SavedCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedCodeRepository extends JpaRepository<SavedCode, Long> {

    Optional<SavedCode> findByUserIdAndProblemIdAndLanguageIgnoreCase(Long userId, Long problemId, String language);
}
