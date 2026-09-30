package com.oj.platform.repository;

import com.oj.platform.entity.AssessmentHostVerification;
import com.oj.platform.entity.HostVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentHostVerificationRepository extends JpaRepository<AssessmentHostVerification, Long> {

    Optional<AssessmentHostVerification> findByUserId(Long userId);

    boolean existsByUserIdAndStatus(Long userId, HostVerificationStatus status);

    List<AssessmentHostVerification> findAllByOrderByCreatedAtDesc();
}
