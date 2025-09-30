package com.certification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Medcert爬虫公共配置类
 * 统一管理所有爬虫的重试机制、超时配置和批量保存配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "medcert.crawler")
public class MedcertCrawlerConfig {
    
    /**
     * 重试机制配置
     */
    private RetryConfig retry = new RetryConfig();
    
    /**
     * 超时配置
     */
    private TimeoutConfig timeout = new TimeoutConfig();
    
    /**
     * 批量保存配置
     */
    private BatchConfig batch = new BatchConfig();
    
    /**
     * 爬取参数配置
     */
    private CrawlConfig crawl = new CrawlConfig();
    
    @Data
    public static class RetryConfig {
        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;
        
        /**
         * 重试延迟时间（秒）
         */
        private int delaySeconds = 5;
        
        /**
         * 重试延迟时间（毫秒）
         */
        private int delayMilliseconds = 5000;
    }
    
    @Data
    public static class TimeoutConfig {
        /**
         * 等待超时时间（秒）
         */
        private int waitTimeoutSeconds = 30;
        
        /**
         * 页面加载超时时间（秒）
         */
        private int pageLoadTimeoutSeconds = 60;
        
        /**
         * HTTP请求超时时间（毫秒）
         */
        private int httpTimeoutMilliseconds = 30000;
    }
    
    @Data
    public static class BatchConfig {
        /**
         * 批量保存大小
         */
        private int saveSize = 50;
        
        /**
         * 小批量保存大小（用于某些特殊场景）
         */
        private int smallSaveSize = 20;
    }
    
    @Data
    public static class CrawlConfig {
        /**
         * 默认最大页数
         */
        private int defaultMaxPages = 5;
        
        /**
         * 默认批次大小
         */
        private int defaultBatchSize = 50;
        
        /**
         * 每页记录数（用于计算maxRecords）
         */
        private int recordsPerPage = 100;
        
        /**
         * API限制配置
         */
        private ApiLimitsConfig apiLimits = new ApiLimitsConfig();
        
        @Data
        public static class ApiLimitsConfig {
            /**
             * US_510K API每页最大记录数
             */
            private int us510kMaxPerPage = 100;
            
            /**
             * US_recall_api每页最大记录数
             */
            private int usRecallMaxPerPage = 1000;
            
            /**
             * US_CustomsCase每页最大记录数
             */
            private int usCustomsCaseMaxPerPage = 10;
            
            /**
             * US_event每页最大记录数
             */
            private int usEventMaxPerPage = 500;
            
            // 欧盟爬虫API限制
            /**
             * EU_CustomCase每页最大记录数
             */
            private int euCustomCaseMaxPerPage = 20;
            
            /**
             * EU_Guidance每页最大记录数
             */
            private int euGuidanceMaxPerPage = 50;
            
            /**
             * EU_Recall每页最大记录数
             */
            private int euRecallMaxPerPage = 100;
            
            /**
             * EU_Registration每页最大记录数
             */
            private int euRegistrationMaxPerPage = 100;
        }
    }
}
