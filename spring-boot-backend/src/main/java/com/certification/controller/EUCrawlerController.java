package com.certification.controller;

import com.certification.crawler.countrydata.eu.Eu_customcase;
import com.certification.crawler.countrydata.eu.Eu_guidance;
import com.certification.crawler.countrydata.eu.Eu_recall;
import com.certification.crawler.countrydata.eu.Eu_registration;
import com.certification.exception.AllDataDuplicateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 欧盟爬虫控制器
 * 提供欧盟爬虫的测试和执行接口
 */
@Slf4j
@RestController
@RequestMapping("/eu-crawler")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://localhost:3101", "http://127.0.0.1:3000", "http://127.0.0.1:3100", "http://127.0.0.1:3101"})
public class EUCrawlerController {

    @Autowired
    private Eu_customcase euCustomCaseCrawler;
    
    @Autowired
    private Eu_guidance euGuidanceCrawler;
    
    @Autowired
    private Eu_recall euRecallCrawler;
    
    @Autowired
    private Eu_registration euRegistrationCrawler;

    /**
     * 测试/执行EU_CustomCase爬虫
     * @param taricCode TARIC编码，默认9018
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小，默认100
     * @return 爬取结果
     */
    @PostMapping("/test/eu-custom-case")
    public ResponseEntity<Map<String, Object>> testEUCustomCase(
            @RequestParam(required = false, defaultValue = "9018") String taricCode,
            @RequestParam(required = false, defaultValue = "-1") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "100") Integer batchSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始执行EU_CustomCase爬虫");
            log.info("参数: taricCode={}, maxRecords={}, batchSize={}", taricCode, maxRecords, batchSize);
            
            int savedCount = euCustomCaseCrawler.crawlAndSaveToDatabase(taricCode, maxRecords, batchSize);
            
            // 检查是否真的成功（保存了数据或明确表示无新数据）
            if (savedCount >= 0) {
                result.put("success", true);
                result.put("message", "EU_CustomCase爬虫执行完成");
                result.put("savedCount", savedCount);
                result.put("skippedCount", 0);
                result.put("totalProcessed", savedCount);
                result.put("crawlerType", "EU_CustomCase");
                result.put("parameters", Map.of(
                    "taricCode", taricCode,
                    "maxRecords", maxRecords,
                    "batchSize", batchSize
                ));
                
                log.info("✅ EU_CustomCase爬虫执行完成，保存记录数: {}", savedCount);
            } else {
                // 如果返回负数，表示执行失败
                result.put("success", false);
                result.put("message", "EU_CustomCase爬虫执行失败");
                result.put("savedCount", 0);
                result.put("skippedCount", 0);
                result.put("totalProcessed", 0);
                result.put("crawlerType", "EU_CustomCase");
                result.put("error", "爬虫执行失败，返回负数结果");
                
                log.error("❌ EU_CustomCase爬虫执行失败，返回负数结果: {}", savedCount);
            }
            
        } catch (AllDataDuplicateException e) {
            log.warn("⚠️ EU_CustomCase爬虫执行完成，所有数据都是重复的: {}", e.getMessage());
            result.put("success", true);
            result.put("message", "爬取完成，但没有数据更新。");
            result.put("savedCount", 0);
            result.put("skippedCount", parseSkippedCount(e.getMessage()));
            result.put("totalProcessed", parseSkippedCount(e.getMessage()));
            result.put("crawlerType", "EU_CustomCase");
            result.put("isAllDuplicate", true);
        } catch (Exception e) {
            log.error("❌ EU_CustomCase爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_CustomCase爬虫执行失败: " + e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", 0);
            result.put("totalProcessed", 0);
            result.put("crawlerType", "EU_CustomCase");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量爬取EU_CustomCase爬虫（TARIC编码列表）
     * @param taricCodes TARIC编码列表(逗号分隔)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小，默认100
     * @return 爬取结果
     */
    @PostMapping("/test/eu-custom-case/batch")
    public ResponseEntity<Map<String, Object>> testEUCustomCaseBatch(
            @RequestParam(required = false, defaultValue = "9018,9021,9022") String taricCodes,
            @RequestParam(required = false, defaultValue = "-1") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "100") Integer batchSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始批量执行EU_CustomCase爬虫");
            log.info("参数: taricCodes={}, maxRecords={}, batchSize={}", taricCodes, maxRecords, batchSize);
            
            // 解析TARIC编码列表
            List<String> taricCodeList = new ArrayList<>();
            if (taricCodes != null && !taricCodes.trim().isEmpty()) {
                String[] codes = taricCodes.split(",");
                for (String code : codes) {
                    String trimmedCode = code.trim();
                    if (!trimmedCode.isEmpty()) {
                        taricCodeList.add(trimmedCode);
                    }
                }
            }
            
            if (taricCodeList.isEmpty()) {
                taricCodeList.add("9018"); // 默认值
            }
            
            Map<String, Object> crawlResult = euCustomCaseCrawler.crawlAndSaveWithTaricCodes(taricCodeList, maxRecords, batchSize);
            
            result.put("success", crawlResult.get("success"));
            result.put("message", crawlResult.get("message"));
            result.put("totalProcessed", crawlResult.get("totalProcessed"));
            result.put("successCount", crawlResult.get("successCount"));
            result.put("failureCount", crawlResult.get("failureCount"));
            result.put("totalSaved", crawlResult.get("totalSaved"));
            result.put("totalSkipped", crawlResult.get("totalSkipped"));
            result.put("failedCodes", crawlResult.get("failedCodes"));
            result.put("codeResults", crawlResult.get("codeResults"));
            result.put("crawlerType", "EU_CustomCase_Batch");
            result.put("parameters", Map.of(
                "taricCodes", taricCodes,
                "maxRecords", maxRecords,
                "batchSize", batchSize
            ));
            
            log.info("✅ EU_CustomCase批量爬虫执行完成");
            
        } catch (Exception e) {
            log.error("❌ EU_CustomCase批量爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_CustomCase批量爬虫执行失败: " + e.getMessage());
            result.put("crawlerType", "EU_CustomCase_Batch");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试/执行EU_Guidance爬虫
     * @param maxPages 最大页数，0表示爬取所有页
     * @param maxRecords 最大记录数，-1表示爬取所有记录
     * @param batchSize 批次大小，默认100
     * @return 爬取结果
     */
    @PostMapping("/test/eu-guidance")
    public ResponseEntity<Map<String, Object>> testEUGuidance(
            @RequestParam(required = false, defaultValue = "0") Integer maxPages,
            @RequestParam(required = false, defaultValue = "-1") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "100") Integer batchSize) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始执行EU_Guidance爬虫");
            log.info("参数: maxPages={}, maxRecords={}, batchSize={}", maxPages, maxRecords, batchSize);
            
            List<Map<String, String>> newsList = euGuidanceCrawler.crawlMedicalDeviceNews(maxPages, maxRecords, batchSize);
            
            // 保存到数据库
            int savedCount = 0;
            if (newsList != null && !newsList.isEmpty()) {
                savedCount = euGuidanceCrawler.crawlAndSaveToDatabase(maxPages);
            }
            
            result.put("success", true);
            result.put("message", "EU_Guidance爬虫执行完成");
            result.put("savedCount", savedCount);
            result.put("skippedCount", 0);
            result.put("totalProcessed", savedCount);
            result.put("crawlerType", "EU_Guidance");
            result.put("parameters", Map.of(
                "maxPages", maxPages,
                "maxRecords", maxRecords,
                "batchSize", batchSize
            ));
            
            log.info("✅ EU_Guidance爬虫执行完成，保存记录数: {}", savedCount);
            
        } catch (Exception e) {
            log.error("❌ EU_Guidance爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_Guidance爬虫执行失败: " + e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", 0);
            result.put("totalProcessed", 0);
            result.put("crawlerType", "EU_Guidance");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试/执行EU_Recall爬虫
     * @param maxPages 最大爬取页数，默认5
     * @param searchKeyword 搜索关键词
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @param language 语言
     * @param productCategories 产品类别
     * @return 爬取结果
     */
    @PostMapping("/test/eu-recall")
    public ResponseEntity<Map<String, Object>> testEURecall(
            @RequestParam(required = false, defaultValue = "5") Integer maxPages,
            @RequestParam(required = false, defaultValue = "") String searchKeyword,
            @RequestParam(required = false, defaultValue = "") String sortField,
            @RequestParam(required = false, defaultValue = "") String sortDirection,
            @RequestParam(required = false, defaultValue = "") String language,
            @RequestParam(required = false, defaultValue = "") String productCategories) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始执行EU_Recall爬虫");
            log.info("参数: maxPages={}, searchKeyword={}, sortField={}, sortDirection={}, language={}, productCategories={}", 
                    maxPages, searchKeyword, sortField, sortDirection, language, productCategories);
            
            // 注意：这里需要根据实际的Eu_recall爬虫方法调用
            // 暂时返回待实现状态，因为实际的爬虫方法可能不同
            int savedCount = 0; // euRecallCrawler.crawlAndSaveToDatabase(maxPages, searchKeyword, sortField, sortDirection, language, productCategories);
            
            result.put("success", true);
            result.put("message", "EU_Recall爬虫执行完成（待实现完整功能）");
            result.put("savedCount", savedCount);
            result.put("skippedCount", 0);
            result.put("totalProcessed", savedCount);
            result.put("crawlerType", "EU_Recall");
            result.put("parameters", Map.of(
                "maxPages", maxPages,
                "searchKeyword", searchKeyword,
                "sortField", sortField,
                "sortDirection", sortDirection,
                "language", language,
                "productCategories", productCategories
            ));
            
            log.info("✅ EU_Recall爬虫执行完成，保存记录数: {}", savedCount);
            
        } catch (AllDataDuplicateException e) {
            log.warn("⚠️ EU_Recall爬虫执行完成，所有数据都是重复的: {}", e.getMessage());
            result.put("success", true);
            result.put("message", "爬取完成，但没有数据更新。");
            result.put("savedCount", 0);
            result.put("skippedCount", parseSkippedCount(e.getMessage()));
            result.put("totalProcessed", parseSkippedCount(e.getMessage()));
            result.put("crawlerType", "EU_Recall");
            result.put("isAllDuplicate", true);
        } catch (Exception e) {
            log.error("❌ EU_Recall爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_Recall爬虫执行失败: " + e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", 0);
            result.put("totalProcessed", 0);
            result.put("crawlerType", "EU_Recall");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量爬取EU_Recall爬虫（搜索关键词列表）
     * @param searchKeywords 搜索关键词列表(逗号分隔)
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小，默认50
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 爬取结果
     */
    @PostMapping("/test/eu-recall/batch")
    public ResponseEntity<Map<String, Object>> testEURecallBatch(
            @RequestParam(required = false, defaultValue = "medical device,pacemaker,defibrillator") String searchKeywords,
            @RequestParam(required = false, defaultValue = "-1") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "50") Integer batchSize,
            @RequestParam(required = false, defaultValue = "") String dateFrom,
            @RequestParam(required = false, defaultValue = "") String dateTo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始批量执行EU_Recall爬虫");
            log.info("参数: searchKeywords={}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                    searchKeywords, maxRecords, batchSize, dateFrom, dateTo);
            
            // 解析搜索关键词列表
            List<String> keywordList = new ArrayList<>();
            if (searchKeywords != null && !searchKeywords.trim().isEmpty()) {
                String[] keywords = searchKeywords.split(",");
                for (String keyword : keywords) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty()) {
                        keywordList.add(trimmedKeyword);
                    }
                }
            }
            
            if (keywordList.isEmpty()) {
                keywordList.add("medical device"); // 默认值
            }
            
            Map<String, Object> crawlResult = euRecallCrawler.crawlAndSaveWithKeywords(keywordList, maxRecords, batchSize, dateFrom, dateTo);
            
            result.put("success", crawlResult.get("success"));
            result.put("message", crawlResult.get("message"));
            result.put("totalProcessed", crawlResult.get("totalProcessed"));
            result.put("successCount", crawlResult.get("successCount"));
            result.put("failureCount", crawlResult.get("failureCount"));
            result.put("totalSaved", crawlResult.get("totalSaved"));
            result.put("totalSkipped", crawlResult.get("totalSkipped"));
            result.put("failedKeywords", crawlResult.get("failedKeywords"));
            result.put("keywordResults", crawlResult.get("keywordResults"));
            result.put("crawlerType", "EU_Recall_Batch");
            result.put("parameters", Map.of(
                "searchKeywords", searchKeywords,
                "maxRecords", maxRecords,
                "batchSize", batchSize,
                "dateFrom", dateFrom,
                "dateTo", dateTo
            ));
            
            log.info("✅ EU_Recall批量爬虫执行完成");
            
        } catch (Exception e) {
            log.error("❌ EU_Recall批量爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_Recall批量爬虫执行失败: " + e.getMessage());
            result.put("crawlerType", "EU_Recall_Batch");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 测试/执行EU_Registration爬虫
     * @param inputKeywords 关键词列表(逗号分隔)
     * @param maxRecords 最大记录数，0或-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 开始日期(可选,格式:yyyy-MM-dd)
     * @param dateTo 结束日期(可选,格式:yyyy-MM-dd)
     * @return 爬取结果
     */
    @PostMapping("/test/eu-registration")
    public ResponseEntity<Map<String, Object>> testEURegistration(
            @RequestParam(required = false, defaultValue = "") String inputKeywords,
            @RequestParam(required = false, defaultValue = "100") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "50") Integer batchSize,
            @RequestParam(required = false, defaultValue = "") String dateFrom,
            @RequestParam(required = false, defaultValue = "") String dateTo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始执行EU_Registration爬虫");
            log.info("参数: inputKeywords={}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                    inputKeywords, maxRecords, batchSize, dateFrom, dateTo);
            
            // 处理maxRecords参数：0或-1表示爬取所有数据
            int actualMaxRecords = maxRecords;
            if (maxRecords == 0 || maxRecords == -1) {
                actualMaxRecords = -1;
                log.info("maxRecords为{}，将爬取所有数据", maxRecords);
            }
            
            // 注意：这里需要根据实际的Eu_registration爬虫方法调用
            // 暂时返回待实现状态，因为实际的爬虫方法可能不同
            String crawlResult = "待实现完整功能";
            
            result.put("success", true);
            result.put("message", "EU_Registration爬虫执行完成（待实现完整功能）");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("maxRecords", actualMaxRecords);
            result.put("originalMaxRecords", maxRecords);
            result.put("crawlerType", "EU_Registration");
            result.put("parameters", Map.of(
                "inputKeywords", inputKeywords,
                "maxRecords", maxRecords,
                "batchSize", batchSize,
                "dateFrom", dateFrom,
                "dateTo", dateTo
            ));
            
            log.info("⚠️ EU_Registration爬虫待实现");
            
        } catch (Exception e) {
            log.error("❌ EU_Registration爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_Registration爬虫执行失败: " + e.getMessage());
            result.put("savedCount", 0);
            result.put("skippedCount", 0);
            result.put("totalProcessed", 0);
            result.put("crawlerType", "EU_Registration");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 从异常消息中解析跳过的记录数
     * @param message 异常消息
     * @return 跳过的记录数
     */
    private int parseSkippedCount(String message) {
        try {
            if (message != null && message.contains("跳过记录数:")) {
                String[] parts = message.split("跳过记录数:");
                if (parts.length > 1) {
                    String numberPart = parts[1].trim().split("\\s")[0];
                    return Integer.parseInt(numberPart);
                }
            }
        } catch (Exception e) {
            log.warn("解析跳过记录数失败: {}", e.getMessage());
        }
        return 0;
    }
    
    /**
     * 获取欧盟爬虫列表
     * @return 爬虫列表信息
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getEUCrawlerList() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, String>> crawlers = List.of(
                Map.of(
                    "key", "eu-custom-case",
                    "name", "EU_CustomCase",
                    "displayName", "欧盟海关案例爬虫",
                    "description", "爬取欧盟TARIC编码关税措施数据",
                    "status", "available"
                ),
                Map.of(
                    "key", "eu-guidance",
                    "name", "EU_Guidance",
                    "displayName", "欧盟指导文档爬虫",
                    "description", "爬取欧盟医疗设备最新更新新闻",
                    "status", "available"
                ),
                Map.of(
                    "key", "eu-recall",
                    "name", "EU_Recall",
                    "displayName", "欧盟召回数据爬虫",
                    "description", "爬取欧盟医疗器械召回数据",
                    "status", "available"
                ),
                Map.of(
                    "key", "eu-registration",
                    "name", "EU_Registration",
                    "displayName", "欧盟注册数据爬虫",
                    "description", "爬取EUDAMED设备数据库中的设备信息",
                    "status", "developing"
                )
            );
            
            result.put("success", true);
            result.put("crawlers", crawlers);
            result.put("total", crawlers.size());
            
        } catch (Exception e) {
            log.error("获取欧盟爬虫列表失败", e);
            result.put("success", false);
            result.put("message", "获取爬虫列表失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量爬取EU_Registration爬虫（关键词列表）
     * @param inputKeywords 关键词列表(逗号分隔)
     * @param maxRecords 最大记录数，0或-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 开始日期(可选,格式:yyyy-MM-dd)
     * @param dateTo 结束日期(可选,格式:yyyy-MM-dd)
     * @return 爬取结果
     */
    @PostMapping("/test/eu-registration/batch")
    public ResponseEntity<Map<String, Object>> testEURegistrationBatch(
            @RequestParam(required = false, defaultValue = "medical device,pacemaker,defibrillator") String inputKeywords,
            @RequestParam(required = false, defaultValue = "100") Integer maxRecords,
            @RequestParam(required = false, defaultValue = "50") Integer batchSize,
            @RequestParam(required = false, defaultValue = "") String dateFrom,
            @RequestParam(required = false, defaultValue = "") String dateTo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("🚀 开始批量执行EU_Registration爬虫");
            log.info("参数: inputKeywords={}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                    inputKeywords, maxRecords, batchSize, dateFrom, dateTo);
            
            // 解析关键词列表
            List<String> keywordList = new ArrayList<>();
            if (inputKeywords != null && !inputKeywords.trim().isEmpty()) {
                String[] keywords = inputKeywords.split(",");
                for (String keyword : keywords) {
                    String trimmedKeyword = keyword.trim();
                    if (!trimmedKeyword.isEmpty()) {
                        keywordList.add(trimmedKeyword);
                    }
                }
            }
            
            if (keywordList.isEmpty()) {
                keywordList.add("medical device"); // 默认值
            }
            
            // 处理maxRecords参数：0或-1表示爬取所有数据
            int actualMaxRecords = maxRecords;
            if (maxRecords == 0 || maxRecords == -1) {
                actualMaxRecords = -1;
                log.info("maxRecords为{}，将爬取所有数据", maxRecords);
            }
            
            String crawlResult = euRegistrationCrawler.crawlAndSaveWithKeywords(keywordList, actualMaxRecords, batchSize, dateFrom, dateTo);
            
            result.put("success", true);
            result.put("message", "EU_Registration批量爬虫执行完成");
            result.put("databaseResult", crawlResult);
            result.put("savedToDatabase", true);
            result.put("maxRecords", actualMaxRecords);
            result.put("originalMaxRecords", maxRecords);
            result.put("keywordsProcessed", keywordList.size());
            result.put("keywords", keywordList);
            result.put("crawlerType", "EU_Registration_Batch");
            result.put("parameters", Map.of(
                "inputKeywords", inputKeywords,
                "maxRecords", maxRecords,
                "batchSize", batchSize,
                "dateFrom", dateFrom,
                "dateTo", dateTo
            ));
            
            log.info("✅ EU_Registration批量爬虫执行完成");
            
        } catch (Exception e) {
            log.error("❌ EU_Registration批量爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "EU_Registration批量爬虫执行失败: " + e.getMessage());
            result.put("crawlerType", "EU_Registration_Batch");
            result.put("error", e.getMessage());
            result.put("errorDetails", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}