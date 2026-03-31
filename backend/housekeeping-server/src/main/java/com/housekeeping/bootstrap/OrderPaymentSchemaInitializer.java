package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(12)
public class OrderPaymentSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public OrderPaymentSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureBookingOrderPaymentColumns();
        ensureOrderPaymentTable();
    }

    private void ensureBookingOrderPaymentColumns() {
        ensureColumn(
                "payable_amount",
                """
                ALTER TABLE booking_order
                ADD COLUMN payable_amount INT NOT NULL DEFAULT 0
                """
        );
        ensureColumn(
                "payment_status",
                """
                ALTER TABLE booking_order
                ADD COLUMN payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID'
                """
        );
        ensureColumn(
                "payment_method",
                """
                ALTER TABLE booking_order
                ADD COLUMN payment_method VARCHAR(30) NOT NULL DEFAULT ''
                """
        );
        ensureColumn(
                "paid_at",
                """
                ALTER TABLE booking_order
                ADD COLUMN paid_at DATETIME NULL
                """
        );
    }

    private void ensureColumn(String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'booking_order'
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                columnName
        );
        if (count != null && count == 0) {
            jdbcTemplate.execute(alterSql);
        }
    }

    private void ensureOrderPaymentTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS booking_order_payment (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    order_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    amount INT NOT NULL DEFAULT 0,
                    payment_method VARCHAR(30) NOT NULL DEFAULT '',
                    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
                    payment_no VARCHAR(64) NOT NULL,
                    created_at DATETIME NOT NULL,
                    paid_at DATETIME NULL,
                    KEY idx_order_payment_order (order_id),
                    KEY idx_order_payment_user (user_id),
                    KEY idx_order_payment_status (payment_status),
                    UNIQUE KEY uk_order_payment_no (payment_no),
                    CONSTRAINT fk_order_payment_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
                    CONSTRAINT fk_order_payment_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
                )
                """);
    }
}
