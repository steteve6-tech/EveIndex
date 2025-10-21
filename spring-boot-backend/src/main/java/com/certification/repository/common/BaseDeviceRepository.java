package com.certification.repository.common;

import com.certification.entity.common.BaseDeviceEntity;
import com.certification.entity.common.CertNewsData.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备数据通用Repository接口
 * 所有设备相关Repository的基础接口
 *
 * @param <T> 继承自BaseDeviceEntity的实体类型
 * @param <ID> 主键类型
 *
 * @author System
 * @since 2025-01-14
 */
@NoRepositoryBean
public interface BaseDeviceRepository<T extends BaseDeviceEntity, ID> extends
        JpaRepository<T, ID>,
        JpaSpecificationExecutor<T> {

    // ==================== 基于风险等级的查询 ====================

    /**
     * 根据风险等级查询数据
     */
    List<T> findByRiskLevel(RiskLevel riskLevel);

    /**
     * 根据风险等级分页查询
     */
    Page<T> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);

    /**
     * 统计指定风险等级的数据量
     */
    long countByRiskLevel(RiskLevel riskLevel);

    /**
     * 查询高风险数据
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.riskLevel = 'HIGH' AND e.dataStatus = 'ACTIVE' ORDER BY e.crawlTime DESC")
    List<T> findHighRiskData();

    /**
     * 查询高风险数据（分页）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.riskLevel = 'HIGH' AND e.dataStatus = 'ACTIVE' ORDER BY e.crawlTime DESC")
    Page<T> findHighRiskData(Pageable pageable);

    // ==================== 基于国家的查询 ====================

    /**
     * 根据国家代码查询
     */
    List<T> findByJdCountry(String jdCountry);

    /**
     * 根据国家代码分页查询
     */
    Page<T> findByJdCountry(String jdCountry, Pageable pageable);

    /**
     * 根据国家和风险等级查询
     */
    List<T> findByJdCountryAndRiskLevel(String jdCountry, RiskLevel riskLevel);

    /**
     * 根据国家和风险等级分页查询
     */
    Page<T> findByJdCountryAndRiskLevel(String jdCountry, RiskLevel riskLevel, Pageable pageable);

    /**
     * 统计各国家的数据量
     */
    @Query("SELECT e.jdCountry, COUNT(e) FROM #{#entityName} e WHERE e.dataStatus = 'ACTIVE' GROUP BY e.jdCountry")
    List<Object[]> countByCountry();

    // ==================== 基于数据源的查询 ====================

    /**
     * 根据数据源查询
     */
    List<T> findByDataSource(String dataSource);

    /**
     * 根据数据源分页查询
     */
    Page<T> findByDataSource(String dataSource, Pageable pageable);

    /**
     * 根据数据源和国家查询
     */
    List<T> findByDataSourceAndJdCountry(String dataSource, String jdCountry);

    // ==================== 基于时间的查询 ====================

    /**
     * 查询指定时间范围内的数据
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.crawlTime BETWEEN :startTime AND :endTime ORDER BY e.crawlTime DESC")
    List<T> findByCrawlTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的数据（分页）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.crawlTime BETWEEN :startTime AND :endTime ORDER BY e.crawlTime DESC")
    Page<T> findByCrawlTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    /**
     * 查询最近的数据
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.dataStatus = 'ACTIVE' ORDER BY e.crawlTime DESC")
    List<T> findLatestData(Pageable pageable);

    // ==================== 基于关键词的查询 ====================

    /**
     * 根据关键词模糊查询
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.keywords LIKE %:keyword% AND e.dataStatus = 'ACTIVE'")
    List<T> findByKeywordsContaining(@Param("keyword") String keyword);

    /**
     * 根据关键词模糊查询（分页）
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.keywords LIKE %:keyword% AND e.dataStatus = 'ACTIVE'")
    Page<T> findByKeywordsContaining(@Param("keyword") String keyword, Pageable pageable);

    // ==================== 基于数据状态的查询 ====================

    /**
     * 查询激活状态的数据
     */
    List<T> findByDataStatus(String dataStatus);

    /**
     * 根据国家和数据状态查询
     */
    List<T> findByJdCountryAndDataStatus(String jdCountry, String dataStatus);

    /**
     * 统计激活状态的数据量
     */
    long countByDataStatus(String dataStatus);

    // ==================== 批量更新操作 ====================

    /**
     * 批量更新风险等级
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.riskLevel = :riskLevel, e.updateTime = :updateTime WHERE e.id IN :ids")
    int batchUpdateRiskLevel(@Param("ids") List<ID> ids, @Param("riskLevel") RiskLevel riskLevel, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量更新数据状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.dataStatus = :dataStatus, e.updateTime = :updateTime WHERE e.id IN :ids")
    int batchUpdateDataStatus(@Param("ids") List<ID> ids, @Param("dataStatus") String dataStatus, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量软删除
     */
    @Modifying
    @Transactional
    @Query("UPDATE #{#entityName} e SET e.dataStatus = 'DELETED', e.updateTime = :updateTime WHERE e.id IN :ids")
    int batchSoftDelete(@Param("ids") List<ID> ids, @Param("updateTime") LocalDateTime updateTime);

    // ==================== 统计查询 ====================

    /**
     * 获取风险等级分布统计
     */
    @Query("SELECT e.riskLevel, COUNT(e) FROM #{#entityName} e WHERE e.dataStatus = 'ACTIVE' GROUP BY e.riskLevel")
    List<Object[]> getRiskLevelStatistics();

    /**
     * 获取数据源分布统计
     */
    @Query("SELECT e.dataSource, COUNT(e) FROM #{#entityName} e WHERE e.dataStatus = 'ACTIVE' GROUP BY e.dataSource")
    List<Object[]> getDataSourceStatistics();

    /**
     * 获取国家风险分布统计
     */
    @Query("SELECT e.jdCountry, e.riskLevel, COUNT(e) FROM #{#entityName} e WHERE e.dataStatus = 'ACTIVE' GROUP BY e.jdCountry, e.riskLevel")
    List<Object[]> getCountryRiskStatistics();
}
