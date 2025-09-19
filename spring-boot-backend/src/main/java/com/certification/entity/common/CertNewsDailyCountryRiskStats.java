package com.certification.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日国家高风险数据统计实体
 * 记录每天每个国家的高风险数据数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_country_risk_stats", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "country"}))
public class CertNewsDailyCountryRiskStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    /**
     * 国家名称
     */
    @Column(name = "country", nullable = false, length = 100)
    private String country;

    /**
     * 高风险数据数量
     */
    @Column(name = "high_risk_count", nullable = false)
    private Long highRiskCount = 0L;

    /**
     * 中风险数据数量
     */
    @Column(name = "medium_risk_count", nullable = false)
    private Long mediumRiskCount = 0L;

    /**
     * 低风险数据数量
     */
    @Column(name = "low_risk_count", nullable = false)
    private Long lowRiskCount = 0L;

    /**
     * 无风险数据数量
     */
    @Column(name = "no_risk_count", nullable = false)
    private Long noRiskCount = 0L;

    /**
     * 总数据数量
     */
    @Column(name = "total_count", nullable = false)
    private Long totalCount = 0L;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 是否删除
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

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
