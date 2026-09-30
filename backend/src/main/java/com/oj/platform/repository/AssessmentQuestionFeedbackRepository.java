package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentQuestionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentQuestionFeedbackRepository extends JpaRepository<AssessmentQuestionFeedback, Long> {
    List<AssessmentQuestionFeedback> findAllByOrderByCreatedAtDesc();
    List<AssessmentQuestionFeedback> findByAssessmentIdOrderByCreatedAtDesc(Long assessmentId);
    List<AssessmentQuestionFeedback> findByUserIdOrderByCreatedAtDesc(Long userId);
}
