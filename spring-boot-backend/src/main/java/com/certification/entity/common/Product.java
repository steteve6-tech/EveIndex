package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品基础信息实体类
 * 对应数据库表：t_product
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_product")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "产品基础信息实体")
public class Product {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 产品名称（如Visia皮肤分析仪）
     */
    @Schema(description = "产品名称", example = "Visia皮肤分析仪")
    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;
    
    /**
     * 品牌
     */
    @Schema(description = "品牌", example = "Canfield")
    @Column(name = "brand", length = 50)
    private String brand;
    
    /**
     * 型号
     */
    @Schema(description = "型号", example = "Visia CR")
    @Column(name = "model", length = 50)
    private String model;
    
    /**
     * 产品类型（如家用/专业医疗设备）
     */
    @Schema(description = "产品类型", example = "专业医疗设备")
    @Column(name = "product_type", length = 30)
    private String productType;
    
    /**
     * 申请人名称
     */
    @Schema(description = "申请人名称", example = "Canfield Scientific Inc.")
    @Column(name = "applicant_name", length = 100)
    private String applicantName;
    
    /**
     * 品牌名称（贸易名称）
     */
    @Schema(description = "品牌名称", example = "Visia")
    @Column(name = "brand_name", length = 100)
    private String brandName;
    
    /**
     * 设备代码（如K编号、注册编号等）
     */
    @Schema(description = "设备代码", example = "K123456")
    @Column(name = "device_code", length = 50)
    private String deviceCode;
    
    /**
     * 数据来源（从哪个高风险数据生成）
     */
    @Schema(description = "数据来源", example = "device510k")
    @Column(name = "data_source", length = 30)
    private String dataSource;
    
    /**
     * 原始数据ID（关联到原始高风险数据）
     */
    @Schema(description = "原始数据ID", example = "123")
    @Column(name = "source_data_id")
    private Long sourceDataId;
    
    /**
     * 设备等级
     */
    @Schema(description = "设备等级", example = "Class II")
    @Column(name = "device_class", length = 20)
    private String deviceClass;
    
    /**
     * 设备描述
     */
    @Schema(description = "设备描述", example = "高精度皮肤分析设备")
    @Column(name = "device_description", columnDefinition = "TEXT")
    private String deviceDescription;
    
    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    /**
     * 是否有效（1-有效/0-无效）
     */
    @Schema(description = "是否有效", example = "true")
    @Column(name = "is_active")
    private Integer isActive = 1;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(name = "create_time")
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @Column(name = "update_time")
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    // 关联关系
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductRegistration> productRegistrations;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategoryCode> productCategoryCodes;
}
