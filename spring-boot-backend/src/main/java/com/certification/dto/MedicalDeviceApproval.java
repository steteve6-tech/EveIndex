package com.certification.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 医疗设备审批信息数据模型类
 * 用于存储从FDA爬取的设备审批关键信息
 */
@Data
public class MedicalDeviceApproval {
    /**
     * 自增主键，用于数据库存储
     */
    private Long id;

    /**
     * FDA审批唯一编号（核心业务标识）
     */
    private String kNumber;

    /**
     * 设备申请企业名称
     */
    private String applicant;

    /**
     * 设备具体名称（含型号）
     */
    private String deviceName;

    /**
     * FDA统一设备名称（通用名）
     */
    private String deviceGeneralName;

    /**
     * 设备风险等级（1/2/3类）
     */
    private String deviceClass;

    /**
     * 审批结果描述
     */
    private String decisionResult;

    /**
     * 审批决策日期
     */
    private LocalDate decisionDate;

    /**
     * 申请接收日期
     */
    private LocalDate dateReceived;

    /**
     * 负责审查的专业委员会
     */
    private String advisoryCommittee;

    /**
     * FDA产品分类代码
     */
    private String productCode;

    /**
     * 对应的FDA法规编号
     */
    private String regulationNumber;

    /**
     * 联系人姓名
     */
    private String contactPerson;

    /**
     * 企业完整地址（合并后的地址信息）
     */
    private String address;

    /**
     * 审批类型
     */
    private String clearanceType;

    /**
     * 数据来源（例如：FDA）
     */
    private String dataSource;
}
