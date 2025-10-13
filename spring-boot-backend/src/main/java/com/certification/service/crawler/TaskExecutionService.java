package com.certification.service.crawler;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.entity.UnifiedTaskLog;
import com.certification.repository.UnifiedTaskConfigRepository;
import com.certification.repository.UnifiedTaskLogRepository;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 任务执行服务
 * 负责执行爬虫任务、记录日志、更新统计
 */
@Slf4j
@Service
public class TaskExecutionService {
    
    @Autowired
    private CrawlerRegistryService crawlerRegistry;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Autowired
    private UnifiedTaskConfigRepository taskConfigRepository;
    
    @Autowired
    private UnifiedTaskLogRepository taskLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 执行任务
     * 
     * @param config 任务配置
     * @param isManual 是否手动触发
     * @param triggeredBy 触发者
     * @return 执行结果
     */
    @Transactional
    public CrawlerResult executeTask(UnifiedTaskConfig config, boolean isManual, String triggeredBy) {
        log.info("开始执行任务: ID={}, 名称={}, 爬虫={}, 手动触发={}", 
            config.getId(), config.getTaskName(), config.getCrawlerName(), isManual);
        
        // 1. 创建执行日志
        UnifiedTaskLog taskLog = createTaskLog(config, isManual, triggeredBy);
        taskLog = taskLogRepository.save(taskLog);
        
        try {
            // 2. 检查爬虫是否启用
            if (!crawlerRegistry.isCrawlerEnabled(config.getCrawlerName())) {
                throw new IllegalStateException("爬虫已停用: " + config.getCrawlerName());
            }
            
            // 3. 参数校验
            Map<String, Object> params = parseParams(config.getParameters());
            if (!validateParams(config.getCrawlerName(), params)) {
                throw new IllegalArgumentException("参数验证失败");
            }
            
            // 4. 获取爬虫执行器
            ICrawlerExecutor executor = crawlerRegistry.getCrawler(config.getCrawlerName());
            if (executor == null) {
                throw new IllegalStateException("爬虫不存在: " + config.getCrawlerName());
            }
            
            // 5. 转换参数
            CrawlerParams crawlerParams = convertToCrawlerParams(params, config);
            crawlerParams.setTaskId(config.getId());
            crawlerParams.setTaskName(config.getTaskName());
            crawlerParams.setIsManual(isManual);
            crawlerParams.setTriggeredBy(triggeredBy);
            
            // 6. 执行爬取
            CrawlerResult result = executor.execute(crawlerParams);
            
            // 7. 更新日志
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus(result.getSuccess() ? "SUCCESS" : "FAILED");
            taskLog.setCrawledCount(result.getCrawledCount() != null ? result.getCrawledCount() : 0);
            taskLog.setSavedCount(result.getSavedCount() != null ? result.getSavedCount() : 0);
            taskLog.setSkippedCount(result.getSkippedCount() != null ? result.getSkippedCount() : 0);
            taskLog.setFailedCount(result.getFailedCount() != null ? result.getFailedCount() : 0);
            taskLog.setResultMessage(result.getMessage());
            taskLog.calculateDuration();
            taskLogRepository.save(taskLog);
            
            // 8. 更新任务配置统计
            config.updateExecutionStats(result.getSuccess(), result.getMessage());
            config.setLastExecutionTime(LocalDateTime.now());
            taskConfigRepository.save(config);
            
            log.info("任务执行完成: ID={}, 结果={}, 保存={}, 跳过={}", 
                config.getId(), result.getSuccess() ? "成功" : "失败", 
                result.getSavedCount(), result.getSkippedCount());
            
            return result;
            
        } catch (Exception e) {
            log.error("任务执行失败: ID={}, 原因: {}", config.getId(), e.getMessage(), e);
            
            // 更新失败日志
            taskLog.setStatus("FAILED");
            taskLog.setErrorMessage(e.getMessage());
            taskLog.setEndTime(LocalDateTime.now());
            taskLog.calculateDuration();
            taskLogRepository.save(taskLog);
            
            // 更新任务配置统计
            config.updateExecutionStats(false, "执行失败: " + e.getMessage());
            config.setLastExecutionTime(LocalDateTime.now());
            taskConfigRepository.save(config);
            
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 手动触发任务执行
     * 
     * @param taskId 任务ID
     * @param triggeredBy 触发者
     * @return 执行结果
     */
    public CrawlerResult triggerTask(Long taskId, String triggeredBy) {
        UnifiedTaskConfig config = taskConfigRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        
        return executeTask(config, true, triggeredBy);
    }
    
    /**
     * 重试失败的任务
     * 
     * @param logId 日志ID
     * @return 执行结果
     */
    public CrawlerResult retryFailedTask(Long logId) {
        UnifiedTaskLog log = taskLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("日志不存在: " + logId));
        
        UnifiedTaskConfig config = taskConfigRepository.findById(log.getTaskId())
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + log.getTaskId()));
        
        return executeTask(config, true, "RETRY");
    }
    
    /**
     * 创建任务日志
     */
    private UnifiedTaskLog createTaskLog(UnifiedTaskConfig config, boolean isManual, String triggeredBy) {
        UnifiedTaskLog log = new UnifiedTaskLog();
        log.setTaskId(config.getId());
        log.setBatchNo(UUID.randomUUID().toString().substring(0, 8));
        log.setTaskName(config.getTaskName());
        log.setCrawlerName(config.getCrawlerName());
        log.setCountryCode(config.getCountryCode());
        log.setStatus("RUNNING");
        log.setStartTime(LocalDateTime.now());
        log.setIsManual(isManual);
        log.setTriggeredBy(triggeredBy);
        log.setCrawlParams(config.getParameters());
        log.setKeywordsUsed(config.getKeywords());
        
        // 记录执行服务器信息
        try {
            InetAddress addr = InetAddress.getLocalHost();
            log.setExecutionServer(addr.getHostName());
            log.setExecutionIp(addr.getHostAddress());
        } catch (Exception e) {
            // 无法获取服务器信息，忽略
        }
        
        return log;
    }
    
    /**
     * 解析参数JSON
     */
    private Map<String, Object> parseParams(String parametersJson) {
        if (parametersJson == null || parametersJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(parametersJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析参数失败: {}", parametersJson, e);
            return new HashMap<>();
        }
    }
    
    /**
     * 验证参数
     */
    private boolean validateParams(String crawlerName, Map<String, Object> params) {
        return schemaRegistry.validateParams(crawlerName, params);
    }
    
    /**
     * 转换为爬虫参数
     */
    private CrawlerParams convertToCrawlerParams(Map<String, Object> params, UnifiedTaskConfig config) {
        CrawlerParams crawlerParams = new CrawlerParams();
        
        // 设置通用参数
        crawlerParams.setMaxRecords((Integer) params.getOrDefault("maxRecords", -1));
        crawlerParams.setBatchSize((Integer) params.getOrDefault("batchSize", 100));
        crawlerParams.setDateFrom((String) params.get("dateFrom"));
        crawlerParams.setDateTo((String) params.get("dateTo"));
        crawlerParams.setRecentDays((Integer) params.get("recentDays"));
        
        // 处理关键词参数
        if (config.getParamsVersion() != null && config.getParamsVersion().equals("v2")) {
            // V2模式：多字段关键词
            Map<String, List<String>> fieldKeywords = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 跳过通用参数
                if (key.equals("maxRecords") || key.equals("batchSize") || 
                    key.equals("dateFrom") || key.equals("dateTo") || key.equals("recentDays")) {
                    continue;
                }
                
                // 转换为List<String>
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>) value;
                    fieldKeywords.put(key, stringList);
                }
            }
            
            crawlerParams.setFieldKeywords(fieldKeywords);
            
        } else {
            // V1模式：单一关键词列表（向后兼容）
            if (config.getKeywords() != null && !config.getKeywords().trim().isEmpty()) {
                try {
                    List<String> keywords = objectMapper.readValue(
                        config.getKeywords(), 
                        new TypeReference<List<String>>() {}
                    );
                    crawlerParams.setKeywords(keywords);
                } catch (Exception e) {
                    log.warn("解析关键词失败，使用空列表", e);
                }
            }
        }
        
        return crawlerParams;
    }
}

