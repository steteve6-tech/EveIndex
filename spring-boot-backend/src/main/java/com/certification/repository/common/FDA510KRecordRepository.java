package com.certification.repository.common;

import com.certification.entity.newcommon.D_510KRecord;
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
 * FDA 510K记录Repository接口
 */
@Repository
public interface FDA510KRecordRepository extends JpaRepository<D_510KRecord, Long> {

    /**
     * 根据K号查找记录
     */
    Optional<D_510KRecord> findBykNumber(String kNumber);

    /**
     * 根据K号检查记录是否存在
     */
    boolean existsBykNumber(String kNumber);

    /**
     * 根据设备名称模糊查询
     */
    List<D_510KRecord> findByDeviceNameContainingIgnoreCase(String deviceName);

    /**
     * 根据设备名称模糊查询（分页）
     */
    Page<D_510KRecord> findByDeviceNameContainingIgnoreCase(String deviceName, Pageable pageable);

    /**
     * 根据申请人模糊查询
     */
    List<D_510KRecord> findByApplicantContainingIgnoreCase(String applicant);

    /**
     * 根据申请人模糊查询（分页）
     */
    Page<D_510KRecord> findByApplicantContainingIgnoreCase(String applicant, Pageable pageable);

    /**
     * 根据决策日期范围查询
     */
    List<D_510KRecord> findByDecisionDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据决策日期范围查询（分页）
     */
    Page<D_510KRecord> findByDecisionDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 根据数据状态查询
     */
    List<D_510KRecord> findByDataStatus(String dataStatus);

    /**
     * 根据爬取时间范围查询
     */
    List<D_510KRecord> findByCrawlTimeBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 根据设备名称和申请人查询
     */
    List<D_510KRecord> findByDeviceNameContainingIgnoreCaseAndApplicantContainingIgnoreCase(
            String deviceName, String applicant);

    /**
     * 根据设备名称、申请人和日期范围查询
     */
    @Query("SELECT f FROM D_510KRecord f WHERE " +
           "LOWER(f.deviceName) LIKE LOWER(CONCAT('%', :deviceName, '%')) AND " +
           "LOWER(f.applicant) LIKE LOWER(CONCAT('%', :applicant, '%')) AND " +
           "f.decisionDate BETWEEN :startDate AND :endDate")
    List<D_510KRecord> findByDeviceNameAndApplicantAndDateRange(
            @Param("deviceName") String deviceName,
            @Param("applicant") String applicant,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内的记录数
     */
    @Query("SELECT COUNT(f) FROM D_510KRecord f WHERE f.decisionDate BETWEEN :startDate AND :endDate")
    long countByDecisionDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定申请人的记录数
     */
    long countByApplicantContainingIgnoreCase(String applicant);

    /**
     * 统计指定设备名称的记录数
     */
    long countByDeviceNameContainingIgnoreCase(String deviceName);

    /**
     * 查找最新的爬取记录
     */
    @Query("SELECT f FROM D_510KRecord f ORDER BY f.crawlTime DESC")
    List<D_510KRecord> findLatestRecords(Pageable pageable);

    /**
     * 根据K号和决策日期查找记录
     */
    Optional<D_510KRecord> findBykNumberAndDecisionDate(String kNumber, LocalDate decisionDate);

    /**
     * 检查是否存在相同的K号和决策日期记录
     */
    boolean existsBykNumberAndDecisionDate(String kNumber, LocalDate decisionDate);

    /**
     * 删除指定日期之前的记录
     */
    @Query("DELETE FROM D_510KRecord f WHERE f.crawlTime < :beforeTime")
    void deleteByCrawlTimeBefore(@Param("beforeTime") java.time.LocalDateTime beforeTime);
}
