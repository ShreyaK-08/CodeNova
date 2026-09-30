package com.oj.platform.repository;

import com.oj.platform.entity.Contest;
import com.oj.platform.entity.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    List<Contest> findByStatusIn(List<ContestStatus> statuses);

    boolean existsByTitle(String title);

    java.util.Optional<Contest> findByTitle(String title);

    List<Contest> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);
}
