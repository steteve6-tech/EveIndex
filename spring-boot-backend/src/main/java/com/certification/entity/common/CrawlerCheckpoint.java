package com.certification.entity.common;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 爬虫断点续传实体类
 * 用于记录爬虫的爬取进度，支持断点续传功能
 */
@Entity
@Table(name = "t_crawler_checkpoint")
@Data
public class CrawlerCheckpoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 爬虫类型标识
     */
    @Column(name = "crawler_type", nullable = false, length = 100)
    private String crawlerType;
    
    /**
     * 搜索条件（用于标识不同的爬取任务）
     */
    @Column(name = "search_term", length = 500)
    private String searchTerm;
    
    /**
     * 时间范围开始日期
     */
    @Column(name = "date_from", length = 20)
    private String dateFrom;
    
    /**
     * 时间范围结束日期
     */
    @Column(name = "date_to", length = 20)
    private String dateTo;
    
    /**
     * 当前爬取的偏移量（skip值）
     */
    @Column(name = "current_skip", nullable = false)
    private Integer currentSkip = 0;
    
    /**
     * 已爬取的总记录数
     */
    @Column(name = "total_fetched", nullable = false)
    private Integer totalFetched = 0;
    
    /**
     * 目标总记录数（null表示爬取所有数据）
     */
    @Column(name = "target_total")
    private Integer targetTotal;
    
    /**
     * 批次大小
     */
    @Column(name = "batch_size", nullable = false)
    private Integer batchSize;
    
    /**
     * 爬取状态：RUNNING, COMPLETED, FAILED, PAUSED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CrawlerStatus status = CrawlerStatus.RUNNING;
    
    /**
     * 最后更新时间
     */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();
    
    /**
     * 错误信息（如果爬取失败）
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    /**
     * 爬取状态枚举
     */
    public enum CrawlerStatus {
        RUNNING,    // 正在运行
        COMPLETED,  // 已完成
        FAILED,     // 失败
        PAUSED      // 暂停
    }
    
    /**
     * 生成唯一标识符
     */
    public String generateKey() {
        return String.format("%s_%s_%s_%s", 
            crawlerType, 
            searchTerm != null ? searchTerm.hashCode() : "null",
            dateFrom != null ? dateFrom : "null",
            dateTo != null ? dateTo : "null");
    }
    
    /**
     * 更新进度
     */
    public void updateProgress(int newSkip, int newTotalFetched) {
        this.currentSkip = newSkip;
        this.totalFetched = newTotalFetched;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 标记为完成
     */
    public void markCompleted() {
        this.status = CrawlerStatus.COMPLETED;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 标记为失败
     */
    public void markFailed(String errorMessage) {
        this.status = CrawlerStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastUpdated = LocalDateTime.now();
    }
    
    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return status == CrawlerStatus.COMPLETED || 
               (targetTotal != null && totalFetched >= targetTotal);
    }
    
    /**
     * 检查是否可以继续爬取
     */
    public boolean canContinue() {
        return status == CrawlerStatus.RUNNING && !isCompleted();
    }
}
