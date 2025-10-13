package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.service.ai.AIClassificationService;
import com.certification.service.ai.AIJudgeStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI判断策略基类
 * 提供通用的AI判断逻辑和工具方法
 */
@Slf4j
public abstract class BaseAIJudgeStrategy implements AIJudgeStrategy {
    
    @Autowired
    protected AIClassificationService aiClassificationService;

    /**
     * 构建用于AI判断的设备数据Map
     * 子类需要实现此方法来提取关键字段
     */
    protected abstract Map<String, Object> buildDeviceDataForAI(Object entity);
    
    /**
     * 提取黑名单关键词
     * 子类需要实现此方法来提取制造商等信息
     */
    protected abstract List<String> extractBlacklistKeywords(Object entity);
    
    @Override
    public AIJudgeResult judge(Object entity) {
        if (entity == null) {
            return AIJudgeResult.failed("实体对象为空");
        }
        
        try {
            // 构建AI判断需要的设备数据
            Map<String, Object> deviceData = buildDeviceDataForAI(entity);
            deviceData.put("entityType", getSupportedEntityType());
            
            // 调用AI分类服务
            var classificationResult = aiClassificationService.classifySkinDevice(deviceData);
            
            // 转换为AIJudgeResult
            AIJudgeResult judgeResult = new AIJudgeResult();
            judgeResult.setRelated(classificationResult.isRelated());
            judgeResult.setConfidence(classificationResult.getConfidence());
            judgeResult.setReason(classificationResult.getReason());
            judgeResult.setCategory(classificationResult.getCategory());
            judgeResult.setDeviceName(String.valueOf(deviceData.getOrDefault("deviceName", "")));
            
            // 如果不相关，提取黑名单关键词
            if (!classificationResult.isRelated()) {
                judgeResult.setBlacklistKeywords(extractBlacklistKeywords(entity));
            }
            
            return judgeResult;
            
        } catch (Exception e) {
            log.error("AI判断失败: entityType={}, error={}", getSupportedEntityType(), e.getMessage(), e);
            return AIJudgeResult.failed("AI判断失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateEntityWithJudgeResult(Object entity, AIJudgeResult judgeResult) {
        if (entity == null || judgeResult == null) {
            return;
        }
        
        try {
            // 更新风险等级
            RiskLevel newRiskLevel = judgeResult.isRelated() ? RiskLevel.HIGH : RiskLevel.LOW;
            setFieldValue(entity, "riskLevel", newRiskLevel);
            
            // 生成备注信息
            String remark = formatRemark(judgeResult);
            setFieldValue(entity, "remark", remark);
            
            log.debug("更新实体风险等级: entityType={}, id={}, riskLevel={}", 
                     getSupportedEntityType(), getEntityId(entity), newRiskLevel);
            
        } catch (Exception e) {
            log.error("更新实体失败: entityType={}, error={}", getSupportedEntityType(), e.getMessage(), e);
        }
    }
    
    /**
     * 格式化备注信息
     */
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();
        
        remark.append("【AI判断】\n");
        remark.append("判断结果: ").append(judgeResult.isRelated() ? "是测肤仪相关设备" : "非测肤仪相关设备").append("\n");
        remark.append("理由: ").append(judgeResult.getReason()).append("\n");
        remark.append("置信度: ").append(String.format("%.1f%%", judgeResult.getConfidence() * 100)).append("\n");
        
        if (judgeResult.getCategory() != null && !judgeResult.getCategory().isEmpty()) {
            remark.append("分类: ").append(judgeResult.getCategory()).append("\n");
        }
        
        remark.append("判断时间: ").append(LocalDateTime.now().format(formatter)).append("\n");
        remark.append("操作: ").append(judgeResult.isRelated() ? "保留高风险" : "降为低风险");
        
        return remark.toString();
    }
    
    /**
     * 清理制造商名称（移除常见后缀）
     */
    protected String cleanManufacturerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = name.trim();
        
        // 移除常见的公司后缀
        String[] suffixes = {
            " Inc.", " Inc", " LLC", " Ltd.", " Ltd", " Co.", " Co", 
            " Corporation", " Corp.", " Corp", " Limited", " L.L.C.", " L.L.C",
            "有限公司", "股份有限公司", "集团", "公司"
        };
        
        for (String suffix : suffixes) {
            if (cleaned.endsWith(suffix)) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length()).trim();
            }
        }
        
        // 如果清理后为空或太短，返回null
        if (cleaned.length() < 3) {
            return null;
        }
        
        return cleaned;
    }
    
    /**
     * 检查是否为知名测肤仪品牌（不应加入黑名单）
     */
    protected boolean isKnownSkinDeviceBrand(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        String nameLower = name.toLowerCase();
        String[] knownBrands = {
            "visia", "canfield", "observ", "dermaflash", "neutrogena",
            "dermalogica", "janus", "callegari", "aimyskin", "skin"
        };
        
        for (String brand : knownBrands) {
            if (nameLower.contains(brand)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 使用反射设置字段值
     */
    protected void setFieldValue(Object entity, String fieldName, Object value) {
        try {
            String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method method = entity.getClass().getMethod(methodName, value.getClass());
            method.invoke(entity, value);
        } catch (Exception e) {
            log.warn("设置字段失败: field={}, error={}", fieldName, e.getMessage());
        }
    }
    
    /**
     * 获取实体ID
     */
    protected Long getEntityId(Object entity) {
        try {
            Method method = entity.getClass().getMethod("getId");
            return (Long) method.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 安全获取字符串字段值
     */
    protected String getStringField(Object entity, String fieldName) {
        try {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method method = entity.getClass().getMethod(methodName);
            Object value = method.invoke(entity);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
}

