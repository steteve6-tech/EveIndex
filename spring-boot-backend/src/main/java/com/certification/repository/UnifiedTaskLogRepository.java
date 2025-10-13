package com.certification.repository;

import com.certification.entity.UnifiedTaskLog;
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
 * 统一任务日志Repository
 */
@Repository
public interface UnifiedTaskLogRepository extends JpaRepository<UnifiedTaskLog, Long> {
    
    /**
     * 根据任务ID查询执行日志
     */
    List<UnifiedTaskLog> findByTaskIdOrderByStartTimeDesc(Long taskId);
    
    /**
     * 根据任务ID分页查询执行日志
     */
    Page<UnifiedTaskLog> findByTaskIdOrderByStartTimeDesc(Long taskId, Pageable pageable);
    
    /**
     * 根据爬虫名称查询执行日志
     */
    List<UnifiedTaskLog> findByCrawlerNameOrderByStartTimeDesc(String crawlerName, Pageable pageable);
    
    /**
     * 根据国家代码查询执行日志
     */
    List<UnifiedTaskLog> findByCountryCodeOrderByStartTimeDesc(String countryCode, Pageable pageable);
    
    /**
     * 根据状态查询执行日志
     */
    List<UnifiedTaskLog> findByStatus(String status);
    
    /**
     * 查询运行中的任务日志
     */
    List<UnifiedTaskLog> findByStatusAndStartTimeAfter(String status, LocalDateTime startTime);
    
    /**
     * 根据时间范围查询执行日志
     */
    @Query("SELECT t FROM UnifiedTaskLog t WHERE t.startTime BETWEEN :startTime AND :endTime ORDER BY t.startTime DESC")
    List<UnifiedTaskLog> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据任务ID和时间范围查询执行日志
     */
    @Query("SELECT t FROM UnifiedTaskLog t WHERE t.taskId = :taskId AND t.startTime BETWEEN :startTime AND :endTime ORDER BY t.startTime DESC")
    List<UnifiedTaskLog> findByTaskIdAndStartTimeBetween(@Param("taskId") Long taskId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计成功执行次数
     */
    @Query("SELECT COUNT(t) FROM UnifiedTaskLog t WHERE t.taskId = :taskId AND t.status = 'SUCCESS'")
    long countSuccessfulExecutions(@Param("taskId") Long taskId);
    
    /**
     * 统计失败执行次数
     */
    @Query("SELECT COUNT(t) FROM UnifiedTaskLog t WHERE t.taskId = :taskId AND t.status = 'FAILED'")
    long countFailedExecutions(@Param("taskId") Long taskId);
    
    /**
     * 统计总爬取数量
     */
    @Query("SELECT COALESCE(SUM(t.crawledCount), 0) FROM UnifiedTaskLog t WHERE t.taskId = :taskId")
    long sumCrawledCount(@Param("taskId") Long taskId);
    
    /**
     * 统计总保存数量
     */
    @Query("SELECT COALESCE(SUM(t.savedCount), 0) FROM UnifiedTaskLog t WHERE t.taskId = :taskId")
    long sumSavedCount(@Param("taskId") Long taskId);
    
    /**
     * 查询最近的执行日志
     */
    List<UnifiedTaskLog> findTop10ByOrderByStartTimeDesc();
    
    /**
     * 根据批次号查询执行日志
     */
    Optional<UnifiedTaskLog> findByBatchNo(String batchNo);
    
    /**
     * 查询手动触发的执行日志
     */
    List<UnifiedTaskLog> findByIsManualTrueOrderByStartTimeDesc(Pageable pageable);
    
    /**
     * 查询自动触发的执行日志
     */
    List<UnifiedTaskLog> findByIsManualFalseOrderByStartTimeDesc(Pageable pageable);
    
    /**
     * 按状态统计执行日志数量
     */
    @Query("SELECT t.status, COUNT(t) FROM UnifiedTaskLog t GROUP BY t.status")
    List<Object[]> countLogsByStatus();
    
    /**
     * 按爬虫统计执行日志数量
     */
    @Query("SELECT t.crawlerName, COUNT(t) FROM UnifiedTaskLog t GROUP BY t.crawlerName")
    List<Object[]> countLogsByCrawler();
    
    /**
     * 按日期统计执行日志数量
     */
    @Query("SELECT DATE(t.startTime), COUNT(t) FROM UnifiedTaskLog t GROUP BY DATE(t.startTime) ORDER BY DATE(t.startTime) DESC")
    List<Object[]> countLogsByDate();
    
    /**
     * 根据时间范围查询执行日志（分页）
     */
    Page<UnifiedTaskLog> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据状态查询执行日志（分页）
     */
    Page<UnifiedTaskLog> findByStatus(String status, Pageable pageable);
}
