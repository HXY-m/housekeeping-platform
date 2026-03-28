package com.housekeeping.admin.dto;

public record AdminCategoryDto(
        Long id,
        String name,
        String description,
        String priceLabel,
        String slug,
        String serviceDuration,
        String serviceArea,
        String serviceScene,
        String extraServices,
        String imageUrl,
        boolean enabled
) {
}
