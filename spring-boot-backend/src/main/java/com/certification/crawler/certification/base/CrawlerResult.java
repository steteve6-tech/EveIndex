package com.certification.crawler.certification.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 爬虫结果基础类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerResult {
    private String title;
    private String url;
    private String content;
    private String date;
    private String source;
    private LocalDateTime crawlTime = LocalDateTime.now();
    private String category;
    private String country;
    private String type;

    // 保留带参数的构造函数
    public CrawlerResult(String title, String url, String content, String date, String source) {
        this();
        this.title = title;
        this.url = url;
        this.content = content;
        this.date = date;
        this.source = source;
    }
}

