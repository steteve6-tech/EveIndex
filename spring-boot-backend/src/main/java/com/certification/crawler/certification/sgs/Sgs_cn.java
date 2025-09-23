package com.certification.crawler.certification.sgs;

import com.certification.crawler.certification.base.BaseCrawler;
import com.certification.crawler.certification.base.CrawlerResult;
import com.certification.crawler.common.HttpUtils;
import com.certification.service.DateFormatService;
import com.certification.standards.CrawlerDataService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * SGS中国网站爬虫实现 - 爬取SGS中国新闻页面
 */
@Slf4j
@Component
@Transactional
public class Sgs_cn implements BaseCrawler {

    @Autowired
    private HttpUtils httpUtils;
    
    @Autowired
    private CrawlerDataService crawlerDataService;

    @Autowired
    private DateFormatService dateFormatService;

    private final CrawlerConfig config;
    private static final String BASE_URL = "https://www.sgsgroup.com.cn";
    private static final String NEWS_URL = "https://www.sgsgroup.com.cn/zh-cn/news";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    public Sgs_cn() {
        this.config = new CrawlerConfig();
        this.config.setBaseUrl(BASE_URL);
        this.config.setTimeout(30000);
        this.config.setRetryCount(3);
        this.config.setUserAgent(USER_AGENT);
    }

    /**
     * 发送GET请求获取页面内容
     *
     * @param url 请求URL
     * @return 页面HTML内容
     * @throws Exception 网络异常
     */
    public String sendGetRequest(String url) throws Exception {
        System.out.println("开始发送GET请求: " + url);
        
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
                .header("User-Agent", USER_AGENT)
                .header("Referer", "https://www.sgsgroup.com.cn/zh-cn")
                .header("Referrer-Policy", "strict-origin-when-cross-origin")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("收到HTTP响应，状态码: " + response.statusCode() + ", 长度: " + response.body().length());
        
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP请求失败，状态码: " + response.statusCode());
        }
    }

    /**
     * 解析新闻页面HTML内容
     *
     * @param html HTML内容
     * @return 爬虫结果列表
     * @throws Exception 解析异常
     */
    public List<CrawlerResult> parseNewsPage(String html) throws Exception {
        List<CrawlerResult> result = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // 查找新闻列表容器
        Elements newsItems = doc.select(".news-item, .article-item, .list-item, [class*='news'], [class*='article']");
        
        // 如果没找到特定的新闻容器，尝试查找通用的链接容器
        if (newsItems.isEmpty()) {
            newsItems = doc.select("a[href*='/news/'], a[href*='/article/'], a[href*='/zh-cn/']");
        }

        for (Element item : newsItems) {
            try {
                String title = "";
                String url = "";
                String content = "";
                String date = "";

                // 提取标题
                Element titleElement = item.selectFirst("h1, h2, h3, h4, .title, .headline, [class*='title'], [class*='headline']");
                if (titleElement != null) {
                    title = titleElement.text().trim();
                } else if (item.tagName().equals("a")) {
                    title = item.text().trim();
                }

                // 提取URL
                if (item.tagName().equals("a")) {
                    url = item.attr("href");
                } else {
                    Element linkElement = item.selectFirst("a");
                    if (linkElement != null) {
                        url = linkElement.attr("href");
                    }
                }

                // 确保URL是完整的
                if (!url.isEmpty() && !url.startsWith("http")) {
                    if (url.startsWith("/")) {
                        url = BASE_URL + url;
                    } else {
                        url = BASE_URL + "/" + url;
                    }
                }

                // 提取内容摘要
                Element contentElement = item.selectFirst(".content, .summary, .description, .excerpt, [class*='content'], [class*='summary'], [class*='description']");
                if (contentElement != null) {
                    content = contentElement.text().trim();
                }

                // 提取日期
                Element dateElement = item.selectFirst(".date, .time, .publish-date, [class*='date'], [class*='time']");
                if (dateElement != null) {
                    date = dateElement.text().trim();
                }

                // 统一日期格式
                String standardizedDate = dateFormatService.standardizeDate(date);
                if (standardizedDate == null) {
                    log.warn("SGS中国爬虫无法解析日期格式: {}", date);
                    standardizedDate = dateFormatService.getCurrentDateString();
                }

                // 只添加有效的新闻项
                if (!title.isEmpty() && !url.isEmpty()) {
                    CrawlerResult crawlerResult = new CrawlerResult(title, url, content, standardizedDate, "SGS中国");
                    crawlerResult.setCategory("certification");
                    crawlerResult.setType("news");
                    crawlerResult.setCountry("CN");
                    result.add(crawlerResult);
                }
            } catch (Exception e) {
                log.warn("解析新闻项失败: {}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    public List<CrawlerResult> crawl(String keyword, int totalCount) throws Exception {
        List<CrawlerResult> result = new ArrayList<>();
        
        try {
            // 获取新闻列表页面
            String html = sendGetRequest(NEWS_URL);
            List<CrawlerResult> newsResults = parseNewsPage(html);
            
            // 如果有关键词，过滤结果
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                newsResults = newsResults.stream()
                    .filter(item -> item.getTitle().toLowerCase().contains(lowerKeyword) ||
                                   item.getContent().toLowerCase().contains(lowerKeyword))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            }
            
            // 限制结果数量
            int count = Math.min(totalCount, newsResults.size());
            for (int i = 0; i < count; i++) {
                result.add(newsResults.get(i));
            }
            
        } catch (Exception e) {
            log.error("SGS中国爬虫执行失败: {}", e.getMessage(), e);
            throw e;
        }
        
        return result;
    }

    @Override
    public List<CrawlerResult> crawlLatest(int totalCount) throws Exception {
        return crawl("", totalCount);
    }

    @Override
    public String getCrawlerName() {
        return "SGS中国爬虫";
    }

    @Override
    public String getSourceName() {
        return "SGS中国";
    }

    @Override
    public boolean isAvailable() {
        try {
            System.out.println("开始检查SGS中国爬虫可用性...");
            
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NEWS_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("SGS中国爬虫可用性检查成功，状态码: " + response.statusCode());
                return true;
            } else {
                System.err.println("SGS中国爬虫可用性检查失败，状态码: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("SGS中国爬虫可用性检查失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public CrawlerConfig getConfig() {
        return config;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            System.out.println("开始测试SGS中国爬虫...");
            
            // 创建爬虫实例
            Sgs_cn crawler = new Sgs_cn();
            
            // 测试可用性
            boolean available = crawler.isAvailable();
            System.out.println("爬虫可用性: " + available);
            
            if (available) {
                // 测试爬取功能
                List<CrawlerResult> results = crawler.crawlLatest(5);
                System.out.println("爬取到 " + results.size() + " 条数据:");
                
                for (int i = 0; i < results.size(); i++) {
                    CrawlerResult result = results.get(i);
                    System.out.println((i + 1) + ". " + result.getTitle());
                    System.out.println("   URL: " + result.getUrl());
                    System.out.println("   日期: " + result.getDate());
                    System.out.println("   内容: " + result.getContent().substring(0, Math.min(100, result.getContent().length())) + "...");
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
