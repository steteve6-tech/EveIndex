package com.certification.service.common;

import com.certification.entity.common.ProductCategoryCode;
import com.certification.repository.common.ProductCategoryCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品分类代码服务类
 */
@Slf4j
@Service
@Transactional
public class ProductCategoryCodeService {
    
    @Autowired
    private ProductCategoryCodeRepository productCategoryCodeRepository;
    
    /**
     * 保存产品分类代码
     */
    public ProductCategoryCode saveProductCategoryCode(ProductCategoryCode productCategoryCode) {
        log.info("保存产品分类代码: {}", productCategoryCode.getHsCode());
        return productCategoryCodeRepository.save(productCategoryCode);
    }
    
    /**
     * 批量保存产品分类代码
     */
    public List<ProductCategoryCode> saveProductCategoryCodes(List<ProductCategoryCode> productCategoryCodes) {
        log.info("批量保存产品分类代码，数量: {}", productCategoryCodes.size());
        return productCategoryCodeRepository.saveAll(productCategoryCodes);
    }
    
    /**
     * 根据ID查询产品分类代码
     */
    public Optional<ProductCategoryCode> findById(Long id) {
        log.info("根据ID查询产品分类代码: {}", id);
        return productCategoryCodeRepository.findById(id);
    }
    
    /**
     * 根据产品ID查询分类代码
     */
    public List<ProductCategoryCode> findByProductId(Long productId) {
        log.info("根据产品ID查询分类代码: {}", productId);
        return productCategoryCodeRepository.findByProductId(productId);
    }
    
    /**
     * 根据国家ID查询分类代码
     */
    public List<ProductCategoryCode> findByCountryId(Long countryId) {
        log.info("根据国家ID查询分类代码: {}", countryId);
        return productCategoryCodeRepository.findByCountryId(countryId);
    }
    
    /**
     * 根据产品ID和国家ID查询
     */
    public List<ProductCategoryCode> findByProductIdAndCountryId(Long productId, Long countryId) {
        log.info("根据产品ID和国家ID查询: {} - {}", productId, countryId);
        return productCategoryCodeRepository.findByProductIdAndCountryId(productId, countryId);
    }
    
    /**
     * 根据HS编码查询
     */
    public List<ProductCategoryCode> findByHsCode(String hsCode) {
        log.info("根据HS编码查询: {}", hsCode);
        return productCategoryCodeRepository.findByHsCode(hsCode);
    }
    
    /**
     * 根据监管分类代码查询
     */
    public List<ProductCategoryCode> findByRegCode(String regCode) {
        log.info("根据监管分类代码查询: {}", regCode);
        return productCategoryCodeRepository.findByRegCode(regCode);
    }
    
    /**
     * 根据是否默认使用查询
     */
    public List<ProductCategoryCode> findByIsDefault(Boolean isDefault) {
        log.info("根据是否默认使用查询: {}", isDefault);
        return productCategoryCodeRepository.findByIsDefault(isDefault);
    }
    
    /**
     * 根据产品ID查询默认分类代码
     */
    public Optional<ProductCategoryCode> findByProductIdAndIsDefaultTrue(Long productId) {
        log.info("根据产品ID查询默认分类代码: {}", productId);
        return productCategoryCodeRepository.findByProductIdAndIsDefaultTrue(productId);
    }
    
    /**
     * 根据产品ID和国家ID查询默认分类代码
     */
    public Optional<ProductCategoryCode> findByProductIdAndCountryIdAndIsDefaultTrue(Long productId, Long countryId) {
        log.info("根据产品ID和国家ID查询默认分类代码: {} - {}", productId, countryId);
        return productCategoryCodeRepository.findByProductIdAndCountryIdAndIsDefaultTrue(productId, countryId);
    }
    
    /**
     * 根据HS编码模糊查询
     */
    public List<ProductCategoryCode> findByHsCodeContaining(String hsCode) {
        log.info("根据HS编码模糊查询: {}", hsCode);
        return productCategoryCodeRepository.findByHsCodeContaining(hsCode);
    }
    
    /**
     * 根据监管分类代码模糊查询
     */
    public List<ProductCategoryCode> findByRegCodeContaining(String regCode) {
        log.info("根据监管分类代码模糊查询: {}", regCode);
        return productCategoryCodeRepository.findByRegCodeContaining(regCode);
    }
    
    /**
     * 根据生效日期查询
     */
    public List<ProductCategoryCode> findByEffectiveDate(LocalDate effectiveDate) {
        log.info("根据生效日期查询: {}", effectiveDate);
        return productCategoryCodeRepository.findByEffectiveDate(effectiveDate);
    }
    
    /**
     * 查询指定日期之后生效的分类代码
     */
    public List<ProductCategoryCode> findByEffectiveDateAfter(LocalDate effectiveDate) {
        log.info("查询指定日期之后生效的分类代码: {}", effectiveDate);
        return productCategoryCodeRepository.findByEffectiveDateAfter(effectiveDate);
    }
    
    /**
     * 根据产品ID列表查询分类代码
     */
    public List<ProductCategoryCode> findByProductIdIn(List<Long> productIds) {
        log.info("根据产品ID列表查询分类代码: {}", productIds);
        return productCategoryCodeRepository.findByProductIdIn(productIds);
    }
    
    /**
     * 根据国家ID列表查询分类代码
     */
    public List<ProductCategoryCode> findByCountryIdIn(List<Long> countryIds) {
        log.info("根据国家ID列表查询分类代码: {}", countryIds);
        return productCategoryCodeRepository.findByCountryIdIn(countryIds);
    }
    
    /**
     * 根据HS编码列表查询
     */
    public List<ProductCategoryCode> findByHsCodeIn(List<String> hsCodes) {
        log.info("根据HS编码列表查询: {}", hsCodes);
        return productCategoryCodeRepository.findByHsCodeIn(hsCodes);
    }
    
    /**
     * 查询所有产品分类代码
     */
    public List<ProductCategoryCode> findAll() {
        log.info("查询所有产品分类代码");
        return productCategoryCodeRepository.findAll();
    }
    
    /**
     * 更新产品分类代码
     */
    public ProductCategoryCode updateProductCategoryCode(ProductCategoryCode productCategoryCode) {
        log.info("更新产品分类代码: {}", productCategoryCode.getHsCode());
        if (productCategoryCode.getId() == null) {
            throw new IllegalArgumentException("更新产品分类代码时ID不能为空");
        }
        return productCategoryCodeRepository.save(productCategoryCode);
    }
    
    /**
     * 删除产品分类代码
     */
    public void deleteProductCategoryCode(Long id) {
        log.info("删除产品分类代码: {}", id);
        productCategoryCodeRepository.deleteById(id);
    }
    
    /**
     * 批量删除产品分类代码
     */
    public void deleteProductCategoryCodes(List<Long> ids) {
        log.info("批量删除产品分类代码: {}", ids);
        productCategoryCodeRepository.deleteAllById(ids);
    }
    
    /**
     * 设置默认分类代码
     */
    public ProductCategoryCode setDefaultCategoryCode(Long id) {
        log.info("设置默认分类代码: {}", id);
        Optional<ProductCategoryCode> optional = productCategoryCodeRepository.findById(id);
        if (optional.isPresent()) {
            ProductCategoryCode categoryCode = optional.get();
            
            // 先将同一产品同一国家的其他分类代码设置为非默认
            List<ProductCategoryCode> existingDefaults = productCategoryCodeRepository
                .findByProductIdAndCountryId(categoryCode.getProductId(), categoryCode.getCountryId());
            for (ProductCategoryCode existing : existingDefaults) {
                if (!existing.getId().equals(id) && existing.getIsDefault()) {
                    existing.setIsDefault(false);
                    productCategoryCodeRepository.save(existing);
                }
            }
            
            // 设置当前分类代码为默认
            categoryCode.setIsDefault(true);
            return productCategoryCodeRepository.save(categoryCode);
        }
        throw new IllegalArgumentException("产品分类代码不存在: " + id);
    }
    
    /**
     * 统计产品分类代码总数
     */
    public long count() {
        return productCategoryCodeRepository.count();
    }
}
