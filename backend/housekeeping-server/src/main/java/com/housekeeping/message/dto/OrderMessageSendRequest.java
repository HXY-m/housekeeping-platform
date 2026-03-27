package com.housekeeping.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderMessageSendRequest(
        @NotBlank(message = "留言内容不能为空")
        @Size(max = 300, message = "留言内容不能超过 300 字")
        String content
) {
}
