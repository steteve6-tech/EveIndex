package com.certification.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * AI翻译服务
 * 用于将韩文、日文等文本翻译成英文，并串接到原文后面
 */
@Slf4j
@Service
public class AITranslationService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4o}")
    private String model;

    @Value("${openai.api.base-url:https://chat-api.ss5.xyz/v1}")
    private String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AITranslationService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 翻译文本到英文并串接到原文后面
     *
     * @param text 原文文本（韩文、日文等）
     * @param sourceLanguage 源语言（可选，如"韩语"、"日语"等）
     * @return 格式为 "原文Translation" 的字符串
     */
    public String translateAndAppend(String text, String sourceLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 如果文本已经包含英文，可能已经翻译过了，直接返回
        if (containsEnglish(text)) {
            log.debug("文本已包含英文，跳过翻译: {}", text);
            return text;
        }

        try {
            String translation = translate(text, sourceLanguage);

            if (translation != null && !translation.isEmpty()) {
                // 串接格式：原文Translation
                return text + translation;
            } else {
                log.warn("翻译结果为空，返回原文");
                return text;
            }

        } catch (Exception e) {
            log.error("翻译失败，返回原文: {}", e.getMessage());
            return text;
        }
    }

    /**
     * 批量翻译文本列表
     *
     * @param textList 文本列表
     * @param sourceLanguage 源语言
     * @return 翻译后的文本列表
     */
    public List<String> batchTranslateAndAppend(List<String> textList, String sourceLanguage) {
        if (textList == null || textList.isEmpty()) {
            return textList;
        }

        List<String> results = new ArrayList<>();

        for (String text : textList) {
            String translated = translateAndAppend(text, sourceLanguage);
            results.add(translated);

            // 避免API速率限制
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return results;
    }

    /**
     * 翻译文本到英文
     *
     * @param text 原文
     * @param sourceLanguage 源语言
     * @return 英文翻译
     */
    private String translate(String text, String sourceLanguage) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API Key未配置，跳过翻译");
            return "";
        }

        String prompt = buildTranslationPrompt(text, sourceLanguage);
        String response = callOpenAI(prompt);

        return parseTranslationResponse(response);
    }

    /**
     * 构建翻译提示词
     */
    private String buildTranslationPrompt(String text, String sourceLanguage) {
        String langHint = sourceLanguage != null ? sourceLanguage : "韩语或日语";

        return String.format(
            "你是一位专业的医疗设备领域翻译专家。\n\n" +
            "**任务：将以下%s文本翻译成英文**\n\n" +
            "**原文：**\n" +
            "%s\n\n" +
            "**翻译要求：**\n" +
            "1. 准确翻译医疗设备相关的专业术语\n" +
            "2. 保持原意，不要添加额外的解释\n" +
            "3. 翻译结果应该简洁、专业、准确\n" +
            "4. 如果是公司名称，保留英文缩写部分，只翻译描述性部分\n" +
            "5. 只返回翻译结果，不要添加任何其他说明\n\n" +
            "**示例格式：**\n" +
            "原文：(주)준영메디칼\n" +
            "翻译：Junyoung Medical Co., Ltd.\n\n" +
            "请直接返回翻译结果（不要包含'翻译：'等前缀）：",
            langHint, text
        );
    }

    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String prompt) throws Exception {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);

        String requestJson = objectMapper.writeValueAsString(requestBody);

        // 发送HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/chat/completions"))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI API调用失败: " + response.statusCode() +
                " - " + response.body());
        }

        // 解析响应
        JsonNode responseNode = objectMapper.readTree(response.body());
        return responseNode.get("choices").get(0)
            .get("message").get("content").asText();
    }

    /**
     * 解析翻译结果
     */
    private String parseTranslationResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "";
        }

        String translation = response.trim();

        // 移除常见的前缀（如果AI添加了）
        String[] prefixes = {"翻译：", "Translation:", "英文：", "English:", "结果：", "Result:"};
        for (String prefix : prefixes) {
            if (translation.startsWith(prefix)) {
                translation = translation.substring(prefix.length()).trim();
            }
        }

        // 移除引号（如果有）
        if (translation.startsWith("\"") && translation.endsWith("\"")) {
            translation = translation.substring(1, translation.length() - 1);
        }
        if (translation.startsWith("'") && translation.endsWith("'")) {
            translation = translation.substring(1, translation.length() - 1);
        }

        return translation;
    }

    /**
     * 检查文本是否包含英文字符
     */
    private boolean containsEnglish(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 检查是否包含至少3个连续的英文字母（避免误判单个英文字符）
        return text.matches(".*[a-zA-Z]{3,}.*");
    }

    /**
     * 智能检测语言并翻译
     *
     * @param text 文本
     * @return 翻译后的文本（原文+英文）
     */
    public String smartTranslateAndAppend(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 检测是否包含韩文
        if (text.matches(".*[\\uAC00-\\uD7A3]+.*")) {
            return translateAndAppend(text, "韩语");
        }
        // 检测是否包含日文
        else if (text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF]+.*")) {
            return translateAndAppend(text, "日语");
        }
        // 检测是否包含中文
        else if (text.matches(".*[\\u4E00-\\u9FA5]+.*")) {
            return translateAndAppend(text, "中文");
        }
        // 已经是英文或其他语言
        else {
            return text;
        }
    }
}
