package com.housekeeping.message.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("order_message")
public class OrderMessageEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long senderUserId;
    private String senderRoleCode;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;

    public OrderMessageEntity() {
    }

    public OrderMessageEntity(Long orderId,
                              Long senderUserId,
                              String senderRoleCode,
                              String senderName,
                              String content,
                              LocalDateTime createdAt) {
        this.orderId = orderId;
        this.senderUserId = senderUserId;
        this.senderRoleCode = senderRoleCode;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderRoleCode() {
        return senderRoleCode;
    }

    public void setSenderRoleCode(String senderRoleCode) {
        this.senderRoleCode = senderRoleCode;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
