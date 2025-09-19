package com.certification.controller;

import com.certification.crawler.unification.UniDeviceCrawler;
import com.certification.crawler.countrydata.us.unicrawl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统一爬虫测试控制器
 * 提供爬虫测试功能的API接口
 */
@RestController
@RequestMapping("/unicrawler")
@Tag(name = "统一爬虫测试", description = "爬虫测试功能API接口")
public class UniCrawlerController {

    @Autowired
    private UniDeviceCrawler UniDeviceCrawler;
    
    @Autowired
    private unicrawl uniCrawler;

    /**
     * 测试UL爬虫批量保存功能
     */
    @PostMapping("/test/ul-batch-save")
    @Operation(summary = "测试UL爬虫批量保存功能", description = "测试UL爬虫的批量保存功能，每10条数据批量保存")
    public ResponseEntity<Map<String, Object>> testULCrawlerBatchSave(
            @Parameter(description = "要爬取的总数量", example = "30") @RequestParam(defaultValue = "30") int totalCount,
            @Parameter(description = "每批保存数量", example = "10") @RequestParam(defaultValue = "10") int batchSize,
            @Parameter(description = "开始位置索引", example = "0") @RequestParam(defaultValue = "0") int startIndex) {
        
        try {
            UniDeviceCrawler.testULCrawlerBatchSave(totalCount, batchSize, startIndex);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "UL爬虫批量保存测试完成，每" + batchSize + "条数据保存一次",
                    "targetCount", totalCount,
                    "batchSize", batchSize,
                    "startIndex", startIndex,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "UL爬虫批量保存测试失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 从指定位置继续执行UL爬虫
     */
    @PostMapping("/test/ul-continue-from-position")
    @Operation(summary = "从指定位置继续执行UL爬虫", description = "从指定索引位置继续执行UL爬虫")
    public ResponseEntity<Map<String, Object>> testULCrawlerContinueFromPosition(
            @Parameter(description = "要爬取的总数量", example = "10") @RequestParam(defaultValue = "10") int totalCount,
            @Parameter(description = "开始位置索引", example = "0") @RequestParam(defaultValue = "0") int startIndex) {
        
        try {
            UniDeviceCrawler.testULCrawlerContinueFromPosition(totalCount, startIndex);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "UL爬虫从位置" + startIndex + "继续执行完成",
                    "targetCount", totalCount,
                    "startIndex", startIndex,
                    "nextStartIndex", startIndex + totalCount,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "UL爬虫继续执行失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取UL爬虫可爬取的总数量
     */
    @GetMapping("/test/ul-total-count")
    @Operation(summary = "获取UL爬虫可爬取的总数量", description = "获取当前UL网站可爬取的数据总数量")
    public ResponseEntity<Map<String, Object>> getULTotalCount() {
        
        try {
            int totalCount = UniDeviceCrawler.getULTotalCount();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取UL爬虫可爬取总数量成功",
                    "totalAvailableCount", totalCount,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取UL爬虫总数量失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试UL爬虫最新数据爬取
     */
    @PostMapping("/test/ul-latest")
    @Operation(summary = "测试UL爬虫最新数据爬取", description = "测试UL爬虫的最新数据爬取功能")
    public ResponseEntity<Map<String, Object>> testULCrawlerLatest(
            @Parameter(description = "要爬取的总数量", example = "10") @RequestParam(defaultValue = "10") int totalCount) {
        
        try {
            UniDeviceCrawler.testULCrawlerLatest(totalCount);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "UL爬虫最新数据测试完成",
                    "targetCount", totalCount,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "UL爬虫最新数据测试失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 测试爬虫可用性
     */
    @GetMapping("/test/availability")
    @Operation(summary = "测试爬虫可用性", description = "测试各种爬虫的可用性")
    public ResponseEntity<Map<String, Object>> testCrawlerAvailability() {
        
        try {
            UniDeviceCrawler.testCrawlerAvailability();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "爬虫可用性测试完成",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "爬虫可用性测试失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 执行完整爬虫测试
     */
    @PostMapping("/test/full")
    @Operation(summary = "执行完整爬虫测试", description = "执行完整的爬虫测试流程")
    public ResponseEntity<Map<String, Object>> runFullTest(
            @Parameter(description = "要爬取的总数量", example = "50") @RequestParam(defaultValue = "50") int totalCount) {
        
        try {
            UniDeviceCrawler.runFullTest(totalCount);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "完整爬虫测试完成",
                    "targetCount", totalCount,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "完整爬虫测试失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查统一爬虫测试服务是否正常运行")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "统一爬虫测试服务正常运行",
                "timestamp", System.currentTimeMillis(),
                "endpoints", new String[]{
                    "POST /api/unicrawler/test/ul-batch-save",
                    "POST /api/unicrawler/test/ul-latest",
                    "GET /api/unicrawler/test/availability",
                    "POST /api/unicrawler/test/full",
                    "GET /api/unicrawler/health",
                    "POST /api/unicrawler/crawl/all",
                    "POST /api/unicrawler/crawl/keyword",
                    "GET /api/unicrawler/status"
                }
        ));
    }
    
    /**
     * 统一爬取所有6个爬虫
     */
    @PostMapping("/crawl/all")
    @Operation(summary = "统一爬取所有爬虫", description = "按照关键词文件统一爬取6个爬虫的数据")
    public ResponseEntity<Map<String, Object>> crawlAllCrawlers(
            @Parameter(description = "每个爬虫的最大记录数", example = "50") @RequestParam(defaultValue = "50") int maxRecordsPerCrawler,
            @Parameter(description = "批处理大小", example = "10") @RequestParam(defaultValue = "10") int batchSize) {
        
        try {
            Map<String, Object> results = uniCrawler.crawlAllCrawlers(maxRecordsPerCrawler, batchSize);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "统一爬取所有爬虫完成",
                    "results", results,
                    "maxRecordsPerCrawler", maxRecordsPerCrawler,
                    "batchSize", batchSize,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "统一爬取失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 按关键词爬取所有爬虫
     */
    @PostMapping("/crawl/keyword")
    @Operation(summary = "按关键词爬取所有爬虫", description = "使用指定关键词爬取所有6个爬虫的数据")
    public ResponseEntity<Map<String, Object>> crawlByKeyword(
            @Parameter(description = "搜索关键词", example = "medical device") @RequestParam String keyword,
            @Parameter(description = "每个爬虫的最大记录数", example = "20") @RequestParam(defaultValue = "20") int maxRecordsPerCrawler,
            @Parameter(description = "批处理大小", example = "10") @RequestParam(defaultValue = "10") int batchSize) {
        
        try {
            Map<String, Object> results = uniCrawler.crawlByKeyword(keyword, maxRecordsPerCrawler, batchSize);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "按关键词爬取完成",
                    "keyword", keyword,
                    "results", results,
                    "maxRecordsPerCrawler", maxRecordsPerCrawler,
                    "batchSize", batchSize,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "按关键词爬取失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取统一爬虫状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取统一爬虫状态", description = "获取所有6个爬虫的状态信息")
    public ResponseEntity<Map<String, Object>> getUniCrawlerStatus() {
        
        try {
            Map<String, Object> status = uniCrawler.getCrawlerStatus();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "获取统一爬虫状态成功",
                    "status", status,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取统一爬虫状态失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 加载搜索关键词
     */
    @GetMapping("/keywords")
    @Operation(summary = "加载搜索关键词", description = "从searchkeywords.txt文件加载搜索关键词")
    public ResponseEntity<Map<String, Object>> loadSearchKeywords() {
        
        try {
            java.util.List<String> keywords = uniCrawler.loadSearchKeywords();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "加载搜索关键词成功",
                    "keywords", keywords,
                    "count", keywords.size(),
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "加载搜索关键词失败: " + e.getMessage(),
                    "error", e.getMessage()
            ));
        }
    }
}
