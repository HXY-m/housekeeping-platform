package com.housekeeping.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("booking_order_progress")
public class OrderProgressEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String progressStatus;
    private String progressNote;

    public OrderProgressEntity() {
    }

    public OrderProgressEntity(Long orderId, String progressStatus, String progressNote) {
        this.orderId = orderId;
        this.progressStatus = progressStatus;
        this.progressNote = progressNote;
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

    public String getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getProgressNote() {
        return progressNote;
    }

    public void setProgressNote(String progressNote) {
        this.progressNote = progressNote;
    }
}
