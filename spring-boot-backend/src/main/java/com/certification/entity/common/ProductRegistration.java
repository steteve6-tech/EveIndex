package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品注册信息实体类
 * 对应数据库表：t_product_registration
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_product_registration")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "产品注册信息实体")
public class ProductRegistration {
    
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
     * 注册证号（如FDA的510(k)编号、NMPA的械字号）
     */
    @Schema(description = "注册证号", example = "K123456")
    @Column(name = "reg_number", length = 50)
    private String regNumber;
    
    /**
     * 注册状态（已注册/审核中/未注册）
     */
    @Schema(description = "注册状态", example = "已注册")
    @Column(name = "reg_status", length = 20, nullable = false)
    private String regStatus;
    
    /**
     * 注册分类（如CE Class I/II、NMPA二类/三类）
     */
    @Schema(description = "注册分类", example = "CE Class II")
    @Column(name = "reg_category", length = 30)
    private String regCategory;
    
    /**
     * 注册日期
     */
    @Schema(description = "注册日期")
    @Column(name = "reg_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate regDate;
    
    /**
     * 有效期
     */
    @Schema(description = "有效期")
    @Column(name = "expire_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;
    
    /**
     * 发证机构（如FDA/NMPA）
     */
    @Schema(description = "发证机构", example = "FDA")
    @Column(name = "issuing_authority", length = 50)
    private String issuingAuthority;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(name = "create_time")
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", insertable = false, updatable = false)
    private Country country;
}
