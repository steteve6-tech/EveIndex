package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 国家基础信息实体类
 * 对应数据库表：t_country
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_country")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "国家基础信息实体")
public class Country {
    
    @Schema(description = "主键ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 国家编码（如US/CN/EU）
     */
    @Schema(description = "国家编码", example = "US")
    @Column(name = "country_code", length = 2, nullable = false, unique = true)
    private String countryCode;
    
    /**
     * 国家名称
     */
    @Schema(description = "国家名称", example = "美国")
    @Column(name = "country_name", length = 50, nullable = false)
    private String countryName;
    
    /**
     * 所属地区（如北美/亚太）
     */
    @Schema(description = "所属地区", example = "北美")
    @Column(name = "region", length = 20)
    private String region;
    
    /**
     * 英文缩写（如us）
     */
    @Schema(description = "英文缩写", example = "us")
    @Column(name = "en_name", length = 100)
    private String enName;
    
    /**
     * 全名
     */
    @Schema(description = "全名", example = "United States of America")
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @Column(name = "create_time")
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    // 关联关系 - 已删除相关实体类的引用
    // @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductRegistration> productRegistrations;
    
    // @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductCategoryCode> productCategoryCodes;
    
    // @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<ProductRecall> productRecalls;
    
    // @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<RegulationNotice> regulationNotices;
}
