package com.oj.platform.repository;

import com.oj.platform.entity.ContestAnnouncementNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestAnnouncementNotificationRepository extends JpaRepository<ContestAnnouncementNotification, Long> {

    boolean existsByContestIdAndRecipientEmailIgnoreCase(Long contestId, String recipientEmail);

    boolean existsByContestIdAndRecipientUserId(Long contestId, Long recipientUserId);

    long countByContestIdAndStatus(Long contestId, String status);

    List<ContestAnnouncementNotification> findByContestId(Long contestId);
}
