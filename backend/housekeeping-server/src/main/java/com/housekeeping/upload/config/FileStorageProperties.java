package com.housekeeping.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    private String uploadDir = "./uploads";
    private String publicUrlPrefix = "/uploads";
    private long maxImageSize = 5L * 1024 * 1024;
    private long maxAttachmentSize = 10L * 1024 * 1024;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPublicUrlPrefix() {
        return publicUrlPrefix;
    }

    public void setPublicUrlPrefix(String publicUrlPrefix) {
        this.publicUrlPrefix = publicUrlPrefix;
    }

    public long getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(long maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public long getMaxAttachmentSize() {
        return maxAttachmentSize;
    }

    public void setMaxAttachmentSize(long maxAttachmentSize) {
        this.maxAttachmentSize = maxAttachmentSize;
    }
}
