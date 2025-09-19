package com.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 爬虫数据查询请求DTO
 */
@Data
@Schema(description = "爬虫数据查询请求")
public class CrawlerDataQueryRequest {

    @Schema(description = "关键词搜索", example = "Wi-Fi")
    private String keyword;

    @Schema(description = "市场代码", example = "EU_RED_BASE")
    private String marketCode;

    @Schema(description = "国家/地区", example = "EU")
    private String country;

    @Schema(description = "数据源名称", example = "SGS")
    private String sourceName;

    @Schema(description = "自定义关键词", example = "Wi-Fi,6GHz")
    private String customKeywords;

    @Schema(description = "是否相关：true-相关，false-不相关，null-未确定", example = "true")
    private Boolean related;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;
}
