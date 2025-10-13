package com.certification.service.crawler;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.entity.UnifiedTaskLog;
import com.certification.repository.UnifiedTaskConfigRepository;
import com.certification.repository.UnifiedTaskLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 爬虫监控统计服务
 */
@Slf4j
@Service
public class CrawlerMonitorService {
    
    @Autowired
    private UnifiedTaskConfigRepository taskConfigRepository;
    
    @Autowired
    private UnifiedTaskLogRepository taskLogRepository;
    
    @Autowired
    private DynamicTaskSchedulerService schedulerService;
    
    /**
     * 获取运行中的任务
     * 
     * @return 运行中任务列表
     */
    public List<UnifiedTaskLog> getRunningTasks() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return taskLogRepository.findByStatusAndStartTimeAfter("RUNNING", oneHourAgo);
    }
    
    /**
     * 获取执行历史
     * 
     * @param crawlerName 爬虫名称（可选）
     * @param status 状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 执行历史
     */
    public Page<UnifiedTaskLog> getExecutionHistory(
            String crawlerName, 
            String status, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Pageable pageable) {
        
        if (startTime != null && endTime != null) {
            return taskLogRepository.findByStartTimeBetween(startTime, endTime, pageable);
        } else if (crawlerName != null) {
            // 使用Page查询方法
            return taskLogRepository.findByTaskIdOrderByStartTimeDesc(null, pageable);
        } else if (status != null) {
            return taskLogRepository.findByStatus(status, pageable);
        } else {
            return taskLogRepository.findAll(pageable);
        }
    }
    
    /**
     * 获取最近执行历史
     * 
     * @param limit 数量限制
     * @return 执行历史列表
     */
    public List<UnifiedTaskLog> getRecentExecutionHistory(int limit) {
        Page<UnifiedTaskLog> page = taskLogRepository.findAll(
            PageRequest.of(0, limit, Sort.by("startTime").descending())
        );
        return page.getContent();
    }
    
    /**
     * 获取任务统计信息
     * 
     * @param taskId 任务ID
     * @return 统计信息
     */
    public Map<String, Object> getTaskStatistics(Long taskId) {
        Map<String, Object> stats = new HashMap<>();
        
        UnifiedTaskConfig config = taskConfigRepository.findById(taskId).orElse(null);
        if (config == null) {
            return stats;
        }
        
        // 基本信息
        stats.put("taskId", taskId);
        stats.put("taskName", config.getTaskName());
        stats.put("crawlerName", config.getCrawlerName());
        stats.put("enabled", config.getEnabled());
        
        // 执行统计
        stats.put("executionCount", config.getExecutionCount());
        stats.put("successCount", config.getSuccessCount());
        stats.put("failureCount", config.getFailureCount());
        stats.put("successRate", config.getSuccessRate());
        
        // 最后执行
        stats.put("lastExecutionTime", config.getLastExecutionTime());
        stats.put("lastExecutionStatus", config.getLastExecutionStatus());
        stats.put("lastExecutionResult", config.getLastExecutionResult());
        
        // 下次执行
        stats.put("nextExecutionTime", config.getNextExecutionTime());
        
        // 数据统计
        long totalCrawled = taskLogRepository.sumCrawledCount(taskId);
        long totalSaved = taskLogRepository.sumSavedCount(taskId);
        stats.put("totalCrawledCount", totalCrawled);
        stats.put("totalSavedCount", totalSaved);
        
        // 执行日志
        Page<UnifiedTaskLog> recentLogsPage = taskLogRepository.findByTaskIdOrderByStartTimeDesc(
            taskId, 
            PageRequest.of(0, 10)
        );
        stats.put("recentLogs", recentLogsPage.getContent());
        
        // 定时任务状态
        stats.put("isScheduled", schedulerService.isTaskRunning(taskId));
        stats.put("isPaused", schedulerService.isTaskPaused(taskId));
        
        return stats;
    }
    
    /**
     * 获取系统总览
     * 
     * @return 系统总览信息
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 任务统计
        long totalTasks = taskConfigRepository.count();
        long enabledTasks = taskConfigRepository.countEnabledTasks();
        long runningTasks = schedulerService.getRunningTaskCount();
        
        overview.put("totalTasks", totalTasks);
        overview.put("enabledTasks", enabledTasks);
        overview.put("runningTasks", runningTasks);
        
        // 今日执行统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<UnifiedTaskLog> todayLogs = taskLogRepository.findByStartTimeBetween(
            todayStart, 
            LocalDateTime.now()
        );
        
        long todayExecutions = todayLogs.size();
        long todaySuccess = todayLogs.stream().filter(log -> "SUCCESS".equals(log.getStatus())).count();
        long todayFailed = todayLogs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
        
        overview.put("todayExecutions", todayExecutions);
        overview.put("todaySuccess", todaySuccess);
        overview.put("todayFailed", todayFailed);
        overview.put("todaySuccessRate", todayExecutions > 0 ? (double) todaySuccess / todayExecutions * 100 : 0.0);
        
        // 按爬虫统计
        List<Object[]> countByCrawler = taskLogRepository.countLogsByCrawler();
        Map<String, Long> crawlerStats = new HashMap<>();
        for (Object[] row : countByCrawler) {
            crawlerStats.put((String) row[0], ((Number) row[1]).longValue());
        }
        overview.put("executionsByCrawler", crawlerStats);
        
        // 按状态统计
        List<Object[]> countByStatus = taskLogRepository.countLogsByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : countByStatus) {
            statusStats.put((String) row[0], ((Number) row[1]).longValue());
        }
        overview.put("executionsByStatus", statusStats);
        
        // 最近执行
        Page<UnifiedTaskLog> recentExecutionsPage = taskLogRepository.findAll(
            PageRequest.of(0, 10, Sort.by("startTime").descending())
        );
        overview.put("recentExecutions", recentExecutionsPage.getContent());
        
        // 运行中任务
        List<UnifiedTaskLog> runningTaskLogs = getRunningTasks();
        overview.put("runningTaskLogs", runningTaskLogs);
        
        return overview;
    }
    
    /**
     * 获取爬虫执行统计
     * 
     * @param crawlerName 爬虫名称
     * @return 统计信息
     */
    public Map<String, Object> getCrawlerStatistics(String crawlerName) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("crawlerName", crawlerName);
        
        // 关联的任务数
        List<UnifiedTaskConfig> tasks = taskConfigRepository.findByCrawlerName(crawlerName);
        stats.put("taskCount", tasks.size());
        stats.put("enabledTaskCount", tasks.stream().filter(UnifiedTaskConfig::getEnabled).count());
        
        // 执行历史统计
        List<UnifiedTaskLog> logs = taskLogRepository.findByCrawlerNameOrderByStartTimeDesc(
            crawlerName, 
            PageRequest.of(0, 100)
        );
        
        long totalExecutions = logs.size();
        long successCount = logs.stream().filter(log -> "SUCCESS".equals(log.getStatus())).count();
        long failedCount = logs.stream().filter(log -> "FAILED".equals(log.getStatus())).count();
        
        stats.put("totalExecutions", totalExecutions);
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("successRate", totalExecutions > 0 ? (double) successCount / totalExecutions * 100 : 0.0);
        
        // 数据统计
        long totalSaved = logs.stream().mapToLong(log -> log.getSavedCount() != null ? log.getSavedCount() : 0).sum();
        long totalSkipped = logs.stream().mapToLong(log -> log.getSkippedCount() != null ? log.getSkippedCount() : 0).sum();
        
        stats.put("totalSaved", totalSaved);
        stats.put("totalSkipped", totalSkipped);
        
        // 最近执行
        if (!logs.isEmpty()) {
            UnifiedTaskLog latestLog = logs.get(0);
            stats.put("lastExecutionTime", latestLog.getStartTime());
            stats.put("lastExecutionStatus", latestLog.getStatus());
            stats.put("lastExecutionResult", latestLog.getResultMessage());
        }
        
        return stats;
    }
}

