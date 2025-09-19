package com.certification.repository;

import com.certification.entity.notification.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志Repository接口
 */
@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    
    /**
     * 根据日志级别查询日志
     */
    List<SystemLog> findByLogLevelAndDeletedOrderByCreatedAtDesc(SystemLog.LogLevel logLevel, Integer deleted);
    
    /**
     * 根据日志类型查询日志
     */
    List<SystemLog> findByLogTypeAndDeletedOrderByCreatedAtDesc(SystemLog.LogType logType, Integer deleted);
    
    /**
     * 根据日志状态查询日志
     */
    List<SystemLog> findByLogStatusAndDeletedOrderByCreatedAtDesc(SystemLog.LogStatus logStatus, Integer deleted);
    
    /**
     * 查询错误日志
     */
    @Query("SELECT s FROM SystemLog s WHERE s.logLevel = :logLevel AND s.deleted = 0 ORDER BY s.createdAt DESC")
    List<SystemLog> findErrorLogs(@Param("logLevel") SystemLog.LogLevel logLevel);
    
    /**
     * 统计各级别日志数量
     */
    @Query("SELECT s.logLevel as logLevel, COUNT(s) as count FROM SystemLog s WHERE s.deleted = 0 GROUP BY s.logLevel")
    List<Map<String, Object>> countByLogLevel();
    
    /**
     * 统计各类型日志数量
     */
    @Query("SELECT s.logType as logType, COUNT(s) as count FROM SystemLog s WHERE s.deleted = 0 GROUP BY s.logType")
    List<Map<String, Object>> countByLogType();
    
    /**
     * 统计各状态日志数量
     */
    @Query("SELECT s.logStatus as logStatus, COUNT(s) as count FROM SystemLog s WHERE s.deleted = 0 GROUP BY s.logStatus")
    List<Map<String, Object>> countByLogStatus();
    
    /**
     * 查询指定时间范围内的日志
     */
    List<SystemLog> findByCreatedAtBetweenAndDeletedOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    

    

    
    /**
     * 查询最近的日志
     */
    @Query("SELECT s FROM SystemLog s WHERE s.deleted = 0 ORDER BY s.createdAt DESC")
    List<SystemLog> findRecentLogs();
}
