package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.kr.Kr_customcase;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 韩国海关案例爬虫适配器
 * 将Kr_customcase爬虫适配到ICrawlerExecutor接口
 */
@Slf4j
@Component
public class KRCustomsCaseAdapter implements ICrawlerExecutor {

    @Autowired
    private Kr_customcase krCustomsCaseCrawler;

    @Override
    public String getCrawlerName() {
        return "KR_CustomsCase";
    }

    @Override
    public String getCountryCode() {
        return "KR";
    }

    @Override
    public String getCrawlerType() {
        return "CustomsCase";
    }

    @Override
    public String getUniqueKey() {
        return "KR_CUSTOMS_CASE";
    }

    @Override
    public CrawlerResult execute(CrawlerParams params) {
        try {
            log.info("执行KR_CustomsCase爬虫，参数: {}", params);
            
            // 从fieldKeywords中提取参数
            List<String> searchKeywords = params.getFieldKeywords("searchQueries");
            List<String> productNames = params.getFieldKeywords("productNames");
            
            // 执行爬取
            String result = krCustomsCaseCrawler.crawlWithMultipleFields(
                searchKeywords,
                productNames,
                params.getMaxRecords(),
                params.getBatchSize(),
                params.getDateFrom(),
                params.getDateTo()
            );
            
            log.info("KR_CustomsCase爬虫执行完成: {}", result);
            
            return CrawlerResult.success(result);
            
        } catch (Exception e) {
            log.error("执行KR_CustomsCase爬虫时发生错误: {}", e.getMessage(), e);
            return CrawlerResult.failure("爬虫执行失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validate(CrawlerParams params) {
        // 韩国海关案例爬虫支持无参数执行（默认搜索）
        return true;
    }

    @Override
    public String getDescription() {
        return "韩国海关案例数据爬虫 - 爬取韩国海关厅的海关案例数据，包括执行机构、HS编码、产品名称等信息";
    }

}
