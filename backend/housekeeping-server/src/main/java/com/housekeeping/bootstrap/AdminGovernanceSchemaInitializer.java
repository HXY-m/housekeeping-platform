package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AdminGovernanceSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AdminGovernanceSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureServiceCategoryColumns();
        ensureOperationLogTable();
    }

    private void ensureServiceCategoryColumns() {
        ensureColumn("service_category", "service_duration", "ALTER TABLE service_category ADD COLUMN service_duration VARCHAR(100) NOT NULL DEFAULT ''");
        ensureColumn("service_category", "service_area", "ALTER TABLE service_category ADD COLUMN service_area VARCHAR(255) NOT NULL DEFAULT ''");
        ensureColumn("service_category", "service_scene", "ALTER TABLE service_category ADD COLUMN service_scene VARCHAR(255) NOT NULL DEFAULT ''");
        ensureColumn("service_category", "extra_services", "ALTER TABLE service_category ADD COLUMN extra_services VARCHAR(255) NOT NULL DEFAULT ''");
        ensureColumn("service_category", "enabled", "ALTER TABLE service_category ADD COLUMN enabled TINYINT(1) NOT NULL DEFAULT 1");
    }

    private void ensureOperationLogTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS operation_log (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    operator_user_id BIGINT NULL,
                    operator_name VARCHAR(50) NOT NULL DEFAULT '',
                    role_code VARCHAR(50) NOT NULL DEFAULT '',
                    action_type VARCHAR(80) NOT NULL,
                    target_type VARCHAR(80) NOT NULL DEFAULT '',
                    target_id BIGINT NULL,
                    content VARCHAR(500) NOT NULL DEFAULT '',
                    ip_address VARCHAR(64) NOT NULL DEFAULT '',
                    created_at DATETIME NOT NULL,
                    KEY idx_operation_log_created_at (created_at),
                    KEY idx_operation_log_action_type (action_type),
                    KEY idx_operation_log_role_code (role_code)
                )
                """);
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName
        );

        if (count != null && count == 0) {
            jdbcTemplate.execute(ddl);
        }
    }
}
