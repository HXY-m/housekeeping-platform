package com.housekeeping.worker.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record WorkerApplicationSubmitRequest(
        @NotBlank(message = "真实姓名不能为空") String realName,
        @NotBlank(message = "联系方式不能为空") String phone,
        @NotBlank(message = "擅长服务类型不能为空") String serviceTypes,
        @Min(value = 0, message = "从业年限不能小于 0") Integer yearsOfExperience,
        @NotBlank(message = "资质标签不能为空") String certificates,
        @NotBlank(message = "服务区域不能为空") String serviceAreas,
        @NotBlank(message = "服务时段不能为空") String availableSchedule,
        @NotBlank(message = "个人简介不能为空") String intro,
        @Valid @NotEmpty(message = "请至少上传 1 份资质附件") List<WorkerApplicationAttachmentRequest> attachments
) {
}
