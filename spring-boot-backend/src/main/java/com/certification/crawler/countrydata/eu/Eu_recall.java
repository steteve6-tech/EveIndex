package com.certification.crawler.countrydata.eu;

import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 欧盟召回数据爬虫
 * 爬取欧盟医疗器械召回数据并保存到数据库
 * 数据来源：欧盟医疗器械数据库、各国监管机构召回公告
 */
@Slf4j
@Component
public class Eu_recall {
    
    private static final String BASE_URL = "https://ec.europa.eu/tools/eudamed/api/devices/recalls";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;
    
    public Eu_recall() {
        // 构造函数
    }
    
    /**
     * 爬取欧盟召回数据并保存到数据库
     * @param maxRecords 最大记录数
     * @return 保存的记录数量
     */
    @Transactional
    public int crawlAndSaveToDatabase(int maxRecords) {
        return crawlAndSaveToDatabase("", maxRecords, 20, "", "");
    }
    
    /**
     * 爬取欧盟召回数据并保存到数据库（带参数）
     * @param searchTerm 搜索关键词
     * @param maxRecords 最大记录数
     * @param batchSize 批次大小
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 保存的记录数量
     */
    @Transactional
    public int crawlAndSaveToDatabase(String searchTerm, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        log.info("开始爬取欧盟召回数据 - 搜索词: {}, 最大记录数: {}, 批次大小: {}, 日期范围: {} - {}", 
                searchTerm, maxRecords, batchSize, dateFrom, dateTo);
        
        int totalSaved = 0;
        int totalSkipped = 0;
        
        try {
            // 使用WebDriver爬取数据
            List<Map<String, String>> recallData = crawlRecallDataWithWebDriver(searchTerm, maxRecords, dateFrom, dateTo);
            
            if (recallData.isEmpty()) {
                log.warn("未获取到召回数据");
                return 0;
            }
            
            log.info("成功爬取到 {} 条召回数据，开始保存到数据库", recallData.size());
            
            // 分批保存到数据库
            for (int i = 0; i < recallData.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, recallData.size());
                List<Map<String, String>> batch = recallData.subList(i, endIndex);
                
                int[] result = saveBatchToDatabase(batch);
                totalSaved += result[0];
                totalSkipped += result[1];
                
                log.info("批次 {}/{} 保存完成: 保存 {} 条，跳过 {} 条", 
                        (i / batchSize) + 1, 
                        (recallData.size() + batchSize - 1) / batchSize,
                        result[0], result[1]);
            }
            
            log.info("欧盟召回数据爬取完成: 总共保存 {} 条，跳过 {} 条", totalSaved, totalSkipped);
            
        } catch (Exception e) {
            log.error("爬取欧盟召回数据失败", e);
            throw new RuntimeException("爬取欧盟召回数据失败: " + e.getMessage(), e);
        }
        
        return totalSaved;
    }
    
    /**
     * 批量爬取多个搜索关键词的召回数据
     * @param searchKeywords 搜索关键词列表
     * @param maxRecords 最大记录数，-1表示爬取所有数据
     * @param batchSize 批次大小
     * @param dateFrom 开始日期
     * @param dateTo 结束日期
     * @return 爬取结果汇总
     */
    @Transactional
    public Map<String, Object> crawlAndSaveWithKeywords(List<String> searchKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        log.info("🚀 开始批量爬取召回数据，共 {} 个搜索关键词", searchKeywords.size());
        log.info("📊 批次大小: {}，最大记录数: {}，日期范围: {} - {}", batchSize, maxRecords == -1 ? "所有数据" : maxRecords, dateFrom, dateTo);
        
        Map<String, Object> result = new HashMap<>();
        int totalSaved = 0;
        int totalSkipped = 0;
        int successCount = 0;
        int failureCount = 0;
        List<String> failedKeywords = new ArrayList<>();
        Map<String, Integer> keywordResults = new HashMap<>();
        
        for (String keyword : searchKeywords) {
            try {
                log.info("🔄 正在爬取关键词: {}", keyword);
                int savedCount = crawlAndSaveToDatabase(keyword, maxRecords, batchSize, dateFrom, dateTo);
                
                if (savedCount >= 0) {
                    totalSaved += savedCount;
                    successCount++;
                    keywordResults.put(keyword, savedCount);
                    log.info("✅ 关键词 {} 爬取成功，保存 {} 条记录", keyword, savedCount);
                } else {
                    failureCount++;
                    failedKeywords.add(keyword);
                    keywordResults.put(keyword, -1);
                    log.error("❌ 关键词 {} 爬取失败", keyword);
                }
                
                // 添加延迟避免请求过快
                Thread.sleep(2000);
                
            } catch (Exception e) {
                failureCount++;
                failedKeywords.add(keyword);
                keywordResults.put(keyword, -1);
                log.error("❌ 关键词 {} 爬取异常: {}", keyword, e.getMessage());
            }
        }
        
        result.put("totalProcessed", searchKeywords.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("failedKeywords", failedKeywords);
        result.put("keywordResults", keywordResults);
        result.put("success", failureCount == 0);
        result.put("message", String.format("批量爬取完成：成功 %d 个，失败 %d 个，共保存 %d 条记录", 
                successCount, failureCount, totalSaved));
        
        log.info("📊 批量爬取汇总:");
        log.info("   ├─ 总处理关键词: {}", searchKeywords.size());
        log.info("   ├─ 成功: {}", successCount);
        log.info("   ├─ 失败: {}", failureCount);
        log.info("   ├─ 总保存记录: {}", totalSaved);
        log.info("   └─ 失败关键词: {}", failedKeywords);
        
        return result;
    }
    
    /**
     * 使用WebDriver爬取召回数据
     */
    private List<Map<String, String>> crawlRecallDataWithWebDriver(String searchTerm, int maxRecords, String dateFrom, String dateTo) {
        List<Map<String, String>> recallData = new ArrayList<>();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=" + USER_AGENT);
        
        WebDriver driver = new ChromeDriver(options);
        
        try {
            log.info("使用WebDriver访问欧盟召回数据页面");
            
            // 访问欧盟医疗器械数据库召回页面
            String targetUrl = buildSearchUrl(searchTerm, dateFrom, dateTo);
            driver.get(targetUrl);
            
            // 等待页面加载
            Thread.sleep(3000);
            
            // 解析页面数据
            Document doc = Jsoup.parse(driver.getPageSource());
            recallData = parseRecallDataFromPage(doc, maxRecords);
            
            log.info("WebDriver爬取完成，获取到 {} 条数据", recallData.size());
            
        } catch (Exception e) {
            log.error("WebDriver爬取失败", e);
        } finally {
            driver.quit();
        }
        
        return recallData;
    }
    
    /**
     * 构建搜索URL
     */
    private String buildSearchUrl(String searchTerm, String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder(BASE_URL);
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            url.append("?search=").append(searchTerm);
        }
        
        if (dateFrom != null && !dateFrom.trim().isEmpty()) {
            url.append(url.toString().contains("?") ? "&" : "?")
               .append("dateFrom=").append(dateFrom);
        }
        
        if (dateTo != null && !dateTo.trim().isEmpty()) {
            url.append(url.toString().contains("?") ? "&" : "?")
               .append("dateTo=").append(dateTo);
        }
        
        return url.toString();
    }
    
    /**
     * 从页面解析召回数据
     */
    private List<Map<String, String>> parseRecallDataFromPage(Document doc, int maxRecords) {
        List<Map<String, String>> recallData = new ArrayList<>();
        
        try {
            // 查找召回数据表格或列表
            Elements recallItems = doc.select(".recall-item, .recall-row, .device-recall, [data-recall-id]");
            
            if (recallItems.isEmpty()) {
                // 尝试其他选择器
                recallItems = doc.select("tr, .item, .record");
            }
            
            log.info("找到 {} 个召回数据项", recallItems.size());
            
            for (int i = 0; i < Math.min(recallItems.size(), maxRecords); i++) {
                Element item = recallItems.get(i);
                Map<String, String> recallInfo = parseRecallItem(item);
                
                if (recallInfo != null && !recallInfo.isEmpty()) {
                    recallData.add(recallInfo);
                }
            }
            
        } catch (Exception e) {
            log.error("解析召回数据失败", e);
        }
        
        return recallData;
    }
    
    /**
     * 解析单个召回数据项
     */
    private Map<String, String> parseRecallItem(Element item) {
        Map<String, String> recallInfo = new HashMap<>();
        
        try {
            // 提取召回编号
            String recallId = extractText(item, ".recall-id, .alert-number, [data-id]");
            if (recallId == null || recallId.trim().isEmpty()) {
                recallId = "EU_RECALL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
            }
            recallInfo.put("cfres_id", recallId);
            
            // 提取产品描述
            String productDescription = extractText(item, ".product-description, .product-name, .device-name");
            recallInfo.put("product_description", productDescription);
            
            // 提取召回公司
            String recallingFirm = extractText(item, ".recalling-firm, .manufacturer, .company");
            recallInfo.put("recalling_firm", recallingFirm);
            
            // 提取召回状态
            String recallStatus = extractText(item, ".recall-status, .status, .class");
            recallInfo.put("recall_status", recallStatus);
            
            // 提取发布日期
            String eventDatePosted = extractText(item, ".date-posted, .publication-date, .date");
            recallInfo.put("event_date_posted", eventDatePosted);
            
            // 提取设备名称
            String deviceName = extractText(item, ".device-name, .product-name, .generic-name");
            recallInfo.put("device_name", deviceName);
            
            // 提取产品代码
            String productCode = extractText(item, ".product-code, .code, .identifier");
            recallInfo.put("product_code", productCode);
            
            // 设置默认值
            if (recallInfo.get("product_description") == null) {
                recallInfo.put("product_description", "欧盟医疗器械召回");
            }
            if (recallInfo.get("recalling_firm") == null) {
                recallInfo.put("recalling_firm", "未知制造商");
            }
            if (recallInfo.get("recall_status") == null) {
                recallInfo.put("recall_status", "CLASS II");
            }
            if (recallInfo.get("device_name") == null) {
                recallInfo.put("device_name", recallInfo.get("product_description"));
            }
            
            // 添加爬取时间戳
            recallInfo.put("crawl_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
        } catch (Exception e) {
            log.error("解析召回数据项失败", e);
            return null;
        }
        
        return recallInfo;
    }
    
    /**
     * 提取元素文本内容
     */
    private String extractText(Element parent, String selector) {
        try {
            Element element = parent.selectFirst(selector);
            if (element != null) {
                return element.text().trim();
            }
        } catch (Exception e) {
            log.debug("提取文本失败: {}", selector);
        }
        return null;
    }
    
    /**
     * 批量保存数据到数据库
     */
    private int[] saveBatchToDatabase(List<Map<String, String>> batchData) {
        int saved = 0;
        int skipped = 0;
        
        for (Map<String, String> data : batchData) {
            try {
                DeviceRecallRecord record = convertToDeviceRecallRecord(data);
                
                // 检查是否已存在相同的召回ID
                if (record.getCfresId() != null && !record.getCfresId().trim().isEmpty()) {
                    if (deviceRecallRecordRepository.existsByCfresId(record.getCfresId())) {
                        log.debug("召回ID {} 已存在，跳过保存", record.getCfresId());
                        skipped++;
                        continue;
                    }
                }
                
                // 保存到数据库
                deviceRecallRecordRepository.save(record);
                saved++;
                log.debug("成功保存召回记录: {}", record.getCfresId());
                
            } catch (Exception e) {
                log.error("保存召回记录失败: {}", e.getMessage());
                skipped++;
            }
        }
        
        log.info("批量保存完成: 保存 {} 条，跳过 {} 条", saved, skipped);
        return new int[]{saved, skipped};
    }
    
    /**
     * 将Map数据转换为DeviceRecallRecord实体
     */
    private DeviceRecallRecord convertToDeviceRecallRecord(Map<String, String> data) {
        DeviceRecallRecord record = new DeviceRecallRecord();
        
        // 设置召回ID
        record.setCfresId(data.getOrDefault("cfres_id", ""));
        
        // 设置产品描述
        record.setProductDescription(data.getOrDefault("product_description", ""));
        
        // 设置召回公司
        record.setRecallingFirm(data.getOrDefault("recalling_firm", ""));
        
        // 设置召回状态
        record.setRecallStatus(data.getOrDefault("recall_status", "CLASS II"));
        
        // 设置发布日期
        String dateStr = data.getOrDefault("event_date_posted", "");
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                LocalDate date = parseDate(dateStr);
                record.setEventDatePosted(date);
            } catch (Exception e) {
                log.debug("解析日期失败: {}", dateStr);
                record.setEventDatePosted(LocalDate.now());
            }
        } else {
            record.setEventDatePosted(LocalDate.now());
        }
        
        // 设置设备名称
        record.setDeviceName(data.getOrDefault("device_name", ""));
        
        // 设置产品代码
        record.setProductCode(data.getOrDefault("product_code", ""));
        
        return record;
    }
    
    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        // 尝试多种日期格式
        String[] formats = {
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "MM-dd-yyyy"
        };
        
        for (String format : formats) {
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，返回当前日期
        log.warn("无法解析日期: {}, 使用当前日期", dateStr);
        return LocalDate.now();
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        欧盟召回数据爬虫程序");
        System.out.println("==========================================");
        
        try {
            System.out.println("请通过Spring容器运行此爬虫");
            System.out.println("或调用 crawlAndSaveToDatabase() 方法");
            
        } catch (Exception e) {
            System.err.println("爬虫执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
