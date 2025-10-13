package com.certification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 设备数据爬虫配置类
 * 从application.yml读取爬虫相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "crawler.device")
public class DeviceCrawlerConfig {
    
    /**
     * 默认批次大小
     */
    private Integer defaultBatchSize = 50;
    
    /**
     * 默认最大记录数
     */
    private Integer defaultMaxRecords = 200;
    
    /**
     * 是否启用爬取后自动AI判断
     */
    private Boolean enableAutoAi = true;
    
    /**
     * 是否异步执行AI判断
     */
    private Boolean aiJudgeAsync = true;
}

