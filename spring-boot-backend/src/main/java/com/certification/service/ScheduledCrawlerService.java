package com.certification.service;

import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.certification.SgsCrawler;
import com.certification.crawler.certification.ULCrawler;
import com.certification.entity.common.CertNewsData;
import com.certification.standards.CrawlerDataService;
import com.certification.standards.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 定时爬虫服务
 * 每天自动执行爬虫任务并处理数据
 */
@Slf4j
@Service
@Transactional
public class ScheduledCrawlerService {

    @Autowired
    private SgsCrawler sgsCrawler;
    
    @Autowired
    private ULCrawler ulCrawler;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * 每天凌晨2点执行爬虫任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyCrawlerTask() {
        log.info("开始执行每日定时爬虫任务: {}", LocalDateTime.now());
        
        try {
            // 并行执行两个爬虫
            CompletableFuture<Void> sgsFuture = CompletableFuture.runAsync(() -> {
                try {
                    executeSgsCrawler();
                } catch (Exception e) {
                    log.error("SGS爬虫定时任务执行失败: {}", e.getMessage(), e);
                }
            }, executorService);
            
            CompletableFuture<Void> ulFuture = CompletableFuture.runAsync(() -> {
                try {
                    executeUlCrawler();
                } catch (Exception e) {
                    log.error("UL爬虫定时任务执行失败: {}", e.getMessage(), e);
                }
            }, executorService);
            
            // 等待所有任务完成
            CompletableFuture.allOf(sgsFuture, ulFuture).join();
            
            // 执行数据后处理
            processNewData();
            
            log.info("每日定时爬虫任务执行完成: {}", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("每日定时爬虫任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 执行SGS爬虫
     */
    private void executeSgsCrawler() throws Exception {
        log.info("开始执行SGS爬虫定时任务");
        
        // 获取上次爬取时间
        LocalDateTime lastCrawlTime = getLastCrawlTime("SGS");
        
        // 执行增量爬取
        List<CrawlerResult> newResults = crawlIncremental(sgsCrawler, "SGS", lastCrawlTime);
        
        // 保存到数据库
        int savedCount = saveCrawlerResults(newResults, "SGS");
        
        // 更新最后爬取时间
        updateLastCrawlTime("SGS", LocalDateTime.now());
        
        log.info("SGS爬虫定时任务完成，新增数据: {} 条", savedCount);
    }

    /**
     * 执行UL爬虫
     */
    private void executeUlCrawler() throws Exception {
        log.info("开始执行UL爬虫定时任务");
        
        // 获取上次爬取时间
        LocalDateTime lastCrawlTime = getLastCrawlTime("UL");
        
        // 执行增量爬取
        List<CrawlerResult> newResults = crawlIncremental(ulCrawler, "UL", lastCrawlTime);
        
        // 保存到数据库
        int savedCount = saveCrawlerResults(newResults, "UL");
        
        // 更新最后爬取时间
        updateLastCrawlTime("UL", LocalDateTime.now());
        
        log.info("UL爬虫定时任务完成，新增数据: {} 条", savedCount);
    }

    /**
     * 增量爬取方法
     */
    private List<CrawlerResult> crawlIncremental(Object crawler, String crawlerName, LocalDateTime lastCrawlTime) throws Exception {
        List<CrawlerResult> allNewResults = new ArrayList<>();
        int batchSize = 20;
        int maxBatches = 50;
        int batchCount = 0;
        
        log.info("开始增量爬取{}，上次爬取时间: {}", crawlerName, lastCrawlTime);
        
        while (batchCount < maxBatches) {
            batchCount++;
            log.info("{}爬虫第{}批爬取，每批{}条", crawlerName, batchCount, batchSize);
            
            // 调用爬虫的crawlLatest方法
            List<CrawlerResult> batchResults;
            if (crawler instanceof SgsCrawler) {
                batchResults = ((SgsCrawler) crawler).crawlLatest(batchSize);
            } else if (crawler instanceof ULCrawler) {
                batchResults = ((ULCrawler) crawler).crawlLatest(batchSize);
            } else {
                throw new IllegalArgumentException("不支持的爬虫类型: " + crawler.getClass().getSimpleName());
            }
            
            if (batchResults.isEmpty()) {
                log.info("{}爬虫第{}批没有获取到数据，停止爬取", crawlerName, batchCount);
                break;
            }
            
            // 过滤新数据
            List<CrawlerResult> newBatchResults = new ArrayList<>();
            boolean foundDuplicate = false;
            
            for (CrawlerResult result : batchResults) {
                // 检查是否已存在
                if (isDataExists(result)) {
                    log.info("{}爬虫发现重复数据，URL: {}，停止爬取", crawlerName, result.getUrl());
                    foundDuplicate = true;
                    break;
                }
                
                // 检查是否比上次爬取时间更新
                if (!isNewerThanLastCrawl(result, lastCrawlTime)) {
                    log.info("{}爬虫到达上次爬取位置，停止爬取", crawlerName);
                    foundDuplicate = true;
                    break;
                }
                
                newBatchResults.add(result);
            }
            
            // 添加新数据到总结果
            allNewResults.addAll(newBatchResults);
            
            // 如果发现重复数据或到达上次爬取位置，停止爬取
            if (foundDuplicate) {
                log.info("{}爬虫在第{}批发现重复数据或到达上次爬取位置，停止爬取", crawlerName, batchCount);
                break;
            }
            
            // 如果这批数据少于batchSize，说明已经爬取完了
            if (batchResults.size() < batchSize) {
                log.info("{}爬虫第{}批数据少于{}条，说明已爬取完毕", crawlerName, batchCount, batchSize);
                break;
            }
            
            // 添加延迟，避免请求过于频繁
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("{}爬虫增量爬取完成，共爬取{}批，获得{}条新数据", crawlerName, batchCount, allNewResults.size());
        return allNewResults;
    }

    /**
     * 处理新数据 - 自动更新国家和处理数据
     */
    private void processNewData() {
        log.info("开始处理新数据");
        
        try {
            // 获取今天新增的数据
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            List<CertNewsData> todayData = crawlerDataService.findByCrawlTimeBetween(today, LocalDateTime.now());
            
            log.info("找到今天新增数据: {} 条", todayData.size());
            
            // 自动更新国家信息
            dataProcessingService.autoUpdateCountries(todayData);
            
            // 自动处理数据（关键词匹配、风险等级评估等）
            dataProcessingService.autoProcessData(todayData);
            
            log.info("新数据处理完成");
            
        } catch (Exception e) {
            log.error("处理新数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取上次爬取时间
     */
    private LocalDateTime getLastCrawlTime(String crawlerName) {
        try {
            List<CertNewsData> lastData = crawlerDataService.findLatestDataBySource(crawlerName, 1);
            if (!lastData.isEmpty()) {
                return lastData.get(0).getCrawlTime();
            }
        } catch (Exception e) {
            log.warn("获取{}爬虫最后爬取时间失败: {}", crawlerName, e.getMessage());
        }
        return LocalDateTime.now().minusDays(1);
    }

    /**
     * 更新最后爬取时间
     */
    private void updateLastCrawlTime(String crawlerName, LocalDateTime crawlTime) {
        log.info("更新{}爬虫最后爬取时间: {}", crawlerName, crawlTime);
    }

    /**
     * 检查数据是否已存在
     */
    private boolean isDataExists(CrawlerResult result) {
        try {
            CertNewsData existingData = crawlerDataService.findByUrl(result.getUrl());
            return existingData != null;
        } catch (Exception e) {
            log.warn("检查数据是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查数据是否比上次爬取时间更新
     */
    private boolean isNewerThanLastCrawl(CrawlerResult result, LocalDateTime lastCrawlTime) {
        try {
            if (result.getCrawlTime() != null) {
                return result.getCrawlTime().isAfter(lastCrawlTime);
            }
            return true;
        } catch (Exception e) {
            log.warn("检查数据时间失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 保存爬虫结果到数据库
     */
    private int saveCrawlerResults(List<CrawlerResult> results, String sourceName) {
        int savedCount = 0;
        for (CrawlerResult result : results) {
            try {
                CertNewsData certNewsData = new CertNewsData();
                certNewsData.setTitle(result.getTitle());
                certNewsData.setUrl(result.getUrl());
                certNewsData.setContent(result.getContent());
                certNewsData.setSourceName(sourceName);
                certNewsData.setCrawlTime(LocalDateTime.now());
                certNewsData.setPublishDate(result.getDate());
                certNewsData.setCountry(result.getCountry());
                certNewsData.setType(result.getType());
                
                crawlerDataService.saveCrawlerData(certNewsData);
                savedCount++;
            } catch (Exception e) {
                log.error("保存爬虫数据失败: {}", e.getMessage(), e);
            }
        }
        return savedCount;
    }
}
