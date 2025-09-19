package com.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 爬虫数据响应DTO
 */
@Data
@Schema(description = "爬虫数据响应")
public class CrawlerDataResponse {
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "响应消息")
    private String message;
    
    @Schema(description = "响应时间戳")
    private String timestamp;
    
    @Schema(description = "响应数据")
    private DataWrapper data;
    
    @Schema(description = "错误信息")
    private String error;
    
    @Data
    @Schema(description = "数据包装器")
    public static class DataWrapper {
        
        @Schema(description = "数据列表")
        private List<CrawlerDataItem> content;
        
        @Schema(description = "总记录数")
        private Long totalElements;
        
        @Schema(description = "总页数")
        private Integer totalPages;
        
        @Schema(description = "当前页码")
        private Integer currentPage;
        
        @Schema(description = "每页大小")
        private Integer pageSize;
        
        @Schema(description = "是否有下一页")
        private Boolean hasNext;
        
        @Schema(description = "是否有上一页")
        private Boolean hasPrevious;
    }
    
    @Data
    @Schema(description = "爬虫数据项")
    public static class CrawlerDataItem {
        
        @Schema(description = "数据ID")
        private String id;
        
        @Schema(description = "标题")
        private String title;
        
        @Schema(description = "摘要")
        private String summary;
        
        @Schema(description = "内容")
        private String content;
        
        @Schema(description = "URL")
        private String url;
        
        @Schema(description = "数据源名称")
        private String sourceName;
        
        @Schema(description = "国家")
        private String country;
        
        @Schema(description = "类型")
        private String type;
        
        @Schema(description = "产品")
        private String product;
        
        @Schema(description = "相关性")
        private Boolean related;
        
        @Schema(description = "状态")
        private String status;
        
        @Schema(description = "备注")
        private String remarks;
        
        @Schema(description = "爬取时间")
        private LocalDateTime crawlTime;
        
        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
        
        @Schema(description = "更新时间")
        private LocalDateTime updatedAt;
    }
}
