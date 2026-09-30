package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentProgrammingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentProgrammingSubmissionRepository extends JpaRepository<AssessmentProgrammingSubmission, Long> {

    List<AssessmentProgrammingSubmission> findByAttemptIdOrderBySubmittedAtDesc(Long attemptId);

    /** Latest FULL submission per question for a given attempt (for scoring). */
    @Query("SELECT s FROM AssessmentProgrammingSubmission s " +
           "WHERE s.attempt.id = :attemptId AND s.question.id = :questionId " +
           "AND s.submissionType = com.oj.platform.entity.AssessmentProgrammingSubmission$SubmissionType.FULL " +
           "ORDER BY s.submittedAt DESC")
    List<AssessmentProgrammingSubmission> findFullSubmissionsByAttemptAndQuestion(
            @Param("attemptId") Long attemptId, @Param("questionId") Long questionId);

    long countByAttemptId(Long attemptId);

    @Query("SELECT SUM(s.programmingScore) FROM AssessmentProgrammingSubmission s " +
           "WHERE s.attempt.id = :attemptId AND s.submissionType = " +
           "com.oj.platform.entity.AssessmentProgrammingSubmission$SubmissionType.FULL")
    Double sumProgrammingScoreByAttemptId(@Param("attemptId") Long attemptId);
}
