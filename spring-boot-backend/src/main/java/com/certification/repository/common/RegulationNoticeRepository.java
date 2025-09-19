package com.certification.repository.common;

import com.certification.entity.common.RegulationNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 相关法规通知Repository接口
 */
@Repository
public interface RegulationNoticeRepository extends JpaRepository<RegulationNotice, Long> {
    
    /**
     * 根据国家ID查询法规通知
     */
    List<RegulationNotice> findByCountryId(Long countryId);
    
    /**
     * 根据法规编号查询
     */
    Optional<RegulationNotice> findByNoticeNumber(String noticeNumber);
    
    /**
     * 根据发布机构查询
     */
    List<RegulationNotice> findByIssuingAuthority(String issuingAuthority);
    
    /**
     * 根据生效日期查询
     */
    List<RegulationNotice> findByEffectiveDate(LocalDate effectiveDate);
    
    /**
     * 查询指定日期范围内的法规通知
     */
    List<RegulationNotice> findByEffectiveDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据发布时间查询
     */
    List<RegulationNotice> findByPublishTime(LocalDateTime publishTime);
    
    /**
     * 查询指定时间范围内的法规通知
     */
    List<RegulationNotice> findByPublishTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据标题模糊查询
     */
    List<RegulationNotice> findByNoticeTitleContaining(String noticeTitle);
    
    /**
     * 根据内容摘要模糊查询
     */
    List<RegulationNotice> findByContentSummaryContaining(String contentSummary);
    
    /**
     * 根据关联HS编码查询
     */
    List<RegulationNotice> findByRelatedHsCodesContaining(String hsCode);
    
    /**
     * 根据关联产品类型查询
     */
    List<RegulationNotice> findByRelatedProductTypesContaining(String productType);
    
    /**
     * 根据国家ID和发布机构查询
     */
    List<RegulationNotice> findByCountryIdAndIssuingAuthority(Long countryId, String issuingAuthority);
    
    /**
     * 查询最近的法规通知
     */
    @Query("SELECT rn FROM RegulationNotice rn ORDER BY rn.publishTime DESC")
    List<RegulationNotice> findRecentNotices();
    
    /**
     * 根据国家ID查询最近的法规通知
     */
    @Query("SELECT rn FROM RegulationNotice rn WHERE rn.countryId = :countryId ORDER BY rn.publishTime DESC")
    List<RegulationNotice> findRecentNoticesByCountryId(@Param("countryId") Long countryId);
    
    /**
     * 根据发布机构查询最近的法规通知
     */
    @Query("SELECT rn FROM RegulationNotice rn WHERE rn.issuingAuthority = :issuingAuthority ORDER BY rn.publishTime DESC")
    List<RegulationNotice> findRecentNoticesByIssuingAuthority(@Param("issuingAuthority") String issuingAuthority);
    
    /**
     * 查询即将生效的法规通知
     */
    @Query("SELECT rn FROM RegulationNotice rn WHERE rn.effectiveDate BETWEEN :startDate AND :endDate")
    List<RegulationNotice> findUpcomingNotices(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 根据国家ID列表查询法规通知
     */
    List<RegulationNotice> findByCountryIdIn(List<Long> countryIds);
    
    /**
     * 根据发布机构列表查询法规通知
     */
    List<RegulationNotice> findByIssuingAuthorityIn(List<String> issuingAuthorities);
    
    /**
     * 根据标题或内容摘要模糊查询
     */
    @Query("SELECT rn FROM RegulationNotice rn WHERE rn.noticeTitle LIKE %:keyword% OR rn.contentSummary LIKE %:keyword%")
    List<RegulationNotice> findByKeyword(@Param("keyword") String keyword);
}
