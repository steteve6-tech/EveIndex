package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.GuidanceDocument;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 覆盖父类方法，生成指导文档专属的备注信息
     * 重点强调从title、topic、description中判断是否包含相关数据
     */
    @Override
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();

        remark.append("【AI判断 - 指导文档】\n");
        remark.append("判断结果: ");
        if (judgeResult.isRelated()) {
            remark.append("标题、主题、描述中包含测肤仪相关内容\n");
        } else {
            remark.append("标题、主题、描述中不包含测肤仪相关内容\n");
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

