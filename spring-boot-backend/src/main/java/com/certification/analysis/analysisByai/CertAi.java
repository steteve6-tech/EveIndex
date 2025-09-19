package com.certification.analysis.analysisByai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * 火山引擎豆包AI客户端
 * 基于火山引擎ARK API实现，支持标准请求、多轮对话和流式响应
 */
@Component
public class CertAi {
    private final WebClient webClient;
    private final VolcAiConfig config;
    private final ObjectMapper objectMapper;

    // 构造函数注入配置
    public CertAi(VolcAiConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 无参构造函数用于测试
    public CertAi() {
        this.config = createTestConfig();
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(config.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // 创建测试配置
    private VolcAiConfig createTestConfig() {
        VolcAiConfig testConfig = new VolcAiConfig();
        
        // 从环境变量获取API密钥，如果没有则使用默认值
        String apiKey = System.getenv("ARK_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "a3e53f23-f797-4d39-9ad1-faf43521504e"; // 默认测试密钥
        }
        
        testConfig.setApiKey(apiKey);
        testConfig.setModelId("bot-20250915145921-rspmk"); // 豆包智能体ID
        testConfig.setApiUrl("https://ark.cn-beijing.volces.com/api/v3/bots");
        
        return testConfig;
    }

    /**
     * 标准请求 - 单次对话
     * @param userMessage 用户消息
     * @return AI响应内容
     */
    public Mono<String> standardRequest(String userMessage) {
        return standardRequest(userMessage, "你是豆包，是由字节跳动开发的 AI 人工智能助手");
    }

    /**
     * 标准请求 - 单次对话（自定义系统提示）
     * @param userMessage 用户消息
     * @param systemMessage 系统提示
     * @return AI响应内容
     */
    public Mono<String> standardRequest(String userMessage, String systemMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemMessage);
        messages.add(systemMsg);
        
        // 用户消息
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        return sendRequest(messages, false)
                .map(this::extractContent)
                .doOnNext(content -> {
                    System.out.println("----- standard request -----");
                    System.out.println(content);
                });
    }

    /**
     * 多轮对话请求
     * @param conversationHistory 对话历史
     * @return AI响应内容
     */
    public Mono<String> multipleRoundsRequest(List<Map<String, String>> conversationHistory) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是豆包，是由字节跳动开发的 AI 人工智能助手");
        messages.add(systemMsg);
        
        // 添加对话历史
        for (Map<String, String> turn : conversationHistory) {
            Map<String, Object> msg = new HashMap<>();
            msg.put("role", turn.get("role"));
            msg.put("content", turn.get("content"));
            messages.add(msg);
        }
        
        return sendRequest(messages, false)
                .map(this::extractContent)
                .doOnNext(content -> {
                    System.out.println("----- multiple rounds request -----");
                    System.out.println(content);
                });
    }

    /**
     * 流式请求
     * @param userMessage 用户消息
     * @return 流式响应内容
     */
    public Flux<String> streamingRequest(String userMessage) {
        return streamingRequest(userMessage, "你是豆包，是由字节跳动开发的 AI 人工智能助手");
    }

    /**
     * 流式请求（自定义系统提示）
     * @param userMessage 用户消息
     * @param systemMessage 系统提示
     * @return 流式响应内容
     */
    public Flux<String> streamingRequest(String userMessage, String systemMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 系统消息
        Map<String, Object> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemMessage);
        messages.add(systemMsg);
        
        // 用户消息
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        return sendStreamingRequest(messages)
                .doOnNext(chunk -> {
                    if (chunk != null && !chunk.isEmpty()) {
                        System.out.print(chunk);
                    }
                });
    }

    /**
     * 发送标准请求
     */
    private Mono<JsonNode> sendRequest(List<Map<String, Object>> messages, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModelId());
        requestBody.put("messages", messages);
        requestBody.put("stream", stream);
        
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    /**
     * 发送流式请求
     */
    private Flux<String> sendStreamingRequest(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModelId());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::parseStreamChunk);
    }

    /**
     * 解析流式响应块
     */
    private String parseStreamChunk(String chunk) {
        try {
            if (chunk.startsWith("data: ")) {
                String jsonStr = chunk.substring(6);
                if (jsonStr.equals("[DONE]")) {
                    return null;
                }
                
                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).get("delta");
                    if (delta != null && delta.has("content")) {
                        return delta.get("content").asText();
                    }
                }
            }
        } catch (Exception e) {
            // 忽略解析错误，继续处理下一个块
        }
        return null;
    }

    /**
     * 提取响应内容
     */
    private String extractContent(JsonNode response) {
        try {
            JsonNode choices = response.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("解析响应内容失败: " + e.getMessage());
        }
        return "无法获取响应内容";
    }


    // 主函数用于测试
    public static void main(String[] args) {
        System.out.println("=== 火山引擎豆包AI测试程序 ===");
        System.out.println("注意：请确保已设置环境变量 ARK_API_KEY");
        System.out.println();

        // 检查配置
        checkConfiguration();
        
        // 创建测试实例
        CertAi certAi = new CertAi();
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("请选择测试模式：");
            System.out.println("1. 标准请求测试");
            System.out.println("2. 多轮对话测试");
            System.out.println("3. 流式请求测试");
            System.out.println("4. 预设测试用例");
            System.out.println("0. 退出");
            System.out.print("请输入选择 (0-4): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    testStandardRequest(scanner, certAi);
                    break;
                case "2":
                    testMultipleRounds(scanner, certAi);
                    break;
                case "3":
                    testStreamingRequest(scanner, certAi);
                    break;
                case "4":
                    testPresetCases(certAi);
                    break;
                case "0":
                    System.out.println("退出测试程序");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选择，请重新输入");
            }
            
            System.out.println("\n" + "=".repeat(50) + "\n");
        }
    }
    
    // 标准请求测试
    private static void testStandardRequest(Scanner scanner, CertAi certAi) {
        System.out.println("\n--- 标准请求测试模式 ---");
        System.out.print("请输入要询问的问题: ");
        String question = scanner.nextLine().trim();
        
        if (question.isEmpty()) {
            System.out.println("问题不能为空");
            return;
        }
        
        System.out.println("\n正在发送请求...");
        
        try {
            String result = certAi.standardRequest(question).block();
            System.out.println("\n豆包AI回答:");
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("请求失败: " + e.getMessage());
            System.out.println("可能的原因:");
            System.out.println("1. API密钥无效");
            System.out.println("2. 网络连接问题");
            System.out.println("3. API服务不可用");
        }
    }
    
    // 多轮对话测试
    private static void testMultipleRounds(Scanner scanner, CertAi certAi) {
        System.out.println("\n--- 多轮对话测试模式 ---");
        
        List<Map<String, String>> conversation = new ArrayList<>();
        
        // 第一轮对话
        Map<String, String> firstTurn = new HashMap<>();
        firstTurn.put("role", "user");
        firstTurn.put("content", "花椰菜是什么？");
        conversation.add(firstTurn);
        
        Map<String, String> firstResponse = new HashMap<>();
        firstResponse.put("role", "assistant");
        firstResponse.put("content", "花椰菜又称菜花、花菜，是一种常见的蔬菜。");
        conversation.add(firstResponse);
        
        // 第二轮对话
        Map<String, String> secondTurn = new HashMap<>();
        secondTurn.put("role", "user");
        secondTurn.put("content", "再详细点");
        conversation.add(secondTurn);
        
        System.out.println("对话历史:");
        for (Map<String, String> turn : conversation) {
            System.out.println(turn.get("role") + ": " + turn.get("content"));
        }
        
        System.out.println("\n正在发送多轮对话请求...");
        
        try {
            String result = certAi.multipleRoundsRequest(conversation).block();
            System.out.println("\n豆包AI回答:");
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("请求失败: " + e.getMessage());
        }
    }
    
    // 流式请求测试
    private static void testStreamingRequest(Scanner scanner, CertAi certAi) {
        System.out.println("\n--- 流式请求测试模式 ---");
        System.out.print("请输入要询问的问题: ");
        String question = scanner.nextLine().trim();
        
        if (question.isEmpty()) {
            System.out.println("问题不能为空");
            return;
        }
        
        System.out.println("\n正在发送流式请求...");
        System.out.println("----- streaming request -----");
        
        try {
            certAi.streamingRequest(question)
                    .doOnComplete(() -> System.out.println("\n流式响应完成"))
                    .doOnError(error -> System.out.println("流式响应错误: " + error.getMessage()))
                    .blockLast();
        } catch (Exception e) {
            System.out.println("请求失败: " + e.getMessage());
        }
    }
    
    // 预设测试用例
    private static void testPresetCases(CertAi certAi) {
        System.out.println("\n--- 预设测试用例 ---");
        
        String[] testCases = {
            "常见的十字花科植物有哪些？",
            "请分析这个医疗器械认证文档的内容",
            "这个产品是否符合FDA认证要求？",
            "请总结这个CE认证证书的关键信息",
            "分析这个召回通知的风险等级"
        };
        
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("\n测试用例 " + (i + 1) + ":");
            System.out.println("输入: " + testCases[i]);
            
            try {
                String result = certAi.standardRequest(testCases[i]).block();
                System.out.println("输出: " + result);
            } catch (Exception e) {
                System.out.println("测试失败: " + e.getMessage());
            }
            
            if (i < testCases.length - 1) {
                System.out.println("-".repeat(30));
            }
        }
    }
    
    // 检查配置
    private static void checkConfiguration() {
        System.out.println("--- 配置检查 ---");
        
        String apiKey = System.getenv("ARK_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            System.out.println("API密钥: 已从环境变量获取");
        } else {
            System.out.println("API密钥: 使用默认测试密钥（建议设置环境变量 ARK_API_KEY）");
        }
        
        System.out.println("模型ID: bot-20250915145921-rspmk（豆包智能体）");
        System.out.println("API地址: https://ark.cn-beijing.volces.com/api/v3/bots");
        System.out.println("--- 配置检查完成 ---\n");
    }
}