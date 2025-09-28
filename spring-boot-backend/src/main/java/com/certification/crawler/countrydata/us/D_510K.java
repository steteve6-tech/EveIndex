package com.certification.crawler.countrydata.us;

import com.certification.entity.common.CertNewsData;
import com.certification.entity.common.Device510K;
import com.certification.repository.common.Device510KRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FDA 510K爬虫类 - 基于HTTP请求的爬虫实现
 * 将Python爬虫代码转换为Java版本
 */
@Slf4j
@Component
public class D_510K {
    // -------------------------- 1. 初始化配置 --------------------------
    private static final String BASE_URL = "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfpmn/pmn.cfm";
    private static final int MAX_PAGES = 10; // 最大爬取页数（0表示爬到最后一页）
    private static final int DELAY_MS = 2000; // 每页延迟（毫秒）
    private static final int BATCH_SIZE = 20; // 每批保存的数据量

    // 搜索筛选条件
    private final Map<String, String> searchParams = new HashMap<>();
    
    @Autowired
    private Device510KRepository device510KRepository;

    /**
     * 执行FDA数据爬取（带参数化搜索）
     */
    public Map<String, Object> crawlFDADataWithParams(String deviceName, String applicantName, String dateFrom, String dateTo, Integer maxPages) {
        List<Map<String, Object>> allResults = new ArrayList<>();
        int currentPage = 1;
        int totalSaved = 0;
        int totalSkipped = 0;
        int actualMaxPages = (maxPages != null && maxPages > 0) ? maxPages : MAX_PAGES;

        log.info("开始爬取FDA 510K数据（参数化搜索）...");
        log.info("设备名称: {}", deviceName);
        log.info("申请人: {}", applicantName.isEmpty() ? "不限" : applicantName);
        log.info("日期范围: {} - {}", dateFrom, dateTo);
        log.info("最大页数: {}", actualMaxPages);
        
        // 初始化搜索参数
        initSearchParams(deviceName, applicantName, dateFrom, dateTo);

        while (true) {
            try {
                log.info("爬取第 {} 页...", currentPage);
                
                // 发送请求并解析结果
                List<Map<String, Object>> pageResults = crawlPage(currentPage);
                
                if (pageResults.isEmpty()) {
                    log.info("第 {} 页无结果，已到最后一页！", currentPage);
                    break;
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
                        break;
                    }
                    
                    allResults.clear(); // 清空已保存的数据
                }
                
                // 如果获取的记录数少于500条，说明已经到达最后一页
                if (pageResults.size() < 500) {
                    log.info("第 {} 页获取记录数 {} 条，少于500条，已到达最后一页，停止爬取", currentPage, pageResults.size());
                    break;
                }
                
            } catch (Exception e) {
                log.error("第 {} 页爬取失败：{}，重试中...", currentPage, e.getMessage());
                try {
                    Thread.sleep(5000); // 等待5秒后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
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
     * 执行FDA数据爬取（保持向后兼容）
     */
    public Map<String, Object> crawlFDAData(String deviceName, String applicantName, String dateFrom, String dateTo) {
        return crawlFDADataWithParams(deviceName, applicantName, dateFrom, dateTo, MAX_PAGES);
    }

    /**
     * 初始化搜索参数
     */
    private void initSearchParams(String deviceName, String applicantName, String dateFrom, String dateTo) {
        searchParams.clear(); // 清空之前的参数
        searchParams.put("start_search", "1");
        searchParams.put("Center", "");
        searchParams.put("Panel", "");
        searchParams.put("ProductCode", "");
        searchParams.put("KNumber", "");
        searchParams.put("Applicant", applicantName);
        searchParams.put("DeviceName", deviceName);
        searchParams.put("Type", "");
        searchParams.put("ThirdPartyReviewed", "");
        searchParams.put("ClinicalTrials", "");
        searchParams.put("Decision", "");
        searchParams.put("DecisionDateFrom", dateFrom);
        searchParams.put("DecisionDateTo", dateTo);
        searchParams.put("IVDProducts", "");
        searchParams.put("Redact510K", "");
        searchParams.put("CombinationProducts", "");
        searchParams.put("PCCP", "");
        searchParams.put("ZNumber", "");
        searchParams.put("PAGENUM", "500"); // 设置为500条记录每页
        
        log.info("搜索参数初始化完成");
        log.info("设备名称关键词: {}", searchParams.get("DeviceName"));
        log.info("申请人: {}", searchParams.get("Applicant").isEmpty() ? "不限" : searchParams.get("Applicant"));
        log.info("日期范围: {} - {}", searchParams.get("DecisionDateFrom"), searchParams.get("DecisionDateTo"));
        log.info("每页记录数: {}", searchParams.get("PAGENUM"));
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
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://www.accessdata.fda.gov/scripts/cdrh/cfdocs/cfpmn/pmn.cfm")
                .timeout(30000)
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
        
        // 根据HTML结构，找到结果表格中的数据行
        Elements resultRows = doc.select("tr[bgcolor='#ffffff'], tr[bgcolor='#f9f9f3']");
        
        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试其他选择器", pageNum);
            // 备用选择器：查找包含链接的行
            resultRows = doc.select("tr:has(td a[href*='pmn.cfm?ID='])");
        }
        
        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行，尝试更宽泛的选择器", pageNum);
            // 更宽泛的选择器：查找所有包含td的行
            resultRows = doc.select("tr:has(td)");
        }
        
        // 过滤掉表头行
        List<Element> filteredRows = resultRows.stream()
                .filter(row -> {
                    Elements links = row.select("td a[href*='pmn.cfm?ID=']");
                    return !links.isEmpty();
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 将List<Element>转换为Elements
        resultRows = new Elements(filteredRows);
        
        if (resultRows.isEmpty()) {
            log.debug("第 {} 页未找到结果行", pageNum);
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
     * 根据HTML结构：Device Name | Applicant | 510(K) Number | Decision Date
     */
    private Map<String, Object> parseRowData(Elements cols, int pageNum) {
        try {
            Map<String, Object> record = new HashMap<>();
            
            if (cols.size() < 4) {
                log.warn("列数不足，跳过该行");
                return null;
            }
            
            // 提取字段（根据HTML结构）
            // 第0列：Device Name（带链接）
            String deviceName = "";
            Element deviceNameLink = cols.get(0).select("a").first();
            if (deviceNameLink != null) {
                deviceName = deviceNameLink.text().trim();
            } else {
                deviceName = cols.get(0).text().trim();
            }
            
            // 第1列：Applicant（申请人）
            String applicant = cols.get(1).text().trim();
            
            // 第2列：510(K) Number（带链接）
            String kNumber = "";
            Element kNumberLink = cols.get(2).select("a").first();
            if (kNumberLink != null) {
                kNumber = kNumberLink.text().trim();
            } else {
                kNumber = cols.get(2).text().trim();
            }
            
            // 设置记录字段
            record.put("deviceName", deviceName);
            record.put("applicant", applicant);
            record.put("kNumber", kNumber);
            
            // 设置其他字段
            record.put("dataSource", "FDA 510K Database");
            record.put("countryCode", "US");
            record.put("jdCountry", "US");
            record.put("crawlTime", LocalDateTime.now());
            record.put("dataStatus", "ACTIVE");
            
            log.debug("解析记录: 设备名称={}, 申请人={}, K号={}", 
                     deviceName, applicant, kNumber);
            
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
        
        // 检查是否在测试模式（没有Spring依赖注入）
        if (device510KRepository == null) {
            log.info("测试模式：跳过数据库保存，仅保存到CSV文件");
            log.info("批量数据: {} 条记录", batchData.size());
            return new int[]{batchData.size(), 0}; // 模拟保存成功
        }
        
        for (Map<String, Object> data : batchData) {
            try {
                String kNumber = (String) data.get("kNumber");
                
                // 检查是否已存在相同的K号记录
                if (device510KRepository.existsBykNumber(kNumber)) {
                    log.debug("跳过已存在的记录: {}", kNumber);
                    skippedCount++;
                    continue;
                }
                
                // 创建新记录并保存
                Device510K record = createFromMap(data);
                device510KRepository.save(record);
                savedCount++;
                
            } catch (Exception e) {
                log.error("保存记录失败: {}", e.getMessage());
                // 继续处理下一条记录，不中断整个批次
                continue;
            }
        }
        
        log.info("批量保存完成，新增: {} 条，跳过: {} 条", savedCount, skippedCount);
        return new int[]{savedCount, skippedCount};
    }

    /**
     * 从Map数据创建Device510K对象
     */
    private Device510K createFromMap(Map<String, Object> data) {
        Device510K record = new Device510K();
        
        // 基本字段映射（限制长度）
        String deviceName = truncateString((String) data.get("deviceName"), 255);
        String applicant = truncateString((String) data.get("applicant"), 255);
        String kNumber = truncateString((String) data.get("kNumber"), 32);
        String tradeName = truncateString((String) data.get("tradeName"), 255);
        
        record.setDeviceName(deviceName);
        record.setApplicant(applicant);
        record.setKNumber(kNumber);
        record.setDataSource((String) data.get("dataSource"));
        record.setCountryCode((String) data.get("countryCode"));
        record.setJdCountry((String) data.get("jdCountry"));
        record.setCrawlTime((LocalDateTime) data.get("crawlTime"));
        record.setDataStatus((String) data.get("dataStatus"));
        
        // 设置默认值
        record.setRiskLevel(CertNewsData.RiskLevel.MEDIUM);
        
        // 设置品牌名称（如果提供的话）
        record.setTradeName(tradeName);
        
        return record;
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 基于关键词列表执行FDA 510K数据爬取
     * 每个关键词作为设备名称进行搜索，其他参数为空
     */
    public Map<String, Object> crawlFDADataWithKeywords(List<String> inputKeywords, String dateFrom, String dateTo, Integer maxPages) {
        int totalSaved = 0;
        int totalSkipped = 0;
        int totalPages = 0;
        
        log.info("开始基于关键词列表爬取FDA 510K数据...");
        log.info("关键词数量: {}", inputKeywords != null ? inputKeywords.size() : 0);
        log.info("日期范围: {} - {}", dateFrom, dateTo);
        log.info("最大页数: {}", maxPages);
        
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            log.warn("关键词列表为空，使用默认搜索");
            return crawlFDADataWithParams("", "", dateFrom, dateTo, maxPages);
        }
        
        // 遍历每个关键词进行搜索
        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            log.info("正在搜索关键词: {}", keyword);
            
            try {
                // 使用关键词作为设备名称进行搜索，申请人名称为空
                Map<String, Object> keywordResult = crawlFDADataWithParams(
                    keyword, "", dateFrom, dateTo, maxPages
                );
                
                // 累加结果
                if (keywordResult != null) {
                    totalSaved += (Integer) keywordResult.getOrDefault("totalSaved", 0);
                    totalSkipped += (Integer) keywordResult.getOrDefault("totalSkipped", 0);
                    totalPages += (Integer) keywordResult.getOrDefault("totalPages", 0);
                }
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(DELAY_MS);
                
            } catch (Exception e) {
                log.error("关键词 '{}' 搜索失败: {}", keyword, e.getMessage());
                // 继续处理下一个关键词
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "基于关键词列表的FDA 510K数据爬取完成");
        result.put("totalSaved", totalSaved);
        result.put("totalSkipped", totalSkipped);
        result.put("totalPages", totalPages);
        result.put("keywordsProcessed", inputKeywords.size());
        
        log.info("关键词列表爬取完成 - 总保存: {}, 总跳过: {}, 总页数: {}, 处理关键词数: {}", 
                totalSaved, totalSkipped, totalPages, inputKeywords.size());
        
        return result;
    }


}
