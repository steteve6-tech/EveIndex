package com.certification.service.ai.strategy;

import com.certification.entity.common.CustomsCase;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 海关判例AI判断策略
 * 通过案例编号、裁定结果、HS编码判断是否为测肤仪相关
 */
@Component
public class CustomsJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "CustomsCase";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        CustomsCase customsCase = (CustomsCase) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", customsCase.getCaseNumber() != null ? customsCase.getCaseNumber() : "");
        data.put("hsCode", customsCase.getHsCodeUsed() != null ? customsCase.getHsCodeUsed() : "");
        data.put("rulingResult", customsCase.getRulingResult() != null ? customsCase.getRulingResult() : "");
        
        // deviceName使用案例编号
        data.put("deviceName", "海关案例: " + data.get("caseNumber"));
        
        // 组合描述信息
        String description = String.format("案例编号: %s, HS编码: %s, 裁定结果: %s",
            data.get("caseNumber"),
            data.get("hsCode"),
            data.get("rulingResult")
        );
        
        // 限制描述长度
        if (description.length() > 500) {
            description = description.substring(0, 500) + "...";
        }
        
        data.put("description", description);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        // 海关判例通常不提取黑名单关键词
        return new ArrayList<>();
    }
}

