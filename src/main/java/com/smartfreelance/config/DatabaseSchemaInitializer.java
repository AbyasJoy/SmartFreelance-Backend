package com.smartfreelance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

/**
 * Ensures required database schema and join tables (such as project_skills)
 * exist across all deployment environments (including production Aiven MySQL and Render).
 * This provides a safe, repeatable, and idempotent schema guarantee that prevents
 * SQL Error 1146 (Table 'defaultdb.project_skills' doesn't exist) when Hibernate
 * DDL update fails or omits join table creation.
 */
@Component
@Order(1)
public class DatabaseSchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    private final JdbcOperations jdbcTemplate;

    public DatabaseSchemaInitializer(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureProjectSkillsTable();
    }

    private void ensureProjectSkillsTable() {
        log.info("Verifying database schema: checking 'project_skills' join table...");
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS project_skills (
                    project_id BIGINT NOT NULL,
                    skill_id BIGINT NOT NULL,
                    PRIMARY KEY (project_id, skill_id),
                    KEY idx_project_skills_project (project_id),
                    KEY idx_project_skills_skill (skill_id),
                    CONSTRAINT fk_project_skills_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
                    CONSTRAINT fk_project_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
            log.info("Verified 'project_skills' table schema successfully.");
        } catch (Exception ex) {
            log.warn("Standard CREATE TABLE IF NOT EXISTS project_skills with FK constraints returned: {}. Attempting dialect fallback...", ex.getMessage());
            try {
                jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS project_skills (
                        project_id BIGINT NOT NULL,
                        skill_id BIGINT NOT NULL,
                        PRIMARY KEY (project_id, skill_id)
                    )
                """);
                log.info("Fallback creation of 'project_skills' succeeded.");
            } catch (Exception fallbackEx) {
                log.error("Could not initialize 'project_skills' table: {}", fallbackEx.getMessage(), fallbackEx);
            }
        }
    }
}
