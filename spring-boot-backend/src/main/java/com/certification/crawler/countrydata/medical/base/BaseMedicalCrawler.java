package com.certification.crawler.countrydata.medical.base;

import java.util.List;

/**
 * 医疗器械信息爬虫基础接口
 */
public interface BaseMedicalCrawler {
    
    /**
     * 爬取最新医疗器械公告
     * @param totalCount 需要爬取的数量
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlLatestAnnouncements(int totalCount) throws Exception;
    
    /**
     * 爬取指定日期的医疗器械信息
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlByDateRange(String startDate, String endDate) throws Exception;
    
    /**
     * 爬取指定类型的医疗器械信息
     * @param documentType 文档类型 (公告、法规、标准、召回等)
     * @param totalCount 需要爬取的数量
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlByDocumentType(String documentType, int totalCount) throws Exception;
    
    /**
     * 爬取指定设备类别的医疗器械信息
     * @param deviceCategory 设备类别
     * @param totalCount 需要爬取的数量
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlByDeviceCategory(String deviceCategory, int totalCount) throws Exception;
    
    /**
     * 爬取指定风险等级的医疗器械信息
     * @param riskLevel 风险等级
     * @param totalCount 需要爬取的数量
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlByRiskLevel(String riskLevel, int totalCount) throws Exception;
    
    /**
     * 爬取指定关键词的医疗器械信息
     * @param keyword 搜索关键词
     * @param totalCount 需要爬取的数量
     * @return 医疗器械信息列表
     * @throws Exception 爬取异常
     */
    List<MedicalCrawlerResult> crawlByKeyword(String keyword, int totalCount) throws Exception;
    
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
     * 获取监管机构名称
     * @return 监管机构名称
     */
    String getRegulatoryAuthorityName();
    
    /**
     * 获取支持的文档类型
     * @return 支持的文档类型列表
     */
    List<String> getSupportedDocumentTypes();
    
    /**
     * 获取支持的设备类别
     * @return 支持的设备类别列表
     */
    List<String> getSupportedDeviceCategories();
    
    /**
     * 获取支持的风险等级
     * @return 支持的风险等级列表
     */
    List<String> getSupportedRiskLevels();
    
    /**
     * 检查爬虫是否可用
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取爬虫配置
     * @return 爬虫配置
     */
    MedicalCrawlerConfig getConfig();
    
    /**
     * 医疗器械爬虫配置类
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    class MedicalCrawlerConfig {
        private String baseUrl;
        private String announcementUrl;
        private String regulationUrl;
        private String recallUrl;
        private String approvalUrl;
        private int timeout = 30000;
        private int retryCount = 3;
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private String language = "en"; // 支持的语言
    }
}

