package com.oj.platform.repository;

import com.oj.platform.entity.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, Long> {

    List<ContestProblem> findByContestIdOrderByDisplayOrderAsc(Long contestId);

    void deleteByContestId(Long contestId);

    boolean existsByContestIdAndProblemId(Long contestId, Long problemId);
}
