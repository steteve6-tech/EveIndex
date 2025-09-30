package com.certification.controller;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.util.CrawlParamsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫配置控制器
 * 提供爬虫配置信息的API接口
 */
@RestController
@RequestMapping("/crawler-config")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3100", "http://localhost:3101", "http://127.0.0.1:3101"})
public class CrawlerConfigController {
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;
    
    @Autowired
    private CrawlParamsUtil crawlParamsUtil;
    
    /**
     * 获取爬虫默认配置
     */
    @GetMapping("/defaults")
    public Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 基本配置
        config.put("defaultMaxPages", crawlerConfig.getCrawl().getDefaultMaxPages());
        config.put("defaultBatchSize", crawlerConfig.getCrawl().getDefaultBatchSize());
        config.put("recordsPerPage", crawlerConfig.getCrawl().getRecordsPerPage());
        
        // API限制配置
        Map<String, Object> apiLimits = new HashMap<>();
        apiLimits.put("us510kMaxPerPage", crawlerConfig.getCrawl().getApiLimits().getUs510kMaxPerPage());
        apiLimits.put("usRecallMaxPerPage", crawlerConfig.getCrawl().getApiLimits().getUsRecallMaxPerPage());
        apiLimits.put("usCustomsCaseMaxPerPage", crawlerConfig.getCrawl().getApiLimits().getUsCustomsCaseMaxPerPage());
        apiLimits.put("usEventMaxPerPage", crawlerConfig.getCrawl().getApiLimits().getUsEventMaxPerPage());
        config.put("apiLimits", apiLimits);
        
        // 计算默认maxRecords
        int defaultMaxRecords = crawlParamsUtil.calculateMaxRecords(crawlerConfig.getCrawl().getDefaultMaxPages());
        config.put("defaultMaxRecords", defaultMaxRecords);
        
        return config;
    }
    
    /**
     * 获取爬取所有数据的配置
     */
    @GetMapping("/full-crawl")
    public Map<String, Object> getFullCrawlConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 爬取所有数据的参数
        config.put("maxPages", 0);  // 0表示爬取所有页
        config.put("batchSize", crawlerConfig.getCrawl().getDefaultBatchSize());
        config.put("maxRecords", -1);  // -1表示爬取所有记录
        config.put("recordsPerPage", crawlerConfig.getCrawl().getRecordsPerPage());
        
        // 添加说明
        config.put("description", "爬取所有可用数据的配置");
        config.put("note", "maxPages=0 和 maxRecords=-1 表示爬取所有数据");
        
        return config;
    }
    
    /**
     * 获取特定爬虫的默认参数
     */
    @GetMapping("/crawler/{crawlerType}/defaults")
    public Map<String, Object> getCrawlerDefaults(@PathVariable String crawlerType) {
        Map<String, Object> params = new HashMap<>();
        
        int[] defaultParams = crawlParamsUtil.getDefaultParams(crawlerType);
        params.put("maxPages", defaultParams[0]);
        params.put("batchSize", defaultParams[1]);
        params.put("maxRecords", defaultParams[2]);
        
        // 根据爬虫类型添加特定参数
        switch (crawlerType.toLowerCase()) {
            // 美国爬虫
            case "us510k":
            case "usevent":
            case "usrecall":
            case "usregistration":
                params.put("useKeywords", false);
                break;
            case "customs-case":
                params.put("hsCode", "9018");
                params.put("useKeywords", false);
                break;
            case "guidance":
                params.put("useKeywords", false);
                break;
            // 欧盟爬虫
            case "eu-custom-case":
                params.put("taricCode", "9018");
                params.put("useKeywords", false);
                break;
            case "eu-guidance":
            case "eu-recall":
            case "eu-registration":
                params.put("useKeywords", false);
                break;
            default:
                params.put("useKeywords", false);
        }
        
        return params;
    }
    
    /**
     * 计算爬取参数
     */
    @PostMapping("/calculate")
    public Map<String, Object> calculateParams(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        String crawlerType = (String) request.get("crawlerType");
        Integer maxPages = (Integer) request.get("maxPages");
        Integer batchSize = (Integer) request.get("batchSize");
        
        if (maxPages != null) {
            int maxRecords = crawlParamsUtil.calculateMaxRecords(maxPages);
            result.put("maxRecords", maxRecords);
        }
        
        if (batchSize != null && maxPages != null) {
            int maxRecords = crawlParamsUtil.calculateMaxRecords(maxPages);
            int actualBatchSize = 0;
            
            switch (crawlerType.toLowerCase()) {
                // 美国爬虫
                case "us510k":
                    actualBatchSize = crawlParamsUtil.getUS510KBatchSize(batchSize, maxRecords);
                    break;
                case "usrecall":
                    actualBatchSize = crawlParamsUtil.getUSRecallBatchSize(batchSize, maxRecords);
                    break;
                case "customs-case":
                    actualBatchSize = crawlParamsUtil.getUSCustomsCaseBatchSize(batchSize, maxRecords);
                    break;
                case "usevent":
                    actualBatchSize = crawlParamsUtil.getUSEventBatchSize(batchSize, maxRecords);
                    break;
                // 欧盟爬虫
                case "eu-custom-case":
                    actualBatchSize = crawlParamsUtil.getEUCustomCaseBatchSize(batchSize, maxRecords);
                    break;
                case "eu-guidance":
                    actualBatchSize = crawlParamsUtil.getEUGuidanceBatchSize(batchSize, maxRecords);
                    break;
                case "eu-recall":
                    actualBatchSize = crawlParamsUtil.getEURecallBatchSize(batchSize, maxRecords);
                    break;
                case "eu-registration":
                    actualBatchSize = crawlParamsUtil.getEURegistrationBatchSize(batchSize, maxRecords);
                    break;
                default:
                    actualBatchSize = batchSize;
            }
            
            result.put("actualBatchSize", actualBatchSize);
        }
        
        return result;
    }
}


