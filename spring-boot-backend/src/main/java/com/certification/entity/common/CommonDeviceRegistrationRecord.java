package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * 设备注册记录共有数据实体类
 * 只包含US FDA和EU EUDAMED两个数据源都有的字段
 * 对应数据库表：t_common_device_registration
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_common_device_registration")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备注册记录共有数据实体")
public class CommonDeviceRegistrationRecord {

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
    @Column(name = "registration_number", length = 100, nullable = false)
    @Schema(description = "主要标识符")
    private String registrationNumber;

    @Column(name = "registration_id", length = 255)
    @Schema(description = "注册ID")
    private String registrationId;

    @Column(name = "fei_number", length = 100)
    @Schema(description = "次要标识符")
    private String feiNumber;

    // ========== 制造商信息（两个数据源都有） ==========
    @Column(name = "manufacturer_name", length = 255)
    @Schema(description = "制造商名称")
    private String manufacturerName;

    @Column(name = "manufacturer_id", length = 100)
    @Schema(description = "制造商ID")
    private String manufacturerId;

    @Column(name = "manufacturer_country_code", length = 10)
    @Schema(description = "制造商国家代码")
    private String manufacturerCountryCode;

    @Column(name = "manufacturer_full_address", length = 512)
    @Schema(description = "制造商完整地址")
    private String manufacturerFullAddress;

    // ========== 设备信息（两个数据源都有） ==========
    @Column(name = "device_name", length = 255)
    @Schema(description = "设备名称")
    private String deviceName;

    @Column(name = "proprietary_name", length = 255)
    @Schema(description = "专有名称/商标名称")
    private String proprietaryName;

    @Column(name = "device_class", length = 50)
    @Schema(description = "设备类别")
    private String deviceClass;

    @Column(name = "risk_class", length = 50)
    @Schema(description = "风险等级")
    private String riskClass;

    // ========== 状态信息（两个数据源都有） ==========
    @Column(name = "status_code", length = 32)
    @Schema(description = "状态码")
    private String statusCode;

    @Column(name = "reg_expiry_year", length = 8)
    @Schema(description = "注册到期年份")
    private String regExpiryYear;

    // ========== 监管信息（两个数据源都有） ==========
    @Column(name = "regulation_number", length = 100)
    @Schema(description = "法规编号")
    private String regulationNumber;

    @Column(name = "product_code", length = 50)
    @Schema(description = "产品代码")
    private String productCode;

    // ========== 扩展信息（JSON格式存储，两个数据源都有） ==========
    @Lob
    @Column(name = "product_codes", columnDefinition = "TEXT")
    @Schema(description = "产品代码列表（JSON数组）")
    private String productCodes;

    @Lob
    @Column(name = "device_names", columnDefinition = "TEXT")
    @Schema(description = "设备名称列表（JSON数组）")
    private String deviceNames;

    @Lob
    @Column(name = "device_classes", columnDefinition = "TEXT")
    @Schema(description = "设备类别列表（JSON数组）")
    private String deviceClasses;

    @Lob
    @Column(name = "regulation_numbers", columnDefinition = "TEXT")
    @Schema(description = "法规编号列表（JSON数组）")
    private String regulationNumbers;

    @Lob
    @Column(name = "proprietary_names", columnDefinition = "TEXT")
    @Schema(description = "专有名称列表（JSON数组）")
    private String proprietaryNames;

    // ========== 分析字段（通用） ==========
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Schema(description = "风险等级评估")
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Column(name = "keywords", columnDefinition = "TEXT")
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
