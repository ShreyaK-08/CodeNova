package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentFeedbackRepository extends JpaRepository<AssessmentFeedback, Long> {

    List<AssessmentFeedback> findByAssessmentIdOrderByCreatedAtDesc(Long assessmentId);

    List<AssessmentFeedback> findByHostIdOrderByCreatedAtDesc(Long hostId);

    List<AssessmentFeedback> findByAssessmentCreatedByIdOrderByCreatedAtDesc(Long hostId);

    Optional<AssessmentFeedback> findByAttemptId(Long attemptId);

    boolean existsByAttemptId(Long attemptId);

    List<AssessmentFeedback> findAllByOrderByCreatedAtDesc();

    @Query("SELECT AVG(f.rating) FROM AssessmentFeedback f WHERE f.assessment.id = :assessmentId")
    Double getAverageRatingForAssessment(@Param("assessmentId") Long assessmentId);

    @Query("SELECT AVG(f.rating) FROM AssessmentFeedback f WHERE f.host.id = :hostId OR f.assessment.createdBy.id = :hostId")
    Double getAverageRatingForHost(@Param("hostId") Long hostId);
}
