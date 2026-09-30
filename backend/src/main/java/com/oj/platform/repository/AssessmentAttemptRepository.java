package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentAttempt;
import com.oj.platform.entity.AssessmentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {

    /** All attempts by one user on one assessment (multi-attempt support). */
    @Query("SELECT a FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId ORDER BY a.attemptNumber ASC")
    List<AssessmentAttempt> findByAssessmentIdAndUserIdOrdered(
            @Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

    /** Latest (highest attempt number) attempt by user for an assessment. */
    @Query("SELECT a FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId ORDER BY a.attemptNumber DESC")
    List<AssessmentAttempt> findByAssessmentIdAndUserIdOrderByAttemptNumberDesc(
            @Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

    /** In-progress attempt for a user+assessment (at most one should be in this state). */
    @Query("SELECT a FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId AND a.status = 'IN_PROGRESS'")
    Optional<AssessmentAttempt> findInProgressByAssessmentIdAndUserId(
            @Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

    /** Kept for backward compatibility (returns the first match - legacy single-attempt use). */
    @Query("SELECT a FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId")
    Optional<AssessmentAttempt> findByAssessmentIdAndUserId(
            @Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

    @Query("SELECT a FROM AssessmentAttempt a WHERE a.user.id = :userId AND a.assessment.id = :assessmentId")
    Optional<AssessmentAttempt> findByUserIdAndAssessmentId(
            @Param("userId") Long userId, @Param("assessmentId") Long assessmentId);

    Optional<AssessmentAttempt> findByUserIdAndAssessmentIdAndStatus(Long userId, Long assessmentId, AssessmentAttemptStatus status);

    List<AssessmentAttempt> findByAssessmentId(Long assessmentId);

    List<AssessmentAttempt> findByUserId(Long userId);

    @Query("SELECT a FROM AssessmentAttempt a JOIN FETCH a.assessment WHERE a.user.id = :userId ORDER BY a.startedAt DESC")
    List<AssessmentAttempt> findByUserIdWithAssessment(@Param("userId") Long userId);

    long countByAssessmentId(Long assessmentId);

    /** Count how many completed/expired/in-progress attempts this user already has for this assessment. */
    @Query("SELECT COUNT(a) FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId")
    long countByAssessmentIdAndUserId(@Param("assessmentId") Long assessmentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT a.user.id) FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId")
    long countDistinctUsersByAssessmentId(@Param("assessmentId") Long assessmentId);

    List<AssessmentAttempt> findByAssessmentIdOrderByStartedAtDesc(Long assessmentId);

    /** Max attempt number for a user+assessment (0 if none). */
    @Query("SELECT COALESCE(MAX(a.attemptNumber), 0) FROM AssessmentAttempt a WHERE a.assessment.id = :assessmentId AND a.user.id = :userId")
    int findMaxAttemptNumberByAssessmentIdAndUserId(
            @Param("assessmentId") Long assessmentId, @Param("userId") Long userId);
}
