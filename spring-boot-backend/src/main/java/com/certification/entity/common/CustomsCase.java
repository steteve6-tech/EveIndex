package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.certification.entity.common.CrawlerData.RiskLevel;

/**
 * 海关过往判例实体类
 * 对应数据库表：t_customs_case
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_customs_case")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "海关过往判例实体")
public class CustomsCase {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 判例编号/URL
     */
    @Schema(description = "判例编号/URL", example = "https://rulings.cbp.gov/ruling/N349263")
    @Column(name = "case_number", length = 1000)
    private String caseNumber;
    
    /**
     * 判例日期
     */
    @Schema(description = "判例日期")
    @Column(name = "case_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate caseDate;
    
    /**
     * 裁定使用的HS编码（多个编码用逗号分隔）
     */
    @Schema(description = "裁定使用的HS编码（多个编码用逗号分隔）", example = "9018.50,8543.70")
    @Column(name = "hs_code_used", columnDefinition = "TEXT")
    private String hsCodeUsed;
    
    /**
     * 裁定结果（如归类认定/处罚决定）
     */
    @Schema(description = "裁定结果", example = "归类认定")
    @Column(name = "ruling_result", columnDefinition = "TEXT")
    private String rulingResult;
    
    /**
     * 违规类型（如标签不符/归类错误）
     */
    @Schema(description = "违规类型", example = "归类错误")
    @Column(name = "violation_type", length = 50)
    private String violationType;
    
    /**
     * 处罚金额（如有）
     */
    @Schema(description = "处罚金额", example = "10000.00")
    @Column(name = "penalty_amount", precision = 12, scale = 2)
    private BigDecimal penaltyAmount;
    
    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "U.S. Customs and Border Protection Securing America's Borders")
    @Column(name = "data_source", length = 255)
    private String dataSource;
    
    /**
     * 数据来源国家（如US/CN/EU等）
     */
    @Schema(description = "数据来源国家", example = "US")
    @Column(name = "jd_country", length = 20, nullable = false)
    private String jdCountry = "US";
    
    /**
     * 爬取时间
     */
    @Schema(description = "爬取时间")
    @Column(name = "crawl_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime crawlTime;
    
    /**
     * 数据状态
     */
    @Schema(description = "数据状态", example = "ACTIVE")
    @Column(name = "data_status", length = 20)
    private String dataStatus = "ACTIVE";

    // 新增：风险等级
    @Schema(description = "风险等级", example = "MEDIUM")
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    // 新增：关键词数组
    @Schema(description = "关键词数组", example = "[\"医疗器械\", \"海关\"]")
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // JSON格式存储关键词数组
}
