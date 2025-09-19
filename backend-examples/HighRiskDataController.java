package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 高风险数据管理控制器
 * 支持关键词筛选和国家筛选功能
 */
@RestController
@RequestMapping("/api/high-risk-data")
@CrossOrigin(origins = "*")
public class HighRiskDataController {

    @Autowired
    private HighRiskDataService highRiskDataService;

    /**
     * 获取高风险数据统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getHighRiskStatistics() {
        Map<String, Object> statistics = highRiskDataService.getHighRiskStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 按国家获取高风险数据统计
     */
    @GetMapping("/statistics/by-country")
    public ResponseEntity<Map<String, Object>> getHighRiskStatisticsByCountry() {
        Map<String, Object> countryStatistics = highRiskDataService.getHighRiskStatisticsByCountry();
        return ResponseEntity.ok(Map.of("countryStatistics", countryStatistics));
    }

    /**
     * 获取关键词统计
     */
    @GetMapping("/keywords/statistics")
    public ResponseEntity<Map<String, Object>> getKeywordStatistics() {
        List<Map<String, Object>> keywords = highRiskDataService.getKeywordStatistics();
        return ResponseEntity.ok(Map.of("keywords", keywords));
    }

    /**
     * 按类型获取高风险数据（支持关键词和国家筛选）
     */
    @GetMapping("/{dataType}")
    public ResponseEntity<Map<String, Object>> getHighRiskDataByType(
            @PathVariable String dataType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String keyword
    ) {
        // 创建分页和排序对象
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // 根据数据类型调用相应的服务方法
        Page<?> dataPage = highRiskDataService.getDataByType(dataType, pageable, country, keyword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", dataPage.getContent());
        response.put("totalElements", dataPage.getTotalElements());
        response.put("totalPages", dataPage.getTotalPages());
        response.put("size", dataPage.getSize());
        response.put("number", dataPage.getNumber());
        response.put("first", dataPage.isFirst());
        response.put("last", dataPage.isLast());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新风险等级
     */
    @PutMapping("/{dataType}/{id}/risk-level")
    public ResponseEntity<Map<String, Object>> updateRiskLevel(
            @PathVariable String dataType,
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String riskLevel = request.get("riskLevel");
        boolean success = highRiskDataService.updateRiskLevel(dataType, id, riskLevel);
        
        return ResponseEntity.ok(Map.of("success", success));
    }

    /**
     * 批量更新风险等级
     */
    @PutMapping("/batch/risk-level")
    public ResponseEntity<Map<String, Object>> batchUpdateRiskLevel(
            @RequestBody Map<String, Object> request
    ) {
        List<Integer> ids = (List<Integer>) request.get("ids");
        String riskLevel = (String) request.get("riskLevel");
        
        boolean success = highRiskDataService.batchUpdateRiskLevel(ids, riskLevel);
        
        return ResponseEntity.ok(Map.of("success", success));
    }

    /**
     * 更新关键词
     */
    @PutMapping("/{dataType}/{id}/keywords")
    public ResponseEntity<Map<String, Object>> updateKeywords(
            @PathVariable String dataType,
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String oldKeyword = request.get("oldKeyword");
        String newKeyword = request.get("newKeyword");
        
        boolean success = highRiskDataService.updateKeywords(dataType, id, oldKeyword, newKeyword);
        
        return ResponseEntity.ok(Map.of("success", success));
    }
}
