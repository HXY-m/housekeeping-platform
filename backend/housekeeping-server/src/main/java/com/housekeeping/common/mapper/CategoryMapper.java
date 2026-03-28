package com.housekeeping.common.mapper;

import com.housekeeping.category.entity.ServiceCategoryEntity;
import com.housekeeping.home.dto.ServiceCategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public ServiceCategoryDto toDto(ServiceCategoryEntity category) {
        return new ServiceCategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getPriceLabel(),
                category.getSlug(),
                category.getImageUrl()
        );
    }
}
