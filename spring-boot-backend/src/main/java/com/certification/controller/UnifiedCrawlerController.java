package com.certification.controller;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.entity.UnifiedTaskLog;
import com.certification.service.crawler.*;
import com.certification.service.crawler.schema.CrawlerSchema;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一爬虫调度管理平台Controller
 */
@RestController
@RequestMapping("/api/unified")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Slf4j
@Tag(name = "爬虫调度管理平台", description = "完整的爬虫注册、参数预设、定时调度、监控管理系统")
public class UnifiedCrawlerController {
    
    private final CrawlerRegistryService crawlerRegistry;
    private final CrawlerSchemaRegistry schemaRegistry;
    private final ParamPresetService presetService;
    private final DynamicTaskSchedulerService schedulerService;
    private final TaskExecutionService executionService;
    private final CrawlerMonitorService monitorService;
    
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
        @Parameter(description = "启用状态") @RequestParam(required = false) Boolean enabled,
        @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询预设列表: crawlerName={}, enabled={}", crawlerName, enabled);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Page<UnifiedTaskConfig> presets = presetService.getPresetsByCondition(
                crawlerName, 
                enabled, 
                PageRequest.of(page, size, Sort.by("createdAt").descending())
            );
            
            result.put("success", true);
            result.put("data", presets.getContent());
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
    @GetMapping("/presets/{id}")
    @Operation(summary = "获取预设详情", description = "获取指定预设的详细信息")
    public ResponseEntity<Map<String, Object>> getPreset(
        @Parameter(description = "预设ID") @PathVariable Long id
    ) {
        log.info("获取预设详情: ID={}", id);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里可以添加具体的获取逻辑
            result.put("success", true);
            result.put("message", "功能开发中");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取预设详情失败: ID={}", id, e);
            result.put("success", false);
            result.put("message", "获取预设详情失败: " + e.getMessage());
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
}
