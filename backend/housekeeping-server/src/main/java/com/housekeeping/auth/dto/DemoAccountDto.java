package com.housekeeping.auth.dto;

public record DemoAccountDto(
        String roleCode,
        String phone,
        String password,
        String realName
) {
}
