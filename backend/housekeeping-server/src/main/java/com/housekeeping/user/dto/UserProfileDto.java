package com.housekeeping.user.dto;

public record UserProfileDto(
        Long userId,
        String realName,
        String phone,
        String gender,
        String city,
        String bio,
        String avatarUrl
) {
}
