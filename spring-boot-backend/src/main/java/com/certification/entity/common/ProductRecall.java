package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

/**
 * 商品召回记录实体类
 * 对应数据库表：t_product_recall
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_product_recall")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "商品召回记录实体")
public class ProductRecall {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    
    /**
     * 国家代码（关联t_country）
     */
    @Schema(description = "国家代码", example = "US")
    @Column(name = "country_code", nullable = false, length = 20)
    private String countryCode;
    
    /**
     * 召回编号（如FDA的召回编号）
     */
    @Schema(description = "召回编号", example = "Z-1234-2024")
    @Column(name = "recall_number", length = 100)
    private String recallNumber;
    
    /**
     * 召回原因（如质量问题/标签违规）
     */
    @Schema(description = "召回原因", example = "产品质量问题")
    @Column(name = "recall_reason", columnDefinition = "TEXT")
    private String recallReason;
    
    /**
     * 召回级别（如FDA的I/II/III级）
     */
    @Schema(description = "召回级别", example = "II级")
    @Column(name = "recall_level", length = 50)
    private String recallLevel;
    
    /**
     * 召回发布日期
     */
    @Schema(description = "召回发布日期")
    @Column(name = "recall_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recallDate;
    
    /**
     * 受影响数量
     */
    @Schema(description = "受影响数量", example = "100")
    @Column(name = "affected_quantity")
    private Integer affectedQuantity;
    
    /**
     * 官方公告URL
     */
    @Schema(description = "官方公告URL", example = "https://www.fda.gov/recall")
    @Column(name = "authority_notice_url", length = 500)
    private String authorityNoticeUrl;
    
    // FDA召回相关字段
    @Schema(description = "CFRES ID", example = "CFRES123456")
    @Column(name = "cfres_id", length = 100)
    private String cfresId;
    
    @Schema(description = "事件发起日期")
    @Column(name = "event_date_initiated")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventDateInitiated;
    
    @Schema(description = "召回状态", example = "Ongoing")
    @Column(name = "recall_status", length = 100)
    private String recallStatus;
    
    @Schema(description = "召回公司", example = "ABC Medical Devices")
    @Column(name = "recalling_firm", length = 500)
    private String recallingFirm;
    
    @Schema(description = "公司FEI编号", example = "1234567")
    @Column(name = "firm_fei_number", length = 50)
    private String firmFeiNumber;
    
    @Schema(description = "产品描述", example = "Medical device for skin analysis")
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;
    
    @Schema(description = "产品代码", example = "ABC123")
    @Column(name = "product_code", length = 100)
    private String productCode;
    
    @Schema(description = "K编号列表", example = "K123456,K789012")
    @Column(name = "k_numbers", columnDefinition = "TEXT")
    private String kNumbers;
    
    @Schema(description = "PMA编号列表", example = "P123456,P789012")
    @Column(name = "pma_numbers", columnDefinition = "TEXT")
    private String pmaNumbers;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_code", referencedColumnName = "country_code", insertable = false, updatable = false)
    private Country country;
}
