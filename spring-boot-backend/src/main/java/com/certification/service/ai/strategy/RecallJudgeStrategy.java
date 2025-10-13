package com.certification.service.ai.strategy;

import com.certification.entity.common.DeviceRecallRecord;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 设备召回记录AI判断策略
 * 通过设备名称、召回原因、召回公司判断是否为测肤仪相关
 */
@Component
public class RecallJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "DeviceRecallRecord";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        DeviceRecallRecord record = (DeviceRecallRecord) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", record.getDeviceName() != null ? record.getDeviceName() : "");
        data.put("manufacturer", record.getRecallingFirm() != null ? record.getRecallingFirm() : "");
        data.put("productDescription", record.getProductDescription() != null ? record.getProductDescription() : "");
        data.put("recallStatus", record.getRecallStatus() != null ? record.getRecallStatus() : "");
        
        // 组合描述信息（包含召回原因）
        String description = String.format("设备名: %s, 产品描述: %s, 召回等级: %s",
            data.get("deviceName"),
            data.get("productDescription"),
            data.get("recallStatus")
        );
        data.put("description", description);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        DeviceRecallRecord record = (DeviceRecallRecord) entity;
        List<String> keywords = new ArrayList<>();
        
        // 提取召回公司名称
        String recallingFirm = record.getRecallingFirm();
        if (recallingFirm != null && !recallingFirm.trim().isEmpty()) {
            String cleaned = cleanManufacturerName(recallingFirm);
            if (cleaned != null && !isKnownSkinDeviceBrand(cleaned)) {
                keywords.add(cleaned);
            }
        }
        
        return keywords;
    }
}

