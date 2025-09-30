package com.certification.repository.common;

import com.certification.entity.common.GuidanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FDAGuidanceDocumentRepository extends JpaRepository<GuidanceDocument, Long> {

    /**
     * 根据标题查找指导文档
     */
    Optional<GuidanceDocument> findByTitle(String title);

    /**
     * 根据文档URL查找指导文档
     */
    Optional<GuidanceDocument> findByDocumentUrl(String documentUrl);

    /**
     * 根据指导状态查找指导文档
     */
    List<GuidanceDocument> findByGuidanceStatus(String guidanceStatus);

    /**
     * 根据话题查找指导文档
     */
    List<GuidanceDocument> findByTopicContaining(String topic);

    /**
     * 根据发布日期范围查找指导文档
     */
    List<GuidanceDocument> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据数据状态查找指导文档
     */
    List<GuidanceDocument> findByDataStatus(String dataStatus);

    /**
     * 最近爬取的指导文档（按时间倒序）
     */
    List<GuidanceDocument> findByCrawlTimeGreaterThanEqualOrderByCrawlTimeDesc(LocalDateTime startTime);

    /**
     * 根据标题和URL检查是否存在
     */
    boolean existsByTitleAndDocumentUrl(String title, String documentUrl);

    /**
     * 所有活跃的指导文档（按发布日期倒序）
     */
    List<GuidanceDocument> findByDataStatusOrderByPublicationDateDesc(String dataStatus);

    /**
     * 根据标题搜索（忽略大小写）
     */
    List<GuidanceDocument> findByTitleContainingIgnoreCase(String title);

    /**
     * 根据话题搜索（忽略大小写）
     */
    List<GuidanceDocument> findByTopicContainingIgnoreCase(String topic);

    /**
     * 根据关键词搜索（忽略大小写）
     */
    List<GuidanceDocument> findByKeywordsContainingIgnoreCase(String keywords);

    /**
     * 根据标题模糊查询
     */
    List<GuidanceDocument> findByTitleContaining(String title);

    /**
     * 根据指导状态模糊查询
     */
    List<GuidanceDocument> findByGuidanceStatusContaining(String guidanceStatus);

    /**
     * 根据关键词模糊查询
     */
    @Query("SELECT gd FROM GuidanceDocument gd WHERE gd.keywords LIKE %:keyword%")
    List<GuidanceDocument> findByKeywordsContaining(@Param("keyword") String keyword);

    /**
     * 根据国家查找
     */
    List<GuidanceDocument> findByJdCountry(String jdCountry);

    /**
     * 根据关键词搜索（支持多个字段）
     */
    @Query("SELECT d FROM GuidanceDocument d WHERE " +
           "(d.title LIKE %:keyword% OR " +
           "d.topic LIKE %:keyword% OR " +
           "d.documentType LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    List<GuidanceDocument> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode);

    /**
     * 根据关键词搜索（支持多个字段，分页）
     */
    @Query("SELECT d FROM GuidanceDocument d WHERE " +
           "(d.title LIKE %:keyword% OR " +
           "d.topic LIKE %:keyword% OR " +
           "d.documentType LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    org.springframework.data.domain.Page<GuidanceDocument> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode, org.springframework.data.domain.Pageable pageable);
}
