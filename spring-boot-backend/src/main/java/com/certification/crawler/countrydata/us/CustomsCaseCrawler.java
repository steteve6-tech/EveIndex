package com.certification.crawler.countrydata.us;

import com.certification.entity.common.CustomsCase;
import com.certification.repository.common.CustomsCaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CBP rulings爬虫 - 爬取美国海关与边境保护局裁定数据并保存到数据库
 * 网址：https://rulings.cbp.gov/search
 * 对应数据库表：t_customs_case
 */
@Slf4j
@Component
public class CustomsCaseCrawler {
    
    // 基础URL
    private static final String BASE_URL = "https://rulings.cbp.gov/search";
    private static final String API_URL = "https://rulings.cbp.gov/api/search";
    // 日期格式化器（页面日期格式为M/d/yyyy，如6/23/2025）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    // 等待时间
    private static final int WAIT_TIMEOUT_SECONDS = 30;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 60;

    @Autowired
    private CustomsCaseRepository customsCaseRepository;

    private HttpClient httpClient;

    /**
     * 初始化HttpClient
     */
    public void initHttpClient() {
        try {
            log.info("正在初始化HttpClient...");
            
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            
            log.info("HttpClient初始化成功！");
            
        } catch (Exception e) {
            log.error("HttpClient 初始化失败：" + e.getMessage());
            throw new RuntimeException("无法初始化HttpClient", e);
        }
    }

    /**
     * 爬取数据主方法
     */
    public List<CustomsCase> crawlAndSaveCustomsCases(String searchTerm, int maxRecords, int batchSize) {
        return crawlAndSaveCustomsCases(searchTerm, maxRecords, batchSize, null);
    }

    /**
     * 爬取数据主方法（带时间过滤）
     */
    public List<CustomsCase> crawlAndSaveCustomsCases(String searchTerm, int maxRecords, int batchSize, LocalDate startDate) {
        List<CustomsCase> allRecords = new ArrayList<>();
        
        try {
            initHttpClient();
            log.info("=== CBP Rulings爬虫启动 ===");
            log.info("搜索关键词: " + searchTerm);
            log.info("最大记录数: " + maxRecords);
            if (startDate != null) {
                log.info("开始日期: " + startDate.format(DATE_FORMATTER));
            }
            
            // 分页爬取逻辑
            int currentPage = 1;
            int totalFetched = 0;
            boolean hasMoreData = true;
            boolean crawlAll = (maxRecords == -1);
            
            while (hasMoreData && (crawlAll || totalFetched < maxRecords)) {
                // 计算当前页面的记录数限制
                int currentPageSize = batchSize;
                if (!crawlAll) {
                    currentPageSize = Math.min(batchSize, maxRecords - totalFetched);
                }
                
                if (currentPageSize <= 0) {
                    break;
                }
                
                log.info("正在获取第 " + currentPage + " 页数据，页面大小: " + currentPageSize);
                
                // 构建API URL
                String apiUrl = API_URL + "?term=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8) + 
                               "&collection=ALL&commodityGrouping=ALL&pageSize=" + currentPageSize + 
                               "&page=" + currentPage + "&sortBy=RELEVANCE";
                log.info("API URL: " + apiUrl);
                
                // 创建HTTP请求
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Accept", "application/json, text/plain, */*")
                        .header("Accept-Language", "zh-CN,zh;q=0.9")
                        .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
                        .header("Sec-Ch-Ua-Mobile", "?0")
                        .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                        .header("Sec-Fetch-Dest", "empty")
                        .header("Sec-Fetch-Mode", "cors")
                        .header("Sec-Fetch-Site", "same-origin")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36")
                        .GET()
                        .build();
                
                // 发送请求
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    log.info("第 " + currentPage + " 页API请求成功，响应长度: " + response.body().length());
                    
                    // 解析JSON响应
                    List<CustomsCase> records = parseApiResponse(response.body());
                    
                    // 检查是否还有更多数据
                    if (records.isEmpty()) {
                        log.info("第 " + currentPage + " 页没有数据，爬取结束");
                        hasMoreData = false;
                        break;
                    }
                    
                    // 时间过滤
                    if (startDate != null) {
                        records = records.stream()
                                .filter(record -> record.getCaseDate() == null || !record.getCaseDate().isBefore(startDate))
                                .collect(Collectors.toList());
                    }
                    
                    allRecords.addAll(records);
                    totalFetched += records.size();
                    
                    log.info("第 " + currentPage + " 页获取到 " + records.size() + " 条记录，累计: " + totalFetched + " 条");
                    
                    // 如果当前页的记录数少于请求的页面大小，说明没有更多数据了
                    if (records.size() < currentPageSize) {
                        log.info("当前页记录数少于页面大小，没有更多数据");
                        hasMoreData = false;
                    }
                    
                    // 如果已达到最大记录数，停止爬取
                    if (!crawlAll && totalFetched >= maxRecords) {
                        log.info("已达到最大记录数 " + maxRecords + "，停止爬取");
                        hasMoreData = false;
                    }
                    
                    currentPage++;
                    
                    // 添加延迟避免请求过于频繁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    
                } else {
                    log.error("第 " + currentPage + " 页API请求失败，状态码: " + response.statusCode());
                    log.error("响应内容: " + response.body());
                    hasMoreData = false;
                }
            }
            
            // 限制最终记录数量
            if (!crawlAll && allRecords.size() > maxRecords) {
                allRecords = allRecords.subList(0, maxRecords);
                log.info("限制记录数量为 " + maxRecords + " 条");
            }
            
            // 保存数据到数据库（仅在Spring上下文中）
            if (!allRecords.isEmpty() && customsCaseRepository != null) {
                saveToDatabase(allRecords);
            }
            
            log.info("=== 爬取完成 ===");
            log.info("总共爬取到 " + allRecords.size() + " 条记录，共 " + (currentPage - 1) + " 页");
            
        } catch (Exception e) {
            log.error("爬取过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allRecords;
    }



    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 首先尝试ISO 8601格式 (1989-10-04T00:00:00)
            if (dateText.contains("T")) {
                return LocalDate.parse(dateText.substring(0, 10)); // 取前10个字符，即日期部分
            }
            // 然后尝试原有的格式
            return LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("日期格式错误（" + dateText + "）：" + e.getMessage());
            return null;
        }
    }

    /**
     * 保存数据到数据库
     */
    private void saveToDatabase(List<CustomsCase> records) {
        int savedCount = 0;
        int skippedCount = 0;
        
        for (CustomsCase record : records) {
            try {
                // 检查是否已存在相同的记录
                if (customsCaseRepository.existsByCaseNumberAndCaseDate(record.getCaseNumber(), record.getCaseDate())) {
                    log.info("跳过已存在的记录: " + record.getCaseNumber());
                    skippedCount++;
                    continue;
                }
                
                // 保存到数据库
                CustomsCase savedRecord = customsCaseRepository.save(record);
                log.info("成功保存记录: " + savedRecord.getCaseNumber() + " (ID: " + savedRecord.getId() + ")");
                savedCount++;
                
            } catch (Exception e) {
                log.error("保存记录失败: " + record.getCaseNumber() + " - " + e.getMessage());
            }
        }
        
        log.info("数据库保存完成 - 新增: " + savedCount + " 条，跳过: " + skippedCount + " 条");
        
        // 如果所有数据都是重复的，抛出异常停止爬取
        if (savedCount == 0 && skippedCount > 0) {
            log.info("批次数据全部重复，停止爬取");
            throw new RuntimeException("批次数据全部重复，停止爬取");
        }
    }

    /**
     * 根据HS编码爬取数据
     */
    public List<CustomsCase> crawlByHsCode(String hsCode, int maxRecords, int batchSize) {
        log.info("根据HS编码爬取数据: " + hsCode);
        return crawlAndSaveCustomsCases(hsCode, maxRecords, batchSize);
    }

    /**
     * 根据HS编码爬取数据（带时间过滤）
     */
    public List<CustomsCase> crawlByHsCode(String hsCode, int maxRecords, int batchSize, LocalDate startDate) {
        log.info("根据HS编码爬取数据: " + hsCode + "，开始日期: " + (startDate != null ? startDate.format(DATE_FORMATTER) : "无"));
        return crawlAndSaveCustomsCases(hsCode, maxRecords, batchSize, startDate);
    }

    /**
     * 根据关键词爬取数据
     */
    public List<CustomsCase> crawlByKeyword(String keyword, int maxRecords, int batchSize) {
        log.info("根据关键词爬取数据: " + keyword);
        return crawlAndSaveCustomsCases(keyword, maxRecords, batchSize);
    }

    /**
     * 根据关键词爬取数据（带时间过滤）
     */
    public List<CustomsCase> crawlByKeyword(String keyword, int maxRecords, int batchSize, LocalDate startDate) {
        log.info("根据关键词爬取数据: " + keyword + "，开始日期: " + (startDate != null ? startDate.format(DATE_FORMATTER) : "无"));
        return crawlAndSaveCustomsCases(keyword, maxRecords, batchSize, startDate);
    }

    /**
     * 基于关键词列表爬取海关案例数据（复杂策略）
     * 每个关键词将依次作为HS编码、关键词进行搜索
     */
    public String crawlWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize, String dateFrom, String dateTo) {
        if (inputKeywords == null || inputKeywords.isEmpty()) {
            log.info("关键词列表为空，使用默认搜索");
            return crawlAndSaveCustomsCases("9018", maxRecords, batchSize, null).size() + " 条记录";
        }

        log.info("开始基于关键词列表爬取海关案例数据...");
        log.info("关键词数量: " + inputKeywords.size());
        log.info("搜索策略: 每个关键词将依次作为HS编码、关键词进行搜索");
        log.info("日期范围: " + dateFrom + " - " + dateTo);
        log.info("最大记录数: " + (maxRecords == -1 ? "所有数据" : maxRecords));

        int totalFetched = 0;
        int keywordsProcessed = 0;
        
        // 遍历每个关键词进行搜索
        for (String keyword : inputKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            
            keyword = keyword.trim();
            log.info("正在搜索关键词: " + keyword);
            keywordsProcessed++;

            try {
                // 1. 使用关键词作为HS编码进行搜索
                log.info("关键词 '" + keyword + "' 作为HS编码搜索");
                List<CustomsCase> hsCodeResults = crawlByHsCode(keyword, maxRecords, batchSize);
                log.info("HS编码搜索结果: " + hsCodeResults.size() + " 条记录");
                totalFetched += hsCodeResults.size();

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // 2. 使用关键词作为普通关键词进行搜索
                log.info("关键词 '" + keyword + "' 作为普通关键词搜索");
                List<CustomsCase> keywordResults = crawlByKeyword(keyword, maxRecords, batchSize);
                log.info("普通关键词搜索结果: " + keywordResults.size() + " 条记录");
                totalFetched += keywordResults.size();

                // 添加延迟避免请求过于频繁
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
            } catch (Exception e) {
                log.error("关键词 '" + keyword + "' 搜索失败: " + e.getMessage());
                // 如果是"未找到匹配记录"的错误，不打印堆栈跟踪
                if (!e.getMessage().contains("未找到匹配记录") && !e.getMessage().contains("No matches found")) {
                    e.printStackTrace();
                }
            }
        }

        String result = String.format("关键词列表爬取完成，处理关键词数: %d, 总获取记录数: %d", keywordsProcessed, totalFetched);
        log.info(result);
        
        return result;
    }
    
    /**
     * 基于关键词列表爬取海关案例数据（简化版本，无时间范围）
     */
    public String crawlWithKeywords(List<String> inputKeywords, int maxRecords, int batchSize) {
        return crawlWithKeywords(inputKeywords, maxRecords, batchSize, null, null);
    }


    /**
     * 解析API JSON响应
     */
    private List<CustomsCase> parseApiResponse(String jsonResponse) {
        List<CustomsCase> records = new ArrayList<>();
        
        try {
            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonResponse);
            
            // 获取rulings数组
            com.fasterxml.jackson.databind.JsonNode rulingsNode = rootNode.get("rulings");
            if (rulingsNode != null && rulingsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode rulingNode : rulingsNode) {
                    CustomsCase record = new CustomsCase();
                    
                    // 解析日期
                    if (rulingNode.has("rulingDate")) {
                        String dateStr = rulingNode.get("rulingDate").asText();
                        record.setCaseDate(parseDate(dateStr));
                    }
                    
                    // 解析HS编码 - 处理所有HS编码
                    if (rulingNode.has("tariffs") && rulingNode.get("tariffs").isArray()) {
                        com.fasterxml.jackson.databind.JsonNode tariffsNode = rulingNode.get("tariffs");
                        if (tariffsNode.size() > 0) {
                            // 收集所有HS编码
                            List<String> hsCodes = new ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode tariffNode : tariffsNode) {
                                String hsCode = tariffNode.asText();
                                if (hsCode != null && !hsCode.trim().isEmpty()) {
                                    hsCodes.add(hsCode.trim());
                                }
                            }
                            
                            // 如果有HS编码，用逗号连接保存所有编码
                            if (!hsCodes.isEmpty()) {
                                if (hsCodes.size() == 1) {
                                    record.setHsCodeUsed(hsCodes.get(0));
                                } else {
                                    record.setHsCodeUsed(String.join(", ", hsCodes));
                                    log.debug("发现多个HS编码: {}", String.join(", ", hsCodes));
                                }
                            }
                        }
                    }
                    
                    // 解析标题/结果
                    if (rulingNode.has("subject")) {
                        record.setRulingResult(rulingNode.get("subject").asText());
                    }
                    
                    // 解析案例编号
                    if (rulingNode.has("rulingNumber")) {
                        record.setCaseNumber(rulingNode.get("rulingNumber").asText());
                    }
                    
                    // 设置固定字段
                    record.setViolationType("归类裁定");
                    record.setPenaltyAmount(BigDecimal.ZERO);
                    record.setDataSource("U.S. Customs and Border Protection Securing America's Borders");
                    record.setJdCountry("US");
                    record.setCrawlTime(LocalDateTime.now());
                    record.setDataStatus("ACTIVE");
                    
                    records.add(record);
                }
            }
            
        } catch (Exception e) {
            log.error("解析API响应时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        return records;
    }

    /**
     * 主方法 - 用于测试CBP爬虫
     */
    public static void main(String[] args) {
        System.out.println("=== CBP Customs Case Crawler 测试 (HttpClient版本) ===");
        
        try {
            // 创建爬虫实例（注意：这里不能使用@Autowired，所以需要手动创建）
            CustomsCaseCrawler crawler = new CustomsCaseCrawler();
            
            // 检查命令行参数
            if (args.length > 0 && "keywords".equals(args[0])) {
                // 关键词测试模式
                System.out.println("模式: 关键词列表爬取测试");
                
                String keywordsStr = args.length > 1 ? args[1] : "9018,9021,9022";
                String[] keywordArray = keywordsStr.split(",");
                List<String> keywords = Arrays.asList(keywordArray);
                
                System.out.println("测试参数:");
                System.out.println("- 关键词列表: " + keywords);
                System.out.println("- 最大记录数: 5");
                System.out.println("- 批次大小: 10");
                System.out.println();
                
                // 测试HttpClient初始化
                System.out.println("正在测试HttpClient初始化...");
                crawler.initHttpClient();
                System.out.println("✅ HttpClient初始化成功！");
                
                // 测试关键词爬取
                System.out.println("\n=== 开始测试关键词爬取 ===");
                String result = crawler.crawlWithKeywords(keywords, 5, 10);
                System.out.println("关键词爬取结果: " + result);
                
            } else {
                // 原有测试模式
                String searchTerm = "9018";            // 搜索关键词（根据fetch请求使用HS编码）
                int maxRecords = 5;                    // 最大记录数（减少数量以便快速测试）
                int batchSize = 30;                    // 批次大小（根据fetch请求调整）
                
                System.out.println("测试参数:");
                System.out.println("- 搜索关键词: " + searchTerm);
                System.out.println("- 最大记录数: " + maxRecords);
                System.out.println("- 批次大小: " + batchSize);
                System.out.println();
                
                // 测试HttpClient初始化
                System.out.println("正在测试HttpClient初始化...");
                crawler.initHttpClient();
                System.out.println("✅ HttpClient初始化成功！");
                
                // 测试API爬取
                System.out.println("\n=== 开始测试API爬取 ===");
                List<CustomsCase> results = crawler.crawlAndSaveCustomsCases(searchTerm, maxRecords, batchSize);
                System.out.println("API爬取到 " + results.size() + " 条记录");
                
                // 显示前3条解析结果
                for (int i = 0; i < Math.min(3, results.size()); i++) {
                    CustomsCase record = results.get(i);
                    System.out.println("\n--- 记录 " + (i + 1) + " ---");
                    System.out.println("日期: " + record.getCaseDate());
                    System.out.println("HS编码: " + record.getHsCodeUsed());
                    System.out.println("描述: " + (record.getRulingResult() != null ? 
                        (record.getRulingResult().length() > 100 ? 
                            record.getRulingResult().substring(0, 100) + "..." : 
                            record.getRulingResult()) : "无"));
                    System.out.println("链接: " + record.getCaseNumber());
                    System.out.println("违规类型: " + record.getViolationType());
                    System.out.println("数据源: " + record.getDataSource());
                    System.out.println("国家: " + record.getJdCountry());
                }
                
                if (results.size() > 3) {
                    System.out.println("\n... 还有 " + (results.size() - 3) + " 条记录");
                }
                
                System.out.println("\n✅ API爬取测试完成！");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}
