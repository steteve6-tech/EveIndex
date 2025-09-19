package com.certification.crawler;

import com.certification.entity.common.CrawlerData;
import com.certification.repository.CrawlerDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 爬虫数据处理器
 * 演示如何使用fliters和KeywordsConfig进行数据过滤和分类
 */
@Slf4j
@Service
public class CrawlerDataProcessor {
    
    @Autowired
    private FliterByKeyword filters;
    
    @Autowired
    private KeywordsConfig keywordsConfig;
    
    @Autowired
    private CrawlerDataRepository crawlerDataRepository;
    
    /**
     * 处理爬取的数据
     */
    public void processCrawledData(String title, String content, String source, String url) {
        log.info("处理爬取数据: title={}, source={}", title, source);
        
        // 合并标题和内容进行关键词匹配
        String fullText = title + " " + content;
        
        // 检查数据相关性
        boolean isRelevant = checkRelevance(fullText);
        
        if (isRelevant) {
            // 相关数据，保存到数据库
            CrawlerData data = createCrawlerData(title, content, source, url, fullText);
            crawlerDataRepository.save(data);
            log.info("保存相关数据: {}", title);
        } else {
            log.debug("过滤无关数据: {}", title);
        }
    }
    
    /**
     * 检查数据相关性
     */
    private boolean checkRelevance(String text) {
        // 使用所有关键词进行匹配
        List<String> allKeywords = keywordsConfig.getAllKeywords();
        return filters.isRelevant(text, allKeywords, FliterByKeyword.MatchMode.CONTAINS, FliterByKeyword.MatchStrategy.ANY);
    }
    
    /**
     * 创建爬虫数据实体
     */
    private CrawlerData createCrawlerData(String title, String content, String source, String url, String fullText) {
        CrawlerData data = new CrawlerData();
        data.setId(generateId());
        data.setTitle(title);
        data.setContent(content);
        data.setSourceName(source);
        data.setUrl(url);
        data.setCrawlTime(LocalDateTime.now());
        data.setStatus(CrawlerData.DataStatus.NEW);
        data.setIsProcessed(false);
        
        // 计算相关性分数并存储在备注中
        List<String> allKeywords = keywordsConfig.getAllKeywords();
        double relevanceScore = filters.calculateRelevanceScore(fullText, allKeywords, FliterByKeyword.MatchMode.CONTAINS);
        
        // 获取匹配的类别
        List<String> matchedCategories = filters.getMatchedCategories(fullText);
        
        // 获取匹配的关键词
        List<String> matchedKeywords = filters.getMatchedKeywords(fullText, allKeywords, FliterByKeyword.MatchMode.CONTAINS);
        
        // 将相关信息存储在备注中
        String remarks = String.format("RelevanceScore: %.2f, Categories: %s, Keywords: %s", 
                relevanceScore, String.join(",", matchedCategories), String.join(",", matchedKeywords));
        data.setRemarks(remarks);
        
        // 设置类型为匹配的主要类别
        if (!matchedCategories.isEmpty()) {
            data.setType(matchedCategories.get(0).toUpperCase());
        }
        
        return data;
    }
    
    /**
     * 生成唯一ID
     */
    private String generateId() {
        return "crawler_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 分类处理数据
     */
    public String classifyData(String text) {
        if (filters.isCertificationRelated(text)) {
            return "CERTIFICATION";
        } else if (filters.isRecallRelated(text)) {
            return "RECALL";
        } else if (filters.isRegulationRelated(text)) {
            return "REGULATION";
        } else if (filters.isStandardRelated(text)) {
            return "STANDARD";
        } else if (filters.isSafetyRelated(text)) {
            return "SAFETY";
        } else if (filters.isEnvironmentalRelated(text)) {
            return "ENVIRONMENTAL";
        } else if (filters.isProductRelated(text)) {
            return "PRODUCT";
        } else if (filters.isIndustryRelated(text)) {
            return "INDUSTRY";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 智能过滤配置
     */
    public boolean shouldProcess(String text, FilterConfig config) {
        // 根据配置选择匹配模式和策略
        FliterByKeyword.MatchMode mode = config.isStrict() ?
            FliterByKeyword.MatchMode.EXACT : FliterByKeyword.MatchMode.CONTAINS;
        
        FliterByKeyword.MatchStrategy strategy = config.isRequireAll() ?
            FliterByKeyword.MatchStrategy.ALL : FliterByKeyword.MatchStrategy.ANY;
        
        // 获取指定类别的关键词
        List<String> keywords = keywordsConfig.getKeywordsByCategory(config.getCategory());
        
        // 计算匹配度
        double score = filters.calculateRelevanceScore(text, keywords, mode);
        
        // 根据阈值判断
        return score >= config.getThreshold();
    }
    
    /**
     * 获取关键词统计信息
     */
    public Map<String, Integer> getKeywordStatistics() {
        return filters.getKeywordStatistics();
    }
    
    /**
     * 搜索关键词
     */
    public List<String> searchKeywords(String searchTerm) {
        return filters.searchKeywords(searchTerm);
    }
    
    /**
     * 批量处理数据
     */
    public void batchProcess(List<CrawlerData> dataList) {
        log.info("开始批量处理 {} 条数据", dataList.size());
        
        int processedCount = 0;
        int savedCount = 0;
        
        for (CrawlerData data : dataList) {
            processedCount++;
            
            String fullText = data.getTitle() + " " + data.getContent();
            boolean isRelevant = checkRelevance(fullText);
            
            if (isRelevant) {
                // 更新数据
                updateCrawlerData(data, fullText);
                crawlerDataRepository.save(data);
                savedCount++;
            }
            
            // 每处理100条数据记录一次日志
            if (processedCount % 100 == 0) {
                log.info("已处理 {} 条数据，保存 {} 条", processedCount, savedCount);
            }
        }
        
        log.info("批量处理完成，共处理 {} 条数据，保存 {} 条", processedCount, savedCount);
    }
    
    /**
     * 更新爬虫数据
     */
    private void updateCrawlerData(CrawlerData data, String fullText) {
        // 计算相关性分数
        List<String> allKeywords = keywordsConfig.getAllKeywords();
        double relevanceScore = filters.calculateRelevanceScore(fullText, allKeywords, FliterByKeyword.MatchMode.CONTAINS);
        
        // 获取匹配的类别
        List<String> matchedCategories = filters.getMatchedCategories(fullText);
        
        // 获取匹配的关键词
        List<String> matchedKeywords = filters.getMatchedKeywords(fullText, allKeywords, FliterByKeyword.MatchMode.CONTAINS);
        
        // 将相关信息存储在备注中
        String remarks = String.format("RelevanceScore: %.2f, Categories: %s, Keywords: %s", 
                relevanceScore, String.join(",", matchedCategories), String.join(",", matchedKeywords));
        data.setRemarks(remarks);
        
        // 设置类型为匹配的主要类别
        if (!matchedCategories.isEmpty()) {
            data.setType(matchedCategories.get(0).toUpperCase());
        }
        
        // 设置处理状态
        data.setStatus(CrawlerData.DataStatus.PROCESSED);
        data.setIsProcessed(true);
        data.setProcessedTime(LocalDateTime.now());
    }
    
    /**
     * 过滤配置类
     */
    public static class FilterConfig {
        private String category = "all";
        private boolean strict = false;
        private boolean requireAll = false;
        private double threshold = 0.1;
        
        public FilterConfig() {}
        
        public FilterConfig(String category, boolean strict, boolean requireAll, double threshold) {
            this.category = category;
            this.strict = strict;
            this.requireAll = requireAll;
            this.threshold = threshold;
        }
        
        // Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public boolean isStrict() { return strict; }
        public void setStrict(boolean strict) { this.strict = strict; }
        
        public boolean isRequireAll() { return requireAll; }
        public void setRequireAll(boolean requireAll) { this.requireAll = requireAll; }
        
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
    }
}
