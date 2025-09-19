package com.certification.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HS编码工具类
 * 用于处理多个HS编码的解析、查询和验证
 */
@Slf4j
@Component
public class HsCodeUtils {

    /**
     * 解析HS编码字符串（逗号分隔）
     * @param hsCodeString HS编码字符串，如 "9018.50,8543.70"
     * @return HS编码列表
     */
    public static List<String> parseHsCodes(String hsCodeString) {
        if (hsCodeString == null || hsCodeString.trim().isEmpty()) {
            return List.of();
        }
        
        return Arrays.stream(hsCodeString.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 将HS编码列表合并为字符串
     * @param hsCodes HS编码列表
     * @return 逗号分隔的HS编码字符串
     */
    public static String joinHsCodes(List<String> hsCodes) {
        if (hsCodes == null || hsCodes.isEmpty()) {
            return "";
        }
        
        return hsCodes.stream()
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.joining(","));
    }

    /**
     * 验证HS编码格式
     * @param hsCode HS编码
     * @return 是否有效
     */
    public static boolean isValidHsCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return false;
        }
        
        String trimmedCode = hsCode.trim();
        // 匹配4位数字或4位数字.2位数字的格式
        return trimmedCode.matches("\\d{4}") || trimmedCode.matches("\\d{4}\\.\\d{2}");
    }

    /**
     * 验证HS编码字符串（可能包含多个编码）
     * @param hsCodeString HS编码字符串
     * @return 是否有效
     */
    public static boolean isValidHsCodeString(String hsCodeString) {
        if (hsCodeString == null || hsCodeString.trim().isEmpty()) {
            return true; // 空字符串认为是有效的
        }
        
        List<String> codes = parseHsCodes(hsCodeString);
        return codes.stream().allMatch(HsCodeUtils::isValidHsCode);
    }

    /**
     * 检查HS编码字符串是否包含指定的编码
     * @param hsCodeString HS编码字符串
     * @param targetCode 目标编码
     * @return 是否包含
     */
    public static boolean containsHsCode(String hsCodeString, String targetCode) {
        if (hsCodeString == null || targetCode == null) {
            return false;
        }
        
        List<String> codes = parseHsCodes(hsCodeString);
        return codes.contains(targetCode.trim());
    }

    /**
     * 获取HS编码的前4位（章）
     * @param hsCode HS编码
     * @return 4位章编码
     */
    public static String getChapterCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return "";
        }
        
        String trimmedCode = hsCode.trim();
        if (trimmedCode.length() >= 4) {
            return trimmedCode.substring(0, 4);
        }
        
        return trimmedCode;
    }

    /**
     * 检查两个HS编码是否属于同一章
     * @param code1 编码1
     * @param code2 编码2
     * @return 是否属于同一章
     */
    public static boolean isSameChapter(String code1, String code2) {
        String chapter1 = getChapterCode(code1);
        String chapter2 = getChapterCode(code2);
        return chapter1.equals(chapter2) && !chapter1.isEmpty();
    }

    /**
     * 从HS编码字符串中提取所有章编码
     * @param hsCodeString HS编码字符串
     * @return 章编码列表（去重）
     */
    public static List<String> extractChapterCodes(String hsCodeString) {
        List<String> codes = parseHsCodes(hsCodeString);
        return codes.stream()
                .map(HsCodeUtils::getChapterCode)
                .filter(chapter -> !chapter.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 格式化HS编码显示
     * @param hsCodeString HS编码字符串
     * @return 格式化后的显示字符串
     */
    public static String formatHsCodes(String hsCodeString) {
        List<String> codes = parseHsCodes(hsCodeString);
        if (codes.isEmpty()) {
            return "无HS编码";
        }
        
        if (codes.size() == 1) {
            return codes.get(0);
        }
        
        return String.format("%s (共%d个)", joinHsCodes(codes), codes.size());
    }
}


