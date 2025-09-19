package com.certification.repository.common;

import com.certification.entity.common.CrawlerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 爬虫状态仓库接口
 */
@Repository
public interface CrawlerStateRepository extends JpaRepository<CrawlerState, Long> {

    /**
     * 根据爬虫名称查找状态
     */
    Optional<CrawlerState> findByCrawlerName(String crawlerName);

    /**
     * 根据爬虫名称和类型查找状态
     */
    Optional<CrawlerState> findByCrawlerNameAndCrawlerType(String crawlerName, String crawlerType);

    /**
     * 查找所有启用的爬虫状态
     */
    List<CrawlerState> findByEnabledTrue();

    /**
     * 根据状态查找爬虫
     */
    List<CrawlerState> findByStatus(CrawlerState.CrawlerStatus status);

    /**
     * 查找有错误的爬虫
     */
    @Query("SELECT cs FROM CrawlerState cs WHERE cs.status = 'ERROR' AND cs.enabled = true")
    List<CrawlerState> findErrorCrawlers();

    /**
     * 查找连续错误次数超过指定值的爬虫
     */
    @Query("SELECT cs FROM CrawlerState cs WHERE cs.consecutiveErrorCount >= :errorCount AND cs.enabled = true")
    List<CrawlerState> findCrawlersWithConsecutiveErrors(@Param("errorCount") Integer errorCount);

    /**
     * 查找长时间未爬取的爬虫
     */
    @Query("SELECT cs FROM CrawlerState cs WHERE cs.lastCrawlTime < :thresholdTime AND cs.enabled = true")
    List<CrawlerState> findStaleCrawlers(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * 查找指定时间范围内爬取过的爬虫
     */
    @Query("SELECT cs FROM CrawlerState cs WHERE cs.lastCrawlTime BETWEEN :startTime AND :endTime")
    List<CrawlerState> findCrawlersByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计爬虫总数
     */
    @Query("SELECT COUNT(cs) FROM CrawlerState cs WHERE cs.enabled = true")
    Long countEnabledCrawlers();

    /**
     * 统计错误爬虫数量
     */
    @Query("SELECT COUNT(cs) FROM CrawlerState cs WHERE cs.status = 'ERROR' AND cs.enabled = true")
    Long countErrorCrawlers();

    /**
     * 统计运行中爬虫数量
     */
    @Query("SELECT COUNT(cs) FROM CrawlerState cs WHERE cs.status = 'RUNNING'")
    Long countRunningCrawlers();

    /**
     * 获取爬虫的累计爬取总数
     */
    @Query("SELECT SUM(cs.totalCrawledCount) FROM CrawlerState cs WHERE cs.enabled = true")
    Long getTotalCrawledCount();

    /**
     * 根据爬虫类型查找
     */
    List<CrawlerState> findByCrawlerType(String crawlerType);

    /**
     * 查找需要更新的爬虫（长时间未爬取或状态异常）
     */
    @Query("SELECT cs FROM CrawlerState cs WHERE cs.enabled = true AND " +
           "(cs.lastCrawlTime < :thresholdTime OR cs.status = 'ERROR' OR cs.status = 'IDLE')")
    List<CrawlerState> findCrawlersNeedingUpdate(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * 删除指定爬虫的状态记录
     */
    void deleteByCrawlerName(String crawlerName);

    /**
     * 检查爬虫是否存在
     */
    boolean existsByCrawlerName(String crawlerName);

    /**
     * 检查爬虫是否存在且启用
     */
    boolean existsByCrawlerNameAndEnabledTrue(String crawlerName);
}
