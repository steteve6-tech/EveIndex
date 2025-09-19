package com.certification.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 医疗设备不良事件报告数据模型类（单表结构）
 * 用于爬虫存储设备不良事件的核心信息，整合嵌套结构为扁平字段
 */
@Data
public class MedicalDeviceEventSingle {
    /** 自增主键，数据库内部唯一标识 */
    private Long id;

    /** FDA报告唯一编号（核心业务标识） */
    private String reportNumber;

    /** 事件类型（如故障、伤害等） */
    private String eventType;

    /** 报告类型（多值用逗号分隔，如"Initial submission"） */
    private String typeOfReport;

    /** 事件发生日期 */
    private LocalDate dateOfEvent;

    /** 报告提交日期 */
    private LocalDate dateReport;

    /** FDA接收报告日期 */
    private LocalDate dateReceived;

    /** 报告来源（多值用逗号分隔，如"Health Professional, User facility"） */
    private String sourceType;

    /** 报告来源代码（如"Manufacturer report"） */
    private String reportSourceCode;

    /** 设备品牌名 */
    private String brandName;

    /** 设备型号 */
    private String modelNumber;

    /** 设备通用名称 */
    private String genericName;

    /** 设备制造商名称 */
    private String manufacturerName;

    /** 制造商所在城市 */
    private String manufacturerCity;

    /** 制造商所在州 */
    private String manufacturerState;

    /** 制造商所在国家 */
    private String manufacturerCountry;

    /** 设备风险等级（1/2/3类） */
    private String deviceClass;

    /** 所属医疗专业领域（如"Radiology"） */
    private String medicalSpecialty;

    /** 对应FDA法规编号 */
    private String regulationNumber;

    /** 制造商是否评估过设备（Y/N） */
    private String deviceEvaluatedByManufacturer;

    /** 事件描述文本 */
    private String mdrTextDescription;

    /** 制造商处理措施文本 */
    private String mdrTextAction;

    /** 制造商联系人（姓名+职位） */
    private String contactPerson;

    /** 联系人电话 */
    private String contactPhone;

    /** 报告录入系统日期 */
    private LocalDate dateAdded;

    /** 涉及患者数量 */
    private Integer patientCount;

    /** 数据来源（例如：FDA） */
    private String dataSource;

    /** 数据适用国家（如 US/CN/EU） */
    private String jdCountry;
}
