package com.certification.standards;

import com.certification.entity.common.CertNewsData;
// import com.certification.service.SystemLogService; // 已删除
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CrawlerData关键词匹配服务
 * 用于根据关键词配置匹配CrawlerData数据并更新related字段
 */
@Slf4j
@Service
@Transactional
public class CrawlerDataKeywordMatcher {

    @Autowired
    private KeywordConfig keywordConfig;

    @Autowired
    private CrawlerDataService crawlerDataService;

    // @Autowired
    // private SystemLogService systemLogService; // 已删除

    /**
     * 执行关键词匹配并更新related字段
     * @param batchSize 批处理大小
     * @return 匹配结果统计
     */
    public Map<String, Object> executeKeywordMatching(int batchSize) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行CrawlerData关键词匹配，批处理大小: {}", batchSize);

            // 获取所有未处理的CrawlerData记录（使用分页查询）
            List<CertNewsData> allData = new ArrayList<>();
            int page = 0;
            int pageSize = 1000;
            boolean hasMore = true;
            
            while (hasMore) {
                Map<String, Object> pageResult = crawlerDataService.findAllWithPagination(page + 1, pageSize);
                List<CertNewsData> pageData = (List<CertNewsData>) pageResult.get("records");
                if (pageData.isEmpty()) {
                    hasMore = false;
                } else {
                    // 过滤未处理的数据
                    List<CertNewsData> unprocessedData = pageData.stream()
                        .filter(data -> data.getIsProcessed() == null || !data.getIsProcessed())
                        .collect(Collectors.toList());
                    allData.addAll(unprocessedData);
                    page++;
                }
            }
            
            log.info("找到 {} 条未处理的CrawlerData记录", allData.size());

            // 获取关键词配置
            Map<String, KeywordConfig.MarketKeyword> marketKeywords = keywordConfig.getMarketKeywords();
            if (marketKeywords == null || marketKeywords.isEmpty()) {
                log.warn("未找到关键词配置，使用默认配置");
                keywordConfig.initDefaultMarketKeywords();
                marketKeywords = keywordConfig.getMarketKeywords();
            }

            // 构建关键词匹配器
            KeywordMatcher matcher = new KeywordMatcher(marketKeywords);

            // 分批处理数据
            int totalProcessed = 0;
            int totalMatched = 0;
            int totalUnmatched = 0;
            List<CertNewsData> updatedData = new ArrayList<>();

            for (int i = 0; i < allData.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allData.size());
                List<CertNewsData> batch = allData.subList(i, endIndex);
                
                log.info("处理批次 {}/{}, 数据量: {}", 
                    (i / batchSize) + 1, 
                    (allData.size() + batchSize - 1) / batchSize, 
                    batch.size());

                for (CertNewsData data : batch) {
                    boolean isRelated = matcher.isRelated(data);
                    data.setRelated(isRelated);
                    data.setProcessedTime(LocalDateTime.now());
                    data.setIsProcessed(true);
                    
                    if (isRelated) {
                        totalMatched++;
                    } else {
                        totalUnmatched++;
                    }
                    
                    updatedData.add(data);
                    totalProcessed++;
                }

                // 批量保存
                if (!updatedData.isEmpty()) {
                    for (CertNewsData data : updatedData) {
                        crawlerDataService.saveCrawlerData(data);
                    }
                    updatedData.clear();
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // 构建返回结果
            result.put("success", true);
            result.put("totalProcessed", totalProcessed);
            result.put("totalMatched", totalMatched);
            result.put("totalUnmatched", totalUnmatched);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "关键词匹配完成");

            log.info("CrawlerData关键词匹配完成，总处理: {}, 匹配: {}, 不匹配: {}, 耗时: {} ms",
                totalProcessed, totalMatched, totalUnmatched, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.error("CrawlerData关键词匹配失败: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", "关键词匹配失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 执行关键词匹配并更新related字段（指定数据源）
     * @param sourceName 数据源名称
     * @param batchSize 批处理大小
     * @return 匹配结果统计
     */
    public Map<String, Object> executeKeywordMatchingBySource(String sourceName, int batchSize) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始执行CrawlerData关键词匹配（数据源: {}），批处理大小: {}", sourceName, batchSize);
            log.info(
                "CrawlerData关键词匹配开始（指定数据源）",
                String.format("开始执行CrawlerData关键词匹配，数据源: %s，批处理大小: %d", sourceName, batchSize),
                "CrawlerDataKeywordMatcher"
            );

            // 获取指定数据源的未处理记录（使用分页查询）
            List<CertNewsData> allData = new ArrayList<>();
            int page = 0;
            int pageSize = 1000;
            boolean hasMore = true;
            
            while (hasMore) {
                Map<String, Object> pageResult = crawlerDataService.findBySourceNameWithPagination(sourceName, page + 1, pageSize);
                List<CertNewsData> pageData = (List<CertNewsData>) pageResult.get("records");
                if (pageData.isEmpty()) {
                    hasMore = false;
                } else {
                    // 过滤未处理的数据
                    List<CertNewsData> unprocessedData = pageData.stream()
                        .filter(data -> data.getIsProcessed() == null || !data.getIsProcessed())
                        .collect(Collectors.toList());
                    allData.addAll(unprocessedData);
                    page++;
                }
            }
            
            log.info("找到 {} 条未处理的CrawlerData记录（数据源: {}）", allData.size(), sourceName);

            // 获取关键词配置
            Map<String, KeywordConfig.MarketKeyword> marketKeywords = keywordConfig.getMarketKeywords();
            if (marketKeywords == null || marketKeywords.isEmpty()) {
                log.warn("未找到关键词配置，使用默认配置");
                keywordConfig.initDefaultMarketKeywords();
                marketKeywords = keywordConfig.getMarketKeywords();
            }

            // 构建关键词匹配器
            KeywordMatcher matcher = new KeywordMatcher(marketKeywords);

            // 分批处理数据
            int totalProcessed = 0;
            int totalMatched = 0;
            int totalUnmatched = 0;
            List<CertNewsData> updatedData = new ArrayList<>();

            for (int i = 0; i < allData.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allData.size());
                List<CertNewsData> batch = allData.subList(i, endIndex);
                
                log.info("处理批次 {}/{}, 数据量: {}", 
                    (i / batchSize) + 1, 
                    (allData.size() + batchSize - 1) / batchSize, 
                    batch.size());

                for (CertNewsData data : batch) {
                    boolean isRelated = matcher.isRelated(data);
                    data.setRelated(isRelated);
                    data.setProcessedTime(LocalDateTime.now());
                    data.setIsProcessed(true);
                    
                    if (isRelated) {
                        totalMatched++;
                    } else {
                        totalUnmatched++;
                    }
                    
                    updatedData.add(data);
                    totalProcessed++;
                }

                // 批量保存
                if (!updatedData.isEmpty()) {
                    for (CertNewsData data : updatedData) {
                        crawlerDataService.saveCrawlerData(data);
                    }
                    updatedData.clear();
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // 记录成功日志
            log.info(
                "CrawlerData关键词匹配完成（指定数据源）",
                String.format("CrawlerData关键词匹配完成，数据源: %s，总处理: %d, 匹配: %d, 不匹配: %d, 耗时: %d ms",
                    sourceName, totalProcessed, totalMatched, totalUnmatched, executionTime),
                "CrawlerDataKeywordMatcher"
            );

            // 构建返回结果
            result.put("success", true);
            result.put("sourceName", sourceName);
            result.put("totalProcessed", totalProcessed);
            result.put("totalMatched", totalMatched);
            result.put("totalUnmatched", totalUnmatched);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "关键词匹配完成");

            log.info("CrawlerData关键词匹配完成（数据源: {}），总处理: {}, 匹配: {}, 不匹配: {}, 耗时: {} ms",
                sourceName, totalProcessed, totalMatched, totalUnmatched, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.error("CrawlerData关键词匹配失败（数据源: {}）: {}", sourceName, e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", "关键词匹配失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 获取匹配统计信息
     * @return 统计信息
     */
    public Map<String, Object> getMatchingStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取总体统计
            long totalCount = crawlerDataService.getCountBySourceName("ALL"); // 使用一个不存在的源来获取总数
            long relatedCount = crawlerDataService.countByRelated(true);
            long unrelatedCount = crawlerDataService.countByRelated(false);

            // 按数据源统计
            Map<String, Object> sourceStats = new HashMap<>();
            List<Map<String, Object>> sourceCounts = crawlerDataService.countBySourceName();
            
            for (Map<String, Object> sourceCount : sourceCounts) {
                String sourceName = (String) sourceCount.get("sourceName");
                Long count = (Long) sourceCount.get("count");
                
                Map<String, Long> stats = new HashMap<>();
                stats.put("total", count);
                stats.put("related", crawlerDataService.countByRelated(true));
                stats.put("unrelated", crawlerDataService.countByRelated(false));
                sourceStats.put(sourceName, stats);
            }

            result.put("success", true);
            result.put("overall", Map.of(
                "totalCount", totalCount,
                "relatedCount", relatedCount,
                "unrelatedCount", unrelatedCount
            ));
            result.put("sourceStats", sourceStats);
            result.put("timestamp", LocalDateTime.now().toString());

        } catch (Exception e) {
            log.error("获取匹配统计信息失败", e);
            result.put("success", false);
            result.put("error", "获取统计信息失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 关键词匹配器内部类
     */
    private static class KeywordMatcher {
        private final Map<String, KeywordConfig.MarketKeyword> marketKeywords;
        private final Map<String, List<Pattern>> keywordPatterns;

        public KeywordMatcher(Map<String, KeywordConfig.MarketKeyword> marketKeywords) {
            this.marketKeywords = marketKeywords;
            this.keywordPatterns = buildKeywordPatterns();
        }

        /**
         * 判断数据是否相关
         * @param data CrawlerData数据
         * @return 是否相关
         */
        public boolean isRelated(CertNewsData data) {
            if (data == null) {
                return false;
            }

            // 构建搜索文本
            String searchText = buildSearchText(data);
            if (!StringUtils.hasText(searchText)) {
                return false;
            }

            // 转换为小写进行匹配
            String lowerSearchText = searchText.toLowerCase();

            // 遍历所有关键词模式进行匹配
            for (List<Pattern> patterns : keywordPatterns.values()) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(lowerSearchText).find()) {
                        log.debug("数据匹配成功，ID: {}, 标题: {}, 匹配模式: {}", 
                            data.getId(), data.getTitle(), pattern.pattern());
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * 构建搜索文本
         * @param data CrawlerData数据
         * @return 搜索文本
         */
        private String buildSearchText(CertNewsData data) {
            StringBuilder sb = new StringBuilder();
            
            // 添加标题
            if (StringUtils.hasText(data.getTitle())) {
                sb.append(data.getTitle()).append(" ");
            }
            
            // 添加摘要
            if (StringUtils.hasText(data.getSummary())) {
                sb.append(data.getSummary()).append(" ");
            }
            
            // 添加内容
            if (StringUtils.hasText(data.getContent())) {
                sb.append(data.getContent()).append(" ");
            }
            
            // 添加产品信息
            if (StringUtils.hasText(data.getProduct())) {
                sb.append(data.getProduct()).append(" ");
            }
            
            // 添加类型
            if (StringUtils.hasText(data.getType())) {
                sb.append(data.getType()).append(" ");
            }
            
            // 添加国家/地区
            if (StringUtils.hasText(data.getCountry())) {
                sb.append(data.getCountry()).append(" ");
            }
            
            return sb.toString();
        }

        /**
         * 构建关键词模式
         * @return 关键词模式映射
         */
        private Map<String, List<Pattern>> buildKeywordPatterns() {
            Map<String, List<Pattern>> patterns = new HashMap<>();
            
            for (Map.Entry<String, KeywordConfig.MarketKeyword> entry : marketKeywords.entrySet()) {
                String region = entry.getKey();
                KeywordConfig.MarketKeyword keyword = entry.getValue();
                List<Pattern> regionPatterns = new ArrayList<>();
                
                // 添加主要关键词
                if (StringUtils.hasText(keyword.getPrimaryKeywords())) {
                    String[] keywords = keyword.getPrimaryKeywords().split(",");
                    for (String kw : keywords) {
                        if (StringUtils.hasText(kw.trim())) {
                            regionPatterns.add(Pattern.compile(Pattern.quote(kw.trim().toLowerCase())));
                        }
                    }
                }
                
                // 添加本地语言关键词
                if (StringUtils.hasText(keyword.getLocalLangKeywords())) {
                    String[] keywords = keyword.getLocalLangKeywords().split(",");
                    for (String kw : keywords) {
                        if (StringUtils.hasText(kw.trim())) {
                            regionPatterns.add(Pattern.compile(Pattern.quote(kw.trim().toLowerCase())));
                        }
                    }
                }
                
                // 添加延伸技术词
                if (StringUtils.hasText(keyword.getExtendTechTerms())) {
                    String[] keywords = keyword.getExtendTechTerms().split(",");
                    for (String kw : keywords) {
                        if (StringUtils.hasText(kw.trim())) {
                            // 移除"必追新制"等标注
                            String cleanKeyword = kw.trim().replaceAll("\\(必追新制\\)", "").trim();
                            if (StringUtils.hasText(cleanKeyword)) {
                                regionPatterns.add(Pattern.compile(Pattern.quote(cleanKeyword.toLowerCase())));
                            }
                        }
                    }
                }
                
                if (!regionPatterns.isEmpty()) {
                    patterns.put(region, regionPatterns);
                }
            }
            
            return patterns;
        }
    }
}
