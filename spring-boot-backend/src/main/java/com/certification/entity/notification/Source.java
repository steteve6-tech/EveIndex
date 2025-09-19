package com.certification.entity.notification;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 数据源实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_source")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "数据源实体")
public class Source {
    
    @Schema(description = "数据源ID", example = "1")
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "数据源名称", example = "SGS")
    @Column(name = "source_name")
    private String sourceName;
    
    @Schema(description = "数据源类型", example = "CERTIFICATION")
    @Column(name = "source_type")
    private SourceType sourceType;
    
    @Schema(description = "数据源URL", example = "https://www.sgs.com")
    @Column(name = "source_url")
    private String sourceUrl;
    
    @Schema(description = "数据源描述")
    @Column(name = "description")
    private String description;
    
    @Schema(description = "更新频率", example = "DAILY")
    @Column(name = "update_frequency")
    private UpdateFrequency updateFrequency;
    
    @Schema(description = "是否激活", example = "true")
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Schema(description = "最后爬取时间")
    @Column(name = "last_crawl_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCrawlTime;
    
    @Schema(description = "爬取状态", example = "IDLE")
    @Column(name = "crawl_status")
    private String crawlStatus;
    
    @Schema(description = "错误次数", example = "0")
    @Column(name = "error_count")
    private Integer errorCount;
    
    @Schema(description = "最后错误信息")
    @Column(name = "last_error_message")
    private String lastErrorMessage;
    
    @Schema(description = "配置信息(JSON)")
    @Column(name = "config_json")
    private String configJson;
    
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
     * 数据源类型枚举
     */
    public enum SourceType {
        CERTIFICATION("认证机构"),
        CUSTOMS("海关"),
        MEDICAL("医疗器械");
        
        private final String description;
        
        SourceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 更新频率枚举
     */
    public enum UpdateFrequency {
        HOURLY("每小时"),
        DAILY("每天"),
        WEEKLY("每周");
        
        private final String description;
        
        UpdateFrequency(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
