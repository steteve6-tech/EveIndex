package com.certification.util;

import com.certification.config.MedcertCrawlerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 爬取参数工具类
 * 统一管理所有爬虫的爬取参数配置
 */
@Component
public class CrawlParamsUtil {
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;
    
    /**
     * 获取默认最大页数
     */
    public int getDefaultMaxPages() {
        return crawlerConfig.getCrawl().getDefaultMaxPages();
    }
    
    /**
     * 获取默认批次大小
     */
    public int getDefaultBatchSize() {
        return crawlerConfig.getCrawl().getDefaultBatchSize();
    }
    
    /**
     * 根据最大页数计算最大记录数
     * @param maxPages 最大页数，0表示爬取所有数据
     * @return 最大记录数，-1表示爬取所有数据
     */
    public int calculateMaxRecords(Integer maxPages) {
        if (maxPages == null || maxPages <= 0) {
            return -1; // 爬取所有数据
        }
        return maxPages * crawlerConfig.getCrawl().getRecordsPerPage();
    }
    
    /**
     * 获取US_510K爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getUS510KBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getUs510kMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            // 爬取所有数据时，使用API限制和默认批次大小的较小值
            return Math.min(batchSize, apiLimit);
        } else {
            // 限制爬取数量时，考虑剩余记录数
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取US_recall_api爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getUSRecallBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getUsRecallMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取US_CustomsCase爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getUSCustomsCaseBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getUsCustomsCaseMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取US_event爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getUSEventBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getUsEventMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    // 欧盟爬虫批次大小计算方法
    
    /**
     * 获取EU_CustomCase爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getEUCustomCaseBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getEuCustomCaseMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取EU_Guidance爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getEUGuidanceBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getEuGuidanceMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取EU_Recall爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getEURecallBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getEuRecallMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取EU_Registration爬虫的批次大小（考虑API限制）
     * @param requestedBatchSize 请求的批次大小
     * @param maxRecords 最大记录数
     * @return 实际使用的批次大小
     */
    public int getEURegistrationBatchSize(int requestedBatchSize, int maxRecords) {
        int apiLimit = crawlerConfig.getCrawl().getApiLimits().getEuRegistrationMaxPerPage();
        int defaultBatchSize = getDefaultBatchSize();
        
        int batchSize = requestedBatchSize > 0 ? requestedBatchSize : defaultBatchSize;
        
        if (maxRecords == -1) {
            return Math.min(batchSize, apiLimit);
        } else {
            return Math.min(batchSize, Math.min(maxRecords, apiLimit));
        }
    }
    
    /**
     * 获取爬虫的默认参数
     * @param crawlerType 爬虫类型
     * @return 包含默认参数的数组 [maxPages, batchSize, maxRecords]
     */
    public int[] getDefaultParams(String crawlerType) {
        int maxPages = getDefaultMaxPages();
        int batchSize = getDefaultBatchSize();
        int maxRecords = calculateMaxRecords(maxPages);
        
        return new int[]{maxPages, batchSize, maxRecords};
    }
}


