package com.housekeeping.auth.dto;

public record LoginResponse(
        String token,
        CurrentUserDto user
) {
}
