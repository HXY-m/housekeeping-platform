package com.housekeeping.admin.dto;

import java.util.List;

public record AdminUserDto(
        Long id,
        String realName,
        String phone,
        String status,
        List<String> roleCodes,
        String city,
        boolean workerProfileBound
) {
}
