package com.housekeeping.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminRolePermissionUpdateRequest(
        @NotNull(message = "permissionCodes cannot be null")
        List<String> permissionCodes
) {
}
