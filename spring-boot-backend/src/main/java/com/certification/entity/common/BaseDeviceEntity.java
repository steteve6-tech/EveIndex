package com.certification.entity.common;

import com.certification.entity.common.CertNewsData.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 设备数据基础实体类
 * 所有设备相关实体(510K, Recall, Event, Registration, Guidance, CustomsCase)的统一基类
 *
 * 提取的通用字段:
 * - 风险等级 (riskLevel)
 * - 关键词 (keywords)
 * - 数据来源国家 (jdCountry)
 * - 数据源 (dataSource)
 * - 备注信息 (remark)
 * - 爬取时间 (crawlTime)
 * - 数据状态 (dataStatus)
 * - 创建时间 (createdTime)
 * - 更新时间 (updatedTime)
 *
 * @author System
 * @since 2025-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备数据基础实体")
public abstract class BaseDeviceEntity {

    /**
     * 风险等级
     * 标识数据的风险等级 (HIGH, MEDIUM, LOW, NONE)
     */
    @Schema(description = "风险等级", example = "MEDIUM")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    /**
     * 关键词
     * 存储与该数据相关的关键词，JSON格式或文本格式
     */
    @Schema(description = "关键词（JSON格式或文本格式）")
    @Lob
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    /**
     * 数据来源国家
     * 标识数据所属国家/地区代码 (US, EU, CN, KR等)
     */
    @Schema(description = "数据来源国家/地区", example = "US")
    @Column(name = "jd_country", length = 20, nullable = false)
    private String jdCountry;

    /**
     * 数据源
     * 数据的具体来源 (如 FDA, EUDAMED, NMPA等)
     */
    @Schema(description = "数据源", example = "FDA")
    @Column(name = "data_source", length = 100)
    private String dataSource;

    /**
     * 备注信息
     * 存储AI判断原因、人工审核意见等附加信息
     */
    @Schema(description = "备注信息（AI判断原因、人工审核意见等）")
    @Lob
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 爬取时间
     * 数据被爬虫抓取的时间戳
     */
    @Schema(description = "爬取时间（数据抓取时的时间戳）")
    @Column(name = "crawl_time")
    private LocalDateTime crawlTime;

    /**
     * 数据状态
     * 标识数据的当前状态 (ACTIVE, INACTIVE, DELETED等)
     */
    @Schema(description = "数据状态", example = "ACTIVE")
    @Column(name = "data_status", length = 20)
    private String dataStatus = "ACTIVE";

    /**
     * 创建时间
     * 数据记录在数据库中的创建时间
     */
    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 数据记录最后更新的时间
     */
    @Schema(description = "更新时间")
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 实体生命周期回调 - 持久化前
     * 设置默认值
     */
    @PrePersist
    protected void onCreate() {
        if (crawlTime == null) {
            crawlTime = LocalDateTime.now();
        }
        if (dataStatus == null || dataStatus.isBlank()) {
            dataStatus = "ACTIVE";
        }
    }

    /**
     * 实体生命周期回调 - 更新前
     */
    @PreUpdate
    protected void onUpdate() {
        // 可以在这里添加更新前的逻辑
    }

    /**
     * 判断数据是否为高风险
     */
    public boolean isHighRisk() {
        return RiskLevel.HIGH.equals(this.riskLevel);
    }

    /**
     * 判断数据是否激活
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.dataStatus);
    }

    /**
     * 获取实体类型名称
     * 子类可以重写此方法返回具体的类型名称
     */
    public String getEntityType() {
        return this.getClass().getSimpleName();
    }
}
