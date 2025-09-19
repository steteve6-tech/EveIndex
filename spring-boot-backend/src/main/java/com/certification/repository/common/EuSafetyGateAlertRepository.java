package com.certification.repository.common;

import com.certification.entity.common.EuSafetyGateAlert;
import com.certification.entity.common.CrawlerData.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 欧盟安全门预警系统数据仓库接口
 */
@Repository
public interface EuSafetyGateAlertRepository extends JpaRepository<EuSafetyGateAlert, Long> {

    /**
     * 根据预警编号查找
     */
    Optional<EuSafetyGateAlert> findByAlertNumber(String alertNumber);

    /**
     * 根据预警编号判断是否存在
     */
    boolean existsByAlertNumber(String alertNumber);

    /**
     * 根据产品名称模糊查询
     */
    List<EuSafetyGateAlert> findByProductContaining(String product);

    /**
     * 根据产品名称模糊查询（分页）
     */
    Page<EuSafetyGateAlert> findByProductContaining(String product, Pageable pageable);

    /**
     * 根据品牌名称模糊查询
     */
    List<EuSafetyGateAlert> findByBrandContaining(String brand);

    /**
     * 根据品牌名称模糊查询（分页）
     */
    Page<EuSafetyGateAlert> findByBrandContaining(String brand, Pageable pageable);

    /**
     * 根据产品类别查询
     */
    List<EuSafetyGateAlert> findByCategory(String category);

    /**
     * 根据产品类别查询（分页）
     */
    Page<EuSafetyGateAlert> findByCategory(String category, Pageable pageable);

    /**
     * 根据风险类型查询
     */
    List<EuSafetyGateAlert> findByRiskType(String riskType);

    /**
     * 根据风险类型查询（分页）
     */
    Page<EuSafetyGateAlert> findByRiskType(String riskType, Pageable pageable);

    /**
     * 根据国家查询
     */
    List<EuSafetyGateAlert> findByCountry(String country);

    /**
     * 根据国家查询（分页）
     */
    Page<EuSafetyGateAlert> findByCountry(String country, Pageable pageable);

    /**
     * 根据通知国家查询
     */
    List<EuSafetyGateAlert> findByNotifyingCountry(String notifyingCountry);

    /**
     * 根据通知国家查询（分页）
     */
    Page<EuSafetyGateAlert> findByNotifyingCountry(String notifyingCountry, Pageable pageable);

    /**
     * 根据发布日期范围查询
     */
    List<EuSafetyGateAlert> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据发布日期范围查询（分页）
     */
    Page<EuSafetyGateAlert> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据风险等级查询
     */
    List<EuSafetyGateAlert> findByRiskLevel(RiskLevel riskLevel);

    /**
     * 根据风险等级查询（分页）
     */
    Page<EuSafetyGateAlert> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);


    /**
     * 根据关键词模糊查询
     */
    @Query("SELECT e FROM EuSafetyGateAlert e WHERE " +
           "e.product LIKE %:keyword% OR " +
           "e.productDescription LIKE %:keyword% OR " +
           "e.brand LIKE %:keyword% OR " +
           "e.category LIKE %:keyword% OR " +
           "e.risk LIKE %:keyword% OR " +
           "e.riskType LIKE %:keyword% OR " +
           "e.description LIKE %:keyword% OR " +
           "e.measures LIKE %:keyword%")
    List<EuSafetyGateAlert> findByKeyword(@Param("keyword") String keyword);

    /**
     * 根据关键词模糊查询（分页）
     */
    @Query("SELECT e FROM EuSafetyGateAlert e WHERE " +
           "e.product LIKE %:keyword% OR " +
           "e.productDescription LIKE %:keyword% OR " +
           "e.brand LIKE %:keyword% OR " +
           "e.category LIKE %:keyword% OR " +
           "e.risk LIKE %:keyword% OR " +
           "e.riskType LIKE %:keyword% OR " +
           "e.description LIKE %:keyword% OR " +
           "e.measures LIKE %:keyword%")
    Page<EuSafetyGateAlert> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据多个关键词查询
     */
    @Query("SELECT e FROM EuSafetyGateAlert e WHERE " +
           "e.product LIKE %:keyword1% OR e.product LIKE %:keyword2% OR " +
           "e.productDescription LIKE %:keyword1% OR e.productDescription LIKE %:keyword2% OR " +
           "e.brand LIKE %:keyword1% OR e.brand LIKE %:keyword2% OR " +
           "e.category LIKE %:keyword1% OR e.category LIKE %:keyword2% OR " +
           "e.risk LIKE %:keyword1% OR e.risk LIKE %:keyword2% OR " +
           "e.riskType LIKE %:keyword1% OR e.riskType LIKE %:keyword2% OR " +
           "e.description LIKE %:keyword1% OR e.description LIKE %:keyword2% OR " +
           "e.measures LIKE %:keyword1% OR e.measures LIKE %:keyword2%")
    List<EuSafetyGateAlert> findByKeywords(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2);

    /**
     * 根据多个关键词查询（分页）
     */
    @Query("SELECT e FROM EuSafetyGateAlert e WHERE " +
           "e.product LIKE %:keyword1% OR e.product LIKE %:keyword2% OR " +
           "e.productDescription LIKE %:keyword1% OR e.productDescription LIKE %:keyword2% OR " +
           "e.brand LIKE %:keyword1% OR e.brand LIKE %:keyword2% OR " +
           "e.category LIKE %:keyword1% OR e.category LIKE %:keyword2% OR " +
           "e.risk LIKE %:keyword1% OR e.risk LIKE %:keyword2% OR " +
           "e.riskType LIKE %:keyword1% OR e.riskType LIKE %:keyword2% OR " +
           "e.description LIKE %:keyword1% OR e.description LIKE %:keyword2% OR " +
           "e.measures LIKE %:keyword1% OR e.measures LIKE %:keyword2%")
    Page<EuSafetyGateAlert> findByKeywords(@Param("keyword1") String keyword1, @Param("keyword2") String keyword2, Pageable pageable);

    /**
     * 统计各国家的预警数量
     */
    @Query("SELECT e.country, COUNT(e) FROM EuSafetyGateAlert e GROUP BY e.country ORDER BY COUNT(e) DESC")
    List<Object[]> countByCountry();

    /**
     * 统计各产品类别的预警数量
     */
    @Query("SELECT e.category, COUNT(e) FROM EuSafetyGateAlert e GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> countByCategory();

    /**
     * 统计各风险类型的预警数量
     */
    @Query("SELECT e.riskType, COUNT(e) FROM EuSafetyGateAlert e GROUP BY e.riskType ORDER BY COUNT(e) DESC")
    List<Object[]> countByRiskType();

    /**
     * 统计各风险等级的预警数量
     */
    @Query("SELECT e.riskLevel, COUNT(e) FROM EuSafetyGateAlert e GROUP BY e.riskLevel ORDER BY COUNT(e) DESC")
    List<Object[]> countByRiskLevel();

    /**
     * 根据发布日期统计月度预警数量
     */
    @Query("SELECT YEAR(e.publicationDate), MONTH(e.publicationDate), COUNT(e) " +
           "FROM EuSafetyGateAlert e " +
           "WHERE e.publicationDate IS NOT NULL " +
           "GROUP BY YEAR(e.publicationDate), MONTH(e.publicationDate) " +
           "ORDER BY YEAR(e.publicationDate) DESC, MONTH(e.publicationDate) DESC")
    List<Object[]> countByMonth();

    /**
     * 根据发布日期统计年度预警数量
     */
    @Query("SELECT YEAR(e.publicationDate), COUNT(e) " +
           "FROM EuSafetyGateAlert e " +
           "WHERE e.publicationDate IS NOT NULL " +
           "GROUP BY YEAR(e.publicationDate) " +
           "ORDER BY YEAR(e.publicationDate) DESC")
    List<Object[]> countByYear();

    /**
     * 查找最新的预警记录
     */
    List<EuSafetyGateAlert> findTop10ByOrderByPublicationDateDesc();

    /**
     * 查找最新的预警记录（分页）
     */
    Page<EuSafetyGateAlert> findByOrderByPublicationDateDesc(Pageable pageable);

    /**
     * 根据爬取时间范围查询
     */
    List<EuSafetyGateAlert> findByCrawlTimeBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 根据爬取时间范围查询（分页）
     */
    Page<EuSafetyGateAlert> findByCrawlTimeBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, Pageable pageable);

    /**
     * 删除指定日期之前的数据
     */
    void deleteByCrawlTimeBefore(java.time.LocalDateTime dateTime);

    /**
     * 统计总记录数
     */
    long count();
}
