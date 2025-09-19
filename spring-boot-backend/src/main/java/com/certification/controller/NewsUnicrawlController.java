package com.certification.controller;

import com.certification.crawler.certification.NewsUnicrawl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 新闻统一爬虫控制器
 * 提供统一的REST API接口来调用多个爬虫
 */
@Slf4j
@RestController
@RequestMapping("/api/news-unicrawl")
@CrossOrigin(originPatterns = "*")
@Tag(name = "新闻统一爬虫", description = "统一调用SGS和UL爬虫的接口")
public class NewsUnicrawlController {

    @Autowired
    private NewsUnicrawl newsUnicrawl;

    /**
     * 获取所有可用的爬虫列表
     */
    @GetMapping("/crawlers")
    @Operation(summary = "获取可用爬虫列表", description = "获取所有可用的爬虫及其状态信息")
    public ResponseEntity<Map<String, Object>> getAvailableCrawlers() {
        try {
            List<Map<String, Object>> crawlers = newsUnicrawl.getAvailableCrawlers();
            
            Map<String, Object> result = Map.of(
                "success", true,
                "crawlers", crawlers,
                "count", crawlers.size(),
                "message", "获取爬虫列表成功"
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取爬虫列表失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "获取爬虫列表失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 执行所有可用爬虫的爬取任务
     */
    @PostMapping("/execute-all")
    @Operation(summary = "执行所有爬虫", description = "并发执行所有可用的爬虫")
    public ResponseEntity<Map<String, Object>> executeAllCrawlers(
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "每个爬虫爬取的数量") @RequestParam(defaultValue = "50") int countPerCrawler) {
        
        try {
            log.info("收到执行所有爬虫请求 - 关键词: {}, 每个爬虫数量: {}", keyword, countPerCrawler);
            
            Map<String, Object> result = newsUnicrawl.executeAllCrawlers(keyword, countPerCrawler);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行所有爬虫失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "执行所有爬虫失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 执行指定爬虫的爬取任务
     */
    @PostMapping("/execute/{crawlerName}")
    @Operation(summary = "执行指定爬虫", description = "执行指定名称的爬虫")
    public ResponseEntity<Map<String, Object>> executeSpecificCrawler(
            @Parameter(description = "爬虫名称（SGS或UL）") @PathVariable String crawlerName,
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "50") int count) {
        
        try {
            log.info("收到执行指定爬虫请求 - 爬虫: {}, 关键词: {}, 数量: {}", crawlerName, keyword, count);
            
            Map<String, Object> result = newsUnicrawl.executeSpecificCrawler(crawlerName, keyword, count);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行指定爬虫失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "crawlerName", crawlerName,
                "error", "执行指定爬虫失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 执行SGS爬虫的过滤条件爬取
     */
    @PostMapping("/execute/sgs/filters")
    @Operation(summary = "执行SGS爬虫（过滤条件）", description = "使用过滤条件执行SGS爬虫")
    public ResponseEntity<Map<String, Object>> executeSgsCrawlerWithFilters(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "50") int count,
            @Parameter(description = "新闻类型值（可选）") @RequestParam(required = false) String newsType,
            @Parameter(description = "日期范围值（可选）") @RequestParam(required = false) String dateRange,
            @Parameter(description = "主题值列表（逗号分隔，可选）") @RequestParam(required = false) String topics) {
        
        try {
            log.info("收到SGS爬虫过滤条件请求 - 关键词: {}, 数量: {}, 新闻类型: {}, 日期范围: {}, 主题: {}", 
                keyword, count, newsType, dateRange, topics);
            
            // 解析主题列表
            List<String> topicList = null;
            if (topics != null && !topics.trim().isEmpty()) {
                topicList = List.of(topics.split(","));
                // 去除空白字符
                topicList = topicList.stream().map(String::trim).toList();
            }
            
            Map<String, Object> result = newsUnicrawl.executeSgsCrawlerWithFilters(
                keyword, count, newsType, dateRange, topicList);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行SGS爬虫（过滤条件）失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "crawlerName", "SGS",
                "error", "执行SGS爬虫（过滤条件）失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取爬虫状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取爬虫状态", description = "获取所有爬虫的状态和数据库统计信息")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        try {
            Map<String, Object> result = newsUnicrawl.getCrawlerStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取爬虫状态失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "获取爬虫状态失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取SGS爬虫的过滤选项
     */
    @GetMapping("/sgs/filter-options")
    @Operation(summary = "获取SGS过滤选项", description = "获取SGS爬虫可用的过滤选项")
    public ResponseEntity<Map<String, Object>> getSgsFilterOptions() {
        try {
            Map<String, Object> result = newsUnicrawl.getSgsFilterOptions();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取SGS过滤选项失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "获取SGS过滤选项失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取API配置信息
     */
    @GetMapping("/config")
    @Operation(summary = "获取API配置", description = "获取统一爬虫API的配置信息")
    public ResponseEntity<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = Map.of(
                "apiName", "新闻统一爬虫API",
                "version", "1.0.0",
                "description", "统一调用SGS和UL爬虫的REST API",
                "endpoints", Map.of(
                    "getCrawlers", "GET /api/news-unicrawl/crawlers - 获取可用爬虫列表",
                    "executeAll", "POST /api/news-unicrawl/execute-all - 执行所有爬虫",
                    "executeSpecific", "POST /api/news-unicrawl/execute/{crawlerName} - 执行指定爬虫",
                    "executeSgsFilters", "POST /api/news-unicrawl/execute/sgs/filters - 执行SGS爬虫（过滤条件）",
                    "testBeice", "POST /api/news-unicrawl/execute/beice/test - 测试北测爬虫",
                    "getStatus", "GET /api/news-unicrawl/status - 获取爬虫状态",
                    "getBeiceStatus", "GET /api/news-unicrawl/beice/status - 获取北测爬虫状态",
                    "getSgsFilterOptions", "GET /api/news-unicrawl/sgs/filter-options - 获取SGS过滤选项",
                    "getConfig", "GET /api/news-unicrawl/config - 获取API配置"
                ),
                "supportedCrawlers", List.of("SGS", "UL", "BEICE"),
                "defaultCount", 50,
                "maxCount", 1000
            );
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("获取API配置失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "获取API配置失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 专门测试北测爬虫的接口
     */
    @PostMapping("/execute/beice/test")
    @Operation(summary = "测试北测爬虫", description = "专门用于测试北测爬虫功能的接口")
    public ResponseEntity<Map<String, Object>> testBeiceCrawler(
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "10") int count) {
        
        try {
            log.info("收到北测爬虫测试请求 - 关键词: {}, 数量: {}", keyword, count);
            
            Map<String, Object> result = newsUnicrawl.executeSpecificCrawler("BEICE", keyword, count);
            
            // 添加测试标识
            result.put("testMode", true);
            result.put("crawlerType", "北测爬虫");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("北测爬虫测试失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "crawlerName", "BEICE",
                "testMode", true,
                "error", "北测爬虫测试失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取北测爬虫状态
     */
    @GetMapping("/beice/status")
    @Operation(summary = "获取北测爬虫状态", description = "获取北测爬虫的详细状态信息")
    public ResponseEntity<Map<String, Object>> getBeiceCrawlerStatus() {
        try {
            // 通过NewsUnicrawl获取整体状态，然后提取北测爬虫信息
            Map<String, Object> overallStatus = newsUnicrawl.getCrawlerStatus();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> crawlers = (List<Map<String, Object>>) overallStatus.get("availableCrawlers");
            
            Map<String, Object> beiceCrawlerInfo = null;
            if (crawlers != null) {
                beiceCrawlerInfo = crawlers.stream()
                    .filter(crawler -> "BEICE".equals(crawler.get("name")))
                    .findFirst()
                    .orElse(null);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> dbStats = (Map<String, Object>) overallStatus.get("databaseStats");
            Long beiceCount = dbStats != null ? (Long) dbStats.get("beiceCount") : 0L;
            
            Map<String, Object> result = Map.of(
                "success", true,
                "crawlerInfo", beiceCrawlerInfo != null ? beiceCrawlerInfo : Map.of(),
                "dataCount", beiceCount,
                "timestamp", overallStatus.get("timestamp")
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取北测爬虫状态失败: {}", e.getMessage(), e);
            Map<String, Object> result = Map.of(
                "success", false,
                "error", "获取北测爬虫状态失败: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(result);
        }
    }
}