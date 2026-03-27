package com.housekeeping.worker;

public final class WorkerQualificationStatus {

    public static final String UNSUBMITTED = "UNSUBMITTED";
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private WorkerQualificationStatus() {
    }

    public static boolean isPublicVisible(String status) {
        return APPROVED.equalsIgnoreCase(status);
    }
}
