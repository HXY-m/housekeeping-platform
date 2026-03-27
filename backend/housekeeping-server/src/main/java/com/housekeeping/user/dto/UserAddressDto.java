package com.housekeeping.user.dto;

public record UserAddressDto(
        Long id,
        String contactName,
        String contactPhone,
        String city,
        String detailAddress,
        String addressTag,
        boolean defaultAddress
) {
}
