package com.certification.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 爬虫状态实体类
 * 用于记录每个爬虫的执行状态和最新数据信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_crawler_state")
public class CrawlerState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 爬虫名称（如：SGS, UL）
     */
    @Column(name = "crawler_name", nullable = false, length = 50)
    private String crawlerName;

    /**
     * 爬虫类型（如：certification, news）
     */
    @Column(name = "crawler_type", length = 50)
    private String crawlerType;

    /**
     * 最后爬取时间
     */
    @Column(name = "last_crawl_time")
    private LocalDateTime lastCrawlTime;

    /**
     * 最后爬取的数据ID或标识
     */
    @Column(name = "last_crawled_id", length = 255)
    private String lastCrawledId;

    /**
     * 最后爬取的数据标题
     */
    @Column(name = "last_crawled_title", length = 500)
    private String lastCrawledTitle;

    /**
     * 最后爬取的数据URL
     */
    @Column(name = "last_crawled_url", length = 1000)
    private String lastCrawledUrl;

    /**
     * 最后爬取的数据发布时间
     */
    @Column(name = "last_crawled_publish_time")
    private LocalDateTime lastCrawledPublishTime;

    /**
     * 最后爬取的数据数量
     */
    @Column(name = "last_crawled_count")
    private Integer lastCrawledCount;

    /**
     * 累计爬取总数
     */
    @Column(name = "total_crawled_count")
    private Long totalCrawledCount = 0L;

    /**
     * 爬虫状态（RUNNING, IDLE, ERROR, DISABLED）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CrawlerStatus status = CrawlerStatus.IDLE;

    /**
     * 最后错误信息
     */
    @Column(name = "last_error_message", length = 1000)
    private String lastErrorMessage;

    /**
     * 最后错误时间
     */
    @Column(name = "last_error_time")
    private LocalDateTime lastErrorTime;

    /**
     * 连续错误次数
     */
    @Column(name = "consecutive_error_count")
    private Integer consecutiveErrorCount = 0;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 备注信息
     */
    @Column(name = "remarks", length = 1000)
    private String remarks;

    /**
     * 爬虫状态枚举
     */
    public enum CrawlerStatus {
        RUNNING,    // 运行中
        IDLE,       // 空闲
        ERROR,      // 错误
        DISABLED    // 禁用
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.updatedTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 更新爬取成功状态
     */
    public void updateSuccessState(String lastCrawledId, String lastCrawledTitle, 
                                 String lastCrawledUrl, LocalDateTime lastCrawledPublishTime, 
                                 Integer crawledCount) {
        this.lastCrawlTime = LocalDateTime.now();
        this.lastCrawledId = lastCrawledId;
        this.lastCrawledTitle = lastCrawledTitle;
        this.lastCrawledUrl = lastCrawledUrl;
        this.lastCrawledPublishTime = lastCrawledPublishTime;
        this.lastCrawledCount = crawledCount;
        this.totalCrawledCount += crawledCount;
        this.status = CrawlerStatus.IDLE;
        this.consecutiveErrorCount = 0;
        this.lastErrorMessage = null;
        this.lastErrorTime = null;
    }

    /**
     * 更新错误状态
     */
    public void updateErrorState(String errorMessage) {
        this.status = CrawlerStatus.ERROR;
        this.lastErrorMessage = errorMessage;
        this.lastErrorTime = LocalDateTime.now();
        this.consecutiveErrorCount++;
    }

    /**
     * 更新运行状态
     */
    public void updateRunningState() {
        this.status = CrawlerStatus.RUNNING;
        this.lastCrawlTime = LocalDateTime.now();
    }

    /**
     * 重置错误计数
     */
    public void resetErrorCount() {
        this.consecutiveErrorCount = 0;
        this.lastErrorMessage = null;
        this.lastErrorTime = null;
    }
}
