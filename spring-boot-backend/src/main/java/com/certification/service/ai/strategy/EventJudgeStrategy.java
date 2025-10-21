package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.DeviceEventReport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 覆盖父类方法，生成设备不良事件专属的备注信息
     * 重点强调从设备名称、品牌、事件信息中判断是否包含相关数据
     */
    @Override
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();

        remark.append("【AI判断 - 设备不良事件】\n");
        remark.append("判断结果: ");
        if (judgeResult.isRelated()) {
            remark.append("设备名称、品牌、事件信息中包含测肤仪相关内容\n");
        } else {
            remark.append("设备名称、品牌、事件信息中不包含测肤仪相关内容\n");
        }
        remark.append("理由: ").append(judgeResult.getReason()).append("\n");
        remark.append("置信度: ").append(String.format("%.1f%%", judgeResult.getConfidence() * 100)).append("\n");

        if (judgeResult.getCategory() != null && !judgeResult.getCategory().isEmpty()) {
            remark.append("分类: ").append(judgeResult.getCategory()).append("\n");
        }

        remark.append("判断时间: ").append(LocalDateTime.now().format(formatter)).append("\n");
        remark.append("操作: ").append(judgeResult.isRelated() ? "保留高风险" : "降为低风险");

        return remark.toString();
    }
}

