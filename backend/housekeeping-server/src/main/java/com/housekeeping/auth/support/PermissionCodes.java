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
            new PermissionDefinition(USER_DASHBOARD_VIEW, "用户看板", "user-experience", "用户端", "查看用户中心首页"),
            new PermissionDefinition(USER_PROFILE_MANAGE, "资料与地址", "user-experience", "用户端", "维护个人资料和地址簿"),
            new PermissionDefinition(USER_FAVORITE_MANAGE, "收藏服务人员", "user-experience", "用户端", "收藏或取消收藏服务人员"),
            new PermissionDefinition(USER_ORDER_USE, "下单与订单", "user-experience", "用户端", "预约、确认、评价订单"),
            new PermissionDefinition(USER_AFTER_SALE_USE, "售后服务", "user-experience", "用户端", "提交并跟踪售后工单"),
            new PermissionDefinition(USER_MESSAGE_USE, "消息中心", "user-experience", "用户端", "查看通知和订单沟通"),

            new PermissionDefinition(WORKER_DASHBOARD_VIEW, "服务人员看板", "worker-console", "服务人员端", "查看服务人员工作台"),
            new PermissionDefinition(WORKER_ORDER_HANDLE, "履约处理", "worker-console", "服务人员端", "接单、开工、完工和上传记录"),
            new PermissionDefinition(WORKER_QUALIFICATION_SUBMIT, "资质申请", "worker-console", "服务人员端", "提交资质审核材料"),
            new PermissionDefinition(WORKER_MESSAGE_USE, "服务人员消息", "worker-console", "服务人员端", "查看通知和订单沟通"),

            new PermissionDefinition(ADMIN_DASHBOARD_VIEW, "运营看板", "admin-console", "后台核心", "查看平台运营看板"),
            new PermissionDefinition(ADMIN_MESSAGE_VIEW, "后台消息", "admin-console", "后台核心", "查看后台通知"),
            new PermissionDefinition(ADMIN_ORDER_MANAGE, "订单监管", "admin-console", "后台核心", "查看和治理平台订单"),
            new PermissionDefinition(ADMIN_USER_MANAGE, "用户管理", "admin-console", "后台核心", "管理平台用户和角色"),
            new PermissionDefinition(ADMIN_SERVICE_MANAGE, "服务项目管理", "admin-console", "后台核心", "维护服务项目和展示内容"),
            new PermissionDefinition(ADMIN_APPLICATION_REVIEW, "资质审核", "admin-console", "后台核心", "审核服务人员资质申请"),
            new PermissionDefinition(ADMIN_AFTER_SALE_MANAGE, "售后处理", "admin-console", "后台核心", "处理售后工单"),

            new PermissionDefinition(ADMIN_OPERATION_LOG_VIEW, "操作日志", "admin-governance", "治理与配置", "查看关键操作日志"),
            new PermissionDefinition(ADMIN_REPORT_EXPORT, "报表导出", "admin-governance", "治理与配置", "导出运营和治理报表"),
            new PermissionDefinition(ADMIN_PERMISSION_MANAGE, "权限配置", "admin-governance", "治理与配置", "配置角色与权限绑定")
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
