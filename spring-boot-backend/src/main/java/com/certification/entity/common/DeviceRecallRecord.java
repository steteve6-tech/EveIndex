package com.certification.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.certification.entity.common.CertNewsData.RiskLevel;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "t_device_recall")
@Schema(description = "医疗器械召回记录（适配D_recall爬虫数据结构）")
public class DeviceRecallRecord extends BaseDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 召回事件ID（核心标识字段）
     */
    @Column(name = "cfres_id", length = 100)
    @Schema(description = "召回事件ID（从FDA网站URL提取）")
    private String cfresId;

    /**
     * 产品描述
     */
    @Column(name = "product_description", columnDefinition = "TEXT")
    @Schema(description = "产品描述")
    private String productDescription;

    /**
     * 召回公司
     */
    @Column(name = "recalling_firm", length = 255)
    @Schema(description = "召回公司")
    private String recallingFirm;

    /**
     * 召回等级
     */
    @Column(name = "recall_status", length = 100)
    @Schema(description = "召回等级（CLASS I/II/III）")
    private String recallStatus;

    /**
     * 召回发布日期
     */
    @Column(name = "event_date_posted")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "召回发布日期")
    private LocalDate eventDatePosted;

    /**
     * 设备名称
     */
    @Column(name = "device_name", length = 255)
    @Schema(description = "设备名称（复用产品描述）")
    private String deviceName;

    /**
     * 产品代码
     */
    @Column(name = "product_code", length = 50)
    @Schema(description = "产品代码")
    private String productCode;

    /**
     * 国家代码
     */
    @Column(name = "country_code", nullable = false, length = 20)
    @Schema(description = "国家代码", example = "US")
    private String countryCode;


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
        return "DeviceRecall";
    }
}
