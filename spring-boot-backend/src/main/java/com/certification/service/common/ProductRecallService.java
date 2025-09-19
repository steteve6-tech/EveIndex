package com.certification.service.common;

import com.certification.entity.common.ProductRecall;
import com.certification.repository.common.ProductRecallRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 商品召回记录服务类
 */
@Slf4j
@Service
@Transactional
public class ProductRecallService {
    
    @Autowired
    private ProductRecallRepository productRecallRepository;
    
    /**
     * 保存产品召回记录
     */
    public ProductRecall saveProductRecall(ProductRecall productRecall) {
        log.info("保存产品召回记录: {}", productRecall.getRecallNumber());
        return productRecallRepository.save(productRecall);
    }
    
    /**
     * 批量保存产品召回记录
     */
    public List<ProductRecall> saveProductRecalls(List<ProductRecall> productRecalls) {
        log.info("批量保存产品召回记录，数量: {}", productRecalls.size());
        return productRecallRepository.saveAll(productRecalls);
    }
    
    /**
     * 根据ID查询产品召回记录
     */
    public Optional<ProductRecall> findById(Long id) {
        log.info("根据ID查询产品召回记录: {}", id);
        return productRecallRepository.findById(id);
    }
    

    
    /**
     * 根据国家代码查询召回记录
     */
    public List<ProductRecall> findByCountryCode(String countryCode) {
        log.info("根据国家代码查询召回记录: {}", countryCode);
        return productRecallRepository.findByCountryCode(countryCode);
    }
    

    /**
     * 根据召回编号查询
     */
    public Optional<ProductRecall> findByRecallNumber(String recallNumber) {
        log.info("根据召回编号查询: {}", recallNumber);
        return productRecallRepository.findByRecallNumber(recallNumber);
    }
    
    /**
     * 根据召回级别查询
     */
    public List<ProductRecall> findByRecallLevel(String recallLevel) {
        log.info("根据召回级别查询: {}", recallLevel);
        return productRecallRepository.findByRecallLevel(recallLevel);
    }
    
    /**
     * 根据召回日期查询
     */
    public List<ProductRecall> findByRecallDate(LocalDate recallDate) {
        log.info("根据召回日期查询: {}", recallDate);
        return productRecallRepository.findByRecallDate(recallDate);
    }
    
    /**
     * 查询指定日期范围内的召回记录
     */
    public List<ProductRecall> findByRecallDateBetween(LocalDate startDate, LocalDate endDate) {
        log.info("查询指定日期范围内的召回记录: {} - {}", startDate, endDate);
        return productRecallRepository.findByRecallDateBetween(startDate, endDate);
    }
    

    
    /**
     * 根据国家代码和召回级别查询
     */
    public List<ProductRecall> findByCountryCodeAndRecallLevel(String countryCode, String recallLevel) {
        log.info("根据国家代码和召回级别查询: {} - {}", countryCode, recallLevel);
        return productRecallRepository.findByCountryCodeAndRecallLevel(countryCode, recallLevel);
    }
    
    /**
     * 查询最近的召回记录
     */
    public List<ProductRecall> findRecentRecalls() {
        log.info("查询最近的召回记录");
        return productRecallRepository.findRecentRecalls();
    }
    

    /**
     * 根据国家代码查询最近的召回记录
     */
    public List<ProductRecall> findRecentRecallsByCountryCode(String countryCode) {
        log.info("根据国家代码查询最近的召回记录: {}", countryCode);
        return productRecallRepository.findRecentRecallsByCountryCode(countryCode);
    }
    

    
    /**
     * 根据国家代码列表查询召回记录
     */
    public List<ProductRecall> findByCountryCodeIn(List<String> countryCodes) {
        log.info("根据国家代码列表查询召回记录: {}", countryCodes);
        return productRecallRepository.findByCountryCodeIn(countryCodes);
    }
    
    /**
     * 根据召回原因模糊查询
     */
    public List<ProductRecall> findByRecallReasonContaining(String recallReason) {
        log.info("根据召回原因模糊查询: {}", recallReason);
        return productRecallRepository.findByRecallReasonContaining(recallReason);
    }
    
    /**
     * 查询所有产品召回记录
     */
    public List<ProductRecall> findAll() {
        log.info("查询所有产品召回记录");
        return productRecallRepository.findAll();
    }
    
    /**
     * 更新产品召回记录
     */
    public ProductRecall updateProductRecall(ProductRecall productRecall) {
        log.info("更新产品召回记录: {}", productRecall.getRecallNumber());
        if (productRecall.getId() == null) {
            throw new IllegalArgumentException("更新产品召回记录时ID不能为空");
        }
        return productRecallRepository.save(productRecall);
    }
    
    /**
     * 删除产品召回记录
     */
    public void deleteProductRecall(Long id) {
        log.info("删除产品召回记录: {}", id);
        productRecallRepository.deleteById(id);
    }
    
    /**
     * 批量删除产品召回记录
     */
    public void deleteProductRecalls(List<Long> ids) {
        log.info("批量删除产品召回记录: {}", ids);
        productRecallRepository.deleteAllById(ids);
    }
    
    /**
     * 检查召回编号是否存在
     */
    public boolean existsByRecallNumber(String recallNumber) {
        return productRecallRepository.findByRecallNumber(recallNumber).isPresent();
    }
    
    /**
     * 统计产品召回记录总数
     */
    public long count() {
        return productRecallRepository.count();
    }
    
    /**
     * 根据召回级别统计数量
     */
    public long countByRecallLevel(String recallLevel) {
        return productRecallRepository.findByRecallLevel(recallLevel).size();
    }
    
    // 新增FDA召回相关服务方法
    /**
     * 根据CFRES ID查询
     */
    public Optional<ProductRecall> findByCfresId(String cfresId) {
        log.info("根据CFRES ID查询: {}", cfresId);
        return productRecallRepository.findByCfresId(cfresId);
    }
    
    /**
     * 根据召回状态查询
     */
    public List<ProductRecall> findByRecallStatus(String recallStatus) {
        log.info("根据召回状态查询: {}", recallStatus);
        return productRecallRepository.findByRecallStatus(recallStatus);
    }
    
    /**
     * 根据召回公司模糊查询
     */
    public List<ProductRecall> findByRecallingFirmContaining(String recallingFirm) {
        log.info("根据召回公司模糊查询: {}", recallingFirm);
        return productRecallRepository.findByRecallingFirmContaining(recallingFirm);
    }
    
    /**
     * 根据产品代码查询
     */
    public List<ProductRecall> findByProductCode(String productCode) {
        log.info("根据产品代码查询: {}", productCode);
        return productRecallRepository.findByProductCode(productCode);
    }
    
    /**
     * 根据产品描述模糊查询
     */
    public List<ProductRecall> findByProductDescriptionContaining(String productDescription) {
        log.info("根据产品描述模糊查询: {}", productDescription);
        return productRecallRepository.findByProductDescriptionContaining(productDescription);
    }
    
    /**
     * 根据事件发起日期查询
     */
    public List<ProductRecall> findByEventDateInitiated(LocalDate eventDateInitiated) {
        log.info("根据事件发起日期查询: {}", eventDateInitiated);
        return productRecallRepository.findByEventDateInitiated(eventDateInitiated);
    }
    
    /**
     * 根据事件发起日期范围查询
     */
    public List<ProductRecall> findByEventDateInitiatedBetween(LocalDate startDate, LocalDate endDate) {
        log.info("根据事件发起日期范围查询: {} - {}", startDate, endDate);
        return productRecallRepository.findByEventDateInitiatedBetween(startDate, endDate);
    }
    
    /**
     * 根据国家代码和召回状态查询
     */
    public List<ProductRecall> findByCountryCodeAndRecallStatus(String countryCode, String recallStatus) {
        log.info("根据国家代码和召回状态查询: {} - {}", countryCode, recallStatus);
        return productRecallRepository.findByCountryCodeAndRecallStatus(countryCode, recallStatus);
    }
    
    /**
     * 根据产品代码和国家代码查询
     */
    public List<ProductRecall> findByProductCodeAndCountryCode(String productCode, String countryCode) {
        log.info("根据产品代码和国家代码查询: {} - {}", productCode, countryCode);
        return productRecallRepository.findByProductCodeAndCountryCode(productCode, countryCode);
    }
}
