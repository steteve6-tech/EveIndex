package com.certification.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 产品上下文领域模型
 * 包含产品在目标国家的所有风险数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductContext {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 国家ID
     */
    private Long countryId;

    /**
     * 产品基本信息
     */
    private ProductInfo productInfo;

    /**
     * 产品注册信息
     */
    private List<ProductRegistration> registrations;

    /**
     * 产品召回信息
     */
    private List<ProductRecall> recalls;

    /**
     * 法规通知信息
     */
    private List<RegulationNotice> regulationNotices;

    /**
     * 海关案例信息
     */
    private List<CustomsCase> customsCases;

    /**
     * 产品基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String productName;
        private String productCode;
        private String category;
        private String manufacturer;
        private String brand;
    }

    /**
     * 产品注册信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRegistration {
        private String registrationNumber;
        private String status;
        private String registrationType;
        private String expiryDate;
        private String issueDate;
    }

    /**
     * 产品召回信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRecall {
        private String recallNumber;
        private String recallLevel;
        private String recallReason;
        private String recallDate;
        private String affectedQuantity;
    }

    /**
     * 法规通知信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegulationNotice {
        private String noticeNumber;
        private String noticeType;
        private String title;
        private String content;
        private String publishDate;
        private String effectiveDate;
    }

    /**
     * 海关案例信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomsCase {
        private String caseNumber;
        private String caseType;
        private String description;
        private String decision;
        private String caseDate;
        private String penalty;
    }
}
