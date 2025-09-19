package com.certification.repository.common;

import com.certification.entity.common.ProductRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品注册信息Repository接口
 */
@Repository
public interface ProductRegistrationRepository extends JpaRepository<ProductRegistration, Long> {
    
    /**
     * 根据产品ID查询注册信息
     */
    List<ProductRegistration> findByProductId(Long productId);
    
    /**
     * 根据国家ID查询注册信息
     */
    List<ProductRegistration> findByCountryId(Long countryId);
    
    /**
     * 根据产品ID和国家ID查询
     */
    Optional<ProductRegistration> findByProductIdAndCountryId(Long productId, Long countryId);
    
    /**
     * 根据注册证号查询
     */
    Optional<ProductRegistration> findByRegNumber(String regNumber);
    
    /**
     * 根据注册状态查询
     */
    List<ProductRegistration> findByRegStatus(String regStatus);
    
    /**
     * 根据发证机构查询
     */
    List<ProductRegistration> findByIssuingAuthority(String issuingAuthority);
    
    /**
     * 根据注册分类查询
     */
    List<ProductRegistration> findByRegCategory(String regCategory);
    
    /**
     * 查询即将过期的注册信息
     */
    @Query("SELECT pr FROM ProductRegistration pr WHERE pr.expireDate BETWEEN :startDate AND :endDate")
    List<ProductRegistration> findExpiringRegistrations(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 根据产品ID和注册状态查询
     */
    List<ProductRegistration> findByProductIdAndRegStatus(Long productId, String regStatus);
    
    /**
     * 根据国家ID和注册状态查询
     */
    List<ProductRegistration> findByCountryIdAndRegStatus(Long countryId, String regStatus);
    
    /**
     * 查询已过期的注册信息
     */
    @Query("SELECT pr FROM ProductRegistration pr WHERE pr.expireDate < :currentDate")
    List<ProductRegistration> findExpiredRegistrations(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 根据产品ID列表查询注册信息
     */
    List<ProductRegistration> findByProductIdIn(List<Long> productIds);
    
    /**
     * 根据国家ID列表查询注册信息
     */
    List<ProductRegistration> findByCountryIdIn(List<Long> countryIds);
}
