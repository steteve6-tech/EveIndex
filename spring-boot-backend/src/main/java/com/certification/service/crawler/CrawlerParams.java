package com.certification.service.crawler;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫执行参数统一封装
 * 用于屏蔽不同爬虫的参数差异，支持多字段参数
 */
@Data
@Accessors(chain = true)
public class CrawlerParams {

    /**
     * 关键词列表（保留用于向后兼容）
     */
    private List<String> keywords;

    /**
     * 多字段参数配置
     * Map<字段名, 关键词列表>
     *
     * 示例：
     * {
     *   "manufacturerNames": ["Abbott", "Medtronic"],
     *   "deviceNames": ["Skin Analyzer", "3D Scanner"],
     *   "proprietaryNames": ["VISIA"]
     * }
     */
    private Map<String, List<String>> fieldKeywords = new HashMap<>();

    /**
     * 最大记录数（-1表示全部）
     */
    private Integer maxRecords = -1;

    /**
     * 批次大小
     */
    private Integer batchSize = 100;

    /**
     * 开始日期（yyyyMMdd格式）
     */
    private String dateFrom;

    /**
     * 结束日期（yyyyMMdd格式）
     */
    private String dateTo;

    /**
     * 最近N天
     */
    private Integer recentDays;

    /**
     * 搜索词（单个关键词）
     */
    private String searchTerm;

    /**
     * 自定义搜索表达式
     */
    private String customSearch;

    /**
     * 任务ID（用于日志关联）
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 是否手动触发
     */
    private Boolean isManual = false;

    /**
     * 触发人
     */
    private String triggeredBy;

    /**
     * 额外参数（JSON格式）
     */
    private String extraParams;

    /**
     * 构建器模式：设置关键词列表
     */
    public static CrawlerParams withKeywords(List<String> keywords) {
        return new CrawlerParams().setKeywords(keywords);
    }

    /**
     * 构建器模式：设置单个搜索词
     */
    public static CrawlerParams withSearchTerm(String searchTerm) {
        return new CrawlerParams().setSearchTerm(searchTerm);
    }

    /**
     * 构建器模式：设置日期范围
     */
    public CrawlerParams dateRange(String dateFrom, String dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        return this;
    }

    /**
     * 构建器模式：设置最近N天
     */
    public CrawlerParams recentDays(int days) {
        this.recentDays = days;
        return this;
    }

    /**
     * 构建器模式：设置最大记录数和批次大小
     */
    public CrawlerParams limits(int maxRecords, int batchSize) {
        this.maxRecords = maxRecords;
        this.batchSize = batchSize;
        return this;
    }

    /**
     * 验证参数是否有效
     */
    public boolean isValid() {
        // 检查多字段参数、关键词或搜索词
        if ((fieldKeywords != null && !fieldKeywords.isEmpty()) ||
            (keywords != null && !keywords.isEmpty()) ||
            (searchTerm != null && !searchTerm.trim().isEmpty())) {

            // 批次大小必须大于0
            if (batchSize != null && batchSize <= 0) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * 获取关键词数量
     */
    public int getKeywordCount() {
        if (keywords != null && !keywords.isEmpty()) {
            return keywords.size();
        }
        if (fieldKeywords != null && !fieldKeywords.isEmpty()) {
            return fieldKeywords.values().stream()
                .mapToInt(List::size)
                .sum();
        }
        return 0;
    }
    
    /**
     * 设置字段关键词
     */
    public CrawlerParams setFieldKeywords(String fieldName, List<String> keywordList) {
        if (fieldKeywords == null) {
            fieldKeywords = new HashMap<>();
        }
        fieldKeywords.put(fieldName, keywordList);
        return this;
    }
    
    /**
     * 获取字段关键词
     */
    public List<String> getFieldKeywords(String fieldName) {
        if (fieldKeywords == null) {
            return null;
        }
        return fieldKeywords.get(fieldName);
    }
    
    /**
     * 是否使用多字段模式
     */
    public boolean isMultiFieldMode() {
        return fieldKeywords != null && !fieldKeywords.isEmpty();
    }
    
    /**
     * 获取配置的字段数量
     */
    public int getFieldCount() {
        return fieldKeywords != null ? fieldKeywords.size() : 0;
    }
    
    /**
     * 获取所有字段名
     */
    public java.util.Set<String> getFieldNames() {
        return fieldKeywords != null ? fieldKeywords.keySet() : java.util.Collections.emptySet();
    }
}

