package com.certification.crawler.countrydata.us;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.certification.entity.common.DeviceEventReport;
import com.certification.repository.common.DeviceEventReportRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FDA MAUDE事件爬虫类 - 基于HTTP请求的爬虫实现
 * 参照D_510K的实现方式
 */
@Slf4j
@Component
public class D_event {

    // -------------------------- 1. 初始化配置 --------------------------
    private static final String BASE_URL = "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfmaude/results.cfm";
    private static final String OUTPUT_CSV = "FDA_MAUDE_Results_Java.csv";
    private static final int MAX_PAGES = 10; // 最大爬取页数（0表示爬到最后一页）
    private static final int DELAY_MS = 2000; // 每页延迟（毫秒）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final int BATCH_SIZE = 20; // 每批保存的数据量
    
    // 重试机制配置
    private static final int MAX_RETRY_ATTEMPTS = 5; // 最大重试次数
    private static final int RETRY_DELAY_MS = 5000; // 重试延迟（毫秒）
    private static final int TIMEOUT_MS = 15000; // 请求超时时间（毫秒）

    // 搜索筛选条件
    private final Map<String, String> searchParams = new HashMap<>();
    
    @Autowired
    private DeviceEventReportRepository deviceEventReportRepository;

    /**
     * 执行FDA MAUDE数据爬取（带参数化搜索）
     */
    public Map<String, Object> crawlMAUDEDataWithParams(String brandName, String manufacturer, String modelNumber, String dateFrom, String dateTo, Integer maxPages) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int currentPage = 1;
        int totalSaved = 0;
        int totalSkipped = 0;
        int actualMaxPages = (maxPages != null && maxPages > 0) ? maxPages : MAX_PAGES;
        int consecutiveEmptyPages = 0; // 连续无结果页面计数器

        log.info("开始爬取FDA MAUDE数据（参数化搜索）...");
        log.info("品牌名称: {}", brandName);
        log.info("制造商: {}", manufacturer);
        log.info("型号: {}", modelNumber);
        log.info("日期范围: {} - {}", dateFrom, dateTo);
        log.info("最大页数: {}", actualMaxPages);

        // 初始化搜索参数
        initSearchParamsWithModel("", manufacturer, brandName, modelNumber, dateFrom, dateTo);

        while (true) {
            boolean pageSuccess = false;
            int retryCount = 0;
            
            // 重试机制：最多重试5次
            while (retryCount < MAX_RETRY_ATTEMPTS && !pageSuccess) {
                try {
                    log.info("爬取第 {} 页... (尝试 {}/{})", currentPage, retryCount + 1, MAX_RETRY_ATTEMPTS);

                    // 发送请求并解析结果
                    Map<String, Object> pageResult = crawlPage(currentPage);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> pageResults = (List<Map<String, Object>>) pageResult.get("pageResults");
                    boolean isLastPage = (Boolean) pageResult.get("isLastPage");

                    if (pageResults.isEmpty()) {
                        consecutiveEmptyPages++;
                        log.info("第 {} 页无结果，连续无结果页面数: {}", currentPage, consecutiveEmptyPages);
                        
                        // 如果连续两次页面没有找到结果行，停止爬取
                        if (consecutiveEmptyPages >= 2) {
                            log.info("连续 {} 次页面无结果，停止爬取", consecutiveEmptyPages);
                            pageSuccess = true;
                            break; // 跳出重试循环
                        }
                        
                        // 如果只是第一次无结果，继续下一页
                        pageSuccess = true;
                        break; // 跳出重试循环，继续下一页
                    } else {
                        // 有结果时重置连续无结果页面计数器
                        consecutiveEmptyPages = 0;
                    }

                    allResults.addAll(pageResults);
                    log.info("第 {} 页解析完成，获取 {} 条记录", currentPage, pageResults.size());

                    // 每20条数据保存一次到数据库
                    if (allResults.size() >= BATCH_SIZE) {
                        int[] result = saveBatchToDatabase(allResults);
                        totalSaved += result[0];
                        totalSkipped += result[1];
                        
                        // 如果这一批数据全部都是重复的，停止爬取
                        if (result[0] == 0 && result[1] > 0) {
                            log.info("第 {} 页批次数据全部重复，停止爬取", currentPage);
                            pageSuccess = true;
                            break;
                        }
                        
                        allResults.clear(); // 清空已保存的数据
                    }

                    // 如果获取的记录数少于500条，说明已经到达最后一页
                    if (pageResults.size() < 500) {
                        log.info("第 {} 页获取记录数 {} 条，少于500条，已到达最后一页，停止爬取", currentPage, pageResults.size());
                        pageSuccess = true;
                        break;
                    }
                    
                    // 如果检测到是最后一页，也停止爬取
                    if (isLastPage) {
                        log.info("第 {} 页检测到是最后一页，停止爬取", currentPage);
                        pageSuccess = true;
                        break;
                    }
                    
                    pageSuccess = true; // 成功完成

                } catch (Exception e) {
                    retryCount++;
                    String errorType = getErrorType(e);
                    
                    if (retryCount >= MAX_RETRY_ATTEMPTS) {
                        log.error("第 {} 页爬取失败，已达到最大重试次数 {} 次，跳过该页。错误类型: {}, 错误信息: {}", 
                                currentPage, MAX_RETRY_ATTEMPTS, errorType, e.getMessage());
                        pageSuccess = true; // 标记为成功，跳过该页继续下一页
                        break;
                    } else {
                        log.warn("第 {} 页爬取失败 (尝试 {}/{}): {} - {}，{}秒后重试...", 
                                currentPage, retryCount, MAX_RETRY_ATTEMPTS, errorType, e.getMessage(), RETRY_DELAY_MS / 1000);
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("线程被中断，停止爬取");
                            break;
                        }
                    }
                }
            }
            
            // 如果页面处理成功或已达到最大重试次数，继续下一页
            if (pageSuccess) {
                // 检查连续无结果页面是否达到停止阈值
                if (consecutiveEmptyPages >= 2) {
                    log.info("连续 {} 次页面无结果，停止整个爬取过程", consecutiveEmptyPages);
                    break;
                }
                
                // 控制分页
                currentPage++;
                
                // 停止条件
                if (actualMaxPages != 0 && currentPage > actualMaxPages) {
                    log.info("已爬取到最大页数 {}，停止爬取", actualMaxPages);
                    break;
                }

                // 延迟
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                log.error("第 {} 页处理失败，停止爬取", currentPage);
                break;
            }
        }

        // 保存剩余的数据
        if (!allResults.isEmpty()) {
            int[] result = saveBatchToDatabase(allResults);
            totalSaved += result[0];
            totalSkipped += result[1];
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("totalPages", currentPage - 1);
        result.put("results", allResults);
        
        log.info("爬取完成！共保存 {} 条记录，跳过 {} 条重复记录", totalSaved, totalSkipped);
        return result;
    }

    /**
     * 执行FDA MAUDE数据爬取（保持向后兼容）
     */
    public Map<String, Object> crawlMAUDEData(String deviceName, String manufacturer, String brandName, String dateFrom, String dateTo) {


        return crawlMAUDEDataWithParams(brandName, manufacturer, "", dateFrom, dateTo, MAX_PAGES);
    }

    /**
     * 初始化搜索参数（带型号支持）
     */
    private void initSearchParamsWithModel(String deviceName, String manufacturer, String brandName, String modelNumber, String dateFrom, String dateTo) {
        searchParams.clear(); // 清空之前的参数
        
        // 根据浏览器请求格式设置参数
        searchParams.put("SearchString", ""); // 对应浏览器中的SearchString
        searchParams.put("SearchYear", ""); // 对应浏览器中的SearchYear
        searchParams.put("ProductProblem", ""); // 对应浏览器中的ProductProblem
        searchParams.put("DeviceName", deviceName); // 对应浏览器中的DeviceName
        searchParams.put("EventType", ""); // 对应浏览器中的EventType
        searchParams.put("Manufacturer", manufacturer); // 对应浏览器中的Manufacturer
        searchParams.put("ModelNumber", modelNumber); // 对应浏览器中的ModelNumber
        searchParams.put("ReportNumber", ""); // 对应浏览器中的ReportNumber
        searchParams.put("BrandName", brandName); // 对应浏览器中的BrandName
        searchParams.put("ProductCode", ""); // 对应浏览器中的ProductCode
        searchParams.put("SummaryReport", ""); // 对应浏览器中的SummaryReport
        searchParams.put("exemptionNumber", ""); // 对应浏览器中的exemptionNumber
        searchParams.put("UDIDI", ""); // 对应浏览器中的UDIDI
        searchParams.put("ReportDateFrom", dateFrom); // 对应浏览器中的ReportDateFrom
        searchParams.put("ReportDateTo", dateTo); // 对应浏览器中的ReportDateTo
        searchParams.put("PMAPMNNUM", ""); // 对应浏览器中的PMAPMNNUM
        searchParams.put("pagenum", "10"); // 对应浏览器中的pagenum，设置为10条记录每页

        log.info("搜索参数初始化完成（带型号支持）");
        log.info("设备名称关键词: {}", searchParams.get("DeviceName"));
        log.info("制造商: {}", searchParams.get("Manufacturer").isEmpty() ? "不限" : searchParams.get("Manufacturer"));
        log.info("品牌名: {}", searchParams.get("BrandName").isEmpty() ? "不限" : searchParams.get("BrandName"));
        log.info("型号: {}", searchParams.get("ModelNumber").isEmpty() ? "不限" : searchParams.get("ModelNumber"));
        log.info("日期范围: {} - {}", searchParams.get("ReportDateFrom"), searchParams.get("ReportDateTo"));
        log.info("每页记录数: {}", searchParams.get("pagenum"));
    }

    /**
     * 初始化搜索参数（保持向后兼容）
     * 基于URL: https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfmaude/results.cfm?start_search=1&productcode=&productproblem=&patientproblem=&devicename=&modelNumber=&exemptionNumber=&reportNumber=&PMAPMNNUM=&UDIDI=&manufacturer=&brandname=visia&eventtype=&summaryreport=&reportdatefrom=08%2F01%2F2025&reportdateto=08%2F29%2F2025&pagenum=10
     */
    private void initSearchParams(String deviceName, String manufacturer, String brandName, String dateFrom, String dateTo) {
        searchParams.clear(); // 清空之前的参数
        searchParams.put("start_search", "1");
        searchParams.put("productcode", "");
        searchParams.put("productproblem", "");
        searchParams.put("patientproblem", "");
        searchParams.put("devicename", deviceName);
        searchParams.put("modelNumber", "");
        searchParams.put("exemptionNumber", "");
        searchParams.put("reportNumber", "");
        searchParams.put("PMAPMNNUM", "");
        searchParams.put("UDIDI", "");
        searchParams.put("manufacturer", manufacturer);
        searchParams.put("brandname", brandName);
        searchParams.put("eventtype", "");
        searchParams.put("summaryreport", "");
        searchParams.put("reportdatefrom", dateFrom);
        searchParams.put("reportdateto", dateTo);
        searchParams.put("pagenum", "500"); // 设置为500条记录每页，参考D_510K

        log.info("搜索参数初始化完成");
        log.info("设备名称关键词: {}", searchParams.get("devicename"));
        log.info("制造商: {}", searchParams.get("manufacturer").isEmpty() ? "不限" : searchParams.get("manufacturer"));
        log.info("品牌名: {}", searchParams.get("brandname").isEmpty() ? "不限" : searchParams.get("brandname"));
        log.info("日期范围: {} - {}", searchParams.get("reportdatefrom"), searchParams.get("reportdateto"));
        log.info("每页记录数: {}", searchParams.get("pagenum"));
    }

    /**
     * 爬取单页数据
     * @return 包含页面数据和是否到达最后一页标志的Map
     */
    private Map<String, Object> crawlPage(int pageNum) throws IOException {
        List<Map<String, Object>> pageResults = new ArrayList<>();

        // 使用POST请求，模拟浏览器行为
        String url = BASE_URL;
        log.info("请求URL: {}", url);
        log.info("请求方法: POST");

        Connection.Response response = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cache-Control", "max-age=0")
                .header("Referer", "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfmaude/search.cfm")
                .header("Origin", "https://www.accessdata.fda.gov")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .data(buildPostData(pageNum))
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .execute();

        // 解析HTML
        Document doc = response.parse();


        // 检查是否返回错误页面
        if (doc.title().contains("Error") || doc.title().contains("Temporarily Unavailable")) {
            log.error("FDA网站返回错误页面: {}", doc.title());
            log.error("页面内容: {}", doc.select("h1, h2").text());
            Map<String, Object> result = new HashMap<>();
            result.put("pageResults", pageResults);
            result.put("isLastPage", true);
            return result;
        }

        // 调试：打印页面标题和部分内容
        log.info("页面标题: {}", doc.title());
        log.debug("页面内容长度: {} 字符", doc.html().length());
        
        // 检查页面是否包含"没有找到结果"的提示
        String pageText = doc.text().toLowerCase();
        if (pageText.contains("no results found") || pageText.contains("no records found") || 
            pageText.contains("没有找到结果") || pageText.contains("未找到记录") ||
            pageText.contains("no records meeting your search criteria")) {
            log.info("第 {} 页明确显示无结果", pageNum);
            Map<String, Object> result = new HashMap<>();
            result.put("pageResults", pageResults);
            result.put("isLastPage", true);
            return result;
        }
        
        // 检查分页信息，判断是否到达最后一页
        Elements paginationInfo = doc.select("td[style*='font-size:8pt']");
        if (!paginationInfo.isEmpty()) {
            String paginationText = paginationInfo.text();
            log.debug("分页信息: {}", paginationText);
            
            // 如果包含"records meeting your search criteria returned"，说明有总记录数信息
            if (paginationText.contains("records meeting your search criteria returned")) {
                log.info("找到总记录数信息: {}", paginationText);
            }
        }
        
        // 检查是否有"Next"按钮，如果没有说明到达最后一页
        Elements nextButton = doc.select("a[title='Next']");
        if (nextButton.isEmpty()) {
            log.debug("第 {} 页未找到Next按钮，可能已到达最后一页", pageNum);
        }

        // 根据实际HTML结构，找到结果表格中的数据行
        // MAUDE数据库使用特定的表格结构：bgcolor="#ffffff" 和 bgcolor="#f9f9f3" 交替
        Elements resultRows = doc.select("tr[bgcolor='#ffffff'], tr[bgcolor='#f9f9f3']");

        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试MAUDE特定的选择器", pageNum);
            // MAUDE特定的选择器：查找包含品牌名称链接的行
            resultRows = doc.select("tr:has(td a[href*='detail.cfm?mdrfoi__id='])");
        }

        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试更宽泛的选择器", pageNum);
            // 更宽泛的选择器：查找所有包含td的行
            resultRows = doc.select("tr:has(td)");
        }

        // 过滤掉表头行和空行
        List<Element> filteredRows = resultRows.stream()
                .filter(row -> {
                    Elements tds = row.select("td");
                    if (tds.size() < 3) return false; // MAUDE表格有3列：制造商、品牌名、日期
                    
                    // 检查是否包含品牌名称链接（MAUDE特有的链接格式）
                    Elements links = row.select("td a[href*='detail.cfm?mdrfoi__id=']");
                    return !links.isEmpty();
                })
                .collect(java.util.stream.Collectors.toList());

        // 将List<Element>转换为Elements
        resultRows = new Elements(filteredRows);

        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行", pageNum);
            Map<String, Object> result = new HashMap<>();
            result.put("pageResults", pageResults);
            result.put("isLastPage", true);
            return result;
        }

        // 提取每行数据
        for (Element row : resultRows) {
            Elements cols = row.select("td");
            if (cols.size() < 3) { // MAUDE表格有3列：制造商、品牌名、日期
                continue;
            }

            try {
                Map<String, Object> record = parseRowData(cols, pageNum);
                if (record != null) {
                    pageResults.add(record);
                }
            } catch (Exception e) {
                log.warn("解析行数据失败: {}", e.getMessage());
            }
        }

        // 检查是否到达最后一页（参考D_510K的停止逻辑）
        boolean isLastPage = pageResults.isEmpty() || 
                           pageResults.size() < 500 || 
                           nextButton.isEmpty();
        
        Map<String, Object> result = new HashMap<>();
        result.put("pageResults", pageResults);
        result.put("isLastPage", isLastPage);
        
        return result;
    }

    /**
     * 构建POST请求的表单数据
     */
    private Map<String, String> buildPostData(int pageNum) {
        Map<String, String> postData = new HashMap<>();
        
        // 复制搜索参数到POST数据
        for (Map.Entry<String, String> entry : searchParams.entrySet()) {
            postData.put(entry.getKey(), entry.getValue());
        }
        
        // 添加必要的表单字段
        postData.put("Search", "Search"); // 搜索按钮
        
        log.debug("POST数据: {}", postData);
        return postData;
    }

    /**
     * 构建GET请求URL（保留作为备用方法）
     */
    private String buildGetUrl(int pageNum) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?");

        // 设置分页参数
        int startSearch = (pageNum - 1) * 500 + 1; // 每页500条记录，计算起始位置
        searchParams.put("start_search", String.valueOf(startSearch));

        for (Map.Entry<String, String> entry : searchParams.entrySet()) {
            if (url.charAt(url.length() - 1) != '?') {
                url.append("&");
            }
            url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    /**
     * 解析行数据
     * 根据MAUDE数据库的HTML结构解析字段
     * 需要根据实际页面结构调整字段映射
     */
    private Map<String, Object> parseRowData(Elements cols, int pageNum) {
        try {
            Map<String, Object> record = new HashMap<>();

            if (cols.size() < 3) {
                log.warn("列数不足，跳过该行");
                return null;
            }

            // 提取字段（根据MAUDE数据库的实际HTML结构）
            // 第0列：制造商
            String manufacturer = cols.get(0).text().trim();

            // 第1列：品牌名（带链接）
            String brandName = "";
            String deviceUrl = "";
            Element brandNameLink = cols.get(1).select("a").first();
            if (brandNameLink != null) {
                brandName = brandNameLink.text().trim();
                deviceUrl = brandNameLink.attr("href");
                if (!deviceUrl.startsWith("http")) {
                    deviceUrl = "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfmaude/" + deviceUrl;
                }
            } else {
                brandName = cols.get(1).text().trim();
            }

            // 第2列：报告日期
            String reportDateStr = cols.get(2).text().trim();

            // 设置记录字段
            record.put("manufacturer", manufacturer);
            record.put("brandName", brandName);
            record.put("deviceUrl", deviceUrl);
            record.put("reportDateStr", reportDateStr);
            record.put("deviceName", brandName); // 品牌名作为设备名称

            // 解析日期
            if (!reportDateStr.isEmpty()) {
                try {
                    LocalDate reportDate = LocalDate.parse(reportDateStr, DATE_FORMATTER);
                    record.put("reportDate", reportDate);
                } catch (Exception e) {
                    log.warn("日期解析失败: {}", reportDateStr);
                    record.put("reportDate", null);
                }
            } else {
                record.put("reportDate", null);
            }

            // 设置其他字段
            record.put("dataSource", "FDA MAUDE Database");
            record.put("countryCode", "US");
            record.put("jdCountry", "US");
            record.put("crawlTime", LocalDateTime.now());
            record.put("dataStatus", "ACTIVE");

            log.debug("解析记录: 制造商={}, 品牌名={}, 日期={}, 设备URL={}",
                    manufacturer, brandName, reportDateStr, deviceUrl);

            return record;

        } catch (Exception e) {
            log.error("解析行数据时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 批量保存数据到CSV文件
     */
    private void saveBatchToCSV(List<Map<String, Object>> batchData) {
        try {
            // 确保输出目录存在
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("crawler_output"));
            String filePath = "crawler_output/" + OUTPUT_CSV;

            // 如果文件不存在，先写入CSV头部
            boolean fileExists = java.nio.file.Files.exists(java.nio.file.Paths.get(filePath));

            try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8, true)) {
                // 如果文件不存在，写入CSV头部
                if (!fileExists) {
                    writer.write("制造商,品牌名,设备名称,报告日期,设备详情链接,数据来源,国家代码,来源国家,爬取时间,数据状态\n");
                }

                // 写入数据行
                for (Map<String, Object> record : batchData) {
                    String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            escapeCsvField((String) record.get("manufacturer")),
                            escapeCsvField((String) record.get("brandName")),
                            escapeCsvField((String) record.get("deviceName")),
                            record.get("reportDate") != null ? ((LocalDate) record.get("reportDate")).format(DATE_FORMATTER) : "",
                            escapeCsvField((String) record.get("deviceUrl")),
                            escapeCsvField((String) record.get("dataSource")),
                            escapeCsvField((String) record.get("countryCode")),
                            escapeCsvField((String) record.get("jdCountry")),
                            record.get("crawlTime") != null ? record.get("crawlTime").toString() : "",
                            escapeCsvField((String) record.get("dataStatus"))
                    );
                    writer.write(line);
                }
            }

            log.info("批量CSV保存完成，已保存 {} 条记录到 {}", batchData.size(), filePath);

        } catch (IOException e) {
            log.error("批量保存CSV文件失败: {}", e.getMessage());
        }
    }

    /**
     * 保存结果到CSV文件
     */
    private void saveToCSV(List<Map<String, Object>> results) {
        if (results.isEmpty()) {
            log.warn("未获取到任何结果，请检查筛选条件！");
            return;
        }

        try {
            // 确保输出目录存在
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("crawler_output"));
            String filePath = "crawler_output/" + OUTPUT_CSV;

            try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
                // 写入CSV头部
                writer.write("制造商,品牌名,设备名称,报告日期,设备详情链接,数据来源,国家代码,来源国家,爬取时间,数据状态\n");

                // 写入数据行
                for (Map<String, Object> record : results) {
                    String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            escapeCsvField((String) record.get("manufacturer")),
                            escapeCsvField((String) record.get("brandName")),
                            escapeCsvField((String) record.get("deviceName")),
                            record.get("reportDate") != null ? ((LocalDate) record.get("reportDate")).format(DATE_FORMATTER) : "",
                            escapeCsvField((String) record.get("deviceUrl")),
                            escapeCsvField((String) record.get("dataSource")),
                            escapeCsvField((String) record.get("countryCode")),
                            escapeCsvField((String) record.get("jdCountry")),
                            record.get("crawlTime") != null ? record.get("crawlTime").toString() : "",
                            escapeCsvField((String) record.get("dataStatus"))
                    );
                    writer.write(line);
                }
            }

            log.info("CSV文件保存完成，已保存到 {}", filePath);

        } catch (IOException e) {
            log.error("保存CSV文件失败: {}", e.getMessage());
        }
    }

    /**
     * 将爬取的数据转换为DeviceEventReport实体
     */
    private DeviceEventReport convertToDeviceEventReport(Map<String, Object> data) {
        DeviceEventReport report = new DeviceEventReport();
        
        // 从设备URL中提取报告编号
        String deviceUrl = (String) data.get("deviceUrl");
        String reportNumber = extractReportNumberFromUrl(deviceUrl);
        report.setReportNumber(reportNumber);
        
        // 设置基本信息
        report.setBrandName((String) data.get("brandName"));
        report.setGenericName((String) data.get("deviceName"));
        report.setManufacturerName((String) data.get("manufacturer"));
        
        // 设置日期
        if (data.get("reportDate") != null) {
            report.setDateReceived((LocalDate) data.get("reportDate"));
        }
        
        // 设置数据源和国家
        report.setDataSource("FDA MAUDE Database");
        report.setJdCountry("US");
        
        // 设置默认值
        report.setEventType("Adverse Event");
        report.setDeviceClass("Unknown");
        report.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.MEDIUM);
        
        return report;
    }
    
    /**
     * 从URL中提取报告编号
     */
    private String extractReportNumberFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "UNKNOWN_" + System.currentTimeMillis();
        }
        
        // 从URL中提取mdrfoi__id参数
        if (url.contains("mdrfoi__id=")) {
            String[] parts = url.split("mdrfoi__id=");
            if (parts.length > 1) {
                String idPart = parts[1].split("&")[0];
                return "MAUDE_" + idPart;
            }
        }
        
        return "UNKNOWN_" + System.currentTimeMillis();
    }
    
    /**
     * 批量保存数据到数据库
     */
    private int[] saveBatchToDatabase(List<Map<String, Object>> batchData) {
        int saved = 0;
        int skipped = 0;
        
        for (Map<String, Object> data : batchData) {
            try {
                DeviceEventReport report = convertToDeviceEventReport(data);
                
                // 检查是否已存在（基于报告编号）
                if (deviceEventReportRepository.findByReportNumber(report.getReportNumber()).isPresent()) {
                    log.debug("报告编号 {} 已存在，跳过保存", report.getReportNumber());
                    skipped++;
                    continue;
                }
                
                // 保存到数据库
                deviceEventReportRepository.save(report);
                saved++;
                log.debug("成功保存报告: {}", report.getReportNumber());
                
            } catch (Exception e) {
                log.error("保存报告失败: {}", e.getMessage());
                skipped++;
            }
        }
        
        log.info("批量保存完成: 保存 {} 条，跳过 {} 条", saved, skipped);
        return new int[]{saved, skipped};
    }
    
    /**
     * 转义CSV字段中的特殊字符
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // 将双引号替换为两个双引号，这是CSV标准
        return field.replace("\"", "\"\"");
    }


    /**
     * 快速测试方法 - 测试基本功能
     */
    public void quickTest() {
        System.out.println("=== 快速测试 FDA MAUDE 爬虫 ===");

        try {
            // 使用简单的测试参数
            Map<String, Object> result = crawlMAUDEData(
                    "monitor",      // 设备名称关键词
                    "",             // 制造商（不限）
                    "",             // 品牌名（不限）
                    "01/01/2024",   // 开始日期
                    "12/31/2024"    // 结束日期
            );

            System.out.println("快速测试结果:");
            System.out.println("  保存记录: " + result.get("totalSaved"));
            System.out.println("  跳过记录: " + result.get("totalSkipped"));
            System.out.println("  爬取页数: " + result.get("totalPages"));

        } catch (Exception e) {
            System.err.println("快速测试失败: " + e.getMessage());
        }
    }

    /**
     * 压力测试方法 - 测试大量数据爬取
     */
    public void stressTest() {
        System.out.println("=== 压力测试 FDA MAUDE 爬虫 ===");

        try {
            // 使用较宽泛的搜索条件
            Map<String, Object> result = crawlMAUDEData(
                    "",             // 设备名称（不限）
                    "",             // 制造商（不限）
                    "",             // 品牌名（不限）
                    "01/01/2020",   // 开始日期（较长时间范围）
                    "12/31/2024"    // 结束日期
            );

            System.out.println("压力测试结果:");
            System.out.println("  保存记录: " + result.get("totalSaved"));
            System.out.println("  跳过记录: " + result.get("totalSkipped"));
            System.out.println("  爬取页数: " + result.get("totalPages"));

        } catch (Exception e) {
            System.err.println("压力测试失败: " + e.getMessage());
        }
    }

    /**
     * 特定制造商测试方法
     */
    public void testSpecificManufacturer(String manufacturerName) {
        System.out.println("=== 特定制造商测试: " + manufacturerName + " ===");

        try {
            Map<String, Object> result = crawlMAUDEData(
                    "",             // 设备名称（不限）
                    manufacturerName, // 指定制造商
                    "",             // 品牌名（不限）
                    "01/01/2023",   // 开始日期
                    "12/31/2024"    // 结束日期
            );

            System.out.println("制造商测试结果:");
            System.out.println("  制造商: " + manufacturerName);
            System.out.println("  保存记录: " + result.get("totalSaved"));
            System.out.println("  跳过记录: " + result.get("totalSkipped"));
            System.out.println("  爬取页数: " + result.get("totalPages"));

        } catch (Exception e) {
            System.err.println("制造商测试失败: " + e.getMessage());
        }
    }

    /**
     * 特定品牌测试方法
     */
    public void testSpecificBrand(String brandName) {
        System.out.println("=== 特定品牌测试: " + brandName + " ===");

        try {
            Map<String, Object> result = crawlMAUDEData(
                    "",             // 设备名称（不限）
                    "",             // 制造商（不限）
                    brandName,      // 指定品牌名
                    "01/01/2023",   // 开始日期
                    "12/31/2024"    // 结束日期
            );

            System.out.println("品牌测试结果:");
            System.out.println("  品牌名: " + brandName);
            System.out.println("  保存记录: " + result.get("totalSaved"));
            System.out.println("  跳过记录: " + result.get("totalSkipped"));
            System.out.println("  爬取页数: " + result.get("totalPages"));

        } catch (Exception e) {
            System.err.println("品牌测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取错误类型，用于重试机制判断
     */
    private String getErrorType(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "未知错误";
        }
        
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("timeout") || lowerMessage.contains("read timed out")) {
            return "超时错误";
        } else if (lowerMessage.contains("connection") || lowerMessage.contains("connect")) {
            return "连接错误";
        } else if (lowerMessage.contains("http") || lowerMessage.contains("status")) {
            return "HTTP错误";
        } else if (lowerMessage.contains("parse") || lowerMessage.contains("html")) {
            return "解析错误";
        } else if (lowerMessage.contains("socket") || lowerMessage.contains("network")) {
            return "网络错误";
        } else {
            return "其他错误";
        }
    }

    /**
     * 基于关键词列表爬取FDA事件报告数据
     * 每个关键词将依次作为品牌名称、制造商、型号进行搜索
     */
    public Map<String, Object> crawlFDADataWithKeywords(List<String> inputKeywords, String dateFrom, String dateTo, Integer maxPages) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int totalSaved = 0;
        int totalSkipped = 0;
        int totalPages = 0;
        
        log.info("开始基于关键词列表爬取FDA事件报告数据...");
        log.info("关键词数量: {}", inputKeywords != null ? inputKeywords.size() : 0);
        log.info("搜索策略: 每个关键词将依次作为品牌名称、制造商、型号进行搜索");
        log.info("日期范围: {} - {}", dateFrom, dateTo);
        log.info("最大页数: {}", maxPages);
        
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            log.warn("关键词列表为空，使用默认搜索");
            return crawlMAUDEDataWithParams("", "", "", dateFrom, dateTo, maxPages);
        }
        
        // 遍历每个关键词进行搜索
        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            log.info("正在搜索关键词: {}", keyword);
            
            try {
                // 1. 使用关键词作为品牌名称进行搜索
                log.info("关键词 '{}' 作为品牌名称搜索", keyword);
                Map<String, Object> brandResult = crawlMAUDEDataWithParams(
                    keyword, "", "", dateFrom, dateTo, maxPages
                );
                if (brandResult != null) {
                    totalSaved += (Integer) brandResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) brandResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) brandResult.getOrDefault("totalPages", 0);
                }
                Thread.sleep(DELAY_MS);
                
                // 2. 使用关键词作为制造商进行搜索
                log.info("关键词 '{}' 作为制造商搜索", keyword);
                Map<String, Object> manufacturerResult = crawlMAUDEDataWithParams(
                    "", keyword, "", dateFrom, dateTo, maxPages
                );
                if (manufacturerResult != null) {
                    totalSaved += (Integer) manufacturerResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) manufacturerResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) manufacturerResult.getOrDefault("totalPages", 0);
                }
                Thread.sleep(DELAY_MS);
                
                // 3. 使用关键词作为型号进行搜索
                log.info("关键词 '{}' 作为型号搜索", keyword);
                Map<String, Object> modelResult = crawlMAUDEDataWithParams(
                    "", "", keyword, dateFrom, dateTo, maxPages
                );
                if (modelResult != null) {
                    totalSaved += (Integer) modelResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) modelResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) modelResult.getOrDefault("totalPages", 0);
                }
                Thread.sleep(DELAY_MS);
                
                log.info("关键词 '{}' 完成三种参数搜索", keyword);
                
            } catch (Exception e) {
                log.error("关键词 '{}' 搜索失败: {}", keyword, e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "基于关键词列表的FDA事件报告数据爬取完成");
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("totalPages", totalPages);
        result.put("keywordsProcessed", inputKeywords.size());
        log.info("关键词列表爬取完成 - 总保存: {}, 总跳过: {}, 总页数: {}, 处理关键词数: {}, 总搜索次数: {}", 
                totalSaved, totalSkipped, totalPages, inputKeywords.size(), inputKeywords.size() * 3);
        
        return result;
    }
}