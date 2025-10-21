package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.kr.KrGuidance;
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
 * 韩国指导文档爬虫适配器
 */
@Slf4j
@Component("KR_Guidance_Adapter")
public class KRGuidanceAdapter implements ICrawlerExecutor {
    
    @Autowired
    private KrGuidance crawler;
    
    @Override
    public String getCrawlerName() {
        return "KR_Guidance";
    }
    
    @Override
    public String getCountryCode() {
        return "KR";
    }
    
    @Override
    public String getCrawlerType() {
        return "GUIDANCE";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行KR_Guidance爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            // 提取搜索关键词列表 (searchKwd)
            List<String> searchKeywords = fieldKeywords.getOrDefault("searchKeywords", new ArrayList<>());
            
            // 兼容旧版本的keywords参数
            if (searchKeywords.isEmpty() && params.getKeywords() != null) {
                searchKeywords = params.getKeywords();
            }
            
            String resultMsg = crawler.crawlWithKeywords(
                searchKeywords,
                params.getMaxRecords() != null ? params.getMaxRecords() : -1,
                params.getBatchSize() != null ? params.getBatchSize() : 100
            );
            
            result.markEnd();
            result.setSuccess(true);
            result.setMessage(resultMsg);
            
            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("KR_Guidance爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        // 韩国指导文档爬虫支持无参数搜索（获取所有数据）
        return true;
    }
}

