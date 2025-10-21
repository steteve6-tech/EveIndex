package com.certification.entity.ai;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI判断待审核实体
 * 用于延迟执行AI判断结果，等待用户确认
 */
@Entity
@Table(name = "t_ai_judgment_pending")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIPendingJudgment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模块类型
     * DEVICE_DATA - 设备数据模块
     * CERT_NEWS - 医疗认证模块
     */
    @Column(name = "module_type", nullable = false, length = 50)
    private String moduleType;

    /**
     * 实体类型
     * Application, Registration, Recall, Event, Document, Customs
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * 原始数据ID
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * AI判断结果（JSON格式）
     * 结构: {"isRelated": true, "confidence": 0.85, "reason": "..."}
     */
    @Column(name = "judge_result", columnDefinition = "JSON")
    private String judgeResult;

    /**
     * 建议风险等级
     * HIGH, MEDIUM, LOW
     */
    @Column(name = "suggested_risk_level", length = 20)
    private String suggestedRiskLevel;

    /**
     * 建议备注内容
     */
    @Column(name = "suggested_remark", columnDefinition = "TEXT")
    private String suggestedRemark;

    /**
     * 新增黑名单关键词（JSON数组，仅设备数据模块）
     * 结构: ["keyword1", "keyword2", ...]
     */
    @Column(name = "blacklist_keywords", columnDefinition = "JSON")
    private String blacklistKeywords;

    /**
     * 是否被黑名单过滤
     */
    @Column(name = "filtered_by_blacklist")
    private Boolean filteredByBlacklist = false;

    /**
     * 状态
     * PENDING - 待审核
     * CONFIRMED - 已确认
     * REJECTED - 已拒绝
     * EXPIRED - 已过期
     */
    @Column(name = "status", length = 20)
    private String status = "PENDING";

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 过期时间（30天后）
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 确认时间
     */
    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;

    /**
     * 确认人
     */
    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    /**
     * 判断是否已过期
     */
    @Transient
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 判断是否待审核
     */
    @Transient
    public boolean isPending() {
        return "PENDING".equals(status) && !isExpired();
    }

    /**
     * 设置为已确认
     */
    public void confirm(String confirmedBy) {
        this.status = "CONFIRMED";
        this.confirmedTime = LocalDateTime.now();
        this.confirmedBy = confirmedBy;
    }

    /**
     * 设置为已拒绝
     */
    public void reject(String rejectedBy) {
        this.status = "REJECTED";
        this.confirmedTime = LocalDateTime.now();
        this.confirmedBy = rejectedBy;
    }

    /**
     * 设置为已过期
     */
    public void expire() {
        this.status = "EXPIRED";
    }

    /**
     * 初始化过期时间（创建后30天）
     */
    @PrePersist
    public void initializeExpireTime() {
        if (this.expireTime == null) {
            this.expireTime = LocalDateTime.now().plusDays(30);
        }
    }
}
