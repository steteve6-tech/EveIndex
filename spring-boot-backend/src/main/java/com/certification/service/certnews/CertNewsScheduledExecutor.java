package com.certification.service.certnews;

import com.certification.entity.common.CertNewsTaskConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证新闻定时任务调度器
 * 每分钟检查一次是否有需要执行的任务
 */
@Slf4j
@Component
public class CertNewsScheduledExecutor {

    @Autowired
    private CertNewsTaskService taskService;

    /**
     * 每分钟检查一次待执行的任务
     * Cron: 每分钟的第0秒执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteTasks() {
        try {
            List<CertNewsTaskConfig> enabledTasks = taskService.getEnabledTasks();

            if (enabledTasks.isEmpty()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            for (CertNewsTaskConfig task : enabledTasks) {
                try {
                    // 检查是否到了执行时间
                    if (shouldExecuteNow(task, now)) {
                        log.info("触发定时任务执行: {}", task.getTaskName());
                        taskService.executeTaskInternal(task);
                    }
                } catch (Exception e) {
                    log.error("执行定时任务失败: {}", task.getTaskName(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查定时任务失败", e);
        }
    }

    /**
     * 判断任务是否应该在当前时间执行
     */
    private boolean shouldExecuteNow(CertNewsTaskConfig task, LocalDateTime now) {
        if (task.getNextExecuteTime() == null) {
            return false;
        }

        // 如果下次执行时间在当前时间之前或等于当前时间（精确到分钟）
        LocalDateTime nextExec = task.getNextExecuteTime();
        return nextExec.getYear() == now.getYear()
                && nextExec.getMonth() == now.getMonth()
                && nextExec.getDayOfMonth() == now.getDayOfMonth()
                && nextExec.getHour() == now.getHour()
                && nextExec.getMinute() == now.getMinute();
    }

    /**
     * 每天凌晨3点清理30天前的日志
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanOldLogs() {
        try {
            log.info("开始清理认证新闻任务旧日志...");
            taskService.cleanOldLogs(30);
            log.info("清理认证新闻任务旧日志完成");
        } catch (Exception e) {
            log.error("清理认证新闻任务旧日志失败", e);
        }
    }
}
