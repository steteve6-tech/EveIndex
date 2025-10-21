package com.certification.crawler.countrydata.tw;

import com.certification.analysis.analysisByai.TranslateAI;
import com.certification.entity.common.DeviceRegistrationRecord;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.repository.common.DeviceRegistrationRecordRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * 台湾FDA医疗器械注册记录爬虫
 * 数据源: https://lmspiq.fda.gov.tw/api/public/sh/piq/1000/search
 * 
 * ⚠️ 重要说明：
 * 台湾FDA API需要验证码才能访问数据。当前实现使用模拟验证码，
 * 因此会返回500错误。要正常使用此爬虫，需要：
 * 
 * 1. 手动获取验证码：
 *    访问 https://lmspiq.fda.gov.tw/web/MDPIQ/MDPIQLicSearch
 *    获取页面上的验证码图片和验证码哈希值
 * 
 * 2. 实现自动验证码识别：
 *    - 使用OCR库（如Tesseract）识别验证码图片
 *    - 或使用第三方验证码识别服务
 * 
 * 3. 获取API密钥：
 *    联系台湾FDA申请API访问密钥（如果有提供）
 * 
 * 当前状态：爬虫框架已完成，等待验证码功能实现
 */
@Slf4j
@Component
public class TwRegistration {

    private static final String BASE_URL = "https://lmspiq.fda.gov.tw/api/public/sh/piq/1000/search";
    private static final String REFERER_URL = "https://lmspiq.fda.gov.tw/web/MDPIQ/MDPIQLicSearch";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final int TIMEOUT = 30000;
    private static final int MAX_RETRIES = 3;
    private static final int BATCH_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 100;

    @Autowired
    private DeviceRegistrationRecordRepository registrationRepository;

    @Autowired
    private TranslateAI translateAI;

    @Autowired
    private TwCaptchaService captchaService;

    /**
     * 台湾注册数据模型
     * 字段说明（根据台湾FDA API响应和MuiDataGrid列位置）：
     * - licenseNumber: 许可证编号
     * - productNamePart1: 产品名称第1部分（第4列）
     * - productNamePart2: 产品名称第2部分（第5列）
     * - productNameChinese: 中文产品名称（合并第4、5列）
     * - productNameEnglish: 英文产品名称
     * - brandName: 品牌名称（第6列）
     * - manufacturerName: 制造商（第7列）
     * - applicantName: 申请人/公司名称
     * - factoryName: 工厂名称
     * - efficacy: 功效/用途
     * - status: 许可证状态
     * - issueDate: 发证日期
     * - expiryDate: 有效期（第3列）
     * - deviceCategory: 医疗器械类别
     * - detailUrl: 详情页面URL
     */
    @Data
    public static class TaiwanRegistrationData {
        private String licenseNumber;        // 许可证编号
        private String productNamePart1;     // 产品名称第1部分（第4列）
        private String productNamePart2;     // 产品名称第2部分（第5列）
        private String productNameChinese;   // 中文产品名称（合并后）
        private String productNameEnglish;   // 英文产品名称
        private String brandName;            // 品牌名称（第6列）
        private String manufacturerName;     // 制造商（第7列）
        private String applicantName;        // 申请人/公司名称
        private String factoryName;          // 工厂名称
        private String efficacy;             // 功效/用途
        private String status;               // 许可证状态
        private String issueDate;            // 发证日期
        private String expiryDate;           // 有效期（第3列）
        private String deviceCategory;       // 医疗器械类别
        private String detailUrl;            // 详情页面URL
    }

    /**
     * 综合搜索爬取
     * 支持4个参数的任意组合：applicantName, factoryName, prodNameC, prodNameE
     * 
     * @param applicantName 申请人名称
     * @param factoryName 制造商/工厂名称
     * @param prodNameC 中文产品名称
     * @param prodNameE 英文产品名称
     * @param maxRecords 最大记录数（默认100）
     * @return 爬取结果消息
     */
    public String crawl(String applicantName, String factoryName, String prodNameC, String prodNameE, int maxRecords) {
        log.info("📝 开始爬取台湾FDA注册记录");
        log.info("  - 申请人: {}", applicantName != null && !applicantName.isEmpty() ? applicantName : "(未指定)");
        log.info("  - 制造商: {}", factoryName != null && !factoryName.isEmpty() ? factoryName : "(未指定)");
        log.info("  - 中文产品名: {}", prodNameC != null && !prodNameC.isEmpty() ? prodNameC : "(未指定)");
        log.info("  - 英文产品名: {}", prodNameE != null && !prodNameE.isEmpty() ? prodNameE : "(未指定)");
        log.info("  - 最大记录数: {}", maxRecords);
        
        TwCaptchaService.CaptchaInfo captcha = null;
        
        try {
            // 构建搜索参数
            Map<String, Object> searchParams = buildSearchParams(
                applicantName != null ? applicantName : "",
                factoryName != null ? factoryName : "",
                prodNameC != null ? prodNameC : "",
                prodNameE != null ? prodNameE : "",
                maxRecords
            );
            
            // 获取验证码信息（用于后续清理）
            captcha = (TwCaptchaService.CaptchaInfo) searchParams.get("captcha");
            
            // 执行搜索
            List<TaiwanRegistrationData> dataList = executeSearch(searchParams);
            
            if (dataList.isEmpty()) {
                log.warn("未找到任何注册记录");
                return "未找到任何注册记录";
            }
            
            log.info("📊 台湾注册记录爬取完成，共获取 {} 条数据", dataList.size());
            
            // 保存到数据库
            String result = saveToDatabase(dataList);
            
            // 爬取完成，删除验证码图片
            if (captcha != null) {
                captcha.deleteAllImages();
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("爬取台湾注册记录失败", e);
            
            // 即使失败也删除验证码图片
            if (captcha != null) {
                captcha.deleteAllImages();
            }
            
            return "爬取失败: " + e.getMessage();
        }
    }
    
    /**
     * 兼容旧接口 - 按申请人搜索
     */
    @Deprecated
    public String crawlByApplicant(String applicantName, int maxRecords) {
        return crawl(applicantName, "", "", "", maxRecords);
    }
    
    /**
     * 兼容旧接口 - 按英文产品名搜索
     */
    @Deprecated
    public String crawlByProductNameEnglish(String productName, int maxRecords) {
        return crawl("", "", "", productName, maxRecords);
    }
    
    /**
     * 兼容旧接口 - 按中文产品名搜索
     */
    @Deprecated
    public String crawlByProductNameChinese(String productName, int maxRecords) {
        return crawl("", "", productName, "", maxRecords);
    }
    
    /**
     * 兼容旧接口 - 组合搜索
     */
    @Deprecated
    public String crawlByCombined(String applicantName, String productNameEnglish, String productNameChinese, int maxRecords) {
        return crawl(applicantName, "", productNameChinese, productNameEnglish, maxRecords);
    }

    /**
     * 构建搜索参数（包含验证码信息）
     * 
     * @param applicantName 申请人名称
     * @param factoryName 制造商/工厂名称
     * @param prodNameC 中文产品名称
     * @param prodNameE 英文产品名称
     * @param maxRecords 最大记录数
     * @return 搜索参数Map
     */
    private Map<String, Object> buildSearchParams(String applicantName, String factoryName, String prodNameC, String prodNameE, int maxRecords) {
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("licUnit", "2");                    // 许可单位类型
        data.put("licid", "");                       // 许可证编号（空值）
        data.put("status", "1");                     // 状态（1=有效）
        data.put("prodNameC", prodNameC);            // 中文产品名称
        data.put("prodNameE", prodNameE);            // 英文产品名称
        data.put("restraintItemsCode", new ArrayList<>()); // 限制项目代码（空数组）
        data.put("efficacy", "");                    // 功效（空值）
        data.put("applicantName", applicantName);    // 申请人名称
        data.put("factoryName", factoryName);        // 制造商/工厂名称
        data.put("sortWay", "2");                    // 排序方式（2=按某种排序）
        data.put("mdKindMCode", null);               // 医疗器械大类代码（空值）
        data.put("mdKindDCode", null);               // 医疗器械细类代码（空值）
        data.put("lickid", "");                      // 许可证ID（空值）

        // 🔐 验证码相关（使用TwCaptchaService自动获取和识别）
        Map<String, Object> code = new HashMap<>();
        TwCaptchaService.CaptchaInfo captcha = null;

        try {
            log.info("🔐 开始获取和识别验证码...");
            captcha = captchaService.getCaptcha();

            code.put("code", captcha.getCode());                    // 验证码
            code.put("verifyCode", captcha.getVerifyCode());        // 验证码校验哈希

            log.info("✅ 验证码获取成功: code={}, verifyCode={}...",
                    captcha.getCode(),
                    captcha.getVerifyCode() != null && !captcha.getVerifyCode().isEmpty()
                    ? captcha.getVerifyCode().substring(0, Math.min(20, captcha.getVerifyCode().length())) + "..."
                    : "null");

            // 如果验证码识别失败（返回默认值0000），给出警告
            if ("0000".equals(captcha.getCode())) {
                log.warn("⚠️  验证码自动识别失败，使用默认值");
                log.warn("这会导致API请求失败！");
                log.warn("建议：");
                log.warn("  1. 查看 logs/captcha/ 目录中的验证码图片");
                log.warn("  2. 下载 eng.traineddata 到 tessdata 目录提高识别率");
                log.warn("  3. 或使用手动输入模式");
            }

        } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
            log.error("❌ 网络连接失败，无法访问台湾FDA网站");
            log.error("可能的原因：");
            log.error("  1. 网络不稳定或被防火墙阻止");
            log.error("  2. 台湾FDA网站服务器繁忙");
            log.error("  3. 需要使用代理服务器访问");
            log.error("建议：");
            log.error("  1. 检查网络连接");
            log.error("  2. 稍后重试");
            log.error("  3. 配置代理服务器（如果在中国大陆）");
            throw new RuntimeException("网络连接超时，无法访问台湾FDA网站: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ 获取验证码失败: {}", e.getMessage(), e);
            log.warn("使用默认验证码（注意：这会导致API请求失败）");
            code.put("code", "0000");                    // 默认验证码（会失败）
            code.put("verifyCode", "");                  // 默认验证码校验
        }

        data.put("code", code);

        Map<String, Object> page = new HashMap<>();
        page.put("page", 1);                         // 页码
        page.put("pageSize", DEFAULT_PAGE_SIZE);     // 每页记录数

        params.put("data", data);
        params.put("page", page);
        params.put("maxRecords", maxRecords);  // 保存最大记录数限制
        params.put("captcha", captcha);  // 保存验证码信息（用于后续请求中的Cookie）

        return params;
    }

    /**
     * 执行搜索请求（使用验证码的Session Cookie）
     * 支持验证码错误时自动重试
     * 根据maxRecords限制获取的数据量
     */
    private List<TaiwanRegistrationData> executeSearch(Map<String, Object> searchParams) throws IOException {
        List<TaiwanRegistrationData> allData = new ArrayList<>();

        Map<String, Object> data = (Map<String, Object>) searchParams.get("data");
        Map<String, Object> page = (Map<String, Object>) searchParams.get("page");
        int maxRecords = (Integer) searchParams.get("maxRecords");
        TwCaptchaService.CaptchaInfo captcha = (TwCaptchaService.CaptchaInfo) searchParams.get("captcha");

        // 验证码重试配置
        final int MAX_CAPTCHA_RETRIES = 7;  // 最大重试次数
        final int RETRY_DELAY_MS = 2000;     // 重试间隔（毫秒）

        int currentPage = 1;

        // 持续请求直到：1) 获取足够数据 2) 没有更多数据 3) 达到合理的页数上限
        // maxRecords = -1 表示不限制数量，获取所有数据
        while ((maxRecords == -1 || allData.size() < maxRecords) && currentPage <= 100) {  // 最多100页防止无限循环
            // 更新页码
            page.put("page", currentPage);

            boolean pageSuccess = false;
            int captchaRetryCount = 0;

            // 验证码重试循环
            while (!pageSuccess && captchaRetryCount < MAX_CAPTCHA_RETRIES) {
                try {
                    // 获取Session Cookies（验证码获取时的Cookie）
                    Map<String, String> cookies = new HashMap<>();
                    if (captcha != null && captcha.getCookies() != null) {
                        cookies = captcha.getCookies();
                        log.debug("使用验证码Session Cookies: {}", cookies.keySet());
                    } else {
                        log.warn("⚠️  没有验证码Cookie，请求可能失败");
                    }

                    // 构建请求体
                    String requestBody = buildRequestBody(data, page);

                    if (captchaRetryCount > 0) {
                        log.info("🔄 验证码重试 {}/{}, 页码: {}", captchaRetryCount, MAX_CAPTCHA_RETRIES, currentPage);
                    } else {
                        log.debug("发送请求到台湾FDA API，页码: {}", currentPage);
                    }
                    log.debug("请求体: {}", requestBody);

                    // 🌐 发送HTTP请求（重要：使用验证码的Session Cookie）
                    Connection.Response response = Jsoup.connect(BASE_URL)
                            .userAgent(USER_AGENT)
                            .referrer(REFERER_URL)
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Accept-Language", "zh-CN,zh;q=0.9")
                            .header("Content-Type", "application/json")
                            .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                            .header("Sec-Ch-Ua-Mobile", "?0")
                            .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                            .header("Sec-Fetch-Dest", "empty")
                            .header("Sec-Fetch-Mode", "cors")
                            .header("Sec-Fetch-Site", "same-origin")
                            .cookies(cookies)  // ← 关键：使用验证码的Session Cookie
                            .requestBody(requestBody)
                            .method(Connection.Method.POST)
                            .timeout(TIMEOUT)
                            .ignoreContentType(true)
                            .execute();

                    // 检查响应状态码
                    if (response.statusCode() == 422) {
                        // 验证码错误，需要重试
                        captchaRetryCount++;
                        log.warn("⚠️  验证码错误 (422)，尝试重新获取验证码... (重试 {}/{})",
                                captchaRetryCount, MAX_CAPTCHA_RETRIES);

                        if (captchaRetryCount >= MAX_CAPTCHA_RETRIES) {
                            log.error("❌ 验证码重试次数已达上限 ({}次)，放弃该页请求", MAX_CAPTCHA_RETRIES);
                            break;
                        }

                        // 等待一段时间后重新获取验证码
                        Thread.sleep(RETRY_DELAY_MS);

                        // 删除旧的验证码图片
                        if (captcha != null) {
                            captcha.deleteAllImages();
                            log.debug("已删除旧验证码图片");
                        }

                        // 重新获取验证码
                        log.info("🔐 重新获取验证码...");
                        captcha = captchaService.getCaptcha();

                        // 更新验证码信息到data中
                        Map<String, Object> codeMap = new HashMap<>();
                        codeMap.put("code", captcha.getCode());
                        codeMap.put("verifyCode", captcha.getVerifyCode());
                        data.put("code", codeMap);

                        log.info("✅ 新验证码获取成功: code={}", captcha.getCode());

                        continue;  // 继续重试
                    }

                    if (response.statusCode() == 500) {
                        log.error("❌ 台湾FDA API返回500错误 - 验证码验证失败");
                        log.error("台湾FDA API需要真实的验证码才能访问数据");
                        log.error("当前使用的是模拟验证码，无法通过服务器验证");
                        log.error("解决方案：");
                        log.error("  1. 手动访问 https://lmspiq.fda.gov.tw 获取验证码");
                        log.error("  2. 实现自动验证码识别（OCR）功能");
                        log.error("  3. 或者联系台湾FDA获取API密钥");
                        throw new RuntimeException("台湾FDA API需要真实验证码，当前无法自动获取。请参考日志中的解决方案。");
                    }

                    if (response.statusCode() != 200) {
                        log.warn("台湾FDA API请求失败，状态码: {}, 页码: {}", response.statusCode(), currentPage);
                        break;  // 非验证码错误，跳过该页
                    }

                    // 解析响应
                    List<TaiwanRegistrationData> pageData = parseApiResponse(response.body());

                    // 检查是否需要截断数据（达到maxRecords限制）
                    // maxRecords = -1 表示不限制，获取所有数据
                    if (maxRecords != -1) {
                        int remainingCount = maxRecords - allData.size();
                        if (pageData.size() > remainingCount) {
                            // 只添加需要的数量
                            pageData = pageData.subList(0, remainingCount);
                            log.debug("第 {} 页获取到数据已超过限制，截取前 {} 条", currentPage, remainingCount);
                        }
                    }

                    allData.addAll(pageData);

                    log.debug("第 {} 页获取到 {} 条数据，当前总计 {} 条", currentPage, pageData.size(), allData.size());

                    pageSuccess = true;  // 标记该页成功

                    // 检查是否已经获取足够数据（仅当maxRecords != -1时检查）
                    if (maxRecords != -1 && allData.size() >= maxRecords) {
                        log.info("已获取足够数据 ({}/{}), 停止请求", allData.size(), maxRecords);
                        return allData;
                    }
                    
                    // 如果返回的数据少于页面大小，说明已经是最后一页
                    if (pageData.size() < DEFAULT_PAGE_SIZE) {
                        log.info("已到达最后一页，总计获取 {} 条数据", allData.size());
                        return allData;  // 直接返回，不再请求下一页
                    }

                } catch (org.jsoup.HttpStatusException e) {
                    if (e.getStatusCode() == 422) {
                        // 422错误（验证码错误）
                        captchaRetryCount++;
                        log.warn("⚠️  验证码错误 (422 Exception)，尝试重新获取验证码... (重试 {}/{})",
                                captchaRetryCount, MAX_CAPTCHA_RETRIES);

                        if (captchaRetryCount >= MAX_CAPTCHA_RETRIES) {
                            log.error("❌ 验证码重试次数已达上限 ({}次)，放弃该页请求", MAX_CAPTCHA_RETRIES);
                            break;
                        }

                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }

                        // 删除旧的验证码图片
                        if (captcha != null) {
                            captcha.deleteAllImages();
                            log.debug("已删除旧验证码图片");
                        }

                        // 重新获取验证码
                        log.info("🔐 重新获取验证码...");
                        captcha = captchaService.getCaptcha();

                        // 更新验证码信息
                        Map<String, Object> codeMap = new HashMap<>();
                        codeMap.put("code", captcha.getCode());
                        codeMap.put("verifyCode", captcha.getVerifyCode());
                        data.put("code", codeMap);

                        log.info("✅ 新验证码获取成功: code={}", captcha.getCode());

                        continue;  // 继续重试
                    } else {
                        // 其他HTTP错误
                        throw e;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("验证码重试被中断");
                    break;
                }
            }

            // 如果该页失败，记录警告后继续下一页
            if (!pageSuccess) {
                log.warn("⚠️  第 {} 页请求失败，跳过该页继续", currentPage);
            }

            // 移动到下一页
            currentPage++;
            
            // 添加延迟避免请求过于频繁
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("分页循环结束，总计获取 {} 条数据", allData.size());
        return allData;
    }

    /**
     * 构建请求体JSON
     */
    private String buildRequestBody(Map<String, Object> data, Map<String, Object> page) {
        // 简单的JSON构建（实际项目中建议使用Jackson或Gson）
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"data\":{");
        json.append("\"licUnit\":\"").append(data.get("licUnit")).append("\",");
        json.append("\"licid\":\"").append(data.get("licid")).append("\",");
        json.append("\"status\":\"").append(data.get("status")).append("\",");
        json.append("\"prodNameC\":\"").append(data.get("prodNameC")).append("\",");
        json.append("\"prodNameE\":\"").append(data.get("prodNameE")).append("\",");
        json.append("\"restraintItemsCode\":[],");
        json.append("\"efficacy\":\"").append(data.get("efficacy")).append("\",");
        json.append("\"applicantName\":\"").append(data.get("applicantName")).append("\",");
        json.append("\"factoryName\":\"").append(data.get("factoryName")).append("\",");
        json.append("\"sortWay\":\"").append(data.get("sortWay")).append("\",");
        json.append("\"mdKindMCode\":null,");
        json.append("\"mdKindDCode\":null,");
        json.append("\"lickid\":\"").append(data.get("lickid")).append("\",");
        
        // 验证码部分
        Map<String, Object> code = (Map<String, Object>) data.get("code");
        json.append("\"code\":{");
        json.append("\"code\":\"").append(code.get("code")).append("\",");
        json.append("\"verifyCode\":\"").append(code.get("verifyCode")).append("\"");
        json.append("}");
        
        json.append("},");
        json.append("\"page\":{");
        json.append("\"page\":").append(page.get("page")).append(",");
        json.append("\"pageSize\":").append(page.get("pageSize"));
        json.append("}");
        json.append("}");
        
        return json.toString();
    }

    /**
     * 解析API响应
     * 实际API返回格式：{"data": [...], "page": {...}, "response": {...}}
     * 字段说明：
     * - licid: 许可证编号
     * - prodNameC: 中文产品名称（包含品牌，如："醫樺"皮膚電極護具）
     * - prodNameE: 英文产品名称（包含品牌，如："Everyway"Garment Electrodes）
     * - validDate: 有效日期（如：2028-05-30）
     * - applicantName: 申请人名称
     * - factoryName: 制造商/工厂名称
     * - status: 状态（1=有效）
     */
    private List<TaiwanRegistrationData> parseApiResponse(String responseBody) {
        List<TaiwanRegistrationData> dataList = new ArrayList<>();
        
        try {
            log.debug("解析台湾FDA API响应，响应体长度: {}", responseBody.length());
            
            // 实际API返回格式：{"data": [...], "page": {...}}
            if (responseBody.contains("\"data\"")) {
                // 提取data数组内容
                int dataStart = responseBody.indexOf("\"data\":");
                if (dataStart == -1) {
                    log.warn("未找到data字段");
                    return dataList;
                }
                
                int arrayStart = responseBody.indexOf("[", dataStart);
                if (arrayStart == -1) {
                    log.warn("data不是数组类型");
                    return dataList;
                }
                
                int arrayEnd = findMatchingBracket(responseBody, arrayStart);
                if (arrayEnd == -1) {
                    log.warn("无法定位data数组结束位置");
                    return dataList;
                }
                
                String dataContent = responseBody.substring(arrayStart + 1, arrayEnd);
                
                // 分割对象
                List<String> objects = splitJsonObjects(dataContent);
                
                log.info("解析到 {} 条记录", objects.size());
                
                for (String objStr : objects) {
                    try {
                        TaiwanRegistrationData data = parseJsonObject(objStr);
                        if (data != null) {
                            // 从产品名称中提取品牌名
                            extractBrandFromProductName(data);
                            
                            dataList.add(data);
                            log.debug("成功解析记录: 许可证号={}, 产品名={}, 品牌={}, 制造商={}, 有效期={}", 
                                    data.getLicenseNumber(), 
                                    data.getProductNameChinese(),
                                    data.getBrandName(),
                                    data.getManufacturerName(),
                                    data.getExpiryDate());
                        }
                    } catch (Exception e) {
                        log.warn("解析单条记录失败: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("解析台湾FDA API响应失败", e);
        }
        
        return dataList;
    }
    
    /**
     * 从产品名称中提取品牌名
     * 格式示例：
     * - "醫樺"皮膚電極護具 → 品牌：醫樺，产品：皮膚電極護具
     * - "愛惜康"得美棒皮膚黏膠劑 → 品牌：愛惜康，产品：得美棒皮膚黏膠劑
     * - 昭惠皮膚牽引帶(未滅菌) → 品牌：昭惠，产品：皮膚牽引帶(未滅菌)
     */
    private void extractBrandFromProductName(TaiwanRegistrationData data) {
        // 处理中文产品名
        if (data.getProductNameChinese() != null && !data.getProductNameChinese().isEmpty()) {
            String name = data.getProductNameChinese();
            
            // 情况1: 带中文引号的品牌名 "品牌"产品名
            // 检查中文引号 " 和 "
            if (name.indexOf('\u201c') != -1 && name.indexOf('\u201d') != -1) {
                int startQuote = name.indexOf('\u201c');
                int endQuote = name.indexOf('\u201d', startQuote + 1);
                
                if (startQuote != -1 && endQuote != -1 && endQuote > startQuote) {
                    String brand = name.substring(startQuote + 1, endQuote);
                    String product = name.substring(endQuote + 1).trim();
                    
                    data.setBrandName(brand);
                    data.setProductNamePart1(product);  // 去除品牌后的产品名
                    
                    log.debug("提取品牌: {} | 产品: {}", brand, product);
                }
            }
            // 情况2: 没有引号，尝试提取第一个词作为品牌（通常是公司名+产品类型）
            else {
                // 尝试找到第一个常见的产品类型关键词
                String[] keywords = {"皮膚", "醫療", "外科", "手術", "診斷", "檢測", "治療", "器材"};
                for (String keyword : keywords) {
                    int keywordIndex = name.indexOf(keyword);
                    if (keywordIndex > 0) {
                        String possibleBrand = name.substring(0, keywordIndex).trim();
                        // 确保品牌名不会太长（通常不超过10个字）
                        if (possibleBrand.length() <= 10 && possibleBrand.length() >= 2) {
                            data.setBrandName(possibleBrand);
                            data.setProductNamePart1(name.substring(keywordIndex).trim());
                            log.debug("推测品牌: {} | 产品: {}", possibleBrand, name.substring(keywordIndex).trim());
                            break;
                        }
                    }
                }
            }
        }
        
        // 处理英文产品名（类似逻辑）
        if (data.getProductNameEnglish() != null && !data.getProductNameEnglish().isEmpty()) {
            String name = data.getProductNameEnglish();
            
            // 情况1: 带引号的品牌名 "BRAND"Product Name
            if (name.contains("\"")) {
                int startQuote = name.indexOf("\"");
                int endQuote = name.indexOf("\"", startQuote + 1);
                
                if (startQuote != -1 && endQuote != -1 && endQuote > startQuote) {
                    String brand = name.substring(startQuote + 1, endQuote);
                    String product = name.substring(endQuote + 1).trim();
                    
                    // 如果中文品牌名为空，使用英文品牌名
                    if (data.getBrandName() == null || data.getBrandName().isEmpty()) {
                        data.setBrandName(brand);
                    }
                    data.setProductNamePart2(product);
                    
                    log.debug("提取英文品牌: {} | 产品: {}", brand, product);
                }
            }
        }
    }
    
    /**
     * 查找匹配的括号位置
     */
    private int findMatchingBracket(String str, int start) {
        int count = 1;
        for (int i = start + 1; i < str.length(); i++) {
            if (str.charAt(i) == '[') count++;
            if (str.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return -1;
    }
    
    /**
     * 分割JSON数组中的对象
     */
    private List<String> splitJsonObjects(String content) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        boolean inString = false;
        char prevChar = ' ';
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            // 处理字符串内的引号
            if (c == '"' && prevChar != '\\') {
                inString = !inString;
            }
            
            if (!inString) {
                if (c == '{') {
                    if (braceCount == 0) start = i;
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        objects.add(content.substring(start, i + 1).trim());
                    }
                }
            }
            
            prevChar = c;
        }
        
        return objects;
    }
    
    /**
     * 解析单个JSON对象
     * 根据实际API返回的字段映射
     */
    private TaiwanRegistrationData parseJsonObject(String jsonStr) {
        TaiwanRegistrationData data = new TaiwanRegistrationData();
        
        try {
            // 提取字段值（使用实际API字段名）
            data.setLicenseNumber(extractJsonValue(jsonStr, "licid"));  // 许可证编号
            data.setProductNameChinese(extractJsonValue(jsonStr, "prodNameC"));  // 中文产品名（包含品牌）
            data.setProductNameEnglish(extractJsonValue(jsonStr, "prodNameE"));  // 英文产品名（包含品牌）
            data.setExpiryDate(extractJsonValue(jsonStr, "validDate"));  // 有效日期
            data.setManufacturerName(extractJsonValue(jsonStr, "factoryName"));  // 制造商/工厂名称
            data.setApplicantName(extractJsonValue(jsonStr, "applicantName"));  // 申请人名称
            data.setStatus(extractJsonValue(jsonStr, "status"));  // 状态
            
            // 其他可能的字段
            data.setIssueDate(extractJsonValue(jsonStr, "licDate"));  // 发证日期（如果有）
            
            // 从restraintItemsDescList中提取国产/进口信息（作为deviceCategory）
            if (jsonStr.contains("restraintItemsDescList")) {
                if (jsonStr.contains("R01 國 產")) {
                    data.setDeviceCategory("国产");
                } else if (jsonStr.contains("R02 輸 入")) {
                    data.setDeviceCategory("进口");
                }
            }
            
            return data;
            
        } catch (Exception e) {
            log.warn("解析JSON对象失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JSON字符串中提取字段值
     */
    private String extractJsonValue(String jsonStr, String fieldName) {
        String searchPattern = "\"" + fieldName + "\"";
        int fieldIndex = jsonStr.indexOf(searchPattern);
        
        if (fieldIndex == -1) {
            return "";
        }
        
        // 找到冒号位置
        int colonIndex = jsonStr.indexOf(":", fieldIndex);
        if (colonIndex == -1) {
            return "";
        }
        
        // 跳过空格
        int valueStart = colonIndex + 1;
        while (valueStart < jsonStr.length() && Character.isWhitespace(jsonStr.charAt(valueStart))) {
            valueStart++;
        }
        
        if (valueStart >= jsonStr.length()) {
            return "";
        }
        
        // 判断值类型
        char firstChar = jsonStr.charAt(valueStart);
        
        if (firstChar == '"') {
            // 字符串值
            int valueEnd = valueStart + 1;
            while (valueEnd < jsonStr.length()) {
                if (jsonStr.charAt(valueEnd) == '"' && jsonStr.charAt(valueEnd - 1) != '\\') {
                    break;
                }
                valueEnd++;
            }
            return jsonStr.substring(valueStart + 1, valueEnd);
        } else if (firstChar == 'n') {
            // null值
            return "";
        } else {
            // 数字或其他
            int valueEnd = valueStart;
            while (valueEnd < jsonStr.length() && 
                   jsonStr.charAt(valueEnd) != ',' && 
                   jsonStr.charAt(valueEnd) != '}' && 
                   jsonStr.charAt(valueEnd) != ']') {
                valueEnd++;
            }
            return jsonStr.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * 保存数据到数据库
     */
    private String saveToDatabase(List<TaiwanRegistrationData> dataList) {
        if (dataList.isEmpty()) {
            return "没有数据需要保存";
        }

        int totalSaved = 0;
        int totalDuplicates = 0;

        // 初始化批次检测器
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);

        // 分批保存
        for (int i = 0; i < dataList.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, dataList.size());
            List<TaiwanRegistrationData> batch = dataList.subList(i, endIndex);

            int batchSaved = 0;

            for (TaiwanRegistrationData data : batch) {
                try {
                    // 检查是否已存在
                    Optional<DeviceRegistrationRecord> existing = registrationRepository
                            .findByRegistrationNumberAndDataSource(data.getLicenseNumber(), "台湾FDA 食品药物管理署");

                    if (existing.isPresent()) {
                        totalDuplicates++;
                        log.debug("记录已存在，跳过: {}", data.getLicenseNumber());
                        continue;
                    }

                    // 转换为实体对象
                    DeviceRegistrationRecord entity = convertToEntity(data);

                    // 保存到数据库
                    registrationRepository.save(entity);
                    totalSaved++;
                    batchSaved++;

                    log.debug("保存台湾注册记录: {}", data.getLicenseNumber());

                } catch (Exception e) {
                    log.error("保存台湾注册记录失败: {}", data.getLicenseNumber(), e);
                }
            }

            // 批次检测：检查是否应该停止
            boolean shouldStop = detector.recordBatch(batch.size(), batchSaved);
            if (shouldStop) {
                log.warn("⚠️ 检测到连续重复批次，停止保存剩余数据");
                break;
            }
        }

        // 打印最终统计
        detector.printFinalStats("TwRegistration");

        log.info("保存完成，新增: {} 条，重复: {} 条", totalSaved, totalDuplicates);
        return String.format("台湾注册数据保存完成，新增: %d 条，重复: %d 条", totalSaved, totalDuplicates);
    }

    /**
     * 转换为实体对象
     * 字段映射说明（根据实际API数据）：
     * - licid → registrationNumber 注册编号
     * - productNameChinese（原始）+ productNamePart1（去除品牌） → deviceName 设备名称
     * - productNameEnglish + productNamePart2（去除品牌） → proprietaryName 专有名称
     * - brandName（从产品名提取） → remark 备注（品牌信息）
     * - factoryName → manufacturerName 制造商名称
     * - validDate → remark 备注（有效期）
     * - applicantName → remark 备注（申请人信息）
     * - deviceCategory（国产/进口） → remark 备注
     * - status → dataStatus 数据状态
     * 
     * 注意：不进行翻译，直接保存原始中文数据
     */
    private DeviceRegistrationRecord convertToEntity(TaiwanRegistrationData src) {
        DeviceRegistrationRecord entity = new DeviceRegistrationRecord();
        
        // 基本信息
        entity.setRegistrationNumber(src.getLicenseNumber());
        entity.setDataSource("台湾FDA 食品药物管理署");
        entity.setJdCountry("TW");
        
        // 产品信息 - 优先使用去除品牌后的产品名，如果没有则使用完整名称（不翻译）
        String deviceName = src.getProductNamePart1();  // 去除品牌后的产品名
        if (deviceName == null || deviceName.trim().isEmpty()) {
            deviceName = src.getProductNameChinese();  // 如果没有提取，使用完整名称
        }
        entity.setDeviceName(deviceName);  // 直接保存，不翻译
        
        // 英文产品名 - 优先使用去除品牌后的英文名（不翻译）
        String proprietaryName = src.getProductNamePart2();  // 去除品牌后的英文名
        if (proprietaryName == null || proprietaryName.trim().isEmpty()) {
            proprietaryName = src.getProductNameEnglish();  // 如果没有提取，使用完整英文名
        }
        if (proprietaryName != null && !proprietaryName.trim().isEmpty()) {
            entity.setProprietaryName(proprietaryName);  // 直接保存，不翻译
        }
        
        // 制造商信息 - factoryName（不翻译）
        if (src.getManufacturerName() != null && !src.getManufacturerName().trim().isEmpty()) {
            entity.setManufacturerName(src.getManufacturerName());  // 直接保存，不翻译
        } else if (src.getApplicantName() != null && !src.getApplicantName().trim().isEmpty()) {
            // 如果制造商为空，使用申请人作为制造商
            entity.setManufacturerName(src.getApplicantName());  // 直接保存，不翻译
        }
        
        // 设置默认值
        entity.setRiskLevel(RiskLevel.MEDIUM);
        entity.setKeywords(null);
        entity.setDataStatus("ACTIVE");
        entity.setJdCountry("TW");
        entity.setCrawlTime(LocalDateTime.now());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        return entity;
    }

    /**
     * 翻译文本（如果需要）
     */
    private String translateIfNeeded(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 如果文本包含中文字符，尝试翻译为英文
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            try {
                String translated = translateAI.translateSingleText(text, "zh", "en");
                if (translated != null && !translated.equals(text)) {
                    log.debug("翻译完成: {} -> {}", text, translated);
                    return translated;
                }
            } catch (Exception e) {
                log.warn("翻译失败，使用原文: {}", text, e);
            }
        }
        
        return text;
    }

    /**
     * 获取验证码（模拟实现）
     * 实际使用时需要：
     * 1. 访问验证码生成页面
     * 2. 下载验证码图片
     * 3. 使用OCR识别验证码
     * 4. 返回验证码字符串
     */
    private String getVerificationCode() {
        // TODO: 实现验证码获取逻辑
        // 这里返回一个模拟的验证码
        return String.valueOf(1000 + (int) (Math.random() * 9000));
    }

    /**
     * 获取验证码校验字符串（模拟实现）
     * 实际使用时需要根据验证码生成相应的校验字符串
     */
    private String getVerifyCodeHash() {
        // TODO: 实现验证码校验字符串生成逻辑
        // 这里返回一个模拟的校验字符串
        return "dummy_verify_hash_" + System.currentTimeMillis();
    }
}
