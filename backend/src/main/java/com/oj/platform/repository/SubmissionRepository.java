package com.oj.platform.repository;

import com.oj.platform.entity.Submission;
import com.oj.platform.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<Submission> findByProblemIdOrderBySubmittedAtDesc(Long problemId);
    List<Submission> findTop10ByOrderBySubmittedAtDesc();
    
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, SubmissionStatus status);
    long countByStatus(SubmissionStatus status);

    // ── Per-problem stats (for problem browser acceptance rate) ──────────
    long countByProblemId(Long problemId);
    long countByProblemIdAndStatus(Long problemId, SubmissionStatus status);

    /** Returns the problem IDs where the given user has at least one ACCEPTED submission. */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    List<Long> findAcceptedProblemIdsByUser(@Param("userId") Long userId);

    /** For each problem that has at least one submission, returns [problemId, totalCount]. */
    @Query("SELECT s.problem.id, COUNT(s) FROM Submission s GROUP BY s.problem.id")
    List<Object[]> countSubmissionsGroupedByProblem();

    /** For each problem with at least one ACCEPTED submission, returns [problemId, acceptedCount]. */
    @Query("SELECT s.problem.id, COUNT(s) FROM Submission s WHERE s.status = 'ACCEPTED' GROUP BY s.problem.id")
    List<Object[]> countAcceptedSubmissionsGroupedByProblem();

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    long countDistinctSolvedProblemsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND LOWER(s.language) = LOWER(:language) AND s.status = 'ACCEPTED'")
    long countDistinctSolvedProblemsByUserAndLanguage(@Param("userId") Long userId, @Param("language") String language);

    @Query("SELECT s.language, COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED' GROUP BY s.language")
    List<Object[]> countDistinctSolvedProblemsGroupedByLanguage(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED' AND s.problem.difficulty = :difficulty")
    long countDistinctSolvedProblemsByUserAndDifficulty(@Param("userId") Long userId, @Param("difficulty") com.oj.platform.entity.Difficulty difficulty);

    long countBySubmittedAtAfter(LocalDateTime dateTime);

    @Query("SELECT s.user.id AS userId, COUNT(DISTINCT s.problem.id) AS solvedCount FROM Submission s WHERE s.status = 'ACCEPTED' GROUP BY s.user.id")
    List<Object[]> countSolvedProblemsGroupedByUser();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.problem.id = :problemId AND s.status IN ('WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'MEMORY_LIMIT_EXCEEDED', 'RUNTIME_ERROR', 'COMPILE_ERROR', 'COMPILATION_ERROR')")
    long countFailedSubmissionsByUserAndProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user.id = :userId AND s.problem.id = :problemId AND s.status = 'ACCEPTED'")
    long countAcceptedSubmissionsByUserAndProblem(@Param("userId") Long userId, @Param("problemId") Long problemId);

    List<Submission> findByStatus(SubmissionStatus status);
    List<Submission> findByStatusAndSubmittedAtAfter(SubmissionStatus status, LocalDateTime dateTime);
    List<Submission> findByStatusAndLanguageIgnoreCase(SubmissionStatus status, String language);

    long countByUserIdAndLanguageIgnoreCase(Long userId, String language);
    long countByUserIdAndSubmittedAtAfter(Long userId, LocalDateTime dateTime);

    // ---- Contest scoring (Task 8) ----
    // Submissions made through the contest coding screen carry a nullable contest_id
    // (see Submission.contest) - every normal /problems submission leaves it null and is
    // completely unaffected by the queries below.

    long countByContestIdAndUserId(Long contestId, Long userId);

    @Query("SELECT DISTINCT s.problem.id FROM Submission s " +
            "WHERE s.contest.id = :contestId AND s.user.id = :userId AND s.status = 'ACCEPTED'")
    List<Long> findDistinctAcceptedProblemIdsByContestAndUser(@Param("contestId") Long contestId, @Param("userId") Long userId);

    @Query("SELECT MAX(s.submittedAt) FROM Submission s " +
            "WHERE s.contest.id = :contestId AND s.user.id = :userId AND s.status = 'ACCEPTED'")
    LocalDateTime findLastAcceptedSubmissionTime(@Param("contestId") Long contestId, @Param("userId") Long userId);
}
