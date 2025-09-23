package com.certification.service;

import com.certification.entity.common.CertNewsData;
import com.certification.standards.CrawlerDataService;
import com.certification.standards.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 数据处理服务
 * 负责自动更新国家信息和处理数据
 */
@Slf4j
@Service
@Transactional
public class DataProcessingService {

    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private KeywordService keywordService;

    /**
     * 自动更新国家信息
     */
    public void autoUpdateCountries(List<CertNewsData> dataList) {
        log.info("开始自动更新国家信息，数据量: {}", dataList.size());
        
        int updatedCount = 0;
        for (CertNewsData data : dataList) {
            try {
                String detectedCountry = detectCountryFromContent(data);
                if (detectedCountry != null && !detectedCountry.equals(data.getCountry())) {
                    data.setCountry(detectedCountry);
                    crawlerDataService.saveCrawlerData(data);
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("更新数据国家信息失败，ID: {}, 错误: {}", data.getId(), e.getMessage());
            }
        }
        
        log.info("自动更新国家信息完成，更新数量: {}", updatedCount);
    }

    /**
     * 自动处理数据
     */
    public void autoProcessData(List<CertNewsData> dataList) {
        log.info("开始自动处理数据，数据量: {}", dataList.size());
        
        int processedCount = 0;
        for (CertNewsData data : dataList) {
            try {
                // 关键词匹配
                String matchedKeywords = matchKeywords(data);
                if (matchedKeywords != null && !matchedKeywords.isEmpty()) {
                    data.setMatchedKeywords(matchedKeywords);
                    data.setRelated(true);
                } else {
                    data.setRelated(false);
                }
                
                // 风险等级评估
                CertNewsData.RiskLevel riskLevel = assessRiskLevel(data);
                data.setRiskLevel(riskLevel);
                
                // 标记为已处理
                data.setIsProcessed(true);
                data.setProcessedTime(LocalDateTime.now());
                
                crawlerDataService.saveCrawlerData(data);
                processedCount++;
                
            } catch (Exception e) {
                log.error("处理数据失败，ID: {}, 错误: {}", data.getId(), e.getMessage());
            }
        }
        
        log.info("自动处理数据完成，处理数量: {}", processedCount);
    }

    /**
     * 从内容中检测国家
     */
    private String detectCountryFromContent(CertNewsData data) {
        String content = (data.getContent() != null ? data.getContent() : "") + 
                        (data.getTitle() != null ? data.getTitle() : "") + 
                        (data.getSummary() != null ? data.getSummary() : "");
        
        if (content.isEmpty()) {
            return null;
        }
        
        // 国家关键词映射
        Map<String, String> countryKeywords = new HashMap<>();
        countryKeywords.put("中国|China|CN|中华人民共和国", "CN");
        countryKeywords.put("美国|United States|USA|US|America", "US");
        countryKeywords.put("欧盟|European Union|EU|Europe", "EU");
        countryKeywords.put("日本|Japan|JP", "JP");
        countryKeywords.put("韩国|South Korea|Korea|KR", "KR");
        countryKeywords.put("印度|India|IN", "IN");
        countryKeywords.put("加拿大|Canada|CA", "CA");
        countryKeywords.put("澳大利亚|Australia|AU", "AU");
        countryKeywords.put("英国|United Kingdom|UK|Britain", "UK");
        countryKeywords.put("德国|Germany|DE", "DE");
        countryKeywords.put("法国|France|FR", "FR");
        countryKeywords.put("意大利|Italy|IT", "IT");
        countryKeywords.put("西班牙|Spain|ES", "ES");
        countryKeywords.put("俄罗斯|Russia|RU", "RU");
        countryKeywords.put("巴西|Brazil|BR", "BR");
        countryKeywords.put("墨西哥|Mexico|MX", "MX");
        countryKeywords.put("新加坡|Singapore|SG", "SG");
        countryKeywords.put("马来西亚|Malaysia|MY", "MY");
        countryKeywords.put("泰国|Thailand|TH", "TH");
        countryKeywords.put("越南|Vietnam|VN", "VN");
        countryKeywords.put("菲律宾|Philippines|PH", "PH");
        countryKeywords.put("印度尼西亚|Indonesia|ID", "ID");
        
        // 检查每个国家的关键词
        for (Map.Entry<String, String> entry : countryKeywords.entrySet()) {
            String keywords = entry.getKey();
            String countryCode = entry.getValue();
            
            String[] keywordArray = keywords.split("\\|");
            for (String keyword : keywordArray) {
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    return countryCode;
                }
            }
        }
        
        return null;
    }

    /**
     * 匹配关键词
     */
    private String matchKeywords(CertNewsData data) {
        try {
            String content = (data.getContent() != null ? data.getContent() : "") + 
                            (data.getTitle() != null ? data.getTitle() : "") + 
                            (data.getSummary() != null ? data.getSummary() : "");
            
            if (content.isEmpty()) {
                return null;
            }
            
            // 获取所有关键词
            List<String> allKeywords = keywordService.getAllKeywords().stream()
                    .map(keyword -> keyword.getKeyword())
                    .toList();
            StringBuilder matchedKeywords = new StringBuilder();
            
            for (String keyword : allKeywords) {
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    if (matchedKeywords.length() > 0) {
                        matchedKeywords.append(",");
                    }
                    matchedKeywords.append(keyword);
                }
            }
            
            return matchedKeywords.length() > 0 ? matchedKeywords.toString() : null;
            
        } catch (Exception e) {
            log.error("匹配关键词失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 评估风险等级
     */
    private CertNewsData.RiskLevel assessRiskLevel(CertNewsData data) {
        String content = (data.getContent() != null ? data.getContent() : "") + 
                        (data.getTitle() != null ? data.getTitle() : "") + 
                        (data.getSummary() != null ? data.getSummary() : "");
        
        if (content.isEmpty()) {
            return CertNewsData.RiskLevel.NONE;
        }
        
        content = content.toLowerCase();
        
        // 高风险关键词
        String[] highRiskKeywords = {
            "recall", "召回", "safety", "安全", "hazard", "危险", "injury", "伤害",
            "death", "死亡", "fire", "火灾", "explosion", "爆炸", "toxic", "有毒",
            "contamination", "污染", "defect", "缺陷", "malfunction", "故障"
        };
        
        // 中风险关键词
        String[] mediumRiskKeywords = {
            "warning", "警告", "caution", "注意", "risk", "风险", "issue", "问题",
            "problem", "问题", "concern", "关注", "alert", "警报", "notice", "通知"
        };
        
        // 检查高风险关键词
        for (String keyword : highRiskKeywords) {
            if (content.contains(keyword)) {
                return CertNewsData.RiskLevel.HIGH;
            }
        }
        
        // 检查中风险关键词
        for (String keyword : mediumRiskKeywords) {
            if (content.contains(keyword)) {
                return CertNewsData.RiskLevel.MEDIUM;
            }
        }
        
        // 如果有匹配的关键词，至少是低风险
        if (data.getMatchedKeywords() != null && !data.getMatchedKeywords().isEmpty()) {
            return CertNewsData.RiskLevel.LOW;
        }
        
        return CertNewsData.RiskLevel.NONE;
    }
}
