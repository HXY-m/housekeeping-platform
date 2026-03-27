package com.housekeeping.aftersale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AfterSaleHandleRequest(
        @NotBlank(message = "处理状态不能为空")
        String status,
        @Size(max = 255, message = "处理备注长度不能超过 255 个字符")
        String adminRemark
) {
}
