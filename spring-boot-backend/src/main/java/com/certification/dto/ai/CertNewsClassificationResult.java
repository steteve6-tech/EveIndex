package com.certification.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证新闻分类结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertNewsClassificationResult {
    private boolean relatedToCertification;     // 是否与认证标准相关
    private double confidence;                  // 置信度 0.0-1.0
    private String reason;                      // 判断原因
    private List<String> extractedKeywords = new ArrayList<>();  // 提取的认证关键词
    
    public CertNewsClassificationResult(boolean relatedToCertification, double confidence, String reason) {
        this.relatedToCertification = relatedToCertification;
        this.confidence = confidence;
        this.reason = reason;
        this.extractedKeywords = new ArrayList<>();
    }
    
    public void addExtractedKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !extractedKeywords.contains(keyword.trim())) {
            extractedKeywords.add(keyword.trim());
        }
    }
}

