package com.certification.repository.common;

import com.certification.entity.common.CertNewsTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证新闻任务日志Repository
 */
@Repository
public interface CertNewsTaskLogRepository extends JpaRepository<CertNewsTaskLog, Long> {

    /**
     * 根据任务ID查找日志
     */
    List<CertNewsTaskLog> findByTaskIdOrderByStartTimeDesc(Long taskId);

    /**
     * 根据爬虫类型查找日志
     */
    List<CertNewsTaskLog> findByCrawlerTypeOrderByStartTimeDesc(String crawlerType);

    /**
     * 查找指定时间范围内的日志
     */
    List<CertNewsTaskLog> findByStartTimeBetweenOrderByStartTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据任务ID查找最近N条日志
     */
    @Query("SELECT l FROM CertNewsTaskLog l WHERE l.taskId = ?1 ORDER BY l.startTime DESC")
    List<CertNewsTaskLog> findRecentLogsByTaskId(Long taskId);

    /**
     * 删除指定时间之前的日志
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}
