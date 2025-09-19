// SgsCrawler.java
// 这是一个用于爬取 SGS 新闻页面内容并导出为 CSV 文件的测试爬虫示例
// 使用POST API调用方式获取数据

package com.certification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

// SgsCrawler 类定义
public class SgsCrawler {
    
    // PageContent 用于存储每条新闻的内容
    static class PageContent {
        String title;   // 新闻标题
        String url;     // 新闻详情页链接
        String date;    // 新闻日期
        String content; // 新闻内容
        String summary; // 新闻摘要
        
        public PageContent(String title, String url, String date, String content, String summary) {
            this.title = title;
            this.url = url;
            this.date = date;
            this.content = content;
            this.summary = summary;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getDate() { return date; }
        public String getContent() { return content; }
        public String getSummary() { return summary; }
        
        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setUrl(String url) { this.url = url; }
        public void setDate(String date) { this.date = date; }
        public void setContent(String content) { this.content = content; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    private static final String API_URL = "https://www.sgs.com/api/filtered-list/post";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    private static final String REFERER = "https://www.sgs.com/en/news";
    
    /**
     * 构建POST请求的JSON数据
     * @param page 页码
     * @param keyword 搜索关键词
     * @return JSON字符串
     */
    private String buildPostData(int page, String keyword) {
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
        
        // 发布日期过滤�����
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
     * @param page 页码
     * @param keyword 搜索关键词
     * @param newsType 新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics 主题值列表（可选）
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
    
    /**
     * 发送POST请求获取数据
     * @param page 页码
     * @param keyword 搜索关键词
     * @return API响应内容
     * @throws Exception 网络异常
     */
    private String sendPostRequest(int page, String keyword) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        String postData = buildPostData(page, keyword);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("Referer", REFERER)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en")
                .header("Origin", "https://www.sgs.com")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("API请求失败，状态码: " + response.statusCode());
        }
        System.out.println(response.body());
        return response.body();
    }
    
    /**
     * 解析API响应数据
     * @param jsonResponse API响应JSON
     * @return 新闻内容列表
     * @throws Exception 解析异常
     */
    private List<PageContent> parseApiResponse(String jsonResponse) throws Exception {
        List<PageContent> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        // 解析items数组
        if (rootNode.has("items") && rootNode.get("items").isArray()) {
            for (JsonNode item : rootNode.get("items")) {
                String title = item.path("headline").path("value").asText("");
                String url = item.path("cta").path("value").path("href").asText("");
                String date = item.path("date").path("value").asText("");
                String content = item.path("description").path("value").asText("");
                String summary = item.path("type").path("value").asText("");
                // 确保URL是完整的
                if (!url.isEmpty() && !url.startsWith("http")) {
                    url = "https://www.sgs.com" + url;
                }
                result.add(new PageContent(title, url, date, content, summary));
            }
        }
        return result;
    }

    /**
     * 爬取 SGS 新闻页面内容，使用POST API获取数据
     *
     * @param keyword    搜索关键词
     * @param totalCount 需要爬取的内容总数
     * @return 新闻内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<PageContent> crawl(String keyword, int totalCount) throws Exception {
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
     * @return 新闻内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<PageContent> crawlWithFilters(String keyword, int totalCount, String newsType, String dateRange, List<String> topics) throws Exception {
        List<PageContent> result = new ArrayList<>();
        int page = 1;
        
        while (result.size() < totalCount) {
            System.out.println("正在爬取第 " + page + " 页，关键词: " + (keyword != null ? keyword : "无"));
            
            try {
                String jsonResponse;
                if (newsType != null || dateRange != null || (topics != null && !topics.isEmpty())) {
                    jsonResponse = sendPostRequestWithFilters(page, keyword, newsType, dateRange, topics);
                } else {
                    jsonResponse = sendPostRequest(page, keyword);
                }
                
                List<PageContent> pageResults = parseApiResponse(jsonResponse);
                
                if (pageResults.isEmpty()) {
                    System.out.println("第 " + page + " 页没有更多数据，停止爬取");
                    break;
                }
                
                for (PageContent content : pageResults) {
                    result.add(content);
                    if (result.size() >= totalCount) {
                        break;
                    }
                }
                
                page++;
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.err.println("爬取第 " + page + " 页时发生错误: " + e.getMessage());
                break;
            }
        }
        
        System.out.println("总共爬取到 " + result.size() + " 条数据");
        return result;
    }
    
    /**
     * 发送带过滤��件的POST请求获取数据
     * @param page 页码
     * @param keyword 搜索关键词
     * @param newsType 新闻类型值（可选）
     * @param dateRange 日期范围值（可选）
     * @param topics 主题值列表（可选）
     * @return API响应内容
     * @throws Exception 网络异常
     */
    private String sendPostRequestWithFilters(int page, String keyword, String newsType, String dateRange, List<String> topics) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        
        String postData = buildPostDataWithFilters(page, keyword, newsType, dateRange, topics);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("Referer", REFERER)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en")
                .header("Origin", "https://www.sgs.com")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("API请求失败，状态码: " + response.statusCode());
        }
        
        return response.body();
    }
    
    /**
     * 爬取认证相关新闻
     * @param totalCount 需要爬取的内容总数
     * @return 新闻内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<PageContent> crawlCertificationNews(int totalCount) throws Exception {
        List<String> topics = new ArrayList<>();
        topics.add("496a050c1be74309b381b138507f4147"); // Certification
        return crawlWithFilters("", totalCount, null, null, topics);
    }
    
    /**
     * 爬取最新新闻（过去一个月）
     * @param totalCount 需要爬取的内容总数
     * @return 新闻内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<PageContent> crawlLatestNews(int totalCount) throws Exception {
        return crawlWithFilters("", totalCount, null, "{57C35CF1-7C22-4590-9C4C-B056ED4B5D46}", null);
    }
    
    /**
     * 爬取特定主题的新闻
     * @param topicValues 主题值列表
     * @param totalCount 需要爬取的内容总数
     * @return 新闻内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<PageContent> crawlByTopics(List<String> topicValues, int totalCount) throws Exception {
        return crawlWithFilters("", totalCount, null, null, topicValues);
    }

    /**
     * 将新闻内容列表保存为 CSV 文件
     *
     * @param contents 新闻内容列表
     * @param filePath 保存的文件路径
     * @throws Exception 文件写入异常
     */
    public void saveToCsv(List<PageContent> contents, String filePath) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("title,url,date,content,summary"); // 添加表头
        
        for (PageContent c : contents) {
            // 处理CSV中的特殊字符
            String title = c.title != null ? c.title.replace("\"", "\"\"") : "";
            String url = c.url != null ? c.url.replace("\"", "\"\"") : "";
            String date = c.date != null ? c.date.replace("\"", "\"\"") : "";
            String content = c.content != null ? c.content.replace("\"", "\"\"") : "";
            String summary = c.summary != null ? c.summary.replace("\"", "\"\"") : "";
            
            // 格式化为 CSV 行
            lines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", 
                    title, url, date, content, summary));
        }
        
        Files.write(Path.of(filePath), lines, StandardCharsets.UTF_8);
        System.out.println("数据已保存到: " + filePath);
    }

    /**
     * JUnit 测试方法，验证爬取和导出功能
     *
     * @param tempDir JUnit 自动分配的临时目录
     * @throws Exception 异常
     */
    @Test
    void testCrawlAndSave(@TempDir Path tempDir) throws Exception {
        System.out.println("开始测试爬取功能...");
        
        // 测试爬取功能
        List<PageContent> contents = crawl("railway", 10); // 爬取 10 条内容
        
        // 验证爬取结果
        assertNotNull(contents, "爬取结果不应为null");
        assertTrue(contents.size() > 0, "应该爬取到数据");
        
        // 保存为CSV
        Path tempFile = tempDir.resolve("sgs-results.csv");
        saveToCsv(contents, tempFile.toString());
        
        // 验证文件
        assertTrue(Files.exists(tempFile), "CSV文件应被创建");
        List<String> lines = Files.readAllLines(tempFile, StandardCharsets.UTF_8);
        assertTrue(lines.size() > 1, "应有内容被写入");
        
        System.out.println("测试完成，爬取到 " + contents.size() + " 条数据");
    }
    
    /**
     * 测试API连接
     */
    @Test
    void testApiConnection() throws Exception {
        System.out.println("测试API连接...");
        
        try {
            String response = sendPostRequest(1, "");
            assertNotNull(response, "API响应不应为null");
            assertFalse(response.isEmpty(), "API响应不应为空");
            
            System.out.println("API连接测试成功");
        } catch (Exception e) {
            System.err.println("API连接测试失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 主方法，直接运行可执行爬虫和导出功能
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("SGS爬虫启动...");
        
        SgsCrawler crawler = new SgsCrawler();
        try {
            // 示例：爬取30条内容��导出到当前目录
            List<PageContent> contents = crawler.crawl(null, 30);
            
            if (!contents.isEmpty()) {
                crawler.saveToCsv(contents, "sgs-results.csv");
                System.out.println("爬取并导出完成，共 " + contents.size() + " 条数据");
                
                // 显示前几条数据作为示例
                System.out.println("\n前5条数据预览:");
                for (int i = 0; i < Math.min(5, contents.size()); i++) {
                    PageContent content = contents.get(i);
                    System.out.println((i + 1) + ". " + content.getTitle());
                    System.out.println("   URL: " + content.getUrl());
                    System.out.println("   日期: " + content.getDate());
                    System.out.println("   摘要: " + content.getSummary());
                    System.out.println();
                }
            } else {
                System.out.println("未爬取到任何数据");
            }
            
        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
