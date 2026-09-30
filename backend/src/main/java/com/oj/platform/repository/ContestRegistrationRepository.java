package com.oj.platform.repository;

import com.oj.platform.entity.ContestRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRegistrationRepository extends JpaRepository<ContestRegistration, Long> {

    boolean existsByUserIdAndContestId(Long userId, Long contestId);

    Optional<ContestRegistration> findByUserIdAndContestId(Long userId, Long contestId);

    List<ContestRegistration> findByContestId(Long contestId);

    boolean existsByContestId(Long contestId);
}
