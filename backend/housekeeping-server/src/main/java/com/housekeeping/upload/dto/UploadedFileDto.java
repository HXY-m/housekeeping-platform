package com.housekeeping.upload.dto;

public record UploadedFileDto(
        String name,
        String url,
        long size
) {
}
