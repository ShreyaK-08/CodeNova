package com.oj.platform.repository;

import com.oj.platform.entity.Difficulty;
import com.oj.platform.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(Difficulty difficulty);
    long countByDifficulty(Difficulty difficulty);
    Optional<Problem> findByTitle(String title);
}
