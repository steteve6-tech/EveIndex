package com.certification.repository.common;

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

    List<Device510K> findByProductCode(String productCode);

    List<Device510K> findByApplicantContaining(String applicant);

    List<Device510K> findByDeviceNameContaining(String deviceName);

    List<Device510K> findByDateReceived(LocalDate dateReceived);

    List<Device510K> findByCountryCode(String countryCode);

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
    List<Device510K> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<Device510K> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    /**
     * 根据OpenFDA数据模糊查询
     */
    List<Device510K> findByOpenfdaContaining(String openfda);
}
