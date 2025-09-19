package com.certification.controller;

import com.certification.service.NetworkDiagnosticService;
import com.certification.crawler.certification.sgs.SgsCrawler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * SGS爬虫控制器
 * 提供SGS爬虫的REST API接口
 */
@Tag(name = "SGS爬虫管理", description = "SGS爬虫的执行、状态查询和数据库操作接口")
@RestController
@RequestMapping("/sgs-crawler")
public class SgsCrawlerController {
    
    @Autowired
    private SgsCrawler sgsCrawler;
    
    @Autowired
    private NetworkDiagnosticService networkDiagnosticService;
    
    /**
     * 执行SGS爬虫并保存到数据库
     */
    @Operation(summary = "执行SGS爬虫并保存到数据库", description = "调用SGS爬虫抓取最新数据并保存到数据库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "执行成功"),
        @ApiResponse(responseCode = "400", description = "执行失败")
    })
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeSgsCrawler(
            @Parameter(description = "爬取数量", example = "10") @RequestParam(defaultValue = "10") int count) {
        
        Map<String, Object> result = sgsCrawler.executeSgsCrawlerAndSave(count);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 执行SGS爬虫（带关键词搜索）并保存到数据库
     */
    @Operation(summary = "执行SGS爬虫（关键词搜索）并保存到数据库", description = "根据关键词调用SGS爬虫抓取数据并保存到数据库")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "执行成功"),
        @ApiResponse(responseCode = "400", description = "执行失败")
    })
    @PostMapping("/execute-with-keyword")
    public ResponseEntity<Map<String, Object>> executeSgsCrawlerWithKeyword(
            @Parameter(description = "搜索关键词", example = "certification") @RequestParam String keyword,
            @Parameter(description = "爬取数量", example = "10") @RequestParam(defaultValue = "10") int count) {
        
        Map<String, Object> result = sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, count);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 测试SGS爬虫连接
     */
    @Operation(summary = "测试SGS爬虫连接", description = "测试SGS爬虫的连接性和API可用性")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "连接成功"),
        @ApiResponse(responseCode = "400", description = "连接失败")
    })
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testSgsCrawlerConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试爬虫可用性
            boolean isAvailable = sgsCrawler.isAvailable();
            
            result.put("success", isAvailable);
            result.put("available", isAvailable);
            result.put("crawlerName", sgsCrawler.getCrawlerName());
            result.put("sourceName", sgsCrawler.getSourceName());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if (isAvailable) {
                result.put("message", "SGS爬虫连接正常");
                return ResponseEntity.ok(result);
            } else {
                result.put("message", "SGS爬虫连接失败");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("available", false);
            result.put("error", "测试连接失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取SGS爬虫状态
     */
    @Operation(summary = "获取SGS爬虫状态", description = "获取SGS爬虫的可用性、配置信息和数据库统计")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "爬虫未找到")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSgsCrawlerStatus() {
        Map<String, Object> result = sgsCrawler.getSgsCrawlerStatus();
        
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 批量执行SGS爬虫
     */
    @Operation(summary = "批量执行SGS爬虫", description = "执行多个关键词的SGS爬虫任务")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "执行成功"),
        @ApiResponse(responseCode = "400", description = "执行失败")
    })
    @PostMapping("/batch-execute")
    public ResponseEntity<Map<String, Object>> batchExecuteSgsCrawler(
            @Parameter(description = "关键词列表，用逗号分隔", example = "certification,testing,compliance") 
            @RequestParam(required = false, defaultValue = "") String keywords,
            @Parameter(description = "每个关键词的爬取数量", example = "5") 
            @RequestParam(defaultValue = "5") int countPerKeyword) {
        
        Map<String, Object> batchResult = new java.util.HashMap<>();
        batchResult.put("success", true);
        batchResult.put("totalKeywords", 0);
        batchResult.put("successfulKeywords", 0);
        batchResult.put("failedKeywords", 0);
        batchResult.put("results", new java.util.ArrayList<>());
        batchResult.put("timestamp", java.time.LocalDateTime.now().toString());
        
        // 如果没有提供关键词，则执行默认爬取
        if (keywords == null || keywords.trim().isEmpty()) {
            try {
                Map<String, Object> result = sgsCrawler.executeSgsCrawlerAndSave(countPerKeyword);
                batchResult.put("totalKeywords", 1);
                if ((Boolean) result.get("success")) {
                    batchResult.put("successfulKeywords", 1);
                } else {
                    batchResult.put("failedKeywords", 1);
                }
                ((java.util.List<Map<String, Object>>) batchResult.get("results")).add(result);
            } catch (Exception e) {
                batchResult.put("totalKeywords", 1);
                batchResult.put("failedKeywords", 1);
                
                Map<String, Object> errorResult = new java.util.HashMap<>();
                errorResult.put("keyword", "default");
                errorResult.put("success", false);
                errorResult.put("error", "执行失败: " + e.getMessage());
                ((java.util.List<Map<String, Object>>) batchResult.get("results")).add(errorResult);
            }
        } else {
            String[] keywordArray = keywords.split(",");
            batchResult.put("totalKeywords", keywordArray.length);
            
            for (String keyword : keywordArray) {
                keyword = keyword.trim();
                if (!keyword.isEmpty()) {
                    try {
                        Map<String, Object> result = sgsCrawler.executeSgsCrawlerWithKeywordAndSave(keyword, countPerKeyword);
                        
                        if ((Boolean) result.get("success")) {
                            batchResult.put("successfulKeywords", (Integer) batchResult.get("successfulKeywords") + 1);
                        } else {
                            batchResult.put("failedKeywords", (Integer) batchResult.get("failedKeywords") + 1);
                        }
                        
                        ((java.util.List<Map<String, Object>>) batchResult.get("results")).add(result);
                        
                    } catch (Exception e) {
                        batchResult.put("failedKeywords", (Integer) batchResult.get("failedKeywords") + 1);
                        
                        Map<String, Object> errorResult = new java.util.HashMap<>();
                        errorResult.put("keyword", keyword);
                        errorResult.put("success", false);
                        errorResult.put("error", "执行失败: " + e.getMessage());
                        ((java.util.List<Map<String, Object>>) batchResult.get("results")).add(errorResult);
                    }
                }
            }
        }
        
        return ResponseEntity.ok(batchResult);
    }
    
    /**
     * 诊断SGS网站网络连接
     */
    @Operation(summary = "诊断SGS网站网络连接", description = "诊断SGS网站的网络连接问题，包括DNS解析、TCP连接和HTTP请求")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "诊断完成"),
        @ApiResponse(responseCode = "500", description = "诊断失败")
    })
    @GetMapping("/diagnose-network")
    public ResponseEntity<Map<String, Object>> diagnoseSgsNetwork() {
        try {
            Map<String, Object> diagnosisResult = networkDiagnosticService.diagnoseSgsConnection();
            Map<String, Object> advice = networkDiagnosticService.getNetworkAdvice(diagnosisResult);
            
            Map<String, Object> result = new HashMap<>();
            result.put("diagnosis", diagnosisResult);
            result.put("advice", advice);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "网络诊断失败: " + e.getMessage());
            errorResult.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 诊断指定URL的网络连接
     */
    @Operation(summary = "诊断指定URL的网络连接", description = "诊断指定URL的网络连接问题")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "诊断完成"),
        @ApiResponse(responseCode = "500", description = "诊断失败")
    })
    @GetMapping("/diagnose-url")
    public ResponseEntity<Map<String, Object>> diagnoseUrl(
            @Parameter(description = "要诊断的URL", example = "https://www.sgs.com") 
            @RequestParam String url) {
        try {
            Map<String, Object> diagnosisResult = networkDiagnosticService.diagnoseUrl(url);
            Map<String, Object> advice = networkDiagnosticService.getNetworkAdvice(diagnosisResult);
            
            Map<String, Object> result = new HashMap<>();
            result.put("diagnosis", diagnosisResult);
            result.put("advice", advice);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "网络诊断失败: " + e.getMessage());
            errorResult.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}
