package com.certification.crawler.countrydata.medical.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 医疗器械信息爬虫结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCrawlerResult {
    private String title;
    private String url;
    private String content;
    private String publishDate;
    private String country;
    private String regulatoryAuthority; // 监管机构名称
    private String documentType; // 公告、法规、标准、召回等
    private String deviceCategory; // 设备类别
    private String riskLevel; // 风险等级
    private LocalDateTime crawlTime = LocalDateTime.now();
    private String source;
    private String documentNumber;
    private String effectiveDate;
    private String status; // 生效、失效、待生效等
    private String manufacturer; // 制造商
    private String productName; // 产品名称
    private String approvalNumber; // 批准文号

    // 保留带参数的构造函数
    public MedicalCrawlerResult(String title, String url, String content, String publishDate, String country) {
        this();
        this.title = title;
        this.url = url;
        this.content = content;
        this.publishDate = publishDate;
        this.country = country;
    }
}

