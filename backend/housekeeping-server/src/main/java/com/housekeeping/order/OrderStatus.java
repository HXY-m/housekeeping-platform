package com.housekeeping.order;

import java.util.List;

public enum OrderStatus {

    PENDING("待接单", "待服务"),
    ACCEPTED("已接单"),
    CONFIRMED("用户已确认"),
    IN_SERVICE("服务中"),
    WAITING_USER_CONFIRMATION("待用户确认完工"),
    COMPLETED("已完成");

    private final List<String> aliases;

    OrderStatus(String... aliases) {
        this.aliases = List.of(aliases);
    }

    public String code() {
        return name();
    }

    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (name().equalsIgnoreCase(value)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(value));
    }
}
