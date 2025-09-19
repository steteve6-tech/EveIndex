package com.example.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 510K设备数据Repository
 * 支持关键词筛选和国家筛选
 */
@Repository
public interface Device510KRepository extends JpaRepository<Device510K, Long> {

    /**
     * 根据风险等级查询
     */
    Page<Device510K> findByRiskLevel(String riskLevel, Pageable pageable);

    /**
     * 根据国家和风险等级查询
     */
    Page<Device510K> findByCountryAndRiskLevel(String country, String riskLevel, Pageable pageable);

    /**
     * 根据关键词和风险等级查询（忽略大小写）
     */
    Page<Device510K> findByMatchedKeywordsContainingIgnoreCaseAndRiskLevel(String keyword, String riskLevel, Pageable pageable);

    /**
     * 根据国家、关键词和风险等级查询（忽略大小写）
     */
    Page<Device510K> findByCountryAndMatchedKeywordsContainingIgnoreCaseAndRiskLevel(
        String country, String keyword, String riskLevel, Pageable pageable);

    /**
     * 统计高风险数据数量
     */
    @Query("SELECT COUNT(d) FROM Device510K d WHERE d.riskLevel = 'HIGH'")
    long countByRiskLevel(String riskLevel);

    /**
     * 统计指定国家的高风险数据数量
     */
    @Query("SELECT COUNT(d) FROM Device510K d WHERE d.country = :country AND d.riskLevel = 'HIGH'")
    long countByCountryAndRiskLevel(@Param("country") String country, @Param("riskLevel") String riskLevel);

    /**
     * 更新风险等级
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device510K d SET d.riskLevel = :riskLevel WHERE d.id = :id")
    int updateRiskLevel(@Param("id") Long id, @Param("riskLevel") String riskLevel);

    /**
     * 更新关键词
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device510K d SET d.matchedKeywords = REPLACE(d.matchedKeywords, :oldKeyword, :newKeyword) WHERE d.id = :id")
    int updateKeywords(@Param("id") Long id, @Param("oldKeyword") String oldKeyword, @Param("newKeyword") String newKeyword);

    /**
     * 获取所有关键词及其统计
     */
    @Query(value = "SELECT DISTINCT keyword, COUNT(*) as count FROM " +
            "(SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(matched_keywords, ',', numbers.n), ',', -1)) as keyword " +
            "FROM device510k " +
            "CROSS JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) numbers " +
            "WHERE CHAR_LENGTH(matched_keywords) - CHAR_LENGTH(REPLACE(matched_keywords, ',', '')) >= numbers.n - 1 " +
            "AND risk_level = 'HIGH') keywords " +
            "WHERE keyword != '' " +
            "GROUP BY keyword " +
            "ORDER BY count DESC", nativeQuery = true)
    List<Object[]> findKeywordStatistics();
}
