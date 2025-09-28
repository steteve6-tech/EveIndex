package com.certification.controller;

import com.certification.crawler.certification.BeiceCrawler;
import com.certification.crawler.certification.SgsCrawler;
import com.certification.crawler.certification.ULCrawler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 新闻统一爬虫控制器
 * 提供统一的REST API接口来调用BeiceCrawler、SgsCrawler、ULCrawler三个爬虫
 */
@Slf4j
@RestController
@RequestMapping("/api/crawlers")
@CrossOrigin(originPatterns = "*")
@Tag(name = "认证新闻爬虫管理", description = "统一管理北测、SGS、UL三个爬虫的接口")
public class NewsUnicrawlController {

    @Autowired
    private BeiceCrawler beiceCrawler;
    
    @Autowired
    private SgsCrawler sgsCrawler;
    
    @Autowired
    private ULCrawler ulCrawler;

    /**
     * 测试北测爬虫
     */
    @GetMapping("/beice/test")
    @Operation(summary = "测试北测爬虫", description = "测试北测爬虫连接和基本功能")
    public ResponseEntity<Map<String, Object>> testBeiceCrawler() {
        try {
            log.info("收到北测爬虫测试请求");
            
            // 执行小量测试爬取
            Map<String, Object> result = beiceCrawler.executeBeiceCrawlerAndSave(5);
            
            result.put("crawlerName", "BEICE");
            result.put("crawlerDisplayName", "北测爬虫");
            result.put("testMode", true);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试北测爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "BEICE",
                "crawlerDisplayName", "北测爬虫",
                "testMode", true,
                "error", "测试北测爬虫失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 执行北测爬虫
     */
    @PostMapping("/beice/execute")
    @Operation(summary = "执行北测爬虫", description = "执行北测爬虫进行数据爬取")
    public ResponseEntity<Map<String, Object>> executeBeiceCrawler(
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "50") int count) {
        
        try {
            log.info("收到北测爬虫执行请求 - 关键词: {}, 数量: {}", keyword, count);
            
            Map<String, Object> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = beiceCrawler.executeBeiceCrawlerWithKeywordAndSave(keyword, count);
            } else {
                result = beiceCrawler.executeBeiceCrawlerAndSave(count);
            }
            
            result.put("crawlerName", "BEICE");
            result.put("crawlerDisplayName", "北测爬虫");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行北测爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "BEICE",
                "crawlerDisplayName", "北测爬虫",
                "error", "执行北测爬虫失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 测试SGS爬虫
     */
    @GetMapping("/sgs/test")
    @Operation(summary = "测试SGS爬虫", description = "测试SGS爬虫连接和基本功能")
    public ResponseEntity<Map<String, Object>> testSgsCrawler() {
        try {
            log.info("收到SGS爬虫测试请求");
            
            // 执行小量测试爬取
            Map<String, Object> result = sgsCrawler.executeSgsCrawlerAndSave(5);
            
            result.put("crawlerName", "SGS");
            result.put("crawlerDisplayName", "SGS爬虫");
            result.put("testMode", true);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试SGS爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "SGS",
                "crawlerDisplayName", "SGS爬虫",
                "testMode", true,
                "error", "测试SGS爬虫失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 执行SGS爬虫
     */
    @PostMapping("/sgs/execute")
    @Operation(summary = "执行SGS爬虫", description = "执行SGS爬虫进行数据爬取")
    public ResponseEntity<Map<String, Object>> executeSgsCrawler(
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "50") int count) {
        
        try {
            log.info("收到SGS爬虫执行请求 - 数量: {}", count);
            
            // 直接使用普通爬取方法，不使用关键词搜索
            Map<String, Object> result = sgsCrawler.executeSgsCrawlerAndSave(count);
            
            result.put("crawlerName", "SGS");
            result.put("crawlerDisplayName", "SGS爬虫");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行SGS爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "SGS",
                "crawlerDisplayName", "SGS爬虫",
                "error", "执行SGS爬虫失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 测试UL爬虫
     */
    @GetMapping("/ul/test")
    @Operation(summary = "测试UL爬虫", description = "测试UL爬虫连接和基本功能")
    public ResponseEntity<Map<String, Object>> testULCrawler() {
        try {
            log.info("收到UL爬虫测试请求");
            
            // 执行小量测试爬取
            Map<String, Object> result = ulCrawler.executeULCrawlerAndSave(5);
            
            result.put("crawlerName", "UL");
            result.put("crawlerDisplayName", "UL爬虫");
            result.put("testMode", true);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("测试UL爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "UL",
                "crawlerDisplayName", "UL爬虫",
                "testMode", true,
                "error", "测试UL爬虫失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 执行UL爬虫
     */
    @PostMapping("/ul/execute")
    @Operation(summary = "执行UL爬虫", description = "执行UL爬虫进行数据爬取")
    public ResponseEntity<Map<String, Object>> executeULCrawler(
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false) String keyword,
            @Parameter(description = "爬取数量") @RequestParam(defaultValue = "50") int count) {
        
        try {
            log.info("收到UL爬虫执行请求 - 关键词: {}, 数量: {}", keyword, count);
            
            Map<String, Object> result;
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = ulCrawler.executeULCrawlerWithKeywordAndSave(keyword, count);
            } else {
                result = ulCrawler.executeULCrawlerAndSave(count);
            }
            
            result.put("crawlerName", "UL");
            result.put("crawlerDisplayName", "UL爬虫");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("执行UL爬虫失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "crawlerName", "UL",
                "crawlerDisplayName", "UL爬虫",
                "error", "执行UL爬虫失败: " + e.getMessage()
            ));
        }
    }
}