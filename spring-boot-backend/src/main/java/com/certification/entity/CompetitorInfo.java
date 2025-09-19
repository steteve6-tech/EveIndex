package com.certification.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 竞品信息实体类
 * 用于存储竞争对手的医疗器械认证信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "competitor_info", indexes = {
    @Index(name = "idx_device_name", columnList = "deviceName"),
    @Index(name = "idx_manufacturer_brand", columnList = "manufacturerBrand"),
    @Index(name = "idx_device_code", columnList = "deviceCode"),
    @Index(name = "idx_data_source", columnList = "dataSource"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_risk_level", columnList = "riskLevel"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class CompetitorInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备名称
     */
    @Column(name = "device_name", nullable = false, length = 500)
    private String deviceName;

    /**
     * 制造商/品牌名称
     */
    @Column(name = "manufacturer_brand", nullable = false, length = 200)
    private String manufacturerBrand;

    /**
     * 设备代码
     */
    @Column(name = "device_code", nullable = false, length = 100)
    private String deviceCode;

    /**
     * 设备使用范围
     */
    @Column(name = "usage_scope", columnDefinition = "TEXT")
    private String usageScope;

    /**
     * 设备描述
     */
    @Column(name = "device_description", columnDefinition = "TEXT")
    private String deviceDescription;

    /**
     * 数据来源
     */
    @Column(name = "data_source", nullable = false, length = 50)
    private String dataSource;

    /**
     * 认证类型
     */
    @Column(name = "certification_type", length = 100)
    private String certificationType;

    /**
     * 状态：active-活跃, inactive-非活跃, expired-已过期
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    /**
     * 风险等级：low-低风险, medium-中风险, high-高风险
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    /**
     * 认证日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "certification_date")
    private LocalDateTime certificationDate;

    /**
     * 有效期至
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 状态枚举
     */
    public enum Status {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        EXPIRED("已过期");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 数据来源枚举
     */
    public enum DataSource {
        FDA_510K("FDA 510K"),
        FDA_MAUDE("FDA MAUDE"),
        FDA_RECALL("FDA Recall"),
        FDA_REGISTRATION("FDA Registration"),
        CUSTOMS_CASE("海关案例"),
        GUIDANCE("指导文档"),
        MANUAL("手动录入");

        private final String description;

        DataSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
