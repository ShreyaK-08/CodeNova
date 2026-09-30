package com.oj.platform.repository;

import com.oj.platform.entity.Assessment;
import com.oj.platform.entity.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByStatus(AssessmentStatus status);

    boolean existsByTitle(String title);

    /** Backs the "Host Assessment" self-service authoring flow (verified non-admin
     *  users) - each host only ever sees/manages assessments they themselves created. */
    List<Assessment> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
