package com.certification.service.ai;

import com.certification.analysis.CertNewsanalysis;
import com.certification.entity.common.AIJudgeTask;
import com.certification.entity.common.CertNewsData;
import com.certification.repository.AIJudgeTaskRepository;
import com.certification.repository.CrawlerDataRepository;
import com.certification.dto.ai.CertNewsClassificationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 异步AI判断服务
 * 支持大批量数据的异步处理
 */
@Slf4j
@Service
public class AsyncAIJudgeService {
    
    @Autowired
    private CertNewsAIJudgeService certNewsAIJudgeService;
    
    @Autowired
    private CrawlerDataRepository crawlerDataRepository;
    
    @Autowired
    private AIJudgeTaskRepository taskRepository;
    
    @Autowired
    private CertNewsanalysis certNewsanalysis;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 异步执行认证新闻AI判断
     * 
     * @param taskId 任务ID
     * @param riskLevel 风险等级
     * @param sourceName 数据源
     * @param limit 限制数量（为null表示处理全部）
     */
    @Async("aiJudgeExecutor")
    public void executeAsyncJudge(
        String taskId,
        String riskLevel,
        String sourceName,
        Integer limit
    ) {
        log.info("开始异步AI判断任务: taskId={}, riskLevel={}, sourceName={}, limit={}", 
            taskId, riskLevel, sourceName, limit);
        
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
            
            // 获取待处理数据
            List<CertNewsData> dataList = getDataForJudge(riskLevel, sourceName, limit);
            task.setTotalCount(dataList.size());
            taskRepository.save(task);
            
            log.info("找到 {} 条数据需要AI判断", dataList.size());
            
            if (dataList.isEmpty()) {
                task.setStatus("COMPLETED");
                task.setEndTime(LocalDateTime.now());
                taskRepository.save(task);
                return;
            }
            
            // 批次处理
            int batchSize = 100;
            Set<String> allExtractedKeywords = new HashSet<>();
            
            for (int i = 0; i < dataList.size(); i++) {
                // 检查任务是否被取消
                task = taskRepository.findByTaskId(taskId).orElse(null);
                if (task == null || "CANCELLED".equals(task.getStatus())) {
                    log.info("任务已取消: {}", taskId);
                    return;
                }
                
                CertNewsData data = dataList.get(i);
                
                try {
                    // AI判断单条数据
                    Map<String, Object> newsDataMap = buildNewsDataMap(data);
                    CertNewsClassificationResult aiResult = certNewsAIJudgeService.classifyCertificationNews(newsDataMap);
                    
                    // 构建remark
                    StringBuilder remark = new StringBuilder();
                    remark.append("AI判断: ").append(aiResult.isRelatedToCertification() ? "相关" : "不相关");
                    remark.append(", 置信度: ").append(String.format("%.1f%%", aiResult.getConfidence() * 100));
                    remark.append(", 理由: ").append(aiResult.getReason());
                    
                    if (aiResult.isRelatedToCertification()) {
                        // 相关 - 设置为高风险
                        data.setRiskLevel(CertNewsData.RiskLevel.HIGH);
                        data.setRelated(true);
                        
                        // 提取关键词
                        List<String> keywords = aiResult.getExtractedKeywords();
                        if (keywords != null && !keywords.isEmpty()) {
                            data.setMatchedKeywords(String.join(",", keywords));
                            allExtractedKeywords.addAll(keywords);
                            remark.append(", 提取关键词: ").append(keywords);
                            task.setKeywordCount(allExtractedKeywords.size());
                        }
                        
                        task.setRelatedCount(task.getRelatedCount() + 1);
                    } else {
                        // 不相关 - 设置为低风险
                        data.setRiskLevel(CertNewsData.RiskLevel.LOW);
                        data.setRelated(false);
                        data.setMatchedKeywords(null);
                        task.setUnrelatedCount(task.getUnrelatedCount() + 1);
                    }
                    
                    data.setRemarks(remark.toString());
                    data.setStatus(CertNewsData.DataStatus.PROCESSED);
                    data.setIsProcessed(true);
                    data.setProcessedTime(LocalDateTime.now());
                    crawlerDataRepository.save(data);
                    
                } catch (Exception e) {
                    log.error("处理数据失败: id={}, error={}", data.getId(), e.getMessage());
                    task.setFailedCount(task.getFailedCount() + 1);
                }
                
                // 更新进度
                task.setProcessedCount(i + 1);
                
                // 每100条或最后一条保存一次任务状态
                if ((i + 1) % batchSize == 0 || i == dataList.size() - 1) {
                    taskRepository.save(task);
                    log.info("任务进度: {}/{} ({}%)", task.getProcessedCount(), task.getTotalCount(), task.getProgress());
                    
                    // 批次休息，避免API限流
                    if (i < dataList.size() - 1) {
                        Thread.sleep(1000);
                    }
                }
            }
            
            // 更新认证关键词文件
            if (!allExtractedKeywords.isEmpty()) {
                certNewsanalysis.updateCertificationKeywords(new ArrayList<>(allExtractedKeywords));
                log.info("更新了 {} 个认证关键词到文件", allExtractedKeywords.size());
            }
            
            // 任务完成
            task.setStatus("COMPLETED");
            task.setEndTime(LocalDateTime.now());
            taskRepository.save(task);
            
            log.info("异步AI判断任务完成: taskId={}, 总计={}, 相关={}, 不相关={}, 失败={}, 关键词={}", 
                taskId, task.getTotalCount(), task.getRelatedCount(), 
                task.getUnrelatedCount(), task.getFailedCount(), task.getKeywordCount());
            
        } catch (Exception e) {
            log.error("异步AI判断任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            taskRepository.save(task);
        }
    }
    
    /**
     * 获取待判断数据
     */
    private List<CertNewsData> getDataForJudge(String riskLevel, String sourceName, Integer limit) {
        List<CertNewsData> dataList = new ArrayList<>();
        
        try {
            // 解析风险等级
            CertNewsData.RiskLevel targetRiskLevel = CertNewsData.RiskLevel.MEDIUM;
            if (riskLevel != null && !riskLevel.trim().isEmpty()) {
                try {
                    targetRiskLevel = CertNewsData.RiskLevel.valueOf(riskLevel.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("无效的风险等级: {}, 使用默认中风险", riskLevel);
                }
            }
            
            // 查询数据
            List<CertNewsData> allData = crawlerDataRepository.findByRiskLevelAndDeleted(targetRiskLevel, 0);
            
            // 应用数据源筛选
            for (CertNewsData data : allData) {
                if (sourceName != null && !sourceName.isEmpty() && !sourceName.equals(data.getSourceName())) {
                    continue;
                }
                dataList.add(data);
                
                // 如果有限制数量，达到后停止
                if (limit != null && limit > 0 && dataList.size() >= limit) {
                    break;
                }
            }
            
        } catch (Exception e) {
            log.error("获取待判断数据失败: {}", e.getMessage(), e);
        }
        
        return dataList;
    }
    
    /**
     * 构建新闻数据Map
     */
    private Map<String, Object> buildNewsDataMap(CertNewsData data) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", data.getId());
        dataMap.put("title", data.getTitle() != null ? data.getTitle() : "");
        dataMap.put("content", data.getContent() != null ? data.getContent() : "");
        dataMap.put("summary", data.getSummary() != null ? data.getSummary() : "");
        dataMap.put("country", data.getCountry() != null ? data.getCountry() : "");
        dataMap.put("sourceName", data.getSourceName() != null ? data.getSourceName() : "");
        return dataMap;
    }
}

