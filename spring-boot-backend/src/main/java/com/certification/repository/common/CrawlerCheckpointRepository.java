package com.certification.repository.common;

import com.certification.entity.common.CrawlerCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 爬虫断点续传Repository接口
 */
@Repository
public interface CrawlerCheckpointRepository extends JpaRepository<CrawlerCheckpoint, Long> {
    
    /**
     * 根据爬虫类型和搜索条件查找断点
     */
    @Query("SELECT c FROM CrawlerCheckpoint c WHERE c.crawlerType = :crawlerType " +
           "AND (:searchTerm IS NULL OR c.searchTerm = :searchTerm) " +
           "AND (:dateFrom IS NULL OR c.dateFrom = :dateFrom) " +
           "AND (:dateTo IS NULL OR c.dateTo = :dateTo)")
    Optional<CrawlerCheckpoint> findByCrawlerTypeAndSearchConditions(
            @Param("crawlerType") String crawlerType,
            @Param("searchTerm") String searchTerm,
            @Param("dateFrom") String dateFrom,
            @Param("dateTo") String dateTo);
    
    /**
     * 根据爬虫类型查找所有断点
     */
    List<CrawlerCheckpoint> findByCrawlerTypeOrderByLastUpdatedDesc(String crawlerType);
    
    /**
     * 查找所有运行中的断点
     */
    List<CrawlerCheckpoint> findByStatus(CrawlerCheckpoint.CrawlerStatus status);
    
    /**
     * 查找所有失败的断点
     */
    List<CrawlerCheckpoint> findByStatusOrderByLastUpdatedDesc(CrawlerCheckpoint.CrawlerStatus status);
    
    /**
     * 根据爬虫类型和状态查找断点
     */
    List<CrawlerCheckpoint> findByCrawlerTypeAndStatusOrderByLastUpdatedDesc(
            String crawlerType, CrawlerCheckpoint.CrawlerStatus status);
    
    /**
     * 删除指定爬虫类型的所有断点
     */
    void deleteByCrawlerType(String crawlerType);
    
    /**
     * 删除指定爬虫类型和搜索条件的断点
     */
    @Query("DELETE FROM CrawlerCheckpoint c WHERE c.crawlerType = :crawlerType " +
           "AND (:searchTerm IS NULL OR c.searchTerm = :searchTerm)")
    void deleteByCrawlerTypeAndSearchTerm(@Param("crawlerType") String crawlerType, 
                                        @Param("searchTerm") String searchTerm);
}
