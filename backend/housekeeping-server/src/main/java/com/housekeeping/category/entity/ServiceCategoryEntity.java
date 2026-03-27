package com.housekeeping.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("service_category")
public class ServiceCategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String priceLabel;
    private String slug;
    private String serviceDuration;
    private String serviceArea;
    private String serviceScene;
    private String extraServices;
    private Integer enabled;

    public ServiceCategoryEntity() {
    }

    public ServiceCategoryEntity(String name, String description, String priceLabel, String slug) {
        this(name, description, priceLabel, slug, "2小时", "平台服务区域", "家庭日常场景", "", 1);
    }

    public ServiceCategoryEntity(String name,
                                 String description,
                                 String priceLabel,
                                 String slug,
                                 String serviceDuration,
                                 String serviceArea,
                                 String serviceScene,
                                 String extraServices,
                                 Integer enabled) {
        this.name = name;
        this.description = description;
        this.priceLabel = priceLabel;
        this.slug = slug;
        this.serviceDuration = serviceDuration;
        this.serviceArea = serviceArea;
        this.serviceScene = serviceScene;
        this.extraServices = extraServices;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriceLabel() {
        return priceLabel;
    }

    public void setPriceLabel(String priceLabel) {
        this.priceLabel = priceLabel;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getServiceDuration() {
        return serviceDuration;
    }

    public void setServiceDuration(String serviceDuration) {
        this.serviceDuration = serviceDuration;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getServiceScene() {
        return serviceScene;
    }

    public void setServiceScene(String serviceScene) {
        this.serviceScene = serviceScene;
    }

    public String getExtraServices() {
        return extraServices;
    }

    public void setExtraServices(String extraServices) {
        this.extraServices = extraServices;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
