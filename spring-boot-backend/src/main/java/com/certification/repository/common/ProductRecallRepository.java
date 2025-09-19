package com.certification.repository.common;

import com.certification.entity.common.ProductRecall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 商品召回记录Repository接口
 */
@Repository
public interface ProductRecallRepository extends JpaRepository<ProductRecall, Long> {
    
    /**


     */
    List<ProductRecall> findByCountryCode(String countryCode);
//
//     * 根据召回编号查询
//     * 查询指定日期范围内的召回记录
//     */
    Optional<ProductRecall> findByRecallNumber(String recallNumber);
    
//
//     * 根据召回级别查询
//     */
    List<ProductRecall> findByRecallLevel(String recallLevel);
    


//     */
    List<ProductRecall> findByRecallDate(LocalDate recallDate);
//
//     * ��询指定日期范围内的召回记录
//     * 查询指定日期范围内的召回记录
//     */
    List<ProductRecall> findByRecallDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据国家代码和召回级别查询
     */
    List<ProductRecall> findByCountryCodeAndRecallLevel(String countryCode, String recallLevel);
    
    /**
     * 查询最近的召回记录
     */
    @Query("SELECT pr FROM ProductRecall pr ORDER BY pr.recallDate DESC")
    List<ProductRecall> findRecentRecalls();

    /**
     * 根据国家代码查询最近的召回记录
     */
    @Query("SELECT pr FROM ProductRecall pr WHERE pr.countryCode = :countryCode ORDER BY pr.recallDate DESC")
    List<ProductRecall> findRecentRecallsByCountryCode(@Param("countryCode") String countryCode);

    /**
     * 根据国家代码列表查询召回记录
     */
    List<ProductRecall> findByCountryCodeIn(List<String> countryCodes);
    
    /**
     * 根据召回原因模糊查询
     */
    List<ProductRecall> findByRecallReasonContaining(String recallReason);
    
    // 新增FDA召回相关查询方法
    /**
     * 根据CFRES ID查询
     */
    Optional<ProductRecall> findByCfresId(String cfresId);
    
    /**
     * 根据召回状态查询
     */
    List<ProductRecall> findByRecallStatus(String recallStatus);
    
    /**
     * 根据召回公司模糊查询
     */
    List<ProductRecall> findByRecallingFirmContaining(String recallingFirm);
    
    /**
     * 根据产品代码查询
     */
    List<ProductRecall> findByProductCode(String productCode);
    
    /**
     * 根据产品描述模糊查询
     */
    List<ProductRecall> findByProductDescriptionContaining(String productDescription);
    
    /**
     * 根据事件发起日期查询
     */
    List<ProductRecall> findByEventDateInitiated(LocalDate eventDateInitiated);
    
    /**
     * 根据事件发起日期范围查询
     */
    List<ProductRecall> findByEventDateInitiatedBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据国家代码和召回状态查询
     */
    List<ProductRecall> findByCountryCodeAndRecallStatus(String countryCode, String recallStatus);
    
    /**
     * 根据产品代码和国家代码查询
     */
    List<ProductRecall> findByProductCodeAndCountryCode(String productCode, String countryCode);
}
