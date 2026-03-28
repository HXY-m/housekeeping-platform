package com.housekeeping.auth.dto;

import java.util.List;

public record CurrentUserDto(
        Long id,
        String phone,
        String realName,
        String roleCode,
        List<String> permissionCodes
) {
}
