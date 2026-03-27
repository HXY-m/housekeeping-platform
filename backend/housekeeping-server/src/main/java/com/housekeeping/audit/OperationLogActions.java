package com.housekeeping.audit;

public final class OperationLogActions {

    public static final String ORDER_CREATE = "ORDER_CREATE";
    public static final String ORDER_ACCEPT = "ORDER_ACCEPT";
    public static final String ORDER_START = "ORDER_START";
    public static final String ORDER_COMPLETE = "ORDER_COMPLETE";
    public static final String ORDER_REVIEW = "ORDER_REVIEW";
    public static final String AFTER_SALE_CREATE = "AFTER_SALE_CREATE";
    public static final String AFTER_SALE_HANDLE = "AFTER_SALE_HANDLE";
    public static final String WORKER_APPLICATION_SUBMIT = "WORKER_APPLICATION_SUBMIT";
    public static final String WORKER_APPLICATION_REVIEW = "WORKER_APPLICATION_REVIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String CATEGORY_CREATE = "CATEGORY_CREATE";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE = "CATEGORY_DELETE";

    private OperationLogActions() {
    }
}
