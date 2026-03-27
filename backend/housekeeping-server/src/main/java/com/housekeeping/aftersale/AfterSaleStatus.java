package com.housekeeping.aftersale;

public enum AfterSaleStatus {

    PENDING,
    PROCESSING,
    RESOLVED,
    REJECTED;

    public boolean isFinalStatus() {
        return this == RESOLVED || this == REJECTED;
    }

    public static AfterSaleStatus from(String value) {
        try {
            return AfterSaleStatus.valueOf(value.trim().toUpperCase());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unsupported after-sale status: " + value, exception);
        }
    }
}
