package com.certification.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

/**
 * FDA指导文档爬虫 - 爬取FDA医疗设备指导文档
 * 网址：https://www.fda.gov/medical-devices/device-advice-comprehensive-regulatory-assistance/guidance-documents-medical-devices-and-radiation-emitting-products
 */
public class FDAGuidance {
    // 基础URL
    private static final String BASE_URL = "https://www.fda.gov/medical-devices/device-advice-comprehensive-regulatory-assistance/guidance-documents-medical-devices-and-radiation-emitting-products";
    // 等待时间
    private static final int WAIT_TIMEOUT_SECONDS = 30;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * 初始化WebDriver - 使用完全无头模式
     */
    public void initDriver() {
        try {
            System.out.println("正在初始化无头ChromeDriver...");
            
            // 设置ChromeDriver路径
            String chromedriverPath = findChromeDriver();
            if (chromedriverPath != null) {
                System.setProperty("webdriver.chrome.driver", chromedriverPath);
                System.out.println("使用ChromeDriver路径: " + chromedriverPath);
            } else {
                System.out.println("未找到ChromeDriver，尝试使用系统PATH中的驱动");
            }
            
            ChromeOptions options = new ChromeOptions();
            
            // 简化的无头模式配置
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            
            // 设置实验性功能
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            System.out.println("使用完全无头模式，不依赖Chrome浏览器安装");
            driver = new ChromeDriver(options);
            System.out.println("无头ChromeDriver初始化成功！");
            
        } catch (Exception e) {
            System.err.println("ChromeDriver 初始化失败：" + e.getMessage());
            System.err.println("请确保：");
            System.err.println("1. 已下载ChromeDriver");
            System.err.println("2. ChromeDriver版本与系统兼容");
            System.err.println("3. 文件路径正确");
            throw new RuntimeException("无法初始化无头ChromeDriver", e);
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
    }

    /**
     * 关闭WebDriver
     */
    public void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * 爬取数据的主方法
     */
    public void crawl() {
        List<GuidanceRecord> allRecords = new ArrayList<>();
        
        try {
            initDriver();
            System.out.println("=== FDA指导文档爬虫启动 ===");
            System.out.println("目标网址: " + BASE_URL);
            
            // 访问页面
            driver.get(BASE_URL);
            
            // 等待页面加载完成
            waitForPageLoad();
            
            // 解析页面并提取数据
            List<GuidanceRecord> records = parsePage();
            allRecords.addAll(records);
            
            // 输出爬取到的记录
            for (int i = 0; i < records.size(); i++) {
                System.out.println("\n----- 第 " + (i + 1) + " 条记录 -----");
                GuidanceRecord record = records.get(i);
                System.out.println("标题: " + record.title);
                System.out.println("日期: " + record.date);
                System.out.println("话题: " + record.topic);
                System.out.println("指导状态: " + record.status);
                System.out.println("URL: " + record.url);
            }
            
            // 保存数据到文件
            if (!allRecords.isEmpty()) {
                saveDataToFiles(allRecords);
            }
            
            System.out.println("\n=== 爬取完成 ===");
            System.out.println("总共爬取到 " + allRecords.size() + " 条记录");
        } finally {
            closeDriver();
        }
    }

    /**
     * 等待页面加载完成
     */
    private void waitForPageLoad() {
        try {
            System.out.println("等待页面加载...");
            
            // 等待页面标题加载
            wait.until(ExpectedConditions.titleContains("Guidance Documents"));
            System.out.println("页面标题已加载");
            
            // 等待数据表格加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0_wrapper")));
            System.out.println("数据表格已加载");
            
            // 等待数据行加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0 tbody tr")));
            System.out.println("数据行已加载");
            
            // 额外等待以确保数据完全加载
            Thread.sleep(3000);
            System.out.println("页面加载完成");
            
        } catch (Exception e) {
            System.err.println("等待页面加载超时: " + e.getMessage());
        }
    }

    /**
     * 解析页面并提取数据
     */
    private List<GuidanceRecord> parsePage() {
        List<GuidanceRecord> records = new ArrayList<>();

        try {
            // 定位父容器
            WebElement parentContainer = driver.findElement(By.cssSelector("#DataTables_Table_0_wrapper > div:nth-child(2) > div"));
            System.out.println("找到父容器");
            
            // 定位所有数据行
            List<WebElement> rows = driver.findElements(By.cssSelector("#DataTables_Table_0 tbody tr"));
            
            if (rows.isEmpty()) {
                System.out.println("未找到数据行");
                return records;
            }

            System.out.println("找到 " + rows.size() + " 行数据");

            // 遍历每一行
            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                GuidanceRecord record = new GuidanceRecord();

                try {
                    // 1. 提取标题和URL
                    WebElement titleElement = null;
                    try {
                        titleElement = row.findElement(By.cssSelector("td a"));
                    } catch (Exception e1) {
                        try {
                            titleElement = row.findElement(By.cssSelector("a"));
                        } catch (Exception e2) {
                            // 如果找不到链接，尝试获取第一个单元格
                            List<WebElement> cells = row.findElements(By.cssSelector("td"));
                            if (!cells.isEmpty()) {
                                try {
                                    titleElement = cells.get(0).findElement(By.cssSelector("a"));
                                } catch (Exception e3) {
                                    // 忽略
                                }
                            }
                        }
                    }
                    
                    if (titleElement != null) {
                        record.title = titleElement.getText().trim();
                        String href = titleElement.getAttribute("href");
                        if (href != null && href.startsWith("/")) {
                            record.url = "https://www.fda.gov" + href;
                        } else {
                            record.url = href;
                        }
                        System.out.println("  标题: '" + record.title + "'");
                        System.out.println("  URL: " + record.url);
                    } else {
                        record.title = "无标题";
                        record.url = "";
                        System.out.println("  未找到标题元素");
                    }

                    // 2. 提取日期
                    WebElement dateElement = null;
                    try {
                        // 首先尝试用户提供的选择器 - 日期在sorting_1类的td中
                        dateElement = row.findElement(By.cssSelector("td.sorting_1"));
                    } catch (Exception e1) {
                        try {
                            // 尝试其他可能的日期选择器
                            dateElement = row.findElement(By.cssSelector("td font[dir='auto']"));
                        } catch (Exception e2) {
                            try {
                                // 尝试查找包含日期的单元格（第2列通常是日期）
                                List<WebElement> cells = row.findElements(By.cssSelector("td"));
                                if (cells.size() >= 2) {
                                    dateElement = cells.get(1); // 第2列通常是日期
                                }
                            } catch (Exception e3) {
                                // 尝试查找包含日期格式的单元格
                                List<WebElement> cells = row.findElements(By.cssSelector("td"));
                                for (WebElement cell : cells) {
                                    String cellText = cell.getText().trim();
                                    // 匹配多种日期格式
                                    if (cellText.matches(".*\\d{4}年\\d{1,2}月\\d{1,2}日.*") || 
                                        cellText.matches(".*\\d{1,2}/\\d{1,2}/\\d{4}.*") ||
                                        cellText.matches(".*\\d{4}-\\d{1,2}-\\d{1,2}.*")) {
                                        dateElement = cell;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (dateElement != null) {
                        record.date = dateElement.getText().trim();
                        System.out.println("  日期: " + record.date);
                    } else {
                        // 如果还是找不到，尝试获取所有单元格的文本用于调试
                        List<WebElement> cells = row.findElements(By.cssSelector("td"));
                        System.out.println("  调试 - 所有单元格内容:");
                        for (int j = 0; j < cells.size(); j++) {
                            String cellText = cells.get(j).getText().trim();
                            System.out.println("    单元格 " + j + ": '" + cellText + "'");
                        }
                        record.date = "无日期";
                        System.out.println("  未找到日期元素");
                    }

                    // 3. 提取话题（第4列）
                    WebElement topicElement = null;
                    try {
                        List<WebElement> cells = row.findElements(By.cssSelector("td"));
                        if (cells.size() >= 4) {
                            topicElement = cells.get(3); // 第4列通常是话题
                        }
                    } catch (Exception e1) {
                        // 尝试查找包含话题关键词的单元格
                        List<WebElement> cells = row.findElements(By.cssSelector("td"));
                        for (WebElement cell : cells) {
                            String cellText = cell.getText().trim();
                            if (cellText.contains("Premarket") || cellText.contains("Biologics") || 
                                cellText.contains("Drugs") || cellText.contains("Medical Devices") || 
                                cellText.contains("Digital Health")) {
                                topicElement = cell;
                                break;
                            }
                        }
                    }
                    
                    if (topicElement != null) {
                        record.topic = topicElement.getText().trim();
                        System.out.println("  话题: " + record.topic);
                    } else {
                        record.topic = "无话题";
                        System.out.println("  未找到话题元素");
                    }

                    // 4. 提取指导状态（第5列）
                    WebElement statusElement = null;
                    try {
                        List<WebElement> cells = row.findElements(By.cssSelector("td"));
                        if (cells.size() >= 5) {
                            statusElement = cells.get(4); // 第5列通常是指导状态
                        }
                    } catch (Exception e1) {
                        // 尝试查找包含状态关键词的单元格
                        List<WebElement> cells = row.findElements(By.cssSelector("td"));
                        for (WebElement cell : cells) {
                            String cellText = cell.getText().trim();
                            if (cellText.contains("Final") || cellText.contains("Draft") || 
                                cellText.contains("Withdrawn")) {
                                statusElement = cell;
                                break;
                            }
                        }
                    }
                    
                    if (statusElement != null) {
                        record.status = statusElement.getText().trim();
                        System.out.println("  指导状态: " + record.status);
                    } else {
                        record.status = "无状态";
                        System.out.println("  未找到指导状态元素");
                    }

                    records.add(record);
                    System.out.println("成功解析第 " + (i + 1) + " 行");

                } catch (Exception e) {
                    System.err.println("解析第 " + (i + 1) + " 行时出错: " + e.getMessage());
                    continue;
                }
            }

        } catch (Exception e) {
            System.err.println("解析页面时出错: " + e.getMessage());
        }
        return records;
    }

    /**
     * 存储每条记录的实体类
     */
    public static class GuidanceRecord {
        public String title;
        public String date;
        public String topic;
        public String status;
        public String url;

        @Override
        public String toString() {
            return String.format("GuidanceRecord{title='%s', date='%s', topic='%s', status='%s', url='%s'}", 
                    title, date, topic, status, url);
        }
    }

    /**
     * 保存数据到文件（CSV和JSON格式）
     */
    private void saveDataToFiles(List<GuidanceRecord> records) {
        try {
            // 创建输出目录
            String outputDir = "crawler_output";
            Files.createDirectories(Paths.get(outputDir));
            
            // 生成文件名（包含时间戳和随机数，避免文件冲突）
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomSuffix = String.valueOf(System.currentTimeMillis() % 10000);
            String baseFileName = String.format("fda_guidance_%s_%s", timestamp, randomSuffix);
            
            // 保存为CSV文件
            String csvFileName = outputDir + "/" + baseFileName + ".csv";
            boolean csvSaved = saveToCSV(records, csvFileName);
            
            // 保存为JSON文件
            String jsonFileName = outputDir + "/" + baseFileName + ".json";
            boolean jsonSaved = saveToJSON(records, jsonFileName);
            
            if (csvSaved || jsonSaved) {
                System.out.println("数据已保存到文件：");
                if (csvSaved) {
                    System.out.println("CSV文件: " + csvFileName);
                }
                if (jsonSaved) {
                    System.out.println("JSON文件: " + jsonFileName);
                }
            } else {
                System.out.println("警告：无法保存任何文件，但数据已成功爬取");
            }
            
        } catch (Exception e) {
            System.err.println("保存数据失败: " + e.getMessage());
            System.err.println("但爬取的数据仍然有效，可以手动复制输出内容");
            e.printStackTrace();
        }
    }
    
    /**
     * 保存数据到CSV文件
     */
    private boolean saveToCSV(List<GuidanceRecord> records, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            // 写入CSV头部
            writer.println("标题,日期,话题,指导状态,URL");
            
            // 写入数据行
            for (GuidanceRecord record : records) {
                // 转义CSV中的特殊字符
                String title = escapeCSV(record.title);
                String date = escapeCSV(record.date);
                String topic = escapeCSV(record.topic);
                String status = escapeCSV(record.status);
                String url = escapeCSV(record.url);
                
                writer.println(String.format("%s,%s,%s,%s,%s", title, date, topic, status, url));
            }
            return true; // 表示CSV保存成功
        } catch (IOException e) {
            System.err.println("保存CSV文件失败: " + e.getMessage());
            return false; // 表示CSV保存失败
        }
    }
    
    /**
     * 保存数据到JSON文件
     */
    private boolean saveToJSON(List<GuidanceRecord> records, String fileName) {
        try {
            // 创建包含元数据的JSON对象
            var jsonData = new java.util.HashMap<String, Object>();
            jsonData.put("source", "FDA Guidance Documents");
            jsonData.put("url", BASE_URL);
            jsonData.put("totalRecords", records.size());
            jsonData.put("crawlTime", LocalDate.now().toString());
            jsonData.put("records", records);
            
            // 转换为JSON字符串并保存
            String jsonString = JSON.toJSONString(jsonData, JSONWriter.Feature.PrettyFormat);
            Files.write(Paths.get(fileName), jsonString.getBytes(StandardCharsets.UTF_8));
            return true; // 表示JSON保存成功
        } catch (IOException e) {
            System.err.println("保存JSON文件失败: " + e.getMessage());
            return false; // 表示JSON保存失败
        }
    }
    
    /**
     * 转义CSV中的特殊字符
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        // 如果包含逗号、引号或换行符，需要用引号包围并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // 主方法：测试爬虫
    public static void main(String[] args) {
        FDAGuidance crawler = new FDAGuidance();
        crawler.crawl();
    }

    /**
     * 尝试在项目根目录或系统PATH中定位 ChromeDriver
     */
    private String findChromeDriver() {
        String projectRoot = System.getProperty("user.dir");
        String[] candidates = new String[] {
                projectRoot + "\\chromedriver.exe",
                projectRoot + "\\chromedriver.exe", // 确保在项目根目录下有
                "chromedriver.exe", // 尝试在系统PATH中
                "chromedriver.exe" // 尝试在系统PATH中
        };
        for (String p : candidates) {
            if (p != null && Files.exists(Paths.get(p))) {
                return p;
            }
        }
        return null;
    }
}
