package com.certification.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * 定时爬取配置实体类
 * 用于管理各个爬虫模块的定时任务配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_scheduled_crawler_config")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "定时爬取配置实体")
public class ScheduledCrawlerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "配置ID")
    private Long id;

    /**
     * 爬虫模块名称
     * 如：certnewsdata, device510k, deviceeventreport等
     */
    @Schema(description = "爬虫模块名称", example = "certnewsdata")
    @Column(name = "module_name", length = 50, nullable = false)
    private String moduleName;

    /**
     * 爬虫名称
     * 如：SGS, UL, Beice, US_510K, EU_CustomCase等
     */
    @Schema(description = "爬虫名称", example = "SGS")
    @Column(name = "crawler_name", length = 50, nullable = false)
    private String crawlerName;

    /**
     * 国家/地区代码
     * 如：US, EU, CN等
     */
    @Schema(description = "国家/地区代码", example = "US")
    @Column(name = "country_code", length = 10)
    private String countryCode;

    /**
     * 是否启用定时任务
     */
    @Schema(description = "是否启用定时任务")
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * Cron表达式
     * 如：0 0 2 * * ? (每天凌晨2点)
     */
    @Schema(description = "Cron表达式", example = "0 0 2 * * ?")
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述", example = "每天凌晨2点执行SGS爬虫")
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 爬取参数配置（JSON格式）
     * 包含批次大小、最大记录数等参数
     */
    @Schema(description = "爬取参数配置")
    @Column(name = "crawl_params", columnDefinition = "TEXT")
    private String crawlParams;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    @Column(name = "last_execution_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastExecutionTime;

    /**
     * 下次执行时间
     */
    @Schema(description = "下次执行时间")
    @Column(name = "next_execution_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextExecutionTime;

    /**
     * 最后执行状态
     */
    @Schema(description = "最后执行状态")
    @Column(name = "last_execution_status", length = 20)
    private String lastExecutionStatus;

    /**
     * 最后执行结果信息
     */
    @Schema(description = "最后执行结果信息")
    @Column(name = "last_execution_result", columnDefinition = "TEXT")
    private String lastExecutionResult;

    /**
     * 执行次数统计
     */
    @Schema(description = "执行次数统计")
    @Column(name = "execution_count")
    private Long executionCount = 0L;

    /**
     * 成功执行次数统计
     */
    @Schema(description = "成功执行次数统计")
    @Column(name = "success_count")
    private Long successCount = 0L;

    /**
     * 失败执行次数统计
     */
    @Schema(description = "失败执行次数统计")
    @Column(name = "failure_count")
    private Long failureCount = 0L;

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
     * 执行状态常量
     */
    public static class ExecutionStatus {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String RUNNING = "RUNNING";
        public static final String PENDING = "PENDING";
        public static final String CANCELLED = "CANCELLED";
    }
}
