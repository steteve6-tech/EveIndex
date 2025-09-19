package com.certification.crawler.certification.unifcrawler;

import com.certification.crawler.certification.sgs.SgsCrawler;
import com.certification.crawler.certification.ul.ULCrawler;
import com.certification.standards.CrawlerDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一爬虫类
 * 使用关键词文件进行SGS和UL数据爬取并保存到数据库
 * 每个关键词爬取完成后立即保存，确保数据的及时性和稳定性
 * 当爬取的批次全部与数据库重复时停止爬虫爬取
 */
@Component
@Transactional
public class UnifiedCrawler {

    private static final Logger log = LoggerFactory.getLogger(UnifiedCrawler.class);

    @Autowired
    private SgsCrawler sgsCrawler;

    @Autowired
    private ULCrawler ulCrawler;

    @Autowired
    private CrawlerDataService crawlerDataService;

    // 关键词文件路径
    private static final String KEYWORDS_FILE_PATH = "src/main/java/com/certification/crawler/certification/keywords.txt";

    /**
     * 使用关键词文件执行统一爬虫（SGS + UL）
     * 每个关键词爬取完成后立即保存到数据库
     * @param maxResultsPerKeyword 每个关键词最大爬取数量
     * @return 爬取结果
     */
    public Map<String, Object> executeUnifiedCrawlerWithKeywordsFile(int maxResultsPerKeyword) {
        Map<String, Object> result = new HashMap<>();
        List<String> keywords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 读取关键词文件
            keywords = readKeywordsFromFile();
            if (keywords.isEmpty()) {
                result.put("success", false);
                result.put("error", "关键词文件为空或读取失败");
                return result;
            }

            System.out.println("=== 开始使用关键词文件执行统一爬虫（SGS + UL） ===");
            System.out.println("关键词数量: " + keywords.size());
            System.out.println("每个关键词最大爬取数量: " + maxResultsPerKeyword);
            System.out.println("开始时间: " + LocalDateTime.now());

            // 2. 对每个关键词执行爬取（立即保存）
            int totalProcessed = 0;
            int totalSaved = 0;
            int totalFailed = 0;
            Map<String, Object> keywordResults = new HashMap<>();
            List<String> failedKeywords = new ArrayList<>();
            
            // 统计各爬虫的结果
            int sgsTotalSaved = 0;
            int ulTotalSaved = 0;
            int sgsTotalFailed = 0;
            int ulTotalFailed = 0;

            for (int i = 0; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                if (keyword.trim().isEmpty()) {
                    continue;
                }

                System.out.println("\n--- 处理第 " + (i + 1) + "/" + keywords.size() + " 个关键词: " + keyword + " ---");
                
                Map<String, Object> keywordResult = new HashMap<>();
                boolean keywordSuccess = true;
                int keywordSaved = 0;
                int keywordFailed = 0;
                
                try {
                    long keywordStartTime = System.currentTimeMillis();
                    
                    // 执行SGS爬虫
                    System.out.println("执行SGS爬虫...");
                    Map<String, Object> sgsResult = sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, maxResultsPerKeyword);
                    
                    // 检查SGS结果
                    if (sgsResult.get("success") != null && (Boolean) sgsResult.get("success")) {
                        int sgsSaved = sgsResult.get("savedCount") != null ? ((Number) sgsResult.get("savedCount")).intValue() : 0;
                        boolean sgsAllDuplicates = sgsResult.get("allDuplicates") != null && (Boolean) sgsResult.get("allDuplicates");
                        sgsTotalSaved += sgsSaved;
                        
                        System.out.println("SGS爬虫完成: 保存 " + sgsSaved + " 条数据" + (sgsAllDuplicates ? " (全部重复)" : ""));
                        
                        if (sgsAllDuplicates) {
                            System.out.println("SGS爬虫: 该批次全部重复，停止SGS爬取");
                        }
                    } else {
                        sgsTotalFailed++;
                        keywordFailed++;
                        System.out.println("SGS爬虫失败: " + sgsResult.get("error"));
                    }
                    
                    // 执行UL爬虫
                    System.out.println("执行UL爬虫...");
                    Map<String, Object> ulResult = ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, maxResultsPerKeyword);
                    
                    // 检查UL结果
                    if (ulResult.get("success") != null && (Boolean) ulResult.get("success")) {
                        int ulSaved = ulResult.get("savedCount") != null ? ((Number) ulResult.get("savedCount")).intValue() : 0;
                        boolean ulAllDuplicates = ulResult.get("allDuplicates") != null && (Boolean) ulResult.get("allDuplicates");
                        ulTotalSaved += ulSaved;
                        
                        System.out.println("UL爬虫完成: 保存 " + ulSaved + " 条数据" + (ulAllDuplicates ? " (全部重复)" : ""));
                        
                        if (ulAllDuplicates) {
                            System.out.println("UL爬虫: 该批次全部重复，停止UL爬取");
                        }
                    } else {
                        ulTotalFailed++;
                        keywordFailed++;
                        System.out.println("UL爬虫失败: " + ulResult.get("error"));
                    }
                    
                    long keywordEndTime = System.currentTimeMillis();
                    long keywordDuration = keywordEndTime - keywordStartTime;
                    
                    keywordSaved = (sgsResult.get("savedCount") != null ? ((Number) sgsResult.get("savedCount")).intValue() : 0) +
                                  (ulResult.get("savedCount") != null ? ((Number) ulResult.get("savedCount")).intValue() : 0);
                    
                    keywordResult.put("sgsResult", sgsResult);
                    keywordResult.put("ulResult", ulResult);
                    keywordResult.put("totalSaved", keywordSaved);
                    keywordResult.put("totalFailed", keywordFailed);
                    keywordResult.put("duration", keywordDuration);
                    keywordResult.put("success", keywordSuccess);
                    
                    totalProcessed++;
                    totalSaved += keywordSaved;
                    totalFailed += keywordFailed;
                    
                    System.out.println("关键词 '" + keyword + "' 处理完成:");
                    System.out.println("  - 总保存数据: " + keywordSaved + " 条");
                    System.out.println("  - 处理时间: " + keywordDuration + " ms");
                    System.out.println("  - 状态: " + (keywordSuccess ? "成功" : "部分失败"));

                } catch (Exception e) {
                    totalFailed++;
                    failedKeywords.add(keyword);
                    System.err.println("关键词 '" + keyword + "' 处理失败: " + e.getMessage());
                    keywordResult.put("success", false);
                    keywordResult.put("error", e.getMessage());
                    keywordResult.put("timestamp", LocalDateTime.now().toString());
                }
                
                keywordResults.put(keyword, keywordResult);
                
                // 每个关键词处理完成后短暂休息，避免过于频繁的请求
                if (i < keywords.size() - 1) {
                    try {
                        Thread.sleep(2000); // 休息2秒，因为要执行两个爬虫
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            long totalDuration = System.currentTimeMillis() - startTime;

            // 3. 构建最终结果
            result.put("success", true);
            result.put("totalKeywords", keywords.size());
            result.put("processedKeywords", totalProcessed);
            result.put("failedKeywords", totalFailed);
            result.put("totalSaved", totalSaved);
            result.put("sgsTotalSaved", sgsTotalSaved);
            result.put("ulTotalSaved", ulTotalSaved);
            result.put("sgsTotalFailed", sgsTotalFailed);
            result.put("ulTotalFailed", ulTotalFailed);
            result.put("keywordResults", keywordResults);
            result.put("failedKeywordsList", failedKeywords);
            result.put("executionTime", totalDuration);
            result.put("timestamp", LocalDateTime.now().toString());

            System.out.println("\n=== 统一爬虫关键词文件执行完成 ===");
            System.out.println("总处理关键词: " + totalProcessed + "/" + keywords.size());
            System.out.println("SGS总保存数据: " + sgsTotalSaved + " 条");
            System.out.println("UL总保存数据: " + ulTotalSaved + " 条");
            System.out.println("总保存数据: " + totalSaved + " 条");
            System.out.println("失败关键词: " + totalFailed + " 个");
            System.out.println("总执行时间: " + totalDuration + " ms");
            System.out.println("结束时间: " + LocalDateTime.now());

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            System.err.println("执行统一爬虫关键词文件时发生错误: " + e.getMessage());
            result.put("success", false);
            result.put("error", "执行失败: " + e.getMessage());
            result.put("executionTime", totalDuration);
            result.put("timestamp", LocalDateTime.now().toString());
        }

        return result;
    }

    /**
     * 使用关键词文件执行统一爬虫（带过滤器）
     * 每个关键词爬取完成后立即保存到数据库
     * @param maxResultsPerKeyword 每个关键词最大爬取数量
     * @param newsType 新闻类型
     * @param dateRange 日期范围
     * @param topics 主题列表
     * @return 爬取结果
     */
    public Map<String, Object> executeUnifiedCrawlerWithKeywordsFileAndFilters(
            int maxResultsPerKeyword, 
            String newsType, 
            String dateRange, 
            List<String> topics) {
        
        Map<String, Object> result = new HashMap<>();
        List<String> keywords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 读取关键词文件
            keywords = readKeywordsFromFile();
            if (keywords.isEmpty()) {
                result.put("success", false);
                result.put("error", "关键词文件为空或读取失败");
                return result;
            }

            System.out.println("=== 开始使用关键词文件和过滤器执行统一爬虫（SGS + UL） ===");
            System.out.println("关键词数量: " + keywords.size());
            System.out.println("每个关键词最大爬取数量: " + maxResultsPerKeyword);
            System.out.println("新闻类型: " + newsType);
            System.out.println("日期范围: " + dateRange);
            System.out.println("主题: " + topics);
            System.out.println("开始时间: " + LocalDateTime.now());

            // 2. 对每个关键词执行爬取（立即保存）
            int totalProcessed = 0;
            int totalSaved = 0;
            int totalFailed = 0;
            Map<String, Object> keywordResults = new HashMap<>();
            List<String> failedKeywords = new ArrayList<>();
            
            // 统计各爬虫的结果
            int sgsTotalSaved = 0;
            int ulTotalSaved = 0;
            int sgsTotalFailed = 0;
            int ulTotalFailed = 0;

            for (int i = 0; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                if (keyword.trim().isEmpty()) {
                    continue;
                }

                System.out.println("\n--- 处理第 " + (i + 1) + "/" + keywords.size() + " 个关键词: " + keyword + " ---");
                
                Map<String, Object> keywordResult = new HashMap<>();
                boolean keywordSuccess = true;
                int keywordSaved = 0;
                int keywordFailed = 0;
                
                try {
                    long keywordStartTime = System.currentTimeMillis();
                    
                    // 执行SGS爬虫（带过滤器）
                    System.out.println("执行SGS爬虫（带过滤器）...");
                    Map<String, Object> sgsResult = sgsCrawler.executeSgsCrawlerWithFiltersAndSave(
                        keyword, maxResultsPerKeyword, newsType, dateRange, topics);
                    
                    // 检查SGS结果
                    if (sgsResult.get("success") != null && (Boolean) sgsResult.get("success")) {
                        int sgsSaved = sgsResult.get("savedCount") != null ? ((Number) sgsResult.get("savedCount")).intValue() : 0;
                        boolean sgsAllDuplicates = sgsResult.get("allDuplicates") != null && (Boolean) sgsResult.get("allDuplicates");
                        sgsTotalSaved += sgsSaved;
                        
                        System.out.println("SGS爬虫完成: 保存 " + sgsSaved + " 条数据" + (sgsAllDuplicates ? " (全部重复)" : ""));
                        
                        if (sgsAllDuplicates) {
                            System.out.println("SGS爬虫: 该批次全部重复，停止SGS爬取");
                        }
                    } else {
                        sgsTotalFailed++;
                        keywordFailed++;
                        System.out.println("SGS爬虫失败: " + sgsResult.get("error"));
                    }
                    
                    // 执行UL爬虫（UL不支持过滤器，使用基础方法）
                    System.out.println("执行UL爬虫...");
                    Map<String, Object> ulResult = ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, maxResultsPerKeyword);
                    
                    // 检查UL结果
                    if (ulResult.get("success") != null && (Boolean) ulResult.get("success")) {
                        int ulSaved = ulResult.get("savedCount") != null ? ((Number) ulResult.get("savedCount")).intValue() : 0;
                        boolean ulAllDuplicates = ulResult.get("allDuplicates") != null && (Boolean) ulResult.get("allDuplicates");
                        ulTotalSaved += ulSaved;
                        
                        System.out.println("UL爬虫完成: 保存 " + ulSaved + " 条数据" + (ulAllDuplicates ? " (全部重复)" : ""));
                        
                        if (ulAllDuplicates) {
                            System.out.println("UL爬虫: 该批次全部重复，停止UL爬取");
                        }
                    } else {
                        ulTotalFailed++;
                        keywordFailed++;
                        System.out.println("UL爬虫失败: " + ulResult.get("error"));
                    }
                    
                    long keywordEndTime = System.currentTimeMillis();
                    long keywordDuration = keywordEndTime - keywordStartTime;
                    
                    keywordSaved = (sgsResult.get("savedCount") != null ? ((Number) sgsResult.get("savedCount")).intValue() : 0) +
                                  (ulResult.get("savedCount") != null ? ((Number) ulResult.get("savedCount")).intValue() : 0);
                    
                    keywordResult.put("sgsResult", sgsResult);
                    keywordResult.put("ulResult", ulResult);
                    keywordResult.put("totalSaved", keywordSaved);
                    keywordResult.put("totalFailed", keywordFailed);
                    keywordResult.put("duration", keywordDuration);
                    keywordResult.put("success", keywordSuccess);
                    
                    totalProcessed++;
                    totalSaved += keywordSaved;
                    totalFailed += keywordFailed;
                    
                    System.out.println("关键词 '" + keyword + "' 处理完成:");
                    System.out.println("  - 总保存数据: " + keywordSaved + " 条");
                    System.out.println("  - 处理时间: " + keywordDuration + " ms");
                    System.out.println("  - 状态: " + (keywordSuccess ? "成功" : "部分失败"));

                } catch (Exception e) {
                    totalFailed++;
                    failedKeywords.add(keyword);
                    System.err.println("关键词 '" + keyword + "' 处理失败: " + e.getMessage());
                    keywordResult.put("success", false);
                    keywordResult.put("error", e.getMessage());
                    keywordResult.put("timestamp", LocalDateTime.now().toString());
                }
                
                keywordResults.put(keyword, keywordResult);
                
                // 每个关键词处理完成后短暂休息，避免过于频繁的请求
                if (i < keywords.size() - 1) {
                    try {
                        Thread.sleep(2000); // 休息2秒，因为要执行两个爬虫
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            long totalDuration = System.currentTimeMillis() - startTime;

            // 3. 构建最终结果
            result.put("success", true);
            result.put("totalKeywords", keywords.size());
            result.put("processedKeywords", totalProcessed);
            result.put("failedKeywords", totalFailed);
            result.put("totalSaved", totalSaved);
            result.put("sgsTotalSaved", sgsTotalSaved);
            result.put("ulTotalSaved", ulTotalSaved);
            result.put("sgsTotalFailed", sgsTotalFailed);
            result.put("ulTotalFailed", ulTotalFailed);
            result.put("keywordResults", keywordResults);
            result.put("failedKeywordsList", failedKeywords);
            result.put("filters", Map.of(
                "newsType", newsType,
                "dateRange", dateRange,
                "topics", topics
            ));
            result.put("executionTime", totalDuration);
            result.put("timestamp", LocalDateTime.now().toString());

            System.out.println("\n=== 统一爬虫关键词文件（带过滤器）执行完成 ===");
            System.out.println("总处理关键词: " + totalProcessed + "/" + keywords.size());
            System.out.println("SGS总保存数据: " + sgsTotalSaved + " 条");
            System.out.println("UL总保存数据: " + ulTotalSaved + " 条");
            System.out.println("总保存数据: " + totalSaved + " 条");
            System.out.println("失败关键词: " + totalFailed + " 个");
            System.out.println("总执行时间: " + totalDuration + " ms");
            System.out.println("结束时间: " + LocalDateTime.now());

        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - startTime;
            System.err.println("执行统一爬虫关键词文件（带过滤器）时发生错误: " + e.getMessage());
            result.put("success", false);
            result.put("error", "执行失败: " + e.getMessage());
            result.put("executionTime", totalDuration);
            result.put("timestamp", LocalDateTime.now().toString());
        }

        return result;
    }

    /**
     * 读取关键词文件
     * @return 关键词列表
     */
    private List<String> readKeywordsFromFile() {
        List<String> keywords = new ArrayList<>();
        
        try {
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(filePath)) {
                System.err.println("关键词文件不存在: " + KEYWORDS_FILE_PATH);
                return keywords;
            }

            List<String> lines = Files.readAllLines(filePath);
            
            for (String line : lines) {
                line = line.trim();
                
                // 跳过空行、注释行和标题行
                if (line.isEmpty() || 
                    line.startsWith("#") || 
                    line.startsWith("##") || 
                    line.startsWith("###") ||
                    line.startsWith("一、") ||
                    line.startsWith("二、") ||
                    line.startsWith("三、") ||
                    line.startsWith("四、") ||
                    line.startsWith("五、") ||
                    line.startsWith("六、") ||
                    line.startsWith("七、") ||
                    line.startsWith("八、") ||
                    line.startsWith("九、") ||
                    line.startsWith("十、") ||
                    line.startsWith("1.") ||
                    line.startsWith("2.") ||
                    line.startsWith("3.") ||
                    line.startsWith("4.") ||
                    line.startsWith("5.") ||
                    line.startsWith("6.") ||
                    line.startsWith("7.") ||
                    line.startsWith("8.") ||
                    line.startsWith("9.") ||
                    line.startsWith("10.") ||
                    line.startsWith("（") ||
                    line.startsWith("(") ||
                    line.startsWith("【") ||
                    line.startsWith("[") ||
                    line.contains("：") ||
                    line.contains(":")) {
                    continue;
                }
                
                // 添加有效关键词
                keywords.add(line);
            }

            System.out.println("成功读取关键词文件，有效关键词数量: " + keywords.size());

        } catch (IOException e) {
            System.err.println("读取关键词文件失败: " + e.getMessage());
        }

        return keywords;
    }

    /**
     * 获取关键词文件内容
     * @return 关键词文件内容
     */
    public Map<String, Object> getKeywordsFileContent() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(filePath)) {
                result.put("success", false);
                result.put("error", "关键词文件不存在");
                return result;
            }

            List<String> lines = Files.readAllLines(filePath);
            List<String> keywords = readKeywordsFromFile();

            result.put("success", true);
            result.put("totalLines", lines.size());
            result.put("validKeywords", keywords.size());
            result.put("keywords", keywords);
            result.put("filePath", KEYWORDS_FILE_PATH);

        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "读取关键词文件失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新关键词文件
     * @param newKeywords 新的关键词列表
     * @return 更新结果
     */
    public Map<String, Object> updateKeywordsFile(List<String> newKeywords) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path filePath = Paths.get(KEYWORDS_FILE_PATH);
            
            // 创建目录（如果不存在）
            Files.createDirectories(filePath.getParent());
            
            // 写入新关键词
            Files.write(filePath, newKeywords);
            
            result.put("success", true);
            result.put("message", "关键词文件更新成功");
            result.put("keywordsCount", newKeywords.size());
            result.put("timestamp", LocalDateTime.now().toString());

            System.out.println("关键词文件更新成功，关键词数量: " + newKeywords.size());

        } catch (IOException e) {
            result.put("success", false);
            result.put("error", "更新关键词文件失败: " + e.getMessage());
            System.err.println("更新关键词文件失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取爬取统计信息
     * @return 统计信息
     */
    public Map<String, Object> getCrawlingStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> keywords = readKeywordsFromFile();
            
            // 获取数据库中的SGS和UL数据统计
            long totalSgsData = crawlerDataService.getCountBySourceName("SGS");
            long totalUlData = crawlerDataService.getCountBySourceName("UL Solutions");
            long totalData = totalSgsData + totalUlData;
            
            result.put("success", true);
            result.put("totalKeywords", keywords.size());
            result.put("totalSGSData", totalSgsData);
            result.put("totalULData", totalUlData);
            result.put("totalData", totalData);
            result.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取统计信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 按关键词爬取所有爬虫
     * @param keyword 搜索关键词
     * @param maxRecordsPerCrawler 每个爬虫的最大记录数
     * @param batchSize 批处理大小
     * @return 爬取结果
     */
    public Map<String, Object> crawlByKeyword(String keyword, int maxRecordsPerCrawler, int batchSize) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始按关键词爬取: keyword={}, maxRecordsPerCrawler={}, batchSize={}", keyword, maxRecordsPerCrawler, batchSize);
            
            // 执行SGS爬虫
            Map<String, Object> sgsResult = sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, maxRecordsPerCrawler);
            
            // 执行UL爬虫
            Map<String, Object> ulResult = ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, maxRecordsPerCrawler);
            
            // 统计结果
            int sgsSaved = (Integer) sgsResult.getOrDefault("savedCount", 0);
            int ulSaved = (Integer) ulResult.getOrDefault("savedCount", 0);
            int totalSaved = sgsSaved + ulSaved;
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("keyword", keyword);
            result.put("sgsResult", sgsResult);
            result.put("ulResult", ulResult);
            result.put("totalSaved", totalSaved);
            result.put("sgsSaved", sgsSaved);
            result.put("ulSaved", ulSaved);
            result.put("executionTime", duration);
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("按关键词爬取完成: keyword={}, 总保存={}, SGS={}, UL={}, 耗时={}ms", 
                    keyword, totalSaved, sgsSaved, ulSaved, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("按关键词爬取失败: keyword={}, error={}", keyword, e.getMessage(), e);
            
            result.put("success", false);
            result.put("keyword", keyword);
            result.put("error", "爬取失败: " + e.getMessage());
            result.put("executionTime", duration);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 获取统一爬虫状态
     * @return 爬虫状态信息
     */
    public Map<String, Object> getCrawlerStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建简单的状态信息
            Map<String, Object> sgsStatus = new HashMap<>();
            sgsStatus.put("name", "SGS爬虫");
            sgsStatus.put("status", "ready");
            sgsStatus.put("description", "SGS新闻爬虫");
            
            Map<String, Object> ulStatus = new HashMap<>();
            ulStatus.put("name", "UL爬虫");
            ulStatus.put("status", "ready");
            ulStatus.put("description", "UL Solutions新闻爬虫");
            
            result.put("success", true);
            result.put("sgsStatus", sgsStatus);
            result.put("ulStatus", ulStatus);
            result.put("timestamp", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.error("获取爬虫状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取状态失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 加载搜索关键词
     * @return 关键词列表
     */
    public List<String> loadSearchKeywords() {
        try {
            Path keywordsPath = Paths.get(KEYWORDS_FILE_PATH);
            if (!Files.exists(keywordsPath)) {
                log.warn("关键词文件不存在: {}", KEYWORDS_FILE_PATH);
                return new ArrayList<>();
            }
            
            List<String> keywords = Files.readAllLines(keywordsPath);
            // 过滤空行和注释行
            keywords = keywords.stream()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("加载关键词成功，共 {} 个关键词", keywords.size());
            return keywords;
            
        } catch (IOException e) {
            log.error("加载关键词文件失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}