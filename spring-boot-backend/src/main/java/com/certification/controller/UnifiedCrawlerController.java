package com.certification.controller;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.entity.UnifiedTaskLog;
import com.certification.service.crawler.*;
import com.certification.service.crawler.schema.CrawlerSchema;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一爬虫调度管理平台Controller
 */
@RestController
@RequestMapping("/unified")
@CrossOrigin(originPatterns = "*")
@Slf4j
@Tag(name = "爬虫调度管理平台", description = "完整的爬虫注册、参数预设、定时调度、监控管理系统")
public class UnifiedCrawlerController {

    @Autowired
    private CrawlerRegistryService crawlerRegistry;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Autowired
    private ParamPresetService presetService;
    
    @Autowired
    private DynamicTaskSchedulerService schedulerService;
    
    @Autowired
    private TaskExecutionService executionService;
    
    @Autowired
    private CrawlerMonitorService monitorService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ==================== 爬虫管理 ====================
    
    /**
     * 获取所有爬虫信息
     */
    @GetMapping("/crawlers")
    @Operation(summary = "获取所有爬虫信息", description = "返回系统中所有已注册的爬虫信息")
    public ResponseEntity<Map<String, Object>> getAllCrawlers() {
        log.info("获取所有爬虫信息");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Map<String, Object>> crawlers = crawlerRegistry.getAllCrawlerInfo();
            
            result.put("success", true);
            result.put("data", crawlers);
            result.put("count", crawlers.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取爬虫信息失败", e);
            result.put("success", false);
            result.put("message", "获取爬虫信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 启用爬虫
     */
    @PutMapping("/crawlers/{name}/enable")
    @Operation(summary = "启用爬虫", description = "启用指定的爬虫")
    public ResponseEntity<Map<String, Object>> enableCrawler(
        @Parameter(description = "爬虫名称") @PathVariable String name
    ) {
        log.info("启用爬虫: {}", name);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = crawlerRegistry.enableCrawler(name);
            
            result.put("success", success);
            result.put("message", success ? "爬虫已启用" : "爬虫不存在");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("启用爬虫失败: {}", name, e);
            result.put("success", false);
            result.put("message", "启用爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 停用爬虫
     */
    @PutMapping("/crawlers/{name}/disable")
    @Operation(summary = "停用爬虫", description = "停用指定的爬虫")
    public ResponseEntity<Map<String, Object>> disableCrawler(
        @Parameter(description = "爬虫名称") @PathVariable String name
    ) {
        log.info("停用爬虫: {}", name);

        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = crawlerRegistry.disableCrawler(name);

            result.put("success", success);
            result.put("message", success ? "爬虫已停用" : "爬虫不存在");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("停用爬虫失败: {}", name, e);
            result.put("success", false);
            result.put("message", "停用爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 测试爬虫
     */
    @PostMapping("/crawlers/{name}/test")
    @Operation(summary = "测试爬虫", description = "测试指定的爬虫，限制爬取数量")
    public ResponseEntity<Map<String, Object>> testCrawler(
        @Parameter(description = "爬虫名称") @PathVariable String name,
        @RequestBody(required = false) Map<String, Object> paramsMap
    ) {
        log.info("测试爬虫: {}, 参数: {}", name, paramsMap);

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取爬虫执行器
            ICrawlerExecutor crawler = crawlerRegistry.getCrawler(name);
            if (crawler == null) {
                result.put("success", false);
                result.put("message", "爬虫不存在: " + name);
                return ResponseEntity.badRequest().body(result);
            }

            // 尝试加载已保存的预设参数
            Map<String, Object> finalParams = new HashMap<>();
            List<com.certification.entity.UnifiedTaskConfig> presets = presetService.getPresetsByCrawler(name);
            
            if (!presets.isEmpty()) {
                // 使用已保存的预设参数
                com.certification.entity.UnifiedTaskConfig preset = presets.get(0);
                if (preset.getParameters() != null && !preset.getParameters().isEmpty()) {
                    try {
                        finalParams = objectMapper.readValue(
                            preset.getParameters(), 
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                        );
                        log.info("从预设加载参数: {}", finalParams);
                    } catch (Exception e) {
                        log.warn("解析预设参数失败: {}", e.getMessage());
                    }
                }
            }
            
            // 用前端传来的参数覆盖（如果有）
            if (paramsMap != null && !paramsMap.isEmpty()) {
                finalParams.putAll(paramsMap);
            }
            
            // 确保测试模式下maxRecords不超过10
            finalParams.put("maxRecords", 10);

            // 转换参数为CrawlerParams对象
            String paramsJson = objectMapper.writeValueAsString(finalParams);
            CrawlerParams params = objectMapper.readValue(paramsJson, CrawlerParams.class);

            log.info("最终测试参数: {}", params);

            // 执行爬虫
            CrawlerResult crawlerResult = crawler.execute(params);

            result.put("success", crawlerResult.getSuccess());
            result.put("message", crawlerResult.getMessage());
            result.put("data", crawlerResult);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("测试爬虫失败: {}", name, e);
            result.put("success", false);
            result.put("message", "测试爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 执行爬虫
     */
    @PostMapping("/crawlers/{name}/execute")
    @Operation(summary = "执行爬虫", description = "执行指定的爬虫")
    public ResponseEntity<Map<String, Object>> executeCrawler(
        @Parameter(description = "爬虫名称") @PathVariable String name,
        @RequestBody Map<String, Object> paramsMap
    ) {
        log.info("执行爬虫: {}, 参数: {}", name, paramsMap);

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取爬虫执行器
            ICrawlerExecutor crawler = crawlerRegistry.getCrawler(name);
            if (crawler == null) {
                result.put("success", false);
                result.put("message", "爬虫不存在: " + name);
                return ResponseEntity.badRequest().body(result);
            }

            // 尝试加载已保存的预设参数
            Map<String, Object> finalParams = new HashMap<>();
            List<com.certification.entity.UnifiedTaskConfig> presets = presetService.getPresetsByCrawler(name);
            
            if (!presets.isEmpty()) {
                // 使用已保存的预设参数
                com.certification.entity.UnifiedTaskConfig preset = presets.get(0);
                if (preset.getParameters() != null && !preset.getParameters().isEmpty()) {
                    try {
                        finalParams = objectMapper.readValue(
                            preset.getParameters(), 
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                        );
                        log.info("从预设加载参数: {}", finalParams);
                    } catch (Exception e) {
                        log.warn("解析预设参数失败: {}", e.getMessage());
                    }
                }
            }
            
            // 用前端传来的参数覆盖（如果有）
            if (paramsMap != null && !paramsMap.isEmpty()) {
                finalParams.putAll(paramsMap);
            }

            // 转换参数为CrawlerParams对象
            String paramsJson = objectMapper.writeValueAsString(finalParams);
            CrawlerParams params = objectMapper.readValue(paramsJson, CrawlerParams.class);

            // 执行爬虫
            CrawlerResult crawlerResult = crawler.execute(params);

            result.put("success", crawlerResult.getSuccess());
            result.put("message", crawlerResult.getMessage());
            result.put("data", crawlerResult);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("执行爬虫失败: {}", name, e);
            result.put("success", false);
            result.put("message", "执行爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 批量测试爬虫
     */
    @PostMapping("/crawlers/batch-test")
    @Operation(summary = "批量测试爬虫", description = "批量测试多个爬虫")
    public ResponseEntity<Map<String, Object>> batchTestCrawlers(
        @RequestBody Map<String, Object> body
    ) {
        @SuppressWarnings("unchecked")
        List<String> crawlerNames = (List<String>) body.get("crawlerNames");

        log.info("批量测试爬虫: {}", crawlerNames);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> results = new java.util.ArrayList<>();

        try {
            for (String name : crawlerNames) {
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("crawlerName", name);

                try {
                    ICrawlerExecutor crawler = crawlerRegistry.getCrawler(name);
                    if (crawler == null) {
                        testResult.put("success", false);
                        testResult.put("message", "爬虫不存在");
                    } else {
                        // 简单测试参数
                        CrawlerParams params = new CrawlerParams();
                        params.setMaxRecords(10);

                        CrawlerResult crawlerResult = crawler.execute(params);
                        testResult.put("success", crawlerResult.getSuccess());
                        testResult.put("message", crawlerResult.getMessage());
                    }
                } catch (Exception e) {
                    testResult.put("success", false);
                    testResult.put("message", e.getMessage());
                }

                results.add(testResult);
            }

            result.put("success", true);
            result.put("data", results);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("批量测试爬虫失败", e);
            result.put("success", false);
            result.put("message", "批量测试爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 批量执行爬虫
     */
    @PostMapping("/crawlers/batch-execute")
    @Operation(summary = "批量执行爬虫", description = "批量执行多个爬虫")
    public ResponseEntity<Map<String, Object>> batchExecuteCrawlers(
        @RequestBody Map<String, Object> data
    ) {
        @SuppressWarnings("unchecked")
        List<String> crawlers = (List<String>) data.get("crawlers");
        String mode = (String) data.getOrDefault("mode", "full");
        Integer maxRecords = (Integer) data.get("maxRecords");

        log.info("批量执行爬虫: {}, 模式: {}", crawlers, mode);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> results = new java.util.ArrayList<>();

        try {
            for (String name : crawlers) {
                Map<String, Object> execResult = new HashMap<>();
                execResult.put("crawlerName", name);

                try {
                    ICrawlerExecutor crawler = crawlerRegistry.getCrawler(name);
                    if (crawler == null) {
                        execResult.put("success", false);
                        execResult.put("message", "爬虫不存在");
                    } else {
                        CrawlerParams params = new CrawlerParams();
                        if (maxRecords != null) {
                            params.setMaxRecords(maxRecords);
                        }

                        CrawlerResult crawlerResult = crawler.execute(params);
                        execResult.put("success", crawlerResult.getSuccess());
                        execResult.put("message", crawlerResult.getMessage());
                    }
                } catch (Exception e) {
                    execResult.put("success", false);
                    execResult.put("message", e.getMessage());
                }

                results.add(execResult);
            }

            result.put("success", true);
            result.put("message", "批量执行完成");
            result.put("data", results);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("批量执行爬虫失败", e);
            result.put("success", false);
            result.put("message", "批量执行爬虫失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // ==================== Schema管理 ====================
    
    /**
     * 获取所有爬虫Schema
     */
    @GetMapping("/schemas")
    @Operation(summary = "获取所有爬虫Schema", description = "返回所有爬虫的参数定义Schema")
    public ResponseEntity<Map<String, Object>> getAllSchemas() {
        log.info("获取所有爬虫Schema");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Map<String, Object>> schemas = schemaRegistry.getSchemasForFrontend();
            
            result.put("success", true);
            result.put("data", schemas);
            result.put("count", schemas.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取Schema失败", e);
            result.put("success", false);
            result.put("message", "获取Schema失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取指定爬虫的Schema
     */
    @GetMapping("/schemas/{crawlerName}")
    @Operation(summary = "获取爬虫Schema", description = "返回指定爬虫的参数定义Schema")
    public ResponseEntity<Map<String, Object>> getCrawlerSchema(
        @Parameter(description = "爬虫名称") @PathVariable String crawlerName
    ) {
        log.info("获取爬虫Schema: {}", crawlerName);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
            
            if (schema == null) {
                result.put("success", false);
                result.put("message", "Schema不存在");
                return ResponseEntity.badRequest().body(result);
            }
            
            result.put("success", true);
            result.put("data", schema);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取爬虫Schema失败: {}", crawlerName, e);
            result.put("success", false);
            result.put("message", "获取爬虫Schema失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // ==================== 参数预设管理 ====================
    
    /**
     * 创建参数预设
     */
    @PostMapping("/presets")
    @Operation(summary = "创建参数预设", description = "创建新的爬虫参数预设")
    public ResponseEntity<Map<String, Object>> createPreset(
        @RequestBody ParamPresetService.PresetRequest request
    ) {
        log.info("创建参数预设: {}", request.getTaskName());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            UnifiedTaskConfig preset = presetService.createPreset(request);
            
            result.put("success", true);
            result.put("message", "预设创建成功");
            result.put("data", preset);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建预设失败", e);
            result.put("success", false);
            result.put("message", "创建预设失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 查询预设列表
     */
    @GetMapping("/presets")
    @Operation(summary = "查询预设列表", description = "根据条件查询参数预设列表")
    public ResponseEntity<Map<String, Object>> getPresets(
        @Parameter(description = "爬虫名称") @RequestParam(required = false) String crawlerName,
        @Parameter(description = "国家代码") @RequestParam(required = false) String countryCode,
        @Parameter(description = "爬虫类型") @RequestParam(required = false) String crawlerType,
        @Parameter(description = "启用状态") @RequestParam(required = false) Boolean enabled,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "100") int size
    ) {
        log.info("查询预设列表: crawlerName={}, countryCode={}, crawlerType={}, enabled={}", 
                 crawlerName, countryCode, crawlerType, enabled);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Page<UnifiedTaskConfig> presets = presetService.getPresetsByCondition(
                crawlerName, 
                countryCode,
                crawlerType,
                enabled, 
                PageRequest.of(page, size, Sort.by("createdAt").descending())
            );
            
            // 为每个任务添加crawlerType字段（从crawlerName推导）
            List<Map<String, Object>> enrichedData = presets.getContent().stream()
                .map(preset -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", preset.getId());
                    map.put("taskName", preset.getTaskName());
                    map.put("crawlerName", preset.getCrawlerName());
                    map.put("countryCode", preset.getCountryCode());
                    map.put("crawlerType", extractCrawlerType(preset.getCrawlerName()));
                    map.put("taskType", preset.getTaskType());
                    map.put("description", preset.getDescription());
                    map.put("enabled", preset.getEnabled());
                    map.put("cronExpression", preset.getCronExpression());
                    map.put("executionCount", preset.getExecutionCount());
                    map.put("successCount", preset.getSuccessCount());
                    map.put("failureCount", preset.getFailureCount());
                    map.put("successRate", preset.getSuccessRate());
                    map.put("lastExecutionTime", preset.getLastExecutionTime());
                    map.put("lastExecutionStatus", preset.getLastExecutionStatus());
                    map.put("nextExecutionTime", preset.getNextExecutionTime());
                    map.put("createdAt", preset.getCreatedAt());
                    map.put("updatedAt", preset.getUpdatedAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            result.put("success", true);
            result.put("data", enrichedData);
            result.put("total", presets.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("查询预设列表失败", e);
            result.put("success", false);
            result.put("message", "查询预设列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取预设详情
     */
    @GetMapping("/presets/{crawlerName}")
    @Operation(summary = "获取预设详情", description = "获取指定爬虫的预设配置")
    public ResponseEntity<Map<String, Object>> getPreset(
        @Parameter(description = "爬虫名称") @PathVariable String crawlerName
    ) {
        log.info("获取预设详情: crawlerName={}", crawlerName);

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取该爬虫的预设配置
            List<com.certification.entity.UnifiedTaskConfig> presets = presetService.getPresetsByCrawler(crawlerName);
            
            if (presets.isEmpty()) {
                result.put("success", false);
                result.put("message", "未找到该爬虫的预设配置");
                return ResponseEntity.notFound().build();
            }
            
            // 返回第一个预设配置
            com.certification.entity.UnifiedTaskConfig preset = presets.get(0);
            result.put("success", true);
            result.put("data", preset);
            result.put("message", "获取预设配置成功");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取预设详情失败: crawlerName={}", crawlerName, e);
            result.put("success", false);
            result.put("message", "获取预设详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取爬虫预设（用于预设编辑器）
     */
    @GetMapping("/crawlers/{crawlerName}/preset")
    @Operation(summary = "获取爬虫预设", description = "获取指定爬虫的默认预设配置和Schema")
    public ResponseEntity<Map<String, Object>> getCrawlerPreset(
        @Parameter(description = "爬虫名称") @PathVariable String crawlerName
    ) {
        log.info("获取爬虫预设: {}", crawlerName);

        Map<String, Object> result = new HashMap<>();

        try {
            // 获取爬虫Schema
            CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
            if (schema == null) {
                result.put("success", false);
                result.put("message", "爬虫不存在或Schema未注册");
                return ResponseEntity.badRequest().body(result);
            }

            // 查找该爬虫的预设（如果存在）
            List<UnifiedTaskConfig> presets = presetService.getPresetsByCrawler(crawlerName);
            UnifiedTaskConfig preset = presets.isEmpty() ? null : presets.get(0);

            result.put("success", true);
            result.put("schema", schema);
            result.put("preset", preset);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取爬虫预设失败: {}", crawlerName, e);
            result.put("success", false);
            result.put("message", "获取爬虫预设失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 保存或更新爬虫预设（用于预设编辑器）
     */
    @PostMapping("/crawlers/{crawlerName}/preset")
    @Operation(summary = "保存爬虫预设", description = "保存或更新指定爬虫的预设配置")
    public ResponseEntity<Map<String, Object>> saveCrawlerPreset(
        @Parameter(description = "爬虫名称") @PathVariable String crawlerName,
        @RequestBody Map<String, Object> presetData
    ) {
        log.info("保存爬虫预设: {}", crawlerName);

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证爬虫是否存在
            CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
            if (schema == null) {
                result.put("success", false);
                result.put("message", "爬虫不存在或Schema未注册: " + crawlerName);
                return ResponseEntity.badRequest().body(result);
            }

            // 验证爬虫执行器是否存在
            ICrawlerExecutor executor = crawlerRegistry.getCrawler(crawlerName);
            if (executor == null) {
                result.put("success", false);
                result.put("message", "爬虫执行器不存在: " + crawlerName);
                return ResponseEntity.badRequest().body(result);
            }

            // 将参数转换为JSON字符串
            String parametersJson = objectMapper.writeValueAsString(presetData);

            // 验证参数有效性
            if (!presetService.validatePresetParams(crawlerName, parametersJson)) {
                result.put("success", false);
                result.put("message", "参数验证失败，请检查参数格式");
                return ResponseEntity.badRequest().body(result);
            }

            // 查找该爬虫的现有预设（确保使用正确的爬虫名称）
            List<UnifiedTaskConfig> existingPresets = presetService.getPresetsByCrawler(crawlerName);

            UnifiedTaskConfig savedPreset;
            if (existingPresets.isEmpty()) {
                // 创建新预设
                ParamPresetService.PresetRequest request = new ParamPresetService.PresetRequest();
                request.setTaskName(crawlerName + "_默认预设");
                request.setCrawlerName(crawlerName);  // 使用executor.getCrawlerName()确保名称一致
                request.setCountryCode(schema.getCountryCode());
                request.setParameters(parametersJson);
                request.setDescription("自动生成的默认预设 - " + schema.getDescription());
                request.setEnabled(true);

                savedPreset = presetService.createPreset(request);
                log.info("创建新预设: ID={}, CrawlerName={}", savedPreset.getId(), savedPreset.getCrawlerName());
            } else {
                // 更新现有预设
                UnifiedTaskConfig existing = existingPresets.get(0);
                ParamPresetService.PresetRequest request = new ParamPresetService.PresetRequest();
                request.setParameters(parametersJson);

                savedPreset = presetService.updatePreset(existing.getId(), request);
                log.info("更新现有预设: ID={}, CrawlerName={}", savedPreset.getId(), savedPreset.getCrawlerName());
            }

            result.put("success", true);
            result.put("message", "预设保存成功");
            result.put("data", savedPreset);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("保存爬虫预设失败: {}", crawlerName, e);
            result.put("success", false);
            result.put("message", "保存爬虫预设失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 更新参数预设
     */
    @PutMapping("/presets/{id}")
    @Operation(summary = "更新参数预设", description = "更新指定的参数预设")
    public ResponseEntity<Map<String, Object>> updatePreset(
        @Parameter(description = "预设ID") @PathVariable Long id,
        @RequestBody ParamPresetService.PresetRequest request
    ) {
        log.info("更新参数预设: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            UnifiedTaskConfig preset = presetService.updatePreset(id, request);
            
            result.put("success", true);
            result.put("message", "预设更新成功");
            result.put("data", preset);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新预设失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "更新预设失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 复制参数预设
     */
    @PostMapping("/presets/{id}/copy")
    @Operation(summary = "复制参数预设", description = "复制指定的参数预设")
    public ResponseEntity<Map<String, Object>> copyPreset(
        @Parameter(description = "预设ID") @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        log.info("复制参数预设: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String newName = body.get("newName");
            UnifiedTaskConfig preset = presetService.copyPreset(id, newName);
            
            result.put("success", true);
            result.put("message", "预设复制成功");
            result.put("data", preset);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("复制预设失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "复制预设失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 删除参数预设
     */
    @DeleteMapping("/presets/{id}")
    @Operation(summary = "删除参数预设", description = "删除指定的参数预设")
    public ResponseEntity<Map<String, Object>> deletePreset(
        @Parameter(description = "预设ID") @PathVariable Long id
    ) {
        log.info("删除参数预设: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            presetService.deletePreset(id);
            
            result.put("success", true);
            result.put("message", "预设删除成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除预设失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "删除预设失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 验证参数
     */
    @PostMapping("/presets/validate")
    @Operation(summary = "验证参数", description = "验证爬虫参数是否有效")
    public ResponseEntity<Map<String, Object>> validateParams(
        @RequestBody Map<String, Object> body
    ) {
        log.info("验证参数");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String crawlerName = (String) body.get("crawlerName");
            String parameters = body.get("params").toString();
            
            boolean valid = presetService.validatePresetParams(crawlerName, parameters);
            
            result.put("success", true);
            result.put("valid", valid);
            result.put("message", valid ? "参数验证通过" : "参数验证失败");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("验证参数失败", e);
            result.put("success", false);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // ==================== 任务调度管理 ====================
    
    /**
     * 手动触发任务执行
     */
    @PostMapping("/tasks/{id}/trigger")
    @Operation(summary = "手动触发任务", description = "立即执行指定任务")
    public ResponseEntity<Map<String, Object>> triggerTask(
        @Parameter(description = "任务ID") @PathVariable Long id,
        @Parameter(description = "触发者") @RequestParam(defaultValue = "MANUAL") String triggeredBy
    ) {
        log.info("手动触发任务: ID={}, 触发者={}", id, triggeredBy);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            CrawlerResult crawlerResult = executionService.triggerTask(id, triggeredBy);
            
            result.put("success", crawlerResult.getSuccess());
            result.put("message", crawlerResult.getMessage());
            result.put("data", crawlerResult);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("触发任务失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "触发任务失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 暂停任务
     */
    @PostMapping("/tasks/{id}/pause")
    @Operation(summary = "暂停任务", description = "暂停定时任务执行")
    public ResponseEntity<Map<String, Object>> pauseTask(
        @Parameter(description = "任务ID") @PathVariable Long id
    ) {
        log.info("暂停任务: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            schedulerService.pauseTask(id);
            
            result.put("success", true);
            result.put("message", "任务已暂停");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("暂停任务失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "暂停任务失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 恢复任务
     */
    @PostMapping("/tasks/{id}/resume")
    @Operation(summary = "恢复任务", description = "恢复暂停的任务")
    public ResponseEntity<Map<String, Object>> resumeTask(
        @Parameter(description = "任务ID") @PathVariable Long id
    ) {
        log.info("恢复任务: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            schedulerService.resumeTask(id);
            
            result.put("success", true);
            result.put("message", "任务已恢复");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("恢复任务失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "恢复任务失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // ==================== 监控查询 ====================
    
    /**
     * 获取运行中任务
     */
    @GetMapping("/monitor/running")
    @Operation(summary = "获取运行中任务", description = "获取当前正在执行的任务列表")
    public ResponseEntity<Map<String, Object>> getRunningTasks() {
        log.info("获取运行中任务");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<UnifiedTaskLog> runningTasks = monitorService.getRunningTasks();
            
            result.put("success", true);
            result.put("data", runningTasks);
            result.put("count", runningTasks.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取运行中任务失败", e);
            result.put("success", false);
            result.put("message", "获取运行中任务失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取执行历史
     */
    @GetMapping("/monitor/history")
    @Operation(summary = "获取执行历史", description = "获取任务执行历史记录")
    public ResponseEntity<Map<String, Object>> getExecutionHistory(
        @Parameter(description = "爬虫名称") @RequestParam(required = false) String crawlerName,
        @Parameter(description = "状态") @RequestParam(required = false) String status,
        @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("获取执行历史: crawlerName={}, status={}", crawlerName, status);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Page<UnifiedTaskLog> history = monitorService.getExecutionHistory(
                crawlerName, 
                status, 
                startTime, 
                endTime, 
                PageRequest.of(page, size, Sort.by("startTime").descending())
            );
            
            result.put("success", true);
            result.put("data", history.getContent());
            result.put("total", history.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取执行历史失败", e);
            result.put("success", false);
            result.put("message", "获取执行历史失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/monitor/statistics/{taskId}")
    @Operation(summary = "获取任务统计", description = "获取指定任务的统计信息")
    public ResponseEntity<Map<String, Object>> getTaskStatistics(
        @Parameter(description = "任务ID") @PathVariable Long taskId
    ) {
        log.info("获取任务统计: ID={}", taskId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> stats = monitorService.getTaskStatistics(taskId);
            
            result.put("success", true);
            result.put("data", stats);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取任务统计失败: ID={}", taskId, e);
            result.put("success", false);
            result.put("message", "获取任务统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 获取系统总览
     */
    @GetMapping("/monitor/overview")
    @Operation(summary = "获取系统总览", description = "获取系统整体统计信息")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        log.info("获取系统总览");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> overview = monitorService.getSystemOverview();
            
            result.put("success", true);
            result.put("data", overview);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取系统总览失败", e);
            result.put("success", false);
            result.put("message", "获取系统总览失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 从爬虫名称提取爬虫类型
     * 例如: US_510K -> 510K, KR_Registration -> REGISTRATION
     */
    private String extractCrawlerType(String crawlerName) {
        if (crawlerName == null || crawlerName.isEmpty()) {
            return "";
        }
        
        // 去掉国家代码前缀
        String type = crawlerName;
        if (crawlerName.contains("_")) {
            String[] parts = crawlerName.split("_", 2);
            if (parts.length > 1) {
                type = parts[1];
            }
        }
        
        // 转换为大写
        return type.toUpperCase();
    }
    
    /**
     * 初始化所有爬虫的默认任务配置
     * ⚠️ 警告：此操作会清空现有任务配置！
     */
    @PostMapping("/tasks/initialize-all")
    @Operation(summary = "初始化所有爬虫默认任务", description = "清空现有任务并为所有爬虫创建默认任务配置（警告：会清空现有数据）")
    public ResponseEntity<Map<String, Object>> initializeAllTasks(
        @Parameter(description = "确认标志") @RequestParam(defaultValue = "false") boolean confirm
    ) {
        log.info("收到初始化所有任务请求，confirm={}", confirm);
        
        Map<String, Object> result = new HashMap<>();
        
        if (!confirm) {
            result.put("success", false);
            result.put("message", "需要确认才能执行此操作，请添加参数 confirm=true");
            return ResponseEntity.badRequest().body(result);
        }
        
        try {
            // 1. 清空现有任务
            presetService.deleteAllPresets();
            log.info("已清空现有任务配置");
            
            // 2. 创建默认任务配置
            List<DefaultTaskConfig> defaultTasks = createDefaultTaskConfigs();
            
            int successCount = 0;
            int failCount = 0;
            
            for (DefaultTaskConfig config : defaultTasks) {
                try {
                    ParamPresetService.PresetRequest request = new ParamPresetService.PresetRequest();
                    request.setTaskName(config.taskName);
                    request.setCrawlerName(config.crawlerName);
                    request.setCountryCode(config.countryCode);
                    request.setParameters(config.parameters);
                    request.setCronExpression(config.cronExpression);
                    request.setDescription(config.description);
                    request.setEnabled(config.enabled);
                    
                    presetService.createPreset(request);
                    successCount++;
                    log.info("创建默认任务: {} ({})", config.taskName, config.crawlerName);
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("创建默认任务失败: {} - {}", config.taskName, e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("初始化完成，成功创建 %d 个任务，失败 %d 个", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", defaultTasks.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("初始化默认任务失败", e);
            result.put("success", false);
            result.put("message", "初始化失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 创建所有爬虫的默认任务配置
     */
    private List<DefaultTaskConfig> createDefaultTaskConfigs() {
        List<DefaultTaskConfig> configs = new ArrayList<>();
        
        // ========== 美国爬虫 (6个) ==========
        configs.add(new DefaultTaskConfig(
            "美国510K每日爬取", "US_510K", "US", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 2 * * ?",
            "每天凌晨2点自动爬取美国FDA 510K申请记录", true
        ));
        configs.add(new DefaultTaskConfig(
            "美国召回每日爬取", "US_Recall", "US", "DATE_RANGE",
            "{\"maxRecords\": 100}", "0 0 3 * * ?",
            "每天凌晨3点自动爬取美国FDA召回数据", true
        ));
        configs.add(new DefaultTaskConfig(
            "美国注册记录每日爬取", "US_Registration", "US", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 4 * * ?",
            "每天凌晨4点自动爬取美国设备注册记录", true
        ));
        configs.add(new DefaultTaskConfig(
            "美国不良事件每日爬取", "US_Event", "US", "DATE_RANGE",
            "{\"maxRecords\": 100}", "0 0 5 * * ?",
            "每天凌晨5点自动爬取美国FDA不良事件报告", true
        ));
        configs.add(new DefaultTaskConfig(
            "美国指导文档每周爬取", "US_Guidance", "US", "FULL",
            "{\"maxRecords\": 50}", "0 0 9 ? * MON",
            "每周一上午9点自动爬取美国FDA指导文档", true
        ));
        configs.add(new DefaultTaskConfig(
            "美国海关案例每周爬取", "US_CustomsCase", "US", "DATE_RANGE",
            "{\"maxRecords\": 50}", "0 0 10 ? * MON",
            "每周一上午10点自动爬取美国海关案例", true
        ));
        
        // ========== 欧盟爬虫 (4个) ==========
        configs.add(new DefaultTaskConfig(
            "欧盟召回每日爬取", "EU_Recall", "EU", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 6 * * ?",
            "每天早上6点自动爬取欧盟召回数据", true
        ));
        configs.add(new DefaultTaskConfig(
            "欧盟注册记录每日爬取", "EU_Registration", "EU", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 7 * * ?",
            "每天早上7点自动爬取欧盟设备注册记录", true
        ));
        configs.add(new DefaultTaskConfig(
            "欧盟指导文档每周爬取", "EU_Guidance", "EU", "FULL",
            "{\"maxRecords\": 50}", "0 0 9 ? * TUE",
            "每周二上午9点自动爬取欧盟医疗设备新闻", true
        ));
        configs.add(new DefaultTaskConfig(
            "欧盟海关案例每周爬取", "EU_CustomsCase", "EU", "DATE_RANGE",
            "{\"maxRecords\": 50}", "0 0 10 ? * TUE",
            "每周二上午10点自动爬取欧盟海关案例", true
        ));
        
        // ========== 韩国爬虫 (5个) ==========
        configs.add(new DefaultTaskConfig(
            "韩国召回每日爬取", "KR_Recall", "KR", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 8 * * ?",
            "每天早上8点自动爬取韩国MFDS召回数据", true
        ));
        configs.add(new DefaultTaskConfig(
            "韩国注册记录每日爬取", "KR_Registration", "KR", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 9 * * ?",
            "每天上午9点自动爬取韩国医疗器械注册数据", true
        ));
        configs.add(new DefaultTaskConfig(
            "韩国不良事件每日爬取", "KR_Event", "KR", "DATE_RANGE",
            "{\"maxRecords\": 100}", "0 0 10 * * ?",
            "每天上午10点自动爬取韩国不良事件报告", true
        ));
        configs.add(new DefaultTaskConfig(
            "韩国指导文档每周爬取", "KR_Guidance", "KR", "FULL",
            "{\"maxRecords\": 50}", "0 0 9 ? * WED",
            "每周三上午9点自动爬取韩国法规指导文档", true
        ));
        configs.add(new DefaultTaskConfig(
            "韩国海关案例每周爬取", "KR_CustomsCase", "KR", "DATE_RANGE",
            "{\"maxRecords\": 50}", "0 0 10 ? * WED",
            "每周三上午10点自动爬取韩国海关案例", true
        ));
        
        // ========== 日本爬虫 (3个) ==========
        configs.add(new DefaultTaskConfig(
            "日本召回每日爬取", "JP_Recall", "JP", "DATE_RANGE",
            "{\"maxRecords\": 100}", "0 0 11 * * ?",
            "每天上午11点自动爬取日本PMDA召回数据", true
        ));
        configs.add(new DefaultTaskConfig(
            "日本指导文档每周爬取", "JP_Guidance", "JP", "FULL",
            "{\"maxRecords\": 50}", "0 0 9 ? * THU",
            "每周四上午9点自动爬取日本医疗器械法规", true
        ));
        configs.add(new DefaultTaskConfig(
            "日本注册记录每日爬取", "JP_Registration", "JP", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 12 * * ?",
            "每天中午12点自动爬取日本医疗器械注册数据", true
        ));
        
        // ========== 台湾爬虫 (4个) ==========
        configs.add(new DefaultTaskConfig(
            "台湾注册记录每日爬取", "TW_Registration", "TW", "KEYWORD_BATCH",
            "{\"maxRecords\": 100, \"applicantNames\": [], \"factoryNames\": [], \"prodNameC\": [], \"prodNameE\": []}", "0 0 13 * * ?",
            "每天下午1点自动爬取台湾FDA注册记录（需要验证码）", false  // 默认禁用
        ));
        configs.add(new DefaultTaskConfig(
            "台湾海关案例每周爬取", "TW_CustomsCase", "TW", "DATE_RANGE",
            "{\"maxRecords\": 50}", "0 0 9 ? * FRI",
            "每周五上午9点自动爬取台湾海关货品输出入规定", true
        ));
        configs.add(new DefaultTaskConfig(
            "台湾指导文档每周爬取", "TW_Guidance", "TW", "FULL",
            "{\"maxRecords\": 50}", "0 0 10 ? * FRI",
            "每周五上午10点自动爬取台湾FDA医疗器材法规", true
        ));
        configs.add(new DefaultTaskConfig(
            "台湾召回每日爬取", "TW_Recall", "TW", "KEYWORD_BATCH",
            "{\"maxRecords\": 100}", "0 0 14 * * ?",
            "每天下午2点自动爬取台湾FDA召回通报", true
        ));
        
        return configs;
    }
    
    /**
     * 默认任务配置内部类
     */
    private static class DefaultTaskConfig {
        String taskName;
        String crawlerName;
        String countryCode;
        String taskType;
        String parameters;
        String cronExpression;
        String description;
        boolean enabled;
        
        DefaultTaskConfig(String taskName, String crawlerName, String countryCode, 
                         String taskType, String parameters, String cronExpression,
                         String description, boolean enabled) {
            this.taskName = taskName;
            this.crawlerName = crawlerName;
            this.countryCode = countryCode;
            this.taskType = taskType;
            this.parameters = parameters;
            this.cronExpression = cronExpression;
            this.description = description;
            this.enabled = enabled;
        }
    }
}
