package com.certification.repository.common;

import com.certification.entity.common.CustomsCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 海关过往判例Repository接口
 */
@Repository
public interface CustomsCaseRepository extends JpaRepository<CustomsCase, Long> {
    
    /**
     * 根据判例编号查询
     */
    Optional<CustomsCase> findByCaseNumber(String caseNumber);
    
    /**
     * 根据判例日期查询
     */
    List<CustomsCase> findByCaseDate(LocalDate caseDate);
    
    /**
     * 查询指定日期范围内的判例
     */
    List<CustomsCase> findByCaseDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据HS编码查询
     */
    List<CustomsCase> findByHsCodeUsed(String hsCodeUsed);
    
    /**
     * 根据违规类型查询
     */
    List<CustomsCase> findByViolationType(String violationType);
    
    /**
     * 根据违规类型模糊查询
     */
    List<CustomsCase> findByViolationTypeContaining(String violationType);
    
    /**
     * 根据处罚金额范围查询
     */
    List<CustomsCase> findByPenaltyAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * 查询有处罚金额的判例
     */
    List<CustomsCase> findByPenaltyAmountIsNotNull();
    
    /**
     * 查询最近的判例
     */
    @Query("SELECT cc FROM CustomsCase cc ORDER BY cc.caseDate DESC")
    List<CustomsCase> findRecentCases();
    
    /**
     * 根据裁定结果模糊查询
     */
    List<CustomsCase> findByRulingResultContaining(String rulingResult);
    
    /**
     * 根据判例编号和日期检查是否存在
     */
    boolean existsByCaseNumberAndCaseDate(String caseNumber, LocalDate caseDate);
    
    /**
     * 根据数据来源查询
     */
    List<CustomsCase> findByDataSource(String dataSource);
    
    /**
     * 根据数据状态查询
     */
    List<CustomsCase> findByDataStatus(String dataStatus);
    
    /**
     * 根据爬取时间查询
     */
    List<CustomsCase> findByCrawlTimeBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据jd_country查询
     */
    List<CustomsCase> findByJdCountry(String jdCountry);
    
    /**
     * 根据jd_country和违规类型查询
     */
    List<CustomsCase> findByJdCountryAndViolationType(String jdCountry, String violationType);
    
    /**
     * 根据jd_country查询最近的判例
     */
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.jdCountry = :jdCountry ORDER BY cc.caseDate DESC")
    List<CustomsCase> findRecentCasesByJdCountry(@Param("jdCountry") String jdCountry);
    
    /**
     * 根据jd_country和HS编码查询
     */
    List<CustomsCase> findByJdCountryAndHsCodeUsed(String jdCountry, String hsCodeUsed);
    
    /**
     * 根据HS编码模糊查询（支持多个编码）
     */
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.hsCodeUsed LIKE %:hsCode%")
    List<CustomsCase> findByHsCodeUsedContaining(@Param("hsCode") String hsCode);
    
    /**
     * 根据jd_country和HS编码模糊查询
     */
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.jdCountry = :jdCountry AND cc.hsCodeUsed LIKE %:hsCode%")
    List<CustomsCase> findByJdCountryAndHsCodeUsedContaining(@Param("jdCountry") String jdCountry, @Param("hsCode") String hsCode);
    
    /**
     * 根据章编码查询（前4位）
     */
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.hsCodeUsed LIKE %:chapterCode%")
    List<CustomsCase> findByChapterCode(@Param("chapterCode") String chapterCode);
    
    /**
     * 根据jd_country和章编码查询
     */
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.jdCountry = :jdCountry AND cc.hsCodeUsed LIKE %:chapterCode%")
    List<CustomsCase> findByJdCountryAndChapterCode(@Param("jdCountry") String jdCountry, @Param("chapterCode") String chapterCode);

    /**
     * 根据风险等级查找记录
     */
    List<CustomsCase> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<CustomsCase> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    // 新增：按关键词搜索
    @Query("SELECT cc FROM CustomsCase cc WHERE cc.keywords LIKE %:keyword%")
    List<CustomsCase> findByKeywordsContaining(@Param("keyword") String keyword);
}
