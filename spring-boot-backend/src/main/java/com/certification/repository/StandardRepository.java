package com.certification.repository;

import com.certification.entity.common.Standard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 标准Repository接口
 */
@Repository
public interface StandardRepository extends JpaRepository<Standard, Long> {
    
    /**
     * 根据标准编号查询标准
     */
    Optional<Standard> findByStandardNumberAndDeleted(String standardNumber, Integer deleted);
    
    /**
     * 根据风险等级查询标准
     */
    List<Standard> findByRiskLevelAndDeletedOrderByUpdatedAtDesc(Standard.RiskLevel riskLevel, Integer deleted);
    
    /**
     * 根据风险等级查询标准（分页）
     */
    Page<Standard> findByRiskLevelAndDeleted(Standard.RiskLevel riskLevel, Integer deleted, Pageable pageable);
    
    /**
     * 根据状态查询标准
     */
    List<Standard> findByStandardStatusAndDeletedOrderByUpdatedAtDesc(Standard.StandardStatus status, Integer deleted);
    
    /**
     * 根据状态查询标准（分页）
     */
    Page<Standard> findByStandardStatusAndDeleted(Standard.StandardStatus status, Integer deleted, Pageable pageable);
    
    /**
     * 查询监控中的标准
     */
    List<Standard> findByIsMonitoredAndDeletedOrderByUpdatedAtDesc(Boolean isMonitored, Integer deleted);
    
    /**
     * 根据国家查询标准
     */
    List<Standard> findByCountryAndDeleted(String country, Integer deleted);
    
    /**
     * 根据国家查询标准（分页）
     */
    Page<Standard> findByCountryAndDeleted(String country, Integer deleted, Pageable pageable);
    
    /**
     * 根据多个国家查询标准
     */
    List<Standard> findByCountriesAndDeleted(String countries, Integer deleted);
    
    /**
     * 根据国家或适用国家列表查询标准（分页）
     */
    @Query("SELECT s FROM Standard s WHERE (s.country = :country OR s.countries LIKE %:country%) AND s.deleted = :deleted ORDER BY s.updatedAt DESC")
    Page<Standard> findByCountryOrCountriesContainingAndDeleted(@Param("country") String country, @Param("deleted") Integer deleted, Pageable pageable);
    
    /**
     * 根据删除标记查询标准（分页）
     */
    Page<Standard> findByDeleted(Integer deleted, Pageable pageable);
    
    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT s.riskLevel as riskLevel, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.riskLevel")
    List<Map<String, Object>> countByRiskLevel();
    
    /**
     * 统计各监管影响的数量
     */
    @Query("SELECT s.regulatoryImpact as regulatoryImpact, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.regulatoryImpact")
    List<Map<String, Object>> countByRegulatoryImpact();
    
    /**
     * 统计各标准状态的数量
     */
    @Query("SELECT s.standardStatus as standardStatus, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.standardStatus")
    List<Map<String, Object>> countByStandardStatus();
    
    /**
     * 统计各国家的数量
     */
    @Query("SELECT s.country as country, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.country")
    List<Map<String, Object>> countByCountry();
    
    /**
     * 统计各产品类型的数量
     */
    @Query("SELECT s.productTypes as productTypes, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.productTypes")
    List<Map<String, Object>> countByProductTypes();
    
    /**
     * 统计删除标记的数量
     */
    @Query("SELECT COUNT(s) FROM Standard s WHERE s.deleted = :deleted")
    long countByDeleted(@Param("deleted") Integer deleted);
    
    /**
     * 统计指定风险等级的数量
     */
    @Query("SELECT COUNT(s) FROM Standard s WHERE s.riskLevel = :riskLevel AND s.deleted = :deleted")
    long countByRiskLevelAndDeleted(@Param("riskLevel") Standard.RiskLevel riskLevel, @Param("deleted") Integer deleted);
    
    /**
     * 统计监控状态的数量
     */
    @Query("SELECT COUNT(s) FROM Standard s WHERE s.isMonitored = :isMonitored AND s.deleted = :deleted")
    long countByIsMonitoredAndDeleted(@Param("isMonitored") Boolean isMonitored, @Param("deleted") Integer deleted);
    
    /**
     * 查询即将到期的标准
     */
    List<Standard> findByComplianceDeadlineLessThanEqualAndDeletedOrderByComplianceDeadlineAsc(LocalDate deadline, Integer deleted);
    
    /**
     * 查询即将到期的标准（基于生效日期）
     */
    @Query("SELECT s FROM Standard s WHERE s.effectiveDate <= :endDate AND s.deleted = 0 ORDER BY s.effectiveDate ASC")
    List<Standard> findExpiringStandards(@Param("endDate") String endDate);
    
    /**
     * 统计即将到期的标准数量
     */
    @Query("SELECT COUNT(s) FROM Standard s WHERE s.effectiveDate <= :endDate AND s.deleted = 0")
    long countExpiringStandards(@Param("endDate") String endDate);
    
    /**
     * 根据关键词搜索标准
     */
    @Query("SELECT s FROM Standard s WHERE (s.title LIKE %:keyword% OR s.description LIKE %:keyword% OR s.standardNumber LIKE %:keyword% OR s.keywords LIKE %:keyword%) AND s.deleted = 0 ORDER BY s.updatedAt DESC")
    Page<Standard> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查询高风险标准
     */
    List<Standard> findByRiskLevelAndDeleted(Standard.RiskLevel riskLevel, Integer deleted);
    
    /**
     * 根据国家查询标准（使用原生SQL）
     */
    @Query(value = "SELECT * FROM t_standard s WHERE JSON_CONTAINS(s.countries, :countryCode) AND s.deleted = 0 ORDER BY s.updated_at DESC", nativeQuery = true)
    List<Standard> findByCountry(@Param("countryCode") String countryCode);
    
    /**
     * 根据国家查询标准（分页）
     */
    @Query(value = "SELECT * FROM t_standard s WHERE JSON_CONTAINS(s.countries, :countryCode) AND s.deleted = 0 ORDER BY s.updated_at DESC", nativeQuery = true)
    Page<Standard> findByCountry(@Param("countryCode") String countryCode, Pageable pageable);
    
    /**
     * 根据多个国家查询标准（使用原生SQL）
     */
    @Query(value = "SELECT * FROM t_standard s WHERE JSON_OVERLAPS(s.countries, :countryCodes) AND s.deleted = 0 ORDER BY s.updated_at DESC", nativeQuery = true)
    List<Standard> findByCountries(@Param("countryCodes") String countryCodes);
    
    /**
     * 根据多个国家查询标准（分页，使用原生SQL）
     */
    @Query(value = "SELECT * FROM t_standard s WHERE JSON_OVERLAPS(s.countries, :countryCodes) AND s.deleted = 0 ORDER BY s.updated_at DESC", nativeQuery = true)
    Page<Standard> findByCountries(@Param("countryCodes") String countryCodes, Pageable pageable);
    
    /**
     * 根据国家代码模糊查询标准
     */
    @Query("SELECT s FROM Standard s WHERE s.countries LIKE %:countryCode% AND s.deleted = 0 ORDER BY s.updatedAt DESC")
    List<Standard> findByCountryContaining(@Param("countryCode") String countryCode);
    
    /**
     * 统计各国家的标准数量
     */
    @Query("SELECT s.countries as countries, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.countries")
    List<Map<String, Object>> countByCountries();
    
    /**
     * 软删除标准
     */
    @Modifying
    @Query("UPDATE Standard s SET s.deleted = 1, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void softDelete(@Param("id") Long id);
    
    /**
     * 更新监控状态
     */
    @Modifying
    @Query("UPDATE Standard s SET s.isMonitored = :isMonitored, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void updateMonitoringStatus(@Param("id") Long id, @Param("isMonitored") Boolean isMonitored);
    
    /**
     * 查询即将生效的标准
     */
    @Query("SELECT s FROM Standard s WHERE s.effectiveDate >= :currentDate AND s.effectiveDate <= :endDate AND s.deleted = 0 ORDER BY s.effectiveDate ASC")
    Page<Standard> findUpcomingStandards(@Param("currentDate") String currentDate, @Param("endDate") String endDate, Pageable pageable);
    
    /**
     * 统计即将生效标准的数量
     */
    @Query("SELECT COUNT(s) FROM Standard s WHERE s.effectiveDate >= :currentDate AND s.effectiveDate <= :endDate AND s.deleted = 0")
    long countUpcomingStandards(@Param("currentDate") String currentDate, @Param("endDate") String endDate);
    
    /**
     * 查询最近更新的标准
     */
    @Query("SELECT s FROM Standard s WHERE s.deleted = 0 ORDER BY s.updatedAt DESC")
    Page<Standard> findRecentlyUpdatedStandards(Pageable pageable);
    
    /**
     * 查询按发布时间排序的最新标准
     */
    @Query("SELECT s FROM Standard s WHERE s.deleted = 0 AND s.publishedDate IS NOT NULL ORDER BY s.publishedDate DESC")
    Page<Standard> findLatestStandardsByPublishedDate(Pageable pageable);
    
    /**
     * 查询按生效时间排序的即将生效标准（未来生效的标准）
     */
    @Query("SELECT s FROM Standard s WHERE s.deleted = 0 AND s.effectiveDate IS NOT NULL ORDER BY s.effectiveDate ASC")
    Page<Standard> findUpcomingStandardsByEffectiveDate(Pageable pageable);
    
    /**
     * 获取国家统计信息
     */
    @Query("SELECT s.country as country, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.country ORDER BY count DESC")
    List<Map<String, Object>> getCountryStatistics();
}
