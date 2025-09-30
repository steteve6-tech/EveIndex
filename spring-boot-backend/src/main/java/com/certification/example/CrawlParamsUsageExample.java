package com.certification.example;

import com.certification.util.CrawlParamsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 爬取参数使用示例
 * 展示如何在爬虫服务中使用统一的参数配置
 */
@Component
public class CrawlParamsUsageExample {
    
    @Autowired
    private CrawlParamsUtil crawlParamsUtil;
    
    /**
     * 示例：在USCrawlerService中使用统一配置
     */
    public void exampleUsage() {
        // 1. 获取默认参数
        int[] defaultParams = crawlParamsUtil.getDefaultParams("US_510K");
        int defaultMaxPages = defaultParams[0];    // 5
        int defaultBatchSize = defaultParams[1];   // 50
        int defaultMaxRecords = defaultParams[2];  // 500 (5 * 100)
        
        // 2. 根据用户输入计算实际参数
        Integer userMaxPages = 10; // 用户输入的页数
        int actualMaxRecords = crawlParamsUtil.calculateMaxRecords(userMaxPages); // 1000
        
        // 3. 获取考虑API限制的批次大小
        int us510kBatchSize = crawlParamsUtil.getUS510KBatchSize(50, actualMaxRecords); // 50 (受API限制100)
        int usRecallBatchSize = crawlParamsUtil.getUSRecallBatchSize(50, actualMaxRecords); // 50 (受API限制1000)
        int usCustomsCaseBatchSize = crawlParamsUtil.getUSCustomsCaseBatchSize(50, actualMaxRecords); // 10 (受API限制10)
        
        // 4. 在爬虫调用中使用
        // 例如：us510kCrawler.crawlAndSaveDevice510K(searchTerm, actualMaxRecords, us510kBatchSize, dateFrom, dateTo);
    }
    
    /**
     * 示例：替换USCrawlerService中的硬编码参数
     */
    public void replaceHardcodedParams() {
        // 原来的硬编码方式：
        // Integer maxPages = (Integer) params.getOrDefault("maxPages", 5);
        // int maxRecords = (maxPages == 0) ? -1 : maxPages * 100;
        // crawlResult = us510kCrawler.crawlAndSaveDevice510K(searchTerm, maxRecords, 50, dateFrom, dateTo);
        
        // 新的统一配置方式：
        // Integer maxPages = (Integer) params.getOrDefault("maxPages", crawlParamsUtil.getDefaultMaxPages());
        // int maxRecords = crawlParamsUtil.calculateMaxRecords(maxPages);
        // int batchSize = crawlParamsUtil.getUS510KBatchSize(50, maxRecords);
        // crawlResult = us510kCrawler.crawlAndSaveDevice510K(searchTerm, maxRecords, batchSize, dateFrom, dateTo);
    }
}


