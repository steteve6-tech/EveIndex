package com.certification.entity.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import com.certification.entity.common.CertNewsData.RiskLevel;

/**
 * 设备注册记录共有数据实体类
 * 只包含US FDA和EU EUDAMED两个数据源都有的字段
 * 对应数据库表：t_device_registration
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_registration")
@Schema(description = "设备注册记录共有数据实体")
public class DeviceRegistrationRecord extends BaseDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 数据源标识已继承自BaseDeviceEntity (dataSource, jdCountry)

    // ========== 核心标识字段（两个数据源都有） ==========
    @Lob
    @Column(name = "registration_number", columnDefinition = "TEXT", nullable = false)
    @Schema(description = "主要标识符（US: K_number+pma_number, EU: udi_di）")
    private String registrationNumber;

    @Column(name = "fei_number", length = 50)
    @Schema(description = "次要标识符（US: fei_number, EU: basic_udi_di）")
    private String feiNumber;

    // ========== 制造商信息（两个数据源都有） ==========
    @Lob
    @Column(name = "manufacturer_name", columnDefinition = "TEXT")
    @Schema(description = "制造商名称")
    private String manufacturerName;

    // ========== 设备信息（两个数据源都有） ==========
    @Lob
    @Column(name = "device_name", columnDefinition = "TEXT")
    @Schema(description = "设备名称")
    private String deviceName;

    @Lob
    @Column(name = "proprietary_name", columnDefinition = "LONGTEXT")
    @Schema(description = "专有名称/商标名称")
    private String proprietaryName;

    @Lob
    @Column(name = "device_class", columnDefinition = "LONGTEXT")
    @Schema(description = "设备类别")
    private String deviceClass;


    // ========== 状态信息（两个数据源都有） ==========
    @Column(name = "status_code", length = 100)
    @Schema(description = "状态码")
    private String statusCode;

    @Column(name = "created_date", length = 50)
    @Schema(description = "创建日期")
    private String createdDate;

    // 分析字段和元数据已继承自BaseDeviceEntity
    // (riskLevel, keywords, crawlTime, createTime, updateTime, remark)


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
        return "DeviceRegistration";
    }
}