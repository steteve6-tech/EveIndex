package com.certification.crawler.countrydata.us;

import com.certification.entity.common.DeviceRecallRecord;
import com.certification.repository.common.DeviceRecallRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
 * FDA召回数据爬虫类 - 基于HTTP请求的爬虫实现
 * 爬取FDA召回数据库：https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfRES/res.cfm
 * 参考D_510K的实现方式
 */
@Slf4j
@Component
public class D_recall {

    @Autowired
    private DeviceRecallRecordRepository deviceRecallRecordRepository;

    // -------------------------- 1. 初始化配置 --------------------------
    private static final String BASE_URL = "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfRES/res.cfm";
    private static final int MAX_PAGES = 10; // 最大爬取页数（0表示爬到最后一页）
    private static final int DELAY_MS = 2000; // 每页延迟（毫秒）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final int BATCH_SIZE = 20; // 每批保存的数据量
    
    // 重试机制配置
    private static final int MAX_RETRY_ATTEMPTS = 5; // 最大重试次数
    private static final int RETRY_DELAY_MS = 5000; // 重试延迟（毫秒）
    private static final int TIMEOUT_MS = 30000; // 请求超时时间（毫秒）

    // 搜索筛选条件
    private final Map<String, String> searchParams = new HashMap<>();

    /**
     * 执行FDA召回数据爬取（带参数化搜索）
     */
    public Map<String, Object> crawlFDARecallDataWithParams(String productName, String reasonForRecall, 
                                                           String recallingFirm, String recallDateFrom, 
                                                           String recallDateTo, Integer maxPages) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int currentPage = 1;
        int totalSaved = 0;
        int totalSkipped = 0;
        int actualMaxPages = (maxPages != null && maxPages > 0) ? maxPages : MAX_PAGES;
        int consecutiveEmptyPages = 0; // 连续无结果页面计数器

        log.info("开始爬取FDA召回数据（参数化搜索）...");
        log.info("产品名称: {}", productName);
        log.info("召回原因: {}", reasonForRecall);
        log.info("召回公司: {}", recallingFirm);
        log.info("日期范围: {} - {}", recallDateFrom, recallDateTo);
        log.info("最大页数: {}", actualMaxPages);
        
        // 初始化搜索参数
        initSearchParamsWithReason(productName, reasonForRecall, recallingFirm, recallDateFrom, recallDateTo);

        while (true) {
            boolean pageSuccess = false;
            int retryCount = 0;
            
            // 重试机制：最多重试5次
            while (retryCount < MAX_RETRY_ATTEMPTS && !pageSuccess) {
                try {
                    log.info("爬取第 {} 页... (尝试 {}/{})", currentPage, retryCount + 1, MAX_RETRY_ATTEMPTS);
                    
                    // 更新分页参数
                    if (currentPage > 1) {
                        // 对于第二页及以后，计算起始位置
                        int startPosition = (currentPage - 1) * Integer.parseInt(searchParams.get("pagenum")) + 1;
                        searchParams.put("start_search", String.valueOf(startPosition));
                    } else {
                        searchParams.put("start_search", "1");
                    }
                    
                    // 发送请求并解析结果
                    List<Map<String, Object>> pageResults = crawlPage(currentPage);
                    
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
                    
                    // 如果获取的记录数少于每页设置的数量，说明已经到达最后一页
                    int pageSize = Integer.parseInt(searchParams.get("pagenum"));
                    if (pageResults.size() < pageSize) {
                        log.info("第 {} 页获取记录数 {} 条，少于每页设置数量 {} 条，已到达最后一页，停止爬取", 
                                currentPage, pageResults.size(), pageSize);
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
     * 执行FDA召回数据爬取（支持关键词列表）
     */
    public Map<String, Object> crawlFDARecallDataWithKeywords(List<String> inputKeywords, String recallDateFrom, 
                                                             String recallDateTo, Integer maxPages) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int totalSaved = 0;
        int totalSkipped = 0;
        int totalPages = 0;
        
        log.info("开始基于关键词列表爬取FDA召回数据...");
        log.info("关键词数量: {}", inputKeywords != null ? inputKeywords.size() : 0);
        log.info("搜索策略: 每个关键词将依次作为产品名称、召回原因、召回公司进行搜索");
        log.info("日期范围: {} - {}", recallDateFrom, recallDateTo);
        log.info("最大页数: {}", maxPages);
        
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            log.warn("关键词列表为空，使用默认搜索");
            return crawlFDARecallDataWithParams("", "", "", recallDateFrom, recallDateTo, maxPages);
        }
        
        // 遍历每个关键词进行搜索
        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            log.info("正在搜索关键词: {}", keyword);
            
            try {
                // 1. 使用关键词作为产品名称进行搜索
                log.info("关键词 '{}' 作为产品名称搜索", keyword);
                Map<String, Object> productNameResult = crawlFDARecallDataWithParams(
                    keyword, "", "", recallDateFrom, recallDateTo, maxPages
                );
                
                // 累加产品名称搜索结果
                if (productNameResult != null) {
                    totalSaved += (Integer) productNameResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) productNameResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) productNameResult.getOrDefault("totalPages", 0);
                }
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(DELAY_MS);
                
                // 2. 使用关键词作为召回原因进行搜索
                log.info("关键词 '{}' 作为召回原因搜索", keyword);
                Map<String, Object> reasonResult = crawlFDARecallDataWithParams(
                    "", keyword, "", recallDateFrom, recallDateTo, maxPages
                );
                
                // 累加召回原因搜索结果
                if (reasonResult != null) {
                    totalSaved += (Integer) reasonResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) reasonResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) reasonResult.getOrDefault("totalPages", 0);
                }
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(DELAY_MS);
                
                // 3. 使用关键词作为召回公司进行搜索
                log.info("关键词 '{}' 作为召回公司搜索", keyword);
                Map<String, Object> firmResult = crawlFDARecallDataWithParams(
                    "", "", keyword, recallDateFrom, recallDateTo, maxPages
                );
                
                // 累加召回公司搜索结果
                if (firmResult != null) {
                    totalSaved += (Integer) firmResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) firmResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) firmResult.getOrDefault("totalPages", 0);
                }
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(DELAY_MS);
                
                log.info("关键词 '{}' 完成三种参数搜索", keyword);
                
            } catch (Exception e) {
                log.error("关键词 '{}' 搜索失败: {}", keyword, e.getMessage());
                // 继续处理下一个关键词
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "基于关键词列表的FDA召回数据爬取完成");
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("totalPages", totalPages);
        result.put("keywordsProcessed", inputKeywords.size());
        
        log.info("关键词列表爬取完成 - 总保存: {}, 总跳过: {}, 总页数: {}, 处理关键词数: {}, 总搜索次数: {}", 
                totalSaved, totalSkipped, totalPages, inputKeywords.size(), inputKeywords.size() * 3);
        
        return result;
    }

    /**
     * 执行FDA召回数据爬取（保持向后兼容）
     */
    public Map<String, Object> crawlFDARecallData(String productDescription, String recallDateFrom, 
                                                  String recallDateTo, String firmName) {


        return crawlFDARecallDataWithParams(productDescription, "", firmName, recallDateFrom, recallDateTo, MAX_PAGES);
    }

    /**
     * 初始化搜索参数（带召回原因支持）
     */
    private void initSearchParamsWithReason(String productName, String reasonForRecall, String recallingFirm, 
                                          String recallDateFrom, String recallDateTo) {
        searchParams.clear(); // 清空之前的参数
        searchParams.put("start_search", "1");
        searchParams.put("productcode", "");
        searchParams.put("PMA_510K_Num", "");
        searchParams.put("knumber", "");
        searchParams.put("centerclassificationtypetext", "");
        searchParams.put("pnumber", "");
        searchParams.put("rootCauseText", "");
        searchParams.put("pagenum", "500"); // 设置为500条记录每页
        searchParams.put("IVDProducts", "");
        
        // 处理日期格式：如果是ISO格式，提取日期部分
        String formattedDateTo = formatDateForFDA(recallDateTo);
        String formattedDateFrom = formatDateForFDA(recallDateFrom);
        
        searchParams.put("postdateto", formattedDateTo);
        searchParams.put("firmlegalnam", recallingFirm);
        searchParams.put("event_id", "");
        searchParams.put("productshortreasontxt", reasonForRecall);  // 新增召回原因参数
        searchParams.put("sortcolumn", "cdd");
        searchParams.put("recallnumber", "");
        searchParams.put("recallstatus", "");
        searchParams.put("postdatefrom", formattedDateFrom);
        searchParams.put("productdescriptiontxt", productName);
        
        log.info("搜索参数初始化完成（带召回原因支持）");
        log.info("产品名称关键词: {}", searchParams.get("productdescriptiontxt"));
        log.info("召回原因: {}", searchParams.get("productshortreasontxt").isEmpty() ? "不限" : searchParams.get("productshortreasontxt"));
        log.info("召回公司: {}", searchParams.get("firmlegalnam").isEmpty() ? "不限" : searchParams.get("firmlegalnam"));
        log.info("日期范围: {} - {}", searchParams.get("postdatefrom"), searchParams.get("postdateto"));
        log.info("每页记录数: {}", searchParams.get("pagenum"));
    }
    
    /**
     * 格式化日期为FDA网站期望的格式
     * @param dateStr 日期字符串（可能是ISO格式或YYYY-MM-DD格式）
     * @return 格式化后的日期字符串
     */
    private String formatDateForFDA(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        
        try {
            // 如果是ISO格式（包含T），提取日期部分
            if (dateStr.contains("T")) {
                return dateStr.substring(0, 10); // 提取YYYY-MM-DD部分
            }
            // 如果已经是YYYY-MM-DD格式，直接返回
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return dateStr;
            }
            // 其他格式，尝试解析并转换
            return dateStr;
        } catch (Exception e) {
            log.warn("日期格式转换失败: {}, 使用原始值", dateStr);
            return dateStr;
        }
    }

    /**
     * 初始化搜索参数（保持向后兼容）
     */
    private void initSearchParams(String productDescription, String recallDateFrom, String recallDateTo, String firmName) {
        searchParams.clear(); // 清空之前的参数
        searchParams.put("start_search", "1");
        searchParams.put("productcode", "");
        searchParams.put("PMA_510K_Num", "");
        searchParams.put("knumber", "");
        searchParams.put("centerclassificationtypetext", "");
        searchParams.put("pnumber", "");
        searchParams.put("rootCauseText", "");
        searchParams.put("pagenum", "500"); // 设置为50条记录每页
        searchParams.put("IVDProducts", "");
        
        // 处理日期格式：如果是ISO格式，提取日期部分
        String formattedDateTo = formatDateForFDA(recallDateTo);
        String formattedDateFrom = formatDateForFDA(recallDateFrom);
        
        searchParams.put("postdateto", formattedDateTo);
        searchParams.put("firmlegalnam", firmName);
        searchParams.put("event_id", "");
        searchParams.put("productshortreasontxt", "");
        searchParams.put("sortcolumn", "cdd");
        searchParams.put("recallnumber", "");
        searchParams.put("recallstatus", "");
        searchParams.put("postdatefrom", formattedDateFrom);
        searchParams.put("productdescriptiontxt", productDescription);
        
        log.info("搜索参数初始化完成");
        log.info("产品描述关键词: {}", searchParams.get("productdescriptiontxt"));
        log.info("公司名称: {}", searchParams.get("firmlegalnam").isEmpty() ? "不限" : searchParams.get("firmlegalnam"));
        log.info("日期范围: {} - {}", searchParams.get("postdatefrom"), searchParams.get("postdateto"));
        log.info("每页记录数: {}", searchParams.get("pagenum"));
    }

    /**
     * 爬取单页数据
     */
    private List<Map<String, Object>> crawlPage(int pageNum) throws IOException {
        List<Map<String, Object>> pageResults = new ArrayList<>();
        
        // 尝试使用GET请求
        String urlWithParams = buildGetUrl();
        log.info("请求URL: {}", urlWithParams);
        
        Connection.Response response = Jsoup.connect(urlWithParams)
                .method(Connection.Method.GET)
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
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .execute();

        // 解析HTML
        Document doc = response.parse();
        
        // 检查是否返回错误页面
        if (doc.title().contains("Error") || doc.title().contains("Temporarily Unavailable")) {
            log.error("FDA网站返回错误页面: {}", doc.title());
            log.error("页面内容: {}", doc.select("h1, h2").text());
            return pageResults;
        }
        
        // 调试：打印页面标题和部分内容
        log.info("页面标题: {}", doc.title());
        log.debug("页面内容长度: {} 字符", doc.html().length());
        
        // 检查页面是否包含"没有找到结果"的提示
        String pageText = doc.text().toLowerCase();
        if (pageText.contains("no results found") || pageText.contains("no records found") || 
            pageText.contains("没有找到结果") || pageText.contains("未找到记录")) {
            log.info("第 {} 页明确显示无结果", pageNum);
            return pageResults;
        }
        
        // 根据HTML结构，找到结果表格中的数据行
        Elements resultRows = doc.select("tr[bgcolor='#ffffff'], tr[bgcolor='#f9f9f3']");
        
        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试其他选择器", pageNum);
            // 备用选择器：查找包含链接的行
            resultRows = doc.select("tr:has(td a[href*='res.cfm?ID='])");
        }
        
        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试更宽泛的选择器", pageNum);
            // 更宽泛的选择器：查找所有包含td的行
            resultRows = doc.select("tr:has(td)");
        }
        
        // 过滤掉表头行
        List<Element> filteredRows = resultRows.stream()
                .filter(row -> {
                    Elements links = row.select("td a[href*='res.cfm?ID=']");
                    return !links.isEmpty();
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 将List<Element>转换为Elements
        resultRows = new Elements(filteredRows);
        
        if (resultRows.isEmpty()) {
            log.info("第 {} 页未找到有效的结果行", pageNum);
            return pageResults;
        }

        // 提取每行数据
        for (Element row : resultRows) {
            Elements cols = row.select("td");
            if (cols.size() < 4) { // 跳过异常行
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

        return pageResults;
    }

    /**
     * 构建GET请求URL
     */
    private String buildGetUrl() {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("?");
        
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
     * 根据HTML结构：Product Description | Recall Class | FDA Recall Posting Date | Recalling Firm
     */
    private Map<String, Object> parseRowData(Elements cols, int pageNum) {
        try {
            Map<String, Object> record = new HashMap<>();
            
            if (cols.size() < 4) {
                log.warn("列数不足，跳过该行");
                return null;
            }
            
            // 提取字段（根据实际HTML结构）
            // 第0列：Product Description（产品描述，带链接）
            String productDescription = "";
            String recallUrl = "";
            Element productLink = cols.get(0).select("a").first();
            if (productLink != null) {
                productDescription = productLink.text().trim();
                recallUrl = productLink.attr("href");
                if (!recallUrl.startsWith("http")) {
                    recallUrl = "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfRES/" + recallUrl;
                }
            } else {
                productDescription = cols.get(0).text().trim();
            }
            
            // 第1列：Recall Class（召回等级）
            String recallClass = cols.get(1).text().trim();
            
            // 第2列：FDA Recall Posting Date（FDA召回发布日期）
            String recallDateStr = cols.get(2).text().trim();
            
            // 第3列：Recalling Firm（召回公司）
            String firmName = cols.get(3).text().trim();
            
            // 从URL中提取召回编号
            String recallNumber = "";
            if (recallUrl.contains("id=")) {
                recallNumber = recallUrl.substring(recallUrl.indexOf("id=") + 3);
            }
            
            // 设置记录字段
            record.put("recallNumber", recallNumber);
            record.put("productDescription", productDescription);
            record.put("recallClass", recallClass);
            record.put("recallDateStr", recallDateStr);
            record.put("firmName", firmName);
            record.put("recallUrl", recallUrl);
            
            // 解析日期
            if (!recallDateStr.isEmpty()) {
                try {
                    LocalDate recallDate = LocalDate.parse(recallDateStr, DATE_FORMATTER);
                    record.put("recallDate", recallDate);
                } catch (Exception e) {
                    log.warn("日期解析失败: {}", recallDateStr);
                    record.put("recallDate", null);
                }
            } else {
                record.put("recallDate", null);
            }
            
            // 设置其他字段
            record.put("dataSource", "FDA Recall Database");
            record.put("countryCode", "US");
            record.put("jdCountry", "US");
            record.put("crawlTime", LocalDateTime.now());
            record.put("dataStatus", "ACTIVE");
            
            log.debug("解析记录: 召回编号={}, 产品描述={}, 召回等级={}, 召回日期={}, 公司名称={}, 召回URL={}", 
                     recallNumber, productDescription, recallClass, recallDateStr, firmName, recallUrl);
            
            return record;
            
        } catch (Exception e) {
            log.error("解析行数据时出错: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 批量保存数据到数据库
     */
    private int[] saveBatchToDatabase(List<Map<String, Object>> batchData) {
        int savedCount = 0;
        int skippedCount = 0;
        
        try {
            List<DeviceRecallRecord> recordsToSave = new ArrayList<>();
            
            for (Map<String, Object> data : batchData) {
                try {
                    DeviceRecallRecord record = convertToEntity(data);
                    
                    // 检查是否已存在相同的记录
                    if (record.getCfresId() != null && !record.getCfresId().isEmpty()) {
                        var existingRecord = deviceRecallRecordRepository.findByCfresId(record.getCfresId());
                        if (existingRecord.isPresent()) {
                            log.debug("记录已存在，跳过: {}", record.getCfresId());
                            skippedCount++;
                            continue;
                        }
                    }
                    
                    recordsToSave.add(record);
                    
                } catch (Exception e) {
                    log.error("转换记录失败: {}", e.getMessage());
                    skippedCount++;
                    continue;
                }
            }
            
            // 批量保存到数据库
            if (!recordsToSave.isEmpty()) {
                deviceRecallRecordRepository.saveAll(recordsToSave);
                savedCount = recordsToSave.size();
                log.info("批量保存到数据库完成，新增: {} 条，跳过: {} 条", savedCount, skippedCount);
            }
            
        } catch (Exception e) {
            log.error("保存到数据库失败: {}", e.getMessage());
            skippedCount = batchData.size();
        }
        
        return new int[]{savedCount, skippedCount};
    }

    /**
     * 将Map数据转换为DeviceRecallRecord实体
     */
    private DeviceRecallRecord convertToEntity(Map<String, Object> data) {
        DeviceRecallRecord record = new DeviceRecallRecord();
        
        // 基本信息
        record.setCfresId((String) data.get("recallNumber"));
        record.setProductDescription((String) data.get("productDescription"));
        record.setRecallingFirm((String) data.get("firmName"));
        record.setDataSource((String) data.get("dataSource"));
        record.setCountryCode((String) data.get("countryCode"));
        record.setJdCountry((String) data.get("jdCountry"));
        
        // 召回状态和等级
        String recallClass = (String) data.get("recallClass");
        record.setRecallStatus(recallClass);
        
        // 根据召回等级设置风险等级
        if (recallClass != null) {
            switch (recallClass.toUpperCase()) {
                case "CLASS I":
                    record.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.HIGH);
                    break;
                case "CLASS II":
                    record.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.MEDIUM);
                    break;
                case "CLASS III":
                    record.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.LOW);
                    break;
                default:
                    record.setRiskLevel(com.certification.entity.common.CrawlerData.RiskLevel.MEDIUM);
                    break;
            }
        }
        
        // 日期处理
        LocalDate recallDate = (LocalDate) data.get("recallDate");
        if (recallDate != null) {
            record.setEventDatePosted(recallDate);
        }
        
        // 设置其他字段
        record.setDeviceName((String) data.get("productDescription"));
        record.setProductCode(""); // 可以从产品描述中提取
        
        return record;
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

}
