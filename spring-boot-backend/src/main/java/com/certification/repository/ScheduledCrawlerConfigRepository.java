package com.certification.repository;

import com.certification.entity.ScheduledCrawlerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 定时爬取配置数据访问层
 */
@Repository
public interface ScheduledCrawlerConfigRepository extends JpaRepository<ScheduledCrawlerConfig, Long> {

    /**
     * 根据模块名称查找配置
     */
    List<ScheduledCrawlerConfig> findByModuleNameAndDeleted(String moduleName, Integer deleted);

    /**
     * 根据爬虫名称查找配置
     */
    List<ScheduledCrawlerConfig> findByCrawlerNameAndDeleted(String crawlerName, Integer deleted);

    /**
     * 根据国家代码查找配置
     */
    List<ScheduledCrawlerConfig> findByCountryCodeAndDeleted(String countryCode, Integer deleted);

    /**
     * 查找所有启用的配置
     */
    List<ScheduledCrawlerConfig> findByEnabledAndDeleted(Boolean enabled, Integer deleted);

    /**
     * 根据模块名称和爬虫名称查找配置
     */
    Optional<ScheduledCrawlerConfig> findByModuleNameAndCrawlerNameAndDeleted(
            String moduleName, String crawlerName, Integer deleted);

    /**
     * 根据模块名称、爬虫名称和国家代码查找配置
     */
    Optional<ScheduledCrawlerConfig> findByModuleNameAndCrawlerNameAndCountryCodeAndDeleted(
            String moduleName, String crawlerName, String countryCode, Integer deleted);

    /**
     * 查找需要执行的定时任务
     */
    @Query("SELECT s FROM ScheduledCrawlerConfig s WHERE s.enabled = true AND s.deleted = 0 " +
           "AND (s.nextExecutionTime IS NULL OR s.nextExecutionTime <= :currentTime)")
    List<ScheduledCrawlerConfig> findTasksToExecute(@Param("currentTime") java.time.LocalDateTime currentTime);

}
