package com.certification.entity.newcommon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FDA 510K记录实体类
 * 用于存储从FDA网站爬取的510K设备信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "d_510k_records", indexes = {
    @Index(name = "idx_device_name", columnList = "device_name"),
    @Index(name = "idx_applicant", columnList = "applicant"),
    @Index(name = "idx_k_number", columnList = "k_number"),
    @Index(name = "idx_decision_date", columnList = "decision_date"),
    @Index(name = "idx_crawl_time", columnList = "crawl_time")
})
public class D_510KRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备名称
     */
    @Column(name = "device_name", length = 500, nullable = false)
    private String deviceName;

    /**
     * 申请人/公司名称
     */
    @Column(name = "applicant", length = 300)
    private String applicant;

    /**
     * 510(K)编号
     */
    @Column(name = "k_number", length = 50, unique = true)
    private String kNumber;

    /**
     * 决策日期
     */
    @Column(name = "decision_date")
    private LocalDate decisionDate;

    /**
     * 设备详情链接
     */
    @Column(name = "device_url", length = 1000)
    private String deviceUrl;

    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 100)
    private String dataSource;

    /**
     * 国家代码
     */
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /**
     * 来源国家
     */
    @Column(name = "source_country", length = 50)
    private String sourceCountry;

    /**
     * 爬取时间
     */
    @Column(name = "crawl_time")
    private LocalDateTime crawlTime;

    /**
     * 数据状态
     */
    @Column(name = "data_status", length = 20)
    private String dataStatus;

    /**
     * 原始决策日期字符串（用于调试）
     */
    @Column(name = "decision_date_str", length = 20)
    private String decisionDateStr;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Column(name = "remarks", length = 1000)
    private String remarks;
}
