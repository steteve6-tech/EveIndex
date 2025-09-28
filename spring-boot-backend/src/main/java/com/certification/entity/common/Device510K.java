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
 * 设备510K记录实体类
 * 对应数据库表：t_device_510k
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_510k")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备510K记录实体")
public class Device510K {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 设备类别
    @Column(name = "device_class", length = 10)
    private String deviceClass;

    // 风险等级
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    // 关键词数组（JSON格式存储）
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    // 品牌名称
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    // 申请人
    @Column(name = "applicant", length = 255)
    private String applicant;

    // 国家代码
    @Column(name = "country_code", length = 2)
    private String countryCode;

    // 接收日期
    @Column(name = "date_received")
    private LocalDate dateReceived;

    // 设备名称
    @Column(name = "device_name", length = 255)
    private String deviceName;

    // K号
    @Column(name = "k_number", length = 32, unique = true)
    private String kNumber;

    // 数据源
    @Column(name = "data_source", length = 50)
    private String dataSource;

    // 创建时间（自动生成）
    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    // 京东国家
    @Column(name = "jd_country", length = 20)
    private String jdCountry;

    // 爬取时间
    @Column(name = "crawl_time")
    private LocalDateTime crawlTime;

    // 数据状态
    @Column(name = "data_status", length = 20)
    private String dataStatus;
}