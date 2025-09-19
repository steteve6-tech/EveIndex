package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备PMA记录实体类
 * 对应数据库表：t_device_pma
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_pma",
       uniqueConstraints = {
               @UniqueConstraint(name = "uk_pma_supplement", columnNames = {"pma_number", "supplement_number"})
       },
       indexes = {
               @Index(name = "idx_pma_number", columnList = "pma_number"),
               @Index(name = "idx_product_code", columnList = "product_code"),
               @Index(name = "idx_decision_date", columnList = "decision_date"),
               @Index(name = "idx_date_received", columnList = "date_received"),
               @Index(name = "idx_jd_country", columnList = "jd_country")
       }
)
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备PMA记录实体")
public class DevicePmaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PMA 主编号 */
    @Column(name = "pma_number", length = 32, nullable = false)
    private String pmaNumber;

    /** 补充申请编号 */
    @Column(name = "supplement_number", length = 32)
    private String supplementNumber;

    /** 申请人（企业名称） */
    @Column(name = "applicant", length = 255)
    private String applicant;

    /** 申请人完整地址 */
    @Column(name = "full_address", length = 512)
    private String fullAddress;

    /** 设备通用名称 */
    @Column(name = "generic_name", length = 255)
    private String genericName;

    /** 设备商品名称 */
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    /** FDA 产品分类代码 */
    @Column(name = "product_code", length = 20)
    private String productCode;

    /** 咨询委员会描述 */
    @Column(name = "advisory_committee_description", length = 255)
    private String advisoryCommitteeDescription;

    /** 补充申请类型 */
    @Column(name = "supplement_type", length = 100)
    private String supplementType;

    /** 补充申请原因 */
    @Column(name = "supplement_reason", length = 255)
    private String supplementReason;

    /** 是否加急审查（Y/N） */
    @Column(name = "expedited_review_flag", length = 10)
    private String expeditedReviewFlag;

    /** FDA 接收申请日期 */
    @Column(name = "date_received")
    private LocalDate dateReceived;

    /** 审批决策日期 */
    @Column(name = "decision_date")
    private LocalDate decisionDate;

    /** 审批结果代码 */
    @Column(name = "decision_code", length = 50)
    private String decisionCode;

    /** 审批结论说明 */
    @Lob
    @Column(name = "ao_statement", columnDefinition = "TEXT")
    private String aoStatement;

    /** FDA 设备分类（风险等级） */
    @Column(name = "device_class", length = 10)
    private String deviceClass;

    /** 数据来源 */
    @Column(name = "data_source", length = 50)
    private String dataSource;

    /** 数据适用国家 */
    @Column(name = "jd_country", length = 20)
    private String jdCountry;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;
}
