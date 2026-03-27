package com.housekeeping.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminUserSaveRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名长度不能超过 50 个字符")
        String realName,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "请输入合法的 11 位手机号")
        String phone,

        @Size(min = 6, max = 20, message = "密码长度需在 6 到 20 位之间")
        String password,

        @Size(max = 20, message = "状态长度不能超过 20 个字符")
        String status,

        @NotEmpty(message = "至少选择一个角色")
        List<String> roleCodes
) {
}
