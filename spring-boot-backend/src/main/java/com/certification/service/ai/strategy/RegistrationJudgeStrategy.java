package com.certification.service.ai.strategy;

import com.certification.entity.common.DeviceRegistrationRecord;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 设备注册记录AI判断策略
 * 通过设备名称、品牌名、制造商判断是否为测肤仪相关
 */
@Component
public class RegistrationJudgeStrategy extends BaseAIJudgeStrategy {
    
    @Override
    public String getSupportedEntityType() {
        return "DeviceRegistrationRecord";
    }
    
    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        DeviceRegistrationRecord record = (DeviceRegistrationRecord) entity;
        
        Map<String, Object> data = new HashMap<>();
        data.put("deviceName", record.getDeviceName() != null ? record.getDeviceName() : "");
        data.put("manufacturer", record.getManufacturerName() != null ? record.getManufacturerName() : "");
        data.put("proprietaryName", record.getProprietaryName() != null ? record.getProprietaryName() : "");
        data.put("deviceClass", record.getDeviceClass() != null ? record.getDeviceClass() : "");
        
        // 组合描述信息
        String description = String.format("设备名: %s, 品牌名: %s, 类别: %s",
            data.get("deviceName"),
            data.get("proprietaryName"),
            data.get("deviceClass")
        );
        data.put("description", description);
        
        return data;
    }
    
    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        DeviceRegistrationRecord record = (DeviceRegistrationRecord) entity;
        List<String> keywords = new ArrayList<>();
        
        // 提取制造商名称
        String manufacturerName = record.getManufacturerName();
        if (manufacturerName != null && !manufacturerName.trim().isEmpty()) {
            String cleaned = cleanManufacturerName(manufacturerName);
            if (cleaned != null && !isKnownSkinDeviceBrand(cleaned)) {
                keywords.add(cleaned);
            }
        }
        
        return keywords;
    }
}

