package com.certification.controller;

import com.certification.standards.CrawlerDataKeywordMatcher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CrawlerData关键词匹配控制器
 * 提供关键词匹配相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/crawler-data-keyword-matcher")
@Tag(name = "CrawlerData关键词匹配", description = "CrawlerData关键词匹配相关接口")
public class CrawlerDataKeywordMatcherController {

    @Autowired
    private CrawlerDataKeywordMatcher crawlerDataKeywordMatcher;

    /**
     * 执行关键词匹配（所有数据）
     * @param batchSize 批处理大小，默认100
     * @return 匹配结果
     */
    @PostMapping("/execute")
    @Operation(summary = "执行关键词匹配", description = "对所有未处理的CrawlerData执行关键词匹配并更新related字段")
    public ResponseEntity<Map<String, Object>> executeKeywordMatching(
            @Parameter(description = "批处理大小", example = "100")
            @RequestParam(defaultValue = "100") int batchSize) {
        
        log.info("收到关键词匹配请求，批处理大小: {}", batchSize);
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.executeKeywordMatching(batchSize);
            
            if ((Boolean) result.get("success")) {
                log.info("关键词匹配执行成功，处理: {} 条数据", result.get("totalProcessed"));
                return ResponseEntity.ok(result);
            } else {
                log.error("关键词匹配执行失败: {}", result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("关键词匹配执行异常", e);
            Map<String, Object> errorResult = Map.of(
                "success", false,
                "error", "执行异常: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 执行关键词匹配（指定数据源）
     * @param sourceName 数据源名称
     * @param batchSize 批处理大小，默认100
     * @return 匹配结果
     */
    @PostMapping("/execute/{sourceName}")
    @Operation(summary = "执行关键词匹配（指定数据源）", description = "对指定数据源的未处理CrawlerData执行关键词匹配并更新related字段")
    public ResponseEntity<Map<String, Object>> executeKeywordMatchingBySource(
            @Parameter(description = "数据源名称", example = "SGS")
            @PathVariable String sourceName,
            @Parameter(description = "批处理大小", example = "100")
            @RequestParam(defaultValue = "100") int batchSize) {
        
        log.info("收到关键词匹配请求（数据源: {}），批处理大小: {}", sourceName, batchSize);
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.executeKeywordMatchingBySource(sourceName, batchSize);
            
            if ((Boolean) result.get("success")) {
                log.info("关键词匹配执行成功（数据源: {}），处理: {} 条数据", sourceName, result.get("totalProcessed"));
                return ResponseEntity.ok(result);
            } else {
                log.error("关键词匹配执行失败（数据源: {}）: {}", sourceName, result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("关键词匹配执行异常（数据源: {}）", sourceName, e);
            Map<String, Object> errorResult = Map.of(
                "success", false,
                "error", "执行异常: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 获取匹配统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取匹配统计信息", description = "获取CrawlerData关键词匹配的统计信息")
    public ResponseEntity<Map<String, Object>> getMatchingStatistics() {
        
        log.info("收到获取匹配统计信息请求");
        
        try {
            Map<String, Object> result = crawlerDataKeywordMatcher.getMatchingStatistics();
            
            if ((Boolean) result.get("success")) {
                log.info("获取匹配统计信息成功");
                return ResponseEntity.ok(result);
            } else {
                log.error("获取匹配统计信息失败: {}", result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("获取匹配统计信息异常", e);
            Map<String, Object> errorResult = Map.of(
                "success", false,
                "error", "获取统计信息异常: " + e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 健康检查接口
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查关键词匹配服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        log.debug("收到健康检查请求");
        
        Map<String, Object> healthResult = Map.of(
            "status", "UP",
            "service", "CrawlerDataKeywordMatcher",
            "timestamp", java.time.LocalDateTime.now().toString(),
            "message", "关键词匹配服务运行正常"
        );
        
        return ResponseEntity.ok(healthResult);
    }
}
