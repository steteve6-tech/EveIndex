package com.certification.dto;

import com.certification.entity.common.Standard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标准查询请求DTO
 */
@Data
@Schema(description = "标准查询请求")
public class StandardQueryRequest {

    @Schema(description = "关键词搜索")
    private String keyword;

    @Schema(description = "风险等级", example = "HIGH")
    private String risk;

    @Schema(description = "国家/地区", example = "US")
    private String country;

    @Schema(description = "标准状态", example = "ACTIVE")
    private Standard.StandardStatus status;

    @Schema(description = "是否监控", example = "true")
    private Boolean isMonitored;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;
}
