package com.certification.dto;

import com.certification.entity.common.CertNewsData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 爬虫数据搜索结果DTO
 */
@Data
@Schema(description = "爬虫数据搜索结果")
public class CrawlerDataSearchResult {

    @Schema(description = "爬虫数据列表")
    private List<CertNewsData> certNewsDataList;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "当前页")
    private Integer page;

    @Schema(description = "每页大小")
    private Integer size;

    @Schema(description = "总页数")
    private Integer totalPages;

    @Schema(description = "搜索关键词")
    private List<String> searchKeywords;

    @Schema(description = "匹配的关键词统计")
    private Map<String, Integer> matchedKeywords;

    @Schema(description = "市场代码")
    private String marketCode;

    @Schema(description = "市场名称")
    private String marketName;

    @Schema(description = "主管机构")
    private String authority;

    @Schema(description = "高优先级监测项")
    private List<String> highPriorityItems;
}
