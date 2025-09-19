package com.certification.crawler.countrydata.customs.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 海关信息爬虫结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomsCrawlerResult {
    private String title;
    private String url;
    private String content;
    private String publishDate;
    private String country;
    private String customsOffice;
    private String documentType; // 公告、法规、通知等
    private String category; // 进口、出口、监管等
    private LocalDateTime crawlTime = LocalDateTime.now();
    private String source;
    private String documentNumber;
    private String effectiveDate;
    private String status; // 生效、失效、待生效等

    // 保留带参数的构造函数
    public CustomsCrawlerResult(String title, String url, String content, String publishDate, String country) {
        this();
        this.title = title;
        this.url = url;
        this.content = content;
        this.publishDate = publishDate;
        this.country = country;
    }
}

