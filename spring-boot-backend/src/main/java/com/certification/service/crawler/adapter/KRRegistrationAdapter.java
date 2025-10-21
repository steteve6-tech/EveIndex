package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.kr.KR_regstration;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 韩国注册记录爬虫适配器
 */
@Slf4j
@Component("KR_Registration_Adapter")
public class KRRegistrationAdapter implements ICrawlerExecutor {
    
    @Autowired
    private KR_regstration crawler;
    
    @Override
    public String getCrawlerName() {
        return "KR_Registration";
    }
    
    @Override
    public String getCountryCode() {
        return "KR";
    }
    
    @Override
    public String getCrawlerType() {
        return "REGISTRATION";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行KR_Registration爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取搜索关键词 (query)
            String searchQuery = null;
            List<String> searchQueries = fieldKeywords.getOrDefault("searchQueries", new ArrayList<>());
            if (!searchQueries.isEmpty()) {
                searchQuery = searchQueries.get(0); // 使用第一个搜索关键词
            }
            
            // 提取企业名称列表 (entpName)
            List<String> companyNames = fieldKeywords.getOrDefault("companyNames", new ArrayList<>());
            
            String resultMsg;
            
            // 使用多字段方式爬取
            if ((searchQuery != null && !searchQuery.isEmpty()) || !companyNames.isEmpty()) {
                resultMsg = crawler.crawlWithMultipleFields(
                    searchQuery,
                    companyNames,
                    params.getMaxRecords() != null ? params.getMaxRecords() : -1,
                    params.getBatchSize() != null ? params.getBatchSize() : 100,
                    params.getDateFrom(),
                    params.getDateTo()
                );
            }
            // 默认搜索
            else {
                log.info("未提供任何搜索参数，使用默认搜索");
                resultMsg = crawler.crawlAndSaveToDatabase(
                    null,
                    null,
                    params.getMaxRecords() != null ? params.getMaxRecords() : -1,
                    params.getBatchSize() != null ? params.getBatchSize() : 100,
                    params.getDateFrom(),
                    params.getDateTo()
                );
            }
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);
            
            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("KR_Registration爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        // 韩国注册爬虫支持无参数搜索（获取所有数据）
        return true;
    }
}

