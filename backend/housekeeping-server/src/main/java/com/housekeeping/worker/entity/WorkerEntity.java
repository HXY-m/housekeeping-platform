package com.housekeeping.worker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("worker_profile")
public class WorkerEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String roleLabel;
    private Double rating;
    private Integer completedOrders;
    private Integer hourlyPrice;
    private String city;
    private String intro;
    private String tags;
    private String nextAvailable;
    private Integer yearsOfExperience;
    private String certificates;
    private String serviceAreas;
    private String serviceCases;
    private String qualificationStatus;

    public WorkerEntity() {
    }

    public WorkerEntity(Long userId, String name, String roleLabel, Double rating, Integer completedOrders, Integer hourlyPrice,
                        String city, String intro, String tags, String nextAvailable, Integer yearsOfExperience,
                        String certificates, String serviceAreas, String serviceCases, String qualificationStatus) {
        this.userId = userId;
        this.name = name;
        this.roleLabel = roleLabel;
        this.rating = rating;
        this.completedOrders = completedOrders;
        this.hourlyPrice = hourlyPrice;
        this.city = city;
        this.intro = intro;
        this.tags = tags;
        this.nextAvailable = nextAvailable;
        this.yearsOfExperience = yearsOfExperience;
        this.certificates = certificates;
        this.serviceAreas = serviceAreas;
        this.serviceCases = serviceCases;
        this.qualificationStatus = qualificationStatus;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public void setRoleLabel(String roleLabel) {
        this.roleLabel = roleLabel;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
    }

    public Integer getHourlyPrice() {
        return hourlyPrice;
    }

    public void setHourlyPrice(Integer hourlyPrice) {
        this.hourlyPrice = hourlyPrice;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNextAvailable() {
        return nextAvailable;
    }

    public void setNextAvailable(String nextAvailable) {
        this.nextAvailable = nextAvailable;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getCertificates() {
        return certificates;
    }

    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }

    public String getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(String serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public String getServiceCases() {
        return serviceCases;
    }

    public void setServiceCases(String serviceCases) {
        this.serviceCases = serviceCases;
    }

    public String getQualificationStatus() {
        return qualificationStatus;
    }

    public void setQualificationStatus(String qualificationStatus) {
        this.qualificationStatus = qualificationStatus;
    }
}
