package com.certification.crawler.certification;

import com.certification.crawler.certification.base.BaseCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.common.HttpUtils;
import com.certification.entity.common.CertNewsData;
import com.certification.service.DateFormatService;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UL Solutions爬虫实现
 */
@Slf4j
@Component
public class ULCrawler implements BaseCrawler {
    
    @Autowired
    private HttpUtils httpUtils;
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private DateFormatService dateFormatService;
    
    private final CrawlerConfig config;
    
    // 顺序ID生成器
    private static final AtomicLong idCounter = new AtomicLong(System.currentTimeMillis());

    /**
     * 生成顺序ID（使用时间戳+随机数确保唯一性）
     * @return 顺序ID字符串
     */
    private String generateSequentialId() {
        long timestamp = System.currentTimeMillis();
        long sequence = idCounter.getAndIncrement();
        int random = (int) (Math.random() * 100); // 减少随机数范围
        // 使用更短的ID格式：UL_时间戳后8位_序列号_随机数
        String shortTimestamp = String.valueOf(timestamp).substring(5); // 取后8位
        return String.format("UL_%s_%d_%02d", shortTimestamp, sequence % 10000, random);
    }
    
         // 日期正则表达式 - 支持多种格式
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4} 年 \\d{1,2} 月 \\d{1,2} 日");
     private static final Pattern DATE_PATTERN_WITH_PREFIX = Pattern.compile("發布日期：\\s*(\\d{4} 年 \\d{1,2} 月 \\d{1,2} 日)");
     private static final Pattern DATE_PATTERN_ENGLISH = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
     private static final Pattern DATE_PATTERN_SLASH = Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}");
    
    public ULCrawler() {
        this.config = new CrawlerConfig();
        this.config.setBaseUrl("https://taiwan.ul.com");
        this.config.setTimeout(30000);
        this.config.setRetryCount(3);
        this.config.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
    }
    
    @Override
    public List<CrawlerResult> crawl(String keyword, int totalCount) throws Exception {
        return crawlWithBatchSave(keyword, totalCount, 10, 0);
    }
    
    /**
     * 带分批保存的爬取方法
     * @param keyword 关键词
     * @param totalCount 总爬取数量
     * @param batchSize 每批保存的数量
     * @param startIndex 开始爬取的索引位置
     * @return 爬取结果列表
     * @throws Exception 爬取异常
     */
    public List<CrawlerResult> crawlWithBatchSave(String keyword, int totalCount, int batchSize, int startIndex) throws Exception {
        List<CrawlerResult> result = new ArrayList<>();
        List<CertNewsData> certNewsDataList = new ArrayList<>();
        String url = config.getBaseUrl() + "/gma/";
        
        Document doc = httpUtils.getDocument(url, config.getUserAgent(), config.getTimeout());
        Elements parentContainer = doc.select("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");
        
        int currentIndex = 0;
        int savedCount = 0;
        
        // 爬取显式标签内容
        Elements items = parentContainer.select("li:has(strong a)");
        for (Element item : items) {
            // 跳过已处理的项目
            if (currentIndex < startIndex) {
                currentIndex++;
                continue;
            }
            
            if (result.size() >= totalCount) break;
            
            String type = extractType(item);
            String country = extractCountry(item);
            String title = "";
            String link = "";
            
            Element aEl = item.selectFirst("strong a");
            if (aEl != null) {
                title = aEl.text();
                link = aEl.attr("href");
            }
            
            CrawlerResult crawlerResult = new CrawlerResult(title, link, country + ", " + type, "", "UL Solutions");
            crawlerResult.setCategory("certification");
            crawlerResult.setType(type);
            crawlerResult.setCountry(country);
            result.add(crawlerResult);
            
            // 爬取具体内容
            String detailedContent = "";
            String publishDate = "";
            String productInfo = "";
            String standardInfo = "";
            Map<String, Object> specificContent = null;
            
            try {
                specificContent = crawlSpecificContent(link);
                if (specificContent != null) {
                    detailedContent = extractUsefulContent(specificContent);
                    crawlerResult.setContent(detailedContent);
                    
                    // 提取发布时间
                    if (specificContent.containsKey("发布时间")) {
                        String rawPublishDate = specificContent.get("发布时间").toString();
                        // 统一日期格式
                        publishDate = dateFormatService.standardizeDate(rawPublishDate);
                        if (publishDate == null) {
                            log.warn("UL爬虫无法解析日期格式: {}", rawPublishDate);
                            publishDate = dateFormatService.getCurrentDateString();
                        }
                    }
                    
                    // 提取产品信息
                    if (specificContent.containsKey("产品列表")) {
                        List<String> products = (List<String>) specificContent.get("产品列表");
                        if (products != null && !products.isEmpty()) {
                            productInfo = String.join(", ", products);
                        }
                    }
                    
                    // 提取标准信息
                    if (specificContent.containsKey("标准列表")) {
                        List<String> standards = (List<String>) specificContent.get("标准列表");
                        if (standards != null && !standards.isEmpty()) {
                            standardInfo = String.join(", ", standards);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("爬取具体内容失败: " + link + " - " + e.getMessage());
            }
            
            // 创建CrawlerData对象并添加到列表
            CertNewsData certNewsData = new CertNewsData()
                .setId(generateSequentialId())
                .setSourceName("UL Solutions")
                .setTitle(title)
                .setUrl(link)
                .setSummary(country + ", " + type)
                .setCountry(country)
                .setType(type)
                .setProduct(productInfo)
                // .setRemarks(standardInfo != null && standardInfo.length() > 500 ? standardInfo.substring(0, 500) + "..." : standardInfo)  // 备注字段默认为空，由AI判断填写
                .setContent(detailedContent)
                .setPublishDate(publishDate)
                .setCrawlTime(LocalDateTime.now())
                .setStatus(CertNewsData.DataStatus.NEW)
                .setIsProcessed(false);
            certNewsDataList.add(certNewsData);
            
            // 每batchSize条数据批量保存一次
            if (certNewsDataList.size() >= batchSize) {
                try {
                    crawlerDataService.saveCrawlerDataList(certNewsDataList);
                    savedCount += certNewsDataList.size();
                    System.out.println("批量保存 " + certNewsDataList.size() + " 条爬虫数据到数据库，当前已保存: " + savedCount + " 条");
                    certNewsDataList.clear(); // 清空列表，准备下一批
                } catch (Exception e) {
                    System.err.println("批量保存爬虫数据到数据库失败: " + e.getMessage());
                }
            }
            
            currentIndex++;
        }
        
        // 爬取隐藏标签内容
        Elements hiddenItems = parentContainer.select("ul > li:has(a)");
        for (Element item : hiddenItems) {
            // 跳过已处理的项目
            if (currentIndex < startIndex) {
                currentIndex++;
                continue;
            }
            
            if (result.size() >= totalCount) break;
            
            String country = extractCountryFromHidden(item);
            String title = "";
            String link = "";
            
            Element aEl = item.selectFirst("a");
            if (aEl != null) {
                title = aEl.text();
                link = aEl.attr("href");
            }
            
            CrawlerResult crawlerResult = new CrawlerResult(title, link, country, "", "UL Solutions");
            crawlerResult.setCategory("certification");
            crawlerResult.setType("announcement");
            crawlerResult.setCountry(country);
            result.add(crawlerResult);
            
            // 爬取具体内容
            String detailedContent = "";
            String publishDate = "";
            String productInfo = "";
            String standardInfo = "";
            List<String> releaseDateList = new ArrayList<>();
            List<String> executionDateList = new ArrayList<>();
            Map<String, Object> specificContent = null;
            
            try {
                specificContent = crawlSpecificContent(link);
                if (specificContent != null) {
                    detailedContent = extractUsefulContent(specificContent);
                    crawlerResult.setContent(detailedContent);
                    
                    // 提取发布时间
                    if (specificContent.containsKey("发布时间")) {
                        String rawPublishDate = specificContent.get("发布时间").toString();
                        // 统一日期格式
                        publishDate = dateFormatService.standardizeDate(rawPublishDate);
                        if (publishDate == null) {
                            log.warn("UL爬虫无法解析日期格式: {}", rawPublishDate);
                            publishDate = dateFormatService.getCurrentDateString();
                        }
                    }
                    
                    // 提取产品信息
                    if (specificContent.containsKey("产品列表")) {
                        List<String> products = (List<String>) specificContent.get("产品列表");
                        if (products != null && !products.isEmpty()) {
                            productInfo = String.join(", ", products);
                        }
                    }
                    
                    // 提取标准信息
                    if (specificContent.containsKey("标准列表")) {
                        List<String> standards = (List<String>) specificContent.get("标准列表");
                        if (standards != null && !standards.isEmpty()) {
                            standardInfo = String.join(", ", standards);
                        }
                    }
                    
                    // 提取发布日期列表
                    if (specificContent.containsKey("发布日期列表")) {
                        releaseDateList = (List<String>) specificContent.get("发布日期列表");
                    }
                    
                    // 提取解析日期列表
                    if (specificContent.containsKey("解析日期列表")) {
                        executionDateList = (List<String>) specificContent.get("解析日期列表");
                    }
                }
            } catch (Exception e) {
                System.err.println("爬取具体内容失败: " + link + " - " + e.getMessage());
            }
            
            // 创建CrawlerData对象并添加到列表
            CertNewsData certNewsData = new CertNewsData()
                .setId(generateSequentialId())
                .setSourceName("UL Solutions")
                .setTitle(title)
                .setUrl(link)
                .setSummary(country)
                .setCountry(country)
                .setType("announcement")
                .setProduct(productInfo)
                // .setRemarks(standardInfo != null && standardInfo.length() > 500 ? standardInfo.substring(0, 500) + "..." : standardInfo)  // 备注字段默认为空，由AI判断填写
                .setContent(detailedContent)
                .setPublishDate(publishDate)
                .setReleaseDate(releaseDateList)  // 设置发布日期列表（JSON格式）
                .setExecutionDate(executionDateList)  // 设置解析日期列表（JSON格式）
                .setCrawlTime(LocalDateTime.now())
                .setStatus(CertNewsData.DataStatus.NEW)
                .setIsProcessed(false);
            certNewsDataList.add(certNewsData);
            
            // 每batchSize条数据批量保存一次
            if (certNewsDataList.size() >= batchSize) {
                try {
                    crawlerDataService.saveCrawlerDataList(certNewsDataList);
                    savedCount += certNewsDataList.size();
                    System.out.println("批量保存 " + certNewsDataList.size() + " 条爬虫数据到数据库，当前已保存: " + savedCount + " 条");
                    certNewsDataList.clear(); // 清空列表，准备下一批
                } catch (Exception e) {
                    System.err.println("批量保存爬虫数据到数据库失败: " + e.getMessage());
                }
            }
            
            currentIndex++;
        }
        
        // 爬取details标签内容
        Elements detailsItems = parentContainer.select("details");
        for (Element detailsItem : detailsItems) {
            // 跳过已处理的项目
            if (currentIndex < startIndex) {
                currentIndex++;
                continue;
            }
            
            if (result.size() >= totalCount) break;
            
            // 提取details标签中的链接
            Elements detailLinks = detailsItem.select("a");
            for (Element linkElement : detailLinks) {
                if (result.size() >= totalCount) break;
                
                String title = linkElement.text();
                String link = linkElement.attr("href");
                
                // 从details标签的summary或父级元素中提取国家信息
                String country = extractCountryFromDetails(detailsItem);
                
                CrawlerResult crawlerResult = new CrawlerResult(title, link, country, "", "UL Solutions");
                crawlerResult.setCategory("certification");
                crawlerResult.setType("announcement");
                crawlerResult.setCountry(country);
                result.add(crawlerResult);
                
                // 爬取具体内容
                String detailedContent = "";
                String publishDate = "";
                String productInfo = "";
                String standardInfo = "";
                List<String> releaseDateList = new ArrayList<>();
                List<String> executionDateList = new ArrayList<>();
                Map<String, Object> specificContent = null;
                
                try {
                    specificContent = crawlSpecificContent(link);
                    if (specificContent != null) {
                        detailedContent = extractUsefulContent(specificContent);
                        crawlerResult.setContent(detailedContent);
                        
                        // 提取发布时间
                        if (specificContent.containsKey("发布时间")) {
                            String rawPublishDate = specificContent.get("发布时间").toString();
                            // 统一日期格式
                            publishDate = dateFormatService.standardizeDate(rawPublishDate);
                            if (publishDate == null) {
                                log.warn("UL爬虫无法解析日期格式: {}", rawPublishDate);
                                publishDate = dateFormatService.getCurrentDateString();
                            }
                        }
                        
                        // 提取产品信息
                        if (specificContent.containsKey("产品列表")) {
                            List<String> products = (List<String>) specificContent.get("产品列表");
                            if (products != null && !products.isEmpty()) {
                                productInfo = String.join(", ", products);
                            }
                        }
                        
                        // 提取标准信息
                        if (specificContent.containsKey("标准列表")) {
                            List<String> standards = (List<String>) specificContent.get("标准列表");
                            if (standards != null && !standards.isEmpty()) {
                                standardInfo = String.join(", ", standards);
                            }
                        }
                        
                        // 提取发布日期列表
                        if (specificContent.containsKey("发布日期列表")) {
                            releaseDateList = (List<String>) specificContent.get("发布日期列表");
                        }
                        
                        // 提取解析日期列表
                        if (specificContent.containsKey("解析日期列表")) {
                            executionDateList = (List<String>) specificContent.get("解析日期列表");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("爬取具体内容失败: " + link + " - " + e.getMessage());
                }
                
                // 创建CrawlerData对象并添加到列表
                CertNewsData certNewsData = new CertNewsData()
                    .setSourceName("UL Solutions")
                    .setTitle(title)
                    .setUrl(link)
                    .setSummary(country)
                    .setCountry(country)
                    .setType("announcement")
                    .setProduct(productInfo)
                    // .setRemarks(standardInfo != null && standardInfo.length() > 500 ? standardInfo.substring(0, 500) + "..." : standardInfo)  // 备注字段默认为空，由AI判断填写
                    .setContent(detailedContent)
                    .setPublishDate(publishDate)
                    .setReleaseDate(releaseDateList)  // 设置发布日期列表（JSON格式）
                    .setExecutionDate(executionDateList)  // 设置解析日期列表（JSON格式）
                    .setCrawlTime(LocalDateTime.now())
                    .setStatus(CertNewsData.DataStatus.NEW)
                    .setIsProcessed(false);
                certNewsDataList.add(certNewsData);
                
                // 每batchSize条数据批量保存一次
                if (certNewsDataList.size() >= batchSize) {
                    try {
                        crawlerDataService.saveCrawlerDataList(certNewsDataList);
                        savedCount += certNewsDataList.size();
                        System.out.println("批量保存 " + certNewsDataList.size() + " 条爬虫数据到数据库，当前已保存: " + savedCount + " 条");
                        certNewsDataList.clear(); // 清空列表，准备下一批
                    } catch (Exception e) {
                        System.err.println("批量保存爬虫数据到数据库失败: " + e.getMessage());
                    }
                }
                
                currentIndex++;
            }
        }
        
        // 保存剩余的数据
        if (!certNewsDataList.isEmpty()) {
            try {
                crawlerDataService.saveCrawlerDataList(certNewsDataList);
                savedCount += certNewsDataList.size();
                System.out.println("保存剩余 " + certNewsDataList.size() + " 条爬虫数据到数据库，总共保存: " + savedCount + " 条");
            } catch (Exception e) {
                System.err.println("保存剩余爬虫数据到数据库失败: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 从指定位置继续爬取
     * @param keyword 关键词
     * @param totalCount 总爬取数量
     * @param startIndex 开始爬取的索引位置
     * @return 爬取结果列表
     * @throws Exception 爬取异常
     */
    public List<CrawlerResult> crawlFromPosition(String keyword, int totalCount, int startIndex) throws Exception {
        return crawlWithBatchSave(keyword, totalCount, 10, startIndex);
    }
    
    /**
     * 获取当前可爬取的总数量
     * @return 可爬取的总数量
     * @throws Exception 获取异常
     */
    public int getTotalAvailableCount() throws Exception {
        String url = config.getBaseUrl() + "/gma/";
        Document doc = httpUtils.getDocument(url, config.getUserAgent(), config.getTimeout());
        Elements parentContainer = doc.select("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");
        
        Elements items = parentContainer.select("li:has(strong a)");
        Elements hiddenItems = parentContainer.select("ul > li:has(a)");
        
        return items.size() + hiddenItems.size();
    }
    
    @Override
    public List<CrawlerResult> crawlLatest(int totalCount) throws Exception {
        return crawl("", totalCount);
    }
    
    @Override
    public String getCrawlerName() {
        return "UL Solutions Crawler";
    }
    
    @Override
    public String getSourceName() {
        return "UL Solutions";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return httpUtils.isUrlAccessible(config.getBaseUrl() + "/gma/");
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public CrawlerConfig getConfig() {
        return config;
    }
    
    /**
     * 爬取指定内容页面的详细信息
     *
     * @param url 目标网页URL
     * @return 爬取结果的Map
     * @throws IOException 网络异常或解析异常
     */
    public Map<String, Object> crawlSpecificContent(String url) throws IOException {
        // 发送请求获取文档
        Document doc = httpUtils.getDocument(url, config.getUserAgent(), config.getTimeout());
        System.out.println("开始爬取: " + url);

        // 获取父容器
        Element parentContainer = doc.selectFirst("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");

        if (parentContainer == null) {
            System.out.println("未找到父容器元素");
            return null;
        }

        // 存储爬取结果
        Map<String, Object> result = new HashMap<>();

        // 1. 爬取时间信息 - 支持多种格式
        String publishTime = extractPublishTime(parentContainer);
        result.put("发布时间", publishTime);
        System.out.println("发布时间: " + publishTime);

        // 2. 爬取p:nth-child(5)的内容
        Element p5Element = parentContainer.selectFirst("p:nth-child(8)");
        if (p5Element != null) {
            result.put("段落5", p5Element.text().trim());
        } else {
//            result.put("段落5", "未找到第5个段落");
        }
        System.out.println("段落5: " + result.get("段落5"));

        // 3. 判断table是否存在
        Element tableElement = parentContainer.selectFirst("table");
        result.put("表格存在", tableElement != null);

        // 如果表格存在，解析表格数据
        if (tableElement != null) {
            result.put("表格存在", true);
            List<Map<String, String>> tableData = parseTable(tableElement);
            result.put("表格数据", tableData);
            result.put("表格行数", tableData.size());
            
            // 提取产品信息用于后续处理
            List<String> products = tableData.stream()
                    .map(row -> row.get("对应产品"))
                    .filter(product -> product != null && !product.trim().isEmpty())
                    .toList();
            result.put("产品列表", products);
            
            // 提取标准信息用于后续处理
            List<String> standards = tableData.stream()
                    .map(row -> row.get("标准"))
                    .filter(standard -> standard != null && !standard.trim().isEmpty())
                    .toList();
            result.put("标准列表", standards);
            
            // 提取发布日期列表用于后续处理
            List<String> releaseDates = tableData.stream()
                    .map(row -> row.get("发布日期"))
                    .filter(date -> date != null && !date.trim().isEmpty())
                    .toList();
            result.put("发布日期列表", releaseDates);
            
            // 提取解析日期列表用于后续处理
            List<String> parseDates = tableData.stream()
                    .map(row -> row.get("解析日期"))
                    .filter(date -> date != null && !date.trim().isEmpty())
                    .toList();
            result.put("解析日期列表", parseDates);
            
            System.out.println("表格解析完成，共解析 " + tableData.size() + " 行数据");
        } else {
            // 表格不存在，继续爬取后续段落
            Map<String, String> subsequentParagraphs = new HashMap<>();
            int currentChild = 6; // 从第6个p标签开始

            while (true) {
                // 查找当前序号的p标签
                Element pElement = parentContainer.selectFirst("p:nth-child(" + currentChild + ")");
                if (pElement != null) {
                    subsequentParagraphs.put("段落" + currentChild, pElement.text().trim());
                    currentChild++;
                } else {
                    // 没有更多p标签时退出循环
                    break;
                }
            }

            result.put("后续段落", subsequentParagraphs);
        }

        return result;
    }
    
    /**
     * 保存爬取结果到CSV文件
     *
     * @param contents 爬取结果列表
     * @param filePath 保存路径
     * @throws Exception 文件写入异常
     */
    public void saveToCsv(List<CrawlerResult> contents, String filePath) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("country,type,title,url,content");
        for (CrawlerResult result : contents) {
            String country = result.getCountry() != null ? result.getCountry() : "";
            String type = result.getType() != null ? result.getType() : "";
            lines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"", 
                country, type, result.getTitle(), result.getUrl(), result.getContent()));
        }
        Files.write(Path.of(filePath), lines);
    }
    
    /**
     * 提取类型信息
     * @param item 页面元素
     * @return 类型字符串
     */
    private String extractType(Element item) {
        String html = item.html();
        int pipeIdx = html.indexOf("|");
        int brIdx = html.indexOf("<br>");
        if (pipeIdx != -1 && brIdx != -1 && brIdx > pipeIdx) {
            return html.substring(pipeIdx, brIdx).replace("|", "").trim();
        }
        return "";
    }
    
    /**
     * 提取国家信息
     * @param item 页面元素
     * @return 国家字符串
     */
    private String extractCountry(Element item) {
        Element strongEl = item.selectFirst("strong");
        if (strongEl != null) {
            String strongText = strongEl.text();
            int leftBracket = strongText.indexOf("[");
            int rightBracket = strongText.indexOf("]");
            if (leftBracket != -1 && rightBracket != -1 && rightBracket > leftBracket) {
                return strongText.substring(leftBracket + 1, rightBracket).trim();
            }
        }
        return "";
    }
    
    /**
     * 从隐藏标签中提取国家信息
     * @param item 页面元素
     * @return 国家字符串
     */
    private String extractCountryFromHidden(Element item) {
        String html = item.html();
        int leftBracket = html.indexOf("[");
        int rightBracket = html.indexOf("]");
        if (leftBracket != -1 && rightBracket != -1 && rightBracket > leftBracket) {
            return html.substring(leftBracket + 1, rightBracket).trim();
        }
        return "";
    }
    
    /**
     * 从details标签中提取国家信息
     * @param detailsItem details元素
     * @return 国家字符串
     */
    private String extractCountryFromDetails(Element detailsItem) {
        // 首先尝试从summary标签中提取
        Element summary = detailsItem.selectFirst("summary");
        if (summary != null) {
            String summaryText = summary.text();
            // 查找方括号中的国家信息
            int leftBracket = summaryText.indexOf("[");
            int rightBracket = summaryText.indexOf("]");
            if (leftBracket != -1 && rightBracket != -1 && rightBracket > leftBracket) {
                return summaryText.substring(leftBracket + 1, rightBracket).trim();
            }
            // 如果没有方括号，尝试从summary文本中提取国家信息
            return extractCountryFromText(summaryText);
        }
        
        // 如果没有summary，尝试从details标签的父级元素中提取
        Element parent = detailsItem.parent();
        if (parent != null) {
            String parentText = parent.text();
            return extractCountryFromText(parentText);
        }
        
        return "";
    }
    
    /**
     * 从文本中提取国家信息
     * @param text 文本内容
     * @return 国家字符串
     */
    private String extractCountryFromText(String text) {
        // 常见的国家/地区名称
        String[] countries = {
            "US", "United States", "美国", "USA",
            "CN", "China", "中国",
            "EU", "European Union", "欧盟", "Europe",
            "JP", "Japan", "日本",
            "KR", "Korea", "韩国", "South Korea",
            "TW", "Taiwan", "台湾",
            "HK", "Hong Kong", "香港",
            "SG", "Singapore", "新加坡",
            "AU", "Australia", "澳大利亚",
            "CA", "Canada", "加拿大",
            "UK", "United Kingdom", "英国",
            "DE", "Germany", "德国",
            "FR", "France", "法国",
            "IT", "Italy", "意大利",
            "ES", "Spain", "西班牙",
            "NL", "Netherlands", "荷兰",
            "SE", "Sweden", "瑞典",
            "NO", "Norway", "挪威",
            "DK", "Denmark", "丹麦",
            "FI", "Finland", "芬兰",
            "CH", "Switzerland", "瑞士",
            "AT", "Austria", "奥地利",
            "BE", "Belgium", "比利时",
            "IE", "Ireland", "爱尔兰",
            "PT", "Portugal", "葡萄牙",
            "GR", "Greece", "希腊",
            "PL", "Poland", "波兰",
            "CZ", "Czech Republic", "捷克",
            "HU", "Hungary", "匈牙利",
            "RO", "Romania", "罗马尼亚",
            "BG", "Bulgaria", "保加利亚",
            "HR", "Croatia", "克罗地亚",
            "SI", "Slovenia", "斯洛文尼亚",
            "SK", "Slovakia", "斯洛伐克",
            "LT", "Lithuania", "立陶宛",
            "LV", "Latvia", "拉脱维亚",
            "EE", "Estonia", "爱沙尼亚",
            "CY", "Cyprus", "塞浦路斯",
            "LU", "Luxembourg", "卢森堡",
            "MT", "Malta", "马耳他"
        };
        
        for (String country : countries) {
            if (text.contains(country)) {
                return country;
            }
        }
        
        return "";
    }
    
    /**
     * 表格解析方法
     * @param tableElement 表格元素
     * @return 解析后的表格数据列表
     */
    private List<Map<String, String>> parseTable(Element tableElement) {
        List<Map<String, String>> tableData = new ArrayList<>();
        
        try {
            // 查找表格体
            Element tbody = tableElement.selectFirst("tbody");
            if (tbody == null) {
                System.out.println("未找到表格体元素");
                return tableData;
            }
            
            // 获取所有行
            Elements rows = tbody.select("tr");
            if (rows.isEmpty()) {
                System.out.println("表格中没有找到行数据");
                return tableData;
            }
            
            // 跳过表头行（第一行）
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");
                
                if (cells.size() >= 3) {
                    Map<String, String> rowData = new HashMap<>();
                    
                    // 解析标准列（第一列）
                    Element standardCell = cells.get(0);
                    String standard = extractTextFromCell(standardCell);
                    rowData.put("标准", standard);
                    
                    // 解析对应产品列（第二列）
                    Element productCell = cells.get(1);
                    String product = extractTextFromCell(productCell);
                    rowData.put("对应产品", product);
                    
                    // 解析发布日期列（第三列）
                    Element dateCell = cells.get(2);
                    String publishDate = extractTextFromCell(dateCell);
                    rowData.put("发布日期", publishDate);
                    
                    // 添加解析日期（当前时间）
                    String parseDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    rowData.put("解析日期", parseDate);
                    
                    // 添加到结果列表
                    tableData.add(rowData);
                    
                    System.out.println("解析表格行: 标准=" + standard + ", 产品=" + product + ", 发布日期=" + publishDate + ", 解析日期=" + parseDate);
                }
            }
            
            System.out.println("表格解析完成，共解析 " + tableData.size() + " 行数据");
            
        } catch (Exception e) {
            System.err.println("表格解析过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tableData;
    }
    
    /**
     * 从表格单元格中提取文本内容
     * @param cell 表格单元格元素
     * @return 提取的文本内容
     */
    private String extractTextFromCell(Element cell) {
        if (cell == null) {
            return "";
        }
        
        // 查找div或p标签中的内容
        Element divElement = cell.selectFirst("div");
        if (divElement != null) {
            return divElement.text().trim();
        }
        
        Element pElement = cell.selectFirst("p");
        if (pElement != null) {
            return pElement.text().trim();
        }
        
        // 如果没有div或p标签，直接获取单元格文本
        return cell.text().trim();
    }
    
    /**
     * 解析页面中的表格数据
     * @param document 页面文档
     * @return 表格数据列表
     */
    public List<Map<String, String>> parseTableData(Document document) {
        List<Map<String, String>> allTableData = new ArrayList<>();
        
        try {
            // 查找所有表格
            Elements tables = document.select("table.table");
            
            if (tables.isEmpty()) {
                System.out.println("页面中未找到表格");
                return allTableData;
            }
            
            System.out.println("找到 " + tables.size() + " 个表格");
            
            // 解析每个表格
            for (int i = 0; i < tables.size(); i++) {
                Element table = tables.get(i);
                System.out.println("开始解析第 " + (i + 1) + " 个表格");
                
                List<Map<String, String>> tableData = parseTable(table);
                allTableData.addAll(tableData);
            }
            
            System.out.println("所有表格解析完成，总共解析 " + allTableData.size() + " 行数据");
            
        } catch (Exception e) {
            System.err.println("解析表格数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allTableData;
    }
    
         /**
      * 测试表格解析功能
      * @param htmlContent HTML内容
      * @return 解析结果
      */
     public List<Map<String, String>> testTableParsing(String htmlContent) {
         try {
             Document document = Jsoup.parse(htmlContent);
             return parseTableData(document);
         } catch (Exception e) {
             System.err.println("测试表格解析时发生错误: " + e.getMessage());
             e.printStackTrace();
             return new ArrayList<>();
         }
     }
     
     /**
      * 测试时间解析功能
      * @param htmlContent HTML内容
      * @return 解析的时间结果
      */
     public String testTimeParsing(String htmlContent) {
         try {
             Document document = Jsoup.parse(htmlContent);
             Element parentContainer = document.selectFirst("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");
             if (parentContainer == null) {
                 parentContainer = document.body(); // 如果没有找到特定容器，使用body
             }
             return extractPublishTime(parentContainer);
         } catch (Exception e) {
             System.err.println("测试时间解析时发生错误: " + e.getMessage());
             e.printStackTrace();
             return "解析失败";
         }
     }
    
         /**
      * 将表格数据转换为CSV格式
      * @param tableData 表格数据
      * @return CSV格式的字符串
      */
     public String convertTableDataToCsv(List<Map<String, String>> tableData) {
         if (tableData.isEmpty()) {
             return "";
         }
         
         StringBuilder csv = new StringBuilder();
         
         // 添加表头
         csv.append("标准,对应产品,发布日期\n");
         
         // 添加数据行
         for (Map<String, String> row : tableData) {
             String standard = row.getOrDefault("标准", "").replace("\"", "\"\"");
             String product = row.getOrDefault("对应产品", "").replace("\"", "\"\"");
             String date = row.getOrDefault("发布日期", "").replace("\"", "\"\"");
             
             csv.append(String.format("\"%s\",\"%s\",\"%s\"\n", standard, product, date));
         }
         
         return csv.toString();
     }
     
     /**
      * 从爬取的内容中提取有用的信息
      * @param specificContent 爬取的原始内容
      * @return 提取的有用内容
      */
     private String extractUsefulContent(Map<String, Object> specificContent) {
         StringBuilder content = new StringBuilder();
         
         // 注释掉发布时间，不再添加到内容中
         // 提取发布时间
         // if (specificContent.containsKey("发布时间")) {
         //     String publishTime = specificContent.get("发布时间").toString();
         //     if (!"未找到时间信息".equals(publishTime)) {
         //         content.append("发布时间: ").append(publishTime).append("\n\n");
         //     }
         // }
         
         // 提取段落5的内容
         if (specificContent.containsKey("段落5")) {
             String paragraph5 = specificContent.get("段落5").toString();
             if (!"未找到第5个段落".equals(paragraph5) && !paragraph5.trim().isEmpty()) {
                 content.append(paragraph5).append("\n\n");
             }
         }
         
         // 提取后续段落的内容并连接成一个整体
         if (specificContent.containsKey("后续段落")) {
             @SuppressWarnings("unchecked")
             Map<String, String> subsequentParagraphs = (Map<String, String>) specificContent.get("后续段落");
             if (subsequentParagraphs != null && !subsequentParagraphs.isEmpty()) {
                 // 将所有后续段落连接成一个整体
                 StringBuilder subsequentContent = new StringBuilder();
                 for (Map.Entry<String, String> entry : subsequentParagraphs.entrySet()) {
                     String paragraphContent = entry.getValue();
                     if (paragraphContent != null && !paragraphContent.trim().isEmpty()) {
                         // 如果不是第一个段落，先添加换行符
                         if (subsequentContent.length() > 0) {
                             subsequentContent.append("\n");
                         }
                         subsequentContent.append(paragraphContent);
                     }
                 }
                 
                 // 将连接后的内容添加到主内容中
                 if (subsequentContent.length() > 0) {
                     content.append(subsequentContent.toString()).append("\n");
                 }
             }
         }
         
         // 提取表格数据
         if (specificContent.containsKey("表格数据")) {
             @SuppressWarnings("unchecked")
             List<Map<String, String>> tableData = (List<Map<String, String>>) specificContent.get("表格数据");
             if (tableData != null && !tableData.isEmpty()) {
                 content.append("相关标准信息:\n");
                 for (Map<String, String> row : tableData) {
                     String standard = row.get("标准");
                     String product = row.get("对应产品");
                     String date = row.get("发布日期");
                     
                     if (standard != null && !standard.trim().isEmpty()) {
                         content.append("标准: ").append(standard);
                         if (product != null && !product.trim().isEmpty()) {
                             content.append(", 产品: ").append(product);
                         }
                         if (date != null && !date.trim().isEmpty()) {
                             content.append(", 发布日期: ").append(date);
                         }
                         content.append("\n");
                     }
                 }
             }
         }
         
                  return content.toString().trim();
     }
     
         /**
     * 提取发布时间 - 支持多种格式
     * @param parentContainer 父容器元素
     * @return 提取的发布时间
     */
    private String extractPublishTime(Element parentContainer) {
        String rawDate = "未找到时间信息";
        
        // 1. 尝试查找包含"發布日期："的p标签
        Elements publishDateElements = parentContainer.select("p");
        for (Element element : publishDateElements) {
            String text = element.text().trim();
            if (text.contains("發布日期：")) {
                Matcher matcher = DATE_PATTERN_WITH_PREFIX.matcher(text);
                if (matcher.find()) {
                    rawDate = matcher.group(1);
                    break;
                }
                // 如果没有匹配到标准格式，返回整个文本
                rawDate = text;
                break;
            }
        }
        
        // 2. 如果还没找到，尝试查找blockquote > p格式
        if ("未找到时间信息".equals(rawDate)) {
            Element timeElement = parentContainer.selectFirst("blockquote > p");
            if (timeElement != null) {
                String timeText = timeElement.text().trim();
                Matcher matcher = DATE_PATTERN.matcher(timeText);
                if (matcher.find()) {
                    rawDate = matcher.group();
                } else {
                    rawDate = timeText;
                }
            }
        }
        
        // 3. 如果还没找到，尝试查找其他可能包含日期的元素
        if ("未找到时间信息".equals(rawDate)) {
            Elements allElements = parentContainer.select("*");
            for (Element element : allElements) {
                String text = element.text().trim();
                
                // 检查是否包含中文日期格式
                Matcher chineseMatcher = DATE_PATTERN.matcher(text);
                if (chineseMatcher.find()) {
                    rawDate = chineseMatcher.group();
                    break;
                }
                
                // 检查是否包含英文日期格式
                Matcher englishMatcher = DATE_PATTERN_ENGLISH.matcher(text);
                if (englishMatcher.find()) {
                    rawDate = englishMatcher.group();
                    break;
                }
                
                // 检查是否包含斜杠日期格式
                Matcher slashMatcher = DATE_PATTERN_SLASH.matcher(text);
                if (slashMatcher.find()) {
                    rawDate = slashMatcher.group();
                    break;
                }
            }
        }
        
        // 4. 使用DateFormatService统一日期格式
        String standardizedDate = dateFormatService.standardizeDate(rawDate);
        if (standardizedDate == null) {
            log.warn("UL爬虫extractPublishTime无法解析日期格式: {}", rawDate);
            return dateFormatService.getCurrentDateString();
        }
        
        return standardizedDate;
    }

    // ==================== 数据保存相关方法 ====================

    /**
     * 执行UL爬虫并保存到数据库
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeULCrawlerAndSave(int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            log.info("UL爬虫开始执行，计划爬取 {} 条数据", count);
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("UL Solutions");
            
            // 执行爬虫
            List<CrawlerResult> crawlerResults = crawl("", count);

            // 转换为CrawlerData实体
            List<CertNewsData> certNewsDataList = convertToCrawlerData(crawlerResults);

            // 检查是否全部重复
            if (certNewsDataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有爬取到任何数据");
                result.put("crawledCount", 0);
                result.put("savedCount", 0);
                result.put("duplicateCount", 0);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                log.info("UL爬虫执行完成，没有爬取到任何数据");
                return result;
            }

            // 获取去重统计信息
            Map<String, Object> duplicateStats = crawlerDataService.getDuplicateUrlStats(certNewsDataList);
            long duplicateCount = duplicateStats.get("duplicateCount") == null ? 0L : ((Number)duplicateStats.get("duplicateCount")).longValue();
            
            // 检查是否全部重复
            boolean allDuplicates = duplicateCount == certNewsDataList.size();
            if (allDuplicates) {
                result.put("success", true);
                result.put("message", "爬取的数据全部与数据库重复，停止爬取");
                result.put("crawledCount", certNewsDataList.size());
                result.put("savedCount", 0);
                result.put("duplicateCount", duplicateCount);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                log.info("UL爬虫执行完成，爬取 {} 条数据全部重复，停止爬取", certNewsDataList.size());
                return result;
            }
            
            // 使用安全的批量保存（自动去重），每30条数据一批
            List<CertNewsData> savedDataList = crawlerDataService.safeSaveCrawlerDataList(certNewsDataList, 30);
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("UL Solutions");
            long newDataCount = afterCount - beforeCount;
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CertNewsData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            log.info("UL爬虫执行完成，爬取 {} 条数据，新增 {} 条，重复 {} 条，耗时 {} ms", 
                crawlerResults.size(), newDataCount, duplicateCount, executionTime);
            
            // 构建返回结果
            result.put("success", true);
            result.put("requestedCount", count);
            result.put("crawledCount", crawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("duplicateStats", duplicateStats);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "UL爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("UL爬虫执行失败: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", "UL爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 执行UL爬虫并保存到数据库（带关键词搜索）
     * @param keyword 搜索关键词
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeULCrawlerWithKeywordAndSave(String keyword, int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            log.info("UL爬虫开始执行（关键词搜索），关键词: {}，计划爬取 {} 条数据", keyword, count);
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("UL Solutions");
            
            // 执行爬虫（带关键词）
            List<CrawlerResult> crawlerResults = crawl(keyword, count);

            // 转换为CrawlerData实体
            List<CertNewsData> certNewsDataList = convertToCrawlerData(crawlerResults);

            // 检查是否全部重复
            if (certNewsDataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有爬取到任何数据");
                result.put("crawledCount", 0);
                result.put("savedCount", 0);
                result.put("duplicateCount", 0);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                log.info("UL爬虫执行完成，关键词: {}，没有爬取到任何数据", keyword);
                return result;
            }

            // 获取去重统计信息
            Map<String, Object> duplicateStats = crawlerDataService.getDuplicateUrlStats(certNewsDataList);
            long duplicateCount = duplicateStats.get("duplicateCount") == null ? 0L : ((Number)duplicateStats.get("duplicateCount")).longValue();
            
            // 检查是否全部重复
            boolean allDuplicates = duplicateCount == certNewsDataList.size();
            if (allDuplicates) {
                result.put("success", true);
                result.put("message", "爬取的数据全部与数据库重复，停止爬取");
                result.put("crawledCount", certNewsDataList.size());
                result.put("savedCount", 0);
                result.put("duplicateCount", duplicateCount);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                log.info("UL爬虫执行完成，关键词: {}，爬取 {} 条数据全部重复，停止爬取", keyword, certNewsDataList.size());
                return result;
            }
            
            // 使用安全的批量保存（自动去重），每30条数据一批
            List<CertNewsData> savedDataList = crawlerDataService.safeSaveCrawlerDataList(certNewsDataList, 30);
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("UL Solutions");
            long newDataCount = afterCount - beforeCount;
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CertNewsData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            log.info("UL爬虫执行完成，关键词: {}，爬取 {} 条数据，新增 {} 条，重复 {} 条，耗时 {} ms", 
                keyword, crawlerResults.size(), newDataCount, duplicateCount, executionTime);
            
            // 构建返回结果
            result.put("success", true);
            result.put("keyword", keyword);
            result.put("requestedCount", count);
            result.put("crawledCount", crawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("duplicateStats", duplicateStats);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "UL爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("UL爬虫执行失败，关键词: {}，错误: {}", keyword, e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", "UL爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }

    /**
     * 将CrawlerResult转换为CrawlerData
     * @param crawlerResults 爬虫结果列表
     * @return CrawlerData列表
     */
    private List<CertNewsData> convertToCrawlerData(List<CrawlerResult> crawlerResults) {
        List<CertNewsData> certNewsDataList = new ArrayList<>();
        
        for (CrawlerResult result : crawlerResults) {
            // 生成摘要（取内容的前200个字符，避免过长）
            String content = result.getContent();
            String summary;
            if (content != null && !content.trim().isEmpty()) {
                summary = content.trim();
                if (summary.length() > 200) {
                    // 确保在字符边界截断，避免截断UTF-8字符
                    summary = summary.substring(0, 200);
                    // 找到最后一个完整的字符边界
                    while (summary.length() > 0 && !Character.isLetterOrDigit(summary.charAt(summary.length() - 1))) {
                        summary = summary.substring(0, summary.length() - 1);
                    }
                    summary = summary + "...";
                }
            } else {
                summary = "无摘要内容";
            }
            
            CertNewsData certNewsData = new CertNewsData()
                .setId(generateSequentialId())
                .setSourceName("UL Solutions")
                .setTitle(result.getTitle())
                .setUrl(result.getUrl())
                .setSummary(summary)
                .setCountry(result.getCountry())
                .setType(result.getType())
                .setContent(content)
                .setCrawlTime(LocalDateTime.now())
                .setStatus(CertNewsData.DataStatus.NEW)
                .setIsProcessed(false)
                .setRiskLevel(CertNewsData.RiskLevel.MEDIUM); // 设置风险等级为MEDIUM
            
            certNewsDataList.add(certNewsData);
        }
        
        return certNewsDataList;
    }

}


