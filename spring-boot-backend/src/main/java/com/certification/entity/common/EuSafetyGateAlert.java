package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 欧盟安全门预警系统实体类
 * 对应Safety Gate Alert数据
 */
@Data
@EqualsAndHashCode
@Entity
@Table(name = "eu_safety_gate_alerts")
@Schema(description = "欧盟安全门预警系统数据")
public class EuSafetyGateAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    @Column(name = "alert_number", unique = true, nullable = false, length = 100)
    @Schema(description = "预警编号")
    private String alertNumber;

    @Column(name = "publication_date")
    @Schema(description = "发布日期")
    private LocalDate publicationDate;

    @Column(name = "product", length = 500)
    @Schema(description = "产品名称")
    private String product;

    @Column(name = "product_description", columnDefinition = "TEXT")
    @Schema(description = "产品描述")
    private String productDescription;

    @Column(name = "product_model", length = 200)
    @Schema(description = "产品型号")
    private String productModel;

    @Column(name = "brand", length = 200)
    @Schema(description = "主要品牌")
    private String brand;

    @Column(name = "category", length = 200)
    @Schema(description = "产品类别")
    private String category;

    @Column(name = "risk", length = 200)
    @Schema(description = "主要风险")
    private String risk;

    @Column(name = "risk_type", length = 200)
    @Schema(description = "风险类型")
    private String riskType;

    @Column(name = "country", length = 100)
    @Schema(description = "产品来源国家")
    private String country;

    @Column(name = "notifying_country", length = 100)
    @Schema(description = "通知国家")
    private String notifyingCountry;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "预警描述")
    private String description;

    @Column(name = "measures", columnDefinition = "TEXT")
    @Schema(description = "采取的措施")
    private String measures;

    @Column(name = "url", length = 1000)
    @Schema(description = "详情链接")
    private String url;

    @Column(name = "crawl_time")
    @Schema(description = "爬取时间")
    private LocalDateTime crawlTime;

    @Column(name = "data_source", length = 100)
    @Schema(description = "数据源")
    private String dataSource;

    @Column(name = "jd_country", length = 10)
    @Schema(description = "国家代码")
    private String jdCountry;

    @Column(name = "keywords", columnDefinition = "TEXT")
    @Schema(description = "关键词")
    private String keywords;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    @Schema(description = "风险等级")
    private com.certification.entity.common.CrawlerData.RiskLevel riskLevel;

    @Column(name = "create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (dataSource == null) {
            dataSource = "Safety Gate Alert";
        }
        if (jdCountry == null) {
            jdCountry = "EU";
        }
        if (crawlTime == null) {
            crawlTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
