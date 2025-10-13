package com.certification.service.crawler.schema;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫参数Schema定义
 * 定义每个爬虫支持的所有参数字段
 */
@Data
@Accessors(chain = true)
public class CrawlerSchema {
    
    /**
     * 爬虫名称
     */
    private String crawlerName;
    
    /**
     * 国家代码
     */
    private String countryCode;
    
    /**
     * 爬虫类型
     */
    private String crawlerType;
    
    /**
     * Schema描述
     */
    private String description;
    
    /**
     * 支持的参数字段列表
     */
    private List<CrawlerParameterField> fields = new ArrayList<>();
    
    /**
     * 通用参数字段（所有爬虫都支持）
     */
    private List<CrawlerParameterField> commonFields = new ArrayList<>();
    
    /**
     * 是否支持关键词批量爬取
     */
    private Boolean supportsKeywordBatch = true;
    
    /**
     * 是否支持日期范围
     */
    private Boolean supportsDateRange = true;
    
    /**
     * 示例配置
     */
    private String exampleConfig;
    
    /**
     * 添加字段
     */
    public CrawlerSchema addField(CrawlerParameterField field) {
        this.fields.add(field);
        return this;
    }
    
    /**
     * 添加通用字段
     */
    public CrawlerSchema addCommonField(CrawlerParameterField field) {
        this.commonFields.add(field);
        return this;
    }
    
    /**
     * 获取所有字段（包括通用字段）
     */
    public List<CrawlerParameterField> getAllFields() {
        List<CrawlerParameterField> allFields = new ArrayList<>();
        allFields.addAll(fields);
        allFields.addAll(commonFields);
        return allFields;
    }
    
    /**
     * 根据字段名查找字段
     */
    public CrawlerParameterField getField(String fieldName) {
        return getAllFields().stream()
            .filter(f -> f.getFieldName().equals(fieldName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取所有关键词列表字段
     */
    public List<CrawlerParameterField> getKeywordListFields() {
        return getAllFields().stream()
            .filter(f -> f.getFieldType() == CrawlerParameterField.FieldType.KEYWORD_LIST)
            .toList();
    }
}

