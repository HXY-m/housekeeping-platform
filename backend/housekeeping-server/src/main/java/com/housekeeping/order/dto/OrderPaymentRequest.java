package com.housekeeping.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderPaymentRequest(
        @NotBlank(message = "支付方式不能为空")
        @Size(max = 30, message = "支付方式长度不能超过 30 个字符")
        String paymentMethod
) {
}
