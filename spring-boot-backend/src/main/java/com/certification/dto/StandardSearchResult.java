package com.certification.dto;

import com.certification.entity.common.Standard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 标准搜索结果DTO
 */
@Data
@Schema(description = "标准搜索结果")
public class StandardSearchResult {

    @Schema(description = "标准列表")
    private List<Standard> standards;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer size;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "风险统计")
    private List<Map<String, Object>> riskStats;

    @Schema(description = "状态统计")
    private List<Map<String, Object>> statusStats;

    @Schema(description = "是否缓存")
    private Boolean cached;

    @Schema(description = "时间戳")
    private String timestamp;
}
