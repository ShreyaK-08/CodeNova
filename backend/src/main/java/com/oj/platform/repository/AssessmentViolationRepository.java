package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentViolationRepository extends JpaRepository<AssessmentViolation, Long> {

    List<AssessmentViolation> findByAttemptIdOrderByTimestampAsc(Long attemptId);

    long countByAttemptId(Long attemptId);
}
