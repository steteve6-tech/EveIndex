package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.us.US_Guidance;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 美国指导文档爬虫适配器
 */
@Slf4j
@Component("US_Guidance_Adapter")
public class USGuidanceAdapter implements ICrawlerExecutor {
    
    @Autowired
    private US_Guidance crawler;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Override
    public String getCrawlerName() {
        return "US_Guidance";
    }
    
    @Override
    public String getCountryCode() {
        return "US";
    }
    
    @Override
    public String getCrawlerType() {
        return "GUIDANCE";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行US_Guidance爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            int maxRecords = params.getMaxRecords() != null ? params.getMaxRecords() : -1;
            
            // US_Guidance不支持关键词搜索
            crawler.crawlWithLimit(maxRecords);
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage("指导文档爬取完成，最大记录数: " + maxRecords);
            result.setSavedCount(maxRecords > 0 ? maxRecords : 50);
            
            return result;
            
        } catch (Exception e) {
            log.error("US_Guidance爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        // US_Guidance不需要关键词，始终有效
        return params != null;
    }
}

