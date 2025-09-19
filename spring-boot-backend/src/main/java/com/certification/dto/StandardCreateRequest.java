package com.certification.dto;

import com.certification.entity.common.Standard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 标准创建请求DTO
 */
@Data
@Schema(description = "标准创建请求")
public class StandardCreateRequest {

    @Schema(description = "标准编号", example = "EN 300 328", required = true)
    private String standardNumber;

    @Schema(description = "标准版本", example = "v2.2.2")
    private String version;

    @Schema(description = "标准标题", required = true)
    private String title;

    @Schema(description = "标准描述")
    private String description;

    @Schema(description = "发布日期", example = "2023-01-01")
    private String publishedDate;

    @Schema(description = "生效日期", example = "2023-06-01")
    private String effectiveDate;

    @Schema(description = "下载链接")
    private String downloadUrl;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "风险等级", example = "MEDIUM")
    private Standard.RiskLevel riskLevel;

    @Schema(description = "监管影响", example = "HIGH")
    private Standard.RegulatoryImpact regulatoryImpact;

    @Schema(description = "标准状态", example = "ACTIVE")
    private Standard.StandardStatus standardStatus;

    @Schema(description = "主要国家/地区", example = "US")
    private String country;

    @Schema(description = "适用国家列表", example = "[\"US\",\"CN\",\"EU\"]")
    private List<String> countries;

    @Schema(description = "适用范围")
    private String scope;

    @Schema(description = "产品类型")
    private String productTypes;

    @Schema(description = "频率范围")
    private String frequencyBands;

    @Schema(description = "功率限制")
    private String powerLimits;

    @Schema(description = "测试方法")
    private String testMethods;

    @Schema(description = "合规截止日期", example = "2024-12-31")
    private LocalDate complianceDeadline;

    @Schema(description = "过渡期结束", example = "2024-06-30")
    private String transitionEnd;

    @Schema(description = "风险评分", example = "7.5")
    private Double riskScore;

    @Schema(description = "匹配的产品档案")
    private String matchedProfiles;

    @Schema(description = "原始摘要")
    private String rawExcerpt;

    @Schema(description = "是否监控", example = "true")
    private Boolean isMonitored;

    @Schema(description = "数据来源URL", example = "https://example.com/source")
    private String sourceUrl;
}
