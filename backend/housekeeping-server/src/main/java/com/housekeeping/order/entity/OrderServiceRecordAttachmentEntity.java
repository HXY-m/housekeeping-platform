package com.housekeeping.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("booking_order_service_record_attachment")
public class OrderServiceRecordAttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long serviceRecordId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime createdAt;

    public OrderServiceRecordAttachmentEntity() {
    }

    public OrderServiceRecordAttachmentEntity(Long serviceRecordId, String fileName, String fileUrl, LocalDateTime createdAt) {
        this.serviceRecordId = serviceRecordId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceRecordId() {
        return serviceRecordId;
    }

    public void setServiceRecordId(Long serviceRecordId) {
        this.serviceRecordId = serviceRecordId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
