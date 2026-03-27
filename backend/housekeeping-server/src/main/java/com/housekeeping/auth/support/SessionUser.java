package com.housekeeping.auth.support;

public record SessionUser(
        Long userId,
        String phone,
        String realName,
        String roleCode,
        long expireAt
) {
}
