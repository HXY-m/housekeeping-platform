package com.housekeeping.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCategorySaveRequest(
        @NotBlank(message = "项目名称不能为空")
        @Size(max = 100, message = "项目名称长度不能超过 100 个字符")
        String name,

        @NotBlank(message = "项目描述不能为空")
        @Size(max = 300, message = "项目描述长度不能超过 300 个字符")
        String description,

        @NotBlank(message = "价格标签不能为空")
        @Size(max = 50, message = "价格标签长度不能超过 50 个字符")
        String priceLabel,

        @Size(max = 100, message = "slug 长度不能超过 100 个字符")
        String slug,

        @Size(max = 100, message = "服务时长长度不能超过 100 个字符")
        String serviceDuration,

        @Size(max = 255, message = "服务范围长度不能超过 255 个字符")
        String serviceArea,

        @Size(max = 255, message = "适用场景长度不能超过 255 个字符")
        String serviceScene,

        @Size(max = 255, message = "增值服务长度不能超过 255 个字符")
        String extraServices,

        @Size(max = 500, message = "展示图片地址长度不能超过 500 个字符")
        String imageUrl,

        @NotNull(message = "请指定是否启用")
        Boolean enabled
) {
}
