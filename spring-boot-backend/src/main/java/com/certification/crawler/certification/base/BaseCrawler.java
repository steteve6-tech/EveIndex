package com.certification.crawler.certification.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 认证信息爬虫基础接口
 */
public interface BaseCrawler {
    
    /**
     * 爬取指定关键词的内容
     * @param keyword 搜索关键词
     * @param totalCount 需要爬取的内容总数
     * @return 爬取结果列表
     * @throws Exception 爬取异常
     */
    List<CrawlerResult> crawl(String keyword, int totalCount) throws Exception;
    
    /**
     * 爬取指定数量的最新内容
     * @param totalCount 需要爬取的内容总数
     * @return 爬取结果列表
     * @throws Exception 爬取异常
     */
    List<CrawlerResult> crawlLatest(int totalCount) throws Exception;
    
    /**
     * 获取爬虫名称
     * @return 爬虫名称
     */
    String getCrawlerName();
    
    /**
     * 获取爬虫支持的源网站
     * @return 源网站名称
     */
    String getSourceName();
    
    /**
     * 检查爬虫是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取爬虫配置信息
     * @return 配置信息
     */
    CrawlerConfig getConfig();
    
    /**
     * 爬虫配置类
     */
    @Data
    @NoArgsConstructor
    class CrawlerConfig {
        private String baseUrl;
        private int timeout = 30000; // 默认30秒超时
        private int retryCount = 3;  // 默认重试3次
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    }
}

