package com.certification.crawler.countrydata.eu.others;

import com.certification.crawler.common.CsvExporter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧盟BTI (Binding Tariff Information) 爬虫
 * 爬取欧洲绑定关税信息数据
 * 
 * 数据来源: https://ec.europa.eu/taxation_customs/dds2/ebti/ebti_consultation.jsp
 */
@Component
public class Eu_BTI {
    
    private static final String BASE_URL = "https://ec.europa.eu/taxation_customs/dds2/ebti/ebti_consultation.jsp";
    private static final String LIST_URL = "https://ec.europa.eu/taxation_customs/dds2/ebti/ebti_list.jsp";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    private final CsvExporter csvExporter;
    
    @Autowired
    public Eu_BTI(CsvExporter csvExporter) {
        this.csvExporter = csvExporter;
    }
    
    /**
     * 爬取BTI数据（基础方法）
     * @param maxPages 最大爬取页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlBTIData(int maxPages) {
        return crawlBTIDataWithParams("", "", "", "", "", "", "", "", "", "", "", "", maxPages);
    }
    
    /**
     * 爬取BTI数据（参数化搜索）
     * @param refCountry 发布国家
     * @param reference BTI参考号
     * @param valStartDate 有效期开始日期 (DD/MM/YYYY)
     * @param valStartDateTo 有效期开始日期结束 (DD/MM/YYYY)
     * @param valEndDate 有效期结束日期 (DD/MM/YYYY)
     * @param valEndDateTo 有效期结束日期结束 (DD/MM/YYYY)
     * @param supplDate 补充日期 (DD/MM/YYYY)
     * @param nomenc 商品编码
     * @param nomencTo 商品编码结束
     * @param keywordSearch 关键词搜索
     * @param keywordMatchRule 关键词匹配规则 (OR/AND)
     * @param excludeKeyword 排除关键词
     * @param maxPages 最大爬取页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlBTIDataWithParams(
            String refCountry, String reference, String valStartDate, String valStartDateTo,
            String valEndDate, String valEndDateTo, String supplDate, String nomenc, String nomencTo,
            String keywordSearch, String keywordMatchRule, String excludeKeyword, int maxPages) {
        
        List<Map<String, String>> allBTIData = new ArrayList<>();
        
        try {
            // 设置Chrome WebDriver
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=" + USER_AGENT);
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images");
            options.addArguments("--disable-javascript");
            
            WebDriver driver = new ChromeDriver(options);
            
            try {
                System.out.println("=".repeat(80));
                System.out.println("🌐 开始爬取BTI数据");
                System.out.println("📡 基础URL: " + BASE_URL);
                System.out.println("⏱️  开始时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                System.out.println("🔍 搜索参数:");
                System.out.println("   - 发布国家: " + (refCountry.isEmpty() ? "全部" : refCountry));
                System.out.println("   - BTI参考号: " + (reference.isEmpty() ? "全部" : reference));
                System.out.println("   - 有效期开始: " + (valStartDate.isEmpty() ? "全部" : valStartDate));
                System.out.println("   - 有效期结束: " + (valEndDate.isEmpty() ? "全部" : valEndDate));
                System.out.println("   - 商品编码: " + (nomenc.isEmpty() ? "全部" : nomenc));
                System.out.println("   - 关键词: " + (keywordSearch.isEmpty() ? "全部" : keywordSearch));
                System.out.println("   - 匹配规则: " + (keywordMatchRule.isEmpty() ? "OR" : keywordMatchRule));
                System.out.println("   - 最大页数: " + maxPages);
                System.out.println("=".repeat(80));
                
                // 构建搜索URL
                String searchUrl = buildSearchUrl(refCountry, reference, valStartDate, valStartDateTo,
                        valEndDate, valEndDateTo, supplDate, nomenc, nomencTo, keywordSearch, keywordMatchRule, excludeKeyword);
                
                System.out.println("🔗 搜索URL: " + searchUrl);
                
                // 访问搜索页面
                long startTime = System.currentTimeMillis();
                driver.get(searchUrl);
                long endTime = System.currentTimeMillis();
                
                System.out.println("⏱️  页面加载时间: " + (endTime - startTime) + "ms");
                System.out.println("📄 页面标题: " + driver.getTitle());
                System.out.println("🔗 当前URL: " + driver.getCurrentUrl());
                
                // 等待页面加载完成
                Thread.sleep(3000);
                
                // 爬取多页数据
                for (int page = 1; page <= maxPages; page++) {
                    System.out.println("正在爬取第" + page + "页数据...");
                    
                    // 构建列表页面URL
                    String listUrl = buildListUrl(refCountry, reference, valStartDate, valStartDateTo,
                            valEndDate, valEndDateTo, supplDate, nomenc, nomencTo, keywordSearch, keywordMatchRule, excludeKeyword, page);
                    
                    System.out.println("📋 列表URL: " + listUrl);
                    
                    // 访问列表页面
                    driver.get(listUrl);
                    Thread.sleep(2000);
                    
                    // 解析当前页面的BTI数据
                    List<Map<String, String>> pageData = parseBTIDataFromPage(driver.getPageSource());
                    
                    if (pageData.isEmpty()) {
                        System.out.println("第" + page + "页没有数据，停止爬取");
                        break;
                    }
                    
                    allBTIData.addAll(pageData);
                    System.out.println("第" + page + "页爬取完成，获取到 " + pageData.size() + " 条BTI数据");
                    
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
        
        System.out.println("=".repeat(80));
        System.out.println("✅ BTI数据爬取完成");
        System.out.println("📊 总共获取到 " + allBTIData.size() + " 条BTI数据");
        System.out.println("⏱️  结束时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=".repeat(80));
        
        return allBTIData;
    }
    
    /**
     * 构建搜索URL
     */
    private String buildSearchUrl(String refCountry, String reference, String valStartDate, String valStartDateTo,
                                 String valEndDate, String valEndDateTo, String supplDate, String nomenc, String nomencTo,
                                 String keywordSearch, String keywordMatchRule, String excludeKeyword) {
        
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?Lang=en");
        
        // 发布国家
        if (!refCountry.isEmpty()) {
            url.append("&refcountry=").append(URLEncoder.encode(refCountry, StandardCharsets.UTF_8));
        }
        
        // BTI参考号
        if (!reference.isEmpty()) {
            url.append("&reference=").append(URLEncoder.encode(reference, StandardCharsets.UTF_8));
        }
        
        // 有效期开始日期
        if (!valStartDate.isEmpty()) {
            url.append("&valstartdate1=").append(URLEncoder.encode(valStartDate.replace("/", "-"), StandardCharsets.UTF_8));
            url.append("&valstartdate=").append(URLEncoder.encode(valStartDate, StandardCharsets.UTF_8));
        }
        
        // 有效期开始日期结束
        if (!valStartDateTo.isEmpty()) {
            url.append("&valstartdateto1=").append(URLEncoder.encode(valStartDateTo.replace("/", "-"), StandardCharsets.UTF_8));
            url.append("&valstartdateto=").append(URLEncoder.encode(valStartDateTo, StandardCharsets.UTF_8));
        }
        
        // 有效期结束日期
        if (!valEndDate.isEmpty()) {
            url.append("&valenddate1=").append(URLEncoder.encode(valEndDate.replace("/", "-"), StandardCharsets.UTF_8));
            url.append("&valenddate=").append(URLEncoder.encode(valEndDate, StandardCharsets.UTF_8));
        }
        
        // 有效期结束日期结束
        if (!valEndDateTo.isEmpty()) {
            url.append("&valenddateto1=").append(URLEncoder.encode(valEndDateTo.replace("/", "-"), StandardCharsets.UTF_8));
            url.append("&valenddateto=").append(URLEncoder.encode(valEndDateTo, StandardCharsets.UTF_8));
        }
        
        // 补充日期
        if (!supplDate.isEmpty()) {
            url.append("&suppldate1=").append(URLEncoder.encode(supplDate.replace("/", "-"), StandardCharsets.UTF_8));
            url.append("&suppldate=").append(URLEncoder.encode(supplDate, StandardCharsets.UTF_8));
        }
        
        // 商品编码
        if (!nomenc.isEmpty()) {
            url.append("&nomenc=").append(URLEncoder.encode(nomenc, StandardCharsets.UTF_8));
        }
        
        // 商品编码结束
        if (!nomencTo.isEmpty()) {
            url.append("&nomencto=").append(URLEncoder.encode(nomencTo, StandardCharsets.UTF_8));
        }
        
        // 关键词搜索
        if (!keywordSearch.isEmpty()) {
            url.append("&keywordsearch1=").append(URLEncoder.encode(keywordSearch, StandardCharsets.UTF_8));
            url.append("&keywordsearch=").append(URLEncoder.encode(keywordSearch, StandardCharsets.UTF_8));
        }
        
        // 关键词匹配规则
        if (!keywordMatchRule.isEmpty()) {
            url.append("&keywordmatchrule=").append(keywordMatchRule);
        } else {
            url.append("&keywordmatchrule=OR");
        }
        
        // 排除关键词
        if (!excludeKeyword.isEmpty()) {
            url.append("&excludekeywordsearch1=").append(URLEncoder.encode(excludeKeyword, StandardCharsets.UTF_8));
            url.append("&excludekeywordsearch=").append(URLEncoder.encode(excludeKeyword, StandardCharsets.UTF_8));
        }
        
        // 其他固定参数
        url.append("&descript=");
        url.append("&orderby=0");
        url.append("&Expand=true");
        url.append("&offset=1");
        url.append("&viewVal=");
        url.append("&isVisitedRef=false");
        url.append("&allRecords=0");
        url.append("&showProgressBar=true");
        
        return url.toString();
    }
    
    /**
     * 构建列表页面URL
     */
    private String buildListUrl(String refCountry, String reference, String valStartDate, String valStartDateTo,
                               String valEndDate, String valEndDateTo, String supplDate, String nomenc, String nomencTo,
                               String keywordSearch, String keywordMatchRule, String excludeKeyword, int page) {
        
        StringBuilder url = new StringBuilder(LIST_URL);
        url.append("?Lang=en");
        url.append("&offset=").append(page);
        url.append("&allRecords=0");
        
        // 有效期开始日期
        if (!valStartDate.isEmpty()) {
            url.append("&valstartdate=").append(URLEncoder.encode(valStartDate, StandardCharsets.UTF_8));
        }
        
        // 有效期开始日期结束
        if (!valStartDateTo.isEmpty()) {
            url.append("&valstartdateto=").append(URLEncoder.encode(valStartDateTo, StandardCharsets.UTF_8));
        }
        
        // 有效期结束日期
        if (!valEndDate.isEmpty()) {
            url.append("&valenddate=").append(URLEncoder.encode(valEndDate, StandardCharsets.UTF_8));
        }
        
        // 有效期结束日期结束
        if (!valEndDateTo.isEmpty()) {
            url.append("&valenddateto=").append(URLEncoder.encode(valEndDateTo, StandardCharsets.UTF_8));
        }
        
        // 补充日期
        if (!supplDate.isEmpty()) {
            url.append("&suppldate=").append(URLEncoder.encode(supplDate, StandardCharsets.UTF_8));
        }
        
        // 商品编码
        if (!nomenc.isEmpty()) {
            url.append("&nomenc=").append(URLEncoder.encode(nomenc, StandardCharsets.UTF_8));
        }
        
        // 商品编码结束
        if (!nomencTo.isEmpty()) {
            url.append("&nomencto=").append(URLEncoder.encode(nomencTo, StandardCharsets.UTF_8));
        }
        
        // 关键词搜索
        if (!keywordSearch.isEmpty()) {
            url.append("&keywordsearch=").append(URLEncoder.encode(keywordSearch, StandardCharsets.UTF_8));
        }
        
        // 关键词匹配规则
        if (!keywordMatchRule.isEmpty()) {
            url.append("&keywordmatchrule=").append(keywordMatchRule);
        } else {
            url.append("&keywordmatchrule=OR");
        }
        
        // 排除关键词
        if (!excludeKeyword.isEmpty()) {
            url.append("&excludekeywordsearch=").append(URLEncoder.encode(excludeKeyword, StandardCharsets.UTF_8));
        }
        
        // 其他固定参数
        url.append("&orderby=0");
        url.append("&isVisitedRef=true");
        url.append("&random=").append(System.currentTimeMillis() % 10000000);
        
        return url.toString();
    }
    
    /**
     * 解析页面中的BTI数据
     */
    private List<Map<String, String>> parseBTIDataFromPage(String html) {
        List<Map<String, String>> btiDataList = new ArrayList<>();
        
        try {
            Document doc = Jsoup.parse(html);
            
            // 查找BTI数据表格 - 使用更精确的选择器
            Element table = doc.select("table.ecl-table--zebra.table-result").first();
            
            if (table != null) {
                Elements rows = table.select("tbody tr.ecl-table__row");
                
                for (Element row : rows) {
                    Elements cells = row.select("td.ecl-table__cell");
                    
                    if (cells.size() >= 5) { // BTI表格有5列
                        Map<String, String> btiData = new HashMap<>();
                        
                        // 解析BTI数据字段 - 按照您提供的HTML结构
                        btiData.put("bti_reference", getCellText(cells, 0));
                        btiData.put("nomenclature_code", getCellText(cells, 1));
                        btiData.put("start_date_validity", getCellText(cells, 2));
                        btiData.put("end_date_validity", getCellText(cells, 3));
                        btiData.put("number_of_images", getCellText(cells, 4));
                        btiData.put("crawl_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        
                        // 只有当有有效数据时才添加
                        if (!btiData.get("bti_reference").isEmpty()) {
                            btiDataList.add(btiData);
                        }
                    }
                }
            }
            
            // 如果没有找到表格数据，尝试查找其他格式的数据
            if (btiDataList.isEmpty()) {
                btiDataList = parseAlternativeFormat(doc);
            }
            
        } catch (Exception e) {
            System.err.println("解析BTI数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return btiDataList;
    }
    
    /**
     * 解析替代格式的数据
     */
    private List<Map<String, String>> parseAlternativeFormat(Document doc) {
        List<Map<String, String>> btiDataList = new ArrayList<>();
        
        try {
            // 尝试查找其他可能的表格结构
            Elements tables = doc.select("table");
            
            for (Element table : tables) {
                Elements rows = table.select("tr");
                
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    
                    if (cells.size() >= 3) { // 至少需要3列数据
                        Map<String, String> btiData = new HashMap<>();
                        
                        // 尝试提取BTI参考号（通常在第一个单元格）
                        String firstCellText = cells.get(0).text().trim();
                        
                        // 检查是否包含BTI参考号格式
                        if (firstCellText.matches(".*[A-Z]{2}\\d{9}.*") || firstCellText.matches(".*\\d{4}/\\d{4}.*")) {
                            btiData.put("bti_reference", firstCellText);
                            
                            // 尝试提取其他字段
                            if (cells.size() > 1) {
                                btiData.put("nomenclature_code", getCellText(cells, 1));
                            }
                            if (cells.size() > 2) {
                                btiData.put("start_date_validity", getCellText(cells, 2));
                            }
                            if (cells.size() > 3) {
                                btiData.put("end_date_validity", getCellText(cells, 3));
                            }
                            if (cells.size() > 4) {
                                btiData.put("number_of_images", getCellText(cells, 4));
                            }
                            
                            btiData.put("crawl_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            
                            btiDataList.add(btiData);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("解析替代格式数据时发生错误: " + e.getMessage());
        }
        
        return btiDataList;
    }
    
    /**
     * 提取BTI参考号
     */
    private String extractBTIReference(String text) {
        // 匹配格式如: GB124356487 或类似的模式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[A-Z]{2}\\d{9}");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        // 也尝试匹配其他可能的格式
        pattern = java.util.regex.Pattern.compile("\\d{4}/\\d{4}");
        matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return "";
    }
    
    /**
     * 获取单元格文本
     */
    private String getCellText(Elements cells, int index) {
        if (index < cells.size()) {
            Element cell = cells.get(index);
            
            // 如果是第一列（BTI Reference），优先获取链接文本
            if (index == 0) {
                Element link = cell.select("a").first();
                if (link != null) {
                    return link.text().trim();
                }
            }
            
            // 获取单元格文本，清理特殊字符
            String text = cell.text().trim();
            // 移除多余的空白字符和换行符
            text = text.replaceAll("\\s+", " ");
            return text;
        }
        return "";
    }
    
    /**
     * 保存BTI数据到CSV文件
     */
    public void saveBTIDataToCSV(List<Map<String, String>> btiDataList, String filename) {
        if (btiDataList.isEmpty()) {
            System.out.println("没有BTI数据需要保存");
            return;
        }
        
        try {
            // 定义CSV表头 - 匹配新的字段结构
            String[] headers = {
                "bti_reference", "nomenclature_code", "start_date_validity", "end_date_validity",
                "number_of_images", "crawl_time"
            };
            
            // 创建输出目录
            File outputDir = new File("crawler_output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 生成文件名
            if (filename == null || filename.isEmpty()) {
                filename = "BTI_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            }
            
            String filePath = outputDir.getAbsolutePath() + File.separator + filename;
            
            // 保存到CSV
            csvExporter.exportToCsv(btiDataList, List.of(headers), filePath);
            
            System.out.println("✅ BTI数据已保存到: " + filePath);
            System.out.println("📊 共保存 " + btiDataList.size() + " 条记录");
            
        } catch (Exception e) {
            System.err.println("保存BTI数据到CSV时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 主方法 - 用于测试
     */
    public static void main(String[] args) {
        CsvExporter csvExporter = new CsvExporter();
        Eu_BTI btiCrawler = new Eu_BTI(csvExporter);
        
        // 测试参数化搜索
        List<Map<String, String>> btiData = btiCrawler.crawlBTIDataWithParams(
            "", // 发布国家
            "", // BTI参考号
            "01/09/2010", // 有效期开始日期
            "10/09/2025", // 有效期开始日期结束
            "", // 有效期结束日期
            "", // 有效期结束日期结束
            "", // 补充日期
            "", // 商品编码
            "", // 商品编码结束
            "SKIN CARE PREPARATION", // 关键词搜索
            "OR", // 关键词匹配规则
            "", // 排除关键词
            3 // 最大页数
        );
        
        // 保存数据
        btiCrawler.saveBTIDataToCSV(btiData, "BTI_SKIN_CARE_PREPARATION.csv");
    }
}
