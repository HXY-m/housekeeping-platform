package com.housekeeping.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_user")
public class SysUserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String username;
    private String password;
    private String realName;
    private String status;

    public SysUserEntity() {
    }

    public SysUserEntity(String phone, String password, String realName, String status) {
        this.phone = phone;
        this.password = password;
        this.realName = realName;
        this.status = status;
    }

    public SysUserEntity(String phone, String username, String password, String realName, String status) {
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
