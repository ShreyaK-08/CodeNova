package com.oj.platform.repository;

import com.oj.platform.entity.ProgrammingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgrammingTestCaseRepository extends JpaRepository<ProgrammingTestCase, Long> {

    List<ProgrammingTestCase> findByProgrammingQuestionIdOrderByOrderIndexAsc(Long programmingQuestionId);

    long countByProgrammingQuestionId(Long programmingQuestionId);
}
