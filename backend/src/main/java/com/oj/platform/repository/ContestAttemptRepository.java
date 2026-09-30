package com.oj.platform.repository;

import com.oj.platform.entity.ContestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Wired into real contest-participation logic in Task 8 (getOrCreateAttempt, scoring,
 *  leaderboard, admin results) - the entity/schema were prepared by an earlier task. */
@Repository
public interface ContestAttemptRepository extends JpaRepository<ContestAttempt, Long> {

    Optional<ContestAttempt> findByParticipantIdAndContestId(Long participantId, Long contestId);

    List<ContestAttempt> findByContestId(Long contestId);

    boolean existsByParticipantIdAndContestId(Long participantId, Long contestId);

    List<ContestAttempt> findByParticipantId(Long participantId);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM ContestAttempt c JOIN FETCH c.contest WHERE c.participant.id = :participantId ORDER BY c.startedAt DESC")
    List<ContestAttempt> findByParticipantIdWithContest(@org.springframework.data.repository.query.Param("participantId") Long participantId);
}
