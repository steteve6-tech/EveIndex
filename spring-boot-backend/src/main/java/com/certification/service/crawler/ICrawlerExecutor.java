package com.certification.service.crawler;

/**
 * 爬虫执行器统一接口
 * 所有爬虫适配器必须实现此接口，以提供统一的调用方式
 */
public interface ICrawlerExecutor {

    /**
     * 获取爬虫名称
     * 例如：US_510K, EU_Recall, KR_Recall
     * 
     * @return 爬虫名称
     */
    String getCrawlerName();

    /**
     * 获取国家代码
     * 例如：US, EU, CN, KR, JP
     * 
     * @return 国家代码
     */
    String getCountryCode();

    /**
     * 获取爬虫类型
     * 例如：510K, RECALL, EVENT, REGISTRATION, GUIDANCE, CUSTOMS
     * 
     * @return 爬虫类型
     */
    String getCrawlerType();

    /**
     * 执行爬取任务
     * 
     * @param params 爬取参数
     * @return 执行结果
     */
    CrawlerResult execute(CrawlerParams params);

    /**
     * 验证参数是否有效
     * 
     * @param params 爬取参数
     * @return 是否有效
     */
    default boolean validate(CrawlerParams params) {
        if (params == null) {
            return false;
        }
        
        // 基本验证：至少需要关键词或搜索词
        if ((params.getKeywords() == null || params.getKeywords().isEmpty()) &&
            (params.getSearchTerm() == null || params.getSearchTerm().trim().isEmpty())) {
            return false;
        }
        
        return true;
    }

    /**
     * 获取爬虫描述
     * 
     * @return 爬虫描述
     */
    default String getDescription() {
        return String.format("%s国家的%s类型数据爬虫", getCountryCode(), getCrawlerType());
    }

    /**
     * 爬虫是否可用
     * 
     * @return 是否可用
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 获取爬虫版本
     * 
     * @return 版本号
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * 获取唯一标识符（国家_类型）
     * 
     * @return 唯一标识符
     */
    default String getUniqueKey() {
        return getCountryCode() + "_" + getCrawlerType();
    }
}

