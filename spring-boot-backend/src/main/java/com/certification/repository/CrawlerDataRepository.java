package com.certification.repository;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.CertNewsData.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 爬虫数据Repository接口
 */
@Repository
public interface CrawlerDataRepository extends JpaRepository<CertNewsData, String> {
    
    /**
     * 根据数据源名称查询数据
     */
    List<CertNewsData> findBySourceNameAndDeleted(String sourceName, Integer deleted);
    
    /**
     * 根据状态查询数据
     */
    List<CertNewsData> findByStatusAndDeleted(CertNewsData.DataStatus status, Integer deleted);
    
    /**
     * 根据URL查询数据
     */
    Optional<CertNewsData> findByUrlAndDeleted(String url, Integer deleted);
    
    /**
     * 根据数据源名称分页查询
     */
    Page<CertNewsData> findBySourceNameAndDeleted(String sourceName, Integer deleted, Pageable pageable);
    
    /**
     * 根据状态分页查询
     */
    Page<CertNewsData> findByStatusAndDeleted(CertNewsData.DataStatus status, Integer deleted, Pageable pageable);
    
    /**
     * 根据关键词搜索（标题、内容、摘要）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND (c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%)")
    Page<CertNewsData> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 综合搜索查询（支持关键词、国家、相关性、数据源、类型、日期范围、匹配关键词）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 " +
           "AND (:keyword IS NULL OR (c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%)) " +
           "AND (:country IS NULL OR c.country = :country) " +
           "AND (:related IS NULL OR c.related = :related) " +
           "AND (:sourceName IS NULL OR c.sourceName = :sourceName) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:startDate IS NULL OR c.crawlTime >= :startDate) " +
           "AND (:endDate IS NULL OR c.crawlTime <= :endDate) " +
           "AND (:riskLevel IS NULL OR c.riskLevel = :riskLevel) " +
           "AND (:matchedKeyword IS NULL OR c.matchedKeywords LIKE %:matchedKeyword%)")
    Page<CertNewsData> searchCrawlerData(
        @Param("keyword") String keyword,
        @Param("country") String country,
        @Param("related") Boolean related,
        @Param("sourceName") String sourceName,
        @Param("type") String type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("riskLevel") String riskLevel,
        @Param("matchedKeyword") String matchedKeyword,
        Pageable pageable
    );
    
    /**
     * 根据多个关键词搜索（标题、内容、摘要）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND (" +
           "(:country IS NULL OR c.country = :country) AND " +
           "(:sourceName IS NULL OR c.sourceName = :sourceName) AND " +
           "(" +
           "  c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%" +
           "  OR c.title LIKE %:keyword2% OR c.content LIKE %:keyword2% OR c.summary LIKE %:keyword2%" +
           "  OR c.title LIKE %:keyword3% OR c.content LIKE %:keyword3% OR c.summary LIKE %:keyword3%" +
           "))")
    Page<CertNewsData> searchByKeywords(@Param("keyword") String keyword,
                                        @Param("keyword2") String keyword2,
                                        @Param("keyword3") String keyword3,
                                        @Param("country") String country,
                                        @Param("sourceName") String sourceName,
                                        Pageable pageable);
    
    /**
     * 按发布时间排序的查询（使用CAST将字符串转换为日期进行排序）
     */
    @Query(value = "SELECT c.* FROM t_crawler_data c WHERE c.deleted = 0 " +
           "AND (:keyword IS NULL OR (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%') OR c.summary LIKE CONCAT('%', :keyword, '%'))) " +
           "AND (:country IS NULL OR c.country = :country) " +
           "AND (:related IS NULL OR c.related = :related) " +
           "AND (:sourceName IS NULL OR c.source_name = :sourceName) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:startDate IS NULL OR c.crawl_time >= :startDate) " +
           "AND (:endDate IS NULL OR c.crawl_time <= :endDate) " +
           "AND (:riskLevel IS NULL OR c.risk_level = :riskLevel) " +
           "AND (:matchedKeyword IS NULL OR c.matched_keywords LIKE CONCAT('%', :matchedKeyword, '%')) " +
           "ORDER BY CAST(c.publish_date AS DATE) DESC, c.crawl_time DESC", 
           countQuery = "SELECT COUNT(*) FROM t_crawler_data c WHERE c.deleted = 0 " +
           "AND (:keyword IS NULL OR (c.title LIKE CONCAT('%', :keyword, '%') OR c.content LIKE CONCAT('%', :keyword, '%') OR c.summary LIKE CONCAT('%', :keyword, '%'))) " +
           "AND (:country IS NULL OR c.country = :country) " +
           "AND (:related IS NULL OR c.related = :related) " +
           "AND (:sourceName IS NULL OR c.source_name = :sourceName) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:startDate IS NULL OR c.crawl_time >= :startDate) " +
           "AND (:endDate IS NULL OR c.crawl_time <= :endDate) " +
           "AND (:riskLevel IS NULL OR c.risk_level = :riskLevel) " +
           "AND (:matchedKeyword IS NULL OR c.matched_keywords LIKE CONCAT('%', :matchedKeyword, '%'))",
           nativeQuery = true)
    Page<CertNewsData> searchCrawlerDataOrderByPublishDate(
        @Param("keyword") String keyword,
        @Param("country") String country,
        @Param("related") Boolean related,
        @Param("sourceName") String sourceName,
        @Param("type") String type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("riskLevel") String riskLevel,
        @Param("matchedKeyword") String matchedKeyword,
        Pageable pageable
    );
    
    /**
     * 统计各数据源的数据量
     */
    @Query("SELECT c.sourceName as sourceName, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.sourceName")
    List<Map<String, Object>> countBySource();
    
    /**
     * 获取所有数据源名称
     */
    @Query("SELECT DISTINCT c.sourceName FROM CertNewsData c WHERE c.deleted = 0 AND c.sourceName IS NOT NULL ORDER BY c.sourceName")
    List<String> findAllSourceNames();
    
    /**
     * 统计各状态的数据量
     */
    @Query("SELECT c.status as status, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.status")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 根据数据源名称统计数据量
     */
    long countBySourceNameAndDeleted(String sourceName, Integer deleted);
    
    /**
     * 根据状态统计数据量
     */
    long countByStatusAndDeleted(CertNewsData.DataStatus status, Integer deleted);
    
    /**
     * 查询指定时间范围内的数据
     */
    List<CertNewsData> findByCrawlTimeBetweenAndDeleted(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    
    /**
     * 统计指定时间范围内的数据量
     */
    long countByCrawlTimeBetweenAndDeleted(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    
    /**
     * 查询未处理的数据
     */
    List<CertNewsData> findByIsProcessedAndDeleted(Boolean isProcessed, Integer deleted);
    
    /**
     * 根据数据源名称和状态查询
     */
    List<CertNewsData> findBySourceNameAndStatusAndDeleted(String sourceName, CertNewsData.DataStatus status, Integer deleted);
    
    /**
     * 根据国家查询数据
     */
    List<CertNewsData> findByCountryAndDeleted(String country, Integer deleted);
    
    /**
     * 根据类型查询数据
     */
    List<CertNewsData> findByTypeAndDeleted(String type, Integer deleted);
    
    /**
     * 查询最近的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 ORDER BY c.crawlTime DESC")
    List<CertNewsData> findRecentData();
    
    /**
     * 查询最近的数据（限制数量）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 ORDER BY c.crawlTime DESC")
    List<CertNewsData> findRecentData(Pageable pageable);
    
    /**
     * 根据数据源查询最近的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.deleted = 0 ORDER BY c.crawlTime DESC")
    List<CertNewsData> findRecentDataBySource(@Param("sourceName") String sourceName, Pageable pageable);
    
    /**
     * 更新数据状态
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.status = :status, c.updatedAt = :updatedAt WHERE c.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") CertNewsData.DataStatus status, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 标记为已处理
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.isProcessed = true, c.processedTime = :processedTime, c.updatedAt = :updatedAt WHERE c.id = :id")
    int markAsProcessed(@Param("id") Long id, @Param("processedTime") LocalDateTime processedTime, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 标记为处理中
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.status = 'PROCESSING', c.updatedAt = :updatedAt WHERE c.id = :id")
    int markAsProcessing(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 标记为错误
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.status = 'ERROR', c.remarks = :errorMessage, c.updatedAt = :updatedAt WHERE c.id = :id")
    int markAsError(@Param("id") Long id, @Param("errorMessage") String errorMessage, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 逻辑删除数据
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.deleted = 1, c.updatedAt = :updatedAt WHERE c.id = :id")
    int softDelete(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 删除旧数据
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.deleted = 1, c.updatedAt = :updatedAt WHERE c.crawlTime < :cutoffTime AND c.deleted = 0")
    int deleteOldData(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 获取今日数据量
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.deleted = 0 AND DATE(c.crawlTime) = CURRENT_DATE")
    long countTodayData();
    
    /**
     * 获取本周数据量
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.deleted = 0 AND YEARWEEK(c.crawlTime) = YEARWEEK(CURRENT_DATE)")
    long countWeekData();
    
    /**
     * 获取本月数据量
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.deleted = 0 AND YEAR(c.crawlTime) = YEAR(CURRENT_DATE) AND MONTH(c.crawlTime) = MONTH(CURRENT_DATE)")
    long countMonthData();
    
    /**
     * 获取趋势数据（按天统计）
     */
    @Query("SELECT DATE(c.crawlTime) as date, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 AND c.crawlTime >= :startDate GROUP BY DATE(c.crawlTime) ORDER BY date")
    List<Map<String, Object>> getDailyTrend(@Param("startDate") LocalDateTime startDate);

    /**
     * 查询指定数据源下所有未删除的URL
     */
    @Query("SELECT c.url FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.deleted = 0")
    List<String> findUrlsBySourceName(@Param("sourceName") String sourceName);
    
    /**
     * 批量查询URL是否存在
     */
    @Query("SELECT c.url FROM CertNewsData c WHERE c.url IN :urls AND c.deleted = 0")
    List<String> findExistingUrls(@Param("urls") List<String> urls);
    
    /**
     * 根据URL列表查询已存在的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.url IN :urls AND c.deleted = 0")
    List<CertNewsData> findByUrls(@Param("urls") List<String> urls);
    
    /**
     * 根据数据源和URL列表查询已存在的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.url IN :urls AND c.deleted = 0")
    List<CertNewsData> findBySourceNameAndUrls(@Param("sourceName") String sourceName, @Param("urls") List<String> urls);
    
    /**
     * 根据产品名称查询数据
     */
    List<CertNewsData> findByProductAndDeleted(String product, Integer deleted);
    
    /**
     * 根据产品名称分页查询
     */
    Page<CertNewsData> findByProductAndDeleted(String product, Integer deleted, Pageable pageable);
    
    /**
     * 根据产品名称统计数据量
     */
    long countByProductAndDeleted(String product, Integer deleted);
    
    /**
     * 根据产品名称模糊查询
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.product LIKE %:product%")
    Page<CertNewsData> findByProductContaining(@Param("product") String product, Pageable pageable);
    
    /**
     * 根据产品名称模糊查询（不分页）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.product LIKE %:product%")
    List<CertNewsData> findByProductContaining(@Param("product") String product);
    
    /**
     * 统计各产品的数据量
     */
    @Query("SELECT c.product as product, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 AND c.product IS NOT NULL GROUP BY c.product ORDER BY count DESC")
    List<Map<String, Object>> countByProduct();
    
    /**
     * 根据数据源和产品名称查询
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.product = :product AND c.deleted = 0")
    List<CertNewsData> findBySourceNameAndProduct(@Param("sourceName") String sourceName, @Param("product") String product);
    
    /**
     * 根据数据源和产品名称分页查询
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.product = :product AND c.deleted = 0")
    Page<CertNewsData> findBySourceNameAndProduct(@Param("sourceName") String sourceName, @Param("product") String product, Pageable pageable);
    
    /**
     * 根据产品名称查询（包含关键词搜索）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.product LIKE %:product% AND (" +
           "c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%)")
    Page<CertNewsData> findByProductAndKeyword(@Param("product") String product, @Param("keyword") String keyword, Pageable pageable);
    
    // ==================== release_date JSON字段相关查询方法 ====================
    
    /**
     * 查询有发布时间列表的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.releaseDate IS NOT NULL")
    List<CertNewsData> findByReleaseDateNotEmpty();
    
    /**
     * 根据数据源查询有发布时间列表的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.deleted = 0 AND c.releaseDate IS NOT NULL")
    List<CertNewsData> findBySourceNameAndReleaseDateNotEmpty(@Param("sourceName") String sourceName);
    
    // ==================== execution_date JSON字段相关查询方法 ====================
    
    /**
     * 查询有执行时间列表的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.executionDate IS NOT NULL")
    List<CertNewsData> findByExecutionDateNotEmpty();
    
    /**
     * 根据数据源查询有执行时间列表的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.deleted = 0 AND c.executionDate IS NOT NULL")
    List<CertNewsData> findBySourceNameAndExecutionDateNotEmpty(@Param("sourceName") String sourceName);
    
    // ==================== 组合查询方法 ====================
    
    /**
     * 根据数据源查询同时有发布时间和执行时间的数据
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.sourceName = :sourceName AND c.deleted = 0 AND c.releaseDate IS NOT NULL AND c.executionDate IS NOT NULL")
    List<CertNewsData> findBySourceNameAndBothDatesNotEmpty(@Param("sourceName") String sourceName);
    
    // ==================== related字段相关查询方法 ====================
    
    /**
     * 根据相关状态查询数据
     */
    List<CertNewsData> findByRelatedAndDeleted(Boolean related, Integer deleted);
    
    /**
     * 根据相关状态分页查询
     */
    Page<CertNewsData> findByRelatedAndDeleted(Boolean related, Integer deleted, Pageable pageable);
    
    /**
     * 根据相关状态统计数据量
     */
    long countByRelatedAndDeleted(Boolean related, Integer deleted);
    
    /**
     * 查询相关数据（related = true）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related = true AND c.deleted = 0")
    List<CertNewsData> findRelatedData();
    
    /**
     * 查询相关数据（related = true）分页
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related = true AND c.deleted = 0")
    Page<CertNewsData> findRelatedData(Pageable pageable);
    
    /**
     * 查询不相关数据（related = false）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related = false AND c.deleted = 0")
    List<CertNewsData> findUnrelatedData();
    
    /**
     * 查询不相关数据（related = false）分页
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related = false AND c.deleted = 0")
    Page<CertNewsData> findUnrelatedData(Pageable pageable);
    
    /**
     * 查询未确定相关性的数据（related = null）
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related IS NULL AND c.deleted = 0")
    List<CertNewsData> findUndeterminedData();
    
    /**
     * 查询未确定相关性的数据（related = null）分页
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.related IS NULL AND c.deleted = 0")
    Page<CertNewsData> findUndeterminedData(Pageable pageable);
    
    /**
     * 根据数据源和相关状态查询
     */
    List<CertNewsData> findBySourceNameAndRelatedAndDeleted(String sourceName, Boolean related, Integer deleted);
    
    /**
     * 根据数据源和相关状态分页查询
     */
    Page<CertNewsData> findBySourceNameAndRelatedAndDeleted(String sourceName, Boolean related, Integer deleted, Pageable pageable);
    
    /**
     * 根据国家相关状态查询
     */
    List<CertNewsData> findByCountryAndRelatedAndDeleted(String country, Boolean related, Integer deleted);
    
    /**
     * 根据国家相关状态分页查询
     */
    Page<CertNewsData> findByCountryAndRelatedAndDeleted(String country, Boolean related, Integer deleted, Pageable pageable);
    
    /**
     * 根据产品名称和相关状态查询
     */
    List<CertNewsData> findByProductAndRelatedAndDeleted(String product, Boolean related, Integer deleted);
    
    /**
     * 根据产品名称和相关状态分页查询
     */
    Page<CertNewsData> findByProductAndRelatedAndDeleted(String product, Boolean related, Integer deleted, Pageable pageable);
    
    /**
     * 统计各相关状态的数据量
     */
    @Query("SELECT c.related as related, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.related")
    List<Map<String, Object>> countByRelated();
    
    /**
     * 根据数据源统计各相关状态的数据量
     */
    @Query("SELECT c.sourceName as sourceName, c.related as related, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.sourceName, c.related")
    List<Map<String, Object>> countBySourceAndRelated();
    
    /**
     * 根据国家统计各相关状态的数据量
     */
    @Query("SELECT c.country as country, c.related as related, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.country, c.related")
    List<Map<String, Object>> countByCountryAndRelated();
    
    /**
     * 更新相关状态
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.related = :related, c.updatedAt = :updatedAt WHERE c.id = :id")
    int updateRelatedStatus(@Param("id") String id, @Param("related") Boolean related, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 批量更新相关状态
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.related = :related, c.updatedAt = :updatedAt WHERE c.id IN :ids")
    int batchUpdateRelatedStatus(@Param("ids") List<String> ids, @Param("related") Boolean related, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新相关状态和匹配的关键词
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.related = :related, c.matchedKeywords = :matchedKeywords, c.updatedAt = :updatedAt WHERE c.id = :id")
    int updateRelatedStatusWithKeywords(@Param("id") String id, @Param("related") Boolean related, @Param("matchedKeywords") String matchedKeywords, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 更新风险等级和风险说明
     */
    @Modifying
    @Query("UPDATE CertNewsData c SET c.riskLevel = :riskLevel, c.riskDescription = :riskDescription, c.updatedAt = :updatedAt WHERE c.id = :id")
    int updateRiskLevel(@Param("id") String id, @Param("riskLevel") RiskLevel riskLevel, @Param("riskDescription") String riskDescription, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 根据关键词搜索并过滤相关状态
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.related = :related AND (c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%)")
    Page<CertNewsData> searchByKeywordAndRelated(@Param("keyword") String keyword, @Param("related") Boolean related, Pageable pageable);
    
    /**
     * 根据多个关键词搜索并过滤相关状态
     */
    @Query("SELECT c FROM CertNewsData c WHERE c.deleted = 0 AND c.related = :related AND (" +
           "(:country IS NULL OR c.country = :country) AND " +
           "(:sourceName IS NULL OR c.sourceName = :sourceName) AND " +
           "(" +
           "  c.title LIKE %:keyword% OR c.content LIKE %:keyword% OR c.summary LIKE %:keyword%" +
           "  OR c.title LIKE %:keyword2% OR c.content LIKE %:keyword2% OR c.summary LIKE %:keyword2%" +
           "  OR c.title LIKE %:keyword3% OR c.content LIKE %:keyword3% OR c.summary LIKE %:keyword3%" +
           "))")
    Page<CertNewsData> searchByKeywordsAndRelated(@Param("keyword") String keyword,
                                                  @Param("keyword2") String keyword2,
                                                  @Param("keyword3") String keyword3,
                                                  @Param("country") String country,
                                                  @Param("sourceName") String sourceName,
                                                  @Param("related") Boolean related,
                                                  Pageable pageable);
    
    /**
     * 根据删除状态查询所有数据
     */
    List<CertNewsData> findByDeleted(Integer deleted);
    
    /**
     * 根据删除状态统计数据量
     */
    long countByDeleted(Integer deleted);
    
    /**
     * 批量将所有数据的related字段设置为null（未确定），并清除匹配关键词
     */
    @Modifying
    @Transactional
    @Query("UPDATE CertNewsData c SET c.related = NULL, c.matchedKeywords = NULL, c.updatedAt = :updatedAt WHERE c.deleted = 0")
    int batchUpdateRelatedToNull(@Param("updatedAt") LocalDateTime updatedAt);

    // ==================== 每日统计相关查询方法 ====================
    
    /**
     * 根据创建时间范围查询数据（用于每日统计）
     */
    List<CertNewsData> findByCreatedAtBetweenAndDeletedFalse(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据爬取时间范围查询数据（用于每日统计）
     */
    List<CertNewsData> findByCrawlTimeBetweenAndDeletedFalse(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询所有未删除的数据
     */
    List<CertNewsData> findByDeletedFalse();
    
    /**
     * 统计指定国家、风险等级和日期范围内的数据数量
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.deleted = 0 AND c.country = :country AND c.riskLevel = :riskLevel AND c.crawlTime >= :startTime AND c.crawlTime < :endTime")
    long countByCountryAndRiskLevelAndCreatedAtBetweenAndDeletedFalse(
        @Param("country") String country, 
        @Param("riskLevel") RiskLevel riskLevel, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定国家和日期范围内的数据总数
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.deleted = 0 AND c.country = :country AND c.crawlTime >= :startTime AND c.crawlTime < :endTime")
    long countByCountryAndCreatedAtBetweenAndDeletedFalse(
        @Param("country") String country, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    // ==================== 风险等级相关查询方法 ====================
    
    /**
     * 根据风险等级查询数据
     */
    List<CertNewsData> findByRiskLevelAndDeleted(RiskLevel riskLevel, Integer deleted);
    
    /**
     * 根据风险等级分页查询
     */
    Page<CertNewsData> findByRiskLevelAndDeleted(RiskLevel riskLevel, Integer deleted, Pageable pageable);
    
    /**
     * 根据风险等级统计数据量
     */
    long countByRiskLevelAndDeleted(RiskLevel riskLevel, Integer deleted);
    
    /**
     * 统计风险等级为null的数据量
     */
    @Query("SELECT COUNT(c) FROM CertNewsData c WHERE c.riskLevel IS NULL AND c.deleted = :deleted")
    long countByRiskLevelIsNullAndDeleted(@Param("deleted") Integer deleted);
    
    /**
     * 统计各风险等级的数据量
     */
    @Query("SELECT c.riskLevel as riskLevel, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.riskLevel")
    List<Map<String, Object>> countByRiskLevel();
    
    /**
     * 根据数据源统计各风险等级的数据量
     */
    @Query("SELECT c.sourceName as sourceName, c.riskLevel as riskLevel, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.sourceName, c.riskLevel")
    List<Map<String, Object>> countBySourceAndRiskLevel();
    
    /**
     * 根据国家统计各风险等级的数据量
     */
    @Query("SELECT c.country as country, c.riskLevel as riskLevel, COUNT(c) as count FROM CertNewsData c WHERE c.deleted = 0 GROUP BY c.country, c.riskLevel")
    List<Map<String, Object>> countByCountryAndRiskLevel();
    
    /**
     * 批量更新风险等级
     */
    @Modifying
    @Transactional
    @Query("UPDATE CertNewsData c SET c.riskLevel = :riskLevel, c.updatedAt = :updatedAt WHERE c.id IN :ids")
    int batchUpdateRiskLevel(@Param("ids") List<String> ids, @Param("riskLevel") RiskLevel riskLevel, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 批量将所有数据的风险等级设置为指定值
     */
    @Modifying
    @Transactional
    @Query("UPDATE CertNewsData c SET c.riskLevel = :riskLevel, c.updatedAt = :updatedAt WHERE c.deleted = 0")
    int batchUpdateAllRiskLevel(@Param("riskLevel") RiskLevel riskLevel, @Param("updatedAt") LocalDateTime updatedAt);
    
    /**
     * 分页查询指定删除状态的数据（用于批量处理）
     */
    @Query(value = "SELECT * FROM t_crawler_data WHERE deleted = :deleted ORDER BY id LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<CertNewsData> findByDeletedWithPagination(@Param("deleted") Integer deleted, @Param("offset") int offset, @Param("limit") int limit);
    
}
