package com.housekeeping.admin.dto;

import java.util.List;

public record AdminPermissionGroupDto(
        String key,
        String label,
        List<AdminPermissionItemDto> permissions
) {
}
