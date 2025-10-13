package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.us.US_registration;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 美国设备注册爬虫适配器
 */
@Slf4j
@Component("US_Registration_Adapter")
public class USRegistrationAdapter implements ICrawlerExecutor {
    
    @Autowired
    private US_registration crawler;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Override
    public String getCrawlerName() {
        return "US_Registration";
    }
    
    @Override
    public String getCountryCode() {
        return "US";
    }
    
    @Override
    public String getCrawlerType() {
        return "REGISTRATION";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行US_Registration爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            List<String> manufacturerNames = fieldKeywords.getOrDefault("manufacturerNames", new ArrayList<>());
            List<String> deviceNames = fieldKeywords.getOrDefault("deviceNames", new ArrayList<>());
            List<String> proprietaryNames = fieldKeywords.getOrDefault("proprietaryNames", new ArrayList<>());
            
            List<String> allKeywords = new ArrayList<>();
            allKeywords.addAll(manufacturerNames);
            allKeywords.addAll(deviceNames);
            allKeywords.addAll(proprietaryNames);
            
            if (allKeywords.isEmpty() && params.getKeywords() != null) {
                allKeywords = params.getKeywords();
            }
            
            if (allKeywords.isEmpty()) {
                allKeywords = List.of("medical");
            }
            
            String resultMsg = crawler.crawlAndSaveWithKeywords(
                allKeywords,
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
            log.error("US_Registration爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        return params.isMultiFieldMode() ? params.getFieldCount() > 0 : 
               (params.getKeywords() != null && !params.getKeywords().isEmpty());
    }
}

