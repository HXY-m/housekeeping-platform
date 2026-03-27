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
import java.util.function.Predicate;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final FileStorageProperties fileStorageProperties;

    public LocalFileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public UploadedFileDto storeImage(MultipartFile file) {
        return storeFile(
                file,
                "images",
                "image",
                fileStorageProperties.getMaxImageSize(),
                contentType -> contentType.startsWith("image/"),
                "请选择图片后再上传。",
                "当前仅支持上传图片格式文件。",
                "图片大小超过系统限制。",
                "图片保存失败，请稍后重试。"
        );
    }

    @Override
    public UploadedFileDto storeAttachment(MultipartFile file) {
        return storeFile(
                file,
                "attachments",
                "attachment",
                fileStorageProperties.getMaxAttachmentSize(),
                this::supportsAttachment,
                "请选择附件后再上传。",
                "当前仅支持图片、PDF、Word 或文本文件。",
                "附件大小超过系统限制。",
                "附件保存失败，请稍后重试。"
        );
    }

    private UploadedFileDto storeFile(MultipartFile file,
                                      String subDirectory,
                                      String fallbackName,
                                      long maxSize,
                                      Predicate<String> contentTypeValidator,
                                      String emptyMessage,
                                      String unsupportedMessage,
                                      String sizeExceededMessage,
                                      String storeFailedMessage) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(emptyMessage);
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentTypeValidator.test(contentType)) {
            throw new BusinessException(unsupportedMessage);
        }

        if (file.getSize() > maxSize) {
            throw new BusinessException(sizeExceededMessage);
        }

        String originalName = StringUtils.hasText(file.getOriginalFilename())
                ? StringUtils.cleanPath(file.getOriginalFilename())
                : fallbackName;
        String extension = resolveExtension(originalName, contentType);
        String storedName = LocalDate.now().format(DATE_FORMATTER) + "-" + UUID.randomUUID() + extension;

        Path baseDirectory = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetDirectory = baseDirectory.resolve(subDirectory);
        Path targetFile = targetDirectory.resolve(storedName);

        try {
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException(storeFailedMessage);
        }

        return new UploadedFileDto(
                originalName,
                buildPublicUrl(subDirectory + "/" + storedName),
                file.getSize()
        );
    }

    private boolean supportsAttachment(String contentType) {
        return contentType.startsWith("image/")
                || "application/pdf".equals(contentType)
                || "application/msword".equals(contentType)
                || "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)
                || "text/plain".equals(contentType);
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
            case "application/pdf" -> ".pdf";
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "text/plain" -> ".txt";
            default -> ".bin";
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
