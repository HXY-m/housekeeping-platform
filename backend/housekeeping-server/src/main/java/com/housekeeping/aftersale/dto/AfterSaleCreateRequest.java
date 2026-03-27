package com.housekeeping.aftersale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AfterSaleCreateRequest(
        @NotNull(message = "订单不能为空")
        Long orderId,
        @NotBlank(message = "问题类型不能为空")
        @Size(max = 50, message = "问题类型长度不能超过 50 个字符")
        String issueType,
        @NotBlank(message = "问题描述不能为空")
        @Size(max = 500, message = "问题描述长度不能超过 500 个字符")
        String content,
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20, message = "联系电话长度不能超过 20 个字符")
        String contactPhone
) {
}
