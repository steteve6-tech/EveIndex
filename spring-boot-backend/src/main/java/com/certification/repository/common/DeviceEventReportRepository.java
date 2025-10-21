package com.certification.repository.common;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.DeviceEventReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceEventReportRepository extends JpaRepository<DeviceEventReport, Long> {

    Optional<DeviceEventReport> findByReportNumber(String reportNumber);
    
    // 检查报告编号是否存在
    boolean existsByReportNumber(String reportNumber);

    List<DeviceEventReport> findByDateReceived(LocalDate dateReceived);

    @Query("select d from DeviceEventReport d where d.dateReceived >= :start and d.dateReceived <= :end")
    List<DeviceEventReport> findByDateReceivedBetween(@Param("start") LocalDate start,
                                                      @Param("end") LocalDate end);

    List<DeviceEventReport> findByDataSource(String dataSource);

    // 新增：按国家过滤
    List<DeviceEventReport> findByJdCountry(String jdCountry);
    
    // 新增：按国家过滤（分页）
    org.springframework.data.domain.Page<DeviceEventReport> findByJdCountry(String jdCountry, org.springframework.data.domain.Pageable pageable);

    // 常用组合查询
    List<DeviceEventReport> findByDataSourceAndJdCountry(String dataSource, String jdCountry);
    /**
     * 根据风险等级查找记录
     */
    List<DeviceEventReport> findByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<DeviceEventReport> findByRiskLevel(CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(CertNewsData.RiskLevel riskLevel);
    
    /**
     * 根据风险等级和国家查找记录
     */
    List<DeviceEventReport> findByRiskLevelAndJdCountry(CertNewsData.RiskLevel riskLevel, String jdCountry);
    
    /**
     * 根据国家和风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<DeviceEventReport> findByJdCountryAndRiskLevel(String jdCountry, CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据品牌名称搜索（忽略大小写）
     */
    List<DeviceEventReport> findByBrandNameContainingIgnoreCase(String brandName);

    /**
     * 根据制造商名称搜索（忽略大小写）
     */
    List<DeviceEventReport> findByManufacturerNameContainingIgnoreCase(String manufacturerName);

    /**
     * 根据通用名称搜索（忽略大小写）
     */
    List<DeviceEventReport> findByGenericNameContainingIgnoreCase(String genericName);

    /**
     * 根据关键词搜索（忽略大小写）
     * 注意：keywords字段是TEXT类型，需要使用CAST转换
     */
    @Query("SELECT d FROM DeviceEventReport d WHERE LOWER(CAST(d.keywords AS string)) LIKE LOWER(CONCAT('%', :keywords, '%'))")
    List<DeviceEventReport> findByKeywordsContainingIgnoreCase(@Param("keywords") String keywords);

    /**
     * 根据关键词搜索（支持多个字段）
     */
    @Query("SELECT d FROM DeviceEventReport d WHERE " +
           "(d.brandName LIKE %:keyword% OR " +
           "d.manufacturerName LIKE %:keyword% OR " +
           "d.genericName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    List<DeviceEventReport> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode);

    /**
     * 根据关键词搜索（支持多个字段，分页）
     */
    @Query("SELECT d FROM DeviceEventReport d WHERE " +
           "(d.brandName LIKE %:keyword% OR " +
           "d.manufacturerName LIKE %:keyword% OR " +
           "d.genericName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    org.springframework.data.domain.Page<DeviceEventReport> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计新增数据数量
     */
    long countByIsNew(Boolean isNew);

    /**
     * 查找新增数据（分页）
     */
    org.springframework.data.domain.Page<DeviceEventReport> findByIsNew(Boolean isNew, org.springframework.data.domain.Pageable pageable);

    /**
     * 查找已查看的新增数据
     */
    List<DeviceEventReport> findByIsNewAndNewDataViewed(Boolean isNew, Boolean newDataViewed);
}
