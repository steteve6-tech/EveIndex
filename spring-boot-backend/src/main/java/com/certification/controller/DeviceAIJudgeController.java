package com.certification.controller;

import com.certification.dto.ai.SmartAuditResult;
import com.certification.dto.ai.AuditItem;
import com.certification.entity.ai.AIPendingJudgment;
import com.certification.repository.ai.AIPendingJudgmentRepository;
import com.certification.service.ai.AISmartAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 设备数据AI判断控制器
 * 提供前端调用的AI判断接口
 */
@Slf4j
@RestController
@RequestMapping("/device-data/ai-judge")
@Tag(name = "设备数据AI判断", description = "AI智能判断设备数据是否与测肤仪相关")
public class DeviceAIJudgeController {
    
    @Autowired
    private AISmartAuditService aiSmartAuditService;

    @Autowired
    private com.certification.service.DeviceMatchKeywordsService deviceMatchKeywordsService;

    @Autowired
    private AIPendingJudgmentRepository pendingJudgmentRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 预览AI判断结果
     */
    @PostMapping("/preview")
    @Operation(summary = "预览AI判断结果", description = "按条件预览AI判断结果，不执行任何操作")
    public ResponseEntity<Map<String, Object>> previewAIJudge(@RequestBody Map<String, Object> params) {
        log.info("收到预览AI判断请求: {}", params);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 提取参数
            String country = (String) params.get("country");
            
            @SuppressWarnings("unchecked")
            List<String> entityTypes = (List<String>) params.get("entityTypes");
            
            String riskLevel = (String) params.get("riskLevel");
            Integer limit = params.get("limit") != null ? ((Number) params.get("limit")).intValue() : 50;
            
            // 调用预览服务
            SmartAuditResult result = aiSmartAuditService.previewSmartAuditByConditions(
                country,
                entityTypes,
                riskLevel,
                limit
            );
            
            // 构建响应
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("data", Map.of(
                "auditItems", result.getAuditItems(),
                "keptCount", result.getKeptCount(),
                "downgradedCount", result.getDowngradedCount(),
                "failedCount", result.getFailedCount(),
                "estimatedCost", calculateEstimatedCost(result.getAuditItems().size())
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("预览AI判断失败", e);
            response.put("success", false);
            response.put("message", "预览失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 执行AI判断操作
     */
    @PostMapping("/execute")
    @Operation(summary = "执行AI判断操作", description = "根据预览结果执行AI判断，更新数据库")
    public ResponseEntity<Map<String, Object>> executeAIJudge(@RequestBody Map<String, Object> params) {
        log.info("收到执行AI判断请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> auditItemMaps = (List<Map<String, Object>>) params.get("auditItems");
            
            if (auditItemMaps == null || auditItemMaps.isEmpty()) {
                response.put("success", false);
                response.put("message", "审核项列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 转换为AuditItem对象
            List<AuditItem> auditItems = convertToAuditItems(auditItemMaps);
            
            // 执行审核
            SmartAuditResult result = aiSmartAuditService.executeSmartAudit(auditItems);
            
            // 构建响应
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("data", Map.of(
                "keptCount", result.getKeptCount(),
                "downgradedCount", result.getDowngradedCount(),
                "failedCount", result.getFailedCount()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("执行AI判断失败", e);
            response.put("success", false);
            response.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取可判断数据统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取可判断数据统计", description = "获取符合条件的可判断数据统计信息")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @Parameter(description = "国家代码") @RequestParam(required = false) String country,
            @Parameter(description = "风险等级") @RequestParam(required = false) String riskLevel
    ) {
        log.info("收到获取统计请求: country={}, riskLevel={}", country, riskLevel);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 实现统计功能
            // 这里可以调用repository的count方法获取统计信息
            
            Map<String, Integer> byEntityType = new HashMap<>();
            byEntityType.put("Device510K", 0);
            byEntityType.put("DeviceRegistrationRecord", 0);
            byEntityType.put("DeviceRecallRecord", 0);
            byEntityType.put("DeviceEventReport", 0);
            
            Map<String, Integer> byRiskLevel = new HashMap<>();
            byRiskLevel.put("HIGH", 0);
            byRiskLevel.put("MEDIUM", 0);
            byRiskLevel.put("LOW", 0);
            
            response.put("success", true);
            response.put("data", Map.of(
                "totalCount", 0,
                "byEntityType", byEntityType,
                "byRiskLevel", byRiskLevel
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取统计失败", e);
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 计算预估成本
     */
    private String calculateEstimatedCost(int count) {
        // GPT-3.5-turbo价格: 约$0.004/条
        double cost = count * 0.004;
        return String.format("$%.2f", cost);
    }
    
    /**
     * 转换Map为AuditItem对象
     */
    @SuppressWarnings("unchecked")
    private List<AuditItem> convertToAuditItems(List<Map<String, Object>> auditItemMaps) {
        List<AuditItem> auditItems = new ArrayList<>();
        
        for (Map<String, Object> map : auditItemMaps) {
            AuditItem item = new AuditItem();
            
            item.setId(((Number) map.get("id")).longValue());
            item.setEntityType((String) map.get("entityType"));
            item.setDeviceName((String) map.get("deviceName"));
            item.setRelatedToSkinDevice((Boolean) map.get("relatedToSkinDevice"));
            item.setReason((String) map.get("reason"));
            item.setConfidence(((Number) map.get("confidence")).doubleValue());
            
            List<String> keywords = (List<String>) map.get("blacklistKeywords");
            if (keywords != null) {
                item.setBlacklistKeywords(keywords);
            }
            
            // 新增字段
            if (map.containsKey("blacklistMatched")) {
                item.setBlacklistMatched((Boolean) map.get("blacklistMatched"));
            }
            if (map.containsKey("matchedBlacklistKeyword")) {
                item.setMatchedBlacklistKeyword((String) map.get("matchedBlacklistKeyword"));
            }
            if (map.containsKey("suggestedBlacklist")) {
                List<String> suggested = (List<String>) map.get("suggestedBlacklist");
                if (suggested != null) {
                    item.setSuggestedBlacklist(suggested);
                }
            }
            
            auditItems.add(item);
        }
        
        return auditItems;
    }
    
    /**
     * 新接口：带黑名单预检查的AI判断预览
     */
    @PostMapping("/preview-with-blacklist")
    @Operation(summary = "预览AI判断（带黑名单检查）", description = "黑名单优先检查，未匹配的数据才进行AI判断")
    public ResponseEntity<Map<String, Object>> previewWithBlacklist(@RequestBody Map<String, Object> params) {
        log.info("收到AI判断预览请求（带黑名单）: {}", params);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 提取参数
            String country = (String) params.get("country");
            
            @SuppressWarnings("unchecked")
            List<String> entityTypes = (List<String>) params.get("entityTypes");
            
            String riskLevel = (String) params.get("riskLevel");
            Integer limit = params.get("limit") != null ? ((Number) params.get("limit")).intValue() : null;
            Boolean judgeAll = params.get("judgeAll") != null ? (Boolean) params.get("judgeAll") : false;
            
            // 调用新的预览服务
            SmartAuditResult result = aiSmartAuditService.previewWithBlacklistCheck(
                country,
                entityTypes,
                riskLevel,
                limit,
                judgeAll
            );
            
            // 构建响应
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("data", Map.of(
                "totalCount", result.getTotal(),
                "blacklistFiltered", result.getBlacklistFiltered(),
                "aiJudged", result.getAiJudged(),
                "aiKept", result.getAiKept(),
                "aiDowngraded", result.getAiDowngraded(),
                "keptCount", result.getKeptCount(),
                "downgradedCount", result.getDowngradedCount(),
                "failedCount", result.getFailedCount(),
                "auditItems", result.getAuditItems(),
                "estimatedCost", calculateEstimatedCost(result.getAiJudged())
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("预览AI判断失败", e);
            response.put("success", false);
            response.put("message", "预览失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 新接口：执行AI判断并更新黑名单
     */
    @PostMapping("/execute-with-blacklist")
    @Operation(summary = "执行AI判断（带黑名单更新）", description = "执行判断并自动添加建议的黑名单关键词")
    public ResponseEntity<Map<String, Object>> executeWithBlacklist(@RequestBody Map<String, Object> params) {
        log.info("收到执行AI判断请求（带黑名单）");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> auditItemMaps = (List<Map<String, Object>>) params.get("auditItems");
            
            @SuppressWarnings("unchecked")
            List<String> newBlacklistKeywords = (List<String>) params.get("newBlacklistKeywords");
            
            if (auditItemMaps == null || auditItemMaps.isEmpty()) {
                response.put("success", false);
                response.put("message", "审核项列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 转换为AuditItem对象
            List<AuditItem> auditItems = convertToAuditItems(auditItemMaps);
            
            // 执行审核并更新黑名单
            SmartAuditResult result = aiSmartAuditService.executeWithBlacklistUpdate(auditItems, newBlacklistKeywords);
            
            // 构建响应
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("data", Map.of(
                "blacklistFiltered", result.getBlacklistFiltered(),
                "aiKept", result.getAiKept(),
                "aiDowngraded", result.getAiDowngraded(),
                "failedCount", result.getFailedCount()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("执行AI判断失败", e);
            response.put("success", false);
            response.put("message", "执行失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 直接执行AI判断（不带预览）
     * 【重要】现在改为延迟执行模式：将AI判断结果保存到待审核表，等待用户确认后再执行
     */
    @PostMapping("/execute-direct")
    @Operation(summary = "执行AI判断", description = "执行AI判断并保存到待审核表，等待用户确认后再执行")
    public ResponseEntity<Map<String, Object>> executeDirectAIJudge(@RequestBody Map<String, Object> params) {
        log.info("收到AI判断请求（延迟执行模式）: {}", params);

        Map<String, Object> response = new HashMap<>();

        try {
            // 提取参数
            String country = (String) params.get("country");

            @SuppressWarnings("unchecked")
            List<String> entityTypes = (List<String>) params.get("entityTypes");

            String riskLevel = (String) params.get("riskLevel");
            Integer limit = params.get("limit") != null ? ((Number) params.get("limit")).intValue() : 50;
            Boolean judgeAll = params.get("judgeAll") != null ? (Boolean) params.get("judgeAll") : false;

            log.info("AI判断参数: country={}, entityTypes={}, riskLevel={}, limit={}, judgeAll={}",
                    country, entityTypes, riskLevel, limit, judgeAll);

            // 1. 获取AI判断结果和建议的黑名单关键词
            SmartAuditResult previewResult = aiSmartAuditService.previewWithBlacklistCheck(
                country, entityTypes, riskLevel, limit, judgeAll
            );

            if (!previewResult.isSuccess()) {
                response.put("success", false);
                response.put("message", previewResult.getMessage());
                return ResponseEntity.ok(response);
            }

            // 2. 收集建议的黑名单关键词
            List<String> suggestedBlacklist = new ArrayList<>();
            if (previewResult.getAuditItems() != null) {
                for (AuditItem item : previewResult.getAuditItems()) {
                    if (item.getSuggestedBlacklist() != null) {
                        suggestedBlacklist.addAll(item.getSuggestedBlacklist());
                    }
                }
            }

            // 去重
            suggestedBlacklist = suggestedBlacklist.stream()
                .distinct()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());

            log.info("收集到建议的黑名单关键词: {}", suggestedBlacklist);

            // 3. 将AI判断结果保存到待审核表（不直接执行）
            int savedCount = 0;
            int failedCount = 0;
            List<AIPendingJudgment> pendingJudgments = new ArrayList<>();

            if (previewResult.getAuditItems() != null) {
                for (AuditItem item : previewResult.getAuditItems()) {
                    try {
                        AIPendingJudgment pending = new AIPendingJudgment();
                        pending.setModuleType("DEVICE_DATA");
                        pending.setEntityType(item.getEntityType());
                        pending.setEntityId(item.getId());

                        // 序列化AI判断结果
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("isRelated", item.isRelatedToSkinDevice());
                        resultMap.put("confidence", item.getConfidence());
                        resultMap.put("reason", item.getReason());
                        resultMap.put("category", item.getCategory());
                        pending.setJudgeResult(objectMapper.writeValueAsString(resultMap));

                        // 设置建议的风险等级和备注
                        String suggestedRiskLevel;
                        if (item.getBlacklistKeywords() != null && !item.getBlacklistKeywords().isEmpty()) {
                            // 黑名单过滤 -> LOW
                            suggestedRiskLevel = "LOW";
                        } else if (item.isRelatedToSkinDevice()) {
                            // AI判断相关 -> HIGH
                            suggestedRiskLevel = "HIGH";
                        } else {
                            // AI判断不相关 -> LOW
                            suggestedRiskLevel = "LOW";
                        }
                        pending.setSuggestedRiskLevel(suggestedRiskLevel);

                        // 设置备注
                        String remark = item.getRemark() != null ? item.getRemark() :
                            (item.isRelatedToSkinDevice() ?
                                String.format("AI判断为测肤仪相关设备 (置信度: %.0f%%, 类别: %s)",
                                    item.getConfidence() * 100, item.getCategory()) :
                                String.format("AI判断为非测肤仪设备 (置信度: %.0f%%)",
                                    item.getConfidence() * 100));
                        pending.setSuggestedRemark(remark);

                        // 设置黑名单关键词
                        if (item.getBlacklistKeywords() != null && !item.getBlacklistKeywords().isEmpty()) {
                            pending.setBlacklistKeywords(objectMapper.writeValueAsString(item.getBlacklistKeywords()));
                            pending.setFilteredByBlacklist(true);
                        } else {
                            pending.setFilteredByBlacklist(false);
                        }

                        pendingJudgments.add(pending);
                        savedCount++;

                    } catch (Exception e) {
                        log.error("保存待审核记录失败: entityId={}, error={}", item.getId(), e.getMessage());
                        failedCount++;
                    }
                }
            }

            // 批量保存到数据库
            if (!pendingJudgments.isEmpty()) {
                pendingJudgmentRepository.saveAll(pendingJudgments);
                log.info("已保存 {} 条AI判断结果到待审核表", savedCount);
            }

            // 返回结果
            response.put("success", true);
            response.put("message", String.format("AI判断完成，已保存 %d 条结果到待审核列表，等待您确认后执行", savedCount));

            // 返回统计数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("totalCount", previewResult.getTotal());
            resultData.put("savedCount", savedCount);
            resultData.put("failedCount", failedCount);
            resultData.put("blacklistFiltered", previewResult.getBlacklistFiltered());
            resultData.put("aiKept", previewResult.getAiKept());
            resultData.put("aiDowngraded", previewResult.getAiDowngraded());
            resultData.put("estimatedCost", "$0.00");
            resultData.put("newBlacklistCount", suggestedBlacklist.size());
            resultData.put("newBlacklistKeywords", suggestedBlacklist);
            resultData.put("pendingReviewUrl", "/ai-judgment/pending?moduleType=DEVICE_DATA");

            response.put("data", resultData);

            log.info("AI判断完成（延迟执行模式）: 保存了 {} 条待审核记录", savedCount);

        } catch (Exception e) {
            log.error("AI判断失败", e);
            response.put("success", false);
            response.put("message", "AI判断失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取黑名单关键词列表
     */
    @GetMapping("/blacklist-keywords")
    @Operation(summary = "获取黑名单关键词", description = "获取所有启用的黑名单关键词")
    public ResponseEntity<Map<String, Object>> getBlacklistKeywords() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> blacklist = deviceMatchKeywordsService.getBlacklistKeywordStrings();
            
            response.put("success", true);
            response.put("data", blacklist);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取黑名单关键词失败", e);
            response.put("success", false);
            response.put("message", "获取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // ========== 异步AI判断任务相关接口 ==========
    
    @Autowired
    private com.certification.service.ai.AsyncDeviceAIJudgeService asyncDeviceAIJudgeService;
    
    @Autowired
    private com.certification.repository.AIJudgeTaskRepository aiJudgeTaskRepository;
    
    /**
     * 创建异步AI判断任务（设备数据）
     */
    @PostMapping("/task/create")
    @Operation(summary = "创建异步AI判断任务", description = "创建异步任务处理大批量设备数据（适合几千条数据）")
    public ResponseEntity<Map<String, Object>> createDeviceAIJudgeTask(@RequestBody Map<String, Object> params) {
        log.info("创建设备AI判断任务: {}", params);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 提取参数
            String country = (String) params.get("country");
            
            @SuppressWarnings("unchecked")
            List<String> entityTypes = (List<String>) params.get("entityTypes");
            
            String riskLevel = (String) params.get("riskLevel");
            Integer limit = params.get("limit") != null ? ((Number) params.get("limit")).intValue() : null;
            
            // 创建任务记录
            String taskId = java.util.UUID.randomUUID().toString();
            com.certification.entity.common.AIJudgeTask task = new com.certification.entity.common.AIJudgeTask();
            task.setTaskId(taskId);
            task.setTaskType("DEVICE_DATA");
            task.setStatus("PENDING");
            
            // 保存筛选条件
            Map<String, Object> filterParams = new HashMap<>();
            filterParams.put("country", country);
            filterParams.put("entityTypes", entityTypes);
            filterParams.put("riskLevel", riskLevel);
            filterParams.put("limit", limit);
            task.setFilterParams(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(filterParams));
            
            task = aiJudgeTaskRepository.save(task);
            
            // 启动异步任务
            asyncDeviceAIJudgeService.executeAsyncDeviceJudge(taskId, country, entityTypes, riskLevel, limit);
            
            result.put("success", true);
            result.put("taskId", taskId);
            result.put("message", "设备AI判断任务已创建，正在后台处理");
            log.info("设备AI判断任务已创建: taskId={}", taskId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("创建设备AI判断任务失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "创建任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * 查询任务进度
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询AI判断任务进度", description = "根据任务ID查询处理进度和状态")
    public ResponseEntity<Map<String, Object>> getDeviceTaskProgress(@PathVariable String taskId) {
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
    @PostMapping("/task/{taskId}/cancel")
    @Operation(summary = "取消AI判断任务", description = "取消正在运行的任务")
    public ResponseEntity<Map<String, Object>> cancelDeviceTask(@PathVariable String taskId) {
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
            log.info("设备AI判断任务已取消: taskId={}", taskId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("取消任务失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "取消失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}

