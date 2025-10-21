package com.certification.controller;

import com.certification.crawler.countrydata.kr.KrRecall;
import com.certification.crawler.countrydata.kr.KR_regstration;
import com.certification.crawler.countrydata.kr.KrEvent;
import com.certification.crawler.countrydata.kr.KrGuidance;
import com.certification.crawler.countrydata.kr.Kr_customcase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 韩国医疗设备爬虫控制器
 * 提供韩国MFDS (Ministry of Food and Drug Safety) 数据爬取接口
 * 支持4种数据类型：召回、注册、不良事件、指导文档
 */
@Slf4j
@RestController
@RequestMapping("/kr-crawler")
@CrossOrigin(originPatterns = "*")
@Tag(name = "韩国医疗设备爬虫", description = "韩国食品药品安全处(MFDS)医疗设备数据爬取管理")
public class KRCrawlerController {

    @Autowired
    private KrRecall krRecallCrawler;
    
    @Autowired
    private KR_regstration krRegistrationCrawler;
    
    @Autowired
    private KrEvent krEventCrawler;
    
    @Autowired
    private KrGuidance krGuidanceCrawler;
    
    @Autowired
    private Kr_customcase krCustomsCaseCrawler;

    // ==================== 召回数据爬虫 ====================

    /**
     * 测试韩国召回爬虫
     */
    @PostMapping("/test/recall")
    @Operation(summary = "测试韩国召回爬虫", description = "测试爬取韩国MFDS召回数据（限制10条）")
    public ResponseEntity<Map<String, Object>> testKRRecall(
            @Parameter(description = "公司名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String itemNames,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🧪 测试韩国召回爬虫");
        log.info("📊 公司: {}, 产品: {}, 日期: {} - {}", companyNames, itemNames, dateFrom, dateTo);

        return executeRecallCrawler(companyNames, itemNames, 10, 10, dateFrom, dateTo, true);
    }

    /**
     * 执行韩国召回爬虫
     */
    @PostMapping("/execute/recall")
    @Operation(summary = "执行韩国召回爬虫", description = "执行韩国MFDS召回数据爬取")
    public ResponseEntity<Map<String, Object>> executeKRRecall(
            @Parameter(description = "公司名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String itemNames,
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🚀 执行韩国召回爬虫");
        
        return executeRecallCrawler(companyNames, itemNames, maxRecords, batchSize, dateFrom, dateTo, false);
    }

    /**
     * 召回爬虫执行逻辑
     */
    private ResponseEntity<Map<String, Object>> executeRecallCrawler(
            String companyNames, String itemNames, int maxRecords, int batchSize,
            String dateFrom, String dateTo, boolean isTest) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> companyList = parseKeywords(companyNames);
            List<String> itemList = parseKeywords(itemNames);
            
            String crawlResult = krRecallCrawler.crawlWithMultipleFields(
                companyList, itemList, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", isTest ? "韩国召回爬虫测试完成" : "韩国召回爬虫执行完成");
            result.put("crawlerType", "KR_Recall");
            result.put("result", crawlResult);
            
            extractAndAddCounts(result, crawlResult);
            
            log.info("✅ 韩国召回爬虫{}成功: {}", isTest ? "测试" : "执行", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国召回爬虫{}失败", isTest ? "测试" : "执行", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 注册数据爬虫 ====================

    /**
     * 测试韩国注册爬虫
     */
    @PostMapping("/test/registration")
    @Operation(summary = "测试韩国注册爬虫", description = "测试爬取韩国MFDS注册数据（限制10条）")
    public ResponseEntity<Map<String, Object>> testKRRegistration(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String searchQuery,
            @Parameter(description = "企业名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🧪 测试韩国注册爬虫");
        
        return executeRegistrationCrawler(searchQuery, companyNames, 10, 10, dateFrom, dateTo, true);
    }

    /**
     * 执行韩国注册爬虫
     */
    @PostMapping("/execute/registration")
    @Operation(summary = "执行韩国注册爬虫", description = "执行韩国MFDS注册数据爬取")
    public ResponseEntity<Map<String, Object>> executeKRRegistration(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String searchQuery,
            @Parameter(description = "企业名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🚀 执行韩国注册爬虫");
        
        return executeRegistrationCrawler(searchQuery, companyNames, maxRecords, batchSize, dateFrom, dateTo, false);
    }

    /**
     * 注册爬虫执行逻辑
     */
    private ResponseEntity<Map<String, Object>> executeRegistrationCrawler(
            String searchQuery, String companyNames, int maxRecords, int batchSize,
            String dateFrom, String dateTo, boolean isTest) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> companyList = parseKeywords(companyNames);
            
            String crawlResult = krRegistrationCrawler.crawlWithMultipleFields(
                searchQuery, companyList, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", isTest ? "韩国注册爬虫测试完成" : "韩国注册爬虫执行完成");
            result.put("crawlerType", "KR_Registration");
            result.put("result", crawlResult);
            
            extractAndAddCounts(result, crawlResult);
            
            log.info("✅ 韩国注册爬虫{}成功: {}", isTest ? "测试" : "执行", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国注册爬虫{}失败", isTest ? "测试" : "执行", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 不良事件爬虫 ====================

    /**
     * 测试韩国不良事件爬虫
     */
    @PostMapping("/test/event")
    @Operation(summary = "测试韩国不良事件爬虫", description = "测试爬取韩国MFDS不良事件数据（限制10条）")
    public ResponseEntity<Map<String, Object>> testKREvent(
            @Parameter(description = "企业名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String productNames,
            @Parameter(description = "型号名称列表（逗号分隔）") @RequestParam(required = false) String modelNames,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🧪 测试韩国不良事件爬虫");
        
        return executeEventCrawler(companyNames, productNames, modelNames, 10, 10, dateFrom, dateTo, true);
    }

    /**
     * 执行韩国不良事件爬虫
     */
    @PostMapping("/execute/event")
    @Operation(summary = "执行韩国不良事件爬虫", description = "执行韩国MFDS不良事件数据爬取")
    public ResponseEntity<Map<String, Object>> executeKREvent(
            @Parameter(description = "企业名称列表（逗号分隔）") @RequestParam(required = false) String companyNames,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String productNames,
            @Parameter(description = "型号名称列表（逗号分隔）") @RequestParam(required = false) String modelNames,
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false) String dateTo) {

        log.info("🚀 执行韩国不良事件爬虫");
        
        return executeEventCrawler(companyNames, productNames, modelNames, maxRecords, batchSize, dateFrom, dateTo, false);
    }

    /**
     * 不良事件爬虫执行逻辑
     */
    private ResponseEntity<Map<String, Object>> executeEventCrawler(
            String companyNames, String productNames, String modelNames,
            int maxRecords, int batchSize, String dateFrom, String dateTo, boolean isTest) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> companyList = parseKeywords(companyNames);
            List<String> productList = parseKeywords(productNames);
            List<String> modelList = parseKeywords(modelNames);
            
            String crawlResult = krEventCrawler.crawlWithMultipleFields(
                companyList, productList, modelList, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", isTest ? "韩国不良事件爬虫测试完成" : "韩国不良事件爬虫执行完成");
            result.put("crawlerType", "KR_Event");
            result.put("result", crawlResult);
            
            extractAndAddCounts(result, crawlResult);
            
            log.info("✅ 韩国不良事件爬虫{}成功: {}", isTest ? "测试" : "执行", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国不良事件爬虫{}失败", isTest ? "测试" : "执行", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 指导文档爬虫 ====================

    /**
     * 测试韩国指导文档爬虫
     */
    @PostMapping("/test/guidance")
    @Operation(summary = "测试韩国指导文档爬虫", description = "测试爬取韩国MFDS指导文档（限制10条）")
    public ResponseEntity<Map<String, Object>> testKRGuidance(
            @Parameter(description = "搜索关键词列表（逗号分隔）") @RequestParam(required = false) String searchKeywords) {

        log.info("🧪 测试韩国指导文档爬虫");
        
        return executeGuidanceCrawler(searchKeywords, 10, 10, true);
    }

    /**
     * 执行韩国指导文档爬虫
     */
    @PostMapping("/execute/guidance")
    @Operation(summary = "执行韩国指导文档爬虫", description = "执行韩国MFDS指导文档数据爬取")
    public ResponseEntity<Map<String, Object>> executeKRGuidance(
            @Parameter(description = "搜索关键词列表（逗号分隔）") @RequestParam(required = false) String searchKeywords,
            @Parameter(description = "最大记录数") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize) {

        log.info("🚀 执行韩国指导文档爬虫");
        
        return executeGuidanceCrawler(searchKeywords, maxRecords, batchSize, false);
    }

    /**
     * 指导文档爬虫执行逻辑
     */
    private ResponseEntity<Map<String, Object>> executeGuidanceCrawler(
            String searchKeywords, int maxRecords, int batchSize, boolean isTest) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> keywordList = parseKeywords(searchKeywords);
            
            String crawlResult = krGuidanceCrawler.crawlWithKeywords(
                keywordList, maxRecords, batchSize);

            result.put("success", true);
            result.put("message", isTest ? "韩国指导文档爬虫测试完成" : "韩国指导文档爬虫执行完成");
            result.put("crawlerType", "KR_Guidance");
            result.put("result", crawlResult);
            
            extractAndAddCounts(result, crawlResult);
            
            log.info("✅ 韩国指导文档爬虫{}成功: {}", isTest ? "测试" : "执行", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国指导文档爬虫{}失败", isTest ? "测试" : "执行", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 信息查询接口 ====================

    /**
     * 获取韩国爬虫列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取韩国爬虫列表", description = "获取所有可用的韩国医疗设备爬虫信息")
    public ResponseEntity<Map<String, Object>> getKRCrawlerList() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Map<String, String>> crawlers = List.of(
                    Map.of(
                            "key", "kr-recall",
                            "name", "KR_Recall",
                            "displayName", "韩国召回数据爬虫",
                            "description", "爬取韩国食品药品安全处(MFDS)医疗器械召回数据",
                            "dataSource", "MFDS",
                            "status", "available",
                            "country", "KR",
                    "type", "RECALL"
                ),
                Map.of(
                    "key", "kr-registration",
                    "name", "KR_Registration",
                    "displayName", "韩国注册数据爬虫",
                    "description", "爬取韩国食品药品安全处(MFDS)医疗器械注册数据",
                    "dataSource", "MFDS",
                    "status", "available",
                    "country", "KR",
                    "type", "REGISTRATION"
                ),
                Map.of(
                    "key", "kr-event",
                    "name", "KR_Event",
                    "displayName", "韩国不良事件爬虫",
                    "description", "爬取韩国食品药品安全处(MFDS)医疗器械不良事件数据",
                    "dataSource", "MFDS",
                    "status", "available",
                    "country", "KR",
                    "type", "EVENT"
                ),
                Map.of(
                    "key", "kr-guidance",
                    "name", "KR_Guidance",
                    "displayName", "韩国指导文档爬虫",
                    "description", "爬取韩国食品药品安全处(MFDS)医疗器械指导文档",
                    "dataSource", "MFDS",
                    "status", "available",
                    "country", "KR",
                    "type", "GUIDANCE"
                    )
            );

            result.put("success", true);
            result.put("crawlers", crawlers);
            result.put("total", crawlers.size());
            result.put("country", "韩国");
            result.put("countryCode", "KR");

        } catch (Exception e) {
            log.error("获取韩国爬虫列表失败", e);
            result.put("success", false);
            result.put("message", "获取爬虫列表失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取爬虫状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取韩国爬虫状态", description = "获取韩国爬虫的运行状态和统计信息")
    public ResponseEntity<Map<String, Object>> getKRCrawlerStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("status", "active");
            result.put("country", "韩国");
            result.put("countryCode", "KR");
            result.put("dataSource", "MFDS");
            result.put("availableCrawlers", List.of("recall", "registration", "event", "guidance"));
            result.put("crawlerCount", 4);
            result.put("message", "韩国爬虫运行正常");

        } catch (Exception e) {
            log.error("获取韩国爬虫状态失败", e);
            result.put("success", false);
            result.put("message", "获取状态失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查韩国爬虫服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("status", "healthy");
            result.put("service", "KR Crawler");
            result.put("timestamp", new Date());
            result.put("crawlers", Map.of(
                "recall", "online",
                "registration", "online",
                "event", "online",
                "guidance", "online"
            ));
            result.put("message", "韩国爬虫服务运行正常");

        } catch (Exception e) {
            log.error("健康检查失败", e);
            result.put("status", "unhealthy");
            result.put("message", "服务异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== 工具方法 ====================

    /**
     * 解析关键词字符串为列表
     */
    private List<String> parseKeywords(String keywords) {
        List<String> result = new ArrayList<>();
        
        if (keywords != null && !keywords.trim().isEmpty()) {
            String[] items = keywords.split("[,，;；\\s]+");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        
        return result;
    }

    /**
     * 从结果字符串中提取并添加统计信息
     */
    private void extractAndAddCounts(Map<String, Object> result, String crawlResult) {
        if (crawlResult == null || crawlResult.isEmpty()) {
            result.put("savedCount", 0);
            result.put("skippedCount", 0);
            return;
        }

        try {
            // 提取保存数量 "保存成功: X 条"
            java.util.regex.Pattern savedPattern = java.util.regex.Pattern.compile(
                "(?:保存成功|新增|入库)[:：]?\\s*(\\d+)\\s*条");
            java.util.regex.Matcher savedMatcher = savedPattern.matcher(crawlResult);
            if (savedMatcher.find()) {
                result.put("savedCount", Integer.parseInt(savedMatcher.group(1)));
            }

            // 提取跳过数量 "跳过重复: X 条"
            java.util.regex.Pattern skippedPattern = java.util.regex.Pattern.compile(
                "(?:跳过重复|跳过|重复)[:：]?\\s*(\\d+)\\s*条");
            java.util.regex.Matcher skippedMatcher = skippedPattern.matcher(crawlResult);
            if (skippedMatcher.find()) {
                result.put("skippedCount", Integer.parseInt(skippedMatcher.group(1)));
            }

            // 提取总共保存数量 "总共保存: X 条"
            java.util.regex.Pattern totalPattern = java.util.regex.Pattern.compile(
                "(?:总共保存|总计)[:：]?\\s*(\\d+)\\s*条");
            java.util.regex.Matcher totalMatcher = totalPattern.matcher(crawlResult);
            if (totalMatcher.find()) {
                result.put("totalSaved", Integer.parseInt(totalMatcher.group(1)));
            }
        } catch (Exception e) {
            log.debug("提取统计信息失败: {}", e.getMessage());
        }
    }

    // ==================== 海关案例数据爬虫 ====================

    /**
     * 测试韩国海关案例爬虫
     */
    @PostMapping("/test/customs-case")
    @Operation(summary = "测试韩国海关案例爬虫", description = "测试爬取韩国海关案例数据（限制10条）")
    public ResponseEntity<Map<String, Object>> testKRCustomsCase(
            @Parameter(description = "搜索关键词列表（逗号分隔）") @RequestParam(required = false) String searchKeywords,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String productNames,
            @Parameter(description = "开始日期（yyyyMMdd）") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期（yyyyMMdd）") @RequestParam(required = false) String dateTo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("测试韩国海关案例爬虫 - 搜索关键词: {}, 产品名称: {}, 日期范围: {} - {}", 
                    searchKeywords, productNames, dateFrom, dateTo);
            
            List<String> searchKeywordList = parseKeywords(searchKeywords);
            List<String> productNameList = parseKeywords(productNames);
            
            String crawlResult = krCustomsCaseCrawler.crawlWithMultipleFields(
                searchKeywordList, productNameList, 10, 10, dateFrom, dateTo);
            
            result.put("success", true);
            result.put("message", "韩国海关案例爬虫测试完成");
            result.put("crawlResult", crawlResult);
            extractAndAddCounts(result, crawlResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("测试韩国海关案例爬虫失败", e);
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 执行韩国海关案例爬虫
     */
    @PostMapping("/execute/customs-case")
    @Operation(summary = "执行韩国海关案例爬虫", description = "执行完整的韩国海关案例数据爬取")
    public ResponseEntity<Map<String, Object>> executeKRCustomsCase(
            @Parameter(description = "搜索关键词列表（逗号分隔）") @RequestParam(required = false) String searchKeywords,
            @Parameter(description = "产品名称列表（逗号分隔）") @RequestParam(required = false) String productNames,
            @Parameter(description = "最大记录数（-1表示全部）") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期（yyyyMMdd）") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期（yyyyMMdd）") @RequestParam(required = false) String dateTo) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("执行韩国海关案例爬虫 - 搜索关键词: {}, 产品名称: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                    searchKeywords, productNames, maxRecords, batchSize, dateFrom, dateTo);
            
            List<String> searchKeywordList = parseKeywords(searchKeywords);
            List<String> productNameList = parseKeywords(productNames);
            
            String crawlResult = krCustomsCaseCrawler.crawlWithMultipleFields(
                searchKeywordList, productNameList, maxRecords, batchSize, dateFrom, dateTo);
            
            result.put("success", true);
            result.put("message", "韩国海关案例爬虫执行完成");
            result.put("crawlResult", crawlResult);
            extractAndAddCounts(result, crawlResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("执行韩国海关案例爬虫失败", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
