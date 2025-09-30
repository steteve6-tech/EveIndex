package com.certification.controller;

import com.certification.entity.ScheduledCrawlerConfig;
import com.certification.service.ScheduledCrawlerConfigService;
import com.certification.service.CertNewsDataScheduledService;
import com.certification.service.DeviceDataScheduledService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时爬取管理控制器
 * 提供定时爬取配置的管理和监控功能
 */
@Slf4j
@RestController
@RequestMapping("/scheduled-crawlers")
@Tag(name = "定时爬取管理", description = "定时爬取配置管理和监控API")
public class ScheduledCrawlerController {

    @Autowired
    private ScheduledCrawlerConfigService configService;
    
    @Autowired
    private CertNewsDataScheduledService certNewsDataScheduledService;
    
    @Autowired
    private DeviceDataScheduledService deviceDataScheduledService;

    /**
     * 获取所有定时爬取配置
     */
    @Operation(summary = "获取所有定时爬取配置", description = "获取系统中所有定时爬取任务的配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ScheduledCrawlerConfig> configs = configService.getAllConfigs();
            result.put("success", true);
            result.put("data", configs);
            result.put("total", configs.size());
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取定时爬取配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 分页获取定时爬取配置
     */
    @Operation(summary = "分页获取定时爬取配置", description = "分页获取定时爬取任务的配置信息")
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getConfigsPage(
            @Parameter(description = "分页参数") Pageable pageable) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Page<ScheduledCrawlerConfig> page = configService.getConfigsPage(pageable);
            result.put("success", true);
            result.put("data", page.getContent());
            result.put("total", page.getTotalElements());
            result.put("page", page.getNumber());
            result.put("size", page.getSize());
            result.put("totalPages", page.getTotalPages());
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("分页获取定时爬取配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 根据模块获取配置
     */
    @Operation(summary = "根据模块获取配置", description = "获取指定模块的所有定时爬取配置")
    @GetMapping("/module/{moduleName}")
    public ResponseEntity<Map<String, Object>> getConfigsByModule(
            @Parameter(description = "模块名称") @PathVariable String moduleName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ScheduledCrawlerConfig> configs = configService.getConfigsByModule(moduleName);
            result.put("success", true);
            result.put("data", configs);
            result.put("total", configs.size());
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("根据模块获取配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 创建或更新配置
     */
    @Operation(summary = "创建或更新配置", description = "创建新的定时爬取配置或更新现有配置")
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveConfig(
            @Parameter(description = "配置信息") @RequestBody ScheduledCrawlerConfig config) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ScheduledCrawlerConfig savedConfig = configService.saveConfig(config);
            result.put("success", true);
            result.put("data", savedConfig);
            result.put("message", "配置保存成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("保存配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "保存配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 更新配置
     */
    @Operation(summary = "更新配置", description = "更新现有的定时爬取配置")
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateConfig(
            @Parameter(description = "配置信息") @RequestBody ScheduledCrawlerConfig config) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ScheduledCrawlerConfig updatedConfig = configService.saveConfig(config);
            result.put("success", true);
            result.put("data", updatedConfig);
            result.put("message", "配置更新成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("更新配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "更新配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 删除配置
     */
    @Operation(summary = "删除配置", description = "删除指定的定时爬取配置")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteConfig(
            @Parameter(description = "配置ID") @PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            configService.deleteConfig(id);
            result.put("success", true);
            result.put("message", "配置删除成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("删除配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "删除配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 启用/禁用配置
     */
    @Operation(summary = "启用/禁用配置", description = "启用或禁用指定的定时爬取配置")
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleConfig(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            configService.toggleConfigStatus(id, enabled);
            result.put("success", true);
            result.put("message", enabled ? "配置已启用" : "配置已禁用");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("切换配置状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "切换配置状态失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 创建默认配置
     */
    @Operation(summary = "创建默认配置", description = "创建系统默认的定时爬取配置")
    @PostMapping("/default")
    public ResponseEntity<Map<String, Object>> createDefaultConfigs() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            configService.createDefaultConfigs();
            result.put("success", true);
            result.put("message", "默认配置创建成功");
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建默认配置失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "创建默认配置失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }


    /**
     * 手动触发CertNewsData爬虫
     */
    @Operation(summary = "手动触发CertNewsData爬虫", description = "手动触发指定CertNewsData模块的爬虫")
    @PostMapping("/trigger/certnewsdata/{crawlerName}")
    public ResponseEntity<Map<String, Object>> triggerCertNewsDataCrawler(
            @Parameter(description = "爬虫名称") @PathVariable String crawlerName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> triggerResult = certNewsDataScheduledService.triggerCrawlerManually(crawlerName);
            result.put("success", triggerResult.get("success"));
            result.put("message", triggerResult.get("message"));
            result.put("executionTime", triggerResult.get("executionTime"));
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("手动触发CertNewsData爬虫失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "触发爬虫失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 手动触发设备数据爬虫
     */
    @Operation(summary = "手动触发设备数据爬虫", description = "手动触发指定设备数据模块的爬虫")
    @PostMapping("/trigger/devicedata/{crawlerName}")
    public ResponseEntity<Map<String, Object>> triggerDeviceDataCrawler(
            @Parameter(description = "爬虫名称") @PathVariable String crawlerName,
            @Parameter(description = "国家代码") @RequestParam String countryCode,
            @Parameter(description = "模块名称") @RequestParam String moduleName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> triggerResult = deviceDataScheduledService.triggerDeviceCrawlerManually(
                    crawlerName, countryCode, moduleName);
            result.put("success", triggerResult.get("success"));
            result.put("message", triggerResult.get("message"));
            result.put("executionTime", triggerResult.get("executionTime"));
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("手动触发设备数据爬虫失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "触发爬虫失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取CertNewsData爬虫状态
     */
    @Operation(summary = "获取CertNewsData爬虫状态", description = "获取CertNewsData模块所有爬虫的执行状态")
    @GetMapping("/status/certnewsdata")
    public ResponseEntity<Map<String, Object>> getCertNewsDataStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> status = certNewsDataScheduledService.getCrawlerStatus();
            result.put("success", true);
            result.put("data", status);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取CertNewsData爬虫状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取状态失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取设备数据爬虫状态
     */
    @Operation(summary = "获取设备数据爬虫状态", description = "获取设备数据模块所有爬虫的执行状态")
    @GetMapping("/status/devicedata")
    public ResponseEntity<Map<String, Object>> getDeviceDataStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> status = deviceDataScheduledService.getDeviceCrawlerStatus();
            result.put("success", true);
            result.put("data", status);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取设备数据爬虫状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取状态失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取所有爬虫状态
     */
    @Operation(summary = "获取所有爬虫状态", description = "获取系统中所有爬虫的执行状态")
    @GetMapping("/status/all")
    public ResponseEntity<Map<String, Object>> getAllCrawlerStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> allStatus = new HashMap<>();
            
            // 获取CertNewsData状态
            Map<String, Object> certNewsDataStatus = certNewsDataScheduledService.getCrawlerStatus();
            allStatus.put("certnewsdata", certNewsDataStatus);
            
            // 获取设备数据状态
            Map<String, Object> deviceDataStatus = deviceDataScheduledService.getDeviceCrawlerStatus();
            allStatus.put("devicedata", deviceDataStatus);
            
            
            result.put("success", true);
            result.put("data", allStatus);
            result.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("获取所有爬虫状态失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "获取状态失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(result);
        }
    }
}
