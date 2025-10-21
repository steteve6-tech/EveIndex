package com.certification.service.crawler.adapter;

import com.certification.crawler.countrydata.jp.JpGuidance;
import com.certification.service.crawler.CrawlerParams;
import com.certification.service.crawler.CrawlerResult;
import com.certification.service.crawler.ICrawlerExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 日本法规指导文档爬虫适配器
 */
@Slf4j
@Component("JP_Guidance_Adapter")
public class JPGuidanceAdapter implements ICrawlerExecutor {
    
    @Autowired
    private JpGuidance crawler;
    
    @Override
    public String getCrawlerName() {
        return "JP_Guidance";
    }
    
    @Override
    public String getCountryCode() {
        return "JP";
    }
    
    @Override
    public String getCrawlerType() {
        return "GUIDANCE";
    }
    
    @Override
    public CrawlerResult execute(CrawlerParams params) {
        log.info("执行JP_Guidance爬虫，参数: {}", params);
        
        CrawlerResult result = new CrawlerResult().markStart();
        
        try {
            // 提取参数1: 基准名称 (Q_kjn_kname)
            String criteriaName = extractParam(params, "criteriaName", "Q_kjn_kname", "searchKeyword");
            
            // 提取参数2: 功效 (Q_kjn_effect_name)
            String effectName = extractParam(params, "effectName", "Q_kjn_effect_name", "effect");
            
            log.info("基准名称(Q_kjn_kname): {}, 功效(Q_kjn_effect_name): {}", criteriaName, effectName);
            
            String resultMsg = crawler.crawlWithParams(
                criteriaName,
                effectName,
                params.getMaxRecords() != null ? params.getMaxRecords() : 20,
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
            log.error("JP_Guidance爬虫执行失败", e);
            result.markEnd();
            return CrawlerResult.failure("执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从多个可能的参数名中提取参数值
     */
    private String extractParam(CrawlerParams params, String... paramNames) {
        // 1. 优先从fieldKeywords中获取
        if (params.getFieldKeywords() != null) {
            for (String paramName : paramNames) {
                var values = params.getFieldKeywords().get(paramName);
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
            }
        }
        
        // 2. 从searchTerm获取（仅对第一个参数有效）
        if (params.getSearchTerm() != null && !params.getSearchTerm().trim().isEmpty()) {
            return params.getSearchTerm();
        }
        
        // 3. 从keywords列表获取第一个（仅对第一个参数有效）
        if (params.getKeywords() != null && !params.getKeywords().isEmpty()) {
            return params.getKeywords().get(0);
        }
        
        return null;
    }
    
    @Override
    public boolean validate(CrawlerParams params) {
        if (params == null) return false;
        // 日本法规爬虫支持无参数搜索（获取所有数据）
        return true;
    }
    
    @Override
    public String getUniqueKey() {
        return "JP_GUIDANCE";
    }
    
    @Override
    public String getDescription() {
        return "日本PMDA法规指导文档爬虫";
    }
}

