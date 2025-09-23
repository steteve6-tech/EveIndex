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

    List<DeviceEventReport> findByEventType(String eventType);

    List<DeviceEventReport> findByDateReceived(LocalDate dateReceived);

    @Query("select d from DeviceEventReport d where d.dateReceived >= :start and d.dateReceived <= :end")
    List<DeviceEventReport> findByDateReceivedBetween(@Param("start") LocalDate start,
                                                      @Param("end") LocalDate end);

    List<DeviceEventReport> findByDataSource(String dataSource);

    // 新增：按国家过滤
    List<DeviceEventReport> findByJdCountry(String jdCountry);

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
     */
    List<DeviceEventReport> findByKeywordsContainingIgnoreCase(String keywords);
}
