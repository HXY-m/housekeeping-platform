package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderWorkflowSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public OrderWorkflowSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureBookingOrderUserIdColumn();
        ensureOrderReviewTable();
    }

    private void ensureBookingOrderUserIdColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'booking_order'
                  AND COLUMN_NAME = 'user_id'
                """,
                Integer.class
        );

        if (count != null && count == 0) {
            jdbcTemplate.execute("""
                    ALTER TABLE booking_order
                    ADD COLUMN user_id BIGINT NULL
                    """);
        }
    }

    private void ensureOrderReviewTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_review (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    order_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    worker_id BIGINT NOT NULL,
                    rating INT NOT NULL,
                    content VARCHAR(500) NOT NULL,
                    created_at DATETIME NOT NULL,
                    UNIQUE KEY uk_order_review_order (order_id),
                    CONSTRAINT fk_order_review_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
                    CONSTRAINT fk_order_review_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
                    CONSTRAINT fk_order_review_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
                )
                """);
    }
}
