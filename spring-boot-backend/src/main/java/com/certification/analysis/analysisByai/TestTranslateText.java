package com.certification.analysis.analysisByai;

import com.volcengine.ApiClient;
import com.volcengine.ApiException;
import com.volcengine.sign.Credentials;
import com.volcengine.translate20250301.Translate20250301Api;
import com.volcengine.translate20250301.model.TranslateTextRequest;
import com.volcengine.translate20250301.model.TranslateTextResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 火山引擎翻译API测试
 * 基于新版API：translate20250301
 */
public class TestTranslateText {
    public static void main(String[] args) {
        // 从环境变量读取密钥
        String ak = System.getenv("VOLCENGINE_ACCESS_KEY");
        String sk = System.getenv("VOLCENGINE_SECRET_KEY");
        String region = System.getenv().getOrDefault("VOLCENGINE_REGION", "cn-beijing");

        if (ak == null || sk == null) {
            System.err.println("错误：请设置环境变量 VOLCENGINE_ACCESS_KEY 和 VOLCENGINE_SECRET_KEY");
            System.err.println("使用方法：");
            System.err.println("  export VOLCENGINE_ACCESS_KEY=your_access_key");
            System.err.println("  export VOLCENGINE_SECRET_KEY=your_secret_key");
            System.exit(1);
        }

        System.out.println("=== 火山引擎翻译API测试 ===\n");

        try {
            // 初始化客户端
            ApiClient apiClient = new ApiClient()
                    .setCredentials(Credentials.getCredentials(ak, sk))
                    .setRegion(region);
            Translate20250301Api api = new Translate20250301Api(apiClient);

            System.out.println("✓ API客户端初始化成功");
            System.out.println("区域: " + region + "\n");

            // ========== 测试1：单文本翻译 ==========
            System.out.println("【测试1】单文本翻译（英文→中文）");
            testSingleTranslation(api, "Hello, this is a test.", "en", "zh");

            // ========== 测试2：批量翻译 ==========
            System.out.println("\n【测试2】批量翻译（英文→中文）");
            testBatchTranslation(api);

            // ========== 测试3：日文翻译 ==========
            System.out.println("\n【测试3】日文翻译（日文→中文）");
            testSingleTranslation(api, "こんにちは", "ja", "zh");

            // ========== 测试4：韩文翻译 ==========
            System.out.println("\n【测试4】韩文翻译（韩文→中文）");
            testSingleTranslation(api, "안녕하세요", "ko", "zh");

            System.out.println("\n=== 所有测试完成 ===");

        } catch (Exception e) {
            System.err.println("✗ 初始化失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试单文本翻译
     */
    private static void testSingleTranslation(Translate20250301Api api, String text, 
                                             String sourceLang, String targetLang) {
        try {
            // 创建请求（注意：使用setTextList，传入列表）
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage(sourceLang);
            request.setTargetLanguage(targetLang);
            request.setTextList(Arrays.asList(text));  // 重要：使用setTextList

            // 调用API
            TranslateTextResponse response = api.translateText(request);

            // 获取结果
            if (response != null && response.getTranslationList() != null 
                && !response.getTranslationList().isEmpty()) {
                
                String translation = response.getTranslationList().get(0).getTranslation();
                
                // 打印结果
                System.out.println("  原文: " + text);
                System.out.println("  译文: " + translation);
                
                // 打印元数据（如果有）
                if (response.getResponseMetadata() != null) {
                    System.out.println("  请求ID: " + response.getResponseMetadata().getRequestId());
                }
                
                System.out.println("  ✓ 翻译成功");
            } else {
                System.out.println("  ✗ 未获取到翻译结果");
            }

        } catch (ApiException e) {
            System.err.println("  ✗ API错误");
            System.err.println("  错误码: " + e.getCode());
            System.err.println("  错误信息: " + e.getMessage());
            if (e.getResponseBody() != null) {
                System.err.println("  响应内容: " + e.getResponseBody());
            }
        } catch (Exception e) {
            System.err.println("  ✗ 其他错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 测试批量翻译
     */
    private static void testBatchTranslation(Translate20250301Api api) {
        try {
            // 准备批量文本
            List<String> texts = Arrays.asList(
                "Medical Device",
                "Quality Management",
                "Clinical Trial",
                "Risk Assessment"
            );

            // 创建请求
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage("en");
            request.setTargetLanguage("zh");
            request.setTextList(texts);  // 批量翻译

            // 调用API
            TranslateTextResponse response = api.translateText(request);

            // 获取结果
            if (response != null && response.getTranslationList() != null) {
                System.out.println("  批量翻译结果：");
                for (int i = 0; i < texts.size(); i++) {
                    String original = texts.get(i);
                    String translated = response.getTranslationList().get(i).getTranslation();
                    System.out.printf("  %d. %s → %s\n", i + 1, original, translated);
                }
                System.out.println("  ✓ 批量翻译成功");
            } else {
                System.out.println("  ✗ 未获取到翻译结果");
            }

        } catch (ApiException e) {
            System.err.println("  ✗ API错误");
            System.err.println("  错误码: " + e.getCode());
            System.err.println("  错误信息: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("  ✗ 其他错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
