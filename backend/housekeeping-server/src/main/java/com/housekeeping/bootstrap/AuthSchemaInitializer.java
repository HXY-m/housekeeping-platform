package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
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
                  password VARCHAR(255) NOT NULL,
                  real_name VARCHAR(50) NOT NULL,
                  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                )
                """);

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
    }
}
