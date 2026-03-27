package com.housekeeping.auth.dto;

public record CurrentUserDto(
        Long id,
        String phone,
        String realName,
        String roleCode
) {
}
