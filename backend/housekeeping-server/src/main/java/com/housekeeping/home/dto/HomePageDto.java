package com.housekeeping.home.dto;

import java.util.List;

public record HomePageDto(
        String headline,
        String subHeadline,
        List<ServiceCategoryDto> categories,
        List<Highlight> highlights,
        List<WorkerCardDto> featuredWorkers
) {
}
