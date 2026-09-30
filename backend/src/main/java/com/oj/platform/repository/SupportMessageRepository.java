package com.oj.platform.repository;

import com.oj.platform.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findBySupportRequestIdOrderByCreatedAtAsc(Long supportRequestId);

    List<SupportMessage> findBySupportRequestIdAndIsInternalNoteFalseOrderByCreatedAtAsc(Long supportRequestId);

    long countBySupportRequestId(Long supportRequestId);

    void deleteBySupportRequestId(Long supportRequestId);
}
