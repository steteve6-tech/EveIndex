package com.certification.service.common;

import com.certification.entity.common.ProductRegistration;
import com.certification.repository.common.ProductRegistrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品注册信息服务类
 */
@Slf4j
@Service
@Transactional
public class ProductRegistrationService {
    
    @Autowired
    private ProductRegistrationRepository productRegistrationRepository;
    
    /**
     * 保存产品注册信息
     */
    public ProductRegistration saveProductRegistration(ProductRegistration productRegistration) {
        log.info("保存产品注册信息: {}", productRegistration.getRegNumber());
        return productRegistrationRepository.save(productRegistration);
    }
    
    /**
     * 批量保存产品注册信息
     */
    public List<ProductRegistration> saveProductRegistrations(List<ProductRegistration> productRegistrations) {
        log.info("批量保存产品注册信息，数量: {}", productRegistrations.size());
        return productRegistrationRepository.saveAll(productRegistrations);
    }
    
    /**
     * 根据ID查询产品注册信息
     */
    public Optional<ProductRegistration> findById(Long id) {
        log.info("根据ID查询产品注册信息: {}", id);
        return productRegistrationRepository.findById(id);
    }
    
    /**
     * 根据产品ID查询注册信息
     */
    public List<ProductRegistration> findByProductId(Long productId) {
        log.info("根据产品ID查询注册信息: {}", productId);
        return productRegistrationRepository.findByProductId(productId);
    }
    
    /**
     * 根据国家ID查询注册信息
     */
    public List<ProductRegistration> findByCountryId(Long countryId) {
        log.info("根据国家ID查询注册信息: {}", countryId);
        return productRegistrationRepository.findByCountryId(countryId);
    }
    
    /**
     * 根据产品ID和国家ID查询
     */
    public Optional<ProductRegistration> findByProductIdAndCountryId(Long productId, Long countryId) {
        log.info("根据产品ID和国家ID查询: {} - {}", productId, countryId);
        return productRegistrationRepository.findByProductIdAndCountryId(productId, countryId);
    }
    
    /**
     * 根据注册证号查询
     */
    public Optional<ProductRegistration> findByRegNumber(String regNumber) {
        log.info("根据注册证号查询: {}", regNumber);
        return productRegistrationRepository.findByRegNumber(regNumber);
    }
    
    /**
     * 根据注册状态查询
     */
    public List<ProductRegistration> findByRegStatus(String regStatus) {
        log.info("根据注册状态查询: {}", regStatus);
        return productRegistrationRepository.findByRegStatus(regStatus);
    }
    
    /**
     * 根据发证机构查询
     */
    public List<ProductRegistration> findByIssuingAuthority(String issuingAuthority) {
        log.info("根据发证机构查询: {}", issuingAuthority);
        return productRegistrationRepository.findByIssuingAuthority(issuingAuthority);
    }
    
    /**
     * 根据注册分类查询
     */
    public List<ProductRegistration> findByRegCategory(String regCategory) {
        log.info("根据注册分类查询: {}", regCategory);
        return productRegistrationRepository.findByRegCategory(regCategory);
    }
    
    /**
     * 查询即将过期的注册信息
     */
    public List<ProductRegistration> findExpiringRegistrations(LocalDate startDate, LocalDate endDate) {
        log.info("查询即将过期的注册信息: {} - {}", startDate, endDate);
        return productRegistrationRepository.findExpiringRegistrations(startDate, endDate);
    }
    
    /**
     * 根据产品ID和注册状态查询
     */
    public List<ProductRegistration> findByProductIdAndRegStatus(Long productId, String regStatus) {
        log.info("根据产品ID和注册状态查询: {} - {}", productId, regStatus);
        return productRegistrationRepository.findByProductIdAndRegStatus(productId, regStatus);
    }
    
    /**
     * 根据国家ID和注册状态查询
     */
    public List<ProductRegistration> findByCountryIdAndRegStatus(Long countryId, String regStatus) {
        log.info("根据国家ID和注册状态查询: {} - {}", countryId, regStatus);
        return productRegistrationRepository.findByCountryIdAndRegStatus(countryId, regStatus);
    }
    
    /**
     * 查询已过期的注册信息
     */
    public List<ProductRegistration> findExpiredRegistrations(LocalDate currentDate) {
        log.info("查询已过期的注册信息: {}", currentDate);
        return productRegistrationRepository.findExpiredRegistrations(currentDate);
    }
    
    /**
     * 根据产品ID列表查询注册信息
     */
    public List<ProductRegistration> findByProductIdIn(List<Long> productIds) {
        log.info("根据产品ID列表查询注册信息: {}", productIds);
        return productRegistrationRepository.findByProductIdIn(productIds);
    }
    
    /**
     * 根据国家ID列表查询注册信息
     */
    public List<ProductRegistration> findByCountryIdIn(List<Long> countryIds) {
        log.info("根据国家ID列表查询注册信息: {}", countryIds);
        return productRegistrationRepository.findByCountryIdIn(countryIds);
    }
    
    /**
     * 查询所有产品注册信息
     */
    public List<ProductRegistration> findAll() {
        log.info("查询所有产品注册信息");
        return productRegistrationRepository.findAll();
    }
    
    /**
     * 更新产品注册信息
     */
    public ProductRegistration updateProductRegistration(ProductRegistration productRegistration) {
        log.info("更新产品注册信息: {}", productRegistration.getRegNumber());
        if (productRegistration.getId() == null) {
            throw new IllegalArgumentException("更新产品注册信息时ID不能为空");
        }
        return productRegistrationRepository.save(productRegistration);
    }
    
    /**
     * 删除产品注册信息
     */
    public void deleteProductRegistration(Long id) {
        log.info("删除产品注册信息: {}", id);
        productRegistrationRepository.deleteById(id);
    }
    
    /**
     * 批量删除产品注册信息
     */
    public void deleteProductRegistrations(List<Long> ids) {
        log.info("批量删除产品注册信息: {}", ids);
        productRegistrationRepository.deleteAllById(ids);
    }
    
    /**
     * 检查注册证号是否存在
     */
    public boolean existsByRegNumber(String regNumber) {
        return productRegistrationRepository.findByRegNumber(regNumber).isPresent();
    }
    
    /**
     * 检查产品在国家是否已有注册信息
     */
    public boolean existsByProductIdAndCountryId(Long productId, Long countryId) {
        return productRegistrationRepository.findByProductIdAndCountryId(productId, countryId).isPresent();
    }
    
    /**
     * 统计产品注册信息总数
     */
    public long count() {
        return productRegistrationRepository.count();
    }
    
    /**
     * 根据注册状态统计数量
     */
    public long countByRegStatus(String regStatus) {
        return productRegistrationRepository.findByRegStatus(regStatus).size();
    }
}
