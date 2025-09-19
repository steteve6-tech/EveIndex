package com.certification.repository;

import com.certification.entity.common.Standard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 标准管理Repository接口
 * 使用common包下的Standard实体
 */
@Repository
public interface StandardManagementRepository extends JpaRepository<Standard, Long> {
    
    /**
     * 根据标准编号查询标准
     */
    Standard findByStandardNumberAndDeleted(String standardNumber, Integer deleted);
    
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
     * 查询监控中的标准（分页）
     */
    Page<Standard> findByIsMonitoredAndDeleted(Boolean isMonitored, Integer deleted, Pageable pageable);
    
    /**
     * 统计各风险等级的数量
     */
    @Query("SELECT s.riskLevel as riskLevel, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.riskLevel")
    List<Map<String, Object>> countByRiskLevel();
    
    /**
     * 统计各状态的数量
     */
    @Query("SELECT s.standardStatus as standardStatus, COUNT(s) as count FROM Standard s WHERE s.deleted = 0 GROUP BY s.standardStatus")
    List<Map<String, Object>> countByStatus();
    
    /**
     * 查询即将到期的标准
     */
    List<Standard> findByComplianceDeadlineLessThanEqualAndDeletedOrderByComplianceDeadlineAsc(LocalDate deadline, Integer deleted);
    
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
}
