package com.certification.repository.common;

import com.certification.entity.common.ProductCategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品分类代码Repository接口
 */
@Repository
public interface ProductCategoryCodeRepository extends JpaRepository<ProductCategoryCode, Long> {
    
    /**
     * 根据产品ID查询分类代码
     */
    List<ProductCategoryCode> findByProductId(Long productId);
    
    /**
     * 根据国家ID查询分类代码
     */
    List<ProductCategoryCode> findByCountryId(Long countryId);
    
    /**
     * 根据产品ID和国家ID查询
     */
    List<ProductCategoryCode> findByProductIdAndCountryId(Long productId, Long countryId);
    
    /**
     * 根据HS编码查询
     */
    List<ProductCategoryCode> findByHsCode(String hsCode);
    
    /**
     * 根据监管分类代码查询
     */
    List<ProductCategoryCode> findByRegCode(String regCode);
    
    /**
     * 根据是否默认使用查询
     */
    List<ProductCategoryCode> findByIsDefault(Boolean isDefault);
    
    /**
     * 根据产品ID查询默认分类代码
     */
    Optional<ProductCategoryCode> findByProductIdAndIsDefaultTrue(Long productId);
    
    /**
     * 根据产品ID和国家ID查询默认分类代码
     */
    Optional<ProductCategoryCode> findByProductIdAndCountryIdAndIsDefaultTrue(Long productId, Long countryId);
    
    /**
     * 根据HS编码模糊查询
     */
    List<ProductCategoryCode> findByHsCodeContaining(String hsCode);
    
    /**
     * 根据监管分类代码模糊查询
     */
    List<ProductCategoryCode> findByRegCodeContaining(String regCode);
    
    /**
     * 根据生效日期查询
     */
    List<ProductCategoryCode> findByEffectiveDate(LocalDate effectiveDate);
    
    /**
     * 查询指定日期之后生效的分类代码
     */
    List<ProductCategoryCode> findByEffectiveDateAfter(LocalDate effectiveDate);
    
    /**
     * 根据产品ID列表查询分类代码
     */
    List<ProductCategoryCode> findByProductIdIn(List<Long> productIds);
    
    /**
     * 根据国家ID列表查询分类代码
     */
    List<ProductCategoryCode> findByCountryIdIn(List<Long> countryIds);
    
    /**
     * 根据HS编码列表查询
     */
    List<ProductCategoryCode> findByHsCodeIn(List<String> hsCodes);
}
