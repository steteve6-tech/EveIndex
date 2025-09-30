package com.certification.repository.common;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.DeviceRecallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRecallRecordRepository extends JpaRepository<DeviceRecallRecord, Long> {

    Optional<DeviceRecallRecord> findByCfresId(String cfresId);

    boolean existsByCfresId(String cfresId);

    List<DeviceRecallRecord> findByRecallStatus(String recallStatus);

    List<DeviceRecallRecord> findByProductCode(String productCode);

    List<DeviceRecallRecord> findByProductDescriptionContaining(String keyword);

    List<DeviceRecallRecord> findByRecallingFirmContaining(String recallingFirm);

    List<DeviceRecallRecord> findByDeviceNameContaining(String deviceName);

    List<DeviceRecallRecord> findByEventDatePosted(LocalDate date);

    List<DeviceRecallRecord> findByEventDatePostedBetween(LocalDate start, LocalDate end);

    @Query("SELECT drr FROM DeviceRecallRecord drr ORDER BY drr.eventDatePosted DESC")
    List<DeviceRecallRecord> findRecent();

    @Query("SELECT drr FROM DeviceRecallRecord drr WHERE drr.productCode = :productCode ORDER BY drr.eventDatePosted DESC")
    List<DeviceRecallRecord> findRecentByProductCode(@Param("productCode") String productCode);
    /**
     * 根据风险等级查找记录
     */
    List<DeviceRecallRecord> findByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<DeviceRecallRecord> findByRiskLevel(CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据关键词搜索（支持多个字段）
     */
    @Query("SELECT d FROM DeviceRecallRecord d WHERE " +
           "(d.productDescription LIKE %:keyword% OR " +
           "d.recallingFirm LIKE %:keyword% OR " +
           "d.deviceName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    List<DeviceRecallRecord> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode);

    /**
     * 根据关键词搜索（支持多个字段，分页）
     */
    @Query("SELECT d FROM DeviceRecallRecord d WHERE " +
           "(d.productDescription LIKE %:keyword% OR " +
           "d.recallingFirm LIKE %:keyword% OR " +
           "d.deviceName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    org.springframework.data.domain.Page<DeviceRecallRecord> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据国家搜索
     */
    List<DeviceRecallRecord> findByJdCountry(String jdCountry);
}