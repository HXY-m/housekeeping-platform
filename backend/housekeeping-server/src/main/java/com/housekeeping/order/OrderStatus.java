package com.housekeeping.order;

import java.util.List;

public enum OrderStatus {

    PENDING("待服务", "待接单"),
    ACCEPTED("已接单"),
    IN_SERVICE("服务中"),
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
