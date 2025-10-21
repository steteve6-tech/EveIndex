package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * 设备事件报告实体类
 * 对应数据库表：t_device_event
 * 支持多种数据源：FDA设备不良事件、EU Safety Gate预警等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_event")
@Schema(description = "设备事件报告实体")
public class DeviceEventReport extends BaseDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 报告编号（核心业务标识）
     * FDA: report_number, EU: alert_number
     */
    @Column(name = "report_number", length = 64, nullable = false, unique = true)
    @Schema(description = "报告编号（FDA/EU等不同来源）", example = "1234567")
    private String reportNumber;

    /**
     * 事件发生日期
     */
    @Column(name = "date_of_event")
    @Schema(description = "事件发生日期")
    private LocalDate dateOfEvent;

    /**
     * 接收日期
     * FDA: date_received, EU: publication_date
     */
    @Column(name = "date_received")
    @Schema(description = "接收/发布日期")
    private LocalDate dateReceived;

    /**
     * 品牌名称
     * FDA: device.manufacturer_name, EU: brand
     */
    @Column(name = "brand_name", length = 255)
    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 通用名称
     * FDA: device.generic_name, EU: product
     */
    @Column(name = "generic_name", length = 255)
    @Schema(description = "通用设备名称")
    private String genericName;

    /**
     * 制造商名称
     */
    @Column(name = "manufacturer_name", length = 255)
    @Schema(description = "制造商名称")
    private String manufacturerName;

    /**
     * 设备类别
     */
    @Column(name = "device_class", length = 10)
    @Schema(description = "设备类别", example = "Class II")
    private String deviceClass;


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
        return "DeviceEvent";
    }
}
