package com.certification.controller;

import com.certification.entity.common.CertNewsData;
import com.certification.standards.CrawlerDataService;
import com.certification.analysis.CertNewsanalysis;
import com.certification.repository.CrawlerDataRepository;
import com.certification.dto.CrawlerDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 爬虫数据控制器
 * 提供爬虫数据的查询和管理接口
 */
@Slf4j
@Tag(name = "认证新闻数据管理", description = "认证新闻数据的查询、统计和管理接口")
@RestController
@RequestMapping("/crawler-data")
public class CertNewsDataController {
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private CertNewsanalysis certNewsanalysis;
    
    @Autowired
    private CrawlerDataRepository crawlerDataRepository;
    
    /**
     * 获取爬虫数据列表（支持搜索、分页、排序）
     * 前端页面主要使用的数据查询接口
     */
    @Operation(summary = "获取爬虫数据列表", description = "支持关键词搜索、国家筛选、相关性筛选、日期范围筛选、分页和排序")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = CrawlerDataResponse.class))),
        @ApiResponse(responseCode = "400", description = "查询参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCrawlerData(
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词搜索（标题、内容、摘要）") @RequestParam(required = false) String keyword,
            @Parameter(description = "国家筛选", example = "CN") @RequestParam(required = false) String country,
            @Parameter(description = "相关性筛选", example = "true") @RequestParam(required = false) Boolean related,
            @Parameter(description = "数据源筛选", example = "UL Solutions") @RequestParam(required = false) String sourceName,
            @Parameter(description = "类型筛选", example = "法规标准") @RequestParam(required = false) String type,
            @Parameter(description = "开始日期 (yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 (yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "排序字段", example = "publishDate") @RequestParam(defaultValue = "publishDate") String sortBy,
            @Parameter(description = "排序方向", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "风险等级筛选", example = "HIGH") @RequestParam(required = false) String riskLevel,
            @Parameter(description = "匹配关键词筛选", example = "FCC认证") @RequestParam(required = false) String matchedKeyword) {
        
        log.info("收到前端请求: page={}, size={}, keyword={}, country={}, related={}, sourceName={}, type={}, startDate={}, endDate={}, sortBy={}, sortDirection={}, riskLevel={}, matchedKeyword={}", 
                page, size, keyword, country, related, sourceName, type, startDate, endDate, sortBy, sortDirection, riskLevel, matchedKeyword);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 处理空字符串参数，转换为null
            String processedKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
            String processedCountry = (country != null && country.trim().isEmpty()) ? null : country;
            String processedSourceName = (sourceName != null && sourceName.trim().isEmpty()) ? null : sourceName;
            String processedType = (type != null && type.trim().isEmpty()) ? null : type;
            String processedStartDate = (startDate != null && startDate.trim().isEmpty()) ? null : startDate;
            String processedEndDate = (endDate != null && endDate.trim().isEmpty()) ? null : endDate;
            String processedMatchedKeyword = (matchedKeyword != null && matchedKeyword.trim().isEmpty()) ? null : matchedKeyword;
            
            log.info("处理后的参数: keyword={}, country={}, related={}, sourceName={}, type={}, startDate={}, endDate={}, riskLevel={}, matchedKeyword={}", 
                    processedKeyword, processedCountry, related, processedSourceName, processedType, processedStartDate, processedEndDate, riskLevel, processedMatchedKeyword);
            
            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 执行查询
            Page<CertNewsData> dataPage = crawlerDataService.searchCrawlerData(
                processedKeyword, processedCountry, related, processedSourceName, processedType, processedStartDate, processedEndDate, riskLevel, processedMatchedKeyword, pageable
            );
            
            // 构建响应数据
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("content", dataPage.getContent());
            dataMap.put("totalElements", dataPage.getTotalElements());
            dataMap.put("totalPages", dataPage.getTotalPages());
            dataMap.put("currentPage", dataPage.getNumber());
            dataMap.put("pageSize", dataPage.getSize());
            dataMap.put("hasNext", dataPage.hasNext());
            dataMap.put("hasPrevious", dataPage.hasPrevious());
            
            result.put("success", true);
            result.put("data", dataMap);
            result.put("message", "查询成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("查询成功: 总数={}, 当前页={}, 每页大小={}, 返回数据条数={}", 
                    dataPage.getTotalElements(), dataPage.getNumber(), dataPage.getSize(), 
                    dataPage.getContent().size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询爬虫数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "查询失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取Dashboard统计数据
     * 专门为Dashboard页面提供风险等级统计，避免传输大量数据
     */
    @Operation(summary = "获取Dashboard统计数据", description = "获取各风险等级的数据统计，专门为Dashboard页面优化")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用Service层获取统计数据
            Map<String, Object> statistics = crawlerDataService.getDashboardStatistics();
            
            result.put("success", true);
            result.put("data", statistics);
            result.put("message", "统计数据获取成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("Dashboard统计数据获取成功: {}", statistics);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取Dashboard统计数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取统计数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取国家风险统计数据
     * 专门为Dashboard国家风险分布提供数据
     */
    @Operation(summary = "获取国家风险统计数据", description = "获取各国风险等级分布统计，专门为Dashboard国家风险分析优化")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/dashboard/country-risk-stats")
    public ResponseEntity<Map<String, Object>> getCountryRiskStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用Service层获取国家风险统计
            List<Map<String, Object>> countryStats = crawlerDataService.getCountryRiskStatistics();
            
            result.put("success", true);
            result.put("data", countryStats);
            result.put("message", "国家风险统计数据获取成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("国家风险统计数据获取成功: {}个国家", countryStats.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取国家风险统计数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家风险统计数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取最新高风险数据
     * 专门为Dashboard最新风险信息提供数据
     */
    @Operation(summary = "获取最新高风险数据", description = "获取最新的高风险数据列表，专门为Dashboard最新风险信息优化")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/dashboard/latest-high-risk")
    public ResponseEntity<Map<String, Object>> getLatestHighRiskData(
            @Parameter(description = "数据条数", example = "3") @RequestParam(defaultValue = "3") int limit) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用Service层获取最新高风险数据
            List<CertNewsData> latestHighRiskData = crawlerDataService.getLatestHighRiskData(limit);
            
            result.put("success", true);
            result.put("data", latestHighRiskData);
            result.put("message", "最新高风险数据获取成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("最新高风险数据获取成功: {}条", latestHighRiskData.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取最新高风险数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取最新高风险数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 更新爬虫数据的相关性
     */
    @Operation(summary = "更新爬虫数据相关性", description = "更新指定爬虫数据的相关性状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败"),
        @ApiResponse(responseCode = "404", description = "数据不存在")
    })
    @PutMapping("/{id}/related")
    public ResponseEntity<Map<String, Object>> updateCrawlerData(
            @Parameter(description = "数据ID") @PathVariable String id,
            @Parameter(description = "相关性状态") @RequestParam Boolean related) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找数据
            Optional<CertNewsData> optionalData = crawlerDataRepository.findById(id);
            if (optionalData.isEmpty()) {
                result.put("success", false);
                result.put("error", "数据不存在");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(result);
            }
            
            CertNewsData certNewsData = optionalData.get();
            
            // 更新相关性状态
            certNewsData.setRelated(related);
            crawlerDataRepository.save(certNewsData);
            
            result.put("success", true);
            result.put("message", "相关性更新成功");
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新爬虫数据相关性失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 更新爬虫数据的风险等级
     */
    @Operation(summary = "更新爬虫数据风险等级", description = "更新指定爬虫数据的风险等级状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败"),
        @ApiResponse(responseCode = "404", description = "数据不存在")
    })
    @PutMapping("/{id}/risk-level")
    public ResponseEntity<Map<String, Object>> updateCrawlerDataRiskLevel(
            @Parameter(description = "数据ID") @PathVariable String id,
            @Parameter(description = "风险等级", example = "HIGH") @RequestParam String riskLevel) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找数据
            Optional<CertNewsData> optionalData = crawlerDataRepository.findById(id);
            if (optionalData.isEmpty()) {
                result.put("success", false);
                result.put("error", "数据不存在");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(result);
            }
            
            CertNewsData certNewsData = optionalData.get();
            
            // 验证风险等级参数
            CertNewsData.RiskLevel riskLevelEnum;
            try {
                if ("null".equals(riskLevel) || riskLevel == null) {
                    riskLevelEnum = null;
                } else {
                    riskLevelEnum = CertNewsData.RiskLevel.valueOf(riskLevel.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("error", "无效的风险等级: " + riskLevel);
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(400).body(result);
            }
            
            // 更新风险等级状态
            certNewsData.setRiskLevel(riskLevelEnum);
            crawlerDataRepository.save(certNewsData);
            
            result.put("success", true);
            result.put("message", "风险等级更新成功");
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新爬虫数据风险等级失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 批量更新爬虫数据相关性
     */
    @Operation(summary = "批量更新爬虫数据相关性", description = "批量更新指定爬虫数据的相关性状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量更新成功"),
        @ApiResponse(responseCode = "400", description = "批量更新失败"),
        @ApiResponse(responseCode = "404", description = "部分数据不存在")
    })
    @PutMapping("/batch-update-related")
    public ResponseEntity<Map<String, Object>> batchUpdateCrawlerDataRelated(
            @Parameter(description = "数据ID列表") @RequestParam List<String> ids,
            @Parameter(description = "相关性状态") @RequestParam Boolean related) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            int successCount = 0;
            int failCount = 0;
            List<String> failedIds = new ArrayList<>();
            
            for (String id : ids) {
                try {
                    // 查找数据
                    Optional<CertNewsData> optionalData = crawlerDataRepository.findById(id);
                    if (optionalData.isEmpty()) {
                        failCount++;
                        failedIds.add(id);
                        continue;
                    }
                    
                    CertNewsData certNewsData = optionalData.get();
                    
                    // 更新相关性状态
                    certNewsData.setRelated(related);
                    crawlerDataRepository.save(certNewsData);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("更新数据 {} 失败: {}", id, e.getMessage());
                    failCount++;
                    failedIds.add(id);
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("批量更新完成，成功: %d 条，失败: %d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failedIds", failedIds);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量更新爬虫数据相关性失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "批量更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 批量更新爬虫数据风险等级
     */
    @Operation(summary = "批量更新爬虫数据风险等级", description = "批量更新指定爬虫数据的风险等级状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量更新成功"),
        @ApiResponse(responseCode = "400", description = "批量更新失败"),
        @ApiResponse(responseCode = "404", description = "部分数据不存在")
    })
    @PutMapping("/batch-update-risk-level")
    public ResponseEntity<Map<String, Object>> batchUpdateCrawlerDataRiskLevel(
            @Parameter(description = "数据ID列表") @RequestParam List<String> ids,
            @Parameter(description = "风险等级", example = "MEDIUM") @RequestParam String riskLevel) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证风险等级参数
            CertNewsData.RiskLevel riskLevelEnum;
            try {
                if ("null".equals(riskLevel) || riskLevel == null) {
                    riskLevelEnum = null;
                } else {
                    riskLevelEnum = CertNewsData.RiskLevel.valueOf(riskLevel.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("error", "无效的风险等级: " + riskLevel);
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(400).body(result);
            }
            
            int successCount = 0;
            int failCount = 0;
            List<String> failedIds = new ArrayList<>();
            
            for (String id : ids) {
                try {
                    // 查找数据
                    Optional<CertNewsData> optionalData = crawlerDataRepository.findById(id);
                    if (optionalData.isEmpty()) {
                        failCount++;
                        failedIds.add(id);
                        continue;
                    }
                    
                    CertNewsData certNewsData = optionalData.get();
                    
                    // 更新风险等级状态
                    certNewsData.setRiskLevel(riskLevelEnum);
                    crawlerDataRepository.save(certNewsData);
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("更新数据 {} 的风险等级失败: {}", id, e.getMessage());
                    failCount++;
                    failedIds.add(id);
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("批量更新风险等级完成，成功: %d 条，失败: %d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("failedIds", failedIds);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量更新爬虫数据风险等级失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "批量更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 批量设置所有数据为中风险
     */
    @Operation(summary = "批量设置所有数据为中风险", description = "将数据库中所有数据的风险等级设置为中风险")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量设置成功"),
        @ApiResponse(responseCode = "500", description = "批量设置失败")
    })
    @PostMapping("/set-all-medium-risk")
    public ResponseEntity<Map<String, Object>> setAllDataToMediumRisk() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始批量设置所有数据为中风险");
            
            // 统计需要更新的数据量
            long totalCount = crawlerDataRepository.countByDeleted(0);
            
            if (totalCount == 0) {
                result.put("success", true);
                result.put("message", "没有数据需要更新");
                result.put("updatedCount", 0);
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(result);
            }
            
            // 使用批量更新方法，更高效
            int updatedCount = crawlerDataRepository.batchUpdateAllRiskLevel(
                CertNewsData.RiskLevel.MEDIUM,
                LocalDateTime.now()
            );
            
            result.put("success", true);
            result.put("message", String.format("成功将 %d 条数据设置为中风险", updatedCount));
            result.put("updatedCount", updatedCount);
            result.put("totalCount", totalCount);
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("批量设置中风险完成，共更新 {} 条数据", updatedCount);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量设置所有数据为中风险失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "批量设置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 更新爬虫数据
     */
    @Operation(summary = "更新爬虫数据", description = "更新爬虫数据的各个字段，包括风险等级")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败"),
        @ApiResponse(responseCode = "404", description = "数据不存在")
    })
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCrawlerData(
            @Parameter(description = "爬虫数据更新请求") @RequestBody CertNewsData certNewsData) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找数据
            Optional<CertNewsData> optionalData = crawlerDataRepository.findById(certNewsData.getId());
            if (optionalData.isEmpty()) {
                result.put("success", false);
                result.put("error", "数据不存在");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(result);
            }
            
            CertNewsData existingData = optionalData.get();
            
            // 更新字段（只更新非null的字段）
            if (certNewsData.getTitle() != null) {
                existingData.setTitle(certNewsData.getTitle());
            }
            if (certNewsData.getCountry() != null) {
                existingData.setCountry(certNewsData.getCountry());
            }
            if (certNewsData.getSourceName() != null) {
                existingData.setSourceName(certNewsData.getSourceName());
            }
            if (certNewsData.getType() != null) {
                existingData.setType(certNewsData.getType());
            }
            if (certNewsData.getSummary() != null) {
                existingData.setSummary(certNewsData.getSummary());
            }
            if (certNewsData.getContent() != null) {
                existingData.setContent(certNewsData.getContent());
            }
            if (certNewsData.getUrl() != null) {
                existingData.setUrl(certNewsData.getUrl());
            }
            if (certNewsData.getPublishDate() != null) {
                existingData.setPublishDate(certNewsData.getPublishDate());
            }
            if (certNewsData.getRelated() != null) {
                existingData.setRelated(certNewsData.getRelated());
            }
            if (certNewsData.getRiskLevel() != null) {
                existingData.setRiskLevel(certNewsData.getRiskLevel());
            }
            if (certNewsData.getRemarks() != null) {
                existingData.setRemarks(certNewsData.getRemarks());
            }
            
            // 保存更新
            crawlerDataRepository.save(existingData);
            
            result.put("success", true);
            result.put("message", "数据更新成功");
            result.put("data", existingData);
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新爬虫数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取风险等级统计信息
     */
    @Operation(summary = "获取风险等级统计信息", description = "获取爬虫数据的风险等级分布统计")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @GetMapping("/risk-level-statistics")
    public ResponseEntity<Map<String, Object>> getRiskLevelStatistics() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 统计各风险等级的数量
            long totalCount = crawlerDataRepository.countByDeleted(0);
            long highRiskCount = crawlerDataRepository.countByRiskLevelAndDeleted(CertNewsData.RiskLevel.HIGH, 0);
            long mediumRiskCount = crawlerDataRepository.countByRiskLevelAndDeleted(CertNewsData.RiskLevel.MEDIUM, 0);
            long lowRiskCount = crawlerDataRepository.countByRiskLevelAndDeleted(CertNewsData.RiskLevel.LOW, 0);
            long noneRiskCount = crawlerDataRepository.countByRiskLevelAndDeleted(CertNewsData.RiskLevel.NONE, 0);
            long undeterminedCount = crawlerDataRepository.countByRiskLevelIsNullAndDeleted(0);
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", totalCount);
            statistics.put("highRiskCount", highRiskCount);
            statistics.put("mediumRiskCount", mediumRiskCount);
            statistics.put("lowRiskCount", lowRiskCount);
            statistics.put("noneRiskCount", noneRiskCount);
            statistics.put("undeterminedCount", undeterminedCount);
            
            result.put("success", true);
            result.put("data", statistics);
            result.put("message", "获取风险等级统计成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("获取风险等级统计成功: 总数={}, 高风险={}, 中风险={}, 低风险={}, 无风险={}, 未确定={}", 
                    totalCount, highRiskCount, mediumRiskCount, lowRiskCount, noneRiskCount, undeterminedCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取风险等级统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取统计失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    
    /**
     * 发送邮件
     */
    @Operation(summary = "发送邮件", description = "发送包含新闻内容的邮件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "发送成功"),
        @ApiResponse(responseCode = "400", description = "发送失败")
    })
    @PostMapping("/send-email")
    public ResponseEntity<Map<String, Object>> sendEmail(
            @Parameter(description = "邮件请求参数") @RequestBody Map<String, Object> emailRequest) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            @SuppressWarnings("unused")
            String recipients = (String) emailRequest.get("recipients");
            @SuppressWarnings("unused")
            String subject = (String) emailRequest.get("subject");
            @SuppressWarnings("unused")
            String content = (String) emailRequest.get("content");
            @SuppressWarnings("unused")
            Boolean includeAttachment = (Boolean) emailRequest.get("includeAttachment");
            @SuppressWarnings("unused")
            String newsId = (String) emailRequest.get("newsId");
            
            // TODO: 实现邮件发送功能
            boolean sent = false; // 暂时返回false，表示未实现
            
            if (sent) {
                result.put("success", true);
                result.put("message", "邮件发送成功");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "邮件发送失败");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(400).body(result);
            }
            
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "发送邮件失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 删除爬虫数据
     */
    @Operation(summary = "删除爬虫数据", description = "删除指定的爬虫数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "数据不存在"),
        @ApiResponse(responseCode = "500", description = "删除失败")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCrawlerData(
            @Parameter(description = "数据ID") @PathVariable String id) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找数据
            Optional<CertNewsData> optionalData = crawlerDataRepository.findById(id);
            if (optionalData.isEmpty()) {
                result.put("success", false);
                result.put("error", "数据不存在");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.status(404).body(result);
            }
            
            // 执行软删除
            crawlerDataRepository.softDelete(Long.valueOf(id), LocalDateTime.now());
            
            result.put("success", true);
            result.put("message", "删除成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除爬虫数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "删除失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 测试查询所有数据
     */
    @Operation(summary = "测试查询所有数据", description = "测试当所有参数都为空时是否能正确返回所有数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @GetMapping("/test-all")
    public ResponseEntity<Map<String, Object>> testGetAllData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("测试查询所有爬虫数据");
            
            // 构建分页和排序
            Sort sort = Sort.by(Sort.Direction.DESC, "crawlTime");
            Pageable pageable = PageRequest.of(0, 10, sort);
            
            // 执行查询，所有参数都为null
            Page<CertNewsData> dataPage = crawlerDataService.searchCrawlerData(
                null, null, null, null, null, null, null, null, null, pageable
            );
            
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("content", dataPage.getContent());
            dataMap.put("totalElements", dataPage.getTotalElements());
            dataMap.put("totalPages", dataPage.getTotalPages());
            dataMap.put("currentPage", dataPage.getNumber());
            dataMap.put("pageSize", dataPage.getSize());
            dataMap.put("hasNext", dataPage.hasNext());
            dataMap.put("hasPrevious", dataPage.hasPrevious());
            
            result.put("success", true);
            result.put("data", dataMap);
            result.put("message", "测试查询成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("测试查询成功: 总数={}, 返回数据条数={}", 
                    dataPage.getTotalElements(), dataPage.getContent().size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("测试查询失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "测试查询失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取统计数据
     */
    @Operation(summary = "获取统计数据", description = "获取爬虫数据的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> stats = crawlerDataService.getStatistics();
            
            result.put("success", true);
            result.put("data", stats);
            result.put("message", "统计数据获取成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取统计数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取相关状态统计信息
     */
    @Operation(summary = "获取相关状态统计", description = "获取爬虫数据相关状态的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @GetMapping("/related-statistics")
    public ResponseEntity<Map<String, Object>> getRelatedStatistics() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> stats = crawlerDataService.getRelatedStatistics();
            
            result.put("success", true);
            result.put("data", stats);
            result.put("message", "相关状态统计获取成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取相关状态统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取相关状态统计失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 根据产品名称查询数据
     */
    @Operation(summary = "根据产品名称查询数据", description = "根据产品名称查询爬虫数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "查询失败")
    })
    @GetMapping("/by-product")
    public ResponseEntity<Map<String, Object>> findByProduct(
            @Parameter(description = "产品名称", example = "洗衣機和脫水機") @RequestParam String product,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CertNewsData> dataPage = crawlerDataService.findByProduct(product, pageable);
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            result.put("product", product);
            result.put("message", "查询成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "查询失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据产品名称模糊查询
     */
    @Operation(summary = "根据产品名称模糊查询", description = "根据产品名称进行模糊查询")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "查询失败")
    })
    @GetMapping("/by-product-contains")
    public ResponseEntity<Map<String, Object>> findByProductContaining(
            @Parameter(description = "产品名称关键词", example = "洗衣機") @RequestParam String product,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CertNewsData> dataPage = crawlerDataService.findByProductContaining(product, pageable);
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            result.put("product", product);
            result.put("message", "模糊查询成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "模糊查询失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 根据数据源和产品名称查询
     */
    @Operation(summary = "根据数据源和产品名称查询", description = "根据数据源和产品名称查询数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "查询失败")
    })
    @GetMapping("/by-source-and-product")
    public ResponseEntity<Map<String, Object>> findBySourceNameAndProduct(
            @Parameter(description = "数据源名称", example = "UL Solutions") @RequestParam String sourceName,
            @Parameter(description = "产品名称", example = "洗衣機和脫水機") @RequestParam String product,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CertNewsData> dataPage = crawlerDataService.findBySourceNameAndProduct(sourceName, product, pageable);
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            result.put("sourceName", sourceName);
            result.put("product", product);
            result.put("message", "查询成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "查询失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据产品名称和关键词查询
     */
    @Operation(summary = "根据产品名称和关键词查询", description = "根据产品名称和关键词进行复合查询")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "查询失败")
    })
    @GetMapping("/by-product-and-keyword")
    public ResponseEntity<Map<String, Object>> findByProductAndKeyword(
            @Parameter(description = "产品名称", example = "洗衣機") @RequestParam String product,
            @Parameter(description = "关键词", example = "IEC") @RequestParam String keyword,
            @Parameter(description = "页码", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CertNewsData> dataPage = crawlerDataService.findByProductAndKeyword(product, keyword, pageable);
            
            result.put("success", true);
            result.put("data", dataPage.getContent());
            result.put("totalElements", dataPage.getTotalElements());
            result.put("totalPages", dataPage.getTotalPages());
            result.put("currentPage", dataPage.getNumber());
            result.put("pageSize", dataPage.getSize());
            result.put("product", product);
            result.put("keyword", keyword);
            result.put("message", "复合查询成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "查询失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 统计各产品的数据量
     */
    @Operation(summary = "统计各产品的数据量", description = "统计各产品的数据量分布")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "统计成功"),
        @ApiResponse(responseCode = "400", description = "统计失败")
    })
    @GetMapping("/product-statistics")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> productCounts = crawlerDataService.countByProduct();
            
            result.put("success", true);
            result.put("productCounts", productCounts);
            result.put("totalProducts", productCounts.size());
            result.put("message", "产品统计成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "统计失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 获取热门产品列表
     */
    @Operation(summary = "获取热门产品列表", description = "获取按数据量排序的热门产品列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/popular-products")
    public ResponseEntity<Map<String, Object>> getPopularProducts(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> popularProducts = crawlerDataService.getPopularProducts(limit);
            
            result.put("success", true);
            result.put("popularProducts", popularProducts);
            result.put("limit", limit);
            result.put("actualCount", popularProducts.size());
            result.put("message", "获取热门产品成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 获取指定产品的详细统计信息
     */
    @Operation(summary = "获取产品详细统计", description = "获取指定产品的详细统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/product-detail-statistics")
    public ResponseEntity<Map<String, Object>> getProductDetailStatistics(
            @Parameter(description = "产品名称", example = "洗衣機和脫水機") @RequestParam String product) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> statistics = crawlerDataService.getProductStatistics(product);
            
            result.put("success", true);
            result.put("product", product);
            result.put("statistics", statistics);
            result.put("message", "获取产品统计成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 获取所有数据源名称
     */
    @Operation(summary = "获取所有数据源名称", description = "获取数据库中所有可用的数据源名称")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/source-names")
    public ResponseEntity<Map<String, Object>> getAllSourceNames() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> sourceNames = crawlerDataService.getAllSourceNames();
            
            result.put("success", true);
            result.put("sourceNames", sourceNames);
            result.put("totalCount", sourceNames.size());
            result.put("message", "获取数据源名称成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 测试数据源分布
     */
    @Operation(summary = "测试数据源分布", description = "查看数据库中各个数据源的数据分布情况")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/source-distribution")
    public ResponseEntity<Map<String, Object>> getSourceDistribution() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有数据源统计
            List<Map<String, Object>> sourceStats = crawlerDataService.countBySourceName();
            
            // 获取总数据量
            long totalCount = crawlerDataRepository.count();
            
            result.put("success", true);
            result.put("sourceStats", sourceStats);
            result.put("totalCount", totalCount);
            result.put("message", "获取数据源分布成功");
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "获取失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 自动处理相关状态
     */
    @Operation(summary = "自动处理相关状态", description = "根据关键词自动设置数据的相关状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "处理成功"),
        @ApiResponse(responseCode = "400", description = "处理失败")
    })
    @PostMapping("/auto-process-related")
    public ResponseEntity<Map<String, Object>> autoProcessRelated(
            @Parameter(description = "关键词列表") @RequestBody(required = false) Map<String, Object> requestBody) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> keywords = null;
            if (requestBody != null && requestBody.containsKey("keywords")) {
                @SuppressWarnings("unchecked")
                List<String> keywordsList = (List<String>) requestBody.get("keywords");
                keywords = keywordsList;
                log.info("接收到关键词列表: {}", keywords);
            }
            
            Map<String, Object> processResult = certNewsanalysis.autoProcessRelatedStatus(keywords);
            
            result.putAll(processResult);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 根据数据源自动处理相关状态
     */
    @Operation(summary = "根据数据源自动处理相关状态", description = "根据关键词自动设置指定数据源的相关状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "处理成功"),
        @ApiResponse(responseCode = "400", description = "处理失败")
    })
    @PostMapping("/auto-process-related-by-source")
    public ResponseEntity<Map<String, Object>> autoProcessRelatedBySource(
            @Parameter(description = "数据源名称") @RequestParam String sourceName) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> processResult = certNewsanalysis.autoProcessRelatedStatusBySource(sourceName);
            
            result.putAll(processResult);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "自动处理失败: " + e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 批量将所有数据设置为未确定
     */
    @Operation(summary = "批量将所有数据设置为未确定", description = "将所有爬虫数据的相关性状态设置为未确定")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "批量更新成功"),
        @ApiResponse(responseCode = "400", description = "批量更新失败")
    })
    @PostMapping("/batch-set-undetermined")
    public ResponseEntity<Map<String, Object>> batchSetAllUndetermined() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始批量将所有数据设置为未确定");
            
            // 获取所有数据的总数
            long totalCount = crawlerDataRepository.count();
            
            // 批量更新所有数据的related字段为null（未确定）
            int updatedCount = crawlerDataRepository.batchUpdateRelatedToNull(LocalDateTime.now());
            
            result.put("success", true);
            result.put("message", "批量更新成功");
            result.put("totalCount", totalCount);
            result.put("updatedCount", updatedCount);
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("批量更新完成: 总数={}, 更新数={}", totalCount, updatedCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("批量更新失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "批量更新失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 根据标题和内容自动更新国家字段
     */
    @Operation(summary = "根据标题和内容自动更新国家字段", description = "分析数据的标题和内容，自动识别并更新country字段")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败")
    })
    @PostMapping("/auto-update-country")
    public ResponseEntity<Map<String, Object>> autoUpdateCountry() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始根据标题和内容自动更新国家字段");
            
            Map<String, Object> updateResult = crawlerDataService.autoUpdateCountryFromContent();
            
            result.putAll(updateResult);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("自动更新国家字段失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "自动更新国家字段失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取当前数据的国家分布统计
     */
    @Operation(summary = "获取国家分布统计", description = "获取当前数据库中所有数据的国家分布情况")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/country-distribution")
    public ResponseEntity<Map<String, Object>> getCountryDistribution() {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始获取国家分布统计");
            
            Map<String, Object> distributionResult = crawlerDataService.getCountryDistribution();
            
            result.putAll(distributionResult);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取国家分布统计失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取国家分布统计失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 保存爬取结果到数据库
     */
    @Operation(summary = "保存爬取结果", description = "将爬虫爬取的结果保存到数据库中")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "保存成功"),
        @ApiResponse(responseCode = "400", description = "保存失败")
    })
    @PostMapping("/save-results")
    public ResponseEntity<Map<String, Object>> saveCrawlerResults(
            @Parameter(description = "爬取结果保存请求") @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始保存爬取结果到数据库");
            
            String crawlerType = (String) requestBody.get("crawlerType");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) requestBody.get("results");
            @SuppressWarnings({"unchecked", "unused"})
            Map<String, Object> params = (Map<String, Object>) requestBody.get("params");
            
            log.info("爬虫类型: {}, 结果数量: {}", crawlerType, results != null ? results.size() : 0);
            
            if (results == null || results.isEmpty()) {
                result.put("success", false);
                result.put("error", "没有数据可保存");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(result);
            }
            
            int savedCount = 0;
            int skippedCount = 0;
            
            for (Map<String, Object> item : results) {
                try {
                    // 创建CrawlerData实体
                    CertNewsData certNewsData = new CertNewsData();
                    
                    // 设置基本信息
                    certNewsData.setTitle((String) item.get("title"));
                    certNewsData.setContent((String) item.get("content"));
                    certNewsData.setUrl((String) item.get("url"));
                    certNewsData.setSourceName((String) item.get("source"));
                    certNewsData.setCountry((String) item.get("country"));
                    certNewsData.setType("爬虫数据");
                    
                    // 设置发布时间
                    String publishTimeStr = (String) item.get("publishTime");
                    if (publishTimeStr != null) {
                        certNewsData.setPublishDate(publishTimeStr);
                    } else {
                        certNewsData.setPublishDate(java.time.LocalDate.now().toString());
                    }
                    
                    // 设置爬取时间
                    certNewsData.setCrawlTime(LocalDateTime.now());
                    
                    // 设置默认值
                    certNewsData.setRelated(false); // 默认为不相关，需要后续手动或自动处理
                    certNewsData.setRiskLevel(CertNewsData.RiskLevel.NONE); // 默认无风险
                    certNewsData.setDeleted(0); // 0表示未删除
                    
                    // 生成摘要（取内容的前200个字符）
                    String content = certNewsData.getContent();
                    if (content != null && content.length() > 200) {
                        certNewsData.setSummary(content.substring(0, 200) + "...");
                    } else {
                        certNewsData.setSummary(content);
                    }
                    
                    // 保存到数据库
                    crawlerDataRepository.save(certNewsData);
                    savedCount++;
                    
                } catch (Exception e) {
                    log.warn("保存单条数据失败: {}", e.getMessage());
                    skippedCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "保存爬取结果成功");
            result.put("crawlerType", crawlerType);
            result.put("totalResults", results.size());
            result.put("savedCount", savedCount);
            result.put("skippedCount", skippedCount);
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("保存爬取结果完成: 总数={}, 成功={}, 跳过={}", 
                    results.size(), savedCount, skippedCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("保存爬取结果失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "保存失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取文件关键词
     */
    @Operation(summary = "获取文件关键词", description = "从CertNewsKeywords.txt文件获取关键词列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "获取失败")
    })
    @GetMapping("/keywords/file")
    public ResponseEntity<Map<String, Object>> getFileKeywords() {
        try {
            Map<String, Object> result = certNewsanalysis.getFileKeywordsInfo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "获取文件关键词失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 保存关键词到文件
     */
    @Operation(summary = "保存关键词到文件", description = "将关键词列表保存到CertNewsKeywords.txt文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "保存成功"),
        @ApiResponse(responseCode = "400", description = "保存失败")
    })
    @PostMapping("/keywords/file")
    public ResponseEntity<Map<String, Object>> saveKeywordsToFile(
            @Parameter(description = "关键词列表") @RequestBody List<String> keywords) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (keywords == null || keywords.isEmpty()) {
                result.put("success", false);
                result.put("error", "关键词列表不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            boolean success = certNewsanalysis.saveKeywordsToFile(keywords);
            if (success) {
                result.put("success", true);
                result.put("message", "关键词已保存到文件");
                result.put("count", keywords.size());
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "保存关键词到文件失败");
                result.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("保存关键词到文件失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "保存失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 从localStorage迁移关键词到文件
     */
    @Operation(summary = "迁移本地关键词到文件", description = "将localStorage中的关键词迁移到CertNewsKeywords.txt文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "迁移成功"),
        @ApiResponse(responseCode = "400", description = "迁移失败")
    })
    @PostMapping("/keywords/migrate-from-local")
    public ResponseEntity<Map<String, Object>> migrateKeywordsFromLocalStorage(
            @Parameter(description = "本地关键词列表") @RequestBody List<String> localKeywords) {
        
        try {
            Map<String, Object> result = certNewsanalysis.migrateFromLocalStorage(localKeywords);
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "迁移失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 手动更新今天的数据
     */
    @Operation(summary = "更新今天的数据", description = "手动触发更新今天的数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "更新失败")
    })
    @PostMapping("/update-today-data")
    public ResponseEntity<Map<String, Object>> updateTodayData() {
        try {
            Map<String, Object> result = certNewsanalysis.updateTodayCountryRiskStats();
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "更新今天的数据失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(result);
        }
    }
//
//    /**
//     * 生成指定日期的数据，数据与参考日期保持一致
//     */
//    @Operation(summary = "生成指定日期的数据", description = "根据参考日期生成目标日期的数据")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "生成成功"),
//        @ApiResponse(responseCode = "400", description = "生成失败")
//    })
//    @PostMapping("/generate-data-for-date")
//    public ResponseEntity<Map<String, Object>> generateDataForDate(
//            @RequestParam String targetDate,
//            @RequestParam String referenceDate) {
//        try {
//            LocalDate target = LocalDate.parse(targetDate);
//            LocalDate reference = LocalDate.parse(referenceDate);
//
//            Map<String, Object> result = certNewsanalysis.generateDataForDate(target, reference);
//            if ((Boolean) result.get("success")) {
//                return ResponseEntity.ok(result);
//            } else {
//                return ResponseEntity.badRequest().body(result);
//            }
//        } catch (Exception e) {
//            Map<String, Object> result = new HashMap<>();
//            result.put("success", false);
//            result.put("error", "生成日期数据失败: " + e.getMessage());
//            result.put("timestamp", LocalDateTime.now().toString());
//            return ResponseEntity.badRequest().body(result);
//        }
//    }
    
    /**
     * 执行AI判断（认证新闻）
     * 判断中风险数据是否与无线电子设备认证标准相关
     */
    @Operation(summary = "执行AI判断", description = "AI判断认证新闻相关性，自动提取认证关键词并写入matched_keywords和remarks字段")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "执行成功"),
        @ApiResponse(responseCode = "500", description = "执行失败")
    })
    @PostMapping("/ai-judge/execute-direct")
    public ResponseEntity<Map<String, Object>> executeAIJudge(
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean judgeAll) {
        
        log.info("收到认证新闻AI判断请求: riskLevel={}, sourceName={}, limit={}, judgeAll={}", 
            riskLevel, sourceName, limit, judgeAll);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 执行AI判断
            Map<String, Object> judgeResult = certNewsanalysis.executeAIJudgeForCertNews(
                riskLevel, sourceName, limit, judgeAll
            );
            
            if (judgeResult != null && (Boolean) judgeResult.getOrDefault("success", false)) {
                result.put("success", true);
                result.put("message", judgeResult.get("message"));
                result.put("data", judgeResult);
                
                log.info("认证新闻AI判断执行完成: {}", judgeResult.get("message"));
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", judgeResult != null ? judgeResult.get("error") : "AI判断执行失败");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("认证新闻AI判断执行失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "AI判断执行失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 获取单个新闻详情
     */
    @Operation(summary = "获取新闻详情", description = "根据ID获取单个新闻的完整信息")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNewsDetail(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            CertNewsData newsData = crawlerDataRepository.findById(id).orElse(null);
            if (newsData != null) {
                result.put("success", true);
                result.put("data", newsData);
                result.put("message", "获取新闻详情成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "新闻不存在");
                return ResponseEntity.status(404).body(result);
            }
        } catch (Exception e) {
            log.error("获取新闻详情失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取新闻详情失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 删除关键词
     */
    @Operation(summary = "删除关键词", description = "从关键词文件中删除指定关键词")
    @DeleteMapping("/keywords/{keyword}")
    public ResponseEntity<Map<String, Object>> deleteKeyword(@PathVariable String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = certNewsanalysis.deleteKeywordFromFile(keyword);
            if (success) {
                result.put("success", true);
                result.put("message", "关键词已删除");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "删除关键词失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("删除关键词失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "删除关键词失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 更新关键词
     */
    @Operation(summary = "更新关键词", description = "在关键词文件中替换关键词")
    @PutMapping("/keywords")
    public ResponseEntity<Map<String, Object>> updateKeyword(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String oldKeyword = requestBody.get("oldKeyword");
            String newKeyword = requestBody.get("newKeyword");
            
            if (oldKeyword == null || oldKeyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "旧关键词不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (newKeyword == null || newKeyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "新关键词不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            boolean success = certNewsanalysis.updateKeywordInFile(oldKeyword, newKeyword);
            if (success) {
                result.put("success", true);
                result.put("message", "关键词已更新");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "更新关键词失败");
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("更新关键词失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新关键词失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    // ========== 异步AI判断任务相关接口 ==========
    
    @Autowired
    private com.certification.service.ai.AsyncAIJudgeService asyncAIJudgeService;
    
    @Autowired
    private com.certification.repository.AIJudgeTaskRepository aiJudgeTaskRepository;
    
    /**
     * 创建异步AI判断任务
     */
    @Operation(summary = "创建异步AI判断任务", description = "创建异步任务处理大批量数据（适合几千条数据）")
    @PostMapping("/ai-judge/task/create")
    public ResponseEntity<Map<String, Object>> createAIJudgeTask(
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) Integer limit) {
        
        log.info("创建AI判断任务: riskLevel={}, sourceName={}, limit={}", riskLevel, sourceName, limit);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建任务记录
            String taskId = java.util.UUID.randomUUID().toString();
            com.certification.entity.common.AIJudgeTask task = new com.certification.entity.common.AIJudgeTask();
            task.setTaskId(taskId);
            task.setTaskType("CERT_NEWS");
            task.setStatus("PENDING");
            
            // 保存筛选条件
            Map<String, Object> filterParams = new HashMap<>();
            filterParams.put("riskLevel", riskLevel);
            filterParams.put("sourceName", sourceName);
            filterParams.put("limit", limit);
            task.setFilterParams(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(filterParams));
            
            task = aiJudgeTaskRepository.save(task);
            
            // 启动异步任务
            asyncAIJudgeService.executeAsyncJudge(taskId, riskLevel, sourceName, limit);
            
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("message", "AI判断任务已创建，正在后台处理");
            log.info("AI判断任务已创建: taskId={}", taskId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建AI判断任务失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "创建任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 查询任务进度
     */
    @Operation(summary = "查询AI判断任务进度", description = "根据任务ID查询处理进度和状态")
    @GetMapping("/ai-judge/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskProgress(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            com.certification.entity.common.AIJudgeTask task = aiJudgeTaskRepository.findByTaskId(taskId).orElse(null);
            
            if (task == null) {
                result.put("success", false);
                result.put("error", "任务不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            result.put("success", true);
            result.put("task", task);
            result.put("progress", task.getProgress());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询任务进度失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 取消任务
     */
    @Operation(summary = "取消AI判断任务", description = "取消正在运行的任务")
    @PostMapping("/ai-judge/task/{taskId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            com.certification.entity.common.AIJudgeTask task = aiJudgeTaskRepository.findByTaskId(taskId).orElse(null);
            
            if (task == null) {
                result.put("success", false);
                result.put("error", "任务不存在");
                return ResponseEntity.status(404).body(result);
            }
            
            if ("COMPLETED".equals(task.getStatus()) || "FAILED".equals(task.getStatus())) {
                result.put("success", false);
                result.put("error", "任务已结束，无法取消");
                return ResponseEntity.badRequest().body(result);
            }
            
            task.setStatus("CANCELLED");
            task.setEndTime(java.time.LocalDateTime.now());
            aiJudgeTaskRepository.save(task);
            
            result.put("success", true);
            result.put("message", "任务已取消");
            log.info("AI判断任务已取消: taskId={}", taskId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("取消任务失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "取消失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
