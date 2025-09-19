package com.certification.crawler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 爬虫数据过滤器
 * 用于根据关键词判断爬虫获取的信息是否与关键词相关
 */
@Slf4j
@Component
public class FliterByKeyword {
    
    @Autowired
    private KeywordsConfig keywordsConfig;
    
    // 关键词匹配模式枚举
    public enum MatchMode {
        EXACT,      // 精确匹配
        CONTAINS,   // 包含匹配
        REGEX,      // 正则表达式匹配
        FUZZY       // 模糊匹配
    }
    
    // 匹配策略枚举
    public enum MatchStrategy {
        ANY,        // 任一关键词匹配即可
        ALL,        // 所有关键词都必须匹配
        MAJORITY    // 多数关键词匹配
    }
    
    /**
     * 检查文本是否包含关键词（默认使用包含匹配模式）
     * 
     * @param text 待检查的文本
     * @param keywords 关键词列表
     * @return 是否匹配
     */
    public boolean isRelevant(String text, List<String> keywords) {
        return isRelevant(text, keywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含关键词（指定匹配模式）
     * 
     * @param text 待检查的文本
     * @param keywords 关键词列表
     * @param matchMode 匹配模式
     * @return 是否匹配
     */
    public boolean isRelevant(String text, List<String> keywords, MatchMode matchMode) {
        return isRelevant(text, keywords, matchMode, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含关键词（完整参数版本）
     * 
     * @param text 待检查的文本
     * @param keywords 关键词列表
     * @param matchMode 匹配模式
     * @param matchStrategy 匹配策略
     * @return 是否匹配
     */
    public boolean isRelevant(String text, List<String> keywords, MatchMode matchMode, MatchStrategy matchStrategy) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("待检查文本为空");
            return false;
        }
        
        if (keywords == null || keywords.isEmpty()) {
            log.warn("关键词列表为空");
            return false;
        }
        
        // 预处理文本和关键词
        String processedText = preprocessText(text);
        List<String> processedKeywords = preprocessKeywords(keywords);
        
        log.debug("检查文本相关性: text={}, keywords={}, mode={}, strategy={}", 
                text.substring(0, Math.min(100, text.length())), keywords, matchMode, matchStrategy);
        
        // 根据匹配策略执行匹配
        switch (matchStrategy) {
            case ANY:
                return matchAny(processedText, processedKeywords, matchMode);
            case ALL:
                return matchAll(processedText, processedKeywords, matchMode);
            case MAJORITY:
                return matchMajority(processedText, processedKeywords, matchMode);
            default:
                return false;
        }
    }
    
    /**
     * 任一关键词匹配即可
     */
    private boolean matchAny(String text, List<String> keywords, MatchMode matchMode) {
        for (String keyword : keywords) {
            if (matches(text, keyword, matchMode)) {
                log.debug("关键词匹配成功: {}", keyword);
                return true;
            }
        }
        return false;
    }
    
    /**
     * 所有关键词都必须匹配
     */
    private boolean matchAll(String text, List<String> keywords, MatchMode matchMode) {
        for (String keyword : keywords) {
            if (!matches(text, keyword, matchMode)) {
                log.debug("关键词匹配失败: {}", keyword);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 多数关键词匹配（超过50%）
     */
    private boolean matchMajority(String text, List<String> keywords, MatchMode matchMode) {
        int matchCount = 0;
        for (String keyword : keywords) {
            if (matches(text, keyword, matchMode)) {
                matchCount++;
            }
        }
        boolean result = matchCount > keywords.size() / 2;
        log.debug("多数匹配结果: {}/{} 匹配", matchCount, keywords.size());
        return result;
    }
    
    /**
     * 执行具体的匹配逻辑
     */
    private boolean matches(String text, String keyword, MatchMode matchMode) {
        switch (matchMode) {
            case EXACT:
                return text.equals(keyword);
            case CONTAINS:
                return text.contains(keyword);
            case REGEX:
                try {
                    Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(text);
                    return matcher.find();
                } catch (Exception e) {
                    log.warn("正则表达式匹配失败: {}", keyword, e);
                    return false;
                }
            case FUZZY:
                return fuzzyMatch(text, keyword);
            default:
                return false;
        }
    }
    
    /**
     * 模糊匹配（使用编辑距离算法）
     */
    private boolean fuzzyMatch(String text, String keyword) {
        if (keyword.length() > text.length()) {
            return false;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(text.toLowerCase(), keyword.toLowerCase());
        
        // 设置相似度阈值（可以根据需要调整）
        double similarity = 1.0 - (double) distance / Math.max(text.length(), keyword.length());
        double threshold = 0.7; // 70%相似度阈值
        
        log.debug("模糊匹配: text={}, keyword={}, similarity={}", 
                text.substring(0, Math.min(50, text.length())), keyword, similarity);
        
        return similarity >= threshold;
    }
    
    /**
     * 计算编辑距离（Levenshtein距离）
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + 1, 
                                      Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 预处理文本
     */
    private String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        
        // 去除多余空白字符
        text = text.replaceAll("\\s+", " ").trim();
        
        // 转换为小写（可选，根据业务需求决定）
        // text = text.toLowerCase();
        
        return text;
    }
    
    /**
     * 预处理关键词
     */
    private List<String> preprocessKeywords(List<String> keywords) {
        List<String> processed = new ArrayList<>();
        
        for (String keyword : keywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String processedKeyword = keyword.trim();
                // 转换为小写（可选，根据业务需求决定）
                // processedKeyword = processedKeyword.toLowerCase();
                processed.add(processedKeyword);
            }
        }
        
        return processed;
    }
    
    /**
     * 检查文本是否包含认证相关关键词
     */
    public boolean isCertificationRelated(String text) {
        List<String> certificationKeywords = keywordsConfig.getCertificationKeywords();
        return isRelevant(text, certificationKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含产品召回相关关键词
     */
    public boolean isRecallRelated(String text) {
        List<String> recallKeywords = keywordsConfig.getRecallKeywords();
        return isRelevant(text, recallKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含法规通知相关关键词
     */
    public boolean isRegulationRelated(String text) {
        List<String> regulationKeywords = keywordsConfig.getRegulationKeywords();
        return isRelevant(text, regulationKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含标准相关关键词
     */
    public boolean isStandardRelated(String text) {
        List<String> standardKeywords = keywordsConfig.getStandardKeywords();
        return isRelevant(text, standardKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含安全相关关键词
     */
    public boolean isSafetyRelated(String text) {
        List<String> safetyKeywords = keywordsConfig.getSafetyKeywords();
        return isRelevant(text, safetyKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含环保相关关键词
     */
    public boolean isEnvironmentalRelated(String text) {
        List<String> environmentalKeywords = keywordsConfig.getEnvironmentalKeywords();
        return isRelevant(text, environmentalKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含产品相关关键词
     */
    public boolean isProductRelated(String text) {
        List<String> productKeywords = keywordsConfig.getProductKeywords();
        return isRelevant(text, productKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含行业相关关键词
     */
    public boolean isIndustryRelated(String text) {
        List<String> industryKeywords = keywordsConfig.getIndustryKeywords();
        return isRelevant(text, industryKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含HS编码相关关键词
     */
    public boolean isHsCodeRelated(String text) {
        List<String> hsCodeKeywords = keywordsConfig.getHsCodeKeywords();
        return isRelevant(text, hsCodeKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含竞品相关关键词
     */
    public boolean isCompetitorRelated(String text) {
        List<String> competitorKeywords = keywordsConfig.getCompetitorKeywords();
        return isRelevant(text, competitorKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 检查文本是否包含产品功能相关关键词
     */
    public boolean isProductFunctionRelated(String text) {
        List<String> productFunctionKeywords = keywordsConfig.getProductFunctionKeywords();
        return isRelevant(text, productFunctionKeywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 根据类别检查文本相关性
     */
    public boolean isRelevantByCategory(String text, String category) {
        List<String> keywords = keywordsConfig.getKeywordsByCategory(category);
        return isRelevant(text, keywords, MatchMode.CONTAINS, MatchStrategy.ANY);
    }
    
    /**
     * 获取文本匹配的所有类别
     */
    public List<String> getMatchedCategories(String text) {
        List<String> matchedCategories = new ArrayList<>();
        List<String> categories = keywordsConfig.getKeywordCategories();
        
        for (String category : categories) {
            if (!"all".equals(category) && isRelevantByCategory(text, category)) {
                matchedCategories.add(category);
            }
        }
        
        return matchedCategories;
    }
    
    /**
     * 获取匹配的关键词列表
     */
    public List<String> getMatchedKeywords(String text, List<String> keywords, MatchMode matchMode) {
        List<String> matchedKeywords = new ArrayList<>();
        
        if (text == null || keywords == null) {
            return matchedKeywords;
        }
        
        String processedText = preprocessText(text);
        List<String> processedKeywords = preprocessKeywords(keywords);
        
        for (String keyword : processedKeywords) {
            if (matches(processedText, keyword, matchMode)) {
                matchedKeywords.add(keyword);
            }
        }
        
        return matchedKeywords;
    }
    
    /**
     * 计算文本与关键词的匹配度（0.0-1.0）
     */
    public double calculateRelevanceScore(String text, List<String> keywords, MatchMode matchMode) {
        if (text == null || keywords == null || keywords.isEmpty()) {
            return 0.0;
        }
        
        String processedText = preprocessText(text);
        List<String> processedKeywords = preprocessKeywords(keywords);
        
        int matchCount = 0;
        for (String keyword : processedKeywords) {
            if (matches(processedText, keyword, matchMode)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / processedKeywords.size();
    }
    
    /**
     * 获取关键词统计信息
     */
    public Map<String, Integer> getKeywordStatistics() {
        return keywordsConfig.getKeywordStatistics();
    }
    
    /**
     * 搜索关键词
     */
    public List<String> searchKeywords(String searchTerm) {
        return keywordsConfig.searchKeywords(searchTerm);
    }
}
