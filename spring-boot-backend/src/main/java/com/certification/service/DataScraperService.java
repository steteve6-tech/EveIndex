package com.certification.service;

import com.certification.dto.ApiStandardData;
import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataScraperService {
    
    @Value("${app.scraper.user-agent}")
    private String userAgent;
    
    @Value("${app.scraper.timeout}")
    private int timeout;
    
    /**
     * FCC Equipment Authorization Database
     */
    public List<ApiStandardData> fetchFCCDatabase(String apiKey) {
        // TODO: 使用同步HTTP客户端实现FCC数据抓取
        return new ArrayList<>();
    }
    
    /**
     * 中国 CCC 认证查询
     */
    public List<ApiStandardData> fetchCCCDatabase() {
        // TODO: 使用同步HTTP客户端实现CCC数据抓取
        return new ArrayList<>();
    }
    
    /**
     * 日本 TELEC 认证查询
     */
    public List<ApiStandardData> fetchTELECDatabase() {
        // TODO: 使用同步HTTP客户端实现TELEC数据抓取
        return new ArrayList<>();
    }
    
    /**
     * 韩国 KC 认证查询
     */
    public List<ApiStandardData> fetchKCDatabase() {
        // TODO: 使用同步HTTP客户端实现KC数据抓取
        return new ArrayList<>();
    }
    
    /**
     * ETSI 标准数据库
     */
    public List<ApiStandardData> fetchETSIStandards() {
        // TODO: 使用同步HTTP客户端实现ETSI数据抓取
        return new ArrayList<>();
    }
    
    /**
     * 全球产品召回API
     */
    public List<ApiStandardData> fetchGlobalRecalls() {
        // TODO: 使用同步HTTP客户端实现全球产品召回数据抓取
        return new ArrayList<>();
    }
    
    /**
     * Wi-Fi Alliance 认证数据库
     */
    public List<ApiStandardData> fetchWiFiAllianceCertifications() {
        // TODO: 使用同步HTTP客户端实现Wi-Fi Alliance数据抓取
        return new ArrayList<>();
    }
    
    /**
     * Compliance & Risks C2P API 数据抓取
     */
    public List<ApiStandardData> fetchComplianceC2P(String apiKey) {
        // TODO: 使用同步HTTP客户端实现Compliance & Risks C2P数据抓取
        return new ArrayList<>();
    }
    
    /**
     * Emergo RAMS API 数据抓取
     */
    public List<ApiStandardData> fetchEmergoRAMS(String apiKey) {
        // TODO: 使用同步HTTP客户端实现Emergo RAMS数据抓取
        return new ArrayList<>();
    }
    
    /**
     * RSS 数据抓取
     */
    public List<ApiStandardData> fetchRSSFeed(String url) {
        // TODO: 使用同步HTTP客户端实现RSS数据抓取
        return new ArrayList<>();
    }
    
    /**
     * OJEU HTML 页面抓取
     */
    public List<ApiStandardData> fetchOJEUPage(String url) {
        // TODO: 使用同步HTTP客户端实现OJEU页面数据抓取
        return new ArrayList<>();
    }
    
    // 私有方法：构建请求体
    private String buildFCCRequestBody() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return String.format(
            "calledFromFrame=N&applFileNum=&grantee=&productDescription=wireless&dateReceivedFrom=%s&dateReceivedTo=%s",
            thirtyDaysAgo.format(formatter),
            today.format(formatter)
        );
    }
    
    private String buildTELECRequestBody() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return String.format(
            "pageID=3&IT=1&mode=0&name=&number=&date_from=%s&date_to=%s",
            thirtyDaysAgo.format(formatter),
            today.format(formatter)
        );
    }
    
    private String buildKCRequestBody() {
        return "searchGbn=A&prdNm=무선&pageUnit=50&pageIndex=1";
    }
    
    // 私有方法：解析数据
    private List<ApiStandardData> parseFCCData(String html) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("tr");
            
            for (Element row : rows) {
                String rowText = row.text();
                if (rowText.contains("FCC ID") || rowText.contains("Equipment Class")) {
                    Pattern fccIdPattern = Pattern.compile("FCC\\s*ID:\\s*([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = fccIdPattern.matcher(rowText);
                    
                    if (matcher.find()) {
                        String fccId = matcher.group(1);
                        ApiStandardData standard = new ApiStandardData();
                        standard.setStandardNumber(fccId);
                        standard.setTitle("FCC Equipment Authorization - " + fccId);
                        standard.setDescription("无线设备FCC认证");
                        standard.setPublishedDate(LocalDate.now().toString());
                        standards.add(standard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 FCC 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseCCCData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            JsonNode dataArray = data.get("data");
            if (dataArray != null && dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    ApiStandardData standard = new ApiStandardData();
                    standard.setStandardNumber(getStringValue(item, "certNo", "CCC-UNKNOWN"));
                    standard.setTitle(getStringValue(item, "productName", "无线产品CCC认证"));
                    standard.setDescription(String.format("%s - %s", 
                        getStringValue(item, "productModel", ""),
                        getStringValue(item, "productStandard", "")));
                    standard.setPublishedDate(getStringValue(item, "certDate", null));
                    standard.setEffectiveDate(getStringValue(item, "validDate", null));
                    standards.add(standard);
                }
            }
        } catch (Exception e) {
            System.err.println("解析 CCC 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseTELECData(String html) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("tr");
            
            for (Element row : rows) {
                String rowText = row.text();
                Pattern telecPattern = Pattern.compile("(\\d{3}-\\d{6})");
                Matcher matcher = telecPattern.matcher(rowText);
                
                if (matcher.find()) {
                    String telecNo = matcher.group(1);
                    ApiStandardData standard = new ApiStandardData();
                    standard.setStandardNumber(telecNo);
                    standard.setTitle("TELEC认证 - " + telecNo);
                    standard.setDescription("无线设备TELEC技适认证");
                    standard.setPublishedDate(LocalDate.now().toString());
                    standards.add(standard);
                }
            }
        } catch (Exception e) {
            System.err.println("解析 TELEC 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseKCData(String html) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("tr");
            
            for (Element row : rows) {
                String rowText = row.text();
                Pattern kcPattern = Pattern.compile("(KCC-[A-Z0-9-]+)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = kcPattern.matcher(rowText);
                
                if (matcher.find()) {
                    String kcNo = matcher.group(1);
                    ApiStandardData standard = new ApiStandardData();
                    standard.setStandardNumber(kcNo);
                    standard.setTitle("KC认证 - " + kcNo);
                    standard.setDescription("무선기기 KC 인증");
                    standard.setPublishedDate(LocalDate.now().toString());
                    standards.add(standard);
                }
            }
        } catch (Exception e) {
            System.err.println("解析 KC 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseETSIData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            JsonNode results = data.get("results");
            if (results != null && results.isArray()) {
                for (JsonNode item : results) {
                    ApiStandardData standard = new ApiStandardData();
                    standard.setStandardNumber(getStringValue(item, "reference", "ETSI-UNKNOWN"));
                    standard.setTitle(getStringValue(item, "title", "ETSI标准"));
                    standard.setDescription(getStringValue(item, "abstract", ""));
                    standard.setPublishedDate(getStringValue(item, "publishedDate", null));
                    standard.setDownloadUrl(getStringValue(item, "downloadUrl", null));
                    standards.add(standard);
                }
            }
        } catch (Exception e) {
            System.err.println("解析 ETSI 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseRecallData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            if (data.isArray()) {
                for (JsonNode recall : data) {
                    String title = getStringValue(recall, "Title", "");
                    String description = getStringValue(recall, "Description", "");
                    String content = (title + " " + description).toLowerCase();
                    
                    // 只处理电子产品相关的召回
                    String[] electronicsKeywords = {"wireless", "bluetooth", "wifi", "battery", "charger", "electronic", "circuit", "radio", "antenna", "无线", "电池", "充电", "电子"};
                    boolean isElectronics = Arrays.stream(electronicsKeywords).anyMatch(content::contains);
                    
                    if (isElectronics) {
                        ApiStandardData standard = new ApiStandardData();
                        standard.setStandardNumber("RECALL-" + getStringValue(recall, "RecallNumber", ""));
                        standard.setTitle("产品召回警告 - " + title);
                        standard.setDescription(description);
                        standard.setPublishedDate(getStringValue(recall, "RecallDate", null));
                        standard.setDownloadUrl(getStringValue(recall, "URL", null));
                        standards.add(standard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析召回数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseWiFiAllianceData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            JsonNode products = data.get("products");
            if (products != null && products.isArray()) {
                for (JsonNode product : products) {
                    ApiStandardData standard = new ApiStandardData();
                    standard.setStandardNumber("WiFi-" + getStringValue(product, "certificationId", ""));
                    standard.setTitle("Wi-Fi认证 - " + getStringValue(product, "productName", ""));
                    standard.setDescription(String.format("%s - %s",
                        getStringValue(product, "vendorName", ""),
                        getStringValue(product, "productModel", "")));
                    standard.setPublishedDate(getStringValue(product, "certificationDate", null));
                    standards.add(standard);
                }
            }
        } catch (Exception e) {
            System.err.println("解析 Wi-Fi Alliance 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseC2PData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            JsonNode standardsArray = data.get("standards");
            if (standardsArray != null && standardsArray.isArray()) {
                for (JsonNode item : standardsArray) {
                    String number = getStringValue(item, "number", "");
                    String title = getStringValue(item, "title", "");
                    
                    if (isRelevantStandard(number, title)) {
                        ApiStandardData standard = new ApiStandardData();
                        standard.setStandardNumber(number);
                        standard.setVersion(getStringValue(item, "version", null));
                        standard.setTitle(title);
                        standard.setDescription(getStringValue(item, "description", ""));
                        standard.setPublishedDate(getStringValue(item, "published_date", null));
                        standard.setEffectiveDate(getStringValue(item, "effective_date", null));
                        standard.setDownloadUrl(getStringValue(item, "download_url", null));
                        standards.add(standard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 C2P 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseEmergoData(JsonNode data) {
        List<ApiStandardData> standards = new ArrayList<>();
        try {
            JsonNode results = data.get("results");
            if (results != null && results.isArray()) {
                for (JsonNode item : results) {
                    String standardId = getStringValue(item, "standard_id", "");
                    String name = getStringValue(item, "name", "");
                    
                    if (isRelevantStandard(standardId, name)) {
                        ApiStandardData standard = new ApiStandardData();
                        standard.setStandardNumber(standardId);
                        standard.setVersion(getStringValue(item, "version", null));
                        standard.setTitle(name);
                        standard.setDescription(getStringValue(item, "summary", ""));
                        standard.setPublishedDate(getStringValue(item, "publication_date", null));
                        standard.setEffectiveDate(getStringValue(item, "enforcement_date", null));
                        standard.setDownloadUrl(getStringValue(item, "document_url", null));
                        standards.add(standard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析 Emergo RAMS 数据错误: " + e.getMessage());
        }
        return standards;
    }
    
    private List<ApiStandardData> parseRSSData(String xmlText) {
        List<ApiStandardData> items = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(xmlText, "", org.jsoup.parser.Parser.xmlParser());
            Elements itemsElements = doc.select("item");
            
            for (Element item : itemsElements) {
                String title = item.select("title").text();
                String description = item.select("description").text();
                String link = item.select("link").text();
                String pubDate = item.select("pubDate").text();
                
                if (isRelevantStandard("", title + " " + description)) {
                    String standardNumber = extractStandardNumber(title + " " + description);
                    if (standardNumber != null) {
                        ApiStandardData standard = new ApiStandardData();
                        standard.setStandardNumber(standardNumber);
                        standard.setTitle(title);
                        standard.setDescription(description);
                        standard.setPublishedDate(normalizeDate(pubDate));
                        standard.setDownloadUrl(link);
                        items.add(standard);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("RSS 解析错误: " + e.getMessage());
        }
        return items;
    }
    
    private List<ApiStandardData> parseOJEUHTML(String htmlText) {
        List<ApiStandardData> items = new ArrayList<>();
        try {
            Pattern en18031Pattern = Pattern.compile("EN\\s*18031[^<]*(?:<[^>]*>[^<]*)*?(?:version|版本)[^<]*(\\d+(?:\\.\\d+)*)?", Pattern.CASE_INSENSITIVE);
            Pattern datePattern = Pattern.compile("(\\d{1,2}[/\\-\\.]\\d{1,2}[/\\-\\.]\\d{4}|\\d{4}[/\\-\\.]\\d{1,2}[/\\-\\.]\\d{1,2})");
            
            Matcher matcher = en18031Pattern.matcher(htmlText);
            while (matcher.find()) {
                String context = htmlText.substring(Math.max(0, matcher.start() - 500), Math.min(htmlText.length(), matcher.end() + 500));
                Matcher dateMatcher = datePattern.matcher(context);
                
                List<String> dates = new ArrayList<>();
                while (dateMatcher.find()) {
                    dates.add(dateMatcher.group(1));
                }
                
                ApiStandardData standard = new ApiStandardData();
                standard.setStandardNumber("EN18031");
                standard.setVersion(matcher.group(1));
                standard.setTitle("EN 18031 - 无线产品电磁兼容性要求");
                standard.setDescription("欧盟官方公报中的 EN18031 标准更新");
                standard.setPublishedDate(dates.isEmpty() ? null : normalizeDate(dates.get(0)));
                standard.setEffectiveDate(dates.size() > 1 ? normalizeDate(dates.get(1)) : null);
                standard.setDownloadUrl("https://eur-lex.europa.eu/search.html?scope=EURLEX&text=EN+18031");
                items.add(standard);
            }
        } catch (Exception e) {
            System.err.println("OJEU HTML 解析错误: " + e.getMessage());
        }
        return items;
    }
    
    // 辅助方法
    private boolean isRelevantStandard(String standardNumber, String content) {
        String[] relevantKeywords = {
            "EN18031", "EN 18031", "测肤仪", "2.4G", "5G", "Bluetooth", 
            "wireless", "radio", "electromagnetic", "EMC", "EMF"
        };
        
        String text = (standardNumber + " " + content).toLowerCase();
        return Arrays.stream(relevantKeywords).anyMatch(keyword -> text.contains(keyword.toLowerCase()));
    }
    
    private String extractStandardNumber(String text) {
        Pattern[] patterns = {
            Pattern.compile("EN\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ISO\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("IEC\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("CISPR\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ETSI\\s*EN\\s*(\\d+)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(0).replaceAll("\\s+", " ").trim();
            }
        }
        return null;
    }
    
    private String normalizeDate(String dateStr) {
        try {
            // 简化的日期标准化，实际应用中需要更复杂的处理
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private String getStringValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : defaultValue;
    }
}
