package com.certification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 统一任务配置实体
 * 管理爬虫任务的调度和参数配置
 */
@Entity
@Table(name = "t_unified_task_config")
@Data
@EqualsAndHashCode(callSuper = false)
public class UnifiedTaskConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;
    
    /**
     * 爬虫名称
     */
    @Column(name = "crawler_name", nullable = false, length = 50)
    private String crawlerName;
    
    /**
     * 国家代码
     */
    @Column(name = "country_code", length = 10)
    private String countryCode;
    
    /**
     * 任务类型
     */
    @Column(name = "task_type", length = 50)
    private String taskType;

    /**
     * 参数配置 (JSON格式)
     */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    /**
     * 关键词列表（保留用于向后兼容）
     */
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;
    
    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;
    
    /**
     * 任务描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority = 5;
    
    /**
     * 超时时间(分钟)
     */
    @Column(name = "timeout_minutes")
    private Integer timeoutMinutes = 30;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 3;
    
    /**
     * 最后执行时间
     */
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;
    
    /**
     * 下次执行时间
     */
    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;
    
    /**
     * 最后执行状态
     */
    @Column(name = "last_execution_status", length = 20)
    private String lastExecutionStatus;
    
    /**
     * 最后执行结果
     */
    @Column(name = "last_execution_result", columnDefinition = "TEXT")
    private String lastExecutionResult;
    
    /**
     * 执行次数
     */
    @Column(name = "execution_count")
    private Integer executionCount = 0;
    
    /**
     * 成功次数
     */
    @Column(name = "success_count")
    private Integer successCount = 0;
    
    /**
     * 失败次数
     */
    @Column(name = "failure_count")
    private Integer failureCount = 0;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 创建者
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    /**
     * 更新者
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
    
    /**
     * 计算成功率
     */
    public double getSuccessRate() {
        if (executionCount == null || executionCount == 0) {
            return 0.0;
        }
        if (successCount == null) {
            return 0.0;
        }
        return (double) successCount / executionCount * 100;
    }
    
    /**
     * 获取任务状态
     */
    public String getTaskStatus() {
        if (enabled == null || !enabled) {
            return "DISABLED";
        }
        if (lastExecutionStatus == null) {
            return "READY";
        }
        return lastExecutionStatus;
    }
    
    /**
     * 更新执行统计
     */
    public void updateExecutionStats(boolean success, String result) {
        if (executionCount == null) {
            executionCount = 0;
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (failureCount == null) {
            failureCount = 0;
        }
        
        executionCount++;
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
        
        lastExecutionTime = LocalDateTime.now();
        lastExecutionStatus = success ? "SUCCESS" : "FAILED";
        lastExecutionResult = result;
    }
}
