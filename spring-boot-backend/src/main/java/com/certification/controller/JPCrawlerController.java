package com.certification.controller;

import com.certification.crawler.countrydata.jp.JpRecall;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日本爬虫控制器
 */
@Slf4j
@RestController
@RequestMapping("/jp-crawler")
@Tag(name = "日本爬虫管理", description = "日本医疗器械数据爬虫接口")
public class JPCrawlerController {

    @Autowired
    private JpRecall jpRecallCrawler;

    /**
     * 测试日本召回爬虫
     */
    @PostMapping("/test/recall")
    @Operation(summary = "测试日本召回爬虫")
    public ResponseEntity<Map<String, Object>> testRecall(@RequestBody(required = false) Map<String, Object> params) {
        log.info("测试日本召回爬虫，参数: {}", params);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> sellers = extractList(params, "sellers");
            List<String> manufacturers = extractList(params, "manufacturers");
            List<String> years = extractList(params, "years");
            
            String crawlResult = jpRecallCrawler.crawlWithMultipleFields(
                sellers, manufacturers, years, 20, 20, null, null
            );
            
            result.put("success", true);
            result.put("message", "测试执行成功");
            result.put("data", crawlResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("测试日本召回爬虫失败", e);
            result.put("success", false);
            result.put("message", "测试执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 执行日本召回爬虫
     */
    @PostMapping("/execute/recall")
    @Operation(summary = "执行日本召回爬虫")
    public ResponseEntity<Map<String, Object>> executeRecall(@RequestBody(required = false) Map<String, Object> params) {
        log.info("执行日本召回爬虫，参数: {}", params);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> sellers = extractList(params, "sellers");
            List<String> manufacturers = extractList(params, "manufacturers");
            List<String> years = extractList(params, "years");
            
            Integer maxRecords = params != null && params.containsKey("maxRecords") 
                ? (Integer) params.get("maxRecords") : -1;
            Integer batchSize = params != null && params.containsKey("batchSize") 
                ? (Integer) params.get("batchSize") : 50;
            String dateFrom = params != null && params.containsKey("dateFrom") 
                ? (String) params.get("dateFrom") : null;
            String dateTo = params != null && params.containsKey("dateTo") 
                ? (String) params.get("dateTo") : null;
            
            String crawlResult = jpRecallCrawler.crawlWithMultipleFields(
                sellers, manufacturers, years, maxRecords, batchSize, dateFrom, dateTo
            );
            
            result.put("success", true);
            result.put("message", "执行成功");
            result.put("data", crawlResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("执行日本召回爬虫失败", e);
            result.put("success", false);
            result.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 从参数Map中提取List
     */
    @SuppressWarnings("unchecked")
    private List<String> extractList(Map<String, Object> params, String key) {
        if (params == null || !params.containsKey(key)) {
            return null;
        }
        
        Object value = params.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.trim().isEmpty()) {
                return null;
            }
            return List.of(str.split(",\\s*"));
        }
        
        return null;
    }
}

