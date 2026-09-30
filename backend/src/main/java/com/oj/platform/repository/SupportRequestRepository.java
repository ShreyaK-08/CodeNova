package com.oj.platform.repository;

import com.oj.platform.entity.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SupportRequest> findAllByOrderByCreatedAtDesc();

    Optional<SupportRequest> findByTicketNumber(String ticketNumber);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    long countByStatus(String status);

    long countByPriority(String priority);

    long countByAssignedToId(Long assignedToId);

    @Query("SELECT r FROM SupportRequest r WHERE r.user.id = :userId " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:search IS NULL OR LOWER(r.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY r.createdAt DESC")
    List<SupportRequest> searchUserRequests(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("category") String category,
            @Param("search") String search);

    @Query("SELECT r FROM SupportRequest r WHERE " +
            "(:status IS NULL OR r.status = :status) " +
            "AND (:priority IS NULL OR r.priority = :priority) " +
            "AND (:category IS NULL OR r.category = :category) " +
            "AND (:assignedToId IS NULL OR (r.assignedTo IS NOT NULL AND r.assignedTo.id = :assignedToId)) " +
            "AND (:search IS NULL OR LOWER(r.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.category) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.user.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(r.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY r.createdAt DESC")
    List<SupportRequest> searchAdminRequests(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("category") String category,
            @Param("assignedToId") Long assignedToId,
            @Param("search") String search);

    @Query("SELECT r.category, COUNT(r) FROM SupportRequest r GROUP BY r.category")
    List<Object[]> countByCategory();

    @Query("SELECT r.priority, COUNT(r) FROM SupportRequest r GROUP BY r.priority")
    List<Object[]> countByPriorityGroup();

    @Query("SELECT r.status, COUNT(r) FROM SupportRequest r GROUP BY r.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT r.rating, COUNT(r) FROM SupportRequest r WHERE r.rating IS NOT NULL GROUP BY r.rating")
    List<Object[]> countByRatingGroup();

    @Query("SELECT AVG(r.rating) FROM SupportRequest r WHERE r.rating IS NOT NULL")
    Double calculateAverageRating();

    @Query("SELECT COUNT(r) FROM SupportRequest r WHERE r.rating IS NOT NULL")
    long countRatedTickets();
}
