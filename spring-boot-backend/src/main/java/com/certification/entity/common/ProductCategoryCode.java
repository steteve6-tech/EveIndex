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
 * 产品分类代码实体类
 * 对应数据库表：t_product_category_code
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_product_category_code")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "产品分类代码实体")
public class ProductCategoryCode {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 产品ID（关联t_product）
     */
    @Schema(description = "产品ID", example = "1")
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    /**
     * 国家ID（关联t_country）
     */
    @Schema(description = "国家ID", example = "1")
    @Column(name = "country_id", nullable = false)
    private Long countryId;
    
    /**
     * HS编码（如9018、8543.70）
     */
    @Schema(description = "HS编码", example = "9018.50")
    @Column(name = "hs_code", length = 20)
    private String hsCode;
    
    /**
     * 监管分类代码（如FDA的Product Code）
     */
    @Schema(description = "监管分类代码", example = "LLZ")
    @Column(name = "reg_code", length = 20)
    private String regCode;
    
    /**
     * 代码描述
     */
    @Schema(description = "代码描述", example = "皮肤分析仪")
    @Column(name = "code_description", length = 200)
    private String codeDescription;
    
    /**
     * 是否默认使用（1-是/0-否）
     */
    @Schema(description = "是否默认使用", example = "true")
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    /**
     * 生效日期
     */
    @Schema(description = "生效日期")
    @Column(name = "effective_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", insertable = false, updatable = false)
    private Country country;
}
