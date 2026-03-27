package com.housekeeping.worker.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WorkerApplicationAttachmentRequest(
        @NotBlank(message = "附件名称不能为空") String name,
        @NotBlank(message = "附件地址不能为空") String url,
        @Min(value = 1, message = "附件大小不正确") long size
) {
}
