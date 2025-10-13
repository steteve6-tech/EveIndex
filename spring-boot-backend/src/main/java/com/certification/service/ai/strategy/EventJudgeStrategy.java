package com.certification.service.ai.strategy;

import com.certification.entity.common.DeviceEventReport;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 设备不良事件AI判断策略
 * 通过设备名称、事件描述、制造商判断是否为测肤仪相关
 */
@Component
public class EventJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "DeviceEventReport";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        DeviceEventReport event = (DeviceEventReport) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", event.getGenericName() != null ? event.getGenericName() : "");
        data.put("manufacturer", event.getManufacturerName() != null ? event.getManufacturerName() : "");
        data.put("brandName", event.getBrandName() != null ? event.getBrandName() : "");
        data.put("deviceClass", event.getDeviceClass() != null ? event.getDeviceClass() : "");
        
        // 组合描述信息
        String description = String.format("设备名: %s, 品牌: %s, 类别: %s",
            data.get("deviceName"),
            data.get("brandName"),
            data.get("deviceClass")
        );
        data.put("description", description);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        DeviceEventReport event = (DeviceEventReport) entity;
        List<String> keywords = new ArrayList<>();
        
        // 提取制造商名称
        String manufacturerName = event.getManufacturerName();
        if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
            String cleaned = cleanManufacturerName(manufacturerName);
            if (cleaned != null && !isKnownSkinDeviceBrand(cleaned)) {
                keywords.add(cleaned);
            }
        }
        
        return keywords;
    }
}

