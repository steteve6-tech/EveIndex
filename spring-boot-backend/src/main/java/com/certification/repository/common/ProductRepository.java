package com.certification.repository.common;

import com.certification.entity.common.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 产品基础信息Repository接口
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 根据产品名称查询
     */
    Optional<Product> findByProductName(String productName);
    
    /**
     * 根据品牌查询产品列表
     */
    List<Product> findByBrand(String brand);
    
    /**
     * 根据产品类型查询
     */
    List<Product> findByProductType(String productType);
    
    /**
     * 根据是否有效查询
     */
    List<Product> findByIsActive(Integer isActive);
    
    /**
     * 根据产品名称模糊查询
     */
    List<Product> findByProductNameContaining(String productName);
    
    /**
     * 根据品牌和产品类型查询
     */
    List<Product> findByBrandAndProductType(String brand, String productType);
    
    /**
     * 根据产品名称、品牌或型号模糊查询
     */
    @Query("SELECT p FROM Product p WHERE p.productName LIKE %:keyword% OR p.brand LIKE %:keyword% OR p.model LIKE %:keyword%")
    List<Product> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查询所有有效的产品
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = 1 ORDER BY p.productName")
    List<Product> findAllActiveProducts();
    
    /**
     * 根据产品名称和品牌查询
     */
    Optional<Product> findByProductNameAndBrand(String productName, String brand);
    
    /**
     * 根据设备代码查询
     */
    Optional<Product> findByDeviceCode(String deviceCode);
    
    /**
     * 根据数据来源查询
     */
    List<Product> findByDataSource(String dataSource);
    
    /**
     * 根据原始数据ID查询
     */
    Optional<Product> findBySourceDataId(Long sourceDataId);
    
    /**
     * 根据设备等级查询
     */
    List<Product> findByDeviceClass(String deviceClass);
    
    /**
     * 根据申请人名称模糊查询
     */
    List<Product> findByApplicantNameContaining(String applicantName);
    
    /**
     * 根据品牌名称模糊查询
     */
    List<Product> findByBrandNameContaining(String brandName);
    
    /**
     * 根据数据来源和原始数据ID查询（避免重复生成）
     */
    Optional<Product> findByDataSourceAndSourceDataId(String dataSource, Long sourceDataId);
    
    /**
     * 分页查询所有竞品信息（按创建时间倒序）
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = 1 ORDER BY p.createTime DESC")
    org.springframework.data.domain.Page<Product> findAllActiveProductsPaged(org.springframework.data.domain.Pageable pageable);
    
    /**
     * 根据关键词搜索竞品信息（产品名称、申请人、品牌名称、设备代码）
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = 1 AND " +
           "(p.productName LIKE %:keyword% OR p.applicantName LIKE %:keyword% OR " +
           "p.brandName LIKE %:keyword% OR p.deviceCode LIKE %:keyword%)")
    org.springframework.data.domain.Page<Product> searchProducts(@Param("keyword") String keyword, 
                                                               org.springframework.data.domain.Pageable pageable);
    
    /**
     * 根据设备等级分页查询
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = 1 AND p.deviceClass = :deviceClass ORDER BY p.createTime DESC")
    org.springframework.data.domain.Page<Product> findByDeviceClassPaged(@Param("deviceClass") String deviceClass, 
                                                                         org.springframework.data.domain.Pageable pageable);
}
