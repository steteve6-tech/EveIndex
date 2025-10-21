package com.certification.service.certnews;

import com.certification.crawler.certification.BeiceCrawler;
import com.certification.crawler.certification.SgsCrawler;
import com.certification.crawler.certification.ULCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.entity.common.CertNewsTaskConfig;
import com.certification.entity.common.CertNewsTaskLog;
import com.certification.repository.common.CertNewsTaskConfigRepository;
import com.certification.repository.common.CertNewsTaskLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 认证新闻定时任务服务
 */
@Slf4j
@Service
public class CertNewsTaskService {

    @Autowired
    private CertNewsTaskConfigRepository taskConfigRepository;

    @Autowired
    private CertNewsTaskLogRepository taskLogRepository;

    @Autowired
    private SgsCrawler sgsCrawler;

    @Autowired
    private ULCrawler ulCrawler;

    @Autowired
    private BeiceCrawler beiceCrawler;

    /**
     * 获取所有任务配置
     */
    public List<CertNewsTaskConfig> getAllTasks() {
        return taskConfigRepository.findAll();
    }

    /**
     * 获取启用的任务
     */
    public List<CertNewsTaskConfig> getEnabledTasks() {
        return taskConfigRepository.findByEnabledTrue();
    }

    /**
     * 根据ID获取任务
     */
    public CertNewsTaskConfig getTaskById(Long id) {
        return taskConfigRepository.findById(id).orElse(null);
    }

    /**
     * 创建或更新任务
     */
    @Transactional
    public CertNewsTaskConfig saveTask(CertNewsTaskConfig task) {
        // 验证Cron表达式
        if (!isValidCronExpression(task.getCronExpression())) {
            throw new IllegalArgumentException("无效的Cron表达式: " + task.getCronExpression());
        }

        // 计算下次执行时间
        task.setNextExecuteTime(calculateNextExecuteTime(task.getCronExpression()));

        return taskConfigRepository.save(task);
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long id) {
        taskConfigRepository.deleteById(id);
    }

    /**
     * 启用任务
     */
    @Transactional
    public void enableTask(Long id) {
        CertNewsTaskConfig task = taskConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        task.setEnabled(true);
        task.setNextExecuteTime(calculateNextExecuteTime(task.getCronExpression()));
        taskConfigRepository.save(task);
    }

    /**
     * 禁用任务
     */
    @Transactional
    public void disableTask(Long id) {
        CertNewsTaskConfig task = taskConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + id));
        task.setEnabled(false);
        task.setNextExecuteTime(null);
        taskConfigRepository.save(task);
    }

    /**
     * 手动执行任务
     */
    @Transactional
    public CertNewsTaskLog executeTask(Long taskId) {
        CertNewsTaskConfig task = taskConfigRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        return executeTaskInternal(task);
    }

    /**
     * 内部执行任务方法
     */
    @Transactional
    public CertNewsTaskLog executeTaskInternal(CertNewsTaskConfig task) {
        log.info("开始执行认证新闻爬虫任务: {}", task.getTaskName());

        // 创建日志
        CertNewsTaskLog taskLog = new CertNewsTaskLog();
        taskLog.setTaskId(task.getId());
        taskLog.setTaskName(task.getTaskName());
        taskLog.setCrawlerType(task.getCrawlerType());
        taskLog.setStartTime(LocalDateTime.now());

        try {
            // 根据爬虫类型执行相应的爬虫
            List<CrawlerResult> results;
            String keyword = task.getKeyword();
            int maxRecords = task.getMaxRecords() != null ? task.getMaxRecords() : 50;

            switch (task.getCrawlerType().toUpperCase()) {
                case "SGS":
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        results = sgsCrawler.crawl(keyword, maxRecords);
                    } else {
                        results = sgsCrawler.crawlCertificationNews(maxRecords);
                    }
                    break;

                case "UL":
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        results = ulCrawler.crawl(keyword, maxRecords);
                    } else {
                        results = ulCrawler.crawl("", maxRecords);
                    }
                    break;

                case "BEICE":
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        results = beiceCrawler.crawl(keyword, maxRecords);
                    } else {
                        results = beiceCrawler.crawl("", maxRecords);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("不支持的爬虫类型: " + task.getCrawlerType());
            }

            // 统计结果
            int totalCount = results.size();
            int successCount = (int) results.stream()
                    .filter(r -> r.getTitle() != null && r.getUrl() != null)
                    .count();
            int errorCount = totalCount - successCount;

            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus("SUCCESS");
            taskLog.setSuccessCount(successCount);
            taskLog.setErrorCount(errorCount);
            taskLog.setMessage(String.format("爬取完成: 总计=%d, 成功=%d, 失败=%d",
                    totalCount, successCount, errorCount));

            // 更新任务配置
            task.setLastExecuteTime(LocalDateTime.now());
            task.setLastExecuteStatus("SUCCESS");
            task.setLastExecuteMessage(taskLog.getMessage());
            task.setNextExecuteTime(calculateNextExecuteTime(task.getCronExpression()));
            taskConfigRepository.save(task);

            log.info("认证新闻爬虫任务执行成功: {}, {}", task.getTaskName(), taskLog.getMessage());

        } catch (Exception e) {
            log.error("认证新闻爬虫任务执行失败: {}", task.getTaskName(), e);

            taskLog.setEndTime(LocalDateTime.now());
            taskLog.setStatus("FAILED");
            taskLog.setErrorMessage(e.getMessage());
            taskLog.setMessage("执行失败: " + e.getMessage());

            // 更新任务配置
            task.setLastExecuteTime(LocalDateTime.now());
            task.setLastExecuteStatus("FAILED");
            task.setLastExecuteMessage("执行失败: " + e.getMessage());
            task.setNextExecuteTime(calculateNextExecuteTime(task.getCronExpression()));
            taskConfigRepository.save(task);
        }

        // 保存日志
        return taskLogRepository.save(taskLog);
    }

    /**
     * 获取任务执行日志
     */
    public List<CertNewsTaskLog> getTaskLogs(Long taskId) {
        return taskLogRepository.findByTaskIdOrderByStartTimeDesc(taskId);
    }

    /**
     * 获取所有任务日志
     */
    public List<CertNewsTaskLog> getAllLogs() {
        return taskLogRepository.findAll();
    }

    /**
     * 获取最近的日志
     */
    public List<CertNewsTaskLog> getRecentLogs(int limit) {
        return taskLogRepository.findAll().stream()
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(limit)
                .toList();
    }

    /**
     * 验证Cron表达式
     */
    private boolean isValidCronExpression(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (Exception e) {
            log.warn("无效的Cron表达式: {}", cronExpression);
            return false;
        }
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            return cron.next(LocalDateTime.now());
        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}", cronExpression, e);
            return null;
        }
    }

    /**
     * 清理旧日志
     */
    @Transactional
    public void cleanOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        taskLogRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("已清理 {} 天前的认证新闻任务日志", daysToKeep);
    }
}
