package com.certification.crawler;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;

/**
 * CBP rulings爬虫：根据关键词爬取指定日期之前的数据
 * 注意：这是一个Angular SPA应用，需要JavaScript渲染才能获取实际数据
 * 当前版本使用HTTP请求，可能无法获取到完整的数据内容
 */
public class CBPRulingCrawler {
    // 基础URL
    private static final String BASE_URL = "https://rulings.cbp.gov/search";
    // 日期格式化器（页面日期格式为yyyy-MM-dd）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    // 重试相关
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY_SECONDS = 2;

    /**
     * 爬取数据的主方法
     *
     * @param term         搜索关键词（如"9018"）
     * @param pageSize     每页记录数
     * @param maxPages     最大爬取页数（防止无限循环）
     * @param endDate      截止日期（只保留此日期及之前的数据）
     */
    public void crawl(String term, int pageSize, int maxPages, LocalDate endDate) {
        System.out.println("=== CBP Rulings 爬虫启动 ===");
        System.out.println("搜索关键词: " + term);
        System.out.println("每页记录数: " + pageSize);
        System.out.println("最大页数: " + maxPages);
        System.out.println("截止日期: " + endDate);
        System.out.println("警告：这是一个Angular SPA应用，直接HTTP请求可能无法获取到实际数据内容。");
        System.out.println("建议使用支持JavaScript渲染的工具，如Selenium WebDriver或Playwright。");
        
        // 循环爬取多页
        for (int page = 1; page <= maxPages; page++) {
            System.out.println("\n===== 爬取第 " + page + " 页 =====");
            boolean hasValidData = false; // 标记当前页是否有符合条件的数据

            try {
                // 1. 构建请求URL
                String url = buildUrl(term, pageSize, page);
                // 2. 发送请求获取页面内容
                String html = fetchHtml(url);
                if (html == null || html.isEmpty()) {
                    System.out.println("页面内容为空，停止爬取");
                    break;
                }

                System.out.println("获取到的HTML长度: " + html.length());
                System.out.println("HTML前500字符: " + html.substring(0, Math.min(500, html.length())));
                
                // 3. 解析页面并提取数据
                List<RulingRecord> records = parseHtml(html, endDate);
                if (records.isEmpty()) {
                    System.out.println("当前页无符合条件的数据");
                    System.out.println("这可能是因为页面需要JavaScript渲染才能显示数据");
                    // 如果当前页无数据，且已爬取超过1页，可能后续也无数据，停止爬取
                    if (page > 1) break;
                } else {
                    hasValidData = true;
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
        
        System.out.println("\n=== 爬取完成 ===");
    }

    /**
     * 构建完整的请求URL
     */
    private String buildUrl(String term, int pageSize, int page) throws Exception {
        // 编码参数（处理特殊字符）
        String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8.name());
        return String.format("%s?term=%s&collection=ALL&sortBy=DATE_DESC&pageSize=%d&page=%d",
                BASE_URL, encodedTerm, pageSize, page);
    }

    /**
     * 发送HTTP请求获取页面HTML
     */
    private String fetchHtml(String url) throws IOException {
        int attempt = 0;
        IOException lastEx = null;
        while (attempt < RETRY_COUNT) {
            attempt++;
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                System.out.println("请求URL: " + url);
                // 设置请求头模拟浏览器
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
                request.setHeader("Accept-Encoding", "gzip, deflate, br");
                request.setHeader("Connection", "keep-alive");
                request.setHeader("Upgrade-Insecure-Requests", "1");
                request.setHeader("Cache-Control", "no-cache");
                request.setHeader("Pragma", "no-cache");

                // 超时配置，避免长时间挂起
                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(10))
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .build();
                request.setConfig(config);

                // 预检查 DNS，若解析失败则进行重试
                try {
                    String host = new URL(url).getHost();
                    InetAddress.getByName(host);
                } catch (UnknownHostException dnsEx) {
                    lastEx = dnsEx;
                    System.err.println("DNS解析失败: " + dnsEx.getMessage() + "（第" + attempt + "次）");
                    // 进入退避等待后重试
                    throw dnsEx;
                }

                // 执行请求
                final int currentAttempt = attempt;
                String html = client.execute(request, response -> {
                    int statusCode = response.getCode();

                    if (statusCode != 200) {
                        System.err.println("请求失败，状态码: " + statusCode + "（第" + currentAttempt + "次）");
                        return null;
                    }
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
                });

                if (html != null && !html.isEmpty()) {
                    return html;
                } else {
                    // 空响应也触发重试
                    System.err.println("响应为空（第" + attempt + "次），准备重试...");
                }
            } catch (IOException ex) {
                lastEx = ex;
                System.err.println("请求异常（第" + attempt + "次）: " + ex.getMessage());
            }

            // 退避等待
            try {
                Thread.sleep((long) (RETRY_DELAY_SECONDS * 1000L * Math.pow(2, attempt - 1)));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // 达到重试上限
        if (lastEx != null) throw lastEx;
        return null;
    }

    /**
     * 解析HTML并提取符合条件的记录（截止日期之前）
     */
    private List<RulingRecord> parseHtml(String html, LocalDate endDate) {
        List<RulingRecord> records = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        // 检查是否是Angular应用的初始页面
        if (isAngularInitialPage(doc)) {
            System.out.println("检测到Angular初始页面，无法直接解析数据内容");
            System.out.println("建议使用支持JavaScript渲染的工具，如Selenium WebDriver");
            System.out.println("或者尝试直接访问API接口获取数据");
            return records;
        }

        // 尝试多种选择器来定位数据表格
        Elements rows = doc.select("mat-table mat-row");
        if (rows.isEmpty()) {
            rows = doc.select("table tr");
        }
        if (rows.isEmpty()) {
            rows = doc.select(".search-result-flex-container-100-row-wrap mat-row");
        }
        if (rows.isEmpty()) {
            rows = doc.select("[class*='mat-row']");
        }
        if (rows.isEmpty()) {
            rows = doc.select("tr[class*='row']");
        }
        
        if (rows.isEmpty()) {
            System.out.println("未找到数据行，可能的原因：");
            System.out.println("1. 页面需要JavaScript渲染");
            System.out.println("2. 选择器不匹配当前页面结构");
            System.out.println("3. 页面返回了错误信息");
            
            // 输出页面结构以便调试
            System.out.println("页面标题: " + doc.title());
            System.out.println("页面包含的class: " + doc.select("[class]").stream()
                    .limit(10)
                    .map(e -> e.className())
                    .distinct()
                    .toList());
            
            // 尝试查找任何可能包含数据的元素
            Elements allElements = doc.select("*");
            System.out.println("页面总元素数: " + allElements.size());
            
            // 查找包含"ruling"的元素
            Elements rulingElements = doc.select("*:containsOwn(ruling)");
            System.out.println("包含'ruling'的元素数: " + rulingElements.size());
            
            return records;
        }

        System.out.println("找到 " + rows.size() + " 行数据");

        // 遍历行（跳过表头，从第一行数据开始）
        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            RulingRecord record = new RulingRecord();

            try {
                // 1. 提取日期 - 尝试多种选择器
                Element dateElement = row.selectFirst("mat-cell.mat-cell.cdk-cell.cdk-column-date.mat-column-date.ng-star-inserted > font > font");
                if (dateElement == null) {
                    dateElement = row.selectFirst("mat-cell.cdk-column-date font font");
                }
                if (dateElement == null) {
                    dateElement = row.selectFirst("mat-cell.cdk-column-date");
                }
                if (dateElement == null) {
                    dateElement = row.selectFirst("[class*='date']");
                }
                if (dateElement == null) {
                    dateElement = row.selectFirst("td:nth-child(1)");
                }
                if (dateElement == null) {
                    dateElement = row.selectFirst("td");
                }
                record.date = dateElement != null ? dateElement.text().trim() : "未知日期";

                // 2. 过滤：只保留截止日期之前的数据
                if (!isDateValid(record.date, endDate)) {
                    System.out.println("跳过日期不符合的记录: " + record.date);
                    continue; // 日期无效或超过截止日期，跳过
                }

                // 3. 提取代码 - 尝试多种选择器
                Element codeElement = row.selectFirst("mat-cell.mat-cell.cdk-cell.cdk-column-category.mat-column-category.ng-star-inserted > div > div:nth-child(3)");
                if (codeElement == null) {
                    codeElement = row.selectFirst("mat-cell.cdk-column-category div div:nth-child(3)");
                }
                if (codeElement == null) {
                    codeElement = row.selectFirst("mat-cell.cdk-column-category");
                }
                if (codeElement == null) {
                    codeElement = row.selectFirst("[class*='category']");
                }
                if (codeElement == null) {
                    codeElement = row.selectFirst("td:nth-child(2)");
                }
                if (codeElement == null) {
                    codeElement = row.selectFirst("td:nth-child(1)");
                }
                record.code = codeElement != null ? codeElement.text().trim().replaceAll("\\s+", " ") : "未知代码";

                // 4. 提取描述和URL - 查找包含href="/ruling/"的链接
                Element linkElement = row.selectFirst("a[href^=/ruling/]");
                if (linkElement == null) {
                    linkElement = row.selectFirst("a[href*='ruling']");
                }
                if (linkElement == null) {
                    linkElement = row.selectFirst("a");
                }
                
                if (linkElement != null) {
                    record.description = linkElement.text().trim();
                    String href = linkElement.attr("href");
                    if (href.startsWith("/ruling/")) {
                        record.url = "https://rulings.cbp.gov" + href;
                    } else if (href.startsWith("http")) {
                        record.url = href;
                    } else {
                        record.url = "https://rulings.cbp.gov" + href;
                    }
                } else {
                    // 尝试从其他元素获取描述
                    Element descElement = row.selectFirst("[class*='description']");
                    if (descElement == null) {
                        descElement = row.selectFirst("td:nth-child(3)");
                    }
                    if (descElement == null) {
                        descElement = row.selectFirst("td:nth-child(2)");
                    }
                    record.description = descElement != null ? descElement.text().trim() : "未知描述";
                    record.url = "无URL";
                }

                records.add(record);
                System.out.println("成功解析第 " + (i + 1) + " 行: " + record.date + " - " + record.code);

            } catch (Exception e) {
                System.err.println("解析第 " + (i + 1) + " 行时出错: " + e.getMessage());
                continue;
            }
        }

        return records;
    }

    /**
     * 检查是否是Angular应用的初始页面
     */
    private boolean isAngularInitialPage(Document doc) {
        // 检查是否包含Angular应用的典型特征
        boolean hasAppRoot = doc.select("app-root").size() > 0;
        boolean hasAngularScripts = doc.select("script[src*='angular']").size() > 0;
        boolean hasMatTable = doc.select("mat-table").size() == 0; // 没有实际的数据表格
        
        return hasAppRoot && hasAngularScripts && hasMatTable;
    }

    /**
     * 验证日期是否有效且在截止日期之前
     */
    private boolean isDateValid(String dateStr, LocalDate endDate) {
        try {
            if (dateStr == null || dateStr.isEmpty()) return false;
            LocalDate recordDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            // 记录日期 <= 截止日期
            return !recordDate.isAfter(endDate);
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

    // 主方法：测试爬虫
    public static void main(String[] args) {
        CBPRulingCrawler crawler = new CBPRulingCrawler();
        // 爬取关键词"9018"、每页30条、最多爬取5页、截止日期2025-06-01之前的数据
        crawler.crawl("9018", 30, 5, LocalDate.of(2025, 6, 1));
    }
}