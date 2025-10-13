package com.certification.service.crawler.schema;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 爬虫参数字段定义
 * 用于描述爬虫支持的每个搜索字段
 */
@Data
@Accessors(chain = true)
public class CrawlerParameterField {
    
    /**
     * 字段名称（英文标识）
     */
    private String fieldName;
    
    /**
     * 字段显示名称（中文）
     */
    private String fieldLabel;
    
    /**
     * 字段类型
     */
    private FieldType fieldType;
    
    /**
     * 是否必填
     */
    private Boolean required = false;
    
    /**
     * 字段描述
     */
    private String description;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 关键词列表（当fieldType为KEYWORD_LIST时使用）
     */
    private List<String> keywords;
    
    /**
     * 是否支持批量关键词
     */
    private Boolean supportsBatch = true;
    
    /**
     * 占位符文本
     */
    private String placeholder;
    
    /**
     * 验证规则
     */
    private String validationRule;
    
    /**
     * 字段类型枚举
     */
    public enum FieldType {
        KEYWORD_LIST,      // 关键词列表（数组）
        TEXT,              // 单个文本
        DATE,              // 日期
        DATE_RANGE,        // 日期范围
        NUMBER,            // 数字
        BOOLEAN,           // 布尔值
        SELECT             // 下拉选择
    }
    
    /**
     * 创建关键词列表字段
     */
    public static CrawlerParameterField keywordList(String fieldName, String fieldLabel) {
        return new CrawlerParameterField()
            .setFieldName(fieldName)
            .setFieldLabel(fieldLabel)
            .setFieldType(FieldType.KEYWORD_LIST)
            .setSupportsBatch(true);
    }
    
    /**
     * 创建文本字段
     */
    public static CrawlerParameterField text(String fieldName, String fieldLabel) {
        return new CrawlerParameterField()
            .setFieldName(fieldName)
            .setFieldLabel(fieldLabel)
            .setFieldType(FieldType.TEXT);
    }
    
    /**
     * 创建数字字段
     */
    public static CrawlerParameterField number(String fieldName, String fieldLabel, Object defaultValue) {
        return new CrawlerParameterField()
            .setFieldName(fieldName)
            .setFieldLabel(fieldLabel)
            .setFieldType(FieldType.NUMBER)
            .setDefaultValue(defaultValue);
    }
    
    /**
     * 创建日期范围字段
     */
    public static CrawlerParameterField dateRange(String fieldName, String fieldLabel) {
        return new CrawlerParameterField()
            .setFieldName(fieldName)
            .setFieldLabel(fieldLabel)
            .setFieldType(FieldType.DATE_RANGE);
    }
}

