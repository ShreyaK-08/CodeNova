package com.oj.platform.repository;

import com.oj.platform.entity.CandidateDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateDetailsRepository extends JpaRepository<CandidateDetails, Long> {

    Optional<CandidateDetails> findByAttemptId(Long attemptId);
}
