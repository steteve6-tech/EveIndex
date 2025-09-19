package com.certification.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.SessionNotCreatedException;
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
 * CBP rulings爬虫 - Selenium版本：使用WebDriver处理Angular SPA应用
 * 能够处理JavaScript渲染的动态内容
 */
public class CBPRulingCrawlerSelenium {
    // 基础URL
    private static final String BASE_URL = "https://rulings.cbp.gov/search";
    // 日期格式化器（页面日期格式为M/d/yyyy，如6/23/2025）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    // 等待时间
    private static final int WAIT_TIMEOUT_SECONDS = 30;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * 初始化WebDriver - 使用完全无头模式，不依赖Chrome浏览器
     */
    public void initDriver() {
        try {
            System.out.println("正在初始化无头ChromeDriver...");
            
//            // 设置ChromeDriver路径
//            String chromedriverPath = findChromeDriver();
//            if (chromedriverPath != null) {
//                System.setProperty("webdriver.chrome.driver", chromedriverPath);
//                System.out.println("使用ChromeDriver路径: " + chromedriverPath);
//            } else {
//                System.out.println("未找到ChromeDriver，尝试使用系统PATH中的驱动");
//            }
            
            ChromeOptions options = new ChromeOptions();
            
            // 完全无头模式配置
            options.addArguments("--headless=new"); // 使用新的无头模式
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-images"); // 禁用图片加载以提高速度
            // 注意：不禁用JavaScript，因为需要JavaScript来渲染Angular SPA
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-features=VizDisplayCompositor");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-field-trial-config");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("--disable-default-apps");
            options.addArguments("--disable-sync");
            options.addArguments("--disable-translate");
            options.addArguments("--hide-scrollbars");
            options.addArguments("--mute-audio");
            options.addArguments("--no-first-run");
            options.addArguments("--no-default-browser-check");
            options.addArguments("--disable-hang-monitor");
            options.addArguments("--disable-prompt-on-repost");
            options.addArguments("--disable-client-side-phishing-detection");
            options.addArguments("--disable-component-update");
            options.addArguments("--disable-domain-reliability");
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-print-preview");
            options.addArguments("--disable-background-networking");
            options.addArguments("--disable-background-downloads");
            options.addArguments("--disable-background-upload");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=TranslateUI,BlinkGenPropertyTrees");
            options.addArguments("--disable-ipc-flooding-protection");
            
            // 设置实验性功能
            options.addArguments("--enable-features=NetworkService,NetworkServiceLogging");
            
            // 设置用户数据目录（临时目录）
            String userDataDir = System.getProperty("java.io.tmpdir") + "\\chrome-headless-" + System.currentTimeMillis();
            options.addArguments("--user-data-dir=" + userDataDir);
            
            // 设置远程调试端口（无头模式下可选）
            // options.addArguments("--remote-debugging-port=9222");
            
            // 设置日志级别
            options.addArguments("--log-level=3");
            options.addArguments("--silent");
            
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
     *
     * @param term         搜索关键词（如"9018"）
     * @param pageSize     每页记录数
     * @param maxPages     最大爬取页数（防止无限循环）
     * @param endDate      截止日期（只保留此日期及之前的数据）
     */
    public void crawl(String term, int pageSize, int maxPages, LocalDate endDate) {
        List<RulingRecord> allRecords = new ArrayList<>();
        
        try {
            initDriver();
            System.out.println("=== CBP Rulings 爬虫启动 (Selenium版本) ===");
            System.out.println("搜索关键词: " + term);
            System.out.println("每页记录数: " + pageSize);
            System.out.println("最大页数: " + maxPages);
            System.out.println("截止日期: " + endDate);
            
            // 循环爬取多页
            for (int page = 1; page <= 1; page++) {
//                for (int page = 1; page <= maxPages; page++) {
                System.out.println("\n===== 爬取第 " + page + " 页 =====");
                boolean hasValidData = false; // 标记当前页是否有符合条件的数据

                try {
                    // 1. 构建请求URL
                    String url = buildUrl(term, pageSize, page);
                    // 2. 访问页面
                    driver.get(url);
                    
                    // 3. 等待页面加载完成
                    waitForPageLoad();
                    
                    // 4. 解析页面并提取数据
                    List<RulingRecord> records = parsePage(endDate);
                    if (records.isEmpty()) {
                        System.out.println("当前页无符合条件的数据");
                        // 如果当前页无数据，且已爬取超过1页，可能后续也无数据，停止爬取
                        if (page > 1) break;
                    } else {
                        hasValidData = true;
                        allRecords.addAll(records);
                        // 输出符合条件的记录
                        for (int i = 0; i < records.size(); i++) {
                            System.out.println("\n----- 第 " + (i + 1) + " 条记录 -----");
                            RulingRecord record = records.get(i);
                            System.out.println("日期: " + record.date);
                            System.out.println("代码: " + record.code);
                            System.out.println("描述: " + record.description);
                            System.out.println("URL: " + record.url);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("爬取第 " + page + " 页失败: " + e.getMessage());
                    e.printStackTrace();
                }

                // 如果当前页无有效数据，且不是第一页，停止爬取（后续页更旧，可能也无数据）
                if (!hasValidData && page > 1) {
                    System.out.println("后续页面无符合条件的数据，停止爬取");
                    break;
                }
            }
            
            // 保存数据到文件
            if (!allRecords.isEmpty()) {
                saveDataToFiles(allRecords, term);
            }
            
            System.out.println("\n=== 爬取完成 ===");
            System.out.println("总共爬取到 " + allRecords.size() + " 条记录");
        } finally {
            closeDriver();
        }
    }

    /**
     * 构建完整的请求URL
     */
    private String buildUrl(String term, int pageSize, int page) {
        return String.format("%s?term=%s&collection=ALL&sortBy=DATE_DESC&pageSize=%d&page=%d",
                BASE_URL, term, pageSize, page);
    }

    /**
     * 等待页面加载完成
     */
    private void waitForPageLoad() {
        try {
            System.out.println("等待页面加载...");
            
            // 等待页面标题加载
            wait.until(ExpectedConditions.titleContains("CROSS"));
            System.out.println("页面标题已加载");
            
            // 等待数据表格加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("mat-table")));
            System.out.println("数据表格已加载");
            
            // 等待数据行加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("mat-row")));
            System.out.println("数据行已加载");
            
            // 额外等待以确保数据完全加载
            Thread.sleep(3000);
            System.out.println("页面加载完成");
            
        } catch (Exception e) {
            System.err.println("等待页面加载超时: " + e.getMessage());
        }
    }

    /**
     * 解析页面并提取符合条件的记录
     */
    private List<RulingRecord> parsePage(LocalDate endDate) {
        List<RulingRecord> records = new ArrayList<>();

        try {
            // 定位所有数据行
            List<WebElement> rows = driver.findElements(By.cssSelector("mat-table mat-row"));
            
            if (rows.isEmpty()) {
                System.out.println("未找到数据行");
                return records;
            }

            System.out.println("找到 " + rows.size() + " 行数据");

            // 遍历每一行
            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                RulingRecord record = new RulingRecord();

                try {
                    // 1. 提取日期 - 尝试多种CSS选择器
                    WebElement dateElement = null;
                    try {
                        dateElement = row.findElement(By.cssSelector("mat-cell.cdk-column-date font font"));
                    } catch (Exception e1) {
                        try {
                            dateElement = row.findElement(By.cssSelector("mat-cell.cdk-column-date"));
                        } catch (Exception e2) {
                            try {
                                dateElement = row.findElement(By.cssSelector("[class*='date']"));
                            } catch (Exception e3) {
                                // 如果都找不到，尝试获取第一个单元格
                                List<WebElement> cells = row.findElements(By.cssSelector("mat-cell"));
                                if (!cells.isEmpty()) {
                                    dateElement = cells.get(0);
                                }
                            }
                        }
                    }
                    
                    if (dateElement != null) {
                        record.date = dateElement.getText().trim();
                    } else {
                        System.err.println("无法找到日期元素，跳过第 " + (i + 1) + " 行");
                        continue;
                    }

                    // 2. 过滤：只保留截止日期之前的数据
                    if (!isDateValid(record.date, endDate)) {
                        System.out.println("跳过日期不符合的记录: " + record.date);
                        break;
//                        continue; // 日期无效或超过截止日期，跳过
                    }

                    // 3. 提取代码 - 尝试多种CSS选择器
                    WebElement codeElement = null;
                    try {
                        codeElement = row.findElement(By.cssSelector("mat-cell.cdk-column-category div div:nth-child(3)"));
                    } catch (Exception e1) {
                        try {
                            codeElement = row.findElement(By.cssSelector("mat-cell.cdk-column-category"));
                        } catch (Exception e2) {
                            try {
                                codeElement = row.findElement(By.cssSelector("[class*='category']"));
                            } catch (Exception e3) {
                                // 如果都找不到，尝试获取第二个单元格
                                List<WebElement> cells = row.findElements(By.cssSelector("mat-cell"));
                                if (cells.size() > 1) {
                                    codeElement = cells.get(1);
                                }
                            }
                        }
                    }
                    
                    if (codeElement != null) {
                        record.code = codeElement.getText().trim().replaceAll("\\s+", " ");
                    } else {
                        record.code = "未知代码";
                    }

                    // 4. 提取描述 - 使用您提供的精确CSS选择器
                    WebElement descElement = null;
                    try {
                        // 使用您提供的精确选择器获取描述
                        descElement = row.findElement(By.cssSelector("mat-cell.cdk-column-title span"));
                    } catch (Exception e1) {
                        try {
                            // 备用选择器
                            descElement = row.findElement(By.cssSelector("mat-cell.cdk-column-title"));
                        } catch (Exception e2) {
                            try {
                                // 更通用的选择器
                                descElement = row.findElement(By.cssSelector("[class*='title'] span"));
                            } catch (Exception e3) {
                                // 最后尝试查找包含title的单元格
                                List<WebElement> cells = row.findElements(By.cssSelector("mat-cell"));
                                for (WebElement cell : cells) {
                                    if (cell.getAttribute("class").contains("title")) {
                                        descElement = cell;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (descElement != null) {
                        record.description = descElement.getText().trim();
                        System.out.println("  描述内容: '" + record.description + "'");
                    } else {
                        record.description = "无描述";
                        System.out.println("  未找到描述元素");
                    }

                    // 5. 提取URL - 查找包含href="/ruling/"的链接
                    WebElement linkElement = null;
                    try {
                        // 首先尝试在当前行中查找链接
                        linkElement = row.findElement(By.cssSelector("a[href^='/ruling/']"));
                    } catch (Exception e1) {
                        try {
                            // 尝试查找所有链接
                            List<WebElement> links = row.findElements(By.cssSelector("a"));
                            for (WebElement link : links) {
                                String href = link.getAttribute("href");
                                if (href != null && href.contains("/ruling/")) {
                                    linkElement = link;
                                    break;
                                }
                            }
                        } catch (Exception e2) {
                            // 如果找不到链接，尝试获取最后一个单元格
                            List<WebElement> cells = row.findElements(By.cssSelector("mat-cell"));
                            if (cells.size() > 2) {
                                try {
                                    List<WebElement> cellLinks = cells.get(cells.size() - 1).findElements(By.cssSelector("a"));
                                    if (!cellLinks.isEmpty()) {
                                        linkElement = cellLinks.get(0);
                                    }
                                } catch (Exception e3) {
                                    // 忽略
                                }
                            }
                        }
                    }
                    
                    if (linkElement != null) {
                        String href = linkElement.getAttribute("href");
                        // 确保URL是完整的
                        if (href != null && href.startsWith("/")) {
                            record.url = "https://rulings.cbp.gov" + href;
                        } else {
                            record.url = href;
                        }
                        System.out.println("  链接URL: " + record.url);
                    } else {
                        record.url = "";
                        System.out.println("  未找到链接元素");
                    }

                    // 5. 尝试查找RULING REFERENCE或其他相关描述内容
                    if (record.description.isEmpty() || record.description.equals("无描述")) {
                        try {
                            // 查找包含"RULING REFERENCE"或类似文本的元素
                            List<WebElement> rulingRefElements = row.findElements(By.xpath(".//*[contains(text(), 'RULING REFERENCE') or contains(text(), 'Ruling Reference') or contains(text(), 'ruling reference')]"));
                            if (!rulingRefElements.isEmpty()) {
                                for (WebElement refElement : rulingRefElements) {
                                    String refText = refElement.getText().trim();
                                    if (!refText.isEmpty()) {
                                        record.description = refText;
                                        System.out.println("  找到RULING REFERENCE: " + refText);
                                        break;
                                    }
                                }
                            }
                            
                            // 如果还是空的，尝试查找所有文本内容
                            if (record.description.isEmpty() || record.description.equals("无描述")) {
                                String rowText = row.getText();
                                System.out.println("  整行文本内容: " + rowText);
                                
                                // 尝试从整行文本中提取描述（排除日期和代码）
                                String[] lines = rowText.split("\n");
                                for (String line : lines) {
                                    line = line.trim();
                                    if (!line.isEmpty() && 
                                        !line.equals(record.date) && 
                                        !line.equals(record.code) &&
                                        !line.matches("\\d{1,2}/\\d{1,2}/\\d{4}") && // 不是日期格式
                                        !line.matches("\\d+") && // 不是纯数字
                                        line.length() > 10) { // 长度足够作为描述
                                        record.description = line;
                                        System.out.println("  从整行文本提取描述: " + line);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("  查找RULING REFERENCE时出错: " + e.getMessage());
                        }
                    }

                    records.add(record);
                    System.out.println("成功解析第 " + (i + 1) + " 行: " + record.date + " - " + record.code);

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
     * 验证日期是否有效且在截止日期之前
     */
    private boolean isDateValid(String dateStr, LocalDate endDate) {
        try {
            if (dateStr == null || dateStr.isEmpty()) return false;
            LocalDate recordDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            // 记录日期 >= 截止日期
            return !recordDate.isBefore(endDate);
        } catch (DateTimeParseException e) {
            System.err.println("日期格式错误（" + dateStr + "）：" + e.getMessage());
            return false;
        }
    }

    /**
     * 存储每条记录的实体类
     */
    public static class RulingRecord {
        public String date;
        public String code;
        public String description;
        public String url;

        @Override
        public String toString() {
            return String.format("RulingRecord{date='%s', code='%s', description='%s', url='%s'}", 
                    date, code, description, url);
        }
    }

    /**
     * 保存数据到文件（CSV和JSON格式）
     */
    private void saveDataToFiles(List<RulingRecord> records, String term) {
        try {
            // 创建输出目录
            String outputDir = "crawler_output";
            Files.createDirectories(Paths.get(outputDir));
            
            // 生成文件名（包含时间戳和随机数，避免文件冲突）
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomSuffix = String.valueOf(System.currentTimeMillis() % 10000);
            String baseFileName = String.format("cbp_rulings_%s_%s_%s", term, timestamp, randomSuffix);
            
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
    private boolean saveToCSV(List<RulingRecord> records, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, StandardCharsets.UTF_8))) {
            // 写入CSV头部
            writer.println("日期,代码,描述,URL");
            
            // 写入数据行
            for (RulingRecord record : records) {
                // 转义CSV中的特殊字符
                String date = escapeCSV(record.date);
                String code = escapeCSV(record.code);
                String description = escapeCSV(record.description);
                String url = escapeCSV(record.url);
                
                writer.println(String.format("%s,%s,%s,%s", date, code, description, url));
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
    private boolean saveToJSON(List<RulingRecord> records, String fileName) {
        try {
            // 创建包含元数据的JSON对象
            var jsonData = new java.util.HashMap<String, Object>();
            jsonData.put("searchTerm", "9018");
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
        CBPRulingCrawlerSelenium crawler = new CBPRulingCrawlerSelenium();
        // 爬取关键词"9018"、每页30条、最多爬取5页、截止日期2025-06-01之前的数据
        crawler.crawl("9018", 30, 5, LocalDate.of(2025, 6, 1));
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
