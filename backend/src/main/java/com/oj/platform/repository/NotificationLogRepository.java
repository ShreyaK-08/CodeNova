package com.oj.platform.repository;

import com.oj.platform.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByUserIdAndNotificationTypeAndRelatedEntityIdAndStatus(
            Long userId, String notificationType, String relatedEntityId, String status);

    boolean existsByRecipientEmailAndNotificationTypeAndRelatedEntityIdAndStatus(
            String recipientEmail, String notificationType, String relatedEntityId, String status);

    List<NotificationLog> findTop100ByOrderByCreatedAtDesc();

    Page<NotificationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    long countByNotificationType(String notificationType);
}
