package com.certification.crawler.certification.sgs;

import com.certification.crawler.certification.base.BaseCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.common.HttpUtils;
import com.certification.entity.common.CrawlerData;
import com.certification.service.DateFormatService;
import com.certification.service.SystemLogService;
import com.certification.standards.CrawlerDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SGS爬虫实现 - 使用POST API获取数据
 */
@Slf4j
@Component
@Transactional
public class SgsCrawler implements BaseCrawler {

    @Autowired
    private HttpUtils httpUtils;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private SystemLogService systemLogService;
    
    @Autowired
    private DateFormatService dateFormatService;

    private final CrawlerConfig config;
    private static final String API_URL = "https://www.sgs.com/api/filtered-list/post";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final String REFERER = "https://www.sgs.com/en/news";

    public SgsCrawler() {
        this.config = new CrawlerConfig();
        this.config.setBaseUrl("https://www.sgs.com");
        this.config.setTimeout(30000);
        this.config.setRetryCount(3);
        this.config.setUserAgent(USER_AGENT);
    }

    /**
     * 构建POST请求的JSON数据
     *
     * @param page    页码
     * @param keyword 搜索关键词
     * @return JSON字符串
     */
    public String buildPostData(int page, String keyword) {
        Map<String, Object> requestData = new HashMap<>();

        // 基础参数
        requestData.put("language", "en");
        requestData.put("pageNumber", page);
        requestData.put("searchKey", keyword != null ? keyword : "");
        requestData.put("remotesOnly", null);
        requestData.put("datasourceId", "{DF7BF6A2-7652-4D3C-9AAE-424DEEA6A2A5}");
        requestData.put("type", "knowledge-center");

        // 构建过滤器
        List<Map<String, Object>> filters = new ArrayList<>();

        // 新闻类型过滤器
        Map<String, Object> newsTypeFilter = new HashMap<>();
        newsTypeFilter.put("label", "News Type");
        newsTypeFilter.put("value", "{9DA17697-2D6F-4580-B783-CFEC8E7D86D3}");
        newsTypeFilter.put("isEnabled", true);
        newsTypeFilter.put("isMultiple", false);
        newsTypeFilter.put("isHidden", false);
        newsTypeFilter.put("selectedValue", "");

        List<Map<String, String>> newsTypeOptions = new ArrayList<>();
        newsTypeOptions.add(Map.of("label", "Business News", "value", "60f24f8e42314d42860ee99a5cd5a652"));
        newsTypeOptions.add(Map.of("label", "Customer Stories", "value", "67f6406845254365b73433cefbadfe84"));
        newsTypeOptions.add(Map.of("label", "Features", "value", "087a1a51d9e944298765f8327b1f9f60"));
        newsTypeOptions.add(Map.of("label", "Global Corporate News", "value", "fb520c62e9c343dd92c4cad6ad7247e8"));
        newsTypeOptions.add(Map.of("label", "Local Corporate News", "value", "c5dc1966fe514abaabe8ef9dc75d172a"));
        newsTypeFilter.put("options", newsTypeOptions);
        filters.add(newsTypeFilter);

        // 发布日期过滤器
        Map<String, Object> dateFilter = new HashMap<>();
        dateFilter.put("label", "Publication Date");
        dateFilter.put("value", "{15424C7E-6D54-4D8B-887E-EAC939AE2394}");
        dateFilter.put("isEnabled", true);
        dateFilter.put("isMultiple", false);
        dateFilter.put("isHidden", false);
        dateFilter.put("selectedValue", "");

        List<Map<String, String>> dateOptions = new ArrayList<>();
        dateOptions.add(Map.of("label", "Past Week", "value", "{A6809EE3-323F-4B0E-8346-1A3CF26B714B}"));
        dateOptions.add(Map.of("label", "Past Month", "value", "{57C35CF1-7C22-4590-9C4C-B056ED4B5D46}"));
        dateOptions.add(Map.of("label", "Past Year", "value", "{1E820BE0-5B7F-4435-882E-DED686057DAB}"));
        dateOptions.add(Map.of("label", "2025", "value", "{6B0C4C80-851D-4ADD-8575-52978AD58599}"));
        dateOptions.add(Map.of("label", "2024", "value", "{9EECBEAE-A412-4167-999F-DCCFEB7405D5}"));
        dateOptions.add(Map.of("label", "2023", "value", "{13F30E19-B3CE-48ED-BFA4-6704A61BEBFA}"));
        dateOptions.add(Map.of("label", "2022", "value", "{A1596BCB-7AA6-4B6D-971E-73416E8B3B06}"));
        dateOptions.add(Map.of("label", "Older", "value", "{2782DF07-157B-436E-8C9E-A96C51B050FF}"));
        dateFilter.put("options", dateOptions);
        filters.add(dateFilter);

        // 主题过滤器
        Map<String, Object> topicFilter = new HashMap<>();
        topicFilter.put("label", "Topic");
        topicFilter.put("value", "{0793416D-101F-483B-9C40-B96C13FE2655}");
        topicFilter.put("isEnabled", true);
        topicFilter.put("isMultiple", true);
        topicFilter.put("isHidden", false);
        topicFilter.put("selectedValue", "");

        List<Map<String, String>> topicOptions = new ArrayList<>();
        topicOptions.add(Map.of("label", "About SGS", "value", "9209eadfaff74fe48a63401202f818af"));
        topicOptions.add(Map.of("label", "Agriculture & Forestry", "value", "bbd469989ef344dfa91acd997ef78087"));
        topicOptions.add(Map.of("label", "Building and Infrastructure", "value", "10e34a46667b4e6d8e3b3d528412bbd6"));
        topicOptions.add(Map.of("label", "Certification", "value", "496a050c1be74309b381b138507f4147"));
        topicOptions.add(Map.of("label", "Connectivity", "value", "5b2355c4277843d995ab44ce3a402eed"));
        topicOptions.add(Map.of("label", "Consumer Products & Retail", "value", "c23eda3e557b4beb9de876b84bb1d11f"));
        topicOptions.add(Map.of("label", "Corporate Sustainability", "value", "5b98b824eabc4bed896cef0ada33a5f3"));
        topicOptions.add(Map.of("label", "Cosmetics & Personal Care", "value", "b1ef2516d5814fb08ba52cbd560effff"));
        topicOptions.add(Map.of("label", "Cybersecurity & Technology", "value", "da16b3c7f2904554a389636adf39f2d3"));
        topicOptions.add(Map.of("label", "Digital Trust Assurance", "value", "da588fd0284e4d17b63f20fe5b26e7eb"));
        topicOptions.add(Map.of("label", "Environmental, Health & Safety", "value", "e075e13116e943dc868cbe70be0ae5b6"));
        topicOptions.add(Map.of("label", "Food", "value", "c7ee57cc8d6544429e172eb25d7ab8f9"));
        topicOptions.add(Map.of("label", "Government & Trade Facilitation", "value", "e634aa5321f3490eb32bb0fcd883d1e0"));
        topicOptions.add(Map.of("label", "Hardgoods, Toys & Juvenile Products", "value", "f45027743c454c2389264d0eb1c6f4ed"));
        topicOptions.add(Map.of("label", "Industrial Manufacturing & Processing", "value", "c5250a583732466ba803f2f5688d24dc"));
        topicOptions.add(Map.of("label", "Investor Relations", "value", "18ba053f103f46be853e112c38995919"));
        topicOptions.add(Map.of("label", "MedTech", "value", "1dc58b0c6f0541be86a08153ab2f7114"));
        topicOptions.add(Map.of("label", "Mining", "value", "b81102ac1d0e47dcb1e3fe26f625782a"));
        topicOptions.add(Map.of("label", "Oil, Gas & Chemicals", "value", "a8fecf02948348b18ee8e6bce8df1f76"));
        topicOptions.add(Map.of("label", "Pharma", "value", "e14e7e1e14b94fcbad81ae9914bf7e3b"));
        topicOptions.add(Map.of("label", "Power & Utilities", "value", "06f9571bcfeb4869ba0603e4049e1d86"));
        topicOptions.add(Map.of("label", "Softlines", "value", "848934348e93444e8532d987586e8639"));
        topicOptions.add(Map.of("label", "Supply Chain", "value", "179d85eac9f549939c86e227b9410b7e"));
        topicOptions.add(Map.of("label", "Sustainability", "value", "f0960a9e139b4dcea7f04efc85026b00"));
        topicOptions.add(Map.of("label", "Training", "value", "a56d996ae15549898949086d148ba9fa"));
        topicOptions.add(Map.of("label", "Transportation", "value", "016482e54206417aa7ded6b2b2c72bc4"));
        topicFilter.put("options", topicOptions);
        filters.add(topicFilter);

        requestData.put("filters", filters);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(requestData);
        } catch (Exception e) {
            throw new RuntimeException("构建请求数据失败", e);
        }
    }

    /**
     * 构建带过滤条件的POST请求数据
     *
     * @param page      页码
     * @param keyword   搜索关键词
     * @param newsType  新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics    主题值列表（可选）
     * @return JSON字符串
     */
    private String buildPostDataWithFilters(int page, String keyword, String newsType, String dateRange, List<String> topics) {
        Map<String, Object> requestData = new HashMap<>();

        // 基础参数
        requestData.put("language", "en");
        requestData.put("pageNumber", page);
        requestData.put("searchKey", keyword != null ? keyword : "");
        requestData.put("remotesOnly", null);
        requestData.put("datasourceId", "{DF7BF6A2-7652-4D3C-9AAE-424DEEA6A2A5}");
        requestData.put("type", "knowledge-center");

        // 构建过滤器
        List<Map<String, Object>> filters = new ArrayList<>();

        // 新闻类型过滤器
        Map<String, Object> newsTypeFilter = new HashMap<>();
        newsTypeFilter.put("label", "News Type");
        newsTypeFilter.put("value", "{9DA17697-2D6F-4580-B783-CFEC8E7D86D3}");
        newsTypeFilter.put("isEnabled", true);
        newsTypeFilter.put("isMultiple", false);
        newsTypeFilter.put("isHidden", false);
        newsTypeFilter.put("selectedValue", newsType != null ? newsType : "");

        List<Map<String, String>> newsTypeOptions = new ArrayList<>();
        newsTypeOptions.add(Map.of("label", "Business News", "value", "60f24f8e42314d42860ee99a5cd5a652"));
        newsTypeOptions.add(Map.of("label", "Customer Stories", "value", "67f6406845254365b73433cefbadfe84"));
        newsTypeOptions.add(Map.of("label", "Features", "value", "087a1a51d9e944298765f8327b1f9f60"));
        newsTypeOptions.add(Map.of("label", "Global Corporate News", "value", "fb520c62e9c343dd92c4cad6ad7247e8"));
        newsTypeOptions.add(Map.of("label", "Local Corporate News", "value", "c5dc1966fe514abaabe8ef9dc75d172a"));
        newsTypeFilter.put("options", newsTypeOptions);
        filters.add(newsTypeFilter);

        // 发布日期过滤器
        Map<String, Object> dateFilter = new HashMap<>();
        dateFilter.put("label", "Publication Date");
        dateFilter.put("value", "{15424C7E-6D54-4D8B-887E-EAC939AE2394}");
        dateFilter.put("isEnabled", true);
        dateFilter.put("isMultiple", false);
        dateFilter.put("isHidden", false);
        dateFilter.put("selectedValue", dateRange != null ? dateRange : "");

        List<Map<String, String>> dateOptions = new ArrayList<>();
        dateOptions.add(Map.of("label", "Past Week", "value", "{A6809EE3-323F-4B0E-8346-1A3CF26B714B}"));
        dateOptions.add(Map.of("label", "Past Month", "value", "{57C35CF1-7C22-4590-9C4C-B056ED4B5D46}"));
        dateOptions.add(Map.of("label", "Past Year", "value", "{1E820BE0-5B7F-4435-882E-DED686057DAB}"));
        dateOptions.add(Map.of("label", "2025", "value", "{6B0C4C80-851D-4ADD-8575-52978AD58599}"));
        dateOptions.add(Map.of("label", "2024", "value", "{9EECBEAE-A412-4167-999F-DCCFEB7405D5}"));
        dateOptions.add(Map.of("label", "2023", "value", "{13F30E19-B3CE-48ED-BFA4-6704A61BEBFA}"));
        dateOptions.add(Map.of("label", "2022", "value", "{A1596BCB-7AA6-4B6D-971E-73416E8B3B06}"));
        dateOptions.add(Map.of("label", "Older", "value", "{2782DF07-157B-436E-8C9E-A96C51B050FF}"));
        dateFilter.put("options", dateOptions);
        filters.add(dateFilter);

        // 主题过滤器
        Map<String, Object> topicFilter = new HashMap<>();
        topicFilter.put("label", "Topic");
        topicFilter.put("value", "{0793416D-101F-483B-9C40-B96C13FE2655}");
        topicFilter.put("isEnabled", true);
        topicFilter.put("isMultiple", true);
        topicFilter.put("isHidden", false);
        topicFilter.put("selectedValue", topics != null && !topics.isEmpty() ? String.join(",", topics) : "");

        List<Map<String, String>> topicOptions = new ArrayList<>();
        topicOptions.add(Map.of("label", "About SGS", "value", "9209eadfaff74fe48a63401202f818af"));
        topicOptions.add(Map.of("label", "Agriculture & Forestry", "value", "bbd469989ef344dfa91acd997ef78087"));
        topicOptions.add(Map.of("label", "Building and Infrastructure", "value", "10e34a46667b4e6d8e3b3d528412bbd6"));
        topicOptions.add(Map.of("label", "Certification", "value", "496a050c1be74309b381b138507f4147"));
        topicOptions.add(Map.of("label", "Connectivity", "value", "5b2355c4277843d995ab44ce3a402eed"));
        topicOptions.add(Map.of("label", "Consumer Products & Retail", "value", "c23eda3e557b4beb9de876b84bb1d11f"));
        topicOptions.add(Map.of("label", "Corporate Sustainability", "value", "5b98b824eabc4bed896cef0ada33a5f3"));
        topicOptions.add(Map.of("label", "Cosmetics & Personal Care", "value", "b1ef2516d5814fb08ba52cbd560effff"));
        topicOptions.add(Map.of("label", "Cybersecurity & Technology", "value", "da16b3c7f2904554a389636adf39f2d3"));
        topicOptions.add(Map.of("label", "Digital Trust Assurance", "value", "da588fd0284e4d17b63f20fe5b26e7eb"));
        topicOptions.add(Map.of("label", "Environmental, Health & Safety", "value", "e075e13116e943dc868cbe70be0ae5b6"));
        topicOptions.add(Map.of("label", "Food", "value", "c7ee57cc8d6544429e172eb25d7ab8f9"));
        topicOptions.add(Map.of("label", "Government & Trade Facilitation", "value", "e634aa5321f3490eb32bb0fcd883d1e0"));
        topicOptions.add(Map.of("label", "Hardgoods, Toys & Juvenile Products", "value", "f45027743c454c2389264d0eb1c6f4ed"));
        topicOptions.add(Map.of("label", "Industrial Manufacturing & Processing", "value", "c5250a583732466ba803f2f5688d24dc"));
        topicOptions.add(Map.of("label", "Investor Relations", "value", "18ba053f103f46be853e112c38995919"));
        topicOptions.add(Map.of("label", "MedTech", "value", "1dc58b0c6f0541be86a08153ab2f7114"));
        topicOptions.add(Map.of("label", "Mining", "value", "b81102ac1d0e47dcb1e3fe26f625782a"));
        topicOptions.add(Map.of("label", "Oil, Gas & Chemicals", "value", "a8fecf02948348b18ee8e6bce8df1f76"));
        topicOptions.add(Map.of("label", "Pharma", "value", "e14e7e1e14b94fcbad81ae9914bf7e3b"));
        topicOptions.add(Map.of("label", "Power & Utilities", "value", "06f9571bcfeb4869ba0603e4049e1d86"));
        topicOptions.add(Map.of("label", "Softlines", "value", "848934348e93444e8532d987586e8639"));
        topicOptions.add(Map.of("label", "Supply Chain", "value", "179d85eac9f549939c86e227b9410b7e"));
        topicOptions.add(Map.of("label", "Sustainability", "value", "f0960a9e139b4dcea7f04efc85026b00"));
        topicOptions.add(Map.of("label", "Training", "value", "a56d996ae15549898949086d148ba9fa"));
        topicOptions.add(Map.of("label", "Transportation", "value", "016482e54206417aa7ded6b2b2c72bc4"));
        topicFilter.put("options", topicOptions);
        filters.add(topicFilter);

        requestData.put("filters", filters);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(requestData);
        } catch (Exception e) {
            throw new RuntimeException("构建请求数据失败", e);
        }
    }

//    /**
//     * 发送POST请求获取数据
//     * @param page 页码
//     * @param keyword 搜索关键词
//     * @return API响应内容
//     * @throws Exception 网络异常
//     */
//    private String sendPostRequest(int page, String keyword) throws Exception {
//        String postData = buildPostData(page, keyword);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", "application/json");
//        headers.put("User-Agent", USER_AGENT);
//        headers.put("Referer", REFERER);
//        headers.put("Accept", "application/json, text/plain, */*");
//        headers.put("Accept-Language", "en");
////        headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
//        headers.put("Origin", "https://www.sgs.com");
//        headers.put("Sec-Fetch-Dest", "empty");
//        headers.put("Sec-Fetch-Mode", "cors");
//        headers.put("Sec-Fetch-Site", "same-origin");
//
//        return httpUtils.postJson(API_URL, postData, headers, config.getTimeout());
//    }
//


    /**
     * 发送POST请求获取数据
     *
     * @param page    页码
     * @param keyword 搜索关键词
     * @return API响应内容
     * @throws Exception 网络异常
     */
    public String sendPostRequest(int page, String keyword) throws Exception {
        System.out.println("开始发送POST请求: page=" + page + ", keyword=" + keyword);
        
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL) // 允许重定向
                .build();

        String postData = buildPostData(page, keyword);
        System.out.println("POST数据构建完成，长度: " + postData.length());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent", USER_AGENT)
                .header("Referer", REFERER)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Origin", "https://www.sgs.com")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

//        System.out.println("发送HTTP请求到: " + API_URL);
        
        // 使用字节处理器来处理压缩响应
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        System.out.println("收到HTTP响应，状态码: " + response.statusCode() + ", 长度: " + response.body().length);
        
        // 处理不同的状态码
        switch (response.statusCode()) {
            case 200:
                // 检查响应是否被压缩
                String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");
                System.out.println("Content-Encoding: " + contentEncoding);
                
                byte[] responseBody = response.body();
                String responseText;
                
                if ("gzip".equalsIgnoreCase(contentEncoding)) {
                    // 解压缩gzip响应
                    System.out.println("检测到gzip压缩，正在解压缩...");
                    responseText = decompressGzip(responseBody);
                } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                    // 解压缩deflate响应
                    System.out.println("检测到deflate压缩，正在解压缩...");
                    responseText = decompressDeflate(responseBody);
                } else {
                    // 无压缩，直接转换为字符串
                    responseText = new String(responseBody, "UTF-8");
                }
                
                System.out.println("解压缩后响应长度: " + responseText.length());
                return responseText;
                
            case 307:
            case 302:
                // 处理重定向
                String location = response.headers().firstValue("Location").orElse(null);
                if (location != null) {
                    // 跟随重定向
                    HttpRequest redirectRequest = HttpRequest.newBuilder()
                            .uri(URI.create(location))
                            .header("Content-Type", "application/json; charset=UTF-8")
                            .header("User-Agent", USER_AGENT)
                            .header("Referer", REFERER)
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .POST(HttpRequest.BodyPublishers.ofString(postData))
                            .build();
                    
                    HttpResponse<byte[]> redirectResponse = client.send(redirectRequest, HttpResponse.BodyHandlers.ofByteArray());
                    if (redirectResponse.statusCode() == 200) {
                        String redirectContentEncoding = redirectResponse.headers().firstValue("Content-Encoding").orElse("");
                        byte[] redirectBody = redirectResponse.body();
                        if ("gzip".equalsIgnoreCase(redirectContentEncoding)) {
                            return decompressGzip(redirectBody);
                        } else {
                            return new String(redirectBody, "UTF-8");
                        }
                    } else {
                        throw new RuntimeException("重定向后请求失败，状态码: " + redirectResponse.statusCode());
                    }
                } else {
                    throw new RuntimeException("收到重定向响应但没有Location头，状态码: " + response.statusCode());
                }
            case 403:
                throw new RuntimeException("访问被拒绝，可能需要更新请求头或API已更改");
            case 404:
                throw new RuntimeException("API端点不存在，可能已更改");
            case 429:
                throw new RuntimeException("请求过于频繁，需要降低请求频率");
            case 500:
            case 502:
            case 503:
            case 504:
                throw new RuntimeException("服务器错误，状态码: " + response.statusCode());
            default:
                throw new RuntimeException("API请求失败，状态码: " + response.statusCode());
        }
    }
    
    /**
     * 解压缩gzip数据
     */
    private String decompressGzip(byte[] compressedData) throws Exception {
        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(compressedData);
             java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(bis);
             java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            return bos.toString("UTF-8");
        }
    }
    
    /**
     * 解压缩deflate数据
     */
    private String decompressDeflate(byte[] compressedData) throws Exception {
        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(compressedData);
             java.util.zip.InflaterInputStream iis = new java.util.zip.InflaterInputStream(bis);
             java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            return bos.toString("UTF-8");
        }
    }

    /**
     * 解析API响应数据
     *
     * @param jsonResponse API响应JSON
     * @return 爬虫结果列表
     * @throws Exception 解析异常
     */
    public List<CrawlerResult> parseApiResponse(String jsonResponse) throws Exception {
        List<CrawlerResult> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);

        // 解析items数组
        if (rootNode.has("items") && rootNode.get("items").isArray()) {
            for (JsonNode item : rootNode.get("items")) {
                String title = item.path("headline").path("value").asText("");
                String url = item.path("cta").path("value").path("href").asText("");
                String rawDate = item.path("date").path("value").asText("");
                String content = item.path("description").path("value").asText("");
                String summary = item.path("type").path("value").asText("");

                // 统一日期格式
                String standardizedDate = dateFormatService.standardizeDate(rawDate);
                if (standardizedDate == null) {
                    log.warn("SGS爬虫无法解析日期格式: {}", rawDate);
                    standardizedDate = dateFormatService.getCurrentDateString();
                }

                // 确保URL是完整的
                if (!url.isEmpty() && !url.startsWith("http")) {
                    url = config.getBaseUrl() + url;
                }

                CrawlerResult crawlerResult = new CrawlerResult(title, url, summary, standardizedDate, "SGS");
                crawlerResult.setCategory("certification");
                crawlerResult.setType("news");
                crawlerResult.setContent(content);
                result.add(crawlerResult);
            }
        }

        return result;
    }

    @Override
    public List<CrawlerResult> crawl(String keyword, int totalCount) throws Exception {
        return crawlWithFilters(keyword, totalCount, null, null, null);
    }

    /**
     * 爬取 SGS 新闻页面内容，支持过滤条件
     *
     * @param keyword    搜索关键词
     * @param totalCount 需要爬取的内容总数
     * @param newsType   新闻类型值（可选）
     * @param dateRange  日期范围值（可选）
     * @param topics     主题值列表（可选）
     * @return 爬虫结果列表
     * @throws Exception 网络异常或解析异常
     */
    public List<CrawlerResult> crawlWithFilters(String keyword, int totalCount, String newsType, String dateRange, List<String> topics) throws Exception {
        List<CrawlerResult> result = new ArrayList<>();
        int page = 1;
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 5;

        while (result.size() < totalCount && consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
            System.out.println("正在爬取第 " + page + " 页，关键词: " + (keyword != null ? keyword : ""));

            int retry = 0;
            boolean success = false;
            while (retry < 3 && !success) {
                try {
                    String jsonResponse;
                    if (newsType != null || dateRange != null || (topics != null && !topics.isEmpty())) {
                        jsonResponse = sendPostRequestWithFilters(page, keyword, newsType, dateRange, topics);
                    } else {
                        jsonResponse = sendPostRequest(page, keyword);
                    }

                    List<CrawlerResult> pageResults = parseApiResponse(jsonResponse);

                    if (pageResults.isEmpty()) {
                        System.out.println("第 " + page + " 页没有更多数据，停止爬取");
                        success = true;
                        break;
                    }

                    for (CrawlerResult crawlerResult : pageResults) {
                        result.add(crawlerResult);
                        if (result.size() >= totalCount) {
                            break;
                        }
                    }
                    success = true;
                    consecutiveFailures = 0; // 重置连续失败计数
                } catch (Exception e) {
                    retry++;
                    System.err.println("第 " + page + " 页爬取失败，第 " + retry + " 次重试，错误: " + e.getMessage());
                    
                    // 根据错误类型调整延迟时间
                    long delayTime;
                    if (e.getMessage().contains("429") || e.getMessage().contains("频繁")) {
                        delayTime = 10000 * retry; // 频率限制错误，更长延迟
                    } else if (e.getMessage().contains("403") || e.getMessage().contains("拒绝")) {
                        delayTime = 5000 * retry; // 访问被拒绝，中等延迟
                    } else {
                        delayTime = 3000 * retry; // 其他错误，较短延迟
                    }
                    
                    if (retry >= 3) {
                        System.err.println("第 " + page + " 页重试3次后仍失败，跳过该页。");
                        consecutiveFailures++;
                        break;
                    }
                    try {
                        Thread.sleep(delayTime); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (!success) {
                // 本页失败，直接跳到下一页
                page++;
                continue;
            }
            page++;
            try {
                Thread.sleep(2000); // 请求间隔，防止被封
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            System.err.println("连续失败次数过多，停止爬取");
        }

        System.out.println("总共爬取到 " + result.size() + " 条数据");
        return result;
    }

    /**
     * 发送带过滤条件的POST请求获取数据
     *
     * @param page      页码
     * @param keyword   搜索关键词
     * @param newsType  新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics    主题值列表（可选）
     * @return API响应内容
     * @throws Exception 网络异常
     */
    private String sendPostRequestWithFilters(int page, String keyword, String newsType, String dateRange, List<String> topics) throws Exception {
        String postData = buildPostDataWithFilters(page, keyword, newsType, dateRange, topics);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("User-Agent", USER_AGENT)
                .header("Referer", REFERER)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Origin", "https://www.sgs.com")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");
            byte[] responseBody = response.body();
            
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                return decompressGzip(responseBody);
            } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
                return decompressDeflate(responseBody);
            } else {
                return new String(responseBody, "UTF-8");
            }
        } else {
            throw new RuntimeException("API请求失败，状态码: " + response.statusCode());
        }
    }

    /**
     * 爬取认证相关新闻
     *
     * @param totalCount 需要爬取的内容总数
     * @return 爬虫结果列表
     * @throws Exception 网络异常或解析异常
     */
    public List<CrawlerResult> crawlCertificationNews(int totalCount) throws Exception {
        List<String> topics = new ArrayList<>();
        topics.add("496a050c1be74309b381b138507f4147"); // Certification
        return crawlWithFilters("", totalCount, null, null, topics);
    }

    /**
     * 爬取最新新闻（过去一个月）
     *
     * @param totalCount 需要爬取的内容总数
     * @return 爬虫结果列表
     * @throws Exception 网络异常或解析异常
     */
    public List<CrawlerResult> crawlLatestNews(int totalCount) throws Exception {
        return crawlWithFilters("", totalCount, null, "{57C35CF1-7C22-4590-9C4C-B056ED4B5D46}", null);
    }

    /**
     * 爬取特定主题的新闻
     *
     * @param topicValues 主题值列表
     * @param totalCount  需要爬取的内容总数
     * @return 爬虫结果列表
     * @throws Exception 网络异常或解析异常
     */
    public List<CrawlerResult> crawlByTopics(List<String> topicValues, int totalCount) throws Exception {
        return crawlWithFilters("", totalCount, null, null, topicValues);
    }

    @Override
    public List<CrawlerResult> crawlLatest(int totalCount) throws Exception {
        return crawl("", totalCount);
    }

    @Override
    public String getCrawlerName() {
        return "SGS Crawler";
    }

    @Override
    public String getSourceName() {
        return "SGS";
    }

    @Override
    public boolean isAvailable() {
        try {
            System.out.println("开始检查SGS爬虫可用性...");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10)) // 连接超时10秒
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String testPostData = buildPostData(1, "");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15)) // 请求超时15秒
                    .header("Content-Type", "application/json")
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", REFERER)
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .POST(HttpRequest.BodyPublishers.ofString(testPostData))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                System.out.println("SGS爬虫可用性检查成功，状态码: " + response.statusCode());
                return true;
            } else {
                System.err.println("SGS爬虫可用性检查失败，状态码: " + response.statusCode());
                return false;
            }

        } catch (java.net.ConnectException e) {
            System.err.println("SGS爬虫网络连接失败: " + e.getMessage());
            System.err.println("可能原因: 网络连接问题、DNS解析失败、防火墙阻止或SGS网站暂时不可用");
            return false;
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("SGS爬虫连接超时: " + e.getMessage());
            return false;
        } catch (java.net.UnknownHostException e) {
            System.err.println("SGS爬虫DNS解析失败: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("SGS爬虫可用性检查失败: " + e.getMessage());
            System.err.println("异常类型: " + e.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public CrawlerConfig getConfig() {
        return config;
    }

    // ==================== 数据保存相关方法 ====================

    /**
     * 执行SGS爬虫并保存到数据库
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeSgsCrawlerAndSave(int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            systemLogService.logInfo(
                "SGS爬虫开始执行",
                String.format("开始执行SGS爬虫，计划爬取 %d 条数据", count),
                "SgsCrawler"
            );
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("SGS");
            
            // 执行爬虫
            List<CrawlerResult> crawlerResults = crawlLatest(count);

            // 转换为CrawlerData实体
            List<CrawlerData> crawlerDataList = convertToCrawlerData(crawlerResults);

            // 使用安全的批量保存（自动去重），每30条数据一批
            List<CrawlerData> savedDataList = crawlerDataService.safeSaveCrawlerDataList(crawlerDataList, 30);

            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("SGS");
            long newDataCount = afterCount - beforeCount;
            
            // 记录数据变更日志
            for (CrawlerData data : savedDataList) {
                systemLogService.logInfo(
                    "爬虫数据创建",
                    String.format("SGS爬虫创建新数据: ID=%s, 标题=%s, URL=%s", 
                        data.getId(), data.getTitle(), data.getUrl()),
                    "SgsCrawler"
                );
            }
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CrawlerData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            systemLogService.logInfo(
                "SGS爬虫执行完成",
                String.format("SGS爬虫执行完成，爬取 %d 条数据，新增 %d 条，耗时 %d ms",
                    crawlerResults.size(), newDataCount, executionTime),
                "SgsCrawler"
            );
            
            // 构建返回结果
            result.put("success", true);
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("requestedCount", count);
            result.put("crawledCount", crawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "SGS爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            systemLogService.logError(
                "SGS爬虫执行失败",
                String.format("SGS爬虫执行失败: %s", e.getMessage()),
                "SgsCrawler",
                e
            );
            
            result.put("success", false);
            result.put("error", "SGS爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 执行SGS爬虫并保存到数据库（带关键词搜索）
     * @param keyword 搜索关键词
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeSgsCrawlerWithKeywordAndSave(String keyword, int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            systemLogService.logInfo(
                "SGS爬虫开始执行（关键词搜索）",
                String.format("开始执行SGS爬虫，关键词: %s，计划爬取 %d 条数据", keyword, count),
                "SgsCrawler"
            );
            
            // 检查爬虫可用性（但不强制要求）
            boolean isAvailable = isAvailable();
            if (!isAvailable) {
                System.out.println("警告: SGS爬虫可用性检查失败，但将继续尝试爬取...");
                systemLogService.logInfo(
                    "SGS爬虫可用性检查",
                    "SGS爬虫可用性检查失败，但将继续尝试爬取",
                    "SgsCrawler"
                );
            }
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("SGS");
            
            // 执行爬虫（带关键词）
            List<CrawlerResult> crawlerResults = crawl(keyword, count);

            // 转换为CrawlerData实体
            List<CrawlerData> crawlerDataList = convertToCrawlerData(crawlerResults);

            // 检查是否全部重复
            if (crawlerDataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有爬取到任何数据");
                result.put("crawledCount", 0);
                result.put("savedCount", 0);
                result.put("duplicateCount", 0);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "SGS爬虫执行完成（无数据）",
                    String.format("SGS爬虫执行完成，关键词: %s，没有爬取到任何数据", keyword),
                    "SgsCrawler"
                );
                return result;
            }

            // 获取去重统计信息
            Map<String, Object> duplicateStats = crawlerDataService.getDuplicateUrlStats(crawlerDataList);
            long duplicateCount = duplicateStats.get("duplicateCount") == null ? 0L : ((Number)duplicateStats.get("duplicateCount")).longValue();
            
            // 检查是否全部重复
            boolean allDuplicates = duplicateCount == crawlerDataList.size();
            if (allDuplicates) {
                result.put("success", true);
                result.put("message", "爬取的数据全部与数据库重复，停止爬取");
                result.put("crawledCount", crawlerDataList.size());
                result.put("savedCount", 0);
                result.put("duplicateCount", duplicateCount);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "SGS爬虫执行完成（全部重复）",
                    String.format("SGS爬虫执行完成，关键词: %s，爬取 %d 条数据全部重复，停止爬取", keyword, crawlerDataList.size()),
                    "SgsCrawler"
                );
                return result;
            }
            
            // 使用安全的批量保存（自动去重），每30条数据一批
            List<CrawlerData> savedDataList = crawlerDataService.safeSaveCrawlerDataList(crawlerDataList, 30);
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("SGS");
            long newDataCount = afterCount - beforeCount;
            
            // 记录数据变更日志
            for (CrawlerData data : savedDataList) {
                systemLogService.logInfo(
                    "爬虫数据创建（关键词搜索）",
                    String.format("SGS爬虫创建新数据: ID=%s, 标题=%s, URL=%s, 关键词=%s", 
                        data.getId(), data.getTitle(), data.getUrl(), keyword),
                    "SgsCrawler"
                );
            }
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CrawlerData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            systemLogService.logInfo(
                "SGS爬虫执行完成（关键词搜索）",
                String.format("SGS爬虫执行完成，关键词: %s，爬取 %d 条数据，新增 %d 条，重复 %d 条，耗时 %d ms", 
                    keyword, crawlerResults.size(), newDataCount, duplicateCount, executionTime),
                "SgsCrawler"
            );
            
            // 构建返回结果
            result.put("success", true);
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("keyword", keyword);
            result.put("requestedCount", count);
            result.put("crawledCount", crawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("duplicateStats", duplicateStats);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "SGS爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            systemLogService.logError(
                "SGS爬虫执行失败（关键词搜索）",
                String.format("SGS爬虫执行失败，关键词: %s，错误: %s", keyword, e.getMessage()),
                "SgsCrawler",
                e
            );
            
            result.put("success", false);
            result.put("error", "SGS爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 执行SGS爬虫并保存到数据库（带过滤条件）
     * @param keyword 搜索关键词
     * @param count 爬取数量
     * @param newsType 新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics 主题值列表（可选）
     * @return 执行结果
     */
    public Map<String, Object> executeSgsCrawlerWithFiltersAndSave(String keyword, int count, String newsType, String dateRange, List<String> topics) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            systemLogService.logInfo(
                "SGS爬虫开始执行（过滤条件）",
                String.format("开始执行SGS爬虫，关键词: %s，新闻类型: %s，日期范围: %s，主题: %s，计划爬取 %d 条数据", 
                    keyword, newsType, dateRange, topics, count),
                "SgsCrawler"
            );
            
            // 检查爬虫可用性（但不强制要求）
            boolean isAvailable = isAvailable();
            if (!isAvailable) {
                System.out.println("警告: SGS爬虫可用性检查失败，但将继续尝试爬取...");
                systemLogService.logInfo(
                    "SGS爬虫可用性检查",
                    "SGS爬虫可用性检查失败，但将继续尝试爬取",
                    "SgsCrawler"
                );
            }
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("SGS");
            
            // 执行爬虫（带过滤条件）
            List<CrawlerResult> crawlerResults = crawlWithFilters(keyword, count, newsType, dateRange, topics);

            // 转换为CrawlerData实体
            List<CrawlerData> crawlerDataList = convertToCrawlerData(crawlerResults);

            // 检查是否全部重复
            if (crawlerDataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有爬取到任何数据");
                result.put("crawledCount", 0);
                result.put("savedCount", 0);
                result.put("duplicateCount", 0);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "SGS爬虫执行完成（无数据）",
                    String.format("SGS爬虫执行完成，关键词: %s，没有爬取到任何数据", keyword),
                    "SgsCrawler"
                );
                return result;
            }

            // 获取去重统计信息
            Map<String, Object> duplicateStats = crawlerDataService.getDuplicateUrlStats(crawlerDataList);
            long duplicateCount = duplicateStats.get("duplicateCount") == null ? 0L : ((Number)duplicateStats.get("duplicateCount")).longValue();
            
            // 检查是否全部重复
            boolean allDuplicates = duplicateCount == crawlerDataList.size();
            if (allDuplicates) {
                result.put("success", true);
                result.put("message", "爬取的数据全部与数据库重复，停止爬取");
                result.put("crawledCount", crawlerDataList.size());
                result.put("savedCount", 0);
                result.put("duplicateCount", duplicateCount);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "SGS爬虫执行完成（全部重复）",
                    String.format("SGS爬虫执行完成，关键词: %s，爬取 %d 条数据全部重复，停止爬取", keyword, crawlerDataList.size()),
                    "SgsCrawler"
                );
                return result;
            }
            
            // 使用安全的批量保存（自动去重），每30条数据一批
            List<CrawlerData> savedDataList = crawlerDataService.safeSaveCrawlerDataList(crawlerDataList, 30);
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("SGS");
            long newDataCount = afterCount - beforeCount;
            
            // 记录数据变更日志
            for (CrawlerData data : savedDataList) {
                systemLogService.logInfo(
                    "爬虫数据创建（过滤条件）",
                    String.format("SGS爬虫创建新数据: ID=%s, 标题=%s, URL=%s, 关键词=%s", 
                        data.getId(), data.getTitle(), data.getUrl(), keyword),
                    "SgsCrawler"
                );
            }
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CrawlerData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            systemLogService.logInfo(
                "SGS爬虫执行完成（过滤条件）",
                String.format("SGS爬虫执行完成，关键词: %s，爬取 %d 条数据，新增 %d 条，重复 %d 条，耗时 %d ms", 
                    keyword, crawlerResults.size(), newDataCount, duplicateCount, executionTime),
                "SgsCrawler"
            );
            
            // 构建返回结果
            result.put("success", true);
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("keyword", keyword);
            result.put("newsType", newsType);
            result.put("dateRange", dateRange);
            result.put("topics", topics);
            result.put("requestedCount", count);
            result.put("crawledCount", crawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("duplicateStats", duplicateStats);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "SGS爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            systemLogService.logError(
                "SGS爬虫执行失败（过滤条件）",
                String.format("SGS爬虫执行失败，关键词: %s，错误: %s", keyword, e.getMessage()),
                "SgsCrawler",
                e
            );
            
            result.put("success", false);
            result.put("error", "SGS爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 将CrawlerResult转换为CrawlerData实体
     * @param crawlerResults 爬虫结果列表
     * @return CrawlerData实体列表
     */
    private List<CrawlerData> convertToCrawlerData(List<CrawlerResult> crawlerResults) {
        List<CrawlerData> crawlerDataList = new ArrayList<>();
        
        for (CrawlerResult result : crawlerResults) {
            CrawlerData crawlerData = new CrawlerData();
            // 设置ID为随机UUID
            crawlerData.setId(UUID.randomUUID().toString());
            
            // 设置基本信息
            crawlerData.setSourceName(result.getSource());
            crawlerData.setTitle(result.getTitle());
            crawlerData.setUrl(result.getUrl());
            crawlerData.setSummary(result.getContent());
            crawlerData.setContent(result.getContent());
            // 统一日期格式
            String rawDate = result.getDate();
            String standardizedDate = dateFormatService.standardizeDate(rawDate);
            if (standardizedDate == null) {
                log.warn("SGS爬虫无法解析日期格式: {}", rawDate);
                standardizedDate = dateFormatService.getCurrentDateString();
            }
            crawlerData.setPublishDate(standardizedDate);
            crawlerData.setCrawlTime(result.getCrawlTime());
            crawlerData.setType(result.getType());
            crawlerData.setCountry(result.getCountry());
            
            // 设置状态
            crawlerData.setStatus(CrawlerData.DataStatus.NEW);
            crawlerData.setIsProcessed(false);
            
            // 设置风险等级为MEDIUM
            crawlerData.setRiskLevel(CrawlerData.RiskLevel.MEDIUM);
            
            // 设置备注
            crawlerData.setRemarks("通过SGS爬虫自动抓取");
            
            crawlerDataList.add(crawlerData);
        }
        
        return crawlerDataList;
    }
    
    /**
     * 获取SGS爬虫状态
     * @return 爬虫状态信息
     */
    public Map<String, Object> getSgsCrawlerStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("available", isAvailable());
            result.put("config", getConfig());
            
            // 获取数据库统计
            long totalCount = crawlerDataService.getCountBySourceName("SGS");
            long newCount = crawlerDataService.getCountByStatus(CrawlerData.DataStatus.NEW);
            long processedCount = crawlerDataService.getCountByStatus(CrawlerData.DataStatus.PROCESSED);
            long errorCount = crawlerDataService.getCountByStatus(CrawlerData.DataStatus.ERROR);
            long duplicateCount = crawlerDataService.getCountByStatus(CrawlerData.DataStatus.DUPLICATE);
            
            result.put("databaseStats", Map.of(
                "totalCount", totalCount,
                "newCount", newCount,
                "processedCount", processedCount,
                "errorCount", errorCount,
                "duplicateCount", duplicateCount
            ));
            
        } catch (Exception e) {
            result.put("error", "获取SGS爬虫状态失败: " + e.getMessage());
        }
        
        return result;
    }
}
