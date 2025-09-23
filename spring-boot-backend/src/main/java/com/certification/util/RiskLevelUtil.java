package com.certification.util;

import com.certification.entity.common.CertNewsData.RiskLevel;
import java.util.List;

/**
 * 风险等级工具类
 * 用于计算和评估数据记录的风险等级
 */
public class RiskLevelUtil {
    
    /**
     * 根据设备类别计算风险等级
     * FDA设备分类标准：
     * Class I: 低风险
     * Class II: 中风险  
     * Class III: 高风险
     */
    public static RiskLevel calculateRiskLevelByDeviceClass(String deviceClass) {
        if (deviceClass == null || deviceClass.trim().isEmpty()) {
            return RiskLevel.MEDIUM; // 默认中风险
        }
        
        String upperClass = deviceClass.trim().toUpperCase();
        
        if (upperClass.contains("CLASS I") || upperClass.contains("I")) {
            return RiskLevel.LOW;
        } else if (upperClass.contains("CLASS II") || upperClass.contains("II")) {
            return RiskLevel.MEDIUM;
        } else if (upperClass.contains("CLASS III") || upperClass.contains("III")) {
            return RiskLevel.HIGH;
        }
        
        return RiskLevel.MEDIUM; // 默认中风险
    }
    
    /**
     * 根据召回状态计算风险等级
     */
    public static RiskLevel calculateRiskLevelByRecallStatus(String recallStatus) {
        if (recallStatus == null || recallStatus.trim().isEmpty()) {
            return RiskLevel.MEDIUM;
        }
        
        String upperStatus = recallStatus.trim().toUpperCase();
        
        if (upperStatus.contains("TERMINATED") || upperStatus.contains("COMPLETED")) {
            return RiskLevel.LOW;
        } else if (upperStatus.contains("ONGOING") || upperStatus.contains("ACTIVE")) {
            return RiskLevel.HIGH;
        }
        
        return RiskLevel.MEDIUM;
    }
    
    /**
     * 根据事件类型计算风险等级
     */
    public static RiskLevel calculateRiskLevelByEventType(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            return RiskLevel.MEDIUM;
        }
        
        String upperType = eventType.trim().toUpperCase();
        
        if (upperType.contains("DEATH") || upperType.contains("SERIOUS")) {
            return RiskLevel.HIGH;
        } else if (upperType.contains("MALFUNCTION") || upperType.contains("INJURY")) {
            return RiskLevel.MEDIUM;
        } else if (upperType.contains("OTHER") || upperType.contains("MINOR")) {
            return RiskLevel.LOW;
        }
        
        return RiskLevel.MEDIUM;
    }
    
    /**
     * 根据指导文档状态计算风险等级
     */
    public static RiskLevel calculateRiskLevelByGuidanceStatus(String guidanceStatus) {
        if (guidanceStatus == null || guidanceStatus.trim().isEmpty()) {
            return RiskLevel.MEDIUM;
        }
        
        String upperStatus = guidanceStatus.trim().toUpperCase();
        
        if (upperStatus.contains("DRAFT") || upperStatus.contains("WITHDRAWN")) {
            return RiskLevel.HIGH;
        } else if (upperStatus.contains("FINAL")) {
            return RiskLevel.LOW;
        }
        
        return RiskLevel.MEDIUM;
    }
    
    /**
     * 根据违规类型计算风险等级
     */
    public static RiskLevel calculateRiskLevelByViolationType(String violationType) {
        if (violationType == null || violationType.trim().isEmpty()) {
            return RiskLevel.MEDIUM;
        }
        
        String upperType = violationType.trim().toUpperCase();
        
        if (upperType.contains("SAFETY") || upperType.contains("CRITICAL")) {
            return RiskLevel.HIGH;
        } else if (upperType.contains("LABELING") || upperType.contains("MINOR")) {
            return RiskLevel.LOW;
        }
        
        return RiskLevel.MEDIUM;
    }
    
    /**
     * 根据关键词匹配计算风险等级
     * 高风险关键词会增加风险等级
     */
    public static RiskLevel calculateRiskLevelByKeywords(List<String> keywords, RiskLevel baseRiskLevel) {
        if (keywords == null || keywords.isEmpty()) {
            return baseRiskLevel;
        }
        
        // 高风险关键词列表
        List<String> highRiskKeywords = List.of(
            "death", "serious", "critical", "emergency", "recall", "withdrawal",
            "malfunction", "failure", "defect", "hazard", "danger", "toxic",
            "cancer", "infection", "bleeding", "stroke", "heart attack"
        );
        
        // 检查是否包含高风险关键词
        boolean hasHighRiskKeyword = keywords.stream()
            .anyMatch(keyword -> 
                highRiskKeywords.stream()
                    .anyMatch(highRisk -> 
                        keyword.toLowerCase().contains(highRisk.toLowerCase())
                    )
            );
        
        if (hasHighRiskKeyword) {
            // 提升风险等级
            return switch (baseRiskLevel) {
                case LOW -> RiskLevel.MEDIUM;
                case MEDIUM -> RiskLevel.HIGH;
                case HIGH -> RiskLevel.HIGH;
                case NONE -> RiskLevel.MEDIUM;
            };
        }
        
        return baseRiskLevel;
    }
    
    /**
     * 获取风险等级的描述信息
     */
    public static String getRiskLevelDescription(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> "高风险 - 需要重点关注和及时处理";
            case MEDIUM -> "中风险 - 需要定期监控和评估";
            case LOW -> "低风险 - 可以正常使用，定期检查";
            case NONE -> "无风险 - 正常状态，无需特别关注";
        };
    }
    
    /**
     * 获取风险等级的颜色代码（用于前端显示）
     */
    public static String getRiskLevelColor(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case HIGH -> "#ff4d4f"; // 红色
            case MEDIUM -> "#faad14"; // 橙色
            case LOW -> "#52c41a"; // 绿色
            case NONE -> "#d9d9d9"; // 灰色
        };
    }
}
