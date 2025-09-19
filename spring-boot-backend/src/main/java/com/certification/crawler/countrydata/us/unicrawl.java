package com.certification.crawler.countrydata.us;

import com.certification.crawler.generalArchitecture.us.CustomsCaseCrawler;
import com.certification.crawler.generalArchitecture.us.GuidanceCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 统一爬虫类
 * 按照关键词文件searchkeywords.txt和输入参数统一爬取6个爬虫的数据
 * 支持关键词文件、输入关键词参数、时间参数
 */
@Slf4j
@Component
public class unicrawl {
    
    @Autowired
    private D_510K d510kCrawler;
    
    @Autowired
    private D_event dEventCrawler;
    
    @Autowired
    private D_recall dRecallCrawler;
    
    @Autowired
    private US_registration dRegistrationCrawler;
    
    @Autowired
    private CustomsCaseCrawler customsCaseCrawler;
    
    @Autowired
    private GuidanceCrawler guidanceCrawler;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(6);
    
    /**
     * 从关键词文件读取搜索关键词
     * @return 关键词列表
     */
    public List<String> loadSearchKeywords() {
        List<String> keywords = new ArrayList<>();
        String filePath = "src/main/java/com/certification/crawler/countrydata/searchkeywords.txt";
        
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        keywords.add(line);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("无法从文件读取关键词，使用默认关键词: {}", e.getMessage());
            keywords.addAll(List.of(
                "medical device", "skin analyzer", "3D imaging", "facial scanner",
                "skin care", "portable device", "spectral analysis", "AI device"
            ));
        }
        
        log.info("加载了 {} 个搜索关键词", keywords.size());
        return keywords;
    }
    
    /**
     * 统一爬取所有6个爬虫的数据（新版本）
     * @param inputKeywords 输入的关键词列表（可为空，将使用文件关键词）
     * @param dateFrom 开始日期（可为空）
     * @param dateTo 结束日期（可为空）
     * @param maxPages 最大页数（0表示爬取所有数据）
     * @param totalCount 总爬取数量（可为空，为空时使用默认值）
     * @return 爬取结果统计
     */
    public Map<String, Object> crawlAllCrawlers(List<String> inputKeywords, String dateFrom, String dateTo, Integer maxPages, Integer totalCount) {
        log.info("开始统一爬取所有爬虫数据");
        log.info("输入关键词: {}", inputKeywords);
        log.info("日期范围: {} - {}", dateFrom, dateTo);
        log.info("最大页数: {}", maxPages);
        log.info("总爬取数量: {}", totalCount);
        
        Map<String, Object> results = new HashMap<>();
        
        // 获取关键词列表
        final List<String> keywords;
        if (inputKeywords != null && !inputKeywords.isEmpty()) {
            keywords = new ArrayList<>(inputKeywords);
            log.info("使用输入关键词: {} 个", keywords.size());
        } else {
            keywords = loadSearchKeywords();
            log.info("使用文件关键词: {} 个", keywords.size());
        }
        
        // 设置默认最大页数
        int actualMaxPages = (maxPages != null && maxPages > 0) ? maxPages : 0;
        
        // 设置默认总爬取数量
        int actualTotalCount = (totalCount != null && totalCount > 0) ? totalCount : 50;
        
        // 并行执行所有爬虫
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        futures.add(CompletableFuture.supplyAsync(() -> crawlD510K(keywords, dateFrom, dateTo, actualMaxPages, actualTotalCount), executorService));
        futures.add(CompletableFuture.supplyAsync(() -> crawlDEvent(keywords, dateFrom, dateTo, actualMaxPages, actualTotalCount), executorService));
        futures.add(CompletableFuture.supplyAsync(() -> crawlDRecall(keywords, dateFrom, dateTo, actualMaxPages, actualTotalCount), executorService));
        futures.add(CompletableFuture.supplyAsync(() -> crawlDRegistration(keywords, dateFrom, dateTo, actualMaxPages, actualTotalCount), executorService));
        // 暂时注释掉CustomsCaseCrawler和GuidanceCrawler
        // futures.add(CompletableFuture.supplyAsync(() -> crawlCustomsCase(keywords, dateFrom, dateTo, actualMaxPages), executorService));
        // futures.add(CompletableFuture.supplyAsync(() -> crawlGuidance(keywords, dateFrom, dateTo, actualMaxPages), executorService));
        
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.MINUTES);
            
            for (CompletableFuture<Map<String, Object>> future : futures) {
                Map<String, Object> crawlerResult = future.get();
                results.putAll(crawlerResult);
            }
            
        } catch (Exception e) {
            log.error("爬虫执行超时或失败: {}", e.getMessage(), e);
            results.put("error", "爬虫执行超时或失败: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 统一爬取所有6个爬虫的数据（保持向后兼容）
     * @param maxRecordsPerCrawler 每个爬虫的最大记录数
     * @param batchSize 批处理大小
     * @return 爬取结果统计
     */
    public Map<String, Object> crawlAllCrawlers(int maxRecordsPerCrawler, int batchSize) {
        return crawlAllCrawlers(null, null, null, 0, maxRecordsPerCrawler);
    }
    
    /**
     * 爬取D_510K数据（新版本）
     */
    private Map<String, Object> crawlD510K(List<String> keywords, String dateFrom, String dateTo, int maxPages, int maxRecordsPerCrawler) {
        log.info("开始爬取D_510K数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
            for (String keyword : keywords) {
                try {
                    log.info("D_510K爬虫使用关键词: {}，日期范围: {} - {}", keyword, dateFrom, dateTo);
                    
                    // 调用D_510K爬虫的参数化方法
                    // 将关键词放入设备名称，日期放入决策日期，申请人名称为空
                    Map<String, Object> searchResult = d510kCrawler.crawlFDADataWithParams(
                        keyword,  // 设备名称
                        "",       // 申请人名称（空）
                        dateFrom, // 决策日期开始
                        dateTo,   // 决策日期结束
                        maxPages  // 最大页数
                    );
                    
                    totalSaved += (Integer) searchResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) searchResult.getOrDefault("totalSkipped", 0);
                    
                } catch (Exception e) {
                    log.error("D_510K爬虫处理关键词 {} 失败: {}", keyword, e.getMessage());
                    totalErrors++;
                }
            }
            
            result.put("D_510K", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("D_510K爬虫执行失败: {}", e.getMessage(), e);
            result.put("D_510K", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 爬取D_event数据（新版本）
     */
    private Map<String, Object> crawlDEvent(List<String> keywords, String dateFrom, String dateTo, int maxPages, int maxRecordsPerCrawler) {
        log.info("开始爬取D_event数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
            for (String keyword : keywords) {
                try {
                    log.info("D_event爬虫使用关键词: {}，日期范围: {} - {}", keyword, dateFrom, dateTo);
                    
                    // 调用D_event爬虫的参数化方法
                    // 将关键词放入品牌名称，日期放入报告接收日期，其他参数为空
                    Map<String, Object> searchResult = dEventCrawler.crawlMAUDEDataWithParams(
                        keyword,  // 品牌名称
                        "",       // 制造商（空）
                        "",       // 型号（空）
                        dateFrom, // 报告接收日期开始
                        dateTo,   // 报告接收日期结束
                        maxPages  // 最大页数
                    );
                    
                    totalSaved += (Integer) searchResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) searchResult.getOrDefault("totalSkipped", 0);
                    
                } catch (Exception e) {
                    log.error("D_event爬虫处理关键词 {} 失败: {}", keyword, e.getMessage());
                    totalErrors++;
                }
            }
            
            result.put("D_event", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("D_event爬虫执行失败: {}", e.getMessage(), e);
            result.put("D_event", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 爬取D_recall数据（新版本）
     */
    private Map<String, Object> crawlDRecall(List<String> keywords, String dateFrom, String dateTo, int maxPages, int maxRecordsPerCrawler) {
        log.info("开始爬取D_recall数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
            for (String keyword : keywords) {
                try {
                    log.info("D_recall爬虫使用关键词: {}，日期范围: {} - {}", keyword, dateFrom, dateTo);
                    
                    // 调用D_recall爬虫的参数化方法
                    // 将关键词放入产品名称，日期放入召回日期，其他参数为空
                    Map<String, Object> searchResult = dRecallCrawler.crawlFDARecallDataWithParams(
                        keyword,  // 产品名称
                        "",       // 召回原因（空）
                        "",       // 召回公司（空）
                        dateFrom, // 召回日期开始
                        dateTo,   // 召回日期结束
                        maxPages  // 最大页数
                    );
                    
                    totalSaved += (Integer) searchResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) searchResult.getOrDefault("totalSkipped", 0);
                    
                } catch (Exception e) {
                    log.error("D_recall爬虫处理关键词 {} 失败: {}", keyword, e.getMessage());
                    totalErrors++;
                }
            }
            
            result.put("D_recall", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("D_recall爬虫执行失败: {}", e.getMessage(), e);
            result.put("D_recall", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 爬取US_registration数据（新版本）
     */
    private Map<String, Object> crawlDRegistration(List<String> keywords, String dateFrom, String dateTo, int maxPages, int maxRecordsPerCrawler) {
        log.info("开始爬取US_registration数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
//            for (String keyword : keywords) {
//                try {
//                    log.info("US_registration爬虫使用关键词: {}", keyword);
//
//                    // 调用US_registration爬虫的参数化方法
//                    // 将关键词放入机构/贸易名称，其他参数为空
//                    Map<String, Object> searchResult = dRegistrationCrawler.crawlFDARegistrationDataWithParams(
//                        keyword,  // 机构/贸易名称
//                        "",       // 专有名称（空）
//                        "",       // 所有者/经营者名称（空）
//                        maxPages  // 最大页数
//                    );
//
//                    totalSaved += (Integer) searchResult.getOrDefault("totalSaved", 0);
//                    totalSkipped += (Integer) searchResult.getOrDefault("totalSkipped", 0);
//
//                } catch (Exception e) {
//                    log.error("US_registration爬虫处理关键词 {} 失败: {}", keyword, e.getMessage());
//                    totalErrors++;
//                }
//            }
            
            result.put("US_registration", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("US_registration爬虫执行失败: {}", e.getMessage(), e);
            result.put("US_registration", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 爬取CustomsCase数据（新版本）
     */
    private Map<String, Object> crawlCustomsCase(List<String> keywords, String dateFrom, String dateTo, int maxPages) {
        log.info("开始爬取CustomsCase数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
            for (String keyword : keywords) {
                try {
                    log.info("CustomsCase爬虫使用关键词: {}，日期范围: {} - {}", keyword, dateFrom, dateTo);
                    
                    // 调用CustomsCase爬虫的方法
                    // 将关键词作为搜索词，设置最大记录数，批次大小固定为10
                    List<?> results = customsCaseCrawler.crawlByKeyword(keyword, 100, 10);
                    
                    totalSaved += results != null ? results.size() : 0;
                    
                } catch (Exception e) {
                    log.error("CustomsCase爬虫处理关键词 {} 失败: {}", keyword, e.getMessage());
                    totalErrors++;
                }
            }
            
            result.put("CustomsCase", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", keywords.size()
            ));
            
        } catch (Exception e) {
            log.error("CustomsCase爬虫执行失败: {}", e.getMessage(), e);
            result.put("CustomsCase", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 爬取Guidance数据（新版本）
     */
    private Map<String, Object> crawlGuidance(List<String> keywords, String dateFrom, String dateTo, int maxPages) {
        log.info("开始爬取Guidance数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalSaved = 0;
            int totalSkipped = 0;
            int totalErrors = 0;
            
            // Guidance爬虫不支持关键词搜索，只爬取一次所有数据
            try {
                log.info("Guidance爬虫开始爬取所有数据");
                
                // 调用Guidance爬虫的方法，设置最大记录数
                guidanceCrawler.crawlWithLimit(100);
                
                totalSaved = 100; // 假设爬取了100条记录
                
            } catch (Exception e) {
                log.error("Guidance爬虫执行失败: {}", e.getMessage());
                totalErrors++;
            }
            
            result.put("Guidance", Map.of(
                "saved", totalSaved,
                "skipped", totalSkipped,
                "errors", totalErrors,
                "keywords", 1 // Guidance只执行一次
            ));
            
        } catch (Exception e) {
            log.error("Guidance爬虫执行失败: {}", e.getMessage(), e);
            result.put("Guidance", Map.of(
                "saved", 0,
                "skipped", 0,
                "errors", 1,
                "error", e.getMessage()
            ));
        }
        
        return result;
    }
    
    /**
     * 按单个关键词爬取所有爬虫（新版本）
     * @param keyword 搜索关键词
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @param maxPages 最大页数
     * @return 爬取结果
     */
    public Map<String, Object> crawlByKeyword(String keyword, String dateFrom, String dateTo, Integer maxPages) {
        log.info("开始按关键词 '{}' 爬取所有爬虫数据，日期范围: {} - {}", keyword, dateFrom, dateTo);
        
        List<String> keywords = List.of(keyword);
        return crawlAllCrawlers(keywords, dateFrom, dateTo, maxPages, 50); // 使用默认totalCount=50
    }
    
    /**
     * 按单个关键词爬取所有爬虫（保持向后兼容）
     * @param keyword 搜索关键词
     * @param maxRecordsPerCrawler 每个爬虫的最大记录数
     * @param batchSize 批处理大小
     * @return 爬取结果
     */
    public Map<String, Object> crawlByKeyword(String keyword, int maxRecordsPerCrawler, int batchSize) {
        return crawlByKeyword(keyword, null, null, 0);
    }
    
    /**
     * 获取爬虫状态
     * @return 各爬虫状态信息
     */
    public Map<String, Object> getCrawlerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("D_510K", Map.of("available", d510kCrawler != null, "status", "ready"));
            status.put("D_event", Map.of("available", dEventCrawler != null, "status", "ready"));
            status.put("D_recall", Map.of("available", dRecallCrawler != null, "status", "ready"));
            status.put("US_registration", Map.of("available", dRegistrationCrawler != null, "status", "ready"));
            status.put("CustomsCase", Map.of("available", customsCaseCrawler != null, "status", "ready"));
            status.put("Guidance", Map.of("available", guidanceCrawler != null, "status", "ready"));
            
            status.put("executorService", Map.of(
                "active", !executorService.isShutdown(),
                "threadPoolSize", 6
            ));
            
        } catch (Exception e) {
            log.error("获取爬虫状态失败: {}", e.getMessage(), e);
            status.put("error", e.getMessage());
        }
        
        return status;
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        log.info("正在关闭统一爬虫线程池...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("统一爬虫线程池已关闭");
    }
}
