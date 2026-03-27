package com.housekeeping.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("booking_order")
public class OrderEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String serviceName;
    private Long workerId;
    private String customerName;
    private String contactPhone;
    private String serviceAddress;
    private String bookingDate;
    private String bookingSlot;
    private String status;
    private String progressNote;
    private String remark;

    public OrderEntity() {
    }

    public OrderEntity(Long userId, String serviceName, Long workerId, String customerName, String contactPhone,
                       String serviceAddress, String bookingDate, String bookingSlot, String status,
                       String progressNote, String remark) {
        this.userId = userId;
        this.serviceName = serviceName;
        this.workerId = workerId;
        this.customerName = customerName;
        this.contactPhone = contactPhone;
        this.serviceAddress = serviceAddress;
        this.bookingDate = bookingDate;
        this.bookingSlot = bookingSlot;
        this.status = status;
        this.progressNote = progressNote;
        this.remark = remark;
    }

    public OrderEntity(String serviceName, Long workerId, String customerName, String contactPhone,
                       String serviceAddress, String bookingDate, String bookingSlot, String status,
                       String progressNote, String remark) {
        this(null, serviceName, workerId, customerName, contactPhone, serviceAddress, bookingDate, bookingSlot,
                status, progressNote, remark);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingSlot() {
        return bookingSlot;
    }

    public void setBookingSlot(String bookingSlot) {
        this.bookingSlot = bookingSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgressNote() {
        return progressNote;
    }

    public void setProgressNote(String progressNote) {
        this.progressNote = progressNote;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
