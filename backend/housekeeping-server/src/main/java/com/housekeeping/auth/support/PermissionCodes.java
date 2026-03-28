package com.housekeeping.auth.support;

import java.util.List;

public final class PermissionCodes {

    public static final String USER_DASHBOARD_VIEW = "USER_DASHBOARD_VIEW";
    public static final String USER_PROFILE_MANAGE = "USER_PROFILE_MANAGE";
    public static final String USER_FAVORITE_MANAGE = "USER_FAVORITE_MANAGE";
    public static final String USER_ORDER_USE = "USER_ORDER_USE";
    public static final String USER_AFTER_SALE_USE = "USER_AFTER_SALE_USE";
    public static final String USER_MESSAGE_USE = "USER_MESSAGE_USE";

    public static final String WORKER_DASHBOARD_VIEW = "WORKER_DASHBOARD_VIEW";
    public static final String WORKER_ORDER_HANDLE = "WORKER_ORDER_HANDLE";
    public static final String WORKER_QUALIFICATION_SUBMIT = "WORKER_QUALIFICATION_SUBMIT";
    public static final String WORKER_MESSAGE_USE = "WORKER_MESSAGE_USE";

    public static final String ADMIN_DASHBOARD_VIEW = "ADMIN_DASHBOARD_VIEW";
    public static final String ADMIN_MESSAGE_VIEW = "ADMIN_MESSAGE_VIEW";
    public static final String ADMIN_ORDER_MANAGE = "ADMIN_ORDER_MANAGE";
    public static final String ADMIN_USER_MANAGE = "ADMIN_USER_MANAGE";
    public static final String ADMIN_SERVICE_MANAGE = "ADMIN_SERVICE_MANAGE";
    public static final String ADMIN_APPLICATION_REVIEW = "ADMIN_APPLICATION_REVIEW";
    public static final String ADMIN_AFTER_SALE_MANAGE = "ADMIN_AFTER_SALE_MANAGE";
    public static final String ADMIN_OPERATION_LOG_VIEW = "ADMIN_OPERATION_LOG_VIEW";
    public static final String ADMIN_REPORT_EXPORT = "ADMIN_REPORT_EXPORT";
    public static final String ADMIN_PERMISSION_MANAGE = "ADMIN_PERMISSION_MANAGE";

    private static final List<PermissionDefinition> DEFINITIONS = List.of(
            new PermissionDefinition(USER_DASHBOARD_VIEW, "User dashboard", "user-experience", "User Experience", "Access the user dashboard."),
            new PermissionDefinition(USER_PROFILE_MANAGE, "Profile and address book", "user-experience", "User Experience", "Maintain personal profile and address book."),
            new PermissionDefinition(USER_FAVORITE_MANAGE, "Favorite workers", "user-experience", "User Experience", "Favorite and unfavorite service workers."),
            new PermissionDefinition(USER_ORDER_USE, "Order workflow", "user-experience", "User Experience", "Create, confirm and review service orders."),
            new PermissionDefinition(USER_AFTER_SALE_USE, "After-sales requests", "user-experience", "User Experience", "Submit and track after-sales requests."),
            new PermissionDefinition(USER_MESSAGE_USE, "Message center", "user-experience", "User Experience", "Read notifications and send order messages."),
            new PermissionDefinition(WORKER_DASHBOARD_VIEW, "Worker dashboard", "worker-console", "Worker Console", "Access the worker dashboard."),
            new PermissionDefinition(WORKER_ORDER_HANDLE, "Order fulfillment", "worker-console", "Worker Console", "Accept, start and complete orders."),
            new PermissionDefinition(WORKER_QUALIFICATION_SUBMIT, "Qualification application", "worker-console", "Worker Console", "Submit worker qualification materials."),
            new PermissionDefinition(WORKER_MESSAGE_USE, "Worker message center", "worker-console", "Worker Console", "Read worker notifications and order conversations."),
            new PermissionDefinition(ADMIN_DASHBOARD_VIEW, "Admin dashboard", "admin-console", "Admin Console", "Access the admin dashboard."),
            new PermissionDefinition(ADMIN_MESSAGE_VIEW, "Admin message center", "admin-console", "Admin Console", "Read admin notifications."),
            new PermissionDefinition(ADMIN_ORDER_MANAGE, "Order governance", "admin-console", "Admin Console", "View and govern all platform orders."),
            new PermissionDefinition(ADMIN_USER_MANAGE, "User governance", "admin-console", "Admin Console", "Manage platform users and role bindings."),
            new PermissionDefinition(ADMIN_SERVICE_MANAGE, "Service governance", "admin-console", "Admin Console", "Maintain service categories."),
            new PermissionDefinition(ADMIN_APPLICATION_REVIEW, "Qualification review", "admin-console", "Admin Console", "Review worker qualification applications."),
            new PermissionDefinition(ADMIN_AFTER_SALE_MANAGE, "After-sales governance", "admin-console", "Admin Console", "Handle after-sales cases."),
            new PermissionDefinition(ADMIN_OPERATION_LOG_VIEW, "Operation logs", "admin-governance", "Admin Governance", "Inspect platform operation logs."),
            new PermissionDefinition(ADMIN_REPORT_EXPORT, "Report export", "admin-governance", "Admin Governance", "Export analytics and governance reports."),
            new PermissionDefinition(ADMIN_PERMISSION_MANAGE, "Role permission config", "admin-governance", "Admin Governance", "Configure role to permission bindings.")
    );

    private PermissionCodes() {
    }

    public static List<PermissionDefinition> definitions() {
        return DEFINITIONS;
    }

    public static List<String> defaultPermissions(String roleCode) {
        return switch (roleCode) {
            case RoleCodes.USER -> List.of(
                    USER_DASHBOARD_VIEW,
                    USER_PROFILE_MANAGE,
                    USER_FAVORITE_MANAGE,
                    USER_ORDER_USE,
                    USER_AFTER_SALE_USE,
                    USER_MESSAGE_USE
            );
            case RoleCodes.WORKER -> List.of(
                    WORKER_DASHBOARD_VIEW,
                    WORKER_ORDER_HANDLE,
                    WORKER_QUALIFICATION_SUBMIT,
                    WORKER_MESSAGE_USE
            );
            case RoleCodes.ADMIN -> List.of(
                    ADMIN_DASHBOARD_VIEW,
                    ADMIN_MESSAGE_VIEW,
                    ADMIN_ORDER_MANAGE,
                    ADMIN_USER_MANAGE,
                    ADMIN_SERVICE_MANAGE,
                    ADMIN_APPLICATION_REVIEW,
                    ADMIN_AFTER_SALE_MANAGE,
                    ADMIN_OPERATION_LOG_VIEW,
                    ADMIN_REPORT_EXPORT,
                    ADMIN_PERMISSION_MANAGE
            );
            default -> List.of();
        };
    }

    public static List<String> requiredPermissions(String roleCode) {
        return switch (roleCode) {
            case RoleCodes.USER -> List.of(USER_DASHBOARD_VIEW);
            case RoleCodes.WORKER -> List.of(WORKER_DASHBOARD_VIEW);
            case RoleCodes.ADMIN -> List.of(ADMIN_DASHBOARD_VIEW, ADMIN_PERMISSION_MANAGE);
            default -> List.of();
        };
    }

    public record PermissionDefinition(
            String code,
            String name,
            String groupKey,
            String groupName,
            String description
    ) {
    }
}
