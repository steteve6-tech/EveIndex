package com.certification.repository;

import com.certification.entity.common.CertNewsDailyCountryRiskStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日国家高风险数据统计Repository
 */
@Repository
public interface DailyCountryRiskStatsRepository extends JpaRepository<CertNewsDailyCountryRiskStats, Long> {

    /**
     * 根据日期和国家查询统计记录
     */
    CertNewsDailyCountryRiskStats findByStatDateAndCountryAndDeletedFalse(LocalDate statDate, String country);

    /**
     * 根据日期查询所有国家的统计记录
     */
    List<CertNewsDailyCountryRiskStats> findByStatDateAndDeletedFalseOrderByHighRiskCountDesc(LocalDate statDate);

    /**
     * 根据日期范围查询统计记录
     */
    List<CertNewsDailyCountryRiskStats> findByStatDateBetweenAndDeletedFalseOrderByStatDateDescCountryAsc(
            LocalDate startDate, LocalDate endDate);


    /**
     * 查询指定日期范围内所有国家的统计数据
     */
    @Query("SELECT d FROM CertNewsDailyCountryRiskStats d WHERE d.statDate BETWEEN :startDate AND :endDate AND d.deleted = false ORDER BY d.statDate DESC, d.highRiskCount DESC")
    List<CertNewsDailyCountryRiskStats> findByDateRangeOrderByDateAndHighRiskCount(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    /**
     * 查询指定国家在指定日期范围内的统计数据
     */
    @Query("SELECT d FROM CertNewsDailyCountryRiskStats d WHERE d.country = :country AND d.statDate BETWEEN :startDate AND :endDate AND d.deleted = false ORDER BY d.statDate ASC")
    List<CertNewsDailyCountryRiskStats> findByCountryAndDateRangeOrderByDate(
            @Param("country") String country, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    /**
     * 查询最新的统计数据（按日期倒序）
     */
    @Query("SELECT d FROM CertNewsDailyCountryRiskStats d WHERE d.deleted = false ORDER BY d.statDate DESC, d.highRiskCount DESC")
    List<CertNewsDailyCountryRiskStats> findLatestStats();

    /**
     * 查询指定日期范围内各国高风险数据的总和
     */
    @Query("SELECT d.country as country, SUM(d.highRiskCount) as totalHighRisk FROM CertNewsDailyCountryRiskStats d WHERE d.statDate BETWEEN :startDate AND :endDate AND d.deleted = false GROUP BY d.country ORDER BY totalHighRisk DESC")
    List<Object[]> getCountryHighRiskSumByDateRange(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    /**
     * 查询指定国家在指定日期范围内的高风险数据趋势
     */
    @Query("SELECT d.statDate as statDate, d.highRiskCount as highRiskCount FROM CertNewsDailyCountryRiskStats d WHERE d.country = :country AND d.statDate BETWEEN :startDate AND :endDate AND d.deleted = false ORDER BY d.statDate ASC")
    List<Object[]> getCountryHighRiskTrend(
            @Param("country") String country, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    /**
     * 根据国家查询所有统计记录
     */
    List<CertNewsDailyCountryRiskStats> findByCountryAndDeletedFalse(String country);

    /**
     * 根据日期范围查询统计记录（按日期和国家排序）
     */
    List<CertNewsDailyCountryRiskStats> findByStatDateBetweenAndDeletedFalseOrderByStatDateAscCountryAsc(
            LocalDate startDate, LocalDate endDate);

    /**
     * 根据国家和日期范围查询统计记录（按日期排序）
     */
    List<CertNewsDailyCountryRiskStats> findByCountryAndStatDateBetweenAndDeletedFalseOrderByStatDateAsc(
            String country, LocalDate startDate, LocalDate endDate);
}
