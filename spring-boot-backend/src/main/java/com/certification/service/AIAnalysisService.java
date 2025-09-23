package com.certification.service;

import com.certification.entity.common.CertNewsData;
import com.certification.repository.CrawlerDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIAnalysisService {

    @Autowired
    private CrawlerDataRepository crawlerDataRepository;

    /**
     * 分析单条数据
     */
    public Map<String, Object> analyzeSingleData(CertNewsData data, String analysisType) {
        log.info("AI分析单条数据: {} - 类型: {}", data.getId(), analysisType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("dataId", data.getId());
        result.put("title", data.getTitle());
        result.put("analysisType", analysisType);
        result.put("analysisTime", LocalDateTime.now());
        
        switch (analysisType) {
            case "relevance":
                result.putAll(analyzeRelevance(data));
                break;
            case "summary":
                result.putAll(analyzeSummary(data));
                break;
            case "classification":
                result.putAll(analyzeClassification(data));
                break;
            case "sentiment":
                result.putAll(analyzeSentiment(data));
                break;
            case "keywords":
                result.putAll(analyzeKeywords(data));
                break;
            default:
                result.put("error", "不支持的分析类型: " + analysisType);
        }
        
        return result;
    }

    /**
     * 批量分析数据
     */
    public Map<String, Object> analyzeBatchData(List<String> dataIds, String analysisType) {
        log.info("AI批量分析数据: {} 条 - 类型: {}", dataIds.size(), analysisType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("analysisType", analysisType);
        result.put("totalCount", dataIds.size());
        result.put("analysisTime", LocalDateTime.now());
        
        List<Map<String, Object>> analysisResults = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        for (String dataId : dataIds) {
            try {
                CertNewsData data = crawlerDataRepository.findById(dataId).orElse(null);
                if (data != null) {
                    Map<String, Object> singleResult = analyzeSingleData(data, analysisType);
                    analysisResults.add(singleResult);
                    successCount++;
                } else {
                    errorCount++;
                    log.warn("数据不存在: {}", dataId);
                }
            } catch (Exception e) {
                errorCount++;
                log.error("分析数据失败: {} - {}", dataId, e.getMessage());
            }
        }
        
        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("analysisResults", analysisResults);
        
        // 添加汇总分析
        if (successCount > 0) {
            result.putAll(generateBatchSummary(analysisResults, analysisType));
        }
        
        return result;
    }

    /**
     * 分析筛选数据
     */
    public Map<String, Object> analyzeFilteredData(String analysisType, Integer maxCount) {
        log.info("AI分析筛选数据 - 类型: {} - 最大数量: {}", analysisType, maxCount);
        
        // 获取当前筛选条件下的数据
        List<CertNewsData> filteredData = crawlerDataRepository.findAll();
        if (filteredData.size() > maxCount) {
            filteredData = filteredData.subList(0, maxCount);
        }
        
        List<String> dataIds = filteredData.stream()
                .map(CertNewsData::getId)
                .collect(Collectors.toList());
        
        return analyzeBatchData(dataIds, analysisType);
    }

    /**
     * 获取可用的分析类型
     */
    public List<Map<String, Object>> getAvailableAnalysisTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        
        Map<String, Object> relevance = new HashMap<>();
        relevance.put("type", "relevance");
        relevance.put("name", "相关性分析");
        relevance.put("description", "分析数据与认证标准的关联程度");
        types.add(relevance);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("type", "summary");
        summary.put("name", "内容摘要");
        summary.put("description", "生成数据的核心内容摘要");
        types.add(summary);
        
        Map<String, Object> classification = new HashMap<>();
        classification.put("type", "classification");
        classification.put("name", "分类分析");
        classification.put("description", "对数据进行分类和标签化");
        types.add(classification);
        
        Map<String, Object> sentiment = new HashMap<>();
        sentiment.put("type", "sentiment");
        sentiment.put("name", "情感分析");
        sentiment.put("description", "分析数据的情感倾向");
        types.add(sentiment);
        
        Map<String, Object> keywords = new HashMap<>();
        keywords.put("type", "keywords");
        keywords.put("name", "关键词提取");
        keywords.put("description", "提取数据中的关键信息");
        types.add(keywords);
        
        return types;
    }

    /**
     * 相关性分析
     */
    private Map<String, Object> analyzeRelevance(CertNewsData data) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟AI相关性分析
        String content = (data.getTitle() + " " + data.getContent()).toLowerCase();
        
        // 定义认证相关关键词
        List<String> certificationKeywords = Arrays.asList(
            "certification", "compliance", "standard", "regulation", "approval",
            "fcc", "ce", "rohs", "iso", "iec", "en", "gb", "srrc", "kc",
            "认证", "合规", "标准", "法规", "核准", "型号核准", "无线电"
        );
        
        int matchCount = 0;
        List<String> matchedKeywords = new ArrayList<>();
        
        for (String keyword : certificationKeywords) {
            if (content.contains(keyword.toLowerCase())) {
                matchCount++;
                matchedKeywords.add(keyword);
            }
        }
        
        double relevanceScore = Math.min(1.0, matchCount / 10.0); // 归一化到0-1
        String relevanceLevel = getRelevanceLevel(relevanceScore);
        
        result.put("relevanceScore", relevanceScore);
        result.put("relevanceLevel", relevanceLevel);
        result.put("matchedKeywords", matchedKeywords);
        result.put("matchCount", matchCount);
        result.put("analysis", generateRelevanceAnalysis(relevanceScore, matchedKeywords));
        
        return result;
    }

    /**
     * 内容摘要
     */
    private Map<String, Object> analyzeSummary(CertNewsData data) {
        Map<String, Object> result = new HashMap<>();
        
        String content = data.getContent();
        if (content == null || content.trim().isEmpty()) {
            content = data.getTitle();
        }
        
        // 模拟AI摘要生成
        String summary = generateSummary(content);
        
        result.put("summary", summary);
        result.put("summaryLength", summary.length());
        result.put("originalLength", content.length());
        result.put("compressionRatio", (double) summary.length() / content.length());
        
        return result;
    }

    /**
     * 分类分析
     */
    private Map<String, Object> analyzeClassification(CertNewsData data) {
        Map<String, Object> result = new HashMap<>();
        
        String content = (data.getTitle() + " " + data.getContent()).toLowerCase();
        
        // 定义分类规则
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("认证标准", Arrays.asList("fcc", "ce", "rohs", "iso", "iec", "en", "gb"));
        categories.put("监管机构", Arrays.asList("srrc", "kc", "fcc", "ce", "nb", "notified body"));
        categories.put("技术标准", Arrays.asList("wifi", "bluetooth", "5g", "4g", "lte", "gsm"));
        categories.put("安全认证", Arrays.asList("safety", "electrical", "fire", "safety standard"));
        categories.put("环保认证", Arrays.asList("rohs", "reach", "环保", "有害物质"));
        
        Map<String, Double> categoryScores = new HashMap<>();
        List<String> detectedCategories = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            String category = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int matchCount = 0;
            for (String keyword : keywords) {
                if (content.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            
            double score = (double) matchCount / keywords.size();
            categoryScores.put(category, score);
            
            if (score > 0.3) { // 阈值
                detectedCategories.add(category);
            }
        }
        
        result.put("detectedCategories", detectedCategories);
        result.put("categoryScores", categoryScores);
        result.put("primaryCategory", detectedCategories.isEmpty() ? "其他" : detectedCategories.get(0));
        
        return result;
    }

    /**
     * 情感分析
     */
    private Map<String, Object> analyzeSentiment(CertNewsData data) {
        Map<String, Object> result = new HashMap<>();
        
        String content = (data.getTitle() + " " + data.getContent()).toLowerCase();
        
        // 定义情感关键词
        List<String> positiveWords = Arrays.asList("批准", "通过", "成功", "合规", "符合", "有效", "认可");
        List<String> negativeWords = Arrays.asList("拒绝", "失败", "违规", "不符合", "无效", "撤销", "处罚");
        List<String> neutralWords = Arrays.asList("发布", "更新", "修订", "通知", "公告", "标准");
        
        int positiveCount = countKeywords(content, positiveWords);
        int negativeCount = countKeywords(content, negativeWords);
        int neutralCount = countKeywords(content, neutralWords);
        
        String sentiment = "中性";
        double sentimentScore = 0.0;
        
        if (positiveCount > negativeCount) {
            sentiment = "积极";
            sentimentScore = 0.5 + (positiveCount - negativeCount) * 0.1;
        } else if (negativeCount > positiveCount) {
            sentiment = "消极";
            sentimentScore = 0.5 - (negativeCount - positiveCount) * 0.1;
        }
        
        sentimentScore = Math.max(-1.0, Math.min(1.0, sentimentScore));
        
        result.put("sentiment", sentiment);
        result.put("sentimentScore", sentimentScore);
        result.put("positiveCount", positiveCount);
        result.put("negativeCount", negativeCount);
        result.put("neutralCount", neutralCount);
        
        return result;
    }

    /**
     * 关键词提取
     */
    private Map<String, Object> analyzeKeywords(CertNewsData data) {
        Map<String, Object> result = new HashMap<>();
        
        String content = (data.getTitle() + " " + data.getContent()).toLowerCase();
        
        // 模拟关键词提取
        List<String> extractedKeywords = extractKeywords(content);
        
        result.put("extractedKeywords", extractedKeywords);
        result.put("keywordCount", extractedKeywords.size());
        result.put("keywordFrequency", calculateKeywordFrequency(content, extractedKeywords));
        
        return result;
    }

    /**
     * 生成批量分析摘要
     */
    private Map<String, Object> generateBatchSummary(List<Map<String, Object>> results, String analysisType) {
        Map<String, Object> summary = new HashMap<>();
        
        switch (analysisType) {
            case "relevance":
                summary.putAll(generateRelevanceSummary(results));
                break;
            case "classification":
                summary.putAll(generateClassificationSummary(results));
                break;
            case "sentiment":
                summary.putAll(generateSentimentSummary(results));
                break;
        }
        
        return summary;
    }

    // 辅助方法
    private String getRelevanceLevel(double score) {
        if (score >= 0.8) return "高度相关";
        if (score >= 0.6) return "中度相关";
        if (score >= 0.4) return "低度相关";
        return "不相关";
    }

    private String generateRelevanceAnalysis(double score, List<String> keywords) {
        if (score >= 0.8) {
            return "该数据与认证标准高度相关，包含多个关键认证要素：" + String.join("、", keywords);
        } else if (score >= 0.6) {
            return "该数据与认证标准中度相关，涉及部分认证要求：" + String.join("、", keywords);
        } else if (score >= 0.4) {
            return "该数据与认证标准低度相关，可能涉及边缘认证信息：" + String.join("、", keywords);
        } else {
            return "该数据与认证标准关联度较低，建议进一步评估";
        }
    }

    private String generateSummary(String content) {
        // 简单的摘要生成逻辑
        if (content.length() <= 200) {
            return content;
        }
        
        // 提取前200个字符作为摘要
        String summary = content.substring(0, 200);
        if (summary.endsWith("。") || summary.endsWith(".")) {
            return summary;
        } else {
            // 找到最后一个完整句子
            int lastPeriod = summary.lastIndexOf("。");
            int lastDot = summary.lastIndexOf(".");
            int lastBreak = Math.max(lastPeriod, lastDot);
            
            if (lastBreak > 0) {
                return summary.substring(0, lastBreak + 1);
            } else {
                return summary + "...";
            }
        }
    }

    private int countKeywords(String content, List<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    private List<String> extractKeywords(String content) {
        // 简单的关键词提取逻辑
        List<String> keywords = new ArrayList<>();
        String[] words = content.split("\\s+");
        
        for (String word : words) {
            if (word.length() > 2 && !isCommonWord(word)) {
                keywords.add(word);
            }
        }
        
        // 去重并限制数量
        return keywords.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean isCommonWord(String word) {
        List<String> commonWords = Arrays.asList("的", "是", "在", "有", "和", "与", "或", "但", "而", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with");
        return commonWords.contains(word.toLowerCase());
    }

    private Map<String, Integer> calculateKeywordFrequency(String content, List<String> keywords) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String keyword : keywords) {
            int count = 0;
            int index = 0;
            while ((index = content.indexOf(keyword.toLowerCase(), index)) != -1) {
                count++;
                index += keyword.length();
            }
            frequency.put(keyword, count);
        }
        return frequency;
    }

    private Map<String, Object> generateRelevanceSummary(List<Map<String, Object>> results) {
        Map<String, Object> summary = new HashMap<>();
        
        double avgScore = results.stream()
                .mapToDouble(r -> (Double) r.get("relevanceScore"))
                .average()
                .orElse(0.0);
        
        long highRelevanceCount = results.stream()
                .filter(r -> "高度相关".equals(r.get("relevanceLevel")))
                .count();
        
        summary.put("averageRelevanceScore", avgScore);
        summary.put("highRelevanceCount", highRelevanceCount);
        summary.put("highRelevancePercentage", (double) highRelevanceCount / results.size());
        
        return summary;
    }

    private Map<String, Object> generateClassificationSummary(List<Map<String, Object>> results) {
        Map<String, Object> summary = new HashMap<>();
        
        Map<String, Long> categoryCounts = new HashMap<>();
        for (Map<String, Object> result : results) {
            @SuppressWarnings("unchecked")
            List<String> categories = (List<String>) result.get("detectedCategories");
            for (String category : categories) {
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0L) + 1);
            }
        }
        
        summary.put("categoryDistribution", categoryCounts);
        summary.put("mostCommonCategory", categoryCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("其他"));
        
        return summary;
    }

    private Map<String, Object> generateSentimentSummary(List<Map<String, Object>> results) {
        Map<String, Object> summary = new HashMap<>();
        
        Map<String, Long> sentimentCounts = results.stream()
                .collect(Collectors.groupingBy(
                    r -> (String) r.get("sentiment"),
                    Collectors.counting()
                ));
        
        double avgSentimentScore = results.stream()
                .mapToDouble(r -> (Double) r.get("sentimentScore"))
                .average()
                .orElse(0.0);
        
        summary.put("sentimentDistribution", sentimentCounts);
        summary.put("averageSentimentScore", avgSentimentScore);
        summary.put("overallSentiment", avgSentimentScore > 0.1 ? "积极" : avgSentimentScore < -0.1 ? "消极" : "中性");
        
        return summary;
    }
}

