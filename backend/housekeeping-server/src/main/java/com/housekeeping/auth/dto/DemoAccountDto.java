package com.housekeeping.auth.dto;

public record DemoAccountDto(
        String roleCode,
        String phone,
        String username,
        String password,
        String realName
) {
}
