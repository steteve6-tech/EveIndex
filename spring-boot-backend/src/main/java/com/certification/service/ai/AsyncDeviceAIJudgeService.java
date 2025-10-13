package com.certification.service.ai;

import com.certification.dto.ai.AuditItem;
import com.certification.dto.ai.ClassificationResult;
import com.certification.entity.common.AIJudgeTask;
import com.certification.entity.common.*;
import com.certification.repository.*;
import com.certification.repository.common.*;
import com.certification.service.DeviceMatchKeywordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备数据异步AI判断服务
 * 支持大批量设备数据的异步处理
 */
@Slf4j
@Service
public class AsyncDeviceAIJudgeService {
    
    @Autowired
    private AIClassificationService aiClassificationService;
    
    @Autowired
    private AISmartAuditService aiSmartAuditService;
    
    @Autowired
    private com.certification.repository.AIJudgeTaskRepository taskRepository;
    
    @Autowired
    private DeviceMatchKeywordsService deviceMatchKeywordsService;
    
    // 各个设备数据Repository
    @Autowired
    private Device510KRepository device510KRepository;
    
    @Autowired
    private DeviceRegistrationRecordRepository deviceRegistrationRecordRepository;
    
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;
    
    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;
    
    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;
    
    @Autowired
    private CustomsCaseRepository customsCaseRepository;
    
    /**
     * 异步执行设备数据AI判断
     * 
     * @param taskId 任务ID
     * @param country 国家
     * @param entityTypes 数据类型列表
     * @param riskLevel 风险等级
     * @param limit 限制数量（为null表示处理全部）
     */
    @Async("aiJudgeExecutor")
    public void executeAsyncDeviceJudge(
        String taskId,
        String country,
        List<String> entityTypes,
        String riskLevel,
        Integer limit
    ) {
        log.info("开始异步设备AI判断任务: taskId={}, country={}, entityTypes={}, riskLevel={}, limit={}", 
            taskId, country, entityTypes, riskLevel, limit);
        
        AIJudgeTask task = taskRepository.findByTaskId(taskId).orElse(null);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        try {
            // 更新任务状态为运行中
            task.setStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            taskRepository.save(task);
            
            // 确定要处理的数据类型
            List<String> typesToProcess = (entityTypes != null && !entityTypes.isEmpty()) 
                ? entityTypes 
                : Arrays.asList("Device510K", "DeviceRegistrationRecord", "DeviceRecallRecord", 
                              "DeviceEventReport", "GuidanceDocument", "CustomsCase");
            
            Set<String> allNewBlacklistKeywords = new HashSet<>();
            int totalProcessed = 0;
            int totalKept = 0;
            int totalDowngraded = 0;
            int totalFailed = 0;
            
            // 逐个类型处理
            for (String entityType : typesToProcess) {
                // 检查任务是否被取消
                task = taskRepository.findByTaskId(taskId).orElse(null);
                if (task == null || "CANCELLED".equals(task.getStatus())) {
                    log.info("任务已取消: {}", taskId);
                    return;
                }
                
                log.info("开始处理数据类型: {}", entityType);
                
                // 获取数据
                List<?> dataList = getDataByTypeAndConditions(entityType, country, riskLevel, limit);
                
                if (dataList == null || dataList.isEmpty()) {
                    log.info("{}类型没有需要处理的数据", entityType);
                    continue;
                }
                
                task.setTotalCount((task.getTotalCount() != null ? task.getTotalCount() : 0) + dataList.size());
                taskRepository.save(task);
                
                // 批次处理
                int batchSize = 100;
                
                for (int i = 0; i < dataList.size(); i++) {
                    // 检查任务是否被取消
                    task = taskRepository.findByTaskId(taskId).orElse(null);
                    if (task == null || "CANCELLED".equals(task.getStatus())) {
                        log.info("任务已取消: {}", taskId);
                        return;
                    }
                    
                    Object data = dataList.get(i);
                    
                    try {
                        // 执行单条数据判断
                        AuditItem auditItem = aiSmartAuditService.auditSingleData(data, entityType);
                        
                        if (auditItem.isRelatedToSkinDevice()) {
                            // 相关 - 保留为高风险，不降级
                            totalKept++;
                            task.setRelatedCount((task.getRelatedCount() != null ? task.getRelatedCount() : 0) + 1);
                        } else {
                            // 不相关 - 降级为低风险，提取黑名单关键词
                            List<String> blacklistKeywords = aiSmartAuditService.extractBlacklistKeywords(data, entityType, auditItem);
                            
                            if (blacklistKeywords != null && !blacklistKeywords.isEmpty()) {
                                allNewBlacklistKeywords.addAll(blacklistKeywords);
                                task.setKeywordCount(allNewBlacklistKeywords.size());
                            }
                            
                            // 降级数据到低风险
                            downgradeDeviceData(data, entityType, auditItem);
                            totalDowngraded++;
                            task.setUnrelatedCount((task.getUnrelatedCount() != null ? task.getUnrelatedCount() : 0) + 1);
                        }
                        
                    } catch (Exception e) {
                        log.error("处理数据失败: entityType={}, error={}", entityType, e.getMessage());
                        totalFailed++;
                        task.setFailedCount((task.getFailedCount() != null ? task.getFailedCount() : 0) + 1);
                    }
                    
                    // 更新进度
                    totalProcessed++;
                    task.setProcessedCount(totalProcessed);
                    
                    // 每100条或最后一条保存一次任务状态
                    if (totalProcessed % batchSize == 0 || i == dataList.size() - 1) {
                        taskRepository.save(task);
                        log.info("任务进度: {}/{} ({}%)", task.getProcessedCount(), task.getTotalCount(), task.getProgress());
                        
                        // 批次休息，避免API限流
                        if (i < dataList.size() - 1) {
                            Thread.sleep(1000);
                        }
                    }
                }
            }
            
            // 保存新增的黑名单关键词
            if (!allNewBlacklistKeywords.isEmpty()) {
                for (String keyword : allNewBlacklistKeywords) {
                    deviceMatchKeywordsService.addKeyword(keyword, 
                        com.certification.entity.common.DeviceMatchKeywords.KeywordType.BLACKLIST);
                }
                log.info("添加了 {} 个黑名单关键词", allNewBlacklistKeywords.size());
            }
            
            // 任务完成
            task.setStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());
            taskRepository.save(task);
            
            log.info("异步设备AI判断任务完成: taskId={}, 总计={}, 保留={}, 降级={}, 失败={}, 黑名单关键词={}", 
                taskId, task.getTotalCount(), task.getRelatedCount(), 
                task.getUnrelatedCount(), task.getFailedCount(), task.getKeywordCount());
            
        } catch (Exception e) {
            log.error("异步设备AI判断任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            taskRepository.save(task);
        }
    }
    
    /**
     * 根据条件获取数据
     */
    private List<?> getDataByTypeAndConditions(String entityType, String country, String riskLevel, Integer limit) {
        return aiSmartAuditService.getDataByTypeAndConditions(entityType, country, riskLevel, limit);
    }
    
    /**
     * 降级设备数据到低风险
     */
    private void downgradeDeviceData(Object data, String entityType, AuditItem auditItem) {
        String updateReason = String.format("AI判断: 不相关, 置信度: %.1f%%, 理由: %s", 
            auditItem.getConfidence() * 100, auditItem.getReason());
        
        switch (entityType) {
            case "DeviceRegistrationRecord":
                DeviceRegistrationRecord registration = (DeviceRegistrationRecord) data;
                registration.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // DeviceRegistrationRecord 没有remarks字段，使用keywords字段记录
                deviceRegistrationRecordRepository.save(registration);
                log.debug("降级注册记录 {} 为低风险: {}", registration.getId(), updateReason);
                break;
                
            case "Device510K":
                Device510K device510K = (Device510K) data;
                device510K.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // Device510K 没有remarks字段，使用keywords字段记录
                device510KRepository.save(device510K);
                log.debug("降级510K申请 {} 为低风险: {}", device510K.getId(), updateReason);
                break;
                
            case "DeviceRecallRecord":
                DeviceRecallRecord recall = (DeviceRecallRecord) data;
                recall.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // DeviceRecallRecord 没有remarks字段
                deviceRecallRecordRepository.save(recall);
                log.debug("降级召回记录 {} 为低风险: {}", recall.getId(), updateReason);
                break;
                
            case "DeviceEventReport":
                DeviceEventReport event = (DeviceEventReport) data;
                event.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // DeviceEventReport 没有remarks字段
                deviceEventReportRepository.save(event);
                log.debug("降级事件报告 {} 为低风险: {}", event.getId(), updateReason);
                break;
                
            case "GuidanceDocument":
                GuidanceDocument document = (GuidanceDocument) data;
                document.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // GuidanceDocument 没有remarks字段，使用keywords字段记录
                guidanceDocumentRepository.save(document);
                log.debug("降级指导文档 {} 为低风险: {}", document.getId(), updateReason);
                break;
                
            case "CustomsCase":
                CustomsCase customsCase = (CustomsCase) data;
                customsCase.setRiskLevel(com.certification.entity.common.CertNewsData.RiskLevel.LOW);
                // CustomsCase 没有remarks字段，使用keywords字段记录
                customsCaseRepository.save(customsCase);
                log.debug("降级海关案例 {} 为低风险: {}", customsCase.getId(), updateReason);
                break;
                
            default:
                log.warn("不支持的实体类型降级操作: {}", entityType);
        }
    }
}

