package com.certification.crawler.countrydata.eu;

import com.certification.entity.common.EuSafetyGateAlert;
import com.certification.repository.common.EuSafetyGateAlertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧盟安全门预警系统爬虫
 * 爬取Safety Gate Alert数据并保存到数据库
 */
@Slf4j
@Component
public class EuSafetyGateAlertCrawler {
    
    private static final String BASE_URL = "https://ec.europa.eu/safety-gate-alerts/screen/search";
    private static final String API_URL = "https://ec.europa.eu/safety-gate-alerts/public/api/search";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final EuSafetyGateAlertRepository repository;
    
    public EuSafetyGateAlertCrawler(EuSafetyGateAlertRepository repository) {
        this.repository = repository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 爬取Safety Gate预警数据并保存到数据库
     * @param maxPages 最大爬取页数
     * @return 爬取结果统计
     */
    public Map<String, Object> crawlSafetyGateAlerts(int maxPages) {
        return crawlSafetyGateAlerts(maxPages, "", "", "", "");
    }
    
    /**
     * 爬取Safety Gate预警数据（带搜索参数）
     * @param maxPages 最大爬取页数
     * @param searchKeyword 搜索关键词
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @param language 语言
     * @return 爬取结果统计
     */
    public Map<String, Object> crawlSafetyGateAlerts(int maxPages, String searchKeyword, String sortField, String sortDirection, String language) {
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
     * @return 爬取结果统计
     */
    public Map<String, Object> crawlSafetyGateAlerts(int maxPages, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        List<EuSafetyGateAlert> allAlerts = new ArrayList<>();
        int totalSaved = 0;
        int totalSkipped = 0;
        
        log.info("开始爬取Safety Gate预警数据...");
        log.info("搜索关键词: {}", searchKeyword);
        log.info("最大页数: {}", maxPages);
        log.info("每页大小: {}", pageSize);
        
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
                log.info("访问URL: {}", BASE_URL);
                long startTime = System.currentTimeMillis();
                driver.get(BASE_URL);
                long endTime = System.currentTimeMillis();
                
                log.info("页面加载时间: {}ms", endTime - startTime);
                log.info("页面标题: {}", driver.getTitle());
                
                // 等待页面加载
                Thread.sleep(5000);
                
                // 爬取多页数据
                for (int page = 0; page < maxPages; page++) {
                    log.info("正在爬取第{}页数据...", page + 1);
                    
                    // 使用API获取数据
                    List<EuSafetyGateAlert> pageAlerts = fetchAlertsFromAPI(page, searchKeyword, sortField, sortDirection, language, years, pageSize);
                    
                    if (pageAlerts.isEmpty()) {
                        log.info("第{}页没有数据，停止爬取", page + 1);
                        break;
                    }
                    
                    // 保存到数据库
                    int[] result = saveBatchToDatabase(pageAlerts);
                    totalSaved += result[0];
                    totalSkipped += result[1];
                    
                    allAlerts.addAll(pageAlerts);
                    log.info("第{}页爬取完成，获取到{}条预警数据，保存{}条，跳过{}条", 
                            page + 1, pageAlerts.size(), result[0], result[1]);
                    
                    // 等待一下再爬取下一页
                    Thread.sleep(2000);
                }
                
            } finally {
                driver.quit();
            }
            
        } catch (Exception e) {
            log.error("爬取过程中发生错误: {}", e.getMessage(), e);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("totalPages", maxPages);
        result.put("totalAlerts", allAlerts.size());
        
        log.info("爬取完成！共保存{}条记录，跳过{}条重复记录", totalSaved, totalSkipped);
        return result;
    }
    
    /**
     * 通过API获取预警数据
     */
    private List<EuSafetyGateAlert> fetchAlertsFromAPI(int page, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        List<EuSafetyGateAlert> alerts = new ArrayList<>();
        
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
            
            log.debug("API请求URL: {}", API_URL);
            log.debug("API请求体: {}", requestBody);
            
            // 发送请求
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                log.debug("API响应长度: {} 字节", responseBody.length());
                
                // 解析JSON响应
                alerts = parseApiResponse(responseBody);
            } else {
                log.error("API请求失败，状态码: {}", response.statusCode());
                log.error("响应内容: {}", response.body());
            }
            
        } catch (Exception e) {
            log.error("API请求时出错: {}", e.getMessage(), e);
        }
        
        return alerts;
    }
    
    /**
     * 构建API请求体
     */
    private String buildApiRequestBody(int page, String searchKeyword, String sortField, String sortDirection, String language, List<Integer> years, int pageSize) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 搜索条件
        Map<String, Object> criteria = new HashMap<>();
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            criteria.put("fullTextSearch", "");
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
        pagination.put("numberElements", pageSize > 0 ? pageSize : 25);
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
            log.error("构建请求体时出错: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 解析API响应
     */
    private List<EuSafetyGateAlert> parseApiResponse(String responseBody) {
        List<EuSafetyGateAlert> alerts = new ArrayList<>();
        
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            // 尝试多种可能的数据结构
            JsonNode searchResults = null;
            
            if (rootNode.has("content")) {
                searchResults = rootNode.get("content");
            } else if (rootNode.has("searchResults")) {
                searchResults = rootNode.get("searchResults");
            } else if (rootNode.has("results")) {
                searchResults = rootNode.get("results");
            } else if (rootNode.has("data")) {
                searchResults = rootNode.get("data");
            } else if (rootNode.has("alerts")) {
                searchResults = rootNode.get("alerts");
            } else if (rootNode.has("notifications")) {
                searchResults = rootNode.get("notifications");
            } else if (rootNode.isArray()) {
                searchResults = rootNode;
            }
            
            if (searchResults != null && searchResults.isArray()) {
                for (JsonNode alertNode : searchResults) {
                    EuSafetyGateAlert alert = parseAlertFromJson(alertNode);
                    if (alert != null) {
                        alerts.add(alert);
                    }
                }
            }
            
            log.info("从API响应中解析出{}条预警数据", alerts.size());
            
        } catch (Exception e) {
            log.error("解析API响应时出错: {}", e.getMessage(), e);
        }
        
        return alerts;
    }
    
    /**
     * 从JSON节点解析单个预警数据
     */
    private EuSafetyGateAlert parseAlertFromJson(JsonNode alertNode) {
        try {
            EuSafetyGateAlert alert = new EuSafetyGateAlert();
            
            // 提取基本字段
            alert.setAlertNumber(getJsonString(alertNode, "reference", "alertNumber", "alert_number", "id", "notificationId"));
            alert.setPublicationDate(parseDate(getJsonString(alertNode, "publicationDate", "publication_date", "date", "createdDate")));
            
            // 处理产品信息
            if (alertNode.has("product") && alertNode.get("product").isObject()) {
                JsonNode productNode = alertNode.get("product");
                
                String productName = getJsonString(productNode, "name", "productName", "productType", "title", "description", "nameSpecific");
                alert.setProduct(productName);
                
                alert.setCategory(getJsonString(productNode, "category", "productCategory", "type", "classification"));
                
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
                    if (!brands.isEmpty()) {
                        alert.setBrand(brands.get(0));
                    }
                } else {
                    String brand = getJsonString(productNode, "brand", "manufacturer", "producer", "company");
                    alert.setBrand(brand);
                }
                
                alert.setProductDescription(getJsonString(productNode, "description", "details", "summary", "specification"));
                alert.setProductModel(getJsonString(productNode, "model", "identifier", "serialNumber", "batchNumber", "lotNumber"));
            } else {
                String product = getJsonString(alertNode, "product", "productName", "productType", "productDescription");
                alert.setProduct(product);
            }
            
            // 处理风险信息
            if (alertNode.has("risk") && alertNode.get("risk").isObject()) {
                JsonNode riskNode = alertNode.get("risk");
                alert.setRiskType(getJsonString(riskNode, "riskType", "risk_type", "hazardType"));
                
                if (riskNode.has("riskType") && riskNode.get("riskType").isArray()) {
                    List<String> risks = new ArrayList<>();
                    for (JsonNode riskTypeNode : riskNode.get("riskType")) {
                        if (riskTypeNode.has("name")) {
                            risks.add(riskTypeNode.get("name").asText());
                        }
                    }
                    if (!risks.isEmpty()) {
                        alert.setRisk(risks.get(0));
                    }
                }
            }
            
            // 其他字段
            alert.setCountry(getJsonString(alertNode, "country", "countryCode", "originCountry"));
            alert.setNotifyingCountry(getJsonString(alertNode, "notifyingCountry", "notifying_country", "notifierCountry"));
            alert.setDescription(getJsonString(alertNode, "description", "summary", "details"));
            alert.setMeasures(getJsonString(alertNode, "measures", "actions", "correctiveActions"));
            alert.setUrl(getJsonString(alertNode, "url", "link", "detailUrl"));
            
            // 设置默认值
            alert.setDataSource("Safety Gate Alert");
            alert.setJdCountry("EU");
            alert.setCrawlTime(LocalDateTime.now());
            
            // 根据风险类型设置风险等级
            setRiskLevel(alert);
            
            return alert;
            
        } catch (Exception e) {
            log.error("解析单个预警数据时出错: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 根据风险信息设置风险等级
     */
    private void setRiskLevel(EuSafetyGateAlert alert) {
        String risk = alert.getRisk();
        String riskType = alert.getRiskType();
        
        if (risk != null || riskType != null) {
            String riskText = (risk != null ? risk : "") + " " + (riskType != null ? riskType : "");
            riskText = riskText.toLowerCase();
            
            if (riskText.contains("death") || riskText.contains("fatal") || riskText.contains("serious injury") || 
                riskText.contains("fire") || riskText.contains("explosion") || riskText.contains("electrical shock")) {
                alert.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.HIGH);
            } else if (riskText.contains("injury") || riskText.contains("harm") || riskText.contains("damage") || 
                       riskText.contains("malfunction") || riskText.contains("failure")) {
                alert.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.MEDIUM);
            } else {
                alert.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.LOW);
            }
        } else {
            alert.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.MEDIUM);
        }
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
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试多种日期格式
            String[] formats = {
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS"
            };
            
            for (String format : formats) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                } catch (Exception e) {
                    // 继续尝试下一个格式
                }
            }
            
            // 如果所有格式都失败，返回null
            log.warn("无法解析日期: {}", dateStr);
            return null;
            
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }
    
    /**
     * 批量保存数据到数据库
     */
    private int[] saveBatchToDatabase(List<EuSafetyGateAlert> alerts) {
        int saved = 0;
        int skipped = 0;
        
        for (EuSafetyGateAlert alert : alerts) {
            try {
                // 检查是否已存在相同的预警编号
                if (alert.getAlertNumber() != null && !alert.getAlertNumber().isEmpty()) {
                    if (repository.existsByAlertNumber(alert.getAlertNumber())) {
                        log.debug("预警编号{}已存在，跳过保存", alert.getAlertNumber());
                        skipped++;
                        continue;
                    }
                }
                
                // 保存到数据库
                repository.save(alert);
                saved++;
                log.debug("成功保存预警: {}", alert.getAlertNumber());
                
            } catch (Exception e) {
                log.error("保存预警失败: {}", e.getMessage());
                skipped++;
            }
        }
        
        log.info("批量保存完成: 保存{}条，跳过{}条", saved, skipped);
        return new int[]{saved, skipped};
    }
    
    /**
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        EU安全门预警爬虫程序");
        System.out.println("==========================================");
        
        try {
            // 注意：这里需要Spring上下文，实际使用时应该通过Spring容器获取
            System.out.println("请通过Spring容器运行此爬虫");
            
        } catch (Exception e) {
            System.err.println("爬虫执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
