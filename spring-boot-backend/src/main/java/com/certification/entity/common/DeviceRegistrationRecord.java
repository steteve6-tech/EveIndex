package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import com.certification.entity.common.CrawlerData.RiskLevel;

/**
 * 设备注册记录共有数据实体类
 * 只包含US FDA和EU EUDAMED两个数据源都有的字段
 * 对应数据库表：t_device_registration
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_registration")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备注册记录共有数据实体")
public class    DeviceRegistrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 数据源标识 ==========
    @Column(name = "data_source", length = 50, nullable = false)
    @Schema(description = "数据源", example = "US_FDA, EU_EUDAMED")
    private String dataSource;

    @Column(name = "jd_country", length = 20, nullable = false)
    @Schema(description = "数据源国家", example = "US, EU")
    private String jdCountry;

    // ========== 核心标识字段（两个数据源都有） ==========
    @Lob
    @Column(name = "registration_number", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "主要标识符（US: K_number+pma_number, EU: udi_di）")
    private String registrationNumber;

    @Lob
    @Column(name = "fei_number", columnDefinition = "TEXT")
    @Schema(description = "次要标识符（US: fei_number, EU: basic_udi_di）")
    private String feiNumber;

    // ========== 制造商信息（两个数据源都有） ==========
    @Lob
    @Column(name = "manufacturer_name", columnDefinition = "TEXT")
    @Schema(description = "制造商名称")
    private String manufacturerName;

    // ========== 设备信息（两个数据源都有） ==========
    @Lob
    @Column(name = "device_name", columnDefinition = "TEXT")
    @Schema(description = "设备名称")
    private String deviceName;

    @Lob
    @Column(name = "proprietary_name", columnDefinition = "LONGTEXT")
    @Schema(description = "专有名称/商标名称")
    private String proprietaryName;

    @Lob
    @Column(name = "device_class", columnDefinition = "TEXT")
    @Schema(description = "设备类别")
    private String deviceClass;

    @Lob
    @Column(name = "risk_class", columnDefinition = "TEXT")
    @Schema(description = "风险等级")
    private String riskClass;

    // ========== 状态信息（两个数据源都有） ==========
    @Column(name = "status_code", length = 100)
    @Schema(description = "状态码")
    private String statusCode;

    @Column(name = "created_date", length = 50)
    @Schema(description = "创建日期")
    private String createdDate;

    // ========== 分析字段（通用） ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Schema(description = "风险等级评估")
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Lob
    @Column(name = "keywords", columnDefinition = "LONGTEXT")
    @Schema(description = "关键词（JSON数组）")
    private String keywords;

    // ========== 元数据（两个数据源都有） ==========
    @Column(name = "crawl_time")
    @Schema(description = "爬取时间")
    private LocalDateTime crawlTime;

    @Column(name = "create_time", insertable = false, updatable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}