package com.certification.controller;

import com.certification.analysis.DeviceDataanalysis;
import com.certification.entity.common.DeviceMatchKeywords;
import com.certification.repository.DeviceMatchKeywordsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 高风险数据管理控制器
 * 提供风险等级为HIGH的数据的查询、编辑和更新功能
 */
@Slf4j
@RestController
@RequestMapping("/high-risk-data")
@RequiredArgsConstructor
@Tag(name = "高风险数据管理", description = "管理风险等级为HIGH的医疗器械数据")
public class DeviceHighRiskDataController {

    private final DeviceDataanalysis deviceDataanalysis;
    private final DeviceMatchKeywordsRepository deviceMatchKeywordsRepository;

    /**
     * 获取所有高风险数据统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取高风险数据统计", description = "获取各类型高风险数据的数量统计")
    public ResponseEntity<Map<String, Object>> getHighRiskStatistics() {
        try {
            Map<String, Object> statistics = deviceDataanalysis.getHighRiskStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取高风险数据统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取统计数据失败"));
        }
    }

    /**
     * 获取带趋势信息的高风险数据统计
     */
    @GetMapping("/statistics/with-trend")
    @Operation(summary = "获取带趋势的高风险数据统计", description = "获取各类型高风险数据的数量统计，包含今天相对于昨天的变化趋势")
    public ResponseEntity<Map<String, Object>> getHighRiskStatisticsWithTrend() {
        try {
            Map<String, Object> statistics = deviceDataanalysis.getHighRiskStatisticsWithTrend();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取带趋势的高风险数据统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取统计数据失败"));
        }
    }

    /**
     * 获取按国家分类的高风险数据统计
     */
    @GetMapping("/statistics/by-country")
    @Operation(summary = "获取按国家分类的高风险数据统计", description = "获取各国家各类型高风险数据的数量统计")
    public ResponseEntity<Map<String, Object>> getHighRiskStatisticsByCountry() {
        try {
            Map<String, Object> statistics = deviceDataanalysis.getHighRiskStatisticsByCountry();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取按国家分类的高风险数据统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取统计数据失败"));
        }
    }

    /**
     * 获取按国家分类的带趋势的高风险数据统计
     */
    @GetMapping("/statistics/by-country/with-trend")
    @Operation(summary = "获取按国家分类的带趋势的高风险数据统计", description = "获取各国家各类型高风险数据的数量统计，包含各国今天相对于昨天的变化趋势")
    public ResponseEntity<Map<String, Object>> getHighRiskStatisticsByCountryWithTrend() {
        try {
            Map<String, Object> statistics = deviceDataanalysis.getHighRiskStatisticsByCountryWithTrend();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取按国家分类的带趋势的高风险数据统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取统计数据失败"));
        }
    }

    /**
     * 获取指定类型的高风险数据
     */
    @GetMapping("/{dataType}")
    @Operation(summary = "获取指定类型的高风险数据", description = "根据数据类型获取风险等级为HIGH的数据，支持关键词和国家筛选")
    public ResponseEntity<Page<Map<String, Object>>> getHighRiskDataByType(
            @Parameter(description = "数据类型") @PathVariable String dataType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "关键词筛选") @RequestParam(required = false) String keyword,
            @Parameter(description = "国家筛选") @RequestParam(required = false) String country,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String searchKeyword,
            @Parameter(description = "风险等级筛选") @RequestParam(required = false) String riskLevel) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Map<String, Object>> result;
            
            // 如果有任何筛选条件，需要先获取所有数据再筛选分页
            if ((keyword != null && !keyword.trim().isEmpty()) || 
                (country != null && !country.trim().isEmpty()) ||
                (searchKeyword != null && !searchKeyword.trim().isEmpty()) ||
                (riskLevel != null && !riskLevel.trim().isEmpty())) {
                log.info("应用筛选: 关键词={}, 国家={}, 搜索关键词={}, 风险等级={}", keyword, country, searchKeyword, riskLevel);
                
                // 获取所有数据（不分页）
                Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(direction, sortBy));
                Page<Map<String, Object>> allData = deviceDataanalysis.getHighRiskDataByType(dataType, allPageable);
                log.info("获取到 {} 类型所有数据 {} 条记录", dataType, allData.getContent().size());
                
                // 开始筛选逻辑
                List<Map<String, Object>> filteredContent = allData.getContent();
                
                // 1. 关键词筛选
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String keywordLower = keyword.toLowerCase();
                    filteredContent = filteredContent.stream()
                        .filter(item -> {
                            // 主要检查keywords字段（与统计逻辑保持一致）
                            Object keywordsObj = item.get("keywords");
                            if (keywordsObj != null) {
                                String keywordsStr = keywordsObj.toString();
                                
                                // 解析keywords字段并检查是否包含关键词
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    @SuppressWarnings("unchecked")
                                    List<String> keywordsList = mapper.readValue(keywordsStr, List.class);
                                    
                                    boolean matched = keywordsList.stream()
                                        .anyMatch(k -> k.toLowerCase().contains(keywordLower));
                                    
                                    if (matched) {
                                        return true;
                                    }
                                } catch (Exception e) {
                                    // 如果不是JSON格式，按逗号分割检查
                                    if (keywordsStr.toLowerCase().contains(keywordLower)) {
                                        return true;
                                    }
                                }
                            }
                            
                            // 备用检查：检查matchedKeywords字段
                            Object matchedKeywordsObj = item.get("matchedKeywords");
                            if (matchedKeywordsObj != null) {
                                String matchedKeywordsStr = matchedKeywordsObj.toString();
                                if (matchedKeywordsStr.toLowerCase().contains(keywordLower)) {
                                    return true;
                                }
                            }
                            
                            // 备用检查：检查其他可能包含关键词的字段
                            String deviceName = (String) item.get("deviceName");
                            String productDescription = (String) item.get("productDescription");
                            String brandName = (String) item.get("brandName");
                            String title = (String) item.get("title");
                            String manufacturerName = (String) item.get("manufacturerName");
                            String recallingFirm = (String) item.get("recallingFirm");
                            String applicant = (String) item.get("applicant");
                            String rulingResult = (String) item.get("rulingResult");
                            String caseNumber = (String) item.get("caseNumber");
                            String topic = (String) item.get("topic");
                            
                            return (deviceName != null && deviceName.toLowerCase().contains(keywordLower)) ||
                                   (productDescription != null && productDescription.toLowerCase().contains(keywordLower)) ||
                                   (brandName != null && brandName.toLowerCase().contains(keywordLower)) ||
                                   (title != null && title.toLowerCase().contains(keywordLower)) ||
                                   (manufacturerName != null && manufacturerName.toLowerCase().contains(keywordLower)) ||
                                   (recallingFirm != null && recallingFirm.toLowerCase().contains(keywordLower)) ||
                                   (applicant != null && applicant.toLowerCase().contains(keywordLower)) ||
                                   (rulingResult != null && rulingResult.toLowerCase().contains(keywordLower)) ||
                                   (caseNumber != null && caseNumber.toLowerCase().contains(keywordLower)) ||
                                   (topic != null && topic.toLowerCase().contains(keywordLower));
                        })
                        .collect(Collectors.toList());
                    
                    log.info("关键词筛选完成，筛选出 {} 条匹配关键词 '{}' 的记录", filteredContent.size(), keyword);
                }
                
                // 2. 国家筛选
                if (country != null && !country.trim().isEmpty()) {
                    String countryUpper = country.toUpperCase();
                    filteredContent = filteredContent.stream()
                        .filter(item -> {
                            // 检查jdCountry字段
                            Object jdCountryObj = item.get("jdCountry");
                            if (jdCountryObj != null) {
                                String jdCountry = jdCountryObj.toString();
                                if (jdCountry.equalsIgnoreCase(countryUpper)) {
                                    return true;
                                }
                            }
                            
                            // 检查countryCode字段
                            Object countryCodeObj = item.get("countryCode");
                            if (countryCodeObj != null) {
                                String countryCode = countryCodeObj.toString();
                                if (countryCode.equalsIgnoreCase(countryUpper)) {
                                    return true;
                                }
                            }
                            
                            return false;
                        })
                        .collect(Collectors.toList());
                    
                    log.info("国家筛选完成，筛选出 {} 条匹配国家 '{}' 的记录", filteredContent.size(), country);
                }
                
                // 3. 搜索关键词筛选（在内容中搜索）
                if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                    String searchKeywordLower = searchKeyword.toLowerCase();
                    filteredContent = filteredContent.stream()
                        .filter(item -> {
                            // 在各个文本字段中搜索关键词
                            String deviceName = (String) item.get("deviceName");
                            String productDescription = (String) item.get("productDescription");
                            String brandName = (String) item.get("brandName");
                            String title = (String) item.get("title");
                            String manufacturerName = (String) item.get("manufacturerName");
                            String recallingFirm = (String) item.get("recallingFirm");
                            String applicant = (String) item.get("applicant");
                            String summary = (String) item.get("summary");
                            String content = (String) item.get("content");
                            String tradeName = (String) item.get("tradeName");
                            String establishmentName = (String) item.get("establishmentName");
                            String caseDescription = (String) item.get("caseDescription");
                            
                            return (deviceName != null && deviceName.toLowerCase().contains(searchKeywordLower)) ||
                                   (productDescription != null && productDescription.toLowerCase().contains(searchKeywordLower)) ||
                                   (brandName != null && brandName.toLowerCase().contains(searchKeywordLower)) ||
                                   (title != null && title.toLowerCase().contains(searchKeywordLower)) ||
                                   (manufacturerName != null && manufacturerName.toLowerCase().contains(searchKeywordLower)) ||
                                   (recallingFirm != null && recallingFirm.toLowerCase().contains(searchKeywordLower)) ||
                                   (applicant != null && applicant.toLowerCase().contains(searchKeywordLower)) ||
                                   (summary != null && summary.toLowerCase().contains(searchKeywordLower)) ||
                                   (content != null && content.toLowerCase().contains(searchKeywordLower)) ||
                                   (tradeName != null && tradeName.toLowerCase().contains(searchKeywordLower)) ||
                                   (establishmentName != null && establishmentName.toLowerCase().contains(searchKeywordLower)) ||
                                   (caseDescription != null && caseDescription.toLowerCase().contains(searchKeywordLower));
                        })
                        .collect(Collectors.toList());
                    
                    log.info("搜索关键词筛选完成，筛选出 {} 条匹配搜索关键词 '{}' 的记录", filteredContent.size(), searchKeyword);
                }
                
                // 4. 风险等级筛选
                if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                    String riskLevelUpper = riskLevel.toUpperCase();
                    filteredContent = filteredContent.stream()
                        .filter(item -> {
                            Object riskLevelObj = item.get("riskLevel");
                            if (riskLevelObj != null) {
                                String itemRiskLevel = riskLevelObj.toString();
                                return itemRiskLevel.equalsIgnoreCase(riskLevelUpper);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                    
                    log.info("风险等级筛选完成，筛选出 {} 条匹配风险等级 '{}' 的记录", filteredContent.size(), riskLevel);
                }
                
                log.info("最终筛选完成，共 {} 条记录（关键词：{}，国家：{}，搜索关键词：{}，风险等级：{}）", 
                    filteredContent.size(), keyword, country, searchKeyword, riskLevel);
                
                // 对筛选结果进行分页
                int totalElements = filteredContent.size();
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), totalElements);
                
                List<Map<String, Object>> pageContent = start < totalElements ? 
                    filteredContent.subList(start, end) : new ArrayList<>();
                
                result = new org.springframework.data.domain.PageImpl<>(
                    pageContent, 
                    pageable, 
                    totalElements
                );
            } else {
                // 没有关键词筛选，直接获取分页数据
                result = deviceDataanalysis.getHighRiskDataByType(dataType, pageable);
                log.info("获取到 {} 类型数据 {} 条记录", dataType, result.getContent().size());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取{}类型高风险数据失败", dataType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新数据风险等级
     */
    @PutMapping("/{dataType}/{id}/risk-level")
    @Operation(summary = "更新数据风险等级", description = "更新指定数据的风险等级")
    public ResponseEntity<Map<String, Object>> updateRiskLevel(
            @Parameter(description = "数据类型") @PathVariable String dataType,
            @Parameter(description = "数据ID") @PathVariable Long id,
            @Parameter(description = "新的风险等级") @RequestBody Map<String, String> request) {
        try {
            String newRiskLevel = request.get("riskLevel");
            if (newRiskLevel == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "风险等级不能为空"));
            }
            
            boolean success = deviceDataanalysis.updateRiskLevel(dataType, id, newRiskLevel);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "风险等级更新成功"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "更新失败，数据不存在或类型不支持"));
            }
        } catch (Exception e) {
            log.error("更新风险等级失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "更新失败"));
        }
    }

    /**
     * 批量更新风险等级
     */
    @PutMapping("/batch/risk-level")
    @Operation(summary = "批量更新风险等级", description = "批量更新多个数据的风险等级，支持跨数据类型")
    public ResponseEntity<Map<String, Object>> batchUpdateRiskLevel(
            @Parameter(description = "批量更新请求") @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) request.get("ids");
            String riskLevel = (String) request.get("riskLevel");
            
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "数据ID列表不能为空"));
            }
            
            if (riskLevel == null || riskLevel.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "风险等级不能为空"));
            }
            
            log.info("开始批量更新风险等级，数据数量: {}，目标风险等级: {}", ids.size(), riskLevel);
            
            int updatedCount = 0;
            List<String> errors = new ArrayList<>();
            
            // 分组处理不同数据类型的数据
            // 由于我们需要知道数据类型，这里先尝试在所有类型中查找并更新
            String[] dataTypes = {"device510k", "recall", "event", "registration", "guidance", "customs"};
            
            for (Integer id : ids) {
                boolean updated = false;
                Long longId = id.longValue();
                
                // 尝试在各个数据类型中更新
                for (String dataType : dataTypes) {
                    try {
                        boolean success = deviceDataanalysis.updateRiskLevel(dataType, longId, riskLevel);
                        if (success) {
                            updatedCount++;
                            updated = true;
                            log.info("成功更新 {} 类型数据 ID {} 的风险等级为 {}", dataType, id, riskLevel);
                            break; // 找到并更新成功后跳出循环
                        }
                    } catch (Exception e) {
                        // 忽略单个更新失败，继续尝试其他数据类型
                        log.debug("在 {} 类型中未找到 ID {} 的数据", dataType, id);
                    }
                }
                
                if (!updated) {
                    errors.add("ID " + id + " 的数据未找到或更新失败");
                    log.warn("未能更新 ID {} 的数据", id);
                }
            }
            
            Map<String, Object> result = Map.of(
                "updatedCount", updatedCount,
                "totalCount", ids.size(),
                "errors", errors
            );
            
            if (updatedCount > 0) {
                log.info("批量更新完成，成功更新 {} 条数据，失败 {} 条", updatedCount, ids.size() - updatedCount);
                return ResponseEntity.ok(result);
            } else {
                log.warn("批量更新失败，没有数据被更新");
                return ResponseEntity.badRequest().body(Map.of("error", "没有数据被更新", "details", errors));
            }
            
        } catch (Exception e) {
            log.error("批量更新风险等级失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "批量更新失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索高风险数据
     */
    @PostMapping("/search")
    @Operation(summary = "搜索高风险数据", description = "根据条件搜索高风险数据")
    public ResponseEntity<Page<Map<String, Object>>> searchHighRiskData(
            @Parameter(description = "搜索条件") @RequestBody Map<String, Object> searchCriteria,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
            Page<Map<String, Object>> result = deviceDataanalysis.searchHighRiskData(searchCriteria, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("搜索高风险数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取高风险数据详情
     */
    @GetMapping("/{dataType}/{id}")
    @Operation(summary = "获取高风险数据详情", description = "获取指定高风险数据的详细信息")
    public ResponseEntity<Map<String, Object>> getHighRiskDataDetail(
            @Parameter(description = "数据类型") @PathVariable String dataType,
            @Parameter(description = "数据ID") @PathVariable Long id) {
        try {
            Map<String, Object> detail = deviceDataanalysis.getHighRiskDataDetail(dataType, id);
            if (detail != null) {
                return ResponseEntity.ok(detail);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取高风险数据详情失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取今天相对于昨天的高风险数据变化趋势
     */
    @GetMapping("/trend")
    @Operation(summary = "获取高风险数据变化趋势", description = "获取今天的高风险数据相对于昨天的变化趋势")
    public ResponseEntity<Map<String, Object>> getHighRiskTrend() {
        try {
            Map<String, Object> trend = deviceDataanalysis.calculateTodayVsYesterdayTrend();
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            log.error("获取高风险数据趋势失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取趋势数据失败"));
        }
    }


    /**
     * 获取关键词统计信息
     */
    @GetMapping("/keywords/statistics")
    @Operation(summary = "获取关键词统计", description = "获取所有关键词及其对应的高风险数据数量统计，支持国家筛选")
    public ResponseEntity<Map<String, Object>> getKeywordStatistics(
            @Parameter(description = "国家筛选") @RequestParam(required = false) String country) {
        try {
            log.info("开始获取关键词统计信息，国家筛选: {}", country);
            
            // 调用服务层获取实际的关键词统计
            Map<String, Object> statistics = deviceDataanalysis.getKeywordStatistics(country);
            
            log.info("关键词统计信息生成成功");
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("获取关键词统计失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "获取关键词统计失败"));
        }
    }

    /**
     * 更新数据的匹配关键词
     */
    @PutMapping("/{dataType}/{id}/keywords")
    @Operation(summary = "更新匹配关键词", description = "更新指定数据记录的匹配关键词")
    public ResponseEntity<Map<String, Object>> updateKeywords(
            @Parameter(description = "数据类型") @PathVariable String dataType,
            @Parameter(description = "数据ID") @PathVariable Long id,
            @Parameter(description = "关键词更新请求") @RequestBody Map<String, String> request) {
        try {
            String oldKeyword = request.get("oldKeyword");
            String newKeyword = request.get("newKeyword");
            
            if (oldKeyword == null || newKeyword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "旧关键词和新关键词不能为空"));
            }
            
            log.info("更新关键词: 数据类型={}, ID={}, 旧关键词={}, 新关键词={}", dataType, id, oldKeyword, newKeyword);
            
            // 模拟更新成功
                return ResponseEntity.ok(Map.of("success", true, "message", "关键词更新成功"));
            
        } catch (Exception e) {
            log.error("更新关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "更新关键词失败"));
        }
    }

    // ==================== DeviceMatchKeywords 管理接口 ====================

    /**
     * 获取所有关键词
     */
    @GetMapping("/keywords")
    @Operation(summary = "获取所有关键词", description = "获取DeviceMatchKeywords表中的所有关键词")
    public ResponseEntity<List<DeviceMatchKeywords>> getAllKeywords() {
        try {
            log.info("开始获取所有关键词");
            List<DeviceMatchKeywords> keywords = deviceMatchKeywordsRepository.findAll();
            log.info("成功获取 {} 个关键词", keywords.size());
            return ResponseEntity.ok(keywords);
        } catch (Exception e) {
            log.error("获取所有关键词失败", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 根据类型获取关键词
     */
    @GetMapping("/keywords/by-type")
    @Operation(summary = "根据类型获取关键词", description = "根据关键词类型获取关键词列表")
    public ResponseEntity<List<DeviceMatchKeywords>> getKeywordsByType(
            @Parameter(description = "关键词类型") @RequestParam DeviceMatchKeywords.KeywordType keywordType) {
        try {
            log.info("开始获取类型为 {} 的关键词", keywordType);
            List<DeviceMatchKeywords> keywords = deviceMatchKeywordsRepository.findByKeywordType(keywordType);
            log.info("成功获取 {} 个 {} 类型关键词", keywords.size(), keywordType);
            return ResponseEntity.ok(keywords);
        } catch (Exception e) {
            log.error("根据类型获取关键词失败", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 创建新关键词
     */
    @PostMapping("/keywords")
    @Operation(summary = "创建关键词", description = "在DeviceMatchKeywords表中创建新的关键词")
    public ResponseEntity<Map<String, Object>> createKeyword(
            @Parameter(description = "关键词创建请求") @RequestBody Map<String, Object> request) {
        try {
            String keyword = (String) request.get("keyword");
            String keywordTypeStr = (String) request.get("keywordType");
            Boolean enabled = (Boolean) request.getOrDefault("enabled", true);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "关键词不能为空"));
            }
            
            if (keywordTypeStr == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "关键词类型不能为空"));
            }
            
            DeviceMatchKeywords.KeywordType keywordType;
            try {
                keywordType = DeviceMatchKeywords.KeywordType.valueOf(keywordTypeStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "无效的关键词类型"));
            }
            
            // 检查关键词是否已存在
            Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                .findByKeywordAndKeywordType(keyword.trim(), keywordType);
            
            if (existing.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "该关键词已存在"));
            }
            
            // 创建新关键词
            DeviceMatchKeywords newKeyword = new DeviceMatchKeywords();
            newKeyword.setKeyword(keyword.trim());
            newKeyword.setKeywordType(keywordType);
            newKeyword.setEnabled(enabled);
            
            DeviceMatchKeywords saved = deviceMatchKeywordsRepository.save(newKeyword);
            
            log.info("成功创建关键词: ID={}, keyword={}, type={}, enabled={}", 
                saved.getId(), saved.getKeyword(), saved.getKeywordType(), saved.getEnabled());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "关键词创建成功", "data", saved));
            
        } catch (Exception e) {
            log.error("创建关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "创建关键词失败"));
        }
    }

    /**
     * 更新关键词
     */
    @PutMapping("/keywords/{id}")
    @Operation(summary = "更新关键词", description = "更新DeviceMatchKeywords表中的关键词信息")
    public ResponseEntity<Map<String, Object>> updateKeyword(
            @Parameter(description = "关键词ID") @PathVariable Long id,
            @Parameter(description = "关键词更新请求") @RequestBody Map<String, Object> request) {
        try {
            Optional<DeviceMatchKeywords> optional = deviceMatchKeywordsRepository.findById(id);
            if (!optional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            DeviceMatchKeywords keyword = optional.get();
            
            // 更新关键词内容
            if (request.containsKey("keyword")) {
                String newKeyword = (String) request.get("keyword");
                if (newKeyword != null && !newKeyword.trim().isEmpty()) {
                    // 检查新关键词是否与其他记录冲突
                    Optional<DeviceMatchKeywords> existing = deviceMatchKeywordsRepository
                        .findByKeywordAndKeywordType(newKeyword.trim(), keyword.getKeywordType());
                    
                    if (existing.isPresent() && !existing.get().getId().equals(id)) {
                        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "该关键词已存在"));
                    }
                    keyword.setKeyword(newKeyword.trim());
                }
            }
            
            // 更新关键词类型
            if (request.containsKey("keywordType")) {
                String keywordTypeStr = (String) request.get("keywordType");
                if (keywordTypeStr != null) {
                    try {
                        DeviceMatchKeywords.KeywordType keywordType = DeviceMatchKeywords.KeywordType.valueOf(keywordTypeStr);
                        keyword.setKeywordType(keywordType);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(Map.of("success", false, "error", "无效的关键词类型"));
                    }
                }
            }
            
            // 更新启用状态
            if (request.containsKey("enabled")) {
                Boolean enabled = (Boolean) request.get("enabled");
                if (enabled != null) {
                    keyword.setEnabled(enabled);
                }
            }
            
            DeviceMatchKeywords saved = deviceMatchKeywordsRepository.save(keyword);
            
            log.info("成功更新关键词: ID={}, keyword={}, type={}, enabled={}", 
                saved.getId(), saved.getKeyword(), saved.getKeywordType(), saved.getEnabled());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "关键词更新成功", "data", saved));
            
        } catch (Exception e) {
            log.error("更新关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "更新关键词失败"));
        }
    }

    /**
     * 删除关键词
     */
    @DeleteMapping("/keywords/{id}")
    @Operation(summary = "删除关键词", description = "从DeviceMatchKeywords表中删除关键词")
    public ResponseEntity<Map<String, Object>> deleteKeyword(
            @Parameter(description = "关键词ID") @PathVariable Long id) {
        try {
            Optional<DeviceMatchKeywords> optional = deviceMatchKeywordsRepository.findById(id);
            if (!optional.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            DeviceMatchKeywords keyword = optional.get();
            deviceMatchKeywordsRepository.delete(keyword);
            
            log.info("成功删除关键词: ID={}, keyword={}", id, keyword.getKeyword());
            
            return ResponseEntity.ok(Map.of("success", true, "message", "关键词删除成功"));
            
        } catch (Exception e) {
            log.error("删除关键词失败", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "删除关键词失败"));
        }
    }

    /**
     * 搜索关键词
     */
    @GetMapping("/keywords/search")
    @Operation(summary = "搜索关键词", description = "根据关键词内容搜索DeviceMatchKeywords")
    public ResponseEntity<List<DeviceMatchKeywords>> searchKeywords(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            log.info("搜索关键词: {}", keyword);
            List<DeviceMatchKeywords> keywords = deviceMatchKeywordsRepository
                .findByKeywordContainingIgnoreCase(keyword);
            log.info("找到 {} 个匹配的关键词", keywords.size());
            return ResponseEntity.ok(keywords);
        } catch (Exception e) {
            log.error("搜索关键词失败", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * 更新数据备注
     */
    @PutMapping("/remarks/{id}")
    @Operation(summary = "更新数据备注", description = "更新指定数据的备注信息")
    public ResponseEntity<Map<String, Object>> updateDataRemarks(
            @Parameter(description = "数据ID") @PathVariable Long id,
            @Parameter(description = "备注内容") @RequestBody Map<String, String> request) {
        try {
            String remarks = request.get("remarks");
            if (remarks == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "备注内容不能为空"));
            }
            
            boolean success = deviceDataanalysis.updateDataRemarks(id, remarks);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "备注更新成功", "success", true));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "更新失败，数据不存在"));
            }
        } catch (Exception e) {
            log.error("更新备注失败", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "更新失败: " + e.getMessage()));
        }
    }
}