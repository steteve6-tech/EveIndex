package com.certification.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI分类结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationResult {
    private boolean isRelated;      // 是否为测肤仪相关
    private double confidence;      // 置信度 0.0-1.0
    private String reason;          // 判断理由
    private String category;        // 设备类别
    
    public ClassificationResult(boolean isRelated, double confidence, String reason) {
        this.isRelated = isRelated;
        this.confidence = confidence;
        this.reason = reason;
        this.category = "";
    }
}

