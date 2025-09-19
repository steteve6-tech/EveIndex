// OtherSiteCrawler.java
// 示例：爬取 UL Taiwan GMA 页面，获取国家、类型、标题、链接
// 依赖 Jsoup 库
package com.certification;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ULSolutionsCrawler {
    static class BlogContent {
        String title;
        String url;
        String summary;
        
        public BlogContent(String title, String url, String summary) {
            this.title = title;
            this.url = url;
            this.summary = summary;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getSummary() { return summary; }
        
        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setUrl(String url) { this.url = url; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    /**
     * 爬取 UL Taiwan GMA 页面，获取国家、类型、标题、链接（包括隐藏的 <ul><li> 标签）
     *
     * @param totalCount 需要爬取的内容总数（可设置为 Integer.MAX_VALUE 获取全部）
     * @return 内容列表
     * @throws Exception 网络异常或解析异常
     */
    public List<BlogContent> crawl(int totalCount) throws Exception {
        List<BlogContent> result = new ArrayList<>();
        String url = "https://taiwan.ul.com/gma/";
        Document doc = Jsoup.connect(url).get();
        Elements parentContainer = doc.select("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");
        // 选取所有 <li> 标签，筛选包含指定结构的内容（显式标签）
        Elements items = parentContainer.select("li:has(strong a)");
        for (Element item : items) {
            // 类型
            String type = "";
            int pipeIdx = item.html().indexOf("|");
            int brIdx = item.html().indexOf("<br>");
            if (pipeIdx != -1 && brIdx != -1 && brIdx > pipeIdx) {
                type = item.html().substring(pipeIdx, brIdx).replace("|", "").trim();
            }
            // 国家
            Element strongEl = item.selectFirst("strong");
            String country = "";
            if (strongEl != null) {
                String strongText = strongEl.text();
                int leftBracket = strongText.indexOf("[");
                int rightBracket = strongText.indexOf("]");
                if (leftBracket != -1 && rightBracket != -1 && rightBracket > leftBracket) {
                    country = strongText.substring(leftBracket + 1, rightBracket).trim();
                }
            }
            // 标题和链接
            Element aEl = item.selectFirst("strong a");
            String title = aEl != null ? aEl.text() : "";
            String link = aEl != null ? aEl.attr("href") : "";
            result.add(new BlogContent(title, link, country + ", " + type));

            SpecificContentCrawler specificContentCrawler=new SpecificContentCrawler();
            specificContentCrawler.crawlSpecificContent(link); // 爬取具体内容
            if (result.size() >= totalCount) break;
        }
        // 选取所有隐藏的 <ul><li> 标签（无 strong，仅 [国家] <a>）
        Elements hiddenItems = parentContainer.select("ul > li:has(a)");
        for (Element item : hiddenItems) {
            // 国家
            String country = "";
            String html = item.html();
            int leftBracket = html.indexOf("[");
            int rightBracket = html.indexOf("]");
            if (leftBracket != -1 && rightBracket != -1 && rightBracket > leftBracket) {
                country = html.substring(leftBracket + 1, rightBracket).trim();
            }
            // 类型（无类型信息，设为空）
            String type = "";
            // 标题和链接
            Element aEl = item.selectFirst("a");
            String title = aEl != null ? aEl.text() : "";
            String link = aEl != null ? aEl.attr("href") : "";
            result.add(new BlogContent(title, link, country + ", " + type));
            if (result.size() >= totalCount) break;
        }
        return result;
    }
    public class SpecificContentCrawler {
        // 日期正则表达式
        private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4} 年 \\d{1,2} 月 \\d{1,2} 日");

        /**
         * 爬取指定内容
         *
         * @param url 目标网页URL
         * @return 爬取结果的Map
         * @throws IOException 网络异常或解析异常
         */
        public static Map<String, Object> crawlSpecificContent(String url) throws IOException {
            // 发送请求获取文档
            Document doc = Jsoup.connect(url).get();
            System.out.println("开始爬取: " + url);

            // 获取父容器
            Element parentContainer = doc.selectFirst("body > div.main.ul-responsive > div > div > div.col-xs-12.col-sm-8");

            if (parentContainer == null) {
                System.out.println("未找到父容器元素");
                return null;
            }

            // 存储爬取结果
            Map<String, Object> result = new HashMap<>();

            // 1. 爬取时间信息 (blockquote > p)
            Element timeElement = parentContainer.selectFirst("blockquote > p");
            if (timeElement != null) {
                String timeText = timeElement.text().trim();
                // 提取日期
                Matcher matcher = DATE_PATTERN.matcher(timeText);
                if (matcher.find()) {
                    result.put("发布时间", matcher.group());
                } else {
                    result.put("发布时间", timeText);
                }
            } else {
                result.put("发布时间", "未找到时间信息");
            }
            System.out.println("发布时间: " + result.get("发布时间"));// 输出发布时间调试信息

            // 2. 爬取p:nth-child(5)的内容
            Element p5Element = parentContainer.selectFirst("p:nth-child(5)");
            if (p5Element != null) {
                result.put("段落5", p5Element.text().trim());
            } else {
                result.put("段落5", "未找到第5个段落");
            }
            System.out.println("段落5: " + result.get("段落5"));// 输出段落5调试信息
            System.exit(0);
            // 3. 判断table是否存在
            Element tableElement = parentContainer.selectFirst("table");
            result.put("表格存在", tableElement != null);

            // 如果表格存在，可以在这里调用表格解析程序
            if (tableElement != null) {
                result.put("表格内容", "表格存在，待解析");
                // 这里可以添加表格解析代码
                // parseTable(tableElement);
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

        // 打印结果的辅助方法
        private static void printResult(Map<String, Object> result, int indent) {
            String indentStr = " ".repeat(indent);

            for (Map.Entry<String, Object> entry : result.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Map) {
                    System.out.println(indentStr + key + ":");
                    printResult((Map<String, Object>) value, indent + 2);
                } else {
                    System.out.println(indentStr + key + ": " + value);
                }
            }
            System.out.println("  ");
            System.exit(0);
        }

        // 表格解析方法（待实现）
        private static void parseTable(Element tableElement) {
            // 表格解析逻辑将在这里实现
        }
    }
    /**
     * 保存爬取结果到CSV文件
     *
     * @param contents 爬取结果列表
     * @param filePath 保存路径
     * @throws Exception 文件写入异常
     */
    public void saveToCsv(List<BlogContent> contents, String filePath) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("country,type,title,url");
        for (BlogContent c : contents) {
            String[] arr = c.summary.split(",");
            String country = arr.length > 0 ? arr[0].trim() : "";
            String type = arr.length > 1 ? arr[1].trim() : "";
            lines.add(String.format("\"%s\",\"%s\",\"%s\",\"%s\"", country, type, c.title, c.url));
        }
        Files.write(Path.of(filePath), lines);
    }

    public static void main(String[] args) {
        System.out.println("OtherSiteCrawler HelloWorld");
        ULSolutionsCrawler crawler = new ULSolutionsCrawler();
        System.out.println("开始爬取 UL Taiwan GMA 页面");
        try {
            List<BlogContent> contents = crawler.crawl(12); // 获取全部内容
            crawler.saveToCsv(contents, "ul-gma-results.csv"); // 保存到csv
            System.out.println("已保存到 ul-gma-results.csv");
        } catch (Exception e) {
            System.err.println("执行出错: " + e.getMessage());
        }
    }
}
