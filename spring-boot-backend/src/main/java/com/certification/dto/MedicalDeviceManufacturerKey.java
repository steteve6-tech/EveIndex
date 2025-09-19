package com.certification.dto;

import lombok.Data;
import java.util.List;

@Data
public class MedicalDeviceManufacturerKey {
    // 企业核心标识
    private String registrationNumber;       // 注册编号（唯一标识）
    private String feiNumber;                // FDA 企业识别号（唯一标识）
    private String manufacturerName;         // 制造商名称（主体标识）

    // 企业核心状态
    private String statusCode;               // 注册状态（如 "1" 表示有效）
    private String regExpiryYear;            // 注册到期年份（合规性参考）
    private String establishmentType;        // 企业类型（如合同制造商）

    // 核心地址信息（合并关键部分）
    private String manufacturerFullAddress;  // 制造商完整地址（国家 + 城市 + 街道 + 邮编）
    private String manufacturerCountryCode;  // 制造商国家代码（如 DE = 德国）

    // 美国代理关键信息（合规必需）
    private String usAgentBusinessName;      // 美国代理企业名称
    private String usAgentContactInfo;       // 美国代理联系方式（电话 + 邮箱）

    // 母公司关键信息（归属关系）
    private String ownerFirmName;            // 母公司名称
    private String ownerFullAddress;         // 母公司完整地址

    // 产品核心信息（多产品用列表对应存储）
    private String kNumber;                  // 产品审批 K 编号（关键合规标识）
    private List<String> productCodes;       // 产品代码列表（FDA 分类标识）
    private List<String> deviceNames;        // 设备名称列表（核心产品标识）
    private List<String> deviceClasses;      // 设备风险等级列表（1/2/3 类）
    private List<String> regulationNumbers;  // 对应法规编号（合规依据）

    // 数据来源（例如：FDA）
    private String dataSource;
}
