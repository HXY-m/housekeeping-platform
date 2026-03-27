package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FavoriteSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public FavoriteSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS favorite_worker (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    worker_id BIGINT NOT NULL,
                    created_at DATETIME NOT NULL,
                    UNIQUE KEY uk_favorite_worker (user_id, worker_id),
                    KEY idx_favorite_worker_user (user_id),
                    KEY idx_favorite_worker_worker (worker_id)
                )
                """);
    }
}
