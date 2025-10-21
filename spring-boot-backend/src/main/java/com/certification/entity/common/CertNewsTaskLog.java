package com.certification.entity.common;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 认证新闻爬虫任务执行日志
 */
@Data
@Entity
@Table(name = "t_cert_news_task_log")
public class CertNewsTaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务配置ID
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 任务名称
     */
    @Column(name = "task_name", length = 100)
    private String taskName;

    /**
     * 爬虫类型: SGS, UL, BEICE
     */
    @Column(name = "crawler_type", length = 50)
    private String crawlerType;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 执行状态: SUCCESS, FAILED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 爬取成功数量
     */
    @Column(name = "success_count")
    private Integer successCount = 0;

    /**
     * 爬取失败数量
     */
    @Column(name = "error_count")
    private Integer errorCount = 0;

    /**
     * 执行结果消息
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
