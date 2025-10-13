package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * 设备事件报告实体类
 * 对应数据库表：t_device_event
 * 支持多种数据源：FDA设备不良事件、EU Safety Gate预警等
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_event")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备事件报告实体")
public class DeviceEventReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 核心业务标识 - 支持多种编号格式
    @Column(name = "report_number", length = 64, nullable = false, unique = true)
    private String reportNumber; // FDA: report_number, EU: alert_number


    @Column(name = "date_of_event")
    private LocalDate dateOfEvent;


    @Column(name = "date_received")
    private LocalDate dateReceived; // FDA: date_received, EU: publication_date


    @Column(name = "brand_name", length = 255)
    private String brandName; // FDA: device.manufacturer_name, EU: brand


    @Column(name = "generic_name", length = 255)
    private String genericName; // FDA: device.generic_name, EU: product

    @Column(name = "manufacturer_name", length = 255)
    private String manufacturerName;


    @Column(name = "device_class", length = 10)
    private String deviceClass;


    // 新增：风险等级
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    // 新增：关键词数组
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // TEXT格式存储关键词（JSON字符串或分号分隔）




    @Column(name = "data_source", length = 50)
    private String dataSource;

    // 新增：用于判定数据所属国家（如 US/CN/EU 等）
    @Column(name = "jd_country", length = 20)
    private String jdCountry;

    // 爬取时间
    @Column(name = "crawl_time")
    @Schema(description = "爬取时间（数据抓取时的时间戳）")
    private LocalDateTime crawlTime;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    // 备注信息（AI判断原因、人工审核意见等）
    @Lob
    @Column(name = "remark", columnDefinition = "TEXT")
    @Schema(description = "备注信息（AI判断原因、人工审核意见等）")
    private String remark;
}
