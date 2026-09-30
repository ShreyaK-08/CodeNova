package com.oj.platform.repository;

import com.oj.platform.entity.ProgrammingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgrammingQuestionRepository extends JpaRepository<ProgrammingQuestion, Long> {
}
