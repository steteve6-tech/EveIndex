package com.certification.crawler.countrydata.eu;

import com.certification.crawler.common.CsvExporter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧盟安全门预警系统爬虫
 * 爬取Safety Gate Alert数据
 */
public class Eu_event {
    
    private static final String BASE_URL = "https://ec.europa.eu/safety-gate-alerts/screen/search";
    private static final String API_URL = "https://ec.europa.eu/safety-gate-alerts/public/api/search";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    private final CsvExporter csvExporter;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public Eu_event() {
        this.csvExporter = new CsvExporter();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 爬取Safety Gate预警数据
     * @param maxPages 最大爬取页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlSafetyGateAlerts(int maxPages) {
        return crawlSafetyGateAlerts(maxPages, "", "", "", "");
    }
    
    /**
     * 爬取Safety Gate预警数据（带搜索参数）
     * @param maxPages 最大爬取页数
     * @param searchKeyword 搜索关键词
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @param language 语言
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlSafetyGateAlerts(int maxPages, String searchKeyword, String sortField, String sortDirection, String language) {
        return crawlSafetyGateAlerts(maxPages, searchKeyword, sortField, sortDirection, language, new ArrayList<>(), 25);
    }
    
    /**
     * 爬取Safety Gate预警数据（完整搜索参数）
     * @param maxPages 最大爬取页数
     * @param searchKeyword 搜索关键词
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @param language 语言
     * @param years 年份筛选列表
     * @param pageSize 每页大小
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlSafetyGateAlerts(int maxPages, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        List<Map<String, String>> allAlerts = new ArrayList<>();
        
        try {
            // 设置Chrome WebDriver
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=" + USER_AGENT);
            
            WebDriver driver = new ChromeDriver(options);
            
            try {
                // 访问搜索页面
                System.out.println("=".repeat(80));
                System.out.println("🌐 WebDriver访问详情:");
                System.out.println("📡 访问URL: " + BASE_URL);
                System.out.println("⏱️  开始时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                
                long startTime = System.currentTimeMillis();
                driver.get(BASE_URL);
                long endTime = System.currentTimeMillis();
                
                System.out.println("⏱️  页面加载时间: " + (endTime - startTime) + "ms");
                System.out.println("📄 页面标题: " + driver.getTitle());
                System.out.println("🔗 当前URL: " + driver.getCurrentUrl());
                System.out.println("=".repeat(80));
                
                // 等待页面加载
                Thread.sleep(5000);
                
                // 爬取多页数据
                for (int page = 0; page < maxPages; page++) {
                    System.out.println("正在爬取第" + (page + 1) + "页数据...");
                    
                    // 使用API获取数据
                    List<Map<String, String>> pageAlerts = fetchAlertsFromAPI(page, searchKeyword, sortField, sortDirection, language, years, pageSize);
                    
                    if (pageAlerts.isEmpty()) {
                        System.out.println("第" + (page + 1) + "页没有数据，停止爬取");
                        break;
                    }
                    
                    allAlerts.addAll(pageAlerts);
                    System.out.println("第" + (page + 1) + "页爬取完成，获取到 " + pageAlerts.size() + " 条预警数据");
                    
                    // 等待一下再爬取下一页
                    Thread.sleep(2000);
                }
                
            } finally {
                driver.quit();
            }
            
        } catch (Exception e) {
            System.err.println("爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allAlerts;
    }
    
    /**
     * 通过API获取预警数据
     */
    private List<Map<String, String>> fetchAlertsFromAPI(int page) {
        return fetchAlertsFromAPI(page, "", "", "", "", new ArrayList<>(), 25);
    }
    
    /**
     * 通过API获取预警数据（带搜索参数）
     */
    private List<Map<String, String>> fetchAlertsFromAPI(int page, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        List<Map<String, String>> alerts = new ArrayList<>();
        
        try {
            // 构建API请求体
            String requestBody = buildApiRequestBody(page, searchKeyword, sortField, sortDirection, language, years, pageSize);
            
            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "No-Cache")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Referer", BASE_URL)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            // 详细打印请求信息
            System.out.println("=".repeat(80));
            System.out.println("🌐 API请求详情:");
            System.out.println("📡 请求URL: " + API_URL);
            System.out.println("📄 请求方法: POST");
            System.out.println("📋 请求头:");
            request.headers().map().forEach((key, values) -> {
                System.out.println("   " + key + ": " + String.join(", ", values));
            });
            System.out.println("📦 请求体: " + requestBody);
            System.out.println("=".repeat(80));
            
            // 发送请求
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long endTime = System.currentTimeMillis();
            
            // 详细打印响应信息
            System.out.println("📥 API响应详情:");
            System.out.println("⏱️  响应时间: " + (endTime - startTime) + "ms");
            System.out.println("📊 响应状态码: " + response.statusCode());
            System.out.println("📏 响应长度: " + response.body().length() + " 字节");
            System.out.println("📋 响应头:");
            response.headers().map().forEach((key, values) -> {
                System.out.println("   " + key + ": " + String.join(", ", values));
            });
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("✅ API请求成功");
                
                // 输出JSON响应的详细信息
                System.out.println("\n📋 JSON响应内容:");
                System.out.println("=".repeat(80));
                System.out.println(responseBody);
                System.out.println("=".repeat(80));
                
                // 解析JSON响应
                alerts = parseApiResponse(responseBody);
            } else {
                System.err.println("❌ API请求失败，状态码: " + response.statusCode());
                System.err.println("响应内容: " + response.body());
            }
            
        } catch (Exception e) {
            System.err.println("❌ API请求时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alerts;
    }
    
    /**
     * 构建API请求体
     */
    private String buildApiRequestBody(int page) {
        return buildApiRequestBody(page, "", "", "", "", new ArrayList<>(), 25);
    }
    
    /**
     * 构建API请求体（带搜索参数）
     */
    private String buildApiRequestBody(int page, String searchKeyword, String sortField, String sortDirection, String language) {
        return buildApiRequestBody(page, searchKeyword, sortField, sortDirection, language, new ArrayList<>(), 25);
    }
    
    /**
     * 构建API请求体（完整搜索参数）
     */
    private String buildApiRequestBody(int page, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 搜索条件
        Map<String, Object> criteria = new HashMap<>();
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            criteria.put("fullTextSearch", ""); // 空搜索获取所有数据
        } else {
            criteria.put("fullTextSearch", searchKeyword.trim());
        }
        
        // 年份筛选
        if (years != null && !years.isEmpty()) {
            criteria.put("year", years);
        } else {
            criteria.put("year", new ArrayList<>());
        }
        
        // 分页信息
        Map<String, Object> pagination = new HashMap<>();
        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "PUBLICATION_DATE";
        }
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            sortDirection = "DESC";
        }
        pagination.put("sortField", sortField);
        pagination.put("sortOrder", sortDirection);
        pagination.put("totalElements", 0);
        pagination.put("numberElements", pageSize > 0 ? pageSize : 25); // 自定义每页大小
        pagination.put("page", page);
        
        requestBody.put("criteria", criteria);
        requestBody.put("searchCriteriaForNotification", false);
        requestBody.put("isLaunched", true);
        requestBody.put("pagination", pagination);
        requestBody.put("searchResults", new ArrayList<>());
        requestBody.put("displayDefaultResults", false);
        requestBody.put("displayTagsWithSelectedCriteria", new ArrayList<>());
        requestBody.put("isForMostRecent", false);
        requestBody.put("isLaunchSearch", true);
        requestBody.put("fullTextSearch", searchKeyword != null ? searchKeyword.trim() : "");
        if (language == null || language.trim().isEmpty()) {
            language = "en";
        }
        requestBody.put("language", language);
        
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("构建请求体时出错: " + e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 解析API响应
     */
    private List<Map<String, String>> parseApiResponse(String responseBody) {
        List<Map<String, String>> alerts = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // 打印API响应的结构以便调试
            System.out.println("API响应根节点字段: " + rootNode.fieldNames().next());
            System.out.println("API响应结构: " + rootNode.toPrettyString().substring(0, Math.min(1000, rootNode.toPrettyString().length())));
            
            // 尝试多种可能的数据结构
            JsonNode searchResults = null;
            
            // 检查不同的可能字段名
            if (rootNode.has("content")) {
                searchResults = rootNode.get("content");
                System.out.println("找到 'content' 字段，包含数据");
            } else if (rootNode.has("searchResults")) {
                searchResults = rootNode.get("searchResults");
                System.out.println("找到 'searchResults' 字段，包含数据");
            } else if (rootNode.has("results")) {
                searchResults = rootNode.get("results");
                System.out.println("找到 'results' 字段，包含数据");
            } else if (rootNode.has("data")) {
                searchResults = rootNode.get("data");
                System.out.println("找到 'data' 字段，包含数据");
            } else if (rootNode.has("alerts")) {
                searchResults = rootNode.get("alerts");
                System.out.println("找到 'alerts' 字段，包含数据");
            } else if (rootNode.has("notifications")) {
                searchResults = rootNode.get("notifications");
                System.out.println("找到 'notifications' 字段，包含数据");
            } else if (rootNode.isArray()) {
                // 如果根节点本身就是数组
                searchResults = rootNode;
                System.out.println("根节点是数组，直接使用");
            }
            
            if (searchResults != null) {
                System.out.println("找到数据节点，类型: " + searchResults.getNodeType());
                
                if (searchResults.isArray()) {
                    System.out.println("数据节点是数组，长度: " + searchResults.size());
                    
                    for (JsonNode alertNode : searchResults) {
                        Map<String, String> alert = parseAlertFromJson(alertNode);
                        if (!alert.isEmpty()) {
                            alerts.add(alert);
                        }
                    }
                } else if (searchResults.isObject()) {
                    System.out.println("数据节点是对象，字段: " + searchResults.fieldNames().next());
                    // 如果是对象，尝试解析单个预警
                    Map<String, String> alert = parseAlertFromJson(searchResults);
                    if (!alert.isEmpty()) {
                        alerts.add(alert);
                    }
                }
            } else {
                System.out.println("未找到预期的数据节点");
                // 尝试直接解析根节点
                if (rootNode.isArray()) {
                    for (JsonNode alertNode : rootNode) {
                        Map<String, String> alert = parseAlertFromJson(alertNode);
                        if (!alert.isEmpty()) {
                            alerts.add(alert);
                        }
                    }
                }
            }
            
            System.out.println("从API响应中解析出 " + alerts.size() + " 条预警数据");
            
        } catch (Exception e) {
            System.err.println("解析API响应时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alerts;
    }
    
    /**
     * 从JSON节点解析单个预警数据
     */
    private Map<String, String> parseAlertFromJson(JsonNode alertNode) {
        Map<String, String> alert = new HashMap<>();
        
        try {
            // 打印节点结构以便调试
            System.out.println("解析预警节点，字段: " + alertNode.fieldNames().next());
            
            // 提取基本字段 - 根据实际API响应结构调整
            alert.put("alert_number", getJsonString(alertNode, "reference", "alertNumber", "alert_number", "id", "notificationId"));
            alert.put("publication_date", getJsonString(alertNode, "publicationDate", "publication_date", "date", "createdDate"));
            
            // 处理产品信息 - 增强解析逻辑
            if (alertNode.has("product") && alertNode.get("product").isObject()) {
                JsonNode productNode = alertNode.get("product");
                
                // 尝试多种可能的产品名称字段
                String productName = getJsonString(productNode, "name", "productName", "productType", "title", "description", "nameSpecific");
                if (productName.isEmpty()) {
                    // 如果直接字段为空，尝试从其他字段获取
                    productName = getJsonString(productNode, "category", "type", "model", "identifier");
                }
                alert.put("product", productName);
                
                // 单独提取nameSpecific字段
                String nameSpecific = getJsonString(productNode, "nameSpecific");
                if (!nameSpecific.isEmpty()) {
                    alert.put("product_name_specific", nameSpecific);
                }
                
                // 处理产品类别和子类别
                alert.put("category", getJsonString(productNode, "category", "productCategory", "type", "classification"));
                alert.put("subcategory", getJsonString(productNode, "subcategory", "productSubCategory", "subType", "subClassification"));
                
                // 处理品牌信息
                if (productNode.has("brands") && productNode.get("brands").isArray()) {
                    List<String> brands = new ArrayList<>();
                    for (JsonNode brandNode : productNode.get("brands")) {
                        if (brandNode.has("brand")) {
                            brands.add(brandNode.get("brand").asText());
                        } else if (brandNode.isTextual()) {
                            brands.add(brandNode.asText());
                        }
                    }
                    alert.put("brands", String.join(", ", brands));
                    if (!brands.isEmpty()) {
                        alert.put("brand", brands.get(0)); // 第一个品牌作为主要品牌
                    }
                } else {
                    // 尝试直接从product节点获取品牌信息
                    String brand = getJsonString(productNode, "brand", "manufacturer", "producer", "company");
                    if (!brand.isEmpty()) {
                        alert.put("brand", brand);
                    }
                }
                
                // 处理产品描述
                String productDescription = getJsonString(productNode, "description", "details", "summary", "specification");
                if (!productDescription.isEmpty()) {
                    alert.put("product_description", productDescription);
                }
                
                // 处理产品型号/标识符
                String productModel = getJsonString(productNode, "model", "identifier", "serialNumber", "batchNumber", "lotNumber");
                if (!productModel.isEmpty()) {
                    alert.put("product_model", productModel);
                }
                
                System.out.println("解析产品信息: " + productName + " | 具体名称: " + nameSpecific + " | 品牌: " + alert.get("brand") + " | 类别: " + alert.get("category"));
            } else {
                // 如果product不是对象，尝试直接获取产品信息
                String product = getJsonString(alertNode, "product", "productName", "productType", "productDescription");
                if (!product.isEmpty()) {
                    alert.put("product", product);
                }
            }
            
            // 处理风险信息
            if (alertNode.has("risk") && alertNode.get("risk").isObject()) {
                JsonNode riskNode = alertNode.get("risk");
                alert.put("risk_type", getJsonString(riskNode, "riskType", "risk_type", "hazardType"));
                
                if (riskNode.has("riskType") && riskNode.get("riskType").isArray()) {
                    List<String> risks = new ArrayList<>();
                    for (JsonNode riskTypeNode : riskNode.get("riskType")) {
                        if (riskTypeNode.has("name")) {
                            risks.add(riskTypeNode.get("name").asText());
                        }
                    }
                    alert.put("risks", String.join(", ", risks));
                    if (!risks.isEmpty()) {
                        alert.put("risk", risks.get(0)); // 第一个风险作为主要风险
                    }
                }
            }
            
            // 其他字段
            alert.put("country", getJsonString(alertNode, "country", "countryCode", "originCountry"));
            alert.put("notifying_country", getJsonString(alertNode, "notifyingCountry", "notifying_country", "notifierCountry"));
            alert.put("category", getJsonString(alertNode, "category", "productCategory"));
            alert.put("subcategory", getJsonString(alertNode, "subcategory", "productSubCategory"));
            alert.put("description", getJsonString(alertNode, "description", "summary", "details"));
            alert.put("measures", getJsonString(alertNode, "measures", "actions", "correctiveActions"));
            alert.put("url", getJsonString(alertNode, "url", "link", "detailUrl"));
            
            // 处理数组字段
            if (alertNode.has("brands") && alertNode.get("brands").isArray()) {
                List<String> brands = new ArrayList<>();
                for (JsonNode brandNode : alertNode.get("brands")) {
                    brands.add(brandNode.asText());
                }
                alert.put("brands", String.join(", ", brands));
            }
            
            if (alertNode.has("risks") && alertNode.get("risks").isArray()) {
                List<String> risks = new ArrayList<>();
                for (JsonNode riskNode : alertNode.get("risks")) {
                    risks.add(riskNode.asText());
                }
                alert.put("risks", String.join(", ", risks));
            }
            
            // 检查是否有任何有效数据
            boolean hasData = false;
            for (String value : alert.values()) {
                if (!value.isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            
            if (hasData) {
                System.out.println("成功解析预警数据: " + alert.get("alert_number"));
            } else {
                System.out.println("预警节点没有有效数据");
            }
            
        } catch (Exception e) {
            System.err.println("解析单个预警数据时出错: " + e.getMessage());
        }
        
        return alert;
    }
    
    /**
     * 从JSON节点获取字符串值
     */
    private String getJsonString(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                    return node.get(fieldName).asText().trim();
                }
            } catch (Exception e) {
                // 忽略错误，尝试下一个字段名
            }
        }
        return "";
    }
    
    /**
     * 解析HTML内容提取预警信息（使用精确CSS选择器）
     */
    public List<Map<String, String>> parseHtmlContent(String htmlContent) {
        List<Map<String, String>> alerts = new ArrayList<>();
        
        try {
            Document doc = Jsoup.parse(htmlContent);
            
            // 使用精确的CSS选择器查找预警卡片
            Elements alertCards = doc.select("#ecl-main-content > div > ng-component > div > div > div.ecl-col-m-9 > span > div.ecl-col-m-12 > div > div > div > app-alert-card > ecl-card");
            System.out.println("找到 " + alertCards.size() + " 个预警卡片");
            
            for (int i = 0; i < alertCards.size(); i++) {
                Element card = alertCards.get(i);
                Map<String, String> alert = new HashMap<>();
                
                // 提取预警号码 - 从按钮中获取
                Element alertNumberElement = card.selectFirst("button.custom_bt, .ecl-button");
                if (alertNumberElement != null) {
                    alert.put("alert_number", alertNumberElement.text().trim());
                }
                
                // 使用精确CSS选择器提取发布日期
                String dateSelector = "#ecl-main-content > div > ng-component > div > div > div.ecl-col-m-9 > span > div.ecl-col-m-12 > div:nth-child(" + (i + 1) + ") > div > div:nth-child(1) > app-alert-card > ecl-card > ecl-card-body > ecl-content-block > ul > li";
                Element dateElement = doc.selectFirst(dateSelector);
                if (dateElement != null) {
                    String dateText = dateElement.text().trim();
                    alert.put("publication_date", dateText);
                    System.out.println("第" + (i + 1) + "个预警的发布日期: " + dateText);
                }
                
                // 使用精确CSS选择器提取URL
                String urlSelector = "#ecl-main-content > div > ng-component > div > div > div.ecl-col-m-9 > span > div.ecl-col-m-12 > div:nth-child(" + (i + 1) + ") > div > div:nth-child(1) > app-alert-card > ecl-card";
                Element urlElement = doc.selectFirst(urlSelector);
                if (urlElement != null) {
                    // 查找链接
                    Element linkElement = urlElement.selectFirst("a[href]");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (!href.isEmpty()) {
                            if (href.startsWith("/")) {
                                alert.put("url", "https://ec.europa.eu" + href);
                            } else {
                                alert.put("url", href);
                            }
                            System.out.println("第" + (i + 1) + "个预警的URL: " + alert.get("url"));
                        }
                    }
                }
                
                // 提取描述列表中的信息
                Elements descriptionLists = card.select(".ecl-description-list");
                for (Element dl : descriptionLists) {
                    Element dt = dl.selectFirst(".ecl-description-list__term");
                    Element dd = dl.selectFirst(".ecl-description-list__definition");
                    
                    if (dt != null && dd != null) {
                        String term = dt.text().trim();
                        String definition = dd.text().trim();
                        
                        switch (term) {
                            case "产品":
                            case "Product":
                                alert.put("product", definition);
                                break;
                            case "品牌":
                            case "Brand":
                                alert.put("brand", definition);
                                break;
                            case "风险":
                            case "Risk":
                                alert.put("risk", definition);
                                break;
                        }
                    }
                }
                
                // 提取图片URL（如果需要的话）
                Element imgElement = card.selectFirst("img.ecl-card__image");
                if (imgElement != null) {
                    String imgSrc = imgElement.attr("src");
                    if (!imgSrc.isEmpty()) {
                        alert.put("image_url", imgSrc);
                    }
                }
                
                // 如果至少有一个重要字段有值，则添加到结果中
                if (hasValidAlertData(alert)) {
                    alerts.add(alert);
                    System.out.println("解析出预警: " + alert.get("alert_number"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("解析HTML内容时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return alerts;
    }
    
    /**
     * 检查预警数据是否有效
     */
    private boolean hasValidAlertData(Map<String, String> alert) {
        return !alert.getOrDefault("alert_number", "").isEmpty() || 
               !alert.getOrDefault("product", "").isEmpty() || 
               !alert.getOrDefault("brand", "").isEmpty();
    }
    
    /**
     * 保存数据到CSV文件
     */
    public void saveToCsv(List<Map<String, String>> alerts, String filePath) {
        try {
            if (alerts.isEmpty()) {
                System.out.println("没有数据需要保存");
                return;
            }
            
            // 确保输出目录存在
            File outputDir = new File("crawler_output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
                System.out.println("创建输出目录: " + outputDir.getAbsolutePath());
            }
            
            // 如果文件路径不包含目录，则添加到crawler_output目录
            String finalFilePath = filePath;
            if (!filePath.contains("/") && !filePath.contains("\\")) {
                finalFilePath = "crawler_output/" + filePath;
            }
            
            // 定义CSV表头 - 包含增强的产品信息字段
            String[] headers = {
                "alert_number", "publication_date", "product", "product_name_specific", "product_description", "product_model",
                "brand", "brands", "category", "subcategory", "risk", "risk_type",
                "country", "notifying_country", "description", "measures", "url", "crawl_time"
            };
            
            // 准备数据
            List<String[]> csvData = new ArrayList<>();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            for (Map<String, String> alert : alerts) {
                String[] row = new String[headers.length];
                for (int i = 0; i < headers.length - 1; i++) {
                    row[i] = alert.getOrDefault(headers[i], "");
                }
                row[headers.length - 1] = currentTime; // 添加爬取时间
                csvData.add(row);
            }
            
            // 导出到CSV
            csvExporter.exportSimpleToCsv(csvData, headers, finalFilePath);
            System.out.println("✅ 数据已保存到: " + finalFilePath);
            System.out.println("📊 总共保存了 " + alerts.size() + " 条预警数据");
            System.out.println("📁 文件路径: " + new File(finalFilePath).getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ 保存CSV文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试带搜索参数的爬虫（搜索skin关键词 + Electrical appliances and equipment产品类别）
     */
    public static void testCrawlerWithSearch() {
        System.out.println("开始测试带搜索参数的爬虫（搜索'skin'关键词 + 'Electrical appliances and equipment'产品类别）...");
        
        Eu_event crawler = new Eu_event();
        
        try {
            // 搜索包含"skin"的预警，产品类别为"Electrical appliances and equipment"
            List<Integer> years = new ArrayList<>();
            years.add(2025); // 筛选2025年的数据
            
            System.out.println("搜索参数:");
            System.out.println("  关键词: skin");
            System.out.println("  产品类别: Electrical appliances and equipment");
            System.out.println("  年份筛选: 2025");
            System.out.println("  排序: 按发布日期降序");
            System.out.println("  每页大小: 9条");
            System.out.println("  最大页数: 2页");
            
            // 组合搜索关键词：skin + Electrical appliances and equipment
            String combinedSearchKeyword = "skin Electrical appliances and equipment";
            
            List<Map<String, String>> alerts = crawler.crawlSafetyGateAlerts(
                2,                    // 最大页数
                combinedSearchKeyword, // 组合搜索关键词
                "PUBLICATION_DATE",   // 排序字段
                "DESC",               // 排序方向
                "en",                 // 语言
                years,                // 年份筛选
                9                     // 每页大小（根据真实API请求）
            );
            
            if (!alerts.isEmpty()) {
                // 生成文件名
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "EU_SafetyGate_Alerts_Skin_Electrical_Search_" + timestamp + ".csv";
                
                // 保存到CSV文件
                crawler.saveToCsv(alerts, fileName);
                
                System.out.println("✅ 搜索完成，找到 " + alerts.size() + " 条'skin + Electrical appliances and equipment'相关预警");
                
                // 显示前几条数据示例
                System.out.println("\n数据示例:");
                for (int i = 0; i < Math.min(3, alerts.size()); i++) {
                    Map<String, String> alert = alerts.get(i);
                    System.out.println("预警 " + (i + 1) + ":");
                    alert.forEach((key, value) -> {
                        if (!value.isEmpty()) {
                            System.out.println("  " + key + ": " + value);
                        }
                    });
                    System.out.println();
                }
            } else {
                System.out.println("❌ 没有找到'skin + Electrical appliances and equipment'相关预警");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 搜索过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("skin + Electrical appliances and equipment关键词搜索测试完成！");
    }
    
    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        // 测试带搜索参数的爬虫（只测试skin关键词）
        testCrawlerWithSearch();
        
        System.out.println("测试完成！");
    }
    
    /**
     * 测试API爬虫
     */
    public static void testApiCrawler() {
        System.out.println("开始测试API爬虫...");
        
        Eu_event crawler = new Eu_event();
        
        try {
            // 爬取前2页数据
            List<Map<String, String>> alerts = crawler.crawlSafetyGateAlerts(2);
            
            if (!alerts.isEmpty()) {
                // 生成文件名
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = "EU_SafetyGate_Alerts_" + timestamp + ".csv";
                
                // 保存到CSV文件
                crawler.saveToCsv(alerts, fileName);
                
                // 显示部分数据示例
                System.out.println("\n数据示例:");
                for (int i = 0; i < Math.min(3, alerts.size()); i++) {
                    Map<String, String> alert = alerts.get(i);
                    System.out.println("预警 " + (i + 1) + ":");
                    alert.forEach((key, value) -> {
                        if (!value.isEmpty()) {
                            System.out.println("  " + key + ": " + value);
                        }
                    });
                    System.out.println();
                }
            } else {
                System.out.println("没有爬取到任何数据");
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("API爬虫测试完成！");
    }
}
