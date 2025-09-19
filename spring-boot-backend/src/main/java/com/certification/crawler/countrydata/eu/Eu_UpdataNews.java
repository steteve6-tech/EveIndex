package com.certification.crawler.countrydata.eu;

import com.certification.crawler.common.CsvExporter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 欧盟医疗设备最新更新新闻爬虫
 * 爬取 https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en 页面内容
 */
public class Eu_UpdataNews {
    
    private static final String BASE_URL = "https://health.ec.europa.eu/medical-devices-topics-interest/latest-updates_en";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
    
    private final CsvExporter csvExporter;
    
    public Eu_UpdataNews() {
        this.csvExporter = new CsvExporter();
    }
    
    /**
     * 爬取欧盟医疗设备最新更新新闻
     * @param maxPages 最大爬取页数
     * @return 爬取结果列表
     */
    public List<Map<String, String>> crawlMedicalDeviceNews(int maxPages) {
        List<Map<String, String>> allNews = new ArrayList<>();
        
        try {
            for (int page = 0; page < maxPages; page++) {
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
                        System.out.println("第" + (page + 1) + "页没有找到新闻数据，停止爬取");
                        break;
                    }
                    
                    allNews.addAll(pageNews);
                    System.out.println("第" + (page + 1) + "页解析完成，获取到 " + pageNews.size() + " 条新闻");
                    
                    // 添加延迟避免请求过快
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    System.err.println("爬取第" + (page + 1) + "页时出错: " + e.getMessage());
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allNews;
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
     * 主函数用于测试
     */
    public static void main(String[] args) {
        Eu_UpdataNews crawler = new Eu_UpdataNews();
        
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
}