package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 爬虫数据实体类
 * 用于存储爬虫爬取到的原始数据
 * 对应数据库表：t_crawler_data
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_crawler_data")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "爬虫数据实体")
public class CertNewsData {

    @Schema(description = "数据ID", example = "b7e6c2e2-8c2a-4e2a-9b1a-2e4e2c2a8c2a")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    /**
     * 数据源名称（如：UL Solutions, SGS等）
     */
    @Schema(description = "数据源名称", example = "SGS")
    @Column(name = "source_name")
    private String sourceName;
    
    /**
     * 标题
     */
    @Column(name = "title")
    private String title;
    
    /**
     * URL链接
     */
    @Column(name = "url")
    private String url;
    
    /**
     * 内容摘要
     */
    @Column(name = "summary")
    private String summary;
    
    /**
     * 详细内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 国家/地区
     */
    @Column(name = "country")
    private String country;
    
    /**
     * 类型/分类
     */
    @Column(name = "type")
    private String type;
    
    /**
     * 适用商品/产品
     */
    @Schema(description = "适用商品/产品", example = "洗衣機和脫水機")
    @Column(name = "product")
    private String product;
    
    /**
     * 发布时间
     */
    @Column(name = "publish_date")
    private String publishDate;
    
    /**
     * 发布时间列表（JSON格式存储）
     */
    @Schema(description = "发布时间列表", example = "[\"2025-07-28\", \"2025-08-01\"]")
    @Column(name = "release_date", columnDefinition = "JSON")
    private List<String> releaseDate;
    
    /**
     * 执行时间列表（JSON格式存储）
     */
    @Schema(description = "执行时间列表", example = "[\"2025-08-25 10:30:00\", \"2025-08-25 15:45:00\"]")
    @Column(name = "execution_date", columnDefinition = "JSON")
    private List<String> executionDate;
    
    /**
     * 爬取时间
     */
    @Column(name = "crawl_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crawlTime;
    
    /**
     * 数据状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DataStatus status;
    
    /**
     * 是否已处理
     */
    @Column(name = "is_processed")
    private Boolean isProcessed;
    
    /**
     * 处理时间
     */
    @Column(name = "processed_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedTime;
    
    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    /**
     * 是否相关
     * 标识这条信息是否与当前业务相关
     */
    @Schema(description = "是否相关", example = "true")
    @Column(name = "related")
    private Boolean related;
    
    /**
     * 匹配的关键词
     * 存储匹配到的关键词列表，用逗号分隔
     */
    @Schema(description = "匹配的关键词", example = "FCC ID,Part 15B")
    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;
    
    /**
     * 风险等级
     * 标识数据的风险等级
     */
    @Schema(description = "风险等级", example = "HIGH")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel = RiskLevel.NONE;
    
    /**
     * 风险说明
     * 风险等级的详细说明
     */
    @Schema(description = "风险说明", example = "该标准涉及产品安全要求")
    @Column(name = "risk_description", columnDefinition = "TEXT")
    private String riskDescription;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除字段
     */
    @Column(name = "deleted")
    private Integer deleted = 0;
    
    /**
     * 数据状态枚举
     */
    public enum DataStatus {
        NEW("新建"),
        PROCESSING("处理中"),
        PROCESSED("已处理"),
        ERROR("错误"),
        DUPLICATE("重复");
        
        private final String description;
        
        DataStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        HIGH("高风险"),
        MEDIUM("中风险"),
        LOW("低风险"),
        NONE("无风险");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
