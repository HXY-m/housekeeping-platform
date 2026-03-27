package com.housekeeping.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderReviewRequest(
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低为 1 分")
        @Max(value = 5, message = "评分最高为 5 分")
        Integer rating,
        @NotBlank(message = "评价内容不能为空")
        String content
) {
}
