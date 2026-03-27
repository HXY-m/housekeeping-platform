package com.housekeeping.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull(message = "服务人员不能为空") Long workerId,
        @NotBlank(message = "服务项目不能为空") String serviceName,
        @NotBlank(message = "联系人不能为空") String customerName,
        @NotBlank(message = "联系电话不能为空") String contactPhone,
        @NotBlank(message = "服务地址不能为空") String serviceAddress,
        @NotBlank(message = "预约日期不能为空") String bookingDate,
        @NotBlank(message = "预约时间段不能为空") String bookingSlot,
        @NotBlank(message = "需求说明不能为空") String remark
) {
}
