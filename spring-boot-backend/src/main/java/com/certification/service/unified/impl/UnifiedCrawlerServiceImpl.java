package com.certification.service.unified.impl;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.entity.UnifiedTaskLog;
import com.certification.repository.UnifiedTaskConfigRepository;
import com.certification.repository.UnifiedTaskLogRepository;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.CrawlerRegistryService;
import com.certification.service.crawler.ICrawlerExecutor;
import com.certification.service.crawler.schema.CrawlerSchema;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import com.certification.service.unified.UnifiedCrawlerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 统一爬虫服务实现
 * 整合V1和V2的所有功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedCrawlerServiceImpl implements UnifiedCrawlerService {
    
    private final CrawlerRegistryService crawlerRegistry;
    private final CrawlerSchemaRegistry schemaRegistry;
    private final UnifiedTaskConfigRepository taskConfigRepository;
    private final UnifiedTaskLogRepository taskLogRepository;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    public List<CrawlerInfo> getAllCrawlers() {
        log.info("获取所有爬虫信息");
        
        List<CrawlerInfo> crawlers = new ArrayList<>();
        
        // 从注册服务获取所有爬虫
        List<ICrawlerExecutor> allCrawlers = crawlerRegistry.getAllCrawlers();
        
        for (ICrawlerExecutor executor : allCrawlers) {
            String crawlerName = executor.getClass().getSimpleName();
            
            CrawlerInfo crawlerInfo = createCrawlerInfo(crawlerName, executor);
            crawlers.add(crawlerInfo);
        }
        
        log.info("获取到 {} 个爬虫", crawlers.size());
        return crawlers;
    }
    
    @Override
    public CrawlerInfo getCrawler(String crawlerName) {
        log.info("获取爬虫信息: {}", crawlerName);
        
        ICrawlerExecutor executor = crawlerRegistry.getCrawler(crawlerName);
        if (executor == null) {
            throw new IllegalArgumentException("爬虫不存在: " + crawlerName);
        }
        
        return createCrawlerInfo(crawlerName, executor);
    }
    
    @Override
    public CrawlerStatus getCrawlerStatus(String crawlerName) {
        log.info("获取爬虫状态: {}", crawlerName);
        
        // 查询最近的执行记录
        List<UnifiedTaskLog> recentLogs = taskLogRepository
            .findByCrawlerNameOrderByStartTimeDesc(crawlerName, PageRequest.of(0, 10));
        
        CrawlerStatus status = new CrawlerStatus();
        
        if (!recentLogs.isEmpty()) {
            UnifiedTaskLog latestLog = recentLogs.get(0);
            status.setStatus(latestLog.getStatus());
            status.setLastExecutionTime(latestLog.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            status.setLastExecutionResult(latestLog.getResultMessage());
        } else {
            status.setStatus("STOPPED");
        }
        
        // 计算统计信息
        int totalExecutions = recentLogs.size();
        int successCount = (int) recentLogs.stream().filter(log -> "SUCCESS".equals(log.getStatus())).count();
        int failureCount = totalExecutions - successCount;
        double successRate = totalExecutions > 0 ? (double) successCount / totalExecutions * 100 : 0.0;
        
        status.setTotalExecutions(totalExecutions);
        status.setSuccessCount(successCount);
        status.setFailureCount(failureCount);
        status.setSuccessRate(successRate);
        
        return status;
    }
    
    @Override
    @Transactional
    public CrawlerResult executeCrawler(String crawlerName, CrawlerParams params) {
        log.info("执行爬虫任务: {}, 参数: {}", crawlerName, params);
        
        ICrawlerExecutor executor = crawlerRegistry.getCrawler(crawlerName);
        if (executor == null) {
            throw new IllegalArgumentException("爬虫不存在: " + crawlerName);
        }
        
        try {
            CrawlerResult result = executor.execute(params);
            log.info("爬虫执行完成: {}, 结果: {}", crawlerName, result);
            return result;
        } catch (Exception e) {
            log.error("爬虫执行失败: {}", crawlerName, e);
            throw new RuntimeException("爬虫执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public BatchResult batchExecute(List<String> crawlerNames, CrawlerParams params) {
        log.info("批量执行爬虫任务: {}, 参数: {}", crawlerNames, params);
        
        BatchResult batchResult = new BatchResult();
        batchResult.setTotalCount(crawlerNames.size());
        batchResult.setSuccessCount(0);
        batchResult.setFailureCount(0);
        batchResult.setResults(new ArrayList<>());
        
        long startTime = System.currentTimeMillis();
        
        // 并行执行所有爬虫
        List<CompletableFuture<CrawlerResult>> futures = crawlerNames.stream()
            .map(crawlerName -> CompletableFuture.supplyAsync(() -> {
                try {
                    return executeCrawler(crawlerName, params);
                } catch (Exception e) {
                    log.error("爬虫执行失败: {}", crawlerName, e);
                    return CrawlerResult.failure(e.getMessage());
                }
            }, executorService))
            .collect(Collectors.toList());
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 收集结果
        for (CompletableFuture<CrawlerResult> future : futures) {
            try {
                CrawlerResult result = future.get();
                batchResult.getResults().add(result);
                if (result != null && result.getSavedCount() > 0) {
                    batchResult.setSuccessCount(batchResult.getSuccessCount() + 1);
                } else {
                    batchResult.setFailureCount(batchResult.getFailureCount() + 1);
                }
            } catch (Exception e) {
                log.error("获取批量执行结果失败", e);
                batchResult.setFailureCount(batchResult.getFailureCount() + 1);
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        batchResult.setExecutionTime(executionTime);
        batchResult.setSuccess(batchResult.getFailureCount() == 0);
        batchResult.setMessage(String.format("批量执行完成: 成功%d个, 失败%d个", 
            batchResult.getSuccessCount(), batchResult.getFailureCount()));
        
        log.info("批量执行完成: {}", batchResult.getMessage());
        return batchResult;
    }
    
    @Override
    @Transactional
    public BatchResult batchTest(List<String> crawlerNames) {
        log.info("批量测试爬虫: {}", crawlerNames);
        
        // 为测试创建默认参数
        CrawlerParams testParams = new CrawlerParams();
        testParams.setMaxRecords(10); // 测试时限制记录数
        
        return batchExecute(crawlerNames, testParams);
    }
    
    @Override
    public CrawlerSchema getCrawlerSchema(String crawlerName) {
        log.info("获取爬虫Schema: {}", crawlerName);
        return schemaRegistry.getSchema(crawlerName);
    }
    
    @Override
    public Map<String, CrawlerSchema> getAllCrawlerSchemas() {
        log.info("获取所有爬虫Schema");
        
        List<CrawlerSchema> schemas = schemaRegistry.getAllSchemas();
        Map<String, CrawlerSchema> schemaMap = new HashMap<>();
        
        for (CrawlerSchema schema : schemas) {
            schemaMap.put(schema.getCrawlerName(), schema);
        }
        
        return schemaMap;
    }
    
    @Override
    @Transactional
    public UnifiedTaskConfig createTask(TaskConfigRequest request) {
        log.info("创建任务: {}", request);
        
        UnifiedTaskConfig task = new UnifiedTaskConfig();
        task.setTaskName(request.getTaskName());
        task.setCrawlerName(request.getCrawlerName());
        task.setCountryCode(request.getCountryCode());
        task.setTaskType(request.getTaskType());
        task.setParamsVersion(request.getParamsVersion());
        task.setParameters(request.getParameters());
        task.setCronExpression(request.getCronExpression());
        task.setDescription(request.getDescription());
        task.setEnabled(request.isEnabled());
        task.setPriority(request.getPriority());
        task.setCreatedAt(LocalDateTime.now());
        
        task = taskConfigRepository.save(task);
        log.info("任务创建成功: ID={}", task.getId());
        
        return task;
    }
    
    @Override
    public List<UnifiedTaskConfig> getTasks(TaskQueryRequest request) {
        log.info("查询任务列表: {}", request);
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), 
            Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<UnifiedTaskConfig> page;
        
        // 构建查询条件
        if (request.getCountryCode() != null || request.getCrawlerName() != null || 
            request.getTaskType() != null || request.getEnabled() != null) {
            
            // 这里需要实现自定义查询方法
            page = taskConfigRepository.findAll(pageable);
        } else {
            page = taskConfigRepository.findAll(pageable);
        }
        
        return page.getContent();
    }
    
    @Override
    @Transactional
    public TaskExecutionResult executeTask(Long taskId) {
        log.info("执行任务: {}", taskId);
        
        UnifiedTaskConfig task = taskConfigRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        
        // 创建执行日志
        UnifiedTaskLog logEntry = new UnifiedTaskLog();
        logEntry.setTaskId(taskId);
        logEntry.setBatchNo("TASK_" + taskId + "_" + System.currentTimeMillis());
        logEntry.setTaskName(task.getTaskName());
        logEntry.setCrawlerName(task.getCrawlerName());
        logEntry.setCountryCode(task.getCountryCode());
        logEntry.setStatus("RUNNING");
        logEntry.setStartTime(LocalDateTime.now());
        logEntry.setIsManual(true);
        logEntry.setTriggeredBy("SYSTEM");
        
        logEntry = taskLogRepository.save(logEntry);
        
        TaskExecutionResult result = new TaskExecutionResult();
        result.setExecutionId(logEntry.getBatchNo());
        result.setStartTime(logEntry.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        
        try {
            // 解析参数：如果任务没有参数，自动加载预设
            CrawlerParams params = parseTaskParametersWithPreset(task);
            
            // 执行爬虫
            CrawlerResult crawlerResult = executeCrawler(task.getCrawlerName(), params);
            
            // 更新执行日志
            logEntry.setStatus("SUCCESS");
            logEntry.setCrawledCount(crawlerResult.getCrawledCount());
            logEntry.setSavedCount(crawlerResult.getSavedCount());
            logEntry.setSkippedCount(crawlerResult.getSkippedCount());
            logEntry.setFailedCount(crawlerResult.getFailedCount());
            logEntry.setResultMessage(crawlerResult.getMessage());
            logEntry.setExecutionResult("SUCCESS", crawlerResult.getMessage(), null);
            
            // 更新任务统计
            task.updateExecutionStats(true, crawlerResult.getMessage());
            taskConfigRepository.save(task);
            
            // 设置结果
            result.setSuccess(true);
            result.setMessage("任务执行成功");
            result.setCrawledCount(crawlerResult.getCrawledCount());
            result.setSavedCount(crawlerResult.getSavedCount());
            result.setSkippedCount(crawlerResult.getSkippedCount());
            result.setFailedCount(crawlerResult.getFailedCount());
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            
            // 更新执行日志
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(e.getMessage());
            logEntry.setExecutionResult("FAILED", null, e.getMessage());
            
            // 更新任务统计
            task.updateExecutionStats(false, e.getMessage());
            taskConfigRepository.save(task);
            
            // 设置结果
            result.setSuccess(false);
            result.setMessage("任务执行失败: " + e.getMessage());
        } finally {
            taskLogRepository.save(logEntry);
            result.setEndTime(System.currentTimeMillis());
        }
        
        return result;
    }
    
    @Override
    public List<TaskExecutionLog> getTaskHistory(Long taskId) {
        log.info("获取任务执行历史: {}", taskId);
        
        List<UnifiedTaskLog> logs = taskLogRepository.findByTaskIdOrderByStartTimeDesc(taskId);
        
        return logs.stream().map(this::convertToTaskExecutionLog).collect(Collectors.toList());
    }
    
    @Override
    public SystemStatistics getSystemStatistics() {
        log.info("获取系统统计信息");
        
        SystemStatistics stats = new SystemStatistics();
        
        // 爬虫统计
        List<ICrawlerExecutor> allCrawlers = crawlerRegistry.getAllCrawlers();
        stats.setTotalCrawlers(allCrawlers.size());
        
        // 任务统计
        List<UnifiedTaskConfig> allTasks = taskConfigRepository.findAll();
        stats.setTotalTasks(allTasks.size());
        stats.setEnabledTasks((int) allTasks.stream().filter(task -> Boolean.TRUE.equals(task.getEnabled())).count());
        
        // 运行中任务统计
        List<UnifiedTaskLog> runningTasks = taskLogRepository.findByStatus("RUNNING");
        stats.setRunningTasks(runningTasks.size());
        
        // 按国家统计爬虫
        Map<String, Integer> crawlerStatsByCountry = allCrawlers.stream()
            .collect(Collectors.groupingBy(
                executor -> getCountryCodeFromCrawlerName(executor.getClass().getSimpleName()),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
        stats.setCrawlerStatsByCountry(crawlerStatsByCountry);
        
        // 按类型统计任务
        Map<String, Integer> taskStatsByType = allTasks.stream()
            .collect(Collectors.groupingBy(
                UnifiedTaskConfig::getTaskType,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
        stats.setTaskStatsByType(taskStatsByType);
        
        // 整体成功率
        long totalExecutions = allTasks.stream().mapToInt(task -> task.getExecutionCount() != null ? task.getExecutionCount() : 0).sum();
        long totalSuccesses = allTasks.stream().mapToInt(task -> task.getSuccessCount() != null ? task.getSuccessCount() : 0).sum();
        double overallSuccessRate = totalExecutions > 0 ? (double) totalSuccesses / totalExecutions * 100 : 0.0;
        stats.setOverallSuccessRate(overallSuccessRate);
        
        return stats;
    }
    
    /**
     * 创建爬虫信息
     */
    private CrawlerInfo createCrawlerInfo(String crawlerName, ICrawlerExecutor executor) {
        String displayName = getDisplayNameFromCrawlerName(crawlerName);
        String countryCode = getCountryCodeFromCrawlerName(crawlerName);
        String crawlerType = getCrawlerTypeFromCrawlerName(crawlerName);
        String description = getDescriptionFromCrawlerName(crawlerName);
        
        CrawlerInfo crawlerInfo = new CrawlerInfo(crawlerName, displayName, countryCode, crawlerType, description, "v2");
        
        // 获取状态
        CrawlerStatus status = getCrawlerStatus(crawlerName);
        crawlerInfo.setStatus(status);
        
        // 获取Schema
        CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
        crawlerInfo.setSchema(schema);
        
        return crawlerInfo;
    }
    
    /**
     * 解析任务参数（带预设加载）
     */
    private CrawlerParams parseTaskParametersWithPreset(UnifiedTaskConfig task) {
        try {
            String parametersToUse = task.getParameters();
            
            // 如果任务没有参数或参数为空，加载预设
            if (parametersToUse == null || parametersToUse.trim().isEmpty() || "{}".equals(parametersToUse.trim())) {
                log.info("任务{}没有参数，加载预设参数", task.getId());
                UnifiedTaskConfig preset = getPreset(task.getCrawlerName());
                parametersToUse = preset.getParameters();
                log.info("已加载预设参数: {}", preset.getTaskName());
            }
            
            // 创建临时任务对象用于解析
            UnifiedTaskConfig tempTask = new UnifiedTaskConfig();
            tempTask.setParameters(parametersToUse);
            tempTask.setParamsVersion(task.getParamsVersion());
            tempTask.setKeywords(task.getKeywords());
            
            return parseTaskParameters(tempTask);
            
        } catch (Exception e) {
            log.error("解析任务参数（带预设）失败: {}", task.getId(), e);
            throw new RuntimeException("解析任务参数失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析任务参数
     */
    private CrawlerParams parseTaskParameters(UnifiedTaskConfig task) {
        CrawlerParams params = new CrawlerParams();
        
        try {
            if ("v2".equals(task.getParamsVersion())) {
                // V2参数格式
                Map<String, Object> paramMap = objectMapper.readValue(task.getParameters(), new TypeReference<Map<String, Object>>() {});
                
                // 处理fieldKeywords
                if (paramMap.containsKey("fieldKeywords")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> fieldKeywords = (Map<String, List<String>>) paramMap.get("fieldKeywords");
                    params.setFieldKeywords(fieldKeywords);
                }
                
                // 处理maxRecords
                if (paramMap.containsKey("maxRecords")) {
                    params.setMaxRecords((Integer) paramMap.get("maxRecords"));
                }
                
            } else {
                // V1参数格式
                if (task.getKeywords() != null && !task.getKeywords().isEmpty()) {
                    List<String> keywords = objectMapper.readValue(task.getKeywords(), new TypeReference<List<String>>() {});
                    params.setKeywords(keywords);
                }
            }
            
        } catch (Exception e) {
            log.error("解析任务参数失败: {}", task.getId(), e);
            throw new RuntimeException("解析任务参数失败: " + e.getMessage(), e);
        }
        
        return params;
    }
    
    /**
     * 转换为任务执行日志
     */
    private TaskExecutionLog convertToTaskExecutionLog(UnifiedTaskLog log) {
        TaskExecutionLog executionLog = new TaskExecutionLog();
        executionLog.setId(log.getId());
        executionLog.setTaskId(log.getTaskId());
        executionLog.setExecutionId(log.getBatchNo());
        executionLog.setStatus(log.getStatus());
        executionLog.setStartTime(log.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        if (log.getEndTime() != null) {
            executionLog.setEndTime(log.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        executionLog.setCrawledCount(log.getCrawledCount());
        executionLog.setSavedCount(log.getSavedCount());
        executionLog.setResult(log.getResultMessage());
        executionLog.setError(log.getErrorMessage());
        
        return executionLog;
    }
    
    /**
     * 从爬虫名称获取显示名称
     */
    private String getDisplayNameFromCrawlerName(String crawlerName) {
        switch (crawlerName) {
            case "US_510K": return "美国510K设备数据爬虫";
            case "US_Registration": return "美国设备注册数据爬虫";
            case "US_Recall": return "美国设备召回数据爬虫";
            case "US_Event": return "美国设备事件数据爬虫";
            case "US_Guidance": return "美国指导文档爬虫";
            case "US_CustomsCase": return "美国海关案例爬虫";
            case "EU_Recall": return "欧盟设备召回数据爬虫";
            case "EU_Registration": return "欧盟设备注册数据爬虫";
            case "EU_Guidance": return "欧盟指导文档爬虫";
            case "EU_CustomsCase": return "欧盟海关案例爬虫";
            case "KR_Recall": return "韩国设备召回数据爬虫";
            default: return crawlerName;
        }
    }
    
    /**
     * 从爬虫名称获取国家代码
     */
    private String getCountryCodeFromCrawlerName(String crawlerName) {
        if (crawlerName.startsWith("US_")) return "US";
        if (crawlerName.startsWith("EU_")) return "EU";
        if (crawlerName.startsWith("KR_")) return "KR";
        if (crawlerName.startsWith("CN_")) return "CN";
        if (crawlerName.startsWith("JP_")) return "JP";
        return "UNKNOWN";
    }
    
    /**
     * 从爬虫名称获取爬虫类型
     */
    private String getCrawlerTypeFromCrawlerName(String crawlerName) {
        if (crawlerName.contains("510K")) return "510K";
        if (crawlerName.contains("Registration")) return "REGISTRATION";
        if (crawlerName.contains("Recall")) return "RECALL";
        if (crawlerName.contains("Event")) return "EVENT";
        if (crawlerName.contains("Guidance")) return "GUIDANCE";
        if (crawlerName.contains("CustomsCase")) return "CUSTOMS";
        return "UNKNOWN";
    }
    
    /**
     * 从爬虫名称获取描述
     */
    private String getDescriptionFromCrawlerName(String crawlerName) {
        return getDisplayNameFromCrawlerName(crawlerName);
    }
    
    // ==================== 预设管理方法 ====================
    
    @Override
    public UnifiedTaskConfig getPreset(String crawlerName) {
        log.info("获取爬虫预设: {}", crawlerName);
        
        return taskConfigRepository
            .findByCrawlerNameAndTaskType(crawlerName, "PRESET")
            .orElseGet(() -> createDefaultPreset(crawlerName));
    }
    
    @Override
    @Transactional
    public UnifiedTaskConfig updatePreset(String crawlerName, String parameters) {
        log.info("更新爬虫预设: {}", crawlerName);
        
        UnifiedTaskConfig preset = getPreset(crawlerName);
        preset.setParameters(parameters);
        preset.setParamsVersion("v2");
        preset.setUpdatedAt(LocalDateTime.now());
        
        UnifiedTaskConfig saved = taskConfigRepository.save(preset);
        log.info("预设更新成功: {}", crawlerName);
        
        return saved;
    }
    
    @Override
    public boolean validatePreset(String crawlerName, Map<String, Object> parameters) {
        log.info("验证预设参数: {}", crawlerName);
        
        try {
            // 获取爬虫Schema
            CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
            if (schema == null) {
                log.warn("未找到爬虫Schema: {}", crawlerName);
                return false;
            }
            
            // 验证必填字段
            // 这里可以根据Schema的字段定义进行更详细的验证
            // 目前简单验证参数是否可序列化为JSON
            String jsonParams = objectMapper.writeValueAsString(parameters);
            objectMapper.readValue(jsonParams, new TypeReference<Map<String, Object>>() {});
            
            return true;
            
        } catch (Exception e) {
            log.error("预设参数验证失败: {}", crawlerName, e);
            return false;
        }
    }
    
    /**
     * 创建默认预设
     */
    private UnifiedTaskConfig createDefaultPreset(String crawlerName) {
        log.info("创建默认预设: {}", crawlerName);
        
        UnifiedTaskConfig preset = new UnifiedTaskConfig();
        preset.setTaskName(crawlerName + "_preset");
        preset.setCrawlerName(crawlerName);
        preset.setCountryCode(getCountryCodeFromCrawlerName(crawlerName));
        preset.setTaskType("PRESET");
        preset.setParamsVersion("v2");
        preset.setEnabled(true);
        preset.setPriority(5);
        
        // 根据Schema生成默认参数JSON
        String defaultParams = generateDefaultParams(crawlerName);
        preset.setParameters(defaultParams);
        preset.setDescription(getDisplayNameFromCrawlerName(crawlerName) + "参数预设");
        
        return taskConfigRepository.save(preset);
    }
    
    /**
     * 根据爬虫Schema生成默认参数JSON
     */
    private String generateDefaultParams(String crawlerName) {
        try {
            CrawlerSchema schema = schemaRegistry.getSchema(crawlerName);
            Map<String, Object> defaultParams = new HashMap<>();
            
            // 添加fieldKeywords（如果爬虫支持）
            if (schema != null && schema.getFields() != null && !schema.getFields().isEmpty()) {
                Map<String, List<String>> fieldKeywords = new HashMap<>();
                for (var field : schema.getFields()) {
                    fieldKeywords.put(field.getFieldName(), new ArrayList<>());
                }
                defaultParams.put("fieldKeywords", fieldKeywords);
            }
            
            // 添加通用参数
            defaultParams.put("maxRecords", 100);
            defaultParams.put("batchSize", 50);
            defaultParams.put("dateFrom", "");
            defaultParams.put("dateTo", "");
            
            return objectMapper.writeValueAsString(defaultParams);
            
        } catch (Exception e) {
            log.error("生成默认参数失败: {}", crawlerName, e);
            return "{\"maxRecords\":100,\"batchSize\":50}";
        }
    }
}
