package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {

    List<AssessmentOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    long countByQuestionIdAndIsCorrectTrue(Long questionId);

    boolean existsByIdAndQuestionId(Long id, Long questionId);
}
