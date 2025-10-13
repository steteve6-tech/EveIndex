package com.certification.crawler.countrydata.eu;

import com.certification.config.MedcertCrawlerConfig;
import com.certification.crawler.common.CsvExporter;
import com.certification.entity.common.GuidanceDocument;
import com.certification.entity.common.CertNewsData.RiskLevel;
import com.certification.exception.AllDataDuplicateException;
import com.certification.repository.common.GuidanceDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧盟医疗设备最新更新新闻爬虫
 * 爬取 https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en 页面内容
 * 支持批次保存到数据库，连续3个批次完全重复则停止爬取
 */
@Slf4j
@Component
public class Eu_guidance {
    
    private static final String BASE_URL = "https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    private final CsvExporter csvExporter;
    
    @Autowired
    private GuidanceDocumentRepository guidanceDocumentRepository;
    
    @Autowired
    private MedcertCrawlerConfig crawlerConfig;
    
    public Eu_guidance() {
        this.csvExporter = new CsvExporter();
    }
    
    /**
     * 爬取欧盟医疗设备最新更新新闻（支持全量爬取）
     * @param maxPages 最大爬取页数，0表示爬取所有页
     * @param maxRecords 最大记录数，-1表示爬取所有记录
     * @param batchSize 批次大小
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlMedicalDeviceNews(int maxPages, int maxRecords, int batchSize) {
        List<Map<String, String>> allNews = new ArrayList<>();
        boolean crawlAll = (maxPages == 0);
        int actualBatchSize = Math.min(batchSize, crawlerConfig.getCrawl().getApiLimits().getEuGuidanceMaxPerPage());
        
        log.info("🚀 开始爬取欧盟医疗设备最新更新新闻...");
        log.info("📊 最大页数: {}，最大记录数: {}，批次大小: {}", 
                maxPages == 0 ? "所有页" : maxPages, 
                maxRecords == -1 ? "所有记录" : maxRecords, 
                actualBatchSize);
        
        try {
            int page = 0;
            while (crawlAll || page < maxPages) {
                try {
                    long pageStartTime = System.currentTimeMillis();
                    
                    // 构建分页URL
                    String pageUrl = buildPageUrl(page);
                    System.out.println("正在爬取第" + (page + 1) + "页: " + pageUrl);
                    
                    // 使用Jsoup获取页面内容
                    Document doc = Jsoup.connect(pageUrl)
                            .userAgent(USER_AGENT)
                            .timeout(30000)
                            .get();
                    
                    long pageEndTime = System.currentTimeMillis();
                    System.out.println("第" + (page + 1) + "页页面加载完成，耗时: " + (pageEndTime - pageStartTime) + " 毫秒");
                    
                    // 解析新闻内容
                    List<Map<String, String>> pageNews = parseNewsContent(doc);
                    if (pageNews.isEmpty()) {
                        log.info("第{}页没有找到新闻数据，停止爬取", page + 1);
                        break;
                    }
                    
                    allNews.addAll(pageNews);
                    log.info("第{}页解析完成，获取到 {} 条新闻", page + 1, pageNews.size());
                    
                    // 检查是否达到最大记录数限制
                    if (maxRecords > 0 && allNews.size() >= maxRecords) {
                        log.info("已达到最大记录数限制: {}", maxRecords);
                        break;
                    }
                    
                    // 添加延迟避免请求过快
                    Thread.sleep(crawlerConfig.getRetry().getDelayMilliseconds() / 5);
                    
                } catch (Exception e) {
                    log.error("爬取第{}页时出错: {}", page + 1, e.getMessage());
                    break;
                }
                
                page++; // 增加页码
            }
            
        } catch (Exception e) {
            log.error("爬取过程中发生错误: {}", e.getMessage(), e);
        }
        
        return allNews;
    }
    
    /**
     * 向后兼容的方法
     * @param maxPages 最大爬取页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlMedicalDeviceNews(int maxPages) {
        return crawlMedicalDeviceNews(maxPages, -1, crawlerConfig.getBatch().getSmallSaveSize());
    }
    
    /**
     * 构建分页URL
     */
    private String buildPageUrl(int page) {
        if (page == 0) {
            return BASE_URL;
        } else {
            return BASE_URL + "?page=" + page;
        }
    }
    
    /**
     * 解析新闻内容
     */
    private List<Map<String, String>> parseNewsContent(Document doc) {
        List<Map<String, String>> newsList = new ArrayList<>();
        
        try {
            System.out.println("🔍 开始解析新闻内容...");
            
            // 查找所有新闻文章
            Elements articles = doc.select("article.ecl-content-item");
            System.out.println("📊 找到 " + articles.size() + " 个新闻文章");
            
            for (int i = 0; i < articles.size(); i++) {
                Element article = articles.get(i);
                Map<String, String> news = new HashMap<>();
                
                try {
                    // 解析新闻类型
                    Element newsTypeElement = article.selectFirst(".ecl-content-block__primary-meta-item");
                    String newsType = newsTypeElement != null ? newsTypeElement.text().trim() : "";
                    news.put("news_type", newsType);
                    
                    // 解析发布日期
                    Element dateElement = article.selectFirst("time[datetime]");
                    String publishDate = "";
                    if (dateElement != null) {
                        publishDate = dateElement.attr("datetime");
                        if (publishDate.isEmpty()) {
                            publishDate = dateElement.text().trim();
                        }
                    }
                    news.put("publish_date", publishDate);
                    
                    // 解析标题
                    Element titleElement = article.selectFirst(".ecl-content-block__title a");
                    String title = "";
                    String detailUrl = "";
                    if (titleElement != null) {
                        title = titleElement.text().trim();
                        detailUrl = titleElement.attr("href");
                        // 构建完整URL
                        if (!detailUrl.startsWith("http")) {
                            detailUrl = "https://health.ec.europa.eu" + detailUrl;
                        }
                    }
                    news.put("title", title);
                    news.put("detail_url", detailUrl);
                    
                    // 解析描述
                    Element descriptionElement = article.selectFirst(".ecl-content-block__description p");
                    String description = descriptionElement != null ? descriptionElement.text().trim() : "";
                    news.put("description", description);
                    
                    // 解析阅读时间
                    Element readTimeElement = article.selectFirst(".ecl-content-block__secondary-meta-label");
                    String readTime = readTimeElement != null ? readTimeElement.text().trim() : "";
                    news.put("read_time", readTime);
                    
                    // 解析图片URL
                    Element imageElement = article.selectFirst(".ecl-content-item__image");
                    String imageUrl = imageElement != null ? imageElement.attr("src") : "";
                    news.put("image_url", imageUrl);
                    
                    // 解析图片alt文本
                    String imageAlt = imageElement != null ? imageElement.attr("alt") : "";
                    news.put("image_alt", imageAlt);
                    
                    // 添加爬取时间
                    news.put("crawl_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    // 添加序号
                    news.put("article_index", String.valueOf(i + 1));
                    
                    newsList.add(news);
                    System.out.println("✅ 新闻 " + (i + 1) + " 解析成功: " + title);
                    
                } catch (Exception e) {
                    System.err.println("❌ 解析第" + (i + 1) + "个新闻时出错: " + e.getMessage());
                }
            }
            
            System.out.println("✅ 成功解析了 " + newsList.size() + " 条新闻");
            
        } catch (Exception e) {
            System.err.println("解析新闻内容时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return newsList;
    }
    
    /**
     * 保存数据到CSV文件
     */
    public void saveToCsv(List<Map<String, String>> newsList, String filePath) {
        try {
            if (newsList.isEmpty()) {
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
            
            // 定义CSV表头
            String[] headers = {
                "article_index", "news_type", "publish_date", "title", "description", 
                "read_time", "image_url", "image_alt", "detail_url", "crawl_time"
            };
            
            // 准备数据
            List<String[]> csvData = new ArrayList<>();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            for (Map<String, String> news : newsList) {
                String[] row = new String[headers.length];
                for (int i = 0; i < headers.length - 1; i++) {
                    row[i] = news.getOrDefault(headers[i], "");
                }
                row[headers.length - 1] = currentTime; // 添加爬取时间
                csvData.add(row);
            }
            
            // 导出到CSV
            csvExporter.exportSimpleToCsv(csvData, headers, finalFilePath);
            System.out.println("✅ 数据已保存到: " + finalFilePath);
            System.out.println("📊 总共保存了 " + newsList.size() + " 条新闻数据");
            System.out.println("📁 文件路径: " + new File(finalFilePath).getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ 保存CSV文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 带搜索关键词的爬取方法
     * @param searchKeyword 搜索关键词
     * @param maxPages 最大页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> searchNews(String searchKeyword, int maxPages) {
        System.out.println("开始搜索新闻...");
        System.out.println("搜索关键词: " + searchKeyword);
        System.out.println("最大页数: " + maxPages);
        
        List<Map<String, String>> allNews = crawlMedicalDeviceNews(maxPages);
        
        // 如果有关键词，进行筛选
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            List<Map<String, String>> filteredNews = new ArrayList<>();
            String keyword = searchKeyword.toLowerCase();
            
            for (Map<String, String> news : allNews) {
                String title = news.getOrDefault("title", "").toLowerCase();
                String description = news.getOrDefault("description", "").toLowerCase();
                
                if (title.contains(keyword) || description.contains(keyword)) {
                    filteredNews.add(news);
                }
            }
            
            System.out.println("🔍 关键词筛选结果: " + filteredNews.size() + " 条新闻");
            allNews = filteredNews;
        }
        
        if (!allNews.isEmpty()) {
            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "EU_MedicalDevice_News_" + 
                (searchKeyword != null && !searchKeyword.trim().isEmpty() ? searchKeyword + "_" : "") + 
                timestamp + ".csv";
            
            // 保存到CSV文件
            saveToCsv(allNews, fileName);
            
            System.out.println("✅ 搜索完成，找到 " + allNews.size() + " 条新闻数据");
            
            // 显示前几条数据示例
            System.out.println("\n数据示例:");
            for (int i = 0; i < Math.min(3, allNews.size()); i++) {
                Map<String, String> news = allNews.get(i);
                System.out.println("新闻 " + (i + 1) + ":");
                news.forEach((key, value) -> {
                    if (!value.isEmpty()) {
                        System.out.println("  " + key + ": " + value);
                    }
                });
                System.out.println();
            }
        } else {
            System.out.println("❌ 没有找到相关新闻数据");
        }
        
        return allNews;
    }
    
    /**
     * 爬取并保存到数据库（支持批次保存和重复检测）
     * @param maxPages 最大爬取页数
     * @return 保存到数据库的记录数量
     */
    @Transactional
    public int crawlAndSaveToDatabase(int maxPages) {
        System.out.println("🚀 开始爬取EU医疗设备新闻并保存到数据库...");
        System.out.println("📊 批次大小: " + crawlerConfig.getBatch().getSmallSaveSize() + "，最大连续重复批次: 3");
        
        int totalSaved = 0;
        int consecutiveDuplicateBatches = 0;
        List<Map<String, String>> currentBatch = new ArrayList<>();
        
        try {
            for (int page = 0; page < maxPages; page++) {
                try {
                    long pageStartTime = System.currentTimeMillis();
                    
                    // 构建分页URL
                    String pageUrl = buildPageUrl(page);
                    System.out.println("📄 正在爬取第" + (page + 1) + "页: " + pageUrl);
                    
                    // 使用Jsoup获取页面内容
                    Document doc = Jsoup.connect(pageUrl)
                            .userAgent(USER_AGENT)
                            .timeout(30000)
                            .get();
                    
                    long pageEndTime = System.currentTimeMillis();
                    System.out.println("⏱️ 第" + (page + 1) + "页页面加载完成，耗时: " + (pageEndTime - pageStartTime) + " 毫秒");
                    
                    // 解析新闻内容
                    List<Map<String, String>> pageNews = parseNewsContent(doc);
                    if (pageNews.isEmpty()) {
                        System.out.println("⚠️ 第" + (page + 1) + "页没有找到新闻数据，停止爬取");
                        break;
                    }
                    
                    // 添加到当前批次
                    currentBatch.addAll(pageNews);
                    System.out.println("📝 第" + (page + 1) + "页解析完成，获取到 " + pageNews.size() + " 条新闻");
                    
                    // 检查是否需要保存批次
                    if (currentBatch.size() >= crawlerConfig.getBatch().getSmallSaveSize()) {
                        int savedInBatch = saveBatchToDatabase(currentBatch);
                        totalSaved += savedInBatch;
                        
                        if (savedInBatch == 0) {
                            consecutiveDuplicateBatches++;
                            System.out.println("🔄 批次完全重复，连续重复批次数: " + consecutiveDuplicateBatches);
                            
                            if (consecutiveDuplicateBatches >= 3) {
                                System.out.println("🛑 连续 3 个批次完全重复，停止爬取");
                                break;
                            }
                        } else {
                            consecutiveDuplicateBatches = 0; // 重置计数器
                            System.out.println("✅ 批次保存成功，保存了 " + savedInBatch + " 条新记录");
                        }
                        
                        currentBatch.clear(); // 清空当前批次
                    }
                    
                    // 添加延迟避免请求过快
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    System.err.println("❌ 爬取第" + (page + 1) + "页时出错: " + e.getMessage());
                    break;
                }
            }
            
            // 处理剩余的批次数据
            if (!currentBatch.isEmpty()) {
                int savedInBatch = saveBatchToDatabase(currentBatch);
                totalSaved += savedInBatch;
                System.out.println("✅ 最后批次保存完成，保存了 " + savedInBatch + " 条记录");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🎉 爬取完成！总共保存了 " + totalSaved + " 条新记录到数据库");
        return totalSaved;
    }
    
    /**
     * 保存批次数据到数据库
     * @param batchData 批次数据
     * @return 实际保存的记录数量
     */
    @Transactional
    private int saveBatchToDatabase(List<Map<String, String>> batchData) {
        if (batchData == null || batchData.isEmpty()) {
            return 0;
        }
        
        int savedCount = 0;
        List<GuidanceDocument> documentsToSave = new ArrayList<>();
        
        try {
            for (Map<String, String> newsData : batchData) {
                // 检查是否已存在（基于标题和发布日期）
                String title = newsData.get("title");
                String publishDate = newsData.get("publish_date");
                
                if (title == null || title.trim().isEmpty()) {
                    continue; // 跳过无效数据
                }
                
                // 检查数据库中是否已存在相同标题和发布日期的记录
                boolean exists = checkIfDocumentExists(title, publishDate);
                if (exists) {
                    System.out.println("⏭️ 跳过重复记录: " + title);
                    continue;
                }
                
                // 创建GuidanceDocument实体
                GuidanceDocument document = createGuidanceDocumentFromNews(newsData);
                if (document != null) {
                    documentsToSave.add(document);
                    savedCount++;
                }
            }
            
            // 批量保存到数据库
            if (!documentsToSave.isEmpty()) {
                guidanceDocumentRepository.saveAll(documentsToSave);
                System.out.println("💾 批次保存完成: " + savedCount + " 条新记录");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 保存批次数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedCount;
    }
    
    /**
     * 检查文档是否已存在
     * @param title 标题
     * @param publishDate 发布日期
     * @return 是否存在
     */
    private boolean checkIfDocumentExists(String title, String publishDate) {
        try {
            // 根据标题查找
            List<GuidanceDocument> existingDocs = guidanceDocumentRepository.findByTitleContaining(title);
            
            if (existingDocs.isEmpty()) {
                return false;
            }
            
            // 如果提供了发布日期，进一步检查
            if (publishDate != null && !publishDate.trim().isEmpty()) {
                for (GuidanceDocument doc : existingDocs) {
                    if (doc.getTitle().equals(title) && 
                        doc.getPublicationDate() != null && 
                        doc.getPublicationDate().toString().equals(publishDate)) {
                        return true;
                    }
                }
            } else {
                // 只检查标题
                for (GuidanceDocument doc : existingDocs) {
                    if (doc.getTitle().equals(title)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("❌ 检查文档是否存在时出错: " + e.getMessage());
            return false; // 出错时假设不存在，继续保存
        }
    }
    
    /**
     * 从新闻数据创建GuidanceDocument实体
     * @param newsData 新闻数据
     * @return GuidanceDocument实体
     */
    private GuidanceDocument createGuidanceDocumentFromNews(Map<String, String> newsData) {
        try {
            GuidanceDocument document = new GuidanceDocument();
            
            // 设置文档类型
            document.setDocumentType("NEWS");
            
            // 核心字段映射
            document.setTitle(getStringValue(newsData, "title"));
            document.setPublicationDate(parseDate(getStringValue(newsData, "publish_date")));
            document.setDocumentUrl(getStringValue(newsData, "detail_url"));
            document.setSourceUrl("https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en");
            document.setDataSource("EU");
            document.setJdCountry("EU");
            
            // EU新闻特有字段
            document.setNewsType(getStringValue(newsData, "news_type"));
            document.setDescription(getStringValue(newsData, "description"));
            document.setReadTime(getStringValue(newsData, "read_time"));
            document.setImageUrl(getStringValue(newsData, "image_url"));
            document.setImageAlt(getStringValue(newsData, "image_alt"));
            document.setArticleIndex(parseInteger(getStringValue(newsData, "article_index")));
            
            // 设置默认值
            document.setRiskLevel(RiskLevel.MEDIUM); // 默认中等风险
            document.setKeywords(""); // 默认为空
            
            // 设置爬取时间
            document.setCrawlTime(LocalDateTime.now());
            
            return document;
        } catch (Exception e) {
            System.err.println("❌ 创建GuidanceDocument实体时出错: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 工具方法：安全获取字符串值
     */
    private String getStringValue(Map<String, String> map, String key) {
        if (map == null || key == null) return null;
        String value = map.get(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
    
    /**
     * 工具方法：解析日期
     */
    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        String[] patterns = {"yyyy-MM-dd", "yyyyMMdd", "MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd'T'HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                return java.time.LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignore) {}
        }
        return null;
    }
    
    /**
     * 工具方法：解析整数
     */
    private Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 主函数用于测试
     */
    public static void main(String[] args) {
        Eu_guidance crawler = new Eu_guidance();
        
        // 测试爬取所有新闻
        System.out.println("=== 测试爬取欧盟医疗设备最新更新新闻 ===");
        List<Map<String, String>> allNews = crawler.crawlMedicalDeviceNews(2);
        
        if (!allNews.isEmpty()) {
            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "EU_MedicalDevice_News_All_" + timestamp + ".csv";
            
            // 保存到CSV文件
            crawler.saveToCsv(allNews, fileName);
            
            System.out.println("✅ 爬取完成，找到 " + allNews.size() + " 条新闻数据");
        } else {
            System.out.println("❌ 没有找到任何新闻数据");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }

    /**
     * 基于关键词列表爬取EU医疗设备新闻数据
     * 注意：EU指导文档爬虫不支持关键词搜索，此方法会忽略关键词参数
     * 仅用于适配统一的爬虫接口
     * 
     * @param inputKeywords 关键词列表（此爬虫不使用）
     * @param maxRecords 最大记录数（-1表示全部，实际按页数计算）
     * @param batchSize 批次大小（未使用，由配置决定）
     * @param dateFrom 开始日期（未使用）
     * @param dateTo 结束日期（未使用）
     * @return 爬取结果描述
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        System.out.println("开始爬取EU医疗设备新闻数据...");
        System.out.println("注意：此爬虫不支持关键词搜索，将爬取列表页面数据");
        
        try {
            // 计算页数（假设每页15-20条数据）
            int maxPages = maxRecords == -1 ? 10 : Math.max(1, maxRecords / 15);
            System.out.println("最大爬取页数: " + maxPages);
            
            // 调用主爬取方法
            int savedCount = crawlAndSaveToDatabase(maxPages);
            
            // 返回成功消息
            String result = String.format("EU医疗设备新闻爬取完成，保存记录数: %d", savedCount);
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            String errorMsg = "EU医疗设备新闻爬取失败: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 基于关键词列表爬取EU医疗设备新闻数据（简化版本，无时间范围）
     */
    public String crawlAndSaveWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize) {
        return crawlAndSaveWithKeywords(inputKeywords, maxRecords, batchSize, null, null);
    }
}