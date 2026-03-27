package com.housekeeping.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "真实姓名不能为空")
        @Size(min = 2, max = 20, message = "真实姓名长度需在 2 到 20 个字符之间")
        String realName,
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "请输入合法的 11 位手机号")
        String phone,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在 6 到 20 位之间")
        String password,
        @NotBlank(message = "注册角色不能为空")
        String roleCode,
        @Size(max = 50, message = "所在城市长度不能超过 50 个字符")
        String city,
        Integer hourlyPrice,
        @Size(max = 255, message = "擅长服务类型长度不能超过 255 个字符")
        String serviceTypes,
        Integer yearsOfExperience,
        @Size(max = 500, message = "资质证书长度不能超过 500 个字符")
        String certificates,
        @Size(max = 255, message = "服务区域长度不能超过 255 个字符")
        String serviceAreas,
        @Size(max = 255, message = "可服务时段长度不能超过 255 个字符")
        String availableSchedule,
        @Size(max = 500, message = "个人介绍长度不能超过 500 个字符")
        String intro
) {
}
