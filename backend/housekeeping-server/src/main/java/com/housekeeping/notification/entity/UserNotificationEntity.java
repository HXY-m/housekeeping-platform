package com.housekeeping.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_notification")
public class UserNotificationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recipientUserId;
    private String recipientRoleCode;
    private String type;
    private String title;
    private String content;
    private String relatedType;
    private Long relatedId;
    private String actionPath;
    private Boolean readFlag;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public UserNotificationEntity() {
    }

    public UserNotificationEntity(Long recipientUserId,
                                  String recipientRoleCode,
                                  String type,
                                  String title,
                                  String content,
                                  String relatedType,
                                  Long relatedId,
                                  String actionPath,
                                  Boolean readFlag,
                                  LocalDateTime createdAt,
                                  LocalDateTime readAt) {
        this.recipientUserId = recipientUserId;
        this.recipientRoleCode = recipientRoleCode;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedType = relatedType;
        this.relatedId = relatedId;
        this.actionPath = actionPath;
        this.readFlag = readFlag;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientRoleCode() {
        return recipientRoleCode;
    }

    public void setRecipientRoleCode(String recipientRoleCode) {
        this.recipientRoleCode = recipientRoleCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getActionPath() {
        return actionPath;
    }

    public void setActionPath(String actionPath) {
        this.actionPath = actionPath;
    }

    public Boolean getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Boolean readFlag) {
        this.readFlag = readFlag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
