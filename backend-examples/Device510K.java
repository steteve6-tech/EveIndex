package com.example.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 510K设备数据实体类
 */
@Entity
@Table(name = "device510k")
public class Device510K {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "applicant")
    private String applicant;

    @Column(name = "date_received")
    private LocalDateTime dateReceived;

    @Column(name = "country")
    private String country;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "matched_fields", columnDefinition = "TEXT")
    private String matchedFields;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 构造函数
    public Device510K() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public LocalDateTime getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(LocalDateTime dateReceived) {
        this.dateReceived = dateReceived;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(String matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public String getMatchedFields() {
        return matchedFields;
    }

    public void setMatchedFields(String matchedFields) {
        this.matchedFields = matchedFields;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
