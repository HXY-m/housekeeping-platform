package com.housekeeping.worker.application.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkerApplicationReviewRequest(
        @NotBlank(message = "审核结果不能为空") String action,
        String adminRemark
) {
}
