package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Order(2)
public class UserProfileSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserProfileSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    gender VARCHAR(20) NOT NULL DEFAULT '',
                    city VARCHAR(50) NOT NULL DEFAULT '',
                    bio VARCHAR(500) NOT NULL DEFAULT '',
                    avatar_url VARCHAR(255) NOT NULL DEFAULT '',
                    UNIQUE KEY uk_user_profile_user (user_id),
                    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_address (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    contact_name VARCHAR(50) NOT NULL,
                    contact_phone VARCHAR(20) NOT NULL,
                    city VARCHAR(50) NOT NULL,
                    detail_address VARCHAR(255) NOT NULL,
                    address_tag VARCHAR(30) NOT NULL DEFAULT '',
                    default_address TINYINT(1) NOT NULL DEFAULT 0,
                    latitude DOUBLE NULL,
                    longitude DOUBLE NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    KEY idx_user_address_user (user_id),
                    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
                )
                """);

        if (!columnExists("user_address", "latitude")) {
            jdbcTemplate.execute("ALTER TABLE user_address ADD COLUMN latitude DOUBLE NULL");
        }
        if (!columnExists("user_address", "longitude")) {
            jdbcTemplate.execute("ALTER TABLE user_address ADD COLUMN longitude DOUBLE NULL");
        }
    }

    private boolean columnExists(String tableName, String columnName) {
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
        return Objects.requireNonNullElse(count, 0) > 0;
    }
}
