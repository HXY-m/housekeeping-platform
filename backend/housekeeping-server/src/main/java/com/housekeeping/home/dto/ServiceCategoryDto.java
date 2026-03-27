package com.housekeeping.home.dto;

public record ServiceCategoryDto(
        Long id,
        String name,
        String description,
        String priceLabel,
        String slug
) {
}
