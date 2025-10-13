package com.certification.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI判断结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIJudgeResult {
    
    /**
     * 是否与测肤仪相关
     */
    private boolean isRelated;
    
    /**
     * 置信度 (0.0-1.0)
     */
    private double confidence;
    
    /**
     * 判断理由
     */
    private String reason;
    
    /**
     * 分类类别（如："皮肤成像系统", "面部分析仪", "其他"）
     */
    private String category;
    
    /**
     * 提取的黑名单关键词（如制造商名称）
     */
    private List<String> blacklistKeywords = new ArrayList<>();
    
    /**
     * 设备名称（用于展示）
     */
    private String deviceName;
    
    /**
     * 创建一个"相关"的判断结果
     */
    public static AIJudgeResult related(String reason, double confidence, String category, String deviceName) {
        AIJudgeResult result = new AIJudgeResult();
        result.setRelated(true);
        result.setReason(reason);
        result.setConfidence(confidence);
        result.setCategory(category);
        result.setDeviceName(deviceName);
        return result;
    }
    
    /**
     * 创建一个"不相关"的判断结果
     */
    public static AIJudgeResult notRelated(String reason, double confidence, List<String> blacklistKeywords, String deviceName) {
        AIJudgeResult result = new AIJudgeResult();
        result.setRelated(false);
        result.setReason(reason);
        result.setConfidence(confidence);
        result.setBlacklistKeywords(blacklistKeywords != null ? blacklistKeywords : new ArrayList<>());
        result.setDeviceName(deviceName);
        return result;
    }
    
    /**
     * 创建一个失败的判断结果
     */
    public static AIJudgeResult failed(String reason) {
        AIJudgeResult result = new AIJudgeResult();
        result.setRelated(false);
        result.setReason(reason);
        result.setConfidence(0.0);
        return result;
    }
}

