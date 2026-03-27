package com.housekeeping.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AfterSaleSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public AfterSaleSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS order_after_sale (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    order_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    worker_id BIGINT NOT NULL,
                    issue_type VARCHAR(50) NOT NULL,
                    content VARCHAR(500) NOT NULL,
                    contact_phone VARCHAR(20) NOT NULL,
                    status VARCHAR(30) NOT NULL,
                    admin_remark VARCHAR(255) NOT NULL DEFAULT '',
                    created_at DATETIME NOT NULL,
                    handled_at DATETIME NULL,
                    UNIQUE KEY uk_after_sale_order (order_id),
                    CONSTRAINT fk_after_sale_order FOREIGN KEY (order_id) REFERENCES booking_order(id),
                    CONSTRAINT fk_after_sale_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
                    CONSTRAINT fk_after_sale_worker FOREIGN KEY (worker_id) REFERENCES worker_profile(id)
                )
                """);
    }
}
