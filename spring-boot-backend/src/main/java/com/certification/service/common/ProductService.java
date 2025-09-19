package com.certification.service.common;

import com.certification.entity.common.Product;
import com.certification.repository.common.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 产品基础信息服务类
 */
@Slf4j
@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 保存产品信息
     */
    public Product saveProduct(Product product) {
        log.info("保存产品信息: {}", product.getProductName());
        return productRepository.save(product);
    }
    
    /**
     * 批量保存产品信息
     */
    public List<Product> saveProducts(List<Product> products) {
        log.info("批量保存产品信息，数量: {}", products.size());
        return productRepository.saveAll(products);
    }
    
    /**
     * 根据ID查询产品信息
     */
    public Optional<Product> findById(Long id) {
        log.info("根据ID查询产品信息: {}", id);
        return productRepository.findById(id);
    }
    
    /**
     * 根据产品名称查询
     */
    public Optional<Product> findByProductName(String productName) {
        log.info("根据产品名称查询: {}", productName);
        return productRepository.findByProductName(productName);
    }
    
    /**
     * 根据品牌查询产品列表
     */
    public List<Product> findByBrand(String brand) {
        log.info("根据品牌查询产品列表: {}", brand);
        return productRepository.findByBrand(brand);
    }
    
    /**
     * 根据产品类型查询
     */
    public List<Product> findByProductType(String productType) {
        log.info("根据产品类型查询: {}", productType);
        return productRepository.findByProductType(productType);
    }
    
    /**
     * 根据是否有效查询
     */
    public List<Product> findByIsActive(Integer isActive) {
        log.info("根据是否有效查询: {}", isActive);
        return productRepository.findByIsActive(isActive);
    }
    
    /**
     * 根据产品名称模糊查询
     */
    public List<Product> findByProductNameContaining(String productName) {
        log.info("根据产品名称模糊查询: {}", productName);
        return productRepository.findByProductNameContaining(productName);
    }
    
    /**
     * 根据品牌和产品类型查询
     */
    public List<Product> findByBrandAndProductType(String brand, String productType) {
        log.info("根据品牌和产品类型查询: {} - {}", brand, productType);
        return productRepository.findByBrandAndProductType(brand, productType);
    }
    
    /**
     * 根据关键词模糊查询
     */
    public List<Product> findByKeyword(String keyword) {
        log.info("根据关键词模糊查询: {}", keyword);
        return productRepository.findByKeyword(keyword);
    }
    
    /**
     * 查询所有有效的产品
     */
    public List<Product> findAllActiveProducts() {
        log.info("查询所有有效的产品");
        return productRepository.findAllActiveProducts();
    }
    
    /**
     * 根据产品名称和品牌查询
     */
    public Optional<Product> findByProductNameAndBrand(String productName, String brand) {
        log.info("根据产品名称和品牌查询: {} - {}", productName, brand);
        return productRepository.findByProductNameAndBrand(productName, brand);
    }
    
    /**
     * 查询所有产品
     */
    public List<Product> findAll() {
        log.info("查询所有产品");
        return productRepository.findAll();
    }
    
    /**
     * 更新产品信息
     */
    public Product updateProduct(Product product) {
        log.info("更新产品信息: {}", product.getProductName());
        if (product.getId() == null) {
            throw new IllegalArgumentException("更新产品信息时ID不能为空");
        }
        return productRepository.save(product);
    }
    
    /**
     * 删除产品信息
     */
    public void deleteProduct(Long id) {
        log.info("删除产品信息: {}", id);
        productRepository.deleteById(id);
    }
    
    /**
     * 批量删除产品信息
     */
    public void deleteProducts(List<Long> ids) {
        log.info("批量删除产品信息: {}", ids);
        productRepository.deleteAllById(ids);
    }
    
    /**
     * 软删除产品（设置为无效）
     */
    public Product deactivateProduct(Long id) {
        log.info("软删除产品: {}", id);
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setIsActive(0);
            return productRepository.save(product);
        }
        throw new IllegalArgumentException("产品不存在: " + id);
    }
    
    /**
     * 激活产品（设置为有效）
     */
    public Product activateProduct(Long id) {
        log.info("激活产品: {}", id);
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setIsActive(1);
            return productRepository.save(product);
        }
        throw new IllegalArgumentException("产品不存在: " + id);
    }
    
    /**
     * 检查产品名称是否存在
     */
    public boolean existsByProductName(String productName) {
        return productRepository.findByProductName(productName).isPresent();
    }
    
    /**
     * 检查产品名称和品牌组合是否存在
     */
    public boolean existsByProductNameAndBrand(String productName, String brand) {
        return productRepository.findByProductNameAndBrand(productName, brand).isPresent();
    }
    
    /**
     * 统计产品总数
     */
    public long count() {
        return productRepository.count();
    }
    
    /**
     * 统计有效产品数量
     */
    public long countActiveProducts() {
        return productRepository.findByIsActive(1).size();
    }
    
    // ==================== 竞品信息相关方法 ====================
    
    /**
     * 根据设备代码查询竞品信息
     */
    public Optional<Product> findByDeviceCode(String deviceCode) {
        log.info("根据设备代码查询竞品信息: {}", deviceCode);
        return productRepository.findByDeviceCode(deviceCode);
    }
    
    /**
     * 根据数据来源查询竞品信息
     */
    public List<Product> findByDataSource(String dataSource) {
        log.info("根据数据来源查询竞品信息: {}", dataSource);
        return productRepository.findByDataSource(dataSource);
    }
    
    /**
     * 根据原始数据ID查询竞品信息
     */
    public Optional<Product> findBySourceDataId(Long sourceDataId) {
        log.info("根据原始数据ID查询竞品信息: {}", sourceDataId);
        return productRepository.findBySourceDataId(sourceDataId);
    }
    
    /**
     * 根据设备等级查询竞品信息
     */
    public List<Product> findByDeviceClass(String deviceClass) {
        log.info("根据设备等级查询竞品信息: {}", deviceClass);
        return productRepository.findByDeviceClass(deviceClass);
    }
    
    /**
     * 根据申请人名称模糊查询竞品信息
     */
    public List<Product> findByApplicantNameContaining(String applicantName) {
        log.info("根据申请人名称模糊查询竞品信息: {}", applicantName);
        return productRepository.findByApplicantNameContaining(applicantName);
    }
    
    /**
     * 根据品牌名称模糊查询竞品信息
     */
    public List<Product> findByBrandNameContaining(String brandName) {
        log.info("根据品牌名称模糊查询竞品信息: {}", brandName);
        return productRepository.findByBrandNameContaining(brandName);
    }
    
    /**
     * 根据数据来源和原始数据ID查询（避免重复生成）
     */
    public Optional<Product> findByDataSourceAndSourceDataId(String dataSource, Long sourceDataId) {
        log.info("根据数据来源和原始数据ID查询: {} - {}", dataSource, sourceDataId);
        return productRepository.findByDataSourceAndSourceDataId(dataSource, sourceDataId);
    }
    
    /**
     * 分页查询所有竞品信息（按创建时间倒序）
     */
    public Page<Product> findAllActiveProductsPaged(Pageable pageable) {
        log.info("分页查询所有竞品信息，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAllActiveProductsPaged(pageable);
    }
    
    /**
     * 根据关键词搜索竞品信息
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        log.info("根据关键词搜索竞品信息: {}, 页码: {}, 大小: {}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.searchProducts(keyword, pageable);
    }
    
    /**
     * 根据设备等级分页查询竞品信息
     */
    public Page<Product> findByDeviceClassPaged(String deviceClass, Pageable pageable) {
        log.info("根据设备等级分页查询竞品信息: {}, 页码: {}, 大小: {}", deviceClass, pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findByDeviceClassPaged(deviceClass, pageable);
    }
    
    /**
     * 从高风险数据生成竞品信息
     */
    @Transactional
    public Product generateFromHighRiskData(String dataSource, Long sourceDataId, String productName, 
                                          String applicantName, String brandName, String deviceCode,
                                          String deviceClass, String deviceDescription) {
        log.info("从高风险数据生成竞品信息: dataSource={}, sourceDataId={}, productName={}", 
                dataSource, sourceDataId, productName);
        
        // 检查是否已经生成过
        Optional<Product> existingProduct = findByDataSourceAndSourceDataId(dataSource, sourceDataId);
        if (existingProduct.isPresent()) {
            log.warn("竞品信息已存在，跳过生成: dataSource={}, sourceDataId={}", dataSource, sourceDataId);
            throw new IllegalArgumentException("该数据已生成过竞品信息，请勿重复生成");
        }
        
        // 创建新的竞品信息
        Product product = new Product();
        product.setProductName(productName);
        product.setApplicantName(applicantName);
        product.setBrandName(brandName);
        product.setDeviceCode(deviceCode);
        product.setDataSource(dataSource);
        product.setSourceDataId(sourceDataId);
        product.setDeviceClass(deviceClass);
        product.setDeviceDescription(deviceDescription);
        product.setProductType("竞品信息");
        product.setIsActive(1);
        
        return saveProduct(product);
    }
    
    /**
     * 批量从高风险数据生成竞品信息
     */
    @Transactional
    public List<Product> batchGenerateFromHighRiskData(List<Product> products) {
        log.info("批量从高风险数据生成竞品信息，数量: {}", products.size());
        
        // 过滤掉已经存在的数据
        List<Product> newProducts = products.stream()
                .filter(product -> {
                    Optional<Product> existing = findByDataSourceAndSourceDataId(
                            product.getDataSource(), product.getSourceDataId());
                    return existing.isEmpty();
                })
                .toList();
        
        log.info("过滤后需要生成的竞品信息数量: {}", newProducts.size());
        
        if (newProducts.isEmpty()) {
            return List.of();
        }
        
        return saveProducts(newProducts);
    }
    
    /**
     * 检查是否已从指定高风险数据生成过竞品信息
     */
    public boolean existsByDataSourceAndSourceDataId(String dataSource, Long sourceDataId) {
        return findByDataSourceAndSourceDataId(dataSource, sourceDataId).isPresent();
    }
}
