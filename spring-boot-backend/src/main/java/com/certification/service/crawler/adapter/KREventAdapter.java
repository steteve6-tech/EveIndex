package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.kr.KrEvent;
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
 * 韩国不良事件爬虫适配器
 */
@Slf4j
@Component("KR_Event_Adapter")
public class KREventAdapter implements ICrawlerExecutor {
    
    @Autowired
    private KrEvent crawler;
    
    @Override
    public String getCrawlerName() {
        return "KR_Event";
    }
    
    @Override
    public String getCountryCode() {
        return "KR";
    }
    
    @Override
    public String getCrawlerType() {
        return "EVENT";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行KR_Event爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取企业名称列表 (searchPentpNm)
            List<String> companyNames = fieldKeywords.getOrDefault("companyNames", new ArrayList<>());
            
            // 提取产品名称列表 (searchPrdtNm)
            List<String> productNames = fieldKeywords.getOrDefault("productNames", new ArrayList<>());
            
            // 提取型号名称列表 (searchModelnm)
            List<String> modelNames = fieldKeywords.getOrDefault("modelNames", new ArrayList<>());
            
            String resultMsg = crawler.crawlWithMultipleFields(
                companyNames,
                productNames,
                modelNames,
                params.getMaxRecords() != null ? params.getMaxRecords() : -1,
                params.getBatchSize() != null ? params.getBatchSize() : 100,
                params.getDateFrom(),
                params.getDateTo()
            );
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);
            
            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("KR_Event爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        // 韩国不良事件爬虫支持无参数搜索（获取所有数据）
        return true;
    }
}

