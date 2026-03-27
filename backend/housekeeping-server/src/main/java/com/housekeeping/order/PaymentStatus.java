package com.housekeeping.order;

public enum PaymentStatus {

    UNPAID,
    PAID;

    public String code() {
        return name();
    }

    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value);
    }
}
