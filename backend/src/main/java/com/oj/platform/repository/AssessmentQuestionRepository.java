package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion> findByAssessmentIdOrderByOrderIndexAsc(Long assessmentId);

    long countByAssessmentId(Long assessmentId);

    boolean existsByIdAndAssessmentId(Long id, Long assessmentId);
}
