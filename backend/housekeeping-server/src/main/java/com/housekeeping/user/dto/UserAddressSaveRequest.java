package com.housekeeping.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserAddressSaveRequest(
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50, message = "联系人长度不能超过 50 个字符")
        String contactName,
        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20, message = "联系电话长度不能超过 20 个字符")
        String contactPhone,
        @NotBlank(message = "所在城市不能为空")
        @Size(max = 50, message = "所在城市长度不能超过 50 个字符")
        String city,
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255, message = "详细地址长度不能超过 255 个字符")
        String detailAddress,
        @Size(max = 30, message = "地址标签长度不能超过 30 个字符")
        String addressTag,
        @NotNull(message = "请指定是否默认地址")
        Boolean defaultAddress,
        Double latitude,
        Double longitude
) {
}
