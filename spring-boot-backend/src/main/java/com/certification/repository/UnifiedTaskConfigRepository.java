package com.certification.repository;

import com.certification.entity.UnifiedTaskConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 统一任务配置Repository
 */
@Repository
public interface UnifiedTaskConfigRepository extends JpaRepository<UnifiedTaskConfig, Long> {
    
    /**
     * 根据爬虫名称查询任务
     */
    List<UnifiedTaskConfig> findByCrawlerName(String crawlerName);
    
    /**
     * 根据国家代码查询任务
     */
    List<UnifiedTaskConfig> findByCountryCode(String countryCode);
    
    /**
     * 根据任务类型查询任务
     */
    List<UnifiedTaskConfig> findByTaskType(String taskType);
    
    /**
     * 根据启用状态查询任务
     */
    List<UnifiedTaskConfig> findByEnabled(Boolean enabled);
    
    /**
     * 根据多个条件查询任务
     */
    @Query("SELECT t FROM UnifiedTaskConfig t WHERE " +
           "(:countryCode IS NULL OR t.countryCode = :countryCode) AND " +
           "(:crawlerName IS NULL OR t.crawlerName = :crawlerName) AND " +
           "(:taskType IS NULL OR t.taskType = :taskType) AND " +
           "(:enabled IS NULL OR t.enabled = :enabled)")
    Page<UnifiedTaskConfig> findByConditions(
        @Param("countryCode") String countryCode,
        @Param("crawlerName") String crawlerName,
        @Param("taskType") String taskType,
        @Param("enabled") Boolean enabled,
        Pageable pageable
    );
    
    /**
     * 查询需要执行的任务（已启用且到了执行时间）
     */
    @Query("SELECT t FROM UnifiedTaskConfig t WHERE " +
           "t.enabled = true AND " +
           "(t.nextExecutionTime IS NULL OR t.nextExecutionTime <= :now)")
    List<UnifiedTaskConfig> findTasksToExecute(@Param("now") LocalDateTime now);
    
    /**
     * 根据任务名称查询
     */
    Optional<UnifiedTaskConfig> findByTaskName(String taskName);
    
    /**
     * 统计启用任务数量
     */
    @Query("SELECT COUNT(t) FROM UnifiedTaskConfig t WHERE t.enabled = true")
    long countEnabledTasks();
    
    /**
     * 统计运行中任务数量
     */
    @Query("SELECT COUNT(t) FROM UnifiedTaskConfig t WHERE t.lastExecutionStatus = 'RUNNING'")
    long countRunningTasks();
    
    /**
     * 按国家统计任务数量
     */
    @Query("SELECT t.countryCode, COUNT(t) FROM UnifiedTaskConfig t GROUP BY t.countryCode")
    List<Object[]> countTasksByCountry();
    
    /**
     * 按任务类型统计任务数量
     */
    @Query("SELECT t.taskType, COUNT(t) FROM UnifiedTaskConfig t GROUP BY t.taskType")
    List<Object[]> countTasksByType();
    
    /**
     * 查询最近创建的任务
     */
    List<UnifiedTaskConfig> findTop10ByOrderByCreatedAtDesc();
    
    /**
     * 查询最近执行的任务
     */
    List<UnifiedTaskConfig> findTop10ByOrderByLastExecutionTimeDesc();
    
    /**
     * 根据爬虫名称和任务类型查询（用于预设查询）
     */
    Optional<UnifiedTaskConfig> findByCrawlerNameAndTaskType(String crawlerName, String taskType);
    
    /**
     * 根据任务类型和启用状态查询
     */
    List<UnifiedTaskConfig> findByTaskTypeAndEnabled(String taskType, Boolean enabled);
    
    /**
     * 根据爬虫名称、任务类型和启用状态查询
     */
    List<UnifiedTaskConfig> findByCrawlerNameAndTaskTypeAndEnabled(String crawlerName, String taskType, Boolean enabled);
}
