package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import com.certification.entity.common.CrawlerData.RiskLevel;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_recall")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "医疗器械召回记录（适配D_recall爬虫数据结构）")
public class DeviceRecallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    // 核心标识字段（D_recall爬虫使用）
    @Column(name = "cfres_id", length = 100)
    @Schema(description = "召回事件ID（从FDA网站URL提取）")
    private String cfresId;

    // 召回基本信息（D_recall爬虫提供）
    @Column(name = "product_description", columnDefinition = "TEXT")
    @Schema(description = "产品描述")
    private String productDescription;

    @Column(name = "recalling_firm", length = 255)
    @Schema(description = "召回公司")
    private String recallingFirm;

    @Column(name = "recall_status", length = 100)
    @Schema(description = "召回等级（CLASS I/II/III）")
    private String recallStatus;

    @Column(name = "event_date_posted")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "召回发布日期")
    private LocalDate eventDatePosted;

    // 设备信息（D_recall爬虫设置）
    @Column(name = "device_name", length = 255)
    @Schema(description = "设备名称（复用产品描述）")
    private String deviceName;

    @Column(name = "product_code", length = 50)
    @Schema(description = "产品代码（D_recall设置为空）")
    private String productCode;

    // 风险等级和关键词（系统计算）
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    @Schema(description = "风险等级（根据召回等级计算）")
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    @Column(name = "keywords", columnDefinition = "TEXT")
    @Schema(description = "关键词（JSON格式）")
    private String keywords;

    // 数据源信息（D_recall爬虫设置）
    @Column(name = "data_source", length = 50)
    @Schema(description = "数据源")
    private String dataSource;

    @Column(name = "country_code", nullable = false, length = 20)
    @Schema(description = "国家代码")
    private String countryCode;

    @Column(name = "jd_country", length = 20)
    @Schema(description = "数据适用国家")
    private String jdCountry;
}
