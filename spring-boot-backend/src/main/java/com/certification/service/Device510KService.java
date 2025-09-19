package com.certification.service;

import com.certification.entity.common.Device510K;
import com.certification.repository.common.Device510KRepository;
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
 * Device510K服务类
 * 提供Device510K实体的业务逻辑处理
 */
@Slf4j
@Service
@Transactional
public class Device510KService {

    @Autowired
    private Device510KRepository device510KRepository;

    /**
     * 保存Device510K记录
     */
    public Device510K save(Device510K record) {
        log.info("保存Device510K记录: {}", record.getKNumber());
        return device510KRepository.save(record);
    }

    /**
     * 批量保存Device510K记录
     */
    public List<Device510K> saveAll(List<Device510K> records) {
        log.info("批量保存Device510K记录，数量: {}", records.size());
        return device510KRepository.saveAll(records);
    }

    /**
     * 根据ID查找记录
     */
    public Optional<Device510K> findById(Long id) {
        return device510KRepository.findById(id);
    }

    /**
     * 根据K号查找记录
     */
    public Optional<Device510K> findByKNumber(String kNumber) {
        return device510KRepository.findByKNumber(kNumber);
    }

    /**
     * 检查K号是否存在
     */
    public boolean existsByKNumber(String kNumber) {
        return device510KRepository.existsBykNumber(kNumber);
    }

    /**
     * 根据设备名称查询
     */
    public List<Device510K> findByDeviceName(String deviceName) {
        return device510KRepository.findByDeviceNameContaining(deviceName);
    }

    /**
     * 根据设备名称查询（分页）
     */
    public Page<Device510K> findByDeviceName(String deviceName, Pageable pageable) {
        // 暂时使用findAll然后过滤，后续可以添加分页查询方法到Repository
        List<Device510K> allResults = device510KRepository.findByDeviceNameContaining(deviceName);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResults.size());
        List<Device510K> pageResults = allResults.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageResults, pageable, allResults.size());
    }

    /**
     * 根据申请人查询
     */
    public List<Device510K> findByApplicant(String applicant) {
        return device510KRepository.findByApplicantContaining(applicant);
    }

    /**
     * 根据申请人查询（分页）
     */
    public Page<Device510K> findByApplicant(String applicant, Pageable pageable) {
        // 暂时使用findAll然后过滤，后续可以添加分页查询方法到Repository
        List<Device510K> allResults = device510KRepository.findByApplicantContaining(applicant);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResults.size());
        List<Device510K> pageResults = allResults.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageResults, pageable, allResults.size());
    }

    /**
     * 根据接收日期查询
     */
    public List<Device510K> findByDateReceived(LocalDate dateReceived) {
        return device510KRepository.findByDateReceived(dateReceived);
    }

    /**
     * 根据国家代码查询
     */
    public List<Device510K> findByCountryCode(String countryCode) {
        return device510KRepository.findByCountryCode(countryCode);
    }

    /**
     * 根据数据源查询
     */
    public List<Device510K> findByDataSource(String dataSource) {
        return device510KRepository.findByDataSource(dataSource);
    }

    /**
     * 根据设备类别查询
     */
    public List<Device510K> findByDeviceClass(String deviceClass) {
        return device510KRepository.findByDeviceClass(deviceClass);
    }

    /**
     * 根据品牌名称模糊查询
     */
    public List<Device510K> findByTradeName(String tradeName) {
        return device510KRepository.findByTradeNameContaining(tradeName);
    }

    /**
     * 根据品牌名称模糊查询（分页）
     */
    public Page<Device510K> findByTradeName(String tradeName, Pageable pageable) {
        // 暂时使用findAll然后过滤，后续可以添加分页查询方法到Repository
        List<Device510K> allResults = device510KRepository.findByTradeNameContaining(tradeName);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResults.size());
        List<Device510K> pageResults = allResults.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(pageResults, pageable, allResults.size());
    }

    /**
     * 分页查询所有记录
     */
    public Page<Device510K> findAll(Pageable pageable) {
        return device510KRepository.findAll(pageable);
    }

    /**
     * 查询所有记录
     */
    public List<Device510K> findAll() {
        return device510KRepository.findAll();
    }

    /**
     * 根据条件查询（支持多条件组合）
     */
    public List<Device510K> findByConditions(String deviceName, String applicant, String deviceClass, 
                                           LocalDate dateFrom, LocalDate dateTo, String dataSource) {
        // 这里可以实现复杂的查询逻辑
        // 暂时返回简单的查询结果
        if (deviceName != null && !deviceName.trim().isEmpty()) {
            return findByDeviceName(deviceName);
        } else if (applicant != null && !applicant.trim().isEmpty()) {
            return findByApplicant(applicant);
        } else if (deviceClass != null && !deviceClass.trim().isEmpty()) {
            return findByDeviceClass(deviceClass);
        } else if (dataSource != null && !dataSource.trim().isEmpty()) {
            return findByDataSource(dataSource);
        } else {
            return findAll();
        }
    }

    /**
     * 统计记录数量
     */
    public long count() {
        return device510KRepository.count();
    }

    /**
     * 根据数据源统计记录数量
     */
    public long countByDataSource(String dataSource) {
        return device510KRepository.findByDataSource(dataSource).size();
    }

    /**
     * 根据设备类别统计记录数量
     */
    public long countByDeviceClass(String deviceClass) {
        return device510KRepository.findByDeviceClass(deviceClass).size();
    }

    /**
     * 根据国家代码统计记录数量
     */
    public long countByCountryCode(String countryCode) {
        return device510KRepository.findByCountryCode(countryCode).size();
    }

    /**
     * 删除记录
     */
    public void deleteById(Long id) {
        log.info("删除Device510K记录，ID: {}", id);
        device510KRepository.deleteById(id);
    }

    /**
     * 批量删除记录
     */
    public void deleteAll(List<Device510K> records) {
        log.info("批量删除Device510K记录，数量: {}", records.size());
        device510KRepository.deleteAll(records);
    }

    /**
     * 删除所有记录
     */
    public void deleteAll() {
        log.info("删除所有Device510K记录");
        device510KRepository.deleteAll();
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("totalCount", count());
        stats.put("fdaCount", countByDataSource("FDA"));
        stats.put("fda510kCount", countByDataSource("FDA_510K"));
        
        // 按设备类别统计
        stats.put("class1Count", countByDeviceClass("1"));
        stats.put("class2Count", countByDeviceClass("2"));
        stats.put("class3Count", countByDeviceClass("3"));
        
        // 按国家统计
        stats.put("usCount", countByCountryCode("US"));
        
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }
}
