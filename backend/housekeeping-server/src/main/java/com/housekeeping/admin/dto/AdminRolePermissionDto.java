package com.housekeeping.admin.dto;

import java.util.List;

public record AdminRolePermissionDto(
        String roleCode,
        String roleName,
        long userCount,
        List<String> permissionCodes,
        List<String> requiredPermissionCodes
) {
}
