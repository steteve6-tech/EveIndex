package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.DeviceRecallRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 覆盖父类方法，生成设备召回记录专属的备注信息
     * 重点强调从设备名称、产品描述、召回等级中判断是否包含相关数据
     */
    @Override
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();

        remark.append("【AI判断 - 设备召回记录】\n");
        remark.append("判断结果: ");
        if (judgeResult.isRelated()) {
            remark.append("设备名称、产品描述、召回信息中包含测肤仪相关内容\n");
        } else {
            remark.append("设备名称、产品描述、召回信息中不包含测肤仪相关内容\n");
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

