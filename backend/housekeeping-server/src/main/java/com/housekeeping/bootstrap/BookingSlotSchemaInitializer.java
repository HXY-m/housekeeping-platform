package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(16)
public class BookingSlotSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public BookingSlotSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Long tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'booking_order'
                """, Long.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE booking_order MODIFY COLUMN booking_slot VARCHAR(255) NOT NULL");
    }
}
