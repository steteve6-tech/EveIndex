package com.certification.crawler.countrydata.us;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.entity.common.GuidanceDocument;
import com.certification.repository.common.FDAGuidanceDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.certification.utils.CrawlerDuplicateDetector;

/**
 * FDA指导文档爬虫 - 爬取FDA医疗设备指导文档并保存到数据库
 * 网址：https://www.fda.gov/medical-devices/device-advice-comprehensive-regulatory-assistance/guidance-documents-medical-devices-and-radiation-emitting-products
 */
@Slf4j
@Component
public class US_Guidance {
    
    // 基础URL
    private static final String BASE_URL = "https://www.fda.gov/medical-devices/device-advice-comprehensive-regulatory-assistance/guidance-documents-medical-devices-and-radiation-emitting-products";
    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Autowired
    private FDAGuidanceDocumentRepository guidanceDocumentRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * 初始化WebDriver - 使用完全无头模式
     */
    public void initDriver() {
        try {
            log.info("正在初始化无头ChromeDriver...");
            
            // 设置ChromeDriver路径
            String chromedriverPath = findChromeDriver();
            if (chromedriverPath != null) {
                System.setProperty("webdriver.chrome.driver", chromedriverPath);
                log.info("使用ChromeDriver路径: " + chromedriverPath);
            } else {
                log.info("未找到ChromeDriver，尝试使用系统PATH中的驱动");
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
            
            log.info("使用完全无头模式，不依赖Chrome浏览器安装");
            driver = new ChromeDriver(options);
            log.info("无头ChromeDriver初始化成功！");
            
        } catch (Exception e) {
            log.error("ChromeDriver 初始化失败：" + e.getMessage());
            log.error("请确保：");
            log.error("1. 已下载ChromeDriver");
            log.error("2. ChromeDriver版本与系统兼容");
            log.error("3. 文件路径正确");
            throw new RuntimeException("无法初始化无头ChromeDriver", e);
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(crawlerConfig.getTimeout().getPageLoadTimeoutSeconds()));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(crawlerConfig.getTimeout().getWaitTimeoutSeconds()));
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
     * 带重试机制的操作执行
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String operationName) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= crawlerConfig.getRetry().getMaxAttempts(); attempt++) {
            try {
                log.debug("执行{}操作，第{}次尝试", operationName, attempt);
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("{}操作第{}次尝试失败: {}", operationName, attempt, e.getMessage());
                
                if (attempt < crawlerConfig.getRetry().getMaxAttempts()) {
                    try {
                        Thread.sleep(crawlerConfig.getRetry().getDelayMilliseconds());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("操作被中断", ie);
                    }
                }
            }
        }
        
        log.error("{}操作失败，已重试{}次", operationName, crawlerConfig.getRetry().getMaxAttempts());
        throw new RuntimeException(operationName + "操作失败", lastException);
    }

    /**
     * 爬取数据的主方法
     */
    public void crawl() {
        crawlWithLimit(-1); // 默认爬取所有数据
    }

    /**
     * 爬取数据的主方法（带数量限制和分批保存）
     */
    public void crawlWithLimit(int maxRecords) {
        try {
            initDriver();
            log.info("=== FDA指导文档爬虫启动 ===");
            log.info("目标网址: " + BASE_URL);
            log.info("最大记录数: " + (maxRecords > 0 ? maxRecords : "全部"));
            log.info("批量保存大小: " + crawlerConfig.getBatch().getSmallSaveSize() + " 条/批");
            
            // 访问页面
            driver.get(BASE_URL);
            
            // 等待页面加载完成
            waitForPageLoad();
            
            // 选择显示所有数据
            selectShowAllRecords();
            
            // 解析页面并分批提取数据
            parsePageWithBatchSave(maxRecords);
            
            log.info("=== 爬取完成 ===");
        } finally {
            closeDriver();
        }
    }

    /**
     * 等待页面加载完成
     */
    private void waitForPageLoad() {
        try {
            log.info("等待页面加载...");
            
            // 等待页面标题加载
            wait.until(ExpectedConditions.titleContains("Guidance Documents"));
            log.info("页面标题已加载");
            
            // 等待数据表格加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0_wrapper")));
            log.info("数据表格已加载");
            
            // 等待数据行加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0 tbody tr")));
            log.info("数据行已加载");
            
            // 额外等待以确保数据完全加载
            Thread.sleep(3000);
            log.info("页面加载完成");
            
        } catch (Exception e) {
            log.error("等待页面加载超时: " + e.getMessage());
        }
    }

    /**
     * 选择显示所有数据
     */
    private void selectShowAllRecords() {
        try {
            log.info("尝试选择显示所有数据...");
            
            // 等待下拉选择框加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0_length > label > select")));
            log.info("找到数据长度选择框");
            
            // 查找下拉选择框
            WebElement selectElement = driver.findElement(By.cssSelector("#DataTables_Table_0_length > label > select"));
            
            // 查找包含"ALL"的选项
            List<WebElement> options = selectElement.findElements(By.cssSelector("option"));
            WebElement allOption = null;
            
            for (WebElement option : options) {
                String optionText = option.getText().trim();
                if ("ALL".equalsIgnoreCase(optionText)) {
                    allOption = option;
                    break;
                }
            }
            
            if (allOption != null) {
                // 点击选择"ALL"选项
                allOption.click();
                log.info("成功选择显示所有数据");
                
                // 等待数据重新加载
                Thread.sleep(2000);
                
                // 等待数据行重新加载
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0 tbody tr")));
                log.info("数据重新加载完成");
                
            } else {
                log.warn("未找到'ALL'选项，使用默认设置");
            }
            
        } catch (Exception e) {
            log.error("选择显示所有数据失败: " + e.getMessage());
            log.info("继续使用默认设置");
        }
    }

    /**
     * 解析页面并分批保存数据
     */
    private void parsePageWithBatchSave(int maxRecords) {
        List<GuidanceDocument> batchToSave = new ArrayList<>();
        int totalProcessed = 0;
        int totalSaved = 0;
        int totalSkipped = 0;
        
        try {
            // 等待数据表格完全加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0 tbody tr")));
            
            // 获取数据行数量（在页面重新加载后）
            List<WebElement> rows = driver.findElements(By.cssSelector("#DataTables_Table_0 tbody tr"));
            
            if (rows.isEmpty()) {
                log.info("未找到数据行");
                return;
            }

            int totalRows = rows.size(); // 保存行数，避免使用Stale Element
            log.info("找到 " + totalRows + " 行数据");

            // 遍历每一行 - 分批处理
            for (int i = 0; i < totalRows; i++) {
                // 检查是否达到最大记录数限制
                if (maxRecords > 0 && totalProcessed >= maxRecords) {
                    log.info("已达到最大记录数限制: " + maxRecords);
                    break;
                }
                
                GuidanceDocument record = new GuidanceDocument();

                try {
                    // 添加短暂等待确保页面稳定
                    Thread.sleep(100);
                    
                    // 使用CSS选择器直接定位到特定行，避免使用WebElement引用
                    String rowSelector = "#DataTables_Table_0 tbody tr:nth-child(" + (i + 1) + ")";
                    
                    // 解析单行数据
                    parseSingleRow(record, rowSelector, i);
                    
                    // 添加到批次
                    batchToSave.add(record);
                    totalProcessed++;
                    
                    log.info("解析第 " + (i + 1) + " 行完成: " + record.getTitle());
                    
                    // 每配置的批量大小条数据保存一次
                    if (batchToSave.size() >= crawlerConfig.getBatch().getSmallSaveSize()) {
                        int[] result = saveBatchToDatabase(batchToSave);
                        totalSaved += result[0];
                        totalSkipped += result[1];
                        batchToSave.clear();
                        log.info("已保存 " + totalSaved + " 条记录，跳过 " + totalSkipped + " 条重复记录");
                    }
                    
                } catch (Exception e) {
                    log.error("解析第 " + (i + 1) + " 行时出错: " + e.getMessage());
                    continue;
                }
            }
            
            // 保存剩余的数据
            if (!batchToSave.isEmpty()) {
                int[] result = saveBatchToDatabase(batchToSave);
                totalSaved += result[0];
                totalSkipped += result[1];
                log.info("保存最后一批 " + batchToSave.size() + " 条记录");
            }
            
            log.info("=== 分批保存完成 ===");
            log.info("总共处理: " + totalProcessed + " 条记录");
            log.info("成功保存: " + totalSaved + " 条记录");
            log.info("跳过重复: " + totalSkipped + " 条记录");
            
        } catch (Exception e) {
            log.error("解析页面时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 解析单行数据
     */
    private void parseSingleRow(GuidanceDocument record, String rowSelector, int rowIndex) {
        // 1. 提取标题和URL - 使用CSS选择器直接定位
        WebElement titleElement = null;
        try {
            titleElement = driver.findElement(By.cssSelector(rowSelector + " td a"));
        } catch (Exception e1) {
            try {
                titleElement = driver.findElement(By.cssSelector(rowSelector + " a"));
            } catch (Exception e2) {
                try {
                    List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                    if (!cells.isEmpty()) {
                        titleElement = cells.get(0).findElement(By.cssSelector("a"));
                    }
                } catch (Exception e3) {
                    // 忽略
                }
            }
        }
        
        if (titleElement != null) {
            record.setTitle(titleElement.getText().trim());
            String href = titleElement.getAttribute("href");
            if (href != null && href.startsWith("/")) {
                record.setDocumentUrl("https://www.fda.gov" + href);
            } else {
                record.setDocumentUrl(href);
            }
        } else {
            record.setTitle("无标题");
            record.setDocumentUrl("");
        }

        // 2. 提取日期 - 使用CSS选择器直接定位
        WebElement dateElement = null;
        try {
            dateElement = driver.findElement(By.cssSelector(rowSelector + " td.sorting_1"));
        } catch (Exception e1) {
            try {
                dateElement = driver.findElement(By.cssSelector(rowSelector + " td font[dir='auto']"));
            } catch (Exception e2) {
                try {
                    List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                    if (cells.size() >= 2) {
                        dateElement = cells.get(1); // 第2列通常是日期
                    }
                } catch (Exception e3) {
                    // 忽略
                }
            }
        }
        
        if (dateElement != null) {
            String dateText = dateElement.getText().trim();
            record.setPublicationDate(parseDate(dateText));
        } else {
            record.setPublicationDate(null);
        }

        // 3. 提取话题（第4列）- 使用CSS选择器直接定位
        WebElement topicElement = null;
        try {
            List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
            if (cells.size() >= 4) {
                topicElement = cells.get(3); // 第4列通常是话题
            }
        } catch (Exception e1) {
            try {
                List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                for (WebElement cell : cells) {
                    String cellText = cell.getText().trim();
                    if (cellText.contains("Premarket") || cellText.contains("Biologics") || 
                        cellText.contains("Drugs") || cellText.contains("Medical Devices") || 
                        cellText.contains("Digital Health")) {
                        topicElement = cell;
                        break;
                    }
                }
            } catch (Exception e2) {
                // 忽略
            }
        }
        
        if (topicElement != null) {
            record.setTopic(topicElement.getText().trim());
        } else {
            record.setTopic("无话题");
        }

        // 4. 提取指导状态（第5列）
        WebElement statusElement = null;
        try {
            List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
            if (cells.size() >= 5) {
                statusElement = cells.get(4); // 第5列通常是指导状态
            }
        } catch (Exception e1) {
            try {
                List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                for (WebElement cell : cells) {
                    String cellText = cell.getText().trim();
                    if (cellText.contains("Final") || cellText.contains("Draft") || 
                        cellText.contains("Withdrawn")) {
                        statusElement = cell;
                        break;
                    }
                }
            } catch (Exception e2) {
                // 忽略
            }
        }
        
        if (statusElement != null) {
            record.setGuidanceStatus(statusElement.getText().trim());
        } else {
            record.setGuidanceStatus("无状态");
        }

        // 设置其他字段
        record.setDocumentType("GUIDANCE");
        record.setDataSource("FDA");
        record.setJdCountry("US");
        record.setCrawlTime(LocalDateTime.now());
        record.setDataStatus("ACTIVE");
        // createTime 和 updateTime 由 JPA 审计自动设置，无需手动设置
    }

    /**
     * 批量保存数据到数据库
     * @return int数组，[0]为保存数量，[1]为跳过数量
     */
    private int[] saveBatchToDatabase(List<GuidanceDocument> batch) {
        if (guidanceDocumentRepository == null) {
            log.warn("Repository未初始化，跳过数据库保存");
            return new int[]{0, batch.size()};
        }

        int savedCount = 0;
        int skippedCount = 0;

        // 初始化批次检测器（如果是首次调用）
        CrawlerDuplicateDetector detector = new CrawlerDuplicateDetector(3);

        for (GuidanceDocument record : batch) {
            try {
                // 使用更健壮的重复检查方法
                boolean exists = false;
                
                // 方法1: 检查标题是否存在
                if (record.getTitle() != null && !record.getTitle().trim().isEmpty()) {
                    exists = guidanceDocumentRepository.findByTitle(record.getTitle()).isPresent();
                }
                
                // 方法2: 如果标题检查失败，检查URL是否存在
                if (!exists && record.getDocumentUrl() != null && !record.getDocumentUrl().trim().isEmpty()) {
                    exists = guidanceDocumentRepository.findByDocumentUrl(record.getDocumentUrl()).isPresent();
                }
                
                // 方法3: 如果前两个方法都失败，尝试组合检查
                if (!exists && record.getTitle() != null && record.getDocumentUrl() != null) {
                    exists = guidanceDocumentRepository.existsByTitleAndDocumentUrl(record.getTitle(), record.getDocumentUrl());
                }
                
                if (exists) {
                    log.info("跳过已存在的记录: " + record.getTitle());
                    skippedCount++;
                    continue;
                }
                
                // 保存到数据库
                guidanceDocumentRepository.save(record);
                log.info("成功保存记录: " + record.getTitle());
                savedCount++;
                
            } catch (Exception e) {
                // 检查是否是重复键错误
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    log.info("跳过重复记录: " + record.getTitle());
                    skippedCount++;
                } else {
                    log.error("保存记录失败: " + record.getTitle() + " - " + e.getMessage());
                }
            }
        }

        // 批次检测（检查本批次是否全部重复）
        boolean shouldStop = detector.recordBatch(batch.size(), savedCount);
        if (shouldStop) {
            log.warn("⚠️ 检测到连续重复批次，建议停止爬取");
        }

        // 打印统计
        detector.printFinalStats("US_Guidance");

        return new int[]{savedCount, skippedCount};
    }

    /**
     * 解析页面并提取数据（保留原方法用于兼容性）
     */
    private List<GuidanceDocument> parsePage() {
        List<GuidanceDocument> records = new ArrayList<>();

        try {
            // 等待数据表格完全加载
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#DataTables_Table_0 tbody tr")));
            
            // 获取数据行数量（在页面重新加载后）
            List<WebElement> rows = driver.findElements(By.cssSelector("#DataTables_Table_0 tbody tr"));
            
            if (rows.isEmpty()) {
                log.info("未找到数据行");
                return records;
            }

            int totalRows = rows.size(); // 保存行数，避免使用Stale Element
            log.info("找到 " + totalRows + " 行数据");

            // 遍历每一行 - 使用更安全的方法避免Stale Element问题
            for (int i = 0; i < totalRows; i++) {
                GuidanceDocument record = new GuidanceDocument();

                try {
                    // 添加短暂等待确保页面稳定
                    Thread.sleep(100);
                    
                    // 使用CSS选择器直接定位到特定行，避免使用WebElement引用
                    String rowSelector = "#DataTables_Table_0 tbody tr:nth-child(" + (i + 1) + ")";
                    
                    // 1. 提取标题和URL - 使用CSS选择器直接定位
                    WebElement titleElement = null;
                    try {
                        titleElement = driver.findElement(By.cssSelector(rowSelector + " td a"));
                    } catch (Exception e1) {
                        try {
                            titleElement = driver.findElement(By.cssSelector(rowSelector + " a"));
                        } catch (Exception e2) {
                            try {
                                List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                            if (!cells.isEmpty()) {
                                    titleElement = cells.get(0).findElement(By.cssSelector("a"));
                                }
                                } catch (Exception e3) {
                                    // 忽略
                            }
                        }
                    }
                    
                    if (titleElement != null) {
                        record.setTitle(titleElement.getText().trim());
                        String href = titleElement.getAttribute("href");
                        if (href != null && href.startsWith("/")) {
                            record.setDocumentUrl("https://www.fda.gov" + href);
                        } else {
                            record.setDocumentUrl(href);
                        }
                        log.info("  标题: '" + record.getTitle() + "'");
                        log.info("  URL: " + record.getDocumentUrl());
                    } else {
                        record.setTitle("无标题");
                        record.setDocumentUrl("");
                        log.info("  未找到标题元素");
                    }

                    // 2. 提取日期 - 使用CSS选择器直接定位
                    WebElement dateElement = null;
                    try {
                        dateElement = driver.findElement(By.cssSelector(rowSelector + " td.sorting_1"));
                    } catch (Exception e1) {
                        try {
                            dateElement = driver.findElement(By.cssSelector(rowSelector + " td font[dir='auto']"));
                        } catch (Exception e2) {
                            // 通过索引直接获取第2列
                            try {
                                List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                                if (cells.size() >= 2) {
                                    dateElement = cells.get(1); // 第2列通常是日期
                                }
                            } catch (Exception e3) {
                                // 忽略
                            }
                        }
                    }
                    
                    if (dateElement != null) {
                        String dateText = dateElement.getText().trim();
                        record.setPublicationDate(parseDate(dateText));
                        log.info("  日期: " + dateText);
                    } else {
                        record.setPublicationDate(null);
                        log.info("  未找到日期元素");
                    }

                    // 3. 提取话题（第4列）
                    WebElement topicElement = null;
                    try {
                        List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                        if (cells.size() >= 4) {
                            topicElement = cells.get(3); // 第4列���常是话题
                        }
                    } catch (Exception e1) {
                        // 尝试查找包含话题关键词的单元格
                        List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
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
                        record.setTopic(topicElement.getText().trim());
                        log.info("  话题: " + record.getTopic());
                    } else {
                        record.setTopic("无话题");
                        log.info("  未找到话题元素");
                    }

                    // 4. 提取指导状态（第5列）
                    WebElement statusElement = null;
                    try {
                        List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
                        if (cells.size() >= 5) {
                            statusElement = cells.get(4); // 第5列通常是指导状态
                        }
                    } catch (Exception e1) {
                        // 尝试查找包含状态关键词的单元格
                        List<WebElement> cells = driver.findElements(By.cssSelector(rowSelector + " td"));
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
                        record.setGuidanceStatus(statusElement.getText().trim());
                        log.info("  指导状态: " + record.getGuidanceStatus());
                    } else {
                        record.setGuidanceStatus("无状态");
                        log.info("  未找到指导状态元素");
                    }

                    // 设置其他字段
                    record.setSourceUrl(BASE_URL);
                    record.setCrawlTime(LocalDateTime.now());
                    record.setDataStatus("ACTIVE");
                    // 新增：设置数据来源与国家
                    record.setDataSource("FDA");
                    record.setJdCountry("US");

                    records.add(record);
                    log.info("成功解析第 " + (i + 1) + " 行");

                } catch (Exception e) {
                    log.error("解析第 " + (i + 1) + " 行时出错: " + e.getMessage());
                    continue;
                }
            }

        } catch (Exception e) {
            log.error("解析页面时出错: " + e.getMessage());
        }
        return records;
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateText) {
        try {
            // 尝试解析 MM/dd/yyyy 格式
            return LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (Exception e) {
            try {
                // 尝试解析其他格式
                if (dateText.matches(".*\\d{4}年\\d{1,2}月\\d{1,2}日.*")) {
                    // 解析中文日期格式
                    String[] parts = dateText.replaceAll("[年月日]", " ").trim().split("\\s+");
                    if (parts.length >= 3) {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day = Integer.parseInt(parts[2]);
                        return LocalDate.of(year, month, day);
                    }
                }
            } catch (Exception e2) {
                log.warn("无法解析日期: " + dateText);
            }
        }
        return null;
    }

    /**
     * 保存数据到数据库
     */
    private void saveToDatabase(List<GuidanceDocument> records) {
        int savedCount = 0;
        int skippedCount = 0;
        
        for (GuidanceDocument record : records) {
            try {
                // 检查是否已存在相同的记录
                if (guidanceDocumentRepository.existsByTitleAndDocumentUrl(record.getTitle(), record.getDocumentUrl())) {
                    log.info("跳过已存在的记录: " + record.getTitle());
                    skippedCount++;
                    continue;
                }
                
                // 保存到数据库
                GuidanceDocument savedRecord = guidanceDocumentRepository.save(record);
                log.info("成功保存记录: " + savedRecord.getTitle() + " (ID: " + savedRecord.getId() + ")");
                savedCount++;
                
            } catch (Exception e) {
                log.error("保存记录失败: " + record.getTitle() + " - " + e.getMessage());
            }
        }
        
        log.info("数据库保存完成 - 新增: " + savedCount + " 条，跳过: " + skippedCount + " 条");
        
        // 如果所有数据都是重复的，抛出异常停止爬取
        if (savedCount == 0 && skippedCount > 0) {
            log.info("批次数据全部重复，停止爬取");
            throw new RuntimeException("批次数据全部重复，停止爬取");
        }
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
            if (p != null && java.nio.file.Files.exists(java.nio.file.Paths.get(p))) {
                return p;
            }
        }
        return null;
    }

    /**
     * 基于关键词列表爬取FDA指导文档数据
     * 注意：FDA指导文档爬虫不支持关键词搜索，此方法会忽略关键词参数
     * 仅用于适配统一的爬虫接口
     * 
     * @param inputKeywords 关键词列表（此爬虫不使用）
     * @param maxRecords 最大记录数（-1表示全部）
     * @param batchSize 批次大小（未使用，由配置决定）
     * @param dateFrom 开始日期（未使用）
     * @param dateTo 结束日期（未使用）
     * @return 爬取结果描述
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        log.info("开始爬取FDA指导文档数据...");
        log.info("注意：此爬虫不支持关键词搜索，将爬取所有数据");
        log.info("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));
        
        try {
            // 调用主爬取方法
            if (maxRecords == -1) {
                crawl();
            } else {
                crawlWithLimit(maxRecords);
            }
            
            // 返回成功消息
            String result = String.format("FDA指导文档爬取完成，最大记录数: %d", maxRecords);
            log.info(result);
            return result;
            
        } catch (Exception e) {
            String errorMsg = "FDA指导文档爬取失败: " + e.getMessage();
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 基于关键词列表爬取FDA指导文档数据（简化版本，无时间范围）
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize) {
        return crawlAndSaveWithKeywords(inputKeywords, maxRecords, batchSize, null, null);
    }

}
