package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(35)
public class CommunicationSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public CommunicationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureNotificationTable();
        ensureOrderMessageTable();
    }

    private void ensureNotificationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_notification (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    recipient_user_id BIGINT NOT NULL,
                    recipient_role_code VARCHAR(50) NOT NULL DEFAULT '',
                    type VARCHAR(50) NOT NULL DEFAULT '',
                    title VARCHAR(120) NOT NULL DEFAULT '',
                    content VARCHAR(500) NOT NULL DEFAULT '',
                    related_type VARCHAR(50) NOT NULL DEFAULT '',
                    related_id BIGINT NULL,
                    action_path VARCHAR(255) NOT NULL DEFAULT '',
                    read_flag TINYINT(1) NOT NULL DEFAULT 0,
                    created_at DATETIME NOT NULL,
                    read_at DATETIME NULL,
                    KEY idx_user_notification_recipient (recipient_user_id, recipient_role_code),
                    KEY idx_user_notification_read_flag (read_flag),
                    KEY idx_user_notification_created_at (created_at)
                )
                """);
    }

    private void ensureOrderMessageTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_message (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    order_id BIGINT NOT NULL,
                    sender_user_id BIGINT NOT NULL,
                    sender_role_code VARCHAR(50) NOT NULL DEFAULT '',
                    sender_name VARCHAR(50) NOT NULL DEFAULT '',
                    content VARCHAR(500) NOT NULL DEFAULT '',
                    created_at DATETIME NOT NULL,
                    KEY idx_order_message_order (order_id),
                    KEY idx_order_message_created_at (created_at),
                    CONSTRAINT fk_order_message_order FOREIGN KEY (order_id) REFERENCES booking_order(id)
                )
                """);
    }
}
