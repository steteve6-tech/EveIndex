package com.certification.standards;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CrawlerData关键词匹配定时任务
 * 定期执行关键词匹配，更新CrawlerData的related字段
 */
@Slf4j
@Component
public class CrawlerDataKeywordMatcherScheduler {

    @Autowired
    private CrawlerDataKeywordMatcher crawlerDataKeywordMatcher;

    /**
     * 每天凌晨2点执行关键词匹配
     * 对所有未处理的CrawlerData执行关键词匹配
     * 暂时关闭自动更新
     */
    // @Scheduled(cron = "0 0 2 * * ?")
    public void executeDailyKeywordMatching() {
        log.info("开始执行每日关键词匹配任务");
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.executeKeywordMatching(100);
            
            if ((Boolean) result.get("success")) {
                log.info("每日关键词匹配任务执行成功，处理: {} 条数据，匹配: {} 条，不匹配: {} 条，耗时: {} ms",
                    result.get("totalProcessed"),
                    result.get("totalMatched"),
                    result.get("totalUnmatched"),
                    result.get("executionTime"));
            } else {
                log.error("每日关键词匹配任务执行失败: {}", result.get("error"));
            }
            
        } catch (Exception e) {
            log.error("每日关键词匹配任务执行异常", e);
        }
    }

    /**
     * 每小时执行一次关键词匹配（仅对SGS数据源）
     * 对SGS数据源的未处理CrawlerData执行关键词匹配
     * 暂时关闭自动更新
     */
    // @Scheduled(cron = "0 0 * * * ?")
    public void executeHourlySgsKeywordMatching() {
        log.info("开始执行每小时SGS关键词匹配任务");
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.executeKeywordMatchingBySource("SGS", 50);
            
            if ((Boolean) result.get("success")) {
                log.info("每小时SGS关键词匹配任务执行成功，处理: {} 条数据，匹配: {} 条，不匹配: {} 条，耗时: {} ms",
                    result.get("totalProcessed"),
                    result.get("totalMatched"),
                    result.get("totalUnmatched"),
                    result.get("executionTime"));
            } else {
                log.error("每小时SGS关键词匹配任务执行失败: {}", result.get("error"));
            }
            
        } catch (Exception e) {
            log.error("每小时SGS关键词匹配任务执行异常", e);
        }
    }

    /**
     * 每30分钟执行一次关键词匹配（仅对UL数据源）
     * 对UL数据源的未处理CrawlerData执行关键词匹配
     * 暂时关闭自动更新
     */
    // @Scheduled(cron = "0 */30 * * * ?")
    public void executeHalfHourlyUlKeywordMatching() {
        log.info("开始执行每30分钟UL关键词匹配任务");
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.executeKeywordMatchingBySource("UL", 30);
            
            if ((Boolean) result.get("success")) {
                log.info("每30分钟UL关键词匹配任务执行成功，处理: {} 条数据，匹配: {} 条，不匹配: {} 条，耗时: {} ms",
                    result.get("totalProcessed"),
                    result.get("totalMatched"),
                    result.get("totalUnmatched"),
                    result.get("executionTime"));
            } else {
                log.error("每30分钟UL关键词匹配任务执行失败: {}", result.get("error"));
            }
            
        } catch (Exception e) {
            log.error("每30分钟UL关键词匹配任务执行异常", e);
        }
    }

    /**
     * 每周日凌晨3点执行完整的关键词匹配统计
     * 生成详细的匹配统计报告
     * 暂时关闭自动更新
     */
    // @Scheduled(cron = "0 0 3 ? * SUN")
    public void executeWeeklyKeywordMatchingStatistics() {
        log.info("开始执行每周关键词匹配统计任务");
        
        try {
            Map<String, Object> statistics = crawlerDataKeywordMatcher.getMatchingStatistics();
            
            if ((Boolean) statistics.get("success")) {
                log.info("每周关键词匹配统计任务执行成功");
                log.info("总体统计: {}", statistics.get("overall"));
                log.info("数据源统计: {}", statistics.get("sourceStats"));
            } else {
                log.error("每周关键词匹配统计任务执行失败: {}", statistics.get("error"));
            }
            
        } catch (Exception e) {
            log.error("每周关键词匹配统计任务执行异常", e);
        }
    }
}
