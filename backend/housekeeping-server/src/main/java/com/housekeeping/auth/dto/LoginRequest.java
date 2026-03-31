package com.housekeeping.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "登录方式不能为空")
        String loginType,
        @NotBlank(message = "登录账号不能为空")
        String account,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在 6 到 20 位之间")
        String password,
        @NotBlank(message = "登录角色不能为空")
        String roleCode
) {
}
