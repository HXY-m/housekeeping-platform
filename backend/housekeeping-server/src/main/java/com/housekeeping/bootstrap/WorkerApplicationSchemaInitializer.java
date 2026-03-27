package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class WorkerApplicationSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public WorkerApplicationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureWorkerUserIdColumn();
        ensureWorkerQualificationStatusColumn();

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS worker_application (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    real_name VARCHAR(50) NOT NULL,
                    phone VARCHAR(20) NOT NULL,
                    service_types VARCHAR(255) NOT NULL,
                    years_of_experience INT NOT NULL DEFAULT 0,
                    certificates VARCHAR(500) NOT NULL,
                    service_areas VARCHAR(255) NOT NULL,
                    available_schedule VARCHAR(255) NOT NULL,
                    intro VARCHAR(500) NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    admin_remark VARCHAR(255) DEFAULT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    CONSTRAINT fk_worker_application_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
                )
                """);
    }

    private void ensureWorkerUserIdColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'worker_profile'
                  AND COLUMN_NAME = 'user_id'
                """,
                Integer.class
        );

        if (count != null && count == 0) {
            jdbcTemplate.execute("""
                    ALTER TABLE worker_profile
                    ADD COLUMN user_id BIGINT NULL
                    """);
        }
    }

    private void ensureWorkerQualificationStatusColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'worker_profile'
                  AND COLUMN_NAME = 'qualification_status'
                """,
                Integer.class
        );

        if (count != null && count == 0) {
            jdbcTemplate.execute("""
                    ALTER TABLE worker_profile
                    ADD COLUMN qualification_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED'
                    """);
        }

        jdbcTemplate.update("""
                UPDATE worker_profile
                SET qualification_status = 'APPROVED'
                WHERE qualification_status IS NULL OR qualification_status = ''
                """);
    }
}
