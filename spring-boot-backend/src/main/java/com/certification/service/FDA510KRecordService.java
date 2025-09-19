package com.certification.service;

import com.certification.entity.newcommon.D_510KRecord;
import com.certification.repository.common.FDA510KRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FDA 510K记录服务类
 */
@Slf4j
@Service
@Transactional
public class FDA510KRecordService {

    @Autowired
    private FDA510KRecordRepository fda510KRecordRepository;

    /**
     * 保存FDA 510K记录
     */
    public D_510KRecord save(D_510KRecord record) {
        log.info("保存FDA 510K记录: {}", record.getKNumber());
        return fda510KRecordRepository.save(record);
    }

    /**
     * 批量保存FDA 510K记录
     */
    public List<D_510KRecord> saveAll(List<D_510KRecord> records) {
        log.info("批量保存FDA 510K记录，数量: {}", records.size());
        return fda510KRecordRepository.saveAll(records);
    }

    /**
     * 根据ID查找记录
     */
    public Optional<D_510KRecord> findById(Long id) {
        return fda510KRecordRepository.findById(id);
    }

    /**
     * 根据K号查找记录
     */
    public Optional<D_510KRecord> findByKNumber(String kNumber) {
        return fda510KRecordRepository.findBykNumber(kNumber);
    }

    /**
     * 检查K号是否存在
     */
    public boolean existsByKNumber(String kNumber) {
        return fda510KRecordRepository.existsBykNumber(kNumber);
    }

    /**
     * 根据设备名称查询
     */
    public List<D_510KRecord> findByDeviceName(String deviceName) {
        return fda510KRecordRepository.findByDeviceNameContainingIgnoreCase(deviceName);
    }

    /**
     * 根据设备名称查询（分页）
     */
    public Page<D_510KRecord> findByDeviceName(String deviceName, Pageable pageable) {
        return fda510KRecordRepository.findByDeviceNameContainingIgnoreCase(deviceName, pageable);
    }

    /**
     * 根据申请人查询
     */
    public List<D_510KRecord> findByApplicant(String applicant) {
        return fda510KRecordRepository.findByApplicantContainingIgnoreCase(applicant);
    }

    /**
     * 根据申请人查询（分页）
     */
    public Page<D_510KRecord> findByApplicant(String applicant, Pageable pageable) {
        return fda510KRecordRepository.findByApplicantContainingIgnoreCase(applicant, pageable);
    }

    /**
     * 根据日期范围查询
     */
    public List<D_510KRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return fda510KRecordRepository.findByDecisionDateBetween(startDate, endDate);
    }

    /**
     * 根据日期范围查询（分页）
     */
    public Page<D_510KRecord> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return fda510KRecordRepository.findByDecisionDateBetween(startDate, endDate, pageable);
    }

    /**
     * 复合查询：设备名称、申请人和日期范围
     */
    public List<D_510KRecord> findByDeviceNameAndApplicantAndDateRange(
            String deviceName, String applicant, LocalDate startDate, LocalDate endDate) {
        return fda510KRecordRepository.findByDeviceNameAndApplicantAndDateRange(
                deviceName, applicant, startDate, endDate);
    }

    /**
     * 获取最新记录
     */
    public List<D_510KRecord> findLatestRecords(Pageable pageable) {
        return fda510KRecordRepository.findLatestRecords(pageable);
    }

    /**
     * 统计指定日期范围内的记录数
     */
    public long countByDateRange(LocalDate startDate, LocalDate endDate) {
        return fda510KRecordRepository.countByDecisionDateRange(startDate, endDate);
    }

    /**
     * 统计指定申请人的记录数
     */
    public long countByApplicant(String applicant) {
        return fda510KRecordRepository.countByApplicantContainingIgnoreCase(applicant);
    }

    /**
     * 统计指定设备名称的记录数
     */
    public long countByDeviceName(String deviceName) {
        return fda510KRecordRepository.countByDeviceNameContainingIgnoreCase(deviceName);
    }

    /**
     * 从Map数据创建D_510KRecord对象
     */
    public D_510KRecord createFromMap(Map<String, Object> data) {
        D_510KRecord record = new D_510KRecord();
        
        record.setDeviceName((String) data.get("deviceName"));
        record.setApplicant((String) data.get("applicant"));
        record.setKNumber((String) data.get("kNumber"));
        record.setDecisionDate((LocalDate) data.get("decisionDate"));
        record.setDeviceUrl((String) data.get("deviceUrl"));
        record.setDataSource((String) data.get("dataSource"));
        record.setCountryCode((String) data.get("countryCode"));
        record.setSourceCountry((String) data.get("jdCountry"));
        record.setCrawlTime((LocalDateTime) data.get("crawlTime"));
        record.setDataStatus((String) data.get("dataStatus"));
        record.setDecisionDateStr((String) data.get("decisionDateStr"));
        
        return record;
    }

    /**
     * 批量保存爬虫数据，避免重复
     */
    public int saveCrawlerData(List<Map<String, Object>> crawlerResults) {
        int savedCount = 0;
        int skippedCount = 0;
        
        for (Map<String, Object> data : crawlerResults) {
            String kNumber = (String) data.get("kNumber");
            LocalDate decisionDate = (LocalDate) data.get("decisionDate");
            
            // 检查是否已存在相同的记录
            if (fda510KRecordRepository.existsBykNumberAndDecisionDate(kNumber, decisionDate)) {
                log.debug("跳过已存在的记录: {}", kNumber);
                skippedCount++;
                continue;
            }
            
            // 创建新记录并保存
            D_510KRecord record = createFromMap(data);
            fda510KRecordRepository.save(record);
            savedCount++;
        }
        
        log.info("批量保存完成，新增: {} 条，跳过: {} 条", savedCount, skippedCount);
        return savedCount;
    }

    /**
     * 删除指定日期之前的记录
     */
    public void deleteOldRecords(LocalDateTime beforeTime) {
        log.info("删除指定时间之前的记录: {}", beforeTime);
        fda510KRecordRepository.deleteByCrawlTimeBefore(beforeTime);
    }

    /**
     * 获取所有记录（分页）
     */
    public Page<D_510KRecord> findAll(Pageable pageable) {
        return fda510KRecordRepository.findAll(pageable);
    }

    /**
     * 获取所有记录
     */
    public List<D_510KRecord> findAll() {
        return fda510KRecordRepository.findAll();
    }

    /**
     * 删除记录
     */
    public void deleteById(Long id) {
        fda510KRecordRepository.deleteById(id);
    }

    /**
     * 删除记录
     */
    public void delete(D_510KRecord record) {
        fda510KRecordRepository.delete(record);
    }
}
