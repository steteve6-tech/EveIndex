package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证标准实体类
 * 对应数据库表：t_standard
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_standard")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "认证标准实体")
public class Standard {
    
    @Schema(description = "标准ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "标准编号", example = "EN 300 328")
    @Column(name = "standard_number")
    private String standardNumber;
    
    @Schema(description = "标准版本", example = "v2.2.2")
    @Column(name = "version")
    private String version;
    
    @Schema(description = "标准标题")
    @Column(name = "title")
    private String title;
    
    @Schema(description = "标准描述")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Schema(description = "发布日期", example = "2023-01-01")
    @Column(name = "published_date")
    private String publishedDate;
    
    @Schema(description = "生效日期", example = "2023-06-01")
    @Column(name = "effective_date")
    private String effectiveDate;
    
    @Schema(description = "下载链接")
    @Column(name = "download_url")
    private String downloadUrl;
    
    @Schema(description = "关键词")
    @Column(name = "keywords")
    private String keywords;
    
    @Schema(description = "风险等级", example = "MEDIUM")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;
    
    @Schema(description = "监管影响", example = "HIGH")
    @Enumerated(EnumType.STRING)
    @Column(name = "regulatory_impact")
    private RegulatoryImpact regulatoryImpact;
    
    @Schema(description = "标准状态", example = "ACTIVE")
    @Enumerated(EnumType.STRING)
    @Column(name = "standard_status")
    private StandardStatus standardStatus;
    
    @Schema(description = "主要国家/地区", example = "US")
    @Column(name = "country")
    private String country;
    
    @Schema(description = "适用国家列表（JSON格式）", example = "[\"US\",\"CN\",\"EU\"]")
    @Column(name = "countries", columnDefinition = "JSON")
    private String countries;
    
    @Schema(description = "适用范围")
    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;
    
    @Schema(description = "产品类型")
    @Column(name = "product_types")
    private String productTypes;
    
    @Schema(description = "频率范围")
    @Column(name = "frequency_bands")
    private String frequencyBands;
    
    @Schema(description = "功率限制")
    @Column(name = "power_limits")
    private String powerLimits;
    
    @Schema(description = "测试方法")
    @Column(name = "test_methods", columnDefinition = "TEXT")
    private String testMethods;
    
    @Schema(description = "合规截止日期", example = "2024-12-31")
    @Column(name = "compliance_deadline")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate complianceDeadline;
    
    @Schema(description = "过渡期结束", example = "2024-06-30")
    @Column(name = "transition_end")
    private String transitionEnd;
    
    @Schema(description = "风险评分", example = "7.5")
    @Column(name = "risk_score")
    private Double riskScore;
    
    @Schema(description = "匹配的产品档案")
    @Column(name = "matched_profiles")
    private String matchedProfiles;
    
    @Schema(description = "原始摘要")
    @Column(name = "raw_excerpt", columnDefinition = "TEXT")
    private String rawExcerpt;
    
    @Schema(description = "是否监控", example = "true")
    @Column(name = "is_monitored")
    private Boolean isMonitored;
    
    @Schema(description = "数据来源URL", example = "https://example.com/source")
    @Column(name = "source_url")
    private String sourceUrl;
    
    @Schema(description = "创建时间")
    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Schema(description = "逻辑删除标记")
    @Column(name = "deleted")
    private Integer deleted = 0;
    
    /**
     * 获取国家列表
     * @return 国家列表
     */
    public List<String> getCountryList() {
        if (countries == null || countries.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(countries, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 设置国家列表
     * @param countryList 国家列表
     */
    public void setCountryList(List<String> countryList) {
        if (countryList == null || countryList.isEmpty()) {
            this.countries = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.countries = mapper.writeValueAsString(countryList);
        } catch (Exception e) {
            this.countries = null;
        }
    }
    
    /**
     * 检查是否适用于指定国家
     * @param countryCode 国家代码
     * @return 是否适用
     */
    public boolean isApplicableToCountry(String countryCode) {
        if (countryCode == null) {
            return false;
        }
        List<String> countryList = getCountryList();
        return countryList.contains(countryCode.toUpperCase());
    }
    
    /**
     * 添加适用国家
     * @param countryCode 国家代码
     */
    public void addCountry(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return;
        }
        List<String> countryList = getCountryList();
        if (!countryList.contains(countryCode.toUpperCase())) {
            countryList.add(countryCode.toUpperCase());
            setCountryList(countryList);
        }
    }
    
    /**
     * 移除适用国家
     * @param countryCode 国家代码
     */
    public void removeCountry(String countryCode) {
        if (countryCode == null) {
            return;
        }
        List<String> countryList = getCountryList();
        countryList.removeIf(country -> country.equalsIgnoreCase(countryCode));
        setCountryList(countryList);
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 监管影响枚举
     */
    public enum RegulatoryImpact {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("关键");
        
        private final String description;
        
        RegulatoryImpact(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 标准状态枚举
     */
    public enum StandardStatus {
        DRAFT("草案"),
        ACTIVE("生效"),
        SUPERSEDED("已替代"),
        WITHDRAWN("已撤销"),
        UNDER_REVISION("修订中");
        
        private final String description;
        
        StandardStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
