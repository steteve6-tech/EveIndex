package com.certification.crawler.certification;

import com.certification.crawler.certification.base.BaseCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.entity.common.CrawlerData;
import com.certification.service.DateFormatService;
import com.certification.service.SystemLogService;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 北测新闻爬虫
 * 爬取北测官网新闻资讯数据并保存到数据库
 */
@Slf4j
@Component
@Transactional
public class BeiceCrawler implements BaseCrawler {
    
    @Autowired
    private CrawlerDataService crawlerDataService;
    
    @Autowired
    private SystemLogService systemLogService;
    
    @Autowired
    private DateFormatService dateFormatService;
    
    // 基础URL
    private static final String BASE_URL = "https://www.ntek.org.cn";
    private static final String NEWS_URL = "https://www.ntek.org.cn/xinwenzixun.html";
    
    // HTTP客户端
    private final HttpClient httpClient;
    
    // 爬虫配置
    private final CrawlerConfig config;

    // 分页相关常量
    private static final int ITEMS_PER_PAGE = 20; // 每页20项新闻
    private static final int MAX_PAGES = 125; // 最大爬取页数，可根据需要调整
    
    public BeiceCrawler() {
        // 初始化配置
        this.config = new CrawlerConfig();
        this.config.setBaseUrl(BASE_URL);
        this.config.setTimeout(30000);
        this.config.setRetryCount(3);
        this.config.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        
        HttpClient tempHttpClient;
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // 创建SSL上下文
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            tempHttpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            log.warn("创建SSL上下文失败，使用默认HttpClient: {}", e.getMessage());
            tempHttpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
        }
        this.httpClient = tempHttpClient;
    }
    

    /**
     * 构建分页URL
     */
    private String buildPageUrl(int page) {
        if (page == 1) {
            return NEWS_URL; // 第一页使用原始URL
        } else {
            // 根据北测网站的分页规则构建URL
            // 例如: /xinwenzixun-4-2.html (第2页)
            return BASE_URL + "/xinwenzixun-4-" + page + ".html";
        }
    }

    /**
     * 移除不需要的元素
     */
    private void removeUnwantedElements(Document document) {
        try {
            // 移除指定的不需要的元素
            Elements unwantedElements = document.select("body > div.rack.fl.wh.NewsListBox > div > div.lefter.fl > div.tab > div");

            for (Element element : unwantedElements) {
                element.remove();
                log.debug("已移除不需要的元素: {}", element.tagName());
            }

            // 也可以移除其他可能干扰的元素
            Elements otherUnwanted = document.select(".tab, .navigation, .sidebar, .advertisement, .ads");
            for (Element element : otherUnwanted) {
                element.remove();
                log.debug("已移除其他不需要的元素: {}", element.className());
            }

                log.info("已移除 {} 个不需要的元素", unwantedElements.size() + otherUnwanted.size());

        } catch (Exception e) {
            log.warn("移除不需要的元素时出错: {}", e.getMessage());
        }
    }

    /**
     * 移除内容中不需要的元素
     */
    private void removeUnwantedContentElements(Element contentElement) {
        try {
            // 移除在线咨询和获取报价等不需要的元素
            Elements unwantedElements = contentElement.select(".online, .consult, .offer, .btn-offer, .advertisement, .ads, .sidebar, .navigation");

            for (Element element : unwantedElements) {
                element.remove();
                log.debug("已移除内容中的不需要元素: {}", element.className());
            }

            // 移除包含特定文本的元素
            Elements allElements = contentElement.select("*");
            for (Element element : allElements) {
                String text = element.text().trim();
                if (text.contains("立即咨询") || text.contains("获取报价") || text.contains("在线咨询")) {
                    element.remove();
                    log.debug("已移除包含咨询文本的元素: {}", text);
                }
            }

            log.debug("已移除 {} 个不需要的内容元素", unwantedElements.size());

        } catch (Exception e) {
            log.warn("移除内容中不需要的元素时出错: {}", e.getMessage());
        }
    }

    /**
     * 格式化日期字符串
     * 将 "14 2025-08" 格式转换为 "2025-08-14" 格式
     */
    private String formatDateString(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return "";
        }

        try {
            // 处理 "14 2025-08" 格式
            if (dateText.matches("\\d{1,2}\\s+\\d{4}-\\d{1,2}")) {
                String[] parts = dateText.trim().split("\\s+");
                if (parts.length == 2) {
                    String day = parts[0];
                    String yearMonth = parts[1];

                    // 确保日期是两位数
                    if (day.length() == 1) {
                        day = "0" + day;
                    }

                    // 确保月份是两位数
                    String[] yearMonthParts = yearMonth.split("-");
                    if (yearMonthParts.length == 2) {
                        String year = yearMonthParts[0];
                        String month = yearMonthParts[1];
                        if (month.length() == 1) {
                            month = "0" + month;
                        }

                        return year + "-" + month + "-" + day;
                    }
                }
            }

            // 处理其他可能的日期格式
            // 如果已经是标准格式，直接返回
            if (dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateText;
            }

            // 处理 "2025-08-21" 格式（如果已经是标准格式）
            if (dateText.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                String[] parts = dateText.split("-");
                if (parts.length == 3) {
                    String year = parts[0];
                    String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                    String day = parts[2].length() == 1 ? "0" + parts[2] : parts[2];
                    return year + "-" + month + "-" + day;
                }
            }

            log.debug("无法解析日期格式: {}", dateText);
            return dateText; // 如果无法解析，返回原始文本

        } catch (Exception e) {
            log.warn("格式化日期时出错: {}, 原始文本: {}", e.getMessage(), dateText);
            return dateText;
        }
    }

    /**
     * 从页面解析新闻列表 - 支持分页
     */
    private List<NewsItem> parseNewsListFromPage(Document document, int pageNumber) {
        List<NewsItem> newsItems = new ArrayList<>();

        try {
            log.info("开始解析第 {} 页的新闻列表...", pageNumber);

            // 在解析前移除不需要的元素
            removeUnwantedElements(document);

            // 查找新闻列表容器 - 使用更通用的选择器
            Elements newsElements = document.select(".list-down li, .list li, .news-list li");

            if (newsElements.isEmpty()) {
                log.warn("第 {} 页未找到新闻列表元素，尝试备用选择器", pageNumber);
                // 备用选择器
                newsElements = document.select("li.effect-oneBox, .news-item, .article-item");
            }

            log.info("第 {} 页找到 {} 个新闻元素", pageNumber, newsElements.size());

            for (int i = 0; i < newsElements.size(); i++) {
                Element element = newsElements.get(i);
                try {
                    NewsItem item = new NewsItem();

                    // 获取标题
                    Element titleElement = element.selectFirst("h4, h3, .title, a");
                    if (titleElement != null) {
                        item.setTitle(titleElement.text().trim());
                    }

                    // 获取链接
                    Element linkElement = element.selectFirst("a");
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        if (href.startsWith("/")) {
                            href = BASE_URL + href;
                        }
                        item.setUrl(href);
                    }

                    // 获取发布时间
                    Element timeElement = element.selectFirst(".time, .date, .publish-time");
                    if (timeElement != null) {
                        String timeText = timeElement.text().trim();
                        // 格式化日期字符串
                        String formattedDate = formatDateString(timeText);
                        item.setPublishTime(formattedDate);
                        log.debug("原始日期: {} -> 格式化后: {}", timeText, formattedDate);
                    }

                    // 获取摘要
                    Element summaryElement = element.selectFirst("p.txt, .summary, .desc, .content");
                    if (summaryElement != null) {
                        item.setSummary(summaryElement.text().trim());
                    }

                    // 只添加有效的新闻项
                    if (item.getTitle() != null && !item.getTitle().isEmpty() &&
                            item.getUrl() != null && !item.getUrl().isEmpty()) {
                        newsItems.add(item);
                        log.debug("第 {} 页第 {} 条新闻: {}", pageNumber, i + 1, item.getTitle());
                    }

                } catch (Exception e) {
                    log.warn("解析第 {} 页第 {} 条新闻失败", pageNumber, i + 1, e);
                }
            }
            
            log.info("第 {} 页解析完成，有效新闻数: {}", pageNumber, newsItems.size());
            
        } catch (Exception e) {
            log.error("解析第 {} 页新闻列表失败", pageNumber, e);
        }

        return newsItems;
    }
    
    /**
     * 获取页面内容
     */
    private Document fetchPage(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                    .header("Sec-Ch-Ua-Mobile", "?0")
                    .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return Jsoup.parse(response.body());
            } else {
                log.error("HTTP请求失败，状态码: {}, URL: {}", response.statusCode(), url);
                return null;
            }
            
        } catch (Exception e) {
            log.error("获取页面失败: {}", url, e);
            return null;
        }
    }
    
    /**
     * 解析新闻列表 - 保留原有方法用于兼容性
     */
    private List<NewsItem> parseNewsList(Document document) {
        return parseNewsListFromPage(document, 1);
    }
    
    /**
     * 获取新闻详情
     */
    private void fetchNewsDetail(NewsItem item) {
        if (item.getUrl() == null || item.getUrl().isEmpty()) {
            return;
        }
        
        try {
            log.debug("正在获取新闻详情: {}", item.getUrl());
            
            // 使用专门的详情页请求方法
            Document detailPage = fetchDetailPage(item.getUrl());
            if (detailPage == null) {
                log.warn("无法获取详情页: {}", item.getUrl());
                return;
            }
            
            // 在解析详情前移除不需要的元素
            removeUnwantedElements(detailPage);

            // 获取详细内容 - 使用指定的选择器
            Element contentElement = detailPage.selectFirst("body > div.rack.fl.wh.NewsDetailBox > div > div.righter.leftBox.fl > div.border > div.detailCont.wor");
            if (contentElement != null) {
                // 在获取内容前，先移除不需要的元素
                removeUnwantedContentElements(contentElement);

                // 获取纯文本内容
                String content = contentElement.text().trim();
                item.setContent(content);
                log.debug("获取到内容长度: {}", content.length());
                log.debug("内容预览: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);
            } else {
                log.warn("未找到指定的内容元素: {}", item.getUrl());
                // 如果找不到指定元素，尝试备用选择器
                Element fallbackElement = detailPage.selectFirst(".content, .article-content, .news-content, .main-content, .detail-content, .post-content, .article-body");
                if (fallbackElement != null) {
                    removeUnwantedContentElements(fallbackElement);
                    String content = fallbackElement.text().trim();
                    item.setContent(content);
                    log.debug("使用备用选择器获取内容，长度: {}", content.length());
                } else {
                    log.warn("未找到任何内容元素: {}", item.getUrl());
                }
            }
            
            // 获取发布时间 - 详情页可能有更准确的时间
            Element timeElement = detailPage.selectFirst(".publish-time, .post-time, .article-time, .date, .time, .created-time");
            if (timeElement != null && (item.getPublishTime() == null || item.getPublishTime().isEmpty())) {
                String timeText = timeElement.text().trim();
                // 格式化日期字符串
                String formattedDate = formatDateString(timeText);
                item.setPublishTime(formattedDate);
                log.debug("详情页原始日期: {} -> 格式化后: {}", timeText, formattedDate);
            }
            
            // 获取摘要 - 详情页可能有更完整的摘要
            if (item.getSummary() == null || item.getSummary().isEmpty()) {
                Element summaryElement = detailPage.selectFirst(".summary, .excerpt, .description, .post-excerpt, .article-summary");
                if (summaryElement != null) {
                    item.setSummary(summaryElement.text().trim());
                }
            }
            
            log.debug("成功获取新闻详情: {} - 内容长度: {}", item.getTitle(), item.getContent() != null ? item.getContent().length() : 0);
            
        } catch (Exception e) {
            log.warn("获取新闻详情失败: {}, 错误: {}", item.getUrl(), e.getMessage());
        }
    }
    
    /**
     * 获取详情页面内容 - 专门用于详情页请求
     */
    private Document fetchDetailPage(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Priority", "u=0, i")
                    .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                    .header("Sec-Ch-Ua-Mobile", "?0")
                    .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Referer", "https://www.ntek.org.cn/xinwenzixun.html")
                    .header("Referrer-Policy", "strict-origin-when-cross-origin")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return Jsoup.parse(response.body());
            } else {
                log.error("详情页HTTP请求失败，状态码: {}, URL: {}", response.statusCode(), url);
                return null;
            }
            
        } catch (Exception e) {
            log.error("获取详情页失败: {}", url, e);
            return null;
        }
    }
    
    
    /**
     * 新闻项数据类
     */
    public static class NewsItem {
        private String title;
        private String url;
        private String publishTime;
        private String summary;
        private String content;
        private String author;
        private String category;
        private String images;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getPublishTime() { return publishTime; }
        public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getImages() { return images; }
        public void setImages(String images) { this.images = images; }
    }
    
    /**
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        北测新闻爬虫测试程序");
        System.out.println("==========================================");
        
        BeiceCrawler crawler = new BeiceCrawler();
        
        try {
            // 默认执行爬取测试
            if (args.length == 0) {
                System.out.println("🚀 开始执行爬取测试...");
                System.out.println("📰 目标网站: " + NEWS_URL);
                System.out.println("📄 每页新闻数: " + ITEMS_PER_PAGE);
                System.out.println("📚 最大页数: " + MAX_PAGES);
                System.out.println();
                
                System.out.println("🚀 模式: 爬取测试（列表+详情）");
                List<CrawlerResult> results = crawler.crawlLatest(5);
                
                System.out.println();
                System.out.println("✅ 爬取完成！共获取 " + results.size() + " 条数据");
//            } else {
//                String command = args[0].toLowerCase();
//
//                switch (command) {
//                    case "detail":
//                        System.out.println("🔍 模式: 测试详情页爬取");
//                        crawler.testDetailCrawl();
//                        break;
//                    case "list":
//                        System.out.println("📋 模式: 测试新闻列表爬取");
//                        crawler.testListCrawl();
//                        break;
//                    case "full":
//                        System.out.println("🚀 模式: 完整分页爬取（列表+详情）");
//                        // 检查是否有页数参数
//                        if (args.length > 1) {
//                            try {
//                                int pages = Integer.parseInt(args[1]);
//                                if (pages > 0 && pages <= 50) { // 限制最大50页
//                                    System.out.println("📄 指定爬取页数: " + pages);
//                                    crawler.crawlNewsData(pages);
//                                } else {
//                                    System.out.println("❌ 错误: 页数必须在1-50之间");
//                                    System.out.println("用法: java BeiceCrawler full [页数]");
//                                }
//                            } catch (NumberFormatException e) {
//                                System.out.println("❌ 错误: 页数必须是数字");
//                                System.out.println("用法: java BeiceCrawler full [页数]");
//                            }
//                        } else {
//                            crawler.crawlNewsData(); // 使用默认页数
//                        }
//                        break;
//                    case "pages":
//                        // 新增：直接指定页数爬取
//                        if (args.length > 1) {
//                            try {
//                                int pages = Integer.parseInt(args[1]);
//                                if (pages > 0 && pages <= 50) {
//                                    System.out.println("📄 模式: 爬取指定页数 (" + pages + " 页)");
//                                    crawler.crawlNewsData(pages);
//                                } else {
//                                    System.out.println("❌ 错误: 页数必须在1-50之间");
//                                    System.out.println("用法: java BeiceCrawler pages <页数>");
//                                }
//                            } catch (NumberFormatException e) {
//                                System.out.println("❌ 错误: 页数必须是数字");
//                                System.out.println("用法: java BeiceCrawler pages <页数>");
//                            }
//                        } else {
//                            System.out.println("❌ 错误: 请提供要爬取的页数");
//                            System.out.println("用法: java BeiceCrawler pages <页数>");
//                        }
//                        break;
//                    case "url":
//                        if (args.length > 1) {
//                            System.out.println("🌐 模式: 测试指定URL");
//                            crawler.testCustomUrl(args[1]);
//                        } else {
//                            System.out.println("❌ 错误: 请提供要测试的URL");
//                            System.out.println("用法: java BeiceCrawler url <URL>");
//                        }
//                        break;
//                    case "help":
//                    case "-h":
//                    case "--help":
//                        showHelp();
//                        break;
//                    default:
//                        System.out.println("❌ 未知命令: " + command);
//                        showHelp();
//                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ 程序执行失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("==========================================");
        System.out.println("🎉 测试完成！");
    }
    
    /**
     * 显示帮助信息
     */
    private static void showHelp() {
        System.out.println("📖 北测新闻爬虫使用说明:");
        System.out.println();
        System.out.println("🚀 基本用法:");
        System.out.println("  java BeiceCrawler                    # 默认执行分页爬取");
        System.out.println("  java BeiceCrawler <命令> [参数]      # 执行指定命令");
        System.out.println();
        System.out.println("📋 可用命令:");
        System.out.println("  detail     - 🔍 测试详情页爬取功能");
        System.out.println("  list       - 📋 测试新闻列表爬取功能");
        System.out.println("  full [页数] - 🚀 执行完整分页爬取（列表+详情）");
        System.out.println("  pages <页数> - 📄 直接指定页数爬取");
        System.out.println("  url <URL>  - 🌐 测试指定URL的爬取");
        System.out.println("  help       - 📖 显示此帮助信息");
        System.out.println();
        System.out.println("💡 使用示例:");
        System.out.println("  java BeiceCrawler                    # 分页爬取并保存到CSV（默认页数）");
        System.out.println("  java BeiceCrawler detail             # 测试详情页爬取");
        System.out.println("  java BeiceCrawler list               # 测试新闻列表爬取");
        System.out.println("  java BeiceCrawler full               # 完整分页爬取（默认页数）");
        System.out.println("  java BeiceCrawler full 5             # 完整分页爬取（指定5页）");
        System.out.println("  java BeiceCrawler pages 3            # 爬取指定3页");
        System.out.println("  java BeiceCrawler url https://www.ntek.org.cn/zixun/13-3095.html");
        System.out.println();
        System.out.println("📁 输出:");
        System.out.println("  数据将保存到数据库中");
        System.out.println();
        System.out.println("🌐 目标网站:");
        System.out.println("  " + NEWS_URL);
        System.out.println();
        System.out.println("📄 分页设置:");
        System.out.println("  每页新闻数: " + ITEMS_PER_PAGE);
        System.out.println("  最大页数: " + MAX_PAGES);
    }
    
    /**
     * 测试详情页爬取功能
     */
    public void testDetailCrawl() {
        System.out.println("开始测试详情页爬取功能...");
        log.info("开始测试详情页爬取功能...");
        
        // 测试URL
        String testUrl = "https://www.ntek.org.cn/zixun/13-3095.html";
        
        try {
            Document detailPage = fetchDetailPage(testUrl);
            if (detailPage != null) {
                System.out.println("✅ 成功获取详情页: " + testUrl);
                log.info("成功获取详情页: {}", testUrl);
                
                // 解析页面内容
                String title = detailPage.selectFirst("title") != null ? 
                    detailPage.selectFirst("title").text() : "无标题";
                System.out.println("📄 页面标题: " + title);
                log.info("页面标题: {}", title);
                
                // 查找内容区域
                Element contentElement = detailPage.selectFirst(".content, .article-content, .news-content, .main-content, .detail-content, .post-content, .article-body");
                if (contentElement != null) {
                    String content = contentElement.text().trim();
                    System.out.println("📝 内容长度: " + content.length() + " 字符");
                    System.out.println("📖 内容预览: " + (content.length() > 200 ? content.substring(0, 200) + "..." : content));
                    log.info("内容长度: {} 字符", content.length());
                    log.info("内容预览: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);
                } else {
                    System.out.println("⚠️  未找到内容元素");
                    log.warn("未找到内容元素");
                }
                
                // 查找作者
                Element authorElement = detailPage.selectFirst(".author, .writer, .editor, .byline, .author-name, .post-author");
                if (authorElement != null) {
                    System.out.println("👤 作者: " + authorElement.text().trim());
                    log.info("作者: {}", authorElement.text().trim());
                }
                
                // 查找发布时间
                Element timeElement = detailPage.selectFirst(".publish-time, .post-time, .article-time, .date, .time, .created-time");
                if (timeElement != null) {
                    System.out.println("🕒 发布时间: " + timeElement.text().trim());
                    log.info("发布时间: {}", timeElement.text().trim());
                }
                
                // 查找图片
                Elements imageElements = detailPage.select(".content img, .article-content img, .detail-content img, .post-content img, .article-body img");
                System.out.println("🖼️  找到 " + imageElements.size() + " 张图片");
                log.info("找到 {} 张图片", imageElements.size());
                for (int i = 0; i < Math.min(imageElements.size(), 3); i++) {
                    Element img = imageElements.get(i);
                    String src = img.attr("src");
                    if (src.startsWith("/")) {
                        src = BASE_URL + src;
                    }
                    System.out.println("   图片 " + (i + 1) + ": " + src);
                    log.info("图片 {}: {}", i + 1, src);
                }
                
            } else {
                System.out.println("❌ 无法获取详情页: " + testUrl);
                log.error("无法获取详情页: {}", testUrl);
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试详情页爬取失败: " + e.getMessage());
            log.error("测试详情页爬取失败", e);
        }
    }
    
    /**
     * 测试新闻列表爬取功能
     */
    public void testListCrawl() {
        System.out.println("开始测试新闻列表爬取功能...");
        log.info("开始测试新闻列表爬取功能...");
        
        try {
            // 获取新闻列表页面
            Document newsPage = fetchPage(NEWS_URL);
            if (newsPage == null) {
                System.out.println("❌ 无法获取新闻列表页面");
                log.error("无法获取新闻列表页面");
                return;
            }
            
            // 解析新闻列表
            List<NewsItem> newsItems = parseNewsList(newsPage);
            if (newsItems != null && !newsItems.isEmpty()) {
                System.out.println("✅ 成功获取新闻列表，共 " + newsItems.size() + " 条新闻");
                log.info("成功获取新闻列表，共 {} 条新闻", newsItems.size());
                
                // 显示前3条新闻的详细信息
                for (int i = 0; i < Math.min(newsItems.size(), 3); i++) {
                    NewsItem item = newsItems.get(i);
                    System.out.println("\n📰 新闻 " + (i + 1) + ":");
                    System.out.println("   标题: " + (item.getTitle() != null ? item.getTitle() : "无标题"));
                    System.out.println("   链接: " + (item.getUrl() != null ? item.getUrl() : "无链接"));
                    System.out.println("   时间: " + (item.getPublishTime() != null ? item.getPublishTime() : "无时间"));
                    System.out.println("   摘要: " + (item.getSummary() != null && item.getSummary().length() > 100 ? 
                        item.getSummary().substring(0, 100) + "..." : item.getSummary()));
                }
                
                if (newsItems.size() > 3) {
                    System.out.println("\n... 还有 " + (newsItems.size() - 3) + " 条新闻");
                }
                
            } else {
                System.out.println("❌ 未获取到新闻列表");
                log.warn("未获取到新闻列表");
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试新闻列表爬取失败: " + e.getMessage());
            log.error("测试新闻列表爬取失败", e);
        }
    }
    
    /**
     * 测试指定URL的爬取
     */
    public void testCustomUrl(String url) {
        System.out.println("开始测试指定URL爬取功能...");
        System.out.println("测试URL: " + url);
        log.info("开始测试指定URL爬取功能: {}", url);
        
        try {
            Document page = fetchDetailPage(url);
            if (page != null) {
                System.out.println("✅ 成功获取页面: " + url);
                log.info("成功获取页面: {}", url);
                
                // 解析页面内容
                String title = page.selectFirst("title") != null ? 
                    page.selectFirst("title").text() : "无标题";
                System.out.println("📄 页面标题: " + title);
                
                // 查找内容区域
                Element contentElement = page.selectFirst(".content, .article-content, .news-content, .main-content, .detail-content, .post-content, .article-body");
                if (contentElement != null) {
                    String content = contentElement.text().trim();
                    System.out.println("📝 内容长度: " + content.length() + " 字符");
                    System.out.println("📖 内容预览: " + (content.length() > 300 ? content.substring(0, 300) + "..." : content));
                } else {
                    System.out.println("⚠️  未找到内容元素");
                }
                
                // 查找所有链接
                Elements links = page.select("a[href]");
                System.out.println("🔗 找到 " + links.size() + " 个链接");
                
                // 查找所有图片
                Elements images = page.select("img[src]");
                System.out.println("🖼️  找到 " + images.size() + " 张图片");
                
            } else {
                System.out.println("❌ 无法获取页面: " + url);
                log.error("无法获取页面: {}", url);
            }
            
        } catch (Exception e) {
            System.out.println("❌ 测试指定URL爬取失败: " + e.getMessage());
            log.error("测试指定URL爬取失败", e);
        }
    }
    
    // ==================== BaseCrawler接口实现 ====================
    
    @Override
    public List<CrawlerResult> crawl(String keyword, int totalCount) throws Exception {
        log.info("开始北测新闻爬虫，关键词: {}, 数量: {}", keyword, totalCount);
        
        List<CrawlerResult> results = new ArrayList<>();
        int page = 1;
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 3;
        
        while (results.size() < totalCount && consecutiveFailures < MAX_CONSECUTIVE_FAILURES && page <= MAX_PAGES) {
            log.info("正在爬取第 {} 页，关键词: {}", page, keyword != null ? keyword : "");
            
            try {
                // 构建分页URL
                String pageUrl = buildPageUrl(page);
                
                // 获取当前页面
                Document newsPage = fetchPage(pageUrl);
                if (newsPage == null) {
                    log.warn("无法获取第 {} 页", page);
                    consecutiveFailures++;
                    page++;
                    continue;
                }
                
                // 解析新闻列表
                List<NewsItem> newsItems = parseNewsListFromPage(newsPage, page);
                if (newsItems.isEmpty()) {
                    log.info("第 {} 页没有新闻数据", page);
                    consecutiveFailures++;
                    page++;
                    continue;
                }
                
                // 处理新闻项
                for (NewsItem item : newsItems) {
                    if (results.size() >= totalCount) {
                        break;
                    }
                    
                    // 如果有关键词过滤，检查标题和摘要是否包含关键词
                    if (keyword != null && !keyword.isEmpty()) {
                        boolean containsKeyword = false;
                        if (item.getTitle() != null && item.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                            containsKeyword = true;
                        }
                        if (!containsKeyword && item.getSummary() != null && item.getSummary().toLowerCase().contains(keyword.toLowerCase())) {
                            containsKeyword = true;
                        }
                        if (!containsKeyword) {
                            continue;
                        }
                    }
                    
                    try {
                        // 获取详情
                        fetchNewsDetail(item);
                        
                        // 转换为CrawlerResult
                        CrawlerResult crawlerResult = convertToCrawlerResult(item);
                        results.add(crawlerResult);
                        
                        log.debug("成功爬取新闻: {}", item.getTitle());
                        
                        // 添加延迟
                        Thread.sleep(1000);
                        
                    } catch (Exception e) {
                        log.warn("处理新闻项失败: {} - {}", item.getTitle(), e.getMessage());
                    }
                }
                
                consecutiveFailures = 0;
                page++;
                
                // 页面间延迟
                Thread.sleep(2000);
                
            } catch (Exception e) {
                log.error("爬取第 {} 页失败: {}", page, e.getMessage());
                consecutiveFailures++;
                page++;
            }
        }
        
        log.info("北测新闻爬虫完成，共爬取 {} 条数据", results.size());
        return results;
    }
    
    @Override
    public List<CrawlerResult> crawlLatest(int totalCount) throws Exception {
        return crawl("", totalCount);
    }
    
    @Override
    public String getCrawlerName() {
        return "北测新闻爬虫";
    }
    
    @Override
    public String getSourceName() {
        return "北测";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Document page = fetchPage(NEWS_URL);
            return page != null;
        } catch (Exception e) {
            log.error("检查北测爬虫可用性失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public CrawlerConfig getConfig() {
        return config;
    }
    
    /**
     * 将NewsItem转换为CrawlerResult
     */
    private CrawlerResult convertToCrawlerResult(NewsItem item) {
        CrawlerResult result = new CrawlerResult();
        result.setTitle(item.getTitle());
        result.setUrl(item.getUrl());
        result.setContent(item.getContent());
        result.setDate(item.getPublishTime());
        result.setSource("北测");
        result.setCrawlTime(LocalDateTime.now());
        result.setCategory("certification");
        // 从标题中识别国家
        result.setCountry(extractCountryFromTitle(item.getTitle()));
        result.setType("news");
        return result;
    }
    
    /**
     * 从标题中提取国家信息
     * @param title 新闻标题
     * @return 国家名称，如果没有匹配到则返回null
     */
    private String extractCountryFromTitle(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        
        // 国家名称映射表 - 包含中文名称和常见的英文缩写/机构名
        Map<String, String> countryMappings = new HashMap<>();
        
        // 亚洲国家
        countryMappings.put("中国", "中国");
        countryMappings.put("日本", "日本");
        countryMappings.put("韩国", "韩国");
        countryMappings.put("印度", "印度");
        countryMappings.put("新加坡", "新加坡");
        countryMappings.put("泰国", "泰国");
        countryMappings.put("马来西亚", "马来西亚");
        countryMappings.put("印度尼西亚", "印度尼西亚");
        countryMappings.put("菲律宾", "菲律宾");
        countryMappings.put("越南", "越南");
        countryMappings.put("缅甸", "缅甸");
        countryMappings.put("柬埔寨", "柬埔寨");
        countryMappings.put("老挝", "老挝");
        countryMappings.put("文莱", "文莱");
        
        // 欧洲国家和地区
        countryMappings.put("欧盟", "欧盟");
        countryMappings.put("EU", "欧盟");
        countryMappings.put("欧洲", "欧盟");
        countryMappings.put("德国", "德国");
        countryMappings.put("法国", "法国");
        countryMappings.put("英国", "英国");
        countryMappings.put("意大利", "意大利");
        countryMappings.put("西班牙", "西班牙");
        countryMappings.put("荷兰", "荷兰");
        countryMappings.put("比利时", "比利时");
        countryMappings.put("瑞士", "瑞士");
        countryMappings.put("奥地利", "奥地利");
        countryMappings.put("瑞典", "瑞典");
        countryMappings.put("挪威", "挪威");
        countryMappings.put("丹麦", "丹麦");
        countryMappings.put("芬兰", "芬兰");
        countryMappings.put("波兰", "波兰");
        countryMappings.put("捷克", "捷克");
        countryMappings.put("匈牙利", "匈牙利");
        countryMappings.put("希腊", "希腊");
        countryMappings.put("葡萄牙", "葡萄牙");
        countryMappings.put("爱尔兰", "爱尔兰");
        countryMappings.put("卢森堡", "卢森堡");
        countryMappings.put("俄罗斯", "俄罗斯");
        countryMappings.put("乌克兰", "乌克兰");
        
        // 北美洲
        countryMappings.put("美国", "美国");
        countryMappings.put("USA", "美国");
        countryMappings.put("US", "美国");
        countryMappings.put("加拿大", "加拿大");
        countryMappings.put("墨西哥", "墨西哥");
        
        // 南美洲
        countryMappings.put("巴西", "巴西");
        countryMappings.put("阿根廷", "阿根廷");
        countryMappings.put("智利", "智利");
        countryMappings.put("秘鲁", "秘鲁");
        countryMappings.put("哥伦比亚", "哥伦比亚");
        countryMappings.put("委内瑞拉", "委内瑞拉");
        countryMappings.put("厄瓜多尔", "厄瓜多尔");
        countryMappings.put("乌拉圭", "乌拉圭");
        countryMappings.put("玻利维亚", "玻利维亚");
        countryMappings.put("巴拉圭", "巴拉圭");
        
        // 大洋洲
        countryMappings.put("澳大利亚", "澳大利亚");
        countryMappings.put("澳洲", "澳大利亚");
        countryMappings.put("新西兰", "新西兰");
        
        // 非洲
        countryMappings.put("南非", "南非");
        countryMappings.put("埃及", "埃及");
        countryMappings.put("尼日利亚", "尼日利亚");
        countryMappings.put("肯尼亚", "肯尼亚");
        countryMappings.put("摩洛哥", "摩洛哥");
        countryMappings.put("突尼斯", "突尼斯");
        countryMappings.put("阿尔及利亚", "阿尔及利亚");
        countryMappings.put("加纳", "加纳");
        countryMappings.put("坦桑尼亚", "坦桑尼亚");
        
        // 中东地区
        countryMappings.put("沙特阿拉伯", "沙特阿拉伯");
        countryMappings.put("沙特", "沙特阿拉伯");
        countryMappings.put("阿联酋", "阿联酋");
        countryMappings.put("UAE", "阿联酋");
        countryMappings.put("以色列", "以色列");
        countryMappings.put("土耳其", "土耳其");
        countryMappings.put("伊朗", "伊朗");
        countryMappings.put("伊拉克", "伊拉克");
        countryMappings.put("科威特", "科威特");
        countryMappings.put("卡塔尔", "卡塔尔");
        countryMappings.put("巴林", "巴林");
        countryMappings.put("阿曼", "阿曼");
        countryMappings.put("约旦", "约旦");
        countryMappings.put("黎巴嫩", "黎巴嫩");
        
        // 监管机构缩写映射
        countryMappings.put("FCC", "美国");
        countryMappings.put("FDA", "美国");
        countryMappings.put("UL", "美国");
        countryMappings.put("CE", "欧盟");
        countryMappings.put("ROHS", "欧盟");
        countryMappings.put("REACH", "欧盟");
        countryMappings.put("PSE", "日本");
        countryMappings.put("KC", "韩国");
        countryMappings.put("KCC", "韩国");
        countryMappings.put("CCC", "中国");
        countryMappings.put("SRRC", "中国");
        countryMappings.put("BIS", "印度");
        countryMappings.put("ACMA", "澳大利亚");
        countryMappings.put("IC", "加拿大");
        countryMappings.put("ISED", "加拿大");
        countryMappings.put("ANATEL", "巴西");
        countryMappings.put("SEC", "智利");
        countryMappings.put("SUBTEL", "智利");
        countryMappings.put("NCC", "中国台湾");
        countryMappings.put("BSMI", "中国台湾");
        countryMappings.put("台湾", "中国台湾");
        countryMappings.put("香港", "中国香港");
        countryMappings.put("澳门", "中国澳门");
        
        // 按长度排序，优先匹配较长的关键词（避免部分匹配问题）
        List<String> sortedKeys = countryMappings.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        // 在标题中查找匹配的国家关键词
        String titleUpper = title.toUpperCase();
        for (String keyword : sortedKeys) {
            String keywordUpper = keyword.toUpperCase();
            if (titleUpper.contains(keywordUpper)) {
                String country = countryMappings.get(keyword);
                log.debug("从标题中识别到国家: {} -> {}, 标题: {}", keyword, country, title);
                return country;
            }
        }
        
        // 没有匹配到任何国家
        log.debug("未从标题中识别到国家: {}", title);
        return null;
    }
    
    // ==================== 数据保存相关方法 ====================
    
    /**
     * 执行北测爬虫并保存到数据库
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeBeiceCrawlerAndSave(int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            systemLogService.logInfo(
                "北测爬虫开始执行",
                String.format("开始执行北测爬虫，计划爬取 %d 条数据", count),
                "BeiceCrawler"
            );
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("北测");
            
            // 执行分页爬取，每页保存到数据库
            List<CrawlerData> allSavedDataList = new ArrayList<>();
            List<CrawlerResult> allCrawlerResults = new ArrayList<>();
            int page = 1;
            int consecutiveFailures = 0;
            final int MAX_CONSECUTIVE_FAILURES = 3;
            // 根据请求数量计算需要的最大页数，但不超过安全限制
            int maxPagesNeeded = Math.min((count / ITEMS_PER_PAGE) + 10, 500);
            
            while (allCrawlerResults.size() < count && consecutiveFailures < MAX_CONSECUTIVE_FAILURES && page <= maxPagesNeeded) {
                log.info("正在爬取第 {} 页，目标数量: {}, 已爬取: {}", page, count, allCrawlerResults.size());
                
                try {
                    // 构建分页URL
                    String pageUrl = buildPageUrl(page);
                    
                    // 获取当前页面
                    Document newsPage = fetchPage(pageUrl);
                    if (newsPage == null) {
                        log.warn("无法获取第 {} 页", page);
                        consecutiveFailures++;
                        page++;
                        continue;
                    }
                    
                    // 解析新闻列表
                    List<NewsItem> newsItems = parseNewsListFromPage(newsPage, page);
                    if (newsItems.isEmpty()) {
                        log.info("第 {} 页没有新闻数据", page);
                        consecutiveFailures++;
                        page++;
                        continue;
                    }
                    
                    // 处理当前页的新闻项
                    List<CrawlerResult> pageResults = new ArrayList<>();
                    for (NewsItem item : newsItems) {
                        if (allCrawlerResults.size() >= count) {
                            break;
                        }
                        
                        try {
                            // 获取详情
                            fetchNewsDetail(item);
                            
                            // 转换为CrawlerResult
                            CrawlerResult crawlerResult = convertToCrawlerResult(item);
                            pageResults.add(crawlerResult);
                            allCrawlerResults.add(crawlerResult);
                            
                            log.debug("成功爬取新闻: {}", item.getTitle());
                            
                            // 添加延迟
                            Thread.sleep(1000);
                            
                        } catch (Exception e) {
                            log.warn("处理新闻项失败: {} - {}", item.getTitle(), e.getMessage());
                        }
                    }
                    
                    // 每页数据立即保存到数据库
                    if (!pageResults.isEmpty()) {
                        log.info("第 {} 页爬取完成，准备保存 {} 条数据到数据库", page, pageResults.size());
                        
                        // 转换为CrawlerData实体
                        List<CrawlerData> pageDataList = convertToCrawlerData(pageResults);
                        
                        // 立即保存当前页数据
                        List<CrawlerData> pageSavedList = crawlerDataService.safeSaveCrawlerDataList(pageDataList, 30);
                        allSavedDataList.addAll(pageSavedList);
                        
                        log.info("第 {} 页数据保存完成，保存了 {} 条新数据", page, pageSavedList.size());
                        
                        // 记录当前页的数据保存日志
                        for (CrawlerData data : pageSavedList) {
                            systemLogService.logInfo(
                                "爬虫数据创建（分页保存）",
                                String.format("北测爬虫第%d页创建新数据: ID=%s, 标题=%s, 国家=%s", 
                                    page, data.getId(), data.getTitle(), data.getCountry()),
                                "BeiceCrawler"
                            );
                        }
                    }
                    
                    consecutiveFailures = 0;
                    page++;
                    
                    // 页面间延迟
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    log.error("爬取第 {} 页失败: {}", page, e.getMessage());
                    consecutiveFailures++;
                    page++;
                }
            }
            
            log.info("北测新闻爬虫完成，共爬取 {} 条数据，保存 {} 条数据", allCrawlerResults.size(), allSavedDataList.size());
            
            // 使用已保存的数据列表进行统计
            List<CrawlerData> savedDataList = allSavedDataList;

            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("北测");
            long newDataCount = afterCount - beforeCount;
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CrawlerData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            systemLogService.logInfo(
                "北测爬虫执行完成",
                String.format("北测爬虫执行完成，爬取 %d 条数据，新增 %d 条，耗时 %d ms",
                    allCrawlerResults.size(), newDataCount, executionTime),
                "BeiceCrawler"
            );
            
            // 构建返回结果
            result.put("success", true);
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("requestedCount", count);
            result.put("crawledCount", allCrawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "北测爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            systemLogService.logError(
                "北测爬虫执行失败",
                String.format("北测爬虫执行失败: %s", e.getMessage()),
                "BeiceCrawler",
                e
            );
            
            result.put("success", false);
            result.put("error", "北测爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
    
    /**
     * 将CrawlerResult转换为CrawlerData实体
     * @param crawlerResults 爬虫结果列表
     * @return CrawlerData实体列表
     */
    private List<CrawlerData> convertToCrawlerData(List<CrawlerResult> crawlerResults) {
        List<CrawlerData> crawlerDataList = new ArrayList<>();
        
        for (CrawlerResult result : crawlerResults) {
            CrawlerData crawlerData = new CrawlerData();
            // 设置ID为随机UUID
            crawlerData.setId(UUID.randomUUID().toString());
            
            // 设置基本信息
            crawlerData.setSourceName(result.getSource());
            crawlerData.setTitle(result.getTitle());
            crawlerData.setUrl(result.getUrl());
            crawlerData.setSummary(result.getContent());
            crawlerData.setContent(result.getContent());
            
            // 统一日期格式
            String rawDate = result.getDate();
            String standardizedDate = dateFormatService.standardizeDate(rawDate);
            if (standardizedDate == null) {
                log.warn("北测爬虫无法解析日期格式: {}", rawDate);
                standardizedDate = dateFormatService.getCurrentDateString();
            }
            crawlerData.setPublishDate(standardizedDate);
            crawlerData.setCrawlTime(result.getCrawlTime());
            crawlerData.setType(result.getType());
            crawlerData.setCountry(result.getCountry());
            
            // 设置状态
            crawlerData.setStatus(CrawlerData.DataStatus.NEW);
            crawlerData.setIsProcessed(false);
            
            // 设置风险等级为MEDIUM
            crawlerData.setRiskLevel(CrawlerData.RiskLevel.MEDIUM);
            
            // 设置备注
            crawlerData.setRemarks("通过北测爬虫自动抓取");
            
            crawlerDataList.add(crawlerData);
        }
        
        return crawlerDataList;
    }
    
    /**
     * 执行北测爬虫并保存到数据库（带关键词搜索）
     * @param keyword 搜索关键词
     * @param count 爬取数量
     * @return 执行结果
     */
    public Map<String, Object> executeBeiceCrawlerWithKeywordAndSave(String keyword, int count) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录开始日志
            systemLogService.logInfo(
                "北测爬虫开始执行（关键词搜索）",
                String.format("开始执行北测爬虫，关键词: %s，计划爬取 %d 条数据", keyword, count),
                "BeiceCrawler"
            );
            
            // 记录爬取前的数据数量
            long beforeCount = crawlerDataService.getCountBySourceName("北测");
            
            // 执行分页爬取（带关键词），每页保存到数据库
            List<CrawlerData> allSavedDataList = new ArrayList<>();
            List<CrawlerResult> allCrawlerResults = new ArrayList<>();
            int page = 10;
            int consecutiveFailures = 0;
            final int MAX_CONSECUTIVE_FAILURES = 3;
            // 根据请求数量计算需要的最大页数，但不超过安全限制
            int maxPagesNeeded = Math.min((count / ITEMS_PER_PAGE) + 10, 500);
            
            while (allCrawlerResults.size() < count && consecutiveFailures < MAX_CONSECUTIVE_FAILURES && page <= maxPagesNeeded) {
                log.info("正在爬取第 {} 页，关键词: {}, 目标数量: {}, 已爬取: {}", page, keyword, count, allCrawlerResults.size());
                
                try {
                    // 构建分页URL
                    String pageUrl = buildPageUrl(page);
                    
                    // 获取当前页面
                    Document newsPage = fetchPage(pageUrl);
                    if (newsPage == null) {
                        log.warn("无法获取第 {} 页", page);
                        consecutiveFailures++;
                        page++;
                        continue;
                    }
                    
                    // 解析新闻列表
                    List<NewsItem> newsItems = parseNewsListFromPage(newsPage, page);
                    if (newsItems.isEmpty()) {
                        log.info("第 {} 页没有新闻数据", page);
                        consecutiveFailures++;
                        page++;
                        continue;
                    }
                    
                    // 处理当前页的新闻项
                    List<CrawlerResult> pageResults = new ArrayList<>();
                    for (NewsItem item : newsItems) {
                        if (allCrawlerResults.size() >= count) {
                            break;
                        }
                        
                        // 如果有关键词过滤，检查标题和摘要是否包含关键词
                        if (keyword != null && !keyword.isEmpty()) {
                            boolean containsKeyword = false;
                            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                                containsKeyword = true;
                            }
                            if (!containsKeyword && item.getSummary() != null && item.getSummary().toLowerCase().contains(keyword.toLowerCase())) {
                                containsKeyword = true;
                            }
                            if (!containsKeyword) {
                                continue;
                            }
                        }
                        
                        try {
                            // 获取详情
                            fetchNewsDetail(item);
                            
                            // 转换为CrawlerResult
                            CrawlerResult crawlerResult = convertToCrawlerResult(item);
                            pageResults.add(crawlerResult);
                            allCrawlerResults.add(crawlerResult);
                            
                            log.debug("成功爬取新闻: {}", item.getTitle());
                            
                            // 添加延迟
                            Thread.sleep(1000);
                            
                        } catch (Exception e) {
                            log.warn("处理新闻项失败: {} - {}", item.getTitle(), e.getMessage());
                        }
                    }
                    
                    // 每页数据立即保存到数据库
                    if (!pageResults.isEmpty()) {
                        log.info("第 {} 页爬取完成，准备保存 {} 条数据到数据库", page, pageResults.size());
                        
                        // 转换为CrawlerData实体
                        List<CrawlerData> pageDataList = convertToCrawlerData(pageResults);
                        
                        // 立即保存当前页数据
                        List<CrawlerData> pageSavedList = crawlerDataService.safeSaveCrawlerDataList(pageDataList, 30);
                        allSavedDataList.addAll(pageSavedList);
                        
                        log.info("第 {} 页数据保存完成，保存了 {} 条新数据", page, pageSavedList.size());
                        
                        // 记录当前页的数据保存日志
                        for (CrawlerData data : pageSavedList) {
                            systemLogService.logInfo(
                                "爬虫数据创建（关键词分页保存）",
                                String.format("北测爬虫第%d页创建新数据: ID=%s, 标题=%s, 关键词=%s, 国家=%s", 
                                    page, data.getId(), data.getTitle(), keyword, data.getCountry()),
                                "BeiceCrawler"
                            );
                        }
                    }
                    
                    consecutiveFailures = 0;
                    page++;
                    
                    // 页面间延迟
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    log.error("爬取第 {} 页失败: {}", page, e.getMessage());
                    consecutiveFailures++;
                    page++;
                }
            }
            
            log.info("北测新闻爬虫完成，共爬取 {} 条数据，保存 {} 条数据", allCrawlerResults.size(), allSavedDataList.size());
            
            // 使用已保存的数据列表进行统计
            List<CrawlerData> crawlerDataList = allSavedDataList;

            // 检查是否全部重复
            if (crawlerDataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "没有爬取到任何数据");
                result.put("crawledCount", 0);
                result.put("savedCount", 0);
                result.put("duplicateCount", 0);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "北测爬虫执行完成（无数据）",
                    String.format("北测爬虫执行完成，关键词: %s，没有爬取到任何数据", keyword),
                    "BeiceCrawler"
                );
                return result;
            }

            // 获取去重统计信息
            Map<String, Object> duplicateStats = crawlerDataService.getDuplicateUrlStats(crawlerDataList);
            long duplicateCount = duplicateStats.get("duplicateCount") == null ? 0L : ((Number)duplicateStats.get("duplicateCount")).longValue();
            
            // 检查是否全部重复
            boolean allDuplicates = duplicateCount == crawlerDataList.size();
            if (allDuplicates) {
                result.put("success", true);
                result.put("message", "爬取的数据全部与数据库重复，停止爬取");
                result.put("crawledCount", crawlerDataList.size());
                result.put("savedCount", 0);
                result.put("duplicateCount", duplicateCount);
                result.put("allDuplicates", true);
                result.put("stoppedEarly", true);
                result.put("executionTime", System.currentTimeMillis() - startTime);
                result.put("timestamp", LocalDateTime.now().toString());
                
                systemLogService.logInfo(
                    "北测爬虫执行完成（全部重复）",
                    String.format("北测爬虫执行完成，关键词: %s，爬取 %d 条数据全部重复，停止爬取", keyword, crawlerDataList.size()),
                    "BeiceCrawler"
                );
                return result;
            }
            
            // 数据已在每页保存时处理，直接使用已保存的数据
            List<CrawlerData> savedDataList = allSavedDataList;
            
            // 记录爬取后的数据数量
            long afterCount = crawlerDataService.getCountBySourceName("北测");
            long newDataCount = afterCount - beforeCount;
            
            // 统计各状态的数据数量
            Map<String, Long> statusCounts = new HashMap<>();
            for (CrawlerData data : savedDataList) {
                String status = data.getStatus().name();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            systemLogService.logInfo(
                "北测爬虫执行完成（关键词搜索）",
                String.format("北测爬虫执行完成，关键词: %s，爬取 %d 条数据，新增 %d 条，重复 %d 条，耗时 %d ms", 
                    keyword, allCrawlerResults.size(), newDataCount, duplicateCount, executionTime),
                "BeiceCrawler"
            );
            
            // 构建返回结果
            result.put("success", true);
            result.put("crawlerName", getCrawlerName());
            result.put("sourceName", getSourceName());
            result.put("keyword", keyword);
            result.put("requestedCount", count);
            result.put("crawledCount", allCrawlerResults.size());
            result.put("savedCount", savedDataList.size());
            result.put("newDataCount", newDataCount);
            result.put("totalDataCount", afterCount);
            result.put("statusCounts", statusCounts);
            result.put("duplicateStats", duplicateStats);
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
            result.put("message", "北测爬虫执行成功");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            systemLogService.logError(
                "北测爬虫执行失败（关键词搜索）",
                String.format("北测爬虫执行失败，关键词: %s，错误: %s", keyword, e.getMessage()),
                "BeiceCrawler",
                e
            );
            
            result.put("success", false);
            result.put("error", "北测爬虫执行失败: " + e.getMessage());
            result.put("executionTime", executionTime);
            result.put("timestamp", LocalDateTime.now().toString());
        }
        
        return result;
    }
}