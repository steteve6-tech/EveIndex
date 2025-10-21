package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.Device510K;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.certification.repository.common.Device510KRepository;

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

    /**
     * 覆盖父类方法，生成510K申请记录专属的备注信息
     * 重点强调从设备名称、品牌名、申请人中判断是否包含相关数据
     */
    @Override
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();

        remark.append("【AI判断 - 510K申请记录】\n");
        remark.append("判断结果: ");
        if (judgeResult.isRelated()) {
            remark.append("设备名称、品牌名、申请人中包含测肤仪相关内容\n");
        } else {
            remark.append("设备名称、品牌名、申请人中不包含测肤仪相关内容\n");
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

    @Autowired
    private Device510KRepository device510KRepository;

    @Override
    public Object findEntityById(Long entityId) {
        return device510KRepository.findById(entityId).orElse(null);
    }

    @Override
    public void saveEntity(Object entity) {
        device510KRepository.save((Device510K) entity);
    }
}
