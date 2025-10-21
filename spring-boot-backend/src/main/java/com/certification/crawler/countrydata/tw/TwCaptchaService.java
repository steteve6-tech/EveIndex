package com.certification.crawler.countrydata.tw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 台湾FDA验证码服务
 * 负责获取、识别和管理验证码
 */
@Slf4j
@Service
public class TwCaptchaService {

    private static final String BASE_URL = "https://lmspiq.fda.gov.tw";
    private static final String CAPTCHA_URL = BASE_URL + "/api/auth/imageCode";
    private static final String MAIN_PAGE_URL = BASE_URL + "/web/MDPIQ/MDPIQLicSearch";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 60000; // 60秒超时
    private static final int MAX_RETRIES = 3; // 最大重试次数
    
    // 验证码缓存，存储 session -> CaptchaInfo
    private final Map<String, CaptchaInfo> captchaCache = new ConcurrentHashMap<>();
    
    // 验证码有效期（毫秒）
    private static final long CAPTCHA_EXPIRY = 5 * 60 * 1000; // 5分钟

    // JSON 解析器
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证码信息
     */
    @Data
    public static class CaptchaInfo {
        private String code;              // 识别出的验证码
        private String verifyCode;        // 验证码哈希
        private Map<String, String> cookies; // Session cookies
        private long timestamp;           // 获取时间戳
        private byte[] imageData;         // 验证码图片数据
        private List<String> savedImagePaths = new ArrayList<>(); // 保存的验证码图片路径列表
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CAPTCHA_EXPIRY;
        }
        
        /**
         * 添加图片路径
         */
        public void addImagePath(String path) {
            if (savedImagePaths == null) {
                savedImagePaths = new ArrayList<>();
            }
            savedImagePaths.add(path);
        }
        
        /**
         * 删除所有保存的验证码图片
         */
        public void deleteAllImages() {
            if (savedImagePaths == null || savedImagePaths.isEmpty()) {
                return;
            }
            
            int deletedCount = 0;
            for (String imagePath : savedImagePaths) {
                try {
                    File file = new File(imagePath);
                    if (file.exists() && file.delete()) {
                        deletedCount++;
                        log.debug("已删除验证码图片: {}", imagePath);
                    }
                } catch (Exception e) {
                    log.warn("删除验证码图片失败: {}", imagePath, e);
                }
            }
            
            if (deletedCount > 0) {
                log.info("🗑️ 已清理 {} 个验证码图片文件", deletedCount);
            }
            savedImagePaths.clear();
        }
    }

    /**
     * 获取新的验证码
     * 
     * @return 验证码信息
     */
    public CaptchaInfo getCaptcha() throws IOException {
        log.info("🔐 开始获取台湾FDA验证码...");
        
        Exception lastException = null;
        
        // 重试机制
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("尝试第 {} 次获取验证码...", attempt);
                return getCaptchaInternal();
            } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
                lastException = e;
                log.warn("获取验证码失败（第{}/{}次）: {} - {}", attempt, MAX_RETRIES, 
                        e.getClass().getSimpleName(), e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        long waitTime = attempt * 2000; // 递增等待时间
                        log.debug("等待 {} 毫秒后重试...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("获取验证码被中断", ie);
                    }
                }
            } catch (IOException e) {
                lastException = e;
                log.error("获取验证码时发生IO异常: {}", e.getMessage());
                throw e;
            }
        }
        
        throw new IOException("获取验证码失败，已重试 " + MAX_RETRIES + " 次", lastException);
    }
    
    /**
     * 内部获取验证码方法
     */
    private CaptchaInfo getCaptchaInternal() throws IOException {
        // 1. 访问主页面获取session
        log.debug("步骤1: 访问主页面获取session");
        Connection.Response mainPage = Jsoup.connect(MAIN_PAGE_URL)
                .userAgent(USER_AGENT)
                .method(Connection.Method.GET)
                .timeout(TIMEOUT)
                .execute();
        
        Map<String, String> cookies = mainPage.cookies();
        log.debug("获取到cookies: {}", cookies.keySet());
        
        // 2. 获取验证码图片和哈希
        log.debug("步骤2: 获取验证码图片");
        Connection.Response captchaResponse = Jsoup.connect(CAPTCHA_URL)
                .userAgent(USER_AGENT)
                .referrer(MAIN_PAGE_URL)
                .header("accept", "*/*")
                .header("accept-language", "zh-CN,zh;q=0.9")
                .header("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .cookies(cookies)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .timeout(TIMEOUT)
                .execute();
        
        // 3. 提取验证码信息
        String responseBody = captchaResponse.body();

        // 解析JSON响应
        byte[] imageData = null;
        String verifyCode = null;

        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            // 提取验证码哈希（在 data.code 字段）
            JsonNode codeNode = jsonResponse.path("data").path("code");
            if (!codeNode.isMissingNode()) {
                verifyCode = codeNode.asText();
                log.debug("✅ 成功提取验证码哈希: {}...", verifyCode.substring(0, Math.min(20, verifyCode.length())));
            }

            // 提取Base64图片数据（在 data.image 字段）
            JsonNode imageNode = jsonResponse.path("data").path("image");
            if (!imageNode.isMissingNode()) {
                String base64Image = imageNode.asText();

                // 移除 data:image/jpeg;base64, 前缀
                if (base64Image.startsWith("data:image")) {
                    base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
                }

                // 解码Base64图片
                imageData = java.util.Base64.getDecoder().decode(base64Image);
                log.debug("✅ 成功解码验证码图片: {} bytes", imageData.length);
            }

        } catch (Exception e) {
            log.error("解析验证码JSON失败: {}", e.getMessage());
            throw new IOException("解析验证码响应失败", e);
        }

        if (imageData == null || verifyCode == null) {
            throw new IOException("验证码图片或哈希提取失败");
        }

        log.debug("验证码图片大小: {} bytes", imageData.length);
        log.debug("验证码哈希: {}", verifyCode.substring(0, Math.min(20, verifyCode.length())) + "...");

        // 4. 创建验证码信息（提前创建，用于记录图片路径）
        CaptchaInfo info = new CaptchaInfo();
        info.setVerifyCode(verifyCode != null ? verifyCode : "");
        info.setCookies(cookies);
        info.setTimestamp(System.currentTimeMillis());
        info.setImageData(imageData);
        
        // 5. 识别验证码（会保存图片并记录路径到info中）
        log.debug("步骤3: 识别验证码");
        String code = recognizeCaptcha(imageData, info);
        
        if (code == null || code.isEmpty()) {
            log.warn("验证码识别失败，返回默认值");
            code = "0000"; // 默认值，实际使用时会失败
        }
        
        info.setCode(code);
        
        log.info("✅ 验证码获取成功: code={}, verifyCode={}", code, 
                verifyCode != null ? verifyCode.substring(0, Math.min(20, verifyCode.length())) + "..." : "null");
        
        // 6. 缓存验证码
        String sessionId = cookies.getOrDefault("JSESSIONID", "default");
        captchaCache.put(sessionId, info);
        
        return info;
    }

    /**
     * 从响应中提取验证码哈希
     *
     * 台湾FDA验证码API返回格式分析：
     * 1. 响应可能是JSON格式，包含verifyCode字段
     * 2. 响应可能是图片二进制，verifyCode在响应头中
     * 3. verifyCode可能在Cookie中
     *
     * 根据实际观察，verifyCode是服务器生成的加密字符串（Base64编码）
     */
    private String extractVerifyCode(Connection.Response response) {
        log.debug("========== 开始提取验证码哈希 ==========");
        log.debug("响应状态码: {}", response.statusCode());
        log.debug("响应Content-Type: {}", response.contentType());

        // 打印所有响应头（调试用）
        log.debug("所有响应头:");
        response.headers().forEach((key, value) ->
            log.debug("  {}: {}", key, value)
        );

        // 方法1: 从响应头获取（常见的验证码哈希头）
        String[] headerNames = {
            "X-Captcha-Hash",
            "X-Verify-Code",
            "X-Captcha-Token",
            "Captcha-Hash",
            "Verify-Code",
            "verifyCode",
            "captcha"
        };

        for (String headerName : headerNames) {
            String verifyCode = response.header(headerName);
            if (verifyCode != null && !verifyCode.isEmpty()) {
                log.info("✅ 从响应头 {} 获取验证码哈希: {}...",
                        headerName,
                        verifyCode.substring(0, Math.min(30, verifyCode.length())));
                return verifyCode;
            }
        }

        // 方法2: 从Cookie获取
        Map<String, String> cookies = response.cookies();
        log.debug("Cookies: {}", cookies.keySet());

        String[] cookieNames = {
            "captcha_hash",
            "verify_code",
            "verifyCode",
            "captcha",
            "captchaHash"
        };

        for (String cookieName : cookieNames) {
            String verifyCode = cookies.get(cookieName);
            if (verifyCode != null && !verifyCode.isEmpty()) {
                log.info("✅ 从Cookie {} 获取验证码哈希: {}...",
                        cookieName,
                        verifyCode.substring(0, Math.min(30, verifyCode.length())));
                return verifyCode;
            }
        }

        // 方法3: 尝试解析响应体（如果是JSON）
        try {
            String body = response.body();
            log.debug("响应体前200字符: {}", body.substring(0, Math.min(200, body.length())));

            // 检查是否是JSON格式
            if (body.startsWith("{") || body.startsWith("[")) {
                try {
                    JsonNode rootNode = objectMapper.readTree(body);

                    // 尝试多种可能的JSON路径
                    String[] jsonPaths = {
                        "verifyCode",
                        "data.verifyCode",
                        "captcha.verifyCode",
                        "result.verifyCode",
                        "hash",
                        "captchaHash"
                    };

                    for (String path : jsonPaths) {
                        String verifyCode = extractJsonValue(rootNode, path);
                        if (verifyCode != null && !verifyCode.isEmpty()) {
                            log.info("✅ 从JSON路径 {} 获取验证码哈希: {}...",
                                    path,
                                    verifyCode.substring(0, Math.min(30, verifyCode.length())));
                            return verifyCode;
                        }
                    }

                    log.debug("JSON响应完整内容: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
                } catch (Exception e) {
                    log.debug("JSON解析失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("解析响应体失败: {}", e.getMessage());
        }

        log.warn("⚠️ 无法提取验证码哈希");
        log.warn("请手动访问网站并检查验证码API的响应格式");
        log.warn("建议: 使用浏览器开发者工具查看 {} 的响应", CAPTCHA_URL);

        return null;
    }

    /**
     * 从JsonNode中提取指定路径的值
     * 支持路径格式：data.verifyCode
     */
    private String extractJsonValue(JsonNode node, String path) {
        try {
            String[] parts = path.split("\\.");
            JsonNode current = node;

            for (String part : parts) {
                if (current == null || current.isNull()) {
                    return null;
                }
                current = current.get(part);
            }

            if (current != null && !current.isNull()) {
                return current.asText();
            }
        } catch (Exception e) {
            log.trace("提取JSON值失败，路径: {}, 错误: {}", path, e.getMessage());
        }
        return null;
    }

    /**
     * 识别验证码
     * 
     * 支持多种识别方法：
     * 1. 简单规则识别（针对简单验证码）
     * 2. OCR识别（需要配置Tesseract）
     * 3. 第三方服务识别（需要配置API密钥）
     * 
     * @param imageData 验证码图片数据
     * @param captchaInfo 验证码信息对象（用于记录保存的图片路径）
     */
    private String recognizeCaptcha(byte[] imageData, CaptchaInfo captchaInfo) {
        try {
            // 保存图片到临时文件（用于调试，并记录路径到captchaInfo）
            saveCaptchaImage(imageData, captchaInfo);
            
            // 方法1: 尝试简单规则识别
            String code = simpleRecognition(imageData);
            if (code != null && !code.isEmpty()) {
                return code;
            }
            
            // 方法2: 尝试OCR识别
            code = ocrRecognition(imageData, captchaInfo);
            if (code != null && !code.isEmpty()) {
                return code;
            }
            
            // 方法3: 尝试第三方服务（如果配置了）
            code = thirdPartyRecognition(imageData);
            if (code != null && !code.isEmpty()) {
                return code;
            }
            
            log.warn("所有验证码识别方法都失败了");
            return null;
            
        } catch (Exception e) {
            log.error("验证码识别异常", e);
            return null;
        }
    }

    /**
     * 简单规则识别
     * 适用于简单的数字验证码
     */
    private String simpleRecognition(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            
            // TODO: 实现简单的图像处理和识别逻辑
            // 1. 二值化
            // 2. 去噪
            // 3. 字符分割
            // 4. 模板匹配
            
            log.debug("简单规则识别未实现，跳过");
            return null;
            
        } catch (Exception e) {
            log.debug("简单规则识别失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * OCR识别
     * 使用Tesseract OCR引擎，支持多种策略
     * 
     * @param imageData 验证码图片数据
     * @param captchaInfo 验证码信息对象（用于记录保存的图片路径）
     */
    private String ocrRecognition(byte[] imageData, CaptchaInfo captchaInfo) {
        try {
            log.debug("开始OCR识别验证码...");

            // 图像预处理
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            BufferedImage processed = preprocessImageForOCR(image, captchaInfo);

            // 尝试多种OCR配置策略
            String[] strategies = {"primary", "psm6", "psm8", "legacy"};

            for (String strategy : strategies) {
                String result = tryOcrWithStrategy(processed, strategy);
                if (result != null && result.length() == 4) {
                    log.info("✅ OCR识别成功（策略: {}）: {}", strategy, result);
                    return result;
                } else if (result != null && result.length() > 4) {
                    result = result.substring(0, 4);
                    log.info("✅ OCR识别成功（策略: {}, 截取前4位）: {}", strategy, result);
                    return result;
                } else if (result != null && result.length() > 0) {
                    log.debug("策略 {} 识别不完整: '{}' ({}位)", strategy, result, result.length());
                }
            }

            // 所有策略都失败
            log.warn("❌ OCR识别失败，所有策略都未能识别完整的4位数字");
            log.warn("建议：");
            log.warn("  1. 查看验证码图片: logs/captcha/captcha_*.png");
            log.warn("  2. 查看预处理图片: logs/captcha/captcha_processed_*.png");
            log.warn("  3. 下载训练数据到 tessdata 目录");
            log.warn("  4. 使用手动输入模式测试");
            return null;

        } catch (UnsatisfiedLinkError e) {
            log.error("❌ Tesseract OCR 库未正确安装");
            log.error("请安装 Tesseract OCR:");
            log.error("  Windows: 下载安装 https://github.com/UB-Mannheim/tesseract/wiki");
            log.error("  Linux: sudo apt-get install tesseract-ocr");
            log.error("  Mac: brew install tesseract");
            return null;
        } catch (Exception e) {
            log.warn("OCR识别异常: {}", e.getMessage());
            log.debug("OCR识别详细错误", e);
            return null;
        }
    }

    /**
     * 使用特定策略尝试OCR识别
     */
    private String tryOcrWithStrategy(BufferedImage processed, String strategy) {
        try {
            net.sourceforge.tess4j.Tesseract tesseract = new net.sourceforge.tess4j.Tesseract();

            // 设置训练数据路径（尝试多个可能的位置）
            String[] possiblePaths = {
                "src/main/resources/tessdata",
                "tessdata",
                System.getProperty("user.dir") + "/tessdata",
                System.getProperty("user.dir") + "/src/main/resources/tessdata"
            };

            for (String path : possiblePaths) {
                File tessdataDir = new File(path);
                if (tessdataDir.exists() && tessdataDir.isDirectory()) {
                    tesseract.setDatapath(path);
                    break;
                }
            }

            // 基础配置
            tesseract.setLanguage("eng");
            tesseract.setVariable("tessedit_char_whitelist", "0123456789");
            tesseract.setVariable("classify_bln_numeric_mode", "1");

            // 根据策略设置不同的参数
            switch (strategy) {
                case "primary":
                    // 策略1: 单行文本 + LSTM引擎（默认）
                    tesseract.setPageSegMode(7);  // 单行文本
                    tesseract.setOcrEngineMode(1);  // LSTM
                    break;

                case "psm6":
                    // 策略2: 单块文本 + LSTM引擎
                    tesseract.setPageSegMode(6);  // 单块文本
                    tesseract.setOcrEngineMode(1);  // LSTM
                    break;

                case "psm8":
                    // 策略3: 单词模式 + LSTM引擎
                    tesseract.setPageSegMode(8);  // 单词
                    tesseract.setOcrEngineMode(1);  // LSTM
                    break;

                case "legacy":
                    // 策略4: 单行文本 + 传统引擎
                    tesseract.setPageSegMode(7);  // 单行文本
                    tesseract.setOcrEngineMode(0);  // 传统引擎
                    break;
            }

            // OCR识别
            String result = tesseract.doOCR(processed);

            // 清理结果（只保留数字）
            result = result.replaceAll("[^0-9]", "").trim();

            log.debug("策略 {} OCR原始结果: '{}'", strategy, result);

            // 后处理：修正常见的OCR错误
            result = postProcessOcrResult(result, processed);

            log.debug("策略 {} OCR修正后结果: '{}'", strategy, result);

            return result.isEmpty() ? null : result;

        } catch (Exception e) {
            log.debug("策略 {} 执行失败: {}", strategy, e.getMessage());
            return null;
        }
    }

    /**
     * 图像预处理，提高OCR识别率
     * 最终方案：先放大 → OTSU自适应二值化 → 去噪
     *
     * 关键：使用OTSU自适应阈值，自动适应不同颜色的验证码
     * 
     * @param original 原始图片
     * @param captchaInfo 验证码信息对象（用于记录保存的图片路径）
     */
    private BufferedImage preprocessImageForOCR(BufferedImage original, CaptchaInfo captchaInfo) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();

            // 步骤1: 先放大原始彩色图像（6倍）
            int scaleFactor = 6;
            int newWidth = width * scaleFactor;
            int newHeight = height * scaleFactor;

            BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                               java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                               java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2.dispose();

            // 步骤2: 转为灰度图
            BufferedImage grayscale = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
            java.awt.Graphics2D g = grayscale.createGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();

            // 步骤3: 使用OTSU算法计算最佳阈值，并适当降低（更严格地过滤噪点）
            int threshold = calculateOtsuThreshold(grayscale);
            // 降低阈值15，使得只有真正暗的像素（数字）被保留，浅色噪点被过滤
            threshold = Math.max(threshold - 15, 128);
            log.debug("OTSU计算的阈值: {} (调整后: {})", calculateOtsuThreshold(grayscale), threshold);

            // 步骤4: 使用调整后的阈值进行二值化
            BufferedImage binary = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_BINARY);
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    int gray = (grayscale.getRGB(x, y) >> 16) & 0xFF;

                    // 使用调整后的阈值二值化
                    // 数字通常比背景暗，所以 < threshold 的是文字
                    int newRgb = gray < threshold ? 0x000000 : 0xFFFFFF;
                    binary.setRGB(x, y, newRgb);
                }
            }

            // 步骤5: 形态学开运算（增强去噪：先腐蚀2次后膨胀2次）
            binary = morphologicalOpen(binary, 2);

            // 步骤6: 添加白色边框
            int borderSize = 20;
            BufferedImage bordered = new BufferedImage(
                newWidth + 2 * borderSize,
                newHeight + 2 * borderSize,
                BufferedImage.TYPE_BYTE_BINARY
            );
            java.awt.Graphics2D gBorder = bordered.createGraphics();
            gBorder.setColor(java.awt.Color.WHITE);
            gBorder.fillRect(0, 0, bordered.getWidth(), bordered.getHeight());
            gBorder.drawImage(binary, borderSize, borderSize, null);
            gBorder.dispose();

            log.debug("图像预处理完成: 放大{}倍 → 灰度化 → OTSU二值化(阈值:{}) → 形态学去噪 → 添加边框",
                     scaleFactor, threshold);

            // 保存预处理后的图像用于调试（并记录路径）
            saveDebugImage(bordered, "processed", captchaInfo);

            return bordered;

        } catch (Exception e) {
            log.warn("图像预处理失败，使用原始图像: {}", e.getMessage());
            return original;
        }
    }

    /**
     * 形态学开运算：先腐蚀后膨胀
     * 用于去除小噪点，保留字符主体
     */
    private BufferedImage morphologicalOpen(BufferedImage image) {
        return morphologicalOpen(image, 1);
    }

    /**
     * 形态学开运算：先腐蚀后膨胀（可配置迭代次数）
     * @param iterations 腐蚀和膨胀的迭代次数
     */
    private BufferedImage morphologicalOpen(BufferedImage image, int iterations) {
        // 腐蚀（去除小噪点）
        BufferedImage eroded = morphologicalErode(image, iterations);
        // 膨胀（恢复字符大小）
        BufferedImage dilated = morphologicalDilate(eroded, iterations);
        return dilated;
    }

    /**
     * 形态学腐蚀
     */
    private BufferedImage morphologicalErode(BufferedImage image, int iterations) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = image;

        for (int iter = 0; iter < iterations; iter++) {
            BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    // 检查3x3邻域，全部是黑色才设为黑色
                    boolean allBlack = true;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int rgb = result.getRGB(x + dx, y + dy);
                            if ((rgb & 0xFF) > 128) {  // 白色
                                allBlack = false;
                                break;
                            }
                        }
                        if (!allBlack) break;
                    }
                    temp.setRGB(x, y, allBlack ? 0x000000 : 0xFFFFFF);
                }
            }
            result = temp;
        }
        return result;
    }

    /**
     * 形态学膨胀
     */
    private BufferedImage morphologicalDilate(BufferedImage image, int iterations) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = image;

        for (int iter = 0; iter < iterations; iter++) {
            BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    // 检查3x3邻域，有任何黑色就设为黑色
                    boolean hasBlack = false;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            int rgb = result.getRGB(x + dx, y + dy);
                            if ((rgb & 0xFF) < 128) {  // 黑色
                                hasBlack = true;
                                break;
                            }
                        }
                        if (hasBlack) break;
                    }
                    temp.setRGB(x, y, hasBlack ? 0x000000 : 0xFFFFFF);
                }
            }
            result = temp;
        }
        return result;
    }

    /**
     * 对比度增强
     * 拉伸灰度值到0-255的完整范围
     */
    private BufferedImage enhanceContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 找到最小和最大灰度值
        int min = 255;
        int max = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (image.getRGB(x, y) >> 16) & 0xFF;
                if (gray < min) min = gray;
                if (gray > max) max = gray;
            }
        }

        log.debug("对比度增强 - 原始范围: [{}, {}]", min, max);

        // 拉伸到[0, 255]
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int range = max - min;
        if (range == 0) range = 1;  // 避免除以0

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (image.getRGB(x, y) >> 16) & 0xFF;
                // 线性拉伸公式
                int newGray = (int) (((gray - min) * 255.0) / range);
                newGray = Math.max(0, Math.min(255, newGray));

                int rgb = (newGray << 16) | (newGray << 8) | newGray;
                enhanced.setRGB(x, y, rgb);
            }
        }

        return enhanced;
    }

    /**
     * 反色处理（如果需要）
     * 确保文字是深色（接近黑色），背景是浅色（接近白色）
     */
    private BufferedImage invertIfNeeded(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 计算平均灰度值
        long sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (image.getRGB(x, y) >> 16) & 0xFF;
                sum += gray;
            }
        }
        double avgGray = (double) sum / (width * height);

        log.debug("图像平均灰度: {:.1f}", avgGray);

        // 如果平均灰度很低（<128），说明整体偏暗，可能需要反色
        // 但台湾验证码通常是浅色背景，所以一般不需要反色
        // 直接返回原图
        return image;
    }

    /**
     * OCR结果后处理
     * 直接返回OCR原始结果，不做修正
     */
    private String postProcessOcrResult(String result, BufferedImage image) {
        // 直接返回原始结果，不做任何修正
        return result;
    }


    /**
     * 中值滤波去噪
     * 类似于 PIL 的 ImageFilter.MedianFilter()
     * 用于去除椒盐噪点，同时保留字符边缘
     */
    private BufferedImage morphologicalProcessing(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 3x3 中值滤波
        BufferedImage filtered = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // 收集3x3邻域的所有像素值
                int[] neighbors = new int[9];
                int idx = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        neighbors[idx++] = rgb & 0xFF;  // 获取灰度值
                    }
                }

                // 对9个值进行排序
                java.util.Arrays.sort(neighbors);

                // 取中值（第5个元素，索引4）
                int median = neighbors[4];

                // 设置为中值（黑色0或白色255）
                int newRgb = median > 127 ? 0xFFFFFF : 0x000000;
                filtered.setRGB(x, y, newRgb);
            }
        }

        // 处理边缘像素（直接复制）
        for (int x = 0; x < width; x++) {
            filtered.setRGB(x, 0, image.getRGB(x, 0));
            filtered.setRGB(x, height - 1, image.getRGB(x, height - 1));
        }
        for (int y = 0; y < height; y++) {
            filtered.setRGB(0, y, image.getRGB(0, y));
            filtered.setRGB(width - 1, y, image.getRGB(width - 1, y));
        }

        return filtered;
    }

    /**
     * 智能检测文字颜色
     * 通过分析图像的灰度分布，判断文字是暗色还是亮色
     *
     * @param grayscale 灰度图像
     * @param threshold OTSU计算的阈值
     * @return true=文字较暗（常见黑字白底），false=文字较亮（罕见白字黑底）
     */
    private boolean detectTextColor(BufferedImage grayscale, int threshold) {
        int width = grayscale.getWidth();
        int height = grayscale.getHeight();

        // 统计低于阈值和高于阈值的像素数量
        int darkPixels = 0;  // 暗色像素（< threshold）
        int lightPixels = 0; // 亮色像素（>= threshold）

        // 计算平均灰度值
        long darkSum = 0;
        long lightSum = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (grayscale.getRGB(x, y) >> 16) & 0xFF;

                if (gray < threshold) {
                    darkPixels++;
                    darkSum += gray;
                } else {
                    lightPixels++;
                    lightSum += gray;
                }
            }
        }

        // 计算两组的平均灰度
        double darkAvg = darkPixels > 0 ? (double) darkSum / darkPixels : 0;
        double lightAvg = lightPixels > 0 ? (double) lightSum / lightPixels : 255;

        // 判断逻辑：
        // 验证码通常是少量文字 + 大量背景
        // - 如果暗色像素较少，则暗色是文字 → 文字较暗 → 返回true
        // - 如果亮色像素较少，则亮色是文字 → 文字较亮 → 返回false
        //
        // 额外验证：检查两组的灰度差异，确保它们确实代表前景和背景

        double pixelRatio = (double) darkPixels / (darkPixels + lightPixels);
        double grayDiff = Math.abs(lightAvg - darkAvg);

        log.debug("颜色检测 - 暗像素占比: {:.1f}%, 暗色均值: {:.0f}, 亮色均值: {:.0f}, 灰度差: {:.0f}",
                 pixelRatio * 100, darkAvg, lightAvg, grayDiff);

        // 判断规则：
        // 1. 如果暗色像素占比在 20%-45% 之间，通常是文字（文字占少数）
        // 2. 如果暗色像素太少（<15%）或太多（>55%），可能需要反转
        if (pixelRatio >= 0.15 && pixelRatio <= 0.55) {
            // 正常情况：暗色是文字，亮色是背景
            return true;
        } else if (pixelRatio < 0.15) {
            // 暗色像素很少，可能是噪点或文字太细
            // 保守判断：仍然认为暗色是文字
            return true;
        } else {
            // 暗色像素太多（>55%），可能是暗背景 + 亮文字
            return false;
        }
    }

    /**
     * 保存调试图像
     * @param image 图像对象
     * @param suffix 文件名后缀
     * @param captchaInfo 验证码信息（用于记录图片路径）
     */
    private void saveDebugImage(BufferedImage image, String suffix, CaptchaInfo captchaInfo) {
        try {
            String filename = "captcha_" + suffix + "_" + System.currentTimeMillis() + ".png";
            File outputDir = new File("logs/captcha");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, filename);
            ImageIO.write(image, "png", outputFile);
            
            // 记录图片路径
            if (captchaInfo != null) {
                captchaInfo.addImagePath(outputFile.getAbsolutePath());
            }
            
            log.debug("调试图像已保存: {}", outputFile.getAbsolutePath());
        } catch (Exception e) {
            log.debug("保存调试图像失败: {}", e.getMessage());
        }
    }

    /**
     * 计算OTSU最佳阈值
     */
    private int calculateOtsuThreshold(BufferedImage grayscale) {
        int width = grayscale.getWidth();
        int height = grayscale.getHeight();
        int totalPixels = width * height;

        // 统计灰度直方图
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (grayscale.getRGB(x, y) >> 16) & 0xFF;
                histogram[gray]++;
            }
        }

        // 计算总和
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float maxVariance = 0;
        int threshold = 0;

        // 遍历所有可能的阈值
        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += (float) (t * histogram[t]);

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            // 计算类间方差
            float variance = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }

        return threshold;
    }

    /**
     * 第三方服务识别
     * 使用2Captcha、Anti-Captcha等服务
     */
    private String thirdPartyRecognition(byte[] imageData) {
        try {
            // TODO: 集成第三方验证码识别服务
            // 例如：2Captcha API
            
            /*
            String apiKey = System.getenv("CAPTCHA_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                log.debug("未配置第三方验证码服务API密钥");
                return null;
            }
            
            // 调用第三方API
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            // ... 发送请求到第三方服务
            // ... 等待识别结果
            */
            
            log.debug("第三方服务识别未配置，跳过");
            return null;
            
        } catch (Exception e) {
            log.debug("第三方服务识别失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 保存验证码图片到文件（用于调试和训练）
     * @param imageData 图片数据
     * @param captchaInfo 验证码信息（用于记录图片路径）
     */
    private void saveCaptchaImage(byte[] imageData, CaptchaInfo captchaInfo) {
        try {
            String filename = "captcha_" + System.currentTimeMillis() + ".png";
            File outputDir = new File("logs/captcha");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            File outputFile = new File(outputDir, filename);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            ImageIO.write(image, "png", outputFile);
            
            // 记录图片路径
            if (captchaInfo != null) {
                captchaInfo.addImagePath(outputFile.getAbsolutePath());
            }
            
            log.debug("验证码图片已保存: {}", outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            log.debug("保存验证码图片失败: {}", e.getMessage());
        }
    }

    /**
     * 手动设置验证码
     * 用于测试或手动输入验证码
     */
    public CaptchaInfo createManualCaptcha(String code, String verifyCode) {
        log.info("手动设置验证码: code={}, verifyCode={}", code, 
                verifyCode.substring(0, Math.min(20, verifyCode.length())) + "...");
        
        CaptchaInfo info = new CaptchaInfo();
        info.setCode(code);
        info.setVerifyCode(verifyCode);
        info.setCookies(new HashMap<>());
        info.setTimestamp(System.currentTimeMillis());
        
        return info;
    }

    /**
     * 获取缓存的验证码
     */
    public CaptchaInfo getCachedCaptcha(String sessionId) {
        CaptchaInfo info = captchaCache.get(sessionId);
        if (info != null && info.isExpired()) {
            captchaCache.remove(sessionId);
            return null;
        }
        return info;
    }

    /**
     * 清除过期的验证码缓存
     */
    public void cleanExpiredCaptcha() {
        captchaCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 获取验证码统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", captchaCache.size());
        stats.put("expiredCount", captchaCache.values().stream()
                .filter(CaptchaInfo::isExpired)
                .count());
        return stats;
    }

    /**
     * 手动输入模式
     * 获取验证码图片，等待用户手动输入
     */
    public CaptchaInfo getManualInputCaptcha() throws IOException {
        log.info("🖐️ 启动手动输入模式...");
        
        // 获取验证码图片
        CaptchaInfo info = getCaptcha();
        
        // 保存图片
        String filename = "captcha_manual_" + System.currentTimeMillis() + ".png";
        File outputFile = new File("logs/captcha", filename);
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(info.getImageData()));
        ImageIO.write(image, "png", outputFile);
        
        log.info("📸 验证码图片已保存到: {}", outputFile.getAbsolutePath());
        log.info("📝 请打开图片并手动输入验证码");
        log.info("💡 提示：可以调用 updateManualCaptchaCode(code) 方法更新验证码");
        
        return info;
    }

    /**
     * 更新手动输入的验证码
     */
    public void updateManualCaptchaCode(CaptchaInfo info, String code) {
        info.setCode(code);
        log.info("✅ 验证码已更新: {}", code);
    }

    /**
     * 测试验证码获取功能
     * 用于调试和验证验证码服务是否正常工作
     *
     * @return 测试结果信息
     */
    public String testCaptchaService() {
        StringBuilder result = new StringBuilder();
        result.append("========== 台湾FDA验证码服务测试 ==========\n");

        try {
            log.info("开始测试验证码获取...");
            result.append("步骤1: 访问主页面获取session\n");

            // 获取验证码
            CaptchaInfo captcha = getCaptcha();

            result.append("步骤2: 获取验证码图片和哈希\n");
            result.append(String.format("  - 验证码Code: %s\n", captcha.getCode()));
            result.append(String.format("  - 验证码VerifyCode: %s...\n",
                    captcha.getVerifyCode() != null ?
                    captcha.getVerifyCode().substring(0, Math.min(50, captcha.getVerifyCode().length())) :
                    "null"));
            result.append(String.format("  - 图片大小: %d bytes\n",
                    captcha.getImageData() != null ? captcha.getImageData().length : 0));
            result.append(String.format("  - Cookies数量: %d\n",
                    captcha.getCookies() != null ? captcha.getCookies().size() : 0));

            result.append("\n步骤3: 保存验证码图片\n");
            if (captcha.getImageData() != null) {
                String filename = "captcha_test_" + System.currentTimeMillis() + ".png";
                File outputDir = new File("logs/captcha");
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                File outputFile = new File(outputDir, filename);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(captcha.getImageData()));
                ImageIO.write(image, "png", outputFile);

                result.append(String.format("  - 图片已保存: %s\n", outputFile.getAbsolutePath()));
            }

            result.append("\n✅ 验证码服务测试完成\n");
            result.append("\n建议步骤：\n");
            result.append("1. 查看保存的验证码图片\n");
            result.append("2. 检查 verifyCode 是否成功提取\n");
            result.append("3. 如果 verifyCode 为 null，请手动访问网站查看 API 响应格式\n");
            result.append("4. 配置OCR或手动输入验证码进行实际测试\n");

            return result.toString();

        } catch (Exception e) {
            log.error("验证码服务测试失败", e);
            result.append(String.format("\n❌ 测试失败: %s\n", e.getMessage()));
            result.append(String.format("详细错误: %s\n", e.toString()));
            return result.toString();
        }
    }

    /**
     * 调试验证码API响应
     * 详细打印API返回的所有信息，用于分析响应格式
     */
    public String debugCaptchaApi() {
        StringBuilder debug = new StringBuilder();
        debug.append("========== 验证码API调试信息 ==========\n");

        try {
            log.info("开始调试验证码API...");

            // 1. 访问主页
            debug.append("步骤1: 访问主页面\n");
            debug.append(String.format("  URL: %s\n", MAIN_PAGE_URL));

            Connection.Response mainPage = Jsoup.connect(MAIN_PAGE_URL)
                    .userAgent(USER_AGENT)
                    .method(Connection.Method.GET)
                    .timeout(30000)
                    .execute();

            debug.append(String.format("  状态码: %d\n", mainPage.statusCode()));
            debug.append(String.format("  Cookies: %s\n", mainPage.cookies()));
            debug.append("\n");

            // 2. 获取验证码
            debug.append("步骤2: 获取验证码\n");
            debug.append(String.format("  URL: %s\n", CAPTCHA_URL));

            Connection.Response captchaResponse = Jsoup.connect(CAPTCHA_URL)
                    .userAgent(USER_AGENT)
                    .referrer(MAIN_PAGE_URL)
                    .cookies(mainPage.cookies())
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .timeout(30000)
                    .execute();

            debug.append(String.format("  状态码: %d\n", captchaResponse.statusCode()));
            debug.append(String.format("  Content-Type: %s\n", captchaResponse.contentType()));
            debug.append(String.format("  Content-Length: %d bytes\n", captchaResponse.bodyAsBytes().length));

            debug.append("\n  响应头:\n");
            captchaResponse.headers().forEach((key, value) ->
                debug.append(String.format("    %s: %s\n", key, value))
            );

            debug.append("\n  响应Cookies:\n");
            captchaResponse.cookies().forEach((key, value) ->
                debug.append(String.format("    %s: %s\n", key, value))
            );

            // 尝试解析响应体
            debug.append("\n  响应体:\n");
            byte[] bodyBytes = captchaResponse.bodyAsBytes();

            // 如果是JSON
            try {
                String bodyStr = new String(bodyBytes, "UTF-8");
                if (bodyStr.startsWith("{")) {
                    debug.append("    格式: JSON\n");
                    JsonNode json = objectMapper.readTree(bodyStr);
                    debug.append(String.format("    内容:\n%s\n",
                            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)));
                } else {
                    debug.append("    格式: 二进制图片\n");
                    debug.append(String.format("    大小: %d bytes\n", bodyBytes.length));
                }
            } catch (Exception e) {
                debug.append(String.format("    解析失败: %s\n", e.getMessage()));
            }

            debug.append("\n✅ 调试信息收集完成\n");
            debug.append("\n分析建议:\n");
            debug.append("1. 检查响应Content-Type是否为 application/json 或 image/png\n");
            debug.append("2. 如果是JSON，查找 verifyCode 字段\n");
            debug.append("3. 如果是图片，查找响应头中的验证码哈希\n");
            debug.append("4. 如果都没有，可能需要先登录或获取特殊token\n");

            return debug.toString();

        } catch (Exception e) {
            log.error("调试验证码API失败", e);
            debug.append(String.format("\n❌ 调试失败: %s\n", e.getMessage()));
            return debug.toString();
        }
    }
}

