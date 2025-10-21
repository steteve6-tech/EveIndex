package com.certification.controller;

import com.certification.dto.ai.AuditItem;
import com.certification.dto.ai.SmartAuditResult;
import com.certification.service.ai.AISmartAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI智能审核Controller
 * 提供高风险数据的AI自动筛选功能
 * 支持两阶段流程：预览 → 确认执行
 */
@Slf4j
@RestController
@RequestMapping("/ai/smart-audit")
public class AISmartAuditController {
    
    @Autowired
    private AISmartAuditService smartAuditService;
    
    /**
     * 预览AI审核结果（不执行任何操作）
     * 
     * 支持的实体类型：
     * - DeviceRegistrationRecord: 注册记录
     * - Device510K: 申请记录
     * - DeviceRecallRecord: 召回记录
     * - DeviceEventReport: 不良事件
     * 
     * @param entityType 实体类型（可选，为空则审核所有类型）
     * @param country 国家代码（可选，US/CN/EU）
     * @param limit 审核数量限制（默认100）
     * @return AI判断结果预览
     */
    @PostMapping("/preview")
    public ResponseEntity<?> previewSmartAudit(
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) String country,
        @RequestParam(defaultValue = "100") Integer limit
    ) {
        log.info("收到AI智能审核预览请求: entityType={}, country={}, limit={}", 
                 entityType, country, limit);
        
        try {
            // 参数验证
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核数量必须在1-1000之间"
                ));
            }
            
            // 预览AI审核结果
            SmartAuditResult result = smartAuditService.previewSmartAudit(
                entityType, country, limit
            );
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("previewMode", true);
            response.put("statistics", Map.of(
                "total", result.getTotal(),
                "willKeep", result.getKeptCount(),
                "willDowngrade", result.getDowngradedCount(),
                "failed", result.getFailedCount()
            ));
            response.put("details", result.getAuditItems());
            response.put("duration", result.getDuration());
            
            log.info("AI智能审核预览完成: {}", result.getMessage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("AI智能审核预览失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "预览失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 执行AI审核操作（基于预览结果）
     * 
     * @param auditItems 预览阶段得到的审核项列表
     * @return 执行结果
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeSmartAudit(@RequestBody List<AuditItem> auditItems) {
        log.info("收到AI智能审核执行请求，共{}条数据", auditItems != null ? auditItems.size() : 0);
        
        try {
            // 参数验证
            if (auditItems == null || auditItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "审核项列表不能为空"
                ));
            }
            
            // 执行审核操作
            SmartAuditResult result = smartAuditService.executeSmartAudit(auditItems);
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("previewMode", false);
            response.put("statistics", Map.of(
                "total", result.getTotal(),
                "kept", result.getKeptCount(),
                "downgraded", result.getDowngradedCount(),
                "failed", result.getFailedCount()
            ));
            response.put("details", result.getAuditItems());
            response.put("duration", result.getDuration());
            
            log.info("AI智能审核执行完成: {}", result.getMessage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("AI智能审核执行失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "执行失败: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 获取AI审核统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAuditStatistics() {
        try {
            // TODO: 实现审核历史统计
            Map<String, Object> stats = Map.of(
                "totalAudited", 0,
                "totalKept", 0,
                "totalDowngraded", 0,
                "lastAuditTime", ""
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 重新判断白名单制造商的所有相关数据
     * 用于添加白名单后，重新审核该制造商的所有产品
     *
     * @param manufacturer 制造商名称（白名单关键词）
     * @return 审核结果
     */
    @PostMapping("/rejudge-whitelist-manufacturer")
    public ResponseEntity<?> reJudgeWhitelistManufacturer(@RequestParam String manufacturer) {
        log.info("收到重新判断白名单制造商请求: manufacturer={}", manufacturer);

        try {
            // 参数验证
            if (manufacturer == null || manufacturer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "制造商名称不能为空"
                ));
            }

            // 重新判断
            SmartAuditResult result = smartAuditService.reJudgeWhitelistManufacturerData(manufacturer);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("statistics", Map.of(
                "total", result.getTotal(),
                "aiKept", result.getAiKept(),
                "aiDowngraded", result.getAiDowngraded(),
                "failed", result.getFailedCount()
            ));
            response.put("details", result.getAuditItems());
            response.put("duration", result.getDuration());

            log.info("重新判断白名单制造商完成: {}", result.getMessage());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("重新判断白名单制造商失败", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "重新判断失败: " + e.getMessage()
            ));
        }
    }
}

