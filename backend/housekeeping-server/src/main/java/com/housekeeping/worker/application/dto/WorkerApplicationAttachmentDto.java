package com.housekeeping.worker.application.dto;

public record WorkerApplicationAttachmentDto(
        Long id,
        String name,
        String url,
        long size
) {
}
