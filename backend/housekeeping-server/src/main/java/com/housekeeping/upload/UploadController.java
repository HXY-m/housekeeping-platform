package com.housekeeping.upload;

import com.housekeeping.common.ApiResponse;
import com.housekeeping.upload.dto.UploadedFileDto;
import com.housekeeping.upload.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@PreAuthorize("isAuthenticated()")
@Tag(name = "文件上传", description = "通用图片与附件上传接口")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片文件")
    public ApiResponse<UploadedFileDto> uploadImage(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileStorageService.storeImage(file));
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传通用附件")
    public ApiResponse<UploadedFileDto> uploadAttachment(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileStorageService.storeAttachment(file));
    }
}
