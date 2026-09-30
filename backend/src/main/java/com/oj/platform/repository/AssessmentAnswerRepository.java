package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {

    Optional<AssessmentAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    List<AssessmentAnswer> findByAttemptId(Long attemptId);
}
