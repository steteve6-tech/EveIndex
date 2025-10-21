package com.certification.service.unified;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.schema.CrawlerSchema;

import java.util.List;
import java.util.Map;

/**
 * 统一爬虫服务接口
 * 整合V1和V2的所有功能
 */
public interface UnifiedCrawlerService {
    
    /**
     * 获取所有爬虫信息
     */
    List<CrawlerInfo> getAllCrawlers();
    
    /**
     * 获取指定爬虫信息
     */
    CrawlerInfo getCrawler(String crawlerName);
    
    /**
     * 获取爬虫状态
     */
    CrawlerStatus getCrawlerStatus(String crawlerName);
    
    /**
     * 执行单个爬虫任务
     */
    CrawlerResult executeCrawler(String crawlerName, CrawlerParams params);
    
    /**
     * 批量执行爬虫任务
     */
    BatchResult batchExecute(List<String> crawlerNames, CrawlerParams params);
    
    /**
     * 批量测试爬虫
     */
    BatchResult batchTest(List<String> crawlerNames);
    
    /**
     * 获取爬虫Schema
     */
    CrawlerSchema getCrawlerSchema(String crawlerName);
    
    /**
     * 获取所有爬虫Schema
     */
    Map<String, CrawlerSchema> getAllCrawlerSchemas();
    
    /**
     * 创建统一任务
     */
    UnifiedTaskConfig createTask(TaskConfigRequest request);
    
    /**
     * 获取任务列表
     */
    List<UnifiedTaskConfig> getTasks(TaskQueryRequest request);
    
    /**
     * 执行任务
     */
    TaskExecutionResult executeTask(Long taskId);
    
    /**
     * 获取任务执行历史
     */
    List<TaskExecutionLog> getTaskHistory(Long taskId);
    
    /**
     * 获取系统统计信息
     */
    SystemStatistics getSystemStatistics();
    
    /**
     * 获取爬虫参数预设
     */
    UnifiedTaskConfig getPreset(String crawlerName);
    
    /**
     * 更新爬虫参数预设
     */
    UnifiedTaskConfig updatePreset(String crawlerName, String parameters);
    
    /**
     * 验证预设参数
     */
    boolean validatePreset(String crawlerName, Map<String, Object> parameters);
    
    /**
     * 爬虫信息
     */
    class CrawlerInfo {
        private String crawlerName;
        private String displayName;
        private String countryCode;
        private String crawlerType;
        private String description;
        private boolean available;
        private CrawlerStatus status;
        private CrawlerSchema schema;

        // 构造器、getter、setter
        public CrawlerInfo() {}

        public CrawlerInfo(String crawlerName, String displayName, String countryCode,
                          String crawlerType, String description) {
            this.crawlerName = crawlerName;
            this.displayName = displayName;
            this.countryCode = countryCode;
            this.crawlerType = crawlerType;
            this.description = description;
            this.available = true;
            this.status = new CrawlerStatus();
        }
        
        // Getters and Setters
        public String getCrawlerName() { return crawlerName; }
        public void setCrawlerName(String crawlerName) { this.crawlerName = crawlerName; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getCrawlerType() { return crawlerType; }
        public void setCrawlerType(String crawlerType) { this.crawlerType = crawlerType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public CrawlerStatus getStatus() { return status; }
        public void setStatus(CrawlerStatus status) { this.status = status; }
        
        public CrawlerSchema getSchema() { return schema; }
        public void setSchema(CrawlerSchema schema) { this.schema = schema; }
    }
    
    /**
     * 爬虫状态
     */
    class CrawlerStatus {
        private String status; // RUNNING, STOPPED, ERROR
        private long lastExecutionTime;
        private String lastExecutionResult;
        private int totalExecutions;
        private int successCount;
        private int failureCount;
        private double successRate;
        
        // 构造器、getter、setter
        public CrawlerStatus() {
            this.status = "STOPPED";
            this.totalExecutions = 0;
            this.successCount = 0;
            this.failureCount = 0;
            this.successRate = 0.0;
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(long lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
        
        public String getLastExecutionResult() { return lastExecutionResult; }
        public void setLastExecutionResult(String lastExecutionResult) { this.lastExecutionResult = lastExecutionResult; }
        
        public int getTotalExecutions() { return totalExecutions; }
        public void setTotalExecutions(int totalExecutions) { this.totalExecutions = totalExecutions; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }
    
    /**
     * 批量执行结果
     */
    class BatchResult {
        private boolean success;
        private String message;
        private int totalCount;
        private int successCount;
        private int failureCount;
        private List<CrawlerResult> results;
        private long executionTime;
        
        // 构造器、getter、setter
        public BatchResult() {}
        
        public BatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public List<CrawlerResult> getResults() { return results; }
        public void setResults(List<CrawlerResult> results) { this.results = results; }
        
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
    
    /**
     * 任务配置请求
     */
    class TaskConfigRequest {
        private String taskName;
        private String crawlerName;
        private String countryCode;
        private String taskType;
        private String parameters; // JSON格式
        private String cronExpression;
        private String description;
        private boolean enabled;
        private int priority;
        
        // 构造器、getter、setter
        public TaskConfigRequest() {}
        
        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        
        public String getCrawlerName() { return crawlerName; }
        public void setCrawlerName(String crawlerName) { this.crawlerName = crawlerName; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
        
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
    
    /**
     * 任务查询请求
     */
    class TaskQueryRequest {
        private String countryCode;
        private String crawlerName;
        private String taskType;
        private Boolean enabled;
        private int page;
        private int size;
        
        // 构造器、getter、setter
        public TaskQueryRequest() {
            this.page = 0;
            this.size = 20;
        }
        
        // Getters and Setters
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getCrawlerName() { return crawlerName; }
        public void setCrawlerName(String crawlerName) { this.crawlerName = crawlerName; }
        
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
    
    /**
     * 任务执行结果
     */
    class TaskExecutionResult {
        private boolean success;
        private String message;
        private String executionId;
        private long startTime;
        private long endTime;
        private int crawledCount;
        private int savedCount;
        private int skippedCount;
        private int failedCount;
        
        // 构造器、getter、setter
        public TaskExecutionResult() {}
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public int getCrawledCount() { return crawledCount; }
        public void setCrawledCount(int crawledCount) { this.crawledCount = crawledCount; }
        
        public int getSavedCount() { return savedCount; }
        public void setSavedCount(int savedCount) { this.savedCount = savedCount; }
        
        public int getSkippedCount() { return skippedCount; }
        public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
        
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    }
    
    /**
     * 任务执行日志
     */
    class TaskExecutionLog {
        private Long id;
        private Long taskId;
        private String executionId;
        private String status;
        private long startTime;
        private long endTime;
        private int crawledCount;
        private int savedCount;
        private String result;
        private String error;
        
        // 构造器、getter、setter
        public TaskExecutionLog() {}
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public int getCrawledCount() { return crawledCount; }
        public void setCrawledCount(int crawledCount) { this.crawledCount = crawledCount; }
        
        public int getSavedCount() { return savedCount; }
        public void setSavedCount(int savedCount) { this.savedCount = savedCount; }
        
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
    
    /**
     * 系统统计信息
     */
    class SystemStatistics {
        private int totalCrawlers;
        private int runningCrawlers;
        private int totalTasks;
        private int enabledTasks;
        private int runningTasks;
        private Map<String, Integer> crawlerStatsByCountry;
        private Map<String, Integer> taskStatsByType;
        private double overallSuccessRate;
        private long totalDataCount;
        
        // 构造器、getter、setter
        public SystemStatistics() {}
        
        // Getters and Setters
        public int getTotalCrawlers() { return totalCrawlers; }
        public void setTotalCrawlers(int totalCrawlers) { this.totalCrawlers = totalCrawlers; }
        
        public int getRunningCrawlers() { return runningCrawlers; }
        public void setRunningCrawlers(int runningCrawlers) { this.runningCrawlers = runningCrawlers; }
        
        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        
        public int getEnabledTasks() { return enabledTasks; }
        public void setEnabledTasks(int enabledTasks) { this.enabledTasks = enabledTasks; }
        
        public int getRunningTasks() { return runningTasks; }
        public void setRunningTasks(int runningTasks) { this.runningTasks = runningTasks; }
        
        public Map<String, Integer> getCrawlerStatsByCountry() { return crawlerStatsByCountry; }
        public void setCrawlerStatsByCountry(Map<String, Integer> crawlerStatsByCountry) { 
            this.crawlerStatsByCountry = crawlerStatsByCountry; 
        }
        
        public Map<String, Integer> getTaskStatsByType() { return taskStatsByType; }
        public void setTaskStatsByType(Map<String, Integer> taskStatsByType) { 
            this.taskStatsByType = taskStatsByType; 
        }
        
        public double getOverallSuccessRate() { return overallSuccessRate; }
        public void setOverallSuccessRate(double overallSuccessRate) { 
            this.overallSuccessRate = overallSuccessRate; 
        }
        
        public long getTotalDataCount() { return totalDataCount; }
        public void setTotalDataCount(long totalDataCount) { this.totalDataCount = totalDataCount; }
    }
}
