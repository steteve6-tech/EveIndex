package com.certification.service.crawler;

import com.certification.entity.UnifiedTaskConfig;
import com.certification.repository.UnifiedTaskConfigRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态任务调度服务
 * 基于Spring TaskScheduler实现Cron表达式动态调度
 */
@Slf4j
@Service
public class DynamicTaskSchedulerService {
    
    @Autowired
    private TaskScheduler taskScheduler;
    
    @Autowired
    private UnifiedTaskConfigRepository taskConfigRepository;
    
    @Autowired
    private TaskExecutionService taskExecutionService;
    
    /**
     * 存储所有已调度的任务
     * Key: 任务ID, Value: ScheduledFuture对象
     */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    /**
     * 存储暂停的任务
     * Key: 任务ID, Value: 任务配置
     */
    private final Map<Long, UnifiedTaskConfig> pausedTasks = new ConcurrentHashMap<>();
    
    /**
     * 初始化：加载所有启用的定时任务
     */
    @PostConstruct
    public void initScheduledTasks() {
        log.info("========== 初始化定时任务 ==========");
        
        try {
            List<UnifiedTaskConfig> enabledTasks = taskConfigRepository.findByEnabled(true);
            
            for (UnifiedTaskConfig task : enabledTasks) {
                if (task.getCronExpression() != null && !task.getCronExpression().trim().isEmpty()) {
                    scheduleTask(task);
                }
            }
            
            log.info("定时任务初始化完成，共加载 {} 个任务", scheduledTasks.size());
            
        } catch (Exception e) {
            log.error("定时任务初始化失败", e);
        }
    }
    
    /**
     * 创建定时任务
     * 
     * @param config 任务配置
     */
    public void scheduleTask(UnifiedTaskConfig config) {
        if (config == null || config.getId() == null) {
            log.warn("任务配置无效");
            return;
        }
        
        // 如果任务已存在，先取消
        if (scheduledTasks.containsKey(config.getId())) {
            cancelTask(config.getId());
        }
        
        try {
            CronTrigger trigger = new CronTrigger(config.getCronExpression());
            
            ScheduledFuture<?> future = taskScheduler.schedule(
                () -> executeScheduledTask(config.getId()),
                trigger
            );
            
            scheduledTasks.put(config.getId(), future);
            
            log.info("定时任务已调度: ID={}, 名称={}, Cron={}", 
                config.getId(), config.getTaskName(), config.getCronExpression());
            
        } catch (Exception e) {
            log.error("调度任务失败: ID={}, Cron={}", config.getId(), config.getCronExpression(), e);
        }
    }
    
    /**
     * 更新定时任务
     * 
     * @param taskId 任务ID
     */
    public void updateScheduledTask(Long taskId) {
        log.info("更新定时任务: ID={}", taskId);
        
        // 先取消旧任务
        cancelTask(taskId);
        
        // 重新加载任务配置
        UnifiedTaskConfig task = taskConfigRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.warn("任务不存在: ID={}", taskId);
            return;
        }
        
        // 如果启用且有Cron表达式，重新调度
        if (task.getEnabled() && task.getCronExpression() != null && !task.getCronExpression().trim().isEmpty()) {
            scheduleTask(task);
        }
    }
    
    /**
     * 取消定时任务
     * 
     * @param taskId 任务ID
     */
    public void cancelTask(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            log.info("定时任务已取消: ID={}", taskId);
        }
    }
    
    /**
     * 暂停任务（保留配置，停止执行）
     * 
     * @param taskId 任务ID
     */
    public void pauseTask(Long taskId) {
        UnifiedTaskConfig task = taskConfigRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.warn("任务不存在: ID={}", taskId);
            return;
        }
        
        // 取消调度
        cancelTask(taskId);
        
        // 保存到暂停列表
        pausedTasks.put(taskId, task);
        
        log.info("任务已暂停: ID={}, 名称={}", taskId, task.getTaskName());
    }
    
    /**
     * 恢复任务
     * 
     * @param taskId 任务ID
     */
    public void resumeTask(Long taskId) {
        UnifiedTaskConfig task = pausedTasks.remove(taskId);
        if (task == null) {
            task = taskConfigRepository.findById(taskId).orElse(null);
        }
        
        if (task == null) {
            log.warn("任务不存在: ID={}", taskId);
            return;
        }
        
        // 重新调度
        if (task.getEnabled() && task.getCronExpression() != null && !task.getCronExpression().trim().isEmpty()) {
            scheduleTask(task);
            log.info("任务已恢复: ID={}, 名称={}", taskId, task.getTaskName());
        }
    }
    
    /**
     * 执行定时任务
     * 
     * @param taskId 任务ID
     */
    private void executeScheduledTask(Long taskId) {
        log.info("定时触发任务执行: ID={}", taskId);
        
        try {
            UnifiedTaskConfig task = taskConfigRepository.findById(taskId).orElse(null);
            if (task == null) {
                log.warn("任务不存在: ID={}", taskId);
                return;
            }
            
            // 检查任务是否仍然启用
            if (!task.getEnabled()) {
                log.info("任务已禁用，跳过执行: ID={}", taskId);
                return;
            }
            
            // 调用执行服务
            taskExecutionService.executeTask(task, false, "SCHEDULER");
            
        } catch (Exception e) {
            log.error("定时任务执行失败: ID={}", taskId, e);
        }
    }
    
    /**
     * 获取运行中的任务数量
     * 
     * @return 任务数量
     */
    public int getRunningTaskCount() {
        return scheduledTasks.size();
    }
    
    /**
     * 获取所有运行中的任务ID
     * 
     * @return 任务ID列表
     */
    public List<Long> getRunningTaskIds() {
        return scheduledTasks.keySet().stream().toList();
    }
    
    /**
     * 检查任务是否正在运行
     * 
     * @param taskId 任务ID
     * @return 是否运行中
     */
    public boolean isTaskRunning(Long taskId) {
        return scheduledTasks.containsKey(taskId);
    }
    
    /**
     * 检查任务是否暂停
     * 
     * @param taskId 任务ID
     * @return 是否暂停
     */
    public boolean isTaskPaused(Long taskId) {
        return pausedTasks.containsKey(taskId);
    }
    
    /**
     * 销毁时取消所有任务
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭定时任务调度器...");
        
        for (Map.Entry<Long, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            entry.getValue().cancel(false);
        }
        
        scheduledTasks.clear();
        pausedTasks.clear();
        
        log.info("定时任务调度器已关闭");
    }
}

