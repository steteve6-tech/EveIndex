package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 设备510K记录实体类
 * 对应数据库表：t_device_510k
 *
 * 继承自 BaseDeviceEntity，包含通用字段：
 * - riskLevel, keywords, jdCountry, dataSource, remark
 * - crawlTime, dataStatus, createTime, updateTime
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_510k")
@Schema(description = "设备510K记录实体")
public class Device510K extends BaseDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 设备类别
     */
    @Schema(description = "设备类别 (Class I, II, III)", example = "Class II")
    @Column(name = "device_class", length = 10)
    private String deviceClass;

    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称", example = "AIMYSKIN")
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    /**
     * 申请人
     */
    @Schema(description = "申请人/公司名称", example = "ABC Medical Inc.")
    @Column(name = "applicant", length = 255)
    private String applicant;

    /**
     * 国家代码
     */
    @Schema(description = "国家代码", example = "US")
    @Column(name = "country_code", length = 2)
    private String countryCode;

    /**
     * 接收日期
     */
    @Schema(description = "FDA接收申请日期")
    @Column(name = "date_received")
    private LocalDate dateReceived;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "Skin Analyzer")
    @Column(name = "device_name", length = 255)
    private String deviceName;

    /**
     * K号 (510K编号)
     */
    @Schema(description = "510K编号", example = "K123456")
    @Column(name = "k_number", length = 32, unique = true)
    private String kNumber;

    /**
     * 是否新增数据
     */
    @Schema(description = "是否新增数据")
    @Column(name = "is_new")
    private Boolean isNew = true;

    /**
     * 新增数据是否已查看
     */
    @Schema(description = "新增数据是否已查看")
    @Column(name = "new_data_viewed")
    private Boolean newDataViewed = false;

    @Override
    public String getEntityType() {
        return "Device510K";
    }
}