package com.housekeeping.auth.support;

import java.util.List;

public record SessionUser(
        Long userId,
        String phone,
        String realName,
        String roleCode,
        List<String> permissionCodes,
        long expireAt
) {
}
