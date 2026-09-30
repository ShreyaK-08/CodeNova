package com.oj.platform.repository;

import com.oj.platform.entity.GeneralFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralFeedbackRepository extends JpaRepository<GeneralFeedback, Long> {
    List<GeneralFeedback> findAllByOrderByCreatedAtDesc();
    List<GeneralFeedback> findByUserIdOrderByCreatedAtDesc(Long userId);
}
