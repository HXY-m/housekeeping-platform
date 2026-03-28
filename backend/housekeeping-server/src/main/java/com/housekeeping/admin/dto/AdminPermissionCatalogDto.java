package com.housekeeping.admin.dto;

import java.util.List;

public record AdminPermissionCatalogDto(
        List<AdminPermissionGroupDto> permissionGroups,
        List<AdminRolePermissionDto> roles
) {
}
