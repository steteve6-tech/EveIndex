package com.certification.repository.common;

import com.certification.entity.common.DeviceRegistrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistrationRecordRepository extends JpaRepository<DeviceRegistrationRecord, Long> {

    List<DeviceRegistrationRecord> findByRegistrationNumber(String registrationNumber);

    List<DeviceRegistrationRecord> findByFeiNumber(String feiNumber);

    // 显式指定查询，避免属性名大小写解析问题
    // @Query("select r from DeviceRegistrationRecord r where r.kNumber = :kNumber")
    // Optional<DeviceRegistrationRecord> findByKNumber(@Param("kNumber") String kNumber);

    @Query("select r from DeviceRegistrationRecord r where r.manufacturerName like concat('%', :name, '%')")
    List<DeviceRegistrationRecord> findByManufacturerNameLike(@Param("name") String manufacturerNamePart);

    List<DeviceRegistrationRecord> findByDataSource(String dataSource);

    // 新增：按国家筛选
    List<DeviceRegistrationRecord> findByJdCountry(String jdCountry);

    // 新增：按数据源+国家筛选
    List<DeviceRegistrationRecord> findByDataSourceAndJdCountry(String dataSource, String jdCountry);
    /**
     * 根据风险等级查找记录
     */
    List<DeviceRegistrationRecord> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    /**
     * 根据风险等级查找记录（分页）
     */
    org.springframework.data.domain.Page<DeviceRegistrationRecord> findByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel, org.springframework.data.domain.Pageable pageable);

    /**
     * 统计指定风险等级的记录数量
     */
    long countByRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel riskLevel);

    // 新增：按设备名称搜索
    @Query("SELECT r FROM DeviceRegistrationRecord r WHERE r.deviceName LIKE %:keyword%")
    List<DeviceRegistrationRecord> findByDeviceNameContaining(@Param("keyword") String keyword);

    // 新增：按设备类别搜索
    @Query("SELECT r FROM DeviceRegistrationRecord r WHERE r.deviceClass LIKE %:keyword%")
    List<DeviceRegistrationRecord> findByDeviceClassContaining(@Param("keyword") String keyword);

    // 新增：按专有名称搜索
    @Query("SELECT r FROM DeviceRegistrationRecord r WHERE r.proprietaryName LIKE %:keyword%")
    List<DeviceRegistrationRecord> findByProprietaryNameContaining(@Param("keyword") String keyword);

    // 新增：按关键词搜索
    @Query("SELECT r FROM DeviceRegistrationRecord r WHERE r.keywords LIKE %:keyword%")
    List<DeviceRegistrationRecord> findByKeywordsContaining(@Param("keyword") String keyword);

    // 新增：复合查询方法 - 用于重复检查
    @Query("SELECT r FROM DeviceRegistrationRecord r WHERE r.registrationNumber = :registrationNumber AND r.feiNumber = :feiNumber AND r.deviceName = :deviceName")
    List<DeviceRegistrationRecord> findByRegistrationNumberAndFeiNumberAndDeviceName(
            @Param("registrationNumber") String registrationNumber,
            @Param("feiNumber") String feiNumber,
            @Param("deviceName") String deviceName);
}
