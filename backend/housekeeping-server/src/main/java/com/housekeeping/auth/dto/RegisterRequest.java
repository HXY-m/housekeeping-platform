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
        String password
) {
}
