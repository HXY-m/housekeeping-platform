package com.housekeeping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan({
        "com.housekeeping.auth.mapper",
        "com.housekeeping.aftersale.mapper",
        "com.housekeeping.category.mapper",
        "com.housekeeping.user.mapper",
        "com.housekeeping.worker.mapper",
        "com.housekeeping.worker.application.mapper",
        "com.housekeeping.order.mapper"
})
public class HousekeepingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HousekeepingApplication.class, args);
    }
}
