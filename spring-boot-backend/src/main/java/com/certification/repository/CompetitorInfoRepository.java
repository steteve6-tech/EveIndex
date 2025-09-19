package com.certification.repository;

import com.certification.entity.CompetitorInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞品信息数据访问层
 */
@Repository
public interface CompetitorInfoRepository extends JpaRepository<CompetitorInfo, Long> {

    /**
     * 根据设备名称模糊查询
     */
    Page<CompetitorInfo> findByDeviceNameContainingIgnoreCase(String deviceName, Pageable pageable);

    /**
     * 根据制造商/品牌名称模糊查询
     */
    Page<CompetitorInfo> findByManufacturerBrandContainingIgnoreCase(String manufacturerBrand, Pageable pageable);

    /**
     * 根据设备代码查询
     */
    List<CompetitorInfo> findByDeviceCode(String deviceCode);

    /**
     * 根据数据来源查询
     */
    Page<CompetitorInfo> findByDataSource(String dataSource, Pageable pageable);

    /**
     * 根据状态查询
     */
    Page<CompetitorInfo> findByStatus(CompetitorInfo.Status status, Pageable pageable);

    /**
     * 根据风险等级查询
     */
    Page<CompetitorInfo> findByRiskLevel(CompetitorInfo.RiskLevel riskLevel, Pageable pageable);

    /**
     * 综合查询：根据关键词、状态、数据来源、风险等级进行分页查询
     */
    @Query("SELECT c FROM CompetitorInfo c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.deviceName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.manufacturerBrand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.deviceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:dataSource IS NULL OR c.dataSource = :dataSource) AND " +
           "(:riskLevel IS NULL OR c.riskLevel = :riskLevel)")
    Page<CompetitorInfo> findByKeywordAndFilters(
            @Param("keyword") String keyword,
            @Param("status") CompetitorInfo.Status status,
            @Param("dataSource") String dataSource,
            @Param("riskLevel") CompetitorInfo.RiskLevel riskLevel,
            Pageable pageable);

    /**
     * 统计总记录数
     */
    @Query("SELECT COUNT(c) FROM CompetitorInfo c")
    long countTotalRecords();

    /**
     * 统计活跃竞品数量
     */
    @Query("SELECT COUNT(c) FROM CompetitorInfo c WHERE c.status = 'ACTIVE'")
    long countActiveCompetitors();

    /**
     * 统计本月新增数量
     */
    @Query("SELECT COUNT(c) FROM CompetitorInfo c WHERE c.createdAt >= :startOfMonth")
    long countMonthlyNew(@Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * 统计高风险提醒数量
     */
    @Query("SELECT COUNT(c) FROM CompetitorInfo c WHERE c.riskLevel = 'HIGH'")
    long countRiskAlerts();

    /**
     * 根据数据来源统计数量
     */
    @Query("SELECT c.dataSource, COUNT(c) FROM CompetitorInfo c GROUP BY c.dataSource")
    List<Object[]> countByDataSource();

    /**
     * 根据状态统计数量
     */
    @Query("SELECT c.status, COUNT(c) FROM CompetitorInfo c GROUP BY c.status")
    List<Object[]> countByStatus();

    /**
     * 根据风险等级统计数量
     */
    @Query("SELECT c.riskLevel, COUNT(c) FROM CompetitorInfo c GROUP BY c.riskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * 删除所有数据
     */
    @Query("DELETE FROM CompetitorInfo")
    void deleteAllData();

    /**
     * 根据设备代码和制造商检查是否存在重复记录
     */
    boolean existsByDeviceCodeAndManufacturerBrand(String deviceCode, String manufacturerBrand);

    /**
     * 查找即将过期的设备（30天内）
     */
    @Query("SELECT c FROM CompetitorInfo c WHERE c.expiryDate IS NOT NULL AND c.expiryDate BETWEEN :now AND :thirtyDaysLater")
    List<CompetitorInfo> findExpiringDevices(@Param("now") LocalDateTime now, @Param("thirtyDaysLater") LocalDateTime thirtyDaysLater);
}
