package com.certification.entity.common;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI判断任务实体
 * 用于记录异步AI判断任务的状态和进度
 */
@Entity
@Table(name = "ai_judge_task")
@Data
public class AIJudgeTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务ID（UUID）
     */
    @Column(name = "task_id", unique = true, nullable = false)
    private String taskId;
    
    /**
     * 任务类型：CERT_NEWS, DEVICE_DATA
     */
    @Column(name = "task_type")
    private String taskType;
    
    /**
     * 任务状态：PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
     */
    @Column(name = "status")
    private String status;
    
    /**
     * 总数据量
     */
    @Column(name = "total_count")
    private Integer totalCount;
    
    /**
     * 已处理数量
     */
    @Column(name = "processed_count")
    private Integer processedCount;
    
    /**
     * 相关数量（高风险）
     */
    @Column(name = "related_count")
    private Integer relatedCount;
    
    /**
     * 不相关数量（低风险）
     */
    @Column(name = "unrelated_count")
    private Integer unrelatedCount;
    
    /**
     * 失败数量
     */
    @Column(name = "failed_count")
    private Integer failedCount;
    
    /**
     * 提取的关键词数量
     */
    @Column(name = "keyword_count")
    private Integer keywordCount;
    
    /**
     * 筛选条件（JSON格式）
     */
    @Column(name = "filter_params", columnDefinition = "TEXT")
    private String filterParams;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 最后更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    /**
     * 进度百分比
     */
    @Transient
    public Integer getProgress() {
        if (totalCount == null || totalCount == 0) {
            return 0;
        }
        return (int) ((processedCount * 100.0) / totalCount);
    }
    
    /**
     * 预初始化
     */
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (processedCount == null) {
            processedCount = 0;
        }
        if (relatedCount == null) {
            relatedCount = 0;
        }
        if (unrelatedCount == null) {
            unrelatedCount = 0;
        }
        if (failedCount == null) {
            failedCount = 0;
        }
        if (keywordCount == null) {
            keywordCount = 0;
        }
    }
    
    /**
     * 更新前
     */
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}

