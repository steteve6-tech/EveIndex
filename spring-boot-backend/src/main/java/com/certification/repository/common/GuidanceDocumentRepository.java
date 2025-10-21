package com.certification.repository.common;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.GuidanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 指导文档Repository接口
 */
@Repository
public interface GuidanceDocumentRepository extends JpaRepository<GuidanceDocument, Long> {

    /**
     * 根据风险等级查找记录
     */
    List<GuidanceDocument> findByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<GuidanceDocument> findByRiskLevel(CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据标题查找
     */
    List<GuidanceDocument> findByTitleContaining(String title);
    
    /**
     * 根据标题和数据源查找（用于去重）
     */
    List<GuidanceDocument> findByTitleAndDataSource(String title, String dataSource);

    /**
     * 根据指导状态查找
     */
    List<GuidanceDocument> findByGuidanceStatus(String guidanceStatus);

    /**
     * 根据数据源查找
     */
    List<GuidanceDocument> findByDataSource(String dataSource);

    /**
     * 根据国家查找
     */
    List<GuidanceDocument> findByJdCountry(String jdCountry);

    /**
     * 根据话题模糊查询
     */
    List<GuidanceDocument> findByTopicContaining(String topic);

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
     * 根据关键词搜索（支持多个字段）
     */
    @Query("SELECT d FROM GuidanceDocument d WHERE " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.topic) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.documentType) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    List<GuidanceDocument> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode);

    /**
     * 根据关键词搜索（支持多个字段，分页）
     */
    @Query("SELECT d FROM GuidanceDocument d WHERE " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.topic) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.documentType) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    org.springframework.data.domain.Page<GuidanceDocument> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据国家查找（分页）
     */
    org.springframework.data.domain.Page<GuidanceDocument> findByJdCountry(String jdCountry, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据国家和风险等级查找（分页）
     */
    org.springframework.data.domain.Page<GuidanceDocument> findByJdCountryAndRiskLevel(String jdCountry, CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计新增数据数量
     */
    long countByIsNew(Boolean isNew);

    /**
     * 查找新增数据（分页）
     */
    org.springframework.data.domain.Page<GuidanceDocument> findByIsNew(Boolean isNew, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找已查看的新增数据
     */
    List<GuidanceDocument> findByIsNewAndNewDataViewed(Boolean isNew, Boolean newDataViewed);

}