package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.eu.Eu_guidance;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 欧盟指导文档爬虫适配器
 */
@Slf4j
@Component("EU_Guidance_Adapter")
public class EUGuidanceAdapter implements ICrawlerExecutor {
    
    @Autowired
    private Eu_guidance crawler;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Override
    public String getCrawlerName() {
        return "EU_Guidance";
    }
    
    @Override
    public String getCountryCode() {
        return "EU";
    }
    
    @Override
    public String getCrawlerType() {
        return "GUIDANCE";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行EU_Guidance爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            int maxPages = params.getMaxRecords() != null && params.getMaxRecords() > 0 ? 
                params.getMaxRecords() / 10 : 5; // 转换为页数
            
            int savedCount = crawler.crawlAndSaveToDatabase(maxPages);
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage("指导文档爬取完成，爬取页数: " + maxPages);
            result.setSavedCount(savedCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("EU_Guidance爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        // EU_Guidance不需要关键词，始终有效
        return params != null;
    }
}

