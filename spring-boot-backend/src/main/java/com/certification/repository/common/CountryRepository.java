package com.certification.repository.common;

import com.certification.entity.common.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 国家基础信息Repository接口
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    
    /**
     * 根据国家编码查询
     */
    Optional<Country> findByCountryCode(String countryCode);
    
    /**
     * 根据国家名称查询
     */
    Optional<Country> findByCountryName(String countryName);
    
    /**
     * 根据地区查询国家列表
     */
    List<Country> findByRegion(String region);
    
    /**
     * 根据国家编码或名称模糊查询
     */
    @Query("SELECT c FROM Country c WHERE c.countryCode LIKE %:keyword% OR c.countryName LIKE %:keyword%")
    List<Country> findByKeyword(@Param("keyword") String keyword);
    
    /**
     * 查询所有有效的国家
     */
    @Query("SELECT c FROM Country c ORDER BY c.countryName")
    List<Country> findAllOrderByCountryName();
    
    /**
     * 根据国家编码列表查询
     */
    List<Country> findByCountryCodeIn(List<String> countryCodes);
}
