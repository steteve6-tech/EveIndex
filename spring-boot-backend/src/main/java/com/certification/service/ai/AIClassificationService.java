package com.certification.service.ai;

import com.certification.dto.ai.ClassificationResult;
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
 * AI分类服务
 * 用于判断医疗设备数据是否为测肤仪相关设备
 */
@Slf4j
@Service
public class AIClassificationService {
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.model:gpt-4o}")
    private String model;
    
    @Value("${openai.api.base-url:https://chat-api.ss5.xyz/v1}")
    private String baseUrl;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public AIClassificationService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 判断设备是否为测肤仪相关
     * 
     * @param deviceData 设备数据
     * @return 分类结果
     */
    public ClassificationResult classifySkinDevice(Map<String, Object> deviceData) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API Key未配置，返回模拟结果");
            return createMockResult(deviceData);
        }
        
        try {
            String prompt = buildClassificationPrompt(deviceData);
            String response = callOpenAI(prompt);
            return parseClassificationResponse(response);
        } catch (Exception e) {
            log.error("AI分类判断失败", e);
            // 返回不确定结果
            return new ClassificationResult(false, 0.0, "AI服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 批量判断设备
     */
    public List<ClassificationResult> batchClassifySkinDevices(List<Map<String, Object>> devices) {
        List<ClassificationResult> results = new ArrayList<>();
        
        for (Map<String, Object> device : devices) {
            ClassificationResult result = classifySkinDevice(device);
            results.add(result);
            
            // 避免超过API速率限制
            try {
                Thread.sleep(500); // 每次请求间隔500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return results;
    }
    
    /**
     * 构建分类提示词
     */
    private String buildClassificationPrompt(Map<String, Object> deviceData) {
        String entityType = String.valueOf(deviceData.getOrDefault("entityType", ""));

        // 根据不同的实体类型构建不同的提示词
        switch (entityType) {
            case "GuidanceDocument":
                return buildGuidanceDocumentPrompt(deviceData);
            case "CustomsCase":
                return buildCustomsCasePrompt(deviceData);
            default:
                return buildDevicePrompt(deviceData);
        }
    }

    /**
     * 构建指导文档的判断提示词
     */
    private String buildGuidanceDocumentPrompt(Map<String, Object> deviceData) {
        String title = String.valueOf(deviceData.getOrDefault("deviceName", ""));
        String description = String.valueOf(deviceData.getOrDefault("description", ""));

        return String.format(
            "你是一位医疗设备监管领域的专家，专门负责识别与测肤仪相关的法规文档。\n\n" +
            "**任务：判断以下指导文档/新闻是否与测肤仪产品或相关法规有关**\n\n" +
            "**测肤仪相关法规包括：**\n" +
            "- 专门针对皮肤检测、分析、成像设备的法规\n" +
            "- 涉及皮肤科医疗设备的监管指南\n" +
            "- 面部成像、皮肤镜、色素检测等设备的标准\n" +
            "- 皮肤健康评估设备的技术要求\n" +
            "- 医疗美容设备中的皮肤分析类设备规定\n\n" +
            "**文档信息：**\n" +
            "%s\n\n" +
            "**请按以下JSON格式返回判断结果（只返回JSON，不要其他内容）：**\n" +
            "```json\n" +
            "{\n" +
            "  \"isRelated\": true/false,\n" +
            "  \"confidence\": 0.0-1.0,\n" +
            "  \"category\": \"测肤仪法规/皮肤设备标准/医美设备指南/其他\",\n" +
            "  \"reason\": \"简要说明文档内容是否涉及测肤仪相关法规或产品\"\n" +
            "}\n" +
            "```\n\n" +
            "**判断标准：**\n" +
            "1. 文档标题或主题明确提到皮肤检测、皮肤分析、面部成像等\n" +
            "2. 内容涉及皮肤科诊断设备、医美分析设备的监管\n" +
            "3. 包含测肤仪相关设备的技术要求或审批指南\n" +
            "4. 排除：纯治疗类设备（激光、射频等）的法规\n" +
            "5. 排除：与皮肤无关的通用医疗设备法规\n" +
            "6. 排除：药品、化妆品相关的法规文档\n",
            description
        );
    }

    /**
     * 构建海关案例的判断提示词
     */
    private String buildCustomsCasePrompt(Map<String, Object> deviceData) {
        String caseInfo = String.valueOf(deviceData.getOrDefault("deviceName", ""));
        String description = String.valueOf(deviceData.getOrDefault("description", ""));

        return String.format(
            "你是一位海关归类专家，专门负责识别与测肤仪相关的海关案例。\n\n" +
            "**任务：判断以下海关案例是否与测肤仪产品相关**\n\n" +
            "**测肤仪相关HS编码：**\n" +
            "- 9018（医疗仪器及器具）：包含皮肤检测诊断设备\n" +
            "- 8543（具有独立功能的电气设备）：可能包含电子皮肤分析仪\n" +
            "- 9031.49（光学测量或检验仪器）：可能包含光学皮肤检测设备\n" +
            "- 9027（分析检验仪器）：可能包含皮肤成分分析设备\n" +
            "- 8525（图像采集设备）：可能包含皮肤成像系统\n\n" +
            "**案例信息：**\n" +
            "%s\n\n" +
            "**请按以下JSON格式返回判断结果（只返回JSON，不要其他内容）：**\n" +
            "```json\n" +
            "{\n" +
            "  \"isRelated\": true/false,\n" +
            "  \"confidence\": 0.0-1.0,\n" +
            "  \"category\": \"测肤仪归类案例/皮肤设备裁定/其他\",\n" +
            "  \"reason\": \"说明HS编码或裁定结果中是否涉及测肤仪相关产品\"\n" +
            "}\n" +
            "```\n\n" +
            "**判断标准：**\n" +
            "1. HS编码为上述测肤仪相关编码（9018/8543/9031.49/9027/8525）\n" +
            "2. 裁定结果中明确提到皮肤检测、面部分析、皮肤成像等设备\n" +
            "3. 案例涉及皮肤科医疗器械或医美分析设备的归类\n" +
            "4. 排除：纯治疗设备（激光治疗仪、射频设备）的案例\n" +
            "5. 排除：与皮肤无关的医疗设备案例\n" +
            "6. 排除：化妆品、护肤品等非设备类产品的案例\n",
            description
        );
    }

    /**
     * 构建设备数据的判断提示词（默认）
     */
    private String buildDevicePrompt(Map<String, Object> deviceData) {
        String deviceName = String.valueOf(deviceData.getOrDefault("deviceName", ""));
        String description = String.valueOf(deviceData.getOrDefault("description", ""));
        String manufacturer = String.valueOf(deviceData.getOrDefault("manufacturer", ""));
        String intendedUse = String.valueOf(deviceData.getOrDefault("intendedUse", ""));
        String entityType = String.valueOf(deviceData.getOrDefault("entityType", ""));

        return String.format(
            "你是一位医疗设备监管领域的专家，专门负责识别皮肤检测相关的医疗设备。\n\n" +
            "**任务：判断以下设备是否为测肤仪/皮肤分析仪相关设备**\n\n" +
            "**测肤仪定义：**\n" +
            "用于皮肤检测、分析、诊断的医疗设备，包括但不限于：\n" +
            "- 皮肤成像系统（Skin Imaging System）\n" +
            "- 面部分析仪（Facial Analysis Device）\n" +
            "- 皮肤镜（Dermatoscope）\n" +
            "- 3D皮肤扫描仪（3D Skin Scanner）\n" +
            "- 色素检测设备（Pigmentation Detector）\n" +
            "- 皮肤弹性测试仪（Skin Elasticity Tester）\n" +
            "- 皮肤水分测试仪（Skin Moisture Analyzer）\n" +
            "- 面部成像分析（Facial Imaging Analysis）\n\n" +
            "**设备信息：**\n" +
            "数据类型: %s\n" +
            "设备名称: %s\n" +
            "制造商: %s\n" +
            "产品描述: %s\n" +
            "预期用途: %s\n\n" +
            "**请按以下JSON格式返回判断结果（只返回JSON，不要其他内容）：**\n" +
            "```json\n" +
            "{\n" +
            "  \"isRelated\": true/false,\n" +
            "  \"confidence\": 0.0-1.0,\n" +
            "  \"category\": \"皮肤成像系统/面部分析仪/皮肤镜/其他\",\n" +
            "  \"reason\": \"简短的判断理由\"\n" +
            "}\n" +
            "```\n\n" +
            "**判断标准：**\n" +
            "1. 设备名称或描述明确提到皮肤、面部、皮肤科相关\n" +
            "2. 用途涉及皮肤检测、分析、诊断\n" +
            "3. 属于医疗美容或皮肤科诊断设备\n" +
            "4. 排除：CT、MRI、X光、超声、心电图等非皮肤专用设备\n" +
            "5. 排除：纯治疗设备（如激光治疗仪）\n" +
            "6. 排除：通用医疗设备（如血压计、体温计）\n",
            entityType, deviceName, manufacturer, description, intendedUse
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
     * 解析AI返回的分类结果
     */
    private ClassificationResult parseClassificationResponse(String response) {
        try {
            // 提取JSON部分
            String json = response.trim();
            if (json.contains("```json")) {
                int start = json.indexOf("```json") + 7;
                int end = json.lastIndexOf("```");
                if (end > start) {
                    json = json.substring(start, end).trim();
                }
            } else if (json.contains("```")) {
                int start = json.indexOf("```") + 3;
                int end = json.lastIndexOf("```");
                if (end > start) {
                    json = json.substring(start, end).trim();
                }
            }
            
            JsonNode node = objectMapper.readTree(json);
            
            boolean isRelated = node.get("isRelated").asBoolean();
            double confidence = node.get("confidence").asDouble();
            String reason = node.get("reason").asText();
            String category = node.has("category") ? node.get("category").asText() : "";
            
            return new ClassificationResult(isRelated, confidence, reason, category);
        } catch (Exception e) {
            log.error("解析AI返回结果失败: {}", response, e);
            return new ClassificationResult(false, 0.0, "解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建模拟结果（当API Key未配置时）
     */
    private ClassificationResult createMockResult(Map<String, Object> deviceData) {
        String deviceName = String.valueOf(deviceData.getOrDefault("deviceName", "")).toLowerCase();
        String description = String.valueOf(deviceData.getOrDefault("description", "")).toLowerCase();
        
        // 简单的关键词匹配
        String[] skinKeywords = {"skin", "facial", "face", "dermat", "pigment", "elasticity", 
            "moisture", "visia", "皮肤", "面部", "肤质", "色素"};
        
        for (String keyword : skinKeywords) {
            if (deviceName.contains(keyword) || description.contains(keyword)) {
                return new ClassificationResult(true, 0.85, 
                    "模拟判断：包含关键词 '" + keyword + "'", "皮肤设备");
            }
        }
        
        return new ClassificationResult(false, 0.90, 
            "模拟判断：未包含皮肤相关关键词", "");
    }
}

