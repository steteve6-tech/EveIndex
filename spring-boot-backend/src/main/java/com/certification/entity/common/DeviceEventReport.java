package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.certification.entity.common.CrawlerData.RiskLevel;

/**
 * 设备事件报告实体类
 * 对应数据库表：t_device_event
 * 支持多种数据源：FDA设备不良事件、EU Safety Gate预警等
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_event")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "设备事件报告实体")
public class DeviceEventReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 核心业务标识 - 支持多种编号格式
    @Column(name = "report_number", length = 64, nullable = false, unique = true)
    private String reportNumber; // FDA: report_number, EU: alert_number

    @Column(name = "event_type", length = 50)
    private String eventType;

    // 多值逗号分隔
    @Column(name = "type_of_report", length = 512)
    private String typeOfReport;

    @Column(name = "date_of_event")
    private LocalDate dateOfEvent;

    @Column(name = "date_report")
    private LocalDate dateReport;

    @Column(name = "date_received")
    private LocalDate dateReceived; // FDA: date_received, EU: publication_date

    // 多值逗号分隔
    @Column(name = "source_type", length = 512)
    private String sourceType;

    @Column(name = "report_source_code", length = 100)
    private String reportSourceCode;

    @Column(name = "brand_name", length = 255)
    private String brandName; // FDA: device.manufacturer_name, EU: brand

    @Column(name = "model_number", length = 255)
    private String modelNumber; // FDA: device.model_number, EU: product_model

    @Column(name = "generic_name", length = 255)
    private String genericName; // FDA: device.generic_name, EU: product

    @Column(name = "manufacturer_name", length = 255)
    private String manufacturerName;

    @Column(name = "manufacturer_city", length = 100)
    private String manufacturerCity;

    @Column(name = "manufacturer_state", length = 100)
    private String manufacturerState;

    @Column(name = "manufacturer_country", length = 100)
    private String manufacturerCountry;

    @Column(name = "device_class", length = 10)
    private String deviceClass;

    @Column(name = "medical_specialty", length = 100)
    private String medicalSpecialty;

    // 新增：风险等级
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    private RiskLevel riskLevel = RiskLevel.MEDIUM;

    // 新增：关键词数组
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords; // TEXT格式存储关键词（JSON字符串或分号分隔）

    @Column(name = "regulation_number", length = 50)
    private String regulationNumber;

    @Column(name = "device_evaluated_by_manufacturer", length = 10)
    private String deviceEvaluatedByManufacturer;

    @Lob
    @Column(name = "mdr_text_description", columnDefinition = "TEXT")
    private String mdrTextDescription;

    @Lob
    @Column(name = "mdr_text_action", columnDefinition = "TEXT")
    private String mdrTextAction;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "contact_phone", length = 100)
    private String contactPhone;

    @Column(name = "date_added")
    private LocalDate dateAdded;

    @Column(name = "patient_count")
    private Integer patientCount;

    @Column(name = "data_source", length = 50)
    private String dataSource;

    // 新增：用于判定数据所属国家（如 US/CN/EU 等）
    @Column(name = "jd_country", length = 20)
    private String jdCountry;

    // ========== EU Safety Gate 特有字段 ==========
    
    /**
     * 产品具体名称（EU特有）
     */
    @Column(name = "product_name_specific", length = 500)
    private String productNameSpecific;
    
    /**
     * 产品描述（EU特有）
     */
    @Lob
    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;
    
    /**
     * 风险类型（EU特有）
     */
    @Column(name = "risk_type", length = 100)
    private String riskType;
    
    /**
     * 风险描述（EU特有）
     */
    @Lob
    @Column(name = "risk_description", columnDefinition = "TEXT")
    private String riskDescription;
    
    /**
     * 通知国家（EU特有）
     */
    @Column(name = "notifying_country", length = 100)
    private String notifyingCountry;
    
    /**
     * 产品类别（EU特有）
     */
    @Column(name = "product_category", length = 200)
    private String productCategory;
    
    /**
     * 产品子类别（EU特有）
     */
    @Column(name = "product_subcategory", length = 200)
    private String productSubcategory;
    
    /**
     * 措施描述（EU特有）
     */
    @Lob
    @Column(name = "measures_description", columnDefinition = "TEXT")
    private String measuresDescription;
    
    /**
     * 详情URL（EU特有）
     */
    @Column(name = "detail_url", length = 512)
    private String detailUrl;
    
    /**
     * 图片URL（EU特有）
     */
    @Column(name = "image_url", length = 512)
    private String imageUrl;
    
    /**
     * 品牌列表（EU特有，多个品牌用逗号分隔）
     */
    @Column(name = "brands_list", length = 1000)
    private String brandsList;
    
    /**
     * 风险列表（EU特有，多个风险用逗号分隔）
     */
    @Column(name = "risks_list", length = 1000)
    private String risksList;
    
    // ========== FDA 特有字段 ==========
    
    /**
     * 不良事件标志（FDA特有）
     */
    @Column(name = "adverse_event_flag", length = 10)
    private String adverseEventFlag;
    
    /**
     * 向FDA报告日期（FDA特有）
     */
    @Column(name = "date_report_to_fda")
    private LocalDate dateReportToFda;
    
    /**
     * 向FDA报告标志（FDA特有）
     */
    @Column(name = "report_to_fda", length = 10)
    private String reportToFda;
    
    /**
     * 向制造商报告标志（FDA特有）
     */
    @Column(name = "report_to_manufacturer", length = 10)
    private String reportToManufacturer;
    
    /**
     * MDR报告键（FDA特有）
     */
    @Column(name = "mdr_report_key", length = 50)
    private String mdrReportKey;
    
    /**
     * 事件位置（FDA特有）
     */
    @Column(name = "event_location", length = 100)
    private String eventLocation;
    
    /**
     * 事件键（FDA特有）
     */
    @Column(name = "event_key", length = 50)
    private String eventKey;
    
    /**
     * 事件中设备数量（FDA特有）
     */
    @Column(name = "number_devices_in_event")
    private Integer numberDevicesInEvent;
    
    /**
     * 产品问题标志（FDA特有）
     */
    @Column(name = "product_problem_flag", length = 10)
    private String productProblemFlag;
    
    /**
     * 产品问题列表（FDA特有，多个问题用逗号分隔）
     */
    @Column(name = "product_problems_list", length = 1000)
    private String productProblemsList;
    
    /**
     * 补救措施列表（FDA特有，多个措施用逗号分隔）
     */
    @Column(name = "remedial_action_list", length = 1000)
    private String remedialActionList;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;
}
