package com.certification.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关键词工具类
 * 用于处理关键词的提取、匹配和存储
 */
public class KeywordUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 从文本中提取关键词
     * 基于预定义的关键词列表进行匹配
     */
    public static List<String> extractKeywordsFromText(String text, List<String> predefinedKeywords) {
        if (text == null || text.trim().isEmpty() || predefinedKeywords == null || predefinedKeywords.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerText = text.toLowerCase();
        List<String> matchedKeywords = new ArrayList<>();
        
        for (String keyword : predefinedKeywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.trim().toLowerCase();
                if (lowerText.contains(lowerKeyword)) {
                    matchedKeywords.add(keyword.trim());
                }
            }
        }
        
        return matchedKeywords;
    }
    
    /**
     * 从设备名称中提取关键词
     */
    public static List<String> extractKeywordsFromDeviceName(String deviceName, List<String> predefinedKeywords) {
        if (deviceName == null || deviceName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 设备名称关键词权重更高
        List<String> deviceKeywords = extractKeywordsFromText(deviceName, predefinedKeywords);
        
        // 如果设备名称包含高风险词汇，添加额外关键词
        if (deviceName.toLowerCase().contains("monitor") || deviceName.toLowerCase().contains("analyzer")) {
            deviceKeywords.add("monitor");
        }
        if (deviceName.toLowerCase().contains("scanner") || deviceName.toLowerCase().contains("imaging")) {
            deviceKeywords.add("imaging");
        }
        
        return deviceKeywords;
    }
    
    /**
     * 从产品描述中提取关键词
     */
    public static List<String> extractKeywordsFromProductDescription(String productDescription, List<String> predefinedKeywords) {
        if (productDescription == null || productDescription.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> descriptionKeywords = extractKeywordsFromText(productDescription, predefinedKeywords);
        
        // 添加技术相关关键词
        if (productDescription.toLowerCase().contains("3d") || productDescription.toLowerCase().contains("three dimensional")) {
            descriptionKeywords.add("3D");
        }
        if (productDescription.toLowerCase().contains("ai") || productDescription.toLowerCase().contains("artificial intelligence")) {
            descriptionKeywords.add("AI");
        }
        if (productDescription.toLowerCase().contains("portable") || productDescription.toLowerCase().contains("mobile")) {
            descriptionKeywords.add("Portable");
        }
        
        return descriptionKeywords;
    }
    
    /**
     * 从公司名称中提取关键词
     */
    public static List<String> extractKeywordsFromCompanyName(String companyName, List<String> predefinedKeywords) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> companyKeywords = new ArrayList<>();
        
        // 检查是否包含特定公司关键词
        if (companyName.toLowerCase().contains("aimyskin")) {
            companyKeywords.add("AIMYSKIN");
        }
        if (companyName.toLowerCase().contains("facial")) {
            companyKeywords.add("Facial");
        }
        if (companyName.toLowerCase().contains("care")) {
            companyKeywords.add("Care");
        }
        
        // 添加预定义关键词匹配
        companyKeywords.addAll(extractKeywordsFromText(companyName, predefinedKeywords));
        
        return companyKeywords;
    }
    
    /**
     * 将关键词列表转换为JSON字符串
     */
    public static String keywordsToJson(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(keywords);
        } catch (JsonProcessingException e) {
            // 如果转换失败，返回简单的JSON数组
            return "[" + keywords.stream()
                .map(keyword -> "\"" + keyword.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",")) + "]";
        }
    }
    
    /**
     * 从JSON字符串解析关键词列表
     */
    public static List<String> jsonToKeywords(String jsonKeywords) {
        if (jsonKeywords == null || jsonKeywords.trim().isEmpty() || "[]".equals(jsonKeywords.trim())) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(jsonKeywords, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 如果解析失败，尝试手动解析
            return parseKeywordsManually(jsonKeywords);
        }
    }
    
    /**
     * 手动解析关键词（备用方案）
     */
    private static List<String> parseKeywordsManually(String jsonKeywords) {
        List<String> keywords = new ArrayList<>();
        
        // 简单的JSON数组解析
        String content = jsonKeywords.trim();
        if (content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
            String[] parts = content.split(",");
            
            for (String part : parts) {
                String keyword = part.trim();
                if (keyword.startsWith("\"") && keyword.endsWith("\"")) {
                    keyword = keyword.substring(1, keyword.length() - 1);
                }
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                }
            }
        }
        
        return keywords;
    }
    
    /**
     * 合并关键词列表，去重
     */
    public static List<String> mergeKeywords(List<String> keywords1, List<String> keywords2) {
        Set<String> mergedSet = new LinkedHashSet<>();
        
        if (keywords1 != null) {
            mergedSet.addAll(keywords1);
        }
        if (keywords2 != null) {
            mergedSet.addAll(keywords2);
        }
        
        return new ArrayList<>(mergedSet);
    }
    
    /**
     * 过滤无效关键词
     */
    public static List<String> filterValidKeywords(List<String> keywords) {
        if (keywords == null) {
            return new ArrayList<>();
        }
        
        return keywords.stream()
            .filter(keyword -> keyword != null && !keyword.trim().isEmpty() && keyword.trim().length() >= 2)
            .map(String::trim)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 计算关键词匹配度
     */
    public static double calculateKeywordMatchScore(List<String> textKeywords, List<String> searchKeywords) {
        if (textKeywords == null || textKeywords.isEmpty() || searchKeywords == null || searchKeywords.isEmpty()) {
            return 0.0;
        }
        
        Set<String> textSet = new HashSet<>(textKeywords);
        Set<String> searchSet = new HashSet<>(searchKeywords);
        
        // 计算交集大小
        Set<String> intersection = new HashSet<>(textSet);
        intersection.retainAll(searchSet);
        
        // 计算并集大小
        Set<String> union = new HashSet<>(textSet);
        union.addAll(searchSet);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        // 使用Jaccard相似度
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 获取关键词统计信息
     */
    public static Map<String, Integer> getKeywordStatistics(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return new HashMap<>();
        }
        
        return keywords.stream()
            .collect(Collectors.groupingBy(
                keyword -> keyword,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }
    
    /**
     * 检查是否包含高风险关键词
     */
    public static boolean containsHighRiskKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        
        List<String> highRiskKeywords = List.of(
            "death", "serious", "critical", "emergency", "recall", "withdrawal",
            "malfunction", "failure", "defect", "hazard", "danger", "toxic"
        );
        
        return keywords.stream()
            .anyMatch(keyword -> 
                highRiskKeywords.stream()
                    .anyMatch(highRisk -> 
                        keyword.toLowerCase().contains(highRisk.toLowerCase())
                    )
            );
    }
}
