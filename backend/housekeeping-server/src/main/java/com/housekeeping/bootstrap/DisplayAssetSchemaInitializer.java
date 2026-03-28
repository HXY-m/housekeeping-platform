package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(12)
public class DisplayAssetSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DisplayAssetSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureColumn("service_category", "image_url", "ALTER TABLE service_category ADD COLUMN image_url VARCHAR(500) NOT NULL DEFAULT ''");
        ensureColumn("worker_profile", "avatar_url", "ALTER TABLE worker_profile ADD COLUMN avatar_url VARCHAR(500) NOT NULL DEFAULT ''");
    }

    private void ensureColumn(String tableName, String columnName, String alterSql) {
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
            jdbcTemplate.execute(alterSql);
        }
    }
}
