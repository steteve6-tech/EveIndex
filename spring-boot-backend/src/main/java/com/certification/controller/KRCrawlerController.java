package com.certification.controller;

import com.certification.crawler.countrydata.kr.KrRecall;
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
 */
@Slf4j
@RestController
@RequestMapping("/api/kr-crawler")
@CrossOrigin(originPatterns = "*")
@Tag(name = "韩国医疗设备爬虫", description = "韩国食品药品安全处(MFDS)医疗设备数据爬取管理")
public class KRCrawlerController {

    @Autowired
    private KrRecall krRecallCrawler;

    /**
     * 测试韩国召回爬虫
     */
    @PostMapping("/test/recall")
    @Operation(summary = "测试韩国召回爬虫", description = "测试爬取韩国MFDS召回数据")
    public ResponseEntity<Map<String, Object>> testKRRecall(
            @Parameter(description = "搜索关键词（产品名称）") @RequestParam(required = false, defaultValue = "") String searchTerm,
            @Parameter(description = "最大记录数，-1表示爬取所有数据") @RequestParam(defaultValue = "10") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "20") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateTo) {

        log.info("🧪 收到韩国召回爬虫测试请求");
        log.info("📊 参数: searchTerm={}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                searchTerm, maxRecords, batchSize, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();

        try {
            String crawlResult = krRecallCrawler.crawlAndSaveToDatabase(
                    searchTerm, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", "韩国召回爬虫测试完成");
            result.put("crawlerType", "KR_Recall");
            result.put("result", crawlResult);
            result.put("parameters", Map.of(
                    "searchTerm", searchTerm,
                    "maxRecords", maxRecords,
                    "batchSize", batchSize,
                    "dateFrom", dateFrom,
                    "dateTo", dateTo
            ));

            log.info("✅ 韩国召回爬虫测试成功: {}", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国召回爬虫测试失败", e);
            result.put("success", false);
            result.put("message", "韩国召回爬虫测试失败: " + e.getMessage());
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 执行韩国召回爬虫
     */
    @PostMapping("/execute/recall")
    @Operation(summary = "执行韩国召回爬虫", description = "执行韩国MFDS召回数据爬取并保存到数据库")
    public ResponseEntity<Map<String, Object>> executeKRRecall(
            @Parameter(description = "搜索关键词（产品名称）") @RequestParam(required = false, defaultValue = "") String searchTerm,
            @Parameter(description = "最大记录数，-1表示爬取所有数据") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateTo) {

        log.info("🚀 收到韩国召回爬虫执行请求");
        log.info("📊 参数: searchTerm={}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                searchTerm, maxRecords, batchSize, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();

        try {
            String crawlResult = krRecallCrawler.crawlAndSaveToDatabase(
                    searchTerm, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", "韩国召回爬虫执行完成");
            result.put("crawlerType", "KR_Recall");
            result.put("result", crawlResult);
            result.put("parameters", Map.of(
                    "searchTerm", searchTerm,
                    "maxRecords", maxRecords,
                    "batchSize", batchSize,
                    "dateFrom", dateFrom,
                    "dateTo", dateTo
            ));

            log.info("✅ 韩国召回爬虫执行成功: {}", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国召回爬虫执行失败", e);
            result.put("success", false);
            result.put("message", "韩国召回爬虫执行失败: " + e.getMessage());
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 基于关键词列表批量爬取
     */
    @PostMapping("/execute/recall/keywords")
    @Operation(summary = "批量爬取韩国召回数据（关键词列表）", 
               description = "使用关键词列表批量爬取韩国MFDS召回数据")
    public ResponseEntity<Map<String, Object>> executeKRRecallWithKeywords(
            @Parameter(description = "关键词列表（逗号分隔）") @RequestParam String inputKeywords,
            @Parameter(description = "最大记录数，-1表示爬取所有数据") @RequestParam(defaultValue = "-1") int maxRecords,
            @Parameter(description = "批次大小") @RequestParam(defaultValue = "50") int batchSize,
            @Parameter(description = "开始日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateFrom,
            @Parameter(description = "结束日期(YYYY-MM-DD)") @RequestParam(required = false, defaultValue = "") String dateTo) {

        log.info("🚀 收到韩国召回爬虫批量执行请求（关键词列表）");
        log.info("📊 关键词: {}, maxRecords={}, batchSize={}, dateFrom={}, dateTo={}", 
                inputKeywords, maxRecords, batchSize, dateFrom, dateTo);

        Map<String, Object> result = new HashMap<>();

        try {
            // 解析关键词列表
            List<String> keywordList = new ArrayList<>();
            if (inputKeywords != null && !inputKeywords.trim().isEmpty()) {
                String[] keywords = inputKeywords.split("[,，;；\\s]+");
                for (String keyword : keywords) {
                    String trimmed = keyword.trim();
                    if (!trimmed.isEmpty()) {
                        keywordList.add(trimmed);
                    }
                }
            }

            if (keywordList.isEmpty()) {
                result.put("success", false);
                result.put("message", "关键词列表为空");
                return ResponseEntity.badRequest().body(result);
            }

            log.info("📝 解析到 {} 个关键词", keywordList.size());

            String crawlResult = krRecallCrawler.crawlAndSaveWithKeywords(
                    keywordList, maxRecords, batchSize, dateFrom, dateTo);

            result.put("success", true);
            result.put("message", "韩国召回爬虫批量执行完成");
            result.put("crawlerType", "KR_Recall");
            result.put("result", crawlResult);
            result.put("keywordCount", keywordList.size());
            result.put("keywords", keywordList);
            result.put("parameters", Map.of(
                    "maxRecords", maxRecords,
                    "batchSize", batchSize,
                    "dateFrom", dateFrom,
                    "dateTo", dateTo
            ));

            log.info("✅ 韩国召回爬虫批量执行成功: {}", crawlResult);

        } catch (Exception e) {
            log.error("❌ 韩国召回爬虫批量执行失败", e);
            result.put("success", false);
            result.put("message", "韩国召回爬虫批量执行失败: " + e.getMessage());
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }

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
                            "countryName", "韩国"
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
            result.put("availableCrawlers", List.of("recall"));
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
            result.put("message", "韩国爬虫服务运行正常");

        } catch (Exception e) {
            log.error("健康检查失败", e);
            result.put("status", "unhealthy");
            result.put("message", "服务异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }

        return ResponseEntity.ok(result);
    }
}

