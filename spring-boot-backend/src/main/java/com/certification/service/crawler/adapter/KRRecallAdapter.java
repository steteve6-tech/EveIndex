package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.kr.KrRecall;
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
 * 韩国召回爬虫适配器
 */
@Slf4j
@Component("KR_Recall_Adapter")
public class KRRecallAdapter implements ICrawlerExecutor {
    
    @Autowired
    private KrRecall crawler;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Override
    public String getCrawlerName() {
        return "KR_Recall";
    }
    
    @Override
    public String getCountryCode() {
        return "KR";
    }
    
    @Override
    public String getCrawlerType() {
        return "RECALL";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行KR_Recall爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            List<String> searchKeywords = fieldKeywords.getOrDefault("searchKeywords", new ArrayList<>());
            
            if (searchKeywords.isEmpty() && params.getKeywords() != null) {
                searchKeywords = params.getKeywords();
            }
            
            if (searchKeywords.isEmpty()) {
                searchKeywords = List.of("의료기기");
            }
            
            String resultMsg = crawler.crawlAndSaveWithKeywords(
                searchKeywords,
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
            log.error("KR_Recall爬虫执行失败", e);
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

