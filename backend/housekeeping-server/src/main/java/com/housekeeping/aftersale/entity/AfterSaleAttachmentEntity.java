package com.housekeeping.aftersale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("order_after_sale_attachment")
public class AfterSaleAttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long afterSaleId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime createdAt;

    public AfterSaleAttachmentEntity() {
    }

    public AfterSaleAttachmentEntity(Long afterSaleId, String fileName, String fileUrl, LocalDateTime createdAt) {
        this.afterSaleId = afterSaleId;
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

    public Long getAfterSaleId() {
        return afterSaleId;
    }

    public void setAfterSaleId(Long afterSaleId) {
        this.afterSaleId = afterSaleId;
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
