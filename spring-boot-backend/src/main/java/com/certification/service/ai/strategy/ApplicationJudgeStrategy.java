package com.certification.service.ai.strategy;

import com.certification.entity.common.Device510K;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 设备申请记录(510K)AI判断策略
 * 通过设备名称、品牌名、申请人判断是否为测肤仪相关
 */
@Component
public class ApplicationJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "Device510K";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        Device510K device = (Device510K) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", device.getDeviceName() != null ? device.getDeviceName() : "");
        data.put("manufacturer", device.getApplicant() != null ? device.getApplicant() : "");
        data.put("tradeName", device.getTradeName() != null ? device.getTradeName() : "");
        data.put("deviceClass", device.getDeviceClass() != null ? device.getDeviceClass() : "");
        
        // 组合描述信息
        String description = String.format("设备名: %s, 品牌名: %s, 类别: %s",
            data.get("deviceName"),
            data.get("tradeName"),
            data.get("deviceClass")
        );
        data.put("description", description);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        Device510K device = (Device510K) entity;
        List<String> keywords = new ArrayList<>();
        
        // 提取申请人名称
        String applicant = device.getApplicant();
        if (applicant != null && !applicant.trim().isEmpty()) {
            String cleaned = cleanManufacturerName(applicant);
            if (cleaned != null && !isKnownSkinDeviceBrand(cleaned)) {
                keywords.add(cleaned);
            }
        }
        
        return keywords;
    }
}

