package com.housekeeping.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名长度不能超过 50 个字符")
        String realName,
        @Size(max = 20, message = "性别长度不能超过 20 个字符")
        String gender,
        @Size(max = 50, message = "所在城市长度不能超过 50 个字符")
        String city,
        @Size(max = 500, message = "个人简介长度不能超过 500 个字符")
        String bio,
        @Size(max = 255, message = "头像地址长度不能超过 255 个字符")
        String avatarUrl
) {
}
