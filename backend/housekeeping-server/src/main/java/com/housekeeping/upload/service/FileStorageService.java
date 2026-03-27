package com.housekeeping.upload.service;

import com.housekeeping.upload.dto.UploadedFileDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    UploadedFileDto storeImage(MultipartFile file);

    UploadedFileDto storeAttachment(MultipartFile file);
}
