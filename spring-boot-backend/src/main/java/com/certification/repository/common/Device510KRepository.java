package com.certification.repository.common;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.Device510K;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface Device510KRepository extends JpaRepository<Device510K, Long> {

    @Query("select d from Device510K d where d.kNumber = :kNumber")
    Optional<Device510K> findByKNumber(@Param("kNumber") String kNumber);


    List<Device510K> findByApplicantContaining(String applicant);

    List<Device510K> findByDeviceNameContaining(String deviceName);

    List<Device510K> findByDateReceived(LocalDate dateReceived);

    List<Device510K> findByCountryCode(String countryCode);
    
    /**
     * 根据国家代码查找记录（分页）
     */
    org.springframework.data.domain.Page<Device510K> findByCountryCode(String countryCode, org.springframework.data.domain.Pageable pageable);

    List<Device510K> findByDataSource(String dataSource);

    List<Device510K> findByJdCountry(String jdCountry);

    /**
     * 根据品牌名称模糊查询
     */
    List<Device510K> findByTradeNameContaining(String tradeName);

    /**
     * 根据设备类别查询
     */
    List<Device510K> findByDeviceClass(String deviceClass);

    /**
     * 检查是否存在相同的K号记录
     */
    boolean existsBykNumber(String kNumber);

    /**
     * 根据风险等级查找记录
     */
    List<Device510K> findByRiskLevel(CertNewsData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<Device510K> findByRiskLevel(CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(CertNewsData.RiskLevel riskLevel);
    
    /**
     * 根据风险等级和国家查找记录
     */
    List<Device510K> findByRiskLevelAndJdCountry(CertNewsData.RiskLevel riskLevel, String jdCountry);
    
    /**
     * 根据国家代码和风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<Device510K> findByCountryCodeAndRiskLevel(String countryCode, CertNewsData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 根据关键词搜索（支持多个字段）
     */
    @Query("SELECT d FROM Device510K d WHERE " +
           "(d.deviceName LIKE %:keyword% OR " +
           "d.applicant LIKE %:keyword% OR " +
           "d.tradeName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    List<Device510K> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode);

    /**
     * 根据关键词搜索（支持多个字段，分页）
     */
    @Query("SELECT d FROM Device510K d WHERE " +
           "(d.deviceName LIKE %:keyword% OR " +
           "d.applicant LIKE %:keyword% OR " +
           "d.tradeName LIKE %:keyword%) " +
           "AND (:countryCode IS NULL OR d.jdCountry = :countryCode)")
    org.springframework.data.domain.Page<Device510K> findByKeywordAndCountry(@Param("keyword") String keyword, @Param("countryCode") String countryCode, org.springframework.data.domain.Pageable pageable);

}
