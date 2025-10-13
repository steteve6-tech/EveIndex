package com.certification.service.ai.strategy;

import com.certification.entity.common.GuidanceDocument;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 指导文档AI判断策略
 * 通过标题、描述、话题判断是否为测肤仪相关
 */
@Component
public class DocumentJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "GuidanceDocument";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        GuidanceDocument document = (GuidanceDocument) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("title", document.getTitle() != null ? document.getTitle() : "");
        data.put("topic", document.getTopic() != null ? document.getTopic() : "");
        data.put("description", document.getDescription() != null ? document.getDescription() : "");
        data.put("documentType", document.getDocumentType() != null ? document.getDocumentType() : "");
        
        // deviceName使用title
        data.put("deviceName", data.get("title"));
        
        // 组合完整描述
        String fullDescription = String.format("标题: %s, 话题: %s, 描述: %s",
            data.get("title"),
            data.get("topic"),
            data.get("description")
        );
        
        // 限制描述长度，避免AI token超限
        if (fullDescription.length() > 500) {
            fullDescription = fullDescription.substring(0, 500) + "...";
        }
        
        data.put("description", fullDescription);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        // 指导文档通常不提取黑名单关键词
        return new ArrayList<>();
    }
}

