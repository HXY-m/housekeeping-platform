package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AuthSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AuthSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  phone VARCHAR(20) NOT NULL UNIQUE,
                  username VARCHAR(50) NULL,
                  password VARCHAR(255) NOT NULL,
                  real_name VARCHAR(50) NOT NULL,
                  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                )
                """);
        ensureColumn("sys_user", "username", "ALTER TABLE sys_user ADD COLUMN username VARCHAR(50) NULL");
        backfillUsernames();
        ensureUniqueIndex("sys_user", "username", "ALTER TABLE sys_user ADD UNIQUE KEY uk_sys_user_username (username)");

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_role (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  role_code VARCHAR(50) NOT NULL UNIQUE,
                  role_name VARCHAR(50) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_user_role (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT NOT NULL,
                  role_id BIGINT NOT NULL,
                  UNIQUE KEY uk_user_role (user_id, role_id)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_permission (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  permission_code VARCHAR(80) NOT NULL UNIQUE,
                  permission_name VARCHAR(120) NOT NULL,
                  permission_group VARCHAR(80) NOT NULL DEFAULT '',
                  description VARCHAR(255) NOT NULL DEFAULT ''
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_role_permission (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  role_id BIGINT NOT NULL,
                  permission_id BIGINT NOT NULL,
                  UNIQUE KEY uk_role_permission (role_id, permission_id)
                )
                """);
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

    private void ensureUniqueIndex(String tableName, String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND NON_UNIQUE = 0
                """,
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void backfillUsernames() {
        jdbcTemplate.execute("""
                UPDATE sys_user
                SET username = CONCAT('user_', id)
                WHERE username IS NULL OR username = ''
                """);
    }
}
