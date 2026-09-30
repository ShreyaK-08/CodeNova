package com.oj.platform.repository;

import com.oj.platform.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUserIdOrderByMilestoneAsc(Long userId);

    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);

    boolean existsByUserIdAndMilestone(Long userId, Integer milestone);

    boolean existsByUserIdAndTitle(Long userId, String title);

    Optional<Certificate> findByVerificationCode(String verificationCode);

    Optional<Certificate> findByUserIdAndMilestone(Long userId, Integer milestone);

    Optional<Certificate> findByUserIdAndTitle(Long userId, String title);

    long count();

    List<Certificate> findAllByOrderByIssuedAtDesc();
}
