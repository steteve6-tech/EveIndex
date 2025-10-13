package com.certification.service.ai;

import com.certification.dto.ai.CertNewsClassificationResult;
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
 * 认证新闻AI判断服务
 * 用于判断认证新闻是否与无线电子设备认证标准相关
 */
@Slf4j
@Service
public class CertNewsAIJudgeService {
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.model:gpt-4o}")
    private String model;
    
    @Value("${openai.api.base-url:https://chat-api.ss5.xyz/v1}")
    private String baseUrl;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public CertNewsAIJudgeService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 判断认证新闻是否与无线电子设备认证标准相关
     * 
     * @param newsData 新闻数据
     * @return 分类结果
     */
    public CertNewsClassificationResult classifyCertificationNews(Map<String, Object> newsData) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("default")) {
            log.warn("OpenAI API Key未配置，返回模拟结果");
            return createMockResult(newsData);
        }
        
        try {
            String prompt = buildCertificationPrompt(newsData);
            String response = callOpenAI(prompt);
            return parseClassificationResponse(response);
        } catch (Exception e) {
            log.error("AI分类判断失败", e);
            // 返回不确定结果
            return new CertNewsClassificationResult(false, 0.0, "AI服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 构建认证新闻判断提示词
     */
    private String buildCertificationPrompt(Map<String, Object> newsData) {
        String title = String.valueOf(newsData.getOrDefault("title", ""));
        String content = String.valueOf(newsData.getOrDefault("content", ""));
        String summary = String.valueOf(newsData.getOrDefault("summary", ""));
        String country = String.valueOf(newsData.getOrDefault("country", ""));
        
        return String.format(
            "你是一位产品认证领域的专家，专门负责识别与无线电子设备认证标准相关的新闻信息。\n\n" +
            "**任务：判断以下新闻是否与无线电子设备认证标准相关**\n\n" +
            "**适用设备范围：**\n" +
            "聚焦\"无线电子设备\"，即具备无线通信功能的电子设备：\n" +
            "- 无线通信功能：Wi-Fi、蓝牙、射频传输、蜂窝通信、NFC、Zigbee等\n" +
            "- 设备类型：无线智能设备、无线通信终端、含无线模块的电子仪器、IoT设备等\n" +
            "- 包括但不限于：智能手机、平板、智能手表、无线耳机、智能家居设备、无线医疗设备、无线测量仪器等\n\n" +
            "**认证/标准范畴（必须包含以下之一）：**\n" +
            "1. 官方认证：\n" +
            "   - FCC认证（美国，需明确FCC Part 15规则，Class A/B数字设备）\n" +
            "   - RoHS认证（限制有害物质）\n" +
            "   - SRRC认证（中国无线设备型号核准）\n" +
            "   - CE认证（欧盟市场准入，含CE-RED无线设备专项）\n" +
            "   - RED认证（欧盟无线设备指令RED 2014/53/EU）\n" +
            "   - EN 18031认证（欧盟无线电设备网络安全标准）\n" +
            "2. 安全/技术标准：\n" +
            "   - GB4706.1（家用电器安全通用要求）\n" +
            "   - GB4706.15（皮肤及毛发护理器具安全）\n" +
            "   - EN 18031系列（欧盟无线电设备网络安全）\n" +
            "   - EN 301 489、EN 300 328等无线设备EMC/射频标准\n" +
            "3. 认证相关动态：\n" +
            "   - 设备通过/更新认证\n" +
            "   - 认证标准发布/修订\n" +
            "   - 认证机构公告/要求变更\n\n" +
            "**新闻信息：**\n" +
            "国家: %s\n" +
            "标题: %s\n" +
            "摘要: %s\n" +
            "内容: %s\n\n" +
            "**请按以下JSON格式返回判断结果（只返回JSON，不要其他内容）：**\n" +
            "```json\n" +
            "{\n" +
            "  \"isRelated\": true/false,\n" +
            "  \"confidence\": 0.0-1.0,\n" +
            "  \"reason\": \"简短的判断理由\",\n" +
            "  \"extractedKeywords\": [\"提取的认证关键词1\", \"关键词2\"]\n" +
            "}\n" +
            "```\n\n" +
            "**extractedKeywords提取规则：**\n" +
            "- 只提取认证标准名称（如FCC Part 15、CE-RED、SRRC、RoHS、GB4706.15、EN 18031）\n" +
            "- 包括认证机构名称（如FCC、工信部、欧盟公告机构）\n" +
            "- 包括具体的技术标准编号（如EN 301 489、EN 300 328）\n" +
            "- 不要提取产品名称或公司名称\n" +
            "- 如果不相关，返回空数组[]\n\n" +
            "**判断标准：**\n" +
            "1. 明确提到上述认证类型之一\n" +
            "2. 内容涉及无线电子设备的认证要求或变更\n" +
            "3. 排除：纯粹的产品发布新闻（无认证信息）\n" +
            "4. 排除：与认证无关的技术新闻\n" +
            "5. 排除：其他行业认证（食品、建筑、汽车等，除非涉及电子设备）\n",
            country, title, summary, truncateContent(content, 2000)
        );
    }
    
    /**
     * 截断过长的内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
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
        requestBody.put("max_tokens", 800);  // 增加token限制以支持关键词提取
        
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        
        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
            .build();
        
        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode responseJson = objectMapper.readTree(response.body());
            String content = responseJson
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
            
            log.debug("OpenAI响应: {}", content);
            return content;
        } else {
            String errorBody = response.body();
            log.error("OpenAI API调用失败: {} - {}", response.statusCode(), errorBody);
            throw new RuntimeException("OpenAI API调用失败: " + response.statusCode() + " - " + errorBody);
        }
    }
    
    /**
     * 解析分类响应
     */
    private CertNewsClassificationResult parseClassificationResponse(String response) {
        try {
            // 提取JSON部分
            String jsonPart = response;
            if (response.contains("```json")) {
                int start = response.indexOf("```json") + 7;
                int end = response.indexOf("```", start);
                if (end > start) {
                    jsonPart = response.substring(start, end).trim();
                }
            } else if (response.contains("```")) {
                int start = response.indexOf("```") + 3;
                int end = response.indexOf("```", start);
                if (end > start) {
                    jsonPart = response.substring(start, end).trim();
                }
            }
            
            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonPart);
            
            boolean isRelated = jsonNode.path("isRelated").asBoolean(false);
            double confidence = jsonNode.path("confidence").asDouble(0.0);
            String reason = jsonNode.path("reason").asText("AI未提供判断理由");
            
            CertNewsClassificationResult result = new CertNewsClassificationResult(isRelated, confidence, reason);
            
            // 提取关键词
            JsonNode keywordsNode = jsonNode.path("extractedKeywords");
            if (keywordsNode.isArray()) {
                for (JsonNode keywordNode : keywordsNode) {
                    result.addExtractedKeyword(keywordNode.asText());
                }
            }
            
            log.debug("解析AI响应成功: isRelated={}, confidence={}, keywords={}", 
                isRelated, confidence, result.getExtractedKeywords());
            
            return result;
            
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            return new CertNewsClassificationResult(false, 0.0, "响应解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建模拟结果（用于测试）
     */
    private CertNewsClassificationResult createMockResult(Map<String, Object> newsData) {
        String title = String.valueOf(newsData.getOrDefault("title", "")).toLowerCase();
        String content = String.valueOf(newsData.getOrDefault("content", "")).toLowerCase();
        String summary = String.valueOf(newsData.getOrDefault("summary", "")).toLowerCase();
        
        String searchText = (title + " " + summary + " " + content).toLowerCase();
        
        // 模拟检查关键认证词
        List<String> certKeywords = Arrays.asList(
            "fcc", "ce", "red", "srrc", "rohs", "18031", "gb4706", "en 301", "en 300"
        );
        
        List<String> extractedKeywords = new ArrayList<>();
        boolean isRelated = false;
        
        for (String keyword : certKeywords) {
            if (searchText.contains(keyword)) {
                isRelated = true;
                extractedKeywords.add(keyword.toUpperCase());
            }
        }
        
        String reason = isRelated ? 
            "模拟判断：包含认证关键词 " + extractedKeywords :
            "模拟判断：未包含已知的认证标准关键词";
        
        CertNewsClassificationResult result = new CertNewsClassificationResult(isRelated, 0.75, reason);
        extractedKeywords.forEach(result::addExtractedKeyword);
        
        return result;
    }
}

