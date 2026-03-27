package com.housekeeping.upload.service;

import com.housekeeping.exception.BusinessException;
import com.housekeeping.upload.config.FileStorageProperties;
import com.housekeeping.upload.dto.UploadedFileDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final FileStorageProperties fileStorageProperties;

    public LocalFileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public UploadedFileDto storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Please select an image to upload.");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new BusinessException("Only image files are supported.");
        }

        if (file.getSize() > fileStorageProperties.getMaxImageSize()) {
            throw new BusinessException("Image size exceeds the configured limit.");
        }

        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? StringUtils.cleanPath(file.getOriginalFilename())
                : "image";
        String extension = resolveExtension(originalName, contentType);
        String storedName = LocalDate.now().format(DATE_FORMATTER) + "-" + UUID.randomUUID() + extension;

        Path baseDirectory = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path imageDirectory = baseDirectory.resolve("images");
        Path targetFile = imageDirectory.resolve(storedName);

        try {
            Files.createDirectories(imageDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException("Failed to store the uploaded image.");
        }

        return new UploadedFileDto(
                originalName,
                buildPublicUrl("images/" + storedName),
                file.getSize()
        );
    }

    private String resolveExtension(String originalName, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalName);
        if (StringUtils.hasText(extension)) {
            return "." + extension.toLowerCase(Locale.ROOT);
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".img";
        };
    }

    private String buildPublicUrl(String relativePath) {
        String prefix = fileStorageProperties.getPublicUrlPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/uploads";
        }
        String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return normalizedPrefix + "/" + relativePath.replace("\\", "/");
    }
}
