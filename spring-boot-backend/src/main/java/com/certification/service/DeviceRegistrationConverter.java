package com.certification.service;

import com.certification.entity.common.DeviceRegistrationRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备注册记录数据转换器
 * 用于将US FDA和EU EUDAMED的数据转换为共有实体类
 */
@Component
public class DeviceRegistrationConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将US FDA数据转换为共有实体类
     */
    public DeviceRegistrationRecord convertFromUSFDA(Map<String, Object> usData) {
        DeviceRegistrationRecord record = new DeviceRegistrationRecord();
        
        // 数据源标识
        record.setDataSource("US_FDA");
        record.setJdCountry("US");
        
        // 核心标识字段 - US FDA使用K_number+pma_number作为主要标识符
        String kNumber = getStringValue(usData, "k_number");
        String pmaNumber = getStringValue(usData, "pma_number");
        String combinedRegistrationNumber = combineRegistrationNumber(kNumber, pmaNumber);
        record.setRegistrationNumber(combinedRegistrationNumber);
        record.setFeiNumber(getStringValue(usData, "fei_number"));
        
        // 制造商信息 - 只保留制造商名称
        record.setManufacturerName(getStringValue(usData, "manufacturer_name"));
        
        // 设备信息
        record.setDeviceName(extractFirstFromList(usData, "device_names"));
        record.setProprietaryName(extractFirstFromList(usData, "proprietary_names"));
        record.setDeviceClass(extractFirstFromList(usData, "device_classes"));
        
        // 状态信息
        record.setStatusCode(getStringValue(usData, "status_code"));
        
        // 元数据
        record.setCrawlTime(LocalDateTime.now());
        
        return record;
    }

    /**
     * 将EU EUDAMED数据转换为共有实体类
     */
    public DeviceRegistrationRecord convertFromEUEUDAMED(Map<String, Object> euData) {
        DeviceRegistrationRecord record = new DeviceRegistrationRecord();
        
        // 数据源标识
        record.setDataSource("EU_EUDAMED");
        record.setJdCountry("EU");
        
        // 核心标识字段
        record.setRegistrationNumber(getStringValue(euData, "udi_di"));
        record.setFeiNumber(getStringValue(euData, "basic_udi_di"));
        
        // 制造商信息 - 只保留制造商名称
        record.setManufacturerName(getStringValue(euData, "manufacturer"));
        
        // 设备信息
        record.setDeviceName(getStringValue(euData, "trade_name"));
        record.setProprietaryName(getStringValue(euData, "trade_name"));
        record.setDeviceClass(getStringValue(euData, "device_class"));
        
        // 状态信息
        record.setStatusCode(getStringValue(euData, "status_code"));
        
        // 元数据
        record.setCrawlTime(LocalDateTime.now());
        
        return record;
    }

    /**
     * 组合K_number和pma_number作为US FDA的注册号
     */
    private String combineRegistrationNumber(String kNumber, String pmaNumber) {
        StringBuilder combined = new StringBuilder();
        
        if (kNumber != null && !kNumber.trim().isEmpty()) {
            combined.append(kNumber.trim());
        }
        
        if (pmaNumber != null && !pmaNumber.trim().isEmpty()) {
            if (combined.length() > 0) {
                combined.append("+");
            }
            combined.append(pmaNumber.trim());
        }
        
        return combined.length() > 0 ? combined.toString() : null;
    }

    /**
     * 从Map中获取字符串值
     */
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从列表中提取第一个元素
     */
    private String extractFirstFromList(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.isEmpty() ? null : list.get(0).toString();
        } else if (value instanceof String) {
            // 如果是JSON字符串，尝试解析
            try {
                List<?> list = objectMapper.readValue((String) value, List.class);
                return list.isEmpty() ? null : list.get(0).toString();
            } catch (JsonProcessingException e) {
                return (String) value;
            }
        }
        return value != null ? value.toString() : null;
    }

    /**
     * 将列表转换为JSON字符串
     */
    private String convertListToJson(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof List) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                return null;
            }
        } else if (value instanceof String) {
            return (String) value;
        }
        
        return value.toString();
    }
}