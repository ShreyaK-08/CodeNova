package com.oj.platform.repository;

import com.oj.platform.entity.Hint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HintRepository extends JpaRepository<Hint, Long> {
    List<Hint> findByProblemIdOrderByHintOrderAsc(Long problemId);
    void deleteByProblemId(Long problemId);
}
