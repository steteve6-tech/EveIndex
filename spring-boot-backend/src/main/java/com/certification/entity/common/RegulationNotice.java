package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 相关法规通知实体类
 * 对应数据库表：t_regulation_notice
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_regulation_notice")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "相关法规通知实体")
public class RegulationNotice {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 国家ID（关联t_country）
     */
    @Schema(description = "国家ID", example = "1")
    @Column(name = "country_id", nullable = false)
    private Long countryId;
    
    /**
     * 法规通知标题
     */
    @Schema(description = "法规通知标题", example = "关于医疗器械分类调整的通知")
    @Column(name = "notice_title", length = 200, nullable = false)
    private String noticeTitle;
    
    /**
     * 法规编号（如FDA的Federal Register编号）
     */
    @Schema(description = "法规编号", example = "FR-2024-001")
    @Column(name = "notice_number", length = 50)
    private String noticeNumber;
    
    /**
     * 发布机构（如FDA/NMPA/EU Commission）
     */
    @Schema(description = "发布机构", example = "FDA")
    @Column(name = "发布机构", length = 100)
    private String issuingAuthority;
    
    /**
     * 生效日期
     */
    @Schema(description = "生效日期")
    @Column(name = "effective_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    
    /**
     * 内容摘要（如分类调整/要求更新）
     */
    @Schema(description = "内容摘要", example = "对医疗器械分类进行调整")
    @Column(name = "content_summary", columnDefinition = "TEXT")
    private String contentSummary;
    
    /**
     * 关联HS编码（逗号分隔）
     */
    @Schema(description = "关联HS编码", example = "9018.50,8543.70")
    @Column(name = "related_hs_codes", length = 200)
    private String relatedHsCodes;
    
    /**
     * 关联产品类型
     */
    @Schema(description = "关联产品类型", example = "医疗设备,电子设备")
    @Column(name = "related_product_types", length = 200)
    private String relatedProductTypes;
    
    /**
     * 官方通知URL
     */
    @Schema(description = "官方通知URL", example = "https://www.fda.gov/notice")
    @Column(name = "notice_url", length = 255)
    private String noticeUrl;
    
    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    @Column(name = "publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", insertable = false, updatable = false)
    private Country country;
}
