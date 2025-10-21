package com.certification.service.ai.strategy;

import com.certification.dto.ai.AIJudgeResult;
import com.certification.entity.common.CustomsCase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 海关判例AI判断策略
 * 通过案例编号、裁定结果、HS编码判断是否为测肤仪相关
 */
@Component
public class CustomsJudgeStrategy extends BaseAIJudgeStrategy {

    // 测肤仪相关的HS编码列表
    private static final Set<String> SKIN_DEVICE_HS_CODES = new HashSet<>(Arrays.asList(
        "9018", "8543", "9031.49", "9027", "8525"
    ));

    @Override
    public String getSupportedEntityType() {
        return "CustomsCase";
    }

    @Override
    protected Map<String, Object> buildDeviceDataForAI(Object entity) {
        CustomsCase customsCase = (CustomsCase) entity;

        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", customsCase.getCaseNumber() != null ? customsCase.getCaseNumber() : "");
        data.put("hsCode", customsCase.getHsCodeUsed() != null ? customsCase.getHsCodeUsed() : "");
        data.put("rulingResult", customsCase.getRulingResult() != null ? customsCase.getRulingResult() : "");

        // deviceName使用案例编号
        data.put("deviceName", "海关案例: " + data.get("caseNumber"));

        // 检查是否包含测肤仪相关的HS编码
        String hsCode = String.valueOf(data.get("hsCode"));
        boolean containsSkinDeviceHsCode = checkIfContainsSkinDeviceHsCode(hsCode);
        data.put("containsSkinDeviceHsCode", containsSkinDeviceHsCode);

        // 组合描述信息
        String description = String.format("案例编号: %s, HS编码: %s%s, 裁定结果: %s",
            data.get("caseNumber"),
            hsCode,
            containsSkinDeviceHsCode ? " (包含测肤仪相关HS编码)" : "",
            data.get("rulingResult")
        );

        // 限制描述长度
        if (description.length() > 500) {
            description = description.substring(0, 500) + "...";
        }

        data.put("description", description);

        return data;
    }

    @Override
    protected List<String> extractBlacklistKeywords(Object entity) {
        // 海关判例通常不提取黑名单关键词
        return new ArrayList<>();
    }

    /**
     * 检查HS编码是否包含测肤仪相关的编码
     * 支持多个HS编码用逗号、分号、空格等分隔
     */
    private boolean checkIfContainsSkinDeviceHsCode(String hsCodeStr) {
        if (hsCodeStr == null || hsCodeStr.trim().isEmpty()) {
            return false;
        }

        // 提取所有数字和点号的组合作为HS编码
        Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(hsCodeStr);

        while (matcher.find()) {
            String code = matcher.group();
            // 检查是否匹配任何测肤仪相关的HS编码
            for (String skinCode : SKIN_DEVICE_HS_CODES) {
                if (code.startsWith(skinCode) || skinCode.startsWith(code)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 覆盖父类方法，生成海关案例专属的备注信息
     * 重点强调从hsCode和rulingResult中判断是否包含相关数据
     */
    @Override
    protected String formatRemark(AIJudgeResult judgeResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder remark = new StringBuilder();

        remark.append("【AI判断 - 海关案例】\n");
        remark.append("判断结果: ");
        if (judgeResult.isRelated()) {
            remark.append("HS编码和裁定结果中包含测肤仪相关内容\n");
        } else {
            remark.append("HS编码和裁定结果中不包含测肤仪相关内容\n");
        }
        remark.append("理由: ").append(judgeResult.getReason()).append("\n");
        remark.append("置信度: ").append(String.format("%.1f%%", judgeResult.getConfidence() * 100)).append("\n");

        if (judgeResult.getCategory() != null && !judgeResult.getCategory().isEmpty()) {
            remark.append("分类: ").append(judgeResult.getCategory()).append("\n");
        }

        remark.append("相关HS编码: 9018, 8543, 9031.49, 9027, 8525\n");
        remark.append("判断时间: ").append(LocalDateTime.now().format(formatter)).append("\n");
        remark.append("操作: ").append(judgeResult.isRelated() ? "保留高风险" : "降为低风险");

        return remark.toString();
    }
}

