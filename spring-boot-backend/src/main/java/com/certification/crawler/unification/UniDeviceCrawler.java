package com.certification.crawler.unification;

import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.certification.ULCrawler;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 统一爬虫测试类
 * 用于测试各种爬虫的批量保存功能
 */
@Slf4j
@Component
public class UniDeviceCrawler {
    
    @Autowired
    private ULCrawler ulCrawler;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    /**
     * 测试UL爬虫的批量保存功能
     * @param totalCount 要爬取的总数量
     */
    public void testULCrawlerBatchSave(int totalCount) {
        testULCrawlerBatchSave(totalCount, 10, 0);
    }
    
    /**
     * 测试UL爬虫的批量保存功能（带参数）
     * @param totalCount 要爬取的总数量
     * @param batchSize 每批保存数量
     * @param startIndex 开始位置索引
     */
    public void testULCrawlerBatchSave(int totalCount, int batchSize, int startIndex) {
        log.info("开始测试UL爬虫批量保存功能，目标数量: {}, 每批保存: {}, 开始位置: {}", totalCount, batchSize, startIndex);
        
        try {
            // 执行爬取
            List<CrawlerResult> results = ulCrawler.crawlWithBatchSave("", totalCount, batchSize, startIndex);
            
            log.info("UL爬虫执行完成，共获取 {} 条结果", results.size());
            
            // 输出统计信息
            log.info("=== UL爬虫执行统计 ===");
            log.info("目标数量: {}", totalCount);
            log.info("实际获取: {}", results.size());
            log.info("每批保存数量: {}", batchSize);
            log.info("开始位置: {}", startIndex);
            log.info("成功率: {:.2f}%", (double) results.size() / totalCount * 100);
            
            // 按类型统计
            long announcementCount = results.stream()
                    .filter(r -> "announcement".equals(r.getType()))
                    .count();
            long otherTypeCount = results.size() - announcementCount;
            
            log.info("公告类型: {} 条", announcementCount);
            log.info("其他类型: {} 条", otherTypeCount);
            
        } catch (Exception e) {
            log.error("UL爬虫测试失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从指定位置继续执行UL爬虫
     * @param totalCount 要爬取的总数量
     * @param startIndex 开始位置索引
     */
    public void testULCrawlerContinueFromPosition(int totalCount, int startIndex) {
        log.info("开始从位置 {} 继续执行UL爬虫，目标数量: {}", startIndex, totalCount);
        
        try {
            // 执行爬取
            List<CrawlerResult> results = ulCrawler.crawlFromPosition("", totalCount, startIndex);
            
            log.info("UL爬虫从位置 {} 继续执行完成，共获取 {} 条结果", startIndex, results.size());
            
            // 输出统计信息
            log.info("=== UL爬虫继续执行统计 ===");
            log.info("目标数量: {}", totalCount);
            log.info("实际获取: {}", results.size());
            log.info("开始位置: {}", startIndex);
            log.info("下次开始位置: {}", startIndex + results.size());
            log.info("成功率: {:.2f}%", (double) results.size() / totalCount * 100);
            
        } catch (Exception e) {
            log.error("UL爬虫继续执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取UL爬虫可爬取的总数量
     * @return 可爬取的总数量
     */
    public int getULTotalCount() {
        try {
            int totalCount = ulCrawler.getTotalAvailableCount();
            log.info("UL爬虫可爬取的总数量: {}", totalCount);
            return totalCount;
        } catch (Exception e) {
            log.error("获取UL爬虫总数量失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 测试UL爬虫的最新数据爬取
     * @param totalCount 要爬取的总数量
     */
    public void testULCrawlerLatest(int totalCount) {
        log.info("开始测试UL爬虫最新数据爬取，目标数量: {}", totalCount);
        
        try {
            // 执行爬取
            List<CrawlerResult> results = ulCrawler.crawlLatest(totalCount);
            
            log.info("UL爬虫最新数据爬取完成，共获取 {} 条结果", results.size());
            
            // 输出统计信息
            log.info("=== UL爬虫最新数据统计 ===");
            log.info("目标数量: {}", totalCount);
            log.info("实际获取: {}", results.size());
            log.info("成功率: {:.2f}%", (double) results.size() / totalCount * 100);
            
        } catch (Exception e) {
            log.error("UL爬虫最新数据测试失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 测试爬虫可用性
     */
    public void testCrawlerAvailability() {
        log.info("开始测试爬虫可用性");
        
        // 测试UL爬虫
        boolean ulAvailable = ulCrawler.isAvailable();
        log.info("UL爬虫可用性: {}", ulAvailable ? "可用" : "不可用");
        
        if (ulAvailable) {
            log.info("UL爬虫配置信息:");
            log.info("  爬虫名称: {}", ulCrawler.getCrawlerName());
            log.info("  数据源名称: {}", ulCrawler.getSourceName());
            log.info("  基础URL: {}", ulCrawler.getConfig().getBaseUrl());
            log.info("  超时时间: {}ms", ulCrawler.getConfig().getTimeout());
            log.info("  重试次数: {}", ulCrawler.getConfig().getRetryCount());
        }
    }
    
    /**
     * 执行完整的爬虫测试
     * @param totalCount 要爬取的总数量
     */
    public void runFullTest(int totalCount) {
        log.info("=== 开始执行完整爬虫测试 ===");
        
        // 1. 测试爬虫可用性
        testCrawlerAvailability();
        
        // 2. 测试批量保存功能
        testULCrawlerBatchSave(totalCount);
        
        // 3. 测试最新数据爬取
        testULCrawlerLatest(Math.min(totalCount, 10)); // 最新数据测试用较小的数量
        
        log.info("=== 完整爬虫测试完成 ===");
    }
}
