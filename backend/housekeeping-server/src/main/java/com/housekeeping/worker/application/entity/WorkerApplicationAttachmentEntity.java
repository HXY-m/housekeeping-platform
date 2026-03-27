package com.housekeeping.worker.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("worker_application_attachment")
public class WorkerApplicationAttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime createdAt;

    public WorkerApplicationAttachmentEntity() {
    }

    public WorkerApplicationAttachmentEntity(Long applicationId,
                                             String fileName,
                                             String fileUrl,
                                             Long fileSize,
                                             LocalDateTime createdAt) {
        this.applicationId = applicationId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
