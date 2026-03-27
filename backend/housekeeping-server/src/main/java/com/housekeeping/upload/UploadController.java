package com.housekeeping.upload;

import com.housekeeping.auth.annotation.RequireLogin;
import com.housekeeping.common.ApiResponse;
import com.housekeeping.upload.dto.UploadedFileDto;
import com.housekeeping.upload.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequireLogin
@Tag(name = "File Upload", description = "Reusable image upload APIs")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an image file")
    public ApiResponse<UploadedFileDto> uploadImage(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileStorageService.storeImage(file));
    }
}
