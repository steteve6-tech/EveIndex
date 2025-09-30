package com.certification.crawler.countrydata.eu.others;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * EU制造商数据爬虫
 * 从EUDAMED系统爬取制造商信息并保存到CSV文件
 */
@Component
public class Eu_Manufacturers {
    
    private static final String API_BASE = "https://ec.europa.eu/tools/eudamed/api";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    
    public Eu_Manufacturers() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    /**
     * 爬取EU制造商数据
     */
    public void crawlEuManufacturers() {
        System.out.println("开始爬取EU制造商数据...");
        
        try {
            // 获取制造商列表
            List<String> manufacturerIds = getManufacturerIds();
            System.out.println("找到 " + manufacturerIds.size() + " 个制造商ID");
            
            // 并发获取制造商详细信息
            List<Map<String, Object>> manufacturers = getManufacturerDetails(manufacturerIds);
            
            // 保存到CSV文件
            saveToCSV(manufacturers);
            
            System.out.println("EU制造商数据爬取完成，共获取 " + manufacturers.size() + " 条记录");
            
        } catch (Exception e) {
            System.err.println("爬取EU制造商数据失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
    
    /**
     * 获取制造商ID列表
     */
    private List<String> getManufacturerIds() throws IOException, InterruptedException {
        List<String> manufacturerIds = new ArrayList<>();
        
        // 这里需要根据实际的API端点来获取制造商ID列表
        // 由于没有提供具体的搜索API，我们使用一些已知的制造商ID作为示例
        // 实际使用时需要根据EUDAMED的搜索API来获取完整的ID列表
        
        // 示例制造商ID（实际使用时需要从搜索API获取）
        manufacturerIds.add("62db5eb6-8769-4945-ae15-65dc1a65c717"); // Visia Lab S.r.l.
        
        // 可以添加更多已知的制造商ID
        // manufacturerIds.add("another-manufacturer-id");
        
        return manufacturerIds;
    }
    
    /**
     * 并发获取制造商详细信息
     */
    private List<Map<String, Object>> getManufacturerDetails(List<String> manufacturerIds) {
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        for (String manufacturerId : manufacturerIds) {
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return getManufacturerDetail(manufacturerId);
                } catch (Exception e) {
                    System.err.println("获取制造商详情失败，ID: " + manufacturerId + ", 错误: " + e.getMessage());
                    return null;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有请求完成
        List<Map<String, Object>> manufacturers = new ArrayList<>();
        for (CompletableFuture<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> manufacturer = future.get(30, TimeUnit.SECONDS);
                if (manufacturer != null) {
                    manufacturers.add(manufacturer);
                }
            } catch (Exception e) {
                System.err.println("等待制造商详情请求失败: " + e.getMessage());
            }
        }
        
        return manufacturers;
    }
    
    /**
     * 获取单个制造商的详细信息
     */
    private Map<String, Object> getManufacturerDetail(String manufacturerId) throws IOException, InterruptedException {
        String url = API_BASE + "/actors/" + manufacturerId + "/publicInformation?languageIso2Code=en";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Cache-Control", "No-Cache")
                .header("User-Agent", USER_AGENT)
                .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                .header("Sec-Ch-Ua-Mobile", "?0")
                .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", "https://ec.europa.eu/tools/eudamed/")
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return parseManufacturerData(response.body());
        } else {
            throw new RuntimeException("API请求失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
    }
    
    /**
     * 解析制造商数据
     */
    private Map<String, Object> parseManufacturerData(String jsonResponse) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode actorData = rootNode.get("actorDataPublicView");
        
        if (actorData == null) {
            throw new RuntimeException("无法找到actorDataPublicView节点");
        }
        
        Map<String, Object> manufacturer = new java.util.HashMap<>();
        
        // 基本信息
        manufacturer.put("uuid", getTextValue(actorData, "uuid"));
        manufacturer.put("ulid", getTextValue(actorData, "ulid"));
        manufacturer.put("eudamedIdentifier", getTextValue(actorData, "eudamedIdentifier"));
        
        // 制造商名称
        JsonNode nameNode = actorData.get("name");
        if (nameNode != null && nameNode.has("texts")) {
            JsonNode textsNode = nameNode.get("texts");
            if (textsNode.isArray() && textsNode.size() > 0) {
                manufacturer.put("name", getTextValue(textsNode.get(0), "text"));
            }
        }
        
        // 制造商类型
        JsonNode typeNode = actorData.get("type");
        if (typeNode != null) {
            manufacturer.put("typeCode", getTextValue(typeNode, "code"));
            manufacturer.put("typeSrnCode", getTextValue(typeNode, "srnCode"));
            manufacturer.put("typeCategory", getTextValue(typeNode, "category"));
        }
        
        // 状态
        JsonNode statusNode = actorData.get("actorStatus");
        if (statusNode != null) {
            manufacturer.put("statusCode", getTextValue(statusNode, "code"));
        }
        
        // 国家信息
        JsonNode countryNode = actorData.get("country");
        if (countryNode != null) {
            manufacturer.put("countryName", getTextValue(countryNode, "name"));
            manufacturer.put("countryIso2Code", getTextValue(countryNode, "iso2Code"));
            manufacturer.put("countryType", getTextValue(countryNode, "type"));
        }
        
        // 地址信息
        JsonNode addressNode = actorData.get("actorAddress");
        if (addressNode != null) {
            manufacturer.put("streetName", getTextValue(addressNode, "streetName"));
            manufacturer.put("buildingNumber", getTextValue(addressNode, "buildingNumber"));
            manufacturer.put("cityName", getTextValue(addressNode, "cityName"));
            manufacturer.put("postalZone", getTextValue(addressNode, "postalZone"));
            
            JsonNode addressCountryNode = addressNode.get("country");
            if (addressCountryNode != null) {
                manufacturer.put("addressCountryName", getTextValue(addressCountryNode, "name"));
                manufacturer.put("addressCountryIso2Code", getTextValue(addressCountryNode, "iso2Code"));
            }
        }
        
        // 联系信息
        manufacturer.put("electronicMail", getTextValue(actorData, "electronicMail"));
        manufacturer.put("website", getTextValue(actorData, "website"));
        manufacturer.put("telephone", getTextValue(actorData, "telephone"));
        
        // 欧洲增值税号
        manufacturer.put("europeanVatNumber", getTextValue(actorData, "europeanVatNumber"));
        manufacturer.put("europeanVatNumberApplicable", getBooleanValue(actorData, "europeanVatNumberApplicable"));
        
        // 监管合规负责人
        JsonNode regulatoryComplianceNode = actorData.get("regulatoryComplianceResponsibles");
        if (regulatoryComplianceNode != null && regulatoryComplianceNode.isArray() && regulatoryComplianceNode.size() > 0) {
            JsonNode firstResponsible = regulatoryComplianceNode.get(0);
            manufacturer.put("responsibleFirstName", getTextValue(firstResponsible, "firstName"));
            manufacturer.put("responsibleFamilyName", getTextValue(firstResponsible, "familyName"));
            manufacturer.put("responsibleEmail", getTextValue(firstResponsible, "electronicMail"));
            manufacturer.put("responsibleTelephone", getTextValue(firstResponsible, "telephone"));
        }
        
        // 验证机构信息
        manufacturer.put("validatorName", getTextValue(actorData, "validatorName"));
        manufacturer.put("validatorSrn", getTextValue(actorData, "validatorSrn"));
        manufacturer.put("validatorEmail", getTextValue(actorData, "validatorEmail"));
        manufacturer.put("validatorTelephone", getTextValue(actorData, "validatorTelephone"));
        
        // 版本信息
        manufacturer.put("versionNumber", getIntValue(actorData, "versionNumber"));
        manufacturer.put("lastUpdateDate", getTextValue(actorData, "lastUpdateDate"));
        
        JsonNode versionStateNode = actorData.get("versionState");
        if (versionStateNode != null) {
            manufacturer.put("versionStatusCode", getTextValue(versionStateNode, "code"));
        }
        
        // 爬取时间
        manufacturer.put("crawlTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return manufacturer;
    }
    
    /**
     * 获取文本值
     */
    private String getTextValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode.isNull() ? null : fieldNode.asText();
        }
        return null;
    }
    
    /**
     * 获取布尔值
     */
    private Boolean getBooleanValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode.isNull() ? null : fieldNode.asBoolean();
        }
        return null;
    }
    
    /**
     * 获取整数值
     */
    private Integer getIntValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode.isNull() ? null : fieldNode.asInt();
        }
        return null;
    }
    
    /**
     * 保存数据到CSV文件
     */
    private void saveToCSV(List<Map<String, Object>> manufacturers) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "eu_manufacturers_" + timestamp + ".csv";
        
        try (FileWriter writer = new FileWriter(filename)) {
            // 写入CSV头部
            String[] headers = {
                "uuid", "ulid", "eudamedIdentifier", "name", "typeCode", "typeSrnCode", "typeCategory",
                "statusCode", "countryName", "countryIso2Code", "countryType", "streetName", "buildingNumber",
                "cityName", "postalZone", "addressCountryName", "addressCountryIso2Code", "electronicMail",
                "website", "telephone", "europeanVatNumber", "europeanVatNumberApplicable",
                "responsibleFirstName", "responsibleFamilyName", "responsibleEmail", "responsibleTelephone",
                "validatorName", "validatorSrn", "validatorEmail", "validatorTelephone",
                "versionNumber", "lastUpdateDate", "versionStatusCode", "crawlTime"
            };
            
            // 写入头部
            writer.append(String.join(",", headers)).append("\n");
            
            // 写入数据
            for (Map<String, Object> manufacturer : manufacturers) {
                List<String> row = new ArrayList<>();
                for (String header : headers) {
                    Object value = manufacturer.get(header);
                    String cellValue = value != null ? value.toString() : "";
                    // 转义CSV中的逗号和引号
                    if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                        cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                    }
                    row.add(cellValue);
                }
                writer.append(String.join(",", row)).append("\n");
            }
        }
        
        System.out.println("数据已保存到文件: " + filename);
    }
    
    /**
     * 主方法，用于测试和直接运行
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        EU制造商爬虫程序");
        System.out.println("==========================================");
        
        try {
            // 创建爬虫实例
            Eu_Manufacturers crawler = new Eu_Manufacturers();
            
            System.out.println("开始执行EU制造商数据爬取...");
            System.out.println("目标API: https://ec.europa.eu/tools/eudamed/api/actors/{id}/publicInformation");
            System.out.println("输出格式: CSV文件");
            System.out.println("测试制造商ID: 62db5eb6-8769-4945-ae15-65dc1a65c717 (Visia Lab S.r.l.)");
            System.out.println("------------------------------------------");
            
            // 执行爬虫
            long startTime = System.currentTimeMillis();
            crawler.crawlEuManufacturers();
            long endTime = System.currentTimeMillis();
            
            System.out.println("------------------------------------------");
            System.out.println("爬虫执行完成！");
            System.out.println("总耗时: " + (endTime - startTime) + " 毫秒");
            System.out.println("请检查当前目录下的CSV文件");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("爬虫执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
