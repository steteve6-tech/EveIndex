package com.certification.crawler.countrydata.customs.base;

import java.util.List;

/**
 * 海关信息爬虫基础接口
 */
public interface BaseCustomsCrawler {
    
    /**
     * 爬取最新海关公告
     * @param totalCount 需要爬取的数量
     * @return 海关信息列表
     * @throws Exception 爬取异常
     */
    List<CustomsCrawlerResult> crawlLatestAnnouncements(int totalCount) throws Exception;
    
    /**
     * 爬取指定日期的海关信息
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 海关信息列表
     * @throws Exception 爬取异常
     */
    List<CustomsCrawlerResult> crawlByDateRange(String startDate, String endDate) throws Exception;
    
    /**
     * 爬取指定类型的海关信息
     * @param documentType 文档类型 (公告、法规、通知等)
     * @param totalCount 需要爬取的数量
     * @return 海关信息列表
     * @throws Exception 爬取异常
     */
    List<CustomsCrawlerResult> crawlByDocumentType(String documentType, int totalCount) throws Exception;
    
    /**
     * 爬取指定关键词的海关信息
     * @param keyword 搜索关键词
     * @param totalCount 需要爬取的数量
     * @return 海关信息列表
     * @throws Exception 爬取异常
     */
    List<CustomsCrawlerResult> crawlByKeyword(String keyword, int totalCount) throws Exception;
    
    /**
     * 获取国家代码
     * @return 国家代码 (US, CN, KR, EU, JP, AE)
     */
    String getCountryCode();
    
    /**
     * 获取国家名称
     * @return 国家名称
     */
    String getCountryName();
    
    /**
     * 获取海关机构名称
     * @return 海关机构名称
     */
    String getCustomsOfficeName();
    
    /**
     * 获取支持的文档类型
     * @return 支持的文档类型列表
     */
    List<String> getSupportedDocumentTypes();
    
    /**
     * 检查爬虫是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取爬虫配置
     * @return 爬虫配置
     */
    CustomsCrawlerConfig getConfig();
    
    /**
     * 海关爬虫配置类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    class CustomsCrawlerConfig {
        private String baseUrl;
        private String announcementUrl;
        private String regulationUrl;
        private int timeout = 30000;
        private int retryCount = 3;
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private String language = "en"; // 支持的语言
    }
}

