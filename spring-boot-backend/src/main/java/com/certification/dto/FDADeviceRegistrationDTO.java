package com.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FDA设备注册信息DTO
 * 用于API接口数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FDA设备注册信息DTO")
public class FDADeviceRegistrationDTO {

    @Schema(description = "数据ID")
    private String id;

    @Schema(description = "专有名称列表")
    private List<String> proprietaryNames;

    @Schema(description = "机构类型列表")
    private List<String> establishmentTypes;

    @Schema(description = "注册号")
    private String registrationNumber;

    @Schema(description = "FEI号码")
    private String feiNumber;

    @Schema(description = "状态代码")
    private String statusCode;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "地址行1")
    private String addressLine1;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "州代码")
    private String stateCode;

    @Schema(description = "国家代码")
    private String countryCode;

    @Schema(description = "邮政编码")
    private String postalCode;

    @Schema(description = "PMA号码")
    private String pmaNumber;

    @Schema(description = "K号码")
    private String kNumber;

    @Schema(description = "产品代码")
    private String productCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类别")
    private String deviceClass;

    @Schema(description = "医学专业")
    private String medicalSpecialty;

    @Schema(description = "法规编号")
    private String regulationNumber;

    @Schema(description = "通用名称")
    private String genericName;

    @Schema(description = "创建日期")
    private String createdDate;

    @Schema(description = "是否豁免")
    private String isExempt;

    @Schema(description = "爬取时间")
    private LocalDateTime crawlTime;

    @Schema(description = "数据状态")
    private String status;

    @Schema(description = "是否已处理")
    private Boolean isProcessed;

    @Schema(description = "处理时间")
    private LocalDateTime processedTime;

    @Schema(description = "备注")
    private String remarks;
}
