package com.certification.analysis.analysisByai;

import com.volcengine.ApiClient;
import com.volcengine.ApiException;
import com.volcengine.sign.Credentials;
import com.volcengine.translate20250301.Translate20250301Api;
import com.volcengine.translate20250301.model.TranslateTextRequest;
import com.volcengine.translate20250301.model.TranslateTextResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * 火山引擎翻译服务（基于新版API：translate20250301）
 * 提供文本翻译、语言检测等功能
 */
@Slf4j
@Component
public class TranslateAI {

    @Value("${volcengine.translate.access-key:}")
    private String accessKey;

    @Value("${volcengine.translate.secret-key:}")
    private String secretKey;

    @Value("${volcengine.translate.region:}")
    private String region;

    private Translate20250301Api translateApi;
    private ApiClient apiClient;
    private boolean serviceAvailable = false;  // 服务是否可用
    private boolean signatureError = false;     // 是否遇到签名错误

    public TranslateAI() {
        // 构造函数中不初始化，等待Spring注入配置
    }

    /**
     * Spring初始化后执行（确保@Value已注入）
     */
    @PostConstruct
    public void init() {
        initializeService();
    }

    /**
     * 初始化翻译服务API客户端
     */
    private void initializeService() {
        try {
            // 检查密钥是否配置
            if (accessKey == null || accessKey.trim().isEmpty() ||
                secretKey == null || secretKey.trim().isEmpty()) {
                log.warn("火山引擎翻译服务未配置密钥，翻译功能将不可用（如需使用，请参考：火山引擎翻译配置说明.md）");
                serviceAvailable = false;
                return;
            }

            // 创建ApiClient实例（使用新版API方式）
            apiClient = new ApiClient()
                    .setCredentials(Credentials.getCredentials(accessKey, secretKey))
                    .setRegion(region);

            // 创建翻译API实例
            translateApi = new Translate20250301Api(apiClient);

            log.info("火山引擎翻译服务初始化完成，区域：{}", region);
            serviceAvailable = true;
        } catch (Exception e) {
            log.error("翻译服务初始化失败！", e);
            translateApi = null;
            serviceAvailable = false;
        }
    }

    /**
     * 翻译文本到中文
     *
     * @param textList 待翻译的文本列表
     * @param sourceLanguage 源语言代码（如：en, ja, ko）
     * @return 翻译结果列表
     */
    public List<String> translateToChinese(List<String> textList, String sourceLanguage) {
        return translateText(textList, sourceLanguage, "zh");
    }

    /**
     * 翻译文本（通用方法）
     *
     * @param textList 待翻译的文本列表
     * @param sourceLanguage 源语言代码（如：en, ja, ko）
     * @param targetLanguage 目标语言代码（如：zh, en）
     * @return 翻译结果列表
     */
    public List<String> translateText(List<String> textList, String sourceLanguage, String targetLanguage) {
        // 如果遇到签名错误，直接返回原文，不再尝试调用API
        if (signatureError) {
            log.debug("翻译服务签名错误，已禁用，返回原文");
            return textList;
        }

        if (!serviceAvailable || translateApi == null) {
            log.debug("翻译服务未初始化或不可用，返回原文");
            return textList;
        }

        if (textList == null || textList.isEmpty()) {
            return textList;
        }

        try {
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage(sourceLanguage);
            request.setTargetLanguage(targetLanguage);
            request.setTextList(textList);  // 使用setTextList传入列表

            TranslateTextResponse response = translateApi.translateText(request);

            if (response != null) {
                log.debug("批量翻译API响应: translationList size={}",
                    response.getTranslationList() != null ? response.getTranslationList().size() : 0);

                if (response.getTranslationList() != null && !response.getTranslationList().isEmpty()) {
                    List<String> translations = new ArrayList<>();
                    for (var item : response.getTranslationList()) {
                        String translation = item.getTranslation();
                        translations.add(translation != null ? translation : "");
                    }

                    log.debug("批量翻译成功，翻译了 {} 个文本", translations.size());
                    return translations;
                } else {
                    log.warn("批量翻译列表为空，textList size={}", textList.size());
                    return textList;
                }
            } else {
                log.warn("批量翻译API响应为null");
                return textList;
            }

        } catch (ApiException e) {
            // 检查是否是签名错误
            if (isSignatureError(e)) {
                signatureError = true;
                serviceAvailable = false;
                log.error("火山引擎翻译服务签名验证失败！密钥配置错误，已禁用翻译功能。请检查配置文件或查看：火山引擎翻译配置说明.md");
                log.error("错误详情：错误码={}, 错误信息={}", e.getCode(), e.getMessage());
            } else {
                log.error("批量翻译API异常！源语言={}, 目标语言={}, 错误码={}, 错误信息={}",
                    sourceLanguage, targetLanguage, e.getCode(), e.getMessage());
            }
            return textList;
        } catch (Exception e) {
            log.error("批量翻译异常！源语言={}, 目标语言={}",
                sourceLanguage, targetLanguage, e);
            return textList;
        }
    }

    /**
     * 翻译单个文本到中文
     *
     * @param text 待翻译的文本
     * @param sourceLanguage 源语言代码（如：en, ja, ko）
     * @return 翻译后的文本，失败时返回原文
     */
    public String translateSingleText(String text, String sourceLanguage) {
        return translateSingleText(text, sourceLanguage, "zh");
    }

    /**
     * 翻译单个文本（通用方法）
     *
     * @param text 待翻译的文本
     * @param sourceLanguage 源语言代码
     * @param targetLanguage 目标语言代码
     * @return 翻译后的文本，失败时返回原文
     */
    public String translateSingleText(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // 如果遇到签名错误，直接返回原文
        if (signatureError) {
            log.debug("翻译服务签名错误，已禁用，返回原文");
            return text;
        }

        if (!serviceAvailable || translateApi == null) {
            log.debug("翻译服务未初始化或不可用，返回原文");
            return text;
        }

        try {
            TranslateTextRequest request = new TranslateTextRequest();
            request.setSourceLanguage(sourceLanguage);
            request.setTargetLanguage(targetLanguage);
            request.setTextList(Arrays.asList(text));  // 单个文本也使用列表

            TranslateTextResponse response = translateApi.translateText(request);

            if (response != null) {
                log.debug("API响应: response={}, translationList={}",
                    response, response.getTranslationList());

                if (response.getTranslationList() != null && !response.getTranslationList().isEmpty()) {
                    String translation = response.getTranslationList().get(0).getTranslation();
                    if (translation != null && !translation.trim().isEmpty()) {
                        log.debug("翻译成功：{} -> {}", text, translation);
                        return translation;
                    } else {
                        log.warn("翻译结果为空字符串，text={}", text);
                    }
                } else {
                    log.warn("翻译列表为空，response={}, text={}", response, text);
                }
            } else {
                log.warn("API响应为null，text={}", text);
            }
        } catch (ApiException e) {
            // 检查是否是签名错误
            if (isSignatureError(e)) {
                signatureError = true;
                serviceAvailable = false;
                log.error("火山引擎翻译服务签名验证失败！密钥配置错误，已禁用翻译功能。");
                log.error("请检查以下配置项：");
                log.error("  1. volcengine.translate.access-key");
                log.error("  2. volcengine.translate.secret-key");
                log.error("  3. volcengine.translate.region (当前: {})", region);
                log.error("详细说明请查看：spring-boot-backend/火山引擎翻译配置说明.md");
            } else {
                log.warn("单文本翻译API异常！text={}, 源语言={}, 目标语言={}, 错误码={}, 错误信息={}",
                    text, sourceLanguage, targetLanguage, e.getCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("单文本翻译异常！text={}, 源语言={}, 目标语言={}",
                text, sourceLanguage, targetLanguage, e);
        }
        return text;
    }

    /**
     * 判断是否是签名错误
     */
    private boolean isSignatureError(ApiException e) {
        if (e.getResponseBody() != null) {
            String responseBody = e.getResponseBody();
            return responseBody.contains("SignatureDoesNotMatch") ||
                   responseBody.contains("签名") ||
                   responseBody.contains("signature");
        }
        return false;
    }

    /**
     * 翻译并追加到原文后（格式：原文Translation）
     *
     * @param text 原文
     * @param sourceLanguage 源语言代码
     * @return 原文+翻译
     */
    public String translateAndAppend(String text, String sourceLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String translation = translateSingleText(text, sourceLanguage, "en");

        // 如果翻译成功且不等于原文，则追加
        if (translation != null && !translation.equals(text)) {
            return text + translation;
        }

        return text;
    }

    /**
     * 检查翻译服务是否可用
     *
     * @return true表示服务可用，false表示不可用
     */
    public boolean isAvailable() {
        if (translateApi == null) {
            return false;
        }

        try {
            // 执行简单的翻译测试
            String testText = "Hello";
            String result = translateSingleText(testText, "en", "zh");
            boolean available = result != null && !testText.equals(result);

            if (available) {
                log.info("火山引擎翻译服务可用");
            } else {
                log.warn("火山引擎翻译服务不可用");
            }

            return available;
        } catch (Exception e) {
            log.error("服务可用性检查失败！", e);
            return false;
        }
    }

    /**
     * 手动设置密钥（用于测试）
     */
    public void setCredentials(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        initializeService();
    }
}
