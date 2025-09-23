package com.certification.crawler.certification;

import com.certification.entity.common.CertNewsData;
// import com.certification.service.SystemLogService; // 已删除
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 新闻统一爬虫 - 整合SGS和UL爬虫功能
 * 提供统一的接口来调用多个爬虫
 */
@Slf4j
@Component
@Transactional
public class NewsUnicrawl {

    @Autowired
    private SgsCrawler sgsCrawler;
    
    @Autowired
    private ULCrawler ulCrawler;
    
    @Autowired
    private CrawlerDataService crawlerDataService;

    // @Autowired
    // private SystemLogService systemLogService; // 已删除
    
    // 线程池用于并发执行爬虫
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * 获取所有可用的爬虫列表
     * @return 爬虫信息列表
     */
    public List<Map<String, Object>> getAvailableCrawlers() {
        List<Map<String, Object>> crawlers = new ArrayList<>();
        
        // SGS爬虫信息
        Map<String, Object> sgsInfo = new HashMap<>();
        sgsInfo.put("name", "SGS");
        sgsInfo.put("displayName", "SGS认证新闻爬虫");
        sgsInfo.put("description", "爬取SGS认证相关的新闻和公告");
        sgsInfo.put("available", sgsCrawler.isAvailable());
        sgsInfo.put("sourceName", sgsCrawler.getSourceName());
        sgsInfo.put("crawlerName", sgsCrawler.getCrawlerName());
        crawlers.add(sgsInfo);
        
        // UL爬虫信息
        Map<String, Object> ulInfo = new HashMap<>();
        ulInfo.put("name", "UL");
        ulInfo.put("displayName", "UL Solutions认证新闻爬虫");
        ulInfo.put("description", "爬取UL Solutions认证相关的新闻和公告");
        ulInfo.put("available", ulCrawler.isAvailable());
        ulInfo.put("sourceName", ulCrawler.getSourceName());
        ulInfo.put("crawlerName", ulCrawler.getCrawlerName());
        crawlers.add(ulInfo);
        
        return crawlers;
    }

    /**
     * 执行所有可用爬虫的爬取任务
     * @param keyword 搜索关键词（可选）
     * @param countPerCrawler 每个爬虫爬取的数量
     * @return 执行结果汇总
     */
    public Map<String, Object> executeAllCrawlers(String keyword, int countPerCrawler) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行所有爬虫，关键词: {}，每个爬虫爬取数量: {}", keyword, countPerCrawler);
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getTotalCount();
            
            // 并发执行所有爬虫
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            
            // SGS爬虫任务
            CompletableFuture<Map<String, Object>> sgsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        return sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, countPerCrawler);
                    } else {
                        return sgsCrawler.executeSgsCrawlerAndSave(countPerCrawler);
                    }
                } catch (Exception e) {
                    log.error("SGS爬虫执行失败: {}", e.getMessage(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("error", "SGS爬虫执行失败: " + e.getMessage());
                    errorResult.put("crawlerName", "SGS");
                    return errorResult;
                }
            }, executorService);
            futures.add(sgsFuture);
            
            // UL爬虫任务
            CompletableFuture<Map<String, Object>> ulFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        return ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, countPerCrawler);
                    } else {
                        return ulCrawler.executeULCrawlerAndSave(countPerCrawler);
                    }
                } catch (Exception e) {
                    log.error("UL爬虫执行失败: {}", e.getMessage(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("error", "UL爬虫执行失败: " + e.getMessage());
                    errorResult.put("crawlerName", "UL");
                    return errorResult;
                }
            }, executorService);
            futures.add(ulFuture);
            
            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            // 设置超时时间（30分钟）
            allFutures.get(30, TimeUnit.MINUTES);
            
            // 收集结果
            List<Map<String, Object>> crawlerResults = new ArrayList<>();
            int totalCrawled = 0;
            int totalSaved = 0;
            int successCount = 0;
            int failureCount = 0;
            
            for (CompletableFuture<Map<String, Object>> future : futures) {
                Map<String, Object> crawlerResult = future.get();
                crawlerResults.add(crawlerResult);
                
                Boolean success = (Boolean) crawlerResult.get("success");
                if (success != null && success) {
                    successCount++;
                    Integer crawled = (Integer) crawlerResult.get("crawledCount");
                    Integer saved = (Integer) crawlerResult.get("savedCount");
                    if (crawled != null) totalCrawled += crawled;
                    if (saved != null) totalSaved += saved;
                } else {
                    failureCount++;
                }
            }
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getTotalCount();
            long newDataCount = afterCount - beforeCount;
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            log.info("统一爬虫执行完成，成功: {}，失败: {}，总爬取: {}，总保存: {}，新增: {}，耗时: {} ms",
                successCount, failureCount, totalCrawled, totalSaved, newDataCount, executionTime);
            
            // 构建返回结果
            result.put("success", true);
            result.put("keyword", keyword);
            result.put("countPerCrawler", countPerCrawler);
            result.put("totalCrawlers", crawlerResults.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("totalCrawled", totalCrawled);
            result.put("totalSaved", totalSaved);
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("crawlerResults", crawlerResults);
            result.put("message", "统一爬虫执行完成");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("统一爬虫执行失败: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", "统一爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 执行指定爬虫的爬取任务
     * @param crawlerName 爬虫名称（SGS或UL）
     * @param keyword 搜索关键词（可选）
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeSpecificCrawler(String crawlerName, String keyword, int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行指定爬虫: {}，关键词: {}，数量: {}", crawlerName, keyword, count);
            
            Map<String, Object> crawlerResult;
            
            switch (crawlerName.toUpperCase()) {
                case "SGS":
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        crawlerResult = sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, count);
                    } else {
                        crawlerResult = sgsCrawler.executeSgsCrawlerAndSave(count);
                    }
                    break;
                case "UL":
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        crawlerResult = ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, count);
                    } else {
                        crawlerResult = ulCrawler.executeULCrawlerAndSave(count);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("不支持的爬虫名称: " + crawlerName);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            crawlerResult.put("executionTime", executionTime);
            crawlerResult.put("timestamp", LocalDateTime.now().toString());
            
            // 记录成功日志
            log.info(
                "指定爬虫执行完成",
                String.format("指定爬虫执行完成: %s，关键词: %s，结果: %s", 
                    crawlerName, keyword, crawlerResult.get("message")),
                "NewsUnicrawl"
            );
            
            return crawlerResult;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("指定爬虫执行失败: {}，爬虫: {}，关键词: {}，错误: {}", 
                crawlerName, crawlerName, keyword, e.getMessage(), e);
            
            result.put("success", false);
            result.put("crawlerName", crawlerName);
            result.put("keyword", keyword);
            result.put("error", "指定爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return result;
        }
    }

    /**
     * 执行SGS爬虫的过滤条件爬取
     * @param keyword 搜索关键词
     * @param count 爬取数量
     * @param newsType 新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics 主题值列表（可选）
     * @return 执行结果
     */
    public Map<String, Object> executeSgsCrawlerWithFilters(String keyword, int count, 
                                                           String newsType, String dateRange, List<String> topics) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行SGS爬虫（过滤条件），关键词: {}，数量: {}，新闻类型: {}，日期范围: {}，主题: {}", 
                keyword, count, newsType, dateRange, topics);
            
            // 记录开始日志
            log.info(
                "SGS爬虫开始执行（过滤条件）",
                String.format("开始执行SGS爬虫（过滤条件），关键词: %s，数量: %d，新闻类型: %s，日期范围: %s，主题: %s", 
                    keyword, count, newsType, dateRange, topics),
                "NewsUnicrawl"
            );
            
            Map<String, Object> crawlerResult = sgsCrawler.executeSgsCrawlerWithFiltersAndSave(
                keyword, count, newsType, dateRange, topics);
            
            long executionTime = System.currentTimeMillis() - startTime;
            crawlerResult.put("executionTime", executionTime);
            crawlerResult.put("timestamp", LocalDateTime.now().toString());
            
            // 记录成功日志
            log.info("SGS爬虫执行完成（过滤条件），关键词: {}，结果: {}", 
                keyword, crawlerResult.get("message"));
            
            return crawlerResult;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("SGS爬虫执行失败（过滤条件），关键词: {}，错误: {}", keyword, e.getMessage(), e);
            
            result.put("success", false);
            result.put("crawlerName", "SGS");
            result.put("keyword", keyword);
            result.put("newsType", newsType);
            result.put("dateRange", dateRange);
            result.put("topics", topics);
            result.put("error", "SGS爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return result;
        }
    }

    /**
     * 获取爬虫状态信息
     * @return 状态信息汇总
     */
    public Map<String, Object> getCrawlerStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有爬虫信息
            List<Map<String, Object>> availableCrawlers = getAvailableCrawlers();
            
            // 获取数据库统计
            long totalCount = crawlerDataService.getTotalCount();
            long sgsCount = crawlerDataService.getCountBySourceName("SGS");
            long ulCount = crawlerDataService.getCountBySourceName("UL Solutions");
            
            // 获取各状态统计
            long newCount = crawlerDataService.getCountByStatus(CertNewsData.DataStatus.NEW);
            long processedCount = crawlerDataService.getCountByStatus(CertNewsData.DataStatus.PROCESSED);
            long errorCount = crawlerDataService.getCountByStatus(CertNewsData.DataStatus.ERROR);
            long duplicateCount = crawlerDataService.getCountByStatus(CertNewsData.DataStatus.DUPLICATE);
            
            result.put("success", true);
            result.put("availableCrawlers", availableCrawlers);
            result.put("databaseStats", Map.of(
                "totalCount", totalCount,
                "sgsCount", sgsCount,
                "ulCount", ulCount,
                "newCount", newCount,
                "processedCount", processedCount,
                "errorCount", errorCount,
                "duplicateCount", duplicateCount
            ));
            result.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取爬虫状态失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 获取SGS爬虫的过滤选项
     * @return 过滤选项信息
     */
    public Map<String, Object> getSgsFilterOptions() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 新闻类型选项
            List<Map<String, String>> newsTypeOptions = Arrays.asList(
                Map.of("label", "Business News", "value", "60f24f8e42314d42860ee99a5cd5a652"),
                Map.of("label", "Customer Stories", "value", "67f6406845254365b73433cefbadfe84"),
                Map.of("label", "Features", "value", "087a1a51d9e944298765f8327b1f9f60"),
                Map.of("label", "Global Corporate News", "value", "fb520c62e9c343dd92c4cad6ad7247e8"),
                Map.of("label", "Local Corporate News", "value", "c5dc1966fe514abaabe8ef9dc75d172a")
            );
            
            // 日期范围选项
            List<Map<String, String>> dateRangeOptions = Arrays.asList(
                Map.of("label", "Past Week", "value", "{A6809EE3-323F-4B0E-8346-1A3CF26B714B}"),
                Map.of("label", "Past Month", "value", "{57C35CF1-7C22-4590-9C4C-B056ED4B5D46}"),
                Map.of("label", "Past Year", "value", "{1E820BE0-5B7F-4435-882E-DED686057DAB}"),
                Map.of("label", "2025", "value", "{6B0C4C80-851D-4ADD-8575-52978AD58599}"),
                Map.of("label", "2024", "value", "{9EECBEAE-A412-4167-999F-DCCFEB7405D5}"),
                Map.of("label", "2023", "value", "{13F30E19-B3CE-48ED-BFA4-6704A61BEBFA}"),
                Map.of("label", "2022", "value", "{A1596BCB-7AA6-4B6D-971E-73416E8B3B06}"),
                Map.of("label", "Older", "value", "{2782DF07-157B-436E-8C9E-A96C51B050FF}")
            );
            
            // 主题选项
            List<Map<String, String>> topicOptions = Arrays.asList(
                Map.of("label", "About SGS", "value", "9209eadfaff74fe48a63401202f818af"),
                Map.of("label", "Agriculture & Forestry", "value", "bbd469989ef344dfa91acd997ef78087"),
                Map.of("label", "Building and Infrastructure", "value", "10e34a46667b4e6d8e3b3d528412bbd6"),
                Map.of("label", "Certification", "value", "496a050c1be74309b381b138507f4147"),
                Map.of("label", "Connectivity", "value", "5b2355c4277843d995ab44ce3a402eed"),
                Map.of("label", "Consumer Products & Retail", "value", "c23eda3e557b4beb9de876b84bb1d11f"),
                Map.of("label", "Corporate Sustainability", "value", "5b98b824eabc4bed896cef0ada33a5f3"),
                Map.of("label", "Cosmetics & Personal Care", "value", "b1ef2516d5814fb08ba52cbd560effff"),
                Map.of("label", "Cybersecurity & Technology", "value", "da16b3c7f2904554a389636adf39f2d3"),
                Map.of("label", "Digital Trust Assurance", "value", "da588fd0284e4d17b63f20fe5b26e7eb"),
                Map.of("label", "Environmental, Health & Safety", "value", "e075e13116e943dc868cbe70be0ae5b6"),
                Map.of("label", "Food", "value", "c7ee57cc8d6544429e172eb25d7ab8f9"),
                Map.of("label", "Government & Trade Facilitation", "value", "e634aa5321f3490eb32bb0fcd883d1e0"),
                Map.of("label", "Hardgoods, Toys & Juvenile Products", "value", "f45027743c454c2389264d0eb1c6f4ed"),
                Map.of("label", "Industrial Manufacturing & Processing", "value", "c5250a583732466ba803f2f5688d24dc"),
                Map.of("label", "Investor Relations", "value", "18ba053f103f46be853e112c38995919"),
                Map.of("label", "MedTech", "value", "1dc58b0c6f0541be86a08153ab2f7114"),
                Map.of("label", "Mining", "value", "b81102ac1d0e47dcb1e3fe26f625782a"),
                Map.of("label", "Oil, Gas & Chemicals", "value", "a8fecf02948348b18ee8e6bce8df1f76"),
                Map.of("label", "Pharma", "value", "e14e7e1e14b94fcbad81ae9914bf7e3b"),
                Map.of("label", "Power & Utilities", "value", "06f9571bcfeb4869ba0603e4049e1d86"),
                Map.of("label", "Softlines", "value", "848934348e93444e8532d987586e8639"),
                Map.of("label", "Supply Chain", "value", "179d85eac9f549939c86e227b9410b7e"),
                Map.of("label", "Sustainability", "value", "f0960a9e139b4dcea7f04efc85026b00"),
                Map.of("label", "Training", "value", "a56d996ae15549898949086d148ba9fa"),
                Map.of("label", "Transportation", "value", "016482e54206417aa7ded6b2b2c72bc4")
            );
            
            result.put("success", true);
            result.put("newsTypeOptions", newsTypeOptions);
            result.put("dateRangeOptions", dateRangeOptions);
            result.put("topicOptions", topicOptions);
            result.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取SGS过滤选项失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 清理资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
