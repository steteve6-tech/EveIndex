package com.certification.analysis.analysisByai;

import com.alibaba.fastjson.JSON;
import com.volcengine.model.request.translate.LangDetectRequest;
import com.volcengine.model.request.translate.TranslateTextRequest;
import com.volcengine.model.response.translate.LangDetectResponse;
import com.volcengine.model.response.translate.TranslateTextResponse;
import com.volcengine.service.translate.ITranslateService;
import com.volcengine.service.translate.impl.TranslateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 火山引擎翻译AI服务
 * 基于火山引擎官方SDK实现，支持语言检测和文本翻译
 */
@Slf4j
@Component
public class TranslateAI {
    
    @Value("${volcengine.translate.access-key:}")
    private String accessKey;
    
    @Value("${volcengine.translate.secret-key:}")
    private String secretKey;
    
    @Value("${volcengine.translate.region:cn-north-1}")
    private String region;
    
    private ITranslateService translateService;
    
    public TranslateAI() {
        this.translateService = TranslateServiceImpl.getInstance();
    }
    
    /**
     * 获取访问密钥
     */
    private String getAccessKey() {
        // 从Spring配置中获取API密钥（通过环境变量注入）
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new RuntimeException("VolcEngine Access Key not configured. Please set VOLCENGINE_ACCESS_KEY environment variable.");
        }
        return accessKey;
    }
    
    /**
     * 获取密钥
     */
    private String getSecretKey() {
        // 从Spring配置中获取API密钥（通过环境变量注入）
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new RuntimeException("VolcEngine Secret Key not configured. Please set VOLCENGINE_SECRET_KEY environment variable.");
        }
        return secretKey;
    }
    
    /**
     * 初始化翻译服务
     */
    private void initializeService() {
        String ak = getAccessKey();
        String sk = getSecretKey();
        
        if (ak != null && !ak.trim().isEmpty() && sk != null && !sk.trim().isEmpty()) {
            translateService.setAccessKey(ak);
            translateService.setSecretKey(sk);
            translateService.setRegion(region);
        }
    }
    
    /**
     * 语言检测
     * @param textList 要检测的文本列表
     * @return 语言检测结果JSON字符串
     */
    public String detectLanguage(List<String> textList) {
        try {
            initializeService();
            
            LangDetectRequest request = new LangDetectRequest();
            request.setTextList(textList);
            
            LangDetectResponse response = translateService.langDetect(request);
            String result = JSON.toJSONString(response);
            
            log.debug("语言检测结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("语言检测失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 翻译文本（英文到中文）
     * @param textList 要翻译的文本列表
     * @return 翻译结果JSON字符串
     */
    public String translateToChinese(List<String> textList) {
        try {
            initializeService();
            
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage("en"); // 源语言：英文
            request.setTargetLanguage("zh"); // 目标语言：中文
            request.setTextList(textList);
            
            TranslateTextResponse response = translateService.translateText(request);
            String result = JSON.toJSONString(response);
            
            log.debug("翻译结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("翻译失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 翻译文本（自动检测源语言到中文）
     * @param textList 要翻译的文本列表
     * @return 翻译结果JSON字符串
     */
    public String translateToChineseAuto(List<String> textList) {
        try {
            initializeService();
            
            TranslateTextRequest request = new TranslateTextRequest();
            // 不设置源语言，表示自动检测
            request.setTargetLanguage("zh"); // 目标语言：中文
            request.setTextList(textList);
            
            TranslateTextResponse response = translateService.translateText(request);
            String result = JSON.toJSONString(response);
            
            log.debug("自动翻译结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("自动翻译失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 翻译单个文本（英文到中文）
     * @param text 要翻译的文本
     * @return 翻译后的文本，失败时返回原文
     */
    public String translateSingleText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            initializeService();
            
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage("en"); // 源语言：英文
            request.setTargetLanguage("zh"); // 目标语言：中文
            request.setTextList(Arrays.asList(text));
            
            TranslateTextResponse response = translateService.translateText(request);
            
            if (response != null && response.getTranslationList() != null && !response.getTranslationList().isEmpty()) {
                return response.getTranslationList().get(0).getTranslation();
            }
        } catch (Exception e) {
            log.error("翻译单个文本失败: {}", e.getMessage(), e);
        }
        
        return text; // 翻译失败时返回原文
    }
    
    /**
     * 翻译单个文本（自动检测源语言到中文）
     * @param text 要翻译的文本
     * @return 翻译后的文本，失败时返回原文
     */
    public String translateSingleTextAuto(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            initializeService();
            
            TranslateTextRequest request = new TranslateTextRequest();
            // 不设置源语言，表示自动检测
            request.setTargetLanguage("zh"); // 目标语言：中文
            request.setTextList(Arrays.asList(text));
            
            TranslateTextResponse response = translateService.translateText(request);
            
            if (response != null && response.getTranslationList() != null && !response.getTranslationList().isEmpty()) {
                return response.getTranslationList().get(0).getTranslation();
            }
        } catch (Exception e) {
            log.error("自动翻译单个文本失败: {}", e.getMessage(), e);
        }
        
        return text; // 翻译失败时返回原文
    }
    
    /**
     * 检查翻译服务是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        try {
            // 检查配置
            String ak = getAccessKey();
            String sk = getSecretKey();
            
            if (ak == null || ak.trim().isEmpty() || sk == null || sk.trim().isEmpty()) {
                log.warn("翻译服务密钥未配置");
                return false;
            }
            
            // 测试翻译
            String testText = "Hello World";
            String result = translateSingleText(testText);
            
            return !testText.equals(result); // 如果翻译结果与原文不同，说明翻译成功
        } catch (Exception e) {
            log.error("翻译服务可用性检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取翻译服务状态
     * @return 服务状态信息
     */
    public String getServiceStatus() {
        try {
            boolean available = isAvailable();
            String ak = getAccessKey();
            String sk = getSecretKey();
            
            return String.format("翻译服务状态: %s, AccessKey: %s, SecretKey: %s, Region: %s", 
                available ? "可用" : "不可用",
                ak != null && !ak.trim().isEmpty() ? "已配置" : "未配置",
                sk != null && !sk.trim().isEmpty() ? "已配置" : "未配置",
                region);
        } catch (Exception e) {
            return "翻译服务状态检查失败: " + e.getMessage();
        }
    }
    
    /**
     * 销毁翻译服务
     */
    public void destroy() {
        try {
            if (translateService != null) {
                translateService.destroy();
            }
        } catch (Exception e) {
            log.error("销毁翻译服务失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        TranslateAI translateAI = new TranslateAI();
        
        try {
            System.out.println("==========================================");
            System.out.println("    火山引擎翻译服务测试程序 (SDK版本)");
            System.out.println("==========================================");
            
            // 检查服务状态
            System.out.println("\n1. 服务状态检查:");
            System.out.println(translateAI.getServiceStatus());
            
            // 测试可用性
            System.out.println("\n2. 服务可用性测试:");
            boolean available = translateAI.isAvailable();
            System.out.println("服务可用性: " + (available ? "✅ 可用" : "❌ 不可用"));
            
            if (!available) {
                System.out.println("⚠️  服务不可用，请检查配置或网络连接");
                return;
            }
            
            // 测试语言检测
            System.out.println("\n3. 语言检测测试:");
            System.out.println("测试文本: [\"hello world\", \"你好世界\", \"bonjour\"]");
            String langResponse = translateAI.detectLanguage(Arrays.asList("hello world", "你好世界", "bonjour"));
            if (langResponse != null) {
                System.out.println("语言检测结果: " + langResponse);
            } else {
                System.out.println("❌ 语言检测失败");
            }
            
            // 测试批量翻译
            System.out.println("\n4. 批量翻译测试:");
            System.out.println("测试文本: [\"hello world\", \"how are you\", \"good morning\"]");
            String translateResponse = translateAI.translateToChinese(Arrays.asList("hello world", "how are you", "good morning"));
            if (translateResponse != null) {
                System.out.println("批量翻译结果: " + translateResponse);
            } else {
                System.out.println("❌ 批量翻译失败");
            }
            
            // 测试单个文本翻译
            System.out.println("\n5. 单个文本翻译测试:");
            String testText = "Hello, this is a test message for translation service.";
            System.out.println("原文: " + testText);
            String singleResult = translateAI.translateSingleText(testText);
            System.out.println("译文: " + singleResult);
            System.out.println("翻译状态: " + (testText.equals(singleResult) ? "❌ 翻译失败" : "✅ 翻译成功"));
            
            // 测试自动翻译
            System.out.println("\n6. 自动翻译测试:");
            String[] testTexts = {
                "Bonjour, comment allez-vous?",  // 法语
                "Hola, ¿cómo estás?",            // 西班牙语
                "Guten Tag, wie geht es dir?",   // 德语
                "こんにちは、元気ですか？"          // 日语
            };
            
            for (String text : testTexts) {
                System.out.println("原文: " + text);
                String autoResult = translateAI.translateSingleTextAuto(text);
                System.out.println("译文: " + autoResult);
                System.out.println("---");
            }
            
            // 测试SGS相关文本翻译
            System.out.println("\n7. SGS相关文本翻译测试:");
            String[] sgsTexts = {
                "SGS provides testing, inspection and certification services",
                "Product safety and quality assurance",
                "Regulatory compliance and standards",
                "Supply chain management solutions"
            };
            
            for (String text : sgsTexts) {
                System.out.println("原文: " + text);
                String result = translateAI.translateSingleText(text);
                System.out.println("译文: " + result);
                System.out.println("---");
            }
            
            System.out.println("\n==========================================");
            System.out.println("    测试完成！");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 销毁服务
            translateAI.destroy();
        }
    }
}
