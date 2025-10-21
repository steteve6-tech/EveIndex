package com.certification.entity.common;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 认证新闻爬虫定时任务配置实体
 */
@Data
@Entity
@Table(name = "t_cert_news_task_config")
public class CertNewsTaskConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, unique = true, length = 100)
    private String taskName;

    /**
     * 爬虫类型: SGS, UL, BEICE
     */
    @Column(name = "crawler_type", nullable = false, length = 50)
    private String crawlerType;

    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 搜索关键词（可选）
     */
    @Column(name = "keyword", length = 200)
    private String keyword;

    /**
     * 每次爬取的最大记录数
     */
    @Column(name = "max_records")
    private Integer maxRecords = 50;

    /**
     * 任务描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 上次执行时间
     */
    @Column(name = "last_execute_time")
    private LocalDateTime lastExecuteTime;

    /**
     * 下次执行时间
     */
    @Column(name = "next_execute_time")
    private LocalDateTime nextExecuteTime;

    /**
     * 上次执行状态: SUCCESS, FAILED
     */
    @Column(name = "last_execute_status", length = 20)
    private String lastExecuteStatus;

    /**
     * 上次执行结果消息
     */
    @Column(name = "last_execute_message", columnDefinition = "TEXT")
    private String lastExecuteMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
