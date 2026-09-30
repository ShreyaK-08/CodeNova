package com.oj.platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures legacy schema constraints that prevent multi-attempt assessments
 * (e.g. uq_assessment_attempt_user_assessment) are dropped on startup.
 * Runs with highest precedence before other seeds or services start.
 */
@Component
@Order(1)
public class DatabaseConstraintMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConstraintMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        dropAssessmentAttemptUniqueConstraint();
    }

    private void dropAssessmentAttemptUniqueConstraint() {
        try {
            // Ensure standalone index exists on user_id so foreign key constraint does not block dropping the composite index
            jdbcTemplate.execute("CREATE INDEX idx_attempt_user_id ON assessment_attempts(user_id)");
        } catch (Exception ignored) {
            // index already exists or table not created yet
        }

        try {
            // Ensure standalone index exists on assessment_id
            jdbcTemplate.execute("CREATE INDEX idx_attempt_assessment_id ON assessment_attempts(assessment_id)");
        } catch (Exception ignored) {
            // index already exists or table not created yet
        }

        try {
            // Drop legacy single-attempt unique constraint
            jdbcTemplate.execute("ALTER TABLE assessment_attempts DROP INDEX uq_assessment_attempt_user_assessment");
            log.info("Successfully dropped legacy unique constraint 'uq_assessment_attempt_user_assessment' on assessment_attempts");
        } catch (Exception e) {
            log.debug("Note: uq_assessment_attempt_user_assessment index drop skipped: {}", e.getMessage());
        }
    }
}
