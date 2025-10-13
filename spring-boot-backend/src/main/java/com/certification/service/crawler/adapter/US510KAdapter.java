package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.us.US_510K;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import com.certification.service.crawler.schema.CrawlerSchema;
import com.certification.service.crawler.schema.CrawlerSchemaRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 美国510K爬虫适配器
 */
@Slf4j
@Component("US_510K_Adapter")
public class US510KAdapter implements ICrawlerExecutor {
    
    @Autowired
    private US_510K crawler;
    
    @Autowired
    private CrawlerSchemaRegistry schemaRegistry;
    
    @Override
    public String getCrawlerName() {
        return "US_510K";
    }
    
    @Override
    public String getCountryCode() {
        return "US";
    }
    
    @Override
    public String getCrawlerType() {
        return "510K";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行US_510K爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            // 从fieldKeywords中提取参数（V2模式）
            Map<String, List<String>> fieldKeywords = params.getFieldKeywords();
            
            List<String> deviceNames = fieldKeywords.getOrDefault("deviceNames", new ArrayList<>());
            List<String> applicants = fieldKeywords.getOrDefault("applicants", new ArrayList<>());
            List<String> tradeNames = fieldKeywords.getOrDefault("tradeNames", new ArrayList<>());
            
            // 合并所有关键词
            List<String> allKeywords = new ArrayList<>();
            allKeywords.addAll(deviceNames);
            allKeywords.addAll(applicants);
            allKeywords.addAll(tradeNames);
            
            // 如果使用V1模式（单一关键词列表）
            if (allKeywords.isEmpty() && params.getKeywords() != null) {
                allKeywords = params.getKeywords();
            }
            
            // 如果仍然为空，使用默认搜索
            if (allKeywords.isEmpty()) {
                allKeywords = List.of("medical");
            }
            
            // 调用爬虫方法
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
            
            // 从返回字符串解析结果
            return CrawlerResult.fromString(resultMsg)
                .setStartTime(result.getStartTime())
                .setEndTime(result.getEndTime())
                .setDurationSeconds(result.getDurationSeconds());
            
        } catch (Exception e) {
            log.error("US_510K爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        
        // V2模式：检查fieldKeywords
        if (params.isMultiFieldMode()) {
            return params.getFieldCount() > 0;
        }
        
        // V1模式：检查keywords
        return params.getKeywords() != null && !params.getKeywords().isEmpty();
    }
}

