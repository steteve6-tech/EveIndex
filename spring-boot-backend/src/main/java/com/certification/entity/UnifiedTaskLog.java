package com.certification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 统一任务执行日志实体
 * 记录所有爬虫任务的执行历史和结果
 */
@Entity
@Table(name = "t_unified_task_log")
@Data
@EqualsAndHashCode(callSuper = false)
public class UnifiedTaskLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 执行批次号
     */
    @Column(name = "batch_no", length = 50)
    private String batchNo;
    
    /**
     * 任务名称
     */
    @Column(name = "task_name", length = 100)
    private String taskName;
    
    /**
     * 爬虫名称
     */
    @Column(name = "crawler_name", length = 50)
    private String crawlerName;
    
    /**
     * 国家代码
     */
    @Column(name = "country_code", length = 10)
    private String countryCode;
    
    /**
     * 执行状态
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行时长(秒)
     */
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    /**
     * 爬取数量
     */
    @Column(name = "crawled_count")
    private Integer crawledCount = 0;
    
    /**
     * 保存数量
     */
    @Column(name = "saved_count")
    private Integer savedCount = 0;
    
    /**
     * 跳过数量
     */
    @Column(name = "skipped_count")
    private Integer skippedCount = 0;
    
    /**
     * 失败数量
     */
    @Column(name = "failed_count")
    private Integer failedCount = 0;
    
    /**
     * 使用的关键词
     */
    @Column(name = "keywords_used", columnDefinition = "TEXT")
    private String keywordsUsed;
    
    /**
     * 爬取参数
     */
    @Column(name = "crawl_params", columnDefinition = "TEXT")
    private String crawlParams;
    
    /**
     * 执行结果
     */
    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 是否手动触发
     */
    @Column(name = "is_manual")
    private Boolean isManual = false;
    
    /**
     * 触发者
     */
    @Column(name = "triggered_by", length = 50)
    private String triggeredBy;
    
    /**
     * 执行服务器
     */
    @Column(name = "execution_server", length = 100)
    private String executionServer;
    
    /**
     * 执行IP
     */
    @Column(name = "execution_ip", length = 50)
    private String executionIp;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
    
    /**
     * 计算执行时长
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        }
    }
    
    /**
     * 设置执行结果
     */
    public void setExecutionResult(String status, String resultMessage, String errorMessage) {
        this.status = status;
        this.resultMessage = resultMessage;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        calculateDuration();
    }
    
    /**
     * 设置统计数据
     */
    public void setStatistics(int crawled, int saved, int skipped, int failed) {
        this.crawledCount = crawled;
        this.savedCount = saved;
        this.skippedCount = skipped;
        this.failedCount = failed;
    }
    
    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (crawledCount == null || crawledCount == 0) {
            return 0.0;
        }
        if (savedCount == null) {
            return 0.0;
        }
        return (double) savedCount / crawledCount * 100;
    }
}
